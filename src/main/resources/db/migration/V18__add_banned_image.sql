CREATE TABLE IF NOT EXISTS `banned_images` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `guild` VARCHAR(255) NOT NULL,
    `author` VARCHAR(255) NOT NULL,
    `hash` VARCHAR(255) NOT NULL,
    `created` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `banned_image_guild_id_fk`
        FOREIGN KEY (`guild`)
        REFERENCES `guild` (`id`),
    CONSTRAINT `banned_image_author_id_fk`
        FOREIGN KEY (`author`)
        REFERENCES `users` (`id`),
    PRIMARY KEY(`id`))
ENGINE = InnoDB;
