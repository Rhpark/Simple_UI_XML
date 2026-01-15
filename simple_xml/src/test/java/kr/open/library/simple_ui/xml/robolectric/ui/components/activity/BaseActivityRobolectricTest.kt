package kr.open.library.simple_ui.xml.robolectric.ui.components.activity

import android.os.Bundle
import android.view.View
import kr.open.library.simple_ui.xml.ui.components.activity.normal.BaseActivity
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BaseActivityRobolectricTest {
    @Test
    fun onCreate_setsContentView() {
        val controller = Robolectric.buildActivity(TestBaseActivity::class.java).setup()
        val activity = controller.get()

        Assert.assertNotNull(activity.findViewById<View>(android.R.id.text1))
    }

    private class TestBaseActivity : BaseActivity(android.R.layout.simple_list_item_1) {
        override fun beforeOnCreated(savedInstanceState: Bundle?) {
            setTheme(androidx.appcompat.R.style.Theme_AppCompat)
        }
    }
}
