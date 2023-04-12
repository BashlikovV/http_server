package database

object SQLiteContract {

    const val MESSENGER_SQLITE_DATABASE_URL = "jdbc:sqlite:messenger.db"

    object UsersTable {
        const val TABLE_NAME = "users"
        const val COLUMN_ID = "id"
        const val COLUMN_USERNAME = "username"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_TOKEN = "token"
        const val COLUMN_SALT = "salt"
        const val COLUMN_CREATED_AT = "createdAt"
    }

    object RoomsTable {
        const val TABLE_NAME = "rooms"
        const val COLUMN_USER_1 = "user1"
        const val COLUMN_USER_2 = "user2"
        const val COLUMN_TOKEN = "token"
    }

    object MessagesTable {
        const val TABLE_NAME = "messages"
        const val COLUMN_ROOM = "room"
        const val COLUMN_IMAGE = "image"
        const val COLUMN_VALUE = "value"
        const val COLUMN_FILE = "file"
        const val COLUMN_OWNER = "owner"
        const val COLUMN_TIME = "time"
        const val COLUMN_FROM = "from"
    }

    object ImagesTable {
        const val TABLE_NAME = "images"
        const val COLUMN_IMAGE = "image"
        const val COLUMN_ID = "id"
    }
}