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
            is CreateAction -> businessLogic.incomingCreateUpdateReservation(reservation = FullBooking(booking = Booking(timestamp_created = ts, timestamp_edited = ts, publicUser = PublicUser(reservation.action.name), timespan = Timespan(reservation.action.startTime.millis, reservation.action.endTime.millis), selectedResources = reservation.action.blocks, comment = reservation.action.commentUser, state = BookingState.Pending), fullUser = FullUser(PublicUser(reservation.action.name), reservation.action.email, reservation.action.telephone)), userProfile = reservation.jwt?.userProfile)
            is DeclineAction -> businessLogic.incomingDeleteByModerator(reservation.action.id, reservation.jwt?.userProfile!!, reservation.action.comment)
            is WithdrawAction -> businessLogic.incomingWithdrawByUser(reservation.action.id, reservation.jwt?.userProfile!!, reservation.action.comment)
            is AcceptAction -> businessLogic.incomingAcceptByModerator(reservation.action.id, reservation.jwt?.userProfile!!, reservation.action.comment)
        }

    }
}