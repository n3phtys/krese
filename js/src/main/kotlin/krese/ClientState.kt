package krese

import jquery.jq
import kotlinext.js.asJsObject
import kotlinx.html.dom.create
import kotlinx.serialization.json.JSON
import krese.data.GetResponse
import krese.data.GetTotalResponse
import krese.data.Routes
import krese.data.UniqueReservableKey
import org.w3c.xhr.XMLHttpRequest
import kotlinx.html.*
import kotlinx.html.js.onClickFunction

import kotlinx.html.*
import kotlinx.html.consumers.*
import kotlinx.html.dom.*
import kotlinx.html.js.*
import org.w3c.dom.*
import kotlin.browser.*
import kotlin.dom.*
import kotlinx.html.*
import org.w3c.dom.*
import org.w3c.dom.events.Event
import kotlin.browser.document
import kotlin.js.Date


external fun encodeURIComponent(str: String): String

external fun decodeURIComponent(str: String): String

val LOCALSTORAGE_KRESE_LOGIN_JWT_KEY = "LOCALSTORAGE_KRESE_LOGIN_JWT_KEY"


class ClientState {
    private val creationDate = Date.now()

    init {
        println("creationDate =" + Date(creationDate))
    }

    var from = Date(creationDate - 1000L * 60 * 60 * 24 * 180)
    var to = Date(creationDate + 1000L * 60 * 60 * 24 * 180)

    var selectedKey: UniqueReservableKey? = null
    var jwt: String? = localStorage.get(LOCALSTORAGE_KRESE_LOGIN_JWT_KEY)
    var allKeys: List<UniqueReservableKey> = listOf()
    val entries: MutableMap<UniqueReservableKey, GetResponse> = mutableMapOf()


    val navbar = document.getElementById("key-tab-nav-bar")!!
    val tabcontainer = document.getElementById("key-tab-container")!!
    val loginJWTDiv = document.getElementById("login_jwt_div")!!
    val loginStatusDiv = document.getElementById("login_status_div")!!

    val SELECTED_KEY_URL_KEY = "selected_key"

    init {
        loadAllKeys()
        updateJWTDiv()
        writeLoginStatus("JWT unchecked")
        window.setInterval(handler = requestJWTValidation(), timeout = 1000 * 60 * 5)

        //setURLParameters(mapOf("myparam1" to "value1", "myparam2" to "value2"))
        //println("Params:")
        //val params = getURLParameters()
        //params.forEach { println("${it.key} = ${it.value}") }
        testForReceiveRelogin()
    }

    fun writeLoginStatus(status: String) {
        loginStatusDiv.innerHTML = status
    }

    fun updateJWTDiv() {
        loginJWTDiv.innerHTML = if (jwt != null) "Logged in as: ${decodeJWT(jwt!!).email}" else "Not logged in"
    }

    fun requestJWTValidation() {
        val jwt = jwt
        writeLoginStatus("JWT check pending")
        if (jwt != null) {
            val xhttp = XMLHttpRequest();
            xhttp.open("POST", "util/valid/credentials", true)
            xhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded")
            xhttp.onreadystatechange = {
                println("Received something... state = ${xhttp.readyState} and status = ${xhttp.status}")
                if (xhttp.readyState == 4.toShort() && xhttp.status == 200.toShort()) {
                    writeLoginStatus(if(xhttp.responseText.equals(true.toString(), true)) "JWT verified!" else "JWT could not be verified!")
                }
            }
            xhttp.send("jwt=$jwt")
        }
    }

    fun testForReceiveRelogin() {
        //("check url for relogin parameter")
        val relogin = getURLParameters().get("relogin")
        if (relogin != null) {
            //("decode both jwts and compare their creation timestamp")
            val decodedRelogin = decodeJWT(relogin)
            //("keep the newer one")
            if (jwt == null || decodeJWT(jwt!!).iat < decodedRelogin.iat) {
                storePassword(relogin)
            } else {
                console.log("Relogin JWT either too old or invalid, discarding.");
            }
            //("unset relogin parameter")
            unsetURLParameter("relogin")
        }
    }

    fun storePassword(jwt: String) {
        console.log("STORING NEWER JWT CREDENTIALS: $jwt")
        localStorage.set(LOCALSTORAGE_KRESE_LOGIN_JWT_KEY, jwt)
        this.jwt = jwt
        updateJWTDiv()
        writeLoginStatus("JWT unchecked")
    }

    fun decodeJWT(jwt: String) : dynamic {
        console.log("Decoding JWT = $jwt")
        val base64Core = jwt.split('.')[1].replace('-', '+').replace('_', '/')
        console.log("core = $base64Core")
        val decoded = window.atob(base64Core)
        console.log("decoded = $decoded")
        val obj = kotlin.js.JSON.parse<Any>(decoded)
        console.log("Parsed:")
        console.log(obj)
        return obj
    }

    fun switchTo(uniqueReservableKey: UniqueReservableKey) {
        val x: HTMLCollection = document.getElementsByClassName("city")

        selectedKey = uniqueReservableKey

        loadEntriesToKey(selectedKey!!, from, to)

        0.until(x.length).map {
            val k: HTMLDivElement = x.get(it)!! as HTMLDivElement
            k.style.display = "none"
        }
        (document.getElementById("div_" + uniqueReservableKey.id)!! as HTMLDivElement).style.display = "block";
    }

    private fun loadAllKeys() {
        val xhttp = XMLHttpRequest();
        xhttp.open("GET", Routes.GET_RESERVABLES.path, true)
        xhttp.onreadystatechange = {
            if (xhttp.readyState == 4.toShort() && xhttp.status == 200.toShort()) {
                allKeys = JSON.parse<GetTotalResponse>(xhttp.responseText).keys
                setAllKeysToGUI()

                getURLParameters().get(SELECTED_KEY_URL_KEY)?.let { switchTo(UniqueReservableKey(it)) }
            }
        }
        xhttp.send()
    }

    private fun transformKeyIntoButton(key: UniqueReservableKey): HTMLElement {
        return document.create.button {
            classes = setOf("w3-bar-item", "w3-button")
            onClickFunction = {
                addURLParameter(SELECTED_KEY_URL_KEY, key.id)
                switchTo(key)
            }
            +"Button for ${key.id}"
        }
    }

    private fun transformKeyIntoDiv(key: UniqueReservableKey): HTMLElement {
        //TODO: prepare raw structure with skeleton

        /*
        val uniqueId : String,
        val prologueMarkdown: String,
        val epilogueMarkdown: String,
        val staticFiles: List<String>,
        val elements: ReservableElement,
        val operatorEmails: List<String>
         */

        return document.create.div {
            id = "div_" + key.id
            style = "display:none"
            classes = setOf("w3-container", "city")
            div {
                id = "pro_" + key.id
                +"Prologue for = ${key.id}"
            }
            div {
                id = "for_" + key.id
                +"Formular"
            }
            div {
                id = "epi_" + key.id
                +"Epilogue"
            }
            div {
                id = "cal_" + key.id
                +"Kalender"
            }
            div {
                id = "dap_" + key.id
                +"From To Date Picker"
            }
            div {
                id = "lis_" + key.id
                +"Liste"
            }
        }
    }

    private fun setAllKeysToGUI() {
        println("setting all keys to gui")
        navbar.innerHTML = ""
        tabcontainer.innerHTML = ""
        allKeys.sortedBy { it.id }.forEach {
            navbar.appendChild(transformKeyIntoButton(it))
            tabcontainer.appendChild(transformKeyIntoDiv(it))
            addDatePickers(document.getElementById("dap_" + it.id)!!)
        }
    }

    fun loadEntriesToKey(uniqueReservableKey: UniqueReservableKey, from: Date, to: Date) {
        val xhttp = XMLHttpRequest();
        xhttp.open("GET", Routes.GET_ENTRIES_TO_RESERVABLE.path + uniqueReservableKey.id, true)
        xhttp.onreadystatechange = {
            if (xhttp.readyState == 4.toShort() && xhttp.status == 200.toShort()) {
                entries.put(uniqueReservableKey, JSON.parse<GetResponse>(xhttp.responseText))
                setEntriesToKey(uniqueReservableKey)
            }
        }
        xhttp.send()
    }

    fun setEntriesToKey(uniqueReservableKey: UniqueReservableKey) {
        document.getElementById("pro_" + uniqueReservableKey.id)!!.innerHTML = entries.get(uniqueReservableKey)!!.reservable.prologueMarkdown
        document.getElementById("epi_" + uniqueReservableKey.id)!!.innerHTML = entries.get(uniqueReservableKey)!!.reservable.epilogueMarkdown
        addFormular(document.getElementById("for_" + uniqueReservableKey.id)!!, uniqueReservableKey)
        setReservationList(document.getElementById("lis_" + uniqueReservableKey.id)!!, uniqueReservableKey)
        setReservationCalendar(document.getElementById("cal_" + uniqueReservableKey.id)!!, uniqueReservableKey)
    }

    fun setReservationList(listDiv: Element, uniqueReservableKey: UniqueReservableKey) {
        val el = document.create.ol {
            entries.get(uniqueReservableKey)!!.existingReservations.map { document.create.li {
                +("Name: ${it.name} From: ${it.startTime} To: ${it.endTime} for blocks: ${it.blocks.map { it.elementPath.map{it.toString() }.joinToString("-") + " (${it.usedNumber} times)"}}")
            } }
        }
        listDiv.appendChild(el)
    }

    fun setReservationCalendar(calDiv: Element, uniqueReservableKey: UniqueReservableKey) {
        val config = entries.get(uniqueReservableKey)!!.toCalendarConfig()
        val configJson = JSON.stringify(config)
        calDiv.innerHTML = ""
        jq( "#${calDiv.id}").asDynamic().fullCalendar(js("JSON.parse(configJson)"))
    }



    fun addFormular(formDiv: Element, uniqueReservableKey: UniqueReservableKey) {
        formDiv.innerHTML = ""
        //TODO: add fields based on Reservable definition
        formDiv.appendChild(

                document.create.form(action = null, encType = null,
                        method = null) {
                    id = "submitform_${uniqueReservableKey.id}"
                    onSubmitFunction = {
                        if (formSubmit(uniqueReservableKey, it)) {
                            (document.getElementById("submitform_${uniqueReservableKey.id}")!! as HTMLFormElement).reset()
                        }
                        it.preventDefault()
                        it.stopPropagation()
                    }
                    input(type = InputType.text, name = "field1")
                    br()
                    input(type = InputType.text, name = "field2")
                    br()
                    input(type = InputType.checkBox, name = "field3")
                    br()

                    submitInput { }
                }
        )
    }

    fun formSubmit(key: UniqueReservableKey, formEvent: Event): Boolean {
        println("formSubmit is checked: ")
        console.log(formEvent)
        return true //TODO: implement checking of form data, and AJAX post if successful
    }

    fun addDatePickers(formDiv: Element) {
        formDiv.appendChild(
                document.create.form(action = null, encType = null,
                        method = null) {
                    onSubmitFunction = {
                        selectedKey?.let { it1 -> loadEntriesToKey(it1, from, to) }
                        it.preventDefault()
                        it.stopPropagation()
                    }
                    label {
                        +"from:"
                    }
                    input(type = InputType.date, name = "from-picker") {
                        value = from.toDateShort()
                        onChangeFunction = {
                            from = Date(this.value)
                        }
                    }
                    br()
                    label {
                        +"to:"
                    }
                    input(type = InputType.date, name = "to-picker") {
                        value = to.toDateShort()
                        println("to.toUTCString() = " + to.toDateShort())
                        onChangeFunction = {
                            to = Date(this.value)
                        }
                    }
                    button {
                        type = ButtonType.submit
                        +"Change Filter"
                    }
                }
        )
    }


    fun Date.toDateShort(): String = this.getFullYear().toString() + "-" + ("0" + (this.getMonth() + 1)).takeLast(2) + "-" + ("0" + this.getDate()).takeLast(2)


    fun setURLParameters(params: Map<String, String>) {
        val frontstr = window.location.href.takeWhile { it != '?' }
        val paramstr: String = params.toList().map {
            encodeURIComponent(it.first) + "=" + encodeURIComponent(it.second)
        }.joinToString("&")

        window.history.pushState(null, "Krese Redirect", frontstr + if (paramstr.isBlank()) "" else "?" + paramstr)
        //causes reloads: window.location.href =
    }


    fun addURLParameter(key: String, value: String) {
        setURLParameters(getURLParameters() + (key to value))
    }

    fun unsetURLParameter(key: String) {
        setURLParameters(getURLParameters() - key)
    }

    fun getURLParameters(): Map<String, String> {
        var paramstr = window.location.href.dropWhile { it != '?' }
        if (paramstr.startsWith("?")) {
            paramstr = paramstr.substring(1)
        }

        return paramstr.split("&").filter { it.isNotBlank() }.map { kvpair ->
            val arr = kvpair.split("=")
            require(arr.size == 2)
            val key = decodeURIComponent(arr[0])
            val value = decodeURIComponent(arr[1])

            key to value
        }.toMap()
    }


}