package kr.open.library.simple_ui.xml.robolectric.ui.adapter

import android.content.Context
import android.os.Build
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.xml.ui.adapter.normal.base.BaseRcvAdapter
import kr.open.library.simple_ui.xml.ui.adapter.normal.result.NormalAdapterResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
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
 * Robolectric tests for BaseRcvAdapter
 *
 * Tests comprehensive BaseRcvAdapter functionality:
 * - Item addition/removal/replacement
 * - List size verification
 * - Position validation
 * - Click listener registration
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class BaseRcvAdapterRobolectricTest {
    private lateinit var context: Context
    private lateinit var adapter: TestAdapter

    // Test data class
    data class TestItem(
        val id: Int,
        val name: String,
    )

    // Test adapter implementation
    private class TestAdapter : BaseRcvAdapter<TestItem, TestViewHolder>() {
        override fun createViewHolderInternal(parent: ViewGroup, viewType: Int): TestViewHolder {
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

    // Adapter capturing payload invocations
    private class PayloadTrackingAdapter : BaseRcvAdapter<TestItem, TestViewHolder>() {
        var lastPayloads: List<Any>? = null

        override fun createViewHolderInternal(parent: ViewGroup, viewType: Int): TestViewHolder = TestViewHolder(View(parent.context))

        override fun onBindViewHolder(
            holder: TestViewHolder,
            item: TestItem,
            position: Int,
        ) {
            // no-op
        }

        override fun onBindViewHolder(
            holder: TestViewHolder,
            item: TestItem,
            position: Int,
            payloads: List<Any>,
        ) {
            lastPayloads = payloads
        }
    }

    // Test ViewHolder
    private class TestViewHolder(
        itemView: View,
    ) : RecyclerView.ViewHolder(itemView)

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        adapter = TestAdapter()
    }

    // ==============================================
    // Basic Functionality Tests
    // ==============================================

    @Test
    fun adapter_createsSuccessfully() {
        // Given & When
        val adapter = TestAdapter()

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
        assertTrue(error?.message?.contains("BaseRcvAdapter.addItem") == true)
    }

    // ==============================================
    // Item Addition Tests
    // ==============================================

    @Test
    fun singleItem_canBeAdded() {
        // Given
        val item = TestItem(1, "Item 1")
        val initialSize = adapter.itemCount

        // When
        adapter.addItem(item)
        shadowOf(Looper.getMainLooper()).idle()

        // Then
        assertEquals(initialSize + 1, adapter.itemCount)
    }

    @Test
    fun addingMultipleItems_increasesCount() {
        // Given
        val item1 = TestItem(1, "Item 1")
        val item2 = TestItem(2, "Item 2")

        // When
        adapter.addItem(item1)
        shadowOf(Looper.getMainLooper()).idle()
        adapter.addItem(item2)
        shadowOf(Looper.getMainLooper()).idle()

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
        val result =
            captureNormalResult { onResult ->
                adapter.addItems(items, onResult)
            }

        // Then
        assertTrue(result.isApplied())
        assertEquals(3, adapter.itemCount)
    }

    @Test
    fun addingEmptyList_fails() {
        // Given
        val emptyList = emptyList<TestItem>()

        // When
        val result =
            captureNormalResult { onResult ->
                adapter.addItems(emptyList, onResult)
            }

        // Then
        assertTrue(result is NormalAdapterResult.Rejected.EmptyInput)
        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun addItemsResult_withEmptyList_returnsRejectedEmptyInput() {
        // Given
        var result: NormalAdapterResult? = null

        // When
        adapter.addItems(emptyList()) { received ->
            result = received
        }

        // Then
        assertTrue(result is NormalAdapterResult.Rejected.EmptyInput)
        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun addingEmptyListAtPosition_fails() {
        // Given
        adapter.addItem(TestItem(1, "Item 1"))

        // When
        val result =
            captureNormalResult { onResult ->
                adapter.addItemsAt(0, emptyList(), onResult)
            }

        // Then
        assertTrue(result is NormalAdapterResult.Rejected.EmptyInput)
        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun item_canBeInsertedAtPosition() {
        // Given
        adapter.setItems(listOf(TestItem(1, "Item 1"), TestItem(3, "Item 3")))
        val newItem = TestItem(2, "Item 2")

        // When
        val result =
            captureNormalResult { onResult ->
                adapter.addItemAt(1, newItem, onResult)
            }
        shadowOf(Looper.getMainLooper()).idle()

        // Then
        assertTrue(result.isApplied())
        assertEquals(3, adapter.itemCount)
        assertEquals(newItem, adapter.getItem(1))
    }

    @Test
    fun item_canBeInsertedAtFront() {
        // Given
        adapter.addItem(TestItem(2, "Item 2"))
        shadowOf(Looper.getMainLooper()).idle()
        shadowOf(Looper.getMainLooper()).idle()

        val newItem = TestItem(1, "Item 1")

        // When
        val result =
            captureNormalResult { onResult ->
                adapter.addItemAt(0, newItem, onResult)
            }
        shadowOf(Looper.getMainLooper()).idle()
        shadowOf(Looper.getMainLooper()).idle()

        // Then
        assertTrue(result.isApplied())
        assertEquals(2, adapter.itemCount)
        assertEquals(newItem, adapter.getItem(0))
        assertEquals(TestItem(2, "Item 2"), adapter.getItem(1))
    }

    @Test
    fun addingItemToInvalidPosition_fails() {
        // Given
        adapter.addItem(TestItem(1, "Item 1"))

        // When
        val result =
            captureNormalResult { onResult ->
                adapter.addItemAt(999, TestItem(2, "Item 2"), onResult)
            }

        // Then
        assertTrue(result is NormalAdapterResult.Rejected.InvalidPosition)
    }

    @Test
    fun item_canBeInsertedAtEnd() {
        // Given
        adapter.setItems(listOf(TestItem(1, "Item 1")))
        val newItem = TestItem(2, "Item 2")

        // When - itemCount is 1, so valid position is 0 or 1 (end)
        val result =
            captureNormalResult { onResult ->
                adapter.addItemAt(1, newItem, onResult)
            }
        shadowOf(Looper.getMainLooper()).idle()

        // Then
        assertTrue(result.isApplied())
        assertEquals(2, adapter.itemCount)
        assertEquals(newItem, adapter.getItem(1))
    }

    // ==============================================
    // Item Retrieval Tests
    // ==============================================

    @Test
    fun addedItem_canBeRetrieved() {
        // Given
        val item = TestItem(1, "Item 1")
        adapter.addItem(item)

        // When
        val retrievedItem = adapter.getItem(0)

        // Then
        assertEquals(item, retrievedItem)
    }

    @Test(expected = IndexOutOfBoundsException::class)
    fun gettingItemWithInvalidPosition_throws() {
        // Given
        adapter.addItem(TestItem(1, "Item 1"))

        // When & Then
        adapter.getItem(999)
    }

    @Test
    fun allItems_canBeRetrieved() {
        // Given
        val items =
            listOf(
                TestItem(1, "Item 1"),
                TestItem(2, "Item 2"),
            )
        adapter.addItems(items)

        // When
        val retrievedItems = adapter.getItems()

        // Then
        assertEquals(2, retrievedItems.size)
        assertEquals(items[0], retrievedItems[0])
        assertEquals(items[1], retrievedItems[1])
    }

    // ==============================================
    // Item Removal Tests
    // ==============================================

    @Test
    fun item_canBeRemovedAtPosition() {
        // Given
        adapter.addItem(TestItem(1, "Item 1"))
        adapter.addItem(TestItem(2, "Item 2"))

        // When
        val result =
            captureNormalResult { onResult ->
                adapter.removeAt(0, onResult)
            }

        // Then
        assertTrue(result.isApplied())
        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun removingItemWithInvalidPosition_fails() {
        // Given
        adapter.addItem(TestItem(1, "Item 1"))

        // When
        val result =
            captureNormalResult { onResult ->
                adapter.removeAt(999, onResult)
            }

        // Then
        assertTrue(result is NormalAdapterResult.Rejected.InvalidPosition)
        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun specificItem_canBeRemoved() {
        // Given
        val item1 = TestItem(1, "Item 1")
        val item2 = TestItem(2, "Item 2")
        adapter.setItems(listOf(item1, item2))

        // When
        val result =
            captureNormalResult { onResult ->
                adapter.removeItem(item1, onResult)
            }
        shadowOf(Looper.getMainLooper()).idle()

        // Then
        assertTrue(result.isApplied())
        assertEquals(1, adapter.itemCount)
        assertEquals(item2, adapter.getItem(0))
    }

    @Test
    fun removingMissingItem_fails() {
        // Given
        adapter.addItem(TestItem(1, "Item 1"))
        val nonExistentItem = TestItem(999, "Non-existent")

        // When
        val result =
            captureNormalResult { onResult ->
                adapter.removeItem(nonExistentItem, onResult)
            }

        // Then
        assertTrue(result is NormalAdapterResult.Rejected.ItemNotFound)
    }

    @Test
    fun removeItemResult_withMissingItem_returnsRejectedItemNotFound() {
        // Given
        adapter.addItem(TestItem(1, "Item 1"))
        var result: NormalAdapterResult? = null

        // When
        adapter.removeItem(TestItem(999, "Missing")) { received ->
            result = received
        }

        // Then
        assertTrue(result is NormalAdapterResult.Rejected.ItemNotFound)
        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun allItems_canBeCleared() {
        // Given
        adapter.setItems(
            listOf(
                TestItem(1, "Item 1"),
                TestItem(2, "Item 2"),
                TestItem(3, "Item 3"),
            ),
        )

        // When
        val result =
            captureNormalResult { onResult ->
                adapter.removeAll(onResult)
            }
        shadowOf(Looper.getMainLooper()).idle()

        // Then
        assertTrue(result.isApplied())
        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun clearOnEmptyList_succeeds() {
        // When
        val result =
            captureNormalResult { onResult ->
                adapter.removeAll(onResult)
            }

        // Then
        assertTrue(result.isApplied())
        assertEquals(0, adapter.itemCount)
    }

    // ==============================================
    // Item Setting Tests
    // ==============================================

    @Test
    fun items_canBeReplaced() {
        // Given
        adapter.setItems(listOf(TestItem(1, "Old Item")))
        val newItems =
            listOf(
                TestItem(2, "New Item 1"),
                TestItem(3, "New Item 2"),
            )

        // When
        adapter.setItems(newItems)
        shadowOf(Looper.getMainLooper()).idle()

        // Then
        assertEquals(2, adapter.itemCount)
        assertEquals(newItems[0], adapter.getItem(0))
    }

    @Test
    fun items_canBeReplacedWithEmpty() {
        // Given
        adapter.setItems(listOf(TestItem(1, "Item 1")))

        // When
        adapter.setItems(emptyList())
        shadowOf(Looper.getMainLooper()).idle()

        // Then
        assertEquals(0, adapter.itemCount)
    }

    // ==============================================
    // Item Movement Tests
    // ==============================================

    @Test
    fun movingToSamePosition_invokesCommitCallback() {
        // Given
        adapter.setItems(listOf(TestItem(1, "Item 1"), TestItem(2, "Item 2")))
        var callbackInvoked = false

        // When
        val result =
            captureNormalResult { onResult ->
                adapter.moveItem(0, 0) {
                    callbackInvoked = true
                    onResult(it)
                }
            }
        shadowOf(Looper.getMainLooper()).idle()

        // Then
        assertTrue(result.isApplied())
        assertTrue(callbackInvoked)
        assertEquals(
            listOf(TestItem(1, "Item 1"), TestItem(2, "Item 2")),
            adapter.getItems(),
        )
    }

    @Test
    fun movingToSameInvalidPosition_fails() {
        // When
        val result =
            captureNormalResult { onResult ->
                adapter.moveItem(0, 0, onResult)
            }

        // Then
        assertTrue(result is NormalAdapterResult.Rejected.InvalidPosition)
    }

    // ==============================================
    // Click Listener Tests
    // ==============================================

    @Test
    fun itemClickListener_invokesWhenViewClicked() {
        // Given
        var clickedPosition = -1
        var clickedItem: TestItem? = null
        val item = TestItem(1, "Item 1")

        adapter.setOnItemClickListener { position, clicked, _ ->
            clickedPosition = position
            clickedItem = clicked
        }
        adapter.addItem(item)
        shadowOf(Looper.getMainLooper()).idle()

        val recyclerView = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@BaseRcvAdapterRobolectricTest.adapter
            measure(
                View.MeasureSpec.makeMeasureSpec(500, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(500, View.MeasureSpec.EXACTLY),
            )
            layout(0, 0, 500, 500)
        }
        shadowOf(Looper.getMainLooper()).idle()

        // When
        val holder = recyclerView.findViewHolderForAdapterPosition(0)
        checkNotNull(holder)
        holder.itemView.performClick()

        // Then
        assertEquals(0, clickedPosition)
        assertEquals(item, clickedItem)
    }

    @Test
    fun itemLongClickListener_invokesWhenViewLongClicked() {
        // Given
        var longClickedPosition = -1
        var longClickedItem: TestItem? = null
        val item = TestItem(1, "Item 1")

        adapter.setOnItemLongClickListener { position, clicked, _ ->
            longClickedPosition = position
            longClickedItem = clicked
        }
        adapter.addItem(item)
        shadowOf(Looper.getMainLooper()).idle()

        val recyclerView = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@BaseRcvAdapterRobolectricTest.adapter
            measure(
                View.MeasureSpec.makeMeasureSpec(500, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(500, View.MeasureSpec.EXACTLY),
            )
            layout(0, 0, 500, 500)
        }
        shadowOf(Looper.getMainLooper()).idle()

        // When
        val holder = recyclerView.findViewHolderForAdapterPosition(0)
        checkNotNull(holder)
        holder.itemView.performLongClick()

        // Then
        assertEquals(0, longClickedPosition)
        assertEquals(item, longClickedItem)
    }

    // ==============================================
    // Multiple Configuration Tests
    // ==============================================

    @Test
    fun setOnItemClickListener_canBeReplacedMultipleTimes() {
        // Given
        var firstCount = 0
        var secondCount = 0
        adapter.addItem(TestItem(1, "Item"))
        shadowOf(Looper.getMainLooper()).idle()

        val recyclerView = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@BaseRcvAdapterRobolectricTest.adapter
            measure(
                View.MeasureSpec.makeMeasureSpec(500, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(500, View.MeasureSpec.EXACTLY),
            )
            layout(0, 0, 500, 500)
        }
        shadowOf(Looper.getMainLooper()).idle()

        // When - set first listener, click, then replace
        adapter.setOnItemClickListener { _, _, _ -> firstCount++ }
        val holder = checkNotNull(recyclerView.findViewHolderForAdapterPosition(0))
        holder.itemView.performClick()

        adapter.setOnItemClickListener { _, _, _ -> secondCount++ }
        holder.itemView.performClick()

        // Then - only the active listener at each moment fires
        assertEquals(1, firstCount)
        assertEquals(1, secondCount)
    }

    // ==============================================
    // Real-world Scenario Tests
    // ==============================================

    @Test
    fun chatMessageScenario_behavesAsExpected() {
        // Given - Chat messages being added sequentially
        val message1 = TestItem(1, "안녕하세요")
        val message2 = TestItem(2, "반갑습니다")
        val message3 = TestItem(3, "잘 부탁드립니다")

        // When
        adapter.addItem(message1)
        shadowOf(Looper.getMainLooper()).idle()
        adapter.addItem(message2)
        shadowOf(Looper.getMainLooper()).idle()
        adapter.addItem(message3)
        shadowOf(Looper.getMainLooper()).idle()

        // Then
        assertEquals(3, adapter.itemCount)
        assertEquals(message3, adapter.getItem(2))
    }

    @Test
    fun todoRemovalScenario_behavesAsExpected() {
        // Given - Todo list with completed item removal
        val todo1 = TestItem(1, "장보기")
        val todo2 = TestItem(2, "청소하기")
        val todo3 = TestItem(3, "운동하기")

        adapter.addItems(listOf(todo1, todo2, todo3))
        shadowOf(Looper.getMainLooper()).idle()

        // When - Remove completed todo
        val removed =
            captureNormalResult { onResult ->
                adapter.removeItem(todo2, onResult)
            }
        shadowOf(Looper.getMainLooper()).idle()

        // Then
        assertTrue(removed.isApplied())
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
        adapter.setItems(initialResults)
        shadowOf(Looper.getMainLooper()).idle()
        shadowOf(Looper.getMainLooper()).idle()

        // When - New search query
        val newResults =
            listOf(
                TestItem(3, "Avocado"),
                TestItem(4, "Apricot"),
            )
        adapter.setItems(newResults)
        shadowOf(Looper.getMainLooper()).idle()
        shadowOf(Looper.getMainLooper()).idle()

        // Then
        assertEquals(2, adapter.itemCount)
        assertEquals(newResults[0], adapter.getItem(0))
        assertEquals(newResults[1], adapter.getItem(1))
    }

    // ==============================================
    // Edge Cases
    // ==============================================

    @Test
    fun adapter_canHandleLargeDataset() {
        // Given
        val largeList = (1..1000).map { TestItem(it, "Item $it") }

        // When
        val result =
            captureNormalResult { onResult ->
                adapter.addItems(largeList, onResult)
            }

        // Then
        assertTrue(result.isApplied())
        assertEquals(1000, adapter.itemCount)
    }

    @Test
    fun adapter_canBeCreatedAndConfiguredMultipleTimes() {
        // When
        for (i in 1..10) {
            val testAdapter = TestAdapter()
            testAdapter.setOnItemClickListener { _, _, _ -> }
            testAdapter.addItem(TestItem(i, "Item $i"))
            assertEquals(1, testAdapter.itemCount)
        }
    }

    @Test
    fun onBindViewHolder_invokesLongClickListener_whenAdapterPositionValid() {
        val longClickAdapter = TestAdapter()
        longClickAdapter.addItem(TestItem(1, "Long"))
        shadowOf(Looper.getMainLooper()).idle()

        var captured: Triple<Int, TestItem, View>? = null
        longClickAdapter.setOnItemLongClickListener { index, item, view ->
            captured = Triple(index, item, view)
        }

        val recyclerView =
            RecyclerView(context).apply {
                layoutManager = LinearLayoutManager(context)
                adapter = longClickAdapter
                measure(
                    View.MeasureSpec.makeMeasureSpec(500, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(500, View.MeasureSpec.EXACTLY),
                )
                layout(0, 0, 500, 500)
            }
        shadowOf(Looper.getMainLooper()).idle()

        val holder = recyclerView.findViewHolderForAdapterPosition(0)
        checkNotNull(holder)

        assertTrue(holder.itemView.performLongClick())
        assertNotNull(captured)
        assertEquals(0, captured!!.first)
        assertEquals(TestItem(1, "Long"), captured!!.second)
    }

    @Test
    fun onBindViewHolder_withPayloads_invokesPayloadOverride() {
        val payloadAdapter = PayloadTrackingAdapter()
        payloadAdapter.addItem(TestItem(1, "Payload"))
        shadowOf(Looper.getMainLooper()).idle()
        val holder = payloadAdapter.onCreateViewHolder(FrameLayout(context), 0)
        holder.forceAdapterPosition(0)

        val payloads = mutableListOf<Any>("partial")
        payloadAdapter.onBindViewHolder(holder, 0, payloads)

        assertEquals(payloads, payloadAdapter.lastPayloads)

        payloadAdapter.lastPayloads = listOf("initial")
        payloadAdapter.onBindViewHolder(holder, 5, payloads)
        assertEquals(listOf("initial"), payloadAdapter.lastPayloads)
    }

    @Test
    fun removingNegativePosition_fails() {
        // Given
        adapter.addItem(TestItem(1, "Item 1"))

        // When
        val result =
            captureNormalResult { onResult ->
                adapter.removeAt(-1, onResult)
            }

        // Then
        assertTrue(result is NormalAdapterResult.Rejected.InvalidPosition)
        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun addingAtNegativePosition_fails() {
        // Given
        val item = TestItem(1, "Item 1")

        // When
        val result =
            captureNormalResult { onResult ->
                adapter.addItemAt(-1, item, onResult)
            }

        // Then
        assertTrue(result is NormalAdapterResult.Rejected.InvalidPosition)
        assertEquals(0, adapter.itemCount)
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
        adapter.setItems(listOf(item1, item2, item3))

        // When
        val result =
            captureNormalResult { onResult ->
                adapter.removeItems(listOf(item1, item3), onResult)
            }
        shadowOf(Looper.getMainLooper()).idle()

        // Then
        assertTrue(result.isApplied())
        assertEquals(1, adapter.itemCount)
        assertEquals(item2, adapter.getItem(0))
    }

    @Test
    fun removeItems_withEmptyList_fails() {
        // Given
        adapter.setItems(listOf(TestItem(1, "Item 1")))

        // When
        val result =
            captureNormalResult { onResult ->
                adapter.removeItems(emptyList(), onResult)
            }

        // Then
        assertTrue(result is NormalAdapterResult.Rejected.EmptyInput)
        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun removeItems_withNonExistentItems_fails() {
        // Given
        adapter.setItems(listOf(TestItem(1, "Item 1")))

        // When
        val result =
            captureNormalResult { onResult ->
                adapter.removeItems(listOf(TestItem(99, "Ghost")), onResult)
            }

        // Then
        assertTrue(result is NormalAdapterResult.Rejected.NoMatchingItems)
        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun removeRange_removesContiguousSlice() {
        // Given
        adapter.setItems(
            listOf(
                TestItem(1, "Item 1"),
                TestItem(2, "Item 2"),
                TestItem(3, "Item 3"),
                TestItem(4, "Item 4"),
            ),
        )

        // When
        val result =
            captureNormalResult { onResult ->
                adapter.removeRange(1, 2, onResult)
            }
        shadowOf(Looper.getMainLooper()).idle()

        // Then
        assertTrue(result.isApplied())
        assertEquals(2, adapter.itemCount)
        assertEquals(TestItem(1, "Item 1"), adapter.getItem(0))
        assertEquals(TestItem(4, "Item 4"), adapter.getItem(1))
    }

    @Test
    fun removeRange_withInvalidRange_fails() {
        // Given
        adapter.setItems(listOf(TestItem(1, "Item 1"), TestItem(2, "Item 2")))

        // When
        val result =
            captureNormalResult { onResult ->
                adapter.removeRange(0, 10, onResult)
            }

        // Then
        assertTrue(result is NormalAdapterResult.Rejected.InvalidPosition)
        assertEquals(2, adapter.itemCount)
    }

    // ==============================================
    // getItemOrNull / getMutableItemList Tests
    // ==============================================

    @Test
    fun getItemOrNull_returnsItem_whenPositionValid() {
        // Given
        val item = TestItem(1, "Item 1")
        adapter.addItem(item)

        // When
        val result = adapter.getItemOrNull(0)

        // Then
        assertEquals(item, result)
    }

    @Test
    fun getItemOrNull_returnsNull_whenPositionInvalid() {
        // Given
        adapter.addItem(TestItem(1, "Item 1"))

        // When
        val result = adapter.getItemOrNull(999)

        // Then
        assertEquals(null, result)
    }

    @Test
    fun getMutableItemList_returnsCopy_notReference() {
        // Given
        adapter.setItems(listOf(TestItem(1, "Item 1"), TestItem(2, "Item 2")))

        // When
        val mutableCopy = adapter.getMutableItemList()
        mutableCopy.add(TestItem(99, "Injected"))

        // Then - original adapter state must be unchanged
        assertEquals(2, adapter.itemCount)
    }

    /**
     * Integration note:
     *
     * Full ViewHolder binding and click events require:
     * - RecyclerView attached to a window
     * - LayoutManager configured
     * - UI thread execution
     *
     * These are best tested through:
     * - Instrumentation tests (on real device/emulator)
     * - Manual testing in sample app
     *
     * This Robolectric test focuses on:
     * - API correctness (methods don't crash)
     * - Data manipulation (add/remove/replace/range/items)
     * - Listener invocation mechanics
     */
}

private fun captureNormalResult(
    trigger: (((NormalAdapterResult) -> Unit) -> Unit),
): NormalAdapterResult {
    var result: NormalAdapterResult? = null
    trigger { received ->
        result = received
    }
    return requireNotNull(result) { "NormalAdapterResult callback was not invoked" }
}

private fun NormalAdapterResult.isApplied(): Boolean = this is NormalAdapterResult.Applied

private fun RecyclerView.ViewHolder.forceAdapterPosition(position: Int) {
    fun setField(name: String) {
        try {
            val field = RecyclerView.ViewHolder::class.java.getDeclaredField(name)
            field.isAccessible = true
            field.setInt(this, position)
        } catch (_: NoSuchFieldException) {
            // Ignore for older/newer versions
        }
    }
    setField("mPosition")
    setField("mBindingAdapterPosition")
    setField("mAbsoluteAdapterPosition")
}
