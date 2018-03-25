package krese

import kotlinx.serialization.json.JSON
import krese.data.*
import krese.impl.*
import org.joda.time.DateTime
import java.nio.file.Path


interface FileSystemWrapper : MailFileReader, MailFileConfigSpecific {
    fun getKeysFromDirectory() : Map<UniqueReservableKey, Path>
    fun getReservableToKey(key: UniqueReservableKey) : Reservable?
} //TODO: also includes static files in subdirectory

interface DatabaseEncapsulation {

    fun createUpdateBooking(id: Long?, data: DbBookingInputData): DbBookingOutputData?

    fun deleteBooking(id: Long) : Boolean

    fun get(id: Long?) : DbBookingOutputData?

    fun acceptBooking(id: Long) : Boolean

    fun retrieveBookingsForKey(key: UniqueReservableKey, includeMinTimestamp: DateTime = DateTime().withMillis(Long.MIN_VALUE), excludeMaxTimestamp: DateTime = DateTime().withMillis(Long.MAX_VALUE)) : List<DbBookingOutputData>

    fun isFree(key: UniqueReservableKey, blocks: List<DbBlockData>, startMillis: Long, endMillis: Long, reservableElement: ReservableElement): Boolean
}

interface JWTReceiver {
    fun receiveJWTAction(jwtAction: String) : PostResponse

    fun loginStillValid(jwt: String) : Boolean

    fun relogin(email: String)
}

interface AuthVerifier {
    fun decodeJWT(jwt: String): JWTPayload?
    fun extractEmailWithoutVerfication(jwt: String): Email?
    fun encodeJWT(content: JWTPayload): String?
    fun encodeBase64(plaintext: String): String
    fun decodeBase64(base64: String): String
}

interface GetReceiver {
    fun retrieve(key: UniqueReservableKey, from : DateTime, to : DateTime, callerEmail: Email?): GetResponse?
    fun retrieveAll(callerEmail: Email?): GetTotalResponse
}


interface PostReceiver {
    fun submitForm(reservation: PostActionInput): PostResponse
}

interface HTMLSanitizer {
    fun sanitize(html:String): String
}

interface StringLocalizer {
    fun getTranslations() : Map<String, String>
    fun getTranslationsAsJS() : String {
        val translations = getTranslations()
        return """var kreseTranslationObject = {${
        translations.map {
            """ '${it.key}' : '${it.value}' """
        }.joinToString(",\n")
        }};"""
    }
}

interface DatabaseConfiguration {
    val databasePort: String
    val databaseHost: String
    val databaseName: String
    val databaseUsername: String
    val databasePassword: String
    val databaseDriver: String
    val databaseJDBC: String
    val loadMigrationData: Boolean
}

interface ApplicationConfiguration : MailFileConfigGlobal {
    val reservablesDirectory: String
    val webDirectory: String
    val applicationHost: String
    val applicationProtocol: String
    val applicationPort: Int
    val hashSecret : String
    val mailUsername: String
    val mailPassword : String
    val mailFrom: String
    val mailTestTarget: String
    val mailHost: String
    val mailPort: Int
    val mailStarttls: Boolean
    val mailAuth: Boolean
    val filePathOfLocalization: String
}

interface MailService {
    fun sendEmail(receivers: List<Email>, bodyHTML: String, subject: String)
    fun sendEmail(receivers: List<Email>, content: ProcessedMailTemplate) = this.sendEmail(receivers, content.body, content.subject)
}


interface MailTemplater {
    fun emailVerificationRequest(sender: Email, action: PostAction): ProcessedMailTemplate
    fun emailNotifyCreationToCreator(action: PostAction): ProcessedMailTemplate
    fun emailNotifyCreationToModerator(action: PostAction): ProcessedMailTemplate
    fun emailNotifyAcceptanceToModerator(action: AcceptAction): ProcessedMailTemplate
    fun emailNotifyAcceptanceToCreator(action: AcceptAction): ProcessedMailTemplate
    fun emailNotifiyDeclineToModerator(action: DeclineAction): ProcessedMailTemplate
    fun emailNotifiyDeclineToCreator(action: DeclineAction): ProcessedMailTemplate
    fun emailNotifiyWithdrawToModerator(action: WithdrawAction): ProcessedMailTemplate
    fun emailNotifyWithdrawToCreator(action: WithdrawAction): ProcessedMailTemplate


    fun loadTemplate(type: TemplateTypes) : MailTemplate
    fun processTemplate(template: MailTemplate) : ProcessedMailTemplate
}

interface BusinessLogic {

    fun process(action: PostAction, verification: Email?, verificationValid: Boolean) : PostResponse

    fun retrieveReservations(urk: UniqueReservableKey, from : DateTime, to : DateTime, callerEmail: Email?) : GetResponse?

    fun retrieveKeys(callerEmail: Email?) : GetTotalResponse
}

