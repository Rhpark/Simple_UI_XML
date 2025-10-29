package kr.open.library.simple_ui.presenter.ui.view.recyclerview

import kotlinx.coroutines.flow.MutableSharedFlow

public interface OnScrollDirectionChangedListener {
    public fun onScrollDirectionChanged(scrollDirection: ScrollDirection)
}

public interface OnEdgeReachedListener {
    public fun onEdgeReached(edge: ScrollEdge, isReached: Boolean)
}

// MutableSharedFlow에 안전하게 이벤트 발행하는 확장 함수
public inline fun <T> MutableSharedFlow<T>.safeEmit(value: T, failure: () -> Unit): Boolean {
    return if (tryEmit(value)) {
        true
    } else {
        failure()
        false
    }
}
