package kr.open.library.simple_ui.xml.robolectric.extensions.view

import android.content.Context
import android.graphics.ColorMatrixColorFilter
import android.graphics.PorterDuff
import android.widget.ImageView
import androidx.core.widget.ImageViewCompat
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.xml.extensions.view.centerCrop
import kr.open.library.simple_ui.xml.extensions.view.centerInside
import kr.open.library.simple_ui.xml.extensions.view.clearTint
import kr.open.library.simple_ui.xml.extensions.view.fitCenter
import kr.open.library.simple_ui.xml.extensions.view.fitXY
import kr.open.library.simple_ui.xml.extensions.view.load
import kr.open.library.simple_ui.xml.extensions.view.makeGrayscale
import kr.open.library.simple_ui.xml.extensions.view.removeGrayscale
import kr.open.library.simple_ui.xml.extensions.view.setImageDrawableRes
import kr.open.library.simple_ui.xml.extensions.view.setTint
import kr.open.library.simple_ui.xml.extensions.view.style
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

    // setImageDrawableRes() 테스트
    @Test
    fun setImageDrawableRes_withValidResource_setsDrawable() {
        imageView.setImageDrawableRes(android.R.drawable.ic_delete)

        assertNotNull(imageView.drawable)
    }

    @Test
    fun setImageDrawableRes_returnsImageView() {
        val result = imageView.setImageDrawableRes(android.R.drawable.ic_delete)

        assertEquals(imageView, result)
    }

    // setTint() 테스트 - 시스템 리소스 사용
    @Test
    fun setTint_withSystemColor_setsImageTint() {
        imageView.setTint(android.R.color.black)

        assertNotNull(ImageViewCompat.getImageTintList(imageView))
    }

    @Test
    fun setTint_withDifferentModes_appliesCorrectly() {
        imageView.setTint(android.R.color.black, PorterDuff.Mode.SRC_IN)
        assertNotNull(ImageViewCompat.getImageTintList(imageView))
        assertEquals(PorterDuff.Mode.SRC_IN, ImageViewCompat.getImageTintMode(imageView))

        imageView.setTint(android.R.color.white, PorterDuff.Mode.SRC_ATOP)
        assertNotNull(ImageViewCompat.getImageTintList(imageView))
        assertEquals(PorterDuff.Mode.SRC_ATOP, ImageViewCompat.getImageTintMode(imageView))
    }

    // clearTint() 테스트
    @Test
    fun clearTint_removesImageTint() {
        imageView.setTint(android.R.color.black)
        assertNotNull(ImageViewCompat.getImageTintList(imageView))

        imageView.clearTint()
        assertNull(ImageViewCompat.getImageTintList(imageView))
        assertNull(ImageViewCompat.getImageTintMode(imageView))
    }

    @Test
    fun clearTint_withoutTint_doesNothing() {
        assertNull(ImageViewCompat.getImageTintList(imageView))

        imageView.clearTint()
        assertNull(ImageViewCompat.getImageTintList(imageView))
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
        val result =
            imageView.style {
                centerCrop()
            }

        assertEquals(imageView, result)
    }

    @Test
    fun style_chainingWorks() {
        imageView
            .style {
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
        assertNotNull(ImageViewCompat.getImageTintList(imageView))

        // grayscale 적용 (colorFilter 교체)
        imageView.makeGrayscale()
        assertNotNull(imageView.colorFilter)
        assertTrue(imageView.colorFilter is ColorMatrixColorFilter)

        // colorFilter 제거
        imageView.clearTint()
        assertNull(ImageViewCompat.getImageTintList(imageView))
        assertNotNull(imageView.colorFilter)
    }

    @Test
    fun complexScenario_scaleType() {
        imageView.style {
            fitCenter()
            setTint(android.R.color.white)
        }

        assertEquals(ImageView.ScaleType.FIT_CENTER, imageView.scaleType)
        assertNotNull(ImageViewCompat.getImageTintList(imageView))
    }

    // load() 테스트
    @Test
    fun load_withDrawableOnly_setsDrawable() {
        imageView.load(android.R.drawable.ic_menu_search)

        assertNotNull(imageView.drawable)
    }

    @Test
    fun load_withBlock_appliesTransformations() {
        imageView.load(android.R.drawable.ic_delete) {
            setTint(android.R.color.black)
            centerCrop()
        }

        assertNotNull(imageView.drawable)
        assertNotNull(ImageViewCompat.getImageTintList(imageView))
        assertEquals(ImageView.ScaleType.CENTER_CROP, imageView.scaleType)
    }

    @Test
    fun load_withNullBlock_onlySetsDrawable() {
        imageView.load(android.R.drawable.ic_delete, null)

        assertNotNull(imageView.drawable)
    }

    @Test
    fun load_returnsImageView() {
        val result = imageView.load(android.R.drawable.ic_delete)

        assertEquals(imageView, result)
    }

    @Test
    fun load_chainingWorks() {
        imageView
            .load(android.R.drawable.ic_delete) {
                centerCrop()
            }.load(android.R.drawable.ic_menu_search) {
                fitCenter()
            }

        assertEquals(ImageView.ScaleType.FIT_CENTER, imageView.scaleType)
        assertNotNull(imageView.drawable)
    }
}
