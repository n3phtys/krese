package krese

import jquery.jq
import kotlinext.js.asJsObject
import kotlinx.html.dom.create
import kotlinx.serialization.json.JSON
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
import krese.data.*
import org.w3c.dom.*
import org.w3c.dom.events.Event
import kotlin.browser.document
import kotlin.js.Date


external fun encodeURIComponent(str: String): String

external fun decodeURIComponent(str: String): String

val LOCALSTORAGE_KRESE_LOGIN_JWT_KEY = "LOCALSTORAGE_KRESE_LOGIN_JWT_KEY"




//TODO: - finish form generation
//TODO: - make bootstrap forms

//TODO: - collapse list of entries per default
//TODO: - format List as responsive table

//TODO: - add internationalization with override possibility
//TODO: - set next day to the day after as default form timespan

//TODO: - action commands via client sided indirection (get from url, show confirm dialog for action, and send to post endpoint on server)
//TODO: - form post submit
//TODO: - action submit




class ClientState {
    private val creationDate = Date.now()
    var jwt: String? = localStorage.get(LOCALSTORAGE_KRESE_LOGIN_JWT_KEY)

    init {
        println("creationDate =" + Date(creationDate))
        if (localStorage.get(LOCALSTORAGE_KRESE_MY_EMAIL) == null && jwt != null) {
            localStorage.set(LOCALSTORAGE_KRESE_MY_EMAIL, decodeJWT(jwt!!).email)
        }
    }

    var from = Date(creationDate - 1000L * 60 * 60 * 24 * 180)
    var to = Date(creationDate + 1000L * 60 * 60 * 24 * 180)

    var selectedKey: UniqueReservableKey? = null

    var jwtState: JWTStatus = JWTStatus.UNCHECKED

    var allKeys: List<UniqueReservableKey> = listOf()
    val entries: MutableMap<UniqueReservableKey, GetResponse> = mutableMapOf()


    val navbar = document.getElementById("key-tab-nav-bar")!!
    val tabcontainer = document.getElementById("key-tab-container")!!

    val loginStatusDiv = document.getElementById("login_status_div")!!

    val SELECTED_KEY_URL_KEY = "selected_key"

    init {
        testForReceiveRelogin()
        loadAllKeys()

        writeLoginStatus(if (jwt != null && jwt!!.isNotBlank()) JWTStatus.PENDING else JWTStatus.UNCHECKED)
        window.setInterval(handler = requestJWTValidation(), timeout = 1000 * 60 * 5)

    }

    fun writeLoginStatus(status: JWTStatus) {
        jwtState = status
        updateJWTButton()
    }

    fun updateJWTButton() {
        loginStatusDiv.innerHTML = ""
        loginStatusDiv.append {
            when (jwtState) {
                JWTStatus.UNCHECKED -> button {
                    classes = setOf("btn", jwtState.buttonStyle)
                    onClickFunction = {relogin()}
                    span {
                        classes = jwtState.glyphname.split(" ").toSet()
                        style = "padding:5px;"
                    }
                        +"Login via Email ${localStorage.get(LOCALSTORAGE_KRESE_MY_EMAIL)}"
                }
                JWTStatus.PENDING -> button {
                    classes = setOf("btn", jwtState.buttonStyle)
                    onClickFunction = {logout()}
                    span {
                        classes = jwtState.glyphname.split(" ").toSet()
                        style = "padding:5px;"
                    }
                    +"Logged in as ${localStorage.get(LOCALSTORAGE_KRESE_MY_EMAIL)} | Pending verification..."
                }
                JWTStatus.VALID -> button {
                    classes = setOf("btn", jwtState.buttonStyle)
                    onClickFunction = {logout()}
                    span {
                        classes = jwtState.glyphname.split(" ").toSet()
                        style = "padding:5px;"
                    }
                    +"Successfully logged in as ${localStorage.get(LOCALSTORAGE_KRESE_MY_EMAIL)} | click to logout"
                }
                JWTStatus.INVALID -> button {
                    classes = setOf("btn", jwtState.buttonStyle)
                    onClickFunction = {logout()}
                    span {
                        classes = jwtState.glyphname.split(" ").toSet()
                        style = "padding:5px;"
                    }
                    +"Invalid Credentials for ${localStorage.get(LOCALSTORAGE_KRESE_MY_EMAIL)}, please logout"
                }
            }
        }
    }

    fun relogin() {
        val oldemail = localStorage.get(LOCALSTORAGE_KRESE_MY_EMAIL)
        val result = if (oldemail != null) {
            window.prompt("Enter your email address to proceed", oldemail)
        } else {
            window.prompt("Enter your email address to proceed")
        }

        if (result != null && result.isNotBlank()) {

            val xhttp = XMLHttpRequest();
            xhttp.open("POST", Routes.POST_RELOGIN.path, true)
            xhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded")
            xhttp.onreadystatechange = {
                println("Received something... state = ${xhttp.readyState} and status = ${xhttp.status}")
                if (xhttp.readyState == 4.toShort() && xhttp.status == 200.toShort()) {
                    window.alert("We have sent you an email. Please open it and follow the link to log in. You should close this Tab now.")
                }
            }
            xhttp.send("email=$result")
        }
    }

    fun logout() {
        this.storePassword(null)
        updateJWTButton()
    }



    fun requestJWTValidation() {
        val jwt = jwt
        if (jwt != null) {
            writeLoginStatus(JWTStatus.PENDING)
            val xhttp = XMLHttpRequest();
            xhttp.open("POST", "util/valid/credentials", true)
            xhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded")
            xhttp.onreadystatechange = {
                println("Received something... state = ${xhttp.readyState} and status = ${xhttp.status}")
                if (xhttp.readyState == 4.toShort() && xhttp.status == 200.toShort()) {
                    writeLoginStatus(if(xhttp.responseText.equals(true.toString(), true)) JWTStatus.VALID else JWTStatus.INVALID)
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

    fun storePassword(jwt: String?) {
        if (jwt != null) {
            console.log("STORING NEWER JWT CREDENTIALS: $jwt")
            localStorage.set(LOCALSTORAGE_KRESE_LOGIN_JWT_KEY, jwt)
        } else {
            localStorage.removeItem(LOCALSTORAGE_KRESE_LOGIN_JWT_KEY)
        }
        this.jwt = jwt

        writeLoginStatus(JWTStatus.UNCHECKED)
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
        xhttp.open("GET", Routes.GET_RESERVABLES.path + if (jwt != null) "?jwt=$jwt" else "", true)
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
            +key.id
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

            h2 { +key.id }

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
        xhttp.open("GET", Routes.GET_RESERVABLES.path + "?key=${uniqueReservableKey.id}&from=${from.getTime()}&to=${to.getTime()}${if (jwt != null) "&jwt=$jwt" else ""}", true)
        xhttp.onreadystatechange = {
            if (xhttp.readyState == 4.toShort() && xhttp.status == 200.toShort()) {
                entries.put(uniqueReservableKey, JSON.parse<GetResponse>(xhttp.responseText))
                setEntriesToKey(uniqueReservableKey)
            }
        }
        xhttp.send()
    }

    fun setEntriesToKey(uniqueReservableKey: UniqueReservableKey) {
        document.getElementById("pro_" + uniqueReservableKey.id)!!.innerHTML = entries.get(uniqueReservableKey)!!.reservable.prologue
        val epilogue = entries.get(uniqueReservableKey)!!.reservable.epilogue
        document.getElementById("epi_" + uniqueReservableKey.id)!!.innerHTML = epilogue
        addFormular(document.getElementById("for_" + uniqueReservableKey.id)!!, uniqueReservableKey)
        setReservationList(document.getElementById("lis_" + uniqueReservableKey.id)!!, uniqueReservableKey)
        setReservationCalendar(document.getElementById("cal_" + uniqueReservableKey.id)!!, uniqueReservableKey)
    }

    fun setReservationList(listDiv: Element, uniqueReservableKey: UniqueReservableKey) {

        /*
        <div class="panel-group">
  <div class="panel panel-default">
    <div class="panel-heading">
      <h4 class="panel-title">
        <a data-toggle="collapse" href="#collapse1">Collapsible panel</a>
      </h4>
    </div>
    <div id="collapse1" class="panel-collapse collapse">
      <div class="panel-body">Panel Body</div>
      <div class="panel-footer">Panel Footer</div>
    </div>
  </div>
</div>
         */



        val el = document.create.ol {
            for (reservation in entries.get(uniqueReservableKey)!!.existingReservations) {
                li {
                +("Name: ${reservation.name} From: ${reservation.startTime} To: ${reservation.endTime} for blocks: ${reservation.blocks.map { it.elementPath.map{it.toString() }.joinToString("-") + " (${it.usedNumber} times)"}}")
            } }
        }
        listDiv.innerHTML = ""
        listDiv.appendChild(el)
    }

    fun setReservationCalendar(calDiv: Element, uniqueReservableKey: UniqueReservableKey) {
        val config = entries.get(uniqueReservableKey)!!.toCalendarConfig()
        val configJson = JSON.stringify(config)
        calDiv.innerHTML = ""
        val id = "#" + calDiv.id
        jq( id).asDynamic().fullCalendar(js("JSON.parse(configJson)"))
    }

    fun ReservableElement.toFormularInputDiv(key: UniqueReservableKey, prefix : String) : HTMLDivElement {
        val r = this

        //TODO: buggy, does not really work

        return document.create.div {
            label { +"${r.name}: ${r.description}"}
            if (r.units != 0) {
                label {
                    +"Number:"
                }
                input {
                    type = InputType.number
                    name = "count_input_${key.id}_${prefix}"
                    step = 1.toString()
                    min = 1.toString()
                    max = r.units.toString()
                }
            } else {
                input {
                    type = InputType.checkBox
                    name = "check_input_${key.id}_${prefix}"
                }
                label {+"Reserve this element"}
            }
            for (element in r.subElements) {
                element.toFormularInputDiv(key, prefix + "_" + r.id)
            }

        }
    }

    fun addFormular(formDiv: Element, uniqueReservableKey: UniqueReservableKey) {
        formDiv.innerHTML = ""
        //TODO: add automatic disabling of submit button when intersect happens

        //TODO: store name + email + phone in localStorage and extract in this method as default value


        //name
        //email
        //phone
        //from
        //to

        //ReservableElement -> form (may contain multiple, id required for prefixing data)


        //comment
        //checkboxes
        //submit button

        //onsubmit abort immediate post and instead build CreateAction object

        val r = entries.get(uniqueReservableKey)!!.reservable

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
                    input(type = InputType.text, name = "name")

                    input(type = InputType.email, name = "email")

                    input(type = InputType.tel, name = "telephone")

                    input(type = InputType.date, name = "from")

                    input(type = InputType.date, name = "to")


                    r.elements.toFormularInputDiv(uniqueReservableKey, "ROOT")

                    label {+"Comment"}
                    textArea { name = "comment_${uniqueReservableKey.id}" }

                    div {
                        if (r.checkBoxes.isNotEmpty())
                        r.checkBoxes.map { div {
                            input(type = InputType.checkBox, name = "checkbox_${uniqueReservableKey.id}_${it.hashCode()}")
                            label {+it}
                        } }
                    }

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


    //used to build a CreateAction, which can be posted afterwards
    fun parseFormularToData(key: UniqueReservableKey) : CreateAction {
        TODO("not yet implemented")
    }

    //TODO: add small buttons for withdraw/accept/decline, with confirm for each

}