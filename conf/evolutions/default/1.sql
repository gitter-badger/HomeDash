# --- !Ups

create table module (
  id                        integer primary key AUTOINCREMENT,
  size                      integer,
  module_order              integer,
  plugin_id                 varchar(255),
  data                      TEXT)
;

create table module_setting (
  id                        integer primary key AUTOINCREMENT,
  module_id                 integer,
  name                      varchar(255),
  value                     varchar(255))
;



create table setting (
  name                      varchar(255) primary key,
  value                     varchar(255))
;
# --- !Downs

PRAGMA foreign_keys = OFF;

drop table setting;drop table module;

drop table module_setting;



PRAGMA foreign_keys = ON;

