package kr.open.library.simple_ui.xml.robolectric.ui.layout.base.bind

import android.app.Application
import android.view.View
import android.widget.FrameLayout
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.xml.ui.layout.base.bind.bindLifecycleOwnerOnce
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

/**
 * Robolectric tests for one-time DataBinding LifecycleOwner resolution.<br><br>
 * DataBinding LifecycleOwner의 1회 연결을 검증하는 Robolectric 테스트입니다.<br>
 */
@RunWith(RobolectricTestRunner::class)
class DataBindingLifecycleOwnerUtilRobolectricTest {
    @Test
    fun bindLifecycleOwnerOnce_whenContextIsOwner_bindsImmediately() {
        val activityController = Robolectric.buildActivity(FragmentActivity::class.java).setup()
        try {
            val activity = activityController.get()
            val view = View(activity)
            val binding = mock(ViewDataBinding::class.java)

            view.bindLifecycleOwnerOnce(binding)

            verify(binding).setLifecycleOwner(activity)
        } finally {
            activityController.destroy()
        }
    }

    @Test
    fun bindLifecycleOwnerOnce_whenViewTreeOwnerExists_prefersViewTreeOwner() {
        val activityController = Robolectric.buildActivity(FragmentActivity::class.java).setup()
        try {
            val view = View(activityController.get())
            val treeOwner = TestLifecycleOwner()
            val binding = mock(ViewDataBinding::class.java)
            view.setViewTreeLifecycleOwner(treeOwner)

            view.bindLifecycleOwnerOnce(binding)

            verify(binding).setLifecycleOwner(treeOwner)
        } finally {
            activityController.destroy()
        }
    }

    @Test
    fun bindLifecycleOwnerOnce_whenOwnerAppearsBeforeLayout_bindsOnLayout() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val view = View(application)
        val binding = mock(ViewDataBinding::class.java)
        val delayedOwner = TestLifecycleOwner()

        view.bindLifecycleOwnerOnce(binding)
        view.setViewTreeLifecycleOwner(delayedOwner)
        val activityController = Robolectric.buildActivity(FragmentActivity::class.java).setup()
        try {
            val container = FrameLayout(activityController.get())
            activityController.get().setContentView(container)
            container.addView(view)
            layout(view)

            verify(binding).setLifecycleOwner(delayedOwner)
        } finally {
            activityController.destroy()
        }
    }

    @Test
    fun bindLifecycleOwnerOnce_whenLayoutOccursDetached_doesNotBind() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val view = View(application)
        val binding = mock(ViewDataBinding::class.java)
        val owner = TestLifecycleOwner()

        view.bindLifecycleOwnerOnce(binding)
        view.setViewTreeLifecycleOwner(owner)
        layout(view)

        verify(binding, never()).setLifecycleOwner(owner)
    }

    private fun layout(view: View) {
        view.layout(0, 0, 10, 10)
        view.viewTreeObserver.dispatchOnGlobalLayout()
    }

    private class TestLifecycleOwner : LifecycleOwner {
        private val registry = LifecycleRegistry(this)

        override val lifecycle: Lifecycle
            get() = registry
    }
}
