CREATE TABLE IF NOT EXISTS `command_aliases` (
    `id` INT(255) NOT NULL AUTO_INCREMENT,
    `alias` VARCHAR(255) NULL DEFAULT NULL,
    `command` VARCHAR(511) NULL DEFAULT NULL,
    `author` VARCHAR(255) NULL DEFAULT NULL,
    PRIMARY KEY (`id`))
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `guild_aliases` (
    `guild` VARCHAR(255) NOT NULL,
    `alias` INT(32) NOT NULL,
    CONSTRAINT `guild_alias_guild_id_fk`
        FOREIGN KEY (`guild`)
        REFERENCES `guild` (`id`),
    CONSTRAINT `guild_alias_alias_id_fk`
        FOREIGN KEY (`alias`)
        REFERENCES `command_aliases` (`id`))
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `channel_aliases` (
    `channel` VARCHAR(255) NOT NULL,
    `alias` INT(32) NOT NULL,
    CONSTRAINT `channel_alias_channel_id_fk`
        FOREIGN KEY (`channel`)
        REFERENCES `text_channel` (`id`),
    CONSTRAINT `channel_alias_alias_id_fk`
        FOREIGN KEY (`alias`)
        REFERENCES `command_aliases` (`id`))
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `user_aliases` (
    `user` VARCHAR(255) NOT NULL,
    `alias` INT(32) NOT NULL,
    CONSTRAINT `user_alias_user_id_fk`
        FOREIGN KEY (`user`)
        REFERENCES `users` (`id`),
    CONSTRAINT `user_alias_alias_id_fk`
        FOREIGN KEY (`alias`)
        REFERENCES `command_aliases` (`id`))
ENGINE = InnoDB;