# Implémentation de la Persistence des Étoiles des Groupes V1.0.0

## Overview
Cette implémentation ajoute un système de **persistence des étoiles** pour les groupes de monstres non-fixes. Les étoiles accumulées par un groupe avant son défait sont restaurées lors du respawn suivant.

## Architecture

### 1. Sauvegarde des Étoiles (`GameMap.java`)

#### Variable de Stockage
```java
private Map<Integer, Integer> savedGroupsStars = new HashMap<>();
```
- **Clé**: `cellID` (numéro de cellule)
- **Valeur**: Nombre d'étoiles du groupe

#### Méthode de Sauvegarde
```java
public void saveGroupsStars()
```
- Appelée **avant** le respawn d'un groupe
- Sauvegarde les étoiles actuelles de tous les groupes non-fixes
- Stockage en mémoire (survit à la session, pas aux redémarrages complets)

---

### 2. Flux de Respawn

#### Phase 1: Groupe est défait
1. Le groupe de monstres perd le combat
2. `spawnAfterTimeGroup()` est appelée
   - **Sauvegarde** les étoiles actuelles via `saveGroupsStars()`
   - Crée un `RespawnGroup` avec délai dynamique
3. Le groupe attend le délai avant respawn

#### Phase 2: Respawn du groupe
1. Après le délai, `spawnGroup()` est appelée
2. **Charge** les étoiles sauvegardées
   ```java
   Integer savedStars = this.savedGroupsStars.get(cellID);
   if (savedStars != null && savedStars > 0) {
       group.setStarBonus(savedStars);
   }
   ```
3. Le nouveau groupe a les **mêmes étoiles** que l'ancien

#### Phase 3: Combat engagé
1. Quand un groupe engage le combat, `setMobGroup()` est appelée (Fight.java)
2. Les étoiles sont **réinitialisées à 0**
   ```java
   void setMobGroup(Monster.MobGroup mobGroup) {
       if (mobGroup != null && mobGroup.getStarBonus() > 0) {
           mobGroup.setStarBonus(0);  // Reset stars when engaged
       }
   }
   ```
3. Le combat commence sans étoiles accumulées

---

## Modifications Effectuées

### 1. GameMap.java

#### Modification 1: `spawnAfterTimeGroup()`
**Ligne 849-851**
```java
public void spawnAfterTimeGroup() {
    // Sauvegarder les étoiles des groupes qui vont respawner
    saveGroupsStars();
    ((ArrayList<RespawnGroup>) updatable.get()).add(new RespawnGroup(this, -1, System.currentTimeMillis(), 0));
}
```

#### Modification 2: `spawnGroup()`
**Ligne 974-984**
```java
// Charger les étoiles sauvegardées si elles existent, sinon mettre 0
Integer savedStars = this.savedGroupsStars.get(cellID);
if (savedStars != null && savedStars > 0) {
    group.setStarBonus(savedStars);
    this.send("cs<font color='#00FF00'>[PERSISTENCE] Groupe " + group.getId() + 
            " a retrouvé " + savedStars + " étoiles au respawn</font>");
} else {
    group.setStarBonus(0);
}
```

### 2. Fight.java

#### Modification: `setMobGroup()`
**Ligne 836-842**
```java
void setMobGroup(Monster.MobGroup mobGroup) {
    this.mobGroup = mobGroup;
    // Réinitialiser les étoiles quand le groupe est engagé dans un combat
    if (mobGroup != null && mobGroup.getStarBonus() > 0) {
        mobGroup.setStarBonus(0);
        getMapOld().send("cs<font color='#FF0000'>[COMBAT] Étoiles du groupe " + mobGroup.getId() + 
                " réinitialisées à 0 car engagé en combat</font>");
    }
}
```

---

## Cycle de Vie des Étoiles

```
[Groupe A: 30 étoiles]
        ↓
[Joueur vainqueur, groupe défait]
        ↓
[saveGroupsStars() - sauvegarde 30 étoiles]
        ↓
[Délai de respawn: 8-11 minutes (délai pour 30 étoiles)]
        ↓
[spawnGroup() - crée Groupe B avec 30 étoiles]
        ↓
[Groupe B: 30 étoiles sur la map]
        ↓
[Joueur engage le groupe en combat]
        ↓
[setMobGroup() - réinitialise à 0 étoiles]
        ↓
[Combat démarre sans étoiles]
```

---

## Délais de Respawn Basés sur les Étoiles

Le système calcule le délai de respawn selon le nombre d'étoiles:

| Étoiles | Délai Min | Délai Max |
|---------|-----------|-----------|
| 0 | 2 min | 5 min |
| 1-14 | 5 min | 8 min |
| 15-29 | 5 min | 8 min |
| 30-44 | 8 min | 11 min |
| 45-59 | 11 min | 14 min |
| 60-74 | 14 min | 17 min |
| 75+ | 17 min | 20 min |

### Fonction de Calcul
```java
private static long calculateDelayForStars(int stars)
```
Localisation: `GameMap.java` ligne 900-930

---

## Logs de Débogage

### À la Sauvegarde
```
[PERSISTENCE] Groupe 123 sur cell 456 retrouvé X étoiles au respawn
```

### À la Restauration
```
[PERSISTENCE] Groupe 456 a retrouvé X étoiles au respawn
```

### À l'Engagement
```
[COMBAT] Étoiles du groupe 456 réinitialisées à 0 car engagé en combat
```

---

## Cas d'Utilisation

### Scénario 1: Combat Normal
1. Groupe avec 45 étoiles est défait
2. Respawn après 11-14 min avec 45 étoiles
3. Joueur engage le groupe → étoiles = 0
4. Combat normal sans bonus

### Scénario 2: Respawn Sans Combat
1. Groupe avec 30 étoiles respawn
2. Personne n'engage le groupe immédiatement
3. Les étoiles **continuent d'être visibles** sur la map
4. Quand le groupe est engagé → étoiles = 0

### Scénario 3: Respawn Rapide Sans Étoiles
1. Groupe sans étoiles est défait
2. Respawn rapide (2-5 min)
3. Respawn avec 0 étoiles
4. Combat sans accumulation d'étoiles

---

## Données Persistées

| Aspect | Persisté | Endroit |
|--------|----------|---------|
| Nombre d'étoiles | ✅ OUI | mémoire (`savedGroupsStars`) |
| Délai de respawn | ✅ OUI | dynamique selon étoiles |
| Position de spawn | ❌ NON | aléatoire |
| Composition du groupe | ❌ NON | re-générée |

---

## Limitations et Notes

1. **Persistence en Mémoire Seulement**
   - Les étoiles sauvegardées sont perdues au redémarrage du serveur
   - Alternative: sauvegarder en base de données (non implémentée)

2. **CellID Unique**
   - La persistence se base sur le cellID
   - Plusieurs groupes peuvent respawner sur la même cellule (scenario rare)

3. **Remise à Zéro au Combat**
   - Les étoiles sont **toujours** réinitialisées à 0 au combat
   - C'est le comportement attendu du système

4. **Pas de Décay des Étoiles**
   - Les étoiles ne diminuent pas avec le temps
   - Elles persistent jusqu'à engagement ou respawn

---

## Fichiers Modifiés

1. `src/org/starloco/locos/area/map/GameMap.java`
   - Ligne 851: ajout `saveGroupsStars()`
   - Ligne 974-984: chargement des étoiles dans `spawnGroup()`

2. `src/org/starloco/locos/fight/Fight.java`
   - Ligne 836-842: réinitialisation des étoiles dans `setMobGroup()`

---

## Version
- **V1.0.0** - Implémentation initiale
- **Date**: Mars 2026
- **Auteur**: Système de Persistence des Étoiles

---

## Prochaines Améliorations Possibles

1. **Persistence Base de Données**
   - Sauvegarder `savedGroupsStars` dans la DB au shutdown
   - Restaurer au startup

2. **Decay des Étoiles**
   - Réduire les étoiles avec le temps
   - Exemple: -1 étoile par heure

3. **Partage d'Étoiles**
   - Distribuer les étoiles du groupe défait aux autres groupes de la map
   - Renforcer progressivement les groupes restants

4. **Logs Détaillés**
   - Ajouter des logs en base de données
   - Suivre l'historique des étoiles par groupe

5. **API d'Admin**
   - Commandes pour modifier les étoiles
   - Exemple: `/resetstars <groupId>`

