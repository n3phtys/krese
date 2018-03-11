package krese.impl

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.instance
import krese.*
import krese.data.*
import org.joda.time.DateTime


class PostReceiverImpl(private val kodein: Kodein) : PostReceiver {
    private val authVerifier: AuthVerifier = kodein.instance()
    private val businessLogic: BusinessLogic = kodein.instance()

    override fun submitForm(reservation: PostActionInput): PostResponse {
        val ts = DateTime().millis

        return when (reservation.action) {
            is CreateAction -> businessLogic.incomingCreateUpdateReservation(reservation = FullBooking(booking = Booking(timestamp_created = ts, timestamp_edited = ts, publicUser = PublicUser((reservation.action as CreateAction).name), timespan = Timespan((reservation.action as CreateAction).startTime, (reservation.action as CreateAction).endTime), selectedResources = (reservation.action as CreateAction).blocks, comment = (reservation.action as CreateAction).commentUser, state = BookingState.Pending), fullUser = FullUser(PublicUser((reservation.action as CreateAction).name), (reservation.action as CreateAction).email, (reservation.action as CreateAction).telephone)), userProfile = reservation.jwt?.userProfile)
            is DeclineAction -> businessLogic.incomingDeleteByModerator((reservation.action as DeclineAction).id, reservation.jwt?.userProfile!!, (reservation.action as DeclineAction).comment)
            is WithdrawAction -> businessLogic.incomingWithdrawByUser((reservation.action as WithdrawAction).id, reservation.jwt?.userProfile!!, (reservation.action as WithdrawAction).comment)
            is AcceptAction -> businessLogic.incomingAcceptByModerator((reservation.action as AcceptAction).id, reservation.jwt?.userProfile!!, (reservation.action as AcceptAction).comment)
        }

    }
}