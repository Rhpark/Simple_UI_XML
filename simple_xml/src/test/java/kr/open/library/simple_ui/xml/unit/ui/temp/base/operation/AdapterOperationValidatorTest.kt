package kr.open.library.simple_ui.xml.unit.ui.temp.base.operation

import kr.open.library.simple_ui.xml.ui.temp.base.operation.AdapterOperationValidator
import kr.open.library.simple_ui.xml.ui.temp.base.operation.ValidationResult
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for AdapterOperationValidator.<br><br>
 * AdapterOperationValidator 단위 테스트입니다.<br>
 */
class AdapterOperationValidatorTest {
    // ===== validateInsertPosition tests =====

    @Test
    fun `validateInsertPosition - valid at position 0`() {
        val result = AdapterOperationValidator.validateInsertPosition(0, 5)
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `validateInsertPosition - valid at end position`() {
        val result = AdapterOperationValidator.validateInsertPosition(5, 5)
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `validateInsertPosition - valid in middle`() {
        val result = AdapterOperationValidator.validateInsertPosition(3, 5)
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `validateInsertPosition - invalid negative position`() {
        val result = AdapterOperationValidator.validateInsertPosition(-1, 5)
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun `validateInsertPosition - invalid position beyond size`() {
        val result = AdapterOperationValidator.validateInsertPosition(6, 5)
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun `validateInsertPosition - valid at 0 for empty list`() {
        val result = AdapterOperationValidator.validateInsertPosition(0, 0)
        assertTrue(result is ValidationResult.Valid)
    }

    // ===== validateAccessPosition tests =====

    @Test
    fun `validateAccessPosition - valid at position 0`() {
        val result = AdapterOperationValidator.validateAccessPosition(0, 5)
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `validateAccessPosition - valid at last position`() {
        val result = AdapterOperationValidator.validateAccessPosition(4, 5)
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `validateAccessPosition - invalid at size position`() {
        val result = AdapterOperationValidator.validateAccessPosition(5, 5)
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun `validateAccessPosition - invalid negative position`() {
        val result = AdapterOperationValidator.validateAccessPosition(-1, 5)
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun `validateAccessPosition - invalid for empty list`() {
        val result = AdapterOperationValidator.validateAccessPosition(0, 0)
        assertTrue(result is ValidationResult.Invalid)
    }

    // ===== validateRemovePosition tests =====

    @Test
    fun `validateRemovePosition - valid at position 0`() {
        val result = AdapterOperationValidator.validateRemovePosition(0, 5)
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `validateRemovePosition - valid at last position`() {
        val result = AdapterOperationValidator.validateRemovePosition(4, 5)
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `validateRemovePosition - invalid at size position`() {
        val result = AdapterOperationValidator.validateRemovePosition(5, 5)
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun `validateRemovePosition - invalid negative position`() {
        val result = AdapterOperationValidator.validateRemovePosition(-1, 5)
        assertTrue(result is ValidationResult.Invalid)
    }

    // ===== validateReplacePosition tests =====

    @Test
    fun `validateReplacePosition - valid at position 0`() {
        val result = AdapterOperationValidator.validateReplacePosition(0, 5)
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `validateReplacePosition - valid at last position`() {
        val result = AdapterOperationValidator.validateReplacePosition(4, 5)
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `validateReplacePosition - invalid at size position`() {
        val result = AdapterOperationValidator.validateReplacePosition(5, 5)
        assertTrue(result is ValidationResult.Invalid)
    }

    // ===== validateMovePositions tests =====

    @Test
    fun `validateMovePositions - valid positions`() {
        val result = AdapterOperationValidator.validateMovePositions(0, 4, 5)
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `validateMovePositions - valid same positions`() {
        val result = AdapterOperationValidator.validateMovePositions(2, 2, 5)
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `validateMovePositions - invalid from position`() {
        val result = AdapterOperationValidator.validateMovePositions(-1, 2, 5)
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun `validateMovePositions - invalid to position`() {
        val result = AdapterOperationValidator.validateMovePositions(0, 5, 5)
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun `validateMovePositions - both positions invalid`() {
        val result = AdapterOperationValidator.validateMovePositions(-1, 10, 5)
        assertTrue(result is ValidationResult.Invalid)
    }

    // ===== validateItemExists tests =====

    @Test
    fun `validateItemExists - valid when index is not -1`() {
        val result = AdapterOperationValidator.validateItemExists<String>(0)
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `validateItemExists - invalid when index is -1`() {
        val result = AdapterOperationValidator.validateItemExists<String>(-1)
        assertTrue(result is ValidationResult.Invalid)
    }
}
