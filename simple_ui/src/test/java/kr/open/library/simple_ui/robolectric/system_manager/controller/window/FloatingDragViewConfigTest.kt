package kr.open.library.simple_ui.robolectric.system_manager.controller.window

import android.os.Build
import android.view.View
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.system_manager.controller.window.drag.FloatingDragView
import kr.open.library.simple_ui.system_manager.controller.window.drag.FloatingDragViewConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.R])
class FloatingDragViewConfigTest {

    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()

    @Test
    fun `onTouchMove updates layout params and drag state`() {
        val view = View(context)
        val dragView = FloatingDragView(view, startX = 0, startY = 0)
        val config = FloatingDragViewConfig(dragView)

        config.onTouchDown(100f, 200f)

        config.onTouchMove(130f, 250f)

        assertEquals(30, dragView.params.x)
        assertEquals(50, dragView.params.y)
        assertTrue(config.getIsDragging())

        config.onTouchUp()
        assertFalse(config.getIsDragging())
    }

    @Test
    fun `getView exposes underlying view`() {
        val view = View(context)
        val dragView = FloatingDragView(view, startX = 5, startY = 5)
        val config = FloatingDragViewConfig(dragView)

        assertEquals(view, config.getView())
    }

    @Test
    fun `short movement keeps click state`() {
        val view = View(context)
        val dragView = FloatingDragView(view, startX = 0, startY = 0)
        val config = FloatingDragViewConfig(dragView)

        config.onTouchDown(0f, 0f)
        config.onTouchMove(10f, 10f) // below threshold

        assertFalse(config.getIsDragging())
    }
}