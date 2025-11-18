package kr.open.library.simple_ui.presenter.ui.adapter.list.base

import android.view.View
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kr.open.library.simple_ui.logcat.Logx
import kr.open.library.simple_ui.presenter.ui.adapter.list.diffutil.RcvListDiffUtilCallBack
import kr.open.library.simple_ui.presenter.ui.adapter.queue.AdapterOperationQueue
import kr.open.library.simple_ui.presenter.ui.adapter.viewholder.BaseRcvViewHolder


/**
 * 기본 RecyclerView ListAdapter 구현체
 * @param ITEM 아이템 타입
 * @param VH ViewHolder 타입
 * @param listDiffUtil 아이템 비교를 위한 DiffUtil
 */
public abstract class BaseRcvListAdapter<ITEM, VH : RecyclerView.ViewHolder>(listDiffUtil: RcvListDiffUtilCallBack<ITEM>) :
    ListAdapter<ITEM, VH>(listDiffUtil) {

    private var onItemClickListener: ((Int, ITEM, View) -> Unit)? = null
    private var onItemLongClickListener: ((Int, ITEM, View) -> Unit)? = null

    // Operation Queue for handling consecutive operations
    private val operationQueue = AdapterOperationQueue<ITEM>(
        getCurrentList = { currentList },
        submitList = { list, callback -> submitList(list, callback) }
    )

    /**
     * 각 ViewHolder에 데이터를 바인딩하는 추상 메서드
     * @param holder ViewHolder
     * @param position 아이템 위치
     * @param item 바인딩할 아이템
     */
    protected abstract fun onBindViewHolder(holder: VH, position: Int, item: ITEM)

    /**
     * 부분 업데이트를 위한 onBindViewHolder (payload 지원)
     * 기본적으로 전체 바인딩을 수행하며, 필요시 오버라이드하여 부분 업데이트 구현
     * @param holder ViewHolder
     * @param position 아이템 위치
     * @param item 바인딩할 아이템
     * @param payloads 부분 업데이트를 위한 payload 리스트
     */
    protected open fun onBindViewHolder(holder: VH, position: Int, item: ITEM, payloads: List<Any>) {
        // 기본적으로 전체 바인딩 수행
        onBindViewHolder(holder, position, item)
    }

    /**
     * 현재 어댑터에 설정된 아이템 리스트 반환
     * @return 현재 아이템 리스트
     */
    public fun getItems(): List<ITEM> = currentList

    /**
     * 현재 아이템 리스트의 복사본 반환
     * @return 현재 아이템 리스트의 복사본
     */
    protected fun getMutableItemList(): MutableList<ITEM> = currentList.toMutableList()

    /**
     * 아이템 리스트 설정
     * 기존 큐의 모든 작업을 취소하고 새로운 리스트로 교체
     * @param itemList 설정할 아이템 리스트
     * @param commitCallback 리스트 갱신 완료 후 호출될 콜백 (null 가능)
     */
    public fun setItems(itemList: List<ITEM>, commitCallback: (() -> Unit)? = null) {
        operationQueue.clearQueueAndExecute(AdapterOperationQueue.SetItemsOp(itemList, commitCallback))
    }

    /**
     * 아이템 추가
     * @param item 추가할 아이템
     * @param commitCallback 리스트 갱신 완료 후 호출될 콜백 (null 가능)
     */
    public fun addItem(item: ITEM, commitCallback: (() -> Unit)? = null) {
        operationQueue.enqueueOperation(AdapterOperationQueue.AddItemOp(item, commitCallback))
    }

    /**
     * 특정 위치에 아이템 추가
     * @param position 추가할 위치
     * @param item 추가할 아이템
     * @param commitCallback 리스트 갱신 완료 후 호출될 콜백 (null 가능)
     * @return 추가 성공 여부 (position이 유효하지 않으면 false 반환)
     */
    public fun addItemAt(position: Int, item: ITEM, commitCallback: (() -> Unit)? = null): Boolean {
        if (position < 0 || position > itemCount) {
            Logx.e("Cannot add item at position $position. Valid range: 0..$itemCount")
            return false
        }
        operationQueue.enqueueOperation(AdapterOperationQueue.AddItemAtOp(position, item, commitCallback))
        return true
    }

    /**
     * 여러 아이템 추가
     * @param itemList 추가할 아이템 리스트
     * @param commitCallback 리스트 갱신 완료 후 호출될 콜백 (null 가능)
     * @return 추가된 아이템 수
     */
    public fun addItems(itemList: List<ITEM>, commitCallback: (() -> Unit)? = null): Int {
        if (itemList.isEmpty()) return 0
        operationQueue.enqueueOperation(AdapterOperationQueue.AddItemsOp(itemList, commitCallback))
        return itemList.size
    }

    /**
     * 여러 아이템 특정 위치에 추가
     * @param position 추가할 위치
     * @param itemList 추가할 아이템 리스트
     * @param commitCallback 리스트 갱신 완료 후 호출될 콜백 (null 가능)
     * @return 추가 성공 여부 (position이 유효하지 않으면 false 반환)
     */
    public fun addItems(position: Int, itemList: List<ITEM>, commitCallback: (() -> Unit)? = null): Boolean {
        if (itemList.isEmpty()) return true
        if (position < 0 || position > itemCount) {
            Logx.e("Cannot add items at position $position. Valid range: 0..$itemCount")
            return false
        }
        operationQueue.enqueueOperation(AdapterOperationQueue.AddItemsAtOp(position, itemList, commitCallback))
        return true
    }

    /**
     * 위치가 유효한지 확인
     * @param position 확인할 위치
     * @return 위치가 유효하면 true, 아니면 false
     */
    protected fun isPositionValid(position: Int): Boolean = (position > RecyclerView.NO_POSITION && position < itemCount)

    /**
     * 특정 아이템 제거
     * @param item 제거할 아이템
     * @param commitCallback 리스트 갱신 완료 후 호출될 콜백 (null 가능)
     * @return 제거 성공 여부 (아이템이 없으면 false 반환)
     */
    public fun removeItem(item: ITEM, commitCallback: (() -> Unit)? = null): Boolean {
        if (!currentList.contains(item)) {
            Logx.e("Item not found in the list")
            return false
        }
        operationQueue.enqueueOperation(AdapterOperationQueue.RemoveItemOp(item, commitCallback))
        return true
    }

    /**
     * 특정 위치의 아이템 제거
     * @param position 제거할 아이템 위치
     * @param commitCallback 리스트 갱신 완료 후 호출될 콜백 (null 가능)
     * @return 제거 성공 여부 (position이 유효하지 않으면 false 반환)
     */
    public fun removeAt(position: Int, commitCallback: (() -> Unit)? = null): Boolean {
        if (position < 0 || position >= itemCount) {
            Logx.e("Cannot remove item at position $position. Valid range: 0 until $itemCount")
            return false
        }
        operationQueue.enqueueOperation(AdapterOperationQueue.RemoveAtOp(position, commitCallback))
        return true
    }

    /**
     * 모든 아이템 제거
     * @param commitCallback 리스트 갱신 완료 후 호출될 콜백 (null 가능)
     */
    public fun removeAll(commitCallback: (() -> Unit)? = null) {
        operationQueue.enqueueOperation(AdapterOperationQueue.ClearItemsOp(commitCallback))
    }

    /**
     * 아이템 위치 이동
     * @param fromPosition 이동할 아이템의 현재 위치
     * @param toPosition 이동할 목표 위치
     * @param commitCallback 리스트 갱신 완료 후 호출될 콜백 (null 가능)
     * @return 이동 성공 여부 (position이 유효하지 않으면 false 반환)
     */
    public fun moveItem(fromPosition: Int, toPosition: Int, commitCallback: (() -> Unit)? = null): Boolean {
        if (fromPosition == toPosition) return true
        if (fromPosition < 0 || fromPosition >= itemCount) {
            Logx.e("Invalid fromPosition $fromPosition. Valid range: 0 until $itemCount")
            return false
        }
        if (toPosition < 0 || toPosition >= itemCount) {
            Logx.e("Invalid toPosition $toPosition. Valid range: 0 until $itemCount")
            return false
        }
        operationQueue.enqueueOperation(AdapterOperationQueue.MoveItemOp(fromPosition, toPosition, commitCallback))
        return true
    }

    /**
     * 특정 위치의 아이템을 새로운 아이템으로 교체
     * @param position 교체할 아이템 위치
     * @param item 새로운 아이템
     * @param commitCallback 리스트 갱신 완료 후 호출될 콜백 (null 가능)
     * @return 교체 성공 여부 (position이 유효하지 않으면 false 반환)
     */
    public fun replaceItemAt(position: Int, item: ITEM, commitCallback: (() -> Unit)? = null): Boolean {
        if (position < 0 || position >= itemCount) {
            Logx.e("Invalid position $position. Valid range: 0 until $itemCount")
            return false
        }
        operationQueue.enqueueOperation(AdapterOperationQueue.ReplaceItemAtOp(position, item, commitCallback))
        return true
    }

    /**
     * 아이템 클릭 리스너 설정
     * @param listener 클릭 이벤트 콜백
     */
    public fun setOnItemClickListener(listener: (Int, ITEM, View) -> Unit) { onItemClickListener = listener }

    /**
     * 아이템 롱클릭 리스너 설정
     * @param listener 롱클릭 이벤트 콜백
     */
    public fun setOnItemLongClickListener(listener: (Int, ITEM, View) -> Unit) { onItemLongClickListener = listener }

    override fun onBindViewHolder(holder: VH, position: Int) {
        if (!isPositionValid(position)) {
            Logx.e("Invalid position: $position, item count: $itemCount")
            return
        }

        val item = getItem(position)

        holder.itemView.apply {
            setOnClickListener { view -> onItemClickListener?.invoke(position, item, view) }
            setOnLongClickListener { view ->
                onItemLongClickListener?.let {
                    it.invoke(position, item, view)
                    true
                } ?: false
            }
        }

        onBindViewHolder(holder, position, item)
    }

    override fun onBindViewHolder(holder: VH, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            // payload가 없으면 전체 바인딩
            onBindViewHolder(holder, position)
        } else {
            // payload가 있으면 부분 업데이트
            if (!isPositionValid(position)) {
                Logx.e("Invalid position: $position, item count: $itemCount")
                return
            }

            val item = getItem(position)
            onBindViewHolder(holder, position, item, payloads)
        }
    }

    override fun onViewRecycled(holder: VH) {
        super.onViewRecycled(holder)
        if(holder is BaseRcvViewHolder){
            holder.clearViewCache()
        }
    }
}