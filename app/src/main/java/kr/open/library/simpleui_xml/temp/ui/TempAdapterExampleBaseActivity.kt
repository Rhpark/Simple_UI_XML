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
import kr.open.library.simple_ui.xml.ui.adapter.queue.QueueDebugEvent
import kr.open.library.simple_ui.xml.ui.adapter.queue.QueueEventType
import kr.open.library.simple_ui.xml.ui.adapter.queue.QueueOverflowPolicy
import kr.open.library.simple_ui.xml.ui.temp.base.OperationFailure
import kr.open.library.simple_ui.xml.ui.temp.base.OperationFailureInfo
import kr.open.library.simple_ui.xml.ui.temp.base.list.RootListAdapterCore
import kr.open.library.simple_ui.xml.ui.temp.base.list.diffcallback.DefaultDiffCallback
import kr.open.library.simple_ui.xml.ui.temp.base.normal.RootRcvAdapterCore
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.temp.adapter.listadapter.databinding.TempMultiDataBindingListAdapter
import kr.open.library.simpleui_xml.temp.adapter.listadapter.databinding.TempSimpleSingleDataBindingListAdapter
import kr.open.library.simpleui_xml.temp.adapter.listadapter.databinding.TempSingleDataBindingListAdapter
import kr.open.library.simpleui_xml.temp.adapter.listadapter.normal.TempMultiNormalListAdapter
import kr.open.library.simpleui_xml.temp.adapter.listadapter.normal.TempSimpleSingleNormalListAdapter
import kr.open.library.simpleui_xml.temp.adapter.listadapter.normal.TempSingleNormalListAdapter
import kr.open.library.simpleui_xml.temp.adapter.listadapter.viewbinding.TempMultiViewBindingListAdapter
import kr.open.library.simpleui_xml.temp.adapter.listadapter.viewbinding.TempSimpleSingleViewBindingListAdapter
import kr.open.library.simpleui_xml.temp.adapter.listadapter.viewbinding.TempSingleViewBindingListAdapter
import kr.open.library.simpleui_xml.temp.adapter.recyclerview.databinding.TempMultiDataBindingAdapter
import kr.open.library.simpleui_xml.temp.adapter.recyclerview.databinding.TempSimpleSingleDataBindingAdapter
import kr.open.library.simpleui_xml.temp.adapter.recyclerview.databinding.TempSingleDataBindingAdapter
import kr.open.library.simpleui_xml.temp.adapter.recyclerview.normal.TempMultiNormalAdapter
import kr.open.library.simpleui_xml.temp.adapter.recyclerview.normal.TempSimpleSingleNormalAdapter
import kr.open.library.simpleui_xml.temp.adapter.recyclerview.normal.TempSingleNormalAdapter
import kr.open.library.simpleui_xml.temp.adapter.recyclerview.viewbinding.TempMultiViewBindingAdapter
import kr.open.library.simpleui_xml.temp.adapter.recyclerview.viewbinding.TempSimpleSingleViewBindingAdapter
import kr.open.library.simpleui_xml.temp.adapter.recyclerview.viewbinding.TempSingleViewBindingAdapter
import kr.open.library.simpleui_xml.temp.data.TempItem
import kr.open.library.simpleui_xml.temp.data.TempItemFactory
import kr.open.library.simpleui_xml.temp.data.TempItemType
import kr.open.library.simpleui_xml.temp.util.TempItemDiffCallback
import java.util.concurrent.Executor
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

        kr.open.library.simple_ui.core.logcat.Logx.d("configureOptionAvailability: allowSimpleAdapter=$allowSimpleAdapter, itemMode=$itemMode, adapterKind=$adapterKind")

        if (allowSimpleAdapter) {
            swUseSimpleAdapter.visibility = View.VISIBLE
            swUseSimpleAdapter.isEnabled = true
            kr.open.library.simple_ui.core.logcat.Logx.d("Simple switch set to VISIBLE")
        } else {
            swUseSimpleAdapter.visibility = View.GONE
            swUseSimpleAdapter.isEnabled = false
            swUseSimpleAdapter.isChecked = false
            kr.open.library.simple_ui.core.logcat.Logx.d("Simple switch set to GONE")
        }
    }

    /**
     * Refreshes adapter based on current selection and options.<br><br>
     * 현재 선택과 옵션 기준으로 어댑터를 갱신합니다.<br>
     */
    private fun refreshAdapter() {
        val bindingMode = resolveBindingMode()
        val useSimpleAdapter = allowSimpleAdapter && swUseSimpleAdapter.isChecked
        val dependencies = buildDependencies()
        val adapter = createAdapter(bindingMode, useSimpleAdapter, dependencies)

        rcvTempExample.adapter = adapter
        applyQueuePolicy(adapter)
        bindAdapterListeners(adapter)

        val items = createItemsForMode(itemMode, DEFAULT_ITEM_COUNT)
        updateNextItemId(items)
        submitItems(adapter, items, "setItems")
    }

    /**
     * Resolves current binding mode selection.<br><br>
     * 현재 바인딩 모드 선택 값을 해석합니다.<br>
     */
    private fun resolveBindingMode(): TempBindingMode =
        when {
            rBtnBindingDataBinding.isChecked -> TempBindingMode.DATABINDING
            rBtnBindingViewBinding.isChecked -> TempBindingMode.VIEWBINDING
            rBtnBindingNormal.isChecked -> TempBindingMode.NORMAL
            else -> TempBindingMode.NORMAL
        }

    /**
     * Builds adapter dependencies from current UI options.<br><br>
     * 현재 UI 옵션을 기반으로 어댑터 의존성을 생성합니다.<br>
     */
    private fun buildDependencies(): TempAdapterDependencies {
        val diffExecutor = if (swUseDiffExecutor.isChecked) diffExecutorService else null
        val diffCallback =
            if (adapterKind == TempAdapterKind.LIST && swUseCustomDiffCallback.isChecked) {
                TempItemDiffCallback()
            } else {
                null
            }
        val diffUtilEnabled = (adapterKind == TempAdapterKind.RV && swEnableDiffUtil.isChecked)
        return TempAdapterDependencies(
            diffExecutor = diffExecutor,
            diffCallback = diffCallback,
            diffUtilEnabled = diffUtilEnabled,
        )
    }

    /**
     * Creates an adapter based on current options.<br><br>
     * 현재 옵션에 맞는 어댑터를 생성합니다.<br>
     */
    private fun createAdapter(
        bindingMode: TempBindingMode,
        useSimpleAdapter: Boolean,
        deps: TempAdapterDependencies,
    ): RecyclerView.Adapter<*> = when (adapterKind) {
        TempAdapterKind.RV -> createRcvAdapter(bindingMode, useSimpleAdapter, deps)
        TempAdapterKind.LIST -> createListAdapter(bindingMode, useSimpleAdapter, deps)
    }

    /**
     * Creates a RecyclerView.Adapter example adapter.<br><br>
     * RecyclerView.Adapter 예제 어댑터를 생성합니다.<br>
     */
    private fun createRcvAdapter(
        bindingMode: TempBindingMode,
        useSimpleAdapter: Boolean,
        deps: TempAdapterDependencies,
    ): RecyclerView.Adapter<*> =
        when (itemMode) {
            TempItemMode.SINGLE -> createRcvSingleAdapter(bindingMode, useSimpleAdapter, deps.diffUtilEnabled, deps.diffExecutor)
            TempItemMode.MULTI -> createRcvMultiAdapter(bindingMode, deps.diffUtilEnabled, deps.diffExecutor)
        }

    /**
     * Creates a RecyclerView.Adapter single-type adapter.<br><br>
     * RecyclerView.Adapter 단일 타입 어댑터를 생성합니다.<br>
     */
    private fun createRcvSingleAdapter(
        bindingMode: TempBindingMode,
        useSimpleAdapter: Boolean,
        diffUtilEnabled: Boolean,
        diffExecutor: Executor?,
    ): RecyclerView.Adapter<*> = when (bindingMode) {
        TempBindingMode.NORMAL ->
            if (useSimpleAdapter) {
                TempSimpleSingleNormalAdapter(diffUtilEnabled = diffUtilEnabled, diffExecutor = diffExecutor)
            } else {
                TempSingleNormalAdapter(diffUtilEnabled = diffUtilEnabled, diffExecutor = diffExecutor)
            }
        TempBindingMode.DATABINDING ->
            if (useSimpleAdapter) {
                TempSimpleSingleDataBindingAdapter(diffUtilEnabled = diffUtilEnabled, diffExecutor = diffExecutor)
            } else {
                TempSingleDataBindingAdapter(diffUtilEnabled = diffUtilEnabled, diffExecutor = diffExecutor)
            }
        TempBindingMode.VIEWBINDING ->
            if (useSimpleAdapter) {
                TempSimpleSingleViewBindingAdapter(diffUtilEnabled = diffUtilEnabled, diffExecutor = diffExecutor)
            } else {
                TempSingleViewBindingAdapter(diffUtilEnabled = diffUtilEnabled, diffExecutor = diffExecutor)
            }
    }

    /**
     * Creates a RecyclerView.Adapter multi-type adapter.<br><br>
     * RecyclerView.Adapter 다중 타입 어댑터를 생성합니다.<br>
     */
    private fun createRcvMultiAdapter(
        bindingMode: TempBindingMode,
        diffUtilEnabled: Boolean,
        diffExecutor: Executor?,
    ): RecyclerView.Adapter<*> = when (bindingMode) {
        TempBindingMode.NORMAL ->
            TempMultiNormalAdapter(diffUtilEnabled = diffUtilEnabled, diffExecutor = diffExecutor)
        TempBindingMode.DATABINDING ->
            TempMultiDataBindingAdapter(diffUtilEnabled = diffUtilEnabled, diffExecutor = diffExecutor)
        TempBindingMode.VIEWBINDING ->
            TempMultiViewBindingAdapter(diffUtilEnabled = diffUtilEnabled, diffExecutor = diffExecutor)
    }

    /**
     * Creates a ListAdapter example adapter.<br><br>
     * ListAdapter 예제 어댑터를 생성합니다.<br>
     */
    private fun createListAdapter(
        bindingMode: TempBindingMode,
        useSimpleAdapter: Boolean,
        deps: TempAdapterDependencies,
    ): RecyclerView.Adapter<*> {
        val diffCallback = resolveDiffCallback(deps)
        return when (itemMode) {
            TempItemMode.SINGLE -> createListSingleAdapter(bindingMode, useSimpleAdapter, diffCallback, deps.diffExecutor)
            TempItemMode.MULTI -> createListMultiAdapter(bindingMode, diffCallback, deps.diffExecutor)
        }
    }

    /**
     * Creates a ListAdapter single-type adapter.<br><br>
     * ListAdapter 단일 타입 어댑터를 생성합니다.<br>
     */
    private fun createListSingleAdapter(
        bindingMode: TempBindingMode,
        useSimpleAdapter: Boolean,
        diffCallback: androidx.recyclerview.widget.DiffUtil.ItemCallback<TempItem>,
        diffExecutor: Executor?,
    ): RecyclerView.Adapter<*> = when (bindingMode) {
        TempBindingMode.NORMAL ->
            if (useSimpleAdapter) {
                TempSimpleSingleNormalListAdapter(diffCallback = diffCallback, diffExecutor = diffExecutor)
            } else {
                TempSingleNormalListAdapter(diffCallback = diffCallback, diffExecutor = diffExecutor)
            }
        TempBindingMode.DATABINDING ->
            if (useSimpleAdapter) {
                TempSimpleSingleDataBindingListAdapter(diffCallback = diffCallback, diffExecutor = diffExecutor)
            } else {
                TempSingleDataBindingListAdapter(diffCallback = diffCallback, diffExecutor = diffExecutor)
            }
        TempBindingMode.VIEWBINDING ->
            if (useSimpleAdapter) {
                TempSimpleSingleViewBindingListAdapter(diffCallback = diffCallback, diffExecutor = diffExecutor)
            } else {
                TempSingleViewBindingListAdapter(diffCallback = diffCallback, diffExecutor = diffExecutor)
            }
    }

    /**
     * Creates a ListAdapter multi-type adapter.<br><br>
     * ListAdapter 다중 타입 어댑터를 생성합니다.<br>
     */
    private fun createListMultiAdapter(
        bindingMode: TempBindingMode,
        diffCallback: androidx.recyclerview.widget.DiffUtil.ItemCallback<TempItem>,
        diffExecutor: Executor?,
    ): RecyclerView.Adapter<*> = when (bindingMode) {
        TempBindingMode.NORMAL ->
            TempMultiNormalListAdapter(diffCallback = diffCallback, diffExecutor = diffExecutor)
        TempBindingMode.DATABINDING ->
            TempMultiDataBindingListAdapter(diffCallback = diffCallback, diffExecutor = diffExecutor)
        TempBindingMode.VIEWBINDING ->
            TempMultiViewBindingListAdapter(diffCallback = diffCallback, diffExecutor = diffExecutor)
    }

    /**
     * Resolves DiffUtil callback for ListAdapter examples.<br><br>
     * ListAdapter 예제용 DiffUtil 콜백을 해석합니다.<br>
     */
    private fun resolveDiffCallback(deps: TempAdapterDependencies): androidx.recyclerview.widget.DiffUtil.ItemCallback<TempItem> =
        deps.diffCallback ?: DefaultDiffCallback()

    /**
     * Creates initial items for the given mode and size.<br><br>
     * 지정 모드와 크기에 맞는 초기 아이템을 생성합니다.<br>
     */
    private fun createItemsForMode(mode: TempItemMode, size: Int): List<TempItem> = when (mode) {
        TempItemMode.SINGLE -> TempItemFactory.createSingleItems(size)
        TempItemMode.MULTI -> TempItemFactory.createMultiItems(size)
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
    private fun createNewItem(mode: TempItemMode, label: String): TempItem {
        // Snapshot of the next id.<br><br>다음 ID 스냅샷입니다.<br>
        val itemId = nextItemId
        // Type resolved by mode and id.<br><br>모드와 ID 기준으로 결정된 타입입니다.<br>
        val type = resolveTypeForMode(mode, itemId)
        val item = TempItemFactory.createItem(itemId, type, label)
        nextItemId += 1
        return item
    }

    /**
     * Resolves item type based on mode and id.<br><br>
     * 모드와 ID 기준으로 아이템 타입을 결정합니다.<br>
     */
    private fun resolveTypeForMode(mode: TempItemMode, id: Long): TempItemType = if (mode == TempItemMode.SINGLE) {
        TempItemType.PRIMARY
    } else {
        if (id % 2L == 0L) TempItemType.PRIMARY else TempItemType.SECONDARY
    }

    /**
     * Submits items to the adapter using queue APIs with callback.<br><br>
     * 큐 API를 사용해 아이템을 제출하고 콜백을 처리합니다.<br>
     */
    private fun submitItems(adapter: RecyclerView.Adapter<*>, items: List<TempItem>, actionLabel: String) {
        val handled = withRootAdapter(
            adapter = adapter,
            onRcv = { rcvAdapter ->
                rcvAdapter.setItems(items) { success ->
                    updateStatus("$actionLabel(${items.size}) -> $success")
                }
            },
            onList = { listAdapter ->
                listAdapter.setItems(items) { success ->
                    updateStatus("$actionLabel(${items.size}) -> $success")
                }
            },
        )
        if (!handled) {
            updateStatus("Unsupported adapter for $actionLabel")
        }
    }

    /**
     * Binds click and long-click listeners to the adapter.<br><br>
     * 어댑터에 클릭/롱클릭 리스너를 바인딩합니다.<br>
     */
    private fun bindAdapterListeners(adapter: RecyclerView.Adapter<*>) {
        val handled = withRootAdapter(
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
        if (!handled) {
            updateStatus("Unsupported adapter for listeners")
        }
    }

    /**
     * Applies queue policy and failure listener to the adapter.<br><br>
     * 어댑터에 큐 정책과 실패 리스너를 적용합니다.<br>
     */
    private fun applyQueuePolicy(adapter: RecyclerView.Adapter<*>) {
        val handled = withRootAdapter(
            adapter = adapter,
            onRcv = { rcvAdapter ->
                rcvAdapter.setQueuePolicy(DEFAULT_QUEUE_MAX_PENDING, QueueOverflowPolicy.DROP_NEW)
                rcvAdapter.setOnOperationFailureListener { info ->
                    updateStatus("queueFail(${formatFailure(info)})")
                }
                rcvAdapter.setQueueDebugListener { event ->
                    if (event.type == QueueEventType.ERROR || event.type == QueueEventType.DROPPED) {
                        updateStatus(formatQueueEvent(event))
                    }
                }
            },
            onList = { listAdapter ->
                listAdapter.setQueuePolicy(DEFAULT_QUEUE_MAX_PENDING, QueueOverflowPolicy.DROP_NEW)
                listAdapter.setOnOperationFailureListener { info ->
                    updateStatus("queueFail(${formatFailure(info)})")
                }
                listAdapter.setQueueDebugListener { event ->
                    if (event.type == QueueEventType.ERROR || event.type == QueueEventType.DROPPED) {
                        updateStatus(formatQueueEvent(event))
                    }
                }
            },
        )
        if (!handled) {
            updateStatus("Unsupported adapter for queue policy")
        }
    }

    /**
     * Formats failure details for UI display.<br><br>
     * 실패 상세 정보를 표시용 문자열로 변환합니다.<br>
     */
    private fun formatFailure(info: OperationFailureInfo): String {
        val detail =
            when (val failure = info.failure) {
                is OperationFailure.Validation -> "validation=${failure.message}"
                is OperationFailure.Exception -> "exception=${failure.error.message ?: "unknown"}"
                is OperationFailure.Dropped -> "dropped=${failure.reason}"
            }
        return "op=${info.operationName}, $detail"
    }

    /**
     * Formats queue debug events for UI display.<br><br>
     * 큐 디버그 이벤트를 표시용 문자열로 변환합니다.<br>
     */
    private fun formatQueueEvent(event: QueueDebugEvent): String {
        val name = event.operationName ?: "unknown"
        val drop = event.dropReason?.let { ", drop=$it" } ?: ""
        val message = event.message?.let { ", msg=$it" } ?: ""
        return "queue:${event.type} op=$name, pending=${event.pendingSize}$drop$message"
    }

    /**
     * Retrieves items from the current adapter.<br><br>
     * 현재 어댑터의 아이템을 조회합니다.<br>
     */
    @Suppress("UNCHECKED_CAST")
    private fun getItems(adapter: RecyclerView.Adapter<*>): List<TempItem> = when (adapter) {
        is RootRcvAdapterCore<*, *> -> (adapter as RootRcvAdapterCore<TempItem, *>).getItems()
        is RootListAdapterCore<*, *> -> (adapter as RootListAdapterCore<TempItem, *>).getItems()
        else -> emptyList()
    }

    /**
     * Executes the action for RootRcv or RootList adapter types.<br><br>
     * RootRcv 또는 RootList 어댑터 타입에 맞춰 동작을 실행합니다.<br>
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
        val items = createItemsForMode(itemMode, DEFAULT_ITEM_COUNT)
        updateNextItemId(items)
        submitItems(adapter, items, "setItems")
    }

    /**
     * Handles set large items action for stress testing.<br><br>
     * 대량 아이템으로 변경하는 동작을 처리합니다.<br>
     */
    private fun handleSetLargeItems() {
        val adapter = requireAdapter() ?: return
        val items = createItemsForMode(itemMode, LARGE_ITEM_COUNT)
        updateNextItemId(items)
        submitItems(adapter, items, "setLargeItems")
    }

    /**
     * Handles adding a single item at the end.<br><br>
     * 아이템 1개를 끝에 추가하는 동작을 처리합니다.<br>
     */
    private fun handleAddItem() {
        val adapter = requireAdapter() ?: return
        val newItem = createNewItem(itemMode, "Add")
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
        val newItem = createNewItem(itemMode, "AddAt")
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
            createNewItem(itemMode, label)
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
        val items = getItems(adapter)
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
        val items = getItems(adapter)
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
        val items = getItems(adapter)
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
        val items = getItems(adapter)
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
