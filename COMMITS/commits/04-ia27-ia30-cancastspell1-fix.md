# Fix IA27/IA30 : canCastSpell1 paramètre erroné + réévaluation ennemis après déplacement

## Résumé

Correction d'un bug dans `IA27` et `IA30` qui empêchait les monstres d'attaquer après s'être
déplacés, malgré des PA disponibles et l'ennemi à portée.

---

## Contexte

### Symptôme
Le monstre utilise tous ses PM pour se rapprocher du joueur, a encore des PA disponibles et
l'ennemi est à portée, mais n'attaque pas. Il continue de se déplacer à chaque cycle jusqu'à
épuisement de son budget d'actions (`count`), puis passe son tour via le guard `endTurn idle guard`.

### Cause racine : bug `canCastSpell1` avec paramètres inversés

Dans le bloc `moveToAttackIfPossible2` de `IA30.apply()` et `IA27.apply()` :

```java
// AVANT (bugué) — cellId est la cellule CIBLE de l'ennemi
if(fight.canCastSpell1(fighter, spellStats, fight.getMap().getCase(cellId), cellId)){
```

La signature de `canCastSpell1(Fighter caster, SortStats SS, GameCase cell, int targetCell)` :
- `cell` = cellule **cible** du sort (là où le sort atterrit)
- `targetCell` = cellule du **lanceur** (si > -1)

En passant `cellId` (= cellule de l'ennemi) comme `targetCell`, la méthode calcule :
```
dist = distance(cellId, cellId) = 0
```
Si le sort a `minPO > 0`, la condition `dist < minAlc` devient vraie → `canCastSpell1` retourne
`false` → le sort n'est jamais lancé → `action` reste `false` → l'IA retombe dans le déplacement
fallback → boucle.

**Les IA80, IA81, IA82, IA56 font déjà la bonne chose** en passant `this.fighter.getCell()` ou
`fighter.getCell().getId()` comme cellule du lanceur.

---

## Changements

### `src/org/starloco/locos/fight/ia/type/IA30.java`

1. **Fix `canCastSpell1`** : passage de `fighter.getCell().getId()` (cellule du lanceur) au lieu
   de `cellId` (cellule cible).
2. **Réévaluation des ennemis après déplacement** : ajout de la recalculation de `longestEnnemy`
   et `nearestEnnemy` immédiatement après `moveautourIfPossible`, comme le fait déjà `IA27`.
   Permet aux blocs d'attaque du même cycle de s'exécuter si l'IA est désormais à portée.

```java
// AVANT
if(fight.canCastSpell1(fighter, spellStats, fight.getMap().getCase(cellId), cellId)){

// APRÈS
if(fight.canCastSpell1(fighter, spellStats, fight.getMap().getCase(cellId), fighter.getCell().getId())){
```

### `src/org/starloco/locos/fight/ia/type/IA27.java`

1. **Fix `canCastSpell1`** : même correction que pour IA30.

---

## Comportement avant / après

| Situation | Avant | Après |
|-----------|-------|-------|
| IA à portée après déplacement, sort minPO > 0 | `canCastSpell1` → false → redéplacement boucle | `canCastSpell1` → true → attaque |
| IA à portée directe (pas de déplacement) | Non affecté | Identique |
| `moveToAttackIfPossible2` retourne position actuelle | Sort non lancé (dist=0 < minPO) | Sort lancé normalement |

---

## Fichiers affectés

- `src/org/starloco/locos/fight/ia/type/IA30.java` (IA basique, types 1, 15, 30)
- `src/org/starloco/locos/fight/ia/type/IA27.java` (IA basique type 27)

---

## Tests

- Build Gradle : `BUILD SUCCESSFUL` sans erreur de compilation.
- Test manuel : observer un combat PvM avec un monstre de type IA 1/15/30/27 → l'IA doit
  désormais attaquer après s'être déplacée au lieu de continuer à se repositionner.

---

## Notes et suivi

- Les IA 80, 81, 82, 56 passent déjà `this.fighter.getCell()` comme cellule du lanceur → non
  affectées par ce bug.
- Vérifier si d'autres IA (types > 30) utilisent le même pattern erroné avec
  `moveToAttackIfPossible2`.
- La condition de `moveToAttackIfPossible2` dans IA30 requiert `nearestEnnemy != null`
  (ennemi ≤ 2 cases) ; si l'ennemi est plus loin, c'est `attackIfPossible(highests)` qui gère
  l'attaque via `longestEnnemy`.

