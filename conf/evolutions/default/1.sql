# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table checkin (
  id                        bigint auto_increment not null,
  name                      varchar(255),
  created                   datetime,
  shout                     varchar(255),
  score                     integer,
  user_id                   bigint,
  loc_id                    bigint,
  constraint pk_checkin primary key (id))
;

create table loc (
  id                        bigint auto_increment not null,
  name                      varchar(255),
  nation                    varchar(255),
  province                  varchar(255),
  city                      varchar(255),
  address                   varchar(255),
  md5                       varchar(255),
  lat                       bigint,
  lng                       bigint,
  type                      integer,
  created                   datetime,
  constraint pk_loc primary key (id))
;

create table user (
  id                        bigint auto_increment not null,
  name                      varchar(255),
  password                  varchar(255),
  email                     varchar(255),
  phone                     varchar(255),
  photo                     varchar(255),
  gender                    tinyint(1) default 0,
  created                   datetime,
  checkin_count             integer,
  follower_count            integer,
  friend_count              integer,
  constraint pk_user primary key (id))
;




# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table checkin;

drop table loc;

drop table user;

SET FOREIGN_KEY_CHECKS=1;

