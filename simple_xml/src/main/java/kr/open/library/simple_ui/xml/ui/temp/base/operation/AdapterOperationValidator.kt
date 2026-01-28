package kr.open.library.simple_ui.xml.ui.temp.base.operation

/**
 * Validation result for adapter operations.<br><br>
 * 어댑터 연산 검증 결과입니다.<br>
 */
internal sealed class ValidationResult {
    /**
     * Validation passed.<br><br>
     * 검증 통과입니다.<br>
     */
    data object Valid : ValidationResult()

    /**
     * Validation failed with error message.<br><br>
     * 에러 메시지와 함께 검증 실패입니다.<br>
     */
    data class Invalid(val message: String) : ValidationResult()
}

/**
 * Validator for adapter operation parameters.<br><br>
 * 어댑터 연산 파라미터 검증기입니다.<br>
 */
internal object AdapterOperationValidator {

    /**
     * Validates insert position (0..listSize allowed).<br><br>
     * 삽입 위치를 검증합니다 (0..listSize 허용).<br>
     */
    fun validateInsertPosition(position: Int, listSize: Int): ValidationResult {
        return if (position < 0 || position > listSize) {
            ValidationResult.Invalid(AdapterOperationMessages.invalidInsertPosition(position, listSize))
        } else {
            ValidationResult.Valid
        }
    }

    /**
     * Validates access position (0 until listSize allowed).<br><br>
     * 접근 위치를 검증합니다 (0 until listSize 허용).<br>
     */
    fun validateAccessPosition(position: Int, listSize: Int): ValidationResult {
        return if (position < 0 || position >= listSize) {
            ValidationResult.Invalid(AdapterOperationMessages.invalidAccessPosition(position, listSize))
        } else {
            ValidationResult.Valid
        }
    }

    /**
     * Validates remove position (0 until listSize allowed).<br><br>
     * 제거 위치를 검증합니다 (0 until listSize 허용).<br>
     */
    fun validateRemovePosition(position: Int, listSize: Int): ValidationResult {
        return if (position < 0 || position >= listSize) {
            ValidationResult.Invalid(AdapterOperationMessages.invalidRemovePosition(position, listSize))
        } else {
            ValidationResult.Valid
        }
    }

    /**
     * Validates replace position (0 until listSize allowed).<br><br>
     * 교체 위치를 검증합니다 (0 until listSize 허용).<br>
     */
    fun validateReplacePosition(position: Int, listSize: Int): ValidationResult {
        return if (position < 0 || position >= listSize) {
            ValidationResult.Invalid(AdapterOperationMessages.invalidReplacePosition(position, listSize))
        } else {
            ValidationResult.Valid
        }
    }

    /**
     * Validates move positions (both must be 0 until listSize).<br><br>
     * 이동 위치를 검증합니다 (둘 다 0 until listSize 여야 함).<br>
     */
    fun validateMovePositions(from: Int, to: Int, listSize: Int): ValidationResult {
        return if (from < 0 || from >= listSize || to < 0 || to >= listSize) {
            ValidationResult.Invalid(AdapterOperationMessages.invalidMovePosition(listSize))
        } else {
            ValidationResult.Valid
        }
    }

    /**
     * Validates item exists in list.<br><br>
     * 아이템이 리스트에 존재하는지 검증합니다.<br>
     */
    fun <ITEM> validateItemExists(index: Int): ValidationResult {
        return if (index == -1) {
            ValidationResult.Invalid(AdapterOperationMessages.itemNotFound())
        } else {
            ValidationResult.Valid
        }
    }
}
