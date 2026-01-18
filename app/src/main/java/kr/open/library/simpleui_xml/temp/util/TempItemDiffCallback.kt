package kr.open.library.simpleui_xml.temp.util

import androidx.recyclerview.widget.DiffUtil
import kr.open.library.simpleui_xml.temp.data.TempItem

/**
 * Custom DiffUtil callback for TempItem based on stable id and content.<br><br>
 * 안정적인 ID와 콘텐츠 비교를 사용하는 TempItem 전용 DiffUtil 콜백입니다.<br>
 */
class TempItemDiffCallback : DiffUtil.ItemCallback<TempItem>() {
    /**
     * Checks whether two items represent the same entity by id.<br><br>
     * ID 기준으로 두 아이템이 동일 엔티티인지 판단합니다.<br>
     */
    override fun areItemsTheSame(oldItem: TempItem, newItem: TempItem): Boolean =
        oldItem.id == newItem.id

    /**
     * Checks whether item contents are equal.<br><br>
     * 아이템 콘텐츠가 동일한지 판단합니다.<br>
     */
    override fun areContentsTheSame(oldItem: TempItem, newItem: TempItem): Boolean =
        oldItem == newItem
}
