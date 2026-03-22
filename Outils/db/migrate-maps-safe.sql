-- Migration safe pour normaliser la table `maps`
-- Compatible MySQL 5.7 (usage de PREPARE + INFORMATION_SCHEMA).
-- Objectif :
--   1) créer la table si elle n'existe pas ;
--   2) ajouter les colonnes manquantes ;
--   3) normaliser types + ordre canonique compatible avec les dumps récents ;
--   4) éviter les imports dangereux en `INSERT INTO maps VALUES (...)` sur un schéma local divergent.
--
-- IMPORTANT :
-- - Exécuter pendant une fenêtre de maintenance.
-- - Faire une sauvegarde DB complète avant exécution.
-- - Si vous avez un très gros volume, le `ALTER TABLE ... MODIFY COLUMN ...` peut reconstruire la table.

SET @db := DATABASE();

-- 1) Sauvegarde opportuniste si la table existe déjà.
SET @sql := (
    SELECT IF(
        EXISTS(
            SELECT 1
            FROM INFORMATION_SCHEMA.TABLES
            WHERE TABLE_SCHEMA = @db
              AND TABLE_NAME = 'maps'
        ),
        'CREATE TABLE IF NOT EXISTS `maps_backup_before_safe_migration` AS SELECT * FROM `maps`',
        'SELECT ''maps absente, pas de backup source'''
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2) Création de base si la table n'existe pas.
SET @sql := (
    SELECT IF(
        EXISTS(
            SELECT 1
            FROM INFORMATION_SCHEMA.TABLES
            WHERE TABLE_SCHEMA = @db
              AND TABLE_NAME = 'maps'
        ),
        'SELECT ''maps existe deja''',
        'CREATE TABLE `maps` (
            `id` int(11) NOT NULL,
            `date` varchar(50) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
            `width` int(11) NOT NULL,
            `heigth` int(11) NOT NULL,
            `places` varchar(400) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
            `key` text CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL,
            `mapData` text CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
            `monsters` text CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL,
            `capabilities` int(11) NOT NULL DEFAULT 6,
            `mappos` varchar(15) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
            `numgroup` int(11) NOT NULL DEFAULT 3,
            `minSize` int(11) NOT NULL DEFAULT 1,
            `fixSize` int(11) NOT NULL DEFAULT -1,
            `maxSize` int(11) NOT NULL DEFAULT 8,
            `forbidden` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT ''0;0;0;0;0;0;0'' COMMENT ''noMarchand;noCollector;noPrism;noTP;noDefie;noAgro;noCanal'',
            `sniffed` tinyint(1) NOT NULL DEFAULT 0,
            PRIMARY KEY (`id`) USING BTREE
        ) ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=Dynamic'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3) Ajout des colonnes manquantes une par une.
SET @sql := (
    SELECT IF(
        EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'maps' AND COLUMN_NAME = 'date'),
        'SELECT ''date existe''',
        'ALTER TABLE `maps` ADD COLUMN `date` varchar(50) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL AFTER `id`'
    )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (
    SELECT IF(
        EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'maps' AND COLUMN_NAME = 'width'),
        'SELECT ''width existe''',
        'ALTER TABLE `maps` ADD COLUMN `width` int(11) NOT NULL AFTER `date`'
    )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (
    SELECT IF(
        EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'maps' AND COLUMN_NAME = 'heigth'),
        'SELECT ''heigth existe''',
        'ALTER TABLE `maps` ADD COLUMN `heigth` int(11) NOT NULL AFTER `width`'
    )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (
    SELECT IF(
        EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'maps' AND COLUMN_NAME = 'places'),
        'SELECT ''places existe''',
        'ALTER TABLE `maps` ADD COLUMN `places` varchar(400) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL AFTER `heigth`'
    )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (
    SELECT IF(
        EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'maps' AND COLUMN_NAME = 'key'),
        'SELECT ''key existe''',
        'ALTER TABLE `maps` ADD COLUMN `key` text CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL AFTER `places`'
    )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (
    SELECT IF(
        EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'maps' AND COLUMN_NAME = 'mapData'),
        'SELECT ''mapData existe''',
        'ALTER TABLE `maps` ADD COLUMN `mapData` text CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL AFTER `key`'
    )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (
    SELECT IF(
        EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'maps' AND COLUMN_NAME = 'monsters'),
        'SELECT ''monsters existe''',
        'ALTER TABLE `maps` ADD COLUMN `monsters` text CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL AFTER `mapData`'
    )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (
    SELECT IF(
        EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'maps' AND COLUMN_NAME = 'capabilities'),
        'SELECT ''capabilities existe''',
        'ALTER TABLE `maps` ADD COLUMN `capabilities` int(11) NOT NULL DEFAULT 6 AFTER `monsters`'
    )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (
    SELECT IF(
        EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'maps' AND COLUMN_NAME = 'mappos'),
        'SELECT ''mappos existe''',
        'ALTER TABLE `maps` ADD COLUMN `mappos` varchar(15) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL AFTER `capabilities`'
    )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (
    SELECT IF(
        EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'maps' AND COLUMN_NAME = 'numgroup'),
        'SELECT ''numgroup existe''',
        'ALTER TABLE `maps` ADD COLUMN `numgroup` int(11) NOT NULL DEFAULT 3 AFTER `mappos`'
    )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (
    SELECT IF(
        EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'maps' AND COLUMN_NAME = 'minSize'),
        'SELECT ''minSize existe''',
        'ALTER TABLE `maps` ADD COLUMN `minSize` int(11) NOT NULL DEFAULT 1 AFTER `numgroup`'
    )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (
    SELECT IF(
        EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'maps' AND COLUMN_NAME = 'fixSize'),
        'SELECT ''fixSize existe''',
        'ALTER TABLE `maps` ADD COLUMN `fixSize` int(11) NOT NULL DEFAULT -1 AFTER `minSize`'
    )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (
    SELECT IF(
        EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'maps' AND COLUMN_NAME = 'maxSize'),
        'SELECT ''maxSize existe''',
        'ALTER TABLE `maps` ADD COLUMN `maxSize` int(11) NOT NULL DEFAULT 8 AFTER `fixSize`'
    )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (
    SELECT IF(
        EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'maps' AND COLUMN_NAME = 'forbidden'),
        'SELECT ''forbidden existe''',
        'ALTER TABLE `maps` ADD COLUMN `forbidden` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT ''0;0;0;0;0;0;0'' COMMENT ''noMarchand;noCollector;noPrism;noTP;noDefie;noAgro;noCanal'' AFTER `maxSize`'
    )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (
    SELECT IF(
        EXISTS(SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'maps' AND COLUMN_NAME = 'sniffed'),
        'SELECT ''sniffed existe''',
        'ALTER TABLE `maps` ADD COLUMN `sniffed` tinyint(1) NOT NULL DEFAULT 0 AFTER `forbidden`'
    )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 4) Normalisation finale des types + ordre canonique des colonnes.
ALTER TABLE `maps`
    MODIFY COLUMN `id` int(11) NOT NULL FIRST,
    MODIFY COLUMN `date` varchar(50) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL AFTER `id`,
    MODIFY COLUMN `width` int(11) NOT NULL AFTER `date`,
    MODIFY COLUMN `heigth` int(11) NOT NULL AFTER `width`,
    MODIFY COLUMN `places` varchar(400) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL AFTER `heigth`,
    MODIFY COLUMN `key` text CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL AFTER `places`,
    MODIFY COLUMN `mapData` text CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL AFTER `key`,
    MODIFY COLUMN `monsters` text CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL AFTER `mapData`,
    MODIFY COLUMN `capabilities` int(11) NOT NULL DEFAULT 6 AFTER `monsters`,
    MODIFY COLUMN `mappos` varchar(15) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL AFTER `capabilities`,
    MODIFY COLUMN `numgroup` int(11) NOT NULL DEFAULT 3 AFTER `mappos`,
    MODIFY COLUMN `minSize` int(11) NOT NULL DEFAULT 1 AFTER `numgroup`,
    MODIFY COLUMN `fixSize` int(11) NOT NULL DEFAULT -1 AFTER `minSize`,
    MODIFY COLUMN `maxSize` int(11) NOT NULL DEFAULT 8 AFTER `fixSize`,
    MODIFY COLUMN `forbidden` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '0;0;0;0;0;0;0' COMMENT 'noMarchand;noCollector;noPrism;noTP;noDefie;noAgro;noCanal' AFTER `maxSize`,
    MODIFY COLUMN `sniffed` tinyint(1) NOT NULL DEFAULT 0 AFTER `forbidden`;

-- 5) Vérifications post-migration.
SHOW CREATE TABLE `maps`;
SHOW COLUMNS FROM `maps`;
SELECT COUNT(*) AS maps_columns
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'maps';

-- 6) Exemple d'import sécurisé recommandé ensuite :
-- INSERT INTO maps (
--   id, date, width, heigth, places, `key`, mapData, monsters,
--   capabilities, mappos, numgroup, minSize, fixSize, maxSize,
--   forbidden, sniffed
-- ) VALUES (...);

