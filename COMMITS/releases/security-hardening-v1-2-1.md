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

### 4) Durcissement SQL (anti-injection)
- Remplacement de requêtes concaténées par des `PreparedStatement` paramétrés sur les chemins exposés.
- `PlayerData.exist(name)` passe de `getData("..." + name)` à `SELECT ... WHERE name = ?`.
- `AccountData.loadPointsWithoutUsersDb(user)` passe de `getData("..." + user)` à `SELECT points ... WHERE account = ?`.
- `AccountData.loadPointsWithUsersDb(account)` passe en double requête paramétrée (`account = ?`, puis `users.id = ?`).
- Objectif : supprimer les vecteurs SQLi sur les entrées texte (`nom personnage`, `nom compte`).

## Impact attendu
- Moins de NPE/erreurs liées aux cellules invalides.
- Réduction du coût serveur sur spam de déplacements malformés.
- Logs exploitables en production (signal utile > bruit).

## Fichiers concernés
- `src/org/starloco/locos/game/GameClient.java`
- `src/org/starloco/locos/common/CryptManager.java`
- `src/org/starloco/locos/common/PathFinding.java`
- `src/org/starloco/locos/database/statics/data/PlayerData.java`
- `src/org/starloco/locos/database/statics/data/AccountData.java`

## Vérification rapide
1. Envoyer des paquets déplacement valides: comportement identique.
2. Envoyer des paquets invalides (cellule non décodable): rejet propre, pas de crash.
3. Flood invalide: cooldown actif + logs `WARN` agrégés.

---

## Addendum v1.4.1 - Echanges joueurs/PNJ

### Durcissement applique (`PlayerExchange`)
- Validation stricte de l'acteur d'echange avant toute mutation (`getActorIndex`).
- Remise a zero centralisee des statuts de validation (`resetReadyState`).
- Validation de quantites ajoutees (bornes reelles d'inventaire, quantite > 0).
- Clamp de quantite au moment du transfert pour eviter depassements/duplication si inventaire change en cours d'echange.
- Clamp des kamas proposes au solde reel du joueur.
- Revalidation des soldes kamas dans `apply()` avant transfert final.
- Garde-fous anti-NPE dans les flux NPC (`NpcExchange`, `NpcExchangePets`, `NpcRessurectPets`).

### Correctif compilation associe
- `Config` expose de nouveau les cles de profiling IA attendues par `IAProfiler`:
  - `AIProfiling`
  - `AIProfilingInvocationOnly`
  - `AIProfilingWarnMs`
- `GameClient` remplace les appels a `fight.queueAction(...)` (inexistant) par `fight.cast(...)`.

### Details
- Voir [`changelog-exchange-security-v1-4-1.md`](./changelog-exchange-security-v1-4-1.md).


