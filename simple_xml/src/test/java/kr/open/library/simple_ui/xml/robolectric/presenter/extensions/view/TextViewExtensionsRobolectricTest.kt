package kr.open.library.simple_ui.xml.robolectric.presenter.extensions.view

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.xml.extensions.view.bold
import kr.open.library.simple_ui.xml.extensions.view.boldItalic
import kr.open.library.simple_ui.xml.extensions.view.getString
import kr.open.library.simple_ui.xml.extensions.view.isTextEmpty
import kr.open.library.simple_ui.xml.extensions.view.isTextNullOrEmpty
import kr.open.library.simple_ui.xml.extensions.view.italic
import kr.open.library.simple_ui.xml.extensions.view.normal
import kr.open.library.simple_ui.xml.extensions.view.removeStrikeThrough
import kr.open.library.simple_ui.xml.extensions.view.removeUnderline
import kr.open.library.simple_ui.xml.extensions.view.setTextColorRes
import kr.open.library.simple_ui.xml.extensions.view.strikeThrough
import kr.open.library.simple_ui.xml.extensions.view.style
import kr.open.library.simple_ui.xml.extensions.view.textToDouble
import kr.open.library.simple_ui.xml.extensions.view.textToFloat
import kr.open.library.simple_ui.xml.extensions.view.textToInt
import kr.open.library.simple_ui.xml.extensions.view.underline
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
class TextViewExtensionsRobolectricTest {

    private lateinit var context: Context
    private lateinit var textView: TextView

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        textView = TextView(context)
    }

    // getString() 테스트
    @Test
    fun getString_withText_returnsCorrectString() {
        textView.text = "Hello TextView"
        assertEquals("Hello TextView", textView.getString())
    }

    @Test
    fun getString_withEmptyText_returnsEmptyString() {
        textView.text = ""
        assertEquals("", textView.getString())
    }

    // isTextEmpty() 테스트
    @Test
    fun isTextEmpty_withEmptyText_returnsTrue() {
        textView.text = ""
        assertTrue(textView.isTextEmpty())
    }

    @Test
    fun isTextEmpty_withNonEmptyText_returnsFalse() {
        textView.text = "Hello"
        assertFalse(textView.isTextEmpty())
    }

    // isTextNullOrEmpty() 테스트
    @Test
    fun isTextNullOrEmpty_withEmptyText_returnsTrue() {
        textView.text = ""
        assertTrue(textView.isTextNullOrEmpty())
    }

    @Test
    fun isTextNullOrEmpty_withNonEmptyText_returnsFalse() {
        textView.text = "Hello"
        assertFalse(textView.isTextNullOrEmpty())
    }

    // textToInt() 테스트
    @Test
    fun textToInt_validInteger_returnsCorrectInt() {
        textView.text = "789"
        assertEquals(789, textView.textToInt())
    }

    @Test
    fun textToInt_negativeInteger_returnsCorrectInt() {
        textView.text = "-321"
        assertEquals(-321, textView.textToInt())
    }

    @Test
    fun textToInt_invalidText_returnsNull() {
        textView.text = "invalid"
        assertNull(textView.textToInt())
    }

    @Test
    fun textToInt_emptyText_returnsNull() {
        textView.text = ""
        assertNull(textView.textToInt())
    }

    // textToFloat() 테스트
    @Test
    fun textToFloat_validFloat_returnsCorrectFloat() {
        textView.text = "45.67"
        assertEquals(45.67f, textView.textToFloat())
    }

    @Test
    fun textToFloat_validInteger_returnsCorrectFloat() {
        textView.text = "200"
        assertEquals(200f, textView.textToFloat())
    }

    @Test
    fun textToFloat_negativeFloat_returnsCorrectFloat() {
        textView.text = "-12.34"
        assertEquals(-12.34f, textView.textToFloat())
    }

    @Test
    fun textToFloat_invalidText_returnsNull() {
        textView.text = "invalid"
        assertNull(textView.textToFloat())
    }

    // textToDouble() 테스트
    @Test
    fun textToDouble_validDouble_returnsCorrectDouble() {
        textView.text = "999.888"
        assertEquals(999.888, textView.textToDouble()!!, 0.0001)
    }

    @Test
    fun textToDouble_validInteger_returnsCorrectDouble() {
        textView.text = "500"
        assertEquals(500.0, textView.textToDouble()!!, 0.0001)
    }

    @Test
    fun textToDouble_negativeDouble_returnsCorrectDouble() {
        textView.text = "-77.77"
        assertEquals(-77.77, textView.textToDouble()!!, 0.0001)
    }

    @Test
    fun textToDouble_invalidText_returnsNull() {
        textView.text = "invalid"
        assertNull(textView.textToDouble())
    }

    // bold() 테스트
    @Test
    fun bold_setsTypefaceToBold() {
        textView.bold()
        assertEquals(Typeface.BOLD, textView.typeface.style)
    }

    // italic() 테스트
    @Test
    fun italic_setsTypefaceToItalic() {
        textView.italic()
        assertEquals(Typeface.ITALIC, textView.typeface.style)
    }

    // boldItalic() 테스트
    @Test
    fun boldItalic_setsTypefaceToBoldAndItalic() {
        textView.boldItalic()
        assertEquals(Typeface.BOLD_ITALIC, textView.typeface.style)
    }

    // normal() 테스트
    @Test
    fun normal_setsTypefaceToNormal() {
        textView.normal()
        assertEquals(Typeface.NORMAL, textView.typeface.style)
    }

    // underline() 테스트
    @Test
    fun underline_addsUnderlineFlag() {
        textView.underline()
        assertTrue(textView.paintFlags and Paint.UNDERLINE_TEXT_FLAG != 0)
    }

    // removeUnderline() 테스트
    @Test
    fun removeUnderline_removesUnderlineFlag() {
        textView.underline()
        textView.removeUnderline()
        assertTrue(textView.paintFlags and Paint.UNDERLINE_TEXT_FLAG == 0)
    }

    // strikeThrough() 테스트
    @Test
    fun strikeThrough_addsStrikeThroughFlag() {
        textView.strikeThrough()
        assertTrue(textView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG != 0)
    }

    // removeStrikeThrough() 테스트
    @Test
    fun removeStrikeThrough_removesStrikeThroughFlag() {
        textView.strikeThrough()
        textView.removeStrikeThrough()
        assertTrue(textView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG == 0)
    }

    // 조합 테스트
    @Test
    fun multipleStyleChanges_applyCorrectly() {
        textView.bold()
        textView.underline()
        textView.strikeThrough()

        assertEquals(Typeface.BOLD, textView.typeface.style)
        assertTrue(textView.paintFlags and Paint.UNDERLINE_TEXT_FLAG != 0)
        assertTrue(textView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG != 0)
    }

    @Test
    fun styleRemoval_worksCorrectly() {
        textView.underline()
        textView.strikeThrough()
        textView.removeUnderline()
        textView.removeStrikeThrough()

        assertTrue(textView.paintFlags and Paint.UNDERLINE_TEXT_FLAG == 0)
        assertTrue(textView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG == 0)
    }

    // setTextColorRes() 테스트
    @Test
    fun setTextColorRes_setsTextColor() {
        // android.R.color.black은 시스템 리소스로 Robolectric에서 사용 가능
        textView.setTextColorRes(android.R.color.black)

        // textColor가 설정되었는지 확인 (정확한 값은 시스템 리소스에 따라 다를 수 있음)
        // 0이 아닌 값이 설정되었는지만 확인
        assertTrue(textView.currentTextColor != 0)
    }

    @Test
    fun setTextColorRes_withDifferentColors_changesColor() {
        textView.setTextColorRes(android.R.color.black)
        val color1 = textView.currentTextColor

        textView.setTextColorRes(android.R.color.white)
        val color2 = textView.currentTextColor

        // 두 색상이 다른지 확인
        assertTrue(color1 != color2)
    }

    // style{} 테스트
    @Test
    fun style_appliesMultipleChanges() {
        textView.style {
            bold()
            underline()
            text = "Styled Text"
        }

        assertEquals(Typeface.BOLD, textView.typeface.style)
        assertTrue(textView.paintFlags and Paint.UNDERLINE_TEXT_FLAG != 0)
        assertEquals("Styled Text", textView.text.toString())
    }

    @Test
    fun style_returnsTextView() {
        val result = textView.style {
            text = "Test"
        }

        // style() 함수가 TextView를 반환하는지 확인 (체이닝 가능)
        assertEquals(textView, result)
    }

    @Test
    fun style_chainingWorks() {
        textView.style {
            bold()
        }.style {
            underline()
        }

        assertEquals(Typeface.BOLD, textView.typeface.style)
        assertTrue(textView.paintFlags and Paint.UNDERLINE_TEXT_FLAG != 0)
    }
}
