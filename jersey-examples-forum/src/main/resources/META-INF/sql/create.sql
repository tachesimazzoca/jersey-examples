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
    VALUES ('user1@example.net', '0000', '70352f41061eda4ff3c322094af068ba70c3b38b', 'user1', 1);

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

DROP TABLE IF EXISTS `favorite_questions`;
CREATE TABLE `favorite_questions` (
    `author_id` BIGINT NOT NULL default 0,
    `question_id` BIGINT NOT NULL default 0,
    `modified_at` TIMESTAMP
);
CREATE INDEX `favorite_questions_author_id` ON `favorite_questions` (`author_id`);
CREATE INDEX `favorite_questions_question_id` ON `favorite_questions` (`question_id`);

DROP TABLE IF EXISTS `answer_points`;
CREATE TABLE `answer_points` (
    `author_id` BIGINT NOT NULL default 0,
    `answer_id` BIGINT NOT NULL default 0,
    `point` INT NOT NULL default 0,
    `modified_at` TIMESTAMP
);
CREATE INDEX `answer_points_author_id` ON `answer_points` (`author_id`);
CREATE INDEX `answer_points_answer_id` ON `answer_points` (`answer_id`);