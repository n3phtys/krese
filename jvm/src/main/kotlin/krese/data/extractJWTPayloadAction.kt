package krese.data

import com.google.gson.Gson
import krese.utility.fromJson
import org.joda.time.DateTime

actual fun extractJWTPayloadAction(payload: JWTPayload): PostAction? = when (payload.action) {
    LinkActions.CreateNewEntry -> if (payload.params.size >= 7 + 2 && payload.params.size % 2 == 1) {
        CreateAction(UniqueReservableKey(payload.params.get(0)), Email(payload.params.get(1)), payload.params.get(2), payload.params.get(3), payload.params.get(4), DateTime.parse(payload.params.get(5)).millis, DateTime.parse(payload.params.get(6)).millis, IntProgression.fromClosedRange(7, payload.params.size, 2).map {
            DbBlockData(Gson().fromJson(payload.params.get(it + 0)), payload.params.get(it + 1).toInt())
        })
    } else {
        null
    }
    LinkActions.AcceptEntry -> if (payload.params.size == 2) {
        AcceptAction(payload.params.get(0).toLong(), payload.params.get(1))
    } else {
        null
    }
    LinkActions.DeclineEntry -> if (payload.params.size == 2) {
        DeclineAction(payload.params.get(0).toLong(), payload.params.get(1))
    } else {
        null
    }
    LinkActions.DeleteEntry -> if (payload.params.size == 2) {
        WithdrawAction(payload.params.get(0).toLong(), payload.params.get(1))
    } else {
        null
    }
    null -> null
}