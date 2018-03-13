package krese.data

import kotlinx.serialization.Serializable


@Serializable
data class ReservableElement(
        val id: Long,
        val name: String,
        val description: String?,
        val units: Int?,
        val subElements: List<ReservableElement>
)

@Serializable
data class Reservation(
        val id : Long, val key: UniqueReservableKey, val email: Email?, val name: String, val telephone: String?, val commentUser: String, val commentOperator: String?, val startTime: Long, val endTime: Long, val createdTimestamp: Long, val modifiedTimestamp: Long?, val accepted: Boolean, val blocks: List<DbBlockData>
)

@Serializable
data class GetResponse(
        val reservable: Reservable,
        val existingReservations: List<Reservation>
)

@Serializable
data class GetTotalResponse(
        val keys : List<UniqueReservableKey>
)

@Serializable
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

