package org.starloco.locos.client;

import org.starloco.locos.command.administration.Group;
import org.starloco.locos.common.SocketManager;
import org.starloco.locos.database.Database;
import org.starloco.locos.game.GameClient;
import org.starloco.locos.game.world.World;
import org.starloco.locos.hdv.HdvEntry;
import org.starloco.locos.kernel.Main;
import org.starloco.locos.object.GameObject;

import java.util.*;

public class Account {

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------
    private int    id;
    private String name;
    private String pseudo;
    private String answer;
    private String currentIp          = "";
    private String lastIP             = "";
    private String lastConnectionDate = "";
    private String lastVoteIP;
    private String mutePseudo         = "";
    private String switchPacketKey;
    private String clientVersion;

    private int     points;
    private long    muteTime    = 0L;
    private long    subscriber  = 1L;
    private long    bankKamas   = 0L;
    private long    heureVote   = 0L;
    private boolean banned      = false;
    private boolean vip         = false;
    private byte    state;

    private Player     currentPlayer;
    private GameClient gameClient;

    private final List<GameObject>              bank       = new ArrayList<>();
    private final List<Integer>                 friends    = new ArrayList<>();
    private final List<Integer>                 enemies    = new ArrayList<>();
    private final Map<Integer, Player>          players    = new HashMap<>();
    private       Map<Integer, ArrayList<HdvEntry>> hdvsItems;

    private static final String VERSION_CONSOLE_THRESHOLD = "1.35.0";

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------
    public Account(int guid, String name, String pseudo,
                   String answer, boolean banned,
                   String lastIp, String lastConnectionDate, String friends,
                   String enemy, int points, long subscriber, long muteTime, String mutePseudo,
                   String lastVoteIP, String heureVote, boolean vip, String switchPacketKey) {
        this.id                 = guid;
        this.name               = name;
        this.pseudo             = pseudo;
        this.answer             = answer;
        this.banned             = banned;
        this.lastIP             = lastIp;
        this.lastConnectionDate = lastConnectionDate;
        this.hdvsItems          = World.world.getMyItems(guid);
        this.points             = points;
        this.subscriber         = subscriber;
        this.muteTime           = muteTime;
        this.mutePseudo         = mutePseudo;
        this.lastVoteIP         = lastVoteIP;
        this.vip                = vip;
        this.switchPacketKey    = switchPacketKey;

        this.heureVote = heureVote.isEmpty() ? 0L : Long.parseLong(heureVote);

        loadFriends(friends);
        loadEnemies(enemy);
        loadBank(guid);

        if (!Database.getDynamics().getGiftData().existByAccount(guid))
            Database.getDynamics().getGiftData().create(guid);
    }

    // -------------------------------------------------------------------------
    // Constructor helpers
    // -------------------------------------------------------------------------
    private void loadFriends(String data) {
        if (data.isEmpty()) return;
        for (String f : data.split(";")) {
            try { this.friends.add(Integer.parseInt(f)); }
            catch (NumberFormatException e) { e.printStackTrace(); }
        }
    }

    private void loadEnemies(String data) {
        if (data.isEmpty()) return;
        for (String e : data.split(";")) {
            try { this.enemies.add(Integer.parseInt(e)); }
            catch (NumberFormatException e1) { e1.printStackTrace(); }
        }
    }

    private void loadBank(int guid) {
        String bank = Database.getDynamics().getBankData().get(guid);
        if (bank == null) {
            Database.getDynamics().getBankData().add(guid);
            return;
        }
        String[] parts = bank.split("@", 2);
        this.bankKamas = Long.parseLong(parts[0]); // était parseInt — risque de troncature
        if (parts.length < 2 || parts[1].isEmpty()) return;

        for (String item : parts[1].split("\\|")) {
            if (item.isEmpty()) continue;
            GameObject obj = World.world.getGameObject(Integer.parseInt(item));
            if (obj != null) this.bank.add(obj);
        }
    }

    // -------------------------------------------------------------------------
    // Identity
    // -------------------------------------------------------------------------
    public int    getId()      { return id; }
    public void   setId(int i) { id = i; }

    public String getName()         { return name; }
    public void   setName(String s) { name = s; }

    public String getPseudo()  { return pseudo; }
    public String getAnswer()  { return answer; }

    public String getCurrentIp()         { return currentIp; }
    public void   setCurrentIp(String s) { currentIp = s; }

    public String getLastIP()         { return lastIP; }
    public void   setLastIP(String s) { lastIP = s; }

    public String getLastConnectionDate()         { return lastConnectionDate; }
    public void   setLastConnectionDate(String s) { lastConnectionDate = s; }

    public String getLastVoteIP() { return lastVoteIP; }
    public long   getHeureVote()  { return heureVote; }

    public void updateVote(String hour, String ip) {
        this.heureVote  = hour.isEmpty() ? 0L : Long.parseLong(hour);
        this.lastVoteIP = ip;
    }

    // -------------------------------------------------------------------------
    // Points
    // -------------------------------------------------------------------------
    public int getPoints() {
        points = Database.getStatics().getAccountData().loadPoints(name);
        return points;
    }

    public void setPoints(int i) {
        points = i;
        Database.getStatics().getAccountData().updatePoints(id, points);
    }

    // -------------------------------------------------------------------------
    // Mute
    // -------------------------------------------------------------------------
    public void mute(long time, String pseudo) {
        if (time <= 0) return;
        muteTime    = time;
        mutePseudo  = pseudo;
        Database.getStatics().getAccountData().update(this);
        long timeMuted = (time - System.currentTimeMillis()) / 60000;
        if (currentPlayer != null)
            currentPlayer.send("Im117;" + pseudo + "~" + timeMuted);
    }

    public void unMute() {
        if (muteTime == 0) return;
        muteTime   = 0L;
        mutePseudo = "";
        Database.getStatics().getAccountData().update(this);
    }

    public boolean isMuted() {
        if (muteTime == 0) return false;
        if (muteTime > System.currentTimeMillis()) return true;
        // mute expiré — nettoyage automatique
        muteTime   = 0L;
        mutePseudo = "";
        Database.getStatics().getAccountData().update(this);
        return false;
    }

    public long   getMuteTime()   { return isMuted() ? muteTime   : 0L; }
    public String getMutePseudo() { return isMuted() ? mutePseudo : ""; }

    // -------------------------------------------------------------------------
    // Bank
    // -------------------------------------------------------------------------
    public List<GameObject> getBank() { return bank; }

    public String parseBankObjectsToDB() {
        if (bank.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (GameObject obj : bank)
            sb.append(obj.getGuid()).append('|');
        return sb.toString();
    }

    public long getBankKamas()      { return bankKamas; }
    public void setBankKamas(long i) {
        bankKamas = i;
        Database.getDynamics().getBankData().update(this);
    }

    // -------------------------------------------------------------------------
    // Players / session
    // -------------------------------------------------------------------------
    public GameClient getGameClient()         { return gameClient; }
    public void       setGameClient(GameClient t) { gameClient = t; }

    public void             addPlayer(Player p)    { players.put(p.getId(), p); }
    public Map<Integer, Player> getPlayers()       { return players; }

    public Player getCurrentPlayer()            { return currentPlayer; }
    public void   setCurrentPlayer(Player p)    { currentPlayer = p; }

    public boolean isBanned()             { return banned; }
    public void    setBanned(boolean b)   { banned = b; }

    public boolean isOnline() { return gameClient != null; }

    public void setState(int s) {
        state = (byte) s;
        Database.getStatics().getAccountData().update(this);
    }
    public byte getState() { return state; }

    // -------------------------------------------------------------------------
    // Subscription
    // -------------------------------------------------------------------------
    public long getSubscribeRemaining() {
        if (!Main.useSubscribe) return 525600L;
        long remaining = subscriber - System.currentTimeMillis();
        return Math.max(remaining, 0L);
    }

    public boolean isSubscribe() {
        return !Main.useSubscribe || (subscriber - System.currentTimeMillis()) > 0L;
    }

    public boolean isSubscribeWithoutCondition() {
        return (subscriber - System.currentTimeMillis()) > 0L;
    }

    // -------------------------------------------------------------------------
    // Character management
    // -------------------------------------------------------------------------
    public boolean createPlayer(String name, int sexe, int classe,
                                int color1, int color2, int color3) {
        return Player.CREATE_PERSONNAGE(name, sexe, classe, color1, color2, color3, this) != null;
    }

    public void deletePlayer(int guid) {
        Player p = players.get(guid);
        if (p == null) return;
        World.world.removePlayer(p);
        players.remove(guid);
    }

    // -------------------------------------------------------------------------
    // Friends
    // -------------------------------------------------------------------------
    public void sendOnline() {
        for (int fid : friends) {
            Player player = World.world.getPlayer(fid);
            if (player != null && player.is_showFriendConnection()
                    && player.isOnline() && player.getAccount().isFriendWith(id))
                SocketManager.GAME_SEND_FRIEND_ONLINE(currentPlayer, player);
        }
    }

    public void addFriend(int fid) {
        if (id == fid) { SocketManager.GAME_SEND_FA_PACKET(currentPlayer, "Ey"); return; }

        Account account = World.world.getAccount(fid);
        if (account == null) { SocketManager.GAME_SEND_MESSAGE(currentPlayer, "Le compte n'existe pas."); return; }

        Player player = account.getCurrentPlayer();
        if (player == null) { SocketManager.GAME_SEND_MESSAGE(currentPlayer, "Le joueur n'existe pas."); return; }

        Group group = player.getGroupe();
        if (group != null && !group.isPlayer()) {
            SocketManager.GAME_SEND_MESSAGE(currentPlayer, "Impossible d'ajouter un membre du staff en ami.");
            return;
        }

        if (friends.contains(fid)) {
            SocketManager.GAME_SEND_FA_PACKET(currentPlayer, "Ea");
            return;
        }
        friends.add(fid);
        SocketManager.GAME_SEND_FA_PACKET(currentPlayer, "K" + account.getPseudo() + player.parseToFriendList(fid));
        Database.getStatics().getAccountData().update(this);
    }

    public void removeFriend(int fid) {
        friends.removeIf(f -> f == fid);
        Database.getStatics().getAccountData().update(this);
        SocketManager.GAME_SEND_FD_PACKET(currentPlayer, "K");
    }

    public boolean isFriendWith(int fid) { return friends.contains(fid); }

    public String parseFriendListToDB() {
        return buildIdList(friends);
    }

    public String parseFriendList() {
        return buildSocialList(friends, true);
    }

    // -------------------------------------------------------------------------
    // Enemies
    // -------------------------------------------------------------------------
    public void addEnemy(String packet, int guid) {
        if (id == guid) { SocketManager.GAME_SEND_FA_PACKET(currentPlayer, "Ey"); return; }

        if (enemies.contains(guid)) { SocketManager.GAME_SEND_iAEA_PACKET(currentPlayer); return; }

        enemies.add(guid);
        Player pr = World.world.getPlayerByName(packet);
        SocketManager.GAME_SEND_ADD_ENEMY(currentPlayer, pr);
        Database.getStatics().getAccountData().update(this);
    }

    public void removeEnemy(int eid) {
        enemies.removeIf(e -> e == eid);
        Database.getStatics().getAccountData().update(this);
        SocketManager.GAME_SEND_iD_COMMANDE(currentPlayer, "K");
    }

    public boolean isEnemyWith(int eid) { return enemies.contains(eid); }

    public String parseEnemyListToDB() {
        return buildIdList(enemies);
    }

    public String parseEnemyList() {
        return buildSocialList(enemies, false);
    }

    // -------------------------------------------------------------------------
    // Social list helpers (DRY)
    // -------------------------------------------------------------------------
    private static String buildIdList(List<Integer> ids) {
        if (ids.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i : ids) {
            if (sb.length() > 0) sb.append(';');
            sb.append(i);
        }
        return sb.toString();
    }

    private String buildSocialList(List<Integer> ids, boolean isFriendList) {
        if (ids.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i : ids) {
            Account c = World.world.getAccount(i);
            if (c == null) continue;
            sb.append('|').append(c.getPseudo());
            if (!c.isOnline()) continue;
            Player p = c.getCurrentPlayer();
            if (p == null) continue;
            sb.append(isFriendList ? p.parseToFriendList(id) : p.parseToEnemyList(id));
        }
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // HDV
    // -------------------------------------------------------------------------
    public boolean recoverItem(int lineId) {
        if (currentPlayer == null || currentPlayer.getExchangeAction() == null) return false;
        if ((Integer) currentPlayer.getExchangeAction().getValue() >= 0)        return false;

        int hdvId = Math.abs((Integer) currentPlayer.getExchangeAction().getValue());
        ArrayList<HdvEntry> entries = hdvsItems.get(hdvId);
        if (entries == null || entries.isEmpty()) return false;

        HdvEntry entry = null;
        for (HdvEntry e : entries) {
            if (e.getLineId() == lineId) { entry = e; break; }
        }
        if (entry == null || entry.buy) return false;

        entries.remove(entry);
        GameObject obj = entry.getGameObject();
        if (!currentPlayer.addObjet(obj, true))
            World.world.removeGameObject(obj.getGuid());

        Database.getDynamics().getHdvObjectData().delete(obj.getGuid());
        World.world.getHdv(hdvId).delEntry(entry);
        Database.getStatics().getPlayerData().update(currentPlayer);
        return true;
    }

    public HdvEntry[] getHdvEntries(int hdvId) {
        ArrayList<HdvEntry> list = hdvsItems.get(hdvId);
        if (list == null) return new HdvEntry[0]; // tableau vide plutôt que [1] avec null
        return list.toArray(new HdvEntry[0]);
    }

    public int countHdvEntries(int hdvId) {
        ArrayList<HdvEntry> list = hdvsItems.get(hdvId);
        return list == null ? 0 : list.size();
    }

    // -------------------------------------------------------------------------
    // Session lifecycle
    // -------------------------------------------------------------------------
    public void resetAllChars() {
        for (Player player : players.values()) {
            if (player.getFight() != null) {
                if (player.getParty() != null) player.getParty().leave(player);
                player.setOnline(true);
            }
            if (player.getExchangeAction() != null) GameClient.leaveExchange(player);
            if (player.getParty() != null)          player.getParty().leave(player);
            if (player.getCurCell() != null)         player.getCurCell().removePlayer(player);
            if (player.getCurMap() != null && player.isOnline())
                SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(player.getCurMap(), player.getId());
            player.setOnline(false);
        }
    }

    public void disconnect(Player player) {
        player.setChangeName(false);
        Database.getStatics().getAccountData().setLogged(id, 0);
        Database.getStatics().getPlayerData().updateAllLogged(id, 0);
        Database.getStatics().getPlayerData().update(player);

        if (player.getExchangeAction() != null) GameClient.leaveExchange(player);
        if (player.getParty()  != null)         player.getParty().leave(player);
        if (player.getMount()  != null)         Database.getDynamics().getMountData().update(player.getMount());

        if (player.getFight() != null) {
            if (player.getFight().playerDisconnect(player, false)) {
                Database.getStatics().getPlayerData().update(player);
                return;
            }
        }

        currentPlayer = null;
        gameClient    = null;
        currentIp     = "";

        for (Player character : players.values())
            Database.getStatics().getPlayerData().update(character);

        player.resetVars();
        resetAllChars();
        Database.getStatics().getAccountData().update(this);
        World.world.logger.info("The player " + player.getName() + " come to disconnect.");
    }

    // -------------------------------------------------------------------------
    // Client version
    // -------------------------------------------------------------------------
    public String  getClientVersion()             { return clientVersion; }
    public void    setClientVersion(String v)     { clientVersion = v; }

    public boolean requiresConsolePacketAdaptation() {
        return clientVersion != null && clientVersion.compareTo(VERSION_CONSOLE_THRESHOLD) >= 0;
    }

    // -------------------------------------------------------------------------
    // Misc
    // -------------------------------------------------------------------------
    public boolean isVip()             { return vip; }
    public String  getSwitchPacketKey() { return switchPacketKey; }
    public void    setSwitchPacketKey(String k) { switchPacketKey = k; }
}