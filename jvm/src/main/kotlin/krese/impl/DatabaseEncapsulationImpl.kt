package krese.impl

import com.github.salomonbrys.kodein.Kodein
import krese.DatabaseEncapsulation
import krese.data.Booking
import krese.data.Timespan
import org.jetbrains.exposed.dao.*

class DatabaseEncapsulationImpl(private val kodein: Kodein) : DatabaseEncapsulation {
    override fun getAllocatedTimeslotsToKey(key: String, includeMinTimestamp: Long, excludeMaxTimestamp: Long, start: Long, end: Long): List<Booking> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }







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
