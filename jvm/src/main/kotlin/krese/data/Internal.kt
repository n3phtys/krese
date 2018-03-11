package krese.data

import com.google.gson.Gson
import krese.utility.fromJson
import org.joda.time.DateTime
import org.joda.time.DateTimeUtils






fun buildUserProfile(email: Email, validFrom: DateTime, validTo: DateTime): UserProfile {
    return UserProfile(email, if (validFrom.millisOfSecond == 0) {
        validFrom.millis
    } else {
        validFrom.withMillisOfSecond(0).plusSeconds(1).millis
    }, validTo.withMillisOfSecond(0).millis)
}
