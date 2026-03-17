package org.starloco.locos.kernel;

import org.fusesource.jansi.AnsiConsole;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Logger;

import org.starloco.locos.area.map.GameMap;
import org.starloco.locos.area.map.entity.InteractiveObject;
import org.starloco.locos.database.Database;
import org.starloco.locos.entity.mount.Mount;
import org.starloco.locos.exchange.ExchangeClient;
import org.starloco.locos.game.GameServer;
import org.starloco.locos.game.scheduler.entity.WorldPub;
import org.starloco.locos.game.scheduler.entity.WorldPlayerOption;
import org.starloco.locos.game.scheduler.entity.WorldSave;
import org.starloco.locos.game.world.World;

import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Main {
    public static Logger logger = (Logger) LoggerFactory.getLogger(Main.class);
    public static final List<Runnable> runnables = new LinkedList<>();

    // Flags runtime
    public static boolean isRunning = false, isSaving = false;
    public static boolean modDebug = false;
    public static boolean allowMulePvp = false, useSubscribe = false;
    public static boolean mapAsBlocked = false, fightAsBlocked = false, tradeAsBlocked = false;

    // Règles gameplay
    public static int startLevel = 1, startKamas = 0;

    // Exchange
    public static String key = "";
    public static int serverId = 0, exchangePort = 0;
    public static String exchangeIp = "127.0.0.1";
    public static String loginHostDB = "127.0.0.1", loginNameDB = "", loginUserDB = "", loginPassDB = "", loginPortDB = "3306";

    // Game
    public static int gamePort = 0;
    public static String hostDB = "127.0.0.1", nameDB = "", userDB = "", passDB = "", portDB = "3306";
    public static String Ip = "127.0.0.1";

    // Database tuning (startup/fail-fast)
    public static int dbConnectTimeoutMs = 5000;
    public static int dbSocketTimeoutMs = 10000;
    public static int dbIdleTimeoutMs = 300000;
    public static int dbMaxLifetimeMs = 1800000;
    public static int dbMaxPoolSize = 10;
    public static int dbMinIdle = 0;
    public static int worldLoadParallelism = 0;

    public static GameServer gameServer;
    public static ExchangeClient exchangeClient;

    public static void main(String[] args) throws SQLException {
        // Forcer UTF-8 pour les entrées/sorties
        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("sun.jnu.encoding", "UTF-8");
        System.setProperty("stdout.encoding", "UTF-8");
        System.setProperty("stderr.encoding", "UTF-8");
        
        // Initialiser Jansi pour les couleurs ANSI sur Windows
        AnsiConsole.systemInstall();

        // Hook d'arrêt gracieux : CTRL+C -> Sauvegarde des données
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (Main.isRunning) {
                Main.stop("Shutdown hook triggered");
            }
        }, "ShutdownHook"));

        try {
            // Configurer les streams en UTF-8 avec support des couleurs
            System.setOut(new PrintStream(System.out, true, "UTF-8"));
            System.setErr(new PrintStream(System.err, true, "UTF-8"));
            
            if (!new File("Logs/Error").exists()) new File("Logs/Error").mkdir();
            System.setErr(new PrintStream(Files.newOutputStream(
                    Paths.get("Logs/Error/" + new SimpleDateFormat("dd-MM-yyyy - HH-mm-ss", Locale.FRANCE)
                            .format(new Date()) + ".log"))));
        } catch (Exception e) {
            e.printStackTrace();
        }

        Main.start();
    }

    public static void start() {
        Main.setTitle("StarLoco - Loading data..");
        Main.logger.info("You use " + System.getProperty("java.vendor") + " with the version " + System.getProperty("java.version"));
        Main.logger.debug("Starting of the server : " + new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss", Locale.FRANCE).format(new Date()));
        Main.logger.debug("Current timestamp : " + System.currentTimeMillis());

        try {
            Main.logger.info("Init config & logging...");
            Config.getInstance().load();
            Logging.getInstance().initialize();
            Main.logger.info("Config & logging initialized.");
        } catch (Exception e) {
            Main.logger.error("Fatal error during config/logging init", e);
            return;
        }

        try {
            Main.logger.info("Launching database...");
            if (Database.launchDatabase()) {
                Main.logger.info("Database.launchDatabase OK, creating world...");
                Main.isRunning = true;
                World.world.createWorld();
                Main.logger.info("World created, initializing GameServer...");

                gameServer = new GameServer();
                gameServer.initialize();
                Main.logger.info("GameServer initialized, initializing ExchangeClient...");

                exchangeClient = new ExchangeClient();
                exchangeClient.initialize();
                Main.logger.info("ExchangeClient initialized, entering main loop...");

                Main.refreshTitle();
                Main.logger.info("The server is ready ! Waiting for connection..\n");

                // Les niveaux de log sont gérés par logback.xml
                // Ne pas surcharger les paramètres ici

                while (Main.isRunning) {
                    try {
                        WorldSave.updatable.update();
                        GameMap.updatable.update();
                        InteractiveObject.updatable.update();
                        Mount.updatable.update();
                        WorldPlayerOption.updatable.update();
                        WorldPub.updatable.update();

                        if (!Main.runnables.isEmpty()) {
                            for (Runnable runnable : new LinkedList<>(Main.runnables)) {
                                try {
                                    if (runnable != null) {
                                        runnable.run();
                                        Main.runnables.remove(runnable);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        Thread.sleep(100);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // Si on sort de la boucle sans passer par stop()
                Main.logger.info("Main loop ended (isRunning = " + Main.isRunning + ")");
            } else {
                Main.logger.error("An error occurred when the server have try a connection on the Mysql server. Please check your identification.");
            }
        } catch (Exception e) {
            Main.logger.error("Fatal error during server startup", e);
        }
    }

    public static void stop(String reason) {
        try {
            logger.warn("═══════════════════════════════════════════════════════════");
            logger.warn("  SERVER SHUTDOWN INITIATED - " + reason);
            logger.warn("═══════════════════════════════════════════════════════════");

            isRunning = false;

            // === ÉTAPE 1 : Arrêter les nouvelles connexions
            logger.info("Step 1/4 - Stopping new connections...");
            GameServer.setState(0);
            
            // === ÉTAPE 2 : Sauvegarder les données du monde
            logger.info("Step 2/4 - Saving world data (players, objects, mounts, etc)...");
            World.world.saveMobGroupStarsSnapshot();
            WorldSave.cast(0);
            GameServer.setState(0);

            // === ÉTAPE 3 : Déconnecter les joueurs
            logger.info("Step 3/4 - Disconnecting all players and saving their data...");
            if (gameServer != null) {
                gameServer.kickAll(true);  // true = avec sauvegarde
            }

            // === ÉTAPE 4 : Fermer les bases de données proprement
            logger.info("Step 4/4 - Closing database connections...");
            Logging.getInstance().stop();
            Database.getStatics().getServerData().loggedZero();
            
            logger.warn("═══════════════════════════════════════════════════════════");
            logger.warn("  ✅ SERVER SHUTDOWN COMPLETE - All data saved");
            logger.warn("═══════════════════════════════════════════════════════════");
            
        } catch (Exception e) {
            logger.error("❌ Error during server shutdown", e);
            e.printStackTrace();
        } finally {
            logger.info("The server is now closed.");
            
            // Nettoyage Jansi
            try {
                AnsiConsole.systemUninstall();
                System.out.flush();
                System.err.flush();
                System.out.println("\r\n🎉 Serveur arrêté !");
            } catch (Exception ignored) {}
            
            // 🚨 KILL BRUTAL : Force tous threads non-daemon
            Thread[] threads = new Thread[Thread.activeCount() + 10];
            int count = Thread.enumerate(threads);
            for (int i = 0; i < count; i++) {
                if (threads[i] != null && !threads[i].isDaemon() && !threads[i].equals(Thread.currentThread())) {
                    try {
                        threads[i].interrupt();
                    } catch (Exception ignored) {}
                }
            }
            
            // EXIT FINAL : halt(0) ignore tous les shutdown hooks restants
            Runtime.getRuntime().halt(0);
        }
    }

    private static void setTitle(String title) {
        AnsiConsole.out.printf("%c]0;%s%c", '\033', title, '\007');
    }

    public static void refreshTitle() {
        if (Main.isRunning)
            Main.setTitle(Config.getInstance().NAME + " - Port : " + Main.gamePort + " | " + Main.key + " | " + Main.gameServer.getClients().size() + " Joueur(s)");
    }

    public static void clear() { //~30ms
        AnsiConsole.out.print("\033[H\033[2J");
    }
}