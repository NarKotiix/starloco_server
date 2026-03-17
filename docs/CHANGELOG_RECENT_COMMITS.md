# Changelog recent (6 derniers commits)

Ce document resume les six derniers commits appliques sur `main`.

## 1) `9fccc52` - `feat(star tracing): add debug logging for star bonus updates and implement tracing logic`

- **Type:** fonctionnalite (trace/debug)
- **Fichiers modifies:**
  - `src/org/starloco/locos/area/map/GameMap.java`
  - `src/org/starloco/locos/entity/monster/Monster.java`
- **Impact:**
  - logs DEBUG des gains d'etoiles (map/groupe/elapsed/required/phase),
  - instrumentation pour diagnostiquer les retours d'etoiles apres clear.

## 2) `6d42388` - `feat(command admin): add debug logging for CLEARMAP and CLEARALL commands`

- **Type:** fonctionnalite (admin/debug)
- **Fichier modifie:** `src/org/starloco/locos/command/CommandAdmin.java`
- **Impact:** trace explicite des executions `STARS CLEARMAP` et `STARS CLEARALL`.

## 3) `7a0f4df` - `feat(exchange logging): add conditional logging for exchange packets based on content`

- **Type:** fonctionnalite (logging)
- **Fichier modifie:** `src/org/starloco/locos/exchange/ExchangeHandler.java`
- **Impact:** filtrage des logs Exchange bruyants selon le type de paquet.

## 4) `f558098` - `feat(command admin): add CLEARMAP option to STARS command for resetting star bonuses on the current map`

- **Type:** fonctionnalite (admin)
- **Fichier modifie:** `src/org/starloco/locos/command/CommandAdmin.java`
- **Impact:** nouvelle commande `STARS CLEARMAP` (reset etoiles map courante).

## 5) `028927d` - `feat(monster): add resetStarBonus method to update star bonus and timestamp`

- **Type:** fonctionnalite (etoiles)
- **Fichier modifie:** `src/org/starloco/locos/entity/monster/Monster.java`
- **Impact:** reset etoiles + horodatage interne pour eviter la remontée immediate.

## 6) `25e10ec` - `feat(command admin): add CLEARALL option to STARS command for resetting star bonuses`

- **Type:** fonctionnalite (admin)
- **Fichier modifie:** `src/org/starloco/locos/command/CommandAdmin.java`
- **Impact:** nouvelle commande globale `STARS CLEARALL`.

---

- Detail perf: `CHANGELOG_PERF_V1.2.0.md`
- Detail securite: `SECURITY_HARDENING_V1.2.1.md`

**Derniere mise a jour:** 17 Mars 2026
