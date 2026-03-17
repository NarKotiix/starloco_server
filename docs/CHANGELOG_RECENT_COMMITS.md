# Changelog récent (4 derniers commits)

Ce document résume les quatre derniers commits appliqués sur `main`.

## 1) `f7837d1` — `perf: O(1) hash reverse lookup and simplify getCase`

- **Type :** performance
- **Fichiers modifiés :** `CryptManager.java`, `GameMap.java`
- **Impact :**
  - Ajout d'une table inversée statique `HASH_REVERSE[128]` dans `CryptManager` : `getIntByHashedValue()` et `cellCode_To_ID()` passent de O(64) à **O(1)**.
  - `getCase()` simplifié : suppression du fallback linéaire, accès direct au tableau `casesById[]`.
  - Suppression de `ensureCaseCapacity()` (devenue inutile).

## 2) `2e64144` — `perf: speed up map loading and case lookup`

- **Type :** performance
- **Fichiers modifiés :** `GameMap.java`, `CryptManager.java`
- **Impact :**
  - Introduction du tableau `casesById[]` pour un lookup O(1) dans `getCase()`.
  - Refactoring du constructeur `GameMap` : `loadMobPossibles()`, `extractMaxTeam()`, `parseMapPos()`, `applyForbidden()`.
  - Parsing manuel par index (zéro allocation `String.split()`).

## 3) `fa47140` — `perf: parallelize world monster group loading`

- **Type :** performance
- **Fichiers modifiés :** `World.java`
- **Impact :** chargement des groupes de monstres parallélisé via `ExecutorService` → démarrage multi-thread.

## 4) `f48cf12` — `perf: accelerate database startup and fail-fast`

- **Type :** performance
- **Fichiers modifiés :** couche base de données, `World.java`
- **Impact :** initialisation DB accélérée ; erreur de connexion MySQL détectée immédiatement (*fail-fast*).

---

> Détail complet dans [`CHANGELOG_PERF_V1.2.0.md`](CHANGELOG_PERF_V1.2.0.md).

**Dernière mise à jour :** 17 Mars 2026
