package krese

import com.google.gson.Gson
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import org.jetbrains.ktor.routing.*
import org.jetbrains.ktor.application.*
import org.jetbrains.ktor.features.DefaultHeaders
import org.jetbrains.ktor.application.*
import org.jetbrains.ktor.features.*
import org.jetbrains.ktor.host.*
import org.jetbrains.ktor.http.*
import org.jetbrains.ktor.netty.*
import org.jetbrains.ktor.response.*
import org.jetbrains.ktor.routing.*
import org.jetbrains.ktor.request.*     // for recieve
import org.jetbrains.ktor.util.*
import com.google.gson.GsonBuilder
import java.nio.charset.Charset
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardWatchEventKinds.*
import java.util.concurrent.TimeUnit


class ApplicationHolder(func : (Config) -> Unit) {

    var server: NettyApplicationHost? = null

    fun startWith(config: Config) : Unit {
        //launch(CommonPool) {

       server = embeddedServer<NettyApplicationHost>(Netty, config.port) {
                routing {
                    get("/") {
                        call.respondText("Hello World", ContentType.Text.Html)
                    }
                }
            }

        println("Server spawned at ${System.currentTimeMillis()}")

        launch(CommonPool) {
            server!!.start(wait = true)
        }

        println("Server is running on port ${config.port}  at ${System.currentTimeMillis()}")

        //}
    }

    fun terminate() : Unit {
        server!!.stop(5000, 10000, TimeUnit.MILLISECONDS)
    }
}



val settingsFilename = "Settings.json"

fun executeWheneverConfigFileChanges( pathOfConfigFileParentDirectory: String, func : (Config) -> Unit) : Unit {

    val dir: Path = Paths.get(pathOfConfigFileParentDirectory)

    val watcher = FileSystems.getDefault().newWatchService()


    val key = dir.register(watcher,
            ENTRY_CREATE,
            ENTRY_DELETE,
            ENTRY_MODIFY)

    var applicationHolder: ApplicationHolder? = null

    while(true) {

        if (applicationHolder == null) {
            applicationHolder = ApplicationHolder(func)
        } else {
            applicationHolder.terminate()
        }
        val confFile = dir.resolve(settingsFilename).toFile()
        applicationHolder.startWith(config = Gson().fromJson(confFile.readText(Charsets.UTF_8), Config::class.java))

    for (event in key.pollEvents()) {
        val kind = event.kind()

        // This key is registered only
        // for ENTRY_CREATE events,
        // but an OVERFLOW event can
        // occur regardless if events
        // are lost or discarded.
        if (kind === OVERFLOW) {
            continue
        }

        if (applicationHolder == null) {
            applicationHolder = ApplicationHolder(func)
        } else {
            applicationHolder.terminate()
        }
        val filecontents = confFile.readText(Charsets.UTF_8)
        applicationHolder.startWith(config = Gson().fromJson(filecontents, Config::class.java))
    }

    // Reset the key -- this step is critical if you want to
    // receive further watch events.  If the key is no longer valid,
    // the directory is inaccessible so exit the loop.
    val valid = key.reset()
    if (!valid) {
        println("key was no longer valid")
    }

}

}

fun createConfigFileDefaultIfNoConfigExists(pathOfConfigFileParentDirectory: String) {
    val parent = Files.createDirectories(Paths.get(pathOfConfigFileParentDirectory))

    val f = parent.resolve(settingsFilename).toFile()

    if (!f.exists()) {
        println("File does not exists: ${f.absolutePath}")
        f.printWriter().use { out ->
            out.println(GsonBuilder().setPrettyPrinting().create().toJson(Config()))
        }
    } else {
        println("File exists: ${f.absolutePath}")
    }
}