package krese.utility

import krese.data.*

fun PostAction.getId() : Long? {
    return when(this) {

        is CreateAction -> null
        is DeclineAction -> this.id
        is WithdrawAction -> this.id
        is AcceptAction -> this.id
    }
}