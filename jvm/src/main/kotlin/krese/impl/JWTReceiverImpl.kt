package krese.impl

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.instance
import krese.*
import krese.data.*

class JWTReceiverImpl(private val kodein: Kodein): JWTReceiver {

    private val authVerifier: AuthVerifier = kodein.instance()
    private val appConfig: ApplicationConfiguration = kodein.instance()
    private val mailService: MailService = kodein.instance()
    private val mailTemplater: MailTemplater = kodein.instance()
    private val postReceiver: PostReceiver = kodein.instance()

    init {
        //this.relogin(appConfig.mailTestTarget)
    }

    override fun receiveJWTAction(jwtAction: String): PostResponse {
        val action: JWTPayload? = authVerifier.decodeJWT(jwtAction)
        val postAction: PostAction? = action?.action
        if (postAction != null) {
            return postReceiver.submitForm(PostActionInput.build(jwtAction, postAction))
        } else {
            return PostResponse(false, false, "could not parse action in jwt")
        }
    }

    override fun loginStillValid(jwt: String): Boolean {
        return authVerifier.decodeJWT(jwt) != null
    }

    override fun relogin(email: String, key: UniqueReservableKey?) {
        val recipient = Email(email)
        //use mail template
        val mail = mailTemplater.construct(TemplateTypes.LoginVerification, key, null, true, null, null, recipient)
        mailService.sendEmail(recipient, mail)
    }

    //TODO replace with a public / private key infrastructure, by creating a private key in clientside localstorage


}
