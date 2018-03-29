package krese

import kotlinx.serialization.Serializable
import krese.data.GetResponse
import krese.data.Reservation
import kotlin.js.Date


@Serializable
data class CalendarEntry(
        val title: String,
        val start: String, //iso string
        val end: String, //iso string
        val allDay: Boolean,
        val color: String //some color string
)

@Serializable
data class FullCalendarConfig(
        val events: List<CalendarEntry>,
        val color: String,
        val textColor: String
)

fun Reservation.toCalendarEntry(acceptedColor: String , pendingColor: String, blockAllDay: Boolean ) : CalendarEntry {
    return CalendarEntry(this.name.deescape(), Date(this.startTime).toISOString(), Date(this.endTime).toISOString(), blockAllDay, if (this.accepted) acceptedColor else pendingColor)
}

fun GetResponse.toCalendarConfig(acceptedColor: String  ="green" , pendingColor: String = "blue", textColor: String = "orange", totalColor: String = "red", blockAllDay: Boolean = true) : FullCalendarConfig {
    val x =  FullCalendarConfig(this.existingReservations.map{it.toCalendarEntry(acceptedColor, pendingColor, blockAllDay)}, totalColor, textColor)
    console.log(x)
    return x
}


enum class FormFieldNames {
    Name,
    Email,
    Telephone,
    From,
    To,
    Comment
}