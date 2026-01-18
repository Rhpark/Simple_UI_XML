package kr.open.library.simple_ui.xml.ui.temp.base.list.diffcallback

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil

/**
 * Default DiffUtil callback using equality comparisons.<br><br>
 * 동등성 비교를 사용하는 기본 DiffUtil 콜백입니다.<br>
 *
 * Use a custom callback when items have stable IDs or equality is expensive.<br><br>
 * 안정적인 ID가 있거나 동등성 비교 비용이 큰 경우 커스텀 콜백을 사용하세요.<br>
 *
 * Example: compare IDs in areItemsTheSame and UI-relevant fields in areContentsTheSame.<br><br>
 * 예: areItemsTheSame은 ID 비교, areContentsTheSame은 UI 반영 필드 비교를 권장합니다.<br>
 *
 * For large replacements, prefer disabling DiffUtil in normal adapters or simplifying comparisons in ListAdapter.<br><br>
 * 대량 교체는 일반 어댑터에서 DiffUtil 비활성, ListAdapter는 비교 단순화를 권장합니다.<br>
 */
class DefaultDiffCallback<ITEM : Any> : DiffUtil.ItemCallback<ITEM>() {
    /**
     * Determines whether two items represent the same entity.<br><br>
     * 두 아이템이 동일한 엔티티인지 판단합니다.<br>
     */
    override fun areItemsTheSame(oldItem: ITEM, newItem: ITEM): Boolean = oldItem == newItem

    /**
     * Determines whether item contents are the same.<br><br>
     * 아이템 콘텐츠가 동일한지 판단합니다.<br>
     */
    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: ITEM, newItem: ITEM): Boolean = oldItem == newItem
}
