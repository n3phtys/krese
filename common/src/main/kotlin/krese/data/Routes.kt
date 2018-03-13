package krese.data

enum class Routes(val path:String) {
    GET_RESERVABLES("reservable"),
    GET_ENTRIES_TO_RESERVABLE("reservable/"),
    POST_ENTRIES_TO_RESERVABLE(GET_ENTRIES_TO_RESERVABLE.name),
    POST_CREDENTIALS_VALID("util/valid/credentials"),
    POST_RELOGIN("util/login"),
    POST_JWTACTION("util/jwtaction")
}
