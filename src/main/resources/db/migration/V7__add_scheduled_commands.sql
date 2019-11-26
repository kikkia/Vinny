CREATE TABLE IF NOT EXISTS `scheduled_command` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `command` VARCHAR(511) NOT NULL,
    `guild` VARCHAR(255) NOT NULL,
    `channel` VARCHAR(255) NOT NULL,
    `author` VARCHAR(255) NOT NULL,
    `interval_time` bigint NOT NULL,
    `last_run` bigint NOT NULL,
    CONSTRAINT `scheduled_command_guild_id_fk`
        FOREIGN KEY (`guild`)
        REFERENCES `guild` (`id`),
    CONSTRAINT `scheduled_command_channel_id_fk`
        FOREIGN KEY (`channel`)
        REFERENCES `text_channel` (`id`),
    CONSTRAINT `scheduled_command_author_id_fk`
        FOREIGN KEY (`author`)
        REFERENCES `users` (`id`),
    PRIMARY KEY(`id`))
ENGINE = InnoDB;
