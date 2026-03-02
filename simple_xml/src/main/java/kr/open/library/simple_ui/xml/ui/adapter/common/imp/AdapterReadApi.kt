package kr.open.library.simple_ui.xml.ui.adapter.common.imp

import androidx.annotation.MainThread

/**
 * Common read-only contract for adapter item access.<br><br>
 * 어댑터 아이템 조회를 위한 공통 읽기 전용 계약입니다.<br>
 */
public interface AdapterReadApi<ITEM> {
    /**
     * Returns current immutable item list.<br><br>
     * 현재 불변 아이템 리스트를 반환합니다.<br>
     */
    @MainThread
    public fun getItems(): List<ITEM>

    /**
     * Returns item at position safely, or null.<br><br>
     * 지정 위치 아이템을 안전하게 조회하며 없으면 null을 반환합니다.<br>
     */
    @MainThread
    public fun getItemOrNull(position: Int): ITEM?

    /**
     * Returns index of the target item, or -1 when not found.<br><br>
     * 대상 아이템의 인덱스를 반환하며, 없으면 -1을 반환합니다.<br>
     */
    @MainThread
    public fun getItemPosition(item: ITEM): Int

    /**
     * Returns mutable snapshot copy of current item list.<br><br>
     * 현재 아이템 리스트의 가변 스냅샷 복사본을 반환합니다.<br>
     */
    @MainThread
    public fun getMutableItemList(): MutableList<ITEM>
}
