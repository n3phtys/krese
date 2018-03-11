package krese.data

actual fun assertTrue(value: Boolean) {
    if (!value) {
        throw RuntimeException("assertion broke")
    }
}