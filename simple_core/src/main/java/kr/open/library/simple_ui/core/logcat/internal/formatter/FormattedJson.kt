package kr.open.library.simple_ui.core.logcat.internal.formatter

/**
 * JSON 로그 출력을 위한 구성 요소 묶음입니다.
 *
 * Container for formatted JSON log output components.
 * <br><br>
 * JSON 로그 출력을 위한 헤더/본문/종료 라인을 보관합니다.
 *
 * @property header JSON 시작 헤더 라인.
 * @property bodyLines JSON 본문 라인 목록.
 * @property endLine JSON 종료 라인.
 */
internal data class FormattedJson(
    val header: String,
    val bodyLines: List<String>,
    val endLine: String = "[End]",
)
