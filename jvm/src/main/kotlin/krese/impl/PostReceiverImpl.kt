package krese.impl

import com.github.salomonbrys.kodein.Kodein
import krese.PostReceiver
import krese.data.Reservation

class PostReceiverImpl(private val kodein: Kodein) : PostReceiver {
    override fun accept(reservation: Reservation): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}