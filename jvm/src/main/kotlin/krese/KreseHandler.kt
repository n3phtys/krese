package krese

import krese.data.Booking
import krese.data.Timespan


interface KreseHandable {
    fun applyJWT(jwt: RawJWT): String
    fun getEvents(timespan: Timespan, eventchain: String): List<Booking>
    fun postEvent(booking: Booking, eventchain: String): String
}


class KreseHandler {

}