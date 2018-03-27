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
import org.joda.time.DateTime
import java.io.UnsupportedEncodingException
import java.util.*

class AuthVerifierImpl(private val kodein: Kodein) : AuthVerifier {

    private val issuer = "krese"
    private val key_email = "email"
    private val key_actionjson = "actionjson"
    private val key_actiontag = "actiontag"

    override fun encodeJWT(content: JWTPayload): String? {
        try {
            val jsonAndTag = toJson(content.action)
            val claimssame = JWT.create()
                    .withIssuer(issuer)
                    .withClaim(key_email, content.userProfile.email.address)
                    .withIssuedAt(Date())
                    .withExpiresAt(Date(content.userProfile.validTo))
                    .withNotBefore(Date(content.userProfile.validFrom))
            val claims = if (content.action != null) {
                claimssame.withClaim(key_actiontag, jsonAndTag.first)
                        .withClaim(key_actionjson, jsonAndTag.second)
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
            val actionTag = decoded.claims.get(key_actiontag)
            val actionJson = decoded.claims.get(key_actionjson)
            var action: PostAction? = null
            try {
                action = buildFromJson(actionTag?.asString(), actionJson?.asString())
            } catch (ignored: NullPointerException) {

            } catch (ignored: IllegalArgumentException) {

            }
            return JWTPayload(action, listOf(), UserProfile(Email(decoded.getClaim(key_email).asString()), decoded.notBefore.time, decoded.expiresAt.time))
        } catch (e: UnsupportedEncodingException) {
            return null
        } catch (e: JWTVerificationException) {
            return null
        }
    }


    override fun buildLink(action: PostAction?, receiver: Email, reservation: Reservation?, reservable: Reservable?): String {
        val loginFrom = DateTime.now()
        val loginTo = DateTime.now().plusDays(2)
        val actionFrom = DateTime.now()
        val actionTo = DateTime.now().plusDays(90)
        val reloginjwt: String? = encodeJWT(JWTPayload(null, listOf(), buildUserProfile(receiver, loginFrom, loginTo)))
        val actionjwt: String? = encodeJWT(JWTPayload(action, listOf(), buildUserProfile(receiver, actionFrom, actionTo)))
        val key: String? = reservable?.uniqueId

        val map: Map<String, String> = mapOf("relogin" to reloginjwt, "action" to actionjwt, "selected_key" to key).filter { it.value != null }.mapValues { it.value!! }

        val link = "${appConfig.applicationProtocol}://${appConfig.applicationHost}:${appConfig.applicationPort}/index.html?${map.map { "${it.key}=${it.value}" }.joinToString("&")}"

        return link
    }
}
