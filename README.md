# Starloco-Fun Server (Dofus 1.29+)

Reprise et optimisation du projet Starloco par [@NarKotiix](https://github.com/NarKotiix).

Ce serveur met l'accent sur :

- la stabilité du runtime
- la lisibilité du code
- l'amélioration des performances
- une documentation claire (guides + historique)

---

## Sommaire

- [Fonctionnalités clés](#fonctionnalités-clés)
- [Mises à jour récentes](#mises-à-jour-récentes)
- [Installation rapide](#installation-rapide)
- [Configuration utile](#configuration-utile)
- [Logs](#logs)
- [Commandes admin utiles](#commandes-admin-utiles)
- [Documentation](#documentation)

---

## Fonctionnalités clés

- Gladiatrool
- Client 1.39.8 compatible (split packets, positions Spells/Obj en int)
- Maps chiffrées officielles
- Correctifs gameplay majeurs (IA, interactions Panda, persistance, anti-abus)
- Démarrage/arrêt serveur robustes (build Gradle, shutdown propre)

---

## Mises à jour récentes

| Date | Version | Type | Résumé |
|------|---------|------|--------|
| 22/03/2026 | **session-dev** | 🧩 runtime + outils DB | levee limite maison Admin + correctifs runtime + pipeline migration maps dans `Outils/db` |
| 21/03/2026 | **v1.4.2-stable** | ✅ release stable | logs IA isolés + durcissement boucle `endTurn` + refonte docs (`COMMITS/`) |
| 20/03/2026 | **v1.4.1** | 🔐 sécurité / build | durcissement `PlayerExchange` + correctifs compilation IA/profiling |
| 19/03/2026 | **v1.4.0** | ⚙ admin / IA / DB | `RELOADITEM`, `SPAWN`, profiling invocations, fix runtime MySQL |
| 19/03/2026 | **v1.3.1** | 🛠 fix | toggle `NPC_MOVEMENT` + correctif blocage déplacement joueur |
| 18/03/2026 | **v1.3.0** | 🧹 qualité | corrections code `GameClient` / `GameCase` |
| 17/03/2026 | **v1.2.1** | 🔐 sécurité | fail-fast déplacements, anti-flood, logs throttlés |
| 17/03/2026 | **v1.2.0** | ⚡ perf | optimisations O(1), fail-fast DB |

Détails : [`COMMITS/README.md`](COMMITS/README.md)

---

## Installation rapide

### Prérequis

- Java 8
- Base MySQL configurée dans `config.properties`

### Compilation

```bash
./gradlew clean build
```

Sous Windows PowerShell :

```powershell
.\gradlew.bat clean build
```

### Lancement

Linux/macOS :

```bash
./start-server.sh
```

Windows :

```powershell
.\Start-Server.bat
```

---

## Configuration utile

Dans `config.properties`.

### IA Profiling (invocations)

```ini
AI_PROFILING=true
AI_PROFILING_INVOCATIONS_ONLY=true
AI_PROFILING_WARN_MS=60

AI_INVOCATION_DELAY=50
AI_INVOCATION_SPELL_MAX_DELAY=220
AI_INVOCATION_MOVEMENT_BASE_DELAY=220
AI_INVOCATION_MOVEMENT_STEP_DELAY=55
```

### Mouvements automatiques

```ini
MOB_GROUP_MOVEMENT=true
NPC_MOVEMENT=false
```

---

## Logs

### Fichiers principaux

- `Logs/server.log` : logs serveur généraux
- `Logs/errors.log` : erreurs uniquement
- `Logs/AIProfiling/ai_profiling.log` : logs dédiés IA (`[AI-PROF]`)

### Comportement attendu

- Les logs `[AI-PROF]` sont écrits dans `Logs/AIProfiling/ai_profiling.log`
- Ils restent affichés en console en direct
- Ils ne polluent pas `server.log`

### Configuration Logback

Fichier runtime : `src/resources/logback.xml`

---

## Commandes admin utiles

```text
RELOADITEM
LINEM 1295
SPAWN 1295,1295
SPAWN 1295,120,200;1295,120,200
```

---

## Arrêt propre (CTRL+C)

L'arrêt en console déclenche un shutdown gracieux :

1. arrêt des nouvelles connexions
2. sauvegarde monde/joueurs
3. fermeture des connexions DB

Guide détaillé : [`docs/graceful-shutdown.md`](docs/graceful-shutdown.md)

---

## Documentation

### Guides stables

- [`docs/README.md`](docs/README.md) — index des guides d'exploitation
- [`docs/quick-start.md`](docs/quick-start.md)
- [`docs/quick-shutdown.md`](docs/quick-shutdown.md)
- [`docs/readme-logs.md`](docs/readme-logs.md)
- [`docs/quick-start-star-respawn.md`](docs/quick-start-star-respawn.md)
- [`docs/maps-db-tooling.md`](docs/maps-db-tooling.md)

### Historique projet / release notes

- [`COMMITS/README.md`](COMMITS/README.md) — index principal historique
- [`COMMITS/commits/`](COMMITS/commits)
- [`COMMITS/releases/`](COMMITS/releases)
- [`COMMITS/topics/`](COMMITS/topics)

---

## Auteurs originaux

- [@sarazar928ghost](https://github.com/sarazar928ghost) — Kevin#6537
- [@arwase](https://github.com/arwase) — Arwase#6656
- [@iR3SH](https://github.com/iR3SH) — Hydronish#0843

