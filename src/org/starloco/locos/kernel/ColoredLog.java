package org.starloco.locos.kernel;

import org.fusesource.jansi.Ansi;

/**
 * Classe utilitaire pour afficher les logs en couleur dans la console
 */
public class ColoredLog {
    
    // Codes couleurs ANSI
    private static final String RESET = Ansi.ansi().reset().toString();
    private static final String DEBUG_COLOR = Ansi.ansi().fg(Ansi.Color.CYAN).toString();
    private static final String INFO_COLOR = Ansi.ansi().fg(Ansi.Color.GREEN).toString();
    private static final String WARN_COLOR = Ansi.ansi().fg(Ansi.Color.YELLOW).toString();
    private static final String ERROR_COLOR = Ansi.ansi().fg(Ansi.Color.RED).toString();
    private static final String FATAL_COLOR = Ansi.ansi().fg(Ansi.Color.RED).toString();
    private static final String TRACE_COLOR = Ansi.ansi().fg(Ansi.Color.MAGENTA).toString();
    
    // Symboles pour chaque niveau
    private static final String DEBUG_SYMBOL = "🔍";
    private static final String INFO_SYMBOL = "ℹ️ ";
    private static final String WARN_SYMBOL = "⚠️ ";
    private static final String ERROR_SYMBOL = "❌";
    private static final String FATAL_SYMBOL = "💀";
    private static final String TRACE_SYMBOL = "🔬";

    /**
     * Formate un message DEBUG
     */
    public static String debug(String message) {
        return formatLog("DEBUG", DEBUG_COLOR, DEBUG_SYMBOL, message);
    }

    /**
     * Formate un message INFO
     */
    public static String info(String message) {
        return formatLog("INFO", INFO_COLOR, INFO_SYMBOL, message);
    }

    /**
     * Formate un message WARNING
     */
    public static String warn(String message) {
        return formatLog("WARN", WARN_COLOR, WARN_SYMBOL, message);
    }

    /**
     * Formate un message ERROR
     */
    public static String error(String message) {
        return formatLog("ERROR", ERROR_COLOR, ERROR_SYMBOL, message);
    }

    /**
     * Formate un message FATAL
     */
    public static String fatal(String message) {
        return formatLog("FATAL", FATAL_COLOR, FATAL_SYMBOL, message);
    }

    /**
     * Formate un message TRACE
     */
    public static String trace(String message) {
        return formatLog("TRACE", TRACE_COLOR, TRACE_SYMBOL, message);
    }

    /**
     * Format interne pour tous les niveaux
     */
    private static String formatLog(String level, String color, String symbol, String message) {
        String timestamp = getTimestamp();
        return color + symbol + " [" + timestamp + "] [" + level + "] " + message + RESET;
    }

    /**
     * Récupère le timestamp formaté HH:mm:ss.SSS
     */
    private static String getTimestamp() {
        long time = System.currentTimeMillis();
        int millis = (int) (time % 1000);
        long seconds = (time / 1000) % 60;
        long minutes = (time / (1000 * 60)) % 60;
        long hours = (time / (1000 * 60 * 60)) % 24;
        
        return String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, millis);
    }

    /**
     * Affiche un séparateur coloré
     */
    public static void printSeparator() {
        System.out.println(INFO_COLOR + "═══════════════════════════════════════════════════════════════" + RESET);
    }

    /**
     * Affiche un titre formaté
     */
    public static void printTitle(String title) {
        printSeparator();
        System.out.println(INFO_COLOR + "║  " + title + RESET);
        printSeparator();
    }

    /**
     * Affiche une success message
     */
    public static void success(String message) {
        System.out.println(Ansi.ansi().fg(Ansi.Color.GREEN).toString() + 
                           "✓ [SUCCESS] " + message + RESET);
    }

    /**
     * Affiche un fail message
     */
    public static void fail(String message) {
        System.out.println(ERROR_COLOR + "✗ [FAIL] " + message + RESET);
    }
}



