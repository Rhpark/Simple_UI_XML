package kr.open.library.simple_ui.presenter.ui.adapter.list.diffutil

import androidx.recyclerview.widget.DiffUtil

/**
 * RecyclerView ListAdapter에 사용되는 DiffUtil 구현체
 *
 * @param ITEM 아이템 타입
 * @param itemsTheSame 두 아이템이 같은 항목인지 비교하는 람다 (ID 비교 등)
 * @param contentsTheSame 두 아이템의 내용이 같은지 비교하는 람다 (내용 비교)
 * @param changePayload 아이템 변경 시 부분 업데이트용 payload를 생성하는 람다 (null 가능)
 */
public class RcvListDiffUtilCallBack<ITEM>(
    private val itemsTheSame: ((oldItem: ITEM, newItem: ITEM) -> Boolean),
    private val contentsTheSame: ((oldItem: ITEM, newItem: ITEM) -> Boolean),
    private val changePayload: ((oldItem: ITEM, newItem: ITEM) -> Any?)? = null
) : DiffUtil.ItemCallback<ITEM>() {

    public override fun areItemsTheSame(oldItem: ITEM & Any, newItem: ITEM & Any): Boolean = itemsTheSame(oldItem, newItem)

    public override fun areContentsTheSame(oldItem: ITEM & Any, newItem: ITEM & Any): Boolean = contentsTheSame(oldItem, newItem)

    /**
     * Returns payload for partial update when items are the same but contents differ
     * 아이템이 같지만 내용이 다를 때 부분 업데이트용 payload를 반환
     *
     * @param oldItem Previous item
     * @param newItem New item
     * @return Payload object for partial update, null for full update
     */
    public override fun getChangePayload(oldItem: ITEM & Any, newItem: ITEM & Any): Any? =
        changePayload?.invoke(oldItem, newItem)
}
