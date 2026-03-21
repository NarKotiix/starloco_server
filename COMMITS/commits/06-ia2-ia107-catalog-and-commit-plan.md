# Resume

## Contexte
- Besoin d'une documentation exhaustive des IA de 2 a 107 pour accelerer le debug et l'onboarding.
- Besoin de preparer des commits propres pour les changements locaux non pousses.

## Changements
- Ajout de `docs/ia2-ia107-catalog.md`.
  - Inventaire IA2-IA107.
  - Role de chaque ID IA selon `IAHandler`.
  - Presence/absence des classes `IA*.java`.
  - Base technique (`AbstractIA` vs `AbstractNeedSpell`) et budget de decision.
  - Comportement concret synthetise (soin, buff, invocation, fuite, rush cac, cast apres placement, teleportation).
- Mise a jour de `docs/README.md` pour lier le nouveau catalogue.

## Tests
- Verification statique de coherence documentaire:
  - chemins des fichiers IA valides,
  - alignement avec `IAHandler`.
- Pas de changement runtime Java dans ce lot.

## Notes
- Certains IDs (ex: 3, 4, 7, 11, 13, 83, 84, 86-99) sont non mappes/non implementes dans l'etat actuel.
- IA 15 est mappee dans `IAHandler` mais ne possede pas de fichier `IA15.java` (heritage sur IA30 dans le switch).

