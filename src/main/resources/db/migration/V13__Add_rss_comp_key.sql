ALTER TABLE `rss_subscription` ADD CONSTRAINT `CK_subject_provider` UNIQUE index(`subject`, `provider`);