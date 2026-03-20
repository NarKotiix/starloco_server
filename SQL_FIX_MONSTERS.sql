-- ============================================================================
-- Script de diagnostic et correction des données malformées de monstres
-- Problème: Format incohérent dans la colonne 'monsters' des cartes
-- Erreur observée: NumberFormatException sur "130527,140" et "# id"
-- ============================================================================
-- ATTENTION: Exécuter les requêtes SELECT d'abord pour voir l'impact
-- avant de lancer les UPDATE
-- ============================================================================

-- STEP 1: DIAGNOSTIC - Identifier tous les maps avec données problématiques
-- ============================================================================

SELECT '[STEP 1] Diagnostique des données malformées' as step;

-- Afficher tous les monstres qui contiennent un format invalide
SELECT
    id,
    monsters,
    LENGTH(monsters) as longueur,
    CASE
        WHEN monsters LIKE '%# %' THEN 'Contient commentaire SQL'
        WHEN monsters REGEXP '[0-9]+,[0-9]+,[0-9]+' THEN 'Format legacy spawn (id,min,max)'
        WHEN monsters REGEXP '[0-9]{5},[0-9]{3}' THEN 'Nombre avec virgule décimale'
        ELSE 'Autre format invalide'
    END as type_erreur
FROM maps
WHERE monsters IS NOT NULL
  AND monsters != ''
  AND (
    monsters LIKE '%#%'  -- Contient commentaire
    OR monsters REGEXP '[ ]{2,}' -- Espaces multiples
    OR monsters REGEXP '[0-9]{5},[0-9]{3}' -- Pattern ressemblant à un nombre décimal
  )
ORDER BY id;

-- ============================================================================
-- STEP 2: BACKUP - Sauvegarder les données avant modification
-- ============================================================================

SELECT '[STEP 2] Création d''une backup des données problématiques' as step;

-- Crée une sauvegarde des lignes problématiques (pour audit)
-- À exécuter AVANT les UPDATE
CREATE TABLE IF NOT EXISTS maps_monsters_backup_2026_03_20 AS
SELECT id, monsters
FROM maps
WHERE monsters IS NOT NULL
  AND monsters != ''
  AND (
    monsters LIKE '%#%'
    OR monsters REGEXP '[0-9]{5},[0-9]{3}'
    OR monsters REGEXP '[ ]{2,}'
  );

-- Vérifier la sauvegarde
SELECT COUNT(*) as backup_rows FROM maps_monsters_backup_2026_03_20;

-- ============================================================================
-- STEP 3: CORRECTION - Nettoyer et corriger les données
-- ============================================================================

SELECT '[STEP 3] Nettoyage des données malformées' as step;

-- 3.1: Supprimer les commentaires SQL
UPDATE maps
SET monsters = TRIM(SUBSTRING_INDEX(monsters, '#', 1))
WHERE monsters LIKE '%#%';

-- 3.2: Nettoyer les espaces superflus
UPDATE maps
SET monsters = REGEXP_REPLACE(monsters, '[ ]{2,}', ' ')
WHERE monsters REGEXP '[ ]{2,}';

-- 3.3: Normaliser les séparateurs
UPDATE maps
SET monsters = REGEXP_REPLACE(monsters, '[ ]*\\|[ ]*', '|')
WHERE monsters LIKE '%|%';

UPDATE maps
SET monsters = REGEXP_REPLACE(monsters, '[ ]*,[ ]*', ',')
WHERE monsters LIKE '%,%';

UPDATE maps
SET monsters = REGEXP_REPLACE(monsters, '[ ]*;[ ]*', ';')
WHERE monsters LIKE '%|%';

-- 3.4: Supprimer les entrées purement corrompues
UPDATE maps
SET monsters = ''
WHERE monsters IS NOT NULL
  AND (
    monsters REGEXP '^[0-9]{5},[0-9]{3}$' -- "130527,140" exactement
    OR monsters = '# id'                  -- Juste un commentaire
    OR TRIM(monsters) = ''                -- Vide après trim
  );

-- ============================================================================
-- STEP 4: VALIDATION - Vérifier que tous les formats sont maintenant valides
-- ============================================================================

SELECT '[STEP 4] Validation des données corrigées' as step;

-- Vérifier qu'il ne reste plus de données invalides
SELECT
    id,
    monsters,
    'TOUJOURS INVALIDE' as status
FROM maps
WHERE monsters IS NOT NULL
  AND monsters != ''
  AND (
    monsters LIKE '%#%'
    OR monsters NOT REGEXP '^[0-9]+(,[0-9]+)?(;[0-9]+(,[0-9]+)?)*(\|[0-9]+(,[0-9]+)?(;[0-9]+(,[0-9]+)?)*)*$'
  );

-- Compter les maps avec monstres valides et invalides
SELECT
    COUNT(CASE WHEN monsters = '' OR monsters IS NULL THEN 1 END) as maps_sans_monstres,
    COUNT(CASE WHEN monsters != '' AND monsters IS NOT NULL THEN 1 END) as maps_avec_monstres,
    COUNT(*) as total_maps
FROM maps;

-- ============================================================================
-- STEP 5: STATISTIQUES - Afficher un résumé de l'opération
-- ============================================================================

SELECT '[STEP 5] Résumé de l''opération' as step;

-- Afficher les types de monstres restants (pour vérification)
SELECT
    COUNT(*) as count,
    CASE
        WHEN monsters REGEXP '^[0-9]+,[0-9]+(\|[0-9]+,[0-9]+)*$' THEN 'id,level format'
        WHEN monsters REGEXP '^[0-9]+,[0-9]+,[0-9]+' THEN 'id,min,max format'
        WHEN monsters REGEXP '^[0-9]+,[0-9]+;[0-9]' THEN 'id,min,max;id,min,max format'
        WHEN monsters = '' OR monsters IS NULL THEN 'empty'
        ELSE 'unknown'
    END as format_type
FROM maps
WHERE monsters IS NOT NULL
GROUP BY format_type
ORDER BY count DESC;

-- ============================================================================
-- STEP 6: ROLLBACK (si besoin)
-- ============================================================================

-- Si quelque chose s'est mal passé, vous pouvez restaurer depuis la backup:
-- REPLACE INTO maps (id, monsters)
-- SELECT m.id, b.monsters
-- FROM maps_monsters_backup_2026_03_20 b
-- JOIN maps m ON m.id = b.id;

-- ============================================================================
-- Exécution recommandée:
-- ============================================================================
-- 1. Exécuter STEP 1 pour voir les problèmes
-- 2. Exécuter STEP 2 pour créer la backup
-- 3. Exécuter STEP 3 pour corriger
-- 4. Exécuter STEP 4 et 5 pour valider
-- ============================================================================



