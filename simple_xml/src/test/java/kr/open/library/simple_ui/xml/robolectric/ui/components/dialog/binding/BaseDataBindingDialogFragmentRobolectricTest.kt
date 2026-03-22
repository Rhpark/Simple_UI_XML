package kr.open.library.simple_ui.xml.robolectric.ui.components.dialog.binding

import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentActivity
import kr.open.library.simple_ui.xml.R
import kr.open.library.simple_ui.xml.ui.components.dialog.binding.BaseDataBindingDialogFragment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLooper

/**
 * Robolectric tests for BaseDataBindingDialogFragment lifecycleOwner lifecycle.<br><br>
 * BaseDataBindingDialogFragment lifecycleOwner 생명주기를 검증하는 Robolectric 테스트입니다.<br>
 */
@RunWith(RobolectricTestRunner::class)
class BaseDataBindingDialogFragmentRobolectricTest {
    @Test
    fun onViewCreated_setsLifecycleOwner_andOnDestroyView_clearsIt() {
        val activity = Robolectric.buildActivity(FragmentActivity::class.java).setup().get()
        val fragment = TestBaseDataBindingDialogFragment()

        fragment.show(activity.supportFragmentManager, TAG)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        val binding = fragment.exposeBinding()
        assertEquals(fragment.viewLifecycleOwner, binding.lifecycleOwner)

        fragment.dismissNow()
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        assertNull(binding.lifecycleOwner)
    }

    class TestBaseDataBindingDialogFragment : BaseDataBindingDialogFragment<ViewDataBinding>(R.layout.test_databinding_fragment) {
        fun exposeBinding(): ViewDataBinding = getBinding()
    }

    private companion object {
        const val TAG = "TestBaseDataBindingDialogFragment"
    }
}
