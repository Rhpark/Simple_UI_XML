package kr.open.library.simple_ui.xml.robolectric.ui.components.dialog.root

import android.view.View
import androidx.fragment.app.FragmentActivity
import kr.open.library.simple_ui.xml.ui.components.dialog.root.RootDialogFragment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLooper

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

    @Test
    fun setCancelableDialog_false_makesCancelableFalse() {
        val activity = Robolectric.buildActivity(FragmentActivity::class.java).setup().get()
        val fragment = TestRootDialogFragment()

        fragment.show(activity.supportFragmentManager, TAG)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        assertTrue(fragment.isCancelable)

        fragment.setCancelableDialogForTest(false)

        assertFalse(fragment.isCancelable)
    }

    @Test
    fun setCancelableDialog_true_makesCancelableTrue() {
        val activity = Robolectric.buildActivity(FragmentActivity::class.java).setup().get()
        val fragment = TestRootDialogFragment()

        fragment.show(activity.supportFragmentManager, TAG)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        fragment.setCancelableDialogForTest(false)
        assertFalse(fragment.isCancelable)

        fragment.setCancelableDialogForTest(true)
        assertTrue(fragment.isCancelable)
    }

    private class TestRootDialogFragment : RootDialogFragment() {
        fun getBackgroundResIdForTest(): Int? = config.getBackgroundResId()

        fun getBackgroundColorForTest(): Int? = config.getBackgroundColor()

        fun callProtectedSetBackgroundResourceForTest(resId: Int, rootView: View?) {
            setBackgroundResource(resId, rootView)
        }

        fun setCancelableDialogForTest(cancelable: Boolean) {
            setCancelableDialog(cancelable)
        }
    }

    private companion object {
        const val TEST_DRAWABLE_RES_ID = 1001
        const val TAG = "TestRootDialogFragment"
    }
}
