package kr.open.library.simpleui_xml.temp.data

import kr.open.library.simpleui_xml.temp.multi.data.TempItemType

/**
 * Example item model used by temp adapter samples.<br><br>
 * temp 어댑터 샘플에서 사용하는 아이템 모델입니다.<br>
 */
data class TempItem(
    /**
     * Stable id for diffing and logging.<br><br>
     * Diff 및 로깅에 사용하는 안정적인 ID입니다.<br>
     */
    val id: Long,
    /**
     * Title text shown in the item view.<br><br>
     * 아이템 뷰에 표시되는 제목 텍스트입니다.<br>
     */
    val title: String,
    /**
     * Description text shown in the item view.<br><br>
     * 아이템 뷰에 표시되는 설명 텍스트입니다.<br>
     */
    val description: String,
    /**
     * Item type used for multi-type examples.<br><br>
     * 다중 타입 예제에서 사용하는 아이템 타입입니다.<br>
     */
    val type: TempItemType = TempItemType.PRIMARY,
)
