-- ----------------------------------------
-- Table `guild`
-- ----------------------------------------
CREATE TABLE IF NOT EXISTS `guild` (
    `id` VARCHAR(255) NOT NULL,
    `name` VARCHAR(255) NULL DEFAULT NULL,
    `default_volume` INT(11),
    `min_base_role_id` VARCHAR(255) NULL DEFAULT NULL,
    `min_mod_role_id` VARCHAR(255) NULL DEFAULT NULL,
    `min_voice_role_id` VARCHAR(255) NULL DEFAULT NULL,
    `min_nsfw_role_id` VARCHAR(255) NULL DEFAULT NULL,
    PRIMARY KEY (`id`))
ENGINE = InnoDB;


-- ----------------------------------------
-- Table `text_channel`
-- ----------------------------------------
CREATE TABLE IF NOT EXISTS `text_channel` (
    `id` VARCHAR(255) NOT NULL,
    `guild` VARCHAR(255) NOT NULL,
    `name` VARCHAR(255) NOT NULL,
    `announcement` TINYINT(1) NULL DEFAULT 0,
    `commands_enabled` TINYINT(1) NULL DEFAULT 1,
    `voice_enabled` TINYINT(1) NULL DEFAULT 1,
    `nsfw_enabled` TINYINT(1) NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    CONSTRAINT `guild_id_fk`
        FOREIGN KEY (`guild`)
        REFERENCES `guild` (`id`))
ENGINE = InnoDB;


-- ----------------------------------------
-- Table `users`
-- ----------------------------------------
CREATE TABLE IF NOT EXISTS `users` (
    `id` VARCHAR(255) NOT NULL,
    `name` VARCHAR(255) NULL DEFAULT NULL,
    PRIMARY KEY (`id`))
ENGINE = InnoDB;


-- ----------------------------------------
-- Table `playlist`
-- ----------------------------------------
CREATE TABLE IF NOT EXISTS `playlist` (
    `id` INT(32) NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(255) NULL DEFAULT NULL,
    `user_id` VARCHAR(255) NULL DEFAULT NULL,
    `guild` VARCHAR(255) NULL DEFAULT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `users_id_fk`
        FOREIGN KEY(`user_id`)
        REFERENCES `users` (`id`),
    CONSTRAINT `guild_id_playlist_fk`
        FOREIGN KEY(`guild`)
        REFERENCES `guild` (`id`))
ENGINE = InnoDB;


-- ----------------------------------------
-- Table `track`
-- ----------------------------------------
CREATE TABLE IF NOT EXISTS `track` (
    `id` INT(32) NOT NULL AUTO_INCREMENT,
    `url` VARCHAR(255) UNIQUE NOT NULL,
    `title` VARCHAR(255) NULL DEFAULT NULL,
    PRIMARY KEY (`id`))
ENGINE = InnoDB;


-- ----------------------------------------
-- Table `playlist_track`
-- ----------------------------------------
CREATE TABLE IF NOT EXISTS `playlist_track` (
    `track` INT(32) NOT NULL,
    `playlist` INT(32) NOT NULL,
    `position` INT(10) NOT NULL,
    CONSTRAINT `track_id_fk`
        FOREIGN KEY (`track`)
        REFERENCES `track` (`id`),
    CONSTRAINT `playlist_id_fk`
        FOREIGN KEY (`playlist`)
        REFERENCES `playlist` (`id`))
ENGINE = InnoDB;


-- ----------------------------------------
-- Table `guild_membership`
-- ----------------------------------------
CREATE TABLE IF NOT EXISTS `guild_membership` (
    `guild` VARCHAR(255) NOT NULL,
    `user_id` VARCHAR(255) NOT NULL,
    `can_use_bot` TINYINT(1) NULL,
    CONSTRAINT `guild_membership_id_fk`
        FOREIGN KEY (`guild`)
        REFERENCES `guild` (`id`),
    CONSTRAINT `users_membership_id_fk`
        FOREIGN KEY (`user_id`)
        REFERENCES `users` (`id`))
ENGINE = InnoDB