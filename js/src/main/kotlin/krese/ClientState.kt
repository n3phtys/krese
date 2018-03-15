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
import org.w3c.dom.*
import kotlin.browser.document
import kotlin.js.Date

class ClientState {
    private val creationDate = Date().getMilliseconds()
    var from = Date(creationDate - 1000L * 60 * 60 * 24 * 180)
    var to = Date(creationDate + 1000L * 60 * 60 * 24 * 180)

    var selectedKey: UniqueReservableKey? = null
    var jwt: String? = null
    var allKeys: List<UniqueReservableKey> = listOf()
    val entries : MutableMap<UniqueReservableKey, GetResponse> = mutableMapOf()



    val navbar = document.getElementById("key-tab-nav-bar")!!
    val tabcontainer = document.getElementById("key-tab-container")!!

    init {
        loadAllKeys()
    }

    fun parseURLParameters() {

    }

    fun switchTo(uniqueReservableKey: UniqueReservableKey) {
        val x : HTMLCollection = document.getElementsByClassName("city")

        selectedKey = uniqueReservableKey

        loadEntriesToKey(selectedKey!!, from, to)

        0.until(x.length).map {
            val k : HTMLDivElement = x.get(it)!! as HTMLDivElement
            k.style.display = "none"
        }
        (document.getElementById("div_"+uniqueReservableKey.id)!! as HTMLDivElement).style.display = "block";
    }

    private fun loadAllKeys() {
        val xhttp = XMLHttpRequest();
        xhttp.open("GET", Routes.GET_RESERVABLES.path, true)
        xhttp.onreadystatechange = {
            if (xhttp.readyState == 4.toShort() && xhttp.status == 200.toShort()) {
                allKeys = JSON.parse<GetTotalResponse>(xhttp.responseText).keys
                setAllKeysToGUI()
            }
        }
        xhttp.send()
    }

    private fun transformKeyIntoButton(key: UniqueReservableKey) : HTMLElement {
        return document.create.button {
            classes = setOf("w3-bar-item", "w3-button")
            onClickFunction = { switchTo(key) }
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
            id = "div_"+key.id
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
        document.getElementById("lis_" + uniqueReservableKey.id)!!.innerHTML = JSON.stringify( entries.get(uniqueReservableKey)!!.existingReservations)


        //TODO: add data to skeleton
    }
}