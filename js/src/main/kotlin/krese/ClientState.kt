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
import org.w3c.dom.HTMLElement
import kotlin.browser.document
import kotlin.js.Date

class ClientState {
    var selectedKey: UniqueReservableKey? = null
    var jwt: String? = null
    var allKeys: List<UniqueReservableKey> = listOf()
    val entries : MutableMap<UniqueReservableKey, GetResponse> = mutableMapOf()



    val navbar = document.getElementById("key-tab-nav-bar")!!
    val tabcontainer = document.getElementById("key-tab-container")

    init {
        loadAllKeys()
    }

    fun parseURLParameters() {

    }

    fun switchTo(uniqueReservableKey: UniqueReservableKey) {
        println("switching to key ${uniqueReservableKey.id}")
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

    private fun setAllKeysToGUI() {
        println("setting all keys to gui")
        navbar.innerHTML = ""
        allKeys.sortedBy { it.id }.forEach {
            navbar.appendChild(transformKeyIntoButton(it))
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
    }
}