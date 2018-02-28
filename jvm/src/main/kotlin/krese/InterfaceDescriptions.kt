package krese

import krese.data.Booking
import krese.data.Email
import krese.data.UniqueReservableKey


interface FileSystemWrapper {
    fun getKeysFromDirectory(directory: String) : List<String>
    fun getReservableToKey(directory:String, key: String) : Reservable?
} //also includes static files in subdirectory

interface DatabaseWrapper {
    fun getAllocatedTimeslotsToKey(key: String, includeMinTimestamp: Long, excludeMaxTimestamp: Long, start: Long, end: Long) : List<Booking>
}

interface JWTActionReceiver {
    fun receiveJWT(jwt: String)
}


data class UserProfile(val email: Email, val validFrom: Long, val validTo: Long) {
    fun isValid() = System.currentTimeMillis() >= validFrom && System.currentTimeMillis() <= validTo
}

interface AuthVerifier {
    fun decode(jwt: String): UserProfile?
}

interface EmailSender {
    fun sendEmail(receiver: String, body: String, text: String)
}

interface EmailTemplater {

}

interface GetExtractor {
    fun accept(key: UniqueReservableKey): GetResponse?
}

interface FormulaReceiver {
    fun accept(reservation: Reservation): Boolean
}