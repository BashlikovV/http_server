package model.rooms

import server.entities.*

interface RoomsRepository {

    fun getRooms(body: GetRoomsRequestBody): GetRoomsResponseBody

    fun addRoom(body: AddRoomRequestBody): AddRoomResponseBody

    fun deleteRoom(body: DeleteRoomRequestBody): DeleteRoomResponseBody

    fun getRoom(body: GetRoomRequestBody): GetRoomResponseBody
}