package krese.impl

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.instance
import krese.ApplicationConfiguration
import krese.StringLocalizer
import java.io.IOException
import java.io.FileInputStream
import java.io.InputStream
import java.util.*


class StringLocalizerImpl(private val kodein: Kodein) : StringLocalizer{
    private val appConfig: ApplicationConfiguration = kodein.instance()

    override fun getTranslations(): Map<String, String> {

        val prop = Properties()
        var input: InputStream? = null

        try {

            input = FileInputStream(appConfig.filePathOfLocalization)

            // load a properties file
            prop.load(input)

            return prop.keys.map {
                it.toString() to prop.getProperty(it.toString())
            }.toMap()

        } catch (ex: IOException) {
            ex.printStackTrace()
            return mapOf<String, String>()
        } finally {
            if (input != null) {
                try {
                    input.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }
    }
}