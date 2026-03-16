# 🎨 Système de Logging Colorisé - StarLoco

## Description

Le système de logging a été complètement revampé pour améliorer la lisibilité de la console avec des **couleurs par type de log** et des **patterns formatés**.

## 📋 Niveaux de Log et Couleurs

| Niveau | Couleur | Symbole | Description |
|--------|---------|---------|-------------|
| **DEBUG** | 🔵 Cyan | 🔍 | Informations de débogage (développement) |
| **INFO** | 🟢 Vert | ℹ️ | Informations générales / Démarrage |
| **WARN** | 🟡 Jaune | ⚠️ | Avertissements / Situations anormales |
| **ERROR** | 🔴 Rouge | ❌ | Erreurs non critiques |
| **FATAL** | 🔴 Rouge Gras | 💀 | Erreurs critiques / Arrêt serveur |
| **TRACE** | 🟣 Magenta | 🔬 | Traces détaillées |

## 📂 Fichiers de Configuration

### `logback.xml`
Configuration principale pour Logback qui définit :
- **Appenders Console** : Logs colorés en temps réel
- **Appenders Fichiers** : Logs persistants sans couleurs
- **Patterns** : Format HH:mm:ss.SSS | LEVEL | Logger - Message
- **Rotation** : Fichiers log rotatifs (max 10MB par fichier)

### `ColoredLog.java`
Classe utilitaire optionnelle pour ajouter des couleurs manuellement dans le code :
```java
ColoredLog.debug("Message de debug");
ColoredLog.info("Information importante");
ColoredLog.warn("Attention !");
ColoredLog.error("Une erreur est survenue");
ColoredLog.success("Opération réussie ✓");
```

## 📁 Organisation des Logs

```
Logs/
├── server.log                    # Tous les logs
├── server-2024-03-16-1.log      # Logs rotatifs (10MB max)
├── server-2024-03-16-2.log      
├── errors.log                    # Erreurs uniquement
└── Error/                        # Ancien format (maintenus)
    ├── 2024-03-16.log
    └── error-2024-03-16-1.log
```

## 🚀 Utilisation

### Dans le code (via SLF4J)
```java
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class MaClasse {
    private static final Logger logger = LoggerFactory.getLogger(MaClasse.class);
    
    public void maMethode() {
        logger.debug("Message debug");
        logger.info("Démarrage de...");
        logger.warn("Attention!");
        logger.error("Erreur!", new Exception());
    }
}
```

### Utilitaire ColoredLog (optionnel)
```java
import org.starloco.locos.kernel.ColoredLog;

public class MaClasse {
    public void maMethode() {
        ColoredLog.info("Message colorisé");
        ColoredLog.success("Succès!");
        ColoredLog.fail("Échec!");
        ColoredLog.printTitle("Titre Important");
    }
}
```

## 🎯 Avantages

✅ **Console lisible** - Distinctions claires par type de log  
✅ **Persistence** - Tous les logs sont sauvegardés en fichier  
✅ **Rotation automatique** - Évite les fichiers trop volumineux  
✅ **Windows compatible** - Jansi gère les couleurs sur Windows  
✅ **SLF4J + Logback** - Stack moderne et performant  
✅ **UTF-8** - Support complet des caractères accentués  

## ⚙️ Configuration Avancée

### Modifier le niveau de log
Dans `logback.xml`, changer :
```xml
<root level="DEBUG">  <!-- Remplacer DEBUG par INFO, WARN, ERROR -->
```

### Ajouter un logger pour un package spécifique
```xml
<logger name="org.starloco.locos.exchange" level="WARN" additivity="false">
    <appender-ref ref="CONSOLE"/>
    <appender-ref ref="FILE"/>
</logger>
```

### Modifier le pattern de sortie
Format disponible :
- `%d{HH:mm:ss.SSS}` - Timestamp
- `%-5level` - Niveau de log
- `%logger{36}` - Classe logger
- `%msg` - Message
- `%n` - Nouvelle ligne
- `%ex` - Exception complète

## 🔧 Dépendances

- **logback-classic** (1.1.2) - Framework de logging
- **logback-core** (1.1.2) - Core logback
- **slf4j-api** (1.7.7) - API de logging
- **jansi** (1.7) - Support couleurs ANSI Windows

Toutes ces dépendances sont déjà présentes dans le projet ! ✓

## 📞 Support

Pour modifier davantage le système de logging, consultez :
- [Documentation Logback](http://logback.qos.ch/)
- [Documentation Jansi](https://github.com/fusesource/jansi)
- [Documentation SLF4J](https://www.slf4j.org/)

