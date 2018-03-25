package krese.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.Optional


@Serializable
data class ReservableElement(
        val id: Int,
        val name: String,
        @Optional val description: String = "",
        @Optional val units: Int = 1, //0 means only children
        @Optional val subElements: List<ReservableElement> = listOf()
) {

    init {
        assertTrue(units >= 1 || (units == 0 && subElements.isNotEmpty()))
    }

    fun allows(block: DbBlockData) : Boolean {
        val head = block.elementPath.first()
        if (this.id == head) {
            val tail = DbBlockData(block.elementPath.drop(1), block.usedNumber)
            if (tail.elementPath.isEmpty()) {
                return units >= tail.usedNumber && tail.usedNumber >= 0
            } else {
                return this.subElements.any { it.allows(tail) }
            }
        } else {
            return false
        }
    }

    fun allows(blocks: List<DbBlockData>) : Boolean {
        val map : Map<List<Int>, Int> = blocks.groupBy { it.elementPath }.mapValues { it.value.map { it.usedNumber }.sum() }
        return map.map { DbBlockData(it.key, it.value) }.all { this.allows(it) }
    }
}

@Serializable
data class Reservation(
        val id : Long, val key: UniqueReservableKey, val email: Email?, val name: String, val telephone: String?, val commentUser: String, val commentOperator: String?, val startTime: Long, val endTime: Long, val createdTimestamp: Long, val modifiedTimestamp: Long?, val accepted: Boolean, val blocks: List<DbBlockData>
) {
    fun toBlockTableCellString() : String {
        return blocks.map { "${it.usedNumber} * ${it.elementPath.toString()}" }.joinToString()
    }
}

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
        @Optional val title: String = "PLACEHOLDER TITLE FOR THIS",
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

