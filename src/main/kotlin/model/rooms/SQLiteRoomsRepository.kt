package model.rooms

import database.SQLiteContract
import database.SQLiteMessengerRepository
import database.entities.Room
import server.entities.*
import utils.SecurityUtils
import utils.SecurityUtilsImpl

class SQLiteRoomsRepository(
    databaseUrl: String = SQLiteContract.MESSENGER_SQLITE_DATABASE_URL
) : RoomsRepository {

    private val messengerRepository = SQLiteMessengerRepository(databaseUrl)

    private val securityUtils: SecurityUtils = SecurityUtilsImpl()

    override fun getRooms(body: GetRoomsRequestBody): GetRoomsResponseBody {
        return try {
            val user = messengerRepository.getUserByToken(body.user)
            val rooms = messengerRepository.getRoomsByUser(user)

            GetRoomsResponseBody(rooms)
        } catch (e: Exception) {
            e.printStackTrace()
            GetRoomsResponseBody(listOf())
        }
    }

    override fun addRoom(body: AddRoomRequestBody): AddRoomResponseBody {
        return try {
            val user1 = messengerRepository.getUserByToken(body.user1)
            val user2 = messengerRepository.getUserByToken(body.user2)
            val token = messengerRepository.addRoomByTwoUsers(user1, user2)

            AddRoomResponseBody(securityUtils.bytesToString(token))
        } catch (e: Exception) {
            e.printStackTrace()
            AddRoomResponseBody("500 ERROR")
        }
    }

    override fun deleteRoom(body: DeleteRoomRequestBody): DeleteRoomResponseBody {
        return try {
            val user1 = messengerRepository.getUserByToken(body.user1)
            val user2 = messengerRepository.getUserByToken(body.user2)

            messengerRepository.deleteRoomByTwoUsers(
                user1 = user1,
                user2 = user2
            )

            DeleteRoomResponseBody("200 OK")
        } catch (e: Exception) {
            e.printStackTrace()
            DeleteRoomResponseBody("500 ERROR")
        }
    }

    override fun getRoom(body: GetRoomRequestBody): GetRoomResponseBody {
        return try {
            val user1 = messengerRepository.getUserByToken(body.user1)
            val user2 = messengerRepository.getUserByToken(body.user2)
            val room = messengerRepository.getRoomByTwoUsers(user1, user2)

            GetRoomResponseBody(room)
        } catch (e: Exception) {
            e.printStackTrace()
            GetRoomResponseBody(Room())
        }
    }
}