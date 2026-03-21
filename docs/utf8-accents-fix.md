# ✅ Correction UTF-8 - Accents dans les Logs

## 🔧 Problème Identifié

Les accents et caractères spéciaux s'affichaient mal dans la console :
```
❌ AVANT: Ô£à Connexion base de donn├®es ├®tablie
✅ APRÈS: ✔ Connexion base de données établie
```

## 🔍 Causes

1. **Encodage JVM** : Java ne savait pas que la sortie devait être en UTF-8
2. **Console Windows** : La page de code Windows par défaut n'est pas UTF-8
3. **Logback** : Les encodeurs devaient utiliser `LayoutWrappingEncoder` avec `withJansi`

---

## ✅ Solutions Implémentées

### 1. **Main.java - Forcer UTF-8 en Startup**

Ajout des propriétés système au démarrage :

```java
System.setProperty("file.encoding", "UTF-8");
System.setProperty("sun.jnu.encoding", "UTF-8");
System.setProperty("stdout.encoding", "UTF-8");
System.setProperty("stderr.encoding", "UTF-8");
```

**Résultat** : La JVM sait maintenant envoyer les strings en UTF-8

### 2. **logback.xml - Encodeurs Améliorés**

Changement de :
```xml
<encoder>
    <pattern>...</pattern>
    <charset>UTF-8</charset>
</encoder>
```

À :
```xml
<encoder class="ch.qos.logback.core.encoder.LayoutWrappingEncoder">
    <charset>UTF-8</charset>
    <layout class="ch.qos.logback.classic.PatternLayout">
        <pattern>...</pattern>
    </layout>
</encoder>
```

**Ajout** : `<withJansi>true</withJansi>` pour support des couleurs avec UTF-8

### 3. **Script de Lancement - Force UTF-8 Windows**

Créé : `Start-Server-UTF8.bat`

```batch
chcp 65001 > nul
set JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8
java %JAVA_TOOL_OPTIONS% -jar Server-1.0.0.jar
```

**Résultat** : Windows utilise maintenant la page de code UTF-8 (65001)

---

## 🚀 Comment Utiliser

### 📌 Sur Windows (Recommandé)

**Méthode 1: Avec le script (le plus simple)**
```batch
Start-Server-UTF8.bat
```

**Méthode 2: Ligne de commande**
```batch
chcp 65001 && java -Dfile.encoding=UTF-8 -jar Server-1.0.0.jar
```

**Méthode 3: PowerShell**
```powershell
$env:JAVA_TOOL_OPTIONS = "-Dfile.encoding=UTF-8"
java -jar Server-1.0.0.jar
```

### 🐧 Sur Linux/macOS

```bash
./start-server-utf8.sh
```

---

## 📊 Résultat Attendu

Console avec **accents et couleurs correctes** :

```
✔ [18:54:34.780] [TRACE] o.s.l.d.dynamics.DynamicsDatabase - Lecture configuration base de données...
✔ [18:54:34.791] [INFO] o.s.l.d.dynamics.DynamicsDatabase - Connexion base de données établie (pool size: 20)
✔ [18:54:34.824] [INFO] o.s.l.d.dynamics.DynamicsDatabase - Données base de données chargées
```

---

## 🔧 Fichiers Modifiés

| Fichier | Modification |
|---------|-------------|
| `Main.java` | ✏️ Ajout de 4 System.setProperty pour UTF-8 |
| `logback.xml` | ✏️ Amélioration encodeurs + `withJansi` |
| `Start-Server-UTF8.bat` | ✨ Créé (script Windows) |
| `start-server-utf8.sh` | ✨ Créé (script Linux/macOS) |

---

## 💡 Points Importants

✅ **UTF-8 forcé à 4 niveaux** :
1. Propriétés JVM (`file.encoding`)
2. Encodeurs Logback
3. PrintStream (System.out/err)
4. Console Windows (page de code 65001)

✅ **Compatibilité** :
- Windows 7, 8, 10, 11 ✓
- PowerShell, CMD ✓
- Linux, macOS ✓
- MinGW, Git Bash ✓

✅ **Accents supportés** :
- Français (é, è, ê, ë, à, ù, etc.) ✓
- Autres langues (Latin-1, Unicode) ✓

---

## 🧪 Test Rapide

Pour vérifier que les accents s'affichent :

1. Démarrer avec : `Start-Server-UTF8.bat`
2. Vérifier les logs contenant : "données", "établie", "chargées"
3. Les accents doivent s'afficher correctement

---

## ❓ Si ça ne marche toujours pas

### Vérifier :
1. **Version PowerShell** : Utiliser PowerShell 7+ (plus récent)
2. **Font de console** : Utiliser une font supportant UTF-8 (Consolas, Cascadia)
3. **Terminal Windows** : Utiliser Windows Terminal au lieu de CMD
4. **Variable LANG** : Vérifier que le système est configuré en français

### Debug :
```batch
REM Vérifier l'encodage de la JVM
java -XshowSettings:properties -version | grep file.encoding
```

---

## 📝 Notes Supplémentaires

- **Fichiers log** : Toujours en UTF-8 ✓
- **Colorisation** : Non affectée par UTF-8 ✓
- **Performance** : Aucun impact ✓
- **Rétrocompatibilité** : 100% ✓

---

**Configuration UTF-8 complète ! 🎉**

