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
import kotlin.math.roundToLong


external fun encodeURIComponent(str: String): String

external fun decodeURIComponent(str: String): String

val LOCALSTORAGE_KRESE_LOGIN_JWT_KEY = "LOCALSTORAGE_KRESE_LOGIN_JWT_KEY"


//TODO: fix bug of calendar disappearing while browsing tabs


class ClientState {
    private val creationDate = Date.now()
    var jwt: String? = localStorage.get(LOCALSTORAGE_KRESE_LOGIN_JWT_KEY)


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
        if (localStorage.get(LOCALSTORAGE_KRESE_MY_EMAIL) == null && jwt != null) {
            localStorage.set(LOCALSTORAGE_KRESE_MY_EMAIL, decodeJWT(jwt!!).email)
        }

        testForReceiveRelogin()
        loadAllKeys()

        writeLoginStatus(if (jwt != null && jwt!!.isNotBlank()) JWTStatus.PENDING else JWTStatus.UNCHECKED)
        window.setInterval(handler = requestJWTValidation(), timeout = 1000 * 60 * 5)
        window.setInterval(handler = checkJWT(), timeout = 1000 * 60 * 1)

    }

    private fun checkJWT() {
        if (localStorage.get(LOCALSTORAGE_KRESE_LOGIN_JWT_KEY) != jwt) {
            window.location.reload()
        }
    }

    fun currentEmail(): Email? {
        val e = localStorage.get(LOCALSTORAGE_KRESE_MY_EMAIL)
        if (e != null && e.isNotBlank()) {
            return Email(e)
        } else {
            return null
        }
    }

    fun isModerator(reservation: Reservation): Boolean = currentEmail() != null && entries.get(reservation.key)!!.reservable.operatorEmails.contains(currentEmail()!!.address)

    fun isCreator(reservation: Reservation): Boolean = currentEmail() != null && reservation.email != null && reservation.email!!.equals(currentEmail()!!.address)

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
                    onClickFunction = { relogin() }
                    span {
                        classes = jwtState.glyphname.split(" ").toSet()
                        style = "padding:5px;"
                    }
                    +"Login via Email ${localStorage.get(LOCALSTORAGE_KRESE_MY_EMAIL)}"
                }
                JWTStatus.PENDING -> button {
                    classes = setOf("btn", jwtState.buttonStyle)
                    onClickFunction = { logout() }
                    span {
                        classes = jwtState.glyphname.split(" ").toSet()
                        style = "padding:5px;"
                    }
                    +"Logged in as ${localStorage.get(LOCALSTORAGE_KRESE_MY_EMAIL)} | Pending verification..."
                }
                JWTStatus.VALID -> button {
                    classes = setOf("btn", jwtState.buttonStyle)
                    onClickFunction = { logout() }
                    span {
                        classes = jwtState.glyphname.split(" ").toSet()
                        style = "padding:5px;"
                    }
                    +"Successfully logged in as ${localStorage.get(LOCALSTORAGE_KRESE_MY_EMAIL)} | click to logout"
                }
                JWTStatus.INVALID -> button {
                    classes = setOf("btn", jwtState.buttonStyle)
                    onClickFunction = { logout() }
                    span {
                        classes = jwtState.glyphname.split(" ").toSet()
                        style = "padding:5px;"
                    }
                    +"Invalid Credentials for ${localStorage.get(LOCALSTORAGE_KRESE_MY_EMAIL)}, please logout"
                }
            }
        }
    }

    fun executeAccept(id: Long) {
        ServerPost(AcceptAction(id, ""), jwt).asyncCall {
            //process result in callback
            if (it.successful) {
                if (it.finished) {
                    window.alert("Successfully accepted reservation, message from server = '${it.message}'")
                    window.location.reload()
                } else {
                    window.alert("Acceptance of Reservation requires reauthentication. Please check your email. Message from server = '${it.message}'")
                }
            } else {
                window.alert("Error while trying to accept reservation, message from server: '${it.message}'")
            }
        }
    }

    fun executeDecline(id: Long) {
        ServerPost(DeclineAction(id, ""), jwt).asyncCall {
            //process result in callback
            if (it.successful) {
                if (it.finished) {
                    window.alert("Successfully declined reservation, message from server = '${it.message}'")
                    window.location.reload()
                } else {
                    window.alert("Decline of Reservation requires reauthentication. Please check your email. Message from server = '${it.message}'")
                }
            } else {
                window.alert("Error while trying to decline reservation, message from server: '${it.message}'")
            }
        }
    }

    fun executeWithdraw(id: Long) {
        ServerPost(WithdrawAction(id, ""), jwt).asyncCall {
            //process result in callback
            if (it.successful) {
                if (it.finished) {
                    window.alert("Successfully withdrawn reservation, message from server = '${it.message}'")
                    window.location.reload()
                } else {
                    window.alert("Withdrawal of Reservation requires reauthentication. Please check your email. Message from server = '${it.message}'")
                }
            } else {
                window.alert("Error while trying to withdraw reservation, message from server: '${it.message}'")
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

            val xhttp = XMLHttpRequest()
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
                    writeLoginStatus(if (xhttp.responseText.equals(true.toString(), true)) JWTStatus.VALID else JWTStatus.INVALID)
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

        //TODO: check for action to commit
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

    fun decodeJWT(jwt: String): dynamic {
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

        val allheaders: HTMLCollection = document.getElementsByClassName("tab_header_button")

        0.until(allheaders.length).map {
            val k = allheaders.get(it)!! as HTMLLIElement
            k.removeClass("active")
            if (k.id.equals("tab_header_${uniqueReservableKey.id}")) {
                k.addClass("active")
            }
        }


        val x: HTMLCollection = document.getElementsByClassName("reservable-tab")

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
        return document.create.li {
            id = "tab_header_${key.id}"
            classes = setOf("tab_header_button")
            a {

                classes = setOf("")
                onClickFunction = {
                    addURLParameter(SELECTED_KEY_URL_KEY, key.id)
                    switchTo(key)
                }
                +key.id
            }
        }
    }

    private fun transformKeyIntoDiv(key: UniqueReservableKey): HTMLElement {

        return document.create.div {
            id = "div_" + key.id
            style = "display:none"
            classes = setOf("container", "reservable-tab")

            h2 {
                id = "title_header_${key.id}"
                +key.id
            }

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
        document.getElementById("title_header_${uniqueReservableKey.id}")!!.innerHTML = entries.get(uniqueReservableKey)!!.reservable.title
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

        val collapseId = "collapsedList_${uniqueReservableKey.id}"

        val el = document.create.div {
            unsafe {
                +"""<a class="btn btn-primary" data-toggle="collapse" href="#$collapseId" role="button" aria-expanded="false" aria-controls="$collapseId">
    Show list
  </a>"""
            }

            div {
                id = collapseId
                classes = setOf("collapse")
                div {
                    classes = setOf("card", "card-body")
                    div {
                        classes = setOf("table-responsive")
                        table {
                            classes = setOf("table", "table-striped")
                            thead {
                                tr {
                                    th {
                                        +"From"
                                    }
                                    th {
                                        +"To"
                                    }
                                    th {
                                        +"Name"
                                    }
                                    th {
                                        +"Elements"
                                    }
                                    th {
                                        +"Comments"
                                    }
                                    th {
                                        +"Actions"
                                    }
                                }
                            }
                            tbody {

                                for (reservation in entries.get(uniqueReservableKey)!!.existingReservations) {
                                    tr {
                                        td {
                                            +reservation.startTime.toDate().toLocalizedDateShort()
                                        }
                                        td {
                                            +reservation.endTime.toDate().toLocalizedDateShort()
                                        }
                                        td {
                                            +reservation.name
                                        }
                                        td {
                                            +reservation.toBlockTableCellString()
                                        }
                                        td {
                                            ActionButtons(reservation)
                                        }
                                        td {
                                            +reservation.commentUser
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }


        listDiv.innerHTML = ""
        listDiv.appendChild(el)
    }

    fun Long.toDate(): Date {
        return Date(this)
    }

    fun Date.toLocalizedDateShort(): String {
        val year = this.getFullYear().toString().takeLast(2)
        val month = ("00" + (this.getMonth() + 1).toString()).takeLast(2)
        val day = ("00" + (this.getDate().toString())).takeLast(2)
        return "$day.$month.$year"
    }

    fun TD.ActionButtons(reservation: Reservation): Unit {
        if (isCreator(reservation)) {
            button {
                //Withdraw
                onClickFunction = {
                    executeWithdraw(reservation.id)
                }
                classes = setOf("btn", "btn-danger")
                span {
                    classes = setOf("glyphicon", "glyphicon-trash")
                }

            }
        }
        if (isModerator(reservation)) {
            button {
                //accept
                onClickFunction = {
                    executeAccept(reservation.id)
                }
                classes = setOf("btn", "btn-success")
                span {
                    classes = setOf("glyphicon", "glyphicon-ok")
                }
            }
            button {
                //decline
                onClickFunction = {
                    executeDecline(reservation.id)
                }
                classes = setOf("btn", "btn-danger")
                span {
                    classes = setOf("glyphicon", "glyphicon-remove")
                }
            }
        }
    }


    fun setReservationCalendar(calDiv: Element, uniqueReservableKey: UniqueReservableKey) {
        val config = entries.get(uniqueReservableKey)!!.toCalendarConfig()
        val configJson = JSON.stringify(config)
        calDiv.innerHTML = ""
        val id = "#" + calDiv.id
        jq(id).asDynamic().fullCalendar(js("JSON.parse(configJson)"))
    }

    fun DIV.toFormularInputDiv(element: ReservableElement, key: UniqueReservableKey, prefix: String): Unit {
        val r = element

        //TODO: buggy, does not really work

        console.log("creating $this with $key and $prefix")


        label { +"${r.name}: ${r.description}" }
        if (r.units != 0) {
            if (r.units != 1) {
                label {
                    +"Number:"
                }
                input {
                    classes = setOf("form-control")
                    type = InputType.number
                    name = "count_input_${key.id}_${prefix}"
                    step = 1.toString()
                    min = 1.toString()
                    max = r.units.toString()
                    value = 1.toString()
                }
            } else {
                input {
                    classes = setOf("form-control")
                    type = InputType.checkBox
                    name = "check_input_${key.id}_${prefix}"
                    checked = prefix.equals("ROOT") && r.subElements.isEmpty()
                    disabled = prefix.equals("ROOT") && r.subElements.isEmpty()
                }
                label { +"Reserve this element" }
            }
        }
        div {
            classes = setOf("form-group col-lg-6")
            for (@Suppress("NAME_SHADOWING") element in r.subElements) {
                this.toFormularInputDiv(element, key, prefix + "_" + r.id)
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
                    div {
                        classes = setOf("form-group col-lg-6")
                        label {
                            +"Name"
                        }
                        input(type = InputType.text, name = FormFieldNames.Name.name, classes = "form-control")
                    }


                    div {
                        classes = setOf("form-group col-lg-6")
                        label {
                            +"Email"
                        }
                        input(type = InputType.email, name = FormFieldNames.Email.name, classes = "form-control")
                    }


                    div {
                        classes = setOf("form-group col-lg-6")
                        label {
                            +"Phone"
                        }
                        input(type = InputType.tel, name = FormFieldNames.Telephone.name, classes = "form-control")
                    }

                    div {
                        classes = setOf("form-group col-lg-6")
                        label {
                            +"From"
                        }
                        input(type = InputType.date, name = FormFieldNames.From.name, classes = "form-control") {
                            value = Date(creationDate).nextFullDay().toDateShort()
                        }
                    }

                    div {
                        classes = setOf("form-group col-lg-6")
                        label {
                            +"To"
                        }
                        input(type = InputType.date, name = FormFieldNames.To.name, classes = "form-control") {
                            value = Date(creationDate).nextFullDay().nextFullDay().toDateShort()
                        }
                    }


                    div {
                        classes = setOf("form-group col-lg-6")
                        label {
                            +"Reserved Elements"
                        }
                        div {
                            classes = setOf("form-group col-lg-6")
                            toFormularInputDiv(r.elements, uniqueReservableKey, "ROOT")
                        }
                    }

                    div {
                        classes = setOf("form-group col-lg-6")
                        label { +"Comment" }
                        textArea {
                            name = FormFieldNames.Comment.name
                            classes = setOf("form-control")
                        }
                    }

                    div {
                        if (r.checkBoxes.isNotEmpty())
                            r.checkBoxes.map {
                                div {
                                    input(type = InputType.checkBox, name = "checkbox_${it.hashCode()}")
                                    label { +it }
                                }
                            }
                    }
                    div {
                        submitInput { }
                    }
                }
        )
    }

    fun formSubmit(uniqueReservableKey: UniqueReservableKey, formEvent: Event): Boolean {
        formEvent.preventDefault()
        formEvent.stopPropagation()
        parseFormularToData(uniqueReservableKey)
        return true
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
    fun parseFormularToData(uniqueReservableKey: UniqueReservableKey) {
        //collect all form data
        val id = "#submitform_${uniqueReservableKey.id}"
        val formURLString: String = jq(id).asDynamic().serialize()
        val fields = formURLString.toNamedMap()
        //post as action
        ServerPost(fields.toCreateAction(uniqueReservableKey), jwt).asyncCall {
            //process result in callback
            if (it.successful) {
                if (it.finished) {
                    window.alert("Successfully created reservation, message from server = '${it.message}', please wait for confirmation by the acting moderator")
                    window.location.reload()
                } else {
                    window.alert("Reservation was posted but not finished. Please check your email. Message from server = '${it.message}'")
                }
            } else {
                window.alert("Error while trying to post new reservation, message from server: '${it.message}'")
            }
        }
    }


    //TODO: withdraw accept decline only shown if jwt is set, valid, and is possible (compare email vs moderator and vs creator)


    //TODO: add small buttons for withdraw/accept/decline, with confirm for each

}

private fun Date.nextFullDay(): Date {
    val d: Date = Date(this.getTime() + (1000L * 60 * 60 * 24))
    val r = Date(d.getFullYear(), d.getMonth(), d.getDate(), 12, 0)
    println("NextFullDate from ${this.toISOString()} to ${d.toISOString()} and ${r.toISOString()}")
    return r
}


fun String.toNamedMap(): Map<String, String> {
    return this.split("&").map {
        val arr = it.split("=")
        decodeURIComponent(arr.get(0)) to decodeURIComponent(arr.get(1))
    }.toMap()
}

fun Map<String, String>.toCreateAction(uniqueReservableKey: UniqueReservableKey): CreateAction {
    console.log("Building CreateAction:")
    console.log(uniqueReservableKey)
    this.forEach {
        println("${it.key} = ${it.value}")
    }
    val email = Email(this.get(FormFieldNames.Email.name)!!)
    val name = this.get(FormFieldNames.Name.name)!!
    val phone = this.get(FormFieldNames.Telephone.name)!!
    val from = this.get(FormFieldNames.From.name)!!.dateStringToMillis()
    val to = this.get(FormFieldNames.To.name)!!.dateStringToMillis()
    val comment = this.get(FormFieldNames.Email.name)!!
    val blocks = listOf<DbBlockData>() //TODO("implement extracting the blocks")
    val ca = CreateAction(uniqueReservableKey, email, name, phone, comment, from, to, blocks)
    console.log("CreateAction:")
    console.log(ca)
    return ca
}

fun String.dateStringToMillis(): Long = Date(Date.parse(this)).getTime().roundToLong()