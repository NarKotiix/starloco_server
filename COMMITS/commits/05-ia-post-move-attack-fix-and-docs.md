# Resume

## Contexte

- Regression observee: certaines IA se deplacent vers la cible mais n'attaquent pas dans le meme tour
  alors qu'il reste des PA.
- Besoin complementaire: disposer d'une documentation IA durable pour accelerer les futurs diagnostics.

## Changements

- `src/org/starloco/locos/fight/ia/type/IA30.java`
  - autorise l'attaque dans le meme cycle si un deplacement de rapprochement ouvre la portee
  - elargit le declenchement de `moveToAttackIfPossible2` a toute cible `ennemy != null`
- `src/org/starloco/locos/fight/ia/type/IA27.java`
  - autorise l'attaque dans le meme cycle si le deplacement ouvre la portee
- `docs/ia-combat-reference.md`
  - nouvelle reference IA (pipeline, verrous, checkpoints cast, symptomes, check-lists)
- `docs/ia-combat-diagnostic-playbook.md`
  - playbook de triage incident IA (logs, hypotheses, validations)
- `docs/README.md`
  - ajout des liens vers les nouveaux guides IA

## Tests

- Compilation Java du projet apres modification.
- Validation manuelle ciblee a realiser en combat:
  - deplacement puis attaque dans le meme tour pour IA27/IA30
  - absence de boucle `endTurn idle guard` sur le scenario corrige

## Notes

- Cette mise a jour est orientee comportement IA de base (IA27/IA30).
- Les IA specialisees (80/81/82/56) restent a verifier en non-regression selon la matrice du playbook.

