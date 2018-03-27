package krese.test

import com.github.salomonbrys.kodein.instance
import junit.framework.TestCase.assertTrue
import krese.FileSystemWrapper
import krese.data.TemplateTypes
import org.junit.Test

class MailTemplateTest {
    val kodein = getMockKodein()

    val fileSystemRaw: FileSystemWrapper = kodein.instance()


    val fileMock = fileSystemRaw as FileSytemMock


    @Test
    fun mailTemplates_canBeLoadedFromResourceDir() {
        assertTrue(TemplateTypes.values().all {
            val loaded = it.getResourceTemplate(fileMock)
            loaded.subject.isNotBlank()
        })
    }
}