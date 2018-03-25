package krese.data

enum class Routes(val path:String) {
    GET_RESERVABLES("reservable"),
    POST_ACTION_CREATE("reservable/create"),
    POST_ACTION_DECLINE("reservable/decline"),
    POST_ACTION_WITHDRAW("reservable/withdraw"),
    POST_ACTION_ACCEPT("reservable/withdraw"),
    POST_CREDENTIALS_VALID("util/valid/credentials"),
    POST_RELOGIN("util/login"),
    GET_LOCALIZATION("util/locale/properties"),
}
