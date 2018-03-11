package krese.data

import kotlin.js.Date
import kotlin.math.roundToLong

actual fun currentTimeMillis(): Long = Date().getTime().roundToLong()