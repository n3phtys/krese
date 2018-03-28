package krese

import kotlinx.serialization.json.JSON
import krese.data.PostResponse
import krese.data.Routes
import org.w3c.xhr.XMLHttpRequest

class ServerActionPost(val action: String) {


    fun asyncCall(func: (PostResponse) -> Unit) {
        val xhttp = XMLHttpRequest()
        xhttp.open("POST", Routes.POST_ACTION_SIGNED.path, true)
        xhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded")
        xhttp.onreadystatechange = {
            println("Received something... state = ${xhttp.readyState} and status = ${xhttp.status}")
            if (xhttp.readyState == 4.toShort() && xhttp.status == 200.toShort()) {
                val res: PostResponse = JSON.parse(xhttp.responseText)
                func.invoke(res)
            }
        }

        xhttp.send("action=$action")
    }
}