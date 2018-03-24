package krese.test

import com.github.salomonbrys.kodein.instance
import junit.framework.TestCase.assertTrue
import krese.FileSystemWrapper
import krese.data.TemplateTypes
import org.junit.Test
import kotlin.test.assertEquals

class MailTemplateTest {
    val kodein = getMockKodein()

    val fileSystemRaw : FileSystemWrapper = kodein.instance()


    val fileMock = fileSystemRaw as FileSytemMock


    @Test
    fun mailTemplates_canBeLoadedFromResourceDir() {
        assertTrue(TemplateTypes.values().all {
            val loaded = it.getResourceTemplate(fileMock)
            assertEquals("Placeholder for Subject", loaded.subject)
            loaded.subject.isNotBlank()
        })
    }
}