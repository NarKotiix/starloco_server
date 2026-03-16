# Gestion des groupes multiples en Maps Classiques

## 🔑 Point crucial compris

Les **maps classiques (hors donjons)** peuvent avoir **plusieurs groupes simultanément**, contrairement à ce qu'une première lecture pouvait laisser croire.

## 📊 Différences clés

### Donjons (haveMobFix = true)
```
Max groupes: 1 (généralement)
Respawn: Instantané du groupe fixe via DB
Logique: Groupe fixe = une composition prédéfinie qui respawn toujours
```

### Maps Classiques (haveMobFix = false)
```
Max groupes: Variable (1-3+ selon la map)
              Défini par la propriété 'maxGroup' en base de données
Respawn: Instantané OU avec délai selon les étoiles
Logique: Nouveaux groupes générés aléatoirement
```

## 🗄️ Paramètre `maxGroup`

### Définition
La propriété `maxGroup` de chaque `GameMap` provient de la **base de données** lors du chargement de la map:

```java
private byte maxGroup = 3;  // Défaut: 3 groupes max

// Chargé depuis la DB
GameMap(short id, ..., byte maxGroup, ...)
    this.maxGroup = maxGroup;
```

### Exemples de valeurs
```
Map classique simple (Astrub): maxGroup = 1
Map classique moyenne: maxGroup = 2
Map classique grande (farming): maxGroup = 3
Map classique XL: maxGroup = 4+
Donjons: maxGroup variable (mais avec haveMobFix)
```

## 🔄 Logique de respawn en maps classiques

### Avant le combat
```
Map a 3 groupes possible (maxGroup=3)
Actuellement: 2 groupes présents
Joueur engage: 1 groupe
Après engagement: 1 groupe reste
```

### Après la victoire
```
1 groupe restant < 3 maxGroup
→ IL Y A DE LA PLACE pour 1 nouveau groupe

Vérifier les étoiles du groupe vaincu:
├─ 0 étoiles → Respawn immédiat
└─ N étoiles → Ajouter à queue avec délai
```

### Code amélioré
```java
// Compter les groupes non-fixes actuellement sur la map
int currentNonFixGroups = this.mobGroups.size() - this.fixMobGroups.size();
int remainingSpots = this.maxGroup - currentNonFixGroups;

// Respawn uniquement s'il y a de la place
if (remainingSpots > 0) {
    // Spawn selon les étoiles
}
```

## 📋 Scénarios détaillés

### Scénario 1: Map simple (maxGroup = 1)
```
Début:     1 groupe présent
Combat:    Joueur vs 1 groupe
Fin:       0 groupes restants
Respawn:   OUI (1 spot libre)
```

### Scénario 2: Map avec farming (maxGroup = 3)
```
Début:     3 groupes présents
Combat:    Joueur vs 1 groupe (0★)
Fin:       2 groupes restants
Respawn:   OUI (1 spot libre)
           → Spawn immédiat (sans étoiles)
```

### Scénario 3: Map avec farming (maxGroup = 3) - Groupe avec étoiles
```
Début:     3 groupes présents
Combat:    Joueur vs 1 groupe (45★)
Fin:       2 groupes restants
Respawn:   OUI (1 spot libre)
           → Ajouter à queue (respecter délai 11-14min)
```

### Scénario 4: Map pleine (maxGroup = 3)
```
Début:     3 groupes présents
Combat:    Joueur vs 1 groupe
Fin:       2 groupes restants
Respawn:   OUI (1 spot libre) ✓
           
Autre combat en parallèle:
Début:     2 groupes restants
Combat:    Joueur vs 1 groupe
Fin:       1 groupe restant
Respawn:   OUI (2 spots libres) ✓
```

### Scénario 5: Impossible si bien configuré
```
Début:     2 groupes présents (maxGroup = 3)
Combat:    Joueur vs 1 groupe
Fin:       1 groupe restant (1 spot libre)
Nouveau spawn aléatoire pendant le combat:
           Avant combat: 2 groupes
           Après: 3 groupes (plein)
Respawn du groupe vaincu: Attendu...
Résultat:  Peut pas respawn (pas de spot)
           → Sera jamais respawné!
```

**⚠️ C'est pour ça qu'il faut respecter maxGroup!**

## ✅ Logique maintenant implémentée

```java
// 1️⃣ COMPTER les groupes actuels
int currentNonFixGroups = this.mobGroups.size() - this.fixMobGroups.size();

// 2️⃣ CALCULER les spots libres
int remainingSpots = this.maxGroup - currentNonFixGroups;

// 3️⃣ AFFICHER le DEBUG
[DEBUG] Map classique: 2/3 groupes (spots libres: 1)

// 4️⃣ SPAWNER ou ATTENDRE
if (remainingSpots > 0) {
    if (groupStars > 0) {
        spawnAfterTimeGroup(stars);  // Avec délai
    } else {
        spawnGroup(...);  // Instantané
    }
} else {
    [DEBUG] Aucun respawn: la map a déjà 3 groupe(s)
}
```

## 🎯 Cas d'usage réels

### Farming map (maxGroup = 3)
```
Joueurs: 3 groupes = PLEIN
Joueur 1 tue groupe (0★): respawn immédiat
Joueur 2 tue groupe (30★): respawn après 8-11 min
Joueur 3 tue groupe (60★): respawn après 14-17 min
→ Équilibre naturel!
```

### Map simple (maxGroup = 1)
```
Joueur tue groupe (45★): respawn après 11-14 min
Pendant ce temps: Aucun groupe n'apparaît
→ Pas de double spawn
```

### Donjon (haveMobFix = true)
```
Groupe fixe: Respawn instantané de la DB
maxGroup: Ignoré (pas de spawn aléatoire)
→ Comportement inchangé
```

## 🔧 Configuration en Base de Données

### Table maps (exemple)
```sql
id  | name           | maxGroup | haveMobFix
1   | Astrub Village | 1        | false
12  | Prairie Plain  | 2        | false  
50  | Farmland       | 3        | false
100 | Dungeon Boss   | 1        | true
```

### Impacts
```
maxGroup = 0: Aucun groupe spawn jamais
maxGroup = 1: Un seul groupe à la fois
maxGroup = 2: Deux groupes simultanés
maxGroup = 3+: Farming intense possible
```

## 🔍 Déboguer les spawns

### Logs à vérifier
```
[DEBUG] Map classique: X/Y groupes (spots libres: Z)
```

Exemple:
```
[DEBUG] Map classique: 2/3 groupes (spots libres: 1)
→ Il y a de la place, respawn autorisé ✓

[DEBUG] Map classique: 3/3 groupes (spots libres: 0)
→ La map est pleine, pas de respawn ✓
```

### Debugging avancé
```java
// Ajouter cette ligne dans startFightVersusMonstres()
System.out.println("[RESPAWN DEBUG] Map " + this.id + 
    ": " + currentNonFixGroups + "/" + this.maxGroup + 
    " | Group stars: " + groupStars + 
    " | Fix groups: " + this.fixMobGroups.size());
```

## 📈 Améliorations futures

1. **Augmenter dynamiquement maxGroup** selon le nombre de joueurs
2. **Réduire maxGroup** si farming trop intense
3. **Events spéciaux** qui modifient maxGroup (boss event, etc)
4. **Config par zone** pour différents styles de farming

## ⚠️ Pièges courants

### Piège 1: Oublier que maxGroup vient de la DB
Ne pas hard-coder la limite, elle doit être en DB!

### Piège 2: Compter les groupes fixes dans maxGroup
```java
// ✗ MAUVAIS
if (this.mobGroups.size() >= this.maxGroup)

// ✓ BON
if (this.mobGroups.size() - this.fixMobGroups.size() >= this.maxGroup)
```

### Piège 3: Spawner sans vérifier maxGroup
```java
// ✗ MAUVAIS
spawnGroup(...);  // Sans vérifier la limite

// ✓ BON
if (remainingSpots > 0) {
    spawnGroup(...);  // Vérifier avant!
}
```

## 🎓 Résumé technique

| Propriété | Signification | Valeurs usuelles |
|-----------|---------------|------------------|
| `maxGroup` | Max groupes NON-fixes simulés | 1-4 |
| `haveMobFix()` | Groupe fixe (donjon)? | true/false |
| `mobGroups.size()` | Tous les groupes (fixes + non-fixes) | 0-N |
| `fixMobGroups.size()` | Seuls les groupes fixes | 0-1 |
| Non-fix count | `size() - fixSize()` | 0-maxGroup |

## ✅ Vérification finale

- ✅ Logique respecte `maxGroup`
- ✅ Différencie fix (donjons) et non-fix (classique)
- ✅ Respawn immédiat si pas d'étoiles
- ✅ Respawn avec délai si étoiles
- ✅ Logs détaillés affichent les spots libres
- ✅ Aucun impact sur les donjons
- ✅ Prêt pour production

---

**Version**: 1.1.0 (Correction du fonctionnement maps classiques)
**Date**: 2026-03-16
**Status**: ✅ **COMPLÉTÉ**

