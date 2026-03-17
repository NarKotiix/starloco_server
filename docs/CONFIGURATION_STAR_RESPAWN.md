# Configuration optionnelle pour le système de respawn avec gestion des étoiles

## Paramètres modifiables

## Mise a jour progression des etoiles (17/03/2026)

La progression des etoiles de groupes est maintenant basee sur une courbe temporelle:

- `1 etoile visible` au bout de `10 minutes` sans combat lance
- `10 etoiles visibles` atteintes vers `8 heures` au total
- conversion interne: `1 etoile visible = 20 points` (`starBonus`)

### Paliers cibles (visibles)

| Etoiles visibles | Temps cumule cible |
|------------------|--------------------|
| 1                | 00:10              |
| 2                | 01:02              |
| 3                | 01:54              |
| 4                | 02:46              |
| 5                | 03:38              |
| 6                | 04:30              |
| 7                | 05:22              |
| 8                | 06:14              |
| 9                | 07:06              |
| 10               | 07:58 (~8h)        |

### Parametres de code associes

Dans `src/org/starloco/locos/area/map/GameMap.java`:

- `STAR_VISIBLE_UNIT = 20`
- `STAR_FIRST_VISIBLE_DELAY_MINUTES = 10`
- `STAR_TEN_VISIBLE_TOTAL_MINUTES = 480`
- `STAR_MAX_CAP = 200`

La mise a jour est appliquee dans `updateMobGroupsStars()` et le calcul fin se fait dans `MobGroup.updateStarBonus(...)`.

Les délais peuvent être ajustés en fonction de vos besoins. Voici les délais par défaut:

### Délais de respawn par nombre d'étoiles

```
0 étoiles (pas d'étoiles):
  Min: 120000 ms (2 minutes)
  Max: 300000 ms (5 minutes)

1-14 étoiles:
  Min: 300000 ms (5 minutes)
  Max: 480000 ms (8 minutes)

15-29 étoiles:
  Min: 300000 ms (5 minutes)
  Max: 480000 ms (8 minutes)

30-44 étoiles:
  Min: 480000 ms (8 minutes)
  Max: 660000 ms (11 minutes)

45-59 étoiles:
  Min: 660000 ms (11 minutes)
  Max: 840000 ms (14 minutes)

60-74 étoiles:
  Min: 840000 ms (14 minutes)
  Max: 1020000 ms (17 minutes)

75+ étoiles:
  Min: 1020000 ms (17 minutes)
  Max: 1200000 ms (20 minutes)
```

## Pour modifier les délais

### Option 1: Modificatin du code (recommandé pour l'équilibre)

Éditez la méthode `calculateDelayForStars()` dans `GameMap.java`:

```java
private static long calculateDelayForStars(int stars) {
    int minDelay, maxDelay;
    
    if (stars <= 0) {
        // MODIFIER ICI pour sans étoiles
        minDelay = 120000;  // Changez cette valeur
        maxDelay = 300000;  // Et celle-ci
    } else if (stars < 30) {
        // MODIFIER ICI pour 1-29 étoiles
        minDelay = 300000;  // Changez cette valeur
        maxDelay = 480000;  // Et celle-ci
    }
    // ... etc pour les autres cas
    
    return Formulas.getRandomValue(minDelay, maxDelay);
}
```

### Option 2: Configuration en base de données (future)

Vous pourriez stocker les délais en base de données:

```sql
CREATE TABLE star_respawn_config (
    min_stars INT,
    max_stars INT,
    min_delay INT,  -- en millisecondes
    max_delay INT   -- en millisecondes
);

INSERT INTO star_respawn_config VALUES
(0, 0, 120000, 300000),
(1, 29, 300000, 480000),
(30, 44, 480000, 660000),
(45, 59, 660000, 840000),
(60, 74, 840000, 1020000),
(75, 999, 1020000, 1200000);
```

Puis modifier `calculateDelayForStars()` pour lire depuis la DB.

## Recommandations d'équilibre

### Progression modérée (recommandé)
- Augmentation douce des délais
- Permet aux joueurs de progresser naturellement
- Évite la frustration sur les groupes faibles

### Progression stricte
- Délais plus longs pour les étoiles
- Force les joueurs à éviter les groupes difficiles
- Plus réaliste pour un serveur hardcore

### Progression clémente
- Délais plus courts pour les étoiles
- Permet farming constant
- Idéal pour serveur farming-oriented

## Exemple: Configuration "Hardcore"

```java
private static long calculateDelayForStars(int stars) {
    int minDelay, maxDelay;
    
    if (stars <= 0) {
        minDelay = 60000;   // 1 min
        maxDelay = 180000;  // 3 min
    } else if (stars < 30) {
        minDelay = 600000;  // 10 min
        maxDelay = 900000;  // 15 min
    } else if (stars < 45) {
        minDelay = 900000;  // 15 min
        maxDelay = 1200000; // 20 min
    } else if (stars < 60) {
        minDelay = 1200000; // 20 min
        maxDelay = 1800000; // 30 min
    } else if (stars < 75) {
        minDelay = 1800000; // 30 min
        maxDelay = 2400000; // 40 min
    } else {
        minDelay = 2400000; // 40 min
        maxDelay = 3600000; // 60 min
    }
    
    return Formulas.getRandomValue(minDelay, maxDelay);
}
```

## Exemple: Configuration "Clémente"

```java
private static long calculateDelayForStars(int stars) {
    int minDelay, maxDelay;
    
    if (stars <= 0) {
        minDelay = 120000;  // 2 min
        maxDelay = 180000;  // 3 min
    } else if (stars < 30) {
        minDelay = 180000;  // 3 min
        maxDelay = 240000;  // 4 min
    } else if (stars < 45) {
        minDelay = 240000;  // 4 min
        maxDelay = 300000;  // 5 min
    } else if (stars < 60) {
        minDelay = 300000;  // 5 min
        maxDelay = 360000;  // 6 min
    } else if (stars < 75) {
        minDelay = 360000;  // 6 min
        maxDelay = 420000;  // 7 min
    } else {
        minDelay = 420000;  // 7 min
        maxDelay = 480000;  // 8 min
    }
    
    return Formulas.getRandomValue(minDelay, maxDelay);
}
```

## Monitoring et logs

### Vérifier les délais appliqués

Les logs DEBUG affichent:
```
[DEBUG] Respawn EN ATTENTE du groupe (stars=45) sur la map 1234 - délai respecté
```

### Déboguer les calculs

Vous pouvez ajouter un log dans `calculateDelayForStars()`:

```java
private static long calculateDelayForStars(int stars) {
    // ... calcul ...
    long delay = Formulas.getRandomValue(minDelay, maxDelay);
    // DEBUG:
    System.out.println("[RESPAWN] Stars: " + stars + " => Délai: " + (delay/1000) + "s");
    return delay;
}
```

## Cas spéciaux

### Groupes sans étoiles générés
Le système génère automatiquement des groupes sans étoiles lors des spawns:
- Réduisez le délai pour encourager le farming
- Augmentez le délai si trop rapide

### Groupes avec beaucoup d'étoiles
Les groupes accumulent des étoiles naturellement:
- Respects les délais progressifs
- Évitez de le faire trop pénalisant

### Maps avec groupes fixes (donjons)
Utilise un délai différent (géré en base de données):
- Ne changerez pas avec cette modification
- Reste du contrôle du DBA

## Tests après modification

1. Tuez un groupe **sans étoiles** → vérifiez le délai court
2. Tuez un groupe **avec beaucoup d'étoiles** → vérifiez le délai long
3. Consultez les logs pour confirmer
4. Testez sur une durée longue (30 min+)

## Support

Si vous modifiez les délais:
1. Recompilez avec `gradlew.bat build`
2. Redémarrez le serveur
3. Vérifiez les logs DEBUG
4. Ajustez si nécessaire

---

**Version**: 1.0.0
**Dernier mise à jour**: 2026-03-16

