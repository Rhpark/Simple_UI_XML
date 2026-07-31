package kr.open.library.simple_ui.xml.robolectric.ui.internal.window

import android.app.Activity
import android.os.Build
import android.util.DisplayMetrics
import kr.open.library.simple_ui.xml.ui.internal.window.DialogWindowSize
import kr.open.library.simple_ui.xml.ui.internal.window.resolveDialogWindowSize
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class DialogWindowSizeRobolectricTest {
    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun resolveDialogWindowSize_api30_usesCurrentWindowMetrics() {
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        val bounds = activity.windowManager.currentWindowMetrics.bounds

        val result = resolveDialogWindowSize(activity.window, activity)

        assertEquals(DialogWindowSize(bounds.width(), bounds.height()), result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun resolveDialogWindowSize_api28_usesMeasuredDecorViewFirst() {
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        activity.window.decorView.layout(0, 0, 320, 640)

        val result = resolveDialogWindowSize(activity.window, activity)

        assertEquals(DialogWindowSize(width = 320, height = 640), result)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun resolveDialogWindowSize_api28_fallsBackToDisplayMetrics() {
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        activity.window.decorView.layout(0, 0, 0, 0)
        val expected = DisplayMetrics().also { metrics ->
            @Suppress("DEPRECATION")
            activity.windowManager.defaultDisplay.getMetrics(metrics)
        }

        val result = resolveDialogWindowSize(activity.window, activity)

        assertNotNull(result)
        assertTrue(expected.widthPixels > 0)
        assertTrue(expected.heightPixels > 0)
        assertEquals(DialogWindowSize(expected.widthPixels, expected.heightPixels), result)
    }
}
