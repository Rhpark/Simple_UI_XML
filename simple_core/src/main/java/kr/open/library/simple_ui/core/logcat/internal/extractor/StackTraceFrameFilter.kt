package kr.open.library.simple_ui.core.logcat.internal.extractor

/**
 * 스택 트레이스에서 건너뛸 프레임을 판정하는 필터입니다.
 *
 * Filters stack trace elements that should be skipped.
 * <br><br>
 * 내부 프레임을 제외하기 위한 필터 로직입니다.
 *
 * @param skipPackages 제외할 패키지 접두사 집합.
 */
internal class StackTraceFrameFilter(
    private val skipPackages: Set<String>,
) {
    /**
     * 스킵 패키지 규칙에 해당하는지 확인합니다.
     *
     * Returns true if the element belongs to a skipped package.
     * <br><br>
     * 제외 대상 패키지인지 여부를 반환합니다.
     *
     * @param element 검사 대상 스택 요소.
     */
    fun isSkipped(element: StackTraceElement): Boolean =
        skipPackages.any { prefix -> element.className.startsWith(prefix) }

    /**
     * 컴파일러/런타임이 생성한 합성 프레임인지 판단합니다.
     *
     * Determines whether a frame is synthetic or unusable.
     * <br><br>
     * 합성 프레임 또는 유효하지 않은 프레임인지 확인합니다.
     *
     * @param element 검사 대상 스택 요소.
     */
    fun isSynthetic(element: StackTraceElement): Boolean {
        if (element.fileName.isNullOrBlank() || element.fileName == "Unknown") return true
        if (element.lineNumber <= 0) return true

        val className = element.className
        val methodName = element.methodName

        if (className.contains("D8\$\$SyntheticClass")) return true
        if (methodName.contains("access\$")) return true

        return false
    }
}

