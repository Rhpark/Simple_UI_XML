package kr.open.library.simple_ui.xml.robolectric.ui.components.dialog.normal

import android.view.View
import androidx.fragment.app.FragmentActivity
import kr.open.library.simple_ui.xml.ui.components.dialog.normal.BaseDialogFragment
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLooper

/**
 * Robolectric tests for BaseDialogFragment view lifecycle.<br><br>
 * BaseDialogFragment 뷰 생명주기를 검증하는 Robolectric 테스트입니다.<br>
 */
@RunWith(RobolectricTestRunner::class)
class BaseDialogFragmentRobolectricTest {
    @Test
    fun getRootView_accessible_afterShow() {
        val activity = Robolectric.buildActivity(FragmentActivity::class.java).setup().get()
        val fragment = TestBaseDialogFragment()

        fragment.show(activity.supportFragmentManager, TAG)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        assertNotNull(fragment.exposeRootView())
    }

    @Test
    fun getRootView_throwsIllegalStateException_afterDismiss() {
        val activity = Robolectric.buildActivity(FragmentActivity::class.java).setup().get()
        val fragment = TestBaseDialogFragment()

        fragment.show(activity.supportFragmentManager, TAG)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        fragment.dismissNow()
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        assertThrows(IllegalStateException::class.java) {
            fragment.exposeRootView()
        }
    }

    class TestBaseDialogFragment : BaseDialogFragment(android.R.layout.simple_list_item_1) {
        fun exposeRootView(): View = getRootView()
    }

    private companion object {
        const val TAG = "TestBaseDialogFragment"
    }
}
