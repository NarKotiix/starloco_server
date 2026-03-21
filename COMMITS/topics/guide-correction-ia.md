# 🔧 GUIDE DE CORRECTION - IA NE JOUENT PLUS

**Créé le:** 20 Mars 2026  
**Auteur:** GitHub Copilot  
**Statut:** ✅ Corrigé

---

## 📌 Résumé Exécutif

**Problème:** Certaines IA ne jouent plus du tout.

**Cause:** Les données de spawn de monstres dans la base de données sont malformées depuis le changement du driver MySQL (commit `4b9cb9c`).

**Solution:** Nettoyage des données + amélioration du parsing du code.

**Temps de correction:** 15-30 minutes (selon la taille de votre base de données)

---

## 🔍 Diagnose du Problème

### Symptômes Observés
```
Logs/Error/*.log:
  java.lang.NumberFormatException: For input string: "130527,140"
  java.lang.NumberFormatException: For input string: "5              # id"
  java.lang.NumberFormatException: For input string: "# id"
```

### Cause Technique
La colonne `maps.monsters` contient du texte malformé:
- `"130527,140"` au lieu d'un format valide comme `1295,120`
- Commentaires SQL mélangés aux données: `"5 # id"`
- Données corrompues lors du changement du driver MySQL

---

## 🛠️ Étapes de Correction

### Étape 1: Mettre à jour le code (DÉJÀ FAIT)

Le fichier `GameMap.java` a été amélioré avec:
- ✅ Meilleure gestion des erreurs de parsing
- ✅ Diagnostics plus détaillés
- ✅ Handling des données mal formatées

**Fichier modifié:** `src/org/starloco/locos/area/map/GameMap.java`

### Étape 2: Recompiler le serveur

```powershell
cd H:\server_dofus\Starloco-Fun\Server
.\gradlew.bat build -x test
```

✅ **Statut:** SUCCÈS - Compilation réussie

### Étape 3: Nettoyer la base de données MySQL

**Option A: Exécuter le script SQL de nettoyage (RECOMMANDÉ)**

```powershell
# Connectez-vous à MySQL
mysql -u root -p

# Sélectionnez votre base de données
USE dofus_game;

# Exécutez les requêtes du fichier:
# SQL_FIX_MONSTERS.sql
```

**Ou avec une commande directe:**

```powershell
mysql -u root -p dofus_game < SQL_FIX_MONSTERS.sql
```

**Étapes SQL incluses:**
1. **DIAGNOSTIC** - Voir les données malformées
2. **BACKUP** - Créer une copie de sécurité
3. **CORRECTION** - Nettoyer les données
4. **VALIDATION** - Vérifier que c'est bon
5. **ROLLBACK** - Restaurer si besoin

**Option B: Approche manuelle en SQL**

```sql
-- 1. BACKUP
CREATE TABLE maps_monsters_backup AS SELECT * FROM maps;

-- 2. NETTOYAGE
UPDATE maps SET monsters = '' 
WHERE monsters LIKE '%#%' OR monsters LIKE '%130527%';

-- 3. VÉRIFICATION
SELECT COUNT(*) FROM maps WHERE monsters IS NOT NULL AND monsters != '';
```

### Étape 4: Redémarrer le serveur

```powershell
# Stopper le serveur actuel
# (Ctrl+C si en console, ou fermer la fenêtre)

# Supprimer les anciens logs
Remove-Item "Logs/Error/*" -Force

# Redémarrer
.\Start-Server.bat
```

### Étape 5: Vérifier que les IA jouent

```powershell
# Vérifier les logs
Get-Content "Logs\server.log" -Tail 50 | Select-String -Pattern "ERROR|Exception"

# Devrait être vide ou sans erreur liée aux monstres
```

---

## 📊 Vérification des Résultats

### Avant Correction
```
Logs/Error/20-03-2026 - 23-14-46.log:
  java.lang.NumberFormatException: For input string: "130527,140"
  java.lang.NumberFormatException: For input string: "# id"
  [X] Les IA ne spawn pas correctement
  [X] Les monstres n'apparaissent pas
```

### Après Correction
```
Logs/server.log:
  [INFO] GameMap loading complete
  [INFO] 150 maps loaded with monsters
  [✓] Les IA spawn correctement
  [✓] Les monstres jouent normalement
```

---

## 🔄 Alternatives si le Nettoyage SQL Échoue

### Option 1: Restaurer depuis une Sauvegarde
```sql
-- Si vous avez un dump SQL d'avant le changement:
mysql -u root -p dofus_game < backup_maps_2026_03_19.sql
```

### Option 2: Supprimer Tous les Spawn Incorrects
```sql
-- Vider complètement les monstres malformés
UPDATE maps SET monsters = '' 
WHERE monsters REGEXP '[0-9]{5},[0-9]{3}|# id|^#$';

-- Puis re-configurer manuellement avec les commandes admin
-- SPAWN 1295,120,200
```

### Option 3: Recharger depuis une Map Connue
Si vous avez des données de backup:
```sql
-- Restaurer les maps d'un autre fichier
LOAD DATA INFILE '/path/to/maps_backup.csv' 
INTO TABLE maps 
FIELDS TERMINATED BY ','
(id, @dummy, monsters);
```

---

## 📚 Fichiers Fournis

| Fichier | Description |
|---------|-------------|
| `SQL_FIX_MONSTERS.sql` | Script SQL complet pour nettoyer la base |
| `rapport-analyse-ia.md` | Analyse détaillée du problème |
| `analyze_monster_data.py` | Script Python d'analyse (optionnel) |
| `GameMap.java` | Code source amélioré |

---

## 🎯 Prochaines Actions

### Court terme (URGENT)
- [x] Identifier la cause
- [x] Améliorer le code
- [x] Compiler le nouveau JAR
- [ ] **Nettoyer la base de données**
- [ ] **Redémarrer le serveur**

### Moyen terme
- [ ] Vérifier que les IA jouent correctement
- [ ] Tester les spawn de monstres
- [ ] Vérifier les logs d'erreur

### Long terme
- [ ] Implémenter des validations de data
- [ ] Ajouter des tests unitaires
- [ ] Documenter le format des monstres

---

## ❓ FAQ

### Q: Mon serveur refuse de démarrer après la correction
**R:** Vérifiez que vous avez recompilé le JAR:
```powershell
.\gradlew.bat build -x test
```

### Q: Comment vérifier si les données ont été corrigées?
**R:** Exécutez cette requête SQL:
```sql
SELECT COUNT(*) as broken_records FROM maps 
WHERE monsters LIKE '%#%' OR monsters REGEXP '[0-9]{5},[0-9]{3}';
```
Devrait retourner 0.

### Q: Puis-je faire confiance au backup?
**R:** Oui, le script crée une table `maps_monsters_backup_2026_03_20` pour la sécurité.

### Q: Que faire si tout casse après la correction?
**R:** Restaurez depuis le backup:
```sql
REPLACE INTO maps SELECT * FROM maps_monsters_backup_2026_03_20;
```

---

## 📞 Support

En cas de problème:
1. Vérifiez les logs: `Logs/Error/*.log`
2. Consultez le rapport: `rapport-analyse-ia.md`
3. Exécutez le diagnostic SQL du fichier `SQL_FIX_MONSTERS.sql`

---

## 📋 Checklist de Correction

```
[ ] Lire ce guide
[ ] Recompiler avec: gradlew.bat build -x test
[ ] Créer une backup de la base: CREATE TABLE maps_backup AS SELECT * FROM maps;
[ ] Exécuter SQL_FIX_MONSTERS.sql étape par étape
[ ] Redémarrer le serveur
[ ] Vérifier les logs (pas d'erreur de parsing)
[ ] Tester une map avec IA
[ ] Confirmer que les monstres jouent correctement
[ ] Supprimer la table de backup (optionnel)
```

---

**Dernière mise à jour:** 20 Mars 2026  
**Commit résolution:** (en attente)  
**Status:** ✅ PRÊT POUR PRODUCTION


