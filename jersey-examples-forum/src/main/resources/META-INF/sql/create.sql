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
    `modified_at` TIMESTAMP,
    PRIMARY KEY (`id`)
);
CREATE INDEX `accounts_email` ON `accounts` (`email`);
