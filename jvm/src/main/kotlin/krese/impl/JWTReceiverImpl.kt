package krese.impl

import com.github.salomonbrys.kodein.Kodein
import krese.JWTReceiver
import krese.data.PostResponse

class JWTReceiverImpl(private val kodein: Kodein): JWTReceiver {
    override fun receiveJWT(jwt: String): PostResponse {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun loginStillValid(jwt: String): Boolean {
        return true
    }

    override fun relogin(email: String): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}