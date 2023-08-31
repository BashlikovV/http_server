CREATE TABLE IF NOT EXISTS users (
    id          INTEGER primary key autoincrement,
    username    TEXT not null,
    email       TEXT not null unique,
    token       TEXT not null,
    salt        TEXT not null,
    createdAt   TEXT not null,
    image       TEXT,
    unique (email, token)
);


CREATE TABLE rooms (
    user1   TEXT NOT NULL,
    user2   TEXT NOT NULL,
    token   TEXT NOT NULL UNIQUE,
    CHECK(user2 != user1)
);

CREATE TABLE messages (
    room    TEXT NOT NULL,
    image   TEXT,
    value   TEXT,
    file    BLOB,
    owner   TEXT NOT NULL,
    time    TEXT NOT NULL,
    "from"  TEXT,
    isRead  integer
);

CREATE TABLE images (
    id      INTEGER PRIMARY KEY AUTOINCREMENT,
    image   TEXT NOT NULL UNIQUE
);
