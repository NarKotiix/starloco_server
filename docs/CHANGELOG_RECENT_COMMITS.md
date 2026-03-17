# Changelog recent (3 derniers commits)

Ce document resume les trois derniers commits appliques sur `main`.

## 1) `cd719aa` - Ajout de `*.snapshot` au `.gitignore`

- **Type:** maintenance repository
- **Fichier modifie:** `.gitignore`
- **Impact:** les fichiers de snapshots locaux ne sont plus suivis par Git.

## 2) `cbccc2a` - Ajout de `test_modifications.sh` au `.gitignore`

- **Type:** maintenance repository
- **Fichier modifie:** `.gitignore`
- **Impact:** le script de test local n'est plus pris en compte dans le versioning.

## 3) `b35fc38` - Respawn etoiles + documentation v1.1.0

- **Type:** fonctionnalite gameplay + documentation
- **Fichiers applicatifs touches:**
  - `src/org/starloco/locos/area/map/GameMap.java`
  - `src/org/starloco/locos/fight/Fight.java`
  - `src/org/starloco/locos/entity/monster/Monster.java`
  - `src/org/starloco/locos/game/world/World.java`
  - `src/org/starloco/locos/command/CommandAdmin.java`
  - `src/org/starloco/locos/kernel/Main.java`
- **Nouveaux documents:**
  - `CHANGELOG_STAR_RESPAWN_V1.0.0.md`
  - `QUICK_START_STAR_RESPAWN.md`
  - `docs/INDEX_DOCUMENTATION_V1.1.0.md`
  - `docs/MODIFICATIONS_STAR_RESPAWN.md`
  - `docs/CONFIGURATION_STAR_RESPAWN.md`
  - `docs/MAPS_CLASSIQUES_MULTIGROUPS.md`
  - `docs/AMELIORATION_MAXGROUP.md`
  - `docs/CHANGELOG_PERSISTENCE_STARS_V1.0.0.md`
  - `docs/IMPLEMENTATION_PERSISTENCE_STARS_V1.0.0.md`
  - `docs/INDEX_PERSISTENCE_STARS_V1.0.0.md`
  - `docs/SUMMARY_PERSISTENCE_STARS_V1.0.0.md`
  - `docs/VERIFICATION_IMPLEMENTATION_V1.0.0.md`
  - `docs/LOGIQUE_CORRIGÉE_V2.0.0.md`
  - `docs/IMPLEMENTATION_FINALE_V3.0.0.md`
  - `docs/V4_FINALE_SIMPLE.md`
  - `docs/MANIFEST_PERSISTENCE_STARS_V1.0.0.md`
  - `docs/TÂCHE_FINALE_COMPLÉTÉE.md`
- **Impact fonctionnel:**
  - gestion de sauvegarde/restauration des etoiles de groupes,
  - gestion de delais de respawn selon etoiles,
  - lot documentaire complet pour exploitation et debug.

---

**Derniere mise a jour:** 2026-03-17


