package krese.test

import kotlinx.serialization.json.JSON
import krese.data.*
import org.junit.Test
import kotlin.test.assertEquals

class SerializationTest {
    val kodein = getMockKodein()




    val createAction = CreateAction(
            key =   UniqueReservableKey("mykey"),
            email = Email("sender@emailserver.com"),
            name = "Sender McSenderson",
            telephone = "",
            commentUser = "no comment",
            startTime = 1522347015218, endTime = 1522519815218, blocks = listOf(DbBlockData(listOf(1), 1)))

    val declineAction = DeclineAction(42, "some comment")

    val actionList = listOf(
            createAction
            , declineAction
    )

//    @Test
//    fun action_serializeCreate() {
//        val json = JSON.stringify(actionList.get(0))
//        assertEquals("""{"key":{"id":"mykey"},"email":{"address":"sender@emailserver.com"},"name":"Sender McSenderson","telephone":"","commentUser":"no comment","startTime":1522347015218,"endTime":1522519815218,"blocks":[{"elementPath":[1],"usedNumber":1}]}""", json)
//    }
//
//    @Test
//    fun action_deserializeCreate() {
//        val input = JSON.stringify(actionList.get(0))
//        val output = JSON.parse<CreateAction>(input)
//        assertTrue( output is CreateAction)
//    }


}