package krese.impl

import krese.ApplicationConfiguration
import krese.DatabaseConfiguration

class ConfigurationImpl : DatabaseConfiguration, ApplicationConfiguration {
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