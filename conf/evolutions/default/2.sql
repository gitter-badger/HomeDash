# --- !Ups

ALTER TABLE `module` ADD `remote` INT(1) DEFAULT 0

# --- !Downs

ALTER TABLE `module` DROP COLUMN `remote`;