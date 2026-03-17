# Starloco-Fun Server (Dofus 1.29+)

**Reprise et optimisation du projet Starloco par [@NarKotiix](https://github.com/NarKotiix)**  
Améliorations : refactoring code, fix UTF-8, optimisations performance (démarrage DB, chargement maps, pathfinding, respawn), build Gradle propre.  
Développé avec aide IA pour debugging et features.

## Auteurs originaux

- [@sarazar928ghost](https://github.com/sarazar928ghost) — Discord : Kevin#6537
- [@arwase](https://github.com/arwase) — Discord : Arwase#6656 (TWEAK Gladiatrool)
- [@iR3SH](https://github.com/iR3SH) — Discord : Hydronish#0843 (FIX Gladiatrool)

---

## Fonctionnalités notables

- Gladiatrool
- Client 1.39.8 : Split packets, positions Spells/Obj en Int (pas Hexa)
- Maps chiffrées officielles

## Corrections

- **Joueur** : FM cac 100%, anti-multi-équip (ex. 20 anneaux), morph armes, drop équip, pas d'ID fantôme, IA, panda porter/jeter, items classe persistants, ban/mute OK
- **Gladiatrool** : Sauvegarde sorts incarnation/perso (reboot-proof), effets toniques

---

## Optimisations de performance [@NarKotiix — v1.2.0]

- **`CryptManager`** : `getIntByHashedValue()` et `cellCode_To_ID()` passent de O(64) à **O(1)** grâce à une table de correspondance inversée statique `HASH_REVERSE[128]`.
- **`GameMap.getCase()`** : lookup O(1) via tableau `casesById[]` — plus aucun scan linéaire O(n) à chaque appel de pathfinding/combat.
- **`GameMap` constructeur** : parsing manuel par index (zéro allocation `String.split()`), extrait en méthodes lisibles.
- **Groupes monstres** : chargement parallélisé au démarrage via `ExecutorService`.
- **Base de données** : initialisation accélérée + détection immédiate des erreurs de connexion (*fail-fast*).
- Refactoring massif : lisibilité/pro (mouvements inventaire, actions objets)
- Optimisation : `getDirBetweenTwoCase`, `addObjet`/`createNewItem`, packets Stats
- Conditions anti-exceptions config — Build Gradle + JDK 8 (Linux/Windows sync) — Fix encodage UTF-8

## Sécurité renforcée [@NarKotiix — v1.2.1]

- **Déplacements (`GameClient`)** : validation stricte path/cellule/orientation, rejet immédiat des paquets malformés (*fail-fast*).
- **Anti-flood léger** : limitation par client des mouvements invalides (fenêtre glissante + cooldown court).
- **Logs sécurité** : journalisation `WARN` throttlée (agrégation des suppressions) pour éviter le bruit en cas de flood.
- **Parsing (`CryptManager`)** : garde-fous de bornes/format (hex, longueurs, cellules), réduction des chemins de crash.
- **Pathfinding (`PathFinding`)** : vérification des cellules interdites en O(1) via masque `boolean[]`.
- **SQL (`PlayerData`/`AccountData`)** : migration des points sensibles vers des requêtes paramétrées (`PreparedStatement`) pour limiter les risques d'injection SQL.

---

## Mises à jour récentes (6 derniers commits)

| Commit | Type | Résumé |
|--------|------|--------|
| `9fccc52` | ✨ stars/debug | Traces détaillées du gain d'étoiles (map/groupe/elapsed/raison) |
| `6d42388` | ✨ admin/debug | Logs DEBUG lors des commandes `STARS CLEARMAP` / `STARS CLEARALL` |
| `7a0f4df` | ✨ exchange/logging | Filtrage conditionnel des logs Exchange selon le paquet |
| `f558098` | ✨ admin | Ajout de `STARS CLEARMAP` (reset étoiles map courante) |
| `028927d` | ✨ stars | Ajout de `resetStarBonus()` (étoiles + timestamp) |
| `25e10ec` | ✨ admin | Ajout de `STARS CLEARALL` (reset global des étoiles) |

> Détail complet → [`docs/CHANGELOG_RECENT_COMMITS.md`](docs/CHANGELOG_RECENT_COMMITS.md), [`docs/CHANGELOG_PERF_V1.2.0.md`](docs/CHANGELOG_PERF_V1.2.0.md) et [`docs/SECURITY_HARDENING_V1.2.1.md`](docs/SECURITY_HARDENING_V1.2.1.md)

---

## Téléchargement

- **Server** : Sources + Gradle (`./gradlew build`)
- **SQL** : help_game / help_login
- **Client** : [Mega 1.39.8](https://mega.nz/file/3sAljAyR#optHLctMbZWvgsksJhOH2gDNkEo-xpwXbyVTr45Q_50)
- Supprimez `config.txt` ancien au 1er lancement

---

## Installation rapide

### Compilation

```bash
# Linux/macOS
./gradlew clean build

# Windows
.\gradlew.bat clean build
```

### Lancement

```bash
# Linux
./start-server.sh

# Windows — mode normal
.\Start-Server.bat

# Windows — mode debug (plus de logs)
.\Start-Server.bat --debug
```

### Option configuration : mouvement des groupes

Vous pouvez activer/desactiver le mouvement automatique des groupes de monstres sur la map avec :

```ini
MOB_GROUP_MOVEMENT=true
```

- `true` = Oui (comportement normal)
- `false` = Non (les groupes ne se deplacent plus automatiquement)

**Le script `Start-Server.bat` active automatiquement :**
- ✅ Encodage UTF-8 pour les caractères spéciaux (é, è, ê, etc.)
- ✅ Support couleurs ANSI dans la console Windows
- ✅ Configuration mémoire optimisée (`-Xms512M -Xmx2G`)

---

## Logs colorés (ANSI)

Les logs du serveur affichent des **couleurs ANSI** pour une meilleure lisibilité :

| Niveau | Couleur    |
|--------|------------|
| FATAL  | Rouge gras |
| ERROR  | Rouge      |
| WARN   | Jaune      |
| INFO   | Vert       |
| DEBUG  | Cyan       |
| TRACE  | Magenta    |

**Dépendances utilisées :**
- **Logback 1.3.14** — Framework de logging avec support natif `%clr()`
- **Jansi 2.4.1** — Support ANSI Windows
- **SLF4J 1.7.36** — Interface de logging standardisée

**Fichier de configuration :** `src/logback.xml`

```
%d{HH:mm:ss.SSS}  %clr(%-5level){FATAL=1;31, ERROR=31, WARN=33, INFO=32, DEBUG=36, TRACE=35}  %logger{36} - %msg%n
```

**Logs persistants** dans `Logs/` :
- `server.log` — Tous les logs (rotatif par jour)
- `errors.log` — Erreurs uniquement (rotatif par jour)

---

## Arrêt gracieux du serveur (CTRL+C)

Quand vous appuyez sur **CTRL+C** dans la console :

1. ✅ **Arrêt des nouvelles connexions** — Aucun nouveau joueur ne peut se connecter
2. ✅ **Sauvegarde du monde** — Objets, montures, maisons, etc.
3. ✅ **Déconnexion des joueurs** — Données sauvegardées en base
4. ✅ **Fermeture des bases de données** — Connexions MySQL proprement fermées

**Avant** : CTRL+C = risque de rollback  
**Après** : CTRL+C = sauvegarde complète + arrêt propre ✅

```
══════════════════════════════════════════════════════════
  SHUTDOWN SIGNAL RECEIVED - Saving data before closing...
══════════════════════════════════════════════════════════
Step 1/4 - Stopping new connections...
Step 2/4 - Saving world data (players, objects, mounts, etc)...
Step 3/4 - Disconnecting all players and saving their data...
Step 4/4 - Closing database connections...
  ✅ SERVER SHUTDOWN COMPLETE - All data saved
══════════════════════════════════════════════════════════
```

---

## Documentation complémentaire

- 📖 **[INDEX_DOCUMENTATION_V1.2.0.md](docs/INDEX_DOCUMENTATION_V1.2.0.md)** — Index complet de tous les documents
- 📋 **[CHANGELOG_RECENT_COMMITS.md](docs/CHANGELOG_RECENT_COMMITS.md)** — Résumé des derniers commits
- ⚡ **[CHANGELOG_PERF_V1.2.0.md](docs/CHANGELOG_PERF_V1.2.0.md)** — Détail des optimisations de performance v1.2.0
- 🔐 **[SECURITY_HARDENING_V1.2.1.md](docs/SECURITY_HARDENING_V1.2.1.md)** — Durcissement sécurité des déplacements (fail-fast, anti-flood, logs throttlés)
- 🔄 **[QUICK_START_STAR_RESPAWN.md](docs/QUICK_START_STAR_RESPAWN.md)** — Système de respawn avec étoiles
- 💾 **[SUMMARY_PERSISTENCE_STARS_V1.0.0.md](docs/SUMMARY_PERSISTENCE_STARS_V1.0.0.md)** — Persistance des étoiles après reboot
- 🛑 **[GRACEFUL_SHUTDOWN.md](docs/GRACEFUL_SHUTDOWN.md)** — Guide complet d'arrêt propre (CTRL+C)
- 📄 **[QUICK_SHUTDOWN.md](docs/QUICK_SHUTDOWN.md)** — Guide rapide d'arrêt en 30 secondes
- 📝 **[README_LOGS.md](docs/README_LOGS.md)** — Logs colorés ANSI / Logback
