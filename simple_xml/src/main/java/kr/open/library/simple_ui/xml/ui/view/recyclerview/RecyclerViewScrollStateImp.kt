package kr.open.library.simple_ui.xml.ui.view.recyclerview

import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * Listener interface for scroll direction change events.<br><br>
 * 스크롤 방향 변경 이벤트를 위한 리스너 인터페이스입니다.<br>
 *
 * @see RecyclerScrollStateView For the custom RecyclerView that uses this listener.<br><br>
 *      이 리스너를 사용하는 커스텀 RecyclerView는 RecyclerScrollStateView를 참조하세요.<br>
 */
public interface OnScrollDirectionChangedListener {
    /**
     * Called when the scroll direction changes.<br><br>
     * 스크롤 방향이 변경될 때 호출됩니다.<br>
     *
     * @param scrollDirection The new scroll direction.<br><br>
     *                        새로운 스크롤 방향.<br>
     */
    public fun onScrollDirectionChanged(scrollDirection: ScrollDirection)
}

/**
 * Listener interface for edge reach events.<br><br>
 * 가장자리 도달 이벤트를 위한 리스너 인터페이스입니다.<br>
 *
 * @see RecyclerScrollStateView For the custom RecyclerView that uses this listener.<br><br>
 *      이 리스너를 사용하는 커스텀 RecyclerView는 RecyclerScrollStateView를 참조하세요.<br>
 */
public interface OnEdgeReachedListener {
    /**
     * Called when an edge reach state changes.<br><br>
     * 가장자리 도달 상태가 변경될 때 호출됩니다.<br>
     *
     * @param edge The edge that was reached or left.<br><br>
     *             도달했거나 떠난 가장자리.<br>
     *
     * @param isReached True if the edge is reached, false if left.<br><br>
     *                  가장자리에 도달했으면 true, 떠났으면 false.<br>
     */
    public fun onEdgeReached(edge: ScrollEdge, isReached: Boolean)
}

/**
 * Extension function to safely emit a value to MutableSharedFlow.<br>
 * Calls the failure callback if emission fails.<br><br>
 * MutableSharedFlow에 안전하게 값을 발행하는 확장 함수입니다.<br>
 * 발행이 실패하면 failure 콜백을 호출합니다.<br>
 *
 * @param value The value to emit.<br><br>
 *              발행할 값.<br>
 *
 * @param failure Callback to invoke when emission fails.<br><br>
 *                발행이 실패했을 때 호출할 콜백.<br>
 *
 * @return True if emission succeeded, false otherwise.<br><br>
 *         발행이 성공하면 true, 그렇지 않으면 false.<br>
 */
public inline fun <T> MutableSharedFlow<T>.safeEmit(value: T, failure: () -> Unit): Boolean {
    return if (tryEmit(value)) {
        true
    } else {
        failure()
        false
    }
}
