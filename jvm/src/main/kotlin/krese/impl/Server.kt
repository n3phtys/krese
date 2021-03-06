package krese.impl

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.instance
import io.ktor.application.call
import io.ktor.content.default
import io.ktor.content.files
import io.ktor.content.static
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.serialization.json.JSON
import krese.*
import krese.data.*
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import java.io.File

class Server(private val kodein: Kodein) {

    private val appConfig: ApplicationConfiguration = kodein.instance()
    private val authVerifier: AuthVerifier = kodein.instance()
    private val getReceiver: GetReceiver = kodein.instance()
    private val jwtReceiver: JWTReceiver = kodein.instance()
    private val postReceiver: PostReceiver = kodein.instance()
    private val stringLocalizer: StringLocalizer = kodein.instance()


    val server = embeddedServer(Netty, appConfig.applicationPort) {
        logger.info("appConfig.webDirectory = " + File(appConfig.webDirectory).absolutePath)
        logger.info("get entries = /" + Routes.GET_RESERVABLES.path)
        logger.info("Binding to port ${appConfig.applicationPort} with outside url: ${appConfig.applicationRoot}")
        routing {

            get("/" + Routes.GET_RESERVABLES.path) {
                val params = call.parameters

                val jwt = params.get("jwt")
                val fromStr = params.get("from")
                val toStr = params.get("to")

                val key: UniqueReservableKey? = params.get("key").let { if (it != null) UniqueReservableKey(it) else null }


                val from: DateTime = if (fromStr != null) DateTime(fromStr.toLong(), DateTimeZone.UTC) else DateTime(Long.MIN_VALUE)
                val to: DateTime = if (toStr != null) DateTime(toStr.toLong(), DateTimeZone.UTC) else DateTime(Long.MAX_VALUE)

                if (key != null) {
                    val ele: GetResponse? = getReceiver.retrieve(key, from, to, jwt?.let { it1 -> authVerifier.decodeJWT(it1)?.userProfile?.email })
                    val json = if (ele != null) JSON.stringify(ele) else "null"
                    call.respondText(json)
                } else {
                    call.respondText(JSON.Companion.stringify(getReceiver.retrieveAll(if (jwt != null) authVerifier.decodeJWT(jwt)?.userProfile?.email else null)))
                }
            }

            get("/" + Routes.GET_LOCALIZATION.path) {
                val r = stringLocalizer.getTranslationsAsJS()
                call.respondText(r, ContentType.parse("text/javascript"))
            }

            post("/" + Routes.POST_ACTION_ACCEPT.path) {
                val params = call.receive<Parameters>()
                val postStr = params.get("action")
                logger.info("INPUT: postStr= $postStr ")
                if (postStr != null) {
                    val postAction: AcceptActionInput = JSON.parse(postStr)
                    call.respondText(JSON.stringify(postReceiver.submitForm(postAction)))
                } else {
                    call.respond(HttpStatusCode.BadRequest, "could.not.read.action.param".localize(kodein.instance()))
                }
            }

            post("/" + Routes.POST_ACTION_CREATE.path) {
                val params = call.receive<Parameters>()
                val postStr = params.get("action")
                logger.info("INPUT: postStr= $postStr ")
                if (postStr != null) {
                    val postAction: CreateActionInput = JSON.parse(postStr)
                    call.respondText(JSON.stringify(postReceiver.submitForm(postAction)))
                } else {
                    call.respond(HttpStatusCode.BadRequest, "could.not.read.action.param".localize(kodein.instance()))
                }
            }

            post("/" + Routes.POST_ACTION_DECLINE.path) {
                val params = call.receive<Parameters>()
                val postStr = params.get("action")
                logger.info("INPUT: postStr= $postStr ")
                if (postStr != null) {
                    val postAction: DeclineActionInput = JSON.parse(postStr)
                    call.respondText(JSON.stringify(postReceiver.submitForm(postAction)))
                } else {
                    call.respond(HttpStatusCode.BadRequest, "could.not.read.action.param".localize(kodein.instance()))
                }
            }

            post("/" + Routes.POST_ACTION_WITHDRAW.path) {
                val params = call.receive<Parameters>()
                val postStr = params.get("action")
                logger.info("INPUT: postStr= $postStr ")
                if (postStr != null) {
                    val postAction: WithdrawActionInput = JSON.parse(postStr)
                    call.respondText(JSON.stringify(postReceiver.submitForm(postAction)))
                } else {
                    call.respond(HttpStatusCode.BadRequest, "could.not.read.action.param".localize(kodein.instance()))
                }
            }


            post("/" + Routes.POST_CREDENTIALS_VALID.path) {
                val params = call.receive<Parameters>()
                val jwt = params.get("jwt")
                if (jwt != null) {
                    call.respondText(jwtReceiver.loginStillValid(jwt).toString())
                } else {
                    call.respond(HttpStatusCode.BadRequest, "could.not.read.jwt.param".localize(kodein.instance()))
                }

            }
            post("/" + Routes.POST_RELOGIN.path) {
                val params = call.receive<Parameters>()
                val email = params.get("email")
                val key = params.get("key")
                if (email != null) {
                    jwtReceiver.relogin(email, key?.let { it1 -> UniqueReservableKey(it1) })
                    call.respondText(true.toString())
                } else {
                    call.respond(HttpStatusCode.BadRequest, "could.not.read.email.param".localize(kodein.instance()))
                }

            }

            post("/" + Routes.POST_ACTION_SIGNED.path) {
                val params = call.receive<Parameters>()
                val postStr = params.get("action")
                logger.info("INPUT: postStr= $postStr ")
                if (postStr != null) {
                    val action = authVerifier.decodeJWT(postStr)
                    if (action != null && action.action != null) {
                        logger.debug("action = $action")
                        call.respondText(JSON.stringify(postReceiver.submitForm(PostActionInput.build(postStr, action.action!!))))
                    } else {
                        call.respond(HttpStatusCode.BadRequest, "could.not.validate.crypto.signature.of.link".localize(kodein.instance()))
                    }
                } else {
                    call.respond(HttpStatusCode.BadRequest, "could.not.read.action.param".localize(kodein.instance()))
                }

            }

            static("static") {
                files(File(appConfig.staticDirectory))
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