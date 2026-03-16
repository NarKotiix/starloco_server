# Guide : Arrêt Gracieux du Serveur StarLoco

## 🛑 Comment arrêter le serveur proprement

### Méthode 1 : CTRL+C (Recommandé) ⭐

Dans la console du serveur, appuyez une seule fois sur **CTRL+C** :

```
Démarrage du serveur StarLoco...
Chemin du JAR: build\libs\Server-1.0.0.jar
Encodage: UTF-8
Support couleurs: ANSI

[... logs du serveur ...]

^C  <-- Appuyez UNE SEULE FOIS sur CTRL+C
```

✅ **Un seul CTRL+C suffit !** Le serveur sauvegarde les données puis se ferme automatiquement.

### Méthode 2 : Fermer la fenêtre

Si vous fermez la fenêtre de console, le shutdown hook se déclenche automatiquement.

---

## 📊 Processus d'arrêt détaillé

Le serveur suivra **4 étapes** pour arrêter proprement, puis se ferme automatiquement :

### Étape 1 : Arrêt des nouvelles connexions
```
Step 1/4 - Stopping new connections...
```
- **Durée** : ~100ms
- **Action** : Le serveur rejette toutes les nouvelles tentatives de connexion
- **Effet** : Les joueurs ne peuvent plus se connecter

### Étape 2 : Sauvegarde du monde
```
Step 2/4 - Saving world data (players, objects, mounts, etc)...
-> of accounts.
-> of players.
-> of objects.
-> of mounts.
```
- **Durée** : 1-5 secondes (dépend du nombre de joueurs)
- **Action** : Toutes les données du monde sont écrites en base de données
- **Effet** : Aucun changement n'est perdu

### Étape 3 : Déconnexion des joueurs
```
Step 3/4 - Disconnecting all players and saving their data...
```
- **Durée** : 1-3 secondes
- **Action** : Les joueurs reçoivent une notification et leurs données sont finalisées
- **Effet** : Les sessions sont correctement fermées

### Étape 4 : Fermeture des bases de données
```
Step 4/4 - Closing database connections...
```
- **Durée** : ~500ms
- **Action** : Les connexions MySQL sont fermées proprement
- **Effet** : Aucune opération en attente

### Fin
```
═══════════════════════════════════════════════════════════
  ✅ SERVER SHUTDOWN COMPLETE - All data saved
═══════════════════════════════════════════════════════════
The server is now closed.
```

---

## ⏱️ Temps total d'arrêt

**Temps estimé : 3-10 secondes**

Cela dépend de :
- 📊 Nombre de joueurs en ligne
- 💾 Nombre d'objets à sauvegarder
- 🏠 Nombre de maisons/donjons actifs
- 🔌 Performance de la base de données

---

## ❌ Ce qu'il NE FAUT PAS faire

### ❌ Forcer la fermeture de la console (Alt+F4)
```
RISQUE : Peut causer un rollback des données non sauvegardées
```

### ❌ Tuer le process Java sans gracieux shutdown
```
RISQUE : Corruption de la base de données possible
```

### ❌ Débrancher le serveur/arrêter Windows sans passer par CTRL+C
```
RISQUE : Perte de données importante
```

---

## ✅ Ce qu'il FAUT faire

### ✅ Appuyer une fois sur CTRL+C dans la console
```
✅ Un seul CTRL+C suffit !
✅ Attendez les 4 étapes d'arrêt (2-10 secondes)
✅ La console se ferme automatiquement
```

### ✅ La console se ferme toute seule
```
Ne pas appuyer 2 fois sur CTRL+C
Le shutdown hook ferme complètement le processus
```

### ✅ Vérifier le message de confirmation
```
✅ SERVER SHUTDOWN COMPLETE - All data saved
↓
[Console se ferme automatiquement]
```

---

## 🔍 Vérification des données sauvegardées

Après un arrêt gracieux, vous pouvez vérifier les logs :

```bash
# Voir les logs d'arrêt
cat Logs/server.log | grep -E "SHUTDOWN|Step|COMPLETE"

# Ou directement
.\Start-Server.bat  # Redémarrer le serveur
```

Au redémarrage, tous les changements avant l'arrêt seront restaurés.

---

## 📝 Exemple complet

```
PS H:\server_dofus\Starloco-Fun\Server> .\Start-Server.bat

Démarrage du serveur StarLoco...
Chemin du JAR: build\libs\Server-1.0.0.jar
Encodage: UTF-8
Support couleurs: ANSI

19:40:00.000 | INFO  | org.starloco.locos.kernel.Main - You use Oracle Corporation with the version 1.8.0_481
19:40:01.000 | INFO  | org.starloco.locos.kernel.Main - The server is ready ! Waiting for connection..
19:40:15.000 | INFO  | org.starloco.locos.kernel.GameServer - Player1 connected
19:40:20.000 | INFO  | org.starloco.locos.kernel.GameServer - Player2 connected
19:40:45.000 | INFO  | org.starloco.locos.kernel.GameServer - Player1 has earned 1000 XP

--- Utilisateur appuie sur CTRL+C ---

╔════════════════════════════════════════════════════════════╗
║  SHUTDOWN SIGNAL RECEIVED - Saving data before closing...  ║
╚════════════════════════════════════════════════════════════╝

19:45:30.123 | WARN  | org.starloco.locos.kernel.Main - ═══════════════════════════════════════════════════════════
19:45:30.124 | WARN  | org.starloco.locos.kernel.Main -   SERVER SHUTDOWN INITIATED - CTRL+C / Shutdown Signal
19:45:30.125 | WARN  | org.starloco.locos.kernel.Main - ═══════════════════════════════════════════════════════════
19:45:30.126 | INFO  | org.starloco.locos.kernel.Main - Step 1/4 - Stopping new connections...
19:45:30.127 | INFO  | org.starloco.locos.kernel.Main - Step 2/4 - Saving world data (players, objects, mounts, etc)...
19:45:30.500 | INFO  | org.starloco.locos.game.world.World - -> of accounts.
19:45:30.600 | INFO  | org.starloco.locos.game.world.World - -> of players.
19:45:30.750 | INFO  | org.starloco.locos.kernel.Main - Step 3/4 - Disconnecting all players and saving their data...
19:45:31.000 | INFO  | org.starloco.locos.kernel.Main - Step 4/4 - Closing database connections...
19:45:31.200 | WARN  | org.starloco.locos.kernel.Main - ═══════════════════════════════════════════════════════════
19:45:31.201 | WARN  | org.starloco.locos.kernel.Main -   ✅ SERVER SHUTDOWN COMPLETE - All data saved
19:45:31.202 | WARN  | org.starloco.locos.kernel.Main - ═══════════════════════════════════════════════════════════
19:45:31.203 | INFO  | org.starloco.locos.kernel.Main - The server is now closed.

PS H:\server_dofus\Starloco-Fun\Server>
```

---

## 🚀 Prochains redémarrages

Après un arrêt gracieux, vous pouvez redémarrer :

```bash
# Redémarrer simplement
.\Start-Server.bat
```

Tous les joueurs seront **restaurés avec leurs données actualisées**, comme si le serveur n'avait jamais été arrêté (sauf pour les changements en temps réel).

---

**Résumé** : CTRL+C = 100% sûr ✅ | Pas de perte de données | Arrêt propre

