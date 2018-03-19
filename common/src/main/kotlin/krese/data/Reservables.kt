package krese.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.Optional


@Serializable
data class ReservableElement(
        val id: Long,
        val name: String,
        @Optional val description: String = "",
        @Optional val units: Int = 1,
        @Optional val subElements: List<ReservableElement> = listOf()
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
        @Optional val prologue: String = "", //on jvm either filepath to markdown file (check) or inline markdown, or hardcoded path, translated to HTML without sanitation for frontend
        @Optional val epilogue: String = "",
        @Optional val staticFiles: List<String> = listOf(),
        val elements: ReservableElement,
        @Optional val checkBoxes: List<String> = listOf(),
        val operatorEmails: List<String>
) {
    fun key(): UniqueReservableKey {
        return UniqueReservableKey(uniqueId)
    }
}

//identifying blocks via unique id, maybe via tree

