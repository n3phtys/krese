package krese

import kotlin.js.Date

class KreseFrontendState {

    val keys= mutableListOf<String>()
    val selectedKey: String? = null
    val fromGetDateTime: Date = Date()
    val toGetDateTime: Date = Date()


    fun renderKeySelection(key: String?) {

    }

    fun renderTimeslotSelection(from: Date, to: Date, key: String?) {

    }

    fun postDecline() {}

    fun postAccept() {}

    fun postWithdraw() {}


    fun postFormular() {}


    fun checkFormularAvailability(): Boolean {
        TODO()
    }

    fun checkLoginWorks() : Boolean {
        TODO()
    }

    fun requestRelogin() {

    }

    fun checkUrlForLoginTokenAndActionTokenAndKey() {

    }


}