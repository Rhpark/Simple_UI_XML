package kr.open.library.simpleui_xml.temp.ui

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kr.open.library.simple_ui.xml.ui.components.activity.binding.BaseDataBindingActivity
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.databinding.ActivityTempAdapterExampleBinding
import kr.open.library.simpleui_xml.temp.data.TempItem
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Base activity for temp adapter example screens.<br><br>
 * Temp 어댑터 예제 화면의 공통 베이스 액티비티입니다.<br>
 */
abstract class TempAdapterExampleBaseActivity :
    BaseDataBindingActivity<ActivityTempAdapterExampleBinding>(R.layout.activity_temp_adapter_example) {
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
     * Binding accessor for this Activity.<br><br>
     * 이 Activity의 바인딩 접근자입니다.<br>
     */
    private val binding: ActivityTempAdapterExampleBinding
        get() = getBinding()

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
    override fun onCreate(binding: ActivityTempAdapterExampleBinding, savedInstanceState: Bundle?) {
        binding.tvExampleTitle.text = screenTitle
        binding.rcvTempExample.layoutManager = LinearLayoutManager(this)

        configureOptionAvailability(binding)
        applyDefaultOptions(binding)
        setupListeners(binding)
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
     * Applies default option values for the screen.<br><br>
     * 화면별 기본 옵션 값을 적용합니다.<br>
     */
    protected open fun applyDefaultOptions(binding: ActivityTempAdapterExampleBinding) {}

    /**
     * Creates an adapter for the provided configuration.<br><br>
     * 제공된 설정으로 어댑터를 생성합니다.<br>
     */
    protected abstract fun createAdapter(config: TempAdapterConfig): RecyclerView.Adapter<*>

    /**
     * Sets up click listeners and option toggles.<br><br>
     * 클릭 리스너와 옵션 토글을 설정합니다.<br>
     */
    private fun setupListeners(binding: ActivityTempAdapterExampleBinding) {
        binding.btnResetItems.setOnClickListener { handleResetItems() }
        binding.btnSetLargeItems.setOnClickListener { handleSetLargeItems() }
        binding.btnAddItem.setOnClickListener { handleAddItem() }
        binding.btnAddItemAt.setOnClickListener { handleAddItemAt() }
        binding.btnAddItems.setOnClickListener { handleAddItems() }
        binding.btnRemoveItem.setOnClickListener { handleRemoveItem() }
        binding.btnRemoveAt.setOnClickListener { handleRemoveAt() }
        binding.btnMoveItem.setOnClickListener { handleMoveItem() }
        binding.btnReplaceItem.setOnClickListener { handleReplaceItem() }
        binding.btnRemoveAll.setOnClickListener { handleRemoveAll() }

        binding.swUseDiffExecutor.setOnCheckedChangeListener { _, _ -> refreshAdapter() }
        binding.swEnableDiffUtil.setOnCheckedChangeListener { _, _ -> refreshAdapter() }
        binding.swUseCustomDiffCallback.setOnCheckedChangeListener { _, _ -> refreshAdapter() }
        binding.swUseSimpleAdapter.setOnCheckedChangeListener { _, _ -> refreshAdapter() }
        binding.rgBindingType.setOnCheckedChangeListener { _, _ -> refreshAdapter() }
    }

    /**
     * Updates option availability based on adapter kind and mode.<br><br>
     * 어댑터 종류/모드에 따라 옵션 활성화를 업데이트합니다.<br>
     */
    private fun configureOptionAvailability(binding: ActivityTempAdapterExampleBinding) {
        binding.swEnableDiffUtil.isEnabled = (adapterKind == TempAdapterKind.RV)
        binding.swUseCustomDiffCallback.isEnabled = (adapterKind == TempAdapterKind.LIST)

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
        val config = buildAdapterConfig()
        val adapter = createAdapter(config)

        binding.rcvTempExample.adapter = adapter

        if (!TempAdapterHelper.applyQueuePolicy(adapter) { status -> updateStatus(status) }) {
            updateStatus("Unsupported adapter for queue policy")
        }

        if (!TempAdapterHelper.bindClickListeners(adapter) { status -> updateStatus(status) }) {
            updateStatus("Unsupported adapter for listeners")
        }

        val items = TempItemGenerator.generateDefaultItems(itemMode)
        updateNextItemId(items)
        if (!TempAdapterHelper.submitItems(adapter, items, "setItems") { status -> updateStatus(status) }) {
            updateStatus("Unsupported adapter for setItems")
        }
    }

    /**
     * Builds TempAdapterConfig from current UI state.<br><br>
     * 현재 UI 상태로부터 TempAdapterConfig를 생성합니다.<br>
     */
    private fun buildAdapterConfig(): TempAdapterConfig {
        val bindingMode =
            when {
                binding.rBtnBindingDataBinding.isChecked -> TempBindingMode.DATABINDING
                binding.rBtnBindingViewBinding.isChecked -> TempBindingMode.VIEWBINDING
                else -> TempBindingMode.NORMAL
            }

        return TempAdapterConfig(
            adapterKind = adapterKind,
            itemMode = itemMode,
            bindingType = bindingMode.toBindingType(),
            useSimpleAdapter = allowSimpleAdapter && binding.swUseSimpleAdapter.isChecked,
            enableDiffUtil = (adapterKind == TempAdapterKind.RV && binding.swEnableDiffUtil.isChecked),
            useCustomDiffCallback = (adapterKind == TempAdapterKind.LIST && binding.swUseCustomDiffCallback.isChecked),
            diffExecutor = if (binding.swUseDiffExecutor.isChecked) diffExecutorService else null,
        )
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
        val item = TempItemGenerator.generateSingleItem(itemMode, nextItemId.toInt())
        nextItemId += 1
        return item.copy(title = label, description = "Created item")
    }

    /**
     * Returns the current adapter or null if missing.<br><br>
     * 현재 어댑터를 반환하며 없으면 null을 반환합니다.<br>
     */
    private fun requireAdapter(): RecyclerView.Adapter<*>? {
        val adapter = binding.rcvTempExample.adapter
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
        binding.tvExampleStatus.text = message
    }

    /**
     * Handles reset action to default items.<br><br>
     * 기본 아이템으로 초기화하는 동작을 처리합니다.<br>
     */
    private fun handleResetItems() {
        val adapter = requireAdapter() ?: return
        val items = TempItemGenerator.generateDefaultItems(itemMode)
        updateNextItemId(items)
        if (!TempAdapterHelper.submitItems(adapter, items, "setItems") { status -> updateStatus(status) }) {
            updateStatus("Unsupported adapter for setItems")
        }
    }

    /**
     * Handles set large items action for stress testing.<br><br>
     * 대량 아이템으로 변경하는 동작을 처리합니다.<br>
     */
    private fun handleSetLargeItems() {
        val adapter = requireAdapter() ?: return
        val items = TempItemGenerator.generateLargeItems(itemMode)
        updateNextItemId(items)
        if (!TempAdapterHelper.submitItems(adapter, items, "setLargeItems") { status -> updateStatus(status) }) {
            updateStatus("Unsupported adapter for setLargeItems")
        }
    }

    /**
     * Handles adding a single item at the end.<br><br>
     * 아이템 1개를 끝에 추가하는 동작을 처리합니다.<br>
     */
    private fun handleAddItem() {
        val adapter = requireAdapter() ?: return
        val newItem = createNewItem("Add")
        if (!TempAdapterHelper.addItem(adapter, newItem) { status -> updateStatus(status) }) {
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
        if (!TempAdapterHelper.addItemAt(adapter, 0, newItem) { status -> updateStatus(status) }) {
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
            val label = "Batch${index + 1}"
            createNewItem(label)
        }
        if (!TempAdapterHelper.addItems(adapter, newItems) { status -> updateStatus(status) }) {
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
        if (!TempAdapterHelper.removeItem(adapter, target) { status -> updateStatus(status) }) {
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
        if (!TempAdapterHelper.removeAt(adapter, targetIndex) { status -> updateStatus(status) }) {
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
        if (!TempAdapterHelper.moveItem(adapter, from, to) { status -> updateStatus(status) }) {
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
        val updatedItem = TempItemGenerator.updateItem(targetItem, "Replaced")
        if (!TempAdapterHelper.replaceItemAt(adapter, targetIndex, updatedItem) { status -> updateStatus(status) }) {
            updateStatus("Unsupported adapter for replaceItemAt")
        }
    }

    /**
     * Handles clearing all items.<br><br>
     * 모든 아이템을 제거하는 동작을 처리합니다.<br>
     */
    private fun handleRemoveAll() {
        val adapter = requireAdapter() ?: return
        if (!TempAdapterHelper.removeAll(adapter) { status -> updateStatus(status) }) {
            updateStatus("Unsupported adapter for removeAll")
        }
    }

    private companion object {
        /**
         * Batch size for addItems operation.<br><br>
         * addItems 연산에서 사용하는 배치 크기입니다.<br>
         */
        private const val ADD_ITEMS_COUNT: Int = 3
    }
}
