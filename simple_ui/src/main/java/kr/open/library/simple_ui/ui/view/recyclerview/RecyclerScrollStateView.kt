package kr.open.library.simple_ui.ui.view.recyclerview

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kr.open.library.logcat.Logx
import kr.open.library.simple_ui.R
import java.lang.ref.WeakReference
import kotlin.math.abs

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

    private var isAtTop = false
    private var isAtBottom = false
    private var isAtLeft = false
    private var isAtRight = false

    private var edgeReachThreshold = DEFAULT_EDGE_REACH_THRESHOLD // Threshold for edge reach detection
    private var scrollDirectionThreshold = DEFAULT_SCROLL_DIRECTION_THRESHOLD // Threshold for scroll direction detection

    private var accumulatedDx = 0
    private var accumulatedDy = 0

    // WeakReference를 사용한 리스너 관리
    private var onEdgeReachedListener: WeakReference<OnEdgeReachedListener>? = null
    private var onScrollDirectionChangedListener: WeakReference<OnScrollDirectionChangedListener>? = null

    private var currentScrollDirection : ScrollDirection = ScrollDirection.IDLE

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
                resetScrollAccumulation()
                updateScrollDirection(ScrollDirection.IDLE)
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            checkEdgeReach()
            updateScrollDirection(dx, dy)
        }
    }

    private fun resetScrollAccumulation() {
        accumulatedDx = 0
        accumulatedDy = 0
    }

    private fun updateScrollDirection(direction: ScrollDirection) {
        currentScrollDirection = direction
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

        val newIsAtTop = computeVerticalScrollOffset() <= edgeReachThreshold
        if (newIsAtTop != isAtTop) {
            isAtTop = newIsAtTop
            onEdgeReachedListener?.get()?.onEdgeReached(ScrollEdge.TOP, newIsAtTop)
            msfEdgeReachedFlow.safeEmit(ScrollEdge.TOP to newIsAtTop) {
                Logx.w("Failure emit Edge ${ScrollEdge.TOP}, $newIsAtTop")
            }
        }

        val isBottomReached = !canScrollVertically(1) &&
                computeVerticalScrollExtent() + computeVerticalScrollOffset() + edgeReachThreshold >= computeVerticalScrollRange()

        if (isBottomReached != isAtBottom) {
            isAtBottom = isBottomReached
            onEdgeReachedListener?.get()?.onEdgeReached(ScrollEdge.BOTTOM, isBottomReached)
            msfEdgeReachedFlow.safeEmit(ScrollEdge.BOTTOM to isBottomReached) {
                Logx.w("Failure emit Edge ${ScrollEdge.BOTTOM}, $isBottomReached")
            }
        }
    }

    /**
     * Left,Right Reach Detection
     */
    private fun checkHorizontalEdges() {

        val newIsAtLeft = computeHorizontalScrollOffset() <= edgeReachThreshold
        if (newIsAtLeft != isAtLeft) {
            isAtLeft = newIsAtLeft
            onEdgeReachedListener?.get()?.onEdgeReached(ScrollEdge.LEFT, newIsAtLeft)
            msfEdgeReachedFlow.safeEmit(ScrollEdge.LEFT to isAtLeft) {
                Logx.w("Failure emit Edge ${ScrollEdge.LEFT}, $isAtLeft")
            }
        }

        val isRightReached = !canScrollHorizontally(1) && computeHorizontalScrollExtent() + computeHorizontalScrollOffset() + edgeReachThreshold >= computeHorizontalScrollRange()
        if (isRightReached != isAtRight) {
            isAtRight = isRightReached
            onEdgeReachedListener?.get()?.onEdgeReached(ScrollEdge.RIGHT, isRightReached)
            msfEdgeReachedFlow.safeEmit(ScrollEdge.RIGHT to isAtRight) {
                Logx.w("Failure emit Edge ${ScrollEdge.RIGHT}, $isAtRight")
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
        accumulatedDy += dy
        if (abs(accumulatedDy) >= scrollDirectionThreshold) {
            val newVerticalScrollDirection = if(accumulatedDy > 0) ScrollDirection.DOWN else ScrollDirection.UP

            if (newVerticalScrollDirection != currentScrollDirection) {
                updateScrollDirection(newVerticalScrollDirection)
            }
            accumulatedDy = 0
        }
    }

    private fun updateHorizontalScrollDirection(dx: Int) {
        accumulatedDx += dx
        if (abs(accumulatedDx) >= scrollDirectionThreshold) {
            val newHorizontalScrollDirection =if(accumulatedDx > 0) ScrollDirection.RIGHT else ScrollDirection.LEFT

            if (newHorizontalScrollDirection != currentScrollDirection) {
                updateScrollDirection(newHorizontalScrollDirection)
            }
            accumulatedDx = 0
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
        this.scrollDirectionThreshold = minimumScrollMovementRange
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
        this.edgeReachThreshold = edgeReachThreshold
    }
}