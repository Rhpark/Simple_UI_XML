package kr.open.library.simple_ui.xml.ui.view.recyclerview

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.VisibleForTesting
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.xml.R
import java.lang.ref.WeakReference

/**
 * A custom RecyclerView that provides edge reach and scroll direction detection.<br>
 * This view extends RecyclerView and adds functionality to detect when the user
 * has scrolled to the edge of the view and the direction of the scroll.<br><br>
 * 가장자리 도달 및 스크롤 방향 감지 기능을 제공하는 커스텀 RecyclerView입니다.<br>
 * 이 뷰는 RecyclerView를 확장하여 사용자가 뷰의 가장자리로 스크롤했을 때와
 * 스크롤 방향을 감지하는 기능을 추가합니다.<br>
 *
 * Features:<br>
 * - Detects when scroll reaches top, bottom, left, or right edges<br>
 * - Tracks scroll direction (UP, DOWN, LEFT, RIGHT, IDLE)<br>
 * - Supports both listener callbacks and Kotlin Flow<br>
 * - Configurable thresholds for edge detection and direction sensitivity<br>
 * - Automatic lifecycle management for scroll listeners<br><br>
 * 기능:<br>
 * - 스크롤이 상단, 하단, 좌측, 우측 가장자리에 도달하는지 감지<br>
 * - 스크롤 방향 추적 (UP, DOWN, LEFT, RIGHT, IDLE)<br>
 * - 리스너 콜백과 Kotlin Flow 모두 지원<br>
 * - 가장자리 감지 및 방향 민감도에 대한 구성 가능한 임계값<br>
 * - 스크롤 리스너의 자동 생명주기 관리<br>
 *
 * Usage example:<br>
 * ```kotlin
 * val recyclerView = RecyclerScrollStateView(context)
 * recyclerView.setOnScrollDirectionListener { direction ->
 *     when (direction) {
 *         ScrollDirection.UP -> // Handle scroll up
 *         ScrollDirection.DOWN -> // Handle scroll down
 *         else -> // Handle other directions
 *     }
 * }
 * recyclerView.setOnReachEdgeListener { edge, isReached ->
 *     if (edge == ScrollEdge.BOTTOM && isReached) {
 *         // Load more items
 *     }
 * }
 * ```
 * <br>
 *
 * @see ScrollDirection For scroll direction types.<br><br>
 *      스크롤 방향 타입은 ScrollDirection을 참조하세요.<br>
 *
 * @see ScrollEdge For edge position types.<br><br>
 *      가장자리 위치 타입은 ScrollEdge를 참조하세요.<br>
 *
 * @see RecyclerScrollStateCalculator For the scroll state calculation logic.<br><br>
 *      스크롤 상태 계산 로직은 RecyclerScrollStateCalculator를 참조하세요.<br>
 */
public open class RecyclerScrollStateView : RecyclerView {
    private companion object {
        /** Default threshold in pixels for edge reach detection. */
        private const val DEFAULT_EDGE_REACH_THRESHOLD = 10

        /** Default threshold in pixels for scroll direction change detection. */
        private const val DEFAULT_SCROLL_DIRECTION_THRESHOLD = 20
    }

    /**
     * Calculator object responsible for scroll state calculation logic.<br>
     * Accessible for testing purposes.<br><br>
     * 스크롤 상태 계산 로직을 담당하는 계산기 객체입니다.<br>
     * 테스트 목적으로 접근 가능합니다.<br>
     */
    @VisibleForTesting
    internal val scrollStateCalculator =
        RecyclerScrollStateCalculator(
            edgeReachThreshold = DEFAULT_EDGE_REACH_THRESHOLD,
            scrollDirectionThreshold = DEFAULT_SCROLL_DIRECTION_THRESHOLD,
        )

    /**
     * Listener for edge reach events using WeakReference to prevent memory leaks.<br><br>
     * 메모리 누수 방지를 위해 WeakReference를 사용하는 가장자리 도달 이벤트 리스너입니다.<br>
     */
    private var onEdgeReachedListener: WeakReference<OnEdgeReachedListener>? = null

    /**
     * Listener for scroll direction change events using WeakReference.<br><br>
     * WeakReference를 사용하는 스크롤 방향 변경 이벤트 리스너입니다.<br>
     */
    private var onScrollDirectionChangedListener: WeakReference<OnScrollDirectionChangedListener>? = null

    /**
     * MutableSharedFlow for scroll direction events.<br>
     * Replays the last emitted value to new collectors.<br><br>
     * 스크롤 방향 이벤트를 위한 MutableSharedFlow입니다.<br>
     * 새로운 컬렉터에게 마지막으로 발행된 값을 다시 전달합니다.<br>
     */
    private val msfScrollDirectionFlow =
        MutableSharedFlow<ScrollDirection>(
            replay = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )

    /**
     * Public SharedFlow for observing scroll direction changes.<br><br>
     * 스크롤 방향 변경을 관찰하기 위한 공개 SharedFlow입니다.<br>
     */
    public val sfScrollDirectionFlow: SharedFlow<ScrollDirection> = msfScrollDirectionFlow.asSharedFlow()

    /**
     * MutableSharedFlow for edge reach events.<br>
     * Emits Pair of ScrollEdge and whether the edge is reached.<br><br>
     * 가장자리 도달 이벤트를 위한 MutableSharedFlow입니다.<br>
     * ScrollEdge와 가장자리 도달 여부의 Pair를 발행합니다.<br>
     */
    private val msfEdgeReachedFlow =
        MutableSharedFlow<Pair<ScrollEdge, Boolean>>(
            replay = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )

    /**
     * Public SharedFlow for observing edge reach events.<br><br>
     * 가장자리 도달 이벤트를 관찰하기 위한 공개 SharedFlow입니다.<br>
     */
    public val sfEdgeReachedFlow: SharedFlow<Pair<ScrollEdge, Boolean>> = msfEdgeReachedFlow.asSharedFlow()

    public constructor(context: Context) : super(context)
    public constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initTypeArray(attrs)
    }
    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
        super(context, attrs, defStyleAttr) {
        initTypeArray(attrs)
    }

    /**
     * Initializes custom attributes from XML.<br><br>
     * XML에서 커스텀 속성을 초기화합니다.<br>
     *
     * @param attrs The attribute set from XML.<br><br>
     *              XML의 속성 집합.<br>
     */
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

    /**
     * Internal scroll listener for monitoring scroll events.<br><br>
     * 스크롤 이벤트를 모니터링하기 위한 내부 스크롤 리스너입니다.<br>
     */
    private val scrollListener =
        object : OnScrollListener() {
            override fun onScrollStateChanged(
                recyclerView: RecyclerView,
                newState: Int,
            ) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == SCROLL_STATE_IDLE) {
                    val result = scrollStateCalculator.resetScrollAccumulation()
                    if (result.directionChanged) {
                        notifyScrollDirectionChanged(result.newDirection)
                    }
                }
            }

            override fun onScrolled(
                recyclerView: RecyclerView,
                dx: Int,
                dy: Int,
            ) {
                super.onScrolled(recyclerView, dx, dy)
                checkEdgeReach()
                updateScrollDirection(dx, dy)
            }
        }

    /**
     * Notifies listeners of scroll direction changes.<br><br>
     * 스크롤 방향 변경을 리스너에게 알립니다.<br>
     *
     * @param direction The new scroll direction.<br><br>
     *                  새로운 스크롤 방향.<br>
     */
    private fun notifyScrollDirectionChanged(direction: ScrollDirection) {
        onScrollDirectionChangedListener?.get()?.onScrollDirectionChanged(direction)
        msfScrollDirectionFlow.safeEmit(direction) {
            Logx.w("Fail emit data $direction")
        }
    }

    /**
     * Checks for edge reach based on scroll orientation.<br><br>
     * 스크롤 방향에 따라 가장자리 도달을 확인합니다.<br>
     */
    private fun checkEdgeReach() {
        when {
            isScrollVertical() -> checkVerticalEdges()
            isScrollHorizontal() -> checkHorizontalEdges()
        }
    }

    /**
     * Checks top and bottom edge reach detection.<br><br>
     * 상단 및 하단 가장자리 도달을 감지합니다.<br>
     */
    private fun checkVerticalEdges() {
        val result =
            scrollStateCalculator.checkVerticalEdges(
                verticalScrollOffset = computeVerticalScrollOffset(),
                canScrollDown = canScrollVertically(1),
                verticalScrollExtent = computeVerticalScrollExtent(),
                verticalScrollRange = computeVerticalScrollRange(),
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
     * Checks left and right edge reach detection.<br><br>
     * 좌측 및 우측 가장자리 도달을 감지합니다.<br>
     */
    private fun checkHorizontalEdges() {
        val result =
            scrollStateCalculator.checkHorizontalEdges(
                horizontalScrollOffset = computeHorizontalScrollOffset(),
                canScrollRight = canScrollHorizontally(1),
                horizontalScrollExtent = computeHorizontalScrollExtent(),
                horizontalScrollRange = computeHorizontalScrollRange(),
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

    /**
     * Checks if the LayoutManager supports vertical scrolling.<br><br>
     * LayoutManager가 수직 스크롤을 지원하는지 확인합니다.<br>
     */
    private fun isScrollVertical() = layoutManager?.canScrollVertically() ?: false

    /**
     * Checks if the LayoutManager supports horizontal scrolling.<br><br>
     * LayoutManager가 수평 스크롤을 지원하는지 확인합니다.<br>
     */
    private fun isScrollHorizontal() = layoutManager?.canScrollHorizontally() ?: false

    /**
     * Updates scroll direction based on scroll deltas.<br><br>
     * 스크롤 델타값을 기반으로 스크롤 방향을 업데이트합니다.<br>
     *
     * @param dx Horizontal scroll delta.<br><br>
     *           수평 스크롤 델타.<br>
     *
     * @param dy Vertical scroll delta.<br><br>
     *           수직 스크롤 델타.<br>
     */
    private fun updateScrollDirection(
        dx: Int,
        dy: Int,
    ) {
        when {
            isScrollVertical() -> updateVerticalScrollDirection(dy)
            isScrollHorizontal() -> updateHorizontalScrollDirection(dx)
        }
    }

    /**
     * Updates vertical scroll direction.<br><br>
     * 수직 스크롤 방향을 업데이트합니다.<br>
     *
     * @param dy Vertical scroll delta.<br><br>
     *           수직 스크롤 델타.<br>
     */
    private fun updateVerticalScrollDirection(dy: Int) {
        val result = scrollStateCalculator.updateVerticalScrollDirection(dy)
        if (result.directionChanged) {
            notifyScrollDirectionChanged(result.newDirection)
        }
    }

    /**
     * Updates horizontal scroll direction.<br><br>
     * 수평 스크롤 방향을 업데이트합니다.<br>
     *
     * @param dx Horizontal scroll delta.<br><br>
     *           수평 스크롤 델타.<br>
     */
    private fun updateHorizontalScrollDirection(dx: Int) {
        val result = scrollStateCalculator.updateHorizontalScrollDirection(dx)
        if (result.directionChanged) {
            notifyScrollDirectionChanged(result.newDirection)
        }
    }

    /**
     * Called when the view is attached to a window.<br>
     * Registers the scroll listener.<br><br>
     * 뷰가 윈도우에 연결될 때 호출됩니다.<br>
     * 스크롤 리스너를 등록합니다.<br>
     */
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        addOnScrollListener(scrollListener)
    }

    /**
     * Called when the view is detached from a window.<br>
     * Unregisters the scroll listener.<br><br>
     * 뷰가 윈도우에서 분리될 때 호출됩니다.<br>
     * 스크롤 리스너를 해제합니다.<br>
     */
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeOnScrollListener(scrollListener)
    }

    /**
     * Sets a listener to be notified of scroll direction changes.<br>
     * This method allows you to register a callback that will be invoked
     * whenever the scroll direction of the `RecyclerScrollStateView` changes.<br>
     * The scroll direction is determined based on the accumulated scroll distance
     * and the configured [scrollDirectionThreshold].<br><br>
     * 스크롤 방향 변경 알림을 받을 리스너를 설정합니다.<br>
     * 이 메서드를 사용하면 `RecyclerScrollStateView`의 스크롤 방향이 변경될 때마다
     * 호출될 콜백을 등록할 수 있습니다.<br>
     * 스크롤 방향은 축적된 스크롤 거리와 구성된 [scrollDirectionThreshold]를 기반으로 결정됩니다.<br>
     *
     * The listener will receive a [ScrollDirection] value indicating the
     * current scroll direction:<br>
     * - [ScrollDirection.UP], [ScrollDirection.DOWN], [ScrollDirection.LEFT], [ScrollDirection.RIGHT], [ScrollDirection.IDLE]<br><br>
     * 리스너는 현재 스크롤 방향을 나타내는 [ScrollDirection] 값을 수신합니다:<br>
     * - [ScrollDirection.UP], [ScrollDirection.DOWN], [ScrollDirection.LEFT], [ScrollDirection.RIGHT], [ScrollDirection.IDLE]<br>
     *
     * @param listener The listener to be notified of scroll direction changes.<br><br>
     *                 스크롤 방향 변경 알림을 받을 리스너.<br>
     */
    public fun setOnScrollDirectionListener(listener: OnScrollDirectionChangedListener?) {
        this.onScrollDirectionChangedListener = listener?.let { WeakReference(it) }
    }

    /**
     * Sets a lambda listener to be notified of scroll direction changes.<br><br>
     * 스크롤 방향 변경 알림을 받을 람다 리스너를 설정합니다.<br>
     *
     * @param listener The lambda to be invoked on scroll direction changes.<br><br>
     *                 스크롤 방향 변경 시 호출될 람다.<br>
     */
    public fun setOnScrollDirectionListener(listener: (scrollDirection: ScrollDirection) -> Unit) {
        setOnScrollDirectionListener(
            object : OnScrollDirectionChangedListener {
                override fun onScrollDirectionChanged(scrollDirection: ScrollDirection) {
                    listener(scrollDirection)
                }
            },
        )
    }

    /**
     * Sets a listener to be notified when the RecyclerView reaches an edge.<br>
     * This method allows you to register a callback that will be invoked
     * whenever the `RecyclerScrollStateView` reaches one of its edges: top, bottom, left, or right.<br>
     * The edge reach detection is determined based on the configured [edgeReachThreshold].<br><br>
     * RecyclerView가 가장자리에 도달했을 때 알림을 받을 리스너를 설정합니다.<br>
     * 이 메서드를 사용하면 `RecyclerScrollStateView`가 상단, 하단, 좌측, 우측 가장자리 중
     * 하나에 도달할 때마다 호출될 콜백을 등록할 수 있습니다.<br>
     * 가장자리 도달 감지는 구성된 [edgeReachThreshold]를 기반으로 결정됩니다.<br>
     *
     * The listener will receive a [ScrollEdge] value indicating which edge was reached,
     * and a boolean value indicating whether the edge is currently reached (`true`) or not (`false`).<br><br>
     * 리스너는 어떤 가장자리에 도달했는지 나타내는 [ScrollEdge] 값과
     * 현재 가장자리에 도달했는지(`true`) 아닌지(`false`)를 나타내는 boolean 값을 수신합니다.<br>
     *
     * @param listener The listener to be notified of edge reach events.<br><br>
     *                 가장자리 도달 이벤트 알림을 받을 리스너.<br>
     */
    public fun setOnReachEdgeListener(listener: OnEdgeReachedListener?) {
        this.onEdgeReachedListener = listener?.let { WeakReference(it) }
    }

    /**
     * Sets a lambda listener to be notified when the RecyclerView reaches an edge.<br><br>
     * RecyclerView가 가장자리에 도달했을 때 알림을 받을 람다 리스너를 설정합니다.<br>
     *
     * @param listener The lambda to be invoked on edge reach events.<br><br>
     *                 가장자리 도달 이벤트 시 호출될 람다.<br>
     */
    public fun setOnReachEdgeListener(listener: (edge: ScrollEdge, isReached: Boolean) -> Unit) {
        setOnReachEdgeListener(
            object : OnEdgeReachedListener {
                override fun onEdgeReached(
                    edge: ScrollEdge,
                    isReached: Boolean,
                ) {
                    listener(edge, isReached)
                }
            },
        )
    }

    /**
     * Sets the minimum scroll movement range required to trigger a scroll direction change event.<br>
     * This method allows you to define the minimum distance (in pixels) that the
     * `RecyclerScrollStateView` must be scrolled in a particular direction
     * before a scroll direction change event is triggered.<br>
     * By default, the minimum scroll movement range is set to 20 pixels.<br>
     * You can adjust this value to control the sensitivity of scroll direction
     * detection. A higher value will make the detection less sensitive, while a
     * lower value will make it more sensitive.<br><br>
     * 스크롤 방향 변경 이벤트를 트리거하는 데 필요한 최소 스크롤 이동 범위(px)를 설정합니다.<br>
     * 이 메서드를 사용하면 스크롤 방향 변경 이벤트가 트리거되기 전에
     * `RecyclerScrollStateView`가 특정 방향으로 스크롤되어야 하는 최소 거리(픽셀 단위)를 정의할 수 있습니다.<br>
     * 기본값으로 최소 스크롤 이동 범위는 20픽셀로 설정됩니다.<br>
     * 이 값을 조정하여 스크롤 방향 감지의 민감도를 제어할 수 있습니다.
     * 값이 높을수록 감지가 덜 민감해지고, 낮을수록 더 민감해집니다.<br>
     *
     * @param minimumScrollMovementRange The minimum scroll movement range in pixels.
     *                                   This value should be >= 0.<br><br>
     *                                   최소 스크롤 이동 범위(픽셀 단위).
     *                                   이 값은 0 이상이어야 합니다.<br>
     */
    public fun setScrollDirectionThreshold(minimumScrollMovementRange: Int) {
        require(minimumScrollMovementRange >= 0) {
            "minimumScrollMovementRange must be >= 0, but input value is $minimumScrollMovementRange"
        }
        scrollStateCalculator.updateThresholds(scrollDirectionThreshold = minimumScrollMovementRange)
    }

    /**
     * Sets the threshold distance (in pixels) from an edge that is considered as reaching the edge.<br>
     * This method allows you to define the distance from an edge (top, bottom, left, or right)
     * within which the `RecyclerScrollStateView` is considered to have reached that edge.<br>
     * By default, the edge reach threshold is set to 10 pixels.<br>
     * You can adjust this value to control the sensitivity of edge reach detection.
     * A higher value will make the detection less sensitive, while a lower value will
     * make it more sensitive.<br><br>
     * 가장자리에 도달한 것으로 간주되는 가장자리로부터의 임계 거리(픽셀 단위)를 설정합니다.<br>
     * 이 메서드를 사용하면 `RecyclerScrollStateView`가 해당 가장자리에 도달한 것으로 간주되는
     * 가장자리(상단, 하단, 좌측 또는 우측)로부터의 거리를 정의할 수 있습니다.<br>
     * 기본값으로 가장자리 도달 임계값은 10픽셀로 설정됩니다.<br>
     * 이 값을 조정하여 가장자리 도달 감지의 민감도를 제어할 수 있습니다.
     * 값이 높을수록 감지가 덜 민감해지고, 낮을수록 더 민감해집니다.<br>
     *
     * @param edgeReachThreshold The edge reach threshold distance in pixels.
     *                           This value should be >= 0.<br><br>
     *                           가장자리 도달 임계 거리(픽셀 단위).
     *                           이 값은 0 이상이어야 합니다.<br>
     */
    public fun setEdgeReachThreshold(edgeReachThreshold: Int) {
        require(edgeReachThreshold >= 0) {
            "edgeReachThreshold must be >= 0, but input value is $edgeReachThreshold"
        }
        scrollStateCalculator.updateThresholds(edgeReachThreshold = edgeReachThreshold)
    }
}
