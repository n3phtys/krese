package krese

import krese.data.Routes
import krese.data.UniqueReservableKey
import org.w3c.xhr.XMLHttpRequest


class ServerGet(val key: UniqueReservableKey?, val jwt: String?) {
    fun asyncCall(func: (String) -> Unit) {
        val xhttp = XMLHttpRequest();
        xhttp.open("GET", Routes.GET_RESERVABLES.path + if (jwt != null) "?jwt=$jwt" else "", true)
        xhttp.onreadystatechange = {
            if (xhttp.readyState == 4.toShort() && xhttp.status == 200.toShort()) {
                func.invoke(xhttp.responseText)
            }
        }
        xhttp.send()
    }
}