package kr.open.library.simple_ui.xml.ui.temp.base.list

import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import androidx.annotation.MainThread
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.xml.BuildConfig
import kr.open.library.simple_ui.xml.ui.adapter.queue.QueueDebugEvent
import kr.open.library.simple_ui.xml.ui.adapter.queue.QueueOverflowPolicy
import kr.open.library.simple_ui.xml.ui.temp.base.AdapterClickBinder
import kr.open.library.simple_ui.xml.ui.temp.base.AdapterOperationFailure
import kr.open.library.simple_ui.xml.ui.temp.base.AdapterOperationFailureInfo
import kr.open.library.simple_ui.xml.ui.temp.base.AdapterOperationQueueCoordinator
import kr.open.library.simple_ui.xml.ui.temp.base.AdapterOperationQueueCoordinator.OperationResult
import kr.open.library.simple_ui.xml.ui.temp.base.AdapterThreadCheckMode
import kr.open.library.simple_ui.xml.ui.temp.base.list.diffcallback.DefaultDiffCallback
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * Builds AsyncDifferConfig with optional executor injection for background diffing.<br><br>
 * 백그라운드 diff 처리를 위한 Executor 주입을 포함해 AsyncDifferConfig를 생성합니다.<br>
 */
private fun <ITEM : Any> buildDifferConfig(
    diffCallback: DiffUtil.ItemCallback<ITEM>,
    diffExecutor: Executor?,
): AsyncDifferConfig<ITEM> {
    // Builder for AsyncDifferConfig.<br><br>AsyncDifferConfig 생성용 빌더입니다.<br>
    val builder = AsyncDifferConfig.Builder(diffCallback)
    if (diffExecutor != null) {
        try {
            // Reflection-based method lookup for backward compatibility.<br><br>하위 호환을 위한 리플렉션 메서드 조회입니다.<br>
            val method =
                AsyncDifferConfig.Builder::class.java.getMethod(
                    "setBackgroundThreadExecutor",
                    Executor::class.java,
                )
            method.invoke(builder, diffExecutor)
        } catch (_: NoSuchMethodException) {
            Logx.w("setBackgroundThreadExecutor not available")
        } catch (e: Exception) {
            Logx.w("Failed to set diff executor: ${e.message}")
        }
    }

    return builder.build()
}

/**
 * Core ListAdapter base with queued operations and DiffUtil-backed updates.<br><br>
 * 연산 큐와 DiffUtil 기반 업데이트를 제공하는 ListAdapter 코어입니다.<br>
 * 아이템 변환은 기본적으로 백그라운드에서 처리하며 필요 시 메인 스레드로 전환할 수 있습니다.<br>
 */
abstract class RootListAdapterCore<ITEM : Any, VH : RecyclerView.ViewHolder>(
    /**
     * DiffUtil callback for item comparison.<br><br>
     * 아이템 비교를 위한 DiffUtil 콜백입니다.<br>
     * Set at creation time; it is not changeable after construction.<br><br>
     * 생성 시점에 설정하며 생성 이후 변경할 수 없습니다.<br>
     */
    diffCallback: DiffUtil.ItemCallback<ITEM> = DefaultDiffCallback(),
    /**
     * Executor used for background diff computation; inject for testing or custom policy.<br><br>
     * 백그라운드 diff 계산을 위한 Executor이며 테스트/정책을 위해 주입합니다.<br>
     * Set at creation time; it is not changeable after construction.<br><br>
     * 생성 시점에 설정하며 생성 이후 변경할 수 없습니다.<br>
     */
    diffExecutor: Executor? = null,
    /**
     * Executor for background list transformation; null means main-thread execution.<br><br>
     * 아이템 변환을 위한 백그라운드 Executor이며 null이면 메인 스레드에서 실행합니다.<br>
     */
    operationExecutor: Executor? = defaultOperationExecutor,
) : ListAdapter<ITEM, VH>(buildDifferConfig(diffCallback, diffExecutor)) {
    companion object {
        /**
         * Shared executor for background item transformation when not provided.<br><br>
         * 아이템 변환 기본 처리에 사용하는 공유 Executor입니다.<br>
         */
        private val defaultOperationExecutor: Executor = Executors.newSingleThreadExecutor()
    }

    /**
     * Main thread handler for UI updates and completion callbacks.<br><br>
     * UI 업데이트와 완료 콜백을 위한 메인 스레드 핸들러입니다.<br>
     */
    private val mainHandler = Handler(Looper.getMainLooper())

    /**
     * Thread check mode for API contract validation.<br><br>
     * API 스레드 계약 검증 모드입니다.<br>
     */
    private var threadCheckMode: AdapterThreadCheckMode =
        if (BuildConfig.DEBUG) AdapterThreadCheckMode.CRASH else AdapterThreadCheckMode.LOG

    /**
     * Executor for background item transformation; null means main-thread execution.<br><br>
     * 아이템 변환을 위한 백그라운드 Executor이며 null이면 메인 스레드에서 실행합니다.<br>
     */
    private var operationExecutor: Executor? = operationExecutor

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
            if (itemsToAdd.isEmpty()) {
                return@enqueueOperation OperationResult(current, true)
            }
            if (position < 0 || position > current.size) {
                val message = "Cannot add items at position $position. Valid range: 0..${current.size}"
                Logx.e(message)
                return@enqueueOperation OperationResult(current, false, failure = AdapterOperationFailure.Validation(message))
            }
            OperationResult(current.toMutableList().apply { addAll(position, itemsToAdd) }, true)
        }
    }

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
     * Shared coordinator for queued adapter operations.<br><br>
     * 어댑터 연산을 직렬 처리하는 공통 코디네이터입니다.<br>
     */
    private val operationCoordinator =
        AdapterOperationQueueCoordinator<ITEM, Unit>(
            runOnMainThread = { action -> runOnMainThread(action) },
            getCurrentItems = { currentList.toList() },
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
     * Applies the operation result to the ListAdapter and invokes completion.<br><br>
     * 연산 결과를 ListAdapter에 적용하고 완료 콜백을 호출합니다.<br>
     */
    private fun applyOperationResult(
        oldItems: List<ITEM>,
        result: OperationResult<ITEM, Unit>,
        complete: (Boolean) -> Unit,
    ) {
        // New items produced by the operation.<br><br>연산 결과로 생성된 새 아이템 목록입니다.<br>
        val newItems = result.items
        submitListInternal(newItems) { complete(true) }
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
     * Discourages direct submitList usage; use queue APIs instead.<br><br>
     * submitList 직접 사용을 지양하고 큐 API 사용을 권장합니다.<br>
     */
    @Deprecated(
        message = "Use setItems()/add/remove/move/replace to preserve operation queue ordering.",
        replaceWith = ReplaceWith("setItems(list ?: emptyList())"),
        level = DeprecationLevel.WARNING,
    )
    @MainThread
    final override fun submitList(list: List<ITEM>?) {
        Logx.w("submitList() direct call is discouraged. Use setItems()/queue APIs.")
        // Normalized list to avoid null usage.<br><br>null 리스트를 비어있는 리스트로 정규화합니다.<br>
        val safeList = list ?: emptyList()
        setItems(safeList)
    }

    /**
     * Discourages direct submitList usage; use queue APIs instead.<br><br>
     * submitList 직접 사용을 지양하고 큐 API 사용을 권장합니다.<br>
     */
    @Deprecated(
        message = "Use setItems()/add/remove/move/replace to preserve operation queue ordering.",
        replaceWith = ReplaceWith("setItems(list ?: emptyList())"),
        level = DeprecationLevel.WARNING,
    )
    @MainThread
    final override fun submitList(list: List<ITEM>?, commitCallback: Runnable?) {
        Logx.w("submitList() direct call is discouraged. Use setItems()/queue APIs.")
        // Normalized list to avoid null usage.<br><br>null 리스트를 비어있는 리스트로 정규화합니다.<br>
        val safeList = list ?: emptyList()
        setItems(safeList) { success ->
            if (success) {
                commitCallback?.run()
            }
        }
    }

    /**
     * Replaces the entire list through the operation queue.<br><br>
     * 연산 큐를 통해 전체 리스트를 교체합니다.<br>
     */
    @MainThread
    fun setItems(
        items: List<ITEM>,
        commitCallback: ((Boolean) -> Unit)? = null,
    ) {
        enqueueOperation("setItems", commitCallback) {
            OperationResult(items.toList(), true)
        }
    }

    /**
     * Clears pending operations and replaces the list with the latest items.<br><br>
     * 대기 중인 연산을 비우고 최신 아이템으로 리스트를 교체합니다.<br>
     */
    @MainThread
    fun setItemsLatest(
        items: List<ITEM>,
        commitCallback: ((Boolean) -> Unit)? = null,
    ) {
        checkMainThread("setItemsLatest")
        operationCoordinator.clearAndEnqueue("setItemsLatest", commitCallback) {
            OperationResult(items.toList(), true)
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
            val mutable = current.toMutableList()
            updater(mutable)
            OperationResult(mutable, true)
        }
    }

    /**
     * Returns an immutable copy of current items.<br><br>
     * 현재 아이템의 불변 복사본을 반환합니다.<br>
     */
    @MainThread
    fun getItems(): List<ITEM> {
        checkMainThread("getItems")
        return currentList.toList()
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
            OperationResult(current.toMutableList().apply { add(item) }, true)
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
            if (position < 0 || position > current.size) {
                val message = "Cannot add item at position $position. Valid range: 0..${current.size}"
                Logx.e(message)
                return@enqueueOperation OperationResult(current, false, failure = AdapterOperationFailure.Validation(message))
            }
            OperationResult(current.toMutableList().apply { add(position, item) }, true)
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
            if (itemsToAdd.isEmpty()) {
                OperationResult(current, true)
            } else {
                OperationResult(current.toMutableList().apply { addAll(itemsToAdd) }, true)
            }
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
            // Target index for item removal.<br><br>아이템 제거 대상 인덱스입니다.<br>
            val target = current.indexOf(item)
            if (target == RecyclerView.NO_POSITION) {
                val message = "Item not found in the list"
                Logx.e(message)
                return@enqueueOperation OperationResult(current, false, failure = AdapterOperationFailure.Validation(message))
            }
            OperationResult(current.toMutableList().apply { removeAt(target) }, true)
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
        enqueueOperation("removeAll", commitCallback) {
            OperationResult(emptyList(), true)
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
            if (position < 0 || position >= current.size) {
                val message = "Cannot replace item at position $position. Valid range: 0 until ${current.size}"
                Logx.e(message)
                return@enqueueOperation OperationResult(current, false, failure = AdapterOperationFailure.Validation(message))
            }
            OperationResult(current.toMutableList().apply { set(position, item) }, true)
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
            if (position < 0 || position >= current.size) {
                val message = "Cannot remove item at position $position. Valid range: 0 until ${current.size}"
                Logx.e(message)
                return@enqueueOperation OperationResult(current, false, failure = AdapterOperationFailure.Validation(message))
            }
            OperationResult(current.toMutableList().apply { removeAt(position) }, true)
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
            if (from < 0 || from >= current.size || to < 0 || to >= current.size) {
                val message = "Cannot move item. Valid range: 0 until ${current.size}"
                Logx.e(message)
                return@enqueueOperation OperationResult(current, false, failure = AdapterOperationFailure.Validation(message))
            }
            if (from == to) {
                return@enqueueOperation OperationResult(current, true)
            }
            OperationResult(
                current.toMutableList().apply {
                    // Item instance being moved.<br><br>이동되는 아이템 인스턴스입니다.<br>
                    val movedItem = removeAt(from)
                    add(to, movedItem)
                },
                true,
            )
        }
    }

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
            Logx.e("Cannot bind item, position is $position, itemCount ${currentList.size}")
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
            Logx.e("Cannot bind item with payload, position is $position, itemCount ${currentList.size}")
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
    protected fun isPositionValid(position: Int): Boolean = position >= 0 && position < currentList.size

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
    private fun getItemOrNull(position: Int): ITEM? = currentList.getOrNull(position)

    /**
     * Enqueues an operation and starts processing when idle.<br><br>
     * 연산을 큐에 추가하고 유휴 상태면 처리를 시작합니다.<br>
     */
    private fun enqueueOperation(
        name: String,
        commitCallback: ((Boolean) -> Unit)?,
        apply: (List<ITEM>) -> OperationResult<ITEM, Unit>,
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
     * Submits list directly to ListAdapter without queue indirection.<br><br>
     * 큐를 거치지 않고 ListAdapter에 직접 리스트를 제출합니다.<br>
     */
    @MainThread
    private fun submitListInternal(items: List<ITEM>, commitCallback: Runnable? = null) {
        super.submitList(items, commitCallback)
    }
}
