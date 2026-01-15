package kr.open.library.simple_ui.xml.robolectric.ui.components.fragment

import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity
import kr.open.library.simple_ui.xml.robolectric.ui.components.TestViewBinding
import kr.open.library.simple_ui.xml.ui.components.fragment.binding.BaseViewBindingFragment
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BaseViewBindingFragmentRobolectricTest {
    @Test
    fun onCreateView_usesBindingRoot() {
        val activityController = Robolectric.buildActivity(FragmentActivity::class.java).setup()
        val activity = activityController.get()
        activity.setContentView(FrameLayout(activity).apply { id = CONTAINER_ID })

        val fragment = TestBaseViewBindingFragment()
        activity.supportFragmentManager
            .beginTransaction()
            .add(CONTAINER_ID, fragment)
            .commitNow()

        Assert.assertSame(fragment.exposeBinding().root, fragment.requireView())
        Assert.assertEquals(ROOT_ID, fragment.exposeBinding().root.id)
    }

    class TestBaseViewBindingFragment :
        BaseViewBindingFragment<TestViewBinding>({ inflater, _, _ ->
            TestViewBinding(FrameLayout(inflater.context).apply { id = ROOT_ID })
        }) {
        fun exposeBinding(): TestViewBinding = getBinding()
    }

    companion object {
        private const val CONTAINER_ID = 2201
        private const val ROOT_ID = 2202
    }
}
