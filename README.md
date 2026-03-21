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

### Correctif sécurité échange + build [@NarKotiix — v1.4.1]

- **`PlayerExchange`** : validation stricte acteur/quantité/kamas, clamp des quantités réelles, garde-fous anti-NPE (joueur + NPC).
- **Anti-duplication** : revalidation des soldes kamas avant `apply()` et bornage des transferts d'objets selon stock réel.
- **Build** : correction compilation IA/profiling (`Config` + appels combat `GameClient`).

---

## Qualité de code [@NarKotiix — v1.3.0]

Revue complète de `GameClient.java` (7 800 lignes) et `GameCase.java` (1 200 lignes) — **30 corrections** appliquées le 18 Mars 2026 :

### 🔴 Bugs corrigés
- **`tchat()`** : `msg == lastMsg` (comparaison de références) → `msg.equals(lastMsg)` — la protection anti-doublon de messages privés n'avait **jamais** fonctionné.
- **`movementItemOrKamas()`** : `split("\\|")[2] == "0"` → `"0".equals(...)` — vérification du prix de vente HDV toujours ignorée (mise en vente à 0 kamas possible).

### ⚡ Performance
- `generateKey()`, `getGifts()`, `ZaapiList`, `newStats` : concaténations `+=` en boucle (O(n²)) → `StringBuilder`.
- `removePlayer()` / `removeFighter()` : double parcours `contains()` + `remove()` → `remove()` seul.
- `getPlayers()` : `new ArrayList<>()` → `Collections.emptyList()` (zéro allocation).
- `buy()` : `Integer.valueOf()` → `Integer.parseInt()` (élimination du boxing).

### 🧹 Qualité
- **27 `e.printStackTrace()`** remplacés par des appels logger Logback structurés avec contexte joueur.
- 3 `System.out.println` de débogage → `logger.debug`.
- 2 imports inutilisés supprimés (`javax.xml.crypto.Data`, `java.util.Collection`).
- `worldInfos()` : cases `J`/`V` identiques fusionnées (fall-through).
- `prismFight()` : 3 blocs `try-catch` séparés → 1 seul.
- `InterruptedException` : ajout de `Thread.currentThread().interrupt()` (conformité threading).
- `craftsmenJobIds` : `public static` → `private static final` + accesseur `getCraftsmenJobIds()`.
- `isValidPlayerName()` extrait : ~70 lignes dupliquées entre `addCharacter()` et `changeName()` → méthode partagée.

---

## Mises à jour récentes

| Date | Version | Type | Résumé |
|------|---------|------|--------|
| 20/03/2026 | **v1.4.1** | 🔐 fix sécurité / build | durcissement `PlayerExchange` + fix compilation (`Config` AI profiling, `GameClient` combat cast) |
| 19/03/2026 | **v1.4.0** | ⚙ admin / IA / DB | `RELOADITEM` synchronisé client, `SPAWN 1295,1295`, profiling/accélération invocations, fix runtime MySQL 8.0.33 |
| 19/03/2026 | **v1.3.1** | 🛠 fix | Toggle global `NPC_MOVEMENT` + correctif regression deplacement joueur (`BN` premature retire) |
| 18/03/2026 | **v1.3.0** | 🧹 qualité | 30 corrections GameClient + GameCase : bugs String `==`, StringBuilder, logging, refactoring |
| 17/03/2026 | v1.2.1 | 🔐 sécurité | Fail-fast déplacements, anti-flood, logs throttlés |
| 17/03/2026 | v1.2.0 | ⚡ perf | CryptManager O(1), GameMap lookup O(1), DB fail-fast |
| — | v1.1.0 | ✨ feature | Respawn étoiles, persistance, commandes admin STARS |

> Détails complets → [`COMMITS/README.md`](COMMITS/README.md), [`COMMITS/releases/changelog-exchange-security-v1-4-1.md`](COMMITS/releases/changelog-exchange-security-v1-4-1.md), [`COMMITS/releases/changelog-admin-ia-db-v1-4-0.md`](COMMITS/releases/changelog-admin-ia-db-v1-4-0.md)

---

## Nouveautés admin / IA / runtime [@NarKotiix — v1.4.0]

### Commandes admin

- **`RELOADITEM`** : vide le cache des templates d'items, recharge la base et notifie les joueurs connectés pour forcer la mise à jour côté client.
- **`SPAWN`** accepte maintenant 2 formats :

```text
SPAWN 1295,120,200;1295,120,200
SPAWN 1295,1295
```

- Le format simple `id,id,id` récupère automatiquement les niveaux disponibles en base et choisit un grade aléatoire compatible.
- Le spawn se fait sur la cellule courante de l'admin avec fallback sur cellule libre si nécessaire.

### IA d'invocations

- Profiling activable via logs `[AI-PROF]`.
- Résumés périodiques + logs de tours lents.
- Réduction des délais artificiels sur les sorts et déplacements d'invocations.

Configuration utile :

```ini
AI_PROFILING=true
AI_PROFILING_INVOCATIONS_ONLY=true
AI_PROFILING_WARN_MS=60
AI_INVOCATION_DELAY=50
AI_INVOCATION_SPELL_MAX_DELAY=220
AI_INVOCATION_MOVEMENT_BASE_DELAY=220
AI_INVOCATION_MOVEMENT_STEP_DELAY=55
```

### Runtime MySQL

- Driver JDBC verrouillé sur **`com.mysql:mysql-connector-j:8.0.33`** pour stabiliser le runtime Java 8/Hikari.
- Correction du `NoClassDefFoundError: com/mysql/cj/protocol/a/NullValueEncoder`.

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
```

> Le lanceur Windows actuel est `Start-Server.bat`.

### Option configuration : mouvement des groupes

Vous pouvez activer/desactiver le mouvement automatique des groupes de monstres sur la map avec :

```ini
MOB_GROUP_MOVEMENT=true
```

- `true` = Oui (comportement normal)
- `false` = Non (les groupes ne se deplacent plus automatiquement)

### Option configuration : mouvement des PNJ

Vous pouvez activer/desactiver le mouvement automatique des PNJ sur toutes les maps avec :

```ini
NPC_MOVEMENT=false
```

- `true` = Oui (PNJ mobiles selon leur path)
- `false` = Non (PNJ statiques sur toutes les maps)

> Recommande en production si vous souhaitez un comportement stable sans deplacement PNJ.

### Option configuration : profiling et vitesse des invocations

```ini
AI_PROFILING=false
AI_PROFILING_INVOCATIONS_ONLY=true
AI_PROFILING_WARN_MS=60
AI_INVOCATION_DELAY=50
AI_INVOCATION_SPELL_MAX_DELAY=220
AI_INVOCATION_MOVEMENT_BASE_DELAY=220
AI_INVOCATION_MOVEMENT_STEP_DELAY=55
```

- `AI_PROFILING=true` : active les logs `[AI-PROF]`
- `AI_PROFILING_INVOCATIONS_ONLY=true` : cible uniquement les invocations
- `AI_INVOCATION_DELAY` : délai minimal entre actions d'invocation
- `AI_INVOCATION_SPELL_MAX_DELAY` : plafond de délai après un sort d'invocation
- `AI_INVOCATION_MOVEMENT_*` : tuning du temps de déplacement des invocations

### Commandes admin utiles

```text
RELOADITEM
LINEM 1295
SPAWN 1295,1295
SPAWN 1295,120,200;1295,120,200
```

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

### Guides stables

- 📖 **[docs/README.md](docs/README.md)** — point d'entrée des guides d'exploitation et de configuration
- 🔄 **[quick-start-star-respawn.md](docs/quick-start-star-respawn.md)** — système de respawn avec étoiles
- 🛑 **[graceful-shutdown.md](docs/graceful-shutdown.md)** — guide complet d'arrêt propre (CTRL+C)
- 📄 **[quick-shutdown.md](docs/quick-shutdown.md)** — guide rapide d'arrêt en 30 secondes
- 📝 **[readme-logs.md](docs/readme-logs.md)** — logs colorés ANSI / Logback

### Historique / changelogs

- 📚 **[COMMITS/README.md](COMMITS/README.md)** — index principal de l'historique projet
- ⚙ **[changelog-admin-ia-db-v1-4-0.md](COMMITS/releases/changelog-admin-ia-db-v1-4-0.md)** — `RELOADITEM`, `SPAWN`, profiling/optimisation invocations, correctif driver MySQL
- 🧹 **[changelog-code-quality-v1-3-0.md](COMMITS/releases/changelog-code-quality-v1-3-0.md)** — corrections qualité : bugs String `==`, StringBuilder, logging, refactoring
- 🛠 **[changelog-npc-movement-player-fix-v1-3-1.md](COMMITS/releases/changelog-npc-movement-player-fix-v1-3-1.md)** — toggle `NPC_MOVEMENT` + correctif blocage déplacement joueur
- ⚡ **[changelog-perf-v1-2-0.md](COMMITS/releases/changelog-perf-v1-2-0.md)** — détail des optimisations de performance
- 🔐 **[security-hardening-v1-2-1.md](COMMITS/releases/security-hardening-v1-2-1.md)** — durcissement sécurité des déplacements

