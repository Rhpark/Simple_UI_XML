package kr.open.library.simpleui_xml.recyclerview.origin

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.databinding.ActivityRecyclerviewOriginBinding
import kr.open.library.simpleui_xml.recyclerview.model.SampleItem
import kr.open.library.simpleui_xml.recyclerview.origin.adapter.OriginCustomAdapter
import kr.open.library.simpleui_xml.recyclerview.origin.adapter.OriginCustomListAdapter
import kotlin.math.abs

class RecyclerViewActivityOrigin : AppCompatActivity() {
    private lateinit var binding: ActivityRecyclerviewOriginBinding

    private val listAdapter =
        OriginCustomListAdapter { item, position ->
            currentRemoveAtAdapter(position)
        }.apply { submitList(SampleItem.createSampleData()) }

    private val adapter =
        OriginCustomAdapter { item, position ->
            currentRemoveAtAdapter(position)
        }.apply { setItems(SampleItem.createSampleData()) }

    private var isScrolling = false
    private var accumulatedDy = 0
    private var lastScrollDirection = "정지"
    private val scrollDirectionThreshold = 20

    private var isAtTop = false
    private var isAtBottom = false
    private val edgeReachThreshold = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_recyclerview_origin)
        binding.lifecycleOwner = this

        setupRecyclerView()
        setupManualScrollDetection()
    }

    private fun setupRecyclerView() {
        binding.apply {
            rcvItems.layoutManager = LinearLayoutManager(this@RecyclerViewActivityOrigin)
            rcvItems.adapter = listAdapter

            rBtnChangeListAdapter.setOnClickListener { rcvItems.adapter = listAdapter }
            rBtnChangeTraditionalAdapter.setOnClickListener { rcvItems.adapter = adapter }
            btnAddItem.setOnClickListener { currentSelectAdapter() }
            btnClearItems.setOnClickListener { currentRemoveAllAdapter() }
            btnShuffleItems.setOnClickListener { currentShuffleAdapter() }
        }
    }

    private fun setupManualScrollDetection() {
        binding.rcvItems.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(
                    recyclerView: RecyclerView,
                    newState: Int,
                ) {
                    super.onScrollStateChanged(recyclerView, newState)
                    when (newState) {
                        RecyclerView.SCROLL_STATE_IDLE -> {
                            isScrolling = false
                            accumulatedDy = 0
                            lastScrollDirection = "정지"
                            binding.tvScrollInfo.text = "🔄 방향: 스크롤 정지"
                            Log.d("SCROLL_ORIGIN", "Scroll Direction: 스크롤 정지")
                        }
                        RecyclerView.SCROLL_STATE_DRAGGING -> {
                            isScrolling = true
                        }
                        RecyclerView.SCROLL_STATE_SETTLING -> {
                            isScrolling = true
                        }
                    }
                }

                override fun onScrolled(
                    recyclerView: RecyclerView,
                    dx: Int,
                    dy: Int,
                ) {
                    super.onScrolled(recyclerView, dx, dy)

                    accumulatedDy += dy
                    if (abs(accumulatedDy) >= scrollDirectionThreshold) {
                        val currentDirection = if (accumulatedDy > 0) "아래로 스크롤" else "위로 스크롤"

                        if (currentDirection != lastScrollDirection) {
                            lastScrollDirection = currentDirection
                            binding.tvScrollInfo.text = "🔄 방향: $currentDirection"
                            Log.d("SCROLL_ORIGIN", "Scroll Direction: $currentDirection")
                        }
                        accumulatedDy = 0
                    }
                    checkEdgeReach(recyclerView)
                }
            },
        )
    }

    private fun checkEdgeReach(recyclerView: RecyclerView) {
        val newIsAtTop = !recyclerView.canScrollVertically(-1)
        if (newIsAtTop != isAtTop) {
            isAtTop = newIsAtTop
            val statusText = if (isAtTop) "도달" else "벗어남"
            binding.tvScrollInfo.text = "📍 상단 $statusText"
            Log.d("EDGE_ORIGIN", "Edge Detection: 상단 $statusText")
        }

        val newIsAtBottom = !recyclerView.canScrollVertically(1)
        if (newIsAtBottom != isAtBottom) {
            isAtBottom = newIsAtBottom
            val statusText = if (isAtBottom) "도달" else "벗어남"
            binding.tvScrollInfo.text = "📍 하단 $statusText"
            Log.d("EDGE_ORIGIN", "Edge Detection: 하단 $statusText")
        }
    }

    private fun currentSelectAdapter() {
        when {
            binding.rBtnChangeListAdapter.isChecked -> {
                val currentList = listAdapter.currentList.toMutableList()
                currentList.add(getItem(currentList.size))
                listAdapter.submitList(currentList)
            }
            binding.rBtnChangeTraditionalAdapter.isChecked -> {
                adapter.addItem(getItem(adapter.itemCount))
            }
        }
    }

    private fun getItem(position: Int) =
        SampleItem(
            id = System.currentTimeMillis(),
            title = "새 아이템 $position",
            description = "동적으로 추가된 아이템입니다",
        )

    private fun currentRemoveAtAdapter(position: Int) {
        when {
            binding.rBtnChangeListAdapter.isChecked -> {
                val currentList = listAdapter.currentList.toMutableList()
                if (position in currentList.indices) {
                    currentList.removeAt(position)
                    listAdapter.submitList(currentList)
                }
            }
            binding.rBtnChangeTraditionalAdapter.isChecked -> {
                adapter.removeAt(position)
            }
        }
    }

    private fun currentRemoveAllAdapter() {
        when {
            binding.rBtnChangeListAdapter.isChecked -> listAdapter.submitList(emptyList())
            binding.rBtnChangeTraditionalAdapter.isChecked -> adapter.removeAll()
        }
    }

    private fun currentShuffleAdapter() {
        when {
            binding.rBtnChangeListAdapter.isChecked -> {
                listAdapter.submitList(listAdapter.currentList.shuffled())
            }
            binding.rBtnChangeTraditionalAdapter.isChecked -> {
                adapter.setItems(adapter.getItems().shuffled())
            }
        }
    }
}
