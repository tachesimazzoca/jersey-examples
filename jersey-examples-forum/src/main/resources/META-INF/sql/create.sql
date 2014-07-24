DROP TABLE IF EXISTS `session_storage`;
CREATE TABLE `session_storage` (
    `storage_key` VARCHAR(255) NOT NULL default '',
    `storage_value` TEXT,
    `storage_timestamp` TIMESTAMP,
    PRIMARY KEY (`storage_key`)
);

DROP TABLE IF EXISTS `accounts`;
CREATE TABLE `accounts` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `email` VARCHAR(255) NOT NULL default '' UNIQUE,
    `password_salt` CHAR(4) NOT NULL default '',
    `password_hash` CHAR(40) NOT NULL default '',
    `status` TINYINT(1) NOT NULL default 0,
    PRIMARY KEY (`id`)
);
CREATE INDEX `accounts_email` ON `accounts` (`email`);
INSERT INTO `accounts` (`email`, `password_salt`, `password_hash`, `status`)
    VALUES ('user1@example.net', '0000', '70352f41061eda4ff3c322094af068ba70c3b38b', 1);

DROP TABLE IF EXISTS `quetions`;
CREATE TABLE `questions` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `author_id` BIGINT NOT NULL default 0,
    `subject` TEXT,
    `body` TEXT,
    `posted_at` TIMESTAMP,
    PRIMARY KEY (`id`)
);
CREATE INDEX `questions_author_id` ON `questions` (`author_id`);
