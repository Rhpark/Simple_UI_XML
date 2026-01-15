package kr.open.library.simple_ui.xml.robolectric.ui.components.activity

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import kr.open.library.simple_ui.xml.robolectric.ui.components.TestViewBinding
import kr.open.library.simple_ui.xml.ui.components.activity.binding.BaseViewBindingActivity
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BaseViewBindingActivityRobolectricTest {
    @Test
    fun onCreate_setsContentView_fromBindingRoot() {
        val controller = Robolectric.buildActivity(TestBaseViewBindingActivity::class.java).setup()
        val activity = controller.get()

        Assert.assertNotNull(activity.findViewById<View>(ROOT_ID))
    }

    private class TestBaseViewBindingActivity :
        BaseViewBindingActivity<TestViewBinding>({ inflater ->
            TestViewBinding(FrameLayout(inflater.context).apply { id = ROOT_ID })
        }) {
        override fun beforeOnCreated(savedInstanceState: Bundle?) {
            setTheme(androidx.appcompat.R.style.Theme_AppCompat)
        }
    }

    companion object {
        private const val ROOT_ID = 1101
    }
}
