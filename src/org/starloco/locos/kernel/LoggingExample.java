package org.starloco.locos.kernel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Classe exemple d'utilisation du système de logging colorisé
 * 
 * À titre d'exemple uniquement - à adapter selon vos besoins
 */
public class LoggingExample {
    
    private static final Logger logger = LoggerFactory.getLogger(LoggingExample.class);
    
    public static void main(String[] args) {
        // Exemples d'utilisation basiques
        demonstrateBasicLogging();
        
        // Exemple avec exceptions
        demonstrateErrorLogging();
        
        // Exemple avec données
        demonstrateDataLogging();
    }
    
    /**
     * Démontre les différents niveaux de logging
     */
    private static void demonstrateBasicLogging() {
        logger.debug("Ceci est un message DEBUG - informations de débogage");
        logger.info("Ceci est un message INFO - démarrage du serveur");
        logger.warn("Ceci est un message WARN - comportement inattendu");
        logger.error("Ceci est un message ERROR - erreur non critique");
        logger.trace("Ceci est un message TRACE - informations très détaillées");
    }
    
    /**
     * Démontre le logging avec exceptions
     */
    private static void demonstrateErrorLogging() {
        try {
            // Code qui pourrait causer une exception
            throw new IllegalArgumentException("Paramètre invalide");
        } catch (IllegalArgumentException e) {
            // Avec stacktrace
            logger.error("Une erreur est survenue lors du traitement", e);
            
            // Ou simplement le message
            logger.warn("Erreur non critique: " + e.getMessage());
        }
    }
    
    /**
     * Démontre le logging avec données
     */
    private static void demonstrateDataLogging() {
        String username = "Player1";
        int playerId = 12345;
        long connectedTime = System.currentTimeMillis();
        
        logger.info("Joueur connecté: {} (ID: {})", username, playerId);
        logger.debug("Temps de connexion: {}", connectedTime);
        
        // Exemple dans une boucle
        for (int i = 0; i < 3; i++) {
            logger.debug("Itération {}/3", i + 1);
        }
    }
    
    /**
     * Exemple d'utilisation de ColoredLog pour sortie personnalisée
     */
    public static void demonstrateColoredLog() {
        ColoredLog.printTitle("Démarrage du Serveur");
        ColoredLog.info("Initialisation du serveur...");
        ColoredLog.debug("Configuration chargée");
        ColoredLog.success("Base de données connectée avec succès");
        ColoredLog.warn("CPU élevé détecté!");
        ColoredLog.error("Erreur de synchronisation réseau");
        ColoredLog.fail("Impossible de charger les ressources");
        ColoredLog.printSeparator();
    }
}

