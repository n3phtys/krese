package krese

import com.google.gson.Gson


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


