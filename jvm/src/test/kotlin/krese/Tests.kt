package krese

import junit.framework.Assert.assertEquals
import krese.data.isValidKey
import org.junit.Test


class CalculatorTest {

    @Test
    fun reservablekeys_arefilteredright() {
        assertEquals(true, isValidKey("my/key215"))
        assertEquals(false, isValidKey("/my/key215"))
        assertEquals(false, isValidKey("my/key215/"))
        assertEquals(false, isValidKey(""))
        assertEquals(false, isValidKey("/"))
        assertEquals(true, isValidKey("my/key215"))
        assertEquals(true, isValidKey("mykey215"))
    }
}