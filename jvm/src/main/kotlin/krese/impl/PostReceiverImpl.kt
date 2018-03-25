package krese.impl

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.instance
import krese.AuthVerifier
import krese.BusinessLogic
import krese.PostReceiver
import krese.data.Email
import krese.data.PostActionInput
import krese.data.PostResponse


class PostReceiverImpl(private val kodein: Kodein) : PostReceiver {
    private val authVerifier: AuthVerifier = kodein.instance()
    private val businessLogic: BusinessLogic = kodein.instance()

    override fun submitForm(reservation: PostActionInput): PostResponse {

        val valid: Boolean = reservation.toJwt()?.let { authVerifier.decodeJWT(it)?.userProfile?.email } != null
        val verification: Email? = reservation.toJwt()?.let { authVerifier.extractEmailWithoutVerfication(it) }

        return businessLogic.process(reservation.toAction(), verification, valid)
    }
}