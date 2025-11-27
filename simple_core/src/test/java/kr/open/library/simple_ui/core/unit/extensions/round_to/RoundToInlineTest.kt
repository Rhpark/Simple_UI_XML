package kr.open.library.simple_ui.core.unit.extensions.round_to

import kr.open.library.simple_ui.core.extensions.round_to.roundDown
import kr.open.library.simple_ui.core.extensions.round_to.roundTo
import kr.open.library.simple_ui.core.extensions.round_to.roundUp
import org.junit.Assert.assertEquals
import org.junit.Ignore
import org.junit.Test

class RoundToInlineTest {

    @Test
    fun double_roundVariants_behaveAsExpected() {
        val value = 3.14159

        assertEquals(3.14, value.roundTo(2), 0.0001)
        assertEquals(3.15, value.roundUp(2), 0.0001)
        assertEquals(3.14, value.roundDown(2), 0.0001)
    }

    @Test
    fun float_roundVariants_behaveAsExpected() {
        val value = 3.14159f

        assertEquals(3.14f, value.roundTo(2), 0.0001f)
        assertEquals(3.15f, value.roundUp(2), 0.0001f)
        assertEquals(3.14f, value.roundDown(2), 0.0001f)
    }

    @Test
    fun integer_roundVariants_behaveAsExpected() {
        val value = 1234

        assertEquals(1200, value.roundTo(2))
        assertEquals(1300, value.roundUp(2))
        assertEquals(1200, value.roundDown(2))
    }

    @Test
    fun long_roundVariants_behaveAsExpected() {
        val value = 1234L

        assertEquals(1200L, value.roundTo(2))
        assertEquals(1300L, value.roundUp(2))
        assertEquals(1200L, value.roundDown(2))
    }

    @Test
    fun short_roundVariants_behaveAsExpected() {
        val value: Short = 1234.toShort()

        assertEquals(1200.toShort(), value.roundTo(2))
        assertEquals(1300.toShort(), value.roundUp(2))
        assertEquals(1200.toShort(), value.roundDown(2))
    }

    // ========== Negative Number Tests ==========

    @Test
    fun double_negativeNumbers_roundCorrectly() {
        val value = -3.14159

        assertEquals(-3.14, value.roundTo(2), 0.0001)
        assertEquals(-3.14, value.roundUp(2), 0.0001)  // ceil moves toward 0
        assertEquals(-3.15, value.roundDown(2), 0.0001) // floor moves away from 0
    }

    @Test
    fun float_negativeNumbers_roundCorrectly() {
        val value = -3.14159f

        assertEquals(-3.14f, value.roundTo(2), 0.0001f)
        assertEquals(-3.14f, value.roundUp(2), 0.0001f)
        assertEquals(-3.15f, value.roundDown(2), 0.0001f)
    }

    @Test
    fun integer_negativeNumbers_roundCorrectly() {
        val value = -1234

        assertEquals(-1200, value.roundTo(2))
        assertEquals(-1200, value.roundUp(2))
        assertEquals(-1300, value.roundDown(2))
    }

    @Test
    fun long_negativeNumbers_roundCorrectly() {
        val value = -1234L

        assertEquals(-1200L, value.roundTo(2))
        assertEquals(-1200L, value.roundUp(2))
        assertEquals(-1300L, value.roundDown(2))
    }

    // ========== Zero Tests ==========

    @Test
    fun double_zero_remainsZero() {
        val value = 0.0

        assertEquals(0.0, value.roundTo(2), 0.0001)
        assertEquals(0.0, value.roundUp(2), 0.0001)
        assertEquals(0.0, value.roundDown(2), 0.0001)
    }

    @Test
    fun integer_zero_remainsZero() {
        val value = 0

        assertEquals(0, value.roundTo(2))
        assertEquals(0, value.roundUp(2))
        assertEquals(0, value.roundDown(2))
    }

    // ========== Zero Decimals/Places Tests ==========

    @Test
    fun double_zeroDecimals_roundsToInteger() {
        val value = 3.14159

        assertEquals(3.0, value.roundTo(0), 0.0001)
        assertEquals(4.0, value.roundUp(0), 0.0001)
        assertEquals(3.0, value.roundDown(0), 0.0001)
    }

    @Test
    fun integer_zeroPlaces_roundsToOnes() {
        val value = 1234

        assertEquals(1234, value.roundTo(0))
        assertEquals(1234, value.roundUp(0))
        assertEquals(1234, value.roundDown(0))
    }

    // ========== Large Number Tests ==========

    @Test
    fun double_largeNumbers_handlePrecision() {
        val value = 123456.789

        assertEquals(123456.79, value.roundTo(2), 0.001)
        assertEquals(123456.79, value.roundUp(2), 0.001)
        assertEquals(123456.78, value.roundDown(2), 0.001)
    }

    @Test
    fun long_largeNumbers_roundCorrectly() {
        val value = 987654321L

        assertEquals(987654300L, value.roundTo(2))
        assertEquals(987654400L, value.roundUp(2))
        assertEquals(987654300L, value.roundDown(2))
    }

    // ========== High Precision Tests ==========

    @Test
    fun double_highPrecision_maintainsAccuracy() {
        val value = 1.23456789

        assertEquals(1.2346, value.roundTo(4), 0.00001)
        assertEquals(1.2346, value.roundUp(4), 0.00001)
        assertEquals(1.2345, value.roundDown(4), 0.00001)
    }

    @Test
    fun float_highPrecision_hasLimitations() {
        val value = 1.23456789f

        // Float has ~7 significant digits
        assertEquals(1.2346f, value.roundTo(4), 0.0001f)
    }

    // ========== Boundary Tests ==========

    @Test
    fun double_halfValue_roundsUp() {
        val value = 1.5

        assertEquals(2.0, value.roundTo(0), 0.0001)
    }

    @Test
    fun double_almostHalf_roundsDown() {
        val value = 1.49

        assertEquals(1.0, value.roundTo(0), 0.0001)
    }

    // ========== Positive Places for Integer Types ==========

    @Test
    fun integer_positivePlaces_roundsToHigherDigits() {
        val value = 12345

        // place=1 means rounding to tens place (12340 or 12350)
        assertEquals(12350, value.roundTo(1))
        assertEquals(12350, value.roundUp(1))
        assertEquals(12340, value.roundDown(1))
    }

    @Test
    fun long_positivePlaces_roundsToHigherDigits() {
        val value = 12345L

        // place=2 means rounding to hundreds place
        assertEquals(12300L, value.roundTo(2))
        assertEquals(12400L, value.roundUp(2))
        assertEquals(12300L, value.roundDown(2))
    }

    // ========== Very Small Number Tests ==========

    @Test
    fun double_verySmallNumbers_roundCorrectly() {
        val value = 0.00123

        assertEquals(0.001, value.roundTo(3), 0.0001)
        assertEquals(0.002, value.roundUp(3), 0.0001)
        assertEquals(0.001, value.roundDown(3), 0.0001)
    }

    @Test
    fun double_verySmallNegative_roundCorrectly() {
        val value = -0.00123

        assertEquals(-0.001, value.roundTo(3), 0.0001)
        assertEquals(-0.001, value.roundUp(3), 0.0001)
        assertEquals(-0.002, value.roundDown(3), 0.0001)
    }

    // ========== Consistency Tests ==========

    @Test
    fun allTypes_behaveSimilarly() {
        // Ensure consistent behavior across types
        // For Double/Float: decimals=2 means 2 decimal places
        assertEquals(1234.0, 1234.0.roundTo(2), 0.0001)
        assertEquals(1234.0f, 1234.0f.roundTo(2), 0.0001f)

        // For Int/Long/Short: place=2 means hundreds place
        assertEquals(1200, 1234.roundTo(2))
        assertEquals(1200L, 1234L.roundTo(2))
        assertEquals(1200.toShort(), 1234.toShort().roundTo(2))
    }
}
