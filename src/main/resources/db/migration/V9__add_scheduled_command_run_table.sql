CREATE TABLE IF NOT EXISTS `scheduled_command_runs` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `finished` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `runtime` BIGINT NOT NULL,
    `commands_run` INT NOT NULL,
    PRIMARY KEY(`id`))
ENGINE = InnoDB;