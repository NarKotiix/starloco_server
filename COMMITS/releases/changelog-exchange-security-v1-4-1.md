# Changelog Exchange Security v1.4.1

## Objectif
Durcir le flux d'echange joueur<->joueur / PNJ pour reduire les risques de duplication, d'etats incoherents et de crash, puis retablir la compilation sur le lot IA/profiling.

## Correctifs code

### 1) Durcissement `PlayerExchange`
Fichier principal : `src/org/starloco/locos/entity/exchange/PlayerExchange.java`

- Validation explicite du participant (`getActorIndex`) avant mutation de l'echange.
- Centralisation du reset des statuts `ok1/ok2` (`resetReadyState`).
- Validation de quantite ajoutee (`getValidatedAddedQuantity`) :
  - quantite strictement positive,
  - possession reelle de l'objet,
  - position equipement valide,
  - borne max par stock reel disponible.
- `setKamas(...)` borne les kamas proposes au solde reel du joueur.
- `apply()` verifie le solde kamas des deux joueurs avant transfert.
- Clamp de quantite en transfert pour eviter les depassements si inventaire modifie pendant l'echange.
- Garde-fous anti-NPE dans le flux joueur/PNJ (`object == null`, `couple == null`, quantites invalides).

### 2) Correction compilation IA/profiling

- `src/org/starloco/locos/kernel/Config.java`
  - Ajout des champs manquants utilises par `IAProfiler`:
    - `AIProfiling`
    - `AIProfilingInvocationOnly`
    - `AIProfilingWarnMs`
  - Chargement de ces cles depuis `config.properties`.
  - Valeurs par defaut ajoutees lors de la generation auto du fichier config.

- `src/org/starloco/locos/game/GameClient.java`
  - Remplacement de `fight.queueAction(...)` (methode absente) par `fight.cast(...)` pour:
    - lancement de sort en combat,
    - attaque au corps-a-corps.

## Verification recommandee

1. Build compilation:

```powershell
.\gradlew.bat compileJava --no-daemon
```

2. Test manuel echange:
- proposer une quantite > stock reel: quantite bornee/refusee,
- retirer une quantite <= 0: ignore,
- proposer des kamas > solde: borne au solde disponible,
- valider un echange avec objet retire entre-temps: pas de crash, pas de duplication.

## Fichiers modifies

- `src/org/starloco/locos/entity/exchange/PlayerExchange.java`
- `src/org/starloco/locos/kernel/Config.java`
- `src/org/starloco/locos/game/GameClient.java`
- `README.md`
- `COMMITS/releases/security-hardening-v1-2-1.md`
- `COMMITS/releases/index-documentation-v1-2-0.md`
- `COMMITS/releases/changelog-recent-commits.md`


