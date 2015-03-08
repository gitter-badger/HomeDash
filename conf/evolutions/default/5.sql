# --- !Ups

ALTER TABLE`module`
ADD `col` INT(5) DEFAULT 1,
ADD `row` INT(5) DEFAULT 1,
CHANGE `module_order` `mobile_order` INT(3) DEFAULT 0;

# --- !Downs

ALTER TABLE`module`
DROP `col`,
DROP `row`,
CHANGE `mobile_order` `module_order` INT(3) DEFAULT 0;