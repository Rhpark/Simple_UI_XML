package kr.open.library.simple_ui.extensions.conditional

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class CollectionExtensionsTest {

    @Test
    fun filterIf_appliesPredicateWhenConditionTrue() {
        val numbers = listOf(1, 2, 3, 4, 5)

        val result = numbers.filterIf(condition = true) { it % 2 == 0 }

        assertEquals(listOf(2, 4), result)
    }

    @Test
    fun filterIf_returnsOriginalWhenConditionFalse() {
        val numbers = listOf(1, 2, 3)

        val result = numbers.filterIf(condition = false) { it > 1 }

        assertSame(numbers, result)
    }

    @Test
    fun list_ifNotEmpty_executesActionOnlyWhenNotEmpty() {
        val source = listOf("a", "b")
        var called = false

        val result = source.ifNotEmpty { _: List<String> -> called = true }

        assertTrue(called)
        assertSame(source, result)
    }

    @Test
    fun list_ifEmpty_executesActionOnlyWhenEmpty() {
        val empty = emptyList<String>()
        var called = false

        val result = empty.ifEmpty { called = true }

        assertTrue(called)
        assertSame(empty, result)
    }

    @Test
    fun map_ifNotEmpty_and_ifEmpty_behaveCorrectly() {
        val map = mapOf("key" to 1)
        var notEmptyCalled = false
        var emptyCalled = false

        val notEmptyResult = map.ifNotEmpty { _: Map<String, Int> -> notEmptyCalled = true }
        val emptyResult = emptyMap<String, Int>().ifEmpty { _: Map<String, Int> -> emptyCalled = true }

        assertTrue(notEmptyCalled)
        assertSame(map, notEmptyResult)
        assertTrue(emptyCalled)
        assertSame(emptyMap<String, Int>(), emptyResult)
    }

    // ========== Extended Tests ==========

    @Test
    fun filterIf_handlesEmptyList() {
        val empty = emptyList<Int>()

        val resultTrue = empty.filterIf(condition = true) { it > 0 }
        val resultFalse = empty.filterIf(condition = false) { it > 0 }

        assertEquals(emptyList<Int>(), resultTrue)
        assertSame(empty, resultFalse)
    }

    @Test
    fun filterIf_predicateNotInvokedWhenFalse() {
        var predicateCalled = false
        val numbers = listOf(1, 2, 3)

        numbers.filterIf(condition = false) {
            predicateCalled = true
            true
        }

        assertEquals(false, predicateCalled)
    }

    @Test
    fun list_ifNotEmpty_withParameter_receivesCorrectList() {
        val source = listOf("a", "b", "c")
        var receivedList: List<String>? = null

        val result = source.ifNotEmpty { list ->
            receivedList = list
        }

        assertSame(source, receivedList)
        assertSame(source, result)
    }

    @Test
    fun list_ifNotEmpty_noParameter_doesNotReceiveList() {
        val source = listOf(1, 2, 3)
        var executed = false

        val result = source.ifNotEmpty { _: List<Int> ->
            executed = true
        }

        assertTrue(executed)
        assertSame(source, result)
    }

    @Test
    fun list_ifEmpty_doesNotExecuteWhenNotEmpty() {
        val source = listOf("item")
        var called = false

        source.ifEmpty { called = true }

        assertEquals(false, called)
    }

    @Test
    fun list_ifNotEmpty_doesNotExecuteWhenEmpty() {
        val empty = emptyList<Int>()
        var called = false

        empty.ifNotEmpty { _: List<Int> -> called = true }

        assertEquals(false, called)
    }

    @Test
    fun list_chainingMultipleConditionals() {
        val numbers = listOf(1, 2, 3, 4, 5)
        val log = mutableListOf<String>()

        numbers
            .filterIf(condition = true) { it > 2 }
            .ifNotEmpty { _: List<Int> -> log += "has-items" }
            .ifEmpty { log += "empty" }

        assertEquals(listOf("has-items"), log)
    }

    @Test
    fun map_ifNotEmpty_withParameter_receivesCorrectMap() {
        val source = mapOf("a" to 1, "b" to 2)
        var receivedMap: Map<String, Int>? = null

        val result = source.ifNotEmpty { map ->
            receivedMap = map
        }

        assertSame(source, receivedMap)
        assertSame(source, result)
    }

    @Test
    fun map_ifEmpty_withParameter_receivesCorrectMap() {
        val emptyMap = emptyMap<String, Int>()
        var receivedMap: Map<String, Int>? = null

        val result = emptyMap.ifEmpty { map ->
            receivedMap = map
        }

        assertSame(emptyMap, receivedMap)
        assertSame(emptyMap, result)
    }

    @Test
    fun map_ifNotEmpty_doesNotExecuteWhenEmpty() {
        val empty = emptyMap<String, Int>()
        var called = false

        empty.ifNotEmpty { _: Map<String, Int> -> called = true }

        assertEquals(false, called)
    }

    @Test
    fun map_ifEmpty_doesNotExecuteWhenNotEmpty() {
        val map = mapOf("key" to "value")
        var called = false

        map.ifEmpty { _: Map<String, String> -> called = true }

        assertEquals(false, called)
    }
}
