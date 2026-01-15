package kr.open.library.simple_ui.xml.robolectric.ui.components.activity

import kr.open.library.simple_ui.xml.ui.components.activity.root.RootActivity
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RootActivityRobolectricTest {
    @Test
    fun requestPermissions_beforeOnCreate_throws() {
        val controller = Robolectric.buildActivity(TestRootActivity::class.java)
        val activity = controller.get()

        val exception = Assert.assertThrows(IllegalStateException::class.java) {
            activity.requestPermissions(listOf("android.permission.CAMERA")) { }
        }

        Assert.assertEquals(
            "PermissionRequester is not initialized. Please call super.onCreate() first.",
            exception.message
        )
    }

    private class TestRootActivity : RootActivity()
}
