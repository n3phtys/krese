package krese.data

import org.apache.commons.validator.EmailValidator

actual fun isValidEmail(address: String): Boolean {
    return EmailValidator.getInstance().isValid(address)
}

val keySeparator = '_'

actual fun isValidKey(str: String): Boolean {
        if(str.length == 0) {
            return false;
        } else {
            if (str[0] == keySeparator || str[str.length - 1] == keySeparator) {
                return false;
            } else {
                val r = Regex("[a-z0-9|"+ keySeparator +"]+")
                return r.matches(str) && !str.contains(keySeparator.toString() + keySeparator)
            }
        }
}