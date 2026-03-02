package kr.open.library.simple_ui.xml.ui.adapter.list.base.result

import kr.open.library.simple_ui.xml.ui.adapter.common.AdapterMutationFailure
import kr.open.library.simple_ui.xml.ui.adapter.list.base.queue.QueueDropReason

/**
 * Result contract for queued ListAdapter mutations.<br><br>
 * 큐 기반 ListAdapter 변경 결과 계약입니다.<br>
 */
public sealed interface ListAdapterResult {
    /**
     * Folds this result into applied, rejected, or failed branches.<br><br>
     * 현재 결과를 적용 성공, 거절, 실패 분기로 처리합니다.<br>
     */
    public fun fold(onApplied: () -> Unit, onRejected: (Rejected) -> Unit, onFailed: (Failed) -> Unit) {
        when (this) {
            is Applied -> onApplied()
            is Rejected -> onRejected(this)
            is Failed -> onFailed(this)
        }
    }

    /**
     * Indicates that the mutation was applied successfully.<br><br>
     * 변경이 성공적으로 반영되었음을 나타냅니다.<br>
     */
    public data object Applied : ListAdapterResult

    /**
     * Indicates that the mutation was rejected before queueing.<br><br>
     * 큐 등록 전에 변경 요청이 거절되었음을 나타냅니다.<br>
     */
    public sealed interface Rejected : ListAdapterResult {
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

    /**
     * Indicates that the queued mutation failed after acceptance.<br><br>
     * 큐에 수락된 뒤 변경이 실패했음을 나타냅니다.<br>
     */
    public sealed interface Failed : ListAdapterResult {
        /**
         * Indicates that the operation was dropped by queue policy or queue clearing.<br><br>
         * 큐 정책 또는 큐 정리로 연산이 드롭되었음을 나타냅니다.<br>
         */
        public data class Dropped(
            val reason: AdapterDropReason,
        ) : Failed

        /**
         * Indicates that the operation failed during execution.<br><br>
         * 연산 실행 중 오류로 실패했음을 나타냅니다.<br>
         */
        public data class ExecutionError(
            val cause: Throwable?,
        ) : Failed
    }
}

internal fun AdapterMutationFailure.toListAdapterResult(): ListAdapterResult.Rejected =
    when (this) {
        AdapterMutationFailure.EMPTY_INPUT -> ListAdapterResult.Rejected.EmptyInput
        AdapterMutationFailure.INVALID_POSITION -> ListAdapterResult.Rejected.InvalidPosition
        AdapterMutationFailure.ITEM_NOT_FOUND -> ListAdapterResult.Rejected.ItemNotFound
        AdapterMutationFailure.NO_MATCHING_ITEMS -> ListAdapterResult.Rejected.NoMatchingItems
    }

internal fun QueueDropReason.toAdapterDropReason(): AdapterDropReason =
    when (this) {
        QueueDropReason.QUEUE_FULL_DROP_NEW -> AdapterDropReason.DROP_NEW
        QueueDropReason.QUEUE_FULL_DROP_OLDEST -> AdapterDropReason.DROP_OLDEST
        QueueDropReason.QUEUE_FULL_CLEAR -> AdapterDropReason.CLEAR_AND_ENQUEUE
        QueueDropReason.CLEARED_EXPLICIT -> AdapterDropReason.CLEARED_EXPLICIT
        QueueDropReason.CLEARED_BY_API -> AdapterDropReason.CLEARED_BY_API
    }
