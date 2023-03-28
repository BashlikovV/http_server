CREATE TABLE users (
    id 			INTEGER PRIMARY KEY AUTOINCREMENT,
    username 	TEXT NOT NULL,
    email 		TEXT NOT NULL,
    token 		TEXT NOT NULL,
    salt 		TEXT NOT NULL,
    createdAt 	TEXT NOT NULL,
    UNIQUE(email, token)
);

CREATE TABLE rooms (
    user1       TEXT NOT NULL,
    user2       TEXT NOT NULL,
    token       TEXT NOT NULL UNIQUE,
    CHECK(user2 != user1)
);

CREATE TABLE messages (
    room        TEXT NOT NULL,
    image       TEXT,
    value       TEXT,
    file        BLOB,
    owner       TEXT NOT NULL,
    time        TEXT NOT NULL,
    "from" TEXT NOT NULL
);