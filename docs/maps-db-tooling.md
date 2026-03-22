# Outillage DB maps (comparaison et migration)

Ce guide decrit l'outillage `Outils/db/` introduit pour comparer deux dumps `maps` et produire des scripts SQL de migration utilisables de maniere controlee.

## Objectif

- Comparer un dump ancien et un dump nouveau de la table `maps`.
- Identifier les differences de schema et de donnees.
- Generer des scripts SQL explicites pour appliquer les changements en base.

## Fichiers principaux

- Scripts:
  - `Outils/db/compare-maps-schema.ps1`
  - `Outils/db/convert-maps-dump-to-explicit-columns.ps1`
  - `Outils/db/diff-maps-ids.ps1`
  - `Outils/db/analyze-maps-diff.py`
  - `Outils/db/generate-maps-diff-inserts.py`
- Entrees/snapshots:
  - `Outils/db/maps_ancien.sql`
  - `Outils/db/maps_new.sql`
- Sorties generees (selon execution):
  - `Outils/db/maps_diff_inserts.sql`
  - `Outils/db/maps_functional_changes.sql`
  - `Outils/db/maps_gameplay_changes.sql`
  - `Outils/db/maps_missing_in_new_from_ancien*.sql`
  - `Outils/db/maps_new_only.sql`
  - `Outils/db/maps_10026_fix.sql`
  - `Outils/db/maps_missing_in_new_ids.txt`

## Workflow recommande (Windows PowerShell)

1. Comparer le schema ancien/nouveau.
2. Normaliser les dumps pour obtenir des colonnes explicites.
3. Produire les diffs de contenu et scripts SQL.
4. Relire les scripts avant execution en base.

## Commandes type

```powershell
Set-Location "H:\server_dofus\Starloco-Fun\Server"

# 1) Schema
powershell -ExecutionPolicy Bypass -File .\Outils\db\compare-maps-schema.ps1

# 2) Normalisation (si necessaire)
powershell -ExecutionPolicy Bypass -File .\Outils\db\convert-maps-dump-to-explicit-columns.ps1

# 3) Diff IDs
powershell -ExecutionPolicy Bypass -File .\Outils\db\diff-maps-ids.ps1

# 4) Analyse + generation SQL
python .\Outils\db\analyze-maps-diff.py
python .\Outils\db\generate-maps-diff-inserts.py
```

## Precautions

- Toujours faire un backup de la base avant d'appliquer un script SQL genere.
- Verifier les cas limites gameplay (spawns, cellules, triggers) avant merge/production.
- Conserver les scripts dans `Outils/db/` pour garder un historique technique clair.

## Historique associe

- Historique de session: `COMMITS/commits/09-db-maps-tooling-session-2026-03-22.md`

