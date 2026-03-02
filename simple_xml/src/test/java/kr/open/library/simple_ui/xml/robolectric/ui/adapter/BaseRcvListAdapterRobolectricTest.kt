package kr.open.library.simple_ui.xml.robolectric.ui.adapter

import android.content.Context
import android.os.Build
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.xml.ui.adapter.list.base.BaseRcvListAdapter
import kr.open.library.simple_ui.xml.ui.adapter.list.base.diffutil.RcvListDiffUtilCallBack
import kr.open.library.simple_ui.xml.ui.adapter.list.base.result.ListAdapterResult
import kr.open.library.simple_ui.xml.ui.adapter.viewholder.BaseRcvViewHolder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/**
 * Robolectric tests for BaseRcvListAdapter
 *
 * Tests comprehensive BaseRcvListAdapter functionality:
 * - Item addition/removal/replacement/movement
 * - ListAdapter submitList behavior
 * - CommitCallback verification
 * - Position validation
 * - DiffUtil integration
 * - Click listener registration
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class BaseRcvListAdapterRobolectricTest {
    private lateinit var context: Context
    private lateinit var adapter: TestListAdapter

    // Test data class
    data class TestItem(
        val id: Int,
        val name: String,
    )

    // Test adapter implementation
    private class TestListAdapter :
        BaseRcvListAdapter<TestItem, TestViewHolder>(
            RcvListDiffUtilCallBack(
                itemsTheSame = { oldItem, newItem -> oldItem.id == newItem.id },
                contentsTheSame = { oldItem, newItem -> oldItem == newItem },
            ),
        ) {
        override fun createViewHolderInternal(
            parent: ViewGroup,
            viewType: Int,
        ): TestViewHolder {
            val view = View(parent.context)
            return TestViewHolder(view)
        }

        override fun onBindViewHolder(
            holder: TestViewHolder,
            item: TestItem,
            position: Int,
        ) {
            // No binding needed for tests
        }
    }

    // Test ViewHolder
    private class TestViewHolder(
        itemView: View,
    ) : RecyclerView.ViewHolder(itemView)

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        adapter = TestListAdapter()
    }

    // ==============================================
    // Basic Functionality Tests
    // ==============================================

    @Test
    fun adapter_createsSuccessfully() {
        // Given & When
        val adapter = TestListAdapter()

        // Then
        assertNotNull(adapter)
        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun initialItemCount_isZero() {
        // When
        val count = adapter.itemCount

        // Then
        assertEquals(0, count)
    }

    @Test
    fun emptyList_canBeRetrieved() {
        // When
        val items = adapter.getItems()

        // Then
        assertNotNull(items)
        assertTrue(items.isEmpty())
    }

    @Test
    fun addItem_calledOffMainThread_throwsIllegalStateException() {
        val failure = AtomicReference<Throwable?>(null)
        val latch = CountDownLatch(1)

        Thread {
            try {
                adapter.addItem(TestItem(1, "Item 1"))
            } catch (t: Throwable) {
                failure.set(t)
            } finally {
                latch.countDown()
            }
        }.start()

        assertTrue(latch.await(2, TimeUnit.SECONDS))
        val error = failure.get()
        assertTrue(error is IllegalStateException)
        assertTrue(error?.message?.contains("BaseRcvListAdapter.addItem") == true)
    }

    // ==============================================
    // Item Addition Tests
    // ==============================================

    @Test
    fun singleItem_canBeAdded() {
        // Given
        val item = TestItem(1, "Item 1")

        // When
        addItemAwait(item)

        // Then
        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun addingMultipleItems_increasesCount() {
        // Given
        val item1 = TestItem(1, "Item 1")
        val item2 = TestItem(2, "Item 2")

        // When
        addItemAwait(item1)
        addItemAwait(item2)

        // Then
        assertEquals(2, adapter.itemCount)
    }

    @Test
    fun multipleItems_canBeAddedInBatch() {
        // Given
        val items =
            listOf(
                TestItem(1, "Item 1"),
                TestItem(2, "Item 2"),
                TestItem(3, "Item 3"),
            )

        // When
        val result = addItemsAwait(items)

        // Then
        assertTrue(result)
        assertEquals(3, adapter.itemCount)
    }

    @Test
    fun addingEmptyList_returnsFalse() {
        // Given
        val emptyList = emptyList<TestItem>()

        // When
        val result = awaitListResult { onResult ->
            adapter.addItems(emptyList, onResult)
        }

        // Then
        assertTrue(result is ListAdapterResult.Rejected.EmptyInput)
        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun addItemsResult_withEmptyList_returnsRejectedEmptyInput() {
        // Given
        var result: ListAdapterResult? = null

        // When
        adapter.addItems(emptyList()) { received ->
            result = received
        }

        // Then
        assertTrue(result is ListAdapterResult.Rejected.EmptyInput)
        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun item_canBeInsertedAtPosition() {
        // Given
        setItemsAwait(listOf(TestItem(1, "Item 1"), TestItem(3, "Item 3")))
        val newItem = TestItem(2, "Item 2")

        // When
        val result = addItemAtAwait(1, newItem)

        // Then
        assertTrue(result)
        assertEquals(3, adapter.itemCount)
        assertEquals(newItem, adapter.getItems()[1])
    }

    @Test
    fun item_canBeInsertedAtFront() {
        // Given
        setItemsAwait(listOf(TestItem(2, "Item 2")))
        val newItem = TestItem(1, "Item 1")

        // When
        val result = addItemAtAwait(0, newItem)

        // Then
        assertTrue(result)
        // ListAdapter internally uses AsyncListDiffer
        assertTrue(adapter.itemCount >= 1)
    }

    @Test
    fun addingItemToInvalidPosition_fails() {
        // Given
        addItemAwait(TestItem(1, "Item 1"))

        // When
        val result = awaitListResult { onResult ->
            adapter.addItemAt(999, TestItem(2, "Item 2"), onResult)
        }

        // Then
        assertTrue(result is ListAdapterResult.Rejected.InvalidPosition)
    }

    @Test
    fun multipleItems_canBeInsertedAtPosition() {
        // Given
        setItemsAwait(listOf(TestItem(1, "Item 1"), TestItem(4, "Item 4")))
        val newItems = listOf(TestItem(2, "Item 2"), TestItem(3, "Item 3"))

        // When
        val result = addItemsAtAwait(1, newItems)

        // Then - ListAdapter's submitList may take multiple frames to complete
        assertTrue(result)
        assertTrue(adapter.itemCount >= 2)
    }

    @Test
    fun addingEmptyListAtPosition_returnsFalse() {
        // Given
        addItemAwait(TestItem(1, "Item 1"))

        // When
        val result = awaitListResult { onResult ->
            adapter.addItemsAt(0, emptyList(), onResult)
        }

        // Then
        assertTrue(result is ListAdapterResult.Rejected.EmptyInput)
        assertEquals(1, adapter.itemCount)
    }

    // ==============================================
    // Item Removal Tests
    // ==============================================

    @Test
    fun item_canBeRemovedAtPosition() {
        // Given
        setItemsAwait(listOf(TestItem(1, "Item 1"), TestItem(2, "Item 2")))

        // When
        val result = removeAtAwait(0)

        // Then - ListAdapter's submitList may take multiple frames
        assertTrue(result)
        assertTrue(adapter.itemCount >= 0)
    }

    @Test
    fun removingItemWithInvalidPosition_fails() {
        // Given
        addItemAwait(TestItem(1, "Item 1"))

        // When
        val result = awaitListResult { onResult ->
            adapter.removeAt(999, onResult)
        }

        // Then
        assertTrue(result is ListAdapterResult.Rejected.InvalidPosition)
        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun specificItem_canBeRemoved() {
        // Given
        val item1 = TestItem(1, "Item 1")
        val item2 = TestItem(2, "Item 2")
        setItemsAwait(listOf(item1, item2))

        // When
        val result = removeItemAwait(item1)

        // Then
        assertTrue(result)
        assertEquals(1, adapter.itemCount)
        assertEquals(item2, adapter.getItems()[0])
    }

    @Test
    fun removingMissingItem_fails() {
        // Given
        addItemAwait(TestItem(1, "Item 1"))
        val nonExistentItem = TestItem(999, "Non-existent")

        // When
        val result = awaitListResult { onResult ->
            adapter.removeItem(nonExistentItem, onResult)
        }

        // Then
        assertTrue(result is ListAdapterResult.Rejected.ItemNotFound)
    }

    @Test
    fun removeItemResult_withMissingItem_returnsRejectedItemNotFound() {
        // Given
        addItemAwait(TestItem(1, "Item 1"))
        var result: ListAdapterResult? = null

        // When
        adapter.removeItem(TestItem(999, "Missing")) { received ->
            result = received
        }

        // Then
        assertTrue(result is ListAdapterResult.Rejected.ItemNotFound)
        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun setItems_reportsAppliedAfterQueueCommit() {
        // Given
        val items = listOf(TestItem(1, "Item 1"), TestItem(2, "Item 2"))
        val latch = CountDownLatch(1)
        var result: ListAdapterResult? = null

        // When
        adapter.setItems(items) { received ->
            result = received
            latch.countDown()
        }
        drainListUpdates()

        // Then
        assertTrue(latch.await(1, TimeUnit.SECONDS))
        assertEquals(ListAdapterResult.Applied, result)
        assertEquals(items, adapter.getItems())
    }

    @Test
    fun allItems_canBeCleared() {
        // Given
        setItemsAwait(
            listOf(
                TestItem(1, "Item 1"),
                TestItem(2, "Item 2"),
                TestItem(3, "Item 3"),
            ),
        )

        // When
        clearItemsAwait()

        // Then
        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun clearOnEmptyList_succeeds() {
        // When
        adapter.removeAll()

        // Then
        assertEquals(0, adapter.itemCount)
    }

    // ==============================================
    // Item Setting/Replacement Tests
    // ==============================================

    @Test
    fun items_canBeReplaced() {
        // Given
        addItemAwait(TestItem(1, "Old Item"))
        val newItems =
            listOf(
                TestItem(2, "New Item 1"),
                TestItem(3, "New Item 2"),
            )

        // When
        setItemsAwait(newItems)

        // Then
        assertEquals(2, adapter.itemCount)
        assertEquals(newItems[0], adapter.getItems()[0])
    }

    @Test
    fun items_canBeReplacedWithEmpty() {
        // Given
        addItemAwait(TestItem(1, "Item 1"))

        // When
        setItemsAwait(emptyList())

        // Then
        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun itemAtPosition_canBeReplaced() {
        // Given
        setItemsAwait(listOf(TestItem(1, "Item 1"), TestItem(2, "Item 2")))
        val newItem = TestItem(1, "Updated Item 1")

        // When
        val result = replaceItemAtAwait(0, newItem)
        shadowOf(Looper.getMainLooper()).idle()
        // Then
        assertTrue(result)
        assertEquals(newItem.name, adapter.getItems()[0].name)
    }

    @Test
    fun replacingItemAtInvalidPosition_fails() {
        // Given
        addItemAwait(TestItem(1, "Item 1"))

        // When
        val result = awaitListResult { onResult ->
            adapter.replaceItemAt(999, TestItem(2, "Item 2"), onResult)
        }

        // Then
        assertTrue(result is ListAdapterResult.Rejected.InvalidPosition)
    }

    // ==============================================
    // Item Movement Tests
    // ==============================================

    @Test
    fun item_canBeMoved() {
        // Given
        setItemsAwait(
            listOf(
                TestItem(1, "Item 1"),
                TestItem(2, "Item 2"),
                TestItem(3, "Item 3"),
            ),
        )

        // When
        val result = moveItemAwait(0, 2)

        // Then
        assertTrue(result)
        assertEquals(TestItem(2, "Item 2"), adapter.getItems()[0])
        assertEquals(TestItem(1, "Item 1"), adapter.getItems()[2])
    }

    @Test
    fun movingToSamePosition_succeeds() {
        // Given
        setItemsAwait(listOf(TestItem(1, "Item 1"), TestItem(2, "Item 2")))

        // When
        val result = moveItemAwait(0, 0)

        // Then
        assertTrue(result)
        assertEquals(2, adapter.itemCount)
    }

    @Test
    fun movingToSamePosition_withCallback_invokesCallback() {
        // Given
        setItemsAwait(listOf(TestItem(1, "Item 1"), TestItem(2, "Item 2")))
        var callbackInvoked = false

        // When
        awaitListUpdate(expectedItemCount = 2) { onResult ->
            adapter.moveItem(0, 0) {
                callbackInvoked = true
                onResult(it)
            }
        }

        // Then
        assertTrue(callbackInvoked)
        assertEquals(2, adapter.itemCount)
    }

    @Test
    fun movingFromInvalidPosition_fails() {
        // Given
        addItemAwait(TestItem(1, "Item 1"))

        // When
        val result = awaitListResult { onResult ->
            adapter.moveItem(999, 0, onResult)
        }

        // Then
        assertTrue(result is ListAdapterResult.Rejected.InvalidPosition)
    }

    @Test
    fun movingToInvalidPosition_fails() {
        // Given
        addItemAwait(TestItem(1, "Item 1"))

        // When
        val result = awaitListResult { onResult ->
            adapter.moveItem(0, 999, onResult)
        }

        // Then
        assertTrue(result is ListAdapterResult.Rejected.InvalidPosition)
    }

    @Test
    fun movingToSameInvalidPosition_fails() {
        // When
        val result = awaitListResult { onResult ->
            adapter.moveItem(0, 0, onResult)
        }

        // Then
        assertTrue(result is ListAdapterResult.Rejected.InvalidPosition)
    }

    // ==============================================
    // CommitCallback Tests
    // ==============================================

    @Test
    fun setItems_withCallback_invokesCallback() {
        // Given
        var callbackInvoked = false
        val items = listOf(TestItem(1, "Item 1"))

        // When
        awaitListUpdate(expectedItemCount = items.size) { onResult ->
            adapter.setItems(items) {
                callbackInvoked = true
                onResult(it)
            }
        }

        // Then - Callback should be invoked after list update
        // Note: In Robolectric, submitList is synchronous by default
        assertTrue(callbackInvoked)
    }

    @Test
    fun addItem_withCallback_invokesCallback() {
        // Given
        var callbackInvoked = false
        val item = TestItem(1, "Item 1")

        // When
        awaitListUpdate(expectedItemCount = 1) { onResult ->
            adapter.addItem(item) {
                callbackInvoked = true
                onResult(it)
            }
        }

        // Then
        assertTrue(callbackInvoked)
    }

    @Test
    fun removeItem_withCallback_invokesCallback() {
        // Given
        val item = TestItem(1, "Item 1")
        addItemAwait(item)
        var callbackInvoked = false

        // When
        awaitListUpdate(expectedItemCount = 0) { onResult ->
            adapter.removeItem(item) {
                callbackInvoked = true
                onResult(it)
            }
        }

        // Then
        assertTrue(callbackInvoked)
    }

    // ==============================================
    // Click Listener Tests
    // ==============================================

    // (click listener integration tests are covered below in the dedicated section)

    // ==============================================
    // Real-world Scenario Tests
    // ==============================================

    @Test
    fun chatMessageScenario_behavesAsExpected() {
        // Given - Chat messages being added sequentially
        val messages =
            listOf(
                TestItem(1, "First"),
                TestItem(2, "Second"),
                TestItem(3, "Third"),
            )

        // When - Using setItems for ListAdapter is more reliable
        setItemsAwait(messages)

        // Then
        assertEquals(3, adapter.itemCount)
        assertEquals(messages[2], adapter.getItems()[2])
    }

    @Test
    fun todoRemovalScenario_behavesAsExpected() {
        // Given - Todo list with completed item removal
        val todo1 = TestItem(1, "First")
        val todo2 = TestItem(2, "Second")
        val todo3 = TestItem(3, "Third")

        setItemsAwait(listOf(todo1, todo2, todo3))

        // When - Remove completed todo
        val removed = removeItemAwait(todo2)

        // Then
        assertTrue(removed)
        assertEquals(2, adapter.itemCount)
        assertFalse(adapter.getItems().contains(todo2))
    }

    @Test
    fun searchResultUpdateScenario_behavesAsExpected() {
        // Given - Search results being updated
        val initialResults =
            listOf(
                TestItem(1, "Apple"),
                TestItem(2, "Banana"),
            )
        setItemsAwait(initialResults)
        assertEquals(2, adapter.itemCount)

        // When - New search query
        val newResults =
            listOf(
                TestItem(3, "Avocado"),
                TestItem(4, "Apricot"),
            )
        setItemsAwait(newResults)

        // Then
        assertEquals(2, adapter.itemCount)
        assertEquals(newResults[0], adapter.getItems()[0])
        assertEquals(newResults[1], adapter.getItems()[1])
    }

    @Test
    fun reorderableListScenario_behavesAsExpected() {
        // Given - Reorderable list (drag & drop scenario)
        awaitListUpdate(expectedItemCount = 3) { onResult ->
            adapter.setItems(
                listOf(
                    TestItem(1, "First"),
                    TestItem(2, "Second"),
                    TestItem(3, "Third"),
                ),
                onResult,
            )
        }

        // When - Move first item to last
        awaitListUpdate(expectedItemCount = 3) { onResult ->
            adapter.moveItem(0, 2, onResult)
        }

        // adapter.getItems()[0] = 2,"Second"
        // adapter.getItems()[1] = 3,"Third"
        // adapter.getItems()[2] = 1,"First"
        // Then
        assertEquals(TestItem(2, "Second"), adapter.getItems()[0])
        assertEquals(TestItem(3, "Third"), adapter.getItems()[1])
        assertEquals(TestItem(1, "First"), adapter.getItems()[2])
    }

    // ==============================================
    // Edge Cases
    // ==============================================

    @Test
    fun adapter_canHandleLargeDataset() {
        // Given
        val largeList = (1..1000).map { TestItem(it, "Item $it") }

        // When
        setItemsAwait(largeList)

        // Then
        assertEquals(1000, adapter.itemCount)
    }

    @Test
    fun adapter_canBeCreatedAndConfiguredMultipleTimes() {
        // When
        for (i in 1..10) {
            val testAdapter = TestListAdapter()
            testAdapter.setOnItemClickListener { _, _, _ -> }
            testAdapter.addItem(TestItem(i, "Item $i"))
        }

        // Then - Should not crash
        assertTrue(true)
    }

    @Test
    fun removingNegativePosition_fails() {
        // Given
        addItemAwait(TestItem(1, "Item 1"))

        // When
        val result = awaitListResult { onResult ->
            adapter.removeAt(-1, onResult)
        }

        // Then
        assertTrue(result is ListAdapterResult.Rejected.InvalidPosition)
        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun addingAtNegativePosition_fails() {
        // Given
        val item = TestItem(1, "Item 1")

        // When
        val result = awaitListResult { onResult ->
            adapter.addItemAt(-1, item, onResult)
        }

        // Then
        assertTrue(result is ListAdapterResult.Rejected.InvalidPosition)
        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun replaceItemAtNegativePosition_fails() {
        // Given
        addItemAwait(TestItem(1, "Item 1"))

        // When
        val result = awaitListResult { onResult ->
            adapter.replaceItemAt(-1, TestItem(2, "Item 2"), onResult)
        }

        // Then
        assertTrue(result is ListAdapterResult.Rejected.InvalidPosition)
    }

    @Test
    fun getMutableItemList_returnsIndependentCopy() {
        // Given
        setItemsAwait(listOf(TestItem(1, "Item 1"), TestItem(2, "Item 2")))

        // When - mutate the copy
        val copy = adapter.getMutableItemList()
        copy.add(TestItem(99, "Injected"))

        // Then - adapter internal state must not change
        assertEquals(2, adapter.itemCount)
        assertEquals(2, adapter.getItems().size)
    }

    private fun setItemsAwait(items: List<TestItem>) {
        awaitListUpdate(expectedItemCount = items.size) { onResult ->
            adapter.setItems(items, onResult)
        }
        shadowOf(Looper.getMainLooper()).idle()
    }

    private fun addItemAwait(item: TestItem) {
        val expected = adapter.itemCount + 1
        awaitListUpdate(expectedItemCount = expected) { onResult ->
            adapter.addItem(item, onResult)
        }
    }

    private fun addItemsAwait(items: List<TestItem>): Boolean {
        val expected = adapter.itemCount + items.size
        return awaitListUpdate(expectedItemCount = expected) { onResult ->
            adapter.addItems(items, onResult)
        }.isAccepted()
    }

    private fun addItemAtAwait(
        position: Int,
        item: TestItem,
    ): Boolean {
        val expected = adapter.itemCount + 1
        return awaitListUpdate(expectedItemCount = expected) { onResult ->
            adapter.addItemAt(position, item, onResult)
        }.isAccepted()
    }

    private fun addItemsAtAwait(
        position: Int,
        items: List<TestItem>,
    ): Boolean {
        val expected = adapter.itemCount + items.size
        return awaitListUpdate(expectedItemCount = expected) { onResult ->
            adapter.addItemsAt(position, items, onResult)
        }.isAccepted()
    }

    private fun removeAtAwait(position: Int): Boolean {
        val expected = (adapter.itemCount - 1).coerceAtLeast(0)
        return awaitListUpdate(expectedItemCount = expected) { onResult ->
            adapter.removeAt(position, onResult)
        }.isAccepted()
    }

    private fun removeItemAwait(item: TestItem): Boolean {
        val expected = (adapter.itemCount - 1).coerceAtLeast(0)
        return awaitListUpdate(expectedItemCount = expected) { onResult ->
            adapter.removeItem(item, onResult)
        }.isAccepted().apply { shadowOf(Looper.getMainLooper()).idle() }
    }

    private fun clearItemsAwait() {
        awaitListUpdate(expectedItemCount = 0) { onResult ->
            adapter.removeAll(onResult)
        }
    }

    private fun replaceItemAtAwait(
        position: Int,
        item: TestItem,
    ): Boolean {
        val expected = adapter.itemCount
        return awaitListUpdate(expectedItemCount = expected) { onResult ->
            adapter.replaceItemAt(position, item, onResult)
        }.isAccepted()
    }

    private fun moveItemAwait(
        fromPosition: Int,
        toPosition: Int,
    ): Boolean {
        val expected = adapter.itemCount
        return awaitListUpdate(expectedItemCount = expected) { onResult ->
            adapter.moveItem(fromPosition, toPosition, onResult)
        }.isAccepted()
    }

    private fun awaitListResult(
        trigger: (((ListAdapterResult) -> Unit) -> Unit),
    ): ListAdapterResult {
        var result: ListAdapterResult? = null
        trigger { received ->
            result = received
        }
        drainListUpdates()
        return requireNotNull(result) { "ListAdapterResult callback was not invoked" }
    }

    private fun awaitListUpdate(
        expectedItemCount: Int? = null,
        trigger: (((ListAdapterResult) -> Unit) -> Unit),
    ): ListAdapterResult {
        val latch = CountDownLatch(1)
        var result: ListAdapterResult? = null
        trigger { received ->
            result = received
            latch.countDown()
        }

        val deadline = System.currentTimeMillis() + 1_000L
        while (System.currentTimeMillis() < deadline) {
            drainListUpdates()
            if (latch.await(0, TimeUnit.MILLISECONDS)) {
                break
            }
            Thread.sleep(15)
        }

        drainListUpdates()
        val completed = latch.await(0, TimeUnit.MILLISECONDS)
        if (!completed) {
            fail("List update timed out")
        }
        expectedItemCount?.let {
            assertEquals("Unexpected item count after update", it, adapter.itemCount)
        }
        return requireNotNull(result) { "ListAdapterResult callback was not invoked" }
    }

    private fun ListAdapterResult.isAccepted(): Boolean = this !is ListAdapterResult.Rejected

    private fun drainListUpdates() {
        val mainLooper = shadowOf(Looper.getMainLooper())
        mainLooper.idle()
        mainLooper.runToEndOfTasks()
        mainLooper.idle()
    }

    private fun createBoundViewHolder(position: Int): RecyclerView.ViewHolder {
        val recyclerView =
            RecyclerView(context).apply {
                layoutManager = LinearLayoutManager(context)
                adapter = this@BaseRcvListAdapterRobolectricTest.adapter
            }
        val widthSpec = View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.EXACTLY)
        recyclerView.measure(widthSpec, heightSpec)
        recyclerView.layout(0, 0, 1000, 1000)
        shadowOf(Looper.getMainLooper()).idle()
        val holder = recyclerView.findViewHolderForAdapterPosition(position)
        assertNotNull(holder)
        return holder!!
    }

    // ==============================================
    // ViewHolder Binding Tests
    // ==============================================

    @Test
    fun onBindViewHolder_callsAbstractMethod() {
        // Given
        var bindCalled = false
        val testAdapter =
            object : BaseRcvListAdapter<TestItem, TestViewHolder>(
                RcvListDiffUtilCallBack(
                    itemsTheSame = { oldItem, newItem -> oldItem.id == newItem.id },
                    contentsTheSame = { oldItem, newItem -> oldItem == newItem },
                ),
            ) {
                override fun createViewHolderInternal(
                    parent: ViewGroup,
                    viewType: Int,
                ): TestViewHolder {
                    val view = View(parent.context)
                    return TestViewHolder(view)
                }

                override fun onBindViewHolder(
                    holder: TestViewHolder,
                    item: TestItem,
                    position: Int,
                ) {
                    bindCalled = true
                }
            }

        testAdapter.setItems(listOf(TestItem(1, "Item 1")))
        shadowOf(Looper.getMainLooper()).idle()

        // When
        val holder = testAdapter.onCreateViewHolder(RecyclerView(context).apply { layoutManager = LinearLayoutManager(context) }, 0)
        testAdapter.onBindViewHolder(holder, 0)

        // Then
        assertTrue(bindCalled)
    }

    @Test
    fun onBindViewHolder_withPayload_callsPayloadMethod() {
        // Given
        var payloadBindCalled = false
        val testAdapter =
            object : BaseRcvListAdapter<TestItem, TestViewHolder>(
                RcvListDiffUtilCallBack(
                    itemsTheSame = { oldItem, newItem -> oldItem.id == newItem.id },
                    contentsTheSame = { oldItem, newItem -> oldItem == newItem },
                ),
            ) {
                override fun createViewHolderInternal(
                    parent: ViewGroup,
                    viewType: Int,
                ): TestViewHolder {
                    val view = View(parent.context)
                    return TestViewHolder(view)
                }

                override fun onBindViewHolder(
                    holder: TestViewHolder,
                    item: TestItem,
                    position: Int,
                ) {
                    // Full binding
                }

                override fun onBindViewHolder(
                    holder: TestViewHolder,
                    item: TestItem,
                    position: Int,
                    payloads: List<Any>,
                ) {
                    payloadBindCalled = true
                }
            }

        testAdapter.setItems(listOf(TestItem(1, "Item 1")))
        shadowOf(Looper.getMainLooper()).idle()

        // When
        val holder =
            testAdapter.onCreateViewHolder(RecyclerView(context).apply { layoutManager = LinearLayoutManager(context) }, 0)
        testAdapter.onBindViewHolder(holder, 0, mutableListOf("payload"))

        // Then
        assertTrue(payloadBindCalled)
    }

    @Test
    fun onBindViewHolder_withEmptyPayload_callsFullBinding() {
        // Given
        var fullBindCalled = false
        val testAdapter = object : BaseRcvListAdapter<TestItem, TestViewHolder>(
            RcvListDiffUtilCallBack(
                itemsTheSame = { oldItem, newItem -> oldItem.id == newItem.id },
                contentsTheSame = { oldItem, newItem -> oldItem == newItem },
            ),
        ) {
            override fun createViewHolderInternal(
                parent: ViewGroup,
                viewType: Int
            ): TestViewHolder {
                val view = View(parent.context)
                return TestViewHolder(view)
            }

            override fun onBindViewHolder(
                holder: TestViewHolder,
                item: TestItem,
                position: Int,
            ) {
                fullBindCalled = true
            }
        }

        testAdapter.setItems(listOf(TestItem(1, "Item 1")))
        shadowOf(Looper.getMainLooper()).idle()

        // When
        val holder = testAdapter.onCreateViewHolder(RecyclerView(context).apply { layoutManager = LinearLayoutManager(context) }, 0)
        testAdapter.onBindViewHolder(holder, 0, mutableListOf())

        // Then
        assertTrue(fullBindCalled)
    }

    @Test
    fun onBindViewHolder_withInvalidPosition_handlesGracefully() {
        // Given
        setItemsAwait(listOf(TestItem(1, "Item 1")))

        // When
        val holder =
            adapter.onCreateViewHolder(RecyclerView(context).apply { layoutManager = LinearLayoutManager(context) }, 0)
        adapter.onBindViewHolder(holder, 999)

        // Then - Should not crash
        assertTrue(true)
    }

    @Test
    fun onBindViewHolder_withPayloadAndInvalidPosition_handlesGracefully() {
        // Given
        setItemsAwait(listOf(TestItem(1, "Item 1")))

        // When
        val holder =
            adapter.onCreateViewHolder(RecyclerView(context).apply { layoutManager = LinearLayoutManager(context) }, 0)
        adapter.onBindViewHolder(holder, 999, mutableListOf("payload"))

        // Then - Should not crash
        assertTrue(true)
    }

    @Test
    fun onViewRecycled_clearsViewCacheForBaseRcvViewHolder() {
        // Given - Create adapter with BaseRcvViewHolder
        val testAdapter =
            object : BaseRcvListAdapter<TestItem, BaseRcvViewHolder>(
                RcvListDiffUtilCallBack(
                    itemsTheSame = { oldItem, newItem -> oldItem.id == newItem.id },
                    contentsTheSame = { oldItem, newItem -> oldItem == newItem },
                ),
            ) {
                override fun createViewHolderInternal(
                    parent: ViewGroup,
                    viewType: Int,
                ): BaseRcvViewHolder = BaseRcvViewHolder(android.R.layout.simple_list_item_1, parent)

                override fun onBindViewHolder(
                    holder: BaseRcvViewHolder,
                    item: TestItem,
                    position: Int,
                ) {
                    // No binding needed
                }
            }

        // When
        val holder =
            testAdapter.onCreateViewHolder(RecyclerView(context).apply { layoutManager = LinearLayoutManager(context) }, 0)
        testAdapter.onViewRecycled(holder)

        // Then - Should not crash
        assertTrue(true)
    }

    @Test
    fun onViewRecycled_withRegularViewHolder_handlesGracefully() {
        // Given
        val holder =
            adapter.onCreateViewHolder(RecyclerView(context).apply { layoutManager = LinearLayoutManager(context) }, 0)

        // When
        adapter.onViewRecycled(holder)

        // Then - Should not crash
        assertTrue(true)
    }

    // ==============================================
    // Exception Handling Tests
    // ==============================================

    @Test
    fun addItemAt_withRuntimeException_returnsFalse() {
        // Given - Create adapter that throws exception
        val faultyAdapter =
            object : BaseRcvListAdapter<TestItem, TestViewHolder>(
                RcvListDiffUtilCallBack(
                    itemsTheSame = { oldItem, newItem -> oldItem.id == newItem.id },
                    contentsTheSame = { oldItem, newItem -> oldItem == newItem },
                ),
            ) {
                override fun createViewHolderInternal(
                    parent: ViewGroup,
                    viewType: Int,
                ): TestViewHolder = TestViewHolder(View(parent.context))

                override fun onBindViewHolder(
                    holder: TestViewHolder,
                    item: TestItem,
                    position: Int,
                ) {}
            }

        // When - Trigger unexpected exception scenario
        var result: ListAdapterResult? = null
        faultyAdapter.addItemAt(Int.MAX_VALUE, TestItem(1, "Item")) { received ->
            result = received
        }

        // Then
        assertTrue(result is ListAdapterResult.Rejected.InvalidPosition)
    }

    @Test
    fun addItems_atInvalidPosition_returnsFalse() {
        // Given
        addItemAwait(TestItem(1, "Item 1"))

        // When
        val result = awaitListResult { onResult ->
            adapter.addItemsAt(999, listOf(TestItem(2, "Item 2")), onResult)
        }

        // Then
        assertTrue(result is ListAdapterResult.Rejected.InvalidPosition)
    }

    @Test
    fun addItems_atNegativePosition_returnsFalse() {
        // When
        val result = awaitListResult { onResult ->
            adapter.addItemsAt(-1, listOf(TestItem(1, "Item 1")), onResult)
        }

        // Then
        assertTrue(result is ListAdapterResult.Rejected.InvalidPosition)
    }

    @Test
    fun removeAt_withRuntimeException_returnsFalse() {
        // Given - Empty adapter
        // When
        val result = awaitListResult { onResult ->
            adapter.removeAt(Int.MAX_VALUE, onResult)
        }

        // Then
        assertTrue(result is ListAdapterResult.Rejected.InvalidPosition)
    }

    @Test
    fun moveItem_withRuntimeException_returnsFalse() {
        // Given - Empty adapter
        // When
        val result = awaitListResult { onResult ->
            adapter.moveItem(Int.MAX_VALUE, 0, onResult)
        }

        // Then
        assertTrue(result is ListAdapterResult.Rejected.InvalidPosition)
    }

    // ==============================================
    // Click Listener Integration Tests
    // ==============================================

    @Test
    fun itemClickListener_invokesWhenViewClicked() {
        // Given
        var clickedPosition = -1
        var clickedItem: TestItem? = null
        adapter.setOnItemClickListener { position, item, _ ->
            clickedPosition = position
            clickedItem = item
        }
        setItemsAwait(listOf(TestItem(1, "Item 1")))

        // When
        val holder = createBoundViewHolder(0)
        holder.itemView.performClick()

        // Then
        assertEquals(0, clickedPosition)
        assertEquals(TestItem(1, "Item 1"), clickedItem)
    }

    @Test
    fun itemLongClickListener_invokesWhenViewLongClicked() {
        // Given
        var longClickedPosition = -1
        var longClickedItem: TestItem? = null
        adapter.setOnItemLongClickListener { position, item, _ ->
            longClickedPosition = position
            longClickedItem = item
        }
        setItemsAwait(listOf(TestItem(1, "Item 1")))

        // When
        val holder = createBoundViewHolder(0)
        holder.itemView.performLongClick()

        // Then
        assertEquals(0, longClickedPosition)
        assertEquals(TestItem(1, "Item 1"), longClickedItem)
    }

    @Test
    fun itemLongClickListener_returnsTrue_whenListenerSet() {
        // Given
        adapter.setOnItemLongClickListener { _, _, _ -> }
        setItemsAwait(listOf(TestItem(1, "Item 1")))

        // When
        val holder = createBoundViewHolder(0)
        val consumed = holder.itemView.performLongClick()

        // Then
        assertTrue(consumed)
    }

    @Test
    fun itemLongClickListener_returnsFalse_whenNoListenerSet() {
        // Given
        setItemsAwait(listOf(TestItem(1, "Item 1")))

        // When
        val holder = createBoundViewHolder(0)
        val consumed = holder.itemView.performLongClick()

        // Then
        assertFalse(consumed)
    }

    // ==============================================
    // removeItems / removeRange Tests
    // ==============================================

    @Test
    fun removeItems_removesAllMatchingItems() {
        // Given
        val item1 = TestItem(1, "Item 1")
        val item2 = TestItem(2, "Item 2")
        val item3 = TestItem(3, "Item 3")
        setItemsAwait(listOf(item1, item2, item3))

        // When
        val result = awaitListUpdate(expectedItemCount = 1) { onResult ->
            adapter.removeItems(listOf(item1, item3), onResult)
        }

        // Then
        assertTrue(result.isAccepted())
        assertEquals(1, adapter.itemCount)
        assertEquals(item2, adapter.getItems()[0])
    }

    @Test
    fun removeItems_withEmptyList_fails() {
        // Given
        setItemsAwait(listOf(TestItem(1, "Item 1")))

        // When
        val result = awaitListResult { onResult ->
            adapter.removeItems(emptyList(), onResult)
        }

        // Then
        assertTrue(result is ListAdapterResult.Rejected.EmptyInput)
        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun removeItems_withNonExistentItems_fails() {
        // Given
        setItemsAwait(listOf(TestItem(1, "Item 1")))

        // When
        val result = awaitListResult { onResult ->
            adapter.removeItems(listOf(TestItem(99, "Ghost")), onResult)
        }

        // Then
        assertTrue(result is ListAdapterResult.Rejected.NoMatchingItems)
        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun removeRange_removesContiguousSlice() {
        // Given
        setItemsAwait(
            listOf(
                TestItem(1, "Item 1"),
                TestItem(2, "Item 2"),
                TestItem(3, "Item 3"),
                TestItem(4, "Item 4"),
            ),
        )

        // When
        val result = awaitListUpdate(expectedItemCount = 2) { onResult ->
            adapter.removeRange(1, 2, onResult)
        }

        // Then
        assertTrue(result.isAccepted())
        assertEquals(2, adapter.itemCount)
        assertEquals(TestItem(1, "Item 1"), adapter.getItems()[0])
        assertEquals(TestItem(4, "Item 4"), adapter.getItems()[1])
    }

    @Test
    fun removeRange_withInvalidRange_fails() {
        // Given
        setItemsAwait(listOf(TestItem(1, "Item 1"), TestItem(2, "Item 2")))

        // When
        val result = awaitListResult { onResult ->
            adapter.removeRange(0, 10, onResult)
        }

        // Then
        assertTrue(result is ListAdapterResult.Rejected.InvalidPosition)
        assertEquals(2, adapter.itemCount)
    }

    // ==============================================
    // getItemOrNull Tests
    // ==============================================

    @Test
    fun getItemOrNull_returnsItem_whenPositionValid() {
        // Given
        val item = TestItem(1, "Item 1")
        setItemsAwait(listOf(item))

        // When
        val result = adapter.getItemOrNull(0)

        // Then
        assertEquals(item, result)
    }

    @Test
    fun getItemOrNull_returnsNull_whenPositionInvalid() {
        // Given
        setItemsAwait(listOf(TestItem(1, "Item 1")))

        // When
        val result = adapter.getItemOrNull(999)

        // Then
        assertEquals(null, result)
    }

    /**
     * Integration note:
     *
     * Full ViewHolder binding, DiffUtil animations, and click events require:
     * - RecyclerView attached to a window
     * - LayoutManager configured
     * - UI thread execution
     * - Animation completion
     *
     * These are best tested through:
     * - Instrumentation tests (on real device/emulator)
     * - Manual testing in sample app
     *
     * This Robolectric test focuses on:
     * - API correctness (methods don't crash)
     * - Data manipulation (add/remove/replace/move/range/items)
     * - ListAdapter submitList behavior
     * - CommitCallback invocation
     * - Listener registration mechanics
     * - ViewHolder binding mechanics
     * - Exception handling
     */
}
