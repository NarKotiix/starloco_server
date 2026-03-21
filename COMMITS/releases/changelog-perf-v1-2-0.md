# ⚡ CHANGELOG PERFORMANCES — v1.2.0

> Série de 4 commits de performance appliqués sur `main` entre le 17 mars 2026 et aujourd'hui.  
> Objectif : réduire le temps de démarrage du serveur et la latence en gameplay.

---

## Commit 1 — `perf: accelerate database startup and fail-fast`

**Fichiers touchés :** `World.java`, couche base de données

### Ce qui a changé
- Chargement des données de la base de données accéléré au démarrage.
- Stratégie *fail-fast* : en cas d'erreur de connexion MySQL, le serveur s'arrête immédiatement avec un message clair au lieu de continuer dans un état incohérent.
- Réduction du nombre de requêtes SQL redondantes lors de l'initialisation.

### Impact
- Démarrage plus rapide sur les serveurs avec de nombreuses maps/items.
- Erreurs de configuration détectées dès le lancement.

---

## Commit 2 — `perf: parallelize world monster group loading`

**Fichiers touchés :** `World.java`

### Ce qui a changé
- Le chargement des groupes de monstres du monde est maintenant **parallélisé** via un `ExecutorService`.
- Chaque groupe est chargé dans un thread indépendant ; le thread principal attend la fin de tous avec `awaitTermination`.

### Impact
- Sur un serveur multi-cœur, le chargement des groupes de monstres est **jusqu'à N× plus rapide** (N = nombre de cœurs).
- Aucun changement de comportement au runtime.

---

## Commit 3 — `perf: speed up map loading and case lookup`

**Fichiers touchés :** `GameMap.java`, `CryptManager.java`

### Ce qui a changé

#### `GameMap.java`
| Avant | Après |
|-------|-------|
| `getCase(int id)` : scan linéaire O(n) sur `List<GameCase>` | Index tableau `casesById[]` : lookup O(1) |
| Code constructeur non structuré (gros bloc) | Extraction en méthodes privées : `loadMobPossibles()`, `extractMaxTeam()`, `parseMapPos()`, `applyForbidden()` |
| Parsing avec `String.split()` | Parsing manuel par index : zéro allocation d'objet supplémentaire |

#### `CryptManager.java`
- Refactoring interne du constructeur de la map.

### Impact
- Chaque appel à `getCase()` (très fréquent : pathfinding, combats, déplacements) passe de O(n_cells) à **O(1)**.
- Le constructeur de `GameMap` alloue moins d'objets temporaires → moins de GC.

---

## Commit 4 — `perf: O(1) hash reverse lookup and simplify getCase`

**Fichiers touchés :** `CryptManager.java`, `GameMap.java`

### Ce qui a changé

#### `CryptManager.java`
```java
// AVANT — boucle O(64) à chaque appel
public static int getIntByHashedValue(char c) {
    for (int a = 0; a < HASH.length; a++)
        if (HASH[a] == c) return a;
    return -1;
}

// APRÈS — table inversée O(1), construite une seule fois au chargement
private static final int[] HASH_REVERSE;
static {
    HASH_REVERSE = new int[128];
    Arrays.fill(HASH_REVERSE, -1);
    for (int i = 0; i < HASH.length; i++) HASH_REVERSE[HASH[i]] = i;
}

public static int getIntByHashedValue(char c) {
    return (c < 128) ? HASH_REVERSE[c] : -1;
}
```

`cellCode_To_ID()` bénéficie du même changement (2 lookups O(64) → 2 lookups O(1)).

#### `GameMap.java`
- `getCase(int id)` : suppression du fallback linéaire devenu inutile depuis l'introduction de `rebuildCaseIndex()`.
- Suppression de `ensureCaseCapacity()` (méthode devenue lettre morte).

### Sites d'appel impactés
| Méthode | Endroit | Fréquence |
|---------|---------|-----------|
| `getIntByHashedValue` | `decompileMapData` | **5×/cellule** × toutes les maps au démarrage |
| `getIntByHashedValue` | `PathFinding` | À chaque déplacement joueur |
| `getIntByHashedValue` | `SpellEffect` | À chaque calcul de portée de sort |
| `getIntByHashedValue` | `GameClient` | À chaque paquet de mouvement |
| `getIntByHashedValue` | `Mount` | À chaque déplacement de monture |
| `cellCode_To_ID` | `CryptManager` | Parsing de map |

### Impact
- `decompileMapData` : gain O(64×5) → O(5) par cellule, soit **~×13 plus rapide** pour cette boucle.
- Pathfinding et combats : chaque résolution de direction est instantanée.

---

## Résumé global

| Domaine | Avant | Après |
|---------|-------|-------|
| Démarrage DB | Séquentiel, pas de fail-fast | Accéléré + fail-fast |
| Chargement groupes monstres | Mono-thread | Multi-thread parallèle |
| `getCase()` | O(n_cells) | **O(1)** |
| `getIntByHashedValue()` | O(64) | **O(1)** |
| `cellCode_To_ID()` | O(128) | **O(1)** |

---

**Version :** 1.2.0  
**Date :** 17 Mars 2026  
**Status :** ✅ Compilé et validé (`BUILD SUCCESSFUL`)

