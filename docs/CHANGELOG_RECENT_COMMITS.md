# Changelog recent (5 derniers commits)

Ce document resume les cinq derniers commits appliques sur `main`.

## 1) `adc64ef` - `security: harden movement parsing and add anti-flood guards`

- **Type:** securite + performance
- **Fichiers modifies:**
  - `GameClient.java`
  - `CryptManager.java`
  - `PathFinding.java`
  - `docs/SECURITY_HARDENING_V1.2.1.md`
  - `docs/INDEX_DOCUMENTATION_V1.2.0.md`
- **Impact:**
  - fail-fast sur deplacements invalides,
  - anti-flood leger cote client,
  - logs de securite throttles,
  - optimisation O(1) des cellules interdites.

## 2) `27321d2` - `docs: update README and docs for v1.2.0 perf release`

- **Type:** documentation
- **Impact:**
  - README nettoye et modernise,
  - nouvel index docs v1.2.0,
  - nettoyage des anciens fichiers docs obsoletes.

## 3) `f7837d1` - `perf: O(1) hash reverse lookup and simplify getCase`

- **Type:** performance
- **Fichiers modifies:** `CryptManager.java`, `GameMap.java`
- **Impact:** lookup HASH en O(1), simplification `getCase()`.

## 4) `2e64144` - `perf: speed up map loading and case lookup`

- **Type:** performance
- **Fichiers modifies:** `GameMap.java`, `CryptManager.java`
- **Impact:** index des cases en O(1), parsing constructeur optimise.

## 5) `fa47140` - `perf: parallelize world monster group loading`

- **Type:** performance
- **Fichier modifie:** `World.java`
- **Impact:** chargement parallelise des groupes de monstres au demarrage.

---

- Detail perf: `CHANGELOG_PERF_V1.2.0.md`
- Detail securite: `SECURITY_HARDENING_V1.2.1.md`

**Derniere mise a jour:** 17 Mars 2026
