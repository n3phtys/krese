package krese.test

import junit.framework.TestCase.assertEquals
import krese.data.isValidKey
import org.junit.Test


class KeyTest {

    @Test
    fun reservablekeys_arefilteredright() {
        assertEquals(true, isValidKey("my_key215"))
        assertEquals(false, isValidKey("_my_key215"))
        assertEquals(false, isValidKey("my_key215_"))
        assertEquals(false, isValidKey(""))
        assertEquals(false, isValidKey("_"))
        assertEquals(true, isValidKey("my_key215"))
        assertEquals(true, isValidKey("mykey215"))
        assertEquals(false, isValidKey("aa__aa"))
    }
}