package kr.open.library.simple_ui.presenter.ui.view.recyclerview

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.VisibleForTesting
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kr.open.library.simple_ui.R
import kr.open.library.simple_ui.logcat.Logx
import java.lang.ref.WeakReference

/**
 * A custom RecyclerView that provides edge reach and scroll direction detection.
 *
 * This view extends RecyclerView and adds functionality to detect when the user
 * has scrolled to the edge of the view and the direction of the scroll.
 */
public open class RecyclerScrollStateView : RecyclerView {

    private companion object {
        private const val DEFAULT_EDGE_REACH_THRESHOLD = 10
        private const val DEFAULT_SCROLL_DIRECTION_THRESHOLD = 20
    }

    // 계산 로직을 담당하는 객체 (테스트 시 접근 가능)
    @VisibleForTesting
    internal val scrollStateCalculator = RecyclerScrollStateCalculator(
        edgeReachThreshold = DEFAULT_EDGE_REACH_THRESHOLD,
        scrollDirectionThreshold = DEFAULT_SCROLL_DIRECTION_THRESHOLD
    )

    // WeakReference를 사용한 리스너 관리
    private var onEdgeReachedListener: WeakReference<OnEdgeReachedListener>? = null
    private var onScrollDirectionChangedListener: WeakReference<OnScrollDirectionChangedListener>? = null

    // Flow를 통한 이벤트 스트림 (옵션)
    private val msfScrollDirectionFlow = MutableSharedFlow<ScrollDirection>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    public val sfScrollDirectionFlow: SharedFlow<ScrollDirection> = msfScrollDirectionFlow.asSharedFlow()

    private val msfEdgeReachedFlow = MutableSharedFlow<Pair<ScrollEdge, Boolean>>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    public val sfEdgeReachedFlow: SharedFlow<Pair<ScrollEdge, Boolean>> = msfEdgeReachedFlow.asSharedFlow()

    public constructor(context: Context) : super(context)
    public constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initTypeArray(attrs)
    }
    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr) {
        initTypeArray(attrs)
    }

    private fun initTypeArray(attrs: AttributeSet?) {

        attrs?.let {
            context.obtainStyledAttributes(it, R.styleable.RecyclerScrollStateView).apply {

                getString(R.styleable.RecyclerScrollStateView_scrollDirectionThreshold).also {
                    setScrollDirectionThreshold(it?.toInt() ?: DEFAULT_SCROLL_DIRECTION_THRESHOLD)
                }

                getString(R.styleable.RecyclerScrollStateView_edgeReachThreshold).also {
                    setEdgeReachThreshold(it?.toInt() ?: DEFAULT_EDGE_REACH_THRESHOLD)
                }
                recycle()
            }
        }
    }

    private val scrollListener = object : OnScrollListener() {

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if(newState == SCROLL_STATE_IDLE) {
                val result = scrollStateCalculator.resetScrollAccumulation()
                if (result.directionChanged) { notifyScrollDirectionChanged(result.newDirection) }
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            checkEdgeReach()
            updateScrollDirection(dx, dy)
        }
    }

    private fun notifyScrollDirectionChanged(direction: ScrollDirection) {
        onScrollDirectionChangedListener?.get()?.onScrollDirectionChanged(direction)
        msfScrollDirectionFlow.safeEmit(direction) {
            Logx.w("Fail emit data $direction")
        }
    }

    private fun checkEdgeReach() {
        when {
            isScrollVertical() -> checkVerticalEdges()
            isScrollHorizontal() -> checkHorizontalEdges()
        }
    }

    /**
     * Top,Bottom Reach Detection
     */
    private fun checkVerticalEdges() {
        val result = scrollStateCalculator.checkVerticalEdges(
            verticalScrollOffset = computeVerticalScrollOffset(),
            canScrollDown = canScrollVertically(1),
            verticalScrollExtent = computeVerticalScrollExtent(),
            verticalScrollRange = computeVerticalScrollRange()
        )

        if (result.topChanged) {
            onEdgeReachedListener?.get()?.onEdgeReached(ScrollEdge.TOP, result.isAtTop)
            msfEdgeReachedFlow.safeEmit(ScrollEdge.TOP to result.isAtTop) {
                Logx.w("Failure emit Edge ${ScrollEdge.TOP}, ${result.isAtTop}")
            }
        }

        if (result.bottomChanged) {
            onEdgeReachedListener?.get()?.onEdgeReached(ScrollEdge.BOTTOM, result.isAtBottom)
            msfEdgeReachedFlow.safeEmit(ScrollEdge.BOTTOM to result.isAtBottom) {
                Logx.w("Failure emit Edge ${ScrollEdge.BOTTOM}, ${result.isAtBottom}")
            }
        }
    }

    /**
     * Left,Right Reach Detection
     */
    private fun checkHorizontalEdges() {
        val result = scrollStateCalculator.checkHorizontalEdges(
            horizontalScrollOffset = computeHorizontalScrollOffset(),
            canScrollRight = canScrollHorizontally(1),
            horizontalScrollExtent = computeHorizontalScrollExtent(),
            horizontalScrollRange = computeHorizontalScrollRange()
        )

        if (result.leftChanged) {
            onEdgeReachedListener?.get()?.onEdgeReached(ScrollEdge.LEFT, result.isAtLeft)
            msfEdgeReachedFlow.safeEmit(ScrollEdge.LEFT to result.isAtLeft) {
                Logx.w("Failure emit Edge ${ScrollEdge.LEFT}, ${result.isAtLeft}")
            }
        }

        if (result.rightChanged) {
            onEdgeReachedListener?.get()?.onEdgeReached(ScrollEdge.RIGHT, result.isAtRight)
            msfEdgeReachedFlow.safeEmit(ScrollEdge.RIGHT to result.isAtRight) {
                Logx.w("Failure emit Edge ${ScrollEdge.RIGHT}, ${result.isAtRight}")
            }
        }
    }

    private fun isScrollVertical() =  layoutManager?.canScrollVertically() ?: false
    private fun isScrollHorizontal() =  layoutManager?.canScrollHorizontally() ?: false

    private fun updateScrollDirection(dx: Int, dy: Int) {
        when {
            isScrollVertical() -> updateVerticalScrollDirection(dy)
            isScrollHorizontal() -> updateHorizontalScrollDirection(dx)
        }
    }

    private fun updateVerticalScrollDirection(dy: Int) {
        val result = scrollStateCalculator.updateVerticalScrollDirection(dy)
        if (result.directionChanged) {
            notifyScrollDirectionChanged(result.newDirection)
        }
    }

    private fun updateHorizontalScrollDirection(dx: Int) {
        val result = scrollStateCalculator.updateHorizontalScrollDirection(dx)
        if (result.directionChanged) {
            notifyScrollDirectionChanged(result.newDirection)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        addOnScrollListener(scrollListener)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeOnScrollListener(scrollListener)
    }

    /**
     * Sets a listener to be notified of scroll direction changes.
     * 스크롤 방향 변경 알림을 받을 리스너를 설정합니다.
     *
     * This method allows you to register a callback that will be invoked
     * whenever the scroll direction of the `RecyclerScrollStateView` changes.
     * The scroll direction is determined based on the accumulated scroll distance
     * and the configured[scrollDirectionThreshold].
     *
     * The listener will receive a [ScrollDirection] value indicating the
     * current scroll direction:
     * - [ScrollDirection.UP, ScrollDirection.DOWN, ScrollDirection.LEFT, ScrollDirection.RIGHT, ScrollDirection.IDLE]
     *
     * @param listener The listener to be notified of scroll direction changes,
     */
    public fun setOnScrollDirectionListener(listener: OnScrollDirectionChangedListener?) {
        this.onScrollDirectionChangedListener = listener?.let { WeakReference(it) }
    }

    public fun setOnScrollDirectionListener(listener: (scrollDirection: ScrollDirection) -> Unit) {
        setOnScrollDirectionListener(object : OnScrollDirectionChangedListener {
            override fun onScrollDirectionChanged(scrollDirection: ScrollDirection) {
                listener(scrollDirection)
            }
        })
    }

    /**
     * Sets a listener to be notified when the RecyclerView reaches an edge.
     * RecyclerView가 가장자리에 도달했을 때 알림을 받을 리스너를 설정합니다.
     *
     * This method allows you to register a callback that will be invoked
     * whenever the `RecyclerScrollStateView` reaches one of its edges: top, bottom, left, or right.
     * The edge reach detection isdetermined based on the configured [edgeReachThreshold].
     *
     * The listener will receive an [Edge] value indicating which edge was reached,
     * and a boolean value indicating whether the edge is currently reached (`true`) or not (`false`).
     *
     * @param listener The listener to be notified of edge reach events,
     */
    public fun setOnReachEdgeListener(listener: OnEdgeReachedListener?) {
        this.onEdgeReachedListener = listener?.let { WeakReference(it) }
    }

    public fun setOnReachEdgeListener(listener: (edge: ScrollEdge, isReached: Boolean) -> Unit) {
        setOnReachEdgeListener(object : OnEdgeReachedListener {
            override fun onEdgeReached(edge: ScrollEdge, isReached: Boolean) {
                listener(edge, isReached)
            }
        })
    }

    /**
     * Sets the minimum scroll movement range required to trigger a scroll direction change event.
     * 스크롤 방향 변경 이벤트를 트리거하는 데 필요한 최소 스크롤 이동 범위(px)를 설정합니다.
     *
     * This method allows you to define the minimum distance (in pixels) that the
     * `RecyclerScrollStateView` must be scrolled in a particular direction
     * before a scroll direction change event is triggered.*
     * By default, the minimum scroll movement range is set to 20 pixels.
     * You can adjust this value to control the sensitivity of scroll direction
     * detection. A higher value will make the detection less sensitive, while a
     * lower value will make it more sensitive.
     *
     * @param minimumScrollMovementRange The minimum scroll movement range in pixels.
     * This value should be >= 0
     */
    public fun setScrollDirectionThreshold(minimumScrollMovementRange: Int) {
        require(minimumScrollMovementRange >= 0) {
            "minimumScrollMovementRange must be >= 0, but input value is $minimumScrollMovementRange"
        }
        scrollStateCalculator.updateThresholds(scrollDirectionThreshold = minimumScrollMovementRange)
    }

    /**
     * Sets the threshold distance (in pixels) from an edge that is considered as reaching the edge.
     * 가장자리에 도달한 것으로 가장자리로부터의 임계 거리(픽셀 단위)를 설정.
     *
     * This method allows you to define the distance from an edge (top, bottom, left, or right)
     * within which the `RecyclerScrollStateView` is considered to have reached thatedge.
     * By default, the edge reach threshold is set to 10 pixels.
     *
     * You can adjust this value to control the sensitivity of edge reach detection.
     * A higher value will make the detection less sensitive, while a lower value will
     * make it more sensitive.
     *
     * @param edgeReachThreshold The edge reach threshold distance in pixels.
     * This value should be >= 0
     */
    public fun setEdgeReachThreshold(edgeReachThreshold: Int) {
        require(edgeReachThreshold >= 0) {
            "edgeReachThreshold must be >= 0, but input value is $edgeReachThreshold"
        }
        scrollStateCalculator.updateThresholds(edgeReachThreshold = edgeReachThreshold)
    }
}