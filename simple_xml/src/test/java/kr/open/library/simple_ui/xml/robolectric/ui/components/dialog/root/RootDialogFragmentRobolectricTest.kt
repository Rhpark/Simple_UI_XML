package kr.open.library.simple_ui.xml.robolectric.ui.components.dialog.root

import android.view.View
import kr.open.library.simple_ui.xml.ui.components.dialog.root.RootDialogFragment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RootDialogFragmentRobolectricTest {
    @Test
    fun `setBackgroundResource stores drawable id and clears color`() {
        val fragment = TestRootDialogFragment()
        fragment.setBackgroundColor(0x123456)

        fragment.setBackgroundResource(TEST_DRAWABLE_RES_ID)

        assertEquals(TEST_DRAWABLE_RES_ID, fragment.getBackgroundResIdForTest())
        assertNull(fragment.getBackgroundColorForTest())
    }

    @Test
    fun `setBackgroundResource with rootView applies drawable resource`() {
        val fragment = TestRootDialogFragment()
        val rootView = mock(View::class.java)

        fragment.callProtectedSetBackgroundResourceForTest(TEST_DRAWABLE_RES_ID, rootView)

        verify(rootView).setBackgroundResource(TEST_DRAWABLE_RES_ID)
        verify(rootView, never()).setBackgroundColor(TEST_DRAWABLE_RES_ID)
        assertEquals(TEST_DRAWABLE_RES_ID, fragment.getBackgroundResIdForTest())
        assertNull(fragment.getBackgroundColorForTest())
    }

    private class TestRootDialogFragment : RootDialogFragment() {
        fun getBackgroundResIdForTest(): Int? = config.getBackgroundResId()

        fun getBackgroundColorForTest(): Int? = config.getBackgroundColor()

        fun callProtectedSetBackgroundResourceForTest(resId: Int, rootView: View?) {
            setBackgroundResource(resId, rootView)
        }
    }

    private companion object {
        const val TEST_DRAWABLE_RES_ID = 1001
    }
}
