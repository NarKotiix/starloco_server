package org.starloco.locos.exchange;

import org.starloco.locos.client.Account;
import org.starloco.locos.command.ExecuteCommandPlayer;
import org.starloco.locos.database.Database;
import org.starloco.locos.exchange.transfer.DataQueue;
import org.starloco.locos.exchange.transfer.DataQueue.Queue;
import org.starloco.locos.game.GameServer;
import org.starloco.locos.game.world.World;
import org.starloco.locos.kernel.Main;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ExchangePacketHandler {

    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");
    private static final String MSG_PREFIX_COLOR = "#C35617";

    public static void parser(String recv) {
        if (recv == null || recv.isEmpty()) return;

        for (String packet : recv.split("#")) {
            if (packet.isEmpty()) continue;
            try {
                handlePacket(packet);
            } catch (Exception e) {
                ExchangeClient.logger.error("Error parsing packet: " + packet, e);
            }
        }
    }

    private static void handlePacket(String packet) {
        if (packet.length() < 2) return;

        switch (packet.charAt(0)) {
            case 'F': handleFreePlaces(packet);  break;
            case 'S': handleServer(packet);      break;
            case 'W': handleWaiting(packet);     break;
            case 'D': handleData(packet);        break;
        }
    }

    // F? — Free places
    private static void handleFreePlaces(String packet) {
        if (packet.charAt(1) == '?') {
            int free = GameServer.MAX_PLAYERS - World.world.getOnlinePlayers().size();
            Main.exchangeClient.send("F" + free);
        }
    }

    // S — Server handshake
    private static void handleServer(String packet) {
        if (packet.length() < 3) return;
        switch (packet.charAt(1)) {
            case 'H': // Host
                if (packet.charAt(2) == 'K') {
                    ExchangeClient.logger.info("The login server has validated the connection.");
                    GameServer.setState(1);
                }
                break;

            case 'K': // Key
                switch (packet.charAt(2)) {
                    case '?':
                        int slots = 50000 - Main.gameServer.getClients().size();
                        Main.exchangeClient.send("SK" + Main.serverId + ";" + Main.key + ";" + slots);
                        break;
                    case 'K':
                        ExchangeClient.logger.info("The login server has accepted the connection.");
                        Main.exchangeClient.send("SH" + Main.Ip + ";" + Main.gamePort);
                        break;
                    case 'R':
                        ExchangeClient.logger.info("The login server has refused the connection.");
                        Main.stop("Connection refused by the login");
                        break;
                }
                break;
        }
    }

    // W — Waiting accounts
    private static void handleWaiting(String packet) {
        switch (packet.charAt(1)) {
            case 'A': // Add
                String[] parts = packet.split(";");
                if (parts.length < 2) return;

                int id = Integer.parseInt(parts[0].substring(2));
                Account account = World.world.getAccount(id);

                if (account == null) {
                    Database.getStatics().getAccountData().load(id);
                    account = World.world.getAccount(id);
                }

                if (account != null) {
                    if (account.getCurrentPlayer() != null)
                        account.getGameClient().kick();
                    Main.gameServer.addWaitingAccount(account);
                    account.setClientVersion(parts[1]);
                }
                break;

            case 'K': // Kick
                int kickId = Integer.parseInt(packet.substring(2));
                Database.getStatics().getPlayerData().updateAllLogged(kickId, 0);
                Database.getStatics().getAccountData().setLogged(kickId, 0);
                Account toKick = World.world.getAccount(kickId);
                if (toKick != null && toKick.getGameClient() != null)
                    toKick.getGameClient().kick();
                break;
        }
    }

    // D — Data
    private static void handleData(String packet) {
        if (packet.length() < 3) return;
        switch (packet.charAt(1)) {
            case 'I': // Id
                for (String data : packet.substring(2).split("DI")) {
                    if (data.isEmpty()) continue;
                    String[] split = data.split(";");
                    long count = Long.parseLong(split[0].substring(1));
                    Queue<?> queue = DataQueue.queues.get(count);
                    if (queue == null) continue;

                    if (data.charAt(0) == '1' && split.length > 1) { // Player
                        ((Queue<Integer>) queue).setValue(Integer.parseInt(split[1]));
                    }
                }
                break;

            case 'M': // Message
                String[] split = packet.substring(2).split("\\|");
                if (split.length < 3) return;

                String time = TIME_FORMAT.format(new Date());
                String prefix = "<font color='" + MSG_PREFIX_COLOR + "'>[" + time + "] ("
                        + ExecuteCommandPlayer.canal + ") (" + split[1] + ") <b>" + split[0] + "</b>";
                String message = "Im116;" + prefix + "~" + split[2] + "</font>";
                String finalMessage = message.replace("%20", " ");

                World.world.getOnlinePlayers().stream()
                        .filter(p -> p != null && !p.noall)
                        .forEach(p -> p.send(finalMessage));
                break;
        }
    }
}