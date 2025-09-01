package kr.open.library.simple_ui.ui.adapter.normal.simple

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import kr.open.library.simple_ui.ui.adapter.normal.base.BaseRcvAdapter
import kr.open.library.simple_ui.ui.adapter.viewholder.BaseRcvViewHolder

/**
 * A simple implementation of RecyclerView adapter without databinding support
 *
 * @param ITEM Type of data items in the list
 * @property layoutRes Layout resource ID for item views
 * @property onBind Function to bind data to the ViewHolder
 */
public open class SimpleRcvAdapter<ITEM : Any>(
    @LayoutRes private val layoutRes: Int,
    private val onBind: (BaseRcvViewHolder, ITEM, position: Int) -> Unit
) : BaseRcvAdapter<ITEM, BaseRcvViewHolder>() {

    private var diffUtilItemSame: ((oldItem: ITEM, newItem: ITEM) -> Boolean)? = null
    private var diffUtilContentsSame: ((oldItem: ITEM, newItem: ITEM) -> Boolean)? = null
    private var diffUtilChangePayload: ((oldItem: ITEM, newItem: ITEM) -> Any?)? = null

    /**
     * DiffUtil에서 아이템이 같은지 비교하는 로직을 설정합니다.
     */
    public fun setDiffUtilItemSame(diffUtilItemSame: (oldItem: ITEM, newItem: ITEM) -> Boolean) {
        this.diffUtilItemSame = diffUtilItemSame
    }

    /**
     * DiffUtil에서 아이템 내용이 같은지 비교하는 로직을 설정합니다.
     */
    public fun setDiffUtilContentsSame(diffUtilContentsSame: (oldItem: ITEM, newItem: ITEM) -> Boolean) {
        this.diffUtilContentsSame = diffUtilContentsSame
    }

    /**
     * DiffUtil에서 아이템 변경 시 부분 업데이트용 payload를 생성하는 로직을 설정합니다.
     */
    public fun setDiffUtilChangePayload(diffUtilChangePayload: (oldItem: ITEM, newItem: ITEM) -> Any?) {
        this.diffUtilChangePayload = diffUtilChangePayload
    }

    override fun diffUtilAreItemsTheSame(oldItem: ITEM, newItem: ITEM): Boolean =
        diffUtilItemSame?.invoke(oldItem, newItem) ?: super.diffUtilAreItemsTheSame(oldItem, newItem)

    override fun diffUtilAreContentsTheSame(oldItem: ITEM, newItem: ITEM): Boolean =
        diffUtilContentsSame?.invoke(oldItem, newItem) ?: super.diffUtilAreContentsTheSame(oldItem, newItem)

    override fun diffUtilGetChangePayload(oldItem: ITEM, newItem: ITEM): Any? =
        diffUtilChangePayload?.invoke(oldItem, newItem) ?: super.diffUtilGetChangePayload(oldItem, newItem)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseRcvViewHolder =
        BaseRcvViewHolder(layoutRes, parent)

    override fun onBindViewHolder(holder: BaseRcvViewHolder, position: Int, item: ITEM) {
        onBind(holder, item, position)
    }
}
