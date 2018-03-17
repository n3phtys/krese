package krese.impl

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.instance
import kotlinx.serialization.json.JSON
import krese.ApplicationConfiguration
import krese.FileSystemWrapper
import krese.data.Reservable
import krese.data.UniqueReservableKey
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths


class FileSystemWrapperImpl(private val kodein: Kodein): FileSystemWrapper {
    private val appConfig: ApplicationConfiguration = kodein.instance()


    /*
    We have one main directory, given by the configuration (env variable)
    We have a multitude of sub directories
    In each such subdirectory we scan for a krese.json, which we can parse to "Reservable"
    In this json we find the key
     */


    init {
        this.getKeysFromDirectory().forEach{this.getReservableToKey(it.key)}
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
            return JSON.parse<Reservable>(str)
        } else {
            return null
        }
    }
}