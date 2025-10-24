package kr.open.library.simple_ui.presenter.ui.adapter.normal.base

import android.view.View
import androidx.recyclerview.widget.AdapterListUpdateCallback
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kr.open.library.simple_ui.logcat.Logx
import kr.open.library.simple_ui.presenter.ui.adapter.viewholder.BaseRcvViewHolder

/**
 * RecyclerView Base Adapter backed by [AsyncListDiffer] for background DiffUtil calculation.
 *
 * @param ITEM Type of items in the adapter
 * @param VH ViewHolder type for the adapter
 */
public abstract class BaseRcvAdapter<ITEM : Any, VH : RecyclerView.ViewHolder> :
    RecyclerView.Adapter<VH>() {

    private var onItemClickListener: ((Int, ITEM, View) -> Unit)? = null
    private var onItemLongClickListener: ((Int, ITEM, View) -> Unit)? = null

    /**
     * Whether DiffUtil should detect moved items.
     * Recreating the differ ensures the updated flag is respected.
     */
    public var detectMoves: Boolean = false
        set(value) {
            if (field == value) return
            field = value
            recreateDiffer()
        }

    private var diffUtilItemSame: ((oldItem: ITEM, newItem: ITEM) -> Boolean)? = null
    private var diffUtilContentsSame: ((oldItem: ITEM, newItem: ITEM) -> Boolean)? = null
    private var diffUtilChangePayload: ((oldItem: ITEM, newItem: ITEM) -> Any?)? = null

    private var differ: AsyncListDiffer<ITEM> =
        AsyncListDiffer(AdapterListUpdateCallback(this), buildDifferConfig())

    /**
     * DiffUtil에서 아이템 동일 여부를 비교하는 로직을 설정합니다.
     */
    public fun setDiffUtilItemSame(diffUtilItemSame: (oldItem: ITEM, newItem: ITEM) -> Boolean) {
        this.diffUtilItemSame = diffUtilItemSame
        recreateDiffer()
    }

    /**
     * DiffUtil에서 아이템 내용이 같은지 비교하는 로직을 설정합니다.
     */
    public fun setDiffUtilContentsSame(diffUtilContentsSame: (oldItem: ITEM, newItem: ITEM) -> Boolean) {
        this.diffUtilContentsSame = diffUtilContentsSame
        recreateDiffer()
    }

    /**
     * DiffUtil에서 변경 payload를 생성하는 로직을 설정합니다.
     */
    public fun setDiffUtilChangePayload(diffUtilChangePayload: (oldItem: ITEM, newItem: ITEM) -> Any?) {
        this.diffUtilChangePayload = diffUtilChangePayload
        recreateDiffer()
    }

    /**
     * Override to customise item comparison for DiffUtil.
     * Default implementation compares by identity.
     */
    protected open fun diffUtilAreItemsTheSame(oldItem: ITEM, newItem: ITEM): Boolean =
        diffUtilItemSame?.invoke(oldItem, newItem) ?: (oldItem === newItem)

    /**
     * Override to customise content comparison for DiffUtil.
     * Default implementation uses equals.
     */
    protected open fun diffUtilAreContentsTheSame(oldItem: ITEM, newItem: ITEM): Boolean =
        diffUtilContentsSame?.invoke(oldItem, newItem) ?: (oldItem == newItem)

    /**
     * Override to provide payload for partial updates when items are the same but contents differ.
     *
     * @return Payload object for partial update, null for full update
     */
    protected open fun diffUtilGetChangePayload(oldItem: ITEM, newItem: ITEM): Any? =
        diffUtilChangePayload?.invoke(oldItem, newItem)

    /**
     * Subclasses must implement this method to bind data to ViewHolder.
     */
    protected abstract fun onBindViewHolder(holder: VH, position: Int, item: ITEM)

    /**
     * Override this method to support partial updates with payloads.
     *
     * @param payloads List of payloads for partial update
     */
    protected open fun onBindViewHolder(holder: VH, position: Int, item: ITEM, payloads: List<Any>) {
        onBindViewHolder(holder, position, item)
    }

    public override fun getItemCount(): Int = differ.currentList.size

    public fun getItem(position: Int): ITEM {
        if (!isPositionValid(position)) {
            val size = differ.currentList.size
            Logx.e("getItem() called with invalid position: $position, size: $size")
            throw IndexOutOfBoundsException("Invalid position: $position, size: $size")
        }
        return differ.currentList[position]
    }

    public fun getItems(): List<ITEM> = differ.currentList

    /**
     * Sets new items using AsyncListDiffer for efficient updates.
     */
    public fun setItems(newItems: List<ITEM>) {
        differ.submitList(newItems.toList())
    }

    /**
     * Adds items to the end of the current list.
     *
     * @return true if items were added successfully
     */
    public open fun addItems(items: List<ITEM>): Boolean {
        return try {
            if (items.isEmpty()) {
                Logx.d("addItems() items is empty")
                return true
            }
            val updatedList = getMutableItemList().apply { addAll(items) }
            differ.submitList(updatedList.toList())
            true
        } catch (e: Exception) {
            Logx.e("Failed to add items to list", e)
            false
        }
    }

    /**
     * Creates a mutable copy of the current item list.
     */
    private fun getMutableItemList(): MutableList<ITEM> = differ.currentList.toMutableList()

    /**
     * Adds a single item to the end of the list.
     *
     * @return true if the item was added successfully
     */
    public open fun addItem(item: ITEM): Boolean {
        return try {
            val updatedList = getMutableItemList().apply { add(item) }
            differ.submitList(updatedList.toList())
            true
        } catch (e: Exception) {
            Logx.e("Failed to add item to list", e)
            false
        }
    }

    /**
     * Adds a single item at the specified position.
     */
    public fun addItemAt(position: Int, item: ITEM): Boolean {
        return try {
            if (position < 0 || position > itemCount) {
                throw IndexOutOfBoundsException("Cannot add item at position $position. Valid range: 0..$itemCount")
            }

            val updatedList = getMutableItemList().apply { add(position, item) }
            differ.submitList(updatedList.toList())
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
     * Removes all items from the list.
     *
     * @return true if items were removed successfully
     */
    public open fun removeAll(): Boolean {
        return try {
            if (differ.currentList.isEmpty()) {
                return true
            }
            differ.submitList(emptyList())
            true
        } catch (e: Exception) {
            Logx.e("Failed to remove all items", e)
            false
        }
    }

    /**
     * Removes the item at the specified position.
     *
     * @return true if the item was removed successfully
     */
    public open fun removeAt(position: Int): Boolean {
        return try {
            if (!isPositionValid(position)) {
                throw IndexOutOfBoundsException(
                    "Cannot remove item at position $position. Valid range: 0 until $itemCount"
                )
            }

            val updatedList = getMutableItemList().apply { removeAt(position) }
            differ.submitList(updatedList.toList())
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
     * Removes the first occurrence of the specified item.
     *
     * @return true if the item was found and removed successfully
     */
    public open fun removeItem(item: ITEM): Boolean {
        val position = differ.currentList.indexOf(item)
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
            onBindViewHolder(holder, position)
        } else {
            if (!isPositionValid(position)) {
                Logx.e("Cannot bind item with payload, position is $position, itemcount $itemCount")
                return
            }

            val item = getItem(position)
            onBindViewHolder(holder, position, item, payloads)
        }
    }

    /**
     * Checks if the given position is valid for the current list.
     */
    protected fun isPositionValid(position: Int): Boolean =
        position > RecyclerView.NO_POSITION && position < differ.currentList.size

    private fun createDiffCallback(): DiffUtil.ItemCallback<ITEM> =
        object : DiffUtil.ItemCallback<ITEM>() {
            override fun areItemsTheSame(oldItem: ITEM, newItem: ITEM): Boolean =
                diffUtilAreItemsTheSame(oldItem, newItem)

            override fun areContentsTheSame(oldItem: ITEM, newItem: ITEM): Boolean =
                diffUtilAreContentsTheSame(oldItem, newItem)

            override fun getChangePayload(oldItem: ITEM, newItem: ITEM): Any? =
                diffUtilGetChangePayload(oldItem, newItem)
        }

    private fun buildDifferConfig(): AsyncDifferConfig<ITEM> =
        AsyncDifferConfig.Builder(createDiffCallback()).apply {
            try {
                val method = AsyncDifferConfig.Builder::class.java.getMethod(
                    "setDetectMoves",
                    Boolean::class.javaPrimitiveType
                )
                method.invoke(this, detectMoves)
            } catch (_: NoSuchMethodException) {
                if (!detectMoves) {
                    Logx.w("AsyncListDiffer", "setDetectMoves not available; detectMoves flag is ignored on this version.")
                }
            } catch (e: Exception) {
                Logx.w("AsyncListDiffer", "Failed to reflectively set detectMoves: ${e.message}")
            }
        }.build()

    private fun recreateDiffer() {
        val snapshot = differ.currentList.toList()
        differ = AsyncListDiffer(AdapterListUpdateCallback(this), buildDifferConfig())
        differ.submitList(snapshot)
    }

    public fun setOnItemClickListener(listener: (Int, ITEM, View) -> Unit) {
        onItemClickListener = listener
    }

    public fun setOnItemLongClickListener(listener: (Int, ITEM, View) -> Unit) {
        onItemLongClickListener = listener
    }

    override fun onViewRecycled(holder: VH) {
        super.onViewRecycled(holder)
        if (holder is BaseRcvViewHolder) {
            holder.clearViewCache()
        }
    }
}
