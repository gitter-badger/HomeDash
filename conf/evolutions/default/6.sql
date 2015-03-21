# --- !Ups

CREATE TABLE `remote_favorite` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(100) DEFAULT NULL,
  `apikey` varchar(50) DEFAULT NULL,
  `url` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`)
)

# --- !Downs

DROP TABLE `remote_favorite`;