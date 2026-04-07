package kr.open.library.simple_ui.xml.ui.adapter.normal.base

import android.annotation.SuppressLint
import android.view.View
import androidx.annotation.MainThread
import androidx.recyclerview.widget.RecyclerView
import kr.open.library.simple_ui.core.extensions.trycatch.requireInBounds
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.xml.ui.adapter.common.imp.AdapterWriteApi
import kr.open.library.simple_ui.xml.ui.adapter.normal.result.NormalAdapterResult
import kr.open.library.simple_ui.xml.ui.adapter.normal.result.toNormalAdapterResult
import kr.open.library.simple_ui.xml.ui.adapter.normal.root.RootRcvAdapter

/**
 * Base RecyclerView.Adapter implementation with content item management.<br><br>
 * content 아이템 관리를 제공하는 기본 RecyclerView.Adapter 구현입니다.<br>
 * For Header/Footer support, use a sealed interface as the ITEM type and dispatch by type in onBindViewHolder.<br><br>
 * Header/Footer가 필요하면 ITEM을 sealed interface로 정의하고 onBindViewHolder에서 타입별로 분기하세요.<br>
 *
 * Features:<br>
 * 주요 기능:<br>
 * - Immediate list mutation with notify-based UI updates.<br>
 * - 리스트를 즉시 변경하고 notify 기반으로 UI를 갱신합니다.<br>
 * - Item click and long-click listener support.<br>
 * - 아이템 클릭 및 롱클릭 리스너를 지원합니다.<br>
 * - Partial bind hook via payloads.<br>
 * - payload 기반 부분 바인딩 훅을 지원합니다.<br>
 * - ViewHolder cache clearing support on recycle.<br><br>
 * - 재활용 시 ViewHolder 캐시 정리를 지원합니다.<br>
 *
 * @param ITEM Item type used by this adapter.<br><br>
 *             이 어댑터가 사용하는 아이템 타입입니다.<br>
 * @param VH ViewHolder type used by this adapter.<br><br>
 *           이 어댑터가 사용하는 ViewHolder 타입입니다.<br>
 */
public abstract class BaseRcvAdapter<ITEM : Any, VH : RecyclerView.ViewHolder> :
    RootRcvAdapter<ITEM, VH>(),
    AdapterWriteApi<ITEM, NormalAdapterResult> {
    internal open val adapterData: BaseRcvAdapterData<ITEM> = BaseRcvAdapterData()

    /**
     * Returns total adapter item count.<br><br>
     * 전체 adapter 아이템 수를 반환합니다.<br>
     */
    public override fun getItemCount(): Int = adapterData.getTotalSize()

    /**
     * Returns content item at position or throws when invalid.<br><br>
     * position의 content 아이템을 반환하고 유효하지 않으면 예외를 발생시킵니다.<br>
     */
    @MainThread
    public fun getItem(position: Int): ITEM {
        assertMainThread("BaseRcvAdapter.getItem")
        requireInBounds(isPositionValid(position)) {
            "Invalid content position: $position, contentSize: ${adapterData.contentItems.size}"
        }
        return adapterData.contentItems[position]
    }

    /**
     * Returns immutable snapshot of current content items.<br><br>
     * 현재 content 아이템의 불변 스냅샷을 반환합니다.<br>
     */
    @MainThread
    public override fun getItems(): List<ITEM> =
        runOnMainThread("BaseRcvAdapter.getItems") { adapterData.contentItems.toList() }

    /**
     * Returns content item at position safely, or null.<br><br>
     * position의 content 아이템을 안전하게 조회하고 없으면 null을 반환합니다.<br>
     */
    @MainThread
    public override fun getItemOrNull(position: Int): ITEM? =
        runOnMainThread("BaseRcvAdapter.getItemOrNull") { adapterData.contentItems.getOrNull(position) }

    /**
     * Returns index of target content item, or -1 when not found.<br><br>
     * 대상 content 아이템의 인덱스를 반환하고 없으면 -1을 반환합니다.<br>
     */
    @MainThread
    public override fun getItemPosition(item: ITEM): Int =
        runOnMainThread("BaseRcvAdapter.getItemPosition") { adapterData.contentItems.indexOf(item) }

    /**
     * Returns mutable copy of current content items.<br><br>
     * 현재 content 아이템의 가변 복사본을 반환합니다.<br>
     * **Warning**: This is a snapshot copy. Mutations do NOT affect the adapter state.<br><br>
     * 경고: 이 리스트는 스냅샷 복사본이므로 변경해도 adapter 상태에 반영되지 않습니다.<br>
     */
    @MainThread
    public override fun getMutableItemList(): MutableList<ITEM> =
        runOnMainThread("BaseRcvAdapter.getMutableItemList") { adapterData.contentItems.toMutableList() }

    /**
     * Replaces all content items immediately.<br><br>
     * 전체 content 아이템을 즉시 교체합니다.<br>
     */
    @SuppressLint("NotifyDataSetChanged")
    @MainThread
    public override fun setItems(items: List<ITEM>, onResult: ((NormalAdapterResult) -> Unit)?) {
        assertMainThread("BaseRcvAdapter.setItems")
        adapterData.setContentItems(items)
        notifyDataSetChanged()
        runResultCallback(NormalAdapterResult.Applied, onResult)
    }

    /**
     * Appends a single content item immediately.<br><br>
     * content 아이템 1개를 즉시 추가합니다.<br>
     */
    @MainThread
    public override fun addItem(item: ITEM, onResult: ((NormalAdapterResult) -> Unit)?) {
        assertMainThread("BaseRcvAdapter.addItem")
        val insertPosition = adapterData.addContentItem(item)
        notifyItemInserted(insertPosition)
        runResultCallback(NormalAdapterResult.Applied, onResult)
    }

    /**
     * Inserts a content item at a position immediately.<br><br>
     * 지정한 위치에 content 아이템을 즉시 삽입합니다.<br>
     */
    @MainThread
    public override fun addItemAt(position: Int, item: ITEM, onResult: ((NormalAdapterResult) -> Unit)?) {
        val failure = commonDataLogic.validateAddItemAt(position, adapterData.contentItems.size)
        if (failure != null) {
            runResultCallback(failure.toNormalAdapterResult(), onResult)
            return
        }
        val insertStart = adapterData.addContentItemAt(position, item)
        notifyItemInserted(insertStart)
        runResultCallback(NormalAdapterResult.Applied, onResult)
    }

    /**
     * Appends multiple content items immediately.<br><br>
     * 여러 content 아이템을 즉시 추가합니다.<br>
     */
    @MainThread
    public override fun addItems(items: List<ITEM>, onResult: ((NormalAdapterResult) -> Unit)?) {
        val failure = commonDataLogic.validateAddItems(items)
        if (failure != null) {
            runResultCallback(failure.toNormalAdapterResult(), onResult)
            return
        }
        val insertStart = adapterData.addContentItems(items)
        notifyItemRangeInserted(insertStart, items.size)
        runResultCallback(NormalAdapterResult.Applied, onResult)
    }

    /**
     * Inserts multiple content items at a position immediately.<br><br>
     * 지정한 위치에 여러 content 아이템을 즉시 삽입합니다.<br>
     */
    @MainThread
    public override fun addItemsAt(position: Int, items: List<ITEM>, onResult: ((NormalAdapterResult) -> Unit)?) {
        val failure = commonDataLogic.validateAddItemsAt(items, position, adapterData.contentItems.size)
        if (failure != null) {
            runResultCallback(failure.toNormalAdapterResult(), onResult)
            return
        }
        val insertStart = adapterData.addContentItemsAt(position, items)
        notifyItemRangeInserted(insertStart, items.size)
        runResultCallback(NormalAdapterResult.Applied, onResult)
    }

    /**
     * Removes the first matching content item immediately.<br><br>
     * 첫 번째로 일치하는 content 아이템을 즉시 제거합니다.<br>
     */
    @MainThread
    public override fun removeItem(item: ITEM, onResult: ((NormalAdapterResult) -> Unit)?) {
        val failure = commonDataLogic.validateRemoveItem(item, adapterData.contentItems)
        if (failure != null) {
            runResultCallback(failure.toNormalAdapterResult(), onResult)
            return
        }
        val removePosition = adapterData.removeContentItem(item)
        notifyItemRemoved(removePosition)
        runResultCallback(NormalAdapterResult.Applied, onResult)
    }

    /**
     * Removes matching content items with best-effort semantics.<br><br>
     * best-effort 방식으로 일치하는 content 아이템들을 제거합니다.<br>
     *
     * **Note**: Each removal triggers an individual `notifyItemRemoved` call.<br>
     * For large or contiguous removals, prefer [removeRange] or [removeAll] for better performance.<br><br>
     * **주의**: 각 제거마다 `notifyItemRemoved`가 개별 호출됩니다.<br>
     * 대량 또는 연속 제거는 성능을 위해 [removeRange] 또는 [removeAll]을 사용하세요.<br>
     */
    @MainThread
    public override fun removeItems(items: List<ITEM>, onResult: ((NormalAdapterResult) -> Unit)?) {
        val failure = commonDataLogic.validateRemoveItems(items, adapterData.contentItems)
        if (failure != null) {
            runResultCallback(failure.toNormalAdapterResult(), onResult)
            return
        }
        val removeSet = items.toHashSet()
        val contentIndicesToRemove = adapterData.contentItems
            .mapIndexedNotNull { index, currentItem -> if (currentItem in removeSet) index else null }
            .reversed()
        contentIndicesToRemove.forEach { contentIndex ->
            val adapterPosition = adapterData.contentToAdapterPosition(contentIndex)
            adapterData.removeContentAt(contentIndex)
            notifyItemRemoved(adapterPosition)
        }
        runResultCallback(NormalAdapterResult.Applied, onResult)
    }

    /**
     * Removes a contiguous content range by start index and count.<br><br>
     * 시작 인덱스와 개수 기준으로 연속된 content 구간을 제거합니다.<br>
     */
    @MainThread
    public override fun removeRange(start: Int, count: Int, onResult: ((NormalAdapterResult) -> Unit)?) {
        val failure = commonDataLogic.validateRemoveRange(start, count, adapterData.contentItems.size)
        if (failure != null) {
            runResultCallback(failure.toNormalAdapterResult(), onResult)
            return
        }
        val startAdapterPosition = adapterData.removeContentRange(start, start + count)
        notifyItemRangeRemoved(startAdapterPosition, count)
        runResultCallback(NormalAdapterResult.Applied, onResult)
    }

    /**
     * Removes content item at position immediately.<br><br>
     * 지정한 위치의 content 아이템을 즉시 제거합니다.<br>
     */
    @MainThread
    public override fun removeAt(position: Int, onResult: ((NormalAdapterResult) -> Unit)?) {
        val failure = commonDataLogic.validateRemoveItemAt(position, adapterData.contentItems.size)
        if (failure != null) {
            runResultCallback(failure.toNormalAdapterResult(), onResult)
            return
        }
        val adapterPosition = adapterData.removeContentAt(position)
        notifyItemRemoved(adapterPosition)
        runResultCallback(NormalAdapterResult.Applied, onResult)
    }

    /**
     * Clears all content items immediately.<br><br>
     * 모든 content 아이템을 즉시 제거합니다.<br>
     */
    @MainThread
    public override fun removeAll(onResult: ((NormalAdapterResult) -> Unit)?) {
        assertMainThread("BaseRcvAdapter.removeAll")
        if (adapterData.contentItems.isEmpty()) {
            runResultCallback(NormalAdapterResult.Applied, onResult)
            return
        }
        val removedCount = adapterData.removeAllContentItems()
        notifyItemRangeRemoved(0, removedCount)
        runResultCallback(NormalAdapterResult.Applied, onResult)
    }

    /**
     * Moves a content item from one position to another immediately.<br><br>
     * content 아이템을 한 위치에서 다른 위치로 즉시 이동합니다.<br>
     */
    @MainThread
    public override fun moveItem(fromPosition: Int, toPosition: Int, onResult: ((NormalAdapterResult) -> Unit)?) {
        val failure = commonDataLogic.validateMoveItem(fromPosition, toPosition, adapterData.contentItems.size)
        if (failure != null) {
            runResultCallback(failure.toNormalAdapterResult(), onResult)
            return
        }
        if (fromPosition == toPosition) {
            runResultCallback(NormalAdapterResult.Applied, onResult)
            return
        }
        val (fromAdapterPosition, toAdapterPosition) = adapterData.moveContentItem(fromPosition, toPosition)
        notifyItemMoved(fromAdapterPosition, toAdapterPosition)
        runResultCallback(NormalAdapterResult.Applied, onResult)
    }

    /**
     * Replaces content item at position immediately.<br><br>
     * 지정한 위치의 content 아이템을 즉시 교체합니다.<br>
     */
    @MainThread
    public override fun replaceItemAt(position: Int, item: ITEM, onResult: ((NormalAdapterResult) -> Unit)?) {
        val failure = commonDataLogic.validateReplaceItemAt(position, adapterData.contentItems.size)
        if (failure != null) {
            runResultCallback(failure.toNormalAdapterResult(), onResult)
            return
        }
        val adapterPosition = adapterData.replaceContentAt(position, item)
        notifyItemChanged(adapterPosition)
        runResultCallback(NormalAdapterResult.Applied, onResult)
    }

    /**
     * Binds holder without payloads.<br><br>
     * payload 없이 holder를 바인딩합니다.<br>
     */
    public override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItemOrNull(position)
        if (item == null) {
            Logx.e("Cannot bind content item, contentPosition=$position, contentSize=${adapterData.contentItems.size}")
            return
        }
        onBindViewHolder(holder, item, position)
    }

    /**
     * Binds holder with payloads when provided.<br><br>
     * payload가 제공되면 holder를 payload 기반으로 바인딩합니다.<br>
     */
    public override fun onBindViewHolder(holder: VH, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
            return
        }

        val item = getItemOrNull(position)
        if (item == null) {
            Logx.e("Cannot bind content item with payload, contentPosition=$position, contentSize=${adapterData.contentItems.size}")
            return
        }
        onBindViewHolder(holder, item, position, payloads)
    }

    /**
     * Checks whether content position is valid in current content bounds.<br><br>
     * 현재 content 범위에서 position 유효성을 확인합니다.<br>
     */
    protected fun isPositionValid(position: Int): Boolean =
        commonDataLogic.isPositionValid(position, adapterData.contentItems.size)

    /**
     * Sets item click listener.<br><br>
     * 아이템 클릭 리스너를 설정합니다.<br>
     */
    @MainThread
    public override fun setOnItemClickListener(listener: (Int, ITEM, View) -> Unit) {
        assertMainThread("BaseRcvAdapter.setOnItemClickListener")
        clickData.onItemClickListener = listener
    }

    /**
     * Sets item long-click listener.<br><br>
     * 아이템 롱클릭 리스너를 설정합니다.<br>
     */
    @MainThread
    public override fun setOnItemLongClickListener(listener: (Int, ITEM, View) -> Unit) {
        assertMainThread("BaseRcvAdapter.setOnItemLongClickListener")
        clickData.onItemLongClickListener = listener
    }
}
