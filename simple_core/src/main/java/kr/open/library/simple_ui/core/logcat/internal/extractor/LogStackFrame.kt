package kr.open.library.simple_ui.core.logcat.internal.extractor

/**
 * 로그 표기에 사용되는 스택 프레임 요약 모델입니다.
 *
 * A compact stack frame model used for log rendering.
 * <br><br>
 * 로그 표기를 위해 가공된 스택 프레임 정보를 담습니다.
 *
 * @property fileName 파일명(확장자 포함).
 * @property lineNumber 라인 번호(1-base).
 * @property methodName 메서드명.
 * @property className 클래스 전체 이름.
 */
internal data class LogStackFrame(
    val fileName: String,
    val lineNumber: Int,
    val methodName: String,
    val className: String,
)
