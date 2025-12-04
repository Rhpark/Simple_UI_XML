package kr.open.library.simple_ui.xml.ui.view.recyclerview

/**
 * Represents the scroll direction of a RecyclerView.<br><br>
 * RecyclerView의 스크롤 방향을 나타냅니다.<br>
 *
 * @see RecyclerScrollStateView For the custom RecyclerView that uses this class.<br><br>
 *      이 클래스를 사용하는 커스텀 RecyclerView는 RecyclerScrollStateView를 참조하세요.<br>
 */
public sealed class ScrollDirection {
    /**
     * Scrolling upward (vertical).<br><br>
     * 위로 스크롤 중 (수직).<br>
     */
    public data object UP : ScrollDirection()

    /**
     * Scrolling downward (vertical).<br><br>
     * 아래로 스크롤 중 (수직).<br>
     */
    public data object DOWN : ScrollDirection()

    /**
     * Scrolling left (horizontal).<br><br>
     * 왼쪽으로 스크롤 중 (수평).<br>
     */
    public data object LEFT : ScrollDirection()

    /**
     * Scrolling right (horizontal).<br><br>
     * 오른쪽으로 스크롤 중 (수평).<br>
     */
    public data object RIGHT : ScrollDirection()

    /**
     * Not scrolling (idle state).<br><br>
     * 스크롤하지 않음 (유휴 상태).<br>
     */
    public data object IDLE : ScrollDirection()
}

/**
 * Represents the edge position of a RecyclerView.<br><br>
 * RecyclerView의 가장자리 위치를 나타냅니다.<br>
 *
 * @see RecyclerScrollStateView For the custom RecyclerView that uses this class.<br><br>
 *      이 클래스를 사용하는 커스텀 RecyclerView는 RecyclerScrollStateView를 참조하세요.<br>
 */
public sealed class ScrollEdge {
    /**
     * Top edge of the RecyclerView.<br><br>
     * RecyclerView의 상단 가장자리.<br>
     */
    public data object TOP : ScrollEdge()

    /**
     * Bottom edge of the RecyclerView.<br><br>
     * RecyclerView의 하단 가장자리.<br>
     */
    public data object BOTTOM : ScrollEdge()

    /**
     * Left edge of the RecyclerView.<br><br>
     * RecyclerView의 왼쪽 가장자리.<br>
     */
    public data object LEFT : ScrollEdge()

    /**
     * Right edge of the RecyclerView.<br><br>
     * RecyclerView의 오른쪽 가장자리.<br>
     */
    public data object RIGHT : ScrollEdge()
}
