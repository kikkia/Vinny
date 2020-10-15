CREATE TABLE `rss_subscription` (
  `id` int(255) NOT NULL AUTO_INCREMENT,
  `subject` varchar(255) NOT NULL,
  `provider` int(32) NOT NULL,
  `lastScanAttempt` bigint(20) NOT NULL,
  `lastScanComplete` bigint(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB;

CREATE TABLE `channel_rss_subscription` (
  `id` int(255) NOT NULL AUTO_INCREMENT,
  `rss_subscription_id` int(255) NOT NULL,
  `text_channel_id` varchar(255) NOT NULL,
  `keyword` varchar(1024) DEFAULT NULL,
  `author` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `rss_sub_fk` (`rss_subscription_id`),
  KEY `rss_channel_fk` (`text_channel_id`),
  KEY `rss_author_fk` (`author`),
  CONSTRAINT `rss_author_fk` FOREIGN KEY (`author`) REFERENCES `users` (`id`),
  CONSTRAINT `rss_channel_fk` FOREIGN KEY (`text_channel_id`) REFERENCES `text_channel` (`id`),
  CONSTRAINT `rss_sub_fk` FOREIGN KEY (`rss_subscription_id`) REFERENCES `rss_subscription` (`id`)
) ENGINE=InnoDB;

