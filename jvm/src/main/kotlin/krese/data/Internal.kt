package krese.data

import com.google.gson.Gson
import krese.utility.fromJson
import org.joda.time.DateTime
import org.joda.time.DateTimeUtils


enum class LinkActions {
    CreateNewEntry,
    AcceptEntry,
    DeclineEntry,
    DeleteEntry,
}

data class JWTPayload(val action: LinkActions?, val params: List<String>, val userProfile: UserProfile) {
    fun extractAction(): PostAction? = when (action) {
        LinkActions.CreateNewEntry -> if (params.size >= 7 + 2 && params.size % 2 == 1) {
            CreateAction(UniqueReservableKey(params.get(0)), Email(params.get(1)), params.get(2), params.get(3), params.get(4), DateTime.parse(params.get(5)), DateTime.parse(params.get(6)), IntProgression.fromClosedRange(7, params.size, 2).map {
                DbBlockData(Gson().fromJson(params.get(it + 0)), params.get(it + 1).toInt())
            })
        } else {
            null
        }
        LinkActions.AcceptEntry -> if (params.size == 2) {
            AcceptAction(params.get(0).toLong(), params.get(1))
        } else {
            null
        }
        LinkActions.DeclineEntry -> if (params.size == 2) {
            DeclineAction(params.get(0).toLong(), params.get(1))
        } else {
            null
        }
        LinkActions.DeleteEntry -> if (params.size == 2) {
            WithdrawAction(params.get(0).toLong(), params.get(1))
        } else {
            null
        }
        null -> null
    }
}


sealed class PostAction {}

data class CreateAction(val key: UniqueReservableKey, val email: Email, val name: String, val telephone: String, val commentUser: String, val startTime: DateTime, val endTime: DateTime, val blocks: List<DbBlockData>) : PostAction()

data class DeclineAction(val id: Long, val comment: String) : PostAction()

data class WithdrawAction(val id: Long, val comment: String) : PostAction()

data class AcceptAction(val id: Long, val comment: String) : PostAction()

data class PostActionInput(val jwt: JWTPayload?, val action: PostAction)


fun buildUserProfile(email: Email, validFrom: DateTime, validTo: DateTime): UserProfile {
    return UserProfile(email, if (validFrom.millisOfSecond == 0) {
        validFrom.millis
    } else {
        validFrom.withMillisOfSecond(0).plusSeconds(1).millis
    }, validTo.withMillisOfSecond(0).millis)
}

data class UserProfile(val email: Email, val validFrom: Long, val validTo: Long) {
    init {
        assert(validFrom % 1000L == 0L)
        assert(validTo % 1000L == 0L)
    }

    fun isValid() = DateTimeUtils.currentTimeMillis() >= validFrom && DateTimeUtils.currentTimeMillis() <= validTo
}
