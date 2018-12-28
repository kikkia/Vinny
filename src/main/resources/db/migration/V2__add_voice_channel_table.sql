-- ----------------------------------------
-- Table `voice_channel`
-- ----------------------------------------
CREATE TABLE IF NOT EXISTS `voice_channel` (
    `id` VARCHAR(255) NOT NULL,
    `guild` VARCHAR(255) NOT NULL,
    `name` VARCHAR(255) NOT NULL,
    `voice_enabled` TINYINT(1) NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    CONSTRAINT `guild_id_fk`
        FOREIGN KEY (`guild`)
        REFERENCES `guild` (`id`))
ENGINE = InnoDB;
