package krese.data

import org.apache.commons.validator.EmailValidator

actual fun isValidEmail(address: String): Boolean {
    return EmailValidator.getInstance().isValid(address)
}

actual fun isValidKey(str: String): Boolean {
        if(str.length == 0) {
            return false;
        } else {
            if (str[0] == '/' || str[str.length - 1] == '/') {
                return false;
            } else {
                val r = Regex("[a-z0-9|/]+")
                return r.matches(str)
            }
        }
}