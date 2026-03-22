package kr.open.library.simple_ui.xml.robolectric.ui.components.dialog.binding

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity
import kr.open.library.simple_ui.xml.robolectric.ui.components.TestViewBinding
import kr.open.library.simple_ui.xml.ui.components.dialog.binding.BaseViewBindingDialogFragment
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLooper

/**
 * Robolectric tests for BaseViewBindingDialogFragment binding lifecycle.<br><br>
 * BaseViewBindingDialogFragment 바인딩 생명주기를 검증하는 Robolectric 테스트입니다.<br>
 */
@RunWith(RobolectricTestRunner::class)
class BaseViewBindingDialogFragmentRobolectricTest {
    @Test
    fun getBinding_returnsBinding_afterShow() {
        val activity = Robolectric.buildActivity(FragmentActivity::class.java).setup().get()
        val fragment = TestBaseViewBindingDialogFragment()

        fragment.show(activity.supportFragmentManager, TAG)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        assertSame(fragment.requireView(), fragment.exposeBinding().root)
    }

    @Test
    fun getBinding_throwsIllegalStateException_afterDismiss() {
        val activity = Robolectric.buildActivity(FragmentActivity::class.java).setup().get()
        val fragment = TestBaseViewBindingDialogFragment()

        fragment.show(activity.supportFragmentManager, TAG)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        fragment.dismissNow()
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        val exception = assertThrows(IllegalStateException::class.java) {
            fragment.exposeBinding()
        }
        assert(exception.message == "Binding accessed after onDestroyView()")
    }

    class TestBaseViewBindingDialogFragment :
        BaseViewBindingDialogFragment<TestViewBinding>(
            inflate = { inflater: LayoutInflater, _: ViewGroup?, _: Boolean ->
                TestViewBinding(FrameLayout(inflater.context).apply { id = ROOT_ID })
            }
        ) {
        fun exposeBinding(): TestViewBinding = getBinding()
    }

    private companion object {
        const val TAG = "TestBaseViewBindingDialogFragment"
        const val ROOT_ID = 3001
    }
}
