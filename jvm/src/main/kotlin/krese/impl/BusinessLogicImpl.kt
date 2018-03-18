package krese.impl

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.instance
import krese.*
import krese.data.*
import org.joda.time.DateTime
import java.util.*

class BusinessLogicImpl(private val kodein: Kodein): BusinessLogic {

    private val authVerifier: AuthVerifier = kodein.instance()
    private val appConfig: ApplicationConfiguration = kodein.instance()
    private val mailService: MailService = kodein.instance()
    private val fileSystemWrapper: FileSystemWrapper = kodein.instance()
    private val databaseEncapsulation: DatabaseEncapsulation = kodein.instance()

    init {
        //mailService.sendEmail(listOf(Email(appConfig.mailTestTarget)), "krese works body", "Krese was just started")
    }

    override fun incomingCreateUpdateReservation(reservation: FullBooking, userProfile: UserProfile?): PostResponse {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun incomingAcceptByModerator(bookingId: Long, userProfile: UserProfile, comment: String?): PostResponse {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun incomingDeleteByModerator(bookingId: Long, userProfile: UserProfile, comment: String?): PostResponse {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun incomingWithdrawByUser(bookingId: Long, userProfile: UserProfile, comment: String?): PostResponse {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    //TODO: from and to
    override fun retrieveReservations(urk: UniqueReservableKey, from : DateTime, to : DateTime, callerEmail: Email?): GetResponse? {
        val res = fileSystemWrapper.getReservableToKey(urk)
        if (res != null) {
            return GetResponse(
                    res,
                    databaseEncapsulation.retrieveBookingsForKey(urk).map { it.toOutput(res.operatorEmails.contains(callerEmail?.address)) }
            )
        } else {
            return null
        }
    }

    override fun retrieveKeys(callerEmail: Email?): GetTotalResponse {
        return GetTotalResponse(fileSystemWrapper.getKeysFromDirectory().keys.toList())
    }
}