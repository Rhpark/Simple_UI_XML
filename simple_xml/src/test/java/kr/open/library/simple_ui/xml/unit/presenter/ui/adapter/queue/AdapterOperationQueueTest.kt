package kr.open.library.simple_ui.xml.unit.presenter.ui.adapter.queue

import kr.open.library.simple_ui.xml.ui.adapter.queue.AdapterOperationQueue
import org.junit.Assert.assertThrows
import org.junit.Test

class AdapterOperationQueueTest {
    private val sampleList = listOf("a", "b", "c")

    @Test
    fun `addItemAt throws when position invalid`() {
        val op = AdapterOperationQueue.AddItemAtOp<String>(position = 5, item = "x", callback = null)

        assertThrows(IndexOutOfBoundsException::class.java) {
            op.execute(sampleList)
        }
    }

    @Test
    fun `addItemsAt throws when position negative`() {
        val op = AdapterOperationQueue.AddItemsAtOp<String>(position = -1, items = listOf("d"), callback = null)

        assertThrows(IndexOutOfBoundsException::class.java) {
            op.execute(sampleList)
        }
    }

    @Test
    fun `removeAt throws when list empty`() {
        val op = AdapterOperationQueue.RemoveAtOp<String>(position = 0, callback = null)

        assertThrows(IndexOutOfBoundsException::class.java) {
            op.execute(emptyList())
        }
    }

    @Test
    fun `removeItem throws when element missing`() {
        val op = AdapterOperationQueue.RemoveItemOp<String>(item = "missing", callback = null)

        assertThrows(IllegalArgumentException::class.java) {
            op.execute(sampleList)
        }
    }

    @Test
    fun `moveItemOp throws when fromPosition invalid`() {
        val op = AdapterOperationQueue.MoveItemOp<String>(fromPosition = -1, toPosition = 1, callback = null)

        assertThrows(IndexOutOfBoundsException::class.java) {
            op.execute(sampleList)
        }
    }

    @Test
    fun `moveItemOp throws when toPosition invalid`() {
        val op = AdapterOperationQueue.MoveItemOp<String>(fromPosition = 0, toPosition = 10, callback = null)

        assertThrows(IndexOutOfBoundsException::class.java) {
            op.execute(sampleList)
        }
    }

    @Test
    fun `replaceItemAt throws when position invalid`() {
        val op = AdapterOperationQueue.ReplaceItemAtOp<String>(position = 10, item = "x", callback = null)

        assertThrows(IndexOutOfBoundsException::class.java) {
            op.execute(sampleList)
        }
    }
}
