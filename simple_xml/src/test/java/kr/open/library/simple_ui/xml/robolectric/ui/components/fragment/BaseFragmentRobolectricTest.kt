package kr.open.library.simple_ui.xml.robolectric.ui.components.fragment

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity
import kr.open.library.simple_ui.xml.ui.components.fragment.normal.BaseFragment
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BaseFragmentRobolectricTest {
    @Test
    fun rootView_accessible_beforeDestroy_andThrowsAfterDestroy() {
        val activityController = Robolectric.buildActivity(FragmentActivity::class.java).setup()
        val activity = activityController.get()
        activity.setContentView(FrameLayout(activity).apply { id = CONTAINER_ID })

        val fragment = TestBaseFragment()
        activity.supportFragmentManager
            .beginTransaction()
            .add(CONTAINER_ID, fragment)
            .commitNow()

        Assert.assertSame(fragment.requireView(), fragment.exposeRootView())

        activity.supportFragmentManager
            .beginTransaction()
            .remove(fragment)
            .commitNow()

        Assert.assertThrows(IllegalStateException::class.java) {
            fragment.exposeRootView()
        }
    }

    class TestBaseFragment : BaseFragment(android.R.layout.simple_list_item_1) {
        fun exposeRootView(): View = getRootView()

        override fun onCreateView(rootView: View, savedInstanceState: Bundle?) {
        }
    }

    companion object {
        private const val CONTAINER_ID = 2001
    }
}
