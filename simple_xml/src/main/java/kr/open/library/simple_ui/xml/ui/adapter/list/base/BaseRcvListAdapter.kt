package kr.open.library.simple_ui.xml.ui.adapter.list.base

import android.view.View
import android.view.ViewGroup
import androidx.annotation.MainThread
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kr.open.library.simple_ui.core.extensions.trycatch.safeCatch
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.xml.ui.adapter.common.AdapterCommonClickData
import kr.open.library.simple_ui.xml.ui.adapter.common.AdapterCommonDataLogic
import kr.open.library.simple_ui.xml.ui.adapter.common.imp.AdapterClickable
import kr.open.library.simple_ui.xml.ui.adapter.common.imp.AdapterReadApi
import kr.open.library.simple_ui.xml.ui.adapter.common.imp.AdapterWriteApi
import kr.open.library.simple_ui.xml.ui.adapter.common.thread.assertAdapterMainThread
import kr.open.library.simple_ui.xml.ui.adapter.list.base.diffutil.RcvListDiffUtilCallBack
import kr.open.library.simple_ui.xml.ui.adapter.list.base.queue.AdapterOperationQueue
import kr.open.library.simple_ui.xml.ui.adapter.list.base.queue.QueueOverflowPolicy
import kr.open.library.simple_ui.xml.ui.adapter.list.base.result.ListAdapterResult
import kr.open.library.simple_ui.xml.ui.adapter.list.base.result.toAdapterDropReason
import kr.open.library.simple_ui.xml.ui.adapter.list.base.result.toListAdapterResult
import kr.open.library.simple_ui.xml.ui.adapter.viewholder.BaseRcvViewHolder

/**
 * Base RecyclerView ListAdapter implementation with queue-based operations.<br>
 * 큐 기반 연산을 제공하는 기본 RecyclerView ListAdapter 구현입니다.<br>
 * Provides comprehensive item management and click handling functionality.<br><br>
 * 아이템 관리와 클릭 처리 기능을 포괄적으로 제공합니다.<br>
 *
 * Features:<br>
 * 주요 기능:<br>
 * - DiffUtil-based efficient list updates<br>
 * - DiffUtil 기반의 효율적인 리스트 업데이트를 지원합니다.<br>
 * - Queue-based consecutive operations handling<br>
 * - 큐 기반 연속 연산 처리를 지원합니다.<br>
 * - Item click and long-click listener support<br>
 * - 아이템 클릭/롱클릭 리스너를 지원합니다.<br>
 * - Add, remove, move, replace item operations<br>
 * - 아이템 추가/제거/이동/교체 연산을 지원합니다.<br>
 * - Partial update support via payloads<br>
 * - payload 기반 부분 업데이트를 지원합니다.<br>
 * - Automatic ViewHolder cache clearing<br><br>
 * - ViewHolder 캐시 자동 정리를 지원합니다.<br>
 *
 * Usage example:<br>
 * 사용 예시:<br>
 * ```kotlin
 * class MyAdapter : BaseRcvListAdapter<MyItem, MyViewHolder>(MyDiffUtil()) {
 *     override fun createViewHolderInternal(parent: ViewGroup, viewType: Int): MyViewHolder {
 *         return MyViewHolder(LayoutInflater.from(parent.context)
 *             .inflate(R.layout.item_layout, parent, false))
 *     }
 *
 *     override fun onBindViewHolder(holder: MyViewHolder, item: MyItem, position: Int) {
 *         holder.bind(item)
 *     }
 * }
 * ```
 * <br>
 * 예시 코드를 참고해 구현 패턴을 적용할 수 있습니다.<br>
 *
 * @param ITEM The type of items in the list.<br><br>
 *             리스트의 아이템 타입입니다.<br>
 *
 * @param VH The type of ViewHolder.<br><br>
 *             ViewHolder 타입입니다.<br>
 *
 * @param listDiffUtil DiffUtil callback for comparing items.<br><br>
 *             아이템 비교를 위한 DiffUtil 콜백입니다.<br>
 *
 * @see RcvListDiffUtilCallBack For the DiffUtil callback implementation.<br><br>
 *      DiffUtil 콜백 구현은 RcvListDiffUtilCallBack을 참고하세요.<br>
 *
 * @see AdapterOperationQueue For the operation queue implementation.<br><br>
 *      연산 큐 구현은 AdapterOperationQueue를 참고하세요.<br>
 */
public abstract class BaseRcvListAdapter<ITEM, VH : RecyclerView.ViewHolder>(
    listDiffUtil: RcvListDiffUtilCallBack<ITEM>,
) : ListAdapter<ITEM, VH>(listDiffUtil),
    AdapterReadApi<ITEM>,
    AdapterWriteApi<ITEM, ListAdapterResult>,
    AdapterClickable<ITEM, VH> {
    private val commonDataLogic = AdapterCommonDataLogic<ITEM>()

    private val clickData = AdapterCommonClickData<ITEM, VH>()

    /**
     * Operation queue for handling consecutive adapter operations.<br>
     * 연속된 adapter 연산을 처리하는 operation queue입니다.<br>
     * Ensures operations are executed in order and prevents conflicts.<br><br>
     * 연산 순서를 보장하고 충돌을 방지합니다.<br>
     */
    private val operationQueue = AdapterOperationQueue<ITEM>(
        getCurrentList = { currentList },
        applyList = { _, _, updatedList, callback -> submitList(updatedList, callback) },
    )

    /**
     * Sets queue overflow policy and max pending size.<br><br>
     * 큐 오버플로 정책과 최대 대기 크기를 설정합니다.<br>
     *
     * @param maxPending Maximum number of pending operations allowed.<br><br>
     *             허용되는 최대 대기 연산 수입니다.<br>
     *
     * @param overflowPolicy Overflow handling policy when queue is full.<br><br>
     *             큐가 가득 찼을 때 적용할 오버플로 정책입니다.<br>
     */
    public fun setQueuePolicy(maxPending: Int, overflowPolicy: QueueOverflowPolicy) {
        assertMainThread("BaseRcvListAdapter.setQueuePolicy")
        operationQueue.setQueuePolicy(maxPending, overflowPolicy)
    }

    /**
     * Creates a ViewHolder and attaches click listeners once.<br><br>
     * ViewHolder를 생성하고 클릭 리스너를 1회 연결합니다.<br>
     */
    public final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val holder = createViewHolderInternal(parent, viewType)
        bindClickListener(holder)
        return holder
    }

    /**
     * Attaches click/long-click listeners once and keeps adapter index mapping as-is.<br><br>
     * 클릭/롱클릭 리스너를 1회 부착하고 adapter 인덱스를 그대로 사용합니다.<br>
     */
    private fun bindClickListener(holder: VH) {
        clickData.attachClickListeners(
            holder = holder,
            positionMapper = { adapterPosition -> adapterPosition },
            itemProvider = { position -> getItemOrNull(position) },
        )
        clickData.attachLongClickListeners(
            holder = holder,
            positionMapper = { adapterPosition -> adapterPosition },
            itemProvider = { position -> getItemOrNull(position) },
        )
    }

    /**
     * Creates a ViewHolder for the given parent and view type.<br><br>
     * 부모 View와 viewType에 맞는 ViewHolder를 생성합니다.<br>
     */
    protected abstract fun createViewHolderInternal(parent: ViewGroup, viewType: Int): VH

    /**
     * onBindViewHolder for partial updates with payload support.<br>
     * payload를 지원하는 부분 업데이트용 onBindViewHolder입니다.<br>
     * By default performs full binding. Override to implement partial updates.<br><br>
     * 기본 구현은 전체 바인딩이며, 부분 업데이트가 필요하면 오버라이드합니다.<br>
     *
     * @param holder The ViewHolder to bind data to.<br><br>
     *             데이터를 바인딩할 ViewHolder입니다.<br>
     *
     * @param position The position of the item in the list.<br><br>
     *             리스트에서 아이템 위치입니다.<br>
     *
     * @param item The item to bind.<br><br>
     *             바인딩할 아이템입니다.<br>
     *
     * @param payloads The list of payloads for partial updates.<br><br>
     *             부분 업데이트를 위한 payload 목록입니다.<br>
     */
    protected open fun onBindViewHolder(holder: VH, item: ITEM, position: Int, payloads: List<Any>) {
        // By default performs full binding
        onBindViewHolder(holder, item, position)
    }

    /**
     * Returns the current item list set in the adapter.<br><br>
     * 현재 adapter에 설정된 아이템 리스트를 반환합니다.<br>
     *
     * @return The current item list.<br><br>
     *         현재 아이템 리스트를 반환합니다.<br>
     */
    @MainThread
    public override fun getItems(): List<ITEM> = runOnMainThread("BaseRcvListAdapter.getItems") { currentList }

    /**
     * Returns the index of [item] within the current list.<br><br>
     * 현재 리스트에서 [item]의 인덱스를 반환합니다.<br>
     *
     * @param item Target item to find.<br><br>
     *             조회할 대상 아이템입니다.<br>
     * @return The index if found, otherwise -1.<br><br>
     *         아이템이 존재하면 인덱스, 없으면 -1을 반환합니다.<br>
     */
    @MainThread
    override fun getItemPosition(item: ITEM): Int =
        runOnMainThread("BaseRcvListAdapter.getItemPosition") { currentList.indexOf(item) }

    /**
     * Returns a mutable copy of the current item list.<br>
     * 현재 아이템 리스트의 가변 복사본을 반환합니다.<br>
     * **Warning**: This is a snapshot copy. Mutations do NOT affect the adapter state.<br>
     * 경고: 이 리스트는 스냅샷 복사본이며 변경해도 adapter 상태에 반영되지 않습니다.<br>
     * To update the adapter, pass the modified list to [setItems] explicitly.<br><br>
     * adapter를 갱신하려면 수정된 리스트를 [setItems]에 명시적으로 전달해야 합니다.<br>
     *
     * @return A mutable copy of the current item list.<br><br>
     *         현재 아이템 리스트의 가변 복사본을 반환합니다.<br>
     */
    @MainThread
    public override fun getMutableItemList(): MutableList<ITEM> =
        runOnMainThread("BaseRcvListAdapter.getMutableItemList") { currentList.toMutableList() }

    /**
     * Sets the item list.<br>
     * 아이템 리스트를 설정합니다.<br>
     * Cancels all pending queue operations and replaces with the new list.<br><br>
     * 대기 중인 큐 연산을 취소하고 새 리스트로 교체합니다.<br>
     *
     * @param items The item list to set.<br><br>
     *             설정할 아이템 리스트입니다.<br>
     *
     * @param onResult Callback invoked when the queue operation reaches terminal state (nullable).<br><br>
     *             큐 연산이 종료 상태에 도달하면 호출되는 결과 콜백입니다(null 가능).<br>
     */
    @MainThread
    public override fun setItems(items: List<ITEM>, onResult: ((ListAdapterResult) -> Unit)?) {
        assertMainThread("BaseRcvListAdapter.setItems")
        operationQueue.clearQueueAndExecute(AdapterOperationQueue.SetItemsOp(items, toOperationCallback(onResult)))
    }

    /**
     * Adds an item to the end of the list.<br><br>
     * 리스트 끝에 아이템을 추가합니다.<br>
     *
     * @param item The item to add.<br><br>
     *             추가할 아이템입니다.<br>
     *
     * @param onResult Callback invoked when the queue operation reaches terminal state (nullable).<br><br>
     *             큐 연산이 종료 상태에 도달하면 호출되는 결과 콜백입니다(null 가능).<br>
     */
    @MainThread
    public override fun addItem(item: ITEM, onResult: ((ListAdapterResult) -> Unit)?) {
        assertMainThread("BaseRcvListAdapter.addItem")
        operationQueue.enqueueOperation(AdapterOperationQueue.AddItemOp(item, toOperationCallback(onResult)))
    }

    /**
     * Adds an item at a specific position.<br><br>
     * 지정한 위치에 아이템을 추가합니다.<br>
     *
     * @param position The position to add the item at.<br><br>
     *             아이템을 추가할 위치입니다.<br>
     *
     * @param item The item to add.<br><br>
     *             추가할 아이템입니다.<br>
     *
     * @param onResult Callback invoked when the queue operation reaches terminal state (nullable).<br><br>
     *             큐 연산이 종료 상태에 도달하면 호출되는 결과 콜백입니다(null 가능).<br>
     */
    @MainThread
    public override fun addItemAt(position: Int, item: ITEM, onResult: ((ListAdapterResult) -> Unit)?) {
        val failure = commonDataLogic.validateAddItemAt(position, itemCount)
        if (failure != null) {
            runResultCallback(failure.toListAdapterResult(), onResult)
            return
        }
        operationQueue.enqueueOperation(AdapterOperationQueue.AddItemAtOp(position, item, toOperationCallback(onResult)))
    }

    /**
     * Adds multiple items to the end of the list.<br><br>
     * 리스트 끝에 여러 아이템을 추가합니다.<br>
     *
     * @param items The list of items to add.<br><br>
     *             추가할 아이템 목록입니다.<br>
     *
     * @param onResult Callback invoked when the queue operation reaches terminal state (nullable).<br><br>
     *             큐 연산이 종료 상태에 도달하면 호출되는 결과 콜백입니다(null 가능).<br>
     */
    @MainThread
    public override fun addItems(items: List<ITEM>, onResult: ((ListAdapterResult) -> Unit)?) {
        val failure = commonDataLogic.validateAddItems(items)
        if (failure != null) {
            runResultCallback(failure.toListAdapterResult(), onResult)
            return
        }
        operationQueue.enqueueOperation(AdapterOperationQueue.AddItemsOp(items, toOperationCallback(onResult)))
    }

    /**
     * Inserts multiple items at a specific position.<br><br>
     * 지정한 위치에 여러 아이템을 삽입합니다.<br>
     *
     * @param position The position to insert items at.<br><br>
     *             아이템들을 삽입할 위치입니다.<br>
     *
     * @param items The list of items to insert.<br><br>
     *             삽입할 아이템 목록입니다.<br>
     *
     * @param onResult Callback invoked when the queue operation reaches terminal state (nullable).<br><br>
     *             큐 연산이 종료 상태에 도달하면 호출되는 결과 콜백입니다(null 가능).<br>
     */
    @MainThread
    public override fun addItemsAt(position: Int, items: List<ITEM>, onResult: ((ListAdapterResult) -> Unit)?) {
        val failure = commonDataLogic.validateAddItemsAt(items, position, itemCount)
        if (failure != null) {
            runResultCallback(failure.toListAdapterResult(), onResult)
            return
        }
        operationQueue.enqueueOperation(AdapterOperationQueue.AddItemsAtOp(position, items, toOperationCallback(onResult)))
    }

    /**
     * Checks if the position is valid.<br><br>
     * position 유효성을 확인합니다.<br>
     *
     * @param position The position to check.<br><br>
     *             확인할 위치입니다.<br>
     *
     * @return True if position is valid, false otherwise.<br><br>
     *         위치가 유효하면 true, 아니면 false를 반환합니다.<br>
     */
    protected fun isPositionValid(position: Int): Boolean = commonDataLogic.isPositionValid(position, itemCount)

    /**
     * Removes a specific item from the list.<br><br>
     * 리스트에서 특정 아이템을 제거합니다.<br>
     *
     * @param item The item to remove.<br><br>
     *             제거할 아이템입니다.<br>
     *
     * @param onResult Callback invoked when the queue operation reaches terminal state (nullable).<br><br>
     *             큐 연산이 종료 상태에 도달하면 호출되는 결과 콜백입니다(null 가능).<br>
     */
    @MainThread
    public override fun removeItem(item: ITEM, onResult: ((ListAdapterResult) -> Unit)?) {
        val failure = commonDataLogic.validateRemoveItem(item, currentList)
        if (failure != null) {
            runResultCallback(failure.toListAdapterResult(), onResult)
            return
        }
        operationQueue.enqueueOperation(AdapterOperationQueue.RemoveItemOp(item, toOperationCallback(onResult)))
    }

    /**
     * Removes the item at a specific position.<br><br>
     * 지정한 위치의 아이템을 제거합니다.<br>
     *
     * @param position The position of the item to remove.<br><br>
     *             제거할 아이템의 위치입니다.<br>
     *
     * @param onResult Callback invoked when the queue operation reaches terminal state (nullable).<br><br>
     *             큐 연산이 종료 상태에 도달하면 호출되는 결과 콜백입니다(null 가능).<br>
     */
    @MainThread
    public override fun removeAt(position: Int, onResult: ((ListAdapterResult) -> Unit)?) {
        val failure = commonDataLogic.validateRemoveItemAt(position, itemCount)
        if (failure != null) {
            runResultCallback(failure.toListAdapterResult(), onResult)
            return
        }
        operationQueue.enqueueOperation(AdapterOperationQueue.RemoveAtOp(position, toOperationCallback(onResult)))
    }

    /**
     * Removes all items from the list.<br><br>
     * 리스트의 모든 아이템을 제거합니다.<br>
     *
     * @param onResult Callback invoked when the queue operation reaches terminal state (nullable).<br><br>
     *             큐 연산이 종료 상태에 도달하면 호출되는 결과 콜백입니다(null 가능).<br>
     */
    @MainThread
    public override fun removeAll(onResult: ((ListAdapterResult) -> Unit)?) {
        assertMainThread("BaseRcvListAdapter.removeAll")
        operationQueue.enqueueOperation(AdapterOperationQueue.ClearItemsOp(toOperationCallback(onResult)))
    }

    /**
     * Removes matching items from current list.<br>
     * 현재 리스트에서 일치하는 아이템을 제거합니다.<br>
     * This method uses best-effort semantics and removes only existing matches.<br><br>
     * best-effort 방식으로 실제 존재하는 항목만 제거합니다.<br>
     *
     * @param items Items requested to remove.<br><br>
     *             제거 요청한 아이템 목록입니다.<br>
     * @param onResult Callback invoked when the queue operation reaches terminal state (nullable).<br><br>
     *             큐 연산이 종료 상태에 도달하면 호출되는 결과 콜백입니다(null 가능).<br>
     */
    @MainThread
    override fun removeItems(items: List<ITEM>, onResult: ((ListAdapterResult) -> Unit)?) {
        val failure = commonDataLogic.validateRemoveItems(items, currentList)
        if (failure != null) {
            runResultCallback(failure.toListAdapterResult(), onResult)
            return
        }
        operationQueue.enqueueOperation(AdapterOperationQueue.RemoveItemsOp(items, toOperationCallback(onResult)))
    }

    /**
     * Removes a contiguous range using start index and count.<br><br>
     * 시작 인덱스와 개수 기준으로 연속 구간을 제거합니다.<br>
     *
     * Example: start=3, count=2 removes indices 3 and 4.<br><br>
     * 예: start=3, count=2이면 인덱스 3, 4를 제거합니다.<br>
     *
     * @param start Start index of range to remove.<br><br>
     *             제거 범위의 시작 인덱스입니다.<br>
     * @param count Number of items to remove from start.<br><br>
     *             start부터 제거할 아이템 개수입니다.<br>
     * @param onResult Callback invoked when the queue operation reaches terminal state (nullable).<br><br>
     *             큐 연산이 종료 상태에 도달하면 호출되는 결과 콜백입니다(null 가능).<br>
     */
    @MainThread
    override fun removeRange(start: Int, count: Int, onResult: ((ListAdapterResult) -> Unit)?) {
        val failure = commonDataLogic.validateRemoveRange(start, count, itemCount)
        if (failure != null) {
            runResultCallback(failure.toListAdapterResult(), onResult)
            return
        }
        operationQueue.enqueueOperation(AdapterOperationQueue.RemoveRangeOp(start, count, toOperationCallback(onResult)))
    }

    /**
     * Moves an item from one position to another.<br><br>
     * 아이템을 한 위치에서 다른 위치로 이동합니다.<br>
     *
     * @param fromPosition The current position of the item to move.<br><br>
     *             이동할 아이템의 현재 위치입니다.<br>
     *
     * @param toPosition The target position to move the item to.<br><br>
     *             아이템을 이동할 목표 위치입니다.<br>
     *
     * @param onResult Callback invoked when the queue operation reaches terminal state (nullable).<br><br>
     *             큐 연산이 종료 상태에 도달하면 호출되는 결과 콜백입니다(null 가능).<br>
     */
    @MainThread
    public override fun moveItem(fromPosition: Int, toPosition: Int, onResult: ((ListAdapterResult) -> Unit)?) {
        val failure = commonDataLogic.validateMoveItem(fromPosition, toPosition, itemCount)
        if (failure != null) {
            runResultCallback(failure.toListAdapterResult(), onResult)
            return
        }
        operationQueue.enqueueOperation(AdapterOperationQueue.MoveItemOp(fromPosition, toPosition, toOperationCallback(onResult)))
    }

    /**
     * Replaces the item at a specific position with a new item.<br><br>
     * 지정한 위치의 아이템을 새 아이템으로 교체합니다.<br>
     *
     * @param position The position of the item to replace.<br><br>
     *             교체할 아이템의 위치입니다.<br>
     *
     * @param item The new item to replace with.<br><br>
     *             교체할 새 아이템입니다.<br>
     *
     * @param onResult Callback invoked when the queue operation reaches terminal state (nullable).<br><br>
     *             큐 연산이 종료 상태에 도달하면 호출되는 결과 콜백입니다(null 가능).<br>
     */
    @MainThread
    public override fun replaceItemAt(position: Int, item: ITEM, onResult: ((ListAdapterResult) -> Unit)?) {
        val failure = commonDataLogic.validateReplaceItemAt(position, itemCount)
        if (failure != null) {
            runResultCallback(failure.toListAdapterResult(), onResult)
            return
        }
        operationQueue.enqueueOperation(AdapterOperationQueue.ReplaceItemAtOp(position, item, toOperationCallback(onResult)))
    }

    /**
     * Abstract bind contract for subclasses.<br><br>
     * 하위 클래스가 구현해야 하는 바인딩 계약입니다.<br>
     *
     * @param holder The ViewHolder to bind.<br><br>
     *             바인딩 대상 ViewHolder입니다.<br>
     * @param item Item resolved for the given position.<br><br>
     *             item 파라미터입니다.<br>
     * @param position Current adapter position.<br><br>
     *             현재 adapter 위치입니다.<br>
     */
    protected abstract fun onBindViewHolder(holder: VH, item: ITEM, position: Int)

    /**
     * Safely returns the item at the position or null.<br><br>
     * position의 아이템을 안전하게 조회하고 없으면 null을 반환합니다.<br>
     */
    @MainThread
    public override fun getItemOrNull(position: Int): ITEM? =
        runOnMainThread("BaseRcvListAdapter.getItemOrNull") { currentList.getOrNull(position) }

    /**
     * Sets the item click listener.<br><br>
     * 아이템 클릭 리스너를 설정합니다.<br>
     *
     * @param listener The callback for click events (receives position, item, and view).<br><br>
     *             클릭 이벤트 콜백입니다(position, item, view 전달).<br>
     */
    @MainThread
    public override fun setOnItemClickListener(listener: (Int, ITEM, View) -> Unit) {
        assertMainThread("BaseRcvListAdapter.setOnItemClickListener")
        clickData.onItemClickListener = listener
    }

    /**
     * Sets the item long-click listener.<br><br>
     * 아이템 롱클릭 리스너를 설정합니다.<br>
     *
     * @param listener The callback for long-click events (receives position, item, and view).<br><br>
     *             롱클릭 이벤트 콜백입니다(position, item, view 전달).<br>
     */
    @MainThread
    public override fun setOnItemLongClickListener(listener: (Int, ITEM, View) -> Unit) {
        assertMainThread("BaseRcvListAdapter.setOnItemLongClickListener")
        clickData.onItemLongClickListener = listener
    }

    /**
     * Binds data to the ViewHolder at the specified position.<br><br>
     * 지정한 위치의 ViewHolder에 데이터를 바인딩합니다.<br>
     *
     * @param holder The ViewHolder to bind.<br><br>
     *             바인딩 대상 ViewHolder입니다.<br>
     *
     * @param position The position of the item.<br><br>
     *             아이템 위치입니다.<br>
     */
    override fun onBindViewHolder(holder: VH, position: Int) {
        if (!isPositionValid(position)) {
            Logx.e("Invalid position: $position, item count: $itemCount")
            return
        }
        onBindViewHolder(holder, getItem(position), position)
    }

    /**
     * Binds data to the ViewHolder with payload support.<br>
     * payload를 사용해 ViewHolder 데이터를 바인딩합니다.<br>
     * Performs full binding if payloads is empty, otherwise partial update.<br><br>
     * payload가 비어 있으면 전체 바인딩, 아니면 부분 업데이트를 수행합니다.<br>
     *
     * @param holder The ViewHolder to bind.<br><br>
     *             바인딩 대상 ViewHolder입니다.<br>
     *
     * @param position The position of the item.<br><br>
     *             아이템 위치입니다.<br>
     *
     * @param payloads The list of payloads for partial updates.<br><br>
     *             부분 업데이트를 위한 payload 목록입니다.<br>
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

            getItemOrNull(position)?.let { onBindViewHolder(holder, it, position, payloads) }
        }
    }

    /**
     * Asserts Main thread and then executes [block].<br><br>
     * 메인 스레드를 검증한 뒤 [block]을 실행합니다.<br>
     */
    private inline fun <T> runOnMainThread(apiName: String, block: () -> T): T {
        assertAdapterMainThread(apiName)
        return block()
    }

    /**
     * Asserts that the current call is on Main thread.<br><br>
     * 현재 호출이 메인 스레드인지 검증합니다.<br>
     */
    private fun assertMainThread(apiName: String) {
        assertAdapterMainThread(apiName)
    }

    /**
     * Safely invokes a result callback with the supplied result value.<br><br>
     * 전달된 결과 값으로 결과 콜백을 안전하게 호출합니다.<br>
     */
    private fun runResultCallback(result: ListAdapterResult, onResult: ((ListAdapterResult) -> Unit)?) = safeCatch {
        onResult?.invoke(result)
    }

    /**
     * Creates an operation callback that maps queue terminal states to public list results.<br><br>
     * 큐 종료 상태를 공개 list 결과로 변환하는 연산 콜백을 생성합니다.<br>
     * Returns null when [onResult] is null, so that no callback is registered in the queue.<br><br>
     * [onResult]가 null이면 null을 반환하여 큐에 콜백을 등록하지 않습니다.<br>
     */
    private fun toOperationCallback(onResult: ((ListAdapterResult) -> Unit)?): ((AdapterOperationQueue.OperationTerminalState) -> Unit)? {
        if (onResult == null) return null
        return { terminalState -> runResultCallback(terminalState.toListAdapterResult(), onResult) }
    }

    /**
     * Maps queue terminal states to public list adapter results.<br><br>
     * 큐 종료 상태를 공개 list adapter 결과로 변환합니다.<br>
     */
    private fun AdapterOperationQueue.OperationTerminalState.toListAdapterResult(): ListAdapterResult = when (this) {
        is AdapterOperationQueue.OperationTerminalState.Applied -> ListAdapterResult.Applied
        is AdapterOperationQueue.OperationTerminalState.Dropped -> ListAdapterResult.Failed.Dropped(reason.toAdapterDropReason())
        is AdapterOperationQueue.OperationTerminalState.ExecutionError -> ListAdapterResult.Failed.ExecutionError(cause)
    }

    /**
     * Called when a ViewHolder is recycled.<br>
     * ViewHolder가 재활용될 때 호출됩니다.<br>
     * Clears the view cache if the holder is a BaseRcvViewHolder.<br><br>
     * holder가 BaseRcvViewHolder이면 뷰 캐시를 정리합니다.<br>
     *
     * @param holder The ViewHolder being recycled.<br><br>
     *               재활용되는 ViewHolder입니다.<br>
     */
    override fun onViewRecycled(holder: VH) {
        super.onViewRecycled(holder)
        if (holder is BaseRcvViewHolder) {
            holder.clearViewCache()
        }
    }
}
