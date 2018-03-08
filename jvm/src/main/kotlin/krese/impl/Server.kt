package krese.impl

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.instance
import com.google.gson.Gson
import io.ktor.application.call
import io.ktor.content.defaultResource
import io.ktor.content.resources
import io.ktor.content.static
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.http.parametersOf
import io.ktor.request.receive
import io.ktor.request.receiveMultipart
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import krese.*
import krese.data.PostAction
import krese.data.PostActionInput
import krese.data.UniqueReservableKey
import krese.utility.fromJson

class Server(private val kodein: Kodein) {

    private val appConfig: ApplicationConfiguration = kodein.instance()
    private val authVerifier: AuthVerifier = kodein.instance()
    private val getReceiver: GetReceiver = kodein.instance()
    private val jwtReceiver: JWTReceiver = kodein.instance()
    private val postReceiver: PostReceiver = kodein.instance()


    val server = embeddedServer(Netty, appConfig.applicationPort) {
        println("appConfig.webDirectory = " + appConfig.webDirectory)
        routing {
            static("") {
                resources(appConfig.webDirectory)
                defaultResource("${appConfig.webDirectory}/index.html")
            }

            get("reservable") {
                val params = call.receive<Parameters>()
                val jwt = params.get("jwt")
                call.respondText(Gson().toJson(getReceiver.retrieveAll(if (jwt != null) authVerifier.decodeJWT(jwt)?.userProfile?.email else null)))
            }

            route("/reservable/{endpoint...}") {
                get {
                    val endpointSegments = call.parameters.getAll("endpoint")
                    val key = UniqueReservableKey(endpointSegments!!.joinToString("/"))

                    val params = call.receive<Parameters>()
                    val jwt = params.get("jwt")

                    call.respondText(Gson().toJson(getReceiver.retrieve(key, if (jwt != null) authVerifier.decodeJWT(jwt)?.userProfile?.email else null)))
                }
                post {
                    val params = call.receive<Parameters>()
                    val postStr = params.get("action")
                    if (postStr != null) {
                        val postAction: PostActionInput = Gson().fromJson(postStr)
                        call.respondText(Gson().toJson(postReceiver.submitForm(postAction)))
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
        }
    }

    fun start() {
        server.start(wait = true)
    }

}