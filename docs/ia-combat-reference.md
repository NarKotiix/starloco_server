# IA Combat Reference

Ce document sert de reference durable pour comprendre rapidement le comportement de l'IA combat,
localiser les points de controle critiques, et accelerer les diagnostics lors des regressions.

## Portee

- Moteur IA de combat PvM/PvP cote serveur.
- Flux d'execution d'un tour IA (selection, decision, action, fin de tour).
- Points sensibles: deplacements, casts, verrous `curAction`/`traped`, scheduler IA.

## Lecture rapide recommandee

1. `src/org/starloco/locos/fight/ia/IAHandler.java`
2. `src/org/starloco/locos/fight/ia/AbstractIA.java`
3. `src/org/starloco/locos/fight/Fight.java` (`onFighterDeplace`, `tryCastSpell`, `canCastSpell1`)
4. `src/org/starloco/locos/fight/ia/util/Function.java` (`attackIfPossible`, `moveToAttackIfPossible2`)
5. IA specialisees selon le type mob (`src/org/starloco/locos/fight/ia/type/IA*.java`)

## Pipeline d'un tour IA

1. `IAHandler` selectionne la classe IA selon le type du mob.
2. `apply()` de l'IA execute une strategie (buff, invocation, deplacement, attaque).
3. Chaque action planifiee passe via `AbstractIA.addNext(...)`.
4. `Fight` applique les actions:
   - Deplacement: `onFighterDeplace(...)`
   - Cast: `tryCastSpell(...)` + validation `canCastSpell1(...)`
5. `AbstractIA.endTurn()` termine le tour si `stop == true`.

## Fichiers et roles

- `src/org/starloco/locos/fight/ia/AbstractIA.java`
  - scheduler commun des IA
  - garde anti-blocage (`scheduler blocked`, `endTurn idle guard`)
- `src/org/starloco/locos/fight/Fight.java`
  - verrous d'action (`curAction`, `traped`)
  - logique de deplacement/cast et verification de tour courant
- `src/org/starloco/locos/fight/ia/util/Function.java`
  - heuristiques tactiques (choix cible, cellule, sort)
- `src/org/starloco/locos/fight/ia/type/IA27.java`, `IA30.java`
  - cas frequents de mobs offensifs simples

## Verrous critiques a surveiller

- `fight.isCurAction()`
  - doit repasser a `false` apres la fin d'une action
  - si bloque a `true`, le scheduler continue de repousser les actions
- `fight.isTraped()`
  - bloque temporairement les actions durant certains traitements (pieges)
- `stop` (dans `AbstractIA`)
  - si jamais positionne, `endTurn` finit par forcer un pass via garde idle

## Decision de cast: checkpoints

`canCastSpell1(caster, spell, targetCell, casterCellOverride)` valide notamment:

- PA suffisants
- cellule cible valide
- contrainte de ligne (sort en ligne)
- ligne de vue
- distance dans `[minPO, maxPO]`
- cooldown et nombre de lancers

`tryCastSpell(...)` echoue immediatement (retour `10`) si:

- ce n'est pas le tour du fighter
- `curAction == true`
- `traped == true`
- sort/cible invalides

## Symptomes courants et causes probables

### 1) "L'IA se deplace mais n'attaque pas"

Causes frequentes:

- Re-evaluation des cibles non faite apres deplacement.
- Deplacement marque comme `action=true`, ce qui bloque les branches d'attaque du meme cycle.
- Mauvais parametres `canCastSpell1` (cellule lanceur/cible incoherentes).
- Cast tente alors que `curAction` est encore actif.

### 2) "Passage de tour par idle guard"

Indique souvent:

- aucune action valide trouvee dans `apply()`
- ou boucle de decision sans bascule `stop=true`

### 3) "scheduler blocked curAction=true"

Indique qu'une action est encore en cours ou un verrou non relache.

## Corrections deja appliquees (session 2026-03-21)

- `Fight.onFighterDeplace(...)`: release explicite de `curAction` pour les mobs apres deplacement.
- `IA27` / `IA30`:
  - correction `canCastSpell1` (cellule lanceur correcte)
  - re-evaluation d'ennemis apres deplacement
  - autorisation d'attaque dans le meme cycle si la portee est ouverte par le deplacement

Voir aussi:

- `COMMITS/commits/04-ia27-ia30-cancastspell1-fix.md`

## Procedure de diagnostic rapide

1. Reproduire en combat simple (1 mob IA cible + 1 joueur).
2. Noter heure exacte du tour et fighter concerne.
3. Correl er:
   - `Logs/AIProfiling/ai_profiling.log`
   - `Logs/errors.log`
   - `Logs/server.log`
4. Verifier dans l'ordre:
   - `curAction` reste-t-il actif trop longtemps ?
   - `canCastSpell1` peut-il reussir depuis la nouvelle cellule ?
   - `tryCastSpell` renvoie-t-il `10` ?
   - l'IA recalcule-t-elle bien ses ennemis apres move ?

## Check-list pre-correctif

- Identifier le type IA exact du mob (IA27, IA30, IA80, etc.).
- Verifier la valeur `minPO` du sort principal.
- Verifier si le sort exige ligne de vue ou lancer en ligne.
- Verifier si le bloc d'attaque est conditionne par `!action` apres un move.
- Verifier que les branches fallback de deplacement ne masquent pas le cast.

## Check-list post-correctif

- Le mob attaque dans le meme tour apres deplacement a portee.
- Aucune boucle de deplacement inutile jusqu'au `endTurn idle guard`.
- Pas de pic anormal de `scheduler blocked` sur le meme fighter.
- Pas de regression sur les IA deja stables (80/81/82/56).

## Tests manuels minimum

- Cas A: ennemi deja a portee, sans deplacement.
- Cas B: ennemi hors portee, deplacement puis attaque meme tour.
- Cas C: ennemi hors ligne de vue, deplacement tactique puis attaque.
- Cas D: sort avec `minPO > 0` pour verifier le comportement hors cac strict.

## Limites

- Cette reference decrit la logique principale et les incidents frequents.
- Pour les details d'une regression precise, creer une page d'analyse dans `COMMITS/topics/`.

