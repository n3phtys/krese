package krese.impl

import krese.BusinessLogic
import krese.FullBooking
import krese.GetResponse
import krese.UserProfile
import krese.data.PostResponse
import krese.data.UniqueReservableKey

class BusinessLogicImpl: BusinessLogic {
    override fun incomingCreateUpdateReservation(reservation: FullBooking, userProfile: UserProfile?): PostResponse {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun incomingEmailUserAuthentication(reservation: FullBooking, userProfile: UserProfile?): PostResponse {
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

    override fun incomingLoginAttempt(email: String): PostResponse {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun retrieveReservations(urk: UniqueReservableKey): GetResponse {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}