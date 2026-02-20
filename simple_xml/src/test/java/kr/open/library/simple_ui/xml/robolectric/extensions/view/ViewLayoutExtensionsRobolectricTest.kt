package kr.open.library.simple_ui.xml.robolectric.extensions.view

import android.content.Context
import android.content.ContextWrapper
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.xml.extensions.view.ViewIds
import kr.open.library.simple_ui.xml.extensions.view.applyWindowInsetsAsPadding
import kr.open.library.simple_ui.xml.extensions.view.bindLifecycleObserver
import kr.open.library.simple_ui.xml.extensions.view.doOnLayout
import kr.open.library.simple_ui.xml.extensions.view.findHostLifecycleOwner
import kr.open.library.simple_ui.xml.extensions.view.getLayoutInflater
import kr.open.library.simple_ui.xml.extensions.view.getLocationOnScreen
import kr.open.library.simple_ui.xml.extensions.view.unbindLifecycleObserver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ViewLayoutExtensionsRobolectricTest {
    private val baseContext: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun getLayoutInflater_inflatesView() {
        val parent = FrameLayout(baseContext)
        val view = parent.getLayoutInflater(android.R.layout.simple_list_item_1, false)

        assertNotNull(view)
        assertSame(parent.context, view.context)
    }

    @Test
    fun bindAndUnbindLifecycleObserver_updatesTag() {
        val context = LifecycleContext(baseContext)
        val view = View(context)
        val observer = RecordingObserver()

        val boundOwner = view.bindLifecycleObserver(observer)
        assertNotNull(boundOwner)
        context.registry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        assertEquals(1, observer.createdCount)

        view.unbindLifecycleObserver(observer)
        context.registry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        assertEquals(0, observer.destroyedCount)
    }

    @Test
    fun doOnLayout_executesImmediatelyWhenLaidOut() {
        val view = View(baseContext)
        view.layout(0, 0, 10, 10)
        var invoked = false

        view.doOnLayout { invoked = true }

        assertEquals(true, invoked)
    }

    @Test
    fun getLocationOnScreen_returnsPair() {
        val view =
            object : View(baseContext) {
                override fun getLocationOnScreen(outLocation: IntArray) {
                    outLocation[0] = 12
                    outLocation[1] = 34
                }
            }

        val (x, y) = view.getLocationOnScreen()
        assertEquals(12, x)
        assertEquals(34, y)
    }

    @Test
    fun applyWindowInsetsAsPadding_updatesPadding() {
        val view = View(baseContext)
        view.setPadding(1, 2, 3, 4)
        view.applyWindowInsetsAsPadding()

        val insets =
            WindowInsetsCompat
                .Builder()
                .setInsets(
                    WindowInsetsCompat.Type.systemBars(),
                    androidx.core.graphics.Insets
                        .of(5, 6, 7, 8),
                ).build()
        ViewCompat.dispatchApplyWindowInsets(view, insets)

        assertEquals(1 + 5, view.paddingLeft)
        assertEquals(2 + 6, view.paddingTop)
        assertEquals(3 + 7, view.paddingRight)
        assertEquals(4 + 8, view.paddingBottom)
    }

    @Test
    fun applyWindowInsetsAsPadding_withFalseFlags_doesNotApplyPadding() {
        val view = View(baseContext)
        view.setPadding(1, 2, 3, 4)
        view.applyWindowInsetsAsPadding(left = false, top = false, right = false, bottom = false)

        val insets =
            WindowInsetsCompat
                .Builder()
                .setInsets(
                    WindowInsetsCompat.Type.systemBars(),
                    androidx.core.graphics.Insets
                        .of(5, 6, 7, 8),
                ).build()
        ViewCompat.dispatchApplyWindowInsets(view, insets)

        assertEquals(1, view.paddingLeft)
        assertEquals(2, view.paddingTop)
        assertEquals(3, view.paddingRight)
        assertEquals(4, view.paddingBottom)
    }

    @Test
    fun applyWindowInsetsAsPadding_bottomInsetNeverShrinksBelowSystemBarInset() {
        val view = View(baseContext)
        view.setPadding(0, 0, 0, 10)
        view.applyWindowInsetsAsPadding()

        val insets =
            WindowInsetsCompat
                .Builder()
                .setInsets(
                    WindowInsetsCompat.Type.systemBars(),
                    androidx.core.graphics.Insets
                        .of(1, 2, 3, 8),
                ).setInsets(
                    WindowInsetsCompat.Type.ime(),
                    androidx.core.graphics.Insets
                        .of(0, 0, 0, 40),
                ).setVisible(
                    WindowInsetsCompat.Type.ime(),
                    true,
                ).build()

        ViewCompat.dispatchApplyWindowInsets(view, insets)

        assertEquals(1, view.paddingLeft)
        assertEquals(2, view.paddingTop)
        assertEquals(3, view.paddingRight)
        assertTrue(view.paddingBottom >= 18)
    }

    @Test
    fun doOnLayout_executesInListenerWhenNotLaidOut() {
        val view = View(baseContext)
        var invoked = false

        view.doOnLayout { invoked = true }

        assertEquals(false, invoked)

        // Trigger layout to invoke the listener
        view.layout(0, 0, 100, 100)
        view.viewTreeObserver.dispatchOnGlobalLayout()

        assertEquals(true, invoked)
    }

    @Test
    fun findHostLifecycleOwner_returnsNull_whenNoOwner() {
        val view = View(baseContext)
        val owner = view.findHostLifecycleOwner()
        assertNull(owner)
    }

    @Test
    fun bindLifecycleObserver_returnsNull_whenNoOwner() {
        val view = View(baseContext)
        val observer = RecordingObserver()
        val result = view.bindLifecycleObserver(observer)
        assertNull(result)
    }

    @Test
    fun bindLifecycleObserver_replacesOldOwner() {
        val context1 = LifecycleContext(baseContext)
        val context2 = LifecycleContext(baseContext)
        val view = View(context2)
        val observer = RecordingObserver()

        @Suppress("UNCHECKED_CAST")
        view.setTag(
            ViewIds.TAG_OBSERVED_OWNER,
            mutableMapOf(observer to context1) as MutableMap<DefaultLifecycleObserver, LifecycleOwner>,
        )

        view.bindLifecycleObserver(observer)

        @Suppress("UNCHECKED_CAST")
        val bindings =
            view.getTag(ViewIds.TAG_OBSERVED_OWNER)
                as? MutableMap<DefaultLifecycleObserver, LifecycleOwner>

        assertNotNull(bindings)
        assertSame(context2, bindings?.get(observer))
    }

    @Test
    fun unbindLifecycleObserver_withExistingOwner_removesObserver() {
        val context = LifecycleContext(baseContext)
        val view = View(context)
        val observer = RecordingObserver()

        view.bindLifecycleObserver(observer)
        context.registry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        assertEquals(1, observer.createdCount)

        view.unbindLifecycleObserver(observer)
        assertNull(view.getTag(ViewIds.TAG_OBSERVED_OWNER))

        context.registry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        assertEquals(0, observer.destroyedCount)
    }

    @Test
    fun unbindLifecycleObserver_withMultipleObservers_removesOnlyTargetObserver() {
        val context = LifecycleContext(baseContext)
        val view = View(context)
        val observerA = RecordingObserver()
        val observerB = RecordingObserver()

        view.bindLifecycleObserver(observerA)
        view.bindLifecycleObserver(observerB)
        view.unbindLifecycleObserver(observerA)

        @Suppress("UNCHECKED_CAST")
        val bindings =
            view.getTag(ViewIds.TAG_OBSERVED_OWNER)
                as? MutableMap<DefaultLifecycleObserver, LifecycleOwner>

        assertNotNull(bindings)
        assertTrue(bindings?.containsKey(observerA) == false)
        assertTrue(bindings?.containsKey(observerB) == true)
    }

    private class LifecycleContext(
        base: Context,
    ) : ContextWrapper(base),
        LifecycleOwner {
        val registry =
            LifecycleRegistry(this).apply {
                currentState = Lifecycle.State.CREATED
            }

        override val lifecycle: Lifecycle
            get() = registry
    }

    private class RecordingObserver : DefaultLifecycleObserver {
        var createdCount = 0
        var destroyedCount = 0

        override fun onCreate(owner: LifecycleOwner) {
            createdCount++
        }

        override fun onDestroy(owner: LifecycleOwner) {
            destroyedCount++
        }
    }

    private class TestLifecycleOwner : LifecycleOwner {
        private val registry = LifecycleRegistry(this)
        override val lifecycle: Lifecycle
            get() = registry
    }
}
