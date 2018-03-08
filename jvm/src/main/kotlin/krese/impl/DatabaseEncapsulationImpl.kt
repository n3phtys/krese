package krese.impl

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.instance
import com.google.gson.Gson
import krese.DatabaseConfiguration
import krese.DatabaseEncapsulation
import krese.data.DbBlockData
import krese.data.Email
import krese.data.Reservation
import krese.data.UniqueReservableKey
import krese.utility.fromJson
import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.joda.time.DateTime


class DatabaseEncapsulationImpl(private val kodein: Kodein) : DatabaseEncapsulation {

    private val databaseConfig: DatabaseConfiguration = kodein.instance()

    init {
        createSchemaIfNotExists()
    }

    fun createSchemaIfNotExists(): Unit {
        Database.connect(databaseConfig.databaseJDBC, driver = databaseConfig.databaseDriver)
        transaction {
            create(DbBookings, DbBlocks)
        }
    }


    override fun createUpdateBooking(id: Long?, data: DbBookingInputData): DbBookingOutputData? {
        Database.connect(databaseConfig.databaseJDBC, driver = databaseConfig.databaseDriver)
        var dbb : DbBookingOutputData? = null
        val ts = DateTime.now()
        if (id != null) {
            transaction {
                val ele = DbBooking.findById(id)
                if (ele != null) {
                    DbBlocks.deleteWhere { DbBlocks.dBBookingId eq id }
                    ele.accepted = data.accepted
                    ele.commentOperator = data.commentOperator
                    ele.commentUser = data.commentUser
                    ele.createdTimestamp = data.createdTimestamp
                    ele.email = data.email.address
                    ele.endDateTime = data.endTime
                    ele.modifiedTimestamp = ts
                    ele.name = data.name
                    ele.startDateTime = data.startTime
                    ele.reservableKey = data.key.id
                    ele.telephone = data.telephone

                    data.blocks.forEach {
                        DbBlock.new {
                            usedNumber = it.usedNumber
                            dBBooking = ele
                            elementPath = Gson().toJson(it.elementPath)
                        }
                    }
                    dbb = ele.toOutput()
                }
            }
        } else {
            transaction {
                val ele = DbBooking.new {
                    accepted = data.accepted
                    commentOperator = data.commentOperator
                    commentUser = data.commentUser
                    createdTimestamp = data.createdTimestamp
                    email = data.email.address
                    endDateTime = data.endTime
                    modifiedTimestamp = ts
                    name = data.name
                    startDateTime = data.startTime
                    reservableKey = data.key.id
                    telephone = data.telephone
                }
                data.blocks.forEach {
                    DbBlock.new {
                        usedNumber = it.usedNumber
                        dBBooking = ele
                        elementPath = Gson().toJson(it.elementPath)
                    }
                }
                dbb = ele.toOutput()
            }
        }
        return dbb
    }

    fun deleteBlocks(bookingId: Long) {
        Database.connect(databaseConfig.databaseJDBC, driver = databaseConfig.databaseDriver)
        transaction {
            DbBlocks.deleteWhere { DbBlocks.dBBookingId eq bookingId }
        }
    }

    override fun deleteBooking(id: Long) : Boolean {
        Database.connect(databaseConfig.databaseJDBC, driver = databaseConfig.databaseDriver)
        var exists : Boolean = false
        transaction {
            exists = DbBooking.findById(id) != null
            DbBlocks.deleteWhere { DbBlocks.dBBookingId eq id }
            DbBookings.deleteWhere { DbBookings.id eq id}

        }
        return exists
    }

    override fun acceptBooking(id: Long) : Boolean {
        Database.connect(databaseConfig.databaseJDBC, driver = databaseConfig.databaseDriver)
        var found : Boolean = false
        transaction {
            val dbB = DbBooking.findById(id)
            if (dbB != null) {
                dbB.accepted = true
                found = true
            }
        }
        return found
    }

    override fun retrieveBookingsForKey(key: UniqueReservableKey, includeMinTimestamp: DateTime, excludeMaxTimestamp: DateTime) : List<DbBookingOutputData> {
        val myBookings = mutableListOf<DbBookingOutputData>()
        val skey = key.id

        Database.connect(databaseConfig.databaseJDBC, driver = databaseConfig.databaseDriver)
        transaction {
            myBookings.addAll(
                    DbBooking.find { DbBookings.reservableKey eq skey and (DbBookings.startDateTime less excludeMaxTimestamp or DbBookings.endDateTime.greaterEq(includeMinTimestamp))} .sortedByDescending { DbBookings.startDateTime }.map { it.toOutput() }
            )
        }
        return myBookings
    }

}

fun createInMemoryElements() {
    Database.connect("jdbc:h2:mem:test", driver = "org.h2.Driver")

    transaction {
        //logger.addLogger(StdOutSqlLogger)

        create (DbBookings, DbBlocks)

        val firstB = DbBooking.new {
            name = "first booking"
            accepted = false
            reservableKey = "reservable/key"
            email = "placeholder@email.com"
            telephone = "1234567890"
             commentUser = "N/A"
             commentOperator = "N/A"
             startDateTime = DateTime.now().plusDays(4)
             endDateTime = DateTime.now().plusDays(5)
             createdTimestamp = DateTime.now()
             modifiedTimestamp = DateTime.now()
        }

        val secondB = DbBooking.new {
            name = "second booking"
            accepted = true
            reservableKey = "reservable/key"
            email = "placeholder@email.com"
            telephone = "1234567890"
            commentUser = "N/A"
            commentOperator = "N/A"
            startDateTime = DateTime.now().plusDays(2)
            endDateTime = DateTime.now().plusDays(3)
            createdTimestamp = DateTime.now()
            modifiedTimestamp = DateTime.now()
        }

        DbBlock.new {
            usedNumber = 1
            dBBooking = firstB
            elementPath = "[5,21,53]"
        }



        println("Bookings: ${DbBooking.all().joinToString {it.name}}")
        println("Blocks in ${firstB.name}: ${firstB.blocks.joinToString {it.elementPath}}")
        println("Accepted Bookings: ${DbBooking.find { DbBookings.accepted eq true}.joinToString {it.name}}")
    }
}


object DbBookings : LongIdTable("db_bookings", "id") {
    val reservableKey = varchar("reservable_key", 127).index(false)
    val email = varchar("email", 255)
    val name = varchar("name", 255)
    val telephone = varchar("telephone", 50)
    val commentUser = varchar("comment_user", 511)
    val commentOperator = varchar("comment_operator", 511)
    val startDateTime = datetime("start_dt")
    val endDateTime = datetime("end_dt")
    val createdTimestamp = datetime("created_ts")
    val modifiedTimestamp = datetime("modified_ts")
    val accepted = bool("accepted")
}

data class DbBookingInputData(val key: UniqueReservableKey, val email: Email, val name: String, val telephone: String, val commentUser: String, val commentOperator: String, val startTime: DateTime, val endTime: DateTime, val createdTimestamp: DateTime, val accepted: Boolean, val blocks: List<DbBlockData>)


class DbBooking(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<DbBooking>(DbBookings)

    var reservableKey by DbBookings.reservableKey
    var email by DbBookings.email
    var name by DbBookings.name
    var telephone by DbBookings.telephone
    var commentUser by DbBookings.commentUser
    var commentOperator by DbBookings.commentOperator
    var startDateTime by DbBookings.startDateTime //start date of the reservation
    var endDateTime by DbBookings.endDateTime //end date of the reservation (should be after startDateTime of course)
    var createdTimestamp by DbBookings.createdTimestamp //set when first created
    var modifiedTimestamp by DbBookings.modifiedTimestamp //set whenever change operation is executed on this data
    var accepted by DbBookings.accepted //true if and only if the operator accepted the booking

    val blocks by DbBlock referrersOn DbBlocks.dBBookingId

    fun getReservableKey() : UniqueReservableKey {
        return UniqueReservableKey(this.reservableKey)
    }


    fun toOutput() : DbBookingOutputData = DbBookingOutputData(id.value, getReservableKey(), Email(email), name, telephone, commentUser, commentOperator, startDateTime, endDateTime, createdTimestamp, modifiedTimestamp, accepted, blocks.map { it.toOutput() })
}


data class DbBookingOutputData(val id : Long, val key: UniqueReservableKey, val email: Email, val name: String, val telephone: String, val commentUser: String, val commentOperator: String, val startTime: DateTime, val endTime: DateTime, val createdTimestamp: DateTime, val modifiedTimestamp: DateTime, val accepted: Boolean, val blocks: List<DbBlockData>) {
        fun toOutput(isOperator: Boolean) : Reservation {
            return Reservation(id, key, if(isOperator) email else null, name, if(isOperator) telephone else null, commentUser, if(isOperator) commentOperator else null, startTime, endTime, createdTimestamp, if(isOperator) modifiedTimestamp else null, accepted, blocks)
        }
}



object DbBlocks: LongIdTable("db_blocks", "id") {

    val dBBookingId  = reference("db_booking_id", DbBookings)
    val elementPath = varchar("elementh_path", 255).index(false)
    val usedNumber = integer("used_number")
}


class DbBlock(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<DbBlock>(DbBlocks)

    var elementPath by DbBlocks.elementPath //should be a JSON array of ints
    var usedNumber by DbBlocks.usedNumber //should be positive number (zero makes no sense)
    var dBBooking by DbBooking referencedOn DbBlocks.dBBookingId

    fun getElementPathSegments() : List<Int> {
        return Gson().fromJson<List<Int>>(this.elementPath)
    }


    fun toOutput() : DbBlockData = DbBlockData(getElementPathSegments(), usedNumber)
}

