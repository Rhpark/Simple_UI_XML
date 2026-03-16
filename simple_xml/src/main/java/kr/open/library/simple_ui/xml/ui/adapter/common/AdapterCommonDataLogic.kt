package kr.open.library.simple_ui.xml.ui.adapter.common

import androidx.recyclerview.widget.RecyclerView
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.xml.ui.adapter.common.thread.assertAdapterMainThread

/**
 * Shared validator for adapter data operations.<br><br>
 * 어댑터 데이터 연산의 공통 검증기입니다.<br>
 */
internal enum class AdapterMutationFailure {
    EMPTY_INPUT,
    INVALID_POSITION,
    ITEM_NOT_FOUND,
    NO_MATCHING_ITEMS,
}

internal class AdapterCommonDataLogic<ITEM> {
    /**
     * Validates add-at operation and executes work only when valid.<br><br>
     * 위치 기반 단건 추가 연산을 검증하고 유효할 때만 작업을 수행합니다.<br>
     */
    internal fun validateAddItemAt(position: Int, itemCount: Int): AdapterMutationFailure? {
        assertAdapterMainThread("AdapterCommonDataLogic.validateAddItemAt")
        return if (position < 0 || position > itemCount) {
            Logx.e("Cannot add item at position $position. Valid range: 0..$itemCount")
            AdapterMutationFailure.INVALID_POSITION
        } else {
            null
        }
    }

    /**
     * Validates append-many operation and executes work only when valid.<br><br>
     * 다건 append 연산을 검증하고 유효할 때만 작업을 수행합니다.<br>
     */
    internal fun validateAddItems(itemList: List<ITEM>): AdapterMutationFailure? {
        assertAdapterMainThread("AdapterCommonDataLogic.validateAddItems")
        return if (itemList.isEmpty()) {
            Logx.e("Cannot add empty list")
            AdapterMutationFailure.EMPTY_INPUT
        } else {
            null
        }
    }

    /**
     * Validates insert-many operation and executes work only when valid.<br><br>
     * 다건 삽입 연산을 검증하고 유효할 때만 작업을 수행합니다.<br>
     */
    internal fun validateAddItemsAt(itemList: List<ITEM>, position: Int, itemCount: Int): AdapterMutationFailure? {
        assertAdapterMainThread("AdapterCommonDataLogic.validateAddItemsAt")
        return if (itemList.isEmpty()) {
            Logx.e("Cannot add empty list")
            AdapterMutationFailure.EMPTY_INPUT
        } else if (position < 0 || position > itemCount) {
            Logx.e("Cannot add items at position $position. Valid range: 0..$itemCount")
            AdapterMutationFailure.INVALID_POSITION
        } else {
            null
        }
    }

    /**
     * Validates remove-by-item operation and executes work only when valid.<br><br>
     * 단건 아이템 제거 연산을 검증하고 유효할 때만 작업을 수행합니다.<br>
     */
    internal fun validateRemoveItem(item: ITEM, currentList: List<ITEM>): AdapterMutationFailure? {
        assertAdapterMainThread("AdapterCommonDataLogic.validateRemoveItem")
        return if (!currentList.contains(item)) {
            Logx.e("Item not found in the list")
            AdapterMutationFailure.ITEM_NOT_FOUND
        } else {
            null
        }
    }

    /**
     * Validates remove-many operation and executes work only when valid.<br><br>
     * 다건 아이템 제거 연산을 검증하고 유효할 때만 작업을 수행합니다.<br>
     */
    internal fun validateRemoveItems(items: List<ITEM>, currentList: List<ITEM>): AdapterMutationFailure? {
        assertAdapterMainThread("AdapterCommonDataLogic.validateRemoveItems")
        if (items.isEmpty()) {
            Logx.e("Cannot remove empty list")
            return AdapterMutationFailure.EMPTY_INPUT
        }
        val currentSet = currentList.toHashSet()
        return if (items.none { it in currentSet }) {
            Logx.e("No matching items found in the list")
            AdapterMutationFailure.NO_MATCHING_ITEMS
        } else {
            null
        }
    }

    /**
     * Validates remove-at operation and executes work only when valid.<br><br>
     * 위치 기반 단건 제거 연산을 검증하고 유효할 때만 작업을 수행합니다.<br>
     */
    internal fun validateRemoveItemAt(position: Int, itemCount: Int): AdapterMutationFailure? {
        assertAdapterMainThread("AdapterCommonDataLogic.validateRemoveItemAt")
        return if (position < 0 || position >= itemCount) {
            Logx.e("Cannot remove item at position $position. Valid range: 0..${itemCount - 1}")
            AdapterMutationFailure.INVALID_POSITION
        } else {
            null
        }
    }

    /**
     * Validates remove-range(start, count) and executes work only when valid.<br><br>
     * 구간 제거(start, count) 연산을 검증하고 유효할 때만 작업을 수행합니다.<br>
     */
    internal fun validateRemoveRange(start: Int, count: Int, itemCount: Int): AdapterMutationFailure? {
        assertAdapterMainThread("AdapterCommonDataLogic.validateRemoveRange")
        return if (count <= 0) {
            Logx.e("Cannot remove range, count must be positive. count: $count")
            AdapterMutationFailure.INVALID_POSITION
        } else if (start < 0 || start >= itemCount) {
            Logx.e("Cannot remove range from start $start. Valid range: 0..${itemCount - 1}")
            AdapterMutationFailure.INVALID_POSITION
        } else if (count > itemCount - start) {
            Logx.e("Cannot remove range start=$start, count=$count. Valid max count: ${itemCount - start}")
            AdapterMutationFailure.INVALID_POSITION
        } else {
            null
        }
    }

    /**
     * Validates move operation and executes work only when valid.<br><br>
     * 이동 연산을 검증하고 유효할 때만 작업을 수행합니다.<br>
     */
    internal fun validateMoveItem(fromPosition: Int, toPosition: Int, itemCount: Int): AdapterMutationFailure? {
        assertAdapterMainThread("AdapterCommonDataLogic.validateMoveItem")
        return if (fromPosition < 0 || fromPosition >= itemCount) {
            Logx.e("Invalid fromPosition $fromPosition. Valid range: 0 until $itemCount")
            AdapterMutationFailure.INVALID_POSITION
        } else if (toPosition < 0 || toPosition >= itemCount) {
            Logx.e("Invalid toPosition $toPosition. Valid range: 0 until $itemCount")
            AdapterMutationFailure.INVALID_POSITION
        } else {
            null
        }
    }

    /**
     * Validates replace-at operation and executes work only when valid.<br><br>
     * 위치 기반 교체 연산을 검증하고 유효할 때만 작업을 수행합니다.<br>
     */
    internal fun validateReplaceItemAt(position: Int, itemCount: Int): AdapterMutationFailure? {
        assertAdapterMainThread("AdapterCommonDataLogic.validateReplaceItemAt")
        return if (position < 0 || position >= itemCount) {
            Logx.e("Invalid position $position. Valid range: 0 until $itemCount")
            AdapterMutationFailure.INVALID_POSITION
        } else {
            null
        }
    }

    /**
     * Checks whether a position is inside valid adapter bounds.<br><br>
     * 위치가 유효한 어댑터 범위 내인지 확인합니다.<br>
     */
    internal fun isPositionValid(position: Int, itemCount: Int): Boolean =
        position > RecyclerView.NO_POSITION && position < itemCount
}
