# IA Combat Diagnostic Playbook

Playbook operationnel pour diagnostiquer rapidement un incident IA en combat.

## Objectif

- Passer d'un symptome en jeu a une cause technique verifiable.
- Produire une correction ciblee avec risque limite.

## Entrees minimales a collecter

- Heure approximative du tour impacte.
- Map / contexte (donjon, combat normal, invocation, etc.).
- Type de monstre ou ID template si disponible.
- Symptome exact:
  - deplacement sans attaque
  - pass de tour
  - erreur serveur associee

## Sequence de triage

### Etape 1 - Correlation logs

Fichiers:

- `Logs/AIProfiling/ai_profiling.log`
- `Logs/errors.log`
- `Logs/server.log`

Indices prioritaires:

- `endTurn idle guard`
- `scheduler blocked ... curAction=true`
- `slow method=ia.apply`
- erreurs de type `Game > Create/Delete action`

### Etape 2 - Identifier l'IA cible

- Relever le fighter (id negatif cote mobs).
- Retrouver le type IA du monstre concerne.
- Ouvrir la classe `src/org/starloco/locos/fight/ia/type/IAxx.java` correspondante.

### Etape 3 - Verifier le cycle de decision

Dans `apply()`:

- branch `move` avant `attack` ?
- variable `action` bloque-t-elle les branches d'attaque (`!action`) ?
- re-evaluation des ennemis faite apres deplacement ?
- fallback de deplacement declenche en boucle ?

### Etape 4 - Verifier le moteur de cast

Dans `Fight.java`:

- `tryCastSpell(...)` retour `10` si `curAction`/`traped`/tour invalide.
- `canCastSpell1(...)` peut refuser le cast (PA, PO, LoS, ligne, cooldown).

### Etape 5 - Verifier le deplacement

Dans `onFighterDeplace(...)`:

- release de `curAction` apres le deplacement mob.
- pas de return anticipe laissant le verrou actif.

## Matrice symptome -> hypotheses

- Deplacement vers cible puis aucun cast:
  - H1: cibles non re-evaluees apres move
  - H2: `action=true` bloque l'attaque du meme cycle
  - H3: `canCastSpell1` faux negatif (PO min, LoS, parametres)
  - H4: `tryCastSpell` refuse (`curAction` encore actif)

- Pass de tour par idle guard:
  - H5: `stop` jamais bascule
  - H6: aucune action valide trouvee

## Validation d'un correctif

Un correctif est valide si:

1. Le mob peut attaquer apres deplacement dans le meme tour (cas attendu).
2. Les logs ne montrent plus de boucle `idle guard` sur le scenario corrige.
3. Le comportement des autres IA n'est pas degrade.

## Scenarios de regression a rejouer

- IA27: cible hors portee au debut, puis a portee apres move.
- IA30: cible plus loin que 2 cases, deplacement tactique puis cast.
- IA80/81/82/56: verifier absence de regression de cast.

## Sortie attendue de l'analyse

- Fichier d'analyse dans `COMMITS/topics/` avec:
  - contexte
  - cause racine
  - correctif
  - tests
- Si changement durable de fonctionnement IA:
  - mise a jour `docs/ia-combat-reference.md`

## Liens utiles

- `docs/ia-combat-reference.md`
- `docs/ia100-optimization-technical-guide.md`
- `COMMITS/commits/04-ia27-ia30-cancastspell1-fix.md`

