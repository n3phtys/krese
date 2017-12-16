package krese

import krese.data.Booking
import krese.data.Timespan

data class RawJWT(val text: String)

interface KreseHandable {
    fun applyJWT(jwt: RawJWT): String
    fun getEvents(timespan: Timespan, eventchain: String): List<Booking>
    fun postEvent(booking: Booking, eventchain: String): String
}


class KreseHandler {

}