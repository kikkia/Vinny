CREATE TABLE IF NOT EXISTS `scheduled_command_failures` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `command_id` INT NOT NULL,
    `attempted` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `message` TEXT NOT NULL,
    PRIMARY KEY(`id`));