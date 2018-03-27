package krese.test

import com.github.salomonbrys.kodein.instance
import krese.BusinessLogic
import krese.DatabaseEncapsulation
import krese.FileSystemWrapper
import krese.MailService
import krese.data.*
import krese.impl.AuthVerifierImpl
import org.joda.time.DateTime
import org.junit.Test
import kotlin.test.assertEquals

class JWTTest {


    val kodein = getMockKodein()

    val businessLogic: BusinessLogic = kodein.instance()

    val fileSystemRaw: FileSystemWrapper = kodein.instance()
    val mailerRaw: MailService = kodein.instance()

    val fileMock = fileSystemRaw as FileSytemMock
    val mailMock = mailerRaw as MailerMock
    val databaseEncapsulation: DatabaseEncapsulation = kodein.instance()


    val creator = Email("sender@mock.com")
    val moderator = Email(fileMock.reservable.operatorEmails.first())

    val createAction = CreateAction(
            key = fileMock.reservable.key(),
            email = creator,
            name = "Sender McSenderson",
            telephone = "",
            commentUser = "no comment",
            startTime = System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 5, endTime = System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 7, blocks = listOf(DbBlockData(listOf(1), 1)))



    @Test
    fun authverifier_encodesAndDecodesJWTCorrectly() {
        val auth = AuthVerifierImpl(kodein)
        val userProfile = buildUserProfile(Email("my@email.com"), DateTime.now().minusSeconds(5), DateTime.now().plusSeconds(10))
        val input1 = JWTPayload(createAction, listOf("param a", "Param b", "[54,24,643]"), userProfile)
        val input2 = JWTPayload(null, listOf("Do not need"), userProfile)
        val input3 = JWTPayload(null, listOf("Do not need"), userProfile.copy(validTo = userProfile.validTo - 11000))
        val encoded1 = auth.encodeJWT(input1)
        val encoded2 = auth.encodeJWT(input2)
        val encoded3 = auth.encodeJWT(input3)
        val output1 = auth.decodeJWT(encoded1!!)
        val output2 = auth.decodeJWT(encoded2!!)
        val output3 = auth.decodeJWT(encoded3!!)
        assertEquals(input1, output1)
        assertEquals(input2, output2)
        assertEquals(null, output3)
    }
}