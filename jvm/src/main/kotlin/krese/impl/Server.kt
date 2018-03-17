package krese.impl

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.instance
import io.ktor.application.call
import io.ktor.content.*
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.http.parametersOf
import io.ktor.request.receive
import io.ktor.request.receiveMultipart
import io.ktor.request.receiveOrNull
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.serialization.json.JSON
import krese.*
import krese.data.*
import java.io.File

class Server(private val kodein: Kodein) {

    private val appConfig: ApplicationConfiguration = kodein.instance()
    private val authVerifier: AuthVerifier = kodein.instance()
    private val getReceiver: GetReceiver = kodein.instance()
    private val jwtReceiver: JWTReceiver = kodein.instance()
    private val postReceiver: PostReceiver = kodein.instance()


    val server = embeddedServer(Netty, appConfig.applicationPort) {
        println("appConfig.webDirectory = " + File(appConfig.webDirectory).absolutePath)
        println("get entries = /" + Routes.GET_ENTRIES_TO_RESERVABLE.path)
        routing {
            get("hello") {
                call.respondText("Hello World")
            }

            get("/" + Routes.GET_RESERVABLES.path) {
                val params = call.receiveOrNull<Parameters>()
                val jwt = params?.get("jwt")
                call.respondText(JSON.Companion.stringify(getReceiver.retrieveAll(if (jwt != null) authVerifier.decodeJWT(jwt)?.userProfile?.email else null)))
            }

            route("/" + Routes.GET_ENTRIES_TO_RESERVABLE.path +"{endpoint...}") {
                get {
                    val endpointSegments = call.parameters.getAll("endpoint")
                    val key = UniqueReservableKey(endpointSegments!!.joinToString("/"))


                    val params = call.receiveOrNull<Parameters>()
                    val jwt: String? = params?.get("jwt")

                    val ele : GetResponse? = getReceiver.retrieve(key, jwt?.let { it1 -> authVerifier.decodeJWT(it1)?.userProfile?.email })
                    val json = if (ele != null) JSON.stringify(ele) else "null"

                        call.respondText(json)

                }
                post {
                    val params = call.receive<Parameters>()
                    val postStr = params.get("action")
                    if (postStr != null) {
                        val postAction: PostActionInput = JSON.parse(postStr)
                        call.respondText(JSON.stringify(postReceiver.submitForm(postAction)))
                    } else {
                        call.respond(HttpStatusCode.BadRequest, "could not read action param")
                    }
                }
            }

            route("/util") {
                post("valid/credentials") {
                    val params = call.receive<Parameters>()
                    val jwt = params.get("jwt")
                    if (jwt != null) {
                        call.respondText(jwtReceiver.loginStillValid(jwt).toString())
                    } else {
                        call.respond(HttpStatusCode.BadRequest, "could not read jwt param")
                    }

                }
                post("login") {
                    val params = call.receive<Parameters>()
                    val email = params.get("email")
                    if (email != null) {
                        jwtReceiver.relogin(email)
                        call.respondText("true")
                    } else {
                        call.respond(HttpStatusCode.BadRequest, "could not read email param")
                    }

                }
                post("jwtaction") {
                    val params = call.receive<Parameters>()
                    val jwtaction = params.get("action")
                    jwtReceiver.receiveJWTAction(jwtaction!!)
                }
            }
            static("") {
                files(File(appConfig.webDirectory))
                default("${appConfig.webDirectory}/index.html")
            }
        }
    }

    fun start() {
        server.start(wait = true)
    }

}