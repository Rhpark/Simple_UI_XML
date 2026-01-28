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
internal class LogStackTraceExtractor(
    skipPackages: Set<String>,
) {
    private var skipPackages: Set<String> = skipPackages

    /**
     * 탐색 시작 인덱스를 결정하는 헬퍼입니다.
     *
     * Resolves the start index for stack trace scanning.
     * <br><br>
     * 스택 탐색 시작 지점을 계산합니다.
     */
    private var startResolver: LogStackTraceStartResolver = createStartResolver(skipPackages)

    /**
     * 스택 프레임 필터링 규칙 모음입니다.
     *
     * Frame filter rules for skipping internal frames.
     * <br><br>
     * 내부 프레임을 건너뛰기 위한 필터입니다.
     */
    private var frameFilter: LogStackTraceFrameFilter = LogStackTraceFrameFilter(skipPackages)

    /**
     * 스킵 패키지 목록을 갱신합니다.<br><br>
     * skipPackages를 업데이트하고 내부 헬퍼를 재구성합니다.<br>
     *
     * @param newSkipPackages 새 스킵 패키지 목록.<br><br>
     *                        새 스킵 패키지 목록.<br>
     */
    fun updateSkipPackages(newSkipPackages: Set<String>) {
        if (skipPackages == newSkipPackages) return
        skipPackages = newSkipPackages
        startResolver = createStartResolver(newSkipPackages)
        frameFilter = LogStackTraceFrameFilter(newSkipPackages)
    }

    /**
     * 현재/부모 프레임을 추출해 반환합니다.
     *
     * Extracts and returns current and parent frames.
     * <br><br>
     * 현재/부모 프레임을 찾아 반환합니다.
     */
    fun extract(): LogStackFrames {
        val stackTrace = Thread.currentThread().stackTrace
        if (stackTrace.isEmpty()) {
            return LogStackFrames(createFallbackFrame(), null)
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

    companion object {
        private val extractorPool = object : ThreadLocal<LogStackTraceExtractor>() {
            override fun initialValue(): LogStackTraceExtractor = LogStackTraceExtractor(emptySet())
        }

        /**
         * ThreadLocal 기반으로 StackTraceExtractor를 재사용합니다.<br><br>
         * 스레드별 추출기를 재사용해 불필요한 객체 생성을 줄입니다.<br>
         *
         * @param skipPackages 스킵 패키지 목록.<br><br>
         *                     스킵 패키지 목록.<br>
         */
        internal fun extract(skipPackages: Set<String>): LogStackFrames {
            val extractor = extractorPool.get()!!
            extractor.updateSkipPackages(skipPackages)
            return extractor.extract()
        }
    }
}
