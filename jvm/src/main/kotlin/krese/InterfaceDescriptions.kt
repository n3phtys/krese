package krese

import krese.data.*
import krese.impl.*
import org.jetbrains.exposed.dao.EntityID
import org.joda.time.DateTime
import org.joda.time.DateTimeUtils.currentTimeMillis
import java.nio.file.Path


interface FileSystemWrapper {
    fun getKeysFromDirectory() : Map<UniqueReservableKey, Path>
    fun getReservableToKey(key: UniqueReservableKey) : Reservable?
} //also includes static files in subdirectory

interface DatabaseEncapsulation {

    fun createUpdateBooking(id: Long?, data: DbBookingInputData): DbBookingOutputData?

    fun deleteBooking(id: Long) : Boolean

    fun acceptBooking(id: Long) : Boolean

    fun retrieveBookingsForKey(key: UniqueReservableKey, includeMinTimestamp: DateTime = DateTime().withMillis(Long.MIN_VALUE), excludeMaxTimestamp: DateTime = DateTime().withMillis(Long.MAX_VALUE)) : List<DbBookingOutputData>

}

interface JWTReceiver {
    fun receiveJWTAction(jwtAction: String) : PostResponse

    fun loginStillValid(jwt: String) : Boolean

    fun relogin(email: String)
}

enum class LinkActions {
    CreateNewEntry,
    AcceptEntry,
    DeclineEntry,
    DeleteEntry,
}

data class JWTPayload(val action: LinkActions?, val params: List<String>, val userProfile: UserProfile)



sealed class PostAction {}

data class CreateAction(val key: UniqueReservableKey, val email: Email, val name: String, val telephone: String, val commentUser: String, val startTime: DateTime, val endTime: DateTime, val blocks: List<DbBlockData>) : PostAction()

data class DeclineAction(val id: Long, val comment: String) : PostAction()

data class WithdrawAction(val id: Long, val comment: String) : PostAction()

data class AcceptAction(val id: Long, val comment: String) : PostAction()

data class PostActionInput(val jwt: JWTPayload?, val action: PostAction)


fun buildUserProfile(email: Email, validFrom: DateTime, validTo: DateTime) : UserProfile {
    return UserProfile(email, if (validFrom.millisOfSecond == 0) {validFrom.millis} else {validFrom.withMillisOfSecond(0).plusSeconds(1).millis},  validTo.withMillisOfSecond(0).millis)
}

data class UserProfile(val email: Email, val validFrom: Long, val validTo: Long) {
    init {
        assert(validFrom % 1000L == 0L)
        assert(validTo % 1000L == 0L)
    }

    fun isValid() = currentTimeMillis() >= validFrom && currentTimeMillis() <= validTo
}

interface AuthVerifier {
    fun decodeJWT(jwt: String): JWTPayload?
    fun encodeJWT(content: JWTPayload): String?
    fun encodeBase64(plaintext: String): String
    fun decodeBase64(base64: String): String
}

interface GetReceiver {
    fun retrieve(key: UniqueReservableKey): GetResponse?
}


interface PostReceiver {
    fun submitForm(reservation: PostActionInput): PostResponse
}

interface DatabaseConfiguration {
    val databasePort: String
    val databaseHost: String
    val databaseName: String
    val databaseUsername: String
    val databasePassword: String
    val databaseDriver: String
    val databaseJDBC: String
}

interface ApplicationConfiguration {
    val reservablesDirectory: String
    val webDirectory: String
    val applicationHost: String
    val applicationProtocol: String
    val applicationPort: Int
    val hashSecret : String
    val mailUsername: String
    val mailPassword : String
    val mailFrom: String
    val mailHost: String
    val mailPort: Int
    val mailStarttls: Boolean
    val mailAuth: Boolean
}

interface MailService {
    fun sendEmail(receivers: List<Email>, bodyHTML: String, subject: String)
}

interface MailTemplater {
}

interface BusinessLogic {

    fun incomingCreateUpdateReservation(reservation: FullBooking, userProfile: UserProfile?) : PostResponse

    fun incomingEmailUserAuthentication(reservation: FullBooking, userProfile: UserProfile?) : PostResponse

    fun incomingAcceptByModerator(bookingId: Long, userProfile: UserProfile, comment: String?) : PostResponse

    fun incomingDeleteByModerator(bookingId: Long, userProfile: UserProfile, comment: String?) : PostResponse

    fun incomingWithdrawByUser(bookingId: Long, userProfile: UserProfile, comment: String?) : PostResponse

    fun retrieveReservations(urk: UniqueReservableKey) : GetResponse
}

