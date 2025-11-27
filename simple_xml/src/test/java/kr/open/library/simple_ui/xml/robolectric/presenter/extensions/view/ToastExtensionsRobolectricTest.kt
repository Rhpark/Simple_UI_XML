package kr.open.library.simple_ui.xml.robolectric.presenter.extensions.view

import android.content.Context
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.xml.extensions.view.toastLong
import kr.open.library.simple_ui.xml.extensions.view.toastShort
import kr.open.library.simple_ui.xml.extensions.view.toastShowLong
import kr.open.library.simple_ui.xml.extensions.view.toastShowShort
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowToast

/**
 * Robolectric tests for Toast extension functions
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ToastExtensionsRobolectricTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    // ========================================
    // Context.toastShort() Tests
    // ========================================

    @Test
    fun toastShort_createsShortToast() {
        val message = "Short Toast"
        val toast = context.toastShort(message)

        assertEquals(Toast.LENGTH_SHORT, toast.duration)
    }

    @Test
    fun toastShort_withEmptyMessage_createsToast() {
        val toast = context.toastShort("")

        assertEquals(Toast.LENGTH_SHORT, toast.duration)
    }

    // ========================================
    // Context.toastLong() Tests
    // ========================================

    @Test
    fun toastLong_createsLongToast() {
        val message = "Long Toast"
        val toast = context.toastLong(message)

        assertEquals(Toast.LENGTH_LONG, toast.duration)
    }

    @Test
    fun toastLong_withLongMessage_createsToast() {
        val longMessage = "a".repeat(1000)
        val toast = context.toastLong(longMessage)

        assertEquals(Toast.LENGTH_LONG, toast.duration)
    }

    // ========================================
    // Context.toastShowShort() Tests
    // ========================================

    @Test
    fun toastShowShort_showsShortToast() {
        val message = "Show Short"
        context.toastShowShort(message)

        val latestToast = ShadowToast.getLatestToast()
        assertEquals(Toast.LENGTH_SHORT, latestToast.duration)
        assertEquals(message, ShadowToast.getTextOfLatestToast())
    }

    @Test
    fun toastShowShort_withSpecialCharacters_showsToast() {
        val message = "Hello\nWorld\t!"
        context.toastShowShort(message)

        assertEquals(message, ShadowToast.getTextOfLatestToast())
    }

    // ========================================
    // Context.toastShowLong() Tests
    // ========================================

    @Test
    fun toastShowLong_showsLongToast() {
        val message = "Show Long"
        context.toastShowLong(message)

        val latestToast = ShadowToast.getLatestToast()
        assertEquals(Toast.LENGTH_LONG, latestToast.duration)
        assertEquals(message, ShadowToast.getTextOfLatestToast())
    }

    @Test
    fun toastShowLong_withNumbers_showsToast() {
        val message = "Count: 12345"
        context.toastShowLong(message)

        assertEquals(message, ShadowToast.getTextOfLatestToast())
    }

    // ========================================
    // Fragment Toast Tests
    // ========================================

    @Test
    fun fragment_toastShowShort_withContext_showsToast() {
        val activity = Robolectric.buildActivity(FragmentActivity::class.java).create().start().resume().get()
        val fragment = Fragment()
        activity.supportFragmentManager.beginTransaction()
            .add(fragment, "test")
            .commitNow()

        val message = "Fragment Short"
        fragment.toastShowShort(message)

        assertEquals(message, ShadowToast.getTextOfLatestToast())
    }

    @Test
    fun fragment_toastShowLong_withContext_showsToast() {
        val activity = Robolectric.buildActivity(FragmentActivity::class.java).create().start().resume().get()
        val fragment = Fragment()
        activity.supportFragmentManager.beginTransaction()
            .add(fragment, "test")
            .commitNow()

        val message = "Fragment Long"
        fragment.toastShowLong(message)

        assertEquals(message, ShadowToast.getTextOfLatestToast())
    }

    @Test
    fun fragment_toastShowShort_withoutContext_doesNotCrash() {
        val fragment = Fragment() // No context

        // Should not crash, just log error
        fragment.toastShowShort("Test")

        // No assertion needed - just verify no crash
    }

    @Test
    fun fragment_toastShowLong_withoutContext_doesNotCrash() {
        val fragment = Fragment() // No context

        // Should not crash, just log error
        fragment.toastShowLong("Test")

        // No assertion needed - just verify no crash
    }

    // ========================================
    // Multiple Toast Tests
    // ========================================

    @Test
    fun multipleToasts_showsLatestToast() {
        context.toastShowShort("First")
        context.toastShowShort("Second")
        context.toastShowShort("Third")

        assertEquals("Third", ShadowToast.getTextOfLatestToast())
    }

    @Test
    fun shortAndLongToasts_showsLatestToast() {
        context.toastShowShort("Short")
        context.toastShowLong("Long")

        val latestToast = ShadowToast.getLatestToast()
        assertEquals(Toast.LENGTH_LONG, latestToast.duration)
        assertEquals("Long", ShadowToast.getTextOfLatestToast())
    }
}
