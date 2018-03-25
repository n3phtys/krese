package krese


external val kreseTranslationObject: dynamic

val parsedTranslation = {
    if (kreseTranslationObject != null) {
        kreseTranslationObject
    } else {
        null
    }
}

fun localize(key : String) : String {
    if (kreseTranslationObject != null) {
        val v = kreseTranslationObject[key]
        if (v != null) {
            return v
        } else {
            return key
        }
    } else {
        return key
    }
}

fun String.localize() : String = localize(this)