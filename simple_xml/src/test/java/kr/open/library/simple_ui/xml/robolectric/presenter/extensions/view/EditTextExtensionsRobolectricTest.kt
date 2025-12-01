package kr.open.library.simple_ui.xml.robolectric.presenter.extensions.view

import android.content.Context
import android.widget.EditText
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.xml.extensions.view.getTextToString
import kr.open.library.simple_ui.xml.extensions.view.isTextEmpty
import kr.open.library.simple_ui.xml.extensions.view.textToDouble
import kr.open.library.simple_ui.xml.extensions.view.textToFloat
import kr.open.library.simple_ui.xml.extensions.view.textToInt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class EditTextExtensionsRobolectricTest {
    private lateinit var context: Context
    private lateinit var editText: EditText

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        editText = EditText(context)
    }

    // getTextToString() 테스트
    @Test
    fun getTextToString_withText_returnsCorrectString() {
        editText.setText("Hello World")
        assertEquals("Hello World", editText.getTextToString())
    }

    @Test
    fun getTextToString_withEmptyText_returnsEmptyString() {
        editText.setText("")
        assertEquals("", editText.getTextToString())
    }

    // isTextEmpty() 테스트
    @Test
    fun isTextEmpty_withEmptyText_returnsTrue() {
        editText.setText("")
        assertTrue(editText.isTextEmpty())
    }

    @Test
    fun isTextEmpty_withNonEmptyText_returnsFalse() {
        editText.setText("Hello")
        assertFalse(editText.isTextEmpty())
    }

    // textToInt() 테스트
    @Test
    fun textToInt_validInteger_returnsCorrectInt() {
        editText.setText("123")
        assertEquals(123, editText.textToInt())
    }

    @Test
    fun textToInt_negativeInteger_returnsCorrectInt() {
        editText.setText("-456")
        assertEquals(-456, editText.textToInt())
    }

    @Test
    fun textToInt_invalidText_returnsNull() {
        editText.setText("abc")
        assertNull(editText.textToInt())
    }

    @Test
    fun textToInt_emptyText_returnsNull() {
        editText.setText("")
        assertNull(editText.textToInt())
    }

    @Test
    fun textToInt_floatText_returnsNull() {
        editText.setText("12.34")
        assertNull(editText.textToInt())
    }

    // textToFloat() 테스트
    @Test
    fun textToFloat_validFloat_returnsCorrectFloat() {
        editText.setText("12.34")
        assertEquals(12.34f, editText.textToFloat())
    }

    @Test
    fun textToFloat_validInteger_returnsCorrectFloat() {
        editText.setText("100")
        assertEquals(100f, editText.textToFloat())
    }

    @Test
    fun textToFloat_negativeFloat_returnsCorrectFloat() {
        editText.setText("-5.67")
        assertEquals(-5.67f, editText.textToFloat())
    }

    @Test
    fun textToFloat_invalidText_returnsNull() {
        editText.setText("abc")
        assertNull(editText.textToFloat())
    }

    @Test
    fun textToFloat_emptyText_returnsNull() {
        editText.setText("")
        assertNull(editText.textToFloat())
    }

    // textToDouble() 테스트
    @Test
    fun textToDouble_validDouble_returnsCorrectDouble() {
        editText.setText("123.456")
        assertEquals(123.456, editText.textToDouble()!!, 0.0001)
    }

    @Test
    fun textToDouble_validInteger_returnsCorrectDouble() {
        editText.setText("789")
        assertEquals(789.0, editText.textToDouble()!!, 0.0001)
    }

    @Test
    fun textToDouble_negativeDouble_returnsCorrectDouble() {
        editText.setText("-98.76")
        assertEquals(-98.76, editText.textToDouble()!!, 0.0001)
    }

    @Test
    fun textToDouble_invalidText_returnsNull() {
        editText.setText("xyz")
        assertNull(editText.textToDouble())
    }

    @Test
    fun textToDouble_emptyText_returnsNull() {
        editText.setText("")
        assertNull(editText.textToDouble())
    }
}
