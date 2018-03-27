package krese

import krese.data.*
import krese.impl.DbBookingInputData
import krese.impl.DbBookingOutputData
import org.joda.time.DateTime
import java.nio.file.Path


interface FileSystemWrapper : MailFileReader, MailFileConfigSpecific {
    fun getKeysFromDirectory() : Map<UniqueReservableKey, Path>
    fun getReservableToKey(key: UniqueReservableKey) : Reservable?
} //TODO: also includes static files in subdirectory

interface DatabaseEncapsulation {

    fun createUpdateBooking(id: Long?, data: DbBookingInputData): DbBookingOutputData?

    fun deleteBooking(id: Long): DbBookingOutputData?

    fun get(id: Long?) : DbBookingOutputData?

    fun acceptBooking(id: Long): DbBookingOutputData?

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

    fun buildLink(action: PostAction?, receiver: Email, reservation: Reservation?, reservable: Reservable?): String
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
    val staticDirectory: String
}

interface MailService {
    fun sendEmail(receivers: List<Email>, bodyHTML: String, subject: String)
    fun sendEmail(receiver: Email, content: ProcessedMailTemplate) = this.sendEmail(kotlin.collections.listOf(receiver), content.body, content.subject)
}


interface MailTemplater {
    fun construct(template: TemplateTypes, key: UniqueReservableKey, action: PostAction?, requiresVerification: Boolean, reservable: Reservable?, reservation: Reservation?, receiver: Email): ProcessedMailTemplate
}

interface BusinessLogic {

    fun process(action: PostAction, verification: Email?, verificationValid: Boolean) : PostResponse

    fun retrieveReservations(urk: UniqueReservableKey, from : DateTime, to : DateTime, callerEmail: Email?) : GetResponse?

    fun retrieveKeys(callerEmail: Email?) : GetTotalResponse
}

