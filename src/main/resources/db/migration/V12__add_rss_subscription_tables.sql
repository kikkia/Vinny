CREATE TABLE IF NOT EXISTS `rss_subscription` (
    `id` INT(255) NOT NULL AUTO_INCREMENT,
    `url` VARCHAR(255) NOT NULL,
    `provider` INT(32) NOT NULL,
    `lastScanAttempt` BIGINT NOT NULL,
    `lastScanComplete` BIGINT NOT NULL,
    PRIMARY KEY (`id`))
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `channel_rss_subscription` (
    `id` INT(255) NOT NULL AUTO_INCREMENT,
    `rss_subscription_id` INT(255) NOT NULL,
    `text_channel_id` VARCHAR(255) NOT NULL,
    `keyword` varchar(1024) NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `rss_sub_fk`
        FOREIGN KEY (`rss_subscription_id`)
        REFERENCES `rss_subscription` (`id`),
    CONSTRAINT `rss_channel_fk`
        FOREIGN KEY (`text_channel_id`)
        REFERENCES `text_channel` (`id`))
ENGINE = InnoDB;