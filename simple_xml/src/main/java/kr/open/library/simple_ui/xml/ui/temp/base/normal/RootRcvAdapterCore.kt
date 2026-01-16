package kr.open.library.simple_ui.xml.ui.temp.base.normal

import android.view.View
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kr.open.library.simple_ui.core.logcat.Logx

abstract class RootRcvAdapterCore<ITEM : Any, VH : RecyclerView.ViewHolder>(
    diffUtilEnabled: Boolean = false,
) : RecyclerView.Adapter<VH>() {
    private val items = mutableListOf<ITEM>()
    private var diffEnabled: Boolean = diffUtilEnabled

    private var diffUtilItemSame: ((oldItem: ITEM, newItem: ITEM) -> Boolean)? = null
    private var diffUtilContentsSame: ((oldItem: ITEM, newItem: ITEM) -> Boolean)? = null
    private var diffUtilChangePayload: ((oldItem: ITEM, newItem: ITEM) -> Any?)? = null

    private var onItemClickListener: ((Int, ITEM, View) -> Unit)? = null
    private var onItemLongClickListener: ((Int, ITEM, View) -> Unit)? = null

    fun setDiffUtilEnabled(enabled: Boolean) {
        diffEnabled = enabled
    }

    fun setDiffUtilItemSame(callback: (oldItem: ITEM, newItem: ITEM) -> Boolean) {
        diffUtilItemSame = callback
    }

    fun setDiffUtilContentsSame(callback: (oldItem: ITEM, newItem: ITEM) -> Boolean) {
        diffUtilContentsSame = callback
    }

    fun setDiffUtilChangePayload(callback: (oldItem: ITEM, newItem: ITEM) -> Any?) {
        diffUtilChangePayload = callback
    }

    fun setOnItemClickListener(listener: (position: Int, item: ITEM, view: View) -> Unit) {
        onItemClickListener = listener
    }

    fun setOnItemLongClickListener(listener: (position: Int, item: ITEM, view: View) -> Unit) {
        onItemLongClickListener = listener
    }

    fun setItems(newItems: List<ITEM>) {
        if (diffEnabled) {
            applyDiff(newItems)
            return
        }

        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun getItems(): List<ITEM> = items.toList()

    fun addItem(item: ITEM) {
        val position = items.size
        items.add(item)
        notifyItemInserted(position)
    }

    fun addItemAt(position: Int, item: ITEM): Boolean {
        if (!isAddPositionValid(position)) {
            Logx.e("Cannot add item at position $position. Valid range: 0..${items.size}")
            return false
        }
        items.add(position, item)
        notifyItemInserted(position)
        return true
    }

    fun addItems(itemsToAdd: List<ITEM>) {
        if (itemsToAdd.isEmpty()) return
        val start = items.size
        items.addAll(itemsToAdd)
        notifyItemRangeInserted(start, itemsToAdd.size)
    }

    fun removeItem(item: ITEM): Boolean {
        val position = items.indexOf(item)
        if (position == RecyclerView.NO_POSITION) {
            Logx.e("Item not found in the list")
            return false
        }
        items.removeAt(position)
        notifyItemRemoved(position)
        return true
    }

    fun removeAll() {
        if (items.isEmpty()) return
        val size = items.size
        items.clear()
        notifyItemRangeRemoved(0, size)
    }

    fun replaceItemAt(position: Int, item: ITEM): Boolean {
        if (!isPositionValid(position)) {
            Logx.e("Cannot replace item at position $position. Valid range: 0 until ${items.size}")
            return false
        }
        items[position] = item
        notifyItemChanged(position)
        return true
    }

    fun removeAt(position: Int): Boolean {
        if (!isPositionValid(position)) {
            Logx.e("Cannot remove item at position $position. Valid range: 0 until ${items.size}")
            return false
        }
        items.removeAt(position)
        notifyItemRemoved(position)
        return true
    }

    fun moveItem(from: Int, to: Int): Boolean {
        if (!isPositionValid(from) || !isPositionValid(to)) {
            Logx.e("Cannot move item. Valid range: 0 until ${items.size}")
            return false
        }
        if (from == to) return true
        val item = items.removeAt(from)
        items.add(to, item)
        notifyItemMoved(from, to)
        return true
    }

    protected fun getItem(position: Int): ITEM = items[position]

    override fun getItemCount(): Int = items.size

    final override fun onBindViewHolder(holder: VH, position: Int) {
        if (!isPositionValid(position)) {
            Logx.e("Cannot bind item, position is $position, itemCount ${items.size}")
            return
        }

        val item = getItem(position)
        bindClickListeners(holder, item)
        onBindItem(holder, position, item)
    }

    final override fun onBindViewHolder(holder: VH, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
            return
        }

        if (!isPositionValid(position)) {
            Logx.e("Cannot bind item with payload, position is $position, itemCount ${items.size}")
            return
        }

        val item = getItem(position)
        bindClickListeners(holder, item)
        onBindItem(holder, position, item, payloads)
    }

    protected abstract fun onBindItem(holder: VH, position: Int, item: ITEM)

    protected open fun onBindItem(holder: VH, position: Int, item: ITEM, payloads: List<Any>) {
        onBindItem(holder, position, item)
    }

    protected fun isPositionValid(position: Int): Boolean = position >= 0 && position < items.size

    private fun isAddPositionValid(position: Int): Boolean = position >= 0 && position <= items.size

    private fun bindClickListeners(holder: VH, item: ITEM) {
        holder.itemView.setOnClickListener { view ->
            val adapterPosition = holder.adapterPosition
            if (adapterPosition != RecyclerView.NO_POSITION) {
                onItemClickListener?.invoke(adapterPosition, item, view)
            }
        }

        holder.itemView.setOnLongClickListener { view ->
            val adapterPosition = holder.adapterPosition
            if (adapterPosition != RecyclerView.NO_POSITION) {
                onItemLongClickListener?.invoke(adapterPosition, item, view)
            }
            true
        }
    }

    private fun diffUtilAreItemsTheSame(oldItem: ITEM, newItem: ITEM): Boolean =
        diffUtilItemSame?.invoke(oldItem, newItem) ?: (oldItem == newItem)

    private fun diffUtilAreContentsTheSame(oldItem: ITEM, newItem: ITEM): Boolean =
        diffUtilContentsSame?.invoke(oldItem, newItem) ?: (oldItem == newItem)

    private fun diffUtilGetChangePayload(oldItem: ITEM, newItem: ITEM): Any? =
        diffUtilChangePayload?.invoke(oldItem, newItem)

    private fun applyDiff(newItems: List<ITEM>) {
        val oldItems = items.toList()
        try {
            val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize(): Int = oldItems.size

                override fun getNewListSize(): Int = newItems.size

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                    diffUtilAreItemsTheSame(oldItems[oldItemPosition], newItems[newItemPosition])

                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                    diffUtilAreContentsTheSame(oldItems[oldItemPosition], newItems[newItemPosition])

                override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? =
                    diffUtilGetChangePayload(oldItems[oldItemPosition], newItems[newItemPosition])
            })

            items.clear()
            items.addAll(newItems)
            diffResult.dispatchUpdatesTo(this)
        } catch (e: Exception) {
            Logx.e("DiffUtil apply failed: ${e.message}")
            items.clear()
            items.addAll(newItems)
            notifyDataSetChanged()
        }
    }
}
