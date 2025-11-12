package kr.open.library.simple_ui.presenter.ui.adapter.normal.base

import android.view.View
import androidx.recyclerview.widget.AdapterListUpdateCallback
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kr.open.library.simple_ui.logcat.Logx
import kr.open.library.simple_ui.presenter.ui.adapter.queue.AdapterOperationQueue
import kr.open.library.simple_ui.presenter.ui.adapter.viewholder.BaseRcvViewHolder
import java.util.concurrent.Executor

/**
 * RecyclerView Base Adapter backed by [AsyncListDiffer] for background DiffUtil calculation.
 *
 * @param ITEM Type of items in the adapter
 * @param VH ViewHolder type for the adapter
 * @param testExecutor Optional executor for testing (synchronous execution in tests)
 */
public abstract class BaseRcvAdapter<ITEM : Any, VH : RecyclerView.ViewHolder>(
    private val testExecutor: Executor? = null
) : RecyclerView.Adapter<VH>() {

    private var onItemClickListener: ((Int, ITEM, View) -> Unit)? = null
    private var onItemLongClickListener: ((Int, ITEM, View) -> Unit)? = null

    // Operation Queue for handling consecutive operations
    private val operationQueue = AdapterOperationQueue<ITEM>(
        getCurrentList = { differ.currentList },
        submitList = { list, callback -> differ.submitList(list, callback) }
    )

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
     * 기존 큐의 모든 작업을 취소하고 새로운 리스트로 교체
     */
    public fun setItems(newItems: List<ITEM>, commitCallback: (() -> Unit)? = null) {
        operationQueue.clearQueueAndExecute(AdapterOperationQueue.SetItemsOp(newItems, commitCallback))
    }

    /**
     * Adds items to the end of the current list.
     *
     * @return true if items were added successfully
     */
    public open fun addItems(items: List<ITEM>, commitCallback: (() -> Unit)? = null): Boolean {
        if (items.isEmpty()) {
            Logx.d("addItems() items is empty")
            return true
        }
        operationQueue.enqueueOperation(AdapterOperationQueue.AddItemsOp(items, commitCallback))
        return true
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
    public open fun addItem(item: ITEM, commitCallback: (() -> Unit)? = null): Boolean {
        operationQueue.enqueueOperation(AdapterOperationQueue.AddItemOp(item, commitCallback))
        return true
    }

    /**
     * Adds a single item at the specified position.
     * @return true if position is valid, false otherwise
     */
    public fun addItemAt(position: Int, item: ITEM, commitCallback: (() -> Unit)? = null): Boolean {
        if (position < 0 || position > itemCount) {
            Logx.e("Cannot add item at position $position. Valid range: 0..$itemCount")
            return false
        }
        operationQueue.enqueueOperation(AdapterOperationQueue.AddItemAtOp(position, item, commitCallback))
        return true
    }

    /**
     * Removes all items from the list.
     *
     * @return true if items were removed successfully
     */
    public open fun removeAll(commitCallback: (() -> Unit)? = null): Boolean {
        operationQueue.enqueueOperation(AdapterOperationQueue.ClearItemsOp(commitCallback))
        return true
    }

    /**
     * Removes the item at the specified position.
     *
     * @return true if position is valid, false otherwise
     */
    public open fun removeAt(position: Int, commitCallback: (() -> Unit)? = null): Boolean {
        if (position < 0 || position >= itemCount) {
            Logx.e("Cannot remove item at position $position. Valid range: 0 until $itemCount")
            return false
        }
        operationQueue.enqueueOperation(AdapterOperationQueue.RemoveAtOp(position, commitCallback))
        return true
    }

    /**
     * Removes the first occurrence of the specified item.
     *
     * @return true if item exists in the list, false otherwise
     */
    public open fun removeItem(item: ITEM, commitCallback: (() -> Unit)? = null): Boolean {
        if (!differ.currentList.contains(item)) {
            Logx.e("Item not found in the list")
            return false
        }
        operationQueue.enqueueOperation(AdapterOperationQueue.RemoveItemOp(item, commitCallback))
        return true
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
            // Set test executor for synchronous execution in tests
            testExecutor?.let { executor ->
                try {
                    val method = AsyncDifferConfig.Builder::class.java.getMethod(
                        "setBackgroundThreadExecutor",
                        Executor::class.java
                    )
                    method.invoke(this, executor)
                } catch (_: NoSuchMethodException) {
                    Logx.w("AsyncListDiffer", "setBackgroundThreadExecutor not available")
                } catch (e: Exception) {
                    Logx.w("AsyncListDiffer", "Failed to set test executor: ${e.message}")
                }
            }

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

    private fun recreateDiffer(commitCallback: (() -> Unit)? = null) {
        val snapshot = differ.currentList.toList()
        differ = AsyncListDiffer(AdapterListUpdateCallback(this), buildDifferConfig())
        differ.submitList(snapshot) { commitCallback?.invoke() }
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
