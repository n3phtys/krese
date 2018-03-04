package krese

import krese.data.*
import krese.impl.DbBooking
import krese.impl.DbBookingInputData
import org.joda.time.DateTimeUtils.currentTimeMillis
import java.nio.file.Path


interface FileSystemWrapper {
    fun getKeysFromDirectory() : Map<UniqueReservableKey, Path>
    fun getReservableToKey(key: UniqueReservableKey) : Reservable?
} //also includes static files in subdirectory

interface DatabaseEncapsulation {

    fun createUpdateBooking(id: Long?, data: DbBookingInputData): DbBooking?

    fun deleteBooking(id: Long) : Boolean

    fun acceptBooking(id: Long) : Boolean

    fun retrieveBookingsForKey(key: UniqueReservableKey, includeMinTimestamp: Long = Long.MIN_VALUE, excludeMaxTimestamp: Long = Long.MAX_VALUE, start: Long = 0, end: Long = Long.MAX_VALUE) : List<DbBooking>

}

interface JWTReceiver {
    fun receiveJWT(jwt: String) : PostResponse

    fun loginStillValid(jwt: String) : Boolean

    fun relogin(email: String) : Boolean
}


data class UserProfile(val email: Email, val validFrom: Long, val validTo: Long, val randomNumber: Long) {
    fun isValid() = currentTimeMillis() >= validFrom && currentTimeMillis() <= validTo
}

interface AuthVerifier {
    fun decodeJWT(jwt: String): UserProfile?
    fun createNewPrivateKey() : Unit
    fun encodeJWT(content: Any): String
    fun encodeBase64(plaintext: String): String
    fun decodeBase64(base64: String): String
}

interface GetReceiver {
    fun accept(key: UniqueReservableKey): GetResponse?
}


interface PostReceiver {
    fun accept(reservation: Reservation): Boolean
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
    val applicationHost: String
    val applicationPort: String
}

interface MailService {
    fun sendEmail(receiver: String, body: String, text: String)
}

interface MailTemplater {
    fun buildLoginMail()
}

interface BusinessLogic {

    fun incomingCreateUpdateReservation(reservation: FullBooking, userProfile: UserProfile?) : PostResponse

    fun incomingEmailUserAuthentication(reservation: FullBooking, userProfile: UserProfile?) : PostResponse

    fun incomingAcceptByModerator(bookingId: Long, userProfile: UserProfile, comment: String?) : PostResponse

    fun incomingDeleteByModerator(bookingId: Long, userProfile: UserProfile, comment: String?) : PostResponse

    fun incomingWithdrawByUser(bookingId: Long, userProfile: UserProfile, comment: String?) : PostResponse

    fun incomingLoginAttempt(email: String) : PostResponse

    fun retrieveReservations(urk: UniqueReservableKey) : GetResponse
}

