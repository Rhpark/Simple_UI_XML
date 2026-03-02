package kr.open.library.simple_ui.xml.ui.adapter.normal.headerfooter

import kr.open.library.simple_ui.xml.ui.adapter.normal.base.BaseRcvAdapterData

/**
 * Section data store for [HeaderFooterRcvAdapter].<br><br>
 * [HeaderFooterRcvAdapter]용 섹션 데이터 저장소입니다.<br>
 *
 * Extends [BaseRcvAdapterData] to add header and footer sections.<br><br>
 * [BaseRcvAdapterData]를 확장해 header와 footer 섹션을 추가합니다.<br>
 */
internal class HeaderFooterAdapterData<ITEM> : BaseRcvAdapterData<ITEM>() {
    /**
     * Mutable header section items.<br><br>
     * header 섹션의 가변 아이템 목록입니다.<br>
     */
    internal val headerItems: MutableList<ITEM> = mutableListOf()

    /**
     * Mutable footer section items.<br><br>
     * footer 섹션의 가변 아이템 목록입니다.<br>
     */
    internal val footerItems: MutableList<ITEM> = mutableListOf()

    /**
     * Returns section item for the resolved adapter position.<br><br>
     * 해석된 adapter 위치의 섹션 아이템을 반환합니다.<br>
     */
    fun getSectionItemOrNull(resolved: HeaderFooterRcvAdapterSectionPosition): ITEM? =
        when (resolved.sectionType) {
            HeaderFooterRcvAdapterSectionType.HEADER -> headerItems.getOrNull(resolved.sectionPosition)
            HeaderFooterRcvAdapterSectionType.CONTENT -> contentItems.getOrNull(resolved.sectionPosition)
            HeaderFooterRcvAdapterSectionType.FOOTER -> footerItems.getOrNull(resolved.sectionPosition)
        }

    /**
     * Returns total item count including header, content, and footer.<br><br>
     * header, content, footer를 포함한 전체 아이템 수를 반환합니다.<br>
     */
    internal override fun getTotalSize(): Int = headerItems.size + contentItems.size + footerItems.size

    /**
     * Replaces all header items.<br><br>
     * 전체 header 아이템을 교체합니다.<br>
     */
    internal fun setHeaderItems(items: List<ITEM>) {
        headerItems.clear()
        headerItems.addAll(items)
    }

    /**
     * Replaces all footer items.<br><br>
     * 전체 footer 아이템을 교체합니다.<br>
     */
    internal fun setFooterItems(items: List<ITEM>) {
        footerItems.clear()
        footerItems.addAll(items)
    }

    /**
     * Appends one header item and returns its adapter position.<br><br>
     * header 아이템 1개를 추가하고 adapter 위치를 반환합니다.<br>
     */
    internal fun addHeaderItem(item: ITEM): Int {
        val insertPosition = headerItems.size
        headerItems.add(item)
        return insertPosition
    }

    /**
     * Inserts one header item at a section position and returns its adapter position.<br><br>
     * header 섹션 위치에 아이템을 삽입하고 adapter 위치를 반환합니다.<br>
     */
    internal fun addHeaderItemAt(position: Int, item: ITEM): Int {
        headerItems.add(position, item)
        return position
    }

    /**
     * Appends header items and returns inserted adapter start position.<br><br>
     * header 아이템 목록을 추가하고 삽입 시작 adapter 위치를 반환합니다.<br>
     */
    internal fun addHeaderItems(items: List<ITEM>): Int {
        val insertStart = headerItems.size
        headerItems.addAll(items)
        return insertStart
    }

    /**
     * Clears all header items and returns removed count.<br><br>
     * 전체 header 아이템을 제거하고 제거 개수를 반환합니다.<br>
     */
    internal fun clearHeaderItems(): Int {
        val removedCount = headerItems.size
        headerItems.clear()
        return removedCount
    }

    /**
     * Appends one footer item and returns its adapter insert position.<br><br>
     * footer 아이템 1개를 추가하고 adapter 삽입 위치를 반환합니다.<br>
     */
    internal fun addFooterItem(item: ITEM): Int {
        val insertStart = footerToAdapterPosition(footerItems.size)
        footerItems.add(item)
        return insertStart
    }

    /**
     * Inserts one footer item and returns its adapter insert position.<br><br>
     * footer 아이템 1개를 삽입하고 adapter 삽입 위치를 반환합니다.<br>
     */
    internal fun addFooterItemAt(position: Int, item: ITEM): Int {
        val insertStart = footerToAdapterPosition(position)
        footerItems.add(position, item)
        return insertStart
    }

    /**
     * Appends footer items and returns inserted adapter start position.<br><br>
     * footer 아이템 목록을 추가하고 삽입 시작 adapter 위치를 반환합니다.<br>
     */
    internal fun addFooterItems(items: List<ITEM>): Int {
        val insertStart = footerToAdapterPosition(footerItems.size)
        footerItems.addAll(items)
        return insertStart
    }

    /**
     * Clears footer items and returns adapter start position before removal.<br><br>
     * footer를 비우기 전 제거 시작 adapter 위치를 반환합니다.<br>
     */
    internal fun clearFooterItems(): Int {
        val start = footerToAdapterPosition(0)
        footerItems.clear()
        return start
    }

    /**
     * Converts content section position to adapter position.<br><br>
     * content 섹션 위치를 adapter 위치로 변환합니다.<br>
     */
    override fun contentToAdapterPosition(contentPosition: Int): Int = headerItems.size + contentPosition

    /**
     * Converts footer section position to adapter position.<br><br>
     * footer 섹션 위치를 adapter 위치로 변환합니다.<br>
     */
    internal fun footerToAdapterPosition(footerPosition: Int): Int =
        headerItems.size + contentItems.size + footerPosition

    /**
     * Converts adapter position to content section position when possible.<br><br>
     * 가능하면 adapter 위치를 content 섹션 위치로 변환합니다.<br>
     */
    internal fun adapterToContentPosition(adapterPosition: Int): Int? {
        val contentPosition = adapterPosition - headerItems.size
        return if (contentPosition in 0 until contentItems.size) {
            contentPosition
        } else {
            null
        }
    }

    /**
     * Resolves adapter position into section type and section position.<br><br>
     * adapter 위치를 섹션 타입과 섹션 위치로 해석합니다.<br>
     *
     * Precondition: [adapterPosition] must be in `0 until getTotalSize()`.<br>
     * Callers are responsible for validating bounds before calling this function.<br><br>
     * 전제 조건: [adapterPosition]은 `0 until getTotalSize()` 범위여야 합니다.<br>
     * 호출자가 이 함수를 호출하기 전에 범위를 검증해야 합니다.<br>
     */
    internal fun resolveSectionPosition(adapterPosition: Int): HeaderFooterRcvAdapterSectionPosition {
        require(adapterPosition >= 0 && adapterPosition < getTotalSize()) {
            "adapterPosition $adapterPosition is out of bounds [0, ${getTotalSize()})"
        }
        val headerSize = headerItems.size
        val contentSize = contentItems.size
        return when {
            adapterPosition < headerSize -> {
                HeaderFooterRcvAdapterSectionPosition(
                    sectionType = HeaderFooterRcvAdapterSectionType.HEADER,
                    adapterPosition = adapterPosition,
                    sectionPosition = adapterPosition,
                )
            }

            adapterPosition < headerSize + contentSize -> {
                HeaderFooterRcvAdapterSectionPosition(
                    sectionType = HeaderFooterRcvAdapterSectionType.CONTENT,
                    adapterPosition = adapterPosition,
                    sectionPosition = adapterPosition - headerSize,
                )
            }

            else -> {
                HeaderFooterRcvAdapterSectionPosition(
                    sectionType = HeaderFooterRcvAdapterSectionType.FOOTER,
                    adapterPosition = adapterPosition,
                    sectionPosition = adapterPosition - headerSize - contentSize,
                )
            }
        }
    }
}
