package krese.data

import org.apache.commons.validator.EmailValidator

actual fun isValidEmail(address: String): Boolean {
    return EmailValidator.getInstance().isValid(address)
}