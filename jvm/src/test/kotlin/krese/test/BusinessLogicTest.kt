package krese.test

import com.github.salomonbrys.kodein.instance
import krese.BusinessLogic
import krese.DatabaseEncapsulation
import krese.FileSystemWrapper
import krese.MailService
import krese.data.*
import org.junit.Test
import kotlin.test.assertEquals

class BusinessLogicTest {
    val kodein = getMockKodein()

    val businessLogic: BusinessLogic = kodein.instance()

    val fileSystemRaw : FileSystemWrapper = kodein.instance()
    val mailerRaw : MailService = kodein.instance()

    val fileMock = fileSystemRaw as FileSytemMock
    val mailMock = mailerRaw as MailerMock
    val databaseEncapsulation : DatabaseEncapsulation = kodein.instance()


    val creator = Email("sender@mock.com")
    val moderator = Email(fileMock.reservable.operatorEmails.first())

    val createAction = CreateAction(
            key =   fileMock.reservable.key(),
            email = creator,
            name = "Sender McSenderson",
            telephone = "",
            commentUser = "no comment",
            startTime = 1522181991448L + 1000L * 60 * 60 * 24 * 5, endTime = 1522181991448L + 1000L * 60 * 60 * 24 * 7, blocks = listOf(DbBlockData(listOf(1), 1)))

    @Test
    fun businesslogic_createwithoutverification() {
        mailMock.sentMails.clear()
        val response : PostResponse = businessLogic.process(createAction,null, false )
        assertEquals(response.finished, false)
        assertEquals(response.successful, true)
        assertEquals(mailMock.sentMails.last().emailReceivers().get(0), creator.address)
        assertEquals(mailMock.sentMails.last().emailSubject(), "Creating reservation requires your action!")
    }

    @Test
    fun businesslogic_createwithverification() {
        mailMock.sentMails.clear()
        val response : PostResponse = businessLogic.process(createAction, creator, true )
        assertEquals(response.finished, true)
        assertEquals(response.successful, true)
        assertEquals(mailMock.sentMails.last().emailReceivers().get(0), creator.address)
        assertEquals(mailMock.sentMails.last().emailSubject(), "Reservation for PLACEHOLDER TITLE FOR THIS on 2018-04-01")
        assertEquals(mailMock.sentMails.get(mailMock.sentMails.size - 2).emailReceivers().get(0), moderator.address)
        assertEquals(mailMock.sentMails.get(mailMock.sentMails.size - 2).emailSubject(), "New Reservation for PLACEHOLDER TITLE FOR THIS on 2018-04-01")
    }



    @Test
    fun businesslogic_acceptwithoutverification() {
        businessLogic.process(createAction, creator, true )
        mailMock.sentMails.clear()
        val acceptAction = AcceptAction(databaseEncapsulation.retrieveBookingsForKey(createAction.key).filter { !it.accepted }.first().id, "")
        val response = businessLogic.process(acceptAction, null, false )
        assertEquals(response.finished, false)
        assertEquals(response.successful, false)
        assertEquals(mailMock.sentMails.size, 0)
    }

    @Test
    fun businesslogic_acceptwithverification() {
        assertEquals(moderator.address, "receiver@fake.com")

        businessLogic.process(createAction, creator, true )
        mailMock.sentMails.clear()
        val acceptAction = AcceptAction(databaseEncapsulation.retrieveBookingsForKey(createAction.key).filter { !it.accepted }.first().id, "")
        val response = businessLogic.process(acceptAction, moderator, true )
        assertEquals(response.finished, true)
        assertEquals(response.successful, true)
        assertEquals(mailMock.sentMails.size, 2)
        assertEquals(mailMock.sentMails.last().emailReceivers().get(0), creator.address)
        assertEquals(mailMock.sentMails.last().emailSubject(), "Reservation for PLACEHOLDER TITLE FOR THIS on 2018-04-01 was accepted")
        assertEquals(mailMock.sentMails.get(mailMock.sentMails.size - 2).emailReceivers().get(0), moderator.address)
        assertEquals(mailMock.sentMails.get(mailMock.sentMails.size - 2).emailSubject(), "Reservation for PLACEHOLDER TITLE FOR THIS on 2018-04-01 was accepted")
    }

    @Test
    fun businesslogic_declinewithverification() {
        businessLogic.process(createAction, creator, true )
        mailMock.sentMails.clear()
        val acceptAction = DeclineAction(databaseEncapsulation.retrieveBookingsForKey(createAction.key).filter { !it.accepted }.first().id, "")
        val response = businessLogic.process(acceptAction, moderator, true )
        assertEquals(response.finished, true)
        assertEquals(response.successful, true)
        assertEquals(mailMock.sentMails.size, 2)
        assertEquals(mailMock.sentMails.last().emailReceivers().get(0), creator.address)
        assertEquals(mailMock.sentMails.last().emailSubject(), "Reservation for PLACEHOLDER TITLE FOR THIS on 2018-04-01 was declined")
        assertEquals(mailMock.sentMails.get(mailMock.sentMails.size - 2).emailReceivers().get(0), moderator.address)
        assertEquals(mailMock.sentMails.get(mailMock.sentMails.size - 2).emailSubject(), "Reservation for PLACEHOLDER TITLE FOR THIS on 2018-04-01 was declined")
    }


    @Test
    fun businesslogic_withdrawwithverification() {
        businessLogic.process(createAction, creator, true )
        mailMock.sentMails.clear()
        val acceptAction = WithdrawAction(databaseEncapsulation.retrieveBookingsForKey(createAction.key).filter { !it.accepted }.first().id, "")
        val response = businessLogic.process(acceptAction, creator, true )
        assertEquals(response.finished, true)
        assertEquals(response.successful, true)
        assertEquals(mailMock.sentMails.size, 2)
        assertEquals(mailMock.sentMails.last().emailReceivers().get(0), creator.address)
        assertEquals(mailMock.sentMails.last().emailSubject(), "Reservation for PLACEHOLDER TITLE FOR THIS on 2018-04-01 was withdrawn")
        assertEquals(mailMock.sentMails.get(mailMock.sentMails.size - 2).emailReceivers().get(0), moderator.address)
        assertEquals(mailMock.sentMails.get(mailMock.sentMails.size - 2).emailSubject(), "Reservation for PLACEHOLDER TITLE FOR THIS on 2018-04-01 was withdrawn")
    }


    @Test
    fun businesslogic_withdrawwithoutverification() {
        businessLogic.process(createAction, creator, true )
        mailMock.sentMails.clear()
        val acceptAction = AcceptAction(databaseEncapsulation.retrieveBookingsForKey(createAction.key).filter { !it.accepted }.first().id, "")
        val response = businessLogic.process(acceptAction, null, false )
        assertEquals(response.finished, false)
        assertEquals(response.successful, false)
        assertEquals(mailMock.sentMails.size, 0)
    }
}