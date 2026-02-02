package kr.open.library.simple_ui.xml.ui.temp.base.operation

import kr.open.library.simple_ui.xml.ui.temp.base.internal.AdapterOperationQueueCoordinator.OperationResult

/**
 * Converts ListOperationResult to OperationResult for ListAdapter (Unit meta).<br><br>
 * ListOperationResult를 ListAdapter용 OperationResult로 변환합니다 (Unit 메타).<br>
 */
internal fun <ITEM : Any> ListOperationResult<ITEM>.toOperationResult(): OperationResult<ITEM, Unit> = OperationResult(
    items = items,
    success = success,
    meta = Unit,
    failure = failure,
)
