package kr.open.library.simple_ui.xml.ui.temp.base.list

import android.view.View
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.xml.ui.temp.base.list.diffcallback.DefaultDiffCallback

abstract class RootListAdapterCore<ITEM : Any, VH : RecyclerView.ViewHolder>(
    diffCallback: DiffUtil.ItemCallback<ITEM> = DefaultDiffCallback(),
) : ListAdapter<ITEM, VH>(diffCallback) {
    private var onItemClickListener: ((Int, ITEM, View) -> Unit)? = null
    private var onItemLongClickListener: ((Int, ITEM, View) -> Unit)? = null

    fun setOnItemClickListener(listener: (position: Int, item: ITEM, view: View) -> Unit) {
        onItemClickListener = listener
    }

    fun setOnItemLongClickListener(listener: (position: Int, item: ITEM, view: View) -> Unit) {
        onItemLongClickListener = listener
    }

    fun setItems(items: List<ITEM>) {
        submitList(items.toList())
    }

    fun getItems(): List<ITEM> = currentList.toList()

    fun addItem(item: ITEM) {
        val newList = currentList.toMutableList()
        newList.add(item)
        submitList(newList)
    }

    fun addItemAt(position: Int, item: ITEM): Boolean {
        if (!isAddPositionValid(position)) {
            Logx.e("Cannot add item at position $position. Valid range: 0..${currentList.size}")
            return false
        }
        val newList = currentList.toMutableList()
        newList.add(position, item)
        submitList(newList)
        return true
    }

    fun addItems(itemsToAdd: List<ITEM>) {
        if (itemsToAdd.isEmpty()) return
        val newList = currentList.toMutableList()
        newList.addAll(itemsToAdd)
        submitList(newList)
    }

    fun removeItem(item: ITEM): Boolean {
        val position = currentList.indexOf(item)
        if (position == RecyclerView.NO_POSITION) {
            Logx.e("Item not found in the list")
            return false
        }
        val newList = currentList.toMutableList()
        newList.removeAt(position)
        submitList(newList)
        return true
    }

    fun removeAll() {
        if (currentList.isEmpty()) return
        submitList(emptyList())
    }

    fun replaceItemAt(position: Int, item: ITEM): Boolean {
        if (!isPositionValid(position)) {
            Logx.e("Cannot replace item at position $position. Valid range: 0 until ${currentList.size}")
            return false
        }
        val newList = currentList.toMutableList()
        newList[position] = item
        submitList(newList)
        return true
    }

    fun removeAt(position: Int): Boolean {
        if (!isPositionValid(position)) {
            Logx.e("Cannot remove item at position $position. Valid range: 0 until ${currentList.size}")
            return false
        }
        val newList = currentList.toMutableList()
        newList.removeAt(position)
        submitList(newList)
        return true
    }

    fun moveItem(from: Int, to: Int): Boolean {
        if (!isPositionValid(from) || !isPositionValid(to)) {
            Logx.e("Cannot move item. Valid range: 0 until ${currentList.size}")
            return false
        }
        if (from == to) return true
        val newList = currentList.toMutableList()
        val item = newList.removeAt(from)
        newList.add(to, item)
        submitList(newList)
        return true
    }

    final override fun onBindViewHolder(holder: VH, position: Int) {
        if (!isPositionValid(position)) {
            Logx.e("Cannot bind item, position is $position, itemCount ${currentList.size}")
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
            Logx.e("Cannot bind item with payload, position is $position, itemCount ${currentList.size}")
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

    protected fun isPositionValid(position: Int): Boolean = position >= 0 && position < currentList.size

    private fun isAddPositionValid(position: Int): Boolean = position >= 0 && position <= currentList.size

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
}
