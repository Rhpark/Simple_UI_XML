package kr.open.library.simple_ui.xml.robolectric.ui.components.activity

import android.os.Bundle
import android.widget.FrameLayout
import kr.open.library.simple_ui.xml.robolectric.ui.components.TestViewBinding
import kr.open.library.simple_ui.xml.ui.components.activity.binding.ParentsBindingActivity
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ParentsBindingActivityRobolectricTest {
    @Test
    fun getBinding_beforeOnCreate_throws() {
        val controller = Robolectric.buildActivity(TestParentsBindingActivity::class.java)
        val activity = controller.get()

        val exception = Assert.assertThrows(IllegalStateException::class.java) {
            activity.exposeBinding()
        }

        Assert.assertEquals(
            "Binding is not initialized. Please call super.onCreate() first.",
            exception.message
        )
    }

    @Test
    fun onCreate_initializesBinding_and_callsOnInitBind() {
        val controller = Robolectric.buildActivity(TestParentsBindingActivity::class.java).setup()
        val activity = controller.get()

        Assert.assertTrue(activity.onInitCalled)
        val binding = activity.exposeBinding()
        Assert.assertEquals(ROOT_ID, binding.root.id)
    }

    private class TestParentsBindingActivity : ParentsBindingActivity<TestViewBinding>() {
        var onInitCalled = false

        override fun createBinding(): TestViewBinding = TestViewBinding(FrameLayout(this).apply { id = ROOT_ID })

        override fun beforeOnCreated(savedInstanceState: Bundle?) {
            setTheme(androidx.appcompat.R.style.Theme_AppCompat)
        }

        override fun onCreate(binding: TestViewBinding, savedInstanceState: Bundle?) {
            onInitCalled = true
        }

        fun exposeBinding(): TestViewBinding = getBinding()
    }

    companion object {
        private const val ROOT_ID = 1001
    }
}
