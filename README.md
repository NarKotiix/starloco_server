# Starloco-Fun Server (Dofus 1.29+)

**Reprise et optimisation du projet Starloco par [@NarKotiix](https://github.com/NarKotiix)**  
Améliorations : refactoring code, fix UTF-8, optimisations performance (donjons, respawn, Account), build Gradle propre.  
Développé avec aide IA Perplexity / Claude pour debugging et features.

## Auteurs originaux

- [@sarazar928ghost](https://github.com/sarazar928ghost) - Discord: Kevin#6537
- [@arwase](https://github.com/arwase) - Discord: Arwase#6656 (TWEAK Gladiatrool)
- [@iR3SH](https://github.com/iR3SH) - Discord: Hydronish#0843 (FIX Gladiatrool)

## Nouveautés

- Gladiatrool
- Client 1.39.8: Split packets, positions Spells/Obj en Int (pas Hexa)
- Maps chiffrées offi

## Debugs

- **Joueur**: FM cac 100%, anti-multi-équip (ex. 20 anneaux), morph armes, drop équipé, pas d'ID fantôme, IA, panda porter/jeter, items class persistants, ban/mute OK
- **Gladiatrool**: Sauvegarde sorts incarnation/perso (reboot-proof), effets toniques

## Optimisations [@NarKotiix]

- Refactoring massif: lisibilité/pro (mouvements inventaire, actions objets)
- Opti: getDirBetweenTwoCase, addObjet/createNewItem, packets Stats (raccourcis)
- Conditions anti-exceptions config
- Build Gradle 7.4 + JDK 8 (Linux/Windows sync)
- Fix encodage UTF-8 (é/à/ç)

## Téléchargement

- **Server**: Sources + Gradle (build.sh / gradlew build)
- **SQL**: help_game / help_login
- **Client**: [Mega 1.39.8](https://mega.nz/file/3sAljAyR#optHLctMbZWvgsksJhOH2gDNkEo-xpwXbyVTr45Q_50)
- Supprimez config.txt ancien au 1er lancement

## Installation rapide

### Compilation du projet

```bash
# Linux/MacOS
./gradlew clean build

# Windows
.\gradlew.bat clean build
```

### Lancement du serveur

```bash
# Windows - Mode normal
.\Start-Server.bat

# Windows - Mode debug (plus de logs)
.\Start-Server.bat --debug
```

**Le script `Start-Server.bat` active automatiquement :**
- ✅ Encodage UTF-8 pour les caractères spéciaux (é, à, ç, etc.)
- ✅ Support couleurs ANSI dans la console Windows
- ✅ Configuration mémoire optimisée (-Xms512M -Xmx2G)

## Support des couleurs dans les logs

Les logs du serveur affichent des **couleurs ANSI natifs** pour une meilleure lisibilité :

### Légende des couleurs

```
🔴 FATAL  - Rouge foncé (erreur critique)
🔴 ERROR  - Rouge (erreur)
🟡 WARN   - Jaune (avertissement)
🟢 INFO   - Vert (information)
🔵 DEBUG  - Cyan (debug)
🟣 TRACE  - Magenta (trace)
```

### Configuration technique

**Dépendances utilisées :**
- **Logback 1.3.14** : Framework de logging Java avec support natif du `%clr()` converter
- **Jansi 2.4.1** : Librairie Windows ANSI support pour la console Windows
- **SLF4J 1.7.36** : Interface de logging standardisée

**Fichier de configuration :** `src/logback.xml`

Pattern utilisé :
```
%d{HH:mm:ss.SSS} | %clr(%-5level){FATAL=1;31, ERROR=31, WARN=33, INFO=32, DEBUG=36, TRACE=35} | %logger{36} - %msg%n
```

### Personnalisation des couleurs

Pour modifier les couleurs, éditez `src/logback.xml` et changez la propriété `LOG_PATTERN` :

```xml
<property name="LOG_PATTERN" value="%d{HH:mm:ss.SSS} | %clr(%-5level){FATAL=1;31, ERROR=31, WARN=33, INFO=32, DEBUG=36, TRACE=35} | %logger{36} - %msg%n"/>
```

Codes ANSI disponibles :
- `31` = Rouge
- `32` = Vert
- `33` = Jaune
- `34` = Bleu
- `35` = Magenta
- `36` = Cyan
- `1;31` = Rouge gras (pour le gras, préfixez avec `1;`)

### Logs persistants

Les logs sont sauvegardés dans le dossier `Logs/` :
- **server.log** : Tous les logs (rotatif par jour)
- **errors.log** : Erreurs uniquement (rotatif par jour)

Vous pouvez consulter l'historique même si la console ne l'affiche plus.

