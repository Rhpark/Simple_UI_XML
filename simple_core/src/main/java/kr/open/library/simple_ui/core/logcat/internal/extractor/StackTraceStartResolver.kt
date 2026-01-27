package kr.open.library.simple_ui.core.logcat.internal.extractor

/**
 * 스택 트레이스 탐색 시작 지점을 계산하는 헬퍼입니다.
 *
 * Resolves the starting index for stack trace scanning.
 * <br><br>
 * 내부 프레임 이후부터 탐색하도록 시작 인덱스를 계산합니다.
 *
 * @param internalPrefixes 내부 프레임으로 간주할 패키지 접두사 목록.
 * @param fallbackStartIndex 내부 프레임을 찾지 못했을 때 사용할 기본 시작 인덱스.
 */
internal class StackTraceStartResolver(
    private val internalPrefixes: List<String> = listOf(
        "kr.open.library.simple_ui.core.temp_logcat",
    ),
    private val fallbackStartIndex: Int = 4,
) {
    /**
     * 스택 트레이스 배열에서 탐색 시작 인덱스를 계산합니다.
     *
     * Computes the start index for scanning the stack trace.
     * <br><br>
     * 스택 탐색 시작 위치를 반환합니다.
     *
     * @param stackTrace 현재 스레드의 스택 트레이스 배열.
     */
    fun resolve(stackTrace: Array<StackTraceElement>): Int {
        if (stackTrace.isEmpty()) return 0

        val lastIndex = stackTrace.lastIndex
        val internalEnd = findInternalEndIndex(stackTrace)
        return (internalEnd + 1)
            .coerceAtLeast(fallbackStartIndex)
            .coerceAtMost(lastIndex)
    }

    /**
     * 내부 패키지 프레임의 마지막 인덱스를 찾습니다.
     *
     * Finds the last index of frames that belong to internal prefixes.
     * <br><br>
     * 내부 패키지에 해당하는 마지막 인덱스를 반환합니다.
     *
     * @param stackTrace 현재 스레드의 스택 트레이스 배열.
     */
    private fun findInternalEndIndex(stackTrace: Array<StackTraceElement>): Int {
        var last = -1
        for (i in 0..stackTrace.lastIndex) {
            val className = stackTrace[i].className
            if (internalPrefixes.any { prefix -> className.startsWith(prefix) }) {
                last = i
            }
        }
        return last
    }
}

