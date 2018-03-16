package krese

import kotlinx.serialization.Serializable
import krese.data.GetResponse
import org.w3c.dom.Element
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


fun GetResponse.toCalendarConfig(acceptedColor: String  ="green" , pendingColor: String = "blue", textColor: String = "orange", totalColor: String = "red", blockAllDay: Boolean = true) : FullCalendarConfig {
    //TODO("not yet implemented")
    val x =  FullCalendarConfig(listOf(CalendarEntry("my test event", Date(Date.now() + 100000000).toISOString(), Date(Date.now() + 200000000).toISOString(), true, acceptedColor )), totalColor, textColor)
    console.log(x)
    return x
}