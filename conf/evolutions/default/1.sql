# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table checkin (
  id                        bigint auto_increment not null,
  name                      varchar(255),
  created                   datetime,
  shout                     varchar(255),
  user_id                   bigint,
  loc_id                    bigint,
  constraint pk_checkin primary key (id))
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

drop table user;

SET FOREIGN_KEY_CHECKS=1;

