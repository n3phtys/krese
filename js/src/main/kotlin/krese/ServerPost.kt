package krese

import kotlinx.serialization.json.JSON
import krese.data.*
import org.w3c.xhr.XMLHttpRequest

class ServerPost(val action: PostAction, val jwt: String?) {


    val routepath = when (action) {

        is CreateAction -> Routes.POST_ACTION_CREATE.path
        is DeclineAction -> Routes.POST_ACTION_DECLINE.path
        is WithdrawAction -> Routes.POST_ACTION_WITHDRAW.path
        is AcceptAction -> Routes.POST_ACTION_ACCEPT.path
        else -> {throw IllegalArgumentException()}
    }


    fun asyncCall(func: (PostResponse) -> Unit) {
        val xhttp = XMLHttpRequest()
        xhttp.open("POST", routepath, true)
        xhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded")
        xhttp.onreadystatechange = {
            println("Received something... state = ${xhttp.readyState} and status = ${xhttp.status}")
            if (xhttp.readyState == 4.toShort() && xhttp.status == 200.toShort()) {
                val res: PostResponse = JSON.parse(xhttp.responseText)
                func.invoke(res)
            }
        }
        val json = (when (action) {
            is CreateAction -> JSON.stringify(CreateActionInput(jwt, action))
            is DeclineAction -> JSON.stringify(DeclineActionInput(jwt, action))
            is WithdrawAction -> JSON.stringify(WithdrawActionInput(jwt, action))
            is AcceptAction -> JSON.stringify(AcceptActionInput(jwt, action))
            else -> {throw IllegalArgumentException()}
        })
        println("Posting action with json = $json")

        xhttp.send("action=${encodeURIComponent(json)}")
    }
}

