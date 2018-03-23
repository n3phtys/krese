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
            startTime = System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 5, endTime = System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 7, blocks = listOf(DbBlockData(listOf(1), 1)))

    @Test
    fun businesslogic_createwithoutverification() {
        mailMock.sentMails.clear()
        val response : PostResponse = businessLogic.process(createAction,null, false )
        assertEquals(response.finished, false)
        assertEquals(response.successful, true)
        assertEquals(mailMock.sentMails.last().emailReceivers().get(0), creator.address)
        assertEquals(mailMock.sentMails.last().emailSubject(), "Confirmation of Request required")
        assertEquals(mailMock.sentMails.last().emailBody(), "body of confirmation")
    }

    @Test
    fun businesslogic_createwithverification() {
        mailMock.sentMails.clear()
        val response : PostResponse = businessLogic.process(createAction, creator, true )
        assertEquals(response.finished, true)
        assertEquals(response.successful, true)
        assertEquals(mailMock.sentMails.last().emailReceivers().get(0), creator.address)
        assertEquals(mailMock.sentMails.last().emailSubject(), "Successfully created reservation")
        assertEquals(mailMock.sentMails.last().emailBody(), "body of success to creator")
        assertEquals(mailMock.sentMails.get(mailMock.sentMails.size - 2).emailReceivers().get(0), moderator.address)
        assertEquals(mailMock.sentMails.get(mailMock.sentMails.size - 2).emailSubject(), "New Reservation created")
        assertEquals(mailMock.sentMails.get(mailMock.sentMails.size - 2).emailBody(), "body of success to moderator")
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
        assertEquals(mailMock.sentMails.last().emailSubject(), "Reservation was acccepted")
        assertEquals(mailMock.sentMails.last().emailBody(), "body of success to creator")
        assertEquals(mailMock.sentMails.get(mailMock.sentMails.size - 2).emailReceivers().get(0), moderator.address)
        assertEquals(mailMock.sentMails.get(mailMock.sentMails.size - 2).emailSubject(), "Reservation was acccepted")
        assertEquals(mailMock.sentMails.get(mailMock.sentMails.size - 2).emailBody(), "body of success to moderator")
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
        assertEquals(mailMock.sentMails.last().emailSubject(), "Reservation was declined")
        assertEquals(mailMock.sentMails.last().emailBody(), "body of success to creator")
        assertEquals(mailMock.sentMails.get(mailMock.sentMails.size - 2).emailReceivers().get(0), moderator.address)
        assertEquals(mailMock.sentMails.get(mailMock.sentMails.size - 2).emailSubject(), "Reservation was declined")
        assertEquals(mailMock.sentMails.get(mailMock.sentMails.size - 2).emailBody(), "body of success to moderator")
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
        assertEquals(mailMock.sentMails.last().emailSubject(), "Reservation was withdrawn")
        assertEquals(mailMock.sentMails.last().emailBody(), "body of success to creator")
        assertEquals(mailMock.sentMails.get(mailMock.sentMails.size - 2).emailReceivers().get(0), moderator.address)
        assertEquals(mailMock.sentMails.get(mailMock.sentMails.size - 2).emailSubject(), "Reservation was withdrawn")
        assertEquals(mailMock.sentMails.get(mailMock.sentMails.size - 2).emailBody(), "body of success to moderator")
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