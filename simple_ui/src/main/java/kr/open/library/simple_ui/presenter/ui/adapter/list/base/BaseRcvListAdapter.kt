package kr.open.library.simple_ui.presenter.ui.adapter.list.base

import android.view.View
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kr.open.library.simple_ui.logcat.Logx
import kr.open.library.simple_ui.presenter.ui.adapter.list.diffutil.RcvListDiffUtilCallBack
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
     * @param itemList 설정할 아이템 리스트
     * @param commitCallback 리스트 갱신 완료 후 호출될 콜백 (null 가능)
     */
    public fun setItems(itemList: List<ITEM>, commitCallback: (() -> Unit)? = null) {
        submitList(itemList, commitCallback)
    }

    /**
     * 아이템 추가
     * @param item 추가할 아이템
     * @param commitCallback 리스트 갱신 완료 후 호출될 콜백 (null 가능)
     */
    public fun addItem(item: ITEM, commitCallback: (() -> Unit)? = null) {
        val updatedList = getMutableItemList().apply { add(item) }
        submitList(updatedList, commitCallback)
    }

    /**
     * 특정 위치에 아이템 추가
     * @param position 추가할 위치
     * @param item 추가할 아이템
     * @param commitCallback 리스트 갱신 완료 후 호출될 콜백 (null 가능)
     * @return 추가 성공 여부
     */
    public fun addItemAt(position: Int, item: ITEM, commitCallback: (() -> Unit)? = null): Boolean {
        return try {
            if (position < 0 || position > itemCount) {
                throw IndexOutOfBoundsException(
                    "Cannot add item at position $position. Valid range: 0..$itemCount"
                )
            }

            val updatedList = getMutableItemList().apply {
                add(position, item)
            }
            submitList(updatedList, commitCallback)
            true
        } catch (e: IndexOutOfBoundsException) {
            Logx.e("Failed to add item at position $position: ${e.message}")
            false
        } catch (e: Exception) {
            Logx.e("Unexpected error while adding item at position $position", e)
            false
        }
    }

    /**
     * 여러 아이템 추가
     * @param itemList 추가할 아이템 리스트
     * @param commitCallback 리스트 갱신 완료 후 호출될 콜백 (null 가능)
     * @return 추가된 아이템 수
     */
    public fun addItems(itemList: List<ITEM>, commitCallback: (() -> Unit)? = null): Int {
        if (itemList.isEmpty()) return 0

        val updatedList = getMutableItemList().apply {
            addAll(itemList)
        }
        submitList(updatedList, commitCallback)
        return itemList.size
    }

    /**
     * 여러 아이템 특정 위치에 추가
     * @param position 추가할 위치
     * @param itemList 추가할 아이템 리스트
     * @param commitCallback 리스트 갱신 완료 후 호출될 콜백 (null 가능)
     * @return 추가 성공 여부
     */
    public fun addItems(position: Int, itemList: List<ITEM>, commitCallback: (() -> Unit)? = null): Boolean {
        return try {
            if (itemList.isEmpty()) return true
            if (position < 0 || position > itemCount) {
                throw IndexOutOfBoundsException(
                    "Cannot add items at position $position. Valid range: 0..$itemCount"
                )
            }

            val updatedList = getMutableItemList().apply { addAll(position, itemList) }
            submitList(updatedList, commitCallback)
            true
        } catch (e: IndexOutOfBoundsException) {
            Logx.e("Failed to add items at position $position: ${e.message}")
            false
        } catch (e: Exception) {
            Logx.e("Unexpected error while adding items at position $position", e)
            false
        }
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
     * @return 제거 성공 여부
     */
    public fun removeItem(item: ITEM, commitCallback: (() -> Unit)? = null): Boolean {
        val position = currentList.indexOf(item)
        if (position == RecyclerView.NO_POSITION) {
            Logx.e("Item not found in the list")
            return false
        }
        return removeAt(position, commitCallback)
    }

    /**
     * 특정 위치의 아이템 제거
     * @param position 제거할 아이템 위치
     * @param commitCallback 리스트 갱신 완료 후 호출될 콜백 (null 가능)
     * @return 제거 성공 여부
     */
    public fun removeAt(position: Int, commitCallback: (() -> Unit)? = null): Boolean {
        return try {
            if (position < 0 || position >= itemCount) {
                throw IndexOutOfBoundsException(
                    "Cannot remove item at position $position. Valid range: 0 until $itemCount"
                )
            }

            val updatedList = getMutableItemList().apply {
                removeAt(position)
            }
            submitList(updatedList, commitCallback)
            true
        } catch (e: IndexOutOfBoundsException) {
            Logx.e("Failed to remove item at position $position: ${e.message}")
            false
        } catch (e: Exception) {
            Logx.e("Unexpected error while removing item at position $position", e)
            false
        }
    }

    /**
     * 모든 아이템 제거
     * @param commitCallback 리스트 갱신 완료 후 호출될 콜백 (null 가능)
     */
    public fun clearItems(commitCallback: (() -> Unit)? = null) {
        submitList(emptyList(), commitCallback)
    }

    /**
     * 아이템 위치 이동
     * @param fromPosition 이동할 아이템의 현재 위치
     * @param toPosition 이동할 목표 위치
     * @param commitCallback 리스트 갱신 완료 후 호출될 콜백 (null 가능)
     * @return 이동 성공 여부
     */
    public fun moveItem(fromPosition: Int, toPosition: Int, commitCallback: (() -> Unit)? = null): Boolean {
        return try {
            if (fromPosition < 0 || fromPosition >= itemCount) {
                throw IndexOutOfBoundsException(
                    "Invalid fromPosition $fromPosition. Valid range: 0 until $itemCount"
                )
            }
            if (toPosition < 0 || toPosition >= itemCount) {
                throw IndexOutOfBoundsException(
                    "Invalid toPosition $toPosition. Valid range: 0 until $itemCount"
                )
            }
            if (fromPosition == toPosition) {
                return true
            }

            val updatedList = getMutableItemList().apply {
                val item = removeAt(fromPosition)
                add(toPosition.coerceAtMost(size), item)
            }
            submitList(updatedList, commitCallback)
            true
        } catch (e: IndexOutOfBoundsException) {
            Logx.e("Failed to move item from $fromPosition to $toPosition: ${e.message}")
            false
        } catch (e: Exception) {
            Logx.e("Unexpected error while moving item from $fromPosition to $toPosition", e)
            false
        }
    }

    /**
     * 특정 위치의 아이템을 새로운 아이템으로 교체
     * @param position 교체할 아이템 위치
     * @param item 새로운 아이템
     * @param commitCallback 리스트 갱신 완료 후 호출될 콜백 (null 가능)
     * @return 교체 성공 여부
     */
    public fun replaceItemAt(position: Int, item: ITEM, commitCallback: (() -> Unit)? = null): Boolean {
        if (!isPositionValid(position)) {
            Logx.e("Invalid position: $position, item count: $itemCount")
            return false
        }
        val updatedList = getMutableItemList().apply {
            set(position, item)
        }
        submitList(updatedList, commitCallback)
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