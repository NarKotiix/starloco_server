# 🧪 Guide de Test - Arrêt Gracieux

## Préparation du test

### Prérequis
- ✅ Serveur compilé (`.\gradlew.bat build`)
- ✅ Bases de données configurées et en marche
- ✅ Console Windows ouverte

---

## Test 1 : Arrêt simple sans joueurs

### Étapes

1. **Démarrer le serveur**
```bash
.\Start-Server.bat
```

2. **Attendre le démarrage**
```
The server is ready ! Waiting for connection..
```

3. **Appuyer sur CTRL+C** (après ~5-10 secondes)

4. **Vérifier la sortie console**

### Résultats attendus ✅

```
╔════════════════════════════════════════════════════════════╗
║  SHUTDOWN SIGNAL RECEIVED - Saving data before closing...  ║
╚════════════════════════════════════════════════════════════╝

19:45:30.123 | WARN  | org.starloco.locos.kernel.Main - ═══════════════════════════════════════════════════════════
19:45:30.124 | WARN  | org.starloco.locos.kernel.Main -   SERVER SHUTDOWN INITIATED - CTRL+C / Shutdown Signal
19:45:30.125 | WARN  | org.starloco.locos.kernel.Main - ═══════════════════════════════════════════════════════════
19:45:30.126 | INFO  | org.starloco.locos.kernel.Main - Step 1/4 - Stopping new connections...
19:45:30.127 | INFO  | org.starloco.locos.kernel.Main - Step 2/4 - Saving world data (players, objects, mounts, etc)...
19:45:30.750 | INFO  | org.starloco.locos.kernel.Main - Step 3/4 - Disconnecting all players and saving their data...
19:45:31.000 | INFO  | org.starloco.locos.kernel.Main - Step 4/4 - Closing database connections...
19:45:31.200 | WARN  | org.starloco.locos.kernel.Main - ═══════════════════════════════════════════════════════════
19:45:31.201 | WARN  | org.starloco.locos.kernel.Main -   ✅ SERVER SHUTDOWN COMPLETE - All data saved
19:45:31.202 | WARN  | org.starloco.locos.kernel.Main - ═══════════════════════════════════════════════════════════
19:45:31.203 | INFO  | org.starloco.locos.kernel.Main - The server is now closed.

PS H:\server_dofus\Starloco-Fun\Server>
```

### Critères de succès
- ✅ Les 4 étapes s'affichent
- ✅ Message "SHUTDOWN COMPLETE" visible
- ✅ Pas d'exceptions dans les logs
- ✅ Console se ferme proprement
- ✅ Temps total : 2-4 secondes

---

## Test 2 : Vérifier la sauvegarde en BD

### Étapes

1. **Démarrer le serveur**
```bash
.\Start-Server.bat
```

2. **Simuler un changement** (via client ou modification directe)
   - Créer un joueur
   - Ajouter des items
   - Changer de level
   - Créer une guilde

3. **Appuyer sur CTRL+C** après ~10-20 secondes

4. **Vérifier la base de données**
```sql
-- Dans MySQL
USE game;
SELECT * FROM players ORDER BY last_action DESC LIMIT 5;
SELECT * FROM accounts WHERE id = 1;
```

5. **Redémarrer le serveur**
```bash
.\Start-Server.bat
```

6. **Vérifier la restauration**
```sql
-- Les données doivent être identiques
SELECT * FROM players WHERE id = 1;
```

### Résultats attendus ✅

- ✅ Les changements sont sauvegardés en BD
- ✅ Au redémarrage, les données sont restaurées
- ✅ Aucune perte de données
- ✅ Timestamps mis à jour (last_action)

---

## Test 3 : Arrêt avec plusieurs joueurs (simulation)

### Étapes

1. **Démarrer le serveur**
```bash
.\Start-Server.bat
```

2. **Simuler plusieurs joueurs** (via script ou client multiple)
```
Player1 connecté
Player2 connecté
Player3 connecté
...
```

3. **Effectuer des actions**
   - Combat
   - Échange
   - Construction de maison
   - Crafting

4. **Appuyer sur CTRL+C** pendant les actions

5. **Vérifier les logs**

### Résultats attendus ✅

- ✅ Étape 3 : "Disconnecting all players..." s'affiche
- ✅ Tous les joueurs sont déconnectés avec sauvegarde
- ✅ Pas d'erreurs de base de données
- ✅ Tous les changements sont persistés

---

## Test 4 : Vérifier les logs

### Fichiers à vérifier

```bash
# Tous les logs d'arrêt
Get-Content Logs/server.log | Select-String "SHUTDOWN"

# Erreurs pendant l'arrêt (ne doit être vide)
Get-Content Logs/errors.log | Select-String "SHUTDOWN" -A 5

# Vérifier le format des logs
Measure-Object -InputObject (Get-Content Logs/server.log) -Line
```

### Résultats attendus ✅

- ✅ `server.log` contient les 4 étapes
- ✅ `errors.log` ne contient pas d'erreurs d'arrêt
- ✅ Formatage cohérent avec couleurs
- ✅ Timestamps corrects

---

## Test 5 : Validation finale complète

### Checklist

- [ ] Compilation réussie
- [ ] Serveur démarre correctement
- [ ] CTRL+C déclenche le shutdown hook
- [ ] Les 4 étapes s'affichent
- [ ] Message "✅ SHUTDOWN COMPLETE" visible
- [ ] Pas d'exceptions dans les logs
- [ ] Les données en BD sont mises à jour
- [ ] Redémarrage restaure les données correctement
- [ ] Aucune perte de données détectée
- [ ] Temps d'arrêt : 2-10 secondes

### Résultat final
- ✅ **TOUS LES TESTS PASSENT** = Fonctionnalité validée ! 🎉

---

## 🐛 Dépannage

### Problème : CTRL+C ne déclenche pas le hook

**Solution** :
```bash
# Vérifier que le serveur est bien lancé
# Le hook ne se déclenche que si Main.isRunning = true
```

### Problème : Étapes incomplètes

**Solution** :
```bash
# Vérifier que toutes les dépendances sont chargées
# Vérifier les erreurs en base de données
```

### Problème : Timeout lors de l'arrêt

**Solution** :
```bash
# Augmenter le délai d'attente dans Main.java
Thread.sleep(5000);  // Au lieu de 2000
```

---

## 📊 Métriques à surveiller

Après chaque test, noter :

| Métrique | Valeur | OK ? |
|----------|--------|------|
| Temps d'arrêt | 2-10 sec | ✅ |
| Étapes complètes | 4/4 | ✅ |
| Erreurs SQL | 0 | ✅ |
| Joueurs sauvegardés | N | ✅ |
| BD synchronized | ✅ | ✅ |

---

## ✅ Validation complète

Quand tous les tests passent :

1. ✅ Créer un commit
```bash
git add -A
git commit -m "Add graceful shutdown with data persistence"
```

2. ✅ Documenter les résultats
3. ✅ Informer les utilisateurs
4. ✅ Production ready ! 🚀

---

## 📞 Support

En cas de problème :
- 📄 Consulter `GRACEFUL_SHUTDOWN.md`
- 🔧 Vérifier `MODIFICATIONS_GRACEFUL_SHUTDOWN.md`
- 💬 Ouvrir une issue GitHub

**Bon test !** 🚀

