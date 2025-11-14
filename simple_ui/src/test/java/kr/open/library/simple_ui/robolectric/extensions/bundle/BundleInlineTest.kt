package kr.open.library.simple_ui.robolectric.extensions.bundle

import android.os.Bundle
import kr.open.library.simple_ui.extensions.bundle.getValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BundleInlineTest {

    @Test
    fun `getValue returns stored primitive`() {
        val bundle = Bundle().apply { putInt("count", 42) }

        val value: Int = bundle.getValue("count", 0)

        assertEquals(42, value)
    }

    @Test
    fun `getValue missing key returns default`() {
        val bundle = Bundle()

        val value: String = bundle.getValue("missing", "fallback")

        assertEquals("fallback", value)
    }

    @Test
    fun `getValue supports bundle type`() {
        val inner = Bundle().apply { putString("msg", "hello") }
        val bundle = Bundle().apply { putBundle("inner", inner) }

        val retrieved: Bundle = bundle.getValue("inner", Bundle())

        assertSame(inner, retrieved)
    }

    @Test
    fun `getValue unknown requested type returns default`() {
        val bundle = Bundle().apply { putString("custom", "value") }

        val default = listOf(1, 2, 3)
        val value: List<Int> = bundle.getValue("custom", default)

        assertSame(default, value)
    }
}
