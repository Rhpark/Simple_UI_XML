package kr.open.library.simple_ui.xml.unit.ui.temp.base.operation

import kr.open.library.simple_ui.xml.ui.temp.base.AdapterOperationFailure
import kr.open.library.simple_ui.xml.ui.temp.base.operation.AdapterListOperations
import kr.open.library.simple_ui.xml.ui.temp.base.operation.PositionInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests for AdapterListOperations.<br><br>
 * AdapterListOperations 단위 테스트입니다.<br>
 */
@RunWith(RobolectricTestRunner::class)
class AdapterListOperationsTest {
    // ===== addItem tests =====

    @Test
    fun `addItem - adds item to empty list`() {
        val current = emptyList<String>()
        val result = AdapterListOperations.addItem(current, "A")

        assertTrue(result.success)
        assertEquals(listOf("A"), result.items)
        assertEquals(PositionInfo.Insert(0, 1), result.positionInfo)
        assertNull(result.failure)
    }

    @Test
    fun `addItem - adds item to end of list`() {
        val current = listOf("A", "B")
        val result = AdapterListOperations.addItem(current, "C")

        assertTrue(result.success)
        assertEquals(listOf("A", "B", "C"), result.items)
        assertEquals(PositionInfo.Insert(2, 1), result.positionInfo)
    }

    // ===== addItemAt tests =====

    @Test
    fun `addItemAt - adds item at valid position`() {
        val current = listOf("A", "C")
        val result = AdapterListOperations.addItemAt(current, 1, "B")

        assertTrue(result.success)
        assertEquals(listOf("A", "B", "C"), result.items)
        assertEquals(PositionInfo.Insert(1, 1), result.positionInfo)
    }

    @Test
    fun `addItemAt - adds item at position 0`() {
        val current = listOf("B", "C")
        val result = AdapterListOperations.addItemAt(current, 0, "A")

        assertTrue(result.success)
        assertEquals(listOf("A", "B", "C"), result.items)
        assertEquals(PositionInfo.Insert(0, 1), result.positionInfo)
    }

    @Test
    fun `addItemAt - adds item at end position`() {
        val current = listOf("A", "B")
        val result = AdapterListOperations.addItemAt(current, 2, "C")

        assertTrue(result.success)
        assertEquals(listOf("A", "B", "C"), result.items)
        assertEquals(PositionInfo.Insert(2, 1), result.positionInfo)
    }

    @Test
    fun `addItemAt - fails with negative position`() {
        val current = listOf("A", "B")
        val result = AdapterListOperations.addItemAt(current, -1, "C")

        assertFalse(result.success)
        assertEquals(current, result.items)
        assertEquals(PositionInfo.None, result.positionInfo)
        assertTrue(result.failure is AdapterOperationFailure.Validation)
    }

    @Test
    fun `addItemAt - fails with position beyond list size`() {
        val current = listOf("A", "B")
        val result = AdapterListOperations.addItemAt(current, 5, "C")

        assertFalse(result.success)
        assertEquals(current, result.items)
        assertTrue(result.failure is AdapterOperationFailure.Validation)
    }

    // ===== addItems tests =====

    @Test
    fun `addItems - adds multiple items to end`() {
        val current = listOf("A")
        val result = AdapterListOperations.addItems(current, listOf("B", "C"))

        assertTrue(result.success)
        assertEquals(listOf("A", "B", "C"), result.items)
        assertEquals(PositionInfo.Insert(1, 2), result.positionInfo)
    }

    @Test
    fun `addItems - empty items returns original list`() {
        val current = listOf("A", "B")
        val result = AdapterListOperations.addItems(current, emptyList())

        assertTrue(result.success)
        assertEquals(current, result.items)
        assertEquals(PositionInfo.None, result.positionInfo)
    }

    // ===== addItemsAt tests =====

    @Test
    fun `addItemsAt - adds multiple items at valid position`() {
        val current = listOf("A", "D")
        val result = AdapterListOperations.addItemsAt(current, 1, listOf("B", "C"))

        assertTrue(result.success)
        assertEquals(listOf("A", "B", "C", "D"), result.items)
        assertEquals(PositionInfo.Insert(1, 2), result.positionInfo)
    }

    @Test
    fun `addItemsAt - empty items returns original list`() {
        val current = listOf("A", "B")
        val result = AdapterListOperations.addItemsAt(current, 1, emptyList())

        assertTrue(result.success)
        assertEquals(current, result.items)
        assertEquals(PositionInfo.None, result.positionInfo)
    }

    @Test
    fun `addItemsAt - fails with invalid position`() {
        val current = listOf("A", "B")
        val result = AdapterListOperations.addItemsAt(current, 10, listOf("C"))

        assertFalse(result.success)
        assertEquals(current, result.items)
        assertTrue(result.failure is AdapterOperationFailure.Validation)
    }

    // ===== removeItem tests =====

    @Test
    fun `removeItem - removes existing item`() {
        val current = listOf("A", "B", "C")
        val result = AdapterListOperations.removeItem(current, "B")

        assertTrue(result.success)
        assertEquals(listOf("A", "C"), result.items)
        assertEquals(PositionInfo.Remove(1, 1), result.positionInfo)
    }

    @Test
    fun `removeItem - fails for non-existing item`() {
        val current = listOf("A", "B")
        val result = AdapterListOperations.removeItem(current, "C")

        assertFalse(result.success)
        assertEquals(current, result.items)
        assertTrue(result.failure is AdapterOperationFailure.Validation)
    }

    // ===== removeAt tests =====

    @Test
    fun `removeAt - removes item at valid position`() {
        val current = listOf("A", "B", "C")
        val result = AdapterListOperations.removeAt(current, 1)

        assertTrue(result.success)
        assertEquals(listOf("A", "C"), result.items)
        assertEquals(PositionInfo.Remove(1, 1), result.positionInfo)
    }

    @Test
    fun `removeAt - fails with negative position`() {
        val current = listOf("A", "B")
        val result = AdapterListOperations.removeAt<String>(current, -1)

        assertFalse(result.success)
        assertEquals(current, result.items)
        assertTrue(result.failure is AdapterOperationFailure.Validation)
    }

    @Test
    fun `removeAt - fails with position equal or greater than size`() {
        val current = listOf("A", "B")
        val result = AdapterListOperations.removeAt<String>(current, 2)

        assertFalse(result.success)
        assertEquals(current, result.items)
        assertTrue(result.failure is AdapterOperationFailure.Validation)
    }

    // ===== removeAll tests =====

    @Test
    fun `removeAll - clears all items`() {
        val current = listOf("A", "B", "C")
        val result = AdapterListOperations.removeAll(current)

        assertTrue(result.success)
        assertEquals(emptyList<String>(), result.items)
        assertEquals(PositionInfo.Remove(0, 3), result.positionInfo)
    }

    @Test
    fun `removeAll - empty list returns no change`() {
        val current = emptyList<String>()
        val result = AdapterListOperations.removeAll(current)

        assertTrue(result.success)
        assertEquals(emptyList<String>(), result.items)
        assertEquals(PositionInfo.None, result.positionInfo)
    }

    // ===== replaceItemAt tests =====

    @Test
    fun `replaceItemAt - replaces item at valid position`() {
        val current = listOf("A", "B", "C")
        val result = AdapterListOperations.replaceItemAt(current, 1, "X")

        assertTrue(result.success)
        assertEquals(listOf("A", "X", "C"), result.items)
        assertEquals(PositionInfo.Change(1, 1), result.positionInfo)
    }

    @Test
    fun `replaceItemAt - fails with invalid position`() {
        val current = listOf("A", "B")
        val result = AdapterListOperations.replaceItemAt(current, 5, "X")

        assertFalse(result.success)
        assertEquals(current, result.items)
        assertTrue(result.failure is AdapterOperationFailure.Validation)
    }

    // ===== moveItem tests =====

    @Test
    fun `moveItem - moves item forward`() {
        val current = listOf("A", "B", "C", "D")
        val result = AdapterListOperations.moveItem(current, 0, 2)

        assertTrue(result.success)
        assertEquals(listOf("B", "C", "A", "D"), result.items)
        assertEquals(PositionInfo.Move(0, 2), result.positionInfo)
    }

    @Test
    fun `moveItem - moves item backward`() {
        val current = listOf("A", "B", "C", "D")
        val result = AdapterListOperations.moveItem(current, 3, 1)

        assertTrue(result.success)
        assertEquals(listOf("A", "D", "B", "C"), result.items)
        assertEquals(PositionInfo.Move(3, 1), result.positionInfo)
    }

    @Test
    fun `moveItem - same position returns no change`() {
        val current = listOf("A", "B", "C")
        val result = AdapterListOperations.moveItem(current, 1, 1)

        assertTrue(result.success)
        assertEquals(current, result.items)
        assertEquals(PositionInfo.None, result.positionInfo)
    }

    @Test
    fun `moveItem - fails with invalid from position`() {
        val current = listOf("A", "B")
        val result = AdapterListOperations.moveItem(current, -1, 1)

        assertFalse(result.success)
        assertEquals(current, result.items)
        assertTrue(result.failure is AdapterOperationFailure.Validation)
    }

    @Test
    fun `moveItem - fails with invalid to position`() {
        val current = listOf("A", "B")
        val result = AdapterListOperations.moveItem(current, 0, 5)

        assertFalse(result.success)
        assertEquals(current, result.items)
        assertTrue(result.failure is AdapterOperationFailure.Validation)
    }

    // ===== setItems tests =====

    @Test
    fun `setItems - replaces entire list`() {
        val result = AdapterListOperations.setItems(listOf("X", "Y", "Z"))

        assertTrue(result.success)
        assertEquals(listOf("X", "Y", "Z"), result.items)
        assertEquals(PositionInfo.Full, result.positionInfo)
    }

    // ===== updateItems tests =====

    @Test
    fun `updateItems - transforms list using updater`() {
        val current = listOf("A", "B", "C")
        val result = AdapterListOperations.updateItems(current) { list ->
            list.removeAt(1)
            list.add("D")
        }

        assertTrue(result.success)
        assertEquals(listOf("A", "C", "D"), result.items)
        assertEquals(PositionInfo.Full, result.positionInfo)
    }

    // ===== Immutability tests =====

    @Test
    fun `operations do not modify original list`() {
        val original = listOf("A", "B", "C")
        val originalCopy = original.toList()

        AdapterListOperations.addItem(original, "D")
        AdapterListOperations.removeItem(original, "B")
        AdapterListOperations.replaceItemAt(original, 0, "X")

        assertEquals(originalCopy, original)
    }
}
