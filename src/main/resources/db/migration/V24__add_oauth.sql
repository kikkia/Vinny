CREATE TABLE `user_oauth_config` (
  `id` varchar(255) NOT NULL,
  `refresh_token` varchar(1024) NOT NULL,
  `access_token` varchar(1024) NOT NULL,
  `token_type` varchar(255) NOT NULL,
  `expiry` BIGINT NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB;