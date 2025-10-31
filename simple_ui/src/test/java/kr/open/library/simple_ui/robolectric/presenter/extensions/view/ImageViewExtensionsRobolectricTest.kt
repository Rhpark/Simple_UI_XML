package kr.open.library.simple_ui.robolectric.presenter.extensions.view

import android.content.Context
import android.graphics.ColorMatrixColorFilter
import android.graphics.PorterDuff
import android.widget.ImageView
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.presenter.extensions.view.centerCrop
import kr.open.library.simple_ui.presenter.extensions.view.centerInside
import kr.open.library.simple_ui.presenter.extensions.view.clearTint
import kr.open.library.simple_ui.presenter.extensions.view.fitCenter
import kr.open.library.simple_ui.presenter.extensions.view.fitXY
import kr.open.library.simple_ui.presenter.extensions.view.makeGrayscale
import kr.open.library.simple_ui.presenter.extensions.view.removeGrayscale
import kr.open.library.simple_ui.presenter.extensions.view.setTint
import kr.open.library.simple_ui.presenter.extensions.view.style
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ImageViewExtensionsRobolectricTest {

    private lateinit var context: Context
    private lateinit var imageView: ImageView

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        imageView = ImageView(context)
    }

    // setTint() 테스트 - 시스템 리소스 사용
    @Test
    fun setTint_withSystemColor_setsColorFilter() {
        imageView.setTint(android.R.color.black)

        assertNotNull(imageView.colorFilter)
    }

    @Test
    fun setTint_withDifferentModes_appliesCorrectly() {
        imageView.setTint(android.R.color.black, PorterDuff.Mode.SRC_IN)
        assertNotNull(imageView.colorFilter)

        imageView.setTint(android.R.color.white, PorterDuff.Mode.SRC_ATOP)
        assertNotNull(imageView.colorFilter)
    }

    // clearTint() 테스트
    @Test
    fun clearTint_removesColorFilter() {
        imageView.setTint(android.R.color.black)
        assertNotNull(imageView.colorFilter)

        imageView.clearTint()
        assertNull(imageView.colorFilter)
    }

    @Test
    fun clearTint_withoutTint_doesNothing() {
        assertNull(imageView.colorFilter)

        imageView.clearTint()
        assertNull(imageView.colorFilter)
    }

    // makeGrayscale() 테스트
    @Test
    fun makeGrayscale_setsColorMatrixFilter() {
        imageView.makeGrayscale()

        assertNotNull(imageView.colorFilter)
        assertTrue(imageView.colorFilter is ColorMatrixColorFilter)
    }

    // removeGrayscale() 테스트
    @Test
    fun removeGrayscale_removesColorFilter() {
        imageView.makeGrayscale()
        assertNotNull(imageView.colorFilter)

        imageView.removeGrayscale()
        assertNull(imageView.colorFilter)
    }

    @Test
    fun removeGrayscale_withoutGrayscale_doesNothing() {
        assertNull(imageView.colorFilter)

        imageView.removeGrayscale()
        assertNull(imageView.colorFilter)
    }

    // centerCrop() 테스트
    @Test
    fun centerCrop_setsScaleTypeToCenterCrop() {
        imageView.centerCrop()

        assertEquals(ImageView.ScaleType.CENTER_CROP, imageView.scaleType)
    }

    // centerInside() 테스트
    @Test
    fun centerInside_setsScaleTypeToCenterInside() {
        imageView.centerInside()

        assertEquals(ImageView.ScaleType.CENTER_INSIDE, imageView.scaleType)
    }

    // fitCenter() 테스트
    @Test
    fun fitCenter_setsScaleTypeToFitCenter() {
        imageView.fitCenter()

        assertEquals(ImageView.ScaleType.FIT_CENTER, imageView.scaleType)
    }

    // fitXY() 테스트
    @Test
    fun fitXY_setsScaleTypeToFitXY() {
        imageView.fitXY()

        assertEquals(ImageView.ScaleType.FIT_XY, imageView.scaleType)
    }

    // ScaleType 변경 테스트
    @Test
    fun scaleType_canChangeMultipleTimes() {
        imageView.centerCrop()
        assertEquals(ImageView.ScaleType.CENTER_CROP, imageView.scaleType)

        imageView.fitCenter()
        assertEquals(ImageView.ScaleType.FIT_CENTER, imageView.scaleType)

        imageView.fitXY()
        assertEquals(ImageView.ScaleType.FIT_XY, imageView.scaleType)
    }

    // style{} 테스트
    @Test
    fun style_appliesMultipleChanges() {
        imageView.style {
            centerCrop()
            makeGrayscale()
        }

        assertEquals(ImageView.ScaleType.CENTER_CROP, imageView.scaleType)
        assertNotNull(imageView.colorFilter)
        assertTrue(imageView.colorFilter is ColorMatrixColorFilter)
    }

    @Test
    fun style_returnsImageView() {
        val result = imageView.style {
            centerCrop()
        }

        assertEquals(imageView, result)
    }

    @Test
    fun style_chainingWorks() {
        imageView.style {
            centerCrop()
        }.style {
            fitCenter() // 다른 ScaleType로 변경
        }

        assertEquals(ImageView.ScaleType.FIT_CENTER, imageView.scaleType) // 마지막 설정된 값
    }

    // 복합 시나리오 테스트
    @Test
    fun complexScenario_tintAndGrayscaleInteraction() {
        // tint 적용
        imageView.setTint(android.R.color.black)
        assertNotNull(imageView.colorFilter)

        // grayscale 적용 (colorFilter 교체)
        imageView.makeGrayscale()
        assertNotNull(imageView.colorFilter)
        assertTrue(imageView.colorFilter is ColorMatrixColorFilter)

        // colorFilter 제거
        imageView.clearTint()
        assertNull(imageView.colorFilter)
    }

    @Test
    fun complexScenario_scaleType() {
        imageView.style {
            fitCenter()
            setTint(android.R.color.white)
        }

        assertEquals(ImageView.ScaleType.FIT_CENTER, imageView.scaleType)
        assertNotNull(imageView.colorFilter)
    }
}
