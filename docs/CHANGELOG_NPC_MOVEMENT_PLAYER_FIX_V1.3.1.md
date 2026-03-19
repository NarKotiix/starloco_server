# Changelog v1.3.1 - NPC Movement Toggle + Player Move Fix

## Date
19 Mars 2026

## Objectif
- Desactiver le deplacement automatique des PNJ sur toutes les maps via configuration.
- Corriger une regression de deplacement joueur (reception de `BN` sans animation de marche).

## Changements appliques

### 1) Nouveau flag de configuration `NPC_MOVEMENT`
- Ajout du booleen `npcMovement` dans `Config`.
- Lecture de la propriete `NPC_MOVEMENT` depuis `config.properties`.
- Conditionnement de l'appel global `NpcMovable.moveAll()` dans `GameMap.updatable`.

Resultat:
- `NPC_MOVEMENT=true` : comportement historique (PNJ mobiles).
- `NPC_MOVEMENT=false` : aucun deplacement PNJ (global, toutes maps).

### 2) Correctif de blocage de deplacement joueur (`GameClient`)
- Suppression d'un retrait premature du joueur de sa cellule dans `gameParseDeplacementPacket`.
- Suppression d'un `GAME_SEND_BN` premature dans le meme bloc.

Cause du bug:
- Le joueur etait retire de la cellule avant l'envoi du paquet d'animation `GA` map.
- Le client ne recevait pas l'animation attendue, puis n'envoyait pas correctement l'ack de fin (`GKA`).

Resultat:
- Le flux de deplacement normal est restaure.
- Le traitement de fin de deplacement reste gere dans `actionAck`.

## Fichiers modifies
- `src/org/starloco/locos/kernel/Config.java`
- `src/org/starloco/locos/area/map/GameMap.java`
- `src/org/starloco/locos/game/GameClient.java`

## Verification rapide
1. Demarrer le serveur avec `NPC_MOVEMENT=false`.
2. Verifier qu'aucun PNJ mobile ne change de cellule.
3. Verifier qu'un joueur peut se deplacer normalement (animation + arrivee cellule).
4. Optionnel: basculer `NPC_MOVEMENT=true` pour confirmer le retour du deplacement PNJ.

