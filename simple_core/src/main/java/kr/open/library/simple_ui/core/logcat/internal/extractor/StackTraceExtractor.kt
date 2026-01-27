package kr.open.library.simple_ui.core.logcat.internal.extractor

/**
 * 현재 스레드의 스택 트레이스를 분석해 로그용 프레임을 추출합니다.
 *
 * Extracts log-friendly stack frames from the current thread's stack trace.
 * <br><br>
 * 현재 스레드의 스택을 분석하여 현재/부모 프레임을 추출합니다.
 *
 * @param skipPackages 로그 프레임 탐색에서 제외할 패키지 접두사 목록.
 */
internal class StackTraceExtractor(private val skipPackages: Set<String>) {
    /**
     * 탐색 시작 인덱스를 결정하는 헬퍼입니다.
     *
     * Resolves the start index for stack trace scanning.
     * <br><br>
     * 스택 탐색 시작 지점을 계산합니다.
     */
    private val startResolver = StackTraceStartResolver()

    /**
     * 스택 프레임 필터링 규칙 모음입니다.
     *
     * Frame filter rules for skipping internal frames.
     * <br><br>
     * 내부 프레임을 건너뛰기 위한 필터입니다.
     */
    private val frameFilter = StackTraceFrameFilter(skipPackages)

    /**
     * 현재/부모 프레임을 추출해 반환합니다.
     *
     * Extracts and returns current and parent frames.
     * <br><br>
     * 현재/부모 프레임을 찾아 반환합니다.
     */
    fun extract(): StackFrames {
        val stackTrace = Thread.currentThread().stackTrace
        if (stackTrace.isEmpty()) {
            return StackFrames(createFallbackFrame(), null)
        }

        val lastIndex = stackTrace.lastIndex
        val searchStart = startResolver.resolve(stackTrace)

        var currentElement: StackTraceElement? = null
        var parentElement: StackTraceElement? = null

        for (i in searchStart..lastIndex) {
            val element = stackTrace[i]
            if (frameFilter.isSkipped(element) || frameFilter.isSynthetic(element)) continue
            if (currentElement == null) {
                currentElement = element
            } else {
                parentElement = element
                break
            }
        }

        val fallbackElement = currentElement ?: stackTrace.getOrNull(searchStart) ?: stackTrace.firstOrNull()
        val currentFrame = toFrame(fallbackElement) ?: createFallbackFrame()
        val parentFrame = toFrame(parentElement)

        return StackFrames(currentFrame, parentFrame)
    }

    /**
     * 스택 트레이스 요소를 StackFrame으로 변환합니다.
     *
     * Converts a stack trace element into a StackFrame.
     * <br><br>
     * 스택 요소를 로그 프레임 모델로 변환합니다.
     *
     * @param element 변환 대상 스택 요소.
     */
    private fun toFrame(element: StackTraceElement?): StackFrame? {
        if (element == null) return null
        return StackFrame(
            fileName = element.fileName ?: "Unknown",
            lineNumber = element.lineNumber,
            methodName = element.methodName,
            className = element.className,
        )
    }

    /**
     * 프레임을 찾지 못했을 때 사용할 기본 프레임입니다.
     *
     * Returns a fallback frame used when none could be extracted.
     * <br><br>
     * 추출 실패 시 사용할 기본 프레임을 반환합니다.
     */
    private fun createFallbackFrame(): StackFrame = StackFrame(
        fileName = "Unknown",
        lineNumber = 0,
        methodName = "unknown",
        className = "unknown",
    )
}

