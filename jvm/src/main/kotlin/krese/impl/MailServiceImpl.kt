package krese.impl

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.instance
import kotlinx.serialization.json.JSON
import krese.ApplicationConfiguration
import krese.JWTReceiver
import krese.MailService
import krese.MailTemplater
import krese.data.*
import org.jetbrains.exposed.sql.exposedLogger
import java.util.*
import javax.activation.MimeType
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

class MailServiceImpl(private val kodein: Kodein) : MailService, MailTemplater {
    private val appConfig: ApplicationConfiguration = kodein.instance()
    private val jwtEndpoint: JWTReceiver = kodein.instance()

    override fun sendEmail(receivers: List<Email>, bodyHTML: String, subject: String) {
        //TODO: use own logger
        exposedLogger.debug("Sending email to [${(receivers.map{it.address}.joinToString(","))}] with subject=$subject and body: $bodyHTML")

        val from = appConfig.mailFrom
        val properties: Properties = Properties().let {
            it.put("mail.smtp.auth", appConfig.mailAuth.toString())
            it.put("mail.smtp.starttls.enable", appConfig.mailStarttls.toString())
            it.put("mail.smtp.host", appConfig.mailHost)
            it.put("mail.smtp.port", appConfig.mailPort)
            it
        }
        val authenticator: Authenticator = object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(appConfig.mailUsername, appConfig.mailPassword)
            }
        }
        val session = javax.mail.Session.getInstance(properties, authenticator)


        try {
            val message = MimeMessage(session)
            //set subject
            message.subject = subject

            //set from
            message.setFrom(from)

            //set recipients
            receivers.forEach {
                message.addRecipients(Message.RecipientType.TO, InternetAddress.parse(it.address))
            }

            //set body
            message.setText(bodyHTML, Charsets.UTF_8.displayName(), "html")


            Transport.send(message)
        } catch (e: MessagingException) {

        }

    }


    override fun emailVerificationRequest(sender: Email, action: PostAction): ProcessedMailTemplate {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        return ProcessedMailTemplate(body = "body of confirmation",  subject = "Confirmation of Request required")
    }


    override fun emailNotifyCreationToCreator(action: PostAction): ProcessedMailTemplate {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        return ProcessedMailTemplate(body = "body of success to creator", subject =  "Successfully created reservation")
    }

    override fun emailNotifyCreationToModerator(action: PostAction): ProcessedMailTemplate {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        return ProcessedMailTemplate(body = "body of success to moderator", subject =  "New Reservation created")
    }


    override fun emailNotifyAcceptanceToModerator(action: AcceptAction): ProcessedMailTemplate {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        return ProcessedMailTemplate(body = "body of success to moderator",  subject = "Reservation was acccepted")
    }

    override fun emailNotifyAcceptanceToCreator(action: AcceptAction): ProcessedMailTemplate {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        return ProcessedMailTemplate(body = "body of success to creator",  subject = "Reservation was acccepted")
    }



    override fun emailNotifiyDeclineToModerator(action: DeclineAction): ProcessedMailTemplate {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        return ProcessedMailTemplate(body = "body of success to moderator",  subject = "Reservation was declined")
    }

    override fun emailNotifiyDeclineToCreator(action: DeclineAction): ProcessedMailTemplate {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        return ProcessedMailTemplate(body = "body of success to creator",  subject = "Reservation was declined")
    }

    override fun emailNotifiyWithdrawToModerator(action: WithdrawAction): ProcessedMailTemplate {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        return ProcessedMailTemplate(body = "body of success to moderator", subject =  "Reservation was withdrawn")
    }

    override fun emailNotifyWithdrawToCreator(action: WithdrawAction): ProcessedMailTemplate {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        return ProcessedMailTemplate(body = "body of success to creator", subject =  "Reservation was withdrawn")
    }




    override fun loadTemplate(type: TemplateTypes): MailTemplate {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun processTemplate(template: MailTemplate): ProcessedMailTemplate {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    private fun String.replaceVariable(variable: TemplateContants, action: PostAction?, requiresVerification: Boolean, reservable: Reservable?, reservation: Reservation?, receiver: Email) : String = if (this.contains(variable.name)) this.replace(variable.name, getVariableValue(variable, action, requiresVerification, reservable, reservation, receiver).or()) else this


    private fun getVariableValue(variable: TemplateContants, action: PostAction?, requiresVerification: Boolean, reservable: Reservable?, reservation: Reservation?, receiver: Email): String? = when(variable) {
        TemplateContants.POSITIVE_ACTION_LINK -> when(action) {
            is AcceptAction -> if (requiresVerification) jwtEndpoint.buildLink(action, receiver, reservation, reservable) else  {
                if (reservation?.email?.address.equals(receiver.address) == true) {
                    //to creator
                    reservation?.id?.let { WithdrawAction(it, "") }?.let { jwtEndpoint.buildLink(it, receiver, reservation, reservable) }
                } else {
                    //to moderator
                    null
                }
            }
            is CreateAction -> if (requiresVerification) jwtEndpoint.buildLink(action, receiver, reservation, reservable) else {
                if (reservation?.email?.address.equals(receiver.address) == true) {
                    //to creator
                    reservation?.id?.let { WithdrawAction(it, "") }?.let { jwtEndpoint.buildLink(it, receiver, reservation, reservable) }
                } else {
                    //to moderator
                    reservation?.id?.let { AcceptAction(it, "") }?.let { jwtEndpoint.buildLink(it, receiver, reservation, reservable) }
                }
            }
            is DeclineAction -> if (requiresVerification) jwtEndpoint.buildLink(action, receiver, reservation, reservable) else null //if false, no link needed
            is WithdrawAction -> if (requiresVerification) jwtEndpoint.buildLink(action, receiver, reservation, reservable) else null //if false, no link needed
            else -> null //no link needed
        }
        TemplateContants.NEGATIVE_ACTION_LINK -> TODO()
        TemplateContants.NAME_OF_CREATOR -> reservation?.name.or()
        TemplateContants.EMAIL_OF_CREATOR -> reservation?.email?.address.or()
        TemplateContants.TELEPHONE_OF_CREATOR -> reservation?.telephone.or()
        TemplateContants.TITLE_OF_RESERVABLE -> reservable?.title.or()
    }


    fun String.replaceVariables(action: PostAction?, requiresVerification: Boolean, reservable: Reservable?, reservation: Reservation?, receiver: Email): String {
        var str = this
        TemplateContants.values().forEach { str = str.replaceVariable(it, action, requiresVerification, reservable, reservation, receiver) }
        return str
    }


    fun String?.or(default: String = "null"): String = if (this != null) this else default
}
