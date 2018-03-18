package krese.migration

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JSON
import krese.data.*
import krese.impl.DbBookingInputData
import org.joda.time.DateTime

@Serializable
data class OldMigrationData(
		val gaestezimmer_ka: List<OldReservation>,
		val saal_ka: List<OldReservation>,
		val skihuette: List<OldReservation>
) {
	fun toJsonConfigs() : List<Reservable> {
		//TODO: only create once
		return listOf(
				Reservable(uniqueId = "gaestezimmer_ka", elements = ReservableElement(1, "Betten", "", 2), operatorEmails = listOf("operator1@email.com")),
				Reservable(uniqueId = "saal_ka", elements = ReservableElement(1, "Saal", "", 1), operatorEmails = listOf("operator2@email.com")),
				Reservable(uniqueId = "skihuette", elements = ReservableElement(1, "Skihütte", "", 10), operatorEmails = listOf("operator3@email.com"))
		)
	}

	fun toDBElements() : List<DbBookingInputData> {
		return gaestezimmer_ka.map {it.toDBElement("gaestezimmer_ka")} + saal_ka.map {it.toDBElement("saal_ka")} + skihuette.map {it.toDBElement("skihuette")}
	}
}


@Serializable
data class OldReservation(
		val id: String,
		val elements: List<OldElement>,
		val name: String,
		val email: String,
		val telephone: String,
		val commentary: String,
		val confirmedBySupseruser: Boolean
) {
	fun toDBElement(key: String) : DbBookingInputData {
		val begin: DateTime = DateTime(elements.map {it.from.toLong()}.min())
		val end : DateTime = DateTime(elements.map {it.to.toLong()}.max())
		return DbBookingInputData(UniqueReservableKey(key), Email(email), name, telephone, commentary, "", begin, end, DateTime.now(), confirmedBySupseruser, listOf(DbBlockData(listOf(1), elements.size)))
	}
}

@Serializable
data class OldElement(
		val element: String,
		val from: String,
		val to: String
)


val migrationFileLocation : String = "../old_migrated_data.json"

val migrationFileLoaded = loadMigrationFile()

fun loadMigrationFile() : OldMigrationData? {
	try {
		val f = java.io.File(migrationFileLocation)
		val text = f.readText(Charsets.UTF_8)
		val json = JSON.parse<OldMigrationData>(text)
		return json
	} catch (e : Exception) {
		return null
	}
}


