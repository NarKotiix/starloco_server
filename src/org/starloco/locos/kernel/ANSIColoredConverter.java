package org.starloco.locos.kernel;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.CompositeConverter;
import java.util.HashMap;
import java.util.Map;

/**
 * Convertisseur de couleur personnalisé pour Logback
 * Ajoute des couleurs ANSI pour la sortie console
 * Supporte la syntaxe : %clr(text){level=color,...}
 */
public class ANSIColoredConverter extends CompositeConverter<ILoggingEvent> {
    
    private static final String RESET = "\u001B[m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN = "\u001B[36m";
    private static final String MAGENTA = "\u001B[35m";
    private static final String BRIGHT_RED = "\u001B[1;31m";
    private static final String BRIGHT_GREEN = "\u001B[1;32m";
    private static final String BRIGHT_YELLOW = "\u001B[1;33m";
    private static final String BRIGHT_CYAN = "\u001B[1;36m";
    private static final String BRIGHT_MAGENTA = "\u001B[1;35m";

    private static final Map<String, String> ANSI_COLORS = new HashMap<>();

    static {
        ANSI_COLORS.put("FATAL", RED);
        ANSI_COLORS.put("ERROR", RED);
        ANSI_COLORS.put("WARN", YELLOW);
        ANSI_COLORS.put("WARNING", YELLOW);
        ANSI_COLORS.put("INFO", GREEN);
        ANSI_COLORS.put("DEBUG", CYAN);
        ANSI_COLORS.put("TRACE", MAGENTA);
        ANSI_COLORS.put("ANSI_RED", RED);
        ANSI_COLORS.put("ANSI_GREEN", GREEN);
        ANSI_COLORS.put("ANSI_YELLOW", YELLOW);
        ANSI_COLORS.put("ANSI_CYAN", CYAN);
        ANSI_COLORS.put("ANSI_MAGENTA", MAGENTA);
    }

    @Override
    protected String transform(ILoggingEvent event, String text) {
        Level level = event.getLevel();
        String color = RESET;

        switch (level.toInt()) {
            case Level.ERROR_INT:
                color = RED;
                break;
            case Level.WARN_INT:
                color = YELLOW;
                break;
            case Level.INFO_INT:
                color = GREEN;
                break;
            case Level.DEBUG_INT:
                color = CYAN;
                break;
            case Level.TRACE_INT:
                color = MAGENTA;
                break;
        }

        return color + text + RESET;
    }
}



