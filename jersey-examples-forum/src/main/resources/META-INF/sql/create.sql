DROP TABLE IF EXISTS `signup_storage`;
CREATE TABLE `signup_storage` (
    `storage_key` VARCHAR(255) NOT NULL default '',
    `storage_value` TEXT,
    `storage_timestamp` TIMESTAMP,
    PRIMARY KEY (`storage_key`)
);

DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
	`id` BIGINT NOT NULL AUTO_INCREMENT,
	`email` VARCHAR(255) NOT NULL default '' UNIQUE,
	`password_salt` CHAR(4) NOT NULL default '',
	`password_hash` CHAR(40) NOT NULL default '',
	`status` TINYINT(1) NOT NULL default 0,
	`modified_at` TIMESTAMP,
    PRIMARY KEY (`id`)
);
CREATE INDEX `users_email` ON `users` (`email`);