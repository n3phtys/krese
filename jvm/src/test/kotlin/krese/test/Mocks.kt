package krese.test

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.singleton
import krese.*
import krese.data.*
import krese.impl.*
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicInteger


fun List<String>.emailReceivers() = this.drop(1).take(this.size - 2)
fun List<String>.emailSubject() = this.first()
fun List<String>.emailBody() = this.last()

class MailerMock(val sentMails: MutableList<List<String>> = mutableListOf()) : MailService {
    override fun sendEmail(receivers: List<Email>, bodyHTML: String, subject: String) {
        assert(receivers.size >= 1)
        println("MOCK-SENDING MAIL WITH SUBJECT = $subject")
        sentMails.add(listOf(subject) + receivers.map {it.address} + bodyHTML)
    }
}

class ConfigMock(val uniqueNumber: Int) : DatabaseConfiguration, ApplicationConfiguration {
    override val loadMigrationData: Boolean
        get() = false
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
    override val databaseDriver: String
        get() = "org.h2.Driver"
    override val databaseJDBC: String
        get() = "jdbc:h2:mem:test$uniqueNumber;DB_CLOSE_DELAY=5"
    override val reservablesDirectory: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val webDirectory: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val applicationHost: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val applicationProtocol: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val applicationPort: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val hashSecret: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val mailUsername: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val mailPassword: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val mailFrom: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val mailTestTarget: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val mailHost: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val mailPort: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val mailStarttls: Boolean
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val mailAuth: Boolean
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun globalMailDir(): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

class FileSytemMock() : FileSystemWrapper {
    val reservable = Reservable(
            uniqueId = "mykey",
            elements = ReservableElement(1, "myreservableelement"),
            operatorEmails = listOf("receiver@fake.com")
    )

    override fun getReservableToKey(key: UniqueReservableKey): Reservable? {
        if (this.reservable.key().equals(key)) {
            return reservable
        } else {
            return null
        }
    }

    override fun getKeysFromDirectory(): Map<UniqueReservableKey, Path> {
        return mapOf(this.reservable.key() to Paths.get("keypath"))
    }


    override fun getTemplatesFromDir(dir: String): Map<TemplateTypes, String> = FileSystemWrapperImpl(kodein).getTemplatesFromDir(dir)

    override fun parseTemplate(path: String): MailTemplate?  = FileSystemWrapperImpl(kodein).parseTemplate(path)

    override fun readResourceOrFail(filePath: String): String  = FileSystemWrapperImpl(kodein).readResourceOrFail(filePath)

    override fun specificMailDir(key: UniqueReservableKey): String? = FileSystemWrapperImpl(kodein).specificMailDir(key)


}

val uniqueNumberGenerator = AtomicInteger(1)

fun getMockKodein(): Kodein {
    val nr = uniqueNumberGenerator.getAndIncrement()
    val kodein = Kodein {
        bind<ApplicationConfiguration>() with singleton { ConfigMock(nr) }
        bind<DatabaseConfiguration>() with singleton { ConfigMock(nr) }
        bind<JWTReceiver>() with singleton { JWTReceiverImpl(kodein) }
        bind<GetReceiver>() with singleton { GetReceiverImpl(kodein) }
        bind<PostReceiver>() with singleton { PostReceiverImpl(kodein) }
        bind<FileSystemWrapper>() with singleton { FileSytemMock() }
        bind<AuthVerifier>() with singleton { AuthVerifierImpl(kodein) }
        bind<BusinessLogic>() with singleton { BusinessLogicImpl(kodein) }
        bind<DatabaseEncapsulation>() with singleton { DatabaseEncapsulationImpl(kodein) }
        bind<MailService>() with singleton { MailerMock() }
        bind<MailTemplater>() with singleton { MailServiceImpl(kodein) }
        bind<HTMLSanitizer>() with singleton { HTMLSanitizerImpl(kodein) }
    }

    return kodein
}