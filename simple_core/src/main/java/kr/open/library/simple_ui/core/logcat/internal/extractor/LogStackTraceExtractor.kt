package kr.open.library.simple_ui.core.logcat.internal.extractor

/**
 * 현재 스레드의 스택 트레이스를 분석해 로그용 프레임을 추출합니다.
 *
 * Extracts log-friendly stack frames from the current thread's stack trace.
 * <br><br>
 * 현재 스레드의 스택을 분석하여 현재/부모 프레임을 추출합니다.
 */
internal object LogStackTraceExtractor {
    /**
     * skipPackages 기준으로 현재/부모 프레임을 추출해 반환합니다.
     *
     * Extracts and returns current and parent frames using the given skip packages.
     * <br><br>
     * 지정된 스킵 패키지를 기준으로 현재/부모 프레임을 찾아 반환합니다.
     *
     * @param skipPackages 로그 프레임 탐색에서 제외할 패키지 접두사 목록.
     */
    internal fun extract(skipPackages: Set<String>): LogStackFrames {
        val stackTrace = Thread.currentThread().stackTrace
        if (stackTrace.isEmpty()) {
            return LogStackFrames(createFallbackFrame(), null)
        }

        val startResolver = createStartResolver(skipPackages)
        val frameFilter = LogStackTraceFrameFilter(skipPackages)

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

        return LogStackFrames(currentFrame, parentFrame)
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
    private fun toFrame(element: StackTraceElement?): LogStackFrame? {
        if (element == null) return null
        return LogStackFrame(
            fileName = element.fileName ?: LogStackTraceConstants.UNKNOWN_FILE_NAME,
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
    private fun createFallbackFrame(): LogStackFrame = LogStackFrame(
        fileName = LogStackTraceConstants.UNKNOWN_FILE_NAME,
        lineNumber = LogStackTraceConstants.FALLBACK_LINE_NUMBER,
        methodName = LogStackTraceConstants.UNKNOWN_METHOD,
        className = LogStackTraceConstants.UNKNOWN_CLASS,
    )

    private fun isCustomStartPrefix(prefix: String): Boolean {
        if (prefix.isBlank()) return false
        return LogStackTraceConstants.START_PREFIX_EXCLUDES.none { excluded -> prefix.startsWith(excluded) }
    }

    private fun createStartResolver(packages: Set<String>): LogStackTraceStartResolver =
        LogStackTraceStartResolver(
            additionalPrefixes = packages.filter { isCustomStartPrefix(it) }.toSet(),
        )
}
