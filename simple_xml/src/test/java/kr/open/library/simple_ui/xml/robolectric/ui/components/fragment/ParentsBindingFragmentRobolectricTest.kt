package kr.open.library.simple_ui.xml.robolectric.ui.components.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity
import kr.open.library.simple_ui.xml.robolectric.ui.components.TestViewBinding
import kr.open.library.simple_ui.xml.ui.components.fragment.binding.ParentsBindingFragment
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ParentsBindingFragmentRobolectricTest {
    @Test
    fun onViewCreated_initializesBinding_andClearsOnDestroy() {
        val activityController = Robolectric.buildActivity(FragmentActivity::class.java).setup()
        val activity = activityController.get()
        activity.setContentView(FrameLayout(activity).apply { id = CONTAINER_ID })

        val fragment = TestParentsBindingFragment()
        activity.supportFragmentManager
            .beginTransaction()
            .add(CONTAINER_ID, fragment)
            .commitNow()

        Assert.assertTrue(fragment.onInitCalled)
        Assert.assertSame(fragment.requireView(), fragment.exposeBinding().root)

        activity.supportFragmentManager
            .beginTransaction()
            .remove(fragment)
            .commitNow()

        val exception = Assert.assertThrows(IllegalStateException::class.java) {
            fragment.exposeBinding()
        }
        Assert.assertEquals("Binding accessed after onDestroyView()", exception.message)
    }

    class TestParentsBindingFragment : ParentsBindingFragment<TestViewBinding>() {
        var onInitCalled = false

        override fun createBinding(
            inflater: LayoutInflater,
            container: ViewGroup?,
            isAttachToParent: Boolean
        ): TestViewBinding = TestViewBinding(FrameLayout(inflater.context).apply { id = ROOT_ID })

        override fun onCreateView(binding: TestViewBinding, savedInstanceState: Bundle?) {
            onInitCalled = true
        }

        fun exposeBinding(): TestViewBinding = getBinding()
    }

    companion object {
        private const val CONTAINER_ID = 2101
        private const val ROOT_ID = 2102
    }
}
