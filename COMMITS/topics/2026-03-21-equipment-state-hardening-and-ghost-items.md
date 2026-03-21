# Hardening équipement: état centralisé, guard double-clic et réduction des ghost items

## Contexte
- Des désynchronisations client/serveur étaient observées lors des double-clics rapides sur les items équipables.
- `QuickSet` et les opérations mimibiote manipulaient encore directement la position des objets.
- Plusieurs flux inventaire/World pouvaient laisser des objets orphelins après fusion dans une pile existante.

## Invariants visés
- Tous les mouvements d'équipement passent par une mutation atomique côté `Player`.
- La lecture d'un slot équipé s'appuie sur `equipedObjects`, plus sur un scan d'inventaire.
- Un paquet `OM` récent sur le même GUID est ignoré pendant une courte fenêtre de garde.
- Un objet verrouillé pour transformation ne peut pas être déplacé via `OM`.
- Aucun nettoyage `World` critique ne doit être oublié quand un objet fusionne dans une pile existante.

## Changements effectués
- `Player`
  - ajout de `getEquippedAt(int)` et `getAllEquipped()` comme API de lecture centralisée ;
  - ajout de `moveEquipmentAtomically(...)` pour centraliser la mutation slot/position ;
  - ajout du suivi `lastEquipChangeByGuid` et de méthodes de timestamp associées ;
  - `GetequipedObjects()` passe en compatibilité lecture seule via copie.
- `GameClient`
  - `QuickSet` lit désormais l'équipement via l'API `Player` et ne fait plus de `setPosition(...)` direct pour l'équipement ;
  - `onMovementEquipUnequipItem`, `onMovementEquipItem` et `onMovementUnEquipObject` utilisent la mutation atomique ;
  - le handler `movementObject(...)` rejette les objets verrouillés pour transformation ;
  - le handler `movementObject(...)` rejette les mouvements trop rapprochés sur un même GUID (`[EQUIP_GUARD]`) ;
  - `buy(...)` nettoie l'objet marchand source du `World` lorsqu'il est fusionné dans une pile existante ;
  - le flux HDV retarde l'ajout du clone au `World` jusqu'après la création logique de l'entrée ;
  - `createMimibiote(...)` et `dissociateMimibiote(...)` posent un verrou temporaire sur les objets concernés.
- `GameObject`
  - ajout d'un verrou simple `lockedForTransform` avec `tryLockForTransform()` / `unlockForTransform()`.

## Logs ajoutés
- Préfixe `[EQUIP_GUARD]`
  - rejet de double-clic ;
  - rejet d'un `OM` sur objet verrouillé ;
  - échec de mutation atomique.
- Préfixe `[GHOST_ITEM]`
  - nettoyage d'un objet devenu inutile après fusion dans une pile.

## Tests
- Ajout de `src/test/java/org/starloco/locos/object/GameObjectTransformLockTest.java` pour valider le comportement du verrou de transformation.
- La compilation et les tests Gradle doivent être relancés après ce lot pour vérifier l'absence de régression.

## Suite recommandée
- Étendre le même hardening aux autres flux inventaire sensibles non encore couverts.
- Ajouter un outillage GM/QA pour rejouer automatiquement deux paquets `OM` successifs sur un même GUID.
- Isoler `QuickSet` dans un lot de vérification fonctionnelle dédié en jeu.

