package krese.impl

import krese.ApplicationConfiguration
import krese.DatabaseConfiguration

enum class EnvKey {
    KRESE_MAIL_USERNAME,
    KRESE_MAIL_PASSWORD,
    KRESE_MAIL_FROM,
    KRESE_MAIL_HOST,
    KRESE_MAIL_PORT,
    KRESE_MAIL_STARTTLS,
    KRESE_MAIL_USE_AUTH,
    KRESE_HASH_SECRET,
}

class ConfigurationImpl : DatabaseConfiguration, ApplicationConfiguration {



    val defaultValues: Map<EnvKey, String> = mapOf(
            EnvKey.KRESE_MAIL_STARTTLS to "true",
            EnvKey.KRESE_MAIL_USERNAME to "username"
    )

    fun getVal(key : EnvKey) : String {
        val prop = System.getenv(key.toString())
        if (prop == null || prop.isBlank()) {
            return defaultValues.get(key)!!
        } else {
            return prop
        }
    }


    override val mailUsername: String
        get() = getVal(EnvKey.KRESE_MAIL_USERNAME)
    override val mailPassword: String
        get() = getVal(EnvKey.KRESE_MAIL_PASSWORD)
    override val mailFrom: String
        get() = getVal(EnvKey.KRESE_MAIL_FROM)
    override val mailHost: String
        get() = getVal(EnvKey.KRESE_MAIL_HOST)
    override val mailPort: Int
        get() = getVal(EnvKey.KRESE_MAIL_PORT).toInt()
    override val mailStarttls: Boolean
        get() = getVal(EnvKey.KRESE_MAIL_STARTTLS).toBoolean()
    override val mailAuth: Boolean
        get() = getVal(EnvKey.KRESE_MAIL_USE_AUTH).toBoolean()
    override val hashSecret: String
        get() = getVal(EnvKey.KRESE_HASH_SECRET)
    override val databaseDriver: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val databaseJDBC: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val databasePort: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val databaseHost: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val databaseName: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val databaseUsername: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val databasePassword: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val reservablesDirectory: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val applicationHost: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val applicationPort: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

}