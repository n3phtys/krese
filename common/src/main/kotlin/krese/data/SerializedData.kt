package krese.data

/*
Gästezimmer KA (2 Betten)
Gästezimmer Berlin laut Mail (multi hierarchy)
Skihütte mit 10 Betten
Saal KA (nur eine Einheit)
 */

/*
CRUD Operationen
erfordern exakte ID jedes Reservable

GUI erlaubt es zu wechseln über links
jeder link ist einzigartig

Liste auch noch zu jedem Reservable verfügbare Dateien und Texte auf
Baue Webseiten on-the-fly aus den verfügbaren Dateien
 */


data class UniqueReservableKey(val id: String) {
    /*init {
        if (!isValidKey(id)) {
            throw Exception("Key is not valid")
        }
    }*/
}

expect fun isValidKey(str: String) : Boolean


data class Block(val id: Long, val label: String, val secondLayerUnitsCount: Int)

data class BlockSet(val availableBlocks: List<Block>)

data class SpecificBlockSetSelection(val selected : Set<Long>)

data class Email(val address: String) {
    /*init {
        if (!isValidEmail(address)) {
            throw Exception("Email is not valid")
        }
    }*/
}
data class FullUser(val id: Long, val publicinfo : PublicUser, val email: Email, val telephone: String)
data class PublicUser(val nickname: String)
data class Timespan(val from: Long, val to: Long) {
    /*init {
        if (from > to) {
            throw Exception("Timespan is illegal (endtime before starttime)")
        }
    }*/

    fun intersects(other: Timespan): Boolean {
        return !(from > other.to || to < other.from)
    }

    fun intersectsOrTangents(other: Timespan): Boolean {
        return (!(from > other.to || to < other.from)) || isNeighbor(other)
    }

    fun isNeighbor(other: Timespan): Boolean {
        return this.immediatelyAfter(other) || this.immediatelyBefore(other)
    }

    fun immediatelyBefore(other: Timespan): Boolean {
        return this.to == other.from
    }

    fun immediatelyAfter(other: Timespan): Boolean {
        return this.from == other.to
    }
}

enum class BookingState {
    //EmailUnconfirmed,
    Pending,
    Confirmed,
    Declined,
}

data class Booking(val timestamp_created: Long, val timestamp_edited: Long, val publicUser: PublicUser, val timespan: Timespan, val selectedResources: SpecificBlockSetSelection, val comment: String, val state: BookingState)

data class FullBooking(val booking: Booking, val fullUser: FullUser)


expect fun isValidEmail(address: String) : Boolean