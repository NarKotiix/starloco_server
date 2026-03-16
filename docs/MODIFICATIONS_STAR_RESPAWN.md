# Modifications - Gestion des Étoiles et Respawn Instantané

## Résumé des modifications

Ce document détaille les modifications apportées à la logique de respawn des groupes de monstres pour :
1. Appliquer le respawn instantané dans les maps classiques (comme dans les donjons)
2. Ajouter une vraie gestion des étoiles qui augmente avec le temps
3. Éviter les spawns avec étoiles si le délai n'est pas respecté

## Fichiers modifiés

### 1. `GameMap.java`

#### A. Classe `RespawnGroup` - Ajout de la propriété `stars`
```java
private static class RespawnGroup {
    private final GameMap map;
    private final int cell;
    private final long lastTime;
    private final int stars;  // Nouveau: Nombre d'étoiles du groupe
}
```

**Objectif**: Stocker le nombre d'étoiles du groupe qui attend son respawn pour appliquer un délai approprié.

#### B. Méthode statique `calculateDelayForStars(int stars)`
Nouvelle méthode qui calcule le délai de respawn en fonction du nombre d'étoiles:

| Étoiles | Délai |
|---------|-------|
| 0 (sans étoiles) | 2-5 minutes |
| 15 étoiles | 5-8 minutes |
| 30 étoiles | 8-11 minutes |
| 45 étoiles | 11-14 minutes |
| 60 étoiles | 14-17 minutes |
| 75+ étoiles | 17-20 minutes |

**Logique**: Plus les étoiles augmentent, plus le délai de respawn s'allonge, reflétant la difficulté croissante du groupe.

#### C. Logique de `updatable.update()`
Modification pour respecter le délai basé sur les étoiles:

```java
// Groupes non-fixes en map classique
long delayMs = calculateDelayForStars(respawnGroup.stars);
if(time - respawnGroup.lastTime > delayMs) {
    respawnGroup.map.spawnGroup(Constant.ALIGNEMENT_NEUTRE, 1, true, -1);
    this.groups.remove(respawnGroup);
}
```

#### D. Méthodes `spawnAfterTimeGroup()`
Surcharge pour accepter les étoiles en paramètre:

```java
public void spawnAfterTimeGroup() {
    // Version par défaut: 0 étoiles (respawn rapide)
    spawnAfterTimeGroup(0);
}

public void spawnAfterTimeGroup(int stars) {
    ((ArrayList<RespawnGroup>) updatable.get()).add(new RespawnGroup(this, -1, System.currentTimeMillis(), stars));
}
```

**Objectif**: Pouvoir appeler `spawnAfterTimeGroup()` sans paramètre (compatibilité) ou avec les étoiles du groupe.

#### E. Méthode `startFightVersusMonstres()`
Modifications principales:

1. **Capture des étoiles du groupe** avant de le retirer de la map:
```java
int groupStars = group.getStarBonus();
this.mobGroups.remove(group.getId());
```

2. **Respawn intelligent selon le type de map**:
   - **Maps avec groupes fixes (donjons)**: Respawn instantané du groupe fixe via la DB
   - **Maps classiques**: 
     - Si le groupe a des étoiles: Ajouter à la queue de respawn avec délai approprié
     - Si sans étoiles: Respawn instantané d'un nouveau groupe neutre

```java
if (groupStars > 0) {
    // Avec étoiles : ajouter à la queue de respawn avec délai
    spawnAfterTimeGroup(groupStars);
} else {
    // Sans étoiles : respawn instantané
    spawnGroup(Constant.ALIGNEMENT_NEUTRE, 1, true, -1);
}
```

### 2. `Fight.java`

#### A. Ligne ~987 - Respawn après combat PVM
```java
if (!this.getMobGroup().isFix() && this.isCheckTimer())
    this.getMapOld().spawnAfterTimeGroup(this.getMobGroup().getStarBonus());
```

**Changement**: Passage des étoiles du groupe vaincu au système de respawn.

#### B. Ligne ~4584 - Respawn après combat Héroïque
```java
if(!group.isFix()) this.getMapOld().spawnAfterTimeGroup(group.getStarBonus());
```

**Changement**: Passage des étoiles du groupe pour la gestion du respawn.

## Comportement résultant

### Avant les modifications
- Respawn aléatoire entre 120-300 secondes pour les maps classiques
- Pas de différenciation basée sur les étoiles du groupe
- Logique de respawn différente entre les donjons et les maps classiques

### Après les modifications
- **Maps classiques**: Respawn instantané si pas d'étoiles, ou avec délai progressif si étoiles
- **Donjons**: Respawn instantané du groupe fixe (inchangé)
- **Délais respectés**: Les groupes avec étoiles ne respawnent que si le délai minimum est écoulé
- **Progression**: Plus un groupe a d'étoiles, plus l'intervalle de respawn s'allonge
- **Cohérence**: Même logique appliquée partout, respawn immédiat si la map est vide

## Exemples de scénarios

### Scénario 1: Map classique avec groupe sans étoiles
1. Joueur engage un groupe (0 étoiles)
2. Combat se termine, groupe vaincu
3. **Résultat**: Nouveau groupe spawn instantanément

### Scénario 2: Map classique avec groupe à 45 étoiles
1. Joueur engage un groupe (45 étoiles)
2. Combat se termine, groupe vaincu
3. **Résultat**: Groupe ajouté à la queue de respawn (11-14 minutes d'attente)
4. Après le délai écoulé, le groupe respawn avec les mêmes étoiles

### Scénario 3: Donjon avec groupe fixe
1. Joueur engage un groupe fixe
2. Combat se termine, groupe vaincu
3. **Résultat**: Le groupe respawn instantanément avec les stats de la DB

### Scénario 4: Map classique avec plusieurs groupes
1. Map avec 2 groupes en même temps
2. Joueur engage l'un des groupes
3. **Résultat**: Aucun respawn (la map a encore 1 groupe)
4. Deuxième groupe se fait vaincre → respawn du premier groupe si délai OK

## Notes techniques

- Le système utilise la classe `RespawnGroup` pour gérer la queue de respawn avec délais
- `calculateDelayForStars()` est une méthode statique utilisant `Formulas.getRandomValue()`
- Les délais sont en millisecondes
- La vérification du délai se fait toutes les secondes (1000ms) par l'updatable
- Les étoiles d'un groupe sont immuables une fois le groupe créé (propriété `private final` dans MobGroup)

## Impact sur les performances

- **Minimal**: Utilise la même structure `updatable` que le système de respawn précédent
- **Queue**: La queue de respawn est itérée une fois par seconde, complexité O(n)
- **Calcul**: `calculateDelayForStars()` est une simple opération conditionnelle et aléatoire

## Compatibilité

- ✅ Rétrocompatible avec les appels à `spawnAfterTimeGroup()` sans paramètre
- ✅ N'affecte pas les donjons (logique preservée)
- ✅ N'affecte pas les spawns manuels (command)
- ✅ Compatible avec le système HEROIC (gestion séparée)

## Améliorations futures possibles

1. Configurer les délais de respawn via `config.properties`
2. Ajouter des logs détaillés pour déboguer les délais
3. Implémenter une augmentation dynamique des étoiles basée sur le temps écoulé
4. Ajouter des événements de respawn (broadcast du respawn avec délai)

