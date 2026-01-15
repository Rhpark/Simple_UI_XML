package kr.open.library.simple_ui.xml.robolectric.ui.components.fragment

import kr.open.library.simple_ui.xml.ui.components.fragment.root.RootFragment
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RootFragmentRobolectricTest {
    @Test
    fun requestPermissions_beforeOnCreate_throws() {
        val fragment = TestRootFragment()

        val exception = Assert.assertThrows(IllegalStateException::class.java) {
            fragment.requestPermissions(listOf("android.permission.CAMERA")) { }
        }

        Assert.assertEquals(
            "permissionRequester is not initialized. Please call super.onCreate() first.",
            exception.message
        )
    }

    private class TestRootFragment : RootFragment()
}
