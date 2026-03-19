# Changelog recent (6 derniers commits + derniere mise a jour)

Ce document resume les six derniers commits appliques sur `main`.

## Derniere mise a jour locale (v1.4.0)

- **Type:** admin + perf + runtime
- **Fichiers modifies:**
  - `build.gradle`
  - `src/org/starloco/locos/game/world/World.java`
  - `src/org/starloco/locos/command/CommandAdmin.java`
  - `src/org/starloco/locos/area/map/GameMap.java`
  - `src/org/starloco/locos/fight/Fight.java`
  - `src/org/starloco/locos/fight/ia/AbstractIA.java`
  - `src/org/starloco/locos/fight/ia/IAHandler.java`
  - `src/org/starloco/locos/fight/ia/IAProfiler.java`
  - `src/org/starloco/locos/fight/ia/util/Function.java`
  - `src/org/starloco/locos/fight/ia/util/AstarPathfinding.java`
  - `src/org/starloco/locos/kernel/Config.java`
  - `README.md`
  - `docs/INDEX_DOCUMENTATION_V1.2.0.md`
  - `docs/CHANGELOG_ADMIN_IA_DB_V1.4.0.md`
- **Impact:**
  - `RELOADITEM` recharge les templates et resynchronise l'affichage client,
  - `SPAWN` supporte le format simple `id,id,id` avec niveaux derives de la base,
  - instrumentation `[AI-PROF]` + acceleration des invocations,
  - correction du runtime MySQL via `mysql-connector-j:8.0.33`,
  - documentation synchronisee.

## 1) local - `feat(admin,ia,db): sync reloaditem/spawn docs, speed up invocation turns and pin mysql runtime`

- **Type:** fonctionnalite + correctif runtime
- **Fichiers modifies:**
  - `build.gradle`
  - `src/org/starloco/locos/game/world/World.java`
  - `src/org/starloco/locos/command/CommandAdmin.java`
  - `src/org/starloco/locos/area/map/GameMap.java`
  - `src/org/starloco/locos/fight/Fight.java`
  - `src/org/starloco/locos/fight/ia/*`
  - `src/org/starloco/locos/kernel/Config.java`
- **Impact:**
  - nouvelles capacites admin et optimisation des invocations,
  - stabilisation du driver JDBC au runtime.

## 2) local - `docs(v1.4.0): add admin/ia/db changelog and refresh README/index`

- **Type:** documentation
- **Fichiers modifies:**
  - `README.md`
  - `docs/CHANGELOG_ADMIN_IA_DB_V1.4.0.md`
  - `docs/INDEX_DOCUMENTATION_V1.2.0.md`
  - `docs/QUICK_START.md`
- **Impact:**
  - documentation alignee avec `RELOADITEM`, `SPAWN`, `AI-PROF` et les nouveaux lanceurs.

## 3) `9fccc52` - `feat(star tracing): add debug logging for star bonus updates and implement tracing logic`

- **Type:** fonctionnalite (trace/debug)
- **Fichiers modifies:**
  - `src/org/starloco/locos/area/map/GameMap.java`
  - `src/org/starloco/locos/entity/monster/Monster.java`
- **Impact:**
  - logs DEBUG des gains d'etoiles (map/groupe/elapsed/required/phase),
  - instrumentation pour diagnostiquer les retours d'etoiles apres clear.

## 4) `6d42388` - `feat(command admin): add debug logging for CLEARMAP and CLEARALL commands`

- **Type:** fonctionnalite (admin/debug)
- **Fichier modifie:** `src/org/starloco/locos/command/CommandAdmin.java`
- **Impact:** trace explicite des executions `STARS CLEARMAP` et `STARS CLEARALL`.

## 5) `7a0f4df` - `feat(exchange logging): add conditional logging for exchange packets based on content`

- **Type:** fonctionnalite (logging)
- **Fichier modifie:** `src/org/starloco/locos/exchange/ExchangeHandler.java`
- **Impact:** filtrage des logs Exchange bruyants selon le type de paquet.

## 6) `f558098` - `feat(command admin): add CLEARMAP option to STARS command for resetting star bonuses on the current map`

- **Type:** fonctionnalite (admin)
- **Fichier modifie:** `src/org/starloco/locos/command/CommandAdmin.java`
- **Impact:** nouvelle commande `STARS CLEARMAP` (reset etoiles map courante).

## 7) `028927d` - `feat(monster): add resetStarBonus method to update star bonus and timestamp`

- **Type:** fonctionnalite (etoiles)
- **Fichier modifie:** `src/org/starloco/locos/entity/monster/Monster.java`
- **Impact:** reset etoiles + horodatage interne pour eviter la remontée immediate.

## 8) `25e10ec` - `feat(command admin): add CLEARALL option to STARS command for resetting star bonuses`

- **Type:** fonctionnalite (admin)
- **Fichier modifie:** `src/org/starloco/locos/command/CommandAdmin.java`
- **Impact:** nouvelle commande globale `STARS CLEARALL`.

---

- Detail perf: `CHANGELOG_PERF_V1.2.0.md`
- Detail securite: `SECURITY_HARDENING_V1.2.1.md`

**Derniere mise a jour:** 19 Mars 2026
