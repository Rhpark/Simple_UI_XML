package kr.open.library.simple_ui.xml.robolectric.presenter.ui.adapter

import android.os.Build
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kr.open.library.simple_ui.xml.ui.adapter.queue.QueueDropReason
import kr.open.library.simple_ui.xml.ui.adapter.queue.QueueOverflowPolicy
import kr.open.library.simple_ui.xml.ui.temp.base.AdapterOperationFailure
import kr.open.library.simple_ui.xml.ui.temp.base.AdapterOperationFailureInfo
import kr.open.library.simple_ui.xml.ui.temp.base.AdapterQueueMergeKeys
import kr.open.library.simple_ui.xml.ui.temp.base.AdapterThreadCheckMode
import kr.open.library.simple_ui.xml.ui.temp.base.list.RootListAdapterCore
import kr.open.library.simple_ui.xml.ui.temp.base.normal.RootRcvAdapterCore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.util.concurrent.Executor

/**
 * Robolectric tests for temp queue policy and failure handling.<br><br>
 * temp 큐 정책과 실패 처리에 대한 Robolectric 테스트입니다.<br>
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class TempQueuePolicyRobolectricTest {
    data class TestItem(
        val id: Int,
        val name: String,
    )

    private class TestViewHolder(
        itemView: View,
    ) : RecyclerView.ViewHolder(itemView)

    private class TestRcvAdapter(
        testExecutor: Executor = Executor { it.run() },
    ) : RootRcvAdapterCore<TestItem, TestViewHolder>(
            diffUtilEnabled = false,
            diffExecutor = testExecutor,
            operationExecutor = testExecutor,
        ) {
        override fun createViewHolderInternal(parent: ViewGroup, viewType: Int): TestViewHolder = TestViewHolder(View(parent.context))

        override fun onBindItem(holder: TestViewHolder, position: Int, item: TestItem) {
            // no-op
        }
    }

    private class TestListAdapter(
        testExecutor: Executor = Executor { it.run() },
    ) : RootListAdapterCore<TestItem, TestViewHolder>(
            diffCallback =
                object : DiffUtil.ItemCallback<TestItem>() {
                    override fun areItemsTheSame(oldItem: TestItem, newItem: TestItem): Boolean = oldItem.id == newItem.id

                    override fun areContentsTheSame(oldItem: TestItem, newItem: TestItem): Boolean = oldItem == newItem
                },
            diffExecutor = testExecutor,
            operationExecutor = testExecutor,
        ) {
        override fun createViewHolderInternal(parent: ViewGroup, viewType: Int): TestViewHolder = TestViewHolder(View(parent.context))

        override fun onBindItem(holder: TestViewHolder, position: Int, item: TestItem) {
            // no-op
        }
    }

    @Test
    fun queuePolicy_dropsNewOperation_whenQueueFull_rcvAdapter() {
        val adapter = TestRcvAdapter()
        val failures = mutableListOf<AdapterOperationFailureInfo>()

        adapter.setThreadCheckMode(AdapterThreadCheckMode.OFF)
        adapter.setQueuePolicy(1, QueueOverflowPolicy.DROP_NEW)
        adapter.setOnAdapterOperationFailureListener { failures.add(it) }

        val worker =
            Thread {
                adapter.addItem(TestItem(1, "A"))
                adapter.addItem(TestItem(2, "B"))
            }
        worker.start()
        worker.join()
        shadowOf(Looper.getMainLooper()).idle()

        assertEquals(1, adapter.getItems().size)
        assertEquals(1, failures.size)
        val failure = failures.first().failure
        assertTrue(failure is AdapterOperationFailure.Dropped)
        assertEquals(QueueDropReason.QUEUE_FULL_DROP_NEW, (failure as AdapterOperationFailure.Dropped).reason)
    }

    @Test
    fun validationFailure_isReported_listAdapter() {
        val adapter = TestListAdapter()
        val failures = mutableListOf<AdapterOperationFailureInfo>()

        adapter.setOnAdapterOperationFailureListener { failures.add(it) }
        adapter.addItemAt(1, TestItem(1, "Invalid"))
        shadowOf(Looper.getMainLooper()).idle()

        assertEquals(1, failures.size)
        val failure = failures.first().failure
        assertTrue(failure is AdapterOperationFailure.Validation)
    }

    @Test
    fun queueMerge_coalescesSetItems_rcvAdapter() {
        val adapter = TestRcvAdapter()
        val failures = mutableListOf<AdapterOperationFailureInfo>()

        adapter.setThreadCheckMode(AdapterThreadCheckMode.OFF)
        adapter.setQueueMergeKeys(setOf(AdapterQueueMergeKeys.SET_ITEMS))
        adapter.setOnAdapterOperationFailureListener { failures.add(it) }

        val worker =
            Thread {
                adapter.setItems(listOf(TestItem(1, "A")))
                adapter.setItems(listOf(TestItem(2, "B")))
            }
        worker.start()
        worker.join()
        shadowOf(Looper.getMainLooper()).idle()

        val items = adapter.getItems()
        assertEquals(1, items.size)
        assertEquals(2, items.first().id)
        assertEquals(1, failures.size)
        val failure = failures.first().failure
        assertTrue(failure is AdapterOperationFailure.Dropped)
        assertEquals(QueueDropReason.MERGED, (failure as AdapterOperationFailure.Dropped).reason)
    }

    @Test
    fun queueMerge_coalescesSetItems_listAdapter() {
        val adapter = TestListAdapter()
        val failures = mutableListOf<AdapterOperationFailureInfo>()

        adapter.setThreadCheckMode(AdapterThreadCheckMode.OFF)
        adapter.setQueueMergeKeys(setOf(AdapterQueueMergeKeys.SET_ITEMS))
        adapter.setOnAdapterOperationFailureListener { failures.add(it) }

        val worker =
            Thread {
                adapter.setItems(listOf(TestItem(1, "A")))
                adapter.setItems(listOf(TestItem(2, "B")))
            }
        worker.start()
        worker.join()
        shadowOf(Looper.getMainLooper()).idle()

        val items = adapter.getItems()
        assertEquals(1, items.size)
        assertEquals(2, items.first().id)
        assertEquals(1, failures.size)
        val failure = failures.first().failure
        assertTrue(failure is AdapterOperationFailure.Dropped)
        assertEquals(QueueDropReason.MERGED, (failure as AdapterOperationFailure.Dropped).reason)
    }
}
