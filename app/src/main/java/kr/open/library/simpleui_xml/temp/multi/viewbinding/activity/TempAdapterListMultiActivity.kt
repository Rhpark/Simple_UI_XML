package kr.open.library.simpleui_xml.temp.multi.viewbinding.activity

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kr.open.library.simple_ui.xml.ui.components.activity.binding.BaseViewBindingActivity
import kr.open.library.simple_ui.xml.ui.temp.base.list.diffcallback.DefaultDiffCallback
import kr.open.library.simpleui_xml.databinding.ActivityTempAdapterExampleBinding
import kr.open.library.simpleui_xml.temp.base.TempAdapterKind
import kr.open.library.simpleui_xml.temp.data.TempItem
import kr.open.library.simpleui_xml.temp.util.TempItemDiffCallback
import kr.open.library.simpleui_xml.temp.multi.viewbinding.adapter.listadapter.TempMultiViewBindingListAdapter
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

import kr.open.library.simple_ui.xml.ui.adapter.queue.QueueDebugEvent
import kr.open.library.simple_ui.xml.ui.adapter.queue.QueueEventType
import kr.open.library.simple_ui.xml.ui.adapter.queue.QueueOverflowPolicy
import kr.open.library.simple_ui.xml.ui.temp.base.AdapterOperationFailure
import kr.open.library.simple_ui.xml.ui.temp.base.AdapterOperationFailureInfo
import kr.open.library.simple_ui.xml.ui.temp.base.list.RootListAdapterCore
import kr.open.library.simple_ui.xml.ui.temp.base.normal.RootRcvAdapterCore
import kr.open.library.simpleui_xml.temp.data.TempItemGenerator
/**
 * ListAdapter multi-type example screen.<br><br>
 * ListAdapter 다중 타입 예제 화면입니다.<br>
 */
class TempAdapterListMultiActivity :
    BaseViewBindingActivity<ActivityTempAdapterExampleBinding>(ActivityTempAdapterExampleBinding::inflate) {
    /**
     * Adapter kind for this screen.<br><br>
     * 이 화면에서 사용하는 어댑터 종류입니다.<br>
     */
    private val adapterKind: TempAdapterKind = TempAdapterKind.LIST

    
    /**
     * Title shown at the top of the screen.<br><br>
     * 화면 상단에 표시되는 제목입니다.<br>
     */
    private val screenTitle: String = "List - Multi"

    /**
     * Whether the Simple adapter option is allowed.<br><br>
     * Simple 어댑터 옵션 사용 여부입니다.<br>
     */
    private val allowSimpleAdapter: Boolean = false

    /**
     * Binding accessor for this Activity.<br><br>
     * 이 Activity의 바인딩 접근자입니다.<br>
     */
    private val binding: ActivityTempAdapterExampleBinding
        get() = getBinding()

    /**
     * Shared diff executor for injection examples.<br><br>
     * 주입 예제에서 사용하는 공용 diff executor입니다.<br>
     */
    private val diffExecutorService: ExecutorService = Executors.newSingleThreadExecutor()

        /**
     * Default max pending queue size.<br><br>
     * 기본 최대 대기 큐 크기입니다.<br>
     */
    private val defaultQueueMaxPending: Int = 100

    /**
     * Next id for newly created items.<br><br>
     * 새로 생성할 아이템의 다음 ID입니다.<br>
     */
    private var nextItemId: Long = 0L

    /**
     * Current adapter instance bound to this screen.<br><br>
     * 이 화면에 바인딩된 현재 어댑터 인스턴스입니다.<br>
     */
    private var currentAdapter: RecyclerView.Adapter<*>? = null
    /**
     * Initializes UI and sets up example interactions.<br><br>
     * UI를 초기화하고 예제 동작을 설정합니다.<br>
     */
    override fun onCreate(binding: ActivityTempAdapterExampleBinding, savedInstanceState: Bundle?) {
        binding.tvExampleTitle.text = screenTitle
        binding.rcvTempExample.layoutManager = LinearLayoutManager(this)

        configureOptionAvailability()
        setupListeners()
        refreshAdapter()
    }

    /**
     * Releases resources on activity destruction.<br><br>
     * Activity 종료 시 리소스를 해제합니다.<br>
     */
    override fun onDestroy() {
        diffExecutorService.shutdown()
        super.onDestroy()
    }

    /**
     * Creates an adapter for this screen.<br><br>
     * 이 화면에서 사용할 어댑터를 생성합니다.<br>
     */
    private fun createAdapter(): RecyclerView.Adapter<*> {
        val diffExecutor = if (binding.swUseDiffExecutor.isChecked) diffExecutorService else null
        val useCustomDiffCallback = binding.swUseCustomDiffCallback.isChecked
        val diffCallback = if (useCustomDiffCallback) {
            TempItemDiffCallback()
        } else {
            DefaultDiffCallback<TempItem>()
        }

        return TempMultiViewBindingListAdapter(
            diffCallback = diffCallback,
            diffExecutor = diffExecutor,
        )
    }

    /**
     * Sets up click listeners and option toggles.<br><br>
     * 클릭 리스너와 옵션 토글을 설정합니다.<br>
     */
    private fun setupListeners() {
        binding.btnResetItems.setOnClickListener { resetItems() }
        binding.btnSetLargeItems.setOnClickListener { setLargeItems() }
        binding.btnAddItem.setOnClickListener { addItem() }
        binding.btnAddItemAt.setOnClickListener { addItemAt() }
        binding.btnAddItems.setOnClickListener { addItems() }
        binding.btnRemoveItem.setOnClickListener { removeItem() }
        binding.btnRemoveAt.setOnClickListener { removeAt() }
        binding.btnMoveItem.setOnClickListener { moveItem() }
        binding.btnReplaceItem.setOnClickListener { replaceItem() }
        binding.btnRemoveAll.setOnClickListener { removeAll() }

        binding.swUseDiffExecutor.setOnCheckedChangeListener { _, _ -> refreshAdapter() }
        binding.swEnableDiffUtil.setOnCheckedChangeListener { _, _ -> refreshAdapter() }
        binding.swUseCustomDiffCallback.setOnCheckedChangeListener { _, _ -> refreshAdapter() }
        binding.swUseSimpleAdapter.setOnCheckedChangeListener { _, _ -> refreshAdapter() }
    }

    /**
     * Updates option availability based on adapter kind and mode.<br><br>
     * 어댑터 종류와 모드 기준으로 옵션 활성화를 갱신합니다.<br>
     */
    private fun configureOptionAvailability() {
        val isAdapter = (adapterKind == TempAdapterKind.RV)
        val isList = (adapterKind == TempAdapterKind.LIST)

        binding.swEnableDiffUtil.isEnabled = isAdapter
        binding.swEnableDiffUtil.alpha = if (isAdapter) 1f else 0.5f
        if (!isAdapter) {
            binding.swEnableDiffUtil.isChecked = false
        }

        binding.swUseCustomDiffCallback.isEnabled = isList
        binding.swUseCustomDiffCallback.alpha = if (isList) 1f else 0.5f
        if (!isList) {
            binding.swUseCustomDiffCallback.isChecked = false
        }

        if (allowSimpleAdapter) {
            binding.swUseSimpleAdapter.visibility = View.VISIBLE
            binding.swUseSimpleAdapter.isEnabled = true
        } else {
            binding.swUseSimpleAdapter.visibility = View.GONE
            binding.swUseSimpleAdapter.isEnabled = false
            binding.swUseSimpleAdapter.isChecked = false
        }
    }

    /**
     * Refreshes adapter based on current selection and options.<br><br>
     * 현재 선택과 옵션 기준으로 어댑터를 갱신합니다.<br>
     */
    private fun refreshAdapter() {
        val adapter = createAdapter()

        binding.rcvTempExample.adapter = adapter
        bindAdapter(adapter)

        applyQueuePolicy()
        bindClickListeners()

        submitInitialItems()
    }

    /**
     * Binds adapter instance for subsequent actions.<br><br>
     * 이후 동작을 위해 어댑터 인스턴스를 바인딩합니다.<br>
     */
    private fun bindAdapter(adapter: RecyclerView.Adapter<*>) {
        currentAdapter = adapter
    }

    /**
     * Applies queue policy to the current adapter.<br><br>
     * 현재 어댑터에 큐 정책을 적용합니다.<br>
     */
    private fun applyQueuePolicy() {
        val adapter = requireAdapter() ?: return
        if (!withRootAdapter(
                adapter = adapter,
                onRcv = { rcvAdapter ->
                    rcvAdapter.setQueuePolicy(defaultQueueMaxPending, QueueOverflowPolicy.DROP_NEW)
                    rcvAdapter.setOnAdapterOperationFailureListener { info ->
                        updateStatus("queueFail(${formatFailure(info)})")
                    }
                    rcvAdapter.setQueueDebugListener { event ->
                        if (event.type == QueueEventType.ERROR || event.type == QueueEventType.DROPPED) {
                            updateStatus(formatQueueEvent(event))
                        }
                    }
                },
                onList = { listAdapter ->
                    listAdapter.setQueuePolicy(defaultQueueMaxPending, QueueOverflowPolicy.DROP_NEW)
                    listAdapter.setOnAdapterOperationFailureListener { info ->
                        updateStatus("queueFail(${formatFailure(info)})")
                    }
                    listAdapter.setQueueDebugListener { event ->
                        if (event.type == QueueEventType.ERROR || event.type == QueueEventType.DROPPED) {
                            updateStatus(formatQueueEvent(event))
                        }
                    }
                },
            )
        ) {
            updateStatus("Unsupported adapter for queue policy")
        }
    }

    /**
     * Binds click listeners to the current adapter.<br><br>
     * 현재 어댑터에 클릭 리스너를 바인딩합니다.<br>
     */
    private fun bindClickListeners() {
        val adapter = requireAdapter() ?: return
        if (!withRootAdapter(
                adapter = adapter,
                onRcv = { rcvAdapter ->
                    rcvAdapter.setOnItemClickListener { position, item, _ ->
                        updateStatus("click(pos=$position, id=${item.id})")
                    }
                    rcvAdapter.setOnItemLongClickListener { position, item, _ ->
                        updateStatus("longClick(pos=$position, id=${item.id})")
                    }
                },
                onList = { listAdapter ->
                    listAdapter.setOnItemClickListener { position, item, _ ->
                        updateStatus("click(pos=$position, id=${item.id})")
                    }
                    listAdapter.setOnItemLongClickListener { position, item, _ ->
                        updateStatus("longClick(pos=$position, id=${item.id})")
                    }
                },
            )
        ) {
            updateStatus("Unsupported adapter for listeners")
        }
    }

    /**
     * Submits default items and updates id tracking.<br><br>
     * 기본 아이템을 제출하고 ID 추적을 업데이트합니다.<br>
     */
    private fun submitInitialItems() {
        val adapter = requireAdapter() ?: return
        val items = TempItemGenerator.generateDefaultItemsMulti()
        updateNextItemId(items)
        if (!withRootAdapter(
                adapter = adapter,
                onRcv = { rcvAdapter ->
                    rcvAdapter.setItems(items) { success ->
                        updateStatus("setItems(${items.size}) -> $success")
                    }
                },
                onList = { listAdapter ->
                    listAdapter.setItems(items) { success ->
                        updateStatus("setItems(${items.size}) -> $success")
                    }
                },
            )
        ) {
            updateStatus("Unsupported adapter for setItems")
        }
    }

    /**
     * Resets items to default set.<br><br>
     * 기본 아이템 세트로 초기화합니다.<br>
     */
    private fun resetItems() {
        val adapter = requireAdapter() ?: return
        val items = TempItemGenerator.generateDefaultItemsMulti()
        updateNextItemId(items)
        if (!withRootAdapter(
                adapter = adapter,
                onRcv = { rcvAdapter ->
                    rcvAdapter.setItems(items) { success ->
                        updateStatus("setItems(${items.size}) -> $success")
                    }
                },
                onList = { listAdapter ->
                    listAdapter.setItems(items) { success ->
                        updateStatus("setItems(${items.size}) -> $success")
                    }
                },
            )
        ) {
            updateStatus("Unsupported adapter for setItems")
        }
    }

    /**
     * Replaces items with large dataset for stress testing.<br><br>
     * 대량 아이템으로 교체하여 스트레스 테스트를 수행합니다.<br>
     */
    private fun setLargeItems() {
        val adapter = requireAdapter() ?: return
        val items = TempItemGenerator.generateLargeItemsMulti()
        updateNextItemId(items)
        if (!withRootAdapter(
                adapter = adapter,
                onRcv = { rcvAdapter ->
                    rcvAdapter.setItems(items) { success ->
                        updateStatus("setLargeItems(${items.size}) -> $success")
                    }
                },
                onList = { listAdapter ->
                    listAdapter.setItems(items) { success ->
                        updateStatus("setLargeItems(${items.size}) -> $success")
                    }
                },
            )
        ) {
            updateStatus("Unsupported adapter for setLargeItems")
        }
    }

    /**
     * Adds a single item at the end.<br><br>
     * 아이템 1개를 끝에 추가합니다.<br>
     */
    private fun addItem() {
        val adapter = requireAdapter() ?: return
        val newItem = createNewItem("Add")
        if (!withRootAdapter(
                adapter = adapter,
                onRcv = { rcvAdapter ->
                    rcvAdapter.addItem(newItem) { success ->
                        updateStatus("addItem(id=${newItem.id}) -> $success")
                    }
                },
                onList = { listAdapter ->
                    listAdapter.addItem(newItem) { success ->
                        updateStatus("addItem(id=${newItem.id}) -> $success")
                    }
                },
            )
        ) {
            updateStatus("Unsupported adapter for addItem")
        }
    }

    /**
     * Adds a single item at the top position.<br><br>
     * 아이템 1개를 최상단에 추가합니다.<br>
     */
    private fun addItemAt() {
        val adapter = requireAdapter() ?: return
        val newItem = createNewItem("AddAt")
        if (!withRootAdapter(
                adapter = adapter,
                onRcv = { rcvAdapter ->
                    rcvAdapter.addItemAt(0, newItem) { success ->
                        updateStatus("addItemAt(0, id=${newItem.id}) -> $success")
                    }
                },
                onList = { listAdapter ->
                    listAdapter.addItemAt(0, newItem) { success ->
                        updateStatus("addItemAt(0, id=${newItem.id}) -> $success")
                    }
                },
            )
        ) {
            updateStatus("Unsupported adapter for addItemAt")
        }
    }

    /**
     * Adds multiple items in a batch.<br><br>
     * 여러 아이템을 일괄 추가합니다.<br>
     */
    private fun addItems() {
        val adapter = requireAdapter() ?: return
        val newItems = List(ADD_ITEMS_COUNT) { index ->
            val label = "Batch${index + 1}"
            createNewItem(label)
        }
        if (!withRootAdapter(
                adapter = adapter,
                onRcv = { rcvAdapter ->
                    rcvAdapter.addItems(newItems) { success ->
                        updateStatus("addItems(${newItems.size}) -> $success")
                    }
                },
                onList = { listAdapter ->
                    listAdapter.addItems(newItems) { success ->
                        updateStatus("addItems(${newItems.size}) -> $success")
                    }
                },
            )
        ) {
            updateStatus("Unsupported adapter for addItems")
        }
    }

    /**
     * Removes a single item by value.<br><br>
     * 값 기준으로 아이템 1개를 제거합니다.<br>
     */
    private fun removeItem() {
        val adapter = requireAdapter() ?: return
        val items = getItems(adapter)
        if (items.isEmpty()) {
            updateStatus("removeItem -> empty list")
            return
        }
        val target = items.first()
        if (!withRootAdapter(
                adapter = adapter,
                onRcv = { rcvAdapter ->
                    rcvAdapter.removeItem(target) { success ->
                        updateStatus("removeItem(id=${target.id}) -> $success")
                    }
                },
                onList = { listAdapter ->
                    listAdapter.removeItem(target) { success ->
                        updateStatus("removeItem(id=${target.id}) -> $success")
                    }
                },
            )
        ) {
            updateStatus("Unsupported adapter for removeItem")
        }
    }

    /**
     * Removes an item at a position.<br><br>
     * 포지션 기준으로 아이템을 제거합니다.<br>
     */
    private fun removeAt() {
        val adapter = requireAdapter() ?: return
        val items = getItems(adapter)
        if (items.isEmpty()) {
            updateStatus("removeAt -> empty list")
            return
        }
        val targetIndex = items.lastIndex
        if (!withRootAdapter(
                adapter = adapter,
                onRcv = { rcvAdapter ->
                    rcvAdapter.removeAt(targetIndex) { success ->
                        updateStatus("removeAt($targetIndex) -> $success")
                    }
                },
                onList = { listAdapter ->
                    listAdapter.removeAt(targetIndex) { success ->
                        updateStatus("removeAt($targetIndex) -> $success")
                    }
                },
            )
        ) {
            updateStatus("Unsupported adapter for removeAt")
        }
    }

    /**
     * Moves an item from top to bottom.<br><br>
     * 아이템을 상단에서 하단으로 이동합니다.<br>
     */
    private fun moveItem() {
        val adapter = requireAdapter() ?: return
        val items = getItems(adapter)
        if (items.size < 2) {
            updateStatus("moveItem -> not enough items")
            return
        }
        val from = 0
        val to = items.lastIndex
        if (!withRootAdapter(
                adapter = adapter,
                onRcv = { rcvAdapter ->
                    rcvAdapter.moveItem(from, to) { success ->
                        updateStatus("moveItem($from -> $to) -> $success")
                    }
                },
                onList = { listAdapter ->
                    listAdapter.moveItem(from, to) { success ->
                        updateStatus("moveItem($from -> $to) -> $success")
                    }
                },
            )
        ) {
            updateStatus("Unsupported adapter for moveItem")
        }
    }

    /**
     * Replaces an item at the middle position.<br><br>
     * 중간 포지션 아이템을 교체합니다.<br>
     */
    private fun replaceItem() {
        val adapter = requireAdapter() ?: return
        val items = getItems(adapter)
        if (items.isEmpty()) {
            updateStatus("replaceItemAt -> empty list")
            return
        }
        val targetIndex = items.size / 2
        val targetItem = items[targetIndex]
        val updatedItem = TempItemGenerator.updateItem(targetItem, "Replaced")
        if (!withRootAdapter(
                adapter = adapter,
                onRcv = { rcvAdapter ->
                    rcvAdapter.replaceItemAt(targetIndex, updatedItem) { success ->
                        updateStatus("replaceItemAt($targetIndex, id=${updatedItem.id}) -> $success")
                    }
                },
                onList = { listAdapter ->
                    listAdapter.replaceItemAt(targetIndex, updatedItem) { success ->
                        updateStatus("replaceItemAt($targetIndex, id=${updatedItem.id}) -> $success")
                    }
                },
            )
        ) {
            updateStatus("Unsupported adapter for replaceItemAt")
        }
    }

    /**
     * Clears all items from the adapter.<br><br>
     * 어댑터의 모든 아이템을 제거합니다.<br>
     */
    private fun removeAll() {
        val adapter = requireAdapter() ?: return
        if (!withRootAdapter(
                adapter = adapter,
                onRcv = { rcvAdapter ->
                    rcvAdapter.removeAll { success ->
                        updateStatus("removeAll -> $success")
                    }
                },
                onList = { listAdapter ->
                    listAdapter.removeAll { success ->
                        updateStatus("removeAll -> $success")
                    }
                },
            )
        ) {
            updateStatus("Unsupported adapter for removeAll")
        }
    }

    /**
     * Returns the current adapter or null if missing.<br><br>
     * 현재 어댑터를 반환하며 없으면 null을 반환합니다.<br>
     */
    private fun requireAdapter(): RecyclerView.Adapter<*>? {
        val adapter = currentAdapter
        if (adapter == null) {
            updateStatus("Adapter is not ready")
        }
        return adapter
    }

    /**
     * Returns items from adapter if supported.<br><br>
     * 지원 가능한 어댑터에서 아이템 목록을 반환합니다.<br>
     */
    @Suppress("UNCHECKED_CAST")
    private fun getItems(adapter: RecyclerView.Adapter<*>): List<TempItem> = when (adapter) {
        is RootRcvAdapterCore<*, *> -> (adapter as RootRcvAdapterCore<TempItem, *>).getItems()
        is RootListAdapterCore<*, *> -> (adapter as RootListAdapterCore<TempItem, *>).getItems()
        else -> emptyList()
    }

    /**
     * Executes action with RootRcvAdapterCore or RootListAdapterCore.<br><br>
     * RootRcvAdapterCore 또는 RootListAdapterCore로 동작을 실행합니다.<br>
     */
    @Suppress("UNCHECKED_CAST")
    private fun withRootAdapter(
        adapter: RecyclerView.Adapter<*>,
        onRcv: (RootRcvAdapterCore<TempItem, *>) -> Unit,
        onList: (RootListAdapterCore<TempItem, *>) -> Unit,
    ): Boolean = when (adapter) {
        is RootRcvAdapterCore<*, *> -> {
            onRcv(adapter as RootRcvAdapterCore<TempItem, *>)
            true
        }
        is RootListAdapterCore<*, *> -> {
            onList(adapter as RootListAdapterCore<TempItem, *>)
            true
        }
        else -> false
    }

    /**
     * Updates next item id based on a new list.<br><br>
     * 새 리스트 기준으로 다음 아이템 ID를 업데이트합니다.<br>
     */
    private fun updateNextItemId(items: List<TempItem>) {
        val maxId = items.maxOfOrNull { it.id } ?: -1L
        nextItemId = maxId + 1
    }

    /**
     * Creates a new item using current mode and next id.<br><br>
     * 현재 모드와 다음 ID로 새 아이템을 생성합니다.<br>
     */
    private fun createNewItem(label: String): TempItem {
        val item = TempItemGenerator.generateSingleItemMulti(nextItemId.toInt())
        nextItemId += 1
        return item.copy(title = label, description = "Created item")
    }

    /**
     * Formats operation failure info.<br><br>
     * 연산 실패 정보를 포맷팅합니다.<br>
     */
    private fun formatFailure(info: AdapterOperationFailureInfo): String {
        val detail = when (val failure = info.failure) {
            is AdapterOperationFailure.Validation -> "validation=${failure.message}"
            is AdapterOperationFailure.Exception -> "exception=${failure.error.message ?: "unknown"}"
            is AdapterOperationFailure.Dropped -> "dropped=${failure.reason}"
        }
        return "op=${info.operationName}, $detail"
    }

    /**
     * Formats queue debug event.<br><br>
     * 큐 디버그 이벤트를 포맷팅합니다.<br>
     */
    private fun formatQueueEvent(event: QueueDebugEvent): String {
        val name = event.operationName ?: "unknown"
        val drop = event.dropReason?.let { ", drop=$it" } ?: ""
        val message = event.message?.let { ", msg=$it" } ?: ""
        return "queue:${event.type} op=$name, pending=${event.pendingSize}$drop$message"
    }

    private companion object {
        /**
         * Batch size for addItems operation.<br><br>
         * addItems 연산의 배치 크기입니다.<br>
         */
        private const val ADD_ITEMS_COUNT: Int = 3
    }

    /**
     * Updates status text with the provided message.<br><br>
     * 제공된 메시지로 상태 텍스트를 갱신합니다.<br>
     */
    private fun updateStatus(message: String) {
        binding.tvExampleStatus.text = message
    }
}