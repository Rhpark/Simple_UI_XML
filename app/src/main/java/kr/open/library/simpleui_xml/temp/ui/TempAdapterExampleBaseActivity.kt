package kr.open.library.simpleui_xml.temp.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kr.open.library.simple_ui.xml.ui.temp.base.list.RootListAdapterCore
import kr.open.library.simple_ui.xml.ui.temp.base.normal.RootRcvAdapterCore
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.temp.data.TempItem
import kr.open.library.simpleui_xml.temp.data.TempItemFactory
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Base activity for temp adapter example screens.<br><br>
 * Temp 어댑터 예제 화면의 공통 베이스 액티비티입니다.<br>
 */
abstract class TempAdapterExampleBaseActivity : AppCompatActivity() {
    /**
     * Adapter kind for this screen.<br><br>
     * 이 화면에서 사용하는 어댑터 종류입니다.<br>
     */
    protected abstract val adapterKind: TempAdapterKind

    /**
     * Item mode for this screen.<br><br>
     * 이 화면에서 사용하는 아이템 모드입니다.<br>
     */
    protected abstract val itemMode: TempItemMode

    /**
     * Title shown at the top of the screen.<br><br>
     * 화면 상단에 표시되는 제목입니다.<br>
     */
    protected abstract val screenTitle: String

    /**
     * Whether the Simple adapter option is allowed.<br><br>
     * Simple 어댑터 옵션 허용 여부입니다.<br>
     */
    protected open val allowSimpleAdapter: Boolean
        get() = (itemMode == TempItemMode.SINGLE)

    /**
     * Title text view for the current example.<br><br>
     * 현재 예제 제목을 표시하는 텍스트 뷰입니다.<br>
     */
    private lateinit var tvExampleTitle: TextView

    /**
     * Status text view for operation results.<br><br>
     * 연산 결과를 표시하는 상태 텍스트 뷰입니다.<br>
     */
    private lateinit var tvExampleStatus: TextView

    /**
     * RecyclerView used to render example items.<br><br>
     * 예제 아이템을 렌더링하는 RecyclerView입니다.<br>
     */
    private lateinit var rcvTempExample: RecyclerView

    /**
     * Button to reset items to default list.<br><br>
     * 기본 리스트로 아이템을 초기화하는 버튼입니다.<br>
     */
    private lateinit var btnResetItems: Button

    /**
     * Button to set a large list for stress testing.<br><br>
     * 대량 리스트로 변경하는 버튼입니다.<br>
     */
    private lateinit var btnSetLargeItems: Button

    /**
     * Button to append a single item.<br><br>
     * 아이템 1개를 추가하는 버튼입니다.<br>
     */
    private lateinit var btnAddItem: Button

    /**
     * Button to insert a single item at top.<br><br>
     * 아이템 1개를 맨 앞에 삽입하는 버튼입니다.<br>
     */
    private lateinit var btnAddItemAt: Button

    /**
     * Button to append multiple items at once.<br><br>
     * 여러 아이템을 한 번에 추가하는 버튼입니다.<br>
     */
    private lateinit var btnAddItems: Button

    /**
     * Button to remove an item by value.<br><br>
     * 값 기준으로 아이템을 제거하는 버튼입니다.<br>
     */
    private lateinit var btnRemoveItem: Button

    /**
     * Button to remove an item by position.<br><br>
     * 포지션 기준으로 아이템을 제거하는 버튼입니다.<br>
     */
    private lateinit var btnRemoveAt: Button

    /**
     * Button to move an item in the list.<br><br>
     * 리스트 내 아이템을 이동하는 버튼입니다.<br>
     */
    private lateinit var btnMoveItem: Button

    /**
     * Button to replace an item at a position.<br><br>
     * 포지션 기준으로 아이템을 교체하는 버튼입니다.<br>
     */
    private lateinit var btnReplaceItem: Button

    /**
     * Button to clear all items.<br><br>
     * 모든 아이템을 제거하는 버튼입니다.<br>
     */
    private lateinit var btnRemoveAll: Button

    /**
     * Switch for diffExecutor injection option.<br><br>
     * diffExecutor 주입 옵션 스위치입니다.<br>
     */
    private lateinit var swUseDiffExecutor: SwitchCompat

    /**
     * Switch for DiffUtil enable option (normal adapters).<br><br>
     * 일반 어댑터 DiffUtil 활성 옵션 스위치입니다.<br>
     */
    private lateinit var swEnableDiffUtil: SwitchCompat

    /**
     * Switch for custom DiffUtil callback option (ListAdapter).<br><br>
     * ListAdapter 커스텀 DiffUtil 콜백 옵션 스위치입니다.<br>
     */
    private lateinit var swUseCustomDiffCallback: SwitchCompat

    /**
     * Switch for Simple adapter option (single only).<br><br>
     * 단일 타입용 Simple 어댑터 옵션 스위치입니다.<br>
     */
    private lateinit var swUseSimpleAdapter: SwitchCompat

    /**
     * RadioGroup for binding type selection.<br><br>
     * 바인딩 타입 선택용 라디오 그룹입니다.<br>
     */
    private lateinit var rgBindingType: RadioGroup

    /**
     * Radio button for normal binding type.<br><br>
     * 일반 바인딩 타입 라디오 버튼입니다.<br>
     */
    private lateinit var rBtnBindingNormal: RadioButton

    /**
     * Radio button for DataBinding type.<br><br>
     * DataBinding 타입 라디오 버튼입니다.<br>
     */
    private lateinit var rBtnBindingDataBinding: RadioButton

    /**
     * Radio button for ViewBinding type.<br><br>
     * ViewBinding 타입 라디오 버튼입니다.<br>
     */
    private lateinit var rBtnBindingViewBinding: RadioButton

    /**
     * Next id for newly created items.<br><br>
     * 새로 생성할 아이템의 다음 ID입니다.<br>
     */
    private var nextItemId: Long = 0L

    /**
     * Shared diff executor for injection examples.<br><br>
     * 주입 예제에 사용할 공용 diff executor입니다.<br>
     */
    private val diffExecutorService: ExecutorService = Executors.newSingleThreadExecutor()

    /**
     * Initializes UI and sets up example interactions.<br><br>
     * UI를 초기화하고 예제 동작을 설정합니다.<br>
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_temp_adapter_example)

        bindViews()
        setupListeners()
        configureOptionAvailability()

        rcvTempExample.layoutManager = LinearLayoutManager(this)
        tvExampleTitle.text = screenTitle
        refreshAdapter()
    }

    /**
     * Releases resources on activity destruction.<br><br>
     * 액티비티 종료 시 리소스를 해제합니다.<br>
     */
    override fun onDestroy() {
        diffExecutorService.shutdown()
        super.onDestroy()
    }

    /**
     * Binds UI views from the layout.<br><br>
     * 레이아웃의 UI 뷰를 바인딩합니다.<br>
     */
    private fun bindViews() {
        tvExampleTitle = findViewById(R.id.tvExampleTitle)
        tvExampleStatus = findViewById(R.id.tvExampleStatus)
        rcvTempExample = findViewById(R.id.rcvTempExample)

        btnResetItems = findViewById(R.id.btnResetItems)
        btnSetLargeItems = findViewById(R.id.btnSetLargeItems)
        btnAddItem = findViewById(R.id.btnAddItem)
        btnAddItemAt = findViewById(R.id.btnAddItemAt)
        btnAddItems = findViewById(R.id.btnAddItems)
        btnRemoveItem = findViewById(R.id.btnRemoveItem)
        btnRemoveAt = findViewById(R.id.btnRemoveAt)
        btnMoveItem = findViewById(R.id.btnMoveItem)
        btnReplaceItem = findViewById(R.id.btnReplaceItem)
        btnRemoveAll = findViewById(R.id.btnRemoveAll)

        swUseDiffExecutor = findViewById(R.id.swUseDiffExecutor)
        swEnableDiffUtil = findViewById(R.id.swEnableDiffUtil)
        swUseCustomDiffCallback = findViewById(R.id.swUseCustomDiffCallback)
        swUseSimpleAdapter = findViewById(R.id.swUseSimpleAdapter)

        rgBindingType = findViewById(R.id.rgBindingType)
        rBtnBindingNormal = findViewById(R.id.rBtnBindingNormal)
        rBtnBindingDataBinding = findViewById(R.id.rBtnBindingDataBinding)
        rBtnBindingViewBinding = findViewById(R.id.rBtnBindingViewBinding)
    }

    /**
     * Sets up click listeners and option toggles.<br><br>
     * 클릭 리스너와 옵션 토글을 설정합니다.<br>
     */
    private fun setupListeners() {
        btnResetItems.setOnClickListener { handleResetItems() }
        btnSetLargeItems.setOnClickListener { handleSetLargeItems() }
        btnAddItem.setOnClickListener { handleAddItem() }
        btnAddItemAt.setOnClickListener { handleAddItemAt() }
        btnAddItems.setOnClickListener { handleAddItems() }
        btnRemoveItem.setOnClickListener { handleRemoveItem() }
        btnRemoveAt.setOnClickListener { handleRemoveAt() }
        btnMoveItem.setOnClickListener { handleMoveItem() }
        btnReplaceItem.setOnClickListener { handleReplaceItem() }
        btnRemoveAll.setOnClickListener { handleRemoveAll() }

        swUseDiffExecutor.setOnCheckedChangeListener { _, _ -> refreshAdapter() }
        swEnableDiffUtil.setOnCheckedChangeListener { _, _ -> refreshAdapter() }
        swUseCustomDiffCallback.setOnCheckedChangeListener { _, _ -> refreshAdapter() }
        swUseSimpleAdapter.setOnCheckedChangeListener { _, _ -> refreshAdapter() }
        rgBindingType.setOnCheckedChangeListener { _, _ -> refreshAdapter() }
    }

    /**
     * Updates option availability based on adapter kind and mode.<br><br>
     * 어댑터 종류/모드에 따라 옵션 활성화를 업데이트합니다.<br>
     */
    private fun configureOptionAvailability() {
        swEnableDiffUtil.isEnabled = (adapterKind == TempAdapterKind.RV)
        swUseCustomDiffCallback.isEnabled = (adapterKind == TempAdapterKind.LIST)

        if (allowSimpleAdapter) {
            swUseSimpleAdapter.visibility = View.VISIBLE
            swUseSimpleAdapter.isEnabled = true
        } else {
            swUseSimpleAdapter.visibility = View.GONE
            swUseSimpleAdapter.isEnabled = false
            swUseSimpleAdapter.isChecked = false
        }
    }

    /**
     * Refreshes adapter based on current selection and options.<br><br>
     * 현재 선택과 옵션 기준으로 어댑터를 갱신합니다.<br>
     */
    private fun refreshAdapter() {
        // Build adapter configuration from UI state.<br><br>UI 상태로부터 어댑터 설정을 생성합니다.<br>
        val config = buildAdapterConfig()

        // Create adapter using Factory.<br><br>Factory로 어댑터를 생성합니다.<br>
        val adapter = TempAdapterFactory.createAdapter(config)

        rcvTempExample.adapter = adapter
        TempAdapterHelper.applyQueuePolicy(adapter) { status -> updateStatus(status) }
        TempAdapterHelper.bindClickListeners(adapter) { status -> updateStatus(status) }

        // Generate initial items using Generator.<br><br>Generator로 초기 아이템을 생성합니다.<br>
        val items = TempItemGenerator.generateDefaultItems(itemMode)
        updateNextItemId(items)
        TempAdapterHelper.submitItems(adapter, items, "setItems") { status -> updateStatus(status) }
    }

    /**
     * Builds TempAdapterConfig from current UI state.<br><br>
     * 현재 UI 상태로부터 TempAdapterConfig를 생성합니다.<br>
     */
    private fun buildAdapterConfig(): TempAdapterConfig {
        val bindingMode = when {
            rBtnBindingDataBinding.isChecked -> TempBindingMode.DATABINDING
            rBtnBindingViewBinding.isChecked -> TempBindingMode.VIEWBINDING
            else -> TempBindingMode.NORMAL
        }

        return TempAdapterConfig(
            adapterKind = adapterKind,
            itemMode = itemMode,
            bindingType = bindingMode.toBindingType(),
            useSimpleAdapter = allowSimpleAdapter && swUseSimpleAdapter.isChecked,
            enableDiffUtil = (adapterKind == TempAdapterKind.RV && swEnableDiffUtil.isChecked),
            useCustomDiffCallback = (adapterKind == TempAdapterKind.LIST && swUseCustomDiffCallback.isChecked),
            diffExecutor = if (swUseDiffExecutor.isChecked) diffExecutorService else null,
        )
    }

    /**
     * Updates next item id based on a new list.<br><br>
     * 새 리스트 기준으로 다음 아이템 ID를 업데이트합니다.<br>
     */
    private fun updateNextItemId(items: List<TempItem>) {
        // Max id from list or -1 when empty.<br><br>리스트의 최대 ID이며 비어있으면 -1입니다.<br>
        val maxId = items.maxOfOrNull { it.id } ?: -1L
        nextItemId = maxId + 1
    }

    /**
     * Creates a new item using current mode and next id.<br><br>
     * 현재 모드와 다음 ID로 새 아이템을 생성합니다.<br>
     */
    private fun createNewItem(label: String): TempItem {
        // Use Generator to create item with current index.<br><br>Generator를 사용해 현재 인덱스로 아이템을 생성합니다.<br>
        val item = TempItemGenerator.generateSingleItem(itemMode, nextItemId.toInt())
        nextItemId += 1
        return item.copy(title = label, description = "Created item")
    }

    /**
     * Returns the current adapter or null if missing.<br><br>
     * 현재 어댑터를 반환하며 없으면 null을 반환합니다.<br>
     */
    private fun requireAdapter(): RecyclerView.Adapter<*>? {
        val adapter = rcvTempExample.adapter
        if (adapter == null) {
            updateStatus("Adapter is not ready")
        }
        return adapter
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
     * Updates status text with the provided message.<br><br>
     * 제공된 메시지로 상태 텍스트를 업데이트합니다.<br>
     */
    private fun updateStatus(message: String) {
        tvExampleStatus.text = message
    }

    /**
     * Handles reset action to default items.<br><br>
     * 기본 아이템으로 초기화하는 동작을 처리합니다.<br>
     */
    private fun handleResetItems() {
        val adapter = requireAdapter() ?: return
        // Use Generator to create default items.<br><br>Generator를 사용해 기본 아이템을 생성합니다.<br>
        val items = TempItemGenerator.generateDefaultItems(itemMode)
        updateNextItemId(items)
        TempAdapterHelper.submitItems(adapter, items, "setItems") { status -> updateStatus(status) }
    }

    /**
     * Handles set large items action for stress testing.<br><br>
     * 대량 아이템으로 변경하는 동작을 처리합니다.<br>
     */
    private fun handleSetLargeItems() {
        val adapter = requireAdapter() ?: return
        // Use Generator to create large items.<br><br>Generator를 사용해 대량 아이템을 생성합니다.<br>
        val items = TempItemGenerator.generateLargeItems(itemMode)
        updateNextItemId(items)
        TempAdapterHelper.submitItems(adapter, items, "setLargeItems") { status -> updateStatus(status) }
    }

    /**
     * Handles adding a single item at the end.<br><br>
     * 아이템 1개를 끝에 추가하는 동작을 처리합니다.<br>
     */
    private fun handleAddItem() {
        val adapter = requireAdapter() ?: return
        val newItem = createNewItem("Add")
        val handled = withRootAdapter(
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
        if (!handled) {
            updateStatus("Unsupported adapter for addItem")
        }
    }

    /**
     * Handles adding a single item at the top position.<br><br>
     * 아이템 1개를 최상단에 추가하는 동작을 처리합니다.<br>
     */
    private fun handleAddItemAt() {
        val adapter = requireAdapter() ?: return
        val newItem = createNewItem("AddAt")
        val handled = withRootAdapter(
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
        if (!handled) {
            updateStatus("Unsupported adapter for addItemAt")
        }
    }

    /**
     * Handles adding multiple items in batch.<br><br>
     * 여러 아이템을 일괄 추가하는 동작을 처리합니다.<br>
     */
    private fun handleAddItems() {
        val adapter = requireAdapter() ?: return
        val newItems = List(ADD_ITEMS_COUNT) { index ->
            // Batch label for each item.<br><br>배치 아이템 라벨입니다.<br>
            val label = "Batch${index + 1}"
            createNewItem(label)
        }
        val handled = withRootAdapter(
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
        if (!handled) {
            updateStatus("Unsupported adapter for addItems")
        }
    }

    /**
     * Handles removing a single item by value.<br><br>
     * 값 기준으로 아이템 1개를 제거하는 동작을 처리합니다.<br>
     */
    private fun handleRemoveItem() {
        val adapter = requireAdapter() ?: return
        val items = TempAdapterHelper.getItems(adapter)
        if (items.isEmpty()) {
            updateStatus("removeItem -> empty list")
            return
        }
        val target = items.first()
        val handled = withRootAdapter(
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
        if (!handled) {
            updateStatus("Unsupported adapter for removeItem")
        }
    }

    /**
     * Handles removing an item at a position.<br><br>
     * 포지션 기준으로 아이템을 제거하는 동작을 처리합니다.<br>
     */
    private fun handleRemoveAt() {
        val adapter = requireAdapter() ?: return
        val items = TempAdapterHelper.getItems(adapter)
        if (items.isEmpty()) {
            updateStatus("removeAt -> empty list")
            return
        }
        val targetIndex = items.lastIndex
        val handled = withRootAdapter(
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
        if (!handled) {
            updateStatus("Unsupported adapter for removeAt")
        }
    }

    /**
     * Handles moving an item from top to bottom.<br><br>
     * 아이템을 상단에서 하단으로 이동하는 동작을 처리합니다.<br>
     */
    private fun handleMoveItem() {
        val adapter = requireAdapter() ?: return
        val items = TempAdapterHelper.getItems(adapter)
        if (items.size < 2) {
            updateStatus("moveItem -> not enough items")
            return
        }
        val from = 0
        val to = items.lastIndex
        val handled = withRootAdapter(
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
        if (!handled) {
            updateStatus("Unsupported adapter for moveItem")
        }
    }

    /**
     * Handles replacing an item at the middle position.<br><br>
     * 중간 포지션 아이템을 교체하는 동작을 처리합니다.<br>
     */
    private fun handleReplaceItem() {
        val adapter = requireAdapter() ?: return
        val items = TempAdapterHelper.getItems(adapter)
        if (items.isEmpty()) {
            updateStatus("replaceItemAt -> empty list")
            return
        }
        val targetIndex = items.size / 2
        val targetItem = items[targetIndex]
        val updatedItem = TempItemFactory.createUpdatedItem(targetItem, "Replaced")
        val handled = withRootAdapter(
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
        if (!handled) {
            updateStatus("Unsupported adapter for replaceItemAt")
        }
    }

    /**
     * Handles clearing all items.<br><br>
     * 모든 아이템을 제거하는 동작을 처리합니다.<br>
     */
    private fun handleRemoveAll() {
        val adapter = requireAdapter() ?: return
        val handled = withRootAdapter(
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
        if (!handled) {
            updateStatus("Unsupported adapter for removeAll")
        }
    }

    private companion object {
        /**
         * Default item count used for initial lists.<br><br>
         * 초기 리스트에 사용하는 기본 아이템 개수입니다.<br>
         */
        private const val DEFAULT_ITEM_COUNT: Int = 20

        /**
         * Large item count used for stress testing.<br><br>
         * 스트레스 테스트에 사용하는 대량 아이템 개수입니다.<br>
         */
        private const val LARGE_ITEM_COUNT: Int = 200

        /**
         * Batch size for addItems operation.<br><br>
         * addItems 연산에서 사용하는 배치 크기입니다.<br>
         */
        private const val ADD_ITEMS_COUNT: Int = 3

        /**
         * Max pending queue size for examples.<br><br>
         * 예제에서 사용하는 최대 대기 큐 크기입니다.<br>
         */
        private const val DEFAULT_QUEUE_MAX_PENDING: Int = 100
    }
}
