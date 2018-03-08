package krese.data

import org.joda.time.DateTime


data class ReservableElement(
        val id: Long,
        val name: String,
        val description: String?,
        val units: Int?,
        val subElements: List<ReservableElement>
)

data class BlockedElement (
        val elementPathSegments: List<Long>
)

data class Reservation(
        val id : Long, val key: UniqueReservableKey, val email: Email?, val name: String, val telephone: String?, val commentUser: String, val commentOperator: String?, val startTime: DateTime, val endTime: DateTime, val createdTimestamp: DateTime, val modifiedTimestamp: DateTime?, val accepted: Boolean, val blocks: List<DbBlockData>
)

data class GetResponse(
        val reservable: Reservable,
        val existingReservations: List<Reservation>
)

data class GetTotalResponse(
        val keys : List<UniqueReservableKey>
)

data class Reservable(
        val uniqueId : String,
        val prologueMarkdown: String,
        val epilogueMarkdown: String,
        val staticFiles: List<String>,
        val elements: ReservableElement,
        val operatorEmails: List<String>
) {
    fun key(): UniqueReservableKey {
        return UniqueReservableKey(uniqueId)
    }
}

//identifying blocks via unique id, maybe via tree

