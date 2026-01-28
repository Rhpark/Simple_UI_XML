package kr.open.library.simpleui_xml.temp.util

import androidx.recyclerview.widget.DiffUtil
import kr.open.library.simpleui_xml.temp.data.TempItem

/**
 * Custom DiffUtil callback for TempItem based on stable id and content.<br><br>
 * 안정적인 ID와 콘텐츠 기반의 TempItem DiffUtil 콜백입니다.<br>
 */
class TempItemDiffCallback : DiffUtil.ItemCallback<TempItem>() {
    /**
     * Checks whether two items represent the same entity by id.<br><br>
     * ID 기준으로 동일 아이템인지 확인합니다.<br>
     */
    override fun areItemsTheSame(oldItem: TempItem, newItem: TempItem): Boolean =
        oldItem.id == newItem.id

    /**
     * Checks whether item contents are equal.<br><br>
     * 아이템 콘텐츠가 동일한지 확인합니다.<br>
     */
    override fun areContentsTheSame(oldItem: TempItem, newItem: TempItem): Boolean =
        oldItem == newItem
}
