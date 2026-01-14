package kr.open.library.simpleui_xml.recyclerview.new_

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kr.open.library.simple_ui.xml.ui.adapter.list.diffutil.RcvListDiffUtilCallBack
import kr.open.library.simple_ui.xml.ui.adapter.list.simple.SimpleBindingRcvListAdapter
import kr.open.library.simple_ui.xml.ui.adapter.normal.simple.SimpleBindingRcvAdapter
import kr.open.library.simple_ui.xml.ui.components.activity.binding.BaseDataBindingActivity
import kr.open.library.simple_ui.xml.ui.view.recyclerview.ScrollDirection
import kr.open.library.simple_ui.xml.ui.view.recyclerview.ScrollEdge
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.databinding.ActivityRecyclerviewBinding
import kr.open.library.simpleui_xml.databinding.ItemRcvTextviewBinding
import kr.open.library.simpleui_xml.recyclerview.model.SampleItem
import kr.open.library.simpleui_xml.recyclerview.new_.adapter.CustomListAdapter

class RecyclerViewActivity : BaseDataBindingActivity<ActivityRecyclerviewBinding>(R.layout.activity_recyclerview) {
    private val simpleListAdapter = SimpleBindingRcvListAdapter<SampleItem, ItemRcvTextviewBinding>(
        R.layout.item_rcv_textview,
        listDiffUtil =
            RcvListDiffUtilCallBack(
                itemsTheSame = { oldItem, newItem -> oldItem.id == newItem.id },
                contentsTheSame = { oldItem, newItem -> oldItem == newItem },
            ),
    ) { holder, item, position ->
        holder.binding.apply {
            tvTitle.text = item.title
            tvDescription.text = item.description
            tvPosition.text = "Position: $position"

            root.setOnClickListener { currentRemoveAtAdapter(position) }
        }
    }.apply { setItems(SampleItem.createSampleData()) }

    private val simpleAdapter = SimpleBindingRcvAdapter<SampleItem, ItemRcvTextviewBinding>(
        R.layout.item_rcv_textview,
    ) { holder, item, position ->
        holder.binding.apply {
            tvTitle.text = item.title
            tvDescription.text = item.description
            tvPosition.text = "Position: $position"

            root.setOnClickListener { currentRemoveAtAdapter(position) }
        }
    }.apply { setItems(SampleItem.createSampleData()) }

    private val customListAdapter = CustomListAdapter()
        .apply {
            setOnItemClickListener { i, sampleItem, view -> currentRemoveAtAdapter(i) }
        }.apply { setItems(SampleItem.createSampleData()) }

    override fun onCreate(binding: ActivityRecyclerviewBinding, savedInstanceState: Bundle?) {
        super.onCreate(binding, savedInstanceState)
        setupRecyclerView()
        setupScrollStateDetection()
    }

    private fun setupRecyclerView() {
        getBinding().apply {
            rcvItems.adapter = simpleListAdapter
            rBtnChangeSimpleAdapter.setOnClickListener { rcvItems.adapter = simpleAdapter }
            rBtnChangeSimpleListAdapter.setOnClickListener { rcvItems.adapter = simpleListAdapter }
            rBtnChangeCustomLIstAdapter.setOnClickListener { rcvItems.adapter = customListAdapter }
            btnAddItem.setOnClickListener { currentSelectAdapter() }
            btnClearItems.setOnClickListener { currentRemoveAllAdapter() }
            btnShuffleItems.setOnClickListener { currentShuffleAdapter() }
        }
    }

    private fun setupScrollStateDetection() {
        getBinding().rcvItems.apply {
            lifecycleScope.launch {
                sfScrollDirectionFlow.collect { direction ->
                    val directionText =
                        when (direction) {
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
                    val edgeText =
                        when (edge) {
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
        if (getBinding().rBtnChangeSimpleAdapter.isChecked) {
            simpleAdapter.addItem(getItem(simpleAdapter.itemCount))
        } else if (getBinding().rBtnChangeSimpleListAdapter.isChecked) {
            simpleListAdapter.addItem(getItem(simpleListAdapter.itemCount))
        } else if (getBinding().rBtnChangeCustomLIstAdapter.isChecked) {
            customListAdapter.addItem(getItem(customListAdapter.itemCount))
        }
    }

    private fun getItem(position: Int) =
        SampleItem(
            id = System.currentTimeMillis(),
            title = "새 아이템 $position",
            description = "동적으로 추가된 아이템입니다",
        )

    private fun currentRemoveAtAdapter(position: Int) {
        if (getBinding().rBtnChangeSimpleAdapter.isChecked) {
            simpleAdapter.removeAt(position)
        } else if (getBinding().rBtnChangeSimpleListAdapter.isChecked) {
            simpleListAdapter.removeAt(position)
        } else if (getBinding().rBtnChangeCustomLIstAdapter.isChecked) {
            customListAdapter.removeAt(position)
        }
    }

    private fun currentRemoveAllAdapter() {
        if (getBinding().rBtnChangeSimpleAdapter.isChecked) {
            simpleAdapter.removeAll()
        } else if (getBinding().rBtnChangeSimpleListAdapter.isChecked) {
            simpleListAdapter.removeAll()
        } else if (getBinding().rBtnChangeCustomLIstAdapter.isChecked) {
            customListAdapter.removeAll()
        }
    }

    private fun currentShuffleAdapter() {
        if (getBinding().rBtnChangeSimpleAdapter.isChecked) {
            simpleAdapter.setItems(simpleAdapter.getItems().shuffled())
        } else if (getBinding().rBtnChangeSimpleListAdapter.isChecked) {
            simpleListAdapter.setItems(simpleListAdapter.getItems().shuffled())
        } else if (getBinding().rBtnChangeCustomLIstAdapter.isChecked) {
            customListAdapter.setItems(customListAdapter.getItems().shuffled())
        }
    }
}
