package krese.data


data class Block(val id: Long, val label: String)

data class BlockSet(val availableBlocks: List<Block>)

data class SpecificBlockSetSelection(val selected : HashSet<Int>)

data class Email(val address: String) {
    init {
        if (!isValidEmail(address)) {
            throw Exception("Email is not valid")
        }
    }
}
data class FullUser(val id: Long, val publicinfo : PublicUser, val email: Email, val telephone: String)
data class PublicUser(val nickname: String)
data class Timespan(val from: Long, val to: Long) {
    init {
        if (from > to) {
            throw Exception("Timespan is illegal (endtime before starttime)")
        }
    }
}

enum class BookingState {
    //EmailUnconfirmed,
    Pending,
    Confirmed,
    Declined,
}

data class Booking(val timestamp_created: Long, val timestamp_edited: Long, val publicUser: PublicUser, val timespan: Timespan, val comment: String, val state: BookingState)

data class FullBooking(val booking: Booking, val fullUser: FullUser)


expect fun isValidEmail(address: String) : Boolean