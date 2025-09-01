package kr.open.library.simple_ui.ui.adapter.normal.simple

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import kr.open.library.simple_ui.ui.adapter.normal.base.BaseRcvAdapter
import kr.open.library.simple_ui.ui.adapter.viewholder.BaseBindingRcvViewHolder

public open class SimpleBindingRcvAdapter<ITEM : Any, BINDING : ViewDataBinding>(
    @LayoutRes private val layoutRes: Int,
    private val onBind: (BaseBindingRcvViewHolder<BINDING>, ITEM, position: Int) -> Unit
) : BaseRcvAdapter<ITEM, BaseBindingRcvViewHolder<BINDING>>() {

    private var diffUtilItemSame: ((oldItem: ITEM, newItem: ITEM) -> Boolean)? = null
    private var diffUtilContentsSame: ((oldItem: ITEM, newItem: ITEM) -> Boolean)? = null
    private var diffUtilChangePayload: ((oldItem: ITEM, newItem: ITEM) -> Any?)? = null

    public fun setDiffUtilItemSame(diffUtilItemSame: (oldItem: ITEM, newItem: ITEM) -> Boolean) {
        this.diffUtilItemSame = diffUtilItemSame
    }

    public fun setDiffUtilContentsSame(diffUtilContentsSame: (oldItem: ITEM, newItem: ITEM) -> Boolean) {
        this.diffUtilContentsSame = diffUtilContentsSame
    }

    public fun setDiffUtilChangePayload(diffUtilChangePayload: (oldItem: ITEM, newItem: ITEM) -> Any?) {
        this.diffUtilChangePayload = diffUtilChangePayload
    }

    override fun diffUtilAreItemsTheSame(oldItem: ITEM, newItem: ITEM): Boolean =
        diffUtilItemSame?.invoke(oldItem, newItem) ?: super.diffUtilAreItemsTheSame(oldItem, newItem)


    override fun diffUtilAreContentsTheSame(oldItem: ITEM, newItem: ITEM): Boolean =
        diffUtilContentsSame?.invoke(oldItem, newItem) ?: super.diffUtilAreContentsTheSame(oldItem, newItem)

    override fun diffUtilGetChangePayload(oldItem: ITEM, newItem: ITEM): Any? =
        diffUtilChangePayload?.invoke(oldItem, newItem) ?: super.diffUtilGetChangePayload(oldItem, newItem)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
            : BaseBindingRcvViewHolder<BINDING> = BaseBindingRcvViewHolder(layoutRes, parent)

    override fun onBindViewHolder(holder: BaseBindingRcvViewHolder<BINDING>, position: Int, item: ITEM) {
        onBind(holder, item, position)
    }
}