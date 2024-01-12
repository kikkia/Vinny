CREATE TABLE `resume_audio_guild` (
  `id` varchar(255) NOT NULL,
  `voice_channel_id` varchar(255) NOT NULL,
  `text_channel_id` varchar(255) NOT NULL,
  `volume` int(20) NOT NULL,
  `volume_locked` TINYINT(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB;

CREATE TABLE `resume_audio_track` (
  `id` int(255) NOT NULL AUTO_INCREMENT,
  `resume_guild` VARCHAR(255) NOT NULL,
  `track_url` varchar(2048) NOT NULL,
  `requester_name` varchar(255) NOT NULL,
  `requester_id` BIGINT(64) DEFAULT NULL,
  `track_position` BIGINT(64),
  `track_index` INT(255) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `resume_audio_guild_track` FOREIGN KEY (`resume_guild`) REFERENCES `resume_audio_guild` (`id`)
) ENGINE=InnoDB;