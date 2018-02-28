package krese

import krese.data.Timespan
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.dao.*


//TODO: IMPORTANT:
/*
We need to have two levels of resources: one named and one number
it's for example possible to have 2 rooms ( A and B) with 4 beds each.
Someone should be able to select B-3 for 3 beds in room B. The second layer is only a number. The first is a named entity one can actually select
 */


object FullBookings : IntIdTable() {
    val eventchainindex = varchar("eventchainindex", 50).index()
    val name = varchar("name", 50).index()
    val age = integer("age")
}

object BlockedResources: IntIdTable() {
    val blockid = long("blockid")
    val booking = reference("booking", FullBookings)
}

class FullBooking(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<FullBooking>(FullBookings)

    var eventchainindex by FullBookings.eventchainindex
    var name by FullBookings.name
    var age by FullBookings.age
}

class BlockedResource(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<BlockedResource>(BlockedResources)

    var blockid by BlockedResources.blockid
    val booking by FullBooking referrersOn BlockedResources.booking
}


class KreseDatabaseManager(config: Config) {

    fun updateBooking(id: Long?, fullBooking: krese.data.FullBooking): Unit {
        TODO("Not yet implemented")
    }

    fun retreiveSingleBooking(id: Long): krese.data.FullBooking? {
        TODO("Not yet implemented")
    }

    fun retreiveBookingRange(eventchain: String, timespan: Timespan) : List<krese.data.FullBooking> {
        TODO("Not yet implemented")
    }
}