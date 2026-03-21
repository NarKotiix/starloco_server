# Diagnostic 2 - Fin de combat donjon non teleporte + NPE PreparedStatement

## Contexte
- Symptomatique reportee:
  - Pas de teleportation vers la salle suivante a la fin d'un combat donjon.
  - Erreur console:
    - `Cannot invoke "java.sql.PreparedStatement.setInt(int, int)" because "p" is null`
- Logs visibles dans `GameHandler.exceptionCaught(...)`:
  - `Exception connexion client : ...`

## Analyse technique
- Le flux de fin de combat prepare l'action de fin via:
  - `Fight.endFight(...)`
  - `GameClient.getExtraInformations()`
  - `GameMap.applyEndFightAction(...)`
  - `Action.apply(...)` (case `15`/`16` pour teleport donjon)
- Le NPE indique un pattern DAO classique:
  - `PreparedStatement p = getPreparedStatement(...);`
  - `p.setInt(...)` alors que `p == null`.
- Cause racine probable:
  - `getPreparedStatement(...)` dans `database/statics/AbstractDAO` et `database/dynamics/AbstractDAO`
    pouvait retourner `null` apres echec/reinit de datasource.
  - Les DAO appelants catchent `SQLException`, pas `NullPointerException`.
  - Le NPE remonte sur le thread client et coupe le traitement du flux (effet utilisateur: sequence de fin de combat inachevee/cassee).

## Correctif applique
- Fichiers modifies:
  - `src/org/starloco/locos/database/statics/AbstractDAO.java`
  - `src/org/starloco/locos/database/dynamics/AbstractDAO.java`
- Changement:
  - `getPreparedStatement(...)` ne retourne plus `null`.
  - En cas d'echec:
    1. fermeture datasource,
    2. tentative de reinitialisation,
    3. retry unique de creation de `PreparedStatement`,
    4. si echec, propagation `SQLException`.
- Benefice:
  - Evite les NPE `p.setInt(...)` / `p.setString(...)`.
  - Les erreurs restent dans le circuit d'exception SQL deja gere par les DAO.

## Validation
- Build/test cible execute:
  - `./gradlew.bat test --tests org.starloco.locos.entity.monster.MobGroupStarProgressionTest`
- Resultat:
  - `BUILD SUCCESSFUL`

## Verification en jeu conseillee
1. Reproduire le donjon qui posait probleme.
2. Finir un combat avec action de fin de type teleport (`Action` id 15/16).
3. Verifier:
   - teleport effectif en salle suivante,
   - absence de log `p is null`.
4. Si un probleme persiste:
   - recuperer la stacktrace complete (classe + ligne) cote serveur pour identifier le DAO exact encore fragile.

## Notes
- Ce correctif est defensif/global et limite le risque de regression sur d'autres flux (house/guild/trunk/map updates, etc.).
- En cas de nouvelle occurrence, prioriser la stacktrace complete pour corriger le site appelant precis.

