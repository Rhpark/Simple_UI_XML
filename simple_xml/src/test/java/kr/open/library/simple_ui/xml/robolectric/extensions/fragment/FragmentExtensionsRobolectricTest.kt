package kr.open.library.simple_ui.xml.robolectric.extensions.fragment

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import kr.open.library.simple_ui.xml.extensions.fragment.withContext
import kr.open.library.simple_ui.xml.extensions.fragment.withContextResult
import kr.open.library.simple_ui.xml.extensions.fragment.withView
import kr.open.library.simple_ui.xml.extensions.fragment.withViewResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

/**
 * Robolectric tests for Fragment context and view guard extensions.<br><br>
 * Fragment의 context와 view 경계를 보호하는 확장 함수의 Robolectric 테스트입니다.<br>
 */
@RunWith(RobolectricTestRunner::class)
class FragmentExtensionsRobolectricTest {
    @Test
    fun withContext_whenFragmentIsDetached_returnsFalseWithoutInvokingBlock() {
        val fragment = Fragment()
        var blockInvoked = false

        val executed = fragment.withContext {
            blockInvoked = true
        }

        assertFalse(executed)
        assertFalse(blockInvoked)
    }

    @Test
    fun withContext_whenFragmentIsAttached_returnsTrueAndPassesContext() {
        withAttachedFragment(hasView = false) { activity, fragment ->
            var receivedContext: Context? = null

            val executed = fragment.withContext { context ->
                receivedContext = context
            }

            assertTrue(executed)
            assertSame(activity, receivedContext)
        }
    }

    @Test
    fun withContextResult_whenFragmentIsDetached_returnsNullWithoutInvokingBlock() {
        val fragment = Fragment()
        var blockInvoked = false

        val result = fragment.withContextResult {
            blockInvoked = true
            it.packageName
        }

        assertNull(result)
        assertFalse(blockInvoked)
    }

    @Test
    fun withContextResult_whenFragmentIsAttached_returnsBlockResult() {
        withAttachedFragment(hasView = false) { activity, fragment ->
            val result = fragment.withContextResult { context -> context.packageName }

            assertEquals(activity.packageName, result)
        }
    }

    @Test
    fun withView_whenAttachedFragmentHasNoView_returnsFalseWithoutInvokingBlock() {
        withAttachedFragment(hasView = false) { _, fragment ->
            var blockInvoked = false

            val executed = fragment.withView {
                blockInvoked = true
            }

            assertFalse(executed)
            assertFalse(blockInvoked)
        }
    }

    @Test
    fun withView_whenFragmentViewExists_returnsTrueAndPassesView() {
        withAttachedFragment(hasView = true) { _, fragment ->
            var receivedView: View? = null

            val executed = fragment.withView { view ->
                receivedView = view
            }

            assertTrue(executed)
            assertSame(fragment.requireView(), receivedView)
        }
    }

    @Test
    fun withViewResult_whenAttachedFragmentHasNoView_returnsNullWithoutInvokingBlock() {
        withAttachedFragment(hasView = false) { _, fragment ->
            var blockInvoked = false

            val result = fragment.withViewResult {
                blockInvoked = true
                it.id
            }

            assertNull(result)
            assertFalse(blockInvoked)
        }
    }

    @Test
    fun withViewResult_whenFragmentViewExists_returnsBlockResult() {
        withAttachedFragment(hasView = true) { _, fragment ->
            val result = fragment.withViewResult { view -> view.id }

            assertEquals(android.R.id.text1, result)
        }
    }

    private fun withAttachedFragment(
        hasView: Boolean,
        block: (FragmentActivity, Fragment) -> Unit,
    ) {
        val activityController = Robolectric.buildActivity(FragmentActivity::class.java).setup()
        try {
            val activity = activityController.get()
            val fragment = if (hasView) {
                activity.setContentView(FrameLayout(activity).apply { id = CONTAINER_ID })
                Fragment(android.R.layout.simple_list_item_1)
            } else {
                Fragment()
            }
            val transaction = activity.supportFragmentManager.beginTransaction()
            if (hasView) {
                transaction.add(CONTAINER_ID, fragment)
            } else {
                transaction.add(fragment, "context-only-fragment")
            }
            transaction.commitNow()

            block(activity, fragment)
        } finally {
            activityController.destroy()
        }
    }

    private companion object {
        const val CONTAINER_ID = 2101
    }
}
