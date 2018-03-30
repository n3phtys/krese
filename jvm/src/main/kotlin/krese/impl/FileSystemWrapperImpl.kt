package krese.impl

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.instance
import kotlinx.serialization.json.JSON
import krese.ApplicationConfiguration
import krese.FileSystemWrapper
import krese.data.*
import krese.migration.migrationFileLoaded
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths


class FileSystemWrapperImpl(private val kodein: Kodein) : FileSystemWrapper {
    private val appConfig: ApplicationConfiguration = kodein.instance()


    /*
    We have one main directory, given by the configuration (env variable)
    We have a multitude of sub directories
    In each such subdirectory we scan for a krese.json, which we can parse to "Reservable"
    In this json we find the key
     */


    init {
        this.getKeysFromDirectory().forEach { this.getReservableToKey(it.key) }


        val importedData = migrationFileLoaded
        if (importedData != null) {
            appConfig.reservablesDirectory
            importedData.toJsonConfigs().forEach {
                val parentDir = appConfig.reservablesDirectory + "/" + it.uniqueId
                File(parentDir).mkdir()
                val json: String = JSON.stringify(it)
                val f = File(parentDir + "/krese.json")
                if (!f.exists()) {
                    File(parentDir + "/krese.json").writeText(json, Charsets.UTF_8)
                }
            }
        }
    }


    override fun getKeysFromDirectory(): Map<UniqueReservableKey, Path> {
        val folder = File(appConfig.reservablesDirectory)
        val listOfFiles = folder.listFiles()
        val res = mutableMapOf<UniqueReservableKey, Path>()
        for (i in listOfFiles.indices) {
            val fd = listOfFiles[i]
            if (fd.isDirectory) {
                val p = Paths.get(appConfig.reservablesDirectory, fd.name, "krese.json")
                val jf = p.toFile()
                if (jf.canRead()) {
                    val txt = jf.readText()
                    val output = JSON.parse<Reservable>(txt)
                    res.put(output.key(), p)
                }
            }
        }
        return res
    }

    /**
     * build complete reservable via key
     */
    override fun getReservableToKey(key: UniqueReservableKey): Reservable? {
        val str = this.getKeysFromDirectory().get(key)?.toFile()?.readText()
        if (str != null) {
            val x = JSON.parse<Reservable>(str)
            val prologue = loadPrologue(key)
            val epilogue = loadEpilogue(key)
            return x.copy(epilogue = if (epilogue != null) epilogue else "", prologue = if (prologue != null) prologue else "")
        } else {
            return null
        }
    }


    private val jsonfilename = "krese.json"
    private fun loadPrologue(key: UniqueReservableKey): String? {
        val jsonPath: Path? = this.getKeysFromDirectory().get(key)
        if (jsonPath != null) {
            try {
                return (File((jsonPath.toString().substring(0, jsonPath.toString().length - jsonfilename.length) + "prologue.html")).readText())
            } catch (e: Exception) {
                return null
            }
        } else {
            return null
        }
    }

    private fun loadEpilogue(key: UniqueReservableKey): String? {
        val jsonPath: Path? = this.getKeysFromDirectory().get(key)
        if (jsonPath != null) {
            try {
                return (File((jsonPath.toString().substring(0, jsonPath.toString().length - jsonfilename.length) + "epilogue.html")).readText())
            } catch (e: Exception) {
                return null
            }
        } else {
            return null
        }
    }


    override fun getTemplatesFromDir(dir: String): Map<TemplateTypes, String> {
        return TemplateTypes.values().map {
            val f = File(dir + "/" + it.fileName())
            if (f.canRead()) {
                it to f.absolutePath
            } else {
                it to null
            }
        }.filter { it.second != null }.map { it.first to it.second!! }.toMap()
    }

    override fun parseTemplate(path: String): MailTemplate? {
        val lines = File(path).readText()
        return lines.buildMailTemplate()
    }

    override fun readResourceOrFail(filePath: String): String {
        return FileSystemWrapperImpl::class.java.getResourceAsStream(filePath)!!.bufferedReader().use { it.readText() }
    }

    override fun specificMailDir(key: UniqueReservableKey): String? {
        return this.getKeysFromDirectory().get(key)?.toFile()?.parent
    }

}