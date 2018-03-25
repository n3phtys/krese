package krese.utility

import krese.ApplicationConfiguration
import krese.AuthVerifier
import krese.data.*

fun PostAction.getId() : Long? {
    return when(this) {

        is CreateAction -> null
        is DeclineAction -> this.id
        is WithdrawAction -> this.id
        is AcceptAction -> this.id
        else -> {throw IllegalArgumentException()}
    }
}

fun PostAction.toActionLink(appConfig: ApplicationConfiguration, authVerifier: AuthVerifier) : String {
    TODO("not yet implemented")
}