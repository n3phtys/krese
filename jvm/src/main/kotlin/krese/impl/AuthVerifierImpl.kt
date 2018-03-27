package krese.impl

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTCreationException
import com.auth0.jwt.exceptions.JWTVerificationException
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.instance
import krese.ApplicationConfiguration
import krese.AuthVerifier
import krese.data.*
import java.io.UnsupportedEncodingException
import java.util.*

class AuthVerifierImpl(private val kodein: Kodein) : AuthVerifier {

    private val issuer = "krese"
    private val key_email = "email"
    private val key_params = "params"
    private val key_action = "action"

    override fun encodeJWT(content: JWTPayload): String? {
        try {
            val claimssame = JWT.create()
                    .withIssuer(issuer)
                    .withClaim(key_email, content.userProfile.email.address)
                    .withClaim(key_action, content.action.toString())
                    .withArrayClaim(key_params, content.params.toTypedArray())
                    .withIssuedAt(Date())
                    .withExpiresAt(Date(content.userProfile.validTo))
                    .withNotBefore(Date(content.userProfile.validFrom))
            val claims = if (content.action != null) {
                claimssame.withClaim(key_action, content.action.toString())
            } else {
                claimssame
            }
            val token = claims.sign(algorithm)
            return token
        } catch (e: UnsupportedEncodingException) {
            return null
        } catch (e: JWTCreationException) {
            return null
        }
    }

    private val appConfig: ApplicationConfiguration = kodein.instance()

    private val algorithm: Algorithm
            get() = Algorithm.HMAC256(appConfig.hashSecret)


    override fun encodeBase64(plaintext: String): String = String(Base64.getEncoder().encode(plaintext.toByteArray(Charsets.UTF_8)),Charsets.UTF_8)

    override fun decodeBase64(base64: String): String = String(Base64.getDecoder().decode(base64), Charsets.UTF_8)


    override fun extractEmailWithoutVerfication(jwt: String): Email? {
        try {
            return Email(JWT.decode(jwt).getClaim(key_email).asString())
        } catch (e : Exception) {
            return null
        }
    }



    override fun decodeJWT(jwt: String): JWTPayload? {
        try {
            val decoded = JWT.require(algorithm).withIssuer(issuer).build().verify(jwt)
            val actionStr = decoded.claims.get(key_action)
            var action : LinkActions? = null
            try {
                action = LinkActions.valueOf(actionStr!!.asString())
            } catch (ignored: NullPointerException) {

            } catch (ignored: IllegalArgumentException) {

            }
            return JWTPayload(action, decoded.getClaim(key_params).asList(String::class.java), UserProfile(Email(decoded.getClaim(key_email).asString()), decoded.notBefore.time, decoded.expiresAt.time))
        } catch (e: UnsupportedEncodingException) {
            return null
        } catch (e: JWTVerificationException) {
            return null
        }
    }


    override fun buildLink(action: PostAction, receiver: Email, reservation: Reservation?, reservable: Reservable?): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
