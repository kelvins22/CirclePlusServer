CREATE TABLE user (
    id          BIGINT NOT NULL,    # unique id, primary key
    name        VARCHAR(31),        # max length is 31
    password    VARCHAR(31),
    email       VARCHAR(31),
    gender      BOOL,               # male true, female false
    phone       CHAR(13),           # xx-yyy-yyyy-yyyy
    photo       TEXT,               # photo url
    created     TIMESTAMP,
    checkin_count   INT,
    follower_count  INT,
    friend_count    INT,
    reserved        TEXT,
    CONSTRAINT pk_user PRIMARY KEY (id)
);

CREATE TABLE location (
    id          BIGINT NOT NULL,
    name        VARCHAR(255),
    lat         BIGINT NOT NULL,
    lng         BIGINT NOT NULL,
    nation      VARCHAR(31),
    province    VARCHAR(31),
    city        VARCHAR(31),
    address     VARCHAR(255),
    type        INT,
    md5         TEXT,
    reserved    TEXT,
    CONSTRAINT pk_location PRIMARY KEY (id)
);

CREATE TABLE checkin (
    id          BIGINT NOT NULL,
    name        VARCHAR(255),
    created     TIMESTAMP,
    shout       TEXT,
    score       INT,
    user_id     BIGINT NOT NULL,
    loc_id      BIGINT NOT NULL,
    CONSTRAINT pk_checkin PRIMARY KEY (id)
);

ALTER TABLE checkin ADD CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE checkin ADD CONSTRAINT fk_loc FOREIGN KEY (loc_id) REFERENCES location (id) ON DELETE RESTRICT ON UPDATE RESTRICT;

CREATE INDEX idx_checkin_user ON user (id);

