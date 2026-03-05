package kr.open.library.simple_ui.xml.ui.adapter.normal.headerfooter

import androidx.annotation.MainThread
import androidx.recyclerview.widget.RecyclerView
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.xml.ui.adapter.normal.base.BaseRcvAdapter
import kr.open.library.simple_ui.xml.ui.adapter.normal.result.NormalAdapterResult
import kr.open.library.simple_ui.xml.ui.adapter.normal.result.toNormalAdapterResult

/**
 * RecyclerView.Adapter with header, content, and footer section support.<br><br>
 * header, content, footer 섹션을 지원하는 RecyclerView.Adapter입니다.<br>
 *
 * Features:<br>
 * 주요 기능:<br>
 * - Header and footer CRUD with precise notify calls.<br>
 * - header와 footer CRUD를 정확한 notify 호출과 함께 처리합니다.<br>
 * - Resolve adapter position into section type and section position.<br>
 * - adapter 위치를 섹션 타입과 섹션 위치로 해석합니다.<br>
 * - Map click callbacks from adapter position to content position.<br>
 * - 클릭 콜백을 adapter 위치에서 content 위치로 매핑합니다.<br>
 * - Per-section bind hooks for header, content, and footer.<br>
 * - header, content, footer별 바인딩 훅을 제공합니다.<br>
 * - Per-section view type hooks for header, content, and footer.<br><br>
 * - header, content, footer별 viewType 훅을 제공합니다.<br>
 *
 * Data store:<br>
 * 데이터 저장소:<br>
 * - Backed by [HeaderFooterAdapterData].<br><br>
 * - [HeaderFooterAdapterData]가 section 데이터를 관리합니다.<br>
 *
 * @param ITEM Item type shared across all sections.<br><br>
 *             모든 섹션에서 공통으로 사용하는 아이템 타입입니다.<br>
 * @param VH ViewHolder type used by this adapter.<br><br>
 *           이 어댑터가 사용하는 ViewHolder 타입입니다.<br>
 */
public abstract class HeaderFooterRcvAdapter<ITEM : Any, VH : RecyclerView.ViewHolder> : BaseRcvAdapter<ITEM, VH>() {
    internal override val adapterData: HeaderFooterAdapterData<ITEM> = HeaderFooterAdapterData()

    /**
     * Returns immutable snapshot of current header items.<br><br>
     * 현재 header 아이템의 불변 스냅샷을 반환합니다.<br>
     */
    @MainThread
    public fun getHeaderItems(): List<ITEM> =
        runOnMainThread("HeaderFooterRcvAdapter.getHeaderItems") { adapterData.headerItems.toList() }

    /**
     * Returns immutable snapshot of current footer items.<br><br>
     * 현재 footer 아이템의 불변 스냅샷을 반환합니다.<br>
     */
    @MainThread
    public fun getFooterItems(): List<ITEM> =
        runOnMainThread("HeaderFooterRcvAdapter.getFooterItems") { adapterData.footerItems.toList() }

    /**
     * Returns immutable snapshot of all sections combined.<br><br>
     * header + content + footer 전체 섹션의 불변 스냅샷을 반환합니다.<br>
     */
    @MainThread
    public fun getAllItems(): List<ITEM> = runOnMainThread("HeaderFooterRcvAdapter.getAllItems") {
        buildList(getItemCount()) {
            addAll(adapterData.headerItems)
            addAll(adapterData.contentItems)
            addAll(adapterData.footerItems)
        }
    }

    /**
     * Replaces all header items immediately.<br><br>
     * 전체 header 아이템을 즉시 교체합니다.<br>
     * Always reports [NormalAdapterResult.Applied].<br><br>
     * 항상 [NormalAdapterResult.Applied]를 전달합니다.<br>
     */
    @MainThread
    public fun setHeaderItems(items: List<ITEM>, onResult: ((NormalAdapterResult) -> Unit)? = null) {
        assertMainThread("HeaderFooterRcvAdapter.setHeaderItems")
        val oldHeaders = adapterData.headerItems.toList()
        val oldSize = oldHeaders.size
        val newSize = items.size

        val canRangeChange = oldSize == newSize &&
            oldSize > 0 &&
            oldHeaders.indices.all { index ->
                getHeaderItemViewType(index, oldHeaders[index]) == getHeaderItemViewType(index, items[index])
            }

        when {
            oldSize == 0 && newSize == 0 -> Unit

            oldSize == 0 -> {
                adapterData.setHeaderItems(items)
                notifyItemRangeInserted(0, newSize)
            }

            newSize == 0 -> {
                adapterData.setHeaderItems(emptyList())
                notifyItemRangeRemoved(0, oldSize)
            }

            canRangeChange -> {
                adapterData.setHeaderItems(items)
                notifyItemRangeChanged(0, newSize)
            }

            else -> {
                adapterData.setHeaderItems(emptyList())
                notifyItemRangeRemoved(0, oldSize)
                adapterData.setHeaderItems(items)
                notifyItemRangeInserted(0, newSize)
            }
        }
        runResultCallback(NormalAdapterResult.Applied, onResult)
    }

    /**
     * Replaces all footer items immediately.<br><br>
     * 전체 footer 아이템을 즉시 교체합니다.<br>
     * Always reports [NormalAdapterResult.Applied].<br><br>
     * 항상 [NormalAdapterResult.Applied]를 전달합니다.<br>
     */
    @MainThread
    public fun setFooterItems(items: List<ITEM>, onResult: ((NormalAdapterResult) -> Unit)? = null) {
        assertMainThread("HeaderFooterRcvAdapter.setFooterItems")
        val oldFooters = adapterData.footerItems.toList()
        val oldSize = oldFooters.size
        val newSize = items.size
        val footerStart = adapterData.footerToAdapterPosition(0)

        val canRangeChange = oldSize == newSize &&
            oldSize > 0 &&
            oldFooters.indices.all { index ->
                getFooterItemViewType(index, oldFooters[index]) == getFooterItemViewType(index, items[index])
            }

        when {
            oldSize == 0 && newSize == 0 -> Unit

            oldSize == 0 -> {
                adapterData.setFooterItems(items)
                notifyItemRangeInserted(footerStart, newSize)
            }

            newSize == 0 -> {
                adapterData.setFooterItems(emptyList())
                notifyItemRangeRemoved(footerStart, oldSize)
            }

            canRangeChange -> {
                adapterData.setFooterItems(items)
                notifyItemRangeChanged(footerStart, newSize)
            }

            else -> {
                adapterData.setFooterItems(emptyList())
                notifyItemRangeRemoved(footerStart, oldSize)
                adapterData.setFooterItems(items)
                notifyItemRangeInserted(footerStart, newSize)
            }
        }
        runResultCallback(NormalAdapterResult.Applied, onResult)
    }

    /**
     * Appends a single header item.<br><br>
     * header 아이템 1개를 추가합니다.<br>
     * Always reports [NormalAdapterResult.Applied].<br><br>
     * 항상 [NormalAdapterResult.Applied]를 전달합니다.<br>
     */
    @MainThread
    public fun addHeaderItem(item: ITEM, onResult: ((NormalAdapterResult) -> Unit)? = null) {
        assertMainThread("HeaderFooterRcvAdapter.addHeaderItem")
        val insertPosition = adapterData.addHeaderItem(item)
        notifyItemInserted(insertPosition)
        runResultCallback(NormalAdapterResult.Applied, onResult)
    }

    /**
     * Inserts a header item at a specific position.<br><br>
     * 지정한 위치에 header 아이템을 삽입합니다.<br>
     */
    @MainThread
    public fun addHeaderItemAt(position: Int, item: ITEM, onResult: ((NormalAdapterResult) -> Unit)? = null) {
        val failure = commonDataLogic.validateAddItemAt(position, adapterData.headerItems.size)
        if (failure != null) {
            runResultCallback(failure.toNormalAdapterResult(), onResult)
            return
        }
        val insertPosition = adapterData.addHeaderItemAt(position, item)
        notifyItemInserted(insertPosition)
        runResultCallback(NormalAdapterResult.Applied, onResult)
    }

    /**
     * Appends multiple header items.<br><br>
     * 여러 header 아이템을 추가합니다.<br>
     */
    @MainThread
    public fun addHeaderItems(items: List<ITEM>, onResult: ((NormalAdapterResult) -> Unit)? = null) {
        val failure = commonDataLogic.validateAddItems(items)
        if (failure != null) {
            runResultCallback(failure.toNormalAdapterResult(), onResult)
            return
        }
        val insertStart = adapterData.addHeaderItems(items)
        notifyItemRangeInserted(insertStart, items.size)
        runResultCallback(NormalAdapterResult.Applied, onResult)
    }

    /**
     * Clears all header items.<br><br>
     * 모든 header 아이템을 제거합니다.<br>
     * Always reports [NormalAdapterResult.Applied].<br><br>
     * 항상 [NormalAdapterResult.Applied]를 전달합니다.<br>
     */
    @MainThread
    public fun clearHeaderItems(onResult: ((NormalAdapterResult) -> Unit)? = null) {
        assertMainThread("HeaderFooterRcvAdapter.clearHeaderItems")
        if (adapterData.headerItems.isEmpty()) {
            runResultCallback(NormalAdapterResult.Applied, onResult)
            return
        }
        val removedCount = adapterData.clearHeaderItems()
        notifyItemRangeRemoved(0, removedCount)
        runResultCallback(NormalAdapterResult.Applied, onResult)
    }

    /**
     * Appends a single footer item.<br><br>
     * footer 아이템 1개를 추가합니다.<br>
     * Always reports [NormalAdapterResult.Applied].<br><br>
     * 항상 [NormalAdapterResult.Applied]를 전달합니다.<br>
     */
    @MainThread
    public fun addFooterItem(item: ITEM, onResult: ((NormalAdapterResult) -> Unit)? = null) {
        assertMainThread("HeaderFooterRcvAdapter.addFooterItem")
        val insertPosition = adapterData.addFooterItem(item)
        notifyItemInserted(insertPosition)
        runResultCallback(NormalAdapterResult.Applied, onResult)
    }

    /**
     * Inserts a footer item at a specific footer position.<br><br>
     * 지정한 footer 위치에 footer 아이템을 삽입합니다.<br>
     */
    @MainThread
    public fun addFooterItemAt(position: Int, item: ITEM, onResult: ((NormalAdapterResult) -> Unit)? = null) {
        val failure = commonDataLogic.validateAddItemAt(position, adapterData.footerItems.size)
        if (failure != null) {
            runResultCallback(failure.toNormalAdapterResult(), onResult)
            return
        }
        val insertPosition = adapterData.addFooterItemAt(position, item)
        notifyItemInserted(insertPosition)
        runResultCallback(NormalAdapterResult.Applied, onResult)
    }

    /**
     * Appends multiple footer items.<br><br>
     * 여러 footer 아이템을 추가합니다.<br>
     */
    @MainThread
    public fun addFooterItems(items: List<ITEM>, onResult: ((NormalAdapterResult) -> Unit)? = null) {
        val failure = commonDataLogic.validateAddItems(items)
        if (failure != null) {
            runResultCallback(failure.toNormalAdapterResult(), onResult)
            return
        }
        val insertStart = adapterData.addFooterItems(items)
        notifyItemRangeInserted(insertStart, items.size)
        runResultCallback(NormalAdapterResult.Applied, onResult)
    }

    /**
     * Clears all footer items.<br><br>
     * 모든 footer 아이템을 제거합니다.<br>
     * Always reports [NormalAdapterResult.Applied].<br><br>
     * 항상 [NormalAdapterResult.Applied]를 전달합니다.<br>
     */
    @MainThread
    public fun clearFooterItems(onResult: ((NormalAdapterResult) -> Unit)? = null) {
        assertMainThread("HeaderFooterRcvAdapter.clearFooterItems")
        if (adapterData.footerItems.isEmpty()) {
            runResultCallback(NormalAdapterResult.Applied, onResult)
            return
        }
        val removedCount = adapterData.footerItems.size
        val start = adapterData.clearFooterItems()
        notifyItemRangeRemoved(start, removedCount)
        runResultCallback(NormalAdapterResult.Applied, onResult)
    }

    /**
     * Binds holder without payloads.<br><br>
     * payload 없이 holder를 바인딩합니다.<br>
     */
    public override fun onBindViewHolder(holder: VH, position: Int) {
        val resolved = resolveSectionPosition(position)
        if (resolved == null) {
            Logx.e("Cannot bind item, adapterPosition is $position, itemCount $itemCount")
            return
        }
        dispatchBindViewHolder(holder, resolved, emptyList())
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

        val resolved = resolveSectionPosition(position)
        if (resolved == null) {
            Logx.e("Cannot bind item with payload, adapterPosition is $position, itemCount $itemCount")
            return
        }
        dispatchBindViewHolder(holder, resolved, payloads)
    }

    /**
     * Dispatches bind calls to the matching section hook.<br><br>
     * 해석된 섹션에 맞는 바인딩 훅으로 분기합니다.<br>
     */
    private fun dispatchBindViewHolder(holder: VH, resolved: HeaderFooterRcvAdapterSectionPosition, payloads: List<Any>) {
        when (resolved.sectionType) {
            HeaderFooterRcvAdapterSectionType.HEADER -> {
                val item = adapterData.headerItems.getOrNull(resolved.sectionPosition)
                if (item == null) {
                    Logx.e(
                        "Cannot bind header item, headerPosition=${resolved.sectionPosition}, headerSize=${adapterData.headerItems.size}"
                    )
                    return
                }
                if (payloads.isEmpty()) onBindHeaderViewHolder(holder, item, resolved.sectionPosition)
                else onBindHeaderViewHolder(holder, item, resolved.sectionPosition, payloads)
            }

            HeaderFooterRcvAdapterSectionType.CONTENT -> {
                val item = adapterData.contentItems.getOrNull(resolved.sectionPosition)
                if (item == null) {
                    Logx.e(
                        "Cannot bind content item, contentPosition=${resolved.sectionPosition}, contentSize=${adapterData.contentItems.size}"
                    )
                    return
                }
                if (payloads.isEmpty()) onBindViewHolder(holder, item, resolved.sectionPosition)
                else onBindViewHolder(holder, item, resolved.sectionPosition, payloads)
            }

            HeaderFooterRcvAdapterSectionType.FOOTER -> {
                val item = adapterData.footerItems.getOrNull(resolved.sectionPosition)
                if (item == null) {
                    Logx.e(
                        "Cannot bind footer item, footerPosition=${resolved.sectionPosition}, footerSize=${adapterData.footerItems.size}"
                    )
                    return
                }
                if (payloads.isEmpty()) onBindFooterViewHolder(holder, item, resolved.sectionPosition)
                else onBindFooterViewHolder(holder, item, resolved.sectionPosition, payloads)
            }
        }
    }

    /**
     * Resolves adapter position into section metadata when within bounds.<br><br>
     * adapter 위치가 유효 범위이면 섹션 메타데이터로 해석합니다.<br>
     */
    private fun resolveSectionPosition(adapterPosition: Int): HeaderFooterRcvAdapterSectionPosition? {
        if (!commonDataLogic.isPositionValid(adapterPosition, itemCount)) {
            return null
        }
        return adapterData.resolveSectionPosition(adapterPosition)
    }

    /**
     * Header payload bind hook.<br><br>
     * header payload 바인딩 훅입니다.<br>
     */
    protected open fun onBindHeaderViewHolder(holder: VH, item: ITEM, position: Int, payloads: List<Any>) {
        onBindHeaderViewHolder(holder, item, position)
    }

    /**
     * Footer full bind hook.<br><br>
     * footer 전체 바인딩 훅입니다.<br>
     * Default delegates to the content bind contract.<br><br>
     * 기본 구현은 content 바인딩 계약으로 위임합니다.<br>
     */
    protected open fun onBindFooterViewHolder(holder: VH, item: ITEM, position: Int) {
        onBindViewHolder(holder, item, position)
    }

    /**
     * Footer payload bind hook.<br><br>
     * footer payload 바인딩 훅입니다.<br>
     */
    protected open fun onBindFooterViewHolder(holder: VH, item: ITEM, position: Int, payloads: List<Any>) {
        onBindFooterViewHolder(holder, item, position)
    }

    /**
     * Header section view type hook.<br><br>
     * header 섹션 viewType 훅입니다.<br>
     */
    protected open fun getHeaderItemViewType(position: Int, item: ITEM): Int =
        getContentItemViewType(position, item)

    /**
     * Footer section view type hook.<br><br>
     * footer 섹션 viewType 훅입니다.<br>
     */
    protected open fun getFooterItemViewType(position: Int, item: ITEM): Int =
        getContentItemViewType(position, item)

    /**
     * Returns the view type for the given adapter position by section.<br><br>
     * 주어진 adapter position을 섹션 기준으로 해석해 viewType을 반환합니다.<br>
     */
    public override fun getItemViewType(position: Int): Int {
        val resolved = resolveSectionPosition(position)
            ?: return super.getItemViewType(position)

        val item = adapterData.getSectionItemOrNull(resolved) ?: return super.getItemViewType(position)
        return when (resolved.sectionType) {
            HeaderFooterRcvAdapterSectionType.HEADER -> getHeaderItemViewType(resolved.sectionPosition, item)
            HeaderFooterRcvAdapterSectionType.CONTENT -> getContentItemViewType(resolved.sectionPosition, item)
            HeaderFooterRcvAdapterSectionType.FOOTER -> getFooterItemViewType(resolved.sectionPosition, item)
        }
    }

    /**
     * Attaches click and long-click listeners once and maps adapter index to content index.<br><br>
     * 클릭 및 롱클릭 리스너를 1회 연결하고 adapter 인덱스를 content 인덱스로 매핑합니다.<br>
     */
    final override fun bindClickListeners(holder: VH) {
        clickData.attachClickListeners(
            holder = holder,
            positionMapper = { adapterPosition -> adapterData.adapterToContentPosition(adapterPosition) },
            itemProvider = { contentPosition -> getItemOrNull(contentPosition) },
        )
        clickData.attachLongClickListeners(
            holder = holder,
            positionMapper = { adapterPosition -> adapterData.adapterToContentPosition(adapterPosition) },
            itemProvider = { contentPosition -> getItemOrNull(contentPosition) },
        )
    }

    /**
     * Clears all content items immediately. Always returns true.<br><br>
     * 모든 content 아이템을 즉시 제거합니다. 항상 true를 반환합니다.<br>
     * Always reports [NormalAdapterResult.Applied].<br><br>
     * 항상 [NormalAdapterResult.Applied]를 전달합니다.<br>
     */
    @MainThread
    public override fun removeAll(onResult: ((NormalAdapterResult) -> Unit)?) {
        assertMainThread("HeaderFooterRcvAdapter.removeAll")
        if (adapterData.contentItems.isEmpty()) {
            runResultCallback(NormalAdapterResult.Applied, onResult)
            return
        }
        val removedCount = adapterData.removeAllContentItems()
        notifyItemRangeRemoved(adapterData.headerItems.size, removedCount)
        runResultCallback(NormalAdapterResult.Applied, onResult)
    }

    /**
     * Header full bind hook.<br><br>
     * header 전체 바인딩 훅입니다.<br>
     * Default delegates to the content bind contract.<br><br>
     * 기본 구현은 content 바인딩 계약으로 위임합니다.<br>
     */
    protected open fun onBindHeaderViewHolder(holder: VH, item: ITEM, position: Int) {
        onBindViewHolder(holder, item, position)
    }
}
