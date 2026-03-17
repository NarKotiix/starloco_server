package org.starloco.locos.exchange;

import org.starloco.locos.kernel.Main;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;

public class ExchangeHandler extends IoHandlerAdapter {

    @Override
    public void sessionCreated(IoSession arg0) throws Exception {
        Main.exchangeClient.setIoSession(arg0);
    }

    @Override
    public void messageReceived(IoSession arg0, Object arg1) throws Exception {
        String packet = ioBufferToString(arg1);
        if (shouldLogExchangePacket(packet)) {
            ExchangeClient.logger.info(formatExchangePacketLog("RECV", packet));
        }
        ExchangePacketHandler.parser(packet);
    }

    @Override
    public void messageSent(IoSession arg0, Object arg1) throws Exception {
        String packet = ioBufferToString(arg1);
        if (shouldLogExchangePacket(packet)) {
            ExchangeClient.logger.info(formatExchangePacketLog("SEND", packet));
        }
    }

    @Override
    public void sessionClosed(IoSession arg0) throws Exception {
        Main.exchangeClient.restart();
    }

    @Override
    public void exceptionCaught(IoSession arg0, Throwable arg1) throws Exception {
        ExchangeClient.logger.error("Erreur Exchange sur session {}", arg0 != null ? arg0.getId() : "unknown", arg1);
    }

    public static String ioBufferToString(Object o) {
        IoBuffer ioBuffer = IoBuffer.allocate(((IoBuffer) o).capacity());
        ioBuffer.put((IoBuffer) o);
        ioBuffer.flip();

        try {
            return ioBuffer.getString(StandardCharsets.UTF_8.newDecoder());
        } catch (CharacterCodingException e) {
            ExchangeClient.logger.error("Impossible de decoder un paquet Exchange en UTF-8", e);
        }
        return "undefined";
    }

    private static String formatExchangePacketLog(String direction, String packet) {
        String raw = packet == null ? "" : packet;
        String normalized = raw.endsWith("#") ? raw.substring(0, raw.length() - 1) : raw;

        if (normalized.isEmpty()) {
            return "[EXCHANGE " + direction + "] Paquet vide";
        }

        // Plusieurs paquets peuvent arriver concaténés, séparés par '#'.
        if (normalized.contains("#")) {
            StringBuilder sb = new StringBuilder("[EXCHANGE ").append(direction).append("] ");
            boolean first = true;
            for (String part : normalized.split("#")) {
                if (part.isEmpty()) {
                    continue;
                }
                if (!first) {
                    sb.append(" | ");
                }
                sb.append(describePacket(part));
                first = false;
            }
            return sb.toString();
        }

        return "[EXCHANGE " + direction + "] " + describePacket(normalized);
    }

    private static String describePacket(String normalized) {
        if ("F?".equals(normalized)) {
            return "Demande de places libres (F?#)";
        }

        if (normalized.matches("^F\\d+$")) {
            return "Reponse nombre de places libres (" + normalized + ")";
        }

        if ("SHK".equals(normalized)) {
            return "Validation de connexion par le login server (SHK)";
        }

        if ("SK?".equals(normalized)) {
            return "Demande d'authentification du game server (SK?)";
        }

        if ("SKK".equals(normalized)) {
            return "Connexion game server acceptee (SKK)";
        }

        if ("SKR".equals(normalized)) {
            return "Connexion game server refusee (SKR)";
        }

        if (normalized.startsWith("SH")) {
            return "Annonce host/port du game server (" + normalized + ")";
        }

        if (normalized.startsWith("WA")) {
            return "Ajout d'un compte en attente de connexion jeu (" + normalized + ")";
        }

        if (normalized.startsWith("WK")) {
            return "Kick d'un compte depuis le login server (" + normalized + ")";
        }

        if (normalized.startsWith("DI")) {
            return "Reponse de donnees inter-serveurs (" + normalized + ")";
        }

        if (normalized.startsWith("DM")) {
            return "Message global inter-serveurs (" + normalized + ")";
        }

        return "Paquet exchange non mappe (" + normalized + ")";
    }

    private static boolean shouldLogExchangePacket(String packet) {
        String raw = packet == null ? "" : packet;
        String normalized = raw.endsWith("#") ? raw.substring(0, raw.length() - 1) : raw;

        if (normalized.isEmpty()) {
            return true;
        }

        String[] parts = normalized.split("#");
        boolean hasNonEmptyPart = false;

        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }

            hasNonEmptyPart = true;
            if (!("F?".equals(part) || part.matches("^F\\d+$"))) {
                return true;
            }
        }

        // Si tous les paquets sont des heartbeats F? / F<nombre>, on ne log pas.
        return !hasNonEmptyPart;
    }
}
