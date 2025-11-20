package kr.open.library.simple_ui.presenter.ui.adapter.list.base

import android.view.View
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kr.open.library.simple_ui.logcat.Logx
import kr.open.library.simple_ui.presenter.ui.adapter.list.diffutil.RcvListDiffUtilCallBack
import kr.open.library.simple_ui.presenter.ui.adapter.queue.AdapterOperationQueue
import kr.open.library.simple_ui.presenter.ui.adapter.viewholder.BaseRcvViewHolder


/**
 * Base RecyclerView ListAdapter implementation with queue-based operations.<br>
 * Provides comprehensive item management and click handling functionality.<br><br>
 * 큐 기반 작업을 지원하는 기본 RecyclerView ListAdapter 구현체입니다.<br>
 * 포괄적인 아이템 관리 및 클릭 처리 기능을 제공합니다.<br>
 *
 * Features:<br>
 * - DiffUtil-based efficient list updates<br>
 * - Queue-based consecutive operations handling<br>
 * - Item click and long-click listener support<br>
 * - Add, remove, move, replace item operations<br>
 * - Partial update support via payloads<br>
 * - Automatic ViewHolder cache clearing<br><br>
 * 기능:<br>
 * - DiffUtil 기반 효율적인 리스트 업데이트<br>
 * - 큐 기반 연속 작업 처리<br>
 * - 아이템 클릭 및 롱클릭 리스너 지원<br>
 * - 아이템 추가, 제거, 이동, 교체 작업<br>
 * - payload를 통한 부분 업데이트 지원<br>
 * - ViewHolder 캐시 자동 정리<br>
 *
 * Usage example:<br>
 * ```kotlin
 * class MyAdapter : BaseRcvListAdapter<MyItem, MyViewHolder>(MyDiffUtil()) {
 *     override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
 *         return MyViewHolder(LayoutInflater.from(parent.context)
 *             .inflate(R.layout.item_layout, parent, false))
 *     }
 *
 *     override fun onBindViewHolder(holder: MyViewHolder, position: Int, item: MyItem) {
 *         holder.bind(item)
 *     }
 * }
 * ```
 * <br>
 *
 * @param ITEM The type of items in the list.<br><br>
 *             리스트의 아이템 타입.<br>
 *
 * @param VH The type of ViewHolder.<br><br>
 *           ViewHolder 타입.<br>
 *
 * @param listDiffUtil DiffUtil callback for comparing items.<br><br>
 *                     아이템 비교를 위한 DiffUtil 콜백.<br>
 *
 * @see RcvListDiffUtilCallBack For the DiffUtil callback implementation.<br><br>
 *      DiffUtil 콜백 구현은 RcvListDiffUtilCallBack을 참조하세요.<br>
 *
 * @see AdapterOperationQueue For the operation queue implementation.<br><br>
 *      작업 큐 구현은 AdapterOperationQueue를 참조하세요.<br>
 */
public abstract class BaseRcvListAdapter<ITEM, VH : RecyclerView.ViewHolder>(listDiffUtil: RcvListDiffUtilCallBack<ITEM>) :
    ListAdapter<ITEM, VH>(listDiffUtil) {

    /**
     * Callback for item click events.<br>
     * Receives position, item, and clicked view.<br><br>
     * 아이템 클릭 이벤트에 대한 콜백입니다.<br>
     * 위치, 아이템, 클릭된 뷰를 수신합니다.<br>
     */
    private var onItemClickListener: ((Int, ITEM, View) -> Unit)? = null

    /**
     * Callback for item long-click events.<br>
     * Receives position, item, and clicked view.<br><br>
     * 아이템 롱클릭 이벤트에 대한 콜백입니다.<br>
     * 위치, 아이템, 클릭된 뷰를 수신합니다.<br>
     */
    private var onItemLongClickListener: ((Int, ITEM, View) -> Unit)? = null

    /**
     * Operation queue for handling consecutive adapter operations.<br>
     * Ensures operations are executed in order and prevents conflicts.<br><br>
     * 연속적인 어댑터 작업을 처리하기 위한 작업 큐입니다.<br>
     * 작업이 순서대로 실행되고 충돌이 방지되도록 보장합니다.<br>
     */
    private val operationQueue = AdapterOperationQueue<ITEM>(
        getCurrentList = { currentList },
        submitList = { list, callback -> submitList(list, callback) }
    )

    /**
     * Abstract method to bind data to each ViewHolder.<br><br>
     * 각 ViewHolder에 데이터를 바인딩하는 추상 메서드입니다.<br>
     *
     * @param holder The ViewHolder to bind data to.<br><br>
     *               데이터를 바인딩할 ViewHolder.<br>
     *
     * @param position The position of the item in the list.<br><br>
     *                 리스트에서 아이템의 위치.<br>
     *
     * @param item The item to bind.<br><br>
     *             바인딩할 아이템.<br>
     */
    protected abstract fun onBindViewHolder(holder: VH, position: Int, item: ITEM)

    /**
     * onBindViewHolder for partial updates with payload support.<br>
     * By default performs full binding. Override to implement partial updates.<br><br>
     * payload를 지원하는 부분 업데이트를 위한 onBindViewHolder입니다.<br>
     * 기본적으로 전체 바인딩을 수행합니다. 부분 업데이트를 구현하려면 오버라이드하세요.<br>
     *
     * @param holder The ViewHolder to bind data to.<br><br>
     *               데이터를 바인딩할 ViewHolder.<br>
     *
     * @param position The position of the item in the list.<br><br>
     *                 리스트에서 아이템의 위치.<br>
     *
     * @param item The item to bind.<br><br>
     *             바인딩할 아이템.<br>
     *
     * @param payloads The list of payloads for partial updates.<br><br>
     *                 부분 업데이트를 위한 payload 리스트.<br>
     */
    protected open fun onBindViewHolder(holder: VH, position: Int, item: ITEM, payloads: List<Any>) {
        // By default performs full binding
        onBindViewHolder(holder, position, item)
    }

    /**
     * Returns the current item list set in the adapter.<br><br>
     * 현재 어댑터에 설정된 아이템 리스트를 반환합니다.<br>
     *
     * @return The current item list.<br><br>
     *         현재 아이템 리스트.<br>
     */
    public fun getItems(): List<ITEM> = currentList

    /**
     * Returns a mutable copy of the current item list.<br><br>
     * 현재 아이템 리스트의 복사본을 반환합니다.<br>
     *
     * @return A mutable copy of the current item list.<br><br>
     *         현재 아이템 리스트의 복사본.<br>
     */
    protected fun getMutableItemList(): MutableList<ITEM> = currentList.toMutableList()

    /**
     * Sets the item list.<br>
     * Cancels all pending queue operations and replaces with the new list.<br><br>
     * 아이템 리스트를 설정합니다.<br>
     * 기존 큐의 모든 작업을 취소하고 새로운 리스트로 교체합니다.<br>
     *
     * @param itemList The item list to set.<br><br>
     *                 설정할 아이템 리스트.<br>
     *
     * @param commitCallback Callback to be invoked after list update completes (nullable).<br><br>
     *                       리스트 갱신 완료 후 호출될 콜백 (null 가능).<br>
     */
    public fun setItems(itemList: List<ITEM>, commitCallback: (() -> Unit)? = null) {
        operationQueue.clearQueueAndExecute(AdapterOperationQueue.SetItemsOp(itemList, commitCallback))
    }

    /**
     * Adds an item to the end of the list.<br><br>
     * 리스트 끝에 아이템을 추가합니다.<br>
     *
     * @param item The item to add.<br><br>
     *             추가할 아이템.<br>
     *
     * @param commitCallback Callback to be invoked after list update completes (nullable).<br><br>
     *                       리스트 갱신 완료 후 호출될 콜백 (null 가능).<br>
     */
    public fun addItem(item: ITEM, commitCallback: (() -> Unit)? = null) {
        operationQueue.enqueueOperation(AdapterOperationQueue.AddItemOp(item, commitCallback))
    }

    /**
     * Adds an item at a specific position.<br><br>
     * 특정 위치에 아이템을 추가합니다.<br>
     *
     * @param position The position to add the item at.<br><br>
     *                 아이템을 추가할 위치.<br>
     *
     * @param item The item to add.<br><br>
     *             추가할 아이템.<br>
     *
     * @param commitCallback Callback to be invoked after list update completes (nullable).<br><br>
     *                       리스트 갱신 완료 후 호출될 콜백 (null 가능).<br>
     *
     * @return True if addition succeeded, false if position is invalid.<br><br>
     *         추가 성공 시 true, position이 유효하지 않으면 false.<br>
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
     * Adds multiple items to the end of the list.<br><br>
     * 리스트 끝에 여러 아이템을 추가합니다.<br>
     *
     * @param itemList The list of items to add.<br><br>
     *                 추가할 아이템 리스트.<br>
     *
     * @param commitCallback Callback to be invoked after list update completes (nullable).<br><br>
     *                       리스트 갱신 완료 후 호출될 콜백 (null 가능).<br>
     *
     * @return The number of items added.<br><br>
     *         추가된 아이템 수.<br>
     */
    public fun addItems(itemList: List<ITEM>, commitCallback: (() -> Unit)? = null): Int {
        if (itemList.isEmpty()) return 0
        operationQueue.enqueueOperation(AdapterOperationQueue.AddItemsOp(itemList, commitCallback))
        return itemList.size
    }

    /**
     * Adds multiple items at a specific position.<br><br>
     * 특정 위치에 여러 아이템을 추가합니다.<br>
     *
     * @param position The position to add items at.<br><br>
     *                 아이템들을 추가할 위치.<br>
     *
     * @param itemList The list of items to add.<br><br>
     *                 추가할 아이템 리스트.<br>
     *
     * @param commitCallback Callback to be invoked after list update completes (nullable).<br><br>
     *                       리스트 갱신 완료 후 호출될 콜백 (null 가능).<br>
     *
     * @return True if addition succeeded, false if position is invalid.<br><br>
     *         추가 성공 시 true, position이 유효하지 않으면 false.<br>
     */
    public fun addItems(position: Int, itemList: List<ITEM>, commitCallback: (() -> Unit)? = null): Boolean {
        if (itemList.isEmpty()) return true
        if (position < 0 || position > itemCount) {
            Logx.e("Cannot add items at position $position. Valid range: 0..$itemCount")
            return false
        }
        operationQueue.enqueueOperation(AdapterOperationQueue.AddItemsAtOp(position, itemList, commitCallback))
        return true
    }

    /**
     * Checks if the position is valid.<br><br>
     * 위치가 유효한지 확인합니다.<br>
     *
     * @param position The position to check.<br><br>
     *                 확인할 위치.<br>
     *
     * @return True if position is valid, false otherwise.<br><br>
     *         위치가 유효하면 true, 아니면 false.<br>
     */
    protected fun isPositionValid(position: Int): Boolean = (position > RecyclerView.NO_POSITION && position < itemCount)

    /**
     * Removes a specific item from the list.<br><br>
     * 리스트에서 특정 아이템을 제거합니다.<br>
     *
     * @param item The item to remove.<br><br>
     *             제거할 아이템.<br>
     *
     * @param commitCallback Callback to be invoked after list update completes (nullable).<br><br>
     *                       리스트 갱신 완료 후 호출될 콜백 (null 가능).<br>
     *
     * @return True if removal succeeded, false if item not found.<br><br>
     *         제거 성공 시 true, 아이템이 없으면 false.<br>
     */
    public fun removeItem(item: ITEM, commitCallback: (() -> Unit)? = null): Boolean {
        if (!currentList.contains(item)) {
            Logx.e("Item not found in the list")
            return false
        }
        operationQueue.enqueueOperation(AdapterOperationQueue.RemoveItemOp(item, commitCallback))
        return true
    }

    /**
     * Removes the item at a specific position.<br><br>
     * 특정 위치의 아이템을 제거합니다.<br>
     *
     * @param position The position of the item to remove.<br><br>
     *                 제거할 아이템의 위치.<br>
     *
     * @param commitCallback Callback to be invoked after list update completes (nullable).<br><br>
     *                       리스트 갱신 완료 후 호출될 콜백 (null 가능).<br>
     *
     * @return True if removal succeeded, false if position is invalid.<br><br>
     *         제거 성공 시 true, position이 유효하지 않으면 false.<br>
     */
    public fun removeAt(position: Int, commitCallback: (() -> Unit)? = null): Boolean {
        if (position < 0 || position >= itemCount) {
            Logx.e("Cannot remove item at position $position. Valid range: 0 until $itemCount")
            return false
        }
        operationQueue.enqueueOperation(AdapterOperationQueue.RemoveAtOp(position, commitCallback))
        return true
    }

    /**
     * Removes all items from the list.<br><br>
     * 리스트의 모든 아이템을 제거합니다.<br>
     *
     * @param commitCallback Callback to be invoked after list update completes (nullable).<br><br>
     *                       리스트 갱신 완료 후 호출될 콜백 (null 가능).<br>
     */
    public fun removeAll(commitCallback: (() -> Unit)? = null) {
        operationQueue.enqueueOperation(AdapterOperationQueue.ClearItemsOp(commitCallback))
    }

    /**
     * Moves an item from one position to another.<br><br>
     * 아이템을 한 위치에서 다른 위치로 이동합니다.<br>
     *
     * @param fromPosition The current position of the item to move.<br><br>
     *                     이동할 아이템의 현재 위치.<br>
     *
     * @param toPosition The target position to move the item to.<br><br>
     *                   아이템을 이동할 목표 위치.<br>
     *
     * @param commitCallback Callback to be invoked after list update completes (nullable).<br><br>
     *                       리스트 갱신 완료 후 호출될 콜백 (null 가능).<br>
     *
     * @return True if move succeeded, false if any position is invalid.<br><br>
     *         이동 성공 시 true, position이 유효하지 않으면 false.<br>
     */
    public fun moveItem(fromPosition: Int, toPosition: Int, commitCallback: (() -> Unit)? = null): Boolean {
        if (fromPosition == toPosition) return true
        if (fromPosition < 0 || fromPosition >= itemCount) {
            Logx.e("Invalid fromPosition $fromPosition. Valid range: 0 until $itemCount")
            return false
        }
        if (toPosition < 0 || toPosition >= itemCount) {
            Logx.e("Invalid toPosition $toPosition. Valid range: 0 until $itemCount")
            return false
        }
        operationQueue.enqueueOperation(AdapterOperationQueue.MoveItemOp(fromPosition, toPosition, commitCallback))
        return true
    }

    /**
     * Replaces the item at a specific position with a new item.<br><br>
     * 특정 위치의 아이템을 새로운 아이템으로 교체합니다.<br>
     *
     * @param position The position of the item to replace.<br><br>
     *                 교체할 아이템의 위치.<br>
     *
     * @param item The new item to replace with.<br><br>
     *             교체할 새로운 아이템.<br>
     *
     * @param commitCallback Callback to be invoked after list update completes (nullable).<br><br>
     *                       리스트 갱신 완료 후 호출될 콜백 (null 가능).<br>
     *
     * @return True if replacement succeeded, false if position is invalid.<br><br>
     *         교체 성공 시 true, position이 유효하지 않으면 false.<br>
     */
    public fun replaceItemAt(position: Int, item: ITEM, commitCallback: (() -> Unit)? = null): Boolean {
        if (position < 0 || position >= itemCount) {
            Logx.e("Invalid position $position. Valid range: 0 until $itemCount")
            return false
        }
        operationQueue.enqueueOperation(AdapterOperationQueue.ReplaceItemAtOp(position, item, commitCallback))
        return true
    }

    /**
     * Sets the item click listener.<br><br>
     * 아이템 클릭 리스너를 설정합니다.<br>
     *
     * @param listener The callback for click events (receives position, item, and view).<br><br>
     *                 클릭 이벤트 콜백 (위치, 아이템, 뷰를 수신).<br>
     */
    public fun setOnItemClickListener(listener: (Int, ITEM, View) -> Unit) { onItemClickListener = listener }

    /**
     * Sets the item long-click listener.<br><br>
     * 아이템 롱클릭 리스너를 설정합니다.<br>
     *
     * @param listener The callback for long-click events (receives position, item, and view).<br><br>
     *                 롱클릭 이벤트 콜백 (위치, 아이템, 뷰를 수신).<br>
     */
    public fun setOnItemLongClickListener(listener: (Int, ITEM, View) -> Unit) { onItemLongClickListener = listener }

    /**
     * Binds data to the ViewHolder at the specified position.<br>
     * Sets up click listeners and delegates to abstract onBindViewHolder.<br><br>
     * 지정된 위치의 ViewHolder에 데이터를 바인딩합니다.<br>
     * 클릭 리스너를 설정하고 추상 onBindViewHolder에 위임합니다.<br>
     *
     * @param holder The ViewHolder to bind.<br><br>
     *               바인딩할 ViewHolder.<br>
     *
     * @param position The position of the item.<br><br>
     *                 아이템의 위치.<br>
     */
    override fun onBindViewHolder(holder: VH, position: Int) {
        if (!isPositionValid(position)) {
            Logx.e("Invalid position: $position, item count: $itemCount")
            return
        }

        val item = getItem(position)

        holder.itemView.apply {
            setOnClickListener { view -> onItemClickListener?.invoke(position, item, view) }
            setOnLongClickListener { view ->
                onItemLongClickListener?.let {
                    it.invoke(position, item, view)
                    true
                } ?: false
            }
        }

        onBindViewHolder(holder, position, item)
    }

    /**
     * Binds data to the ViewHolder with payload support.<br>
     * Performs full binding if payloads is empty, otherwise partial update.<br><br>
     * payload를 지원하여 ViewHolder에 데이터를 바인딩합니다.<br>
     * payloads가 비어있으면 전체 바인딩을 수행하고, 그렇지 않으면 부분 업데이트를 수행합니다.<br>
     *
     * @param holder The ViewHolder to bind.<br><br>
     *               바인딩할 ViewHolder.<br>
     *
     * @param position The position of the item.<br><br>
     *                 아이템의 위치.<br>
     *
     * @param payloads The list of payloads for partial updates.<br><br>
     *                 부분 업데이트를 위한 payload 리스트.<br>
     */
    override fun onBindViewHolder(holder: VH, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            // Full binding if no payloads
            onBindViewHolder(holder, position)
        } else {
            // Partial update if payloads exist
            if (!isPositionValid(position)) {
                Logx.e("Invalid position: $position, item count: $itemCount")
                return
            }

            val item = getItem(position)
            onBindViewHolder(holder, position, item, payloads)
        }
    }

    /**
     * Called when a ViewHolder is recycled.<br>
     * Clears the view cache if the holder is a BaseRcvViewHolder.<br><br>
     * ViewHolder가 재활용될 때 호출됩니다.<br>
     * holder가 BaseRcvViewHolder인 경우 뷰 캐시를 정리합니다.<br>
     *
     * @param holder The ViewHolder being recycled.<br><br>
     *               재활용되는 ViewHolder.<br>
     */
    override fun onViewRecycled(holder: VH) {
        super.onViewRecycled(holder)
        if(holder is BaseRcvViewHolder){
            holder.clearViewCache()
        }
    }
}