package kr.open.library.simple_ui.xml.robolectric.presenter.extensions.view

import android.content.Context
import android.content.ContextWrapper
import android.view.View
import android.view.ViewGroup
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
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows

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
        val view = object : View(baseContext) {
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

        val insets = WindowInsetsCompat.Builder()
            .setInsets(WindowInsetsCompat.Type.systemBars(), androidx.core.graphics.Insets.of(5, 6, 7, 8))
            .build()
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

        val insets = WindowInsetsCompat.Builder()
            .setInsets(WindowInsetsCompat.Type.systemBars(), androidx.core.graphics.Insets.of(5, 6, 7, 8))
            .build()
        ViewCompat.dispatchApplyWindowInsets(view, insets)

        assertEquals(1, view.paddingLeft)
        assertEquals(2, view.paddingTop)
        assertEquals(3, view.paddingRight)
        assertEquals(4, view.paddingBottom)
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
        val view = View(context1)
        val observer = RecordingObserver()

        view.bindLifecycleObserver(observer)
        view.setTag(ViewIds.TAG_OBSERVED_OWNER, context1)

        val view2 = View(context2)
        view2.setTag(ViewIds.TAG_OBSERVED_OWNER, context1)
        view2.bindLifecycleObserver(observer)

        assertSame(view2.getTag(ViewIds.TAG_OBSERVED_OWNER), context2)
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

    private class LifecycleContext(base: Context) : ContextWrapper(base), LifecycleOwner {
        val registry = LifecycleRegistry(this).apply {
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
