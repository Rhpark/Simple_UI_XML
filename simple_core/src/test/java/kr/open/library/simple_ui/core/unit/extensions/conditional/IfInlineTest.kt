package kr.open.library.simple_ui.core.unit.extensions.conditional

import kr.open.library.simple_ui.core.extensions.conditional.firstNotNull
import kr.open.library.simple_ui.core.extensions.conditional.ifEquals
import kr.open.library.simple_ui.core.extensions.conditional.ifFalse
import kr.open.library.simple_ui.core.extensions.conditional.ifGreaterThan
import kr.open.library.simple_ui.core.extensions.conditional.ifGreaterThanOrEqual
import kr.open.library.simple_ui.core.extensions.conditional.ifNotEmpty
import kr.open.library.simple_ui.core.extensions.conditional.ifNotEquals
import kr.open.library.simple_ui.core.extensions.conditional.ifTrue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Test

class IfInlineTest {

    @Test
    fun numeric_ifGreaterThan_executesPositiveBranch() {
        val result = 10.ifGreaterThan(5) { "hit" }

        assertEquals("hit", result)
    }

    @Test
    fun numeric_ifGreaterThan_returnsNullWhenFalse() {
        val result = 3.ifGreaterThan(5) { "shouldNotRun" }

        assertNull(result)
    }

    @Test
    fun boolean_ifTrue_executesPositiveBranch() {
        val outcome = true.ifTrue(
            positiveWork = { "positive" },
            negativeWork = { "negative" }
        )

        assertEquals("positive", outcome)
    }

    @Test
    fun boolean_ifFalse_executesNegativeBranch() {
        val result = false.ifFalse { "executed" }

        assertEquals("executed", result)
    }

    @Test
    fun firstNotNull_returnsFirstNonNullValue() {
        val value = firstNotNull(null, null, "answer", "ignored")

        assertEquals("answer", value)
    }

    @Test
    fun firstNotNull_returnsNullWhenAllNull() {
        val value = firstNotNull<String>(null, null)

        assertNull(value)
    }

    @Test
    fun numeric_ifEquals_executesCorrectBranch() {
        val positiveResult = 42.ifEquals(42, positiveWork = { "yes" }, negativeWork = { "no" })
        val nullResult = 42.ifEquals(24) { "won't run" }

        assertEquals("yes", positiveResult)
        assertNull(nullResult)
    }

    @Test
    fun numeric_ifNotEquals_executesWhenDifferent() {
        val result = 5.ifNotEquals(10) { "different" }

        assertEquals("different", result)
    }

    @Test
    fun nullableChain_allowsFluentUsage() {
        val builder = mutableListOf<String>()

        listOf("a", "b").ifNotEmpty { _: List<String> -> builder += "list-not-empty" }
        true.ifTrue { builder += "boolean-true" }

        assertEquals(listOf("list-not-empty", "boolean-true"), builder)
    }

    // ========== ifGreaterThan Extended Tests ==========

    @Test
    fun float_ifGreaterThan_singleArg_works() {
        val hit = 5.5f.ifGreaterThan(3.2f) { "executed" }
        val miss = 2.0f.ifGreaterThan(5.0f) { "skipped" }

        assertEquals("executed", hit)
        assertNull(miss)
    }

    @Test
    fun float_ifGreaterThan_twoArgs_executesBranches() {
        val positive = 10.5f.ifGreaterThan(5.0f, { "yes" }, { "no" })
        val negative = 2.0f.ifGreaterThan(5.0f, { "yes" }, { "no" })

        assertEquals("yes", positive)
        assertEquals("no", negative)
    }

    @Test
    fun double_ifGreaterThan_singleArg_works() {
        val hit = 10.5.ifGreaterThan(5.2) { "hit" }
        val miss = 3.0.ifGreaterThan(10.0) { "miss" }

        assertEquals("hit", hit)
        assertNull(miss)
    }

    @Test
    fun double_ifGreaterThan_twoArgs_executesBranches() {
        val positive = 15.0.ifGreaterThan(10.0, { "greater" }, { "notGreater" })
        val negative = 5.0.ifGreaterThan(10.0, { "greater" }, { "notGreater" })

        assertEquals("greater", positive)
        assertEquals("notGreater", negative)
    }

    @Test
    fun long_ifGreaterThan_singleArg_works() {
        val hit = 100L.ifGreaterThan(50L) { "executed" }
        val miss = 20L.ifGreaterThan(50L) { "skipped" }

        assertEquals("executed", hit)
        assertNull(miss)
    }

    @Test
    fun long_ifGreaterThan_twoArgs_executesBranches() {
        val positive = 1000L.ifGreaterThan(500L, { "yes" }, { "no" })
        val negative = 100L.ifGreaterThan(500L, { "yes" }, { "no" })

        assertEquals("yes", positive)
        assertEquals("no", negative)
    }

    @Test
    fun short_ifGreaterThan_singleArg_works() {
        val hit = 50.toShort().ifGreaterThan(30.toShort()) { "hit" }
        val miss = 10.toShort().ifGreaterThan(30.toShort()) { "miss" }

        assertEquals("hit", hit)
        assertNull(miss)
    }

    @Test
    fun short_ifGreaterThan_twoArgs_executesBranches() {
        val positive = 100.toShort().ifGreaterThan(50.toShort(), { "greater" }, { "less" })
        val negative = 20.toShort().ifGreaterThan(50.toShort(), { "greater" }, { "less" })

        assertEquals("greater", positive)
        assertEquals("less", negative)
    }

    @Test
    fun int_ifGreaterThan_twoArgs_executesBranches() {
        val positive = 20.ifGreaterThan(10, { "yes" }, { "no" })
        val negative = 5.ifGreaterThan(10, { "yes" }, { "no" })

        assertEquals("yes", positive)
        assertEquals("no", negative)
    }

    // ========== ifGreaterThanOrEqual Tests ==========

    @Test
    fun int_ifGreaterThanOrEqual_singleArg_works() {
        val greater = 10.ifGreaterThanOrEqual(5) { "hit" }
        val equal = 5.ifGreaterThanOrEqual(5) { "also hit" }
        val less = 3.ifGreaterThanOrEqual(5) { "miss" }

        assertEquals("hit", greater)
        assertEquals("also hit", equal)
        assertNull(less)
    }

    @Test
    fun int_ifGreaterThanOrEqual_twoArgs_executesBranches() {
        val positive = 10.ifGreaterThanOrEqual(10, { "yes" }, { "no" })
        val negative = 5.ifGreaterThanOrEqual(10, { "yes" }, { "no" })

        assertEquals("yes", positive)
        assertEquals("no", negative)
    }

    @Test
    fun float_ifGreaterThanOrEqual_singleArg_works() {
        val greater = 5.5f.ifGreaterThanOrEqual(3.0f) { "hit" }
        val equal = 3.0f.ifGreaterThanOrEqual(3.0f) { "equal" }
        val less = 1.0f.ifGreaterThanOrEqual(3.0f) { "miss" }

        assertEquals("hit", greater)
        assertEquals("equal", equal)
        assertNull(less)
    }

    @Test
    fun float_ifGreaterThanOrEqual_twoArgs_executesBranches() {
        val positive = 10.0f.ifGreaterThanOrEqual(10.0f, { "yes" }, { "no" })
        val negative = 5.0f.ifGreaterThanOrEqual(10.0f, { "yes" }, { "no" })

        assertEquals("yes", positive)
        assertEquals("no", negative)
    }

    @Test
    fun double_ifGreaterThanOrEqual_singleArg_works() {
        val greater = 10.5.ifGreaterThanOrEqual(5.0) { "hit" }
        val equal = 5.0.ifGreaterThanOrEqual(5.0) { "equal" }
        val less = 2.0.ifGreaterThanOrEqual(5.0) { "miss" }

        assertEquals("hit", greater)
        assertEquals("equal", equal)
        assertNull(less)
    }

    @Test
    fun double_ifGreaterThanOrEqual_twoArgs_executesBranches() {
        val positive = 15.0.ifGreaterThanOrEqual(15.0, { "yes" }, { "no" })
        val negative = 10.0.ifGreaterThanOrEqual(15.0, { "yes" }, { "no" })

        assertEquals("yes", positive)
        assertEquals("no", negative)
    }

    @Test
    fun short_ifGreaterThanOrEqual_singleArg_works() {
        val greater = 50.toShort().ifGreaterThanOrEqual(30.toShort()) { "hit" }
        val equal = 30.toShort().ifGreaterThanOrEqual(30.toShort()) { "equal" }
        val less = 10.toShort().ifGreaterThanOrEqual(30.toShort()) { "miss" }

        assertEquals("hit", greater)
        assertEquals("equal", equal)
        assertNull(less)
    }

    @Test
    fun short_ifGreaterThanOrEqual_twoArgs_executesBranches() {
        val positive = 100.toShort().ifGreaterThanOrEqual(100.toShort(), { "yes" }, { "no" })
        val negative = 50.toShort().ifGreaterThanOrEqual(100.toShort(), { "yes" }, { "no" })

        assertEquals("yes", positive)
        assertEquals("no", negative)
    }

    @Test
    fun long_ifGreaterThanOrEqual_singleArg_works() {
        val greater = 100L.ifGreaterThanOrEqual(50L) { "hit" }
        val equal = 50L.ifGreaterThanOrEqual(50L) { "equal" }
        val less = 20L.ifGreaterThanOrEqual(50L) { "miss" }

        assertEquals("hit", greater)
        assertEquals("equal", equal)
        assertNull(less)
    }

    @Test
    fun long_ifGreaterThanOrEqual_twoArgs_executesBranches() {
        val positive = 500L.ifGreaterThanOrEqual(500L, { "yes" }, { "no" })
        val negative = 200L.ifGreaterThanOrEqual(500L, { "yes" }, { "no" })

        assertEquals("yes", positive)
        assertEquals("no", negative)
    }

    // ========== ifEquals Extended Tests ==========

    @Test
    fun float_ifEquals_singleArg_works() {
        val hit = 3.14f.ifEquals(3.14f) { "match" }
        val miss = 3.14f.ifEquals(2.71f) { "no match" }

        assertEquals("match", hit)
        assertNull(miss)
    }

    @Test
    fun float_ifEquals_twoArgs_executesBranches() {
        val positive = 5.0f.ifEquals(5.0f, { "equal" }, { "notEqual" })
        val negative = 5.0f.ifEquals(10.0f, { "equal" }, { "notEqual" })

        assertEquals("equal", positive)
        assertEquals("notEqual", negative)
    }

    @Test
    fun double_ifEquals_singleArg_works() {
        val hit = 2.71828.ifEquals(2.71828) { "e" }
        val miss = 2.71828.ifEquals(3.14159) { "pi" }

        assertEquals("e", hit)
        assertNull(miss)
    }

    @Test
    fun double_ifEquals_twoArgs_executesBranches() {
        val positive = 10.0.ifEquals(10.0, { "same" }, { "different" })
        val negative = 10.0.ifEquals(20.0, { "same" }, { "different" })

        assertEquals("same", positive)
        assertEquals("different", negative)
    }

    @Test
    fun long_ifEquals_singleArg_works() {
        val hit = 1234L.ifEquals(1234L) { "match" }
        val miss = 1234L.ifEquals(5678L) { "no match" }

        assertEquals("match", hit)
        assertNull(miss)
    }

    @Test
    fun long_ifEquals_twoArgs_executesBranches() {
        val positive = 999L.ifEquals(999L, { "yes" }, { "no" })
        val negative = 999L.ifEquals(111L, { "yes" }, { "no" })

        assertEquals("yes", positive)
        assertEquals("no", negative)
    }

    @Test
    fun short_ifEquals_singleArg_works() {
        val hit = 42.toShort().ifEquals(42.toShort()) { "answer" }
        val miss = 42.toShort().ifEquals(24.toShort()) { "wrong" }

        assertEquals("answer", hit)
        assertNull(miss)
    }

    @Test
    fun short_ifEquals_twoArgs_executesBranches() {
        val positive = 100.toShort().ifEquals(100.toShort(), { "equal" }, { "notEqual" })
        val negative = 100.toShort().ifEquals(200.toShort(), { "equal" }, { "notEqual" })

        assertEquals("equal", positive)
        assertEquals("notEqual", negative)
    }

    @Test
    fun int_ifEquals_singleArg_works() {
        val hit = 100.ifEquals(100) { "hit" }
        val miss = 100.ifEquals(200) { "miss" }

        assertEquals("hit", hit)
        assertNull(miss)
    }

    // ========== ifNotEquals Extended Tests ==========

    @Test
    fun int_ifNotEquals_returnsNullWhenEqual() {
        val result = 42.ifNotEquals(42) { "shouldNotRun" }

        assertNull(result)
    }

    @Test
    fun float_ifNotEquals_works() {
        val hit = 5.5f.ifNotEquals(3.2f) { "different" }
        val miss = 5.0f.ifNotEquals(5.0f) { "same" }

        assertEquals("different", hit)
        assertNull(miss)
    }

    @Test
    fun double_ifNotEquals_works() {
        val hit = 10.5.ifNotEquals(20.3) { "notEqual" }
        val miss = 15.0.ifNotEquals(15.0) { "equal" }

        assertEquals("notEqual", hit)
        assertNull(miss)
    }

    @Test
    fun long_ifNotEquals_works() {
        val hit = 1000L.ifNotEquals(2000L) { "different" }
        val miss = 500L.ifNotEquals(500L) { "same" }

        assertEquals("different", hit)
        assertNull(miss)
    }

    // ========== Boolean Extended Tests ==========

    @Test
    fun boolean_ifTrue_singleArg_executesWhenTrue() {
        val hit = true.ifTrue { "executed" }
        val miss = false.ifTrue { "skipped" }

        assertEquals("executed", hit)
        assertNull(miss)
    }

    @Test
    fun boolean_ifFalse_returnsNullWhenTrue() {
        val result = true.ifFalse { "shouldNotRun" }

        assertNull(result)
    }

    @Test
    fun boolean_ifTrue_twoArgs_executesNegativeWhenFalse() {
        val negative = false.ifTrue(
            positiveWork = { "positive" },
            negativeWork = { "negative" }
        )

        assertEquals("negative", negative)
    }

    // ========== Edge Cases ==========

    @Test
    fun zeroComparison_worksCorrectly() {
        val greaterThanZero = 1.ifGreaterThan(0) { "positive" }
        val equalToZero = 0.ifEquals(0) { "zero" }
        val notZero = 5.ifNotEquals(0) { "nonZero" }

        assertEquals("positive", greaterThanZero)
        assertEquals("zero", equalToZero)
        assertEquals("nonZero", notZero)
    }

    @Test
    fun negativeNumbers_workCorrectly() {
        val result1 = (-5).ifGreaterThan(-10) { "greater" }
        val result2 = (-5).ifGreaterThanOrEqual(-5) { "equal" }
        val result3 = (-10).ifGreaterThan(-5) { "shouldNotRun" }

        assertEquals("greater", result1)
        assertEquals("equal", result2)
        assertNull(result3) // -10 is not greater than -5
    }

    @Test
    fun firstNotNull_stopsAtFirstMatch() {
        val result = firstNotNull(null, null, "first", "second")

        assertEquals("first", result)
    }

    @Test
    fun firstNotNull_handlesEmptyArray() {
        val result = firstNotNull<String>()

        assertNull(result)
    }

    @Test
    fun int_ifGreaterThan_runtimeToggle_coversBothBranches() {
        var conditionFlag = false
        val threshold = 5
        val hitLog = mutableListOf<String>()

        fun subject(): Int = if (conditionFlag) 8 else 3

        // 1차 호출: false 경로
        conditionFlag = false
        val first = subject().ifGreaterThan(threshold) {
            hitLog += "hit-true"
            "hit-true"
        }
        assertNull(first)

        // 2차 호출: 같은 호출 지점에서 true 경로
        conditionFlag = true
        val second = subject().ifGreaterThan(threshold) {
            hitLog += "hit-false"
            "hit-false"
        }
        assertEquals("hit-false", second)
        assertEquals(listOf("hit-false"), hitLog)
    }

    // ========== Branch Coverage Enhancements ==========

    @Test
    fun branch_ifGreaterThan_singleLambda_allTypes() {
        assertSingleConditionalBranches(
            falseInput = 2,
            trueInput = 7,
            expectedTrueResult = "int-gt-single"
        ) { value, action -> value.ifGreaterThan(5) { action() } }

        assertSingleConditionalBranches(
            falseInput = 2.5f,
            trueInput = 7.5f,
            expectedTrueResult = "float-gt-single"
        ) { value, action -> value.ifGreaterThan(5.0f) { action() } }

        assertSingleConditionalBranches(
            falseInput = 1.0,
            trueInput = 6.0,
            expectedTrueResult = "double-gt-single"
        ) { value, action -> value.ifGreaterThan(3.0) { action() } }

        assertSingleConditionalBranches(
            falseInput = 2L,
            trueInput = 9L,
            expectedTrueResult = "long-gt-single"
        ) { value, action -> value.ifGreaterThan(5L) { action() } }

        assertSingleConditionalBranches(
            falseInput = 1.toShort(),
            trueInput = 6.toShort(),
            expectedTrueResult = "short-gt-single"
        ) { value, action -> value.ifGreaterThan(4.toShort()) { action() } }
    }

    @Test
    fun branch_ifGreaterThan_dualLambda_allTypes() {
        assertDualConditionalBranches(
            falseInput = 2,
            trueInput = 8,
            expectedNegative = "int-gt-neg",
            expectedPositive = "int-gt-pos"
        ) { value, pos, neg -> value.ifGreaterThan(5, { pos() }, { neg() }) }

        assertDualConditionalBranches(
            falseInput = 2.5f,
            trueInput = 8.5f,
            expectedNegative = "float-gt-neg",
            expectedPositive = "float-gt-pos"
        ) { value, pos, neg -> value.ifGreaterThan(5.0f, { pos() }, { neg() }) }

        assertDualConditionalBranches(
            falseInput = 1.0,
            trueInput = 7.0,
            expectedNegative = "double-gt-neg",
            expectedPositive = "double-gt-pos"
        ) { value, pos, neg -> value.ifGreaterThan(3.5, { pos() }, { neg() }) }

        assertDualConditionalBranches(
            falseInput = 3L,
            trueInput = 11L,
            expectedNegative = "long-gt-neg",
            expectedPositive = "long-gt-pos"
        ) { value, pos, neg -> value.ifGreaterThan(6L, { pos() }, { neg() }) }

        assertDualConditionalBranches(
            falseInput = 3.toShort(),
            trueInput = 9.toShort(),
            expectedNegative = "short-gt-neg",
            expectedPositive = "short-gt-pos"
        ) { value, pos, neg -> value.ifGreaterThan(5.toShort(), { pos() }, { neg() }) }
    }

    @Test
    fun branch_ifGreaterThanOrEqual_singleLambda_allTypes() {
        assertSingleConditionalBranches(
            falseInput = 3,
            trueInput = 5,
            expectedTrueResult = "int-gte-single"
        ) { value, action -> value.ifGreaterThanOrEqual(5) { action() } }

        assertSingleConditionalBranches(
            falseInput = 3.5f,
            trueInput = 5.0f,
            expectedTrueResult = "float-gte-single"
        ) { value, action -> value.ifGreaterThanOrEqual(5.0f) { action() } }

        assertSingleConditionalBranches(
            falseInput = 2.0,
            trueInput = 2.5,
            expectedTrueResult = "double-gte-single"
        ) { value, action -> value.ifGreaterThanOrEqual(2.5) { action() } }

        assertSingleConditionalBranches(
            falseInput = 4L,
            trueInput = 7L,
            expectedTrueResult = "long-gte-single"
        ) { value, action -> value.ifGreaterThanOrEqual(7L) { action() } }

        assertSingleConditionalBranches(
            falseInput = 10.toShort(),
            trueInput = 12.toShort(),
            expectedTrueResult = "short-gte-single"
        ) { value, action -> value.ifGreaterThanOrEqual(12.toShort()) { action() } }
    }

    @Test
    fun branch_ifGreaterThanOrEqual_dualLambda_allTypes() {
        assertDualConditionalBranches(
            falseInput = 4,
            trueInput = 6,
            expectedNegative = "int-gte-neg",
            expectedPositive = "int-gte-pos"
        ) { value, pos, neg -> value.ifGreaterThanOrEqual(6, { pos() }, { neg() }) }

        assertDualConditionalBranches(
            falseInput = 4.5f,
            trueInput = 7.0f,
            expectedNegative = "float-gte-neg",
            expectedPositive = "float-gte-pos"
        ) { value, pos, neg -> value.ifGreaterThanOrEqual(7.0f, { pos() }, { neg() }) }

        assertDualConditionalBranches(
            falseInput = 1.0,
            trueInput = 1.5,
            expectedNegative = "double-gte-neg",
            expectedPositive = "double-gte-pos"
        ) { value, pos, neg -> value.ifGreaterThanOrEqual(1.5, { pos() }, { neg() }) }

        assertDualConditionalBranches(
            falseInput = 8L,
            trueInput = 10L,
            expectedNegative = "long-gte-neg",
            expectedPositive = "long-gte-pos"
        ) { value, pos, neg -> value.ifGreaterThanOrEqual(10L, { pos() }, { neg() }) }

        assertDualConditionalBranches(
            falseInput = 5.toShort(),
            trueInput = 7.toShort(),
            expectedNegative = "short-gte-neg",
            expectedPositive = "short-gte-pos"
        ) { value, pos, neg -> value.ifGreaterThanOrEqual(7.toShort(), { pos() }, { neg() }) }
    }

    @Test
    fun branch_ifEquals_singleLambda_allTypes() {
        assertSingleConditionalBranches(
            falseInput = 4,
            trueInput = 6,
            expectedTrueResult = "int-eq-single"
        ) { value, action -> value.ifEquals(6) { action() } }

        assertSingleConditionalBranches(
            falseInput = 4.1f,
            trueInput = 3.3f,
            expectedTrueResult = "float-eq-single"
        ) { value, action -> value.ifEquals(3.3f) { action() } }

        assertSingleConditionalBranches(
            falseInput = 2.7,
            trueInput = 2.2,
            expectedTrueResult = "double-eq-single"
        ) { value, action -> value.ifEquals(2.2) { action() } }

        assertSingleConditionalBranches(
            falseInput = 9L,
            trueInput = 11L,
            expectedTrueResult = "long-eq-single"
        ) { value, action -> value.ifEquals(11L) { action() } }

        assertSingleConditionalBranches(
            falseInput = 3.toShort(),
            trueInput = 5.toShort(),
            expectedTrueResult = "short-eq-single"
        ) { value, action -> value.ifEquals(5.toShort()) { action() } }
    }

    @Test
    fun branch_ifEquals_dualLambda_allTypes() {
        assertDualConditionalBranches(
            falseInput = 8,
            trueInput = 10,
            expectedNegative = "int-eq-neg",
            expectedPositive = "int-eq-pos"
        ) { value, pos, neg -> value.ifEquals(10, { pos() }, { neg() }) }

        assertDualConditionalBranches(
            falseInput = 6.1f,
            trueInput = 6.0f,
            expectedNegative = "float-eq-neg",
            expectedPositive = "float-eq-pos"
        ) { value, pos, neg -> value.ifEquals(6.0f, { pos() }, { neg() }) }

        assertDualConditionalBranches(
            falseInput = 2.2,
            trueInput = 1.1,
            expectedNegative = "double-eq-neg",
            expectedPositive = "double-eq-pos"
        ) { value, pos, neg -> value.ifEquals(1.1, { pos() }, { neg() }) }

        assertDualConditionalBranches(
            falseInput = 15L,
            trueInput = 12L,
            expectedNegative = "long-eq-neg",
            expectedPositive = "long-eq-pos"
        ) { value, pos, neg -> value.ifEquals(12L, { pos() }, { neg() }) }

        assertDualConditionalBranches(
            falseInput = 20.toShort(),
            trueInput = 18.toShort(),
            expectedNegative = "short-eq-neg",
            expectedPositive = "short-eq-pos"
        ) { value, pos, neg -> value.ifEquals(18.toShort(), { pos() }, { neg() }) }
    }

    @Test
    fun branch_ifNotEquals_singleLambda_allTypes() {
        assertSingleConditionalBranches(
            falseInput = 9,
            trueInput = 11,
            expectedTrueResult = "int-neq-single"
        ) { value, action -> value.ifNotEquals(9) { action() } }

        assertSingleConditionalBranches(
            falseInput = 4.4f,
            trueInput = 5.5f,
            expectedTrueResult = "float-neq-single"
        ) { value, action -> value.ifNotEquals(4.4f) { action() } }

        assertSingleConditionalBranches(
            falseInput = 3.3,
            trueInput = 1.1,
            expectedTrueResult = "double-neq-single"
        ) { value, action -> value.ifNotEquals(3.3) { action() } }

        assertSingleConditionalBranches(
            falseInput = 7L,
            trueInput = 9L,
            expectedTrueResult = "long-neq-single"
        ) { value, action -> value.ifNotEquals(7L) { action() } }

        assertSingleConditionalBranches(
            falseInput = 10.toShort(),
            trueInput = 15.toShort(),
            expectedTrueResult = "short-neq-single"
        ) { value, action -> value.ifNotEquals(10.toShort()) { action() } }
    }

    @Test
    fun branch_boolean_conditionals_coverBothBranches() {
        assertSingleConditionalBranches(
            falseInput = false,
            trueInput = true,
            expectedTrueResult = "bool-ifTrue-single"
        ) { value, action -> value.ifTrue { action() } }

        assertDualConditionalBranches(
            falseInput = false,
            trueInput = true,
            expectedNegative = "bool-ifTrue-neg",
            expectedPositive = "bool-ifTrue-pos"
        ) { value, pos, neg -> value.ifTrue({ pos() }, { neg() }) }

        assertSingleConditionalBranches(
            falseInput = true,
            trueInput = false,
            expectedTrueResult = "bool-ifFalse-single"
        ) { value, action -> value.ifFalse { action() } }
    }

    // ========== Helpers ==========

    private fun <I, R> assertSingleConditionalBranches(
        falseInput: I,
        trueInput: I,
        expectedTrueResult: R,
        invocation: (I, () -> R) -> R?
    ) {
        var lambdaCalled = false

        val falseResult = invocation(falseInput) {
            lambdaCalled = true
            expectedTrueResult
        }
        assertNull(falseResult)
        assertFalse(lambdaCalled)

        lambdaCalled = false
        val trueResult = invocation(trueInput) {
            lambdaCalled = true
            expectedTrueResult
        }
        assertEquals(expectedTrueResult, trueResult)
        assertTrue(lambdaCalled)
    }

    private fun <I, R> assertDualConditionalBranches(
        falseInput: I,
        trueInput: I,
        expectedNegative: R,
        expectedPositive: R,
        invocation: (I, () -> R, () -> R) -> R?
    ) {
        var positiveCalls = 0
        var negativeCalls = 0

        val negativeResult = invocation(
            falseInput,
            {
                positiveCalls++
                expectedPositive
            },
            {
                negativeCalls++
                expectedNegative
            }
        )
        assertEquals(expectedNegative, negativeResult)
        assertEquals(0, positiveCalls)
        assertEquals(1, negativeCalls)

        val positiveResult = invocation(
            trueInput,
            {
                positiveCalls++
                expectedPositive
            },
            {
                negativeCalls++
                expectedNegative
            }
        )
        assertEquals(expectedPositive, positiveResult)
        assertEquals(1, positiveCalls)
        assertEquals(1, negativeCalls)
    }
}
