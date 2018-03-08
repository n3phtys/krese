package krese.impl

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.instance
import krese.*
import krese.data.*
import org.joda.time.DateTime

class JWTReceiverImpl(private val kodein: Kodein): JWTReceiver {

    private val authVerifier: AuthVerifier = kodein.instance()
    private val appConfig: ApplicationConfiguration = kodein.instance()
    private val mailService: MailService = kodein.instance()
    private val postReceiver: PostReceiver = kodein.instance()

    override fun receiveJWTAction(jwtAction: String): PostResponse {
        val action: JWTPayload? = authVerifier.decodeJWT(jwtAction)
        val postAction: PostAction? = action?.extractAction()
        if (postAction != null) {
            return postReceiver.submitForm(PostActionInput(action, postAction))
        } else {
            return PostResponse(false, false, "could not parse action in jwt")
        }
    }

    override fun loginStillValid(jwt: String): Boolean {
        return authVerifier.decodeJWT(jwt) != null
    }

    override fun relogin(email: String) {
        //check if email is legal & create jwt for user
        val recipient = Email(email)
        val from = DateTime.now()
        val to = DateTime.now().plusDays(2)
        val jwt = authVerifier.encodeJWT(JWTPayload(null, listOf(), buildUserProfile(recipient, from, to)))
        //build link that can be parsed by client (by checking params)
        val link = "${appConfig.applicationProtocol}://${appConfig.applicationHost}:${appConfig.applicationPort}/index.html?relogin=$jwt" //TODO: deal with reverse proxy by making this configurable
        //send link to given email adress
        val mailBody = "To login into Krese, follow this link: <a href=\"$link\">$link</a>"
        val mailSubject = "Krese Login Request"

        mailService.sendEmail(listOf(recipient), mailBody, mailSubject)
    }

    //TODO replace with a public / private key infrastructure, by creating a private key in clientside localstorage
}
