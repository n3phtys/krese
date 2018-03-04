package krese.data


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
        val elementsTree: List<BlockedElement>
)

data class GetResponse(
        val reservable: Reservable,
        val existingReservations: List<Reservation>
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

