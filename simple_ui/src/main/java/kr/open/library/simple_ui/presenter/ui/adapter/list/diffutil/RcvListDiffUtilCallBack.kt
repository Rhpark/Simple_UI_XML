package kr.open.library.simple_ui.presenter.ui.adapter.list.diffutil

import androidx.recyclerview.widget.DiffUtil

/**
 * DiffUtil.ItemCallback implementation for RecyclerView ListAdapter.<br>
 * Provides lambda-based item comparison for efficient list updates.<br><br>
 * RecyclerView ListAdapter를 위한 DiffUtil.ItemCallback 구현체입니다.<br>
 * 효율적인 리스트 업데이트를 위한 람다 기반 아이템 비교를 제공합니다.<br>
 *
 * Features:<br>
 * - Lambda-based flexible item comparison<br>
 * - Support for identity and content comparison<br>
 * - Optional payload generation for partial updates<br>
 * - Generic type support for any item type<br><br>
 * 기능:<br>
 * - 람다 기반 유연한 아이템 비교<br>
 * - 동일성 및 내용 비교 지원<br>
 * - 부분 업데이트를 위한 선택적 payload 생성<br>
 * - 모든 아이템 타입에 대한 제네릭 타입 지원<br>
 *
 * Usage example:<br>
 * ```kotlin
 * val diffUtil = RcvListDiffUtilCallBack<User>(
 *     itemsTheSame = { old, new -> old.id == new.id },
 *     contentsTheSame = { old, new -> old == new },
 *     changePayload = { old, new ->
 *         if (old.name != new.name) "name" else null
 *     }
 * )
 * ```
 * <br>
 *
 * @param ITEM The type of items to compare.<br><br>
 *             비교할 아이템의 타입.<br>
 *
 * @param itemsTheSame Lambda to check if two items represent the same entity (e.g., ID comparison).<br><br>
 *                     두 아이템이 같은 항목인지 확인하는 람다 (예: ID 비교).<br>
 *
 * @param contentsTheSame Lambda to check if two items have the same content.<br><br>
 *                        두 아이템의 내용이 같은지 확인하는 람다.<br>
 *
 * @param changePayload Lambda to generate payload for partial updates when items are the same but contents differ (nullable).<br><br>
 *                      아이템이 같지만 내용이 다를 때 부분 업데이트용 payload를 생성하는 람다 (null 가능).<br>
 *
 * @see BaseRcvListAdapter For the ListAdapter that uses this DiffUtil callback.<br><br>
 *      이 DiffUtil 콜백을 사용하는 ListAdapter는 BaseRcvListAdapter를 참조하세요.<br>
 */
public class RcvListDiffUtilCallBack<ITEM>(
    private val itemsTheSame: ((oldItem: ITEM, newItem: ITEM) -> Boolean),
    private val contentsTheSame: ((oldItem: ITEM, newItem: ITEM) -> Boolean),
    private val changePayload: ((oldItem: ITEM, newItem: ITEM) -> Any?)? = null
) : DiffUtil.ItemCallback<ITEM>() {

    /**
     * Checks if two items represent the same entity.<br>
     * Used to determine if an item was moved or changed.<br><br>
     * 두 아이템이 같은 항목을 나타내는지 확인합니다.<br>
     * 아이템이 이동되었는지 또는 변경되었는지 판단하는 데 사용됩니다.<br>
     *
     * @param oldItem The item in the old list.<br><br>
     *                이전 리스트의 아이템.<br>
     *
     * @param newItem The item in the new list.<br><br>
     *                새 리스트의 아이템.<br>
     *
     * @return True if the items represent the same entity, false otherwise.<br><br>
     *         같은 항목이면 true, 아니면 false.<br>
     */
    public override fun areItemsTheSame(oldItem: ITEM & Any, newItem: ITEM & Any): Boolean = itemsTheSame(oldItem, newItem)

    /**
     * Checks if two items have the same content.<br>
     * Called only when areItemsTheSame returns true.<br><br>
     * 두 아이템의 내용이 같은지 확인합니다.<br>
     * areItemsTheSame이 true를 반환할 때만 호출됩니다.<br>
     *
     * @param oldItem The item in the old list.<br><br>
     *                이전 리스트의 아이템.<br>
     *
     * @param newItem The item in the new list.<br><br>
     *                새 리스트의 아이템.<br>
     *
     * @return True if the items have the same content, false otherwise.<br><br>
     *         내용이 같으면 true, 아니면 false.<br>
     */
    public override fun areContentsTheSame(oldItem: ITEM & Any, newItem: ITEM & Any): Boolean = contentsTheSame(oldItem, newItem)

    /**
     * Returns payload for partial update when items are the same but contents differ.<br>
     * Called only when areItemsTheSame returns true and areContentsTheSame returns false.<br><br>
     * 아이템이 같지만 내용이 다를 때 부분 업데이트용 payload를 반환합니다.<br>
     * areItemsTheSame이 true를 반환하고 areContentsTheSame이 false를 반환할 때만 호출됩니다.<br>
     *
     * @param oldItem The item in the old list.<br><br>
     *                이전 리스트의 아이템.<br>
     *
     * @param newItem The item in the new list.<br><br>
     *                새 리스트의 아이템.<br>
     *
     * @return Payload object for partial update, null for full update.<br><br>
     *         부분 업데이트용 payload 객체, 전체 업데이트면 null.<br>
     */
    public override fun getChangePayload(oldItem: ITEM & Any, newItem: ITEM & Any): Any? =
        changePayload?.invoke(oldItem, newItem)
}
