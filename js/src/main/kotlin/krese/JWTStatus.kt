package krese

enum class JWTStatus(val glyphname: String, val buttonStyle: String) {
    UNCHECKED("glyphicon glyphicon-plus", "btn-primary"),
    PENDING("glyphicon glyphicon-refresh", "btn-warning"),
    VALID("glyphicon glyphicon-ok", "btn-success"),
    INVALID("glyphicon glyphicon-remove", "btn-cancel")
}

val LOCALSTORAGE_KRESE_MY_EMAIL = "LOCALSTORAGE_KRESE_MY_EMAIL"