package kr.open.library.simple_ui.xml.ui.adapter.normal.base

import android.view.View
import androidx.recyclerview.widget.AdapterListUpdateCallback
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.xml.ui.adapter.queue.AdapterOperationQueue
import kr.open.library.simple_ui.xml.ui.adapter.viewholder.BaseRcvViewHolder
import java.util.concurrent.Executor

/**
 * RecyclerView base adapter backed by [AsyncListDiffer] for background DiffUtil calculation.<br><br>
 * 백그라운드 DiffUtil 계산을 위해 [AsyncListDiffer]를 사용하는 RecyclerView 기본 어댑터입니다.<br>
 *
 * @param ITEM Item type used by this adapter.<br><br>
 *             어댑터에 담기는 아이템 타입입니다.
 *
 * @param VH ViewHolder type used by this adapter.<br><br>
 *           어댑터에서 사용하는 ViewHolder 타입입니다.
 *
 * @param testExecutor Optional executor for tests (synchronous execution in tests).<br><br>
 *                     테스트에서 동기 실행을 강제할 때 주입할 수 있는 Executor입니다.
 */
public abstract class BaseRcvAdapter<ITEM : Any, VH : RecyclerView.ViewHolder>(
    private val testExecutor: Executor? = null
) : RecyclerView.Adapter<VH>() {

    /**
     * Listener for item click events.<br><br>
     * 아이템 클릭 이벤트를 위한 리스너입니다.<br>
     */
    private var onItemClickListener: ((Int, ITEM, View) -> Unit)? = null

    /**
     * Listener for item long-click events.<br><br>
     * 아이템 롱클릭 이벤트를 위한 리스너입니다.<br>
     */
    private var onItemLongClickListener: ((Int, ITEM, View) -> Unit)? = null

    /**
     * Operation Queue for handling consecutive operations safely.<br><br>
     * 연속적인 작업을 안전하게 처리하기 위한 작업 큐입니다.<br>
     */
    private val operationQueue = AdapterOperationQueue<ITEM>(
        getCurrentList = { differ.currentList },
        submitList = { list, callback -> differ.submitList(list, callback) }
    )

    /**
     * Whether DiffUtil should detect moved items. Recreating the differ ensures the updated flag is respected.<br><br>
     * DiffUtil이 이동된 아이템을 감지해야 하는지 여부입니다. differ를 재생성하여 변경된 플래그가 반영되도록 합니다.<br>
     */
    public var detectMoves: Boolean = false
        set(value) {
            if (field == value) return
            field = value
            recreateDiffer()
        }

    /**
     * Custom logic for determining if items are the same.<br><br>
     * 아이템이 같은지 판단하기 위한 커스텀 로직입니다.<br>
     */
    private var diffUtilItemSame: ((oldItem: ITEM, newItem: ITEM) -> Boolean)? = null

    /**
     * Custom logic for determining if item contents are the same.<br><br>
     * 아이템 내용이 같은지 판단하기 위한 커스텀 로직입니다.<br>
     */
    private var diffUtilContentsSame: ((oldItem: ITEM, newItem: ITEM) -> Boolean)? = null

    /**
     * Custom logic for calculating item change payload.<br><br>
     * 아이템 변경 payload를 계산하기 위한 커스텀 로직입니다.<br>
     */
    private var diffUtilChangePayload: ((oldItem: ITEM, newItem: ITEM) -> Any?)? = null

    /**
     * AsyncListDiffer helper for computing differences on a background thread.<br><br>
     * 백그라운드 스레드에서 차이를 계산하기 위한 AsyncListDiffer 헬퍼입니다.<br>
     */
    private var differ: AsyncListDiffer<ITEM> =
        AsyncListDiffer(AdapterListUpdateCallback(this), buildDifferConfig())

    /**
     * Sets custom identity comparison for DiffUtil.<br><br>
     * DiffUtil에서 아이템 동일성 비교 로직을 설정합니다.<br>
     */
    public fun setDiffUtilItemSame(diffUtilItemSame: (oldItem: ITEM, newItem: ITEM) -> Boolean) {
        this.diffUtilItemSame = diffUtilItemSame
        recreateDiffer()
    }

    /**
     * Sets custom content comparison for DiffUtil.<br><br>
     * DiffUtil에서 아이템 내용 비교 로직을 설정합니다.<br>
     */
    public fun setDiffUtilContentsSame(diffUtilContentsSame: (oldItem: ITEM, newItem: ITEM) -> Boolean) {
        this.diffUtilContentsSame = diffUtilContentsSame
        recreateDiffer()
    }

    /**
     * Sets custom change-payload logic for DiffUtil.<br><br>
     * DiffUtil에서 변경 payload 생성 로직을 설정합니다.<br>
     */
    public fun setDiffUtilChangePayload(diffUtilChangePayload: (oldItem: ITEM, newItem: ITEM) -> Any?) {
        this.diffUtilChangePayload = diffUtilChangePayload
        recreateDiffer()
    }

    /**
     * Item identity comparison used by DiffUtil.<br><br>
     * DiffUtil에서 사용하는 아이템 동일성 비교입니다.<br>
     *
     * @return True if items are the same, false otherwise.<br><br>
     *         아이템이 같으면 true, 그렇지 않으면 false.<br>
     */
    protected open fun diffUtilAreItemsTheSame(oldItem: ITEM, newItem: ITEM): Boolean =
        diffUtilItemSame?.invoke(oldItem, newItem) ?: (oldItem === newItem)

    /**
     * Item content comparison used by DiffUtil.<br><br>
     * DiffUtil에서 사용하는 아이템 내용 비교입니다.<br>
     *
     * @return True if contents are the same, false otherwise.<br><br>
     *         내용이 같으면 true, 그렇지 않으면 false.<br>
     */
    protected open fun diffUtilAreContentsTheSame(oldItem: ITEM, newItem: ITEM): Boolean =
        diffUtilContentsSame?.invoke(oldItem, newItem) ?: (oldItem == newItem)

    /**
     * Provides payload when items match but contents differ.<br><br>
     * 아이템은 같고 내용만 다를 때 payload를 제공합니다.<br>
     *
     * @return Change payload object, or null if none.<br><br>
     *         변경 payload 객체, 없으면 null.<br>
     */
    protected open fun diffUtilGetChangePayload(oldItem: ITEM, newItem: ITEM): Any? =
        diffUtilChangePayload?.invoke(oldItem, newItem)

    /**
     * Binds data to the ViewHolder (must be implemented by subclasses).<br><br>
     * 서브클래스가 ViewHolder에 데이터를 바인딩해야 합니다.<br>
     */
    protected abstract fun onBindViewHolder(holder: VH, position: Int, item: ITEM)

    /**
     * Binds with payloads for partial updates.<br><br>
     * 부분 갱신 payload와 함께 바인딩합니다.<br>
     */
    protected open fun onBindViewHolder(holder: VH, position: Int, item: ITEM, payloads: List<Any>) {
        onBindViewHolder(holder, position, item)
    }

    /**
     * Returns the current list size.<br><br>
     * 현재 리스트 크기를 반환합니다.<br>
     *
     * @return The number of items in the list.<br><br>
     *         리스트의 아이템 개수.<br>
     */
    public override fun getItemCount(): Int = differ.currentList.size

    /**
     * Returns the item at the given position, or throws if invalid.<br><br>
     * 주어진 위치의 아이템을 반환하며, 잘못된 경우 예외를 던집니다.<br>
     *
     * @return The item at the specified position.<br><br>
     *         지정된 위치의 아이템.<br>
     */
    public fun getItem(position: Int): ITEM {
        if (!isPositionValid(position)) {
            val size = differ.currentList.size
            Logx.e("getItem() called with invalid position: $position, size: $size")
            throw IndexOutOfBoundsException("Invalid position: $position, size: $size")
        }
        return differ.currentList[position]
    }

    /**
     * Returns an immutable snapshot of items.<br><br>
     * 아이템의 불변 스냅샷을 반환합니다.<br>
     *
     * @return A list containing all items.<br><br>
     *         모든 아이템을 포함하는 리스트.<br>
     */
    public fun getItems(): List<ITEM> = differ.currentList

    /**
     * Replaces items using AsyncListDiffer for efficient updates.<br><br>
     * AsyncListDiffer로 아이템을 교체해 효율적으로 갱신합니다.<br>
     */
    public fun setItems(newItems: List<ITEM>, commitCallback: (() -> Unit)? = null) {
        operationQueue.clearQueueAndExecute(AdapterOperationQueue.SetItemsOp(newItems, commitCallback))
    }

    /**
     * Appends multiple items.<br><br>
     * 여러 아이템을 뒤에 추가합니다.<br>
     *
     * @return True if operation was enqueued, false otherwise.<br><br>
     *         작업이 큐에 추가되면 true, 그렇지 않으면 false.<br>
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
     * Creates a mutable copy of the current list.<br><br>
     * 현재 리스트의 가변 사본을 만듭니다.<br>
     *
     * @return A mutable list containing all items.<br><br>
     *         모든 아이템을 포함하는 가변 리스트.<br>
     */
    private fun getMutableItemList(): MutableList<ITEM> = differ.currentList.toMutableList()

    /**
     * Appends a single item.<br><br>
     * 단일 아이템을 추가합니다.<br>
     *
     * @return True if operation was enqueued.<br><br>
     *         작업이 큐에 추가되면 true.<br>
     */
    public open fun addItem(item: ITEM, commitCallback: (() -> Unit)? = null): Boolean {
        operationQueue.enqueueOperation(AdapterOperationQueue.AddItemOp(item, commitCallback))
        return true
    }

    /**
     * Inserts an item at the given position.<br><br>
     * 지정 위치에 아이템을 삽입합니다.<br>
     *
     * @return True if operation was enqueued, false if position is invalid.<br><br>
     *         작업이 큐에 추가되면 true, 위치가 잘못되었으면 false.<br>
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
     * Clears all items from the list.<br><br>
     * 리스트의 모든 아이템을 제거합니다.<br>
     *
     * @return True if operation was enqueued.<br><br>
     *         작업이 큐에 추가되면 true.<br>
     */
    public open fun removeAll(commitCallback: (() -> Unit)? = null): Boolean {
        operationQueue.enqueueOperation(AdapterOperationQueue.ClearItemsOp(commitCallback))
        return true
    }

    /**
     * Removes the item at the given position.<br><br>
     * 지정 위치의 아이템을 제거합니다.<br>
     *
     * @return True if operation was enqueued, false if position is invalid.<br><br>
     *         작업이 큐에 추가되면 true, 위치가 잘못되었으면 false.<br>
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
     * Removes the first matching item.<br><br>
     * 첫 번째로 일치하는 아이템을 제거합니다.<br>
     *
     * @return True if operation was enqueued, false if item not found.<br><br>
     *         작업이 큐에 추가되면 true, 아이템을 찾지 못했으면 false.<br>
     */
    public open fun removeItem(item: ITEM, commitCallback: (() -> Unit)? = null): Boolean {
        if (!differ.currentList.contains(item)) {
            Logx.e("Item not found in the list")
            return false
        }
        operationQueue.enqueueOperation(AdapterOperationQueue.RemoveItemOp(item, commitCallback))
        return true
    }

    /**
     * Binds holder without payloads and wires click listeners.<br><br>
     * payload 없이 바인딩하며 클릭 리스너를 설정합니다.<br>
     */
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

    /**
     * Binds holder with payloads when provided.<br><br>
     * payload가 있을 때 ViewHolder를 바인딩합니다.<br>
     */
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
     * Checks if position is within current list bounds.<br><br>
     * 위치가 현재 리스트 범위 내인지 확인합니다.<br>
     *
     * @return True if position is valid, false otherwise.<br><br>
     *         위치가 유효하면 true, 그렇지 않으면 false.<br>
     */
    protected fun isPositionValid(position: Int): Boolean =
        position > RecyclerView.NO_POSITION && position < differ.currentList.size

    /**
     * Builds DiffUtil.ItemCallback using current comparison logic.<br><br>
     * 현재 비교 로직으로 DiffUtil.ItemCallback을 생성합니다.<br>
     *
     * @return A DiffUtil.ItemCallback instance.<br><br>
     *         DiffUtil.ItemCallback 인스턴스.<br>
     */
    private fun createDiffCallback(): DiffUtil.ItemCallback<ITEM> =
        object : DiffUtil.ItemCallback<ITEM>() {
            /**
             * DiffUtil identity check.<br><br>
             * DiffUtil의 동일성 비교입니다.<br>
             */
            override fun areItemsTheSame(oldItem: ITEM, newItem: ITEM): Boolean =
                diffUtilAreItemsTheSame(oldItem, newItem)

            /**
             * DiffUtil content equality check.<br><br>
             * DiffUtil의 내용 비교입니다.<br>
             */
            override fun areContentsTheSame(oldItem: ITEM, newItem: ITEM): Boolean =
                diffUtilAreContentsTheSame(oldItem, newItem)

            /**
             * DiffUtil payload provider.<br><br>
             * DiffUtil payload 제공자입니다.<br>
             */
            override fun getChangePayload(oldItem: ITEM, newItem: ITEM): Any? =
                diffUtilGetChangePayload(oldItem, newItem)
        }

    /**
     * Builds AsyncDifferConfig with optional test executor and detectMoves flag.<br><br>
     * 테스트 실행기·detectMoves 설정을 포함한 AsyncDifferConfig를 생성합니다.<br>
     *
     * @return An AsyncDifferConfig instance.<br><br>
     *         AsyncDifferConfig 인스턴스.<br>
     */
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

    /**
     * Recreates differ to apply updated config while keeping current list.<br><br>
     * 현재 리스트를 유지한 채 설정을 반영하도록 differ를 재생성합니다.<br>
     */
    private fun recreateDiffer(commitCallback: (() -> Unit)? = null) {
        val snapshot = differ.currentList.toList()
        differ = AsyncListDiffer(AdapterListUpdateCallback(this), buildDifferConfig())
        differ.submitList(snapshot) { commitCallback?.invoke() }
    }

    /**
     * Sets click listener for items.<br><br>
     * 아이템 클릭 리스너를 설정합니다.<br>
     */
    public fun setOnItemClickListener(listener: (Int, ITEM, View) -> Unit) {
        onItemClickListener = listener
    }

    /**
     * Sets long-click listener for items.<br><br>
     * 아이템 롱클릭 리스너를 설정합니다.<br>
     */
    public fun setOnItemLongClickListener(listener: (Int, ITEM, View) -> Unit) {
        onItemLongClickListener = listener
    }

    /**
     * Clears view cache when a BaseRcvViewHolder is recycled.<br><br>
     * BaseRcvViewHolder가 재활용될 때 뷰 캐시를 비웁니다.<br>
     */
    override fun onViewRecycled(holder: VH) {
        super.onViewRecycled(holder)
        if (holder is BaseRcvViewHolder) {
            holder.clearViewCache()
        }
    }
}
