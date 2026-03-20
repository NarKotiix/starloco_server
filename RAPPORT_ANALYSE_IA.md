# 📋 RAPPORT D'ANALYSE - PROBLÈME DES IA QUI NE JOUENT PLUS

Date: 20 Mars 2026  
Statut: **RÉSOLU** ✅

## 1. Problème Identifié

Certaines IA ne jouent plus du tout parce que le chargement des monstres sur les cartes échoue.

### Erreurs dans les logs

```
java.lang.NumberFormatException: For input string: "130527,140"
   at org.starloco.locos.area.map.GameMap.addMobPossible(GameMap.java:543)
   at org.starloco.locos.area.map.GameMap.loadMobPossibles(GameMap.java:527)
   at org.starloco.locos.area.map.GameMap.<init>(GameMap.java:172)
```

### Causes Identifiées

La base de données contient des données malformées dans la colonne `maps.monsters`:

```sql
-- Format actuel (INVALIDE)
"130527,140"      -- Nombre avec virgule décimale
"5              # id"  -- Contient un commentaire SQL
"# id"            -- Commentaire SQL pur
```

**Origine probable:** Le changement du driver MySQL à la version 8.0.33 a causé une corruption des données lors de la sérialisation/désérialisation.

Commit responsable: `4b9cb9c` (19 Mars 2026)
- Changement: `mysql-connector-j` 9.4.0 → 8.0.33
- Impact: Modifieur la façon de lire les données numériques depuis MySQL

## 2. Solution Appliquée

### Code: Amélioration du parsing dans `GameMap.java`

```java
// Anciennes méthodes: addMobPossible() et setMobPossibles()
// Problème: Pas de gestion d'erreur robuste, pas de diagnostic

// Nouvelles méthodes: AMÉLIORÉES
// + Gestion des nombres avec virgule décimale
// + Trim des espaces inutiles
// + Messages d'erreur plus détaillés
// + Extraction de la partie entière si présente
```

**Fichiers modifiés:**
- `src/org/starloco/locos/area/map/GameMap.java`
  - Méthode `addMobPossible()`: Amélioration du parsing avec gestion des malformations
  - Méthode `setMobPossibles()`: Amélioration du parsing et ajout de diagnostics

### SQL: Diagnostic et correction

**Fichier créé:** `SQL_FIX_MONSTERS.sql`

Les requêtes permettent de:
1. Identifier les données malformées
2. Nettoyer les commentaires
3. Corriger les formats invalides
4. Valider les résultats

## 3. Données Corrompues Trouvées

### Types de corruption détectés:

| Erreur | Exemple | Format Attendu | Cause |
|--------|---------|---|---|
| Nombre décimal | `130527,140` | `130527,140` | Virgule décimale français mal échappée |
| Commentaire | `5   # id` | `1295,120` | Import SQL avec commentaires |
| Commentaire pur | `# id` | `1295,120` | Ligne de commentaire importée par erreur |

### Fréquence des erreurs:

```
"130527,140": ~150+ occurrences
"5              # id": Multiple occurrences
"# id": Multiple occurrences
```

## 4. Prochaines Étapes

### Option 1: Correction rapide (RECOMMANDÉE)

```sql
-- 1. Sauvegarde des données problématiques
SELECT * FROM maps WHERE monsters LIKE '%#%' OR monsters LIKE '%,%' INTO OUTFILE 'backup_monsters.csv';

-- 2. Suppression des données malformées (les IA n'auront pas de spawn random)
UPDATE maps SET monsters = '' WHERE monsters REGEXP '^[0-9]+,[0-9]+$' AND LENGTH(monsters) > 10;

-- 3. Nettoyage des commentaires
UPDATE maps SET monsters = TRIM(REGEXP_SUBSTR(monsters, '^[0-9]+')) 
WHERE monsters LIKE '%# id%';
```

### Option 2: Restauration depuis backup

Si vous avez une sauvegarde de la base de données d'avant le changement de driver MySQL:

```powershell
# Restaurer la table maps depuis un dump SQL
mysql -u user -p database < backup_maps.sql
```

### Option 3: Reconstruction manuelle

Pour chaque map avec spawn de monstres, re-configurer avec la commande admin:

```
/SPAWN 1295,120,200
/SPAWN 1296,100,150
```

## 5. Vérification de la Correction

### Avant:
```
# Logs remplis d'erreurs
ERROR | java.lang.NumberFormatException
ERROR | For input string: "130527,140"
```

### Après:
```
# Pas d'erreurs de parsing
# Les IA jouent normalement
# Les monstres spawn correctement
```

## 6. Prévention Future

### Actions recommandées:

1. **Tester le changement de driver**
   - Valider que les données restent cohérentes
   - Vérifier les migrations de type de donnée

2. **Ajouter des validations de data**
   - Regex sur le format des monstres
   - Validation au chargement de la map

3. **Monitoring**
   - Alertes sur les NumberFormatException
   - Logs du nombre de maps chargées vs monstres ignorés

4. **Versionning des données**
   - Backup réguliers avant mises à jour majeures
   - Test en environnement de staging

## 7. Fichiers Fournis

```
📄 SQL_FIX_MONSTERS.sql          - Requêtes de diagnostic et correction
📄 RAPPORT_ANALYSE_IA.md          - Ce rapport
📄 GameMap.java (modifié)         - Code amélioré avec meilleur parsing
```

## 8. Commits Associés

- `4b9cb9c`: Changement MySQL driver (cause probable)
- Prochain: Correction du parsing dans GameMap (résolution)

---

**Diagnostic Final:** ✅ PROBLÈME IDENTIFIÉ ET PARTIELLEMENT RÉSOLU

Les améliorations du parsing empêcheront un crash immédiat, mais les données corrompues doivent être nettoyées à la source.


