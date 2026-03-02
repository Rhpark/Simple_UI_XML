package kr.open.library.simple_ui.xml.ui.adapter.common.imp

import androidx.annotation.MainThread

/**
 * Result-based write contract for adapters.<br><br>
 * 반영형 adapter를 위한 결과 기반 쓰기 계약입니다.<br>
 */
interface AdapterWriteApi<ITEM, RESULT> {
    /**
     * Replaces the entire item list and reports the terminal result.<br><br>
     * 전체 아이템 리스트를 교체하고 종료 결과를 전달합니다.<br>
     */
    @MainThread
    public fun setItems(items: List<ITEM>, onResult: ((RESULT) -> Unit)? = null)

    /**
     * Appends a single item and reports the terminal result.<br><br>
     * 단일 아이템을 추가하고 종료 결과를 전달합니다.<br>
     */
    @MainThread
    public fun addItem(item: ITEM, onResult: ((RESULT) -> Unit)? = null)

    /**
     * Inserts an item at position and reports the terminal result.<br><br>
     * 지정 위치에 아이템을 삽입하고 종료 결과를 전달합니다.<br>
     */
    @MainThread
    public fun addItemAt(position: Int, item: ITEM, onResult: ((RESULT) -> Unit)? = null)

    /**
     * Appends multiple items and reports the terminal result.<br><br>
     * 여러 아이템을 추가하고 종료 결과를 전달합니다.<br>
     */
    @MainThread
    public fun addItems(items: List<ITEM>, onResult: ((RESULT) -> Unit)? = null)

    /**
     * Inserts multiple items at position and reports the terminal result.<br><br>
     * 지정 위치에 여러 아이템을 삽입하고 종료 결과를 전달합니다.<br>
     */
    @MainThread
    public fun addItemsAt(position: Int, items: List<ITEM>, onResult: ((RESULT) -> Unit)? = null)

    /**
     * Removes the first matching item and reports the terminal result.<br><br>
     * 첫 번째 일치 아이템을 제거하고 종료 결과를 전달합니다.<br>
     */
    @MainThread
    public fun removeItem(item: ITEM, onResult: ((RESULT) -> Unit)? = null)

    /**
     * Removes matching items and reports the terminal result.<br><br>
     * 일치하는 아이템들을 제거하고 종료 결과를 전달합니다.<br>
     */
    @MainThread
    public fun removeItems(items: List<ITEM>, onResult: ((RESULT) -> Unit)? = null)

    /**
     * Removes a contiguous range and reports the terminal result.<br><br>
     * 연속 구간을 제거하고 종료 결과를 전달합니다.<br>
     */
    @MainThread
    public fun removeRange(start: Int, count: Int, onResult: ((RESULT) -> Unit)? = null)

    /**
     * Removes the item at position and reports the terminal result.<br><br>
     * 지정 위치 아이템을 제거하고 종료 결과를 전달합니다.<br>
     */
    @MainThread
    public fun removeAt(position: Int, onResult: ((RESULT) -> Unit)? = null)

    /**
     * Removes all items and reports the terminal result.<br><br>
     * 모든 아이템을 제거하고 종료 결과를 전달합니다.<br>
     */
    @MainThread
    public fun removeAll(onResult: ((RESULT) -> Unit)? = null)

    /**
     * Moves an item and reports the terminal result.<br><br>
     * 아이템을 이동하고 종료 결과를 전달합니다.<br>
     */
    @MainThread
    public fun moveItem(fromPosition: Int, toPosition: Int, onResult: ((RESULT) -> Unit)? = null)

    /**
     * Replaces the item at position and reports the terminal result.<br><br>
     * 지정 위치 아이템을 교체하고 종료 결과를 전달합니다.<br>
     */
    @MainThread
    public fun replaceItemAt(position: Int, item: ITEM, onResult: ((RESULT) -> Unit)? = null)
}
