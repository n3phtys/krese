package krese.data

actual fun extractJWTPayloadAction(payload: JWTPayload): PostAction? = payload.action