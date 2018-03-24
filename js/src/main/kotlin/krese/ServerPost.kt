package krese

import kotlinx.serialization.json.JSON
import krese.data.*
import org.w3c.xhr.XMLHttpRequest

class ServerPost(val action: PostAction, val jwt: String?) {

    fun asyncCall(func: (PostResponse) -> Unit) {
        val xhttp = XMLHttpRequest()
        xhttp.open("POST", Routes.POST_ENTRIES_TO_RESERVABLE.path, true)
        xhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded")
        xhttp.onreadystatechange = {
            println("Received something... state = ${xhttp.readyState} and status = ${xhttp.status}")
            if (xhttp.readyState == 4.toShort() && xhttp.status == 200.toShort()) {
                val res: PostResponse = JSON.parse(xhttp.responseText)
                func.invoke(res)
            }
        }
        val json = (when(action) {
            is CreateAction -> JSON.stringify(PostActionInput(jwt, null, action))
            is DeclineAction -> TODO()
            is WithdrawAction -> TODO()
            is AcceptAction -> TODO()
        })
        println("Posting action with json = $json")

        xhttp.send("action=${encodeURIComponent(json)}")
    }
}