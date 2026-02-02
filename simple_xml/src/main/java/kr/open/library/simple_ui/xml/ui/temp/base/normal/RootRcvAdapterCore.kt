package kr.open.library.simple_ui.xml.ui.temp.base.normal

import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.xml.BuildConfig
import kr.open.library.simple_ui.xml.ui.adapter.queue.QueueDebugEvent
import kr.open.library.simple_ui.xml.ui.adapter.queue.QueueOverflowPolicy
import kr.open.library.simple_ui.xml.ui.temp.base.AdapterOperationFailure
import kr.open.library.simple_ui.xml.ui.temp.base.AdapterOperationFailureInfo
import kr.open.library.simple_ui.xml.ui.temp.base.AdapterThreadCheckMode
import kr.open.library.simple_ui.xml.ui.temp.base.internal.AdapterClickBinder
import kr.open.library.simple_ui.xml.ui.temp.base.internal.AdapterOperationQueueCoordinator
import kr.open.library.simple_ui.xml.ui.temp.base.internal.AdapterOperationQueueCoordinator.OperationResult
import kr.open.library.simple_ui.xml.ui.temp.base.operation.AdapterListOperations
import kr.open.library.simple_ui.xml.ui.temp.base.operation.ListOperationResult
import kr.open.library.simple_ui.xml.ui.temp.base.operation.PositionInfo
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * Core RecyclerView.Adapter base with queued operations and optional DiffUtil.<br><br>
 * 연산 큐와 DiffUtil 옵션을 제공하는 RecyclerView.Adapter 코어입니다.<br>
 * 아이템 변환은 기본적으로 백그라운드에서 처리하며 필요 시 메인 스레드로 전환할 수 있습니다.<br>
 */
abstract class RootRcvAdapterCore<ITEM : Any, VH : RecyclerView.ViewHolder>(
    /**
     * Enables DiffUtil for normal adapter updates; keep OFF for very large replacements and enable for incremental changes.<br><br>
     * 일반 어댑터 업데이트에서 DiffUtil을 활성화하며, 대량 치환은 OFF, 점진 변경은 ON을 권장합니다.<br>
     * Decide this at creation time for a consistent policy.<br><br>
     * 일관된 정책을 위해 생성 시점에 결정하는 것을 권장합니다.<br>
     */
    diffUtilEnabled: Boolean = false,
    /**
     * Executor used for DiffUtil background computation; inject for testing or custom threading policy.<br><br>
     * DiffUtil 백그라운드 계산에 사용하는 Executor이며, 테스트/스레딩 정책을 위해 주입합니다.<br>
     * Set at creation time; it is not changeable after construction.<br><br>
     * 생성 시점에 설정하며 생성 이후 변경할 수 없습니다.<br>
     */
    diffExecutor: Executor? = null,
    /**
     * Executor for background list transformation; null means main-thread execution.<br><br>
     * 아이템 변환을 위한 백그라운드 Executor이며 null이면 메인 스레드에서 실행합니다.<br>
     */
    operationExecutor: Executor? = defaultOperationExecutor,
) : RecyclerView.Adapter<VH>() {
    companion object {
        /**
         * Default thread check mode based on build type.<br><br>
         * 빌드 타입 기준 기본 스레드 검증 모드입니다.<br>
         */
        private val defaultThreadCheckMode: AdapterThreadCheckMode =
            if (BuildConfig.DEBUG) AdapterThreadCheckMode.CRASH else AdapterThreadCheckMode.LOG

        /**
         * Shared executor for DiffUtil calculations when not provided.<br><br>
         * DiffUtil 기본 계산에 사용하는 공유 Executor입니다.<br>
         */
        private val defaultDiffExecutor: Executor = Executors.newSingleThreadExecutor()

        /**
         * Shared executor for background item transformation when not provided.<br><br>
         * 아이템 변환 기본 처리에 사용하는 공유 Executor입니다.<br>
         */
        private val defaultOperationExecutor: Executor = Executors.newSingleThreadExecutor()
    }

    /**
     * Dispatches adapter updates when DiffUtil is disabled.<br><br>
     * DiffUtil 비활성 시 어댑터 업데이트를 분배합니다.<br>
     */
    private fun dispatchUpdate(update: UpdateOp, oldSize: Int, newSize: Int) {
        when (update) {
            UpdateOp.None -> return
            UpdateOp.Full -> notifyDataSetChanged()
            is UpdateOp.Insert -> {
                if (update.count <= 0 || update.position < 0 || update.position > newSize - update.count) {
                    notifyDataSetChanged()
                } else {
                    notifyItemRangeInserted(update.position, update.count)
                }
            }
            is UpdateOp.Remove -> {
                if (update.count <= 0 || update.position < 0 || update.position + update.count > oldSize) {
                    notifyDataSetChanged()
                } else {
                    notifyItemRangeRemoved(update.position, update.count)
                }
            }
            is UpdateOp.Move -> {
                if (update.from < 0 || update.to < 0 || update.from >= newSize || update.to >= newSize) {
                    notifyDataSetChanged()
                } else {
                    notifyItemMoved(update.from, update.to)
                }
            }
            is UpdateOp.Change -> {
                if (update.count <= 0 || update.position < 0 || update.position + update.count > newSize) {
                    notifyDataSetChanged()
                } else {
                    notifyItemRangeChanged(update.position, update.count)
                }
            }
        }
    }

    /**
     * Inserts multiple items at the specified position through the operation queue.<br><br>
     * 지정한 위치에 여러 아이템을 삽입합니다.<br>
     */
    @MainThread
    fun addItemsAt(
        position: Int,
        itemsToAdd: List<ITEM>,
        commitCallback: ((Boolean) -> Unit)? = null,
    ) {
        enqueueOperation("addItemsAt", commitCallback) { current ->
            AdapterListOperations.addItemsAt(current, position, itemsToAdd).toUpdateOpResult()
        }
    }

    /**
     * Backing store for current items; mutated only through queued operations.<br><br>
     * 현재 아이템 백킹 스토리지이며 큐 연산으로만 변경됩니다.<br>
     */
    private val items = mutableListOf<ITEM>()

    /**
     * DiffUtil toggle for RecyclerView.Adapter path; user decides based on update size.<br><br>
     * RecyclerView.Adapter 경로의 DiffUtil 토글이며 변경 규모에 따라 사용자가 결정합니다.<br>
     */
    private var diffEnabled: Boolean = diffUtilEnabled

    /**
     * Thread check mode for API contract validation.<br><br>
     * API 스레드 계약 검증 모드입니다.<br>
     */
    private var threadCheckMode: AdapterThreadCheckMode = defaultThreadCheckMode

    /**
     * Executor for background item transformation; null means main-thread execution.<br><br>
     * 아이템 변환을 위한 백그라운드 Executor이며 null이면 메인 스레드에서 실행합니다.<br>
     */
    private var operationExecutor: Executor? = operationExecutor

    /**
     * Main thread handler for UI updates and completion callbacks.<br><br>
     * UI 업데이트와 완료 콜백을 위한 메인 스레드 핸들러입니다.<br>
     */
    private val mainHandler = Handler(Looper.getMainLooper())

    /**
     * Executor for background DiffUtil calculation; defaults to a single thread when not provided.<br><br>
     * DiffUtil 계산용 백그라운드 Executor이며 미지정 시 단일 스레드를 사용합니다.<br>
     */
    private val backgroundDiffExecutor: Executor = diffExecutor ?: defaultDiffExecutor

    /**
     * Logs a warning when a main-thread contract is violated.<br><br>
     * 메인 스레드 계약 위반 시 경고 로그를 남깁니다.<br>
     */
    private fun checkMainThread(apiName: String) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            handleThreadViolation("$apiName should be called on the main thread.")
        }
    }

    /**
     * Handles thread contract violations based on policy.<br><br>
     * 정책에 따라 스레드 계약 위반을 처리합니다.<br>
     */
    private fun handleThreadViolation(message: String) {
        when (threadCheckMode) {
            AdapterThreadCheckMode.OFF -> return
            AdapterThreadCheckMode.LOG -> Logx.w(message)
            AdapterThreadCheckMode.CRASH -> throw IllegalStateException(message)
        }
    }

    /**
     * Logs a warning when a worker-thread contract is violated.<br><br>
     * 워커 스레드 계약 위반 시 경고 로그를 남깁니다.<br>
     */
    private fun checkWorkerThread(apiName: String) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            handleThreadViolation("$apiName should be called on a worker thread.")
        }
    }

    /**
     * DiffUtil identity comparison callback.<br><br>
     * DiffUtil 아이템 동일성 비교 콜백입니다.<br>
     */
    private var diffUtilItemSame: ((oldItem: ITEM, newItem: ITEM) -> Boolean)? = null

    /**
     * DiffUtil content comparison callback.<br><br>
     * DiffUtil 콘텐츠 동일성 비교 콜백입니다.<br>
     */
    private var diffUtilContentsSame: ((oldItem: ITEM, newItem: ITEM) -> Boolean)? = null

    /**
     * DiffUtil payload provider callback.<br><br>
     * DiffUtil 페이로드 제공 콜백입니다.<br>
     */
    private var diffUtilChangePayload: ((oldItem: ITEM, newItem: ITEM) -> Any?)? = null

    /**
     * Item click listener invoked with current position and item.<br><br>
     * 현재 포지션과 아이템으로 호출되는 클릭 리스너입니다.<br>
     */
    private var onItemClickListener: ((Int, ITEM, View) -> Unit)? = null

    /**
     * Item long-click listener invoked with current position and item.<br><br>
     * 현재 포지션과 아이템으로 호출되는 롱클릭 리스너입니다.<br>
     */
    private var onItemLongClickListener: ((Int, ITEM, View) -> Unit)? = null

    /**
     * Operation failure listener invoked with failure details.<br><br>
     * 연산 실패 상세를 전달하는 리스너입니다.<br>
     */
    private var onAdapterOperationFailureListener: ((AdapterOperationFailureInfo) -> Unit)? = null

    /**
     * Update metadata for notifyItem* dispatch when DiffUtil is disabled.<br><br>
     * DiffUtil 비활성 시 notifyItem* 호출에 필요한 메타데이터입니다.<br>
     */
    private sealed class UpdateOp {
        /**
         * No adapter update required.<br><br>
         * 어댑터 업데이트가 필요 없습니다.<br>
         */
        object None : UpdateOp()

        /**
         * Full dataset refresh required.<br><br>
         * 전체 데이터 갱신이 필요합니다.<br>
         */
        object Full : UpdateOp()

        /**
         * Insert range update.<br><br>
         * 삽입 범위 업데이트입니다.<br>
         */
        data class Insert(
            val position: Int,
            val count: Int
        ) : UpdateOp()

        /**
         * Remove range update.<br><br>
         * 삭제 범위 업데이트입니다.<br>
         */
        data class Remove(
            val position: Int,
            val count: Int
        ) : UpdateOp()

        /**
         * Move single item update.<br><br>
         * 아이템 이동 업데이트입니다.<br>
         */
        data class Move(
            val from: Int,
            val to: Int
        ) : UpdateOp()

        /**
         * Change range update.<br><br>
         * 변경 범위 업데이트입니다.<br>
         */
        data class Change(
            val position: Int,
            val count: Int
        ) : UpdateOp()
    }

    /**
     * Converts PositionInfo to internal UpdateOp.<br><br>
     * PositionInfo를 내부 UpdateOp로 변환합니다.<br>
     */
    private fun PositionInfo.toUpdateOp(): UpdateOp = when (this) {
        is PositionInfo.None -> UpdateOp.None
        is PositionInfo.Full -> UpdateOp.Full
        is PositionInfo.Insert -> UpdateOp.Insert(position, count)
        is PositionInfo.Remove -> UpdateOp.Remove(position, count)
        is PositionInfo.Change -> UpdateOp.Change(position, count)
        is PositionInfo.Move -> UpdateOp.Move(from, to)
    }

    /**
     * Converts ListOperationResult to OperationResult with UpdateOp meta.<br><br>
     * ListOperationResult를 UpdateOp 메타를 포함한 OperationResult로 변환합니다.<br>
     */
    private fun ListOperationResult<ITEM>.toUpdateOpResult(): OperationResult<ITEM, UpdateOp> = OperationResult(
        items = items,
        success = success,
        meta = positionInfo?.toUpdateOp() ?: UpdateOp.None,
        failure = failure,
    )

    /**
     * Shared coordinator for queued adapter operations.<br><br>
     * 어댑터 연산을 직렬 처리하는 공통 코디네이터입니다.<br>
     */
    private val operationCoordinator =
        AdapterOperationQueueCoordinator<ITEM, UpdateOp>(
            runOnMainThread = { action -> runOnMainThread(action) },
            getCurrentItems = { items.toList() },
            operationExecutorProvider = { operationExecutor },
            applyResult = { oldItems, result, complete -> applyOperationResult(oldItems, result, complete) },
            onFailure = { operationName, failure -> dispatchAdapterOperationFailure(operationName, failure) },
            onError = { message, error ->
                if (error != null) {
                    Logx.e(message, error)
                } else {
                    Logx.e(message)
                }
            },
        )

    /**
     * Applies the operation result to adapter items and dispatches updates.<br><br>
     * 연산 결과를 어댑터 아이템에 적용하고 업데이트를 분배합니다.<br>
     */
    private fun applyOperationResult(
        oldItems: List<ITEM>,
        result: OperationResult<ITEM, UpdateOp>,
        complete: (Boolean) -> Unit,
    ) {
        // New items produced by the operation.<br><br>연산 결과로 생성된 새 아이템 목록입니다.<br>
        val newItems = result.items
        if (!diffEnabled) {
            val update = result.meta ?: UpdateOp.Full
            if (newItems !== items) {
                items.clear()
                items.addAll(newItems)
            }
            dispatchUpdate(update, oldItems.size, items.size)
            complete(true)
            return
        }

        backgroundDiffExecutor.execute {
            // DiffUtil result for dispatching updates.<br><br>업데이트를 위한 DiffUtil 결과입니다.<br>
            val diffResult = calculateDiff(oldItems, newItems)

            mainHandler.post {
                items.clear()
                items.addAll(newItems)
                if (diffResult != null) {
                    diffResult.dispatchUpdatesTo(this)
                } else {
                    notifyDataSetChanged()
                }
                complete(true)
            }
        }
    }

    /**
     * Dispatches operation failure on the main thread.<br><br>
     * 연산 실패를 메인 스레드에서 전달합니다.<br>
     */
    private fun dispatchAdapterOperationFailure(operationName: String, failure: AdapterOperationFailure) {
        val listener = onAdapterOperationFailureListener ?: return
        runOnMainThread { listener.invoke(AdapterOperationFailureInfo(operationName, failure)) }
    }

    /**
     * Enables or disables DiffUtil; use ON for incremental changes and OFF for large replacements.<br><br>
     * DiffUtil을 활성/비활성하며 점진 변경은 ON, 대량 치환은 OFF를 권장합니다.<br>
     */
    @MainThread
    fun setDiffUtilEnabled(enabled: Boolean) {
        checkMainThread("setDiffUtilEnabled")
        diffEnabled = enabled
    }

    /**
     * Sets the thread check mode for API contracts.<br><br>
     * API 스레드 계약 검증 모드를 설정합니다.<br>
     */
    @MainThread
    fun setThreadCheckMode(mode: AdapterThreadCheckMode) {
        checkMainThread("setThreadCheckMode")
        threadCheckMode = mode
    }

    /**
     * Sets queue overflow policy and max pending size.<br><br>
     * 큐 오버플로 정책과 최대 대기 크기를 설정합니다.<br>
     */
    @MainThread
    fun setQueuePolicy(maxPending: Int, overflowPolicy: QueueOverflowPolicy) {
        checkMainThread("setQueuePolicy")
        operationCoordinator.setQueuePolicy(maxPending, overflowPolicy)
    }

    /**
     * Sets the queue debug listener for operational tracing.<br><br>
     * 운영 추적을 위한 큐 디버그 리스너를 설정합니다.<br>
     */
    @MainThread
    fun setQueueDebugListener(listener: ((QueueDebugEvent) -> Unit)?) {
        checkMainThread("setQueueDebugListener")
        operationCoordinator.setQueueDebugListener(listener)
    }

    /**
     * Sets merge keys to coalesce consecutive operations with the same name.<br><br>
     * 동일 이름의 연속 연산을 병합하기 위한 머지 키를 설정합니다.<br>
     */
    @MainThread
    fun setQueueMergeKeys(mergeKeys: Set<String>) {
        checkMainThread("setQueueMergeKeys")
        operationCoordinator.setQueueMergeKeys(mergeKeys)
    }

    /**
     * Sets the executor for background item transformation.<br><br>
     * 아이템 변환용 백그라운드 Executor를 설정합니다.<br>
     * null을 전달하면 메인 스레드에서 처리합니다.<br>
     */
    @MainThread
    fun setOperationExecutor(executor: Executor?) {
        checkMainThread("setOperationExecutor")
        operationExecutor = executor
    }

    /**
     * Sets the DiffUtil item identity comparator.<br><br>
     * DiffUtil 아이템 동일성 비교자를 설정합니다.<br>
     */
    @MainThread
    fun setDiffUtilItemSame(callback: (oldItem: ITEM, newItem: ITEM) -> Boolean) {
        checkMainThread("setDiffUtilItemSame")
        diffUtilItemSame = callback
    }

    /**
     * Sets the DiffUtil content comparator.<br><br>
     * DiffUtil 콘텐츠 비교자를 설정합니다.<br>
     */
    @MainThread
    fun setDiffUtilContentsSame(callback: (oldItem: ITEM, newItem: ITEM) -> Boolean) {
        checkMainThread("setDiffUtilContentsSame")
        diffUtilContentsSame = callback
    }

    /**
     * Sets the DiffUtil payload provider.<br><br>
     * DiffUtil 페이로드 제공자를 설정합니다.<br>
     */
    @MainThread
    fun setDiffUtilChangePayload(callback: (oldItem: ITEM, newItem: ITEM) -> Any?) {
        checkMainThread("setDiffUtilChangePayload")
        diffUtilChangePayload = callback
    }

    /**
     * Registers an item click listener invoked with current position and item.<br><br>
     * 현재 포지션과 아이템으로 호출되는 클릭 리스너를 등록합니다.<br>
     */
    @MainThread
    fun setOnItemClickListener(listener: (position: Int, item: ITEM, view: View) -> Unit) {
        checkMainThread("setOnItemClickListener")
        onItemClickListener = listener
    }

    /**
     * Registers an item long-click listener invoked with current position and item.<br><br>
     * 현재 포지션과 아이템으로 호출되는 롱클릭 리스너를 등록합니다.<br>
     */
    @MainThread
    fun setOnItemLongClickListener(listener: (position: Int, item: ITEM, view: View) -> Unit) {
        checkMainThread("setOnItemLongClickListener")
        onItemLongClickListener = listener
    }

    /**
     * Registers an operation failure listener with failure details.<br><br>
     * 연산 실패 상세를 전달하는 리스너를 등록합니다.<br>
     */
    @MainThread
    fun setOnAdapterOperationFailureListener(listener: ((AdapterOperationFailureInfo) -> Unit)?) {
        checkMainThread("setOnAdapterOperationFailureListener")
        onAdapterOperationFailureListener = listener
    }

    /**
     * Replaces the entire list through the operation queue.<br><br>
     * 연산 큐를 통해 전체 리스트를 교체합니다.<br>
     */
    @MainThread
    fun setItems(
        newItems: List<ITEM>,
        commitCallback: ((Boolean) -> Unit)? = null,
    ) {
        enqueueOperation("setItems", commitCallback) {
            AdapterListOperations.setItems(newItems).toUpdateOpResult()
        }
    }

    /**
     * Clears pending operations and replaces the list with the latest items.<br><br>
     * 대기 중인 연산을 비우고 최신 아이템으로 리스트를 교체합니다.<br>
     */
    @MainThread
    fun setItemsLatest(
        newItems: List<ITEM>,
        commitCallback: ((Boolean) -> Unit)? = null,
    ) {
        checkMainThread("setItemsLatest")
        operationCoordinator.clearAndEnqueue("setItemsLatest", commitCallback) {
            AdapterListOperations.setItems(newItems).toUpdateOpResult()
        }
    }

    /**
     * Updates items in a single batch operation.<br><br>
     * 단일 배치 연산으로 아이템을 갱신합니다.<br>
     *
     * Uses a mutable copy so partial failures do not corrupt the current list.<br><br>
     * 가변 복사본을 사용하여 부분 실패가 현재 리스트에 영향을 주지 않게 합니다.<br>
     */
    @MainThread
    fun updateItems(
        commitCallback: ((Boolean) -> Unit)? = null,
        updater: (MutableList<ITEM>) -> Unit,
    ) {
        enqueueOperation("updateItems", commitCallback) { current ->
            AdapterListOperations.updateItems(current, updater).toUpdateOpResult()
        }
    }

    /**
     * Returns an immutable copy of current items.<br><br>
     * 현재 아이템의 불변 복사본을 반환합니다.<br>
     */
    @MainThread
    fun getItems(): List<ITEM> {
        checkMainThread("getItems")
        return items.toList()
    }

    /**
     * Clears pending operations without changing current items.<br><br>
     * 현재 아이템을 변경하지 않고 대기 중인 연산을 비웁니다.<br>
     */
    @MainThread
    fun clearQueue() {
        checkMainThread("clearQueue")
        operationCoordinator.clearQueue()
    }

    /**
     * Adds a single item through the operation queue.<br><br>
     * 연산 큐를 통해 단일 아이템을 추가합니다.<br>
     */
    @MainThread
    fun addItem(
        item: ITEM,
        commitCallback: ((Boolean) -> Unit)? = null,
    ) {
        enqueueOperation("addItem", commitCallback) { current ->
            AdapterListOperations.addItem(current, item).toUpdateOpResult()
        }
    }

    /**
     * Inserts an item at the specified position through the operation queue.<br><br>
     * 연산 큐를 통해 지정 위치에 아이템을 삽입합니다.<br>
     */
    @MainThread
    fun addItemAt(
        position: Int,
        item: ITEM,
        commitCallback: ((Boolean) -> Unit)? = null,
    ) {
        enqueueOperation("addItemAt", commitCallback) { current ->
            AdapterListOperations.addItemAt(current, position, item).toUpdateOpResult()
        }
    }

    /**
     * Adds multiple items through the operation queue.<br><br>
     * 연산 큐를 통해 여러 아이템을 추가합니다.<br>
     */
    @MainThread
    fun addItems(
        itemsToAdd: List<ITEM>,
        commitCallback: ((Boolean) -> Unit)? = null,
    ) {
        enqueueOperation("addItems", commitCallback) { current ->
            AdapterListOperations.addItems(current, itemsToAdd).toUpdateOpResult()
        }
    }

    /**
     * Removes the first matching item through the operation queue.<br><br>
     * 연산 큐를 통해 첫 번째로 일치하는 아이템을 제거합니다.<br>
     */
    @MainThread
    fun removeItem(
        item: ITEM,
        commitCallback: ((Boolean) -> Unit)? = null,
    ) {
        enqueueOperation("removeItem", commitCallback) { current ->
            AdapterListOperations.removeItem(current, item).toUpdateOpResult()
        }
    }

    /**
     * Clears all items through the operation queue.<br><br>
     * 연산 큐를 통해 모든 아이템을 제거합니다.<br>
     */
    @MainThread
    fun removeAll(
        commitCallback: ((Boolean) -> Unit)? = null,
    ) {
        enqueueOperation("removeAll", commitCallback) { current ->
            AdapterListOperations.removeAll(current).toUpdateOpResult()
        }
    }

    /**
     * Replaces an item at the specified position through the operation queue.<br><br>
     * 연산 큐를 통해 지정 위치의 아이템을 교체합니다.<br>
     */
    @MainThread
    fun replaceItemAt(
        position: Int,
        item: ITEM,
        commitCallback: ((Boolean) -> Unit)? = null,
    ) {
        enqueueOperation("replaceItemAt", commitCallback) { current ->
            AdapterListOperations.replaceItemAt(current, position, item).toUpdateOpResult()
        }
    }

    /**
     * Removes an item at the specified position through the operation queue.<br><br>
     * 연산 큐를 통해 지정 위치의 아이템을 제거합니다.<br>
     */
    @MainThread
    fun removeAt(
        position: Int,
        commitCallback: ((Boolean) -> Unit)? = null,
    ) {
        enqueueOperation("removeAt", commitCallback) { current ->
            AdapterListOperations.removeAt(current, position).toUpdateOpResult()
        }
    }

    /**
     * Moves an item within the list through the operation queue.<br><br>
     * 연산 큐를 통해 리스트 내 아이템을 이동합니다.<br>
     */
    @MainThread
    fun moveItem(
        from: Int,
        to: Int,
        commitCallback: ((Boolean) -> Unit)? = null,
    ) {
        enqueueOperation("moveItem", commitCallback) { current ->
            AdapterListOperations.moveItem(current, from, to).toUpdateOpResult()
        }
    }

    /**
     * Returns the item at the specified position.<br><br>
     * 지정 위치의 아이템을 반환합니다.<br>
     */
    @MainThread
    protected fun getItem(position: Int): ITEM = items[position]

    /**
     * Returns the total item count.<br><br>
     * 전체 아이템 개수를 반환합니다.<br>
     */
    @MainThread
    override fun getItemCount(): Int = items.size

    /**
     * Creates a ViewHolder and attaches click listeners once.<br><br>
     * ViewHolder를 생성하고 클릭 리스너를 1회 바인딩합니다.<br>
     */
    @MainThread
    final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val holder = createViewHolderInternal(parent, viewType)
        attachClickListeners(holder)
        return holder
    }

    /**
     * Creates a ViewHolder for the given parent and view type.<br><br>
     * 부모 뷰와 뷰 타입에 맞는 ViewHolder를 생성합니다.<br>
     */
    @MainThread
    protected abstract fun createViewHolderInternal(parent: ViewGroup, viewType: Int): VH

    /**
     * Binds the item for the given position when valid.<br><br>
     * 유효한 포지션의 아이템을 바인딩합니다.<br>
     */
    @MainThread
    final override fun onBindViewHolder(holder: VH, position: Int) {
        if (!isPositionValid(position)) {
            Logx.e("Cannot bind item, position is $position, itemCount ${items.size}")
            return
        }

        // Item snapshot for current bind position.<br><br>현재 바인딩 포지션의 아이템 스냅샷입니다.<br>
        val item = getItem(position)
        onBindItem(holder, position, item)
    }

    /**
     * Binds the item with payloads when provided and position is valid.<br><br>
     * 페이로드가 있을 때 유효한 포지션의 아이템을 바인딩합니다.<br>
     */
    @MainThread
    final override fun onBindViewHolder(holder: VH, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
            return
        }

        if (!isPositionValid(position)) {
            Logx.e("Cannot bind item with payload, position is $position, itemCount ${items.size}")
            return
        }

        // Item snapshot for current bind position with payloads.<br><br>페이로드 바인딩용 아이템 스냅샷입니다.<br>
        val item = getItem(position)
        onBindItem(holder, position, item, payloads)
    }

    /**
     * Binds the item for the given position.<br><br>
     * 지정 포지션의 아이템을 바인딩합니다.<br>
     */
    @MainThread
    protected abstract fun onBindItem(holder: VH, position: Int, item: ITEM)

    /**
     * Binds the item with payloads for partial updates.<br><br>
     * 부분 업데이트를 위한 페이로드 바인딩입니다.<br>
     */
    @MainThread
    protected open fun onBindItem(holder: VH, position: Int, item: ITEM, payloads: List<Any>) {
        onBindItem(holder, position, item)
    }

    /**
     * Checks whether the position is within the current list bounds.<br><br>
     * 포지션이 현재 리스트 범위 내인지 확인합니다.<br>
     */
    @MainThread
    protected fun isPositionValid(position: Int): Boolean = position >= 0 && position < items.size

    /**
     * Attaches click listeners once per ViewHolder creation.<br><br>
     * ViewHolder 생성 시점에 클릭 리스너를 1회 바인딩합니다.<br>
     */
    @MainThread
    protected fun attachClickListeners(holder: VH) {
        AdapterClickBinder.bind(
            holder = holder,
            getItemOrNull = ::getItemOrNull,
            getClickListener = { onItemClickListener },
            getLongClickListener = { onItemLongClickListener },
        )
    }

    /**
     * Safely returns the item at the position or null.<br><br>
     * 포지션의 아이템을 안전하게 반환하거나 null을 반환합니다.<br>
     */
    private fun getItemOrNull(position: Int): ITEM? = items.getOrNull(position)

    /**
     * Checks whether two items represent the same entity for DiffUtil.<br><br>
     * DiffUtil을 위한 동일 아이템 판별을 수행합니다.<br>
     */
    private fun diffUtilAreItemsTheSame(oldItem: ITEM, newItem: ITEM): Boolean =
        diffUtilItemSame?.invoke(oldItem, newItem) ?: (oldItem == newItem)

    /**
     * Checks whether item contents are the same for DiffUtil.<br><br>
     * DiffUtil을 위한 콘텐츠 동일성 판별을 수행합니다.<br>
     */
    private fun diffUtilAreContentsTheSame(oldItem: ITEM, newItem: ITEM): Boolean =
        diffUtilContentsSame?.invoke(oldItem, newItem) ?: (oldItem == newItem)

    /**
     * Provides a payload for partial updates when contents change.<br><br>
     * 콘텐츠 변경 시 부분 업데이트용 페이로드를 제공합니다.<br>
     */
    private fun diffUtilGetChangePayload(oldItem: ITEM, newItem: ITEM): Any? =
        diffUtilChangePayload?.invoke(oldItem, newItem)

    /**
     * Enqueues an operation and starts processing when idle.<br><br>
     * 연산을 큐에 추가하고 유휴 상태면 처리를 시작합니다.<br>
     */
    private fun enqueueOperation(
        name: String,
        commitCallback: ((Boolean) -> Unit)?,
        apply: (List<ITEM>) -> OperationResult<ITEM, UpdateOp>,
    ) {
        checkMainThread(name)
        operationCoordinator.enqueue(name, commitCallback, apply)
    }

    /**
     * Executes the action on the main thread, posting if necessary.<br><br>
     * 메인 스레드에서 실행하며 필요 시 메인으로 포스트합니다.<br>
     */
    private fun runOnMainThread(action: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            action()
        } else {
            if (threadCheckMode == AdapterThreadCheckMode.LOG) {
                Logx.w("Not on main thread. Posting to main.")
            }
            mainHandler.post(action)
        }
    }

    /**
     * Calculates DiffUtil result on a worker thread.<br><br>
     * 워커 스레드에서 DiffUtil 결과를 계산합니다.<br>
     */
    @WorkerThread
    private fun calculateDiff(oldItems: List<ITEM>, newItems: List<ITEM>): DiffUtil.DiffResult? {
        checkWorkerThread("calculateDiff")
        return try {
            DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize(): Int = oldItems.size

                override fun getNewListSize(): Int = newItems.size

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                    diffUtilAreItemsTheSame(oldItems[oldItemPosition], newItems[newItemPosition])

                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                    diffUtilAreContentsTheSame(oldItems[oldItemPosition], newItems[newItemPosition])

                override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? =
                    diffUtilGetChangePayload(oldItems[oldItemPosition], newItems[newItemPosition])
            })
        } catch (e: Exception) {
            Logx.e("DiffUtil apply failed: ${e.message}")
            null
        }
    }
}
