package kr.open.library.simple_ui.core.logcat.internal.formatter

import kr.open.library.simple_ui.core.logcat.config.LogType
import kr.open.library.simple_ui.core.logcat.internal.common.LogxTimeUtils

/**
 * 파일 로그 라인 포맷을 생성하는 빌더입니다.
 *
 * Builds formatted lines for file logging.
 * <br><br>
 * 파일 기록용 라인 포맷을 생성합니다.
 */
internal class LogxFileLineBuilder {
    /**
     * 일반 로그 라인을 생성합니다.
     *
     * Builds file log lines for a given payload list.
     * <br><br>
     * 일반 로그 payload 목록을 파일 라인으로 변환합니다.
     *
     * @param type 로그 타입.
     * @param prefix 프레임/태그 등의 접두 정보.
     * @param payloadLines 출력할 본문 라인 목록.
     */
    fun buildLines(type: LogType, prefix: String, payloadLines: List<String>): List<String> {
        val timestamp = LogxTimeUtils.nowTimestamp()
        return payloadLines.map { payload ->
            buildLine(timestamp, type, prefix, payload)
        }
    }

    /**
     * JSON 로그 라인을 생성합니다.
     *
     * Builds file log lines for formatted JSON output.
     * <br><br>
     * JSON 포맷 출력에 맞춰 파일 라인 목록을 생성합니다.
     *
     * @param prefix 프레임/태그 등의 접두 정보.
     * @param formattedJson JSON 출력 구성 요소.
     */
    fun buildJsonLines(prefix: String, formattedJson: FormattedJson): List<String> {
        val timestamp = LogxTimeUtils.nowTimestamp()
        val firstLine = buildLine(timestamp, LogType.JSON, prefix, formattedJson.header)
        return buildList {
            add(firstLine)
            addAll(formattedJson.bodyLines)
            add(formattedJson.endLine)
        }
    }

    /**
     * 단일 파일 로그 라인을 조합합니다.
     *
     * Builds a single formatted file log line.
     * <br><br>
     * 단일 로그 라인을 조합합니다.
     *
     * @param timestamp 로그 시각 문자열.
     * @param type 로그 타입.
     * @param prefix 프레임/태그 등의 접두 정보.
     * @param payload 출력 payload.
     */
    private fun buildLine(timestamp: String, type: LogType, prefix: String, payload: String): String =
        "$timestamp [${type.outputChar}] $prefix : $payload"
}
