package kr.open.library.simple_ui.xml.ui.adapter.normal.headerfooter

/**
 * Resolved section metadata for an adapter position.<br><br>
 * adapter 위치에 대한 섹션 해석 메타데이터입니다.<br>
 */
internal data class HeaderFooterRcvAdapterSectionPosition(
    /**
     * Resolved section type.<br><br>
     * 해석된 섹션 타입입니다.<br>
     */
    val sectionType: HeaderFooterRcvAdapterSectionType,
    /**
     * Original adapter position.<br><br>
     * 원본 adapter 위치입니다.<br>
     */
    val adapterPosition: Int,
    /**
     * Position inside the resolved section.<br><br>
     * 해석된 섹션 내부 위치입니다.<br>
     */
    val sectionPosition: Int,
)
