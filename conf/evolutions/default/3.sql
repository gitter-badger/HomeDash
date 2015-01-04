# --- !Ups

ALTER TABLE `module` ADD `page` INT(2) DEFAULT 1;

CREATE TABLE `page` (
	`id` INT(3) AUTO_INCREMENT,
	`name` VARCHAR(255),
	PRIMARY KEY (`id`)
);

INSERT INTO `page` (`name`) VALUES('Main');

# --- !Downs

DROP TABLE `page`;

ALTER TABLE `module` DROP COLUMN `page`;