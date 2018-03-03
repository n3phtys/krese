package krese.impl

import com.github.salomonbrys.kodein.Kodein
import com.google.gson.Gson
import krese.AuthVerifier
import krese.UserProfile

class AuthVerifierImpl(private val kodein: Kodein) : AuthVerifier {
    override fun createNewPrivateKey() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun encodeJWT(content: Any): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun encodeBase64(plaintext: String): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun decodeBase64(base64: String): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun decodeJWT(jwt: String): UserProfile? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}



data class RawJWT(val text: String) {
    fun deserialize() : FullJWT? {
        return Gson().fromJson(text, FullJWT::class.java)
    }
}

data class JWTPayload(val fromMillis: Long, val toMillis: Long, val randomNumber: Long, val contentJson: String) {
    fun computeMAC(macGen : javax.crypto.Mac): String {
        val str = this.serializePayload()
        TODO("implemented the computation of a MAC")
    }

    fun serializePayload(): String {
        return Gson().toJson(this)
    }

    fun sign(macGen : javax.crypto.Mac) : FullJWT {
        return FullJWT(this, this.computeMAC(macGen))
    }
}

data class FullJWT(val payload: JWTPayload, val mac: String) {
    fun serialize() : RawJWT {
        return RawJWT(Gson().toJson(this))
    }

    fun isValid(macGen : javax.crypto.Mac) : Boolean {
        return payload.computeMAC(macGen).contentEquals(mac)
    }
}