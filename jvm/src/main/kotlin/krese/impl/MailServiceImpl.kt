package krese.impl

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.instance
import krese.*
import krese.data.*
import org.jetbrains.exposed.sql.exposedLogger
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class MailServiceImpl(private val kodein: Kodein) : MailService, MailTemplater {
    private val appConfig: ApplicationConfiguration = kodein.instance()
    private val jwtEndpoint: AuthVerifier = kodein.instance()
    private val fileSystemWrapper: FileSystemWrapper = kodein.instance()

    override fun sendEmail(receivers: List<Email>, bodyHTML: String, subject: String) {
        //TODO: use own logger
        exposedLogger.debug("Sending email to [${(receivers.map { it.address }.joinToString(","))}] with subject=$subject and body: $bodyHTML")

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

    override fun construct(template: TemplateTypes, key: UniqueReservableKey, action: PostAction?, requiresVerification: Boolean, reservable: Reservable?, reservation: Reservation?, receiver: Email): ProcessedMailTemplate {
        val x = template.getMostSpecificTemplate(key, fileSystemWrapper, this.appConfig, fileSystemWrapper)
        return ProcessedMailTemplate(x.subject.replaceVariables(action, requiresVerification, reservable, reservation, receiver), x.body.replaceVariables(action, requiresVerification, reservable, reservation, receiver))
    }

    private fun String.replaceVariable(variable: TemplateContants, action: PostAction?, requiresVerification: Boolean, reservable: Reservable?, reservation: Reservation?, receiver: Email): String = if (this.contains(variable.name)) this.replace(variable.name, getVariableValue(variable, action, requiresVerification, reservable, reservation, receiver).or()) else this


    private fun getVariableValue(variable: TemplateContants, action: PostAction?, requiresVerification: Boolean, reservable: Reservable?, reservation: Reservation?, receiver: Email): String? = when (variable) {
        TemplateContants.POSITIVE_ACTION_LINK -> when (action) {
            is AcceptAction -> if (requiresVerification) jwtEndpoint.buildLink(action, receiver, reservation, reservable) else {
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
        TemplateContants.NEGATIVE_ACTION_LINK -> when (action) {
            is AcceptAction -> null
            is CreateAction -> if (requiresVerification) null else {
                if (reservation?.email?.address.equals(receiver.address) == true) {
                    null
                } else {
                    //to moderator
                    reservation?.id?.let { DeclineAction(it, "") }?.let { jwtEndpoint.buildLink(it, receiver, reservation, reservable) }
                }
            }
            is DeclineAction -> null
            is WithdrawAction -> null
            else -> null
        }
        TemplateContants.NAME_OF_CREATOR -> reservation?.name.or()
        TemplateContants.EMAIL_OF_CREATOR -> reservation?.email?.address.or()
        TemplateContants.TELEPHONE_OF_CREATOR -> reservation?.telephone.or()
        TemplateContants.TITLE_OF_RESERVABLE -> reservable?.title.or()
        TemplateContants.MODERATOR_MARKDOWN_LINKS -> TODO()
        TemplateContants.LOGIN_LINK -> TODO()
        TemplateContants.LIST_OF_RESERVED_ELEMENTS -> TODO()
        TemplateContants.RESERVATION_COMMENT -> TODO()
        TemplateContants.RESERVATION_FROM_STRING -> TODO()
        TemplateContants.RESERVATION_TO_STRING -> TODO()
        TemplateContants.LINK_DURATION -> TODO()
        TemplateContants.FULL_HOST_ROOT -> TODO()
        TemplateContants.CREATION_DATE -> TODO()
        TemplateContants.RESERVATION_DATE -> DateTime(reservation?.startTime).toString(ISODateTimeFormat.date())

    }


    fun String.replaceVariables(action: PostAction?, requiresVerification: Boolean, reservable: Reservable?, reservation: Reservation?, receiver: Email): String {
        var str = this
        TemplateContants.values().forEach { str = str.replaceVariable(it, action, requiresVerification, reservable, reservation, receiver) }
        return str
    }


    fun String?.or(default: String = "null"): String = if (this != null) this else default
}
