package krese

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


class ClientState {
    private val creationDate = Date.now()

    init {
        println("creationDate =" + Date(creationDate))
    }

    var from = Date(creationDate - 1000L * 60 * 60 * 24 * 180)
    var to = Date(creationDate + 1000L * 60 * 60 * 24 * 180)

    var selectedKey: UniqueReservableKey? = null
    var jwt: String? = null
    var allKeys: List<UniqueReservableKey> = listOf()
    val entries: MutableMap<UniqueReservableKey, GetResponse> = mutableMapOf()


    val navbar = document.getElementById("key-tab-nav-bar")!!
    val tabcontainer = document.getElementById("key-tab-container")!!

    val SELECTED_KEY_URL_KEY = "selected_key"

    init {
        loadAllKeys()
        //setURLParameters(mapOf("myparam1" to "value1", "myparam2" to "value2"))
        //println("Params:")
        //val params = getURLParameters()
        //params.forEach { println("${it.key} = ${it.value}") }

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
        println("setting all entries to key ${uniqueReservableKey.id}")
        document.getElementById("pro_" + uniqueReservableKey.id)!!.innerHTML = entries.get(uniqueReservableKey)!!.reservable.prologueMarkdown
        document.getElementById("epi_" + uniqueReservableKey.id)!!.innerHTML = entries.get(uniqueReservableKey)!!.reservable.epilogueMarkdown
        document.getElementById("lis_" + uniqueReservableKey.id)!!.innerHTML = (entries.get(uniqueReservableKey)!!.existingReservations).toString()
        addFormular(document.getElementById("for_" + uniqueReservableKey.id)!!, uniqueReservableKey)

    }

    fun addFormular(formDiv: Element, uniqueReservableKey: UniqueReservableKey) {
        formDiv.innerHTML = ""
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