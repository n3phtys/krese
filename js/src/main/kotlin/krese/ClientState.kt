package krese

import jquery.jq
import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.html.dom.create
import kotlinx.html.js.*
import kotlinx.serialization.json.JSON
import krese.data.*
import org.w3c.dom.*
import org.w3c.dom.events.Event
import org.w3c.dom.parsing.DOMParser
import org.w3c.xhr.XMLHttpRequest
import kotlin.browser.document
import kotlin.browser.localStorage
import kotlin.browser.window
import kotlin.dom.addClass
import kotlin.dom.removeClass
import kotlin.js.Date
import kotlin.math.roundToLong


external fun encodeURIComponent(str: String): String

external fun decodeURIComponent(str: String): String

val LOCALSTORAGE_KRESE_LOGIN_JWT_KEY = "LOCALSTORAGE_KRESE_LOGIN_JWT_KEY"

val domparser = DOMParser()

class ClientState {
    private val creationDate = Date.now()
    var jwt: String? = localStorage.get(LOCALSTORAGE_KRESE_LOGIN_JWT_KEY)


    var from = Date(creationDate - 1000L * 60 * 60 * 24 * 180)
    var to = Date(creationDate + 1000L * 60 * 60 * 24 * 180)

    var selectedKey: UniqueReservableKey? = null

    var jwtState: JWTStatus = JWTStatus.UNCHECKED

    var allKeys: List<UniqueReservableKey> = listOf()
    val reservables: MutableMap<UniqueReservableKey, Reservable> = mutableMapOf()
    val reservations: MutableMap<UniqueReservableKey, GetResponse> = mutableMapOf()


    val navbar = document.getElementById("key-tab-nav-bar")!!
    val tabcontainer = document.getElementById("key-tab-container")!!

    val loginStatusDiv = document.getElementById("login_status_div")!!

    val SELECTED_KEY_URL_KEY = "selected_key"


    init {
        document.title = "frontend.title".localize()
        document.getElementById("krese_header")!!.innerHTML = "frontend.header".localize()


        if (localStorage.get(LOCALSTORAGE_KRESE_MY_EMAIL) == null && jwt != null) {
            localStorage.set(LOCALSTORAGE_KRESE_MY_EMAIL, decodeJWT(jwt!!).email)
        }

        testForReceiveAction()
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

    fun isModerator(reservation: Reservation): Boolean = jwt != null && currentEmail() != null && reservables.get(reservation.key)!!.operatorEmails.contains(currentEmail()!!.address)

    fun isCreator(reservation: Reservation): Boolean = jwt != null && currentEmail() != null && reservation.email != null && reservation.email!!.equals(currentEmail()!!.address)

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
                    style = "white-space: normal;"
                    onClickFunction = { relogin() }
                    span {
                        classes = jwtState.glyphname.split(" ").toSet()
                        style = "padding:5px;"
                    }
                    +"${"frontend.loginbtn.fresh".localize()} ${if (localStorage.get(LOCALSTORAGE_KRESE_MY_EMAIL) != null) localStorage.get(LOCALSTORAGE_KRESE_MY_EMAIL) else ""}"
                }
                JWTStatus.PENDING -> button {
                    classes = setOf("btn", jwtState.buttonStyle)
                    style = "white-space: normal;"
                    onClickFunction = { logout() }
                    span {
                        classes = jwtState.glyphname.split(" ").toSet()
                        style = "padding:5px;"
                    }
                    +"${"frontend.loginbtn.pending".localize()} ${localStorage.get(LOCALSTORAGE_KRESE_MY_EMAIL)} | ${"pending.verification".localize()}"
                }
                JWTStatus.VALID -> button {
                    classes = setOf("btn", jwtState.buttonStyle)
                    style = "white-space: normal;"
                    onClickFunction = { logout() }
                    span {
                        classes = jwtState.glyphname.split(" ").toSet()
                        style = "padding:5px;"
                    }
                    +"${"frontend.loginbtn.success".localize()} ${localStorage.get(LOCALSTORAGE_KRESE_MY_EMAIL)} | ${"click.to.logout".localize()}"
                }
                JWTStatus.INVALID -> button {
                    classes = setOf("btn", jwtState.buttonStyle)
                    style = "white-space: normal;"
                    onClickFunction = { logout() }
                    span {
                        classes = jwtState.glyphname.split(" ").toSet()
                        style = "padding:5px;"
                    }
                    +"${"frontend.loginbtn.invalid".localize()} ${localStorage.get(LOCALSTORAGE_KRESE_MY_EMAIL)}, ${"please.log.out".localize()}"
                }
            }
        }
    }

    fun executeAccept(id: Long) {
        if (window.confirm("${"frontend.confirm.accept".localize()} (ID #$id)?")) {
            ServerPost(AcceptAction(id, ""), jwt).asyncCall {
                //process result in callback
                if (it.successful) {
                    if (it.finished) {
                        window.alert("${"frontend.success.accept".localize()} '${it.message}'")
                        window.location.reload()
                    } else {
                        window.alert("${"frontend.uncomplete.accept".localize()} '${it.message}'")
                    }
                } else {
                    window.alert("${"frontend.error.accept".localize()} '${it.message}'")
                }
            }
        }
    }

    fun executeDecline(id: Long) {
        if (window.confirm("${"frontend.confirm.decline".localize()} (ID$id)?")) {
            ServerPost(DeclineAction(id, ""), jwt).asyncCall {
                //process result in callback
                if (it.successful) {
                    if (it.finished) {
                        window.alert("${"frontend.success.decline".localize()} '${it.message}'")
                        window.location.reload()
                    } else {
                        window.alert("${"frontend.uncomplete.decline".localize()} '${it.message}'")
                    }
                } else {
                    window.alert("${"frontend.error.decline".localize()} '${it.message}'")
                }
            }
        }
    }

    fun executeWithdraw(id: Long) {
        if (window.confirm("${"frontend.confirm.withdraw".localize()} (ID$id)?")) {
            ServerPost(WithdrawAction(id, ""), jwt).asyncCall {
                //process result in callback
                if (it.successful) {
                    if (it.finished) {
                        window.alert("${"frontend.success.withdrawn".localize()} '${it.message}'")
                        window.location.reload()
                    } else {
                        window.alert("${"frontend.uncomplete.withdrawn".localize()} '${it.message}'")
                    }
                } else {
                    window.alert("${"frontend.error.withdrawn".localize()} '${it.message}'")
                }
            }
        }
    }


    fun relogin() {
        val oldemail = localStorage.get(LOCALSTORAGE_KRESE_MY_EMAIL)
        val result = if (oldemail != null) {
            window.prompt("enter.your.email".localize(), oldemail)
        } else {
            window.prompt("enter.your.email".localize())
        }

        if (result != null && result.isNotBlank()) {

            val xhttp = XMLHttpRequest()
            xhttp.open("POST", Routes.POST_RELOGIN.path, true)
            xhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded")
            xhttp.onreadystatechange = {
                println("Received something... state = ${xhttp.readyState} and status = ${xhttp.status}")
                if (xhttp.readyState == 4.toShort() && xhttp.status == 200.toShort()) {
                    window.alert("we.have.send.you.an.email".localize())
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
        if (relogin != null && relogin.isNotBlank()) {
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

    fun testForReceiveAction() {

        val action = getURLParameters().get("action")
        val cs: ClientState = this
        if (action != null && action.isNotBlank()) {

            if (window.confirm("${"frontend.confirm.action".localize()} $action")) {
                ServerActionPost(action).asyncCall {
                    window.alert(if (it.successful) {
                        if (it.finished) {
                            "${"frontend.success.action".localize()} ${it.message}"
                        } else {
                            "${"frontend.uncomplete.action".localize()} ${it.message}"
                        }
                    } else {
                        "${"frontend.error.action".localize()} ${it.message}"
                    })
                    cs.unsetURLParameter("action")
                    window.location.reload()
                }
            } else {
                cs.unsetURLParameter("action")
            }
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
                val x = JSON.parse<GetTotalResponse>(xhttp.responseText)
                allKeys = x.keys.map { it.key() }
                reservables.clear()
                reservables.putAll(x.keys.map { it.key() to it }.toMap())

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
                +reservables.get(key)!!.title
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
                div {
                    id = "pro_" + key.id
                    +"Prologue for = ${key.id}"
                }
                div {
                    div {
                        id = "for_" + key.id
                        +"Formular"
                    }
                }
                div {
                    id = "epi_" + key.id
                    +"Epilogue"
                }
            }
            div {
                div {
                    id = "cal_" + key.id
                    +"Kalender"
                }
                div {
                    id = "dap_" + key.id
                }
                div {
                    id = "lis_" + key.id
                    +"Liste"
                }
            }
        }
    }

    private fun setAllKeysToGUI() {
        navbar.innerHTML = ""
        tabcontainer.innerHTML = ""
        allKeys.sortedBy { it.id }.forEach {
            navbar.appendChild(transformKeyIntoButton(it))
            tabcontainer.appendChild(transformKeyIntoDiv(it))
            addDatePickers(document.getElementById("dap_" + it.id)!!)
            setReservableToKey(it)
        }
    }

    fun loadEntriesToKey(uniqueReservableKey: UniqueReservableKey, from: Date, to: Date) {
        val xhttp = XMLHttpRequest();
        xhttp.open("GET", Routes.GET_RESERVABLES.path + "?key=${uniqueReservableKey.id}&from=${from.getTime()}&to=${to.getTime()}${if (jwt != null) "&jwt=$jwt" else ""}", true)
        xhttp.onreadystatechange = {
            if (xhttp.readyState == 4.toShort() && xhttp.status == 200.toShort()) {
                reservations.put(uniqueReservableKey, JSON.parse<GetResponse>(xhttp.responseText))
                setEntriesToKey(uniqueReservableKey)
            }
        }
        xhttp.send()
    }

    fun setReservableToKey(uniqueReservableKey: UniqueReservableKey) {
        document.getElementById("title_header_${uniqueReservableKey.id}")!!.innerHTML = reservables.get(uniqueReservableKey)!!.title
        document.getElementById("pro_" + uniqueReservableKey.id)!!.innerHTML = reservables.get(uniqueReservableKey)!!.prologue
        val epilogue = reservables.get(uniqueReservableKey)!!.epilogue
        document.getElementById("epi_" + uniqueReservableKey.id)!!.innerHTML = epilogue
        addFormular(document.getElementById("for_" + uniqueReservableKey.id)!!, uniqueReservableKey)

    }

    fun setEntriesToKey(uniqueReservableKey: UniqueReservableKey) {
        setReservableToKey(uniqueReservableKey)
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

        val reservable = this.reservables.get(uniqueReservableKey)

        val collapseId = "collapsedList_${uniqueReservableKey.id}"

        val el = document.create.div {
            style = "width: 100%;margin-top:40px;margin-bottom:40px;"
            div {
                style = "margin:0 auto;"
                unsafe {
                    +"""<a class="btn btn-primary" data-toggle="collapse" style="width:30%;white-space: normal;" href="#$collapseId" role="button" aria-expanded="false" aria-controls="$collapseId">
    ${"frontend.list.showallbtn".localize()}
  </a>"""
                }
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
                                        +"Actions"
                                    }
                                    th {
                                        +"Comments"
                                    }
                                }
                            }
                            tbody {

                                for (reservation in reservations.get(uniqueReservableKey)!!.existingReservations) {
                                    tr {
                                        td {
                                            +reservation.startTime.toDate().toLocalizedDateShort()
                                        }
                                        td {
                                            +reservation.endTime.toDate().toLocalizedDateShort()
                                        }
                                        td {
                                            +reservation.name.deescape()
                                        }
                                        td {
                                            div {
                                                if (reservation.accepted) {
                                                    span {
                                                        classes = setOf("glyphicon", "glyphicon-ok-sign")
                                                    }
                                                } else {
                                                    span {
                                                        classes = setOf("glyphicon", "glyphicon-question-sign")
                                                    }
                                                }
                                            }
                                            unsafe {
                                                +reservation.toBlockTableCellHTML(reservable)
                                            }
                                        }
                                        td {
                                            ActionButtons(reservation)
                                        }
                                        td {
                                            +reservation.commentUser.deescape()
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
            if (!reservation.accepted) {
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
        val config = reservations.get(uniqueReservableKey)!!.toCalendarConfig()
        @Suppress("UNUSED_VARIABLE")
        val configJson = JSON.stringify(config)
        calDiv.innerHTML = ""

        val calid = "#child_" + calDiv.id
        calDiv.append {
            div {
                id = calid.drop(1)
            }
        }

        @Suppress("DEPRECATION")
        jq(calid).asDynamic().fullCalendar(js("JSON.parse(configJson)"))
    }

    fun DIV.toFormularInputDiv(element: ReservableElement, key: UniqueReservableKey, prefix: String): Unit {
        val r = element


        div {
            style = "margin-top:15px;margin-bottom:3px;border:2px #AACFEF solid;border-radius: 8px;padding:8px;"
            div {
                div {
                    label { +"${r.name}: " }
                }
                div {
                    +r.description
                }
            }
            if (r.units != 0) {
                if (r.units != 1) {
                    input {
                        classes = setOf("form-control")
                        type = InputType.number
                        name = "count_input_${key.id}_${prefix}_${r.id}"
                        step = 1.toString()
                        min = if (prefix.equals("ROOT") && r.subElements.isEmpty()) 1.toString() else 0.toString()
                        max = r.units.toString()
                        value = if (prefix.equals("ROOT") && r.subElements.isEmpty()) 1.toString() else 0.toString()
                    }
                } else {
                    div {
                        classes = setOf("checkbox")
                        label {
                            input {
                                type = InputType.checkBox
                                name = "check_input_${key.id}_${prefix}_${r.id}"
                                checked = prefix.equals("ROOT") && r.subElements.isEmpty()
                                disabled = prefix.equals("ROOT") && r.subElements.isEmpty()
                            }
                            +"reserve.this.element".localize()
                        }
                    }
                }
            }
        }
        for (@Suppress("NAME_SHADOWING") element in r.subElements) {
            this.toFormularInputDiv(element, key, prefix + "_" + r.id)
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

        val r = reservables.get(uniqueReservableKey)!!

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
                        classes = setOf("col-lg-6")
                        div {
                            label {
                                +"formfield.label.name".localize()
                            }
                            input(type = InputType.text, name = FormFieldNames.Name.name, classes = "form-control") {
                                required = true
                            }
                        }


                        div {
                            classes = setOf("form-group")
                            label {
                                +"formfield.label.email".localize()
                            }
                            input(type = InputType.email, name = FormFieldNames.Email.name, classes = "form-control") {
                                required = true
                            }
                        }


                        div {
                            classes = setOf("form-group")
                            label {
                                +"formfield.label.phone".localize()
                            }
                            input(type = InputType.tel, name = FormFieldNames.Telephone.name, classes = "form-control")
                        }

                        div {
                            classes = setOf("form-group")
                            label {
                                +"formfield.label.from".localize()
                            }
                            input(type = InputType.date, name = FormFieldNames.From.name, classes = "form-control") {
                                value = Date(creationDate).nextFullDay().toDateShort()
                            }
                        }

                        div {
                            classes = setOf("form-group")
                            label {
                                +"formfield.label.to".localize()
                            }
                            input(type = InputType.date, name = FormFieldNames.To.name, classes = "form-control") {
                                value = Date(creationDate).nextFullDay().nextFullDay().toDateShort()
                            }
                        }

                    }

                    div {
                        classes = setOf("form-group", "col-lg-6")
                        toFormularInputDiv(r.elements, uniqueReservableKey, "ROOT")
                    }

                    div {
                        classes = setOf("form-group", "col-lg-6")
                        label { +"formfield.label.comment".localize() }
                        textArea {
                            name = FormFieldNames.Comment.name
                            classes = setOf("form-control")
                        }
                    }

                    div {
                        classes = setOf("form-group", "col-lg-6")
                        if (r.checkBoxes.isNotEmpty())
                            r.checkBoxes.map {
                                div {
                                    classes = setOf("checkbox")
                                    label {
                                        input(type = InputType.checkBox, name = "checkbox_${it.hashCode()}") {
                                            id = "checkbox_${uniqueReservableKey.id}_${it.hashCode()}"
                                            onChangeFunction = { checkCheckBoxes(uniqueReservableKey) }
                                        }
                                        +" $it"
                                    }
                                }
                            }
                    }
                    div {
                        classes = setOf("form-group", "col-lg-6")

                        submitInput {
                            classes = setOf("btn", "btn-info")
                            id = "create_submit_btn_${uniqueReservableKey.id}"

                        }
                    }
                }
        )
        checkCheckBoxes(uniqueReservableKey)
    }


    fun checkCheckBoxes(uniqueReservableKey: UniqueReservableKey) {
        val allChecked = reservables.get(uniqueReservableKey)!!.checkBoxes.all {
            document.getElementById("checkbox_${uniqueReservableKey.id}_${it.hashCode()}")!!.asDynamic().checked == true
        }
        document.getElementById("create_submit_btn_${uniqueReservableKey.id}")!!.asDynamic().disabled = !allChecked
    }


    fun formSubmit(uniqueReservableKey: UniqueReservableKey, formEvent: Event): Boolean {
        formEvent.preventDefault()
        formEvent.stopPropagation()
        if (window.confirm("frontend.confirm.create".localize())) {
            parseFormularToData(uniqueReservableKey)
            return true
        } else {
            return false
        }
    }

    fun addDatePickers(formDiv: Element) {
        formDiv.appendChild(
                document.create.form(action = null, encType = null,
                        method = null) {
                    onSubmitFunction = {
                        println("Getting new events to key = $selectedKey from = $from until $to")
                        selectedKey?.let { it1 -> loadEntriesToKey(it1, from, to) }
                        it.preventDefault()

                        it.stopPropagation()
                    }
                    div {

                        style = "padding:15px;"
                    div {
                        classes = setOf("form-inline")
                        div {
                            classes = setOf("form-group")
                            style = "margin-left:20px;"
                            label {
                                +"filter.label.from".localize()
                            }
                            input(type = InputType.date, name = "from-picker") {
                                classes = setOf("form-control")
                                value = from.toDateShort()
                                onChangeFunction = {
                                    from = Date(this.value)
                                }
                            }
                        }
                        div {
                            classes = setOf("form-group")
                            style = "margin-left:20px;"
                            label {
                                +"filter.label.to".localize()
                            }
                            input(type = InputType.date, name = "to-picker") {
                                classes = setOf("form-control")
                                value = to.toDateShort()
                                onChangeFunction = {
                                    to = Date(this.value)
                                }
                            }
                            button {
                                type = ButtonType.submit
                                classes = setOf("btn", "btn-default")
                                style = "margin-left:20px;"
                                +"filter.btn.submit".localize()
                            }
                        }
                    }
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
        @Suppress("DEPRECATION")
        val formURLString: String = jq(id).asDynamic().serialize()
        val fields = formURLString.toNamedMap()
        //post as action
        ServerPost(fields.toCreateAction(uniqueReservableKey), jwt).asyncCall {
            //process result in callback
            if (it.successful) {
                if (it.finished) {
                    window.alert("${"frontend.error.create.before".localize()} '${it.message}', ${"frontend.error.create.after".localize()}")
                    window.location.reload()
                } else {
                    window.alert("${"frontend.uncomplete.create".localize()} '${it.message}'")
                }
            } else {
                window.alert("${"frontend.error.create".localize()} '${it.message}'")
            }
        }
    }
}

private fun String.unescape(): String {
    val dom = domparser.parseFromString("<!doctype html><body>" + this, "text/html")
    return dom.body?.textContent.or("HTML_PARSER_ERROR")
}

fun String.deescape(): String = this.unescape()

private fun Date.nextFullDay(): Date {
    val d: Date = Date(this.getTime() + (1000L * 60 * 60 * 24))
    val r = Date(d.getFullYear(), d.getMonth(), d.getDate(), 12, 0)
    return r
}


fun String.toNamedMap(): Map<String, String> {
    return this.split("&").map {
        val arr = it.split("=")
        decodeURIComponent(arr.get(0)) to decodeURIComponent(arr.get(1))
    }.toMap()
}

private fun String.getTrailingIds(prefix: String, uniqueReservableKey: UniqueReservableKey): List<Int> {
    val previousStr = "$prefix${uniqueReservableKey.id}_ROOT_"
    val suffix = this.substring(previousStr.length)
    println("suffix: $suffix")
    return suffix.split("_").map {
        println("Parsing id to Int = $it")
        it.toInt()
    }
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
    val comment = this.get(FormFieldNames.Comment.name)!!

    val blocks: List<DbBlockData> = this.toList().map {
        println("analyzing pair: ${it.first} -> ${it.second}")
        if (it.first.startsWith("count_input_")) {
            DbBlockData(it.first.getTrailingIds("count_input_", uniqueReservableKey), it.second.toInt())
        } else if (it.first.startsWith("check_input_") && it.second.equals("on", true)) {
            DbBlockData(it.first.getTrailingIds("check_input_", uniqueReservableKey), 1)
        } else {
            null
        }
    }.filter { it != null && it.usedNumber > 0 }.map { it!! }

    val ca = CreateAction(uniqueReservableKey, email, name, phone, comment, from, to, blocks)
    console.log("CreateAction:")
    console.log(ca)
    return ca
}

fun String.dateStringToMillis(): Long = Date(Date.parse(this)).getTime().roundToLong()