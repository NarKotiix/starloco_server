-- Persistance du mode donjon modulaire par joueur.
-- A executer avant le deploiement du code si la colonne n'existe pas deja.
ALTER TABLE `players`
    ADD COLUMN `dungeon_modular_mode` TINYINT(1) NOT NULL DEFAULT 0 AFTER `noall`;

