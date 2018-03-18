package krese.impl

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.instance
import krese.*
import krese.data.*
import org.joda.time.DateTime


class PostReceiverImpl(private val kodein: Kodein) : PostReceiver {
    private val authVerifier: AuthVerifier = kodein.instance()
    private val businessLogic: BusinessLogic = kodein.instance()

    override fun submitForm(reservation: PostActionInput): PostResponse {

        val valid: Boolean = reservation.jwt?.let { authVerifier.decodeJWT(it)?.userProfile?.email } != null
        val verification: Email? = reservation.jwt?.let { authVerifier.extractEmailWithoutVerfication(it) }

        return businessLogic.process(reservation.action, verification, valid)
    }
}