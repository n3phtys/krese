package krese.impl

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.instance
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

class Server(private val kodein: Kodein) {
    private val appConfig: ApplicationConfiguration = kodein.instance()
    private val getReceiver: GetReceiver = kodein.instance()
    private val jwtReceiver: JWTReceiver = kodein.instance()
    private val postReceiver: PostReceiver = kodein.instance()


    val server = embeddedServer(Netty, 8080) {
        routing {
            static("") {
                resources("web")
                defaultResource("web/index.html")
            }

            get("/reservable/{endpoint...}") {
                val endpointSegments = call.parameters.getAll("endpoint")
                println("reservable received: " + endpointSegments)
            }

            route("/util") {
                route("valid/credentials") {
                    post {
                        //println(parametersOf().get("jwt"))
                        val params = call.receive<Parameters>()

                        val jwt = params.get("jwt")
                        println("jwt:")
                        if (jwt != null) {
                            println(jwt)
                        call.respondText(jwtReceiver.loginStillValid(jwt).toString())} else {
                            println(jwt)
                            call.respond(HttpStatusCode.BadRequest, "could not read jwt")
                        }
                    }

                }
                route("login") {

                }
                route("jwtaction") {

                }
            }
        }
    }

    fun start() {
        server.start(wait = true)
    }

    /*embeddedServer(Netty, config.port) {
routing {
    static("") {
        resources("web")
        defaultResource("web/index.html")
    }
    route("api") {
        route("events") {
            get("{eventchain}/{from?}/{to?}") {
                val eventchain = call.parameters["eventchain"]
                val fromStr = call.parameters.get("from")
                val toStr = call.parameters.get("to")
                val from = fromStr!!.toLong()
                val to = toStr!!.toLong()

                call.respondText("eventchain = $eventchain", ContentType.parse("application/json"))
            }
            post("{eventchain}") {
                val jsonStr = call.receiveText()
                call.respondText("posted to event chain, json = $jsonStr")
            }
            get("actionlink/{jwt}") {
                val jwt = call.parameters["jwt"]

                call.respondText("JWT received: $jwt")
            }
        }
}
    }*/
}