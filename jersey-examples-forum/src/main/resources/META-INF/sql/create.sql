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
    `nickname` VARCHAR(255) NOT NULL default '',
    `status` TINYINT(1) NOT NULL default 0,
    PRIMARY KEY (`id`)
);
CREATE INDEX `accounts_email` ON `accounts` (`email`);
INSERT INTO `accounts` (`email`, `password_salt`, `password_hash`, `nickname`, `status`)
    VALUES ('alice@example.net', '0000', '70352f41061eda4ff3c322094af068ba70c3b38b', 'Alice', 1);
INSERT INTO `accounts` (`email`, `password_salt`, `password_hash`, `nickname`, `status`)
    VALUES ('bob@example.net', '0000', '70352f41061eda4ff3c322094af068ba70c3b38b', 'Bob', 1);
INSERT INTO `accounts` (`email`, `password_salt`, `password_hash`, `nickname`, `status`)
    VALUES ('carol@example.net', '0000', '70352f41061eda4ff3c322094af068ba70c3b38b', 'Carol', 1);
INSERT INTO `accounts` (`email`, `password_salt`, `password_hash`, `nickname`, `status`)
    VALUES ('ellen@example.net', '0000', '70352f41061eda4ff3c322094af068ba70c3b38b', 'Ellen', 1);
INSERT INTO `accounts` (`email`, `password_salt`, `password_hash`, `nickname`, `status`)
    VALUES ('frank@example.net', '0000', '70352f41061eda4ff3c322094af068ba70c3b38b', 'Frank', 1);

DROP TABLE IF EXISTS `questions`;
CREATE TABLE `questions` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `author_id` BIGINT NOT NULL default 0,
    `subject` TEXT,
    `body` TEXT,
    `posted_at` TIMESTAMP,
    `status` TINYINT(1) NOT NULL default 0,
    PRIMARY KEY (`id`)
);
CREATE INDEX `questions_author_id` ON `questions` (`author_id`);

DROP TABLE IF EXISTS `answers`;
CREATE TABLE `answers` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `question_id` BIGINT NOT NULL default 0,
    `author_id` BIGINT NOT NULL default 0,
    `body` TEXT,
    `posted_at` TIMESTAMP,
    `status` TINYINT(1) NOT NULL default 0,
    PRIMARY KEY (`id`)
);
CREATE INDEX `answers_question_id` ON `answers` (`question_id`);
CREATE INDEX `answers_author_id` ON `answers` (`author_id`);

DROP TABLE IF EXISTS `account_questions`;
CREATE TABLE `account_questions` (
    `account_id` BIGINT NOT NULL default 0,
    `question_id` BIGINT NOT NULL default 0,
    `point` INT NOT NULL default 0,
    `modified_at` TIMESTAMP
);
CREATE INDEX `account_questions_account_id` ON `account_questions` (`account_id`);
CREATE INDEX `account_questions_question_id` ON `account_questions` (`question_id`);

DROP TABLE IF EXISTS `account_answers`;
CREATE TABLE `account_answers` (
    `account_id` BIGINT NOT NULL default 0,
    `answer_id` BIGINT NOT NULL default 0,
    `point` INT NOT NULL default 0,
    `modified_at` TIMESTAMP
);
CREATE INDEX `account_answers_account_id` ON `account_answers` (`account_id`);
CREATE INDEX `account_answers_answer_id` ON `account_answers` (`answer_id`);
