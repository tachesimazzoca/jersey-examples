DROP TABLE IF EXISTS `signup_storage`;
CREATE TABLE `signup_storage` (`storage_key` VARCHAR(255) NOT NULL default '', `storage_value` TEXT, `storage_timestamp` TIMESTAMP, PRIMARY KEY (`storage_key`));