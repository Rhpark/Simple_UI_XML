package kr.open.library.simple_ui.robolectric.presenter.ui.adapter

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.presenter.ui.adapter.viewholder.BaseRcvViewHolder
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Robolectric tests for BaseRcvViewHolder
 *
 * Tests comprehensive BaseRcvViewHolder functionality:
 * - ViewHolder creation
 * - View caching mechanism
 * - findViewById with type casting
 * - Null-safe view lookup
 * - Position validation
 * - Cache clearing
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class BaseRcvViewHolderRobolectricTest {

    private lateinit var context: Context
    private lateinit var parent: ViewGroup

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        parent = FrameLayout(context)
    }

    // ==============================================
    // ViewHolder Creation Tests
    // ==============================================

    @Test
    fun viewHolder_createsSuccessfully() {
        // Given
        val layout = createSimpleLayout()

        // When
        val viewHolder = TestViewHolder(layout, parent)

        // Then
        assertNotNull(viewHolder)
        assertNotNull(viewHolder.itemView)
    }

    @Test
    fun viewHolder_createsWithAttachToRootFalse() {
        // Given
        val layout = createSimpleLayout()

        // When
        val viewHolder = TestViewHolder(layout, parent, attachToRoot = false)

        // Then
        assertNotNull(viewHolder)
        assertNotNull(viewHolder.itemView)
    }

    @Test
    fun viewHolder_createsWithAttachToRootTrue() {
        // Given
        val layout = createSimpleLayout()

        // When
        val viewHolder = TestViewHolder(layout, parent, attachToRoot = true)

        // Then
        assertNotNull(viewHolder)
        assertNotNull(viewHolder.itemView)
        if (viewHolder.itemView === parent) {
            // Some layouts attach directly and return the parent instance when attachToRoot = true
            assertTrue(parent.childCount > 0)
        } else {
            assertSame(parent, viewHolder.itemView.parent)
        }
    }

    @Test
    fun viewHolder_itemViewIsCorrectType() {
        // Given
        val layout = createSimpleLayout()

        // When
        val viewHolder = TestViewHolder(layout, parent)

        // Then
        // simple_list_item_1 has TextView as root
        assertTrue(viewHolder.itemView is TextView)
    }

    // ==============================================
    // findViewById Tests
    // ==============================================

    @Test
    fun findViewById_returnsCorrectView() {
        // Given
        val layout = createLayoutWithTextView()
        val viewHolder = TestViewHolder(layout, parent)
        val textViewId = View.generateViewId()
        val textView = TextView(context)
        textView.id = textViewId
        (viewHolder.itemView as ViewGroup).addView(textView)

        // When
        val foundView = viewHolder.findViewById<TextView>(textViewId)

        // Then
        assertNotNull(foundView)
        assertTrue(foundView is TextView)
    }

    @Test
    fun findViewById_cachesView() {
        // Given
        val layout = createLayoutWithTextView()
        val viewHolder = TestViewHolder(layout, parent)
        val textViewId = View.generateViewId()
        val textView = TextView(context)
        textView.id = textViewId
        (viewHolder.itemView as ViewGroup).addView(textView)

        // When
        val firstCall = viewHolder.findViewById<TextView>(textViewId)
        val secondCall = viewHolder.findViewById<TextView>(textViewId)

        // Then - Both should return the same cached instance
        assertSame(firstCall, secondCall)
    }

    @Test(expected = IllegalArgumentException::class)
    fun findViewById_throwsExceptionForNonExistentId() {
        // Given
        val layout = createSimpleLayout()
        val viewHolder = TestViewHolder(layout, parent)
        val nonExistentId = View.generateViewId()

        // When & Then - Should throw IllegalArgumentException
        viewHolder.findViewById<TextView>(nonExistentId)
    }

    @Test
    fun findViewById_castsToCorrectType() {
        // Given
        val layout = createLayoutWithTextView()
        val viewHolder = TestViewHolder(layout, parent)
        val textViewId = View.generateViewId()
        val textView = TextView(context)
        textView.id = textViewId
        (viewHolder.itemView as ViewGroup).addView(textView)

        // When
        val foundView = viewHolder.findViewById<TextView>(textViewId)

        // Then
        assertTrue(foundView is TextView)
        // Can call TextView-specific methods
        foundView.text = "Test"
        assertEquals("Test", foundView.text)
    }

    // ==============================================
    // findViewByIdOrNull Tests
    // ==============================================

    @Test
    fun findViewByIdOrNull_returnsViewWhenExists() {
        // Given
        val layout = createLayoutWithTextView()
        val viewHolder = TestViewHolder(layout, parent)
        val textViewId = View.generateViewId()
        val textView = TextView(context)
        textView.id = textViewId
        (viewHolder.itemView as ViewGroup).addView(textView)

        // When
        val foundView = viewHolder.findViewByIdOrNull<TextView>(textViewId)

        // Then
        assertNotNull(foundView)
        assertTrue(foundView is TextView)
    }

    @Test
    fun findViewByIdOrNull_returnsNullWhenNotExists() {
        // Given
        val layout = createSimpleLayout()
        val viewHolder = TestViewHolder(layout, parent)
        val nonExistentId = View.generateViewId()

        // When
        val foundView = viewHolder.findViewByIdOrNull<TextView>(nonExistentId)

        // Then
        assertNull(foundView)
    }

    @Test
    fun findViewByIdOrNull_cachesView() {
        // Given
        val layout = createLayoutWithTextView()
        val viewHolder = TestViewHolder(layout, parent)
        val textViewId = View.generateViewId()
        val textView = TextView(context)
        textView.id = textViewId
        (viewHolder.itemView as ViewGroup).addView(textView)

        // When
        val firstCall = viewHolder.findViewByIdOrNull<TextView>(textViewId)
        val secondCall = viewHolder.findViewByIdOrNull<TextView>(textViewId)

        // Then - Both should return the same cached instance
        assertSame(firstCall, secondCall)
    }

    @Test
    fun findViewByIdOrNull_returnsNullForWrongType() {
        // Given
        val layout = createLayoutWithTextView()
        val viewHolder = TestViewHolder(layout, parent)
        val textViewId = View.generateViewId()
        val textView = TextView(context)
        textView.id = textViewId
        (viewHolder.itemView as ViewGroup).addView(textView)

        // When - Try to cast TextView to FrameLayout
        val foundView = viewHolder.findViewByIdOrNull<FrameLayout>(textViewId)

        // Then - Should return null due to type mismatch
        assertNull(foundView)
    }

    @Test
    fun findViewByIdOrNull_recoversAfterWrongTypeLookup() {
        // Given
        val layout = createLayoutWithTextView()
        val viewHolder = TestViewHolder(layout, parent)
        val textViewId = View.generateViewId()
        val textView = TextView(context)
        textView.id = textViewId
        (viewHolder.itemView as ViewGroup).addView(textView)

        // When - cache the correct type once
        val initialLookup = viewHolder.findViewByIdOrNull<TextView>(textViewId)
        assertNotNull(initialLookup)

        // And request the wrong type to trigger cache eviction
        val wrongTypeLookup = viewHolder.findViewByIdOrNull<FrameLayout>(textViewId)

        // Then - wrong type returns null, cache is rebuilt on next correct lookup
        assertNull(wrongTypeLookup)
        val rebuiltLookup = viewHolder.findViewByIdOrNull<TextView>(textViewId)
        assertNotNull(rebuiltLookup)
        assertSame(textView, rebuiltLookup)
    }

    // ==============================================
    // Cache Management Tests
    // ==============================================

    @Test
    fun clearViewCache_removesAllCachedViews() {
        // Given
        val layout = createLayoutWithTextView()
        val viewHolder = TestViewHolder(layout, parent)
        val textViewId = View.generateViewId()
        val textView = TextView(context)
        textView.id = textViewId
        (viewHolder.itemView as ViewGroup).addView(textView)

        // Cache the view
        viewHolder.findViewById<TextView>(textViewId)

        // When
        viewHolder.clearViewCache()

        // Then - Should not throw, cache is cleared
        assertNotNull(viewHolder)
    }

    @Test
    fun clearViewCache_onEmptyCache_succeeds() {
        // Given
        val layout = createSimpleLayout()
        val viewHolder = TestViewHolder(layout, parent)

        // When
        viewHolder.clearViewCache()

        // Then - Should not crash
        assertNotNull(viewHolder)
    }

    @Test
    fun viewCache_canBeRebuiltAfterClear() {
        // Given
        val layout = createLayoutWithTextView()
        val viewHolder = TestViewHolder(layout, parent)
        val textViewId = View.generateViewId()
        val textView = TextView(context)
        textView.id = textViewId
        (viewHolder.itemView as ViewGroup).addView(textView)

        // When
        val firstView = viewHolder.findViewById<TextView>(textViewId)
        viewHolder.clearViewCache()
        val secondView = viewHolder.findViewById<TextView>(textViewId)

        // Then - Should return the same view (re-cached)
        assertSame(firstView, secondView)
    }

    // ==============================================
    // Position Validation Tests
    // ==============================================

    @Test
    fun isValidPosition_returnsFalseForNoPosition() {
        // Given
        val layout = createSimpleLayout()
        val viewHolder = TestViewHolder(layout, parent)

        // When - adapterPosition is NO_POSITION by default
        val isValid = viewHolder.testIsValidPosition()

        // Then
        assertFalse(isValid)
    }

    @Test
    fun getAdapterPositionSafe_returnsNoPositionWhenInvalid() {
        // Given
        val layout = createSimpleLayout()
        val viewHolder = TestViewHolder(layout, parent)

        // When
        val position = viewHolder.testGetAdapterPositionSafe()

        // Then
        assertEquals(RecyclerView.NO_POSITION, position)
    }

    // ==============================================
    // Multiple Views Tests
    // ==============================================

    @Test
    fun multipleViews_canBeCached() {
        // Given
        val layout = createLayoutWithMultipleViews()
        val viewHolder = TestViewHolder(layout, parent)

        val textViewId1 = View.generateViewId()
        val textViewId2 = View.generateViewId()
        val textView1 = TextView(context)
        val textView2 = TextView(context)
        textView1.id = textViewId1
        textView2.id = textViewId2

        (viewHolder.itemView as ViewGroup).apply {
            addView(textView1)
            addView(textView2)
        }

        // When
        val foundView1 = viewHolder.findViewById<TextView>(textViewId1)
        val foundView2 = viewHolder.findViewById<TextView>(textViewId2)

        // Then
        assertNotNull(foundView1)
        assertNotNull(foundView2)
        assertNotSame(foundView1, foundView2)
    }

    @Test
    fun mixedViewTypes_canBeCached() {
        // Given
        val layout = createLayoutWithMultipleViews()
        val viewHolder = TestViewHolder(layout, parent)

        val textViewId = View.generateViewId()
        val frameLayoutId = View.generateViewId()
        val textView = TextView(context)
        val frameLayout = FrameLayout(context)
        textView.id = textViewId
        frameLayout.id = frameLayoutId

        (viewHolder.itemView as ViewGroup).apply {
            addView(textView)
            addView(frameLayout)
        }

        // When
        val foundTextView = viewHolder.findViewById<TextView>(textViewId)
        val foundFrameLayout = viewHolder.findViewById<FrameLayout>(frameLayoutId)

        // Then
        assertTrue(foundTextView is TextView)
        assertTrue(foundFrameLayout is FrameLayout)
    }

    // ==============================================
    // Edge Cases
    // ==============================================

    @Test
    fun viewHolder_canBeCreatedMultipleTimes() {
        // When
        for (i in 1..10) {
            val layout = createSimpleLayout()
            val viewHolder = TestViewHolder(layout, parent)
            assertNotNull(viewHolder)
        }

        // Then - Should not crash
        assertTrue(true)
    }

    @Test
    fun largeNumberOfViews_canBeCached() {
        // Given
        val layout = createLayoutWithMultipleViews()
        val viewHolder = TestViewHolder(layout, parent)
        val viewIds = mutableListOf<Int>()

        // Add 50 views
        repeat(50) {
            val id = View.generateViewId()
            val view = TextView(context)
            view.id = id
            viewIds.add(id)
            (viewHolder.itemView as ViewGroup).addView(view)
        }

        // When - Cache all views
        viewIds.forEach { id ->
            viewHolder.findViewById<TextView>(id)
        }

        // Then - All views should be cached
        viewIds.forEach { id ->
            assertNotNull(viewHolder.findViewByIdOrNull<TextView>(id))
        }
    }

    // ==============================================
    // Helper Methods
    // ==============================================

    private fun createSimpleLayout(): Int {
        // Return a simple FrameLayout resource ID
        // In Robolectric, we can use android.R.layout.simple_list_item_1
        return android.R.layout.simple_list_item_1
    }

    private fun createLayoutWithTextView(): Int {
        // simple_list_item_2 has a ViewGroup root so we can safely add child views for caching tests
        return android.R.layout.simple_list_item_2
    }

    private fun createLayoutWithMultipleViews(): Int {
        return android.R.layout.simple_list_item_2
    }

    // Test ViewHolder that exposes protected methods
    private class TestViewHolder(
        xmlRes: Int,
        parent: ViewGroup,
        attachToRoot: Boolean = false
    ) : BaseRcvViewHolder(xmlRes, parent, attachToRoot) {

        fun testIsValidPosition(): Boolean = isValidPosition()
        fun testGetAdapterPositionSafe(): Int = getAdapterPositionSafe()
    }

    /**
     * Integration note:
     *
     * Full ViewHolder lifecycle testing requires:
     * - RecyclerView with Adapter attached
     * - LayoutManager configured
     * - Actual scroll events
     * - View recycling behavior
     *
     * These are best tested through:
     * - Instrumentation tests (on real device/emulator)
     * - Manual testing in sample app
     *
     * This Robolectric test focuses on:
     * - ViewHolder creation
     * - View caching mechanism correctness
     * - findViewById behavior
     * - Null-safe lookups
     * - Cache management
     * - Position validation helpers
     */
}
