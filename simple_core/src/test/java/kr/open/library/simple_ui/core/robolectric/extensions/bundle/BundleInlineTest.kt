package kr.open.library.simple_ui.core.robolectric.extensions.bundle

import android.os.Bundle
import kr.open.library.simple_ui.core.extensions.bundle.getValue
import org.junit.Assert.assertArrayEquals
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

    @Test
    fun `getValue supports all numeric primitives`() {
        val bundle = Bundle().apply {
            putBoolean("bool", true)
            putFloat("float", 3.14f)
            putLong("long", 1234L)
            putDouble("double", 9.81)
            putChar("char", 'Z')
            putShort("short", 7.toShort())
            putByte("byte", 1.toByte())
        }

        assertEquals(true, bundle.getValue("bool", false))
        assertEquals(3.14f, bundle.getValue("float", 0f))
        assertEquals(1234L, bundle.getValue("long", 0L))
        assertEquals(9.81, bundle.getValue("double", 0.0), 0.0)
        assertEquals('Z', bundle.getValue("char", 'A'))
        assertEquals(7.toShort(), bundle.getValue("short", 0.toShort()))
        assertEquals(1.toByte(), bundle.getValue("byte", 0.toByte()))
    }

    @Test
    fun `getValue returns byte array or default when missing`() {
        val payload = byteArrayOf(1, 2, 3)
        val bundle = Bundle().apply {
            putByteArray("data", payload)
            putByteArray("empty", null)
        }

        val retrieved: ByteArray = bundle.getValue("data", ByteArray(0))
        assertArrayEquals(payload, retrieved)

        val fallbackDefault = byteArrayOf(9, 9)
        val fallback: ByteArray = bundle.getValue("missing", fallbackDefault)
        assertArrayEquals(fallbackDefault, fallback)

        val nullStoredDefault = byteArrayOf(5)
        val fromNull: ByteArray = bundle.getValue("empty", nullStoredDefault)
        assertArrayEquals(nullStoredDefault, fromNull)
    }
}
