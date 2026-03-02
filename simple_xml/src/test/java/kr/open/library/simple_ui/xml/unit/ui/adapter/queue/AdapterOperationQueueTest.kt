package kr.open.library.simple_ui.xml.unit.ui.adapter.queue

import kr.open.library.simple_ui.xml.ui.adapter.list.base.queue.AdapterOperationQueue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class AdapterOperationQueueTest {
    private val sampleList = listOf("a", "b", "c")

    // ==============================================
    // SetItemsOp
    // ==============================================

    @Test
    fun `setItems replaces entire list`() {
        val op = AdapterOperationQueue.SetItemsOp(items = listOf("x", "y"), callback = null)
        val result = op.execute(sampleList)
        assertEquals(listOf("x", "y"), result)
    }

    @Test
    fun `setItems with empty list clears list`() {
        val op = AdapterOperationQueue.SetItemsOp<String>(items = emptyList(), callback = null)
        val result = op.execute(sampleList)
        assertTrue(result.isEmpty())
    }

    // ==============================================
    // AddItemOp
    // ==============================================

    @Test
    fun `addItem appends to end`() {
        val op = AdapterOperationQueue.AddItemOp(item = "d", callback = null)
        val result = op.execute(sampleList)
        assertEquals(listOf("a", "b", "c", "d"), result)
    }

    @Test
    fun `addItem on empty list produces single-element list`() {
        val op = AdapterOperationQueue.AddItemOp(item = "x", callback = null)
        val result = op.execute(emptyList())
        assertEquals(listOf("x"), result)
    }

    // ==============================================
    // AddItemAtOp
    // ==============================================

    @Test
    fun `addItemAt inserts at valid position`() {
        val op = AdapterOperationQueue.AddItemAtOp(position = 1, item = "x", callback = null)
        val result = op.execute(sampleList)
        assertEquals(listOf("a", "x", "b", "c"), result)
    }

    @Test
    fun `addItemAt inserts at position 0`() {
        val op = AdapterOperationQueue.AddItemAtOp(position = 0, item = "x", callback = null)
        val result = op.execute(sampleList)
        assertEquals(listOf("x", "a", "b", "c"), result)
    }

    @Test
    fun `addItemAt inserts at end position`() {
        val op = AdapterOperationQueue.AddItemAtOp(position = 3, item = "x", callback = null)
        val result = op.execute(sampleList)
        assertEquals(listOf("a", "b", "c", "x"), result)
    }

    @Test
    fun `addItemAt throws when position invalid`() {
        val op = AdapterOperationQueue.AddItemAtOp<String>(position = 5, item = "x", callback = null)
        assertThrows(IndexOutOfBoundsException::class.java) { op.execute(sampleList) }
    }

    @Test
    fun `addItemAt throws when position negative`() {
        val op = AdapterOperationQueue.AddItemAtOp<String>(position = -1, item = "x", callback = null)
        assertThrows(IndexOutOfBoundsException::class.java) { op.execute(sampleList) }
    }

    // ==============================================
    // AddItemsOp
    // ==============================================

    @Test
    fun `addItems appends all to end`() {
        val op = AdapterOperationQueue.AddItemsOp(items = listOf("d", "e"), callback = null)
        val result = op.execute(sampleList)
        assertEquals(listOf("a", "b", "c", "d", "e"), result)
    }

    @Test
    fun `addItems with empty items returns unchanged list`() {
        val op = AdapterOperationQueue.AddItemsOp<String>(items = emptyList(), callback = null)
        val result = op.execute(sampleList)
        assertEquals(sampleList, result)
    }

    // ==============================================
    // AddItemsAtOp
    // ==============================================

    @Test
    fun `addItemsAt inserts at valid position`() {
        val op = AdapterOperationQueue.AddItemsAtOp(position = 1, items = listOf("x", "y"), callback = null)
        val result = op.execute(sampleList)
        assertEquals(listOf("a", "x", "y", "b", "c"), result)
    }

    @Test
    fun `addItemsAt inserts at position 0`() {
        val op = AdapterOperationQueue.AddItemsAtOp(position = 0, items = listOf("x", "y"), callback = null)
        val result = op.execute(sampleList)
        assertEquals(listOf("x", "y", "a", "b", "c"), result)
    }

    @Test
    fun `addItemsAt throws when position negative`() {
        val op = AdapterOperationQueue.AddItemsAtOp<String>(position = -1, items = listOf("d"), callback = null)
        assertThrows(IndexOutOfBoundsException::class.java) { op.execute(sampleList) }
    }

    @Test
    fun `addItemsAt throws when position out of bounds`() {
        val op = AdapterOperationQueue.AddItemsAtOp<String>(position = 10, items = listOf("d"), callback = null)
        assertThrows(IndexOutOfBoundsException::class.java) { op.execute(sampleList) }
    }

    // ==============================================
    // RemoveAtOp
    // ==============================================

    @Test
    fun `removeAt removes item at valid position`() {
        val op = AdapterOperationQueue.RemoveAtOp<String>(position = 1, callback = null)
        val result = op.execute(sampleList)
        assertEquals(listOf("a", "c"), result)
    }

    @Test
    fun `removeAt removes first item`() {
        val op = AdapterOperationQueue.RemoveAtOp<String>(position = 0, callback = null)
        val result = op.execute(sampleList)
        assertEquals(listOf("b", "c"), result)
    }

    @Test
    fun `removeAt removes last item`() {
        val op = AdapterOperationQueue.RemoveAtOp<String>(position = 2, callback = null)
        val result = op.execute(sampleList)
        assertEquals(listOf("a", "b"), result)
    }

    @Test
    fun `removeAt throws when list empty`() {
        val op = AdapterOperationQueue.RemoveAtOp<String>(position = 0, callback = null)
        assertThrows(IndexOutOfBoundsException::class.java) { op.execute(emptyList()) }
    }

    @Test
    fun `removeAt throws when position out of bounds`() {
        val op = AdapterOperationQueue.RemoveAtOp<String>(position = 5, callback = null)
        assertThrows(IndexOutOfBoundsException::class.java) { op.execute(sampleList) }
    }

    // ==============================================
    // RemoveItemOp
    // ==============================================

    @Test
    fun `removeItem removes matching element`() {
        val op = AdapterOperationQueue.RemoveItemOp(item = "b", callback = null)
        val result = op.execute(sampleList)
        assertEquals(listOf("a", "c"), result)
    }

    @Test
    fun `removeItem removes first occurrence only`() {
        val list = listOf("a", "b", "a")
        val op = AdapterOperationQueue.RemoveItemOp(item = "a", callback = null)
        val result = op.execute(list)
        assertEquals(listOf("b", "a"), result)
    }

    @Test
    fun `removeItem throws when element missing`() {
        val op = AdapterOperationQueue.RemoveItemOp<String>(item = "missing", callback = null)
        assertThrows(IndexOutOfBoundsException::class.java) { op.execute(sampleList) }
    }

    // ==============================================
    // RemoveItemsOp
    // ==============================================

    @Test
    fun `removeItems removes all matching elements`() {
        val op = AdapterOperationQueue.RemoveItemsOp(items = listOf("a", "c"), callback = null)
        val result = op.execute(sampleList)
        assertEquals(listOf("b"), result)
    }

    @Test
    fun `removeItems with empty items returns unchanged list`() {
        val op = AdapterOperationQueue.RemoveItemsOp<String>(items = emptyList(), callback = null)
        val result = op.execute(sampleList)
        assertEquals(sampleList, result)
    }

    @Test
    fun `removeItems ignores non-existing elements`() {
        val op = AdapterOperationQueue.RemoveItemsOp(items = listOf("a", "z"), callback = null)
        val result = op.execute(sampleList)
        assertEquals(listOf("b", "c"), result)
    }

    // ==============================================
    // RemoveRangeOp
    // ==============================================

    @Test
    fun `removeRange removes contiguous slice`() {
        val op = AdapterOperationQueue.RemoveRangeOp<String>(start = 1, count = 2, callback = null)
        val result = op.execute(sampleList)
        assertEquals(listOf("a"), result)
    }

    @Test
    fun `removeRange removes from start`() {
        val op = AdapterOperationQueue.RemoveRangeOp<String>(start = 0, count = 2, callback = null)
        val result = op.execute(sampleList)
        assertEquals(listOf("c"), result)
    }

    @Test
    fun `removeRange removes entire list`() {
        val op = AdapterOperationQueue.RemoveRangeOp<String>(start = 0, count = 3, callback = null)
        val result = op.execute(sampleList)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `removeRange throws when count is zero`() {
        val op = AdapterOperationQueue.RemoveRangeOp<String>(start = 0, count = 0, callback = null)
        assertThrows(IndexOutOfBoundsException::class.java) { op.execute(sampleList) }
    }

    @Test
    fun `removeRange throws when start is negative`() {
        val op = AdapterOperationQueue.RemoveRangeOp<String>(start = -1, count = 1, callback = null)
        assertThrows(IndexOutOfBoundsException::class.java) { op.execute(sampleList) }
    }

    @Test
    fun `removeRange throws when count exceeds remaining`() {
        val op = AdapterOperationQueue.RemoveRangeOp<String>(start = 2, count = 5, callback = null)
        assertThrows(IndexOutOfBoundsException::class.java) { op.execute(sampleList) }
    }

    // ==============================================
    // ClearItemsOp
    // ==============================================

    @Test
    fun `clearItems returns empty list`() {
        val op = AdapterOperationQueue.ClearItemsOp<String>(callback = null)
        val result = op.execute(sampleList)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `clearItems on empty list stays empty`() {
        val op = AdapterOperationQueue.ClearItemsOp<String>(callback = null)
        val result = op.execute(emptyList())
        assertTrue(result.isEmpty())
    }

    // ==============================================
    // MoveItemOp
    // ==============================================

    @Test
    fun `moveItem moves forward`() {
        val op = AdapterOperationQueue.MoveItemOp<String>(fromPosition = 0, toPosition = 2, callback = null)
        val result = op.execute(sampleList)
        assertEquals(listOf("b", "c", "a"), result)
    }

    @Test
    fun `moveItem moves backward`() {
        val op = AdapterOperationQueue.MoveItemOp<String>(fromPosition = 2, toPosition = 0, callback = null)
        val result = op.execute(sampleList)
        assertEquals(listOf("c", "a", "b"), result)
    }

    @Test
    fun `moveItem to same position preserves order`() {
        val op = AdapterOperationQueue.MoveItemOp<String>(fromPosition = 1, toPosition = 1, callback = null)
        val result = op.execute(sampleList)
        assertEquals(sampleList, result)
    }

    @Test
    fun `moveItemOp throws when fromPosition invalid`() {
        val op = AdapterOperationQueue.MoveItemOp<String>(fromPosition = -1, toPosition = 1, callback = null)
        assertThrows(IndexOutOfBoundsException::class.java) { op.execute(sampleList) }
    }

    @Test
    fun `moveItemOp throws when toPosition invalid`() {
        val op = AdapterOperationQueue.MoveItemOp<String>(fromPosition = 0, toPosition = 10, callback = null)
        assertThrows(IndexOutOfBoundsException::class.java) { op.execute(sampleList) }
    }

    // ==============================================
    // ReplaceItemAtOp
    // ==============================================

    @Test
    fun `replaceItemAt replaces item at valid position`() {
        val op = AdapterOperationQueue.ReplaceItemAtOp(position = 1, item = "x", callback = null)
        val result = op.execute(sampleList)
        assertEquals(listOf("a", "x", "c"), result)
    }

    @Test
    fun `replaceItemAt replaces first item`() {
        val op = AdapterOperationQueue.ReplaceItemAtOp(position = 0, item = "x", callback = null)
        val result = op.execute(sampleList)
        assertEquals(listOf("x", "b", "c"), result)
    }

    @Test
    fun `replaceItemAt replaces last item`() {
        val op = AdapterOperationQueue.ReplaceItemAtOp(position = 2, item = "x", callback = null)
        val result = op.execute(sampleList)
        assertEquals(listOf("a", "b", "x"), result)
    }

    @Test
    fun `replaceItemAt throws when position invalid`() {
        val op = AdapterOperationQueue.ReplaceItemAtOp<String>(position = 10, item = "x", callback = null)
        assertThrows(IndexOutOfBoundsException::class.java) { op.execute(sampleList) }
    }

    @Test
    fun `replaceItemAt throws when position negative`() {
        val op = AdapterOperationQueue.ReplaceItemAtOp<String>(position = -1, item = "x", callback = null)
        assertThrows(IndexOutOfBoundsException::class.java) { op.execute(sampleList) }
    }

    // ==============================================
    // Immutability: original list not mutated
    // ==============================================

    @Test
    fun `operations do not mutate original list`() {
        val original = listOf("a", "b", "c")

        AdapterOperationQueue.AddItemOp(item = "d", callback = null).execute(original)
        AdapterOperationQueue.RemoveAtOp<String>(position = 0, callback = null).execute(original)
        AdapterOperationQueue.MoveItemOp<String>(fromPosition = 0, toPosition = 2, callback = null).execute(original)
        AdapterOperationQueue.ReplaceItemAtOp(position = 0, item = "x", callback = null).execute(original)

        assertEquals(listOf("a", "b", "c"), original)
    }
}
