package kr.open.library.simple_ui.presenter.ui.adapter.normal.base

import android.view.View
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kr.open.library.simple_ui.logcat.Logx
import kr.open.library.simple_ui.presenter.ui.adapter.viewholder.BaseRcvViewHolder


/**
 * Recycler View Base Adapter
 * @param ITEM Type of items in the adapter
 * @param VH ViewHolder type for the adapter
 */
public abstract class BaseRcvAdapter<ITEM: Any, VH : RecyclerView.ViewHolder>() : RecyclerView.Adapter<VH>() {

    /**
     * Current list of items in the adapter
     */
    private var itemList: List<ITEM> = emptyList()

    private var onItemClickListener: ((Int, ITEM, View) -> Unit)? = null
    private var onItemLongClickListener: ((Int, ITEM, View) -> Unit)? = null

    /**
     * Whether DiffUtil should detect moved items
     */
    public var detectMoves: Boolean = false

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

    /**
     * Override to customize item comparison for DiffUtil
     * Default implementation compares by identity
     */
    protected open fun diffUtilAreItemsTheSame(oldItem: ITEM, newItem: ITEM): Boolean =
        diffUtilItemSame?.invoke(oldItem, newItem) ?: (oldItem === newItem)


    /**
     * Override to customize content comparison for DiffUtil
     * Default implementation uses equals
     */
    protected open fun diffUtilAreContentsTheSame(oldItem: ITEM, newItem: ITEM): Boolean =
        diffUtilContentsSame?.invoke(oldItem, newItem) ?: (oldItem == newItem)

    /**
     * Override to provide payload for partial updates when items are the same but contents differ
     * @param oldItem Previous item
     * @param newItem New item
     * @return Payload object for partial update, null for full update
     */
    protected open fun diffUtilGetChangePayload(oldItem: ITEM, newItem: ITEM): Any? = diffUtilChangePayload?.invoke(oldItem, newItem)

    /**
     * Subclasses must implement this method to bind data to ViewHolder
     */
    protected abstract fun onBindViewHolder(holder: VH, position: Int, item: ITEM)

    /**
     * Override this method to support partial updates with payloads
     * 부분 업데이트를 위한 payload 지원 바인딩 메서드
     * @param holder ViewHolder
     * @param position Position of the item
     * @param item Item to bind
     * @param payloads List of payloads for partial update
     */
    protected open fun onBindViewHolder(holder: VH, position: Int, item: ITEM, payloads: List<Any>) {
        // Default implementation falls back to full binding
        onBindViewHolder(holder, position, item)
    }

    public override fun getItemCount(): Int = itemList.size

    public fun getItem(position: Int): ITEM {
        // Add bounds check for safety, although isPositionValid should handle most cases
        if (!isPositionValid(position)) {
            Logx.e("getItem() called with invalid position: $position, size: ${itemList.size}")
            throw IndexOutOfBoundsException("Invalid position: $position, size: ${itemList.size}")
        }
        return itemList[position]
    }

    public fun getItems(): List<ITEM> = itemList

    /**
     * Sets new items using DiffUtil for efficient updates
     */
    public fun setItems(newItems: List<ITEM>) {
        update(newItems)
    }

    /**
     * Adds items to the end of the current list
     * @param items List of items to add
     * @return true if items were added successfully
     */
    public open fun addItems(items: List<ITEM>): Boolean {
        return try {
            if (items.isEmpty()) {
                Logx.d("addItems() items is empty")
                return true // Empty list is considered successful
            }

            val fromSize = itemList.size
            itemList = getMutableItemList().apply {
                addAll(items)
            }
            notifyItemRangeInserted(fromSize, items.size)
            true
        } catch (e: Exception) {
            Logx.e("Failed to add items to list", e)
            false
        }
    }

    /**
     * Creates a mutable copy of the current item list
     */
    private fun getMutableItemList(): MutableList<ITEM> = itemList.toMutableList()

    /**
     * Adds a single item to the end of the list
     * @param item Item to add
     * @return true if item was added successfully
     */
    public open fun addItem(item: ITEM): Boolean {
        return try {
            val newPosition = itemList.size
            itemList = itemList + item
            notifyItemInserted(newPosition)
            true
        } catch (e: Exception) {
            Logx.e("Failed to add item to list", e)
            false
        }
    }

    /**
     * Adds a single item at the specified position
     */
    public fun addItemAt(position: Int, item: ITEM): Boolean {
        return try {
            if (position < 0 || position > itemCount) {
                throw IndexOutOfBoundsException("Cannot add item at position $position. Valid range: 0..$itemCount")
            }

            itemList = getMutableItemList().apply {
                add(position, item)
            }
            notifyItemInserted(position)
            true
        } catch (e: IndexOutOfBoundsException) {
            Logx.e("Failed to add item at position $position: ${e.message}")
            false
        } catch (e: Exception) {
            Logx.e("Unexpected error while adding item at position $position", e)
            false
        }
    }

    /**
     * Removes all items from the list
     * @return true if items were removed successfully
     */
    public open fun removeAll(): Boolean {
        return try {
            val itemSize = itemList.size
            if (itemSize == 0) {
                return true // Already empty
            }
            itemList = emptyList()
            notifyItemRangeRemoved(0, itemSize)
            true
        } catch (e: Exception) {
            Logx.e("Failed to remove all items", e)
            false
        }
    }

    /**
     * Removes the item at the specified position
     * @param position Position of the item to remove
     * @return true if item was removed successfully
     */
    public open fun removeAt(position: Int): Boolean {
        return try {
            if (position < 0 || position >= itemCount) {
                throw IndexOutOfBoundsException(
                    "Cannot remove item at position $position. Valid range: 0 until $itemCount"
                )
            }

            itemList = getMutableItemList().apply {
                removeAt(position)
            }
            notifyItemRemoved(position)
            true
        } catch (e: IndexOutOfBoundsException) {
            Logx.e("Failed to remove item at position $position: ${e.message}")
            false
        } catch (e: Exception) {
            Logx.e("Unexpected error while removing item at position $position", e)
            false
        }
    }

    /**
     * Removes the first occurrence of the specified item
     * @param item Item to remove
     * @return true if item was found and removed successfully
     */
    public open fun removeItem(item: ITEM): Boolean {
        val position = itemList.indexOf(item)
        return if (position != -1) {
            removeAt(position)
        } else {
            Logx.w("Item not found in list for removal")
            false
        }
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        if (!isPositionValid(position)) {
            Logx.e("Cannot bind item, position is $position, itemcount $itemCount")
            return
        }

        val item = getItem(position)

        holder.itemView.apply {
            setOnClickListener { view ->
                val adapterPosition = holder.adapterPosition
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onItemClickListener?.invoke(adapterPosition, item, view)
                }
            }

            setOnLongClickListener { view ->
                val adapterPosition = holder.adapterPosition
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onItemLongClickListener?.invoke(adapterPosition, item, view)
                }
                true
            }
        }

        onBindViewHolder(holder, position, item)
    }

    override fun onBindViewHolder(holder: VH, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            // No payloads, perform full binding
            onBindViewHolder(holder, position)
        } else {
            // Payloads exist, perform partial update
            if (!isPositionValid(position)) {
                Logx.e("Cannot bind item with payload, position is $position, itemcount $itemCount")
                return
            }

            val item = getItem(position)
            onBindViewHolder(holder, position, item, payloads)
        }
    }

    /**
     * Checks if the given position is valid for the current list
     */
    protected fun isPositionValid(position: Int): Boolean =
        position > RecyclerView.NO_POSITION && position < itemList.size

    /**
     * Updates the list using DiffUtil for efficient updates
     */
    private fun update(newItemList: List<ITEM>) {
        val diffResult = DiffUtil.calculateDiff(
            RecyclerViewDiffUtil(itemList, newItemList),
            detectMoves
        )
        itemList = newItemList
        diffResult.dispatchUpdatesTo(this)
    }

    private inner class RecyclerViewDiffUtil(
        private val oldList: List<ITEM>,
        private val newList: List<ITEM>
    ) : DiffUtil.Callback() {

        override fun getOldListSize() = oldList.size

        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            diffUtilAreItemsTheSame(oldList[oldItemPosition], newList[newItemPosition])

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            diffUtilAreContentsTheSame(oldList[oldItemPosition], newList[newItemPosition])

        override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? =
            diffUtilGetChangePayload(oldList[oldItemPosition], newList[newItemPosition])
    }

    public fun setOnItemClickListener(listener: (Int, ITEM, View) -> Unit) {
        onItemClickListener = listener
    }

    public fun setOnItemLongClickListener(listener: (Int, ITEM, View) -> Unit) {
        onItemLongClickListener = listener
    }

    override fun onViewRecycled(holder: VH) {
        super.onViewRecycled(holder)
        if(holder is BaseRcvViewHolder){
            holder.clearViewCache()
        }
    }
}