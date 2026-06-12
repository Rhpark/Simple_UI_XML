package kr.open.library.simple_ui.compose.scroll

/**
 * Represents the edge position of a lazy list.<br>
 * LazyList의 가장자리 위치를 나타냅니다.<br>
 *
 * Used with [rememberEdgeReachedState] to observe when a lazy list reaches a particular edge.<br>
 * [rememberEdgeReachedState]와 함께 사용하여 LazyList가 특정 가장자리에 도달하는 시점을 관찰합니다.<br>
 *
 * @see rememberEdgeReachedState For the Composable that produces edge-reached state.<br><br>
 *      가장자리 도달 상태를 생성하는 Composable은 rememberEdgeReachedState를 참조하세요.<br>
 */
public enum class ScrollEdge {
    /**
     * Top edge (vertical list).<br><br>
     * 상단 가장자리 (수직 리스트).<br>
     */
    TOP,

    /**
     * Bottom edge (vertical list).<br><br>
     * 하단 가장자리 (수직 리스트).<br>
     */
    BOTTOM,

    /**
     * Left edge (horizontal list).<br><br>
     * 왼쪽 가장자리 (수평 리스트).<br>
     */
    LEFT,

    /**
     * Right edge (horizontal list).<br><br>
     * 오른쪽 가장자리 (수평 리스트).<br>
     */
    RIGHT,
}
