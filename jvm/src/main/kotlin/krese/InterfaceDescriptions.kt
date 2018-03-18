package krese

import krese.data.*
import krese.impl.*
import org.joda.time.DateTime
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

interface AuthVerifier {
    fun decodeJWT(jwt: String): JWTPayload?
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
    val mailTestTarget: String
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

    fun incomingAcceptByModerator(bookingId: Long, userProfile: UserProfile, comment: String?) : PostResponse

    fun incomingDeleteByModerator(bookingId: Long, userProfile: UserProfile, comment: String?) : PostResponse

    fun incomingWithdrawByUser(bookingId: Long, userProfile: UserProfile, comment: String?) : PostResponse

    fun retrieveReservations(urk: UniqueReservableKey, from : DateTime, to : DateTime, callerEmail: Email?) : GetResponse?

    fun retrieveKeys(callerEmail: Email?) : GetTotalResponse
}

