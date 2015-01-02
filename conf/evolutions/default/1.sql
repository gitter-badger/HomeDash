# --- !Ups

CREATE TABLE `module` (
	`id` INT(10)  AUTO_INCREMENT,
	`size` INT(1),
	`module_order` INT(3),
	`plugin_id` VARCHAR(255),
	`data` TEXT,
	PRIMARY KEY (`id`)
)
;
	
CREATE TABLE `module_setting`(
	`id` INT(10) AUTO_INCREMENT,
	`module_id` INT(10),
	`name` VARCHAR(255),
	`value` VARCHAR(255),
	PRIMARY KEY (`id`)
);



CREATE TABLE `setting` (
  `name` VARCHAR(255),
  `value` VARCHAR(255),
  PRIMARY KEY (`name`)
);
# --- !Downs


drop table setting;drop table module;

drop table module_setting;




