package org.starloco.locos.kernel;

import java.io.*;
import java.util.Properties;

public class Config {

    private static final String CONFIG_FILE = "config.properties";
    public static final Config singleton = new Config();

    public final long startTime = System.currentTimeMillis();

    // Events
    public boolean HALLOWEEN = false, NOEL = false, HEROIC = false;

    // Server info
    public String NAME, url, startMessage = "", colorMessage = "B9121B";

    // Gameplay rules
    public boolean autoReboot = true, allZaap = false, allEmote = false, onlyLocal = false, prestige = false;
    public boolean mobGroupMovement = true;
    public boolean npcMovement = true;
    public float playerMoveSpeedMultiplier = 1.0f;
    public int startMap = 0, startCell = 0;

    // Rates
    public int rateKamas = 1, rateDrop = 1, rateHonor = 1, rateJob = 1, rateFm = 1;
    public float rateXp = 1;
    public int erosion = 10;

    // Start items/pano
    public static int[] START_PANO = new int[0];
    public static int[] START_ITEM = new int[0];

    // AI delays (ms)
    public int AIDelay = 100, AIMovementCellDelay = 180, AIMovementFlatDelay = 700;

    public static Config getInstance() {
        return singleton;
    }

    public void load() {
        File configFile = new File(CONFIG_FILE);
        if (!configFile.exists()) {
            generateDefaultConfigFile();
            return;
        }

        Properties props = new Properties();
        try (InputStream in = new FileInputStream(configFile)) {
            props.load(in);
        } catch (IOException e) {
            Main.logger.error("Failed to read " + CONFIG_FILE, e);
            return;
        }

        applyConnection(props);
        applyDatabase(props);
        applyRules(props);
        applyConfiguration(props);
        applyRates(props);
    }

    // -------------------------------------------------------------------------
    // Sections d'application des propriétés
    // -------------------------------------------------------------------------

    private void applyConnection(Properties p) {
        Main.Ip           = get(p,  "IP",            Main.Ip);
        Main.gamePort     = getInt(p, "GAME_PORT",   Main.gamePort);
        Main.exchangePort = getInt(p, "EXCHANGE_PORT", Main.exchangePort);
        Main.exchangeIp   = get(p,  "EXCHANGE_IP",  Main.exchangeIp);
    }

    private void applyDatabase(Properties p) {
        Main.loginHostDB = get(p, "LOGIN_IP_DB",   Main.loginHostDB);
        Main.loginNameDB = get(p, "LOGIN_NAME_DB", Main.loginNameDB);
        Main.loginUserDB = get(p, "LOGIN_USER_DB", Main.loginUserDB);
        Main.loginPassDB = get(p, "LOGIN_PASS_DB", Main.loginPassDB);
        Main.loginPortDB = get(p, "LOGIN_PORT_DB", Main.loginPortDB);

        Main.hostDB = get(p, "GAME_IP_DB",   Main.hostDB);
        Main.nameDB = get(p, "GAME_NAME_DB", Main.nameDB);
        Main.userDB = get(p, "GAME_USER_DB", Main.userDB);
        Main.passDB = get(p, "GAME_PASS_DB", Main.passDB);
        Main.portDB = get(p, "GAME_PORT_DB", Main.portDB);

        Main.dbConnectTimeoutMs = getInt(p, "DB_CONNECT_TIMEOUT_MS", Main.dbConnectTimeoutMs);
        Main.dbSocketTimeoutMs = getInt(p, "DB_SOCKET_TIMEOUT_MS", Main.dbSocketTimeoutMs);
        Main.dbIdleTimeoutMs = getInt(p, "DB_IDLE_TIMEOUT_MS", Main.dbIdleTimeoutMs);
        Main.dbMaxLifetimeMs = getInt(p, "DB_MAX_LIFETIME_MS", Main.dbMaxLifetimeMs);
        Main.dbMaxPoolSize = getInt(p, "DB_POOL_MAX_SIZE", Main.dbMaxPoolSize);
        Main.dbMinIdle = getInt(p, "DB_POOL_MIN_IDLE", Main.dbMinIdle);
        Main.worldLoadParallelism = getInt(p, "WORLD_LOAD_PARALLELISM", Main.worldLoadParallelism);
    }

    private void applyRules(Properties p) {
        Main.startLevel      = getInt(p,   "START_LEVEL",      Main.startLevel);
        Main.startKamas      = getInt(p,   "START_KAMAS",      Main.startKamas);
        Main.useSubscribe    = getBool(p,  "SUBSCRIBER",       Main.useSubscribe);
        this.prestige        = getBool(p,  "ALLOW_PRESTIGE",   this.prestige);
        this.allZaap         = getBool(p,  "ALL_ZAAP",         this.allZaap);
        this.allEmote        = getBool(p,  "ALL_EMOTE",        this.allEmote);
        this.NOEL            = getBool(p,  "NOEL",             this.NOEL);
        this.HALLOWEEN       = getBool(p,  "HALLOWEEN",        this.HALLOWEEN);
        this.HEROIC          = getBool(p,  "HEROIC",           this.HEROIC);
        Main.allowMulePvp    = getBool(p,  "ALLOW_MULE_PVP",   Main.allowMulePvp);
        Main.mapAsBlocked    = getBool(p,  "MAP_AS_BLOCKED",   Main.mapAsBlocked);
        Main.fightAsBlocked  = getBool(p,  "FIGHT_AS_BLOCKED", Main.fightAsBlocked);
        Main.tradeAsBlocked  = getBool(p,  "TRADE_AS_BLOCKED", Main.tradeAsBlocked);
        this.mobGroupMovement = getBool(p, "MOB_GROUP_MOVEMENT", this.mobGroupMovement);
        this.npcMovement = getBool(p, "NPC_MOVEMENT", this.npcMovement);
        this.playerMoveSpeedMultiplier = Math.max(1.0f, getFloat(p, "PLAYER_MOVE_SPEED_MULTIPLIER", this.playerMoveSpeedMultiplier));

        String startPlayer = getSanitized(p, "START_PLAYER");
        if (startPlayer != null) {
            try {
                String[] parts = startPlayer.split(",");
                this.startMap  = Integer.parseInt(parts[0].trim());
                this.startCell = Integer.parseInt(parts[1].trim());
            } catch (Exception ignored) {}
        }

        String startPano = getSanitized(p, "START_PANO");
        if (startPano != null && !startPano.isEmpty()) {
            try { START_PANO = parseIntArray(startPano.split(";")); }
            catch (NumberFormatException e) { e.printStackTrace(); }
        }

        String startItem = getSanitized(p, "START_ITEM");
        if (startItem != null) {
            try { START_ITEM = startItem.isEmpty() ? new int[]{} : parseIntArray(startItem.split(";")); }
            catch (NumberFormatException e) { e.printStackTrace(); }
        }
    }

    private void applyConfiguration(Properties p) {
        this.startMessage = get(p,   "MESSAGE",     this.startMessage);
        this.url          = get(p,   "URL",         this.url);
        this.NAME         = get(p,   "NAME",        this.NAME);
        this.autoReboot   = getBool(p, "AUTO_REBOOT", this.autoReboot);
        Main.serverId     = getInt(p,  "SERVER_ID",  Main.serverId);
        Main.key          = get(p,   "SERVER_KEY",  Main.key);
        Main.modDebug     = getBool(p, "DEBUG",      Main.modDebug);
        Logging.USE_LOG   = getBool(p, "USE_LOG",    Logging.USE_LOG);
    }

    private void applyRates(Properties p) {
        this.rateXp    = getFloat(p, "RATE_XP",    this.rateXp);
        this.rateDrop  = getInt(p,   "RATE_DROP",  this.rateDrop);
        this.rateJob   = getInt(p,   "RATE_JOB",   this.rateJob);
        this.rateKamas = getInt(p,   "RATE_KAMAS", this.rateKamas);
        this.rateFm    = getInt(p,   "RATE_FM",    this.rateFm);
    }

    // -------------------------------------------------------------------------
    // Génération du fichier par défaut
    // -------------------------------------------------------------------------
    private void generateDefaultConfigFile() {
        Properties props = new Properties();

        // Connexion
        props.setProperty("IP",            "127.0.0.1");
        props.setProperty("GAME_PORT",     "5555");
        props.setProperty("EXCHANGE_IP",   "127.0.0.1");
        props.setProperty("EXCHANGE_PORT", "666");

        // BDD Login
        props.setProperty("LOGIN_IP_DB",   "127.0.0.1");
        props.setProperty("LOGIN_NAME_DB", "login");
        props.setProperty("LOGIN_USER_DB", "root");
        props.setProperty("LOGIN_PASS_DB", "");
        props.setProperty("LOGIN_PORT_DB", "3306");

        // BDD Game
        props.setProperty("GAME_IP_DB",   "127.0.0.1");
        props.setProperty("GAME_NAME_DB", "game");
        props.setProperty("GAME_USER_DB", "root");
        props.setProperty("GAME_PASS_DB", "");
        props.setProperty("GAME_PORT_DB", "3306");

        // Tuning DB / startup fail-fast
        props.setProperty("DB_CONNECT_TIMEOUT_MS", "5000");
        props.setProperty("DB_SOCKET_TIMEOUT_MS", "10000");
        props.setProperty("DB_IDLE_TIMEOUT_MS", "300000");
        props.setProperty("DB_MAX_LIFETIME_MS", "1800000");
        props.setProperty("DB_POOL_MAX_SIZE", "10");
        props.setProperty("DB_POOL_MIN_IDLE", "0");
        props.setProperty("WORLD_LOAD_PARALLELISM", "0");

        // Règles / flags
        props.setProperty("SUBSCRIBER",       "false");
        props.setProperty("START_LEVEL",      "1");
        props.setProperty("START_KAMAS",      "50000000");
        props.setProperty("START_PLAYER",     "164,298");
        props.setProperty("START_PANO",       "5");
        props.setProperty("START_ITEM",       "");
        props.setProperty("ALL_ZAAP",         "true");
        props.setProperty("ALL_EMOTE",        "true");
        props.setProperty("ALLOW_PRESTIGE",   "false");
        props.setProperty("ALLOW_MULE_PVP",   "false");
        props.setProperty("HEROIC",           "false");
        props.setProperty("NOEL",             "false");
        props.setProperty("HALLOWEEN",        "false");
        props.setProperty("MAP_AS_BLOCKED",   "false");
        props.setProperty("FIGHT_AS_BLOCKED", "false");
        props.setProperty("TRADE_AS_BLOCKED", "false");
        props.setProperty("MOB_GROUP_MOVEMENT", "true");
        props.setProperty("PLAYER_MOVE_SPEED_MULTIPLIER", "2.0");

        // Configuration serveur
        props.setProperty("MESSAGE",        "Bienvenue dans l'émulation FREE d'entraide");
        props.setProperty("URL",            "nashira");
        props.setProperty("NAME",           "Eratz");
        props.setProperty("AUTO_REBOOT",    "true");
        props.setProperty("SERVER_ID",      "601");
        props.setProperty("SERVER_KEY",     "eratz");
        props.setProperty("DEBUG",          "true");
        props.setProperty("USE_LOG",        "true");

        // Rates
        props.setProperty("RATE_XP",    "1");
        props.setProperty("RATE_DROP",  "1");
        props.setProperty("RATE_JOB",   "1");
        props.setProperty("RATE_KAMAS", "1");
        props.setProperty("RATE_FM",    "2");

        try (OutputStream out = new FileOutputStream(CONFIG_FILE)) {
            props.store(out, "Fichier de configuration StarLoco - par Kevin#6537");
            Main.logger.info("Configuration file '" + CONFIG_FILE + "' created with default values.");
        } catch (IOException e) {
            Main.logger.error("Failed to create " + CONFIG_FILE, e);
            return;
        }

        load();
    }

    // -------------------------------------------------------------------------
    // Helpers de lecture sécurisée
    // -------------------------------------------------------------------------

    private String getSanitized(Properties p, String key) {
        String v = p.getProperty(key);
        if (v == null) return null;

        String value = v.trim();
        if (value.contains("#")) {
            value = value.substring(0, value.indexOf('#')).trim();
        }
        return value;
    }

    private String get(Properties p, String key, String defaultValue) {
        String v = p.getProperty(key);
        return (v != null) ? v.trim() : defaultValue;
    }

    private int getInt(Properties p, String key, int defaultValue) {
        String v = getSanitized(p, key);
        if (v == null) return defaultValue;
        try { return Integer.parseInt(v.trim()); }
        catch (NumberFormatException e) { return defaultValue; }
    }

    private float getFloat(Properties p, String key, float defaultValue) {
        String v = getSanitized(p, key);
        if (v == null) return defaultValue;
        try { return Float.parseFloat(v.trim()); }
        catch (NumberFormatException e) { return defaultValue; }
    }

    private boolean getBool(Properties p, String key, boolean defaultValue) {
        String v = getSanitized(p, key);
        if (v == null) return defaultValue;

        String value = v.trim().toLowerCase();

        if (value.equals("true") || value.equals("yes") || value.equals("oui") || value.equals("1"))
            return true;
        if (value.equals("false") || value.equals("no") || value.equals("non") || value.equals("0"))
            return false;
        return defaultValue;
    }

    private int[] parseIntArray(String[] parts) throws NumberFormatException {
        int[] result = new int[parts.length];
        for (int i = 0; i < parts.length; i++)
            result[i] = Integer.parseInt(parts[i].trim());
        return result;
    }
}