package server

class HttpHandlerImpl : HttpHandler {
    override fun handle(request: HttpRequest, response: HttpResponse): String? {
        return when (request.url.substringBefore("?").substringAfter("/")) {
            HttpContract.UrlMethods.SIGN_UP -> {
                response.handleSignUpRequest(request = request)
            }
            HttpContract.UrlMethods.SIGN_IN -> {
                response.handleSignInRequest(request = request)
            }
            HttpContract.UrlMethods.ROOM_MESSAGES -> {
                response.handleGetRoomMessagesRequest(request = request)
            }
            HttpContract.UrlMethods.GET_ROOMS -> {
                response.handleGetRoomsRequest(request = request)
            }
            HttpContract.UrlMethods.ADD_ROOM -> {
                response.handleAddRoomRequest(request = request)
            }
            HttpContract.UrlMethods.ADD_MESSAGE -> {
                response.handleAddMessageRequest(request = request)
            }
            HttpContract.UrlMethods.GET_USERS -> {
                response.handleGetAllUsersRequest()
            }
            HttpContract.UrlMethods.DELETE_ROOM -> {
                response.handleDeleteRoomRequest(request = request)
            }
            HttpContract.UrlMethods.GET_USERNAME -> {
                response.handleGetUsernameRequest(request = request)
            }
            HttpContract.UrlMethods.GET_ROOM -> {
                response.handleGetRoomRequest(request = request)
            }
            HttpContract.UrlMethods.GET_USER -> {
                response.handleGetUserRequest(request = request)
            }
            HttpContract.UrlMethods.DELETE_MESSAGE -> {
                response.handleDeleteMessageRequest(request = request)
            }
            HttpContract.UrlMethods.ADD_IMAGE -> {
                response.handleAddImageRequest(request = request)
            }
            else -> {
                "Error"
            }
        }
    }
}