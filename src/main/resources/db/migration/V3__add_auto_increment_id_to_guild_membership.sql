ALTER TABLE `guild_membership`
    ADD guild_membership_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY;

ALTER TABLE `guild_membership`
    ADD CONSTRAINT unique_pair UNIQUE KEY (guild, user_id)