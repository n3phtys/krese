package krese.test

import junit.framework.TestCase.assertEquals
import krese.data.isValidKey
import org.junit.Test


class KeyTest {

    @Test
    fun reservablekeys_arefilteredright() {
        assertEquals(true, isValidKey("my/key215"))
        assertEquals(false, isValidKey("/my/key215"))
        assertEquals(false, isValidKey("my/key215/"))
        assertEquals(false, isValidKey(""))
        assertEquals(false, isValidKey("/"))
        assertEquals(true, isValidKey("my/key215"))
        assertEquals(true, isValidKey("mykey215"))
        assertEquals(false, isValidKey("aa//aa"))
    }
}