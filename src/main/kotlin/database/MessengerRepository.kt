package database

import database.entities.Message
import database.entities.Room
import database.entities.User

interface MessengerRepository {

    /**
     * [signUp] - function for create new account in database
     * */
    fun signUp(email: String, password: String, username: String, imageUri: String)

    /**
     * [signIn] - function for getting token by email and password
     * */
    fun signIn(email: String, password: String): User

    /**
     * [getUserByToken] - function for getting user by his token
     * */
    fun getUserByToken(token: String): User

    /**
     * [updateUsernameByToken] - function for updating username using his token
     * */
    fun updateUsernameByToken(token: String, username: String)

    /**
     * [getAllUsers] - function for getting all users from database
     * */
    fun getAllUsers(token: String): List<User>

    /**
     * [getRoomByTwoUsers] - function for getting room using two users
     * */
    fun getRoomByTwoUsers(user1: User, user2: User): Room

    /**
     * [deleteRoomByTwoUsers] - function for deleting room from database
     * */
    fun deleteRoomByTwoUsers(user1: User, user2: User)

    /**
     * [addRoomByTwoUsers] - function for adding new room in rooms table. Returns new room token
     * */
    fun addRoomByTwoUsers(user1: User, user2: User): ByteArray

    /**
     * [addMessage] - function for adding new message into messages table
     * */
    fun addMessage(message: Message)

    /**
     * [deleteMessage] - function for deleting message from messages database
     * */
    fun deleteMessage(message: Message)

    /**
     * [getMessagesByRoom] - function for getting all messages between users into room
     * */
    fun getMessagesByRoom(room: Room, pagination: IntRange): List<Message>

    /**
     * [getRoomsByUser] - function for getting rooms of user
     * */
    fun getRoomsByUser(user: User): List<Room>

    /**
     * [getUserByToken] - function for getting room by room token
     * */
    fun getRoomByToken(token: String): Room

    /**
     * [addImage] - function for adding image Uri in database
     * */
    fun addImage(imageUri: String)

    /**
     * [getMaxId] - function for getting max id of image from table
     * */
    fun getMaxId(): Int

    /**
    * [readRoomMessages] - mark all messages of room as read
    * */
    fun readRoomMessages(room: Room)

    /**
     * [updateLastConnectionTime] - update last connection time in table
     * */
    fun updateLastConnectionTime(token: String)
}