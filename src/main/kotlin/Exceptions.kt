
open class AppException : RuntimeException()

class AccountAlreadyExistsException : AppException()

class AuthException : AppException()

class RoomNotFoundException : AppException()

class UserNotFoundException : AppException()