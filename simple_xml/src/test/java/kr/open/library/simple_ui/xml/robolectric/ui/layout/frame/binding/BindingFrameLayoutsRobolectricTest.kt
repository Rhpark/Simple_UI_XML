package kr.open.library.simple_ui.xml.robolectric.ui.layout.frame.binding

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentActivity
import kr.open.library.simple_ui.xml.robolectric.ui.components.TestViewBinding
import kr.open.library.simple_ui.xml.ui.layout.frame.binding.BaseDataBindingFrameLayout
import kr.open.library.simple_ui.xml.ui.layout.frame.binding.BaseViewBindingFrameLayout
import kr.open.library.simple_ui.xml.ui.layout.frame.binding.ParentsBindingFrameLayout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.verify
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BindingFrameLayoutsRobolectricTest {
    @Test
    fun parentsBindingFrameLayout_clearsBindingOnDetach() {
        val env = createEnv()
        val view = TestParentsBindingFrameLayout(env.activity, clearBinding = true)

        attachAndLayout(env, view)
        val firstBinding = view.lastBinding

        assertEquals(1, view.createCount)
        assertEquals(1, view.initCount)
        assertEquals(1, view.eventCount)

        env.container.removeView(view)
        attachAndLayout(env, view)

        assertEquals(2, view.createCount)
        assertEquals(2, view.initCount)
        assertEquals(2, view.eventCount)
        assertNotSame(firstBinding, view.lastBinding)
    }

    @Test
    fun parentsBindingFrameLayout_reusesBindingWhenClearFalse() {
        val env = createEnv()
        val view = TestParentsBindingFrameLayout(env.activity, clearBinding = false)

        attachAndLayout(env, view)
        val firstBinding = view.lastBinding

        env.container.removeView(view)
        attachAndLayout(env, view)

        assertEquals(1, view.createCount)
        assertEquals(1, view.initCount)
        assertEquals(2, view.eventCount)
        assertSame(firstBinding, view.lastBinding)
    }

    @Test
    fun baseViewBindingFrameLayout_usesInflateFunction() {
        val env = createEnv()
        val recorder = InflateRecorder()
        val view = TestBaseViewBindingFrameLayout(env.activity, recorder, attachToParent = false)

        attachAndLayout(env, view)

        assertSame(view, recorder.lastParent)
        assertEquals(false, recorder.lastAttach)
        assertEquals(1, view.initCount)
        assertEquals(1, view.eventCount)
    }

    @Test
    fun baseDataBindingFrameLayout_setsLifecycleOwnerAndUnbinds() {
        val env = createEnv()
        mockStatic(DataBindingUtil::class.java).use { mocked ->
            val binding = mock(ViewDataBinding::class.java)
            mocked
                .`when`<ViewDataBinding> {
                    DataBindingUtil.inflate<ViewDataBinding>(
                        any(LayoutInflater::class.java),
                        anyInt(),
                        any(ViewGroup::class.java),
                        anyBoolean(),
                    )
                }.thenReturn(binding)

            val view = TestBaseDataBindingFrameLayout(env.activity, layoutId = 123)
            attachAndLayout(env, view)

            assertEquals(1, view.initCount)
            assertEquals(1, view.eventCount)
            verify(binding).setLifecycleOwner(env.activity)

            env.container.removeView(view)
            verify(binding).unbind()
            verify(binding).setLifecycleOwner(null)
        }
    }

    private fun attachAndLayout(env: TestEnv, view: View) {
        env.container.addView(view)
        view.layout(0, 0, 10, 10)
        view.viewTreeObserver.dispatchOnGlobalLayout()
    }

    private fun createEnv(): TestEnv {
        val activity = Robolectric.buildActivity(FragmentActivity::class.java).setup().get()
        val container = FrameLayout(activity)
        activity.setContentView(container)

        return TestEnv(activity, container)
    }

    private class TestParentsBindingFrameLayout(
        context: Context,
        clearBinding: Boolean,
    ) : ParentsBindingFrameLayout<TestViewBinding>(context) {
        private val clearOnDetach = clearBinding

        var createCount = 0
        var initCount = 0
        var eventCount = 0
        var lastBinding: TestViewBinding? = null

        override val clearBindingOnDetach: Boolean
            get() = clearOnDetach

        override fun createBinding(): TestViewBinding {
            createCount++
            return TestViewBinding(FrameLayout(context)).also { lastBinding = it }
        }

        override fun onInitBind(binding: TestViewBinding) {
            initCount++
        }

        override fun onEventVmCollect(binding: TestViewBinding) {
            eventCount++
        }
    }

    private class TestBaseViewBindingFrameLayout(
        context: Context,
        private val recorder: InflateRecorder,
        attachToParent: Boolean,
    ) : BaseViewBindingFrameLayout<TestViewBinding>(
            context,
            inflate = { inflater, parent, attach ->
                recorder.lastParent = parent
                recorder.lastAttach = attach
                TestViewBinding(FrameLayout(inflater.context))
            },
            attachToParent = attachToParent,
        ) {
        var initCount = 0
        var eventCount = 0

        override fun onInitBind(binding: TestViewBinding) {
            initCount++
        }

        override fun onEventVmCollect(binding: TestViewBinding) {
            eventCount++
        }
    }

    private class TestBaseDataBindingFrameLayout(
        context: Context,
        layoutId: Int,
    ) : BaseDataBindingFrameLayout<ViewDataBinding>(context, layoutId) {
        var initCount = 0
        var eventCount = 0

        override fun onInitBind(binding: ViewDataBinding) {
            initCount++
        }

        override fun onEventVmCollect(binding: ViewDataBinding) {
            eventCount++
        }
    }

    private class InflateRecorder {
        var lastParent: ViewGroup? = null
        var lastAttach: Boolean? = null
    }

    private data class TestEnv(
        val activity: FragmentActivity,
        val container: FrameLayout,
    )
}
