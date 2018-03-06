package krese.impl

import com.github.salomonbrys.kodein.Kodein
import krese.BusinessLogic
import krese.data.GetResponse
import krese.UserProfile
import krese.data.FullBooking
import krese.data.PostResponse
import krese.data.UniqueReservableKey

class BusinessLogicImpl(private val kodein: Kodein): BusinessLogic {
    override fun incomingEmailUserAuthentication(reservation: FullBooking, userProfile: UserProfile?): PostResponse {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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

    override fun retrieveReservations(urk: UniqueReservableKey): GetResponse {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}