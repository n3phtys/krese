package krese.data

import org.joda.time.DateTime


fun buildUserProfile(email: Email, validFrom: DateTime, validTo: DateTime): UserProfile {
    return UserProfile(email, if (validFrom.millisOfSecond == 0) {
        validFrom.millis
    } else {
        validFrom.withMillisOfSecond(0).plusSeconds(1).millis
    }, validTo.withMillisOfSecond(0).millis)
}
