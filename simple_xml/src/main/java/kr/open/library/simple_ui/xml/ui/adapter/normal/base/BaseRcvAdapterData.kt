package kr.open.library.simple_ui.xml.ui.adapter.normal.base

import androidx.recyclerview.widget.RecyclerView

/**
 * Section data store for BaseRcvAdapter.<br><br>
 * BaseRcvAdapter의 섹션 데이터 저장소입니다.<br>
 */
internal open class BaseRcvAdapterData<ITEM> {
    /**
     * Mutable content section items.<br><br>
     * content 섹션의 가변 아이템 목록입니다.<br>
     */
    internal val contentItems: MutableList<ITEM> = mutableListOf()

    /**
     * Returns total item count.<br><br>
     * 전체 아이템 개수를 반환합니다.<br>
     */
    internal open fun getTotalSize(): Int = contentItems.size

    /**
     * Replaces all content items.<br><br>
     * content 아이템 전체를 교체합니다.<br>
     */
    internal fun setContentItems(items: List<ITEM>) {
        contentItems.clear()
        contentItems.addAll(items)
    }

    /**
     * Appends one content item and returns adapter insert position.<br><br>
     * content 아이템을 1개 추가하고 adapter 삽입 위치를 반환합니다.<br>
     */
    internal fun addContentItem(item: ITEM): Int {
        val insertStart = contentToAdapterPosition(contentItems.size)
        contentItems.add(item)
        return insertStart
    }

    /**
     * Inserts one content item and returns adapter insert position.<br><br>
     * content 아이템을 삽입하고 adapter 삽입 위치를 반환합니다.<br>
     */
    internal fun addContentItemAt(position: Int, item: ITEM): Int {
        val insertStart = contentToAdapterPosition(position)
        contentItems.add(position, item)
        return insertStart
    }

    /**
     * Appends content items and returns adapter insert start position.<br><br>
     * content 아이템 목록을 추가하고 adapter 삽입 시작 위치를 반환합니다.<br>
     */
    internal fun addContentItems(items: List<ITEM>): Int {
        val insertStart = contentToAdapterPosition(contentItems.size)
        contentItems.addAll(items)
        return insertStart
    }

    /**
     * Inserts content items at section position and returns adapter start position.<br><br>
     * content 섹션 위치에 아이템 목록을 삽입하고 adapter 시작 위치를 반환합니다.<br>
     */
    internal fun addContentItemsAt(position: Int, items: List<ITEM>): Int {
        val insertStart = contentToAdapterPosition(position)
        contentItems.addAll(position, items)
        return insertStart
    }

    /**
     * Removes first matching content item and returns removed adapter position.<br><br>
     * 일치하는 첫 번째 content 아이템을 제거하고 제거된 adapter 위치를 반환합니다.<br>
     */
    internal fun removeContentItem(item: ITEM): Int {
        val contentPosition = contentItems.indexOf(item)
        if (contentPosition == RecyclerView.NO_POSITION) {
            return RecyclerView.NO_POSITION
        }
        contentItems.removeAt(contentPosition)
        return contentToAdapterPosition(contentPosition)
    }

    /**
     * Removes content range and returns adapter start position.<br><br>
     * content 구간을 제거하고 adapter 시작 위치를 반환합니다.<br>
     */
    internal fun removeContentRange(startPosition: Int, endExclusivePosition: Int): Int {
        val startAdapterPosition = contentToAdapterPosition(startPosition)
        contentItems.subList(startPosition, endExclusivePosition).clear()
        return startAdapterPosition
    }

    /**
     * Removes one content item and returns removed adapter position.<br><br>
     * content 아이템을 1개 제거하고 제거된 adapter 위치를 반환합니다.<br>
     */
    internal fun removeContentAt(position: Int): Int {
        val adapterPosition = contentToAdapterPosition(position)
        contentItems.removeAt(position)
        return adapterPosition
    }

    /**
     * Clears all content items and returns removed count.<br><br>
     * content 아이템 전체를 제거하고 제거 개수를 반환합니다.<br>
     */
    internal fun removeAllContentItems(): Int {
        val removedCount = contentItems.size
        contentItems.clear()
        return removedCount
    }

    /**
     * Moves content item and returns adapter from/to positions.<br><br>
     * content 아이템을 이동하고 adapter 이동 전/후 위치를 반환합니다.<br>
     */
    internal fun moveContentItem(fromPosition: Int, toPosition: Int): Pair<Int, Int> {
        val item = contentItems.removeAt(fromPosition)
        contentItems.add(toPosition, item)
        return contentToAdapterPosition(fromPosition) to contentToAdapterPosition(toPosition)
    }

    /**
     * Replaces content item and returns adapter changed position.<br><br>
     * content 아이템을 교체하고 adapter 변경 위치를 반환합니다.<br>
     */
    internal fun replaceContentAt(position: Int, item: ITEM): Int {
        contentItems[position] = item
        return contentToAdapterPosition(position)
    }

    /**
     * Converts content section position to adapter position.<br><br>
     * content 섹션 위치를 adapter 위치로 변환합니다.<br>
     */
    internal open fun contentToAdapterPosition(contentPosition: Int): Int = contentPosition
}
