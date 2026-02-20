package kr.open.library.simple_ui.xml.robolectric.system_manager.extensions

import android.app.Activity
import kr.open.library.simple_ui.xml.BuildConfig
import kr.open.library.simple_ui.xml.R
import kr.open.library.simple_ui.xml.system_manager.extensions.destroySystemBarControllerCache
import kr.open.library.simple_ui.xml.system_manager.extensions.getSystemBarController
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.atomic.AtomicReference

@RunWith(RobolectricTestRunner::class)
class SystemBarControllerExtensionsRobolectricTest {
    private lateinit var activity: Activity

    @Before
    fun setUp() {
        activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        activity.window.decorView.layout(0, 0, 1080, 1920)
    }

    @Test
    fun `getSystemBarController caches one controller per window`() {
        val first = activity.window.getSystemBarController()
        val second = activity.window.getSystemBarController()

        assertSame(first, second)
        assertSame(first, activity.window.decorView.getTag(R.id.tag_system_bar_controller))
    }

    @Test
    fun `destroySystemBarControllerCache clears cache and recreates controller`() {
        val first = activity.window.getSystemBarController()

        activity.window.destroySystemBarControllerCache()
        assertNull(activity.window.decorView.getTag(R.id.tag_system_bar_controller))

        val second = activity.window.getSystemBarController()
        assertNotSame(first, second)
        assertSame(second, activity.window.decorView.getTag(R.id.tag_system_bar_controller))
    }

    @Test
    fun `destroySystemBarControllerCache is safe when cache is empty`() {
        activity.window.destroySystemBarControllerCache()
        assertNull(activity.window.decorView.getTag(R.id.tag_system_bar_controller))
    }

    @Test
    fun `window extension throws off main thread in debug`() {
        val thrown = AtomicReference<Throwable?>(null)

        val worker =
            Thread {
                try {
                    activity.window.getSystemBarController()
                } catch (t: Throwable) {
                    thrown.set(t)
                }
            }

        worker.start()
        worker.join()

        if (BuildConfig.DEBUG) {
            assertTrue(thrown.get() is IllegalStateException)
        } else {
            assertTrue(thrown.get() == null)
        }
    }
}
