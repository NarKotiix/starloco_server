# Résumé – Hardening Équipement & Commande Admin TauxDrop

**Date :** 2026-03-22  
**Commits :** C1 (équipement), C2 (admin)

---

## Contexte

- Des désynchronisations client/serveur étaient observées lors des doubles-clics rapides sur les items équipables.
- `QuickSet` et les opérations mimibiote manipulaient encore directement la position des objets sans invariants.
- Plusieurs flux inventaire/World pouvaient laisser des objets orphelins après fusion dans une pile existante.
- Les admins manquaient d'un outil pour consulter en jeu les taux de drop d'un monstre par grade.

---

## C1 – fix(equipment): hardening état équipement – mutation atomique, EQUIP_GUARD, GHOST_ITEM

### Changements

**`src/org/starloco/locos/client/Player.java`**
- Ajout de `getEquippedAt(int)` – lecture centralisée et synchronisée d'un slot équipé.
- Ajout de `getAllEquipped()` – snapshot safe de tous les items équipés.
- Ajout de `moveEquipmentAtomically(...)` – mutation atomique slot/position avec validation GUID.
- Ajout de `lastEquipChangeByGuid` + `getElapsedSinceLastEquipChange()` + `markEquipChange()` – suivi temporel par GUID pour le guard anti-double-clic.
- `getObjetByPos(int)` délègue désormais à `getEquippedAt(int)`.

**`src/org/starloco/locos/object/GameObject.java`**
- Ajout d'un verrou simple `lockedForTransform` (volatile) avec `tryLockForTransform()` / `unlockForTransform()`.

**`src/org/starloco/locos/game/GameClient.java`**
- `QuickSet` utilise désormais `getAllEquipped()` au lieu de `GetequipedObjects().values()`.
- `QuickSet` équip/déséquip passe par `moveEquipmentAtomically(...)`.
- `onMovementEquipUnequipItem` utilise `getEquippedAt(position)` + validation snapshot GUID.
- `onMovementEquipItem` et `onMovementUnEquipObject` passent par `moveEquipmentAtomically(...)`.
- `createMimibiote(...)` et `dissociateMimibiote(...)` posent un verrou `lockedForTransform` sur les objets concernés.
- Handler `movementObject(...)` : rejet des objets verrouillés pour transformation (`[EQUIP_GUARD]`).
- Handler `movementObject(...)` : rejet des mouvements trop rapprochés sur un même GUID (`[EQUIP_GUARD] reason=DOUBLE_CLICK`).
- `buy(...)` nettoie l'objet source du `World` lorsqu'il fusionne dans une pile existante (`[GHOST_ITEM]`).
- Flux HDV : ajout du clone au `World` différé jusqu'après la création logique de l'entrée.
- Ajout de `logEquipmentMoveFailure(...)` et `canProcessEquipmentMove(...)` comme helpers internes.
- Constante `EQUIP_DOUBLE_CLICK_GUARD_MS = 200L`.

**`src/test/java/org/starloco/locos/object/GameObjectTransformLockTest.java`**
- Nouveau test unitaire : validation du verrou `lockedForTransform` (tryLock / unlock / re-tryLock).

**`COMMITS/topics/2026-03-21-diagnostic-equipment-bug-upstream-race-condition.md`**
- Diagnostic détaillé de la race condition équipement upstream.

**`COMMITS/topics/2026-03-21-fix-rapid-equip-unequip-bug.md`**
- Solution GUID snapshot et re-validation.

**`COMMITS/topics/2026-03-21-equipment-state-hardening-and-ghost-items.md`**
- Document complet du hardening : invariants visés, changements effectués, logs ajoutés.

### Comportement avant / après

| Aspect | Avant | Après |
|---|---|---|
| Lecture slot équipé | Scan complet inventaire (non synchronisé) | `getEquippedAt` : lecture directe dans `equipedObjects` (synchronisée) |
| Mutation équipement | `setPosition()` direct + `equipItem()`/`unEquipItem()` éparpillés | `moveEquipmentAtomically(...)` centralisé avec validation GUID |
| Double-clic rapide | Item aléatoire peut apparaître dans un slot | Rejeté par guard 200 ms (`[EQUIP_GUARD] reason=DOUBLE_CLICK`) |
| Objet en transformation | Peut être déplacé simultanément | Verrou `lockedForTransform` bloque le `OM` concurrent |
| Ghost items | Objet orphelin possible après fusion | Nettoyage World explicite + log `[GHOST_ITEM]` |

### Tests

- `./gradlew.bat compileJava` → BUILD SUCCESSFUL
- `./gradlew.bat compileTestJava` → BUILD SUCCESSFUL
- `./gradlew.bat test` → BUILD SUCCESSFUL

---

## C2 – feat(admin/config): commande TD/TAUXDROP + flag MOB_AGGRESSION

### Changements

**`src/org/starloco/locos/command/CommandAdmin.java`**
- Ajout des méthodes privées `getDropPercentForGrade(drop, grade)` et `getBestDropPercent(drop)`.
- Nouveau bloc de commande `TD` / `TAUXDROP` : affiche tous les drops d'un monstre (par ID) avec :
  - Nom de l'item (ou `Item#ID` si absent)
  - Taux par grade G1..G5
  - Ceil, action, level, condition
  - Tri décroissant par meilleur taux de drop
- Accessible aux administrateurs uniquement (bloqué pour les groupes non-admin).

**`src/org/starloco/locos/kernel/Config.java`**
- Ajout du flag runtime `mobAggression` (par défaut `true`).
- Chargement de la propriété `MOB_AGGRESSION` depuis `config.properties`.

**`src/org/starloco/locos/area/map/GameMap.java`**
- `isAggroByMob(...)` respecte désormais `Config.singleton.mobAggression`.
- Permet de couper l'aggro des groupes de mobs sans désactiver le mouvement des groupes.

### Utilisation

```
/TD [monsterId]
/TAUXDROP [monsterId]
```

Exemple de sortie :
```
[TD] monsterId=200  drops=12
Format: [itemId] nom  G1..G5  ceil  action  level  condition
- [1234] Épée de bois  G1=5.000% G2=10.000% G3=15.000% G4=20.000% G5=25.000%  ceil=100  action=1  level=1
```

### Tests

- `./gradlew.bat compileJava` → BUILD SUCCESSFUL
- `./gradlew.bat test` → BUILD SUCCESSFUL

---

## Notes et suivi

- Les préfixes de log `[EQUIP_GUARD]` et `[GHOST_ITEM]` sont dans `server.log` et `errors.log`.
- La fenêtre de guard 200 ms est réglable via la constante `EQUIP_DOUBLE_CLICK_GUARD_MS` dans `GameClient`.
- Prochaines étapes possibles :
  - Étendre le hardening aux autres flux inventaire non encore couverts.
  - Ajouter un outillage GM pour rejouer deux paquets `OM` successifs sur un même GUID en test.
  - Isoler `QuickSet` dans un lot de vérification fonctionnelle dédié en jeu.


