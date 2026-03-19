# 🚀 Guide de Démarrage - Serveur avec Couleurs et UTF-8

## 📌 Situation Actuelle

✅ **Système de logging colorisé** : Configuré et fonctionnel  
✅ **Encodage UTF-8** : Corrigé pour les accents  
✅ **Windows compatible** : Scripts de démarrage fournis  
✅ **Compilation** : Réussie sans erreurs  

---

## 🎯 Comment Démarrer le Serveur

### ✨ **MEILLEURE MÉTHODE - Script Recommandé**

Double-cliquez sur :
```
Start-Server.bat
```

Cela va :
- ✅ Activer UTF-8 dans Windows
- ✅ Afficher les accents correctement
- ✅ Afficher les logs en couleur
- ✅ Lancer le serveur avec la bonne configuration

---

### Autres Méthodes

**Méthode 2 : PowerShell**
```powershell
PS> .\Start-Server.bat
```

**Méthode 3 : Ligne de commande Windows**
```batch
chcp 65001 && java -Dfile.encoding=UTF-8 -jar build\libs\Server-1.0.0.jar
```

**Méthode 4 : Ligne de commande simple (moins bon)**
```batch
java -jar build\libs\Server-1.0.0.jar
```

---

## 🎨 Résultat Attendu

### ✅ Console avec COULEURS et ACCENTS

```
✔ [14:23:45.123] | DEBUG | org.starloco.locos.kernel - Configuration chargée
✔ [14:23:46.456] | INFO  | org.starloco.locos.kernel - Base de données connectée
✔ [14:23:47.789] | WARN  | org.starloco.locos.entity - Charge mémoire élevée
✔ [14:23:48.012] | ERROR | org.starloco.locos.exchange - Erreur de connexion
```

Couleurs :
- 🟢 **INFO** - Vert
- 🔵 **DEBUG** - Cyan  
- 🟡 **WARN** - Jaune
- 🔴 **ERROR** - Rouge

---

## 📂 Fichiers Importants

| Fichier | Utilité |
|---------|---------|
| `Start-Server.bat` | ⭐ **À UTILISER** - Lance le serveur correctement |
| `build/libs/Server-1.0.0.jar` | Le serveur compilé |
| `config.properties` | Configuration (port, BDD, rates, etc.) |
| `Logs/` | Dossier des logs persistants |
| `docs/` | Documentation complète |

---

## ⚙️ Configuration Rapide

**Fichier** : `config.properties`

### Réseau
```properties
IP=127.0.0.1              # IP du serveur
GAME_PORT=5555            # Port de jeu
EXCHANGE_IP=127.0.0.1     # IP exchange
EXCHANGE_PORT=666         # Port exchange
```

### Base de Données Jeu
```properties
GAME_IP_DB=127.0.0.1      # IP BDD jeu
GAME_NAME_DB=game         # Nom BDD jeu
GAME_USER_DB=root         # User BDD jeu
GAME_PASS_DB=password     # Pass BDD jeu
GAME_PORT_DB=3306         # Port BDD jeu
```

### Rates (Multiplicateurs)
```properties
RATE_XP=1                 # Multiplicateur XP
RATE_DROP=1               # Multiplicateur drop
RATE_JOB=1                # Multiplicateur métiers
RATE_KAMAS=1              # Multiplicateur kamas
RATE_FM=2                 # Multiplicateur force magique
```

### Règles
```properties
START_LEVEL=1             # Niveau de départ
START_KAMAS=50000000      # Kamas de départ
ALL_ZAAP=true             # Tous zaaps accessibles
ALL_EMOTE=true            # Toutes émotes accessibles
ALLOW_PRESTIGE=false      # Prestige autorisé
HEROIC=false              # Mode héroïque
```

### IA / Invocations / Profiling
```properties
AI_PROFILING=false
AI_PROFILING_INVOCATIONS_ONLY=true
AI_PROFILING_WARN_MS=60
AI_INVOCATION_DELAY=50
AI_INVOCATION_SPELL_MAX_DELAY=220
AI_INVOCATION_MOVEMENT_BASE_DELAY=220
AI_INVOCATION_MOVEMENT_STEP_DELAY=55
```

---

## 🧪 Vérifier que Tout Fonctionne

### 1. Démarrer le serveur
```batch
Start-Server.bat
```

### 2. Vérifier les logs
- Les accents s'affichent ? ✅
- Les couleurs s'affichent ? ✅
- "Server is ready" apparaît ? ✅

### 3. Tester la connexion
- Connecter un client
- Vérifier les logs DEBUG
- Créer un perso

---

## 📊 Fichiers Log

**Localisation** : `Logs/`

| Fichier | Contenu |
|---------|---------|
| `server.log` | Tous les logs |
| `errors.log` | Erreurs uniquement |
| `Error/` | Stderr (ancien format) |

**Rotation** : Automatique (10MB par fichier, 30 jours)

---

## 🆘 Dépannage Rapide

### ❌ Les couleurs ne s'affichent pas
→ Utiliser `Start-Server.bat`

### ❌ Les accents sont mal affichés
→ Vérifier : `chcp` → doit afficher `65001`  
→ Sinon : `chcp 65001` puis relancer

### ❌ Le serveur ne démarre pas
→ Vérifier le fichier `config.properties`  
→ Vérifier la BDD (connexion, droits)

### ❌ Erreur `NullValueEncoder`
→ Rebuild puis redémarrer avec le jar mis à jour  
→ Vérifier que le runtime utilise bien le driver MySQL embarqué

### ❌ Les logs fichiers sont vides
→ Vérifier l'accès en écriture sur `Logs/`  
→ Vérifier que `logback.xml` est dans le JAR

---

## 📚 Documentation Complète

Voir les fichiers dans `docs/` :
- **README_LOGS.md** - Système de logging détaillé
- **UTF8_ACCENTS_FIX.md** - Correction UTF-8
- **QUICK_START_LOGGING.md** - Guide rapide
- **MODIFICATIONS_LOGGING.md** - Détail des changements

---

## ✅ Checklist de Démarrage

- [ ] Fichier `config.properties` configuré
- [ ] BDD jeu accessible
- [ ] BDD exchange accessible
- [ ] Port 5555 disponible (si utilisé)
- [ ] Dossier `Logs/` accessible en écriture
- [ ] JAR `build/libs/Server-1.0.0.jar` présent

**Puis :**
- [ ] Exécuter `Start-Server.bat`
- [ ] Vérifier console : couleurs + accents
- [ ] Vérifier "Server is ready" dans les logs
- [ ] Connecter un client pour tester

---

## 🚀 C'est Prêt !

**Le serveur est prêt à démarrer avec :**
✅ Système de logging colorisé  
✅ Accents correctement affichés  
✅ Gestion UTF-8 complète  
✅ Scripts de lancement optimisés  

**Bonne chance ! 🎮**

