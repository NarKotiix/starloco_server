# Security Hardening v1.2.1

## Objectif
Réduire les risques de crash, états incohérents et flood CPU/logs via des paquets de déplacement malformés.

## Changements appliqués

### 1) Fail-fast sur parsing mouvement (`GameClient`)
- Validation de longueur du path (`>= 3`).
- Validation stricte du décodage cellule (`cellCode_To_ID` puis `getCase != null`).
- Validation de l'orientation décodée (`>= 0`).
- Rejet immédiat propre (`GA 0` + `removeAction`) en cas d'invalidité.

### 2) Anti-flood léger (invalid move)
- Fenêtre glissante par client :
  - `6` paquets invalides sur `2s` max.
  - `1s` de cooldown ensuite.
- But : freiner le spam sans kick agressif ni impact visible pour les joueurs normaux.

### 3) Journalisation throttlée
- Ajout d'un log `WARN` limité à 1 toutes les `5s` par client.
- Les logs supprimés dans l'intervalle sont agrégés (`N suppression(s)`).
- Permet la visibilité sécurité sans polluer les logs en cas de flood.

## Impact attendu
- Moins de NPE/erreurs liées aux cellules invalides.
- Réduction du coût serveur sur spam de déplacements malformés.
- Logs exploitables en production (signal utile > bruit).

## Fichiers concernés
- `src/org/starloco/locos/game/GameClient.java`
- `src/org/starloco/locos/common/CryptManager.java`
- `src/org/starloco/locos/common/PathFinding.java`

## Vérification rapide
1. Envoyer des paquets déplacement valides: comportement identique.
2. Envoyer des paquets invalides (cellule non décodable): rejet propre, pas de crash.
3. Flood invalide: cooldown actif + logs `WARN` agrégés.

