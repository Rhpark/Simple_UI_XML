package kr.open.library.simple_ui.ui.adapter.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

public open class BaseBindingRcvViewHolder<BINDING : ViewDataBinding>(
    @LayoutRes xmlRes: Int,
    parent: ViewGroup,
    attachToRoot: Boolean = false
) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(xmlRes, parent, attachToRoot)
) {
    public val binding: BINDING by lazy {
        DataBindingUtil.bind<BINDING>(itemView) ?: throw IllegalStateException("Exception Binding is null!!")
    }

    /**
     * Verification of the existence of an item
     * for listener(ex OnItemClickListener...)
     */
    protected fun isValidPosition(): Boolean = (adapterPosition > RecyclerView.NO_POSITION)

    /**
     * Get current adapter position safely
     * @return adapter position or -1 if invalid
     */
    protected fun getAdapterPositionSafe(): Int =
        if (isValidPosition()) adapterPosition else RecyclerView.NO_POSITION

    /**
     * Abstract method to bind data to the ViewHolder
     * @param item Data item to bind
     * @param position Position of the item
     */
//    public abstract fun bind(item: ITEM, position: Int)

    /**
     * Execute pending bindings for DataBinding
     * Call this after setting data to binding variables
     */
    protected fun executePendingBindings() {
        binding.executePendingBindings()
    }
}