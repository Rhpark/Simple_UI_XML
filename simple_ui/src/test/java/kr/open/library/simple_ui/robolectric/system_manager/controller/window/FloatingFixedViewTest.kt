package kr.open.library.simple_ui.robolectric.system_manager.controller.window

import android.graphics.PixelFormat
import android.os.Build
import android.view.View
import android.view.WindowManager
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.system_manager.controller.window.fixed.FloatingFixedView
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.util.ReflectionHelpers

@RunWith(RobolectricTestRunner::class)
class FloatingFixedViewTest {

    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun `params use overlay type on modern devices`() {
        val view = View(context)
        val floatingView = FloatingFixedView(view, startX = 12, startY = 34)

        val params = floatingView.params
        assertEquals(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, params.type)
        assertEquals(12, params.x)
        assertEquals(34, params.y)
        assertEquals(PixelFormat.TRANSLUCENT, params.format)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun `getRect uses measured width when view has not been laid out`() {
        val view = View(context)
        val floatingView = FloatingFixedView(view, startX = 5, startY = 7)

        val widthSpec = View.MeasureSpec.makeMeasureSpec(80, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(40, View.MeasureSpec.EXACTLY)
        view.measure(widthSpec, heightSpec)

        val rect = floatingView.getRect()
        assertEquals(5, rect.left)
        assertEquals(7, rect.top)
        assertEquals(85, rect.right)
        assertEquals(47, rect.bottom)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun `getRect uses actual width and height when view is laid out`() {
        val view = View(context)
        val floatingView = FloatingFixedView(view, startX = 10, startY = 15)

        val widthSpec = View.MeasureSpec.makeMeasureSpec(100, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(50, View.MeasureSpec.EXACTLY)
        view.measure(widthSpec, heightSpec)
        view.layout(0, 0, 100, 50)

        val rect = floatingView.getRect()
        assertEquals(10, rect.left)
        assertEquals(15, rect.top)
        assertEquals(110, rect.right)
        assertEquals(65, rect.bottom)
    }
}