package krese.data


import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JSON


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


@Serializable
data class UniqueReservableKey(val id: String) {
    /*init {
        if (!isValidKey(id)) {
            throw Exception("Key is not valid")
        }
    }*/
}

expect fun isValidKey(str: String) : Boolean



@Serializable
data class Email(val address: String) {
    /*init {
        if (!isValidEmail(address)) {
            throw Exception("Email is not valid")
        }
    }*/
}

@Serializable
data class FullUser(val publicinfo : PublicUser, val email: Email, val telephone: String)

@Serializable
data class PublicUser(val nickname: String)

@Serializable
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

@Serializable
data class Booking(val timestamp_created: Long, val timestamp_edited: Long, val publicUser: PublicUser, val timespan: Timespan, val selectedResources: List<DbBlockData>, val comment: String, val state: BookingState)

@Serializable
data class DbBlockData(val elementPath: List<Int>, val usedNumber: Int) {
    fun namedBlock(reservable: Reservable?): String {
        if (reservable != null) {
            var current = reservable.elements
            this.elementPath.drop(1).forEach { id ->
                val ne = current.subElements.find { it.id == id }
                if (ne != null) {
                    current = ne
                }
            }
            return "* ${current.name}${if (current.units > 1) " × ${this.usedNumber}" else ""}"
        } else {
            return ""
        }
    }
}

@Serializable
data class FullBooking(val booking: Booking, val fullUser: FullUser)


expect fun isValidEmail(address: String) : Boolean



@Serializable
data class PostRequest(
        val jwt: String,
        val delete: Boolean,
        val accept: Boolean,
        val payload: FullBooking
)

@Serializable
data class PostResponse(
        val successful: Boolean,
        val finished: Boolean,
        val message: String
)



enum class LinkActions {
    CreateNewEntry,
    AcceptEntry,
    DeclineEntry,
    DeleteEntry,
}


interface PostAction {
}


fun toJson(action: PostAction?): Pair<String, String> {
    return when (action) {
        is CreateAction -> Pair("CreateAction", JSON.Companion.stringify(action))
        is AcceptAction -> Pair("AcceptAction", JSON.Companion.stringify(action))
        is DeclineAction -> Pair("DeclineAction", JSON.Companion.stringify(action))
        is WithdrawAction -> Pair("WithdrawAction", JSON.Companion.stringify(action))
        else -> {
            Pair("null", "{}")
        }
    }
}

fun String?.or(s: String): String = if (this != null) this else s

fun buildFromJson(tag: String?, json: String?): PostAction? {
    if (json == null) {
        return null
    }
    return when (tag) {
        "CreateAction" -> JSON.parse<CreateAction>(json)
        "AcceptAction" -> JSON.parse<AcceptAction>(json)
        "DeclineAction" -> JSON.parse<DeclineAction>(json)
        "WithdrawAction" -> JSON.parse<WithdrawAction>(json)
        else -> null
    }
}


@Serializable
data class CreateAction(val key: UniqueReservableKey, val email: Email, val name: String, val telephone: String, val commentUser: String, val startTime: Long, val endTime: Long, val blocks: List<DbBlockData>) : PostAction {
    fun isValid(): Boolean = true
}

@Serializable
data class DeclineAction(val id: Long, val comment: String) : PostAction

@Serializable
data class WithdrawAction(val id: Long, val comment: String) : PostAction

@Serializable
data class AcceptAction(val id: Long, val comment: String) : PostAction

@Serializable
sealed class PostActionInput() {
    companion object {
        fun build(jwt: String?, action: PostAction) : PostActionInput {
            return when(action) {

                is CreateAction -> CreateActionInput(jwt, action)
                is DeclineAction -> DeclineActionInput(jwt, action)
                is WithdrawAction -> WithdrawActionInput(jwt, action)
                is AcceptAction -> AcceptActionInput(jwt, action)
                else -> {throw IllegalArgumentException()}
            }
        }
    }

    abstract fun toJwt(): String?
    abstract fun toAction(): PostAction
}

@Serializable
data class CreateActionInput(val jwt: String?, val action: CreateAction) : PostActionInput() {
    override fun toJwt(): String? = jwt

    override fun toAction(): PostAction = action
}

@Serializable
data class DeclineActionInput(val jwt: String?, val action: DeclineAction) : PostActionInput() {
    override fun toJwt(): String? = jwt

    override fun toAction(): PostAction = action
}

@Serializable
data class AcceptActionInput(val jwt: String?, val action: AcceptAction) : PostActionInput() {
    override fun toJwt(): String? = jwt

    override fun toAction(): PostAction = action
}

@Serializable
data class WithdrawActionInput(val jwt: String?, val action: WithdrawAction) : PostActionInput() {
    override fun toJwt(): String? = jwt

    override fun toAction(): PostAction = action
}


@Serializable
data class JWTPayload(val action: PostAction?, val params: List<String>, val userProfile: UserProfile) {
}

expect fun extractJWTPayloadAction(payload: JWTPayload) : PostAction?

expect fun assertTrue(value: Boolean): Unit

expect fun currentTimeMillis(): Long


@Serializable
data class UserProfile(val email: Email, val validFrom: Long, val validTo: Long) {
    init {
        assertTrue(validFrom % 1000L == 0L)
        assertTrue(validTo % 1000L == 0L)
    }

    fun isValid() = currentTimeMillis() >= validFrom && currentTimeMillis() <= validTo

}

