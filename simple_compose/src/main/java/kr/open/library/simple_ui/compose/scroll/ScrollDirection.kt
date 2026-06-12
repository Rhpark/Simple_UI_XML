package kr.open.library.simple_ui.compose.scroll

/**
 * Represents the scroll direction of a lazy list.<br>
 * LazyList의 스크롤 방향을 나타냅니다.<br>
 *
 * The direction is determined by accumulating scroll deltas against a configurable threshold.<br>
 * 방향은 설정 가능한 임계값에 대해 스크롤 델타를 누적하여 결정됩니다.<br>
 *
 * @see rememberScrollDirectionState For the Composable that produces this state.<br><br>
 *      이 상태를 생성하는 Composable은 rememberScrollDirectionState를 참조하세요.<br>
 */
public enum class ScrollDirection {
    /**
     * Scrolling upward (vertical list).<br><br>
     * 위로 스크롤 중 (수직 리스트).<br>
     */
    UP,

    /**
     * Scrolling downward (vertical list).<br><br>
     * 아래로 스크롤 중 (수직 리스트).<br>
     */
    DOWN,

    /**
     * Scrolling left (horizontal list).<br><br>
     * 왼쪽으로 스크롤 중 (수평 리스트).<br>
     */
    LEFT,

    /**
     * Scrolling right (horizontal list).<br><br>
     * 오른쪽으로 스크롤 중 (수평 리스트).<br>
     */
    RIGHT,

    /**
     * Not scrolling (idle state).<br><br>
     * 스크롤하지 않음 (유휴 상태).<br>
     */
    IDLE,
}
