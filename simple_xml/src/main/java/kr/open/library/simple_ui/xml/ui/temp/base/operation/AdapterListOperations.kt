package kr.open.library.simple_ui.xml.ui.temp.base.operation

import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.xml.ui.temp.base.AdapterOperationFailure

/**
 * Result of a list operation.<br><br>
 * 리스트 연산 결과입니다.<br>
 *
 * @param items Resulting list after operation.<br><br>연산 후 결과 리스트입니다.<br>
 * @param success Whether the operation succeeded.<br><br>연산 성공 여부입니다.<br>
 * @param positionInfo Position metadata for adapter notification (e.g., insert/remove position).<br><br>
 *                     어댑터 알림용 위치 메타데이터입니다 (예: 삽입/제거 위치).<br>
 * @param failure Failure details if operation failed.<br><br>연산 실패 시 실패 상세입니다.<br>
 */
data class ListOperationResult<ITEM>(
    val items: List<ITEM>,
    val success: Boolean,
    val positionInfo: PositionInfo? = null,
    val failure: AdapterOperationFailure? = null,
)

/**
 * Position metadata for adapter notifications.<br><br>
 * 어댑터 알림용 위치 메타데이터입니다.<br>
 */
sealed class PositionInfo {
    /**
     * No position change needed.<br><br>
     * 위치 변경이 필요 없습니다.<br>
     */
    data object None : PositionInfo()

    /**
     * Full refresh needed.<br><br>
     * 전체 갱신이 필요합니다.<br>
     */
    data object Full : PositionInfo()

    /**
     * Insert operation metadata.<br><br>
     * 삽입 연산 메타데이터입니다.<br>
     */
    data class Insert(
        val position: Int,
        val count: Int
    ) : PositionInfo()

    /**
     * Remove operation metadata.<br><br>
     * 제거 연산 메타데이터입니다.<br>
     */
    data class Remove(
        val position: Int,
        val count: Int
    ) : PositionInfo()

    /**
     * Change operation metadata.<br><br>
     * 변경 연산 메타데이터입니다.<br>
     */
    data class Change(
        val position: Int,
        val count: Int
    ) : PositionInfo()

    /**
     * Move operation metadata.<br><br>
     * 이동 연산 메타데이터입니다.<br>
     */
    data class Move(
        val from: Int,
        val to: Int
    ) : PositionInfo()
}

/**
 * Pure functions for list operations with validation.<br><br>
 * 검증이 포함된 리스트 연산 순수 함수들입니다.<br>
 */
object AdapterListOperations {
    /**
     * Adds a single item at the end of the list.<br><br>
     * 리스트 끝에 단일 아이템을 추가합니다.<br>
     */
    fun <ITEM> addItem(
        current: List<ITEM>,
        item: ITEM,
    ): ListOperationResult<ITEM> {
        val insertPosition = current.size
        val newList = current.toMutableList().apply { add(item) }
        return ListOperationResult(
            items = newList,
            success = true,
            positionInfo = PositionInfo.Insert(insertPosition, 1),
        )
    }

    /**
     * Adds a single item at the specified position.<br><br>
     * 지정된 위치에 단일 아이템을 추가합니다.<br>
     */
    fun <ITEM> addItemAt(
        current: List<ITEM>,
        position: Int,
        item: ITEM,
    ): ListOperationResult<ITEM> {
        val validation = AdapterOperationValidator.validateInsertPosition(position, current.size)
        if (validation is ValidationResult.Invalid) {
            Logx.e(validation.message)
            return ListOperationResult(
                items = current,
                success = false,
                positionInfo = PositionInfo.None,
                failure = AdapterOperationFailure.Validation(validation.message),
            )
        }
        val newList = current.toMutableList().apply { add(position, item) }
        return ListOperationResult(
            items = newList,
            success = true,
            positionInfo = PositionInfo.Insert(position, 1),
        )
    }

    /**
     * Adds multiple items at the end of the list.<br><br>
     * 리스트 끝에 여러 아이템을 추가합니다.<br>
     */
    fun <ITEM> addItems(
        current: List<ITEM>,
        itemsToAdd: List<ITEM>,
    ): ListOperationResult<ITEM> {
        if (itemsToAdd.isEmpty()) {
            return ListOperationResult(
                items = current,
                success = true,
                positionInfo = PositionInfo.None,
            )
        }
        val insertPosition = current.size
        val newList = current.toMutableList().apply { addAll(itemsToAdd) }
        return ListOperationResult(
            items = newList,
            success = true,
            positionInfo = PositionInfo.Insert(insertPosition, itemsToAdd.size),
        )
    }

    /**
     * Adds multiple items at the specified position.<br><br>
     * 지정된 위치에 여러 아이템을 추가합니다.<br>
     */
    fun <ITEM> addItemsAt(
        current: List<ITEM>,
        position: Int,
        itemsToAdd: List<ITEM>,
    ): ListOperationResult<ITEM> {
        if (itemsToAdd.isEmpty()) {
            return ListOperationResult(
                items = current,
                success = true,
                positionInfo = PositionInfo.None,
            )
        }
        val validation = AdapterOperationValidator.validateInsertPosition(position, current.size)
        if (validation is ValidationResult.Invalid) {
            Logx.e(validation.message)
            return ListOperationResult(
                items = current,
                success = false,
                positionInfo = PositionInfo.None,
                failure = AdapterOperationFailure.Validation(validation.message),
            )
        }
        val newList = current.toMutableList().apply { addAll(position, itemsToAdd) }
        return ListOperationResult(
            items = newList,
            success = true,
            positionInfo = PositionInfo.Insert(position, itemsToAdd.size),
        )
    }

    /**
     * Removes the first matching item from the list.<br><br>
     * 리스트에서 첫 번째로 일치하는 아이템을 제거합니다.<br>
     */
    fun <ITEM> removeItem(
        current: List<ITEM>,
        item: ITEM,
    ): ListOperationResult<ITEM> {
        val targetIndex = current.indexOf(item)
        val validation = AdapterOperationValidator.validateItemExists<ITEM>(targetIndex)
        if (validation is ValidationResult.Invalid) {
            Logx.e(validation.message)
            return ListOperationResult(
                items = current,
                success = false,
                positionInfo = PositionInfo.None,
                failure = AdapterOperationFailure.Validation(validation.message),
            )
        }
        val newList = current.toMutableList().apply { removeAt(targetIndex) }
        return ListOperationResult(
            items = newList,
            success = true,
            positionInfo = PositionInfo.Remove(targetIndex, 1),
        )
    }

    /**
     * Removes an item at the specified position.<br><br>
     * 지정된 위치의 아이템을 제거합니다.<br>
     */
    fun <ITEM> removeAt(
        current: List<ITEM>,
        position: Int,
    ): ListOperationResult<ITEM> {
        val validation = AdapterOperationValidator.validateRemovePosition(position, current.size)
        if (validation is ValidationResult.Invalid) {
            Logx.e(validation.message)
            return ListOperationResult(
                items = current,
                success = false,
                positionInfo = PositionInfo.None,
                failure = AdapterOperationFailure.Validation(validation.message),
            )
        }
        val newList = current.toMutableList().apply { removeAt(position) }
        return ListOperationResult(
            items = newList,
            success = true,
            positionInfo = PositionInfo.Remove(position, 1),
        )
    }

    /**
     * Removes all items from the list.<br><br>
     * 리스트의 모든 아이템을 제거합니다.<br>
     */
    fun <ITEM> removeAll(
        current: List<ITEM>,
    ): ListOperationResult<ITEM> {
        if (current.isEmpty()) {
            return ListOperationResult(
                items = current,
                success = true,
                positionInfo = PositionInfo.None,
            )
        }
        val oldSize = current.size
        return ListOperationResult(
            items = emptyList(),
            success = true,
            positionInfo = PositionInfo.Remove(0, oldSize),
        )
    }

    /**
     * Replaces an item at the specified position.<br><br>
     * 지정된 위치의 아이템을 교체합니다.<br>
     */
    fun <ITEM> replaceItemAt(
        current: List<ITEM>,
        position: Int,
        item: ITEM,
    ): ListOperationResult<ITEM> {
        val validation = AdapterOperationValidator.validateReplacePosition(position, current.size)
        if (validation is ValidationResult.Invalid) {
            Logx.e(validation.message)
            return ListOperationResult(
                items = current,
                success = false,
                positionInfo = PositionInfo.None,
                failure = AdapterOperationFailure.Validation(validation.message),
            )
        }
        val newList = current.toMutableList().apply { set(position, item) }
        return ListOperationResult(
            items = newList,
            success = true,
            positionInfo = PositionInfo.Change(position, 1),
        )
    }

    /**
     * Moves an item from one position to another.<br><br>
     * 아이템을 한 위치에서 다른 위치로 이동합니다.<br>
     */
    fun <ITEM> moveItem(
        current: List<ITEM>,
        from: Int,
        to: Int,
    ): ListOperationResult<ITEM> {
        val validation = AdapterOperationValidator.validateMovePositions(from, to, current.size)
        if (validation is ValidationResult.Invalid) {
            Logx.e(validation.message)
            return ListOperationResult(
                items = current,
                success = false,
                positionInfo = PositionInfo.None,
                failure = AdapterOperationFailure.Validation(validation.message),
            )
        }
        if (from == to) {
            return ListOperationResult(
                items = current,
                success = true,
                positionInfo = PositionInfo.None,
            )
        }
        val newList = current.toMutableList().apply {
            val movedItem = removeAt(from)
            add(to, movedItem)
        }
        return ListOperationResult(
            items = newList,
            success = true,
            positionInfo = PositionInfo.Move(from, to),
        )
    }

    /**
     * Replaces entire list with new items.<br><br>
     * 전체 리스트를 새 아이템으로 교체합니다.<br>
     */
    fun <ITEM> setItems(
        newItems: List<ITEM>,
    ): ListOperationResult<ITEM> = ListOperationResult(
        items = newItems.toList(),
        success = true,
        positionInfo = PositionInfo.Full,
    )

    /**
     * Updates items using a transform function.<br><br>
     * 변환 함수를 사용하여 아이템을 업데이트합니다.<br>
     */
    fun <ITEM> updateItems(
        current: List<ITEM>,
        updater: (MutableList<ITEM>) -> Unit,
    ): ListOperationResult<ITEM> {
        val mutableList = current.toMutableList()
        updater(mutableList)
        return ListOperationResult(
            items = mutableList,
            success = true,
            positionInfo = PositionInfo.Full,
        )
    }
}
