package kr.open.library.simpleui_xml.recyclerview.new_

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import kr.open.library.simple_ui.xml.ui.adapter.list.base.diffutil.RcvListDiffUtilCallBack
import kr.open.library.simple_ui.xml.ui.adapter.list.simple.SimpleRcvDataBindingListAdapter
import kr.open.library.simple_ui.xml.ui.adapter.list.simple.SimpleRcvViewBindingListAdapter
import kr.open.library.simple_ui.xml.ui.adapter.normal.base.BaseRcvAdapter
import kr.open.library.simple_ui.xml.ui.adapter.normal.simple.SimpleRcvDataBindingAdapter
import kr.open.library.simple_ui.xml.ui.adapter.normal.simple.SimpleRcvViewBindingAdapter
import kr.open.library.simple_ui.xml.ui.adapter.viewholder.BaseRcvViewBindingViewHolder
import kr.open.library.simple_ui.xml.ui.components.activity.binding.BaseDataBindingActivity
import kr.open.library.simple_ui.xml.ui.view.recyclerview.ScrollDirection
import kr.open.library.simple_ui.xml.ui.view.recyclerview.ScrollEdge
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.databinding.ActivityRecyclerviewBinding
import kr.open.library.simpleui_xml.databinding.ItemRcvTextviewBinding
import kr.open.library.simpleui_xml.databinding.ItemRcvTextviewViewBinding
import kr.open.library.simpleui_xml.recyclerview.model.RcvItem
import kr.open.library.simpleui_xml.recyclerview.model.SampleItem
import kr.open.library.simpleui_xml.recyclerview.new_.adapter.CustomListAdapter

class RecyclerViewActivity : BaseDataBindingActivity<ActivityRecyclerviewBinding>(R.layout.activity_recyclerview) {
    private enum class AdapterMode {
        SIMPLE_LIST,
        SIMPLE,
        CUSTOM_LIST,
        SIMPLE_VIEW_BINDING_LIST,
        SIMPLE_VIEW_BINDING,
        SIMPLE_HEADER_FOOTER_VIEW_BINDING,
    }

    private val sampleDiffUtil = RcvListDiffUtilCallBack<SampleItem>(
        itemsTheSame = { oldItem, newItem -> oldItem.id == newItem.id },
        contentsTheSame = { oldItem, newItem -> oldItem == newItem },
    )

    private val simpleListAdapter = SimpleRcvDataBindingListAdapter<SampleItem, ItemRcvTextviewBinding>(
        R.layout.item_rcv_textview,
        listDiffUtil = sampleDiffUtil,
    ) { holder, item, position ->
        holder.binding.apply {
            tvTitle.text = item.title
            tvDescription.text = item.description
            tvPosition.text = "Position: $position"
        }
    }.apply {
        setItems(SampleItem.createSampleData())
        setOnItemClickListener { position, _, _ -> currentRemoveAtAdapter(position) }
    }

    private val simpleAdapter = SimpleRcvDataBindingAdapter<SampleItem, ItemRcvTextviewBinding>(
        R.layout.item_rcv_textview,
    ) { holder, item, position ->
        holder.binding.apply {
            tvTitle.text = item.title
            tvDescription.text = item.description
            tvPosition.text = "Position: $position"
        }
    }.apply {
        setItems(SampleItem.createSampleData())
        setOnItemClickListener { position, _, _ -> currentRemoveAtAdapter(position) }
    }

    private val simpleViewBindingListAdapter =
        SimpleRcvViewBindingListAdapter<SampleItem, ItemRcvTextviewViewBinding>(
            inflate = ItemRcvTextviewViewBinding::inflate,
            listDiffUtil = sampleDiffUtil,
        ) { holder, item, position ->
            bindViewBindingItem(
                binding = holder.binding,
                item = item,
                position = position,
                badgePrefix = "VB LIST",
            )
        }.apply {
            setItems(SampleItem.createSampleData())
            setOnItemClickListener { position, _, _ -> currentRemoveAtAdapter(position) }
        }

    private val simpleViewBindingAdapter =
        SimpleRcvViewBindingAdapter<SampleItem, ItemRcvTextviewViewBinding>(
            inflate = ItemRcvTextviewViewBinding::inflate,
        ) { holder, item, position ->
            bindViewBindingItem(
                binding = holder.binding,
                item = item,
                position = position,
                badgePrefix = "VB",
            )
        }.apply {
            setItems(SampleItem.createSampleData())
            setOnItemClickListener { position, _, _ -> currentRemoveAtAdapter(position) }
        }

    // sealed interface 기반 Header/Content/Footer 어댑터
    // RcvItem.Header, RcvItem.Content, RcvItem.Footer를 단일 리스트로 관리
    private val sealedHeaderFooterAdapter = object : BaseRcvAdapter<RcvItem, RecyclerView.ViewHolder>() {
        override fun getContentItemViewType(position: Int, item: RcvItem): Int = when (item) {
            is RcvItem.Header -> VIEW_TYPE_HEADER
            is RcvItem.Content -> VIEW_TYPE_CONTENT
            is RcvItem.Footer -> VIEW_TYPE_FOOTER
        }

        override fun createViewHolderInternal(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return when (viewType) {
                VIEW_TYPE_HEADER, VIEW_TYPE_FOOTER ->
                    BaseRcvViewBindingViewHolder(ItemRcvTextviewViewBinding.inflate(inflater, parent, false))
                else ->
                    BaseRcvViewBindingViewHolder(ItemRcvTextviewViewBinding.inflate(inflater, parent, false))
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, item: RcvItem, position: Int) {
            @Suppress("UNCHECKED_CAST")
            val binding = (holder as BaseRcvViewBindingViewHolder<ItemRcvTextviewViewBinding>).binding
            when (item) {
                is RcvItem.Header -> {
                    binding.tvTitle.text = item.title
                    binding.tvDescription.text = item.description
                    binding.tvPosition.text = "[ HEADER ]"
                }
                is RcvItem.Content -> {
                    binding.tvTitle.text = item.item.title
                    binding.tvDescription.text = item.item.description
                    binding.tvPosition.text = "SEALED H/F : $position"
                }
                is RcvItem.Footer -> {
                    binding.tvTitle.text = "Footer"
                    binding.tvDescription.text = "현재 content 수: ${item.contentCount}"
                    binding.tvPosition.text = "[ FOOTER ]"
                }
            }
        }
    }.apply {
        setOnItemClickListener { position, item, _ ->
            if (item is RcvItem.Content) currentRemoveAtAdapter(position)
        }
        setItems(buildSealedItems(SampleItem.createSampleData()))
    }

    private val customListAdapter = CustomListAdapter()
        .apply {
            setOnItemClickListener { position, _, _ -> currentRemoveAtAdapter(position) }
        }.apply { setItems(SampleItem.createSampleData()) }

    override fun onCreate(binding: ActivityRecyclerviewBinding, savedInstanceState: Bundle?) {
        super.onCreate(binding, savedInstanceState)
        setupRecyclerView()
        setupScrollStateDetection()
    }

    private fun setupRecyclerView() {
        getBinding().apply {
            rcvItems.adapter = simpleListAdapter
            rGroupAdapterMode.setOnCheckedChangeListener { _, checkedId ->
                rcvItems.adapter = getAdapter(currentAdapterMode(checkedId))
            }
            btnAddItem.setOnClickListener { currentSelectAdapter() }
            btnClearItems.setOnClickListener { currentRemoveAllAdapter() }
            btnShuffleItems.setOnClickListener { currentShuffleAdapter() }
        }
    }

    private fun setupScrollStateDetection() {
        getBinding().rcvItems.apply {
            lifecycleScope.launch {
                sfScrollDirectionFlow.collect { direction ->
                    val directionText = when (direction) {
                        ScrollDirection.UP -> "위로 스크롤"
                        ScrollDirection.DOWN -> "아래로 스크롤"
                        ScrollDirection.LEFT -> "왼쪽으로 스크롤"
                        ScrollDirection.RIGHT -> "오른쪽으로 스크롤"
                        ScrollDirection.IDLE -> "스크롤 정지"
                    }
                    getBinding().tvScrollInfo.text = "방향: $directionText"
                }
            }

            lifecycleScope.launch {
                sfEdgeReachedFlow.collect { (edge, isReached) ->
                    val edgeText = when (edge) {
                        ScrollEdge.TOP -> "상단"
                        ScrollEdge.BOTTOM -> "하단"
                        ScrollEdge.LEFT -> "좌측"
                        ScrollEdge.RIGHT -> "우측"
                    }
                    val statusText = if (isReached) "도달" else "벗어남"
                    getBinding().tvScrollInfo.text = "$edgeText $statusText"
                }
            }
        }
    }

    private fun currentSelectAdapter() {
        when (currentAdapterMode()) {
            AdapterMode.SIMPLE_LIST -> simpleListAdapter.addItem(getItem(simpleListAdapter.itemCount))
            AdapterMode.SIMPLE -> simpleAdapter.addItem(getItem(simpleAdapter.itemCount))
            AdapterMode.CUSTOM_LIST -> customListAdapter.addItem(getItem(customListAdapter.itemCount))
            AdapterMode.SIMPLE_VIEW_BINDING_LIST -> simpleViewBindingListAdapter.addItem(getItem(simpleViewBindingListAdapter.itemCount))
            AdapterMode.SIMPLE_VIEW_BINDING -> simpleViewBindingAdapter.addItem(getItem(simpleViewBindingAdapter.itemCount))
            AdapterMode.SIMPLE_HEADER_FOOTER_VIEW_BINDING -> {
                val currentItems = sealedHeaderFooterAdapter.getItems()
                    .filterIsInstance<RcvItem.Content>()
                    .map { it.item }
                sealedHeaderFooterAdapter.setItems(buildSealedItems(currentItems + getItem(currentItems.size)))
            }
        }
    }

    private fun getItem(position: Int) = SampleItem(
        id = System.currentTimeMillis(),
        title = "새 아이템 $position",
        description = "동적으로 추가된 아이템입니다",
    )

    private fun currentRemoveAtAdapter(position: Int) {
        when (currentAdapterMode()) {
            AdapterMode.SIMPLE_LIST -> simpleListAdapter.removeAt(position)
            AdapterMode.SIMPLE -> simpleAdapter.removeAt(position)
            AdapterMode.CUSTOM_LIST -> customListAdapter.removeAt(position)
            AdapterMode.SIMPLE_VIEW_BINDING_LIST -> simpleViewBindingListAdapter.removeAt(position)
            AdapterMode.SIMPLE_VIEW_BINDING -> simpleViewBindingAdapter.removeAt(position)
            AdapterMode.SIMPLE_HEADER_FOOTER_VIEW_BINDING -> {
                val currentItems = sealedHeaderFooterAdapter.getItems()
                    .filterIsInstance<RcvItem.Content>()
                    .map { it.item }
                    .toMutableList()
                // position에서 header offset(1) 제거
                val contentPosition = position - 1
                if (contentPosition in currentItems.indices) {
                    currentItems.removeAt(contentPosition)
                    sealedHeaderFooterAdapter.setItems(buildSealedItems(currentItems))
                }
            }
        }
    }

    private fun currentRemoveAllAdapter() {
        when (currentAdapterMode()) {
            AdapterMode.SIMPLE_LIST -> simpleListAdapter.removeAll()
            AdapterMode.SIMPLE -> simpleAdapter.removeAll()
            AdapterMode.CUSTOM_LIST -> customListAdapter.removeAll()
            AdapterMode.SIMPLE_VIEW_BINDING_LIST -> simpleViewBindingListAdapter.removeAll()
            AdapterMode.SIMPLE_VIEW_BINDING -> simpleViewBindingAdapter.removeAll()
            AdapterMode.SIMPLE_HEADER_FOOTER_VIEW_BINDING -> sealedHeaderFooterAdapter.setItems(buildSealedItems(emptyList()))
        }
    }

    private fun currentShuffleAdapter() {
        when (currentAdapterMode()) {
            AdapterMode.SIMPLE_LIST -> simpleListAdapter.setItems(simpleListAdapter.getItems().shuffled())
            AdapterMode.SIMPLE -> simpleAdapter.setItems(simpleAdapter.getItems().shuffled())
            AdapterMode.CUSTOM_LIST -> customListAdapter.setItems(customListAdapter.getItems().shuffled())
            AdapterMode.SIMPLE_VIEW_BINDING_LIST -> simpleViewBindingListAdapter.setItems(simpleViewBindingListAdapter.getItems().shuffled())
            AdapterMode.SIMPLE_VIEW_BINDING -> simpleViewBindingAdapter.setItems(simpleViewBindingAdapter.getItems().shuffled())
            AdapterMode.SIMPLE_HEADER_FOOTER_VIEW_BINDING -> {
                val currentItems = sealedHeaderFooterAdapter.getItems()
                    .filterIsInstance<RcvItem.Content>()
                    .map { it.item }
                sealedHeaderFooterAdapter.setItems(buildSealedItems(currentItems.shuffled()))
            }
        }
    }

    private fun currentAdapterMode(
        checkedId: Int = getBinding().rGroupAdapterMode.checkedRadioButtonId,
    ): AdapterMode = when (checkedId) {
        R.id.rBtnChangeSimpleAdapter -> AdapterMode.SIMPLE
        R.id.rBtnChangeCustomLIstAdapter -> AdapterMode.CUSTOM_LIST
        R.id.rBtnChangeSimpleViewBindingListAdapter -> AdapterMode.SIMPLE_VIEW_BINDING_LIST
        R.id.rBtnChangeSimpleViewBindingAdapter -> AdapterMode.SIMPLE_VIEW_BINDING
        R.id.rBtnChangeSimpleHeaderFooterViewBindingAdapter -> AdapterMode.SIMPLE_HEADER_FOOTER_VIEW_BINDING
        else -> AdapterMode.SIMPLE_LIST
    }

    private fun getAdapter(mode: AdapterMode): RecyclerView.Adapter<*> = when (mode) {
        AdapterMode.SIMPLE_LIST -> simpleListAdapter
        AdapterMode.SIMPLE -> simpleAdapter
        AdapterMode.CUSTOM_LIST -> customListAdapter
        AdapterMode.SIMPLE_VIEW_BINDING_LIST -> simpleViewBindingListAdapter
        AdapterMode.SIMPLE_VIEW_BINDING -> simpleViewBindingAdapter
        AdapterMode.SIMPLE_HEADER_FOOTER_VIEW_BINDING -> sealedHeaderFooterAdapter
    }

    private fun bindViewBindingItem(
        binding: ItemRcvTextviewViewBinding,
        item: SampleItem,
        position: Int,
        badgePrefix: String,
    ) {
        binding.tvTitle.text = item.title
        binding.tvDescription.text = item.description
        binding.tvPosition.text = "$badgePrefix : $position"
    }

    // Header 1개 + Content 목록 + Footer 1개를 단일 sealed interface 리스트로 조립
    private fun buildSealedItems(contentItems: List<SampleItem>): List<RcvItem> = buildList {
        add(RcvItem.Header(title = "Header", description = "sealed interface 기반 Header/Content/Footer 예제"))
        contentItems.forEach { add(RcvItem.Content(it)) }
        add(RcvItem.Footer(contentCount = contentItems.size))
    }

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_CONTENT = 1
        private const val VIEW_TYPE_FOOTER = 2
    }
}
