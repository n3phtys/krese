package krese.test

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.singleton
import krese.DatabaseConfiguration
import krese.DatabaseEncapsulation
import krese.HTMLSanitizer
import krese.data.DbBlockData
import krese.data.Email
import krese.data.UniqueReservableKey
import krese.impl.DatabaseEncapsulationImpl
import krese.impl.DbBookingInputData
import krese.impl.HTMLSanitizerImpl
import org.joda.time.DateTime
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DBTest {

    data class DBMockConfig(override val databaseDriver: String = "org.h2.Driver", override val databaseJDBC: String = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1") : DatabaseConfiguration {
        override val loadMigrationData: Boolean
            get() = false
        override val databasePort: String
            get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        override val databaseHost: String
            get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        override val databaseName: String
            get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        override val databaseUsername: String
            get() = "admin"
        override val databasePassword: String
            get() = "secret"
    }

    val kodein = Kodein {
        bind<DatabaseConfiguration>() with singleton {  DBMockConfig() }
        bind<HTMLSanitizer>() with singleton { HTMLSanitizerImpl(kodein) }
    }

    @Test
    fun databasewrapper_createsAndUpdatesCorrectly() {
        val firstkey = UniqueReservableKey("firstkey")
        val secondkey = UniqueReservableKey("secondkey")
        val db : DatabaseEncapsulation = DatabaseEncapsulationImpl(kodein)
        assertTrue(db.retrieveBookingsForKey(firstkey).isEmpty())
        assertTrue(db.retrieveBookingsForKey(secondkey).isEmpty())


        println("So far")

        val first = db.createUpdateBooking(null, DbBookingInputData(firstkey, Email("some@email.com"), "name", "01245", "unchanged comment", "comment b", DateTime.now().plusDays(2), DateTime.now().plusDays(3), DateTime.now(), false, listOf(DbBlockData(listOf(1,2,3), 1))))
        val second = db.createUpdateBooking(null, DbBookingInputData(secondkey, Email("some@email.com"), "name", "01245", "comment c", "comment d", DateTime.now().plusDays(2), DateTime.now().plusDays(3), DateTime.now(), false, listOf(DbBlockData(listOf(1,2,3), 1))))
        db.createUpdateBooking(first?.id, DbBookingInputData(firstkey, Email("some@email.com"), "name", "01245", "comment a", "comment b", DateTime.now().plusDays(2), DateTime.now().plusDays(3), DateTime.now(), false, listOf(DbBlockData(listOf(1,2,3), 1))))

        if (first != null) {
            db.acceptBooking(first.id)
        }

        val fl = db.retrieveBookingsForKey(firstkey)
        val sl = db.retrieveBookingsForKey(secondkey)
        assertEquals(fl.size,1)
        assertTrue(fl.get(0).commentUser.contentEquals("comment a"))
        assertEquals(fl.get(0).accepted, true)
        assertEquals(sl.size , 1)
        if (second != null) {
            db.deleteBooking(second.id)
        }
        val sl2 = db.retrieveBookingsForKey(secondkey)
        assertEquals(sl2.size,0)
        assertTrue(sl2.isEmpty())
    }

}