package kr.open.library.simple_ui.xml.system_manager.info.display

data class DisplayInfoBarInsets(
    val top: Int,
    val bottom: Int,
    val left: Int,
    val right: Int
) {
    // 내비게이션바 "두께"가 필요할 때 사용 (bottom/side 모두 커버)
    public val thickness: Int get() = maxOf(left, top, right, bottom)
    public val isEmpty: Boolean get() = thickness == 0
}
