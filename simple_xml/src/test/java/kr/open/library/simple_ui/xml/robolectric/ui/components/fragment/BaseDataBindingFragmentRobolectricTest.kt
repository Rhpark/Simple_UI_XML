package kr.open.library.simple_ui.xml.robolectric.ui.components.fragment

import android.widget.FrameLayout
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentActivity
import kr.open.library.simple_ui.xml.R
import kr.open.library.simple_ui.xml.ui.components.fragment.binding.BaseDataBindingFragment
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BaseDataBindingFragmentRobolectricTest {
    @Test
    fun onViewCreated_setsLifecycleOwner_andOnDestroyView_clearsIt() {
        val activityController = Robolectric.buildActivity(FragmentActivity::class.java).setup()
        val activity = activityController.get()
        activity.setContentView(FrameLayout(activity).apply { id = CONTAINER_ID })

        val fragment = TestBaseDataBindingFragment()
        activity.supportFragmentManager
            .beginTransaction()
            .add(CONTAINER_ID, fragment)
            .commitNow()

        val binding = fragment.exposeBinding()
        Assert.assertEquals(fragment.viewLifecycleOwner, binding.lifecycleOwner)

        activity.supportFragmentManager
            .beginTransaction()
            .remove(fragment)
            .commitNow()

        Assert.assertNull(binding.lifecycleOwner)
    }

    class TestBaseDataBindingFragment : BaseDataBindingFragment<ViewDataBinding>(R.layout.test_databinding_fragment) {
        fun exposeBinding(): ViewDataBinding = getBinding()
    }

    companion object {
        private const val CONTAINER_ID = 2301
    }
}
