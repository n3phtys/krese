package krese.test

import com.github.salomonbrys.kodein.instance
import krese.BusinessLogic
import krese.FileSystemWrapper
import krese.MailService
import krese.data.CreateAction
import krese.data.DbBlockData
import krese.data.Email
import krese.data.PostResponse
import org.junit.Test
import kotlin.test.assertEquals

class BusinessLogicTest {
    val kodein = getMockKodein()

    val businessLogic: BusinessLogic = kodein.instance()

    val fileSystemRaw : FileSystemWrapper = kodein.instance()
    val mailerRaw : MailService = kodein.instance()

    val fileMock = fileSystemRaw as FileSytemMock
    val mailMock = mailerRaw as MailerMock


    val creator = Email("sender@mock.com")
    val moderator = Email(fileMock.reservable.operatorEmails.first())

    val createAction = CreateAction(
            key =   fileMock.reservable.key(),
            email = creator,
            name = "Sender McSenderson",
            telephone = "",
            commentUser = "no comment",
            startTime = System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 5, endTime = System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 7, blocks = listOf(DbBlockData(listOf(1), 1)))

    @Test
    fun businesslogic_createwithoutverification() {
        //TODO("implement test")
        val response : PostResponse = businessLogic.process(createAction,null, false
        )
        assertEquals(response.finished, false)
        assertEquals(response.successful, true)
        assertEquals(mailMock.sentMails.last().emailReceivers().get(0), creator.address)
        assertEquals(mailMock.sentMails.last().emailSubject(), "Confirmation of Request required")
        assertEquals(mailMock.sentMails.last().emailBody(), "body of confirmation")
    }

//    @Test
//    fun businesslogic_createwithverification() {
//        TODO("implement test")
//    }
//
//
//
//    @Test
//    fun businesslogic_acceptwithoutverification() {
//        TODO("implement test")
//    }
//
//    @Test
//    fun businesslogic_acceptwithverification() {
//        TODO("implement test")
//    }
//
//    @Test
//    fun businesslogic_declinewithverification() {
//        TODO("implement test")
//    }
//
//
//    @Test
//    fun businesslogic_withdrawwithverification() {
//        TODO("implement test")
//    }
//
//
//    @Test
//    fun businesslogic_withdrawwithoutverification() {
//        TODO("implement test")
//    }
}