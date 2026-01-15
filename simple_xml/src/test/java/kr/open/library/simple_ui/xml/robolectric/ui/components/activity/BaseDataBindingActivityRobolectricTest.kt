package kr.open.library.simple_ui.xml.robolectric.ui.components.activity

import android.os.Bundle
import android.view.View
import androidx.databinding.ViewDataBinding
import kr.open.library.simple_ui.xml.R
import kr.open.library.simple_ui.xml.ui.components.activity.binding.BaseDataBindingActivity
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BaseDataBindingActivityRobolectricTest {
    @Test
    fun onCreate_setsLifecycleOwner_andInflatesLayout() {
        val controller = Robolectric.buildActivity(TestBaseDataBindingActivity::class.java).setup()
        val activity = controller.get()

        Assert.assertNotNull(activity.findViewById<View>(R.id.test_databinding_activity_root))
        Assert.assertEquals(activity, activity.exposeBinding().lifecycleOwner)
    }

    private class TestBaseDataBindingActivity : BaseDataBindingActivity<ViewDataBinding>(R.layout.test_databinding_activity) {
        override fun beforeOnCreated(savedInstanceState: Bundle?) {
            setTheme(androidx.appcompat.R.style.Theme_AppCompat)
        }

        fun exposeBinding(): ViewDataBinding = getBinding()
    }
}
