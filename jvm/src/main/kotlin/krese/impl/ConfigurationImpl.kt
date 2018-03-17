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
    KRESE_MAIL_TEST_RECEIVER,
    KRESE_HASH_SECRET,

    KRESE_DATABASE_DRIVER,

    KRESE_DATABASE_JDBC,

    KRESE_DATABASE_NAME,

    KRESE_DATABASE_HOST,

    KRESE_DATABASE_PORT,

    KRESE_DATABASE_USERNAME,

    KRESE_DATABASE_PASSWORD,

    KRESE_RESERVABLES_DIRECTORY,

    KRESE_APPLICATION_HOST,

    KRESE_APPLICATION_PORT,

    KRESE_WEB_DIRECTORY,

    KRESE_APPLICATION_PROTOCOL
}

class ConfigurationImpl : DatabaseConfiguration, ApplicationConfiguration {

    fun defaultValue(key: EnvKey): String = when (key) {
        EnvKey.KRESE_MAIL_USERNAME -> "emailusername"
        EnvKey.KRESE_MAIL_PASSWORD -> "emailpassword"
        EnvKey.KRESE_MAIL_FROM -> "sender@email.com"
        EnvKey.KRESE_MAIL_HOST -> "email.com"
        EnvKey.KRESE_MAIL_PORT -> "1234"
        EnvKey.KRESE_MAIL_STARTTLS -> "false"
        EnvKey.KRESE_MAIL_USE_AUTH -> "true"
        EnvKey.KRESE_HASH_SECRET -> "myhashsecret1234567890"
        EnvKey.KRESE_DATABASE_DRIVER -> "org.h2.Driver"
        EnvKey.KRESE_DATABASE_JDBC -> "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"
        EnvKey.KRESE_DATABASE_NAME -> "test"
        EnvKey.KRESE_DATABASE_HOST -> "localhost"
        EnvKey.KRESE_DATABASE_PORT -> "5432"
        EnvKey.KRESE_DATABASE_USERNAME -> "admin"
        EnvKey.KRESE_DATABASE_PASSWORD -> "secret"
        EnvKey.KRESE_RESERVABLES_DIRECTORY -> "./conf"
        EnvKey.KRESE_APPLICATION_HOST -> "localhost"
        EnvKey.KRESE_APPLICATION_PORT -> "8080"
        EnvKey.KRESE_WEB_DIRECTORY -> "../web"
        EnvKey.KRESE_APPLICATION_PROTOCOL -> "http"
        EnvKey.KRESE_MAIL_TEST_RECEIVER -> "receiver@email.com"
    }


    fun getVal(key: EnvKey): String {
        val prop = System.getenv(key.toString())
        if (prop == null || prop.isBlank()) {
            return defaultValue(key)
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
        get() = getVal(EnvKey.KRESE_DATABASE_DRIVER)
    override val databaseJDBC: String
        get() = getVal(EnvKey.KRESE_DATABASE_JDBC)
    override val databasePort: String
        get() = getVal(EnvKey.KRESE_DATABASE_PORT)
    override val databaseHost: String
        get() = getVal(EnvKey.KRESE_DATABASE_HOST)
    override val databaseName: String
        get() = getVal(EnvKey.KRESE_DATABASE_NAME)
    override val databaseUsername: String
        get() = getVal(EnvKey.KRESE_DATABASE_USERNAME)
    override val databasePassword: String
        get() = getVal(EnvKey.KRESE_DATABASE_PASSWORD)
    override val reservablesDirectory: String
        get() = getVal(EnvKey.KRESE_RESERVABLES_DIRECTORY)
    override val applicationHost: String
        get() = getVal(EnvKey.KRESE_APPLICATION_HOST)
    override val applicationProtocol: String
        get() = getVal(EnvKey.KRESE_APPLICATION_PROTOCOL)
    override val applicationPort: Int
        get() = getVal(EnvKey.KRESE_APPLICATION_PORT).toInt()
    override val webDirectory: String
        get() = getVal(EnvKey.KRESE_WEB_DIRECTORY)
    override val mailTestTarget: String
        get() = getVal(EnvKey.KRESE_MAIL_TEST_RECEIVER)

}