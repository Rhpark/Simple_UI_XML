package kr.open.library.simple_ui.xml.ui.adapter.normal.result

import kr.open.library.simple_ui.xml.ui.adapter.common.AdapterMutationFailure

/**
 * Result contract for immediate normal adapter mutations.<br><br>
 * 즉시 반영형 normal adapter 변경 결과 계약입니다.<br>
 */
public sealed interface NormalAdapterResult {
    /**
     * Folds this result into applied or rejected branches.<br><br>
     * 현재 결과를 적용 성공 또는 거절 분기로 처리합니다.<br>
     */
    public fun fold(onApplied: () -> Unit, onRejected: (Rejected) -> Unit) {
        when (this) {
            is Applied -> onApplied()
            is Rejected -> onRejected(this)
        }
    }

    /**
     * Indicates that the mutation was applied successfully.<br><br>
     * 변경이 성공적으로 반영되었음을 나타냅니다.<br>
     */
    public data object Applied : NormalAdapterResult

    /**
     * Indicates that the mutation was rejected before execution.<br><br>
     * 실행 전에 변경 요청이 거절되었음을 나타냅니다.<br>
     */
    public sealed interface Rejected : NormalAdapterResult {
        /**
         * Indicates that the provided input list was empty.<br><br>
         * 전달된 입력 리스트가 비어 있어 거절되었음을 나타냅니다.<br>
         */
        public data object EmptyInput : Rejected

        /**
         * Indicates that the requested position or range was invalid.<br><br>
         * 요청한 위치 또는 범위가 유효하지 않아 거절되었음을 나타냅니다.<br>
         */
        public data object InvalidPosition : Rejected

        /**
         * Indicates that the target item was not found.<br><br>
         * 대상 아이템을 찾지 못해 거절되었음을 나타냅니다.<br>
         */
        public data object ItemNotFound : Rejected

        /**
         * Indicates that no requested items matched the current list.<br><br>
         * 요청한 아이템 중 현재 리스트와 일치하는 항목이 없어 거절되었음을 나타냅니다.<br>
         */
        public data object NoMatchingItems : Rejected
    }
}

internal fun AdapterMutationFailure.toNormalAdapterResult(): NormalAdapterResult.Rejected =
    when (this) {
        AdapterMutationFailure.EMPTY_INPUT -> NormalAdapterResult.Rejected.EmptyInput
        AdapterMutationFailure.INVALID_POSITION -> NormalAdapterResult.Rejected.InvalidPosition
        AdapterMutationFailure.ITEM_NOT_FOUND -> NormalAdapterResult.Rejected.ItemNotFound
        AdapterMutationFailure.NO_MATCHING_ITEMS -> NormalAdapterResult.Rejected.NoMatchingItems
    }
