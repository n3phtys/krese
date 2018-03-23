package krese.impl

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.instance
import kotlinx.serialization.json.JSON
import krese.ApplicationConfiguration
import krese.MailService
import krese.MailTemplate
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


    override fun emailVerificationRequest(sender: Email, action: PostAction): MailTemplate {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        return MailTemplate("body of confirmation", "Confirmation of Request required")
    }


    override fun emailNotifyCreationToCreator(action: PostAction): MailTemplate {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        return MailTemplate("body of success to creator", "Successfully created reservation")
    }

    override fun emailNotifyCreationToModerator(action: PostAction): MailTemplate {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        return MailTemplate("body of success to moderator", "New Reservation created")
    }


    override fun emailNotifyAcceptanceToModerator(action: AcceptAction): MailTemplate {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        return MailTemplate("body of success to moderator", "Reservation was acccepted")
    }

    override fun emailNotifyAcceptanceToCreator(action: AcceptAction): MailTemplate {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        return MailTemplate("body of success to creator", "Reservation was acccepted")
    }



    override fun emailNotifiyDeclineToModerator(action: DeclineAction): MailTemplate {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        return MailTemplate("body of success to moderator", "Reservation was declined")
    }

    override fun emailNotifiyDeclineToCreator(action: DeclineAction): MailTemplate {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        return MailTemplate("body of success to creator", "Reservation was declined")
    }

    override fun emailNotifiyWithdrawToModerator(action: WithdrawAction): MailTemplate {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        return MailTemplate("body of success to moderator", "Reservation was withdrawn")
    }

    override fun emailNotifyWithdrawToCreator(action: WithdrawAction): MailTemplate {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        return MailTemplate("body of success to creator", "Reservation was withdrawn")
    }


}
