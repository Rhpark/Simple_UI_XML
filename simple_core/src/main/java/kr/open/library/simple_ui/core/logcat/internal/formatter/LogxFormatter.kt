package kr.open.library.simple_ui.core.logcat.internal.formatter

import kr.open.library.simple_ui.core.logcat.internal.extractor.StackFrame
import kr.open.library.simple_ui.core.logcat.internal.extractor.StackFrames
import kr.open.library.simple_ui.core.logcat.internal.common.LogxConstants

/**
 * 로그 메시지 포맷을 생성하는 내부 포맷터입니다.
 *
 * Internal formatter that builds log output strings.
 * <br><br>
 * 로그 출력 문자열을 조합하는 내부 포맷터입니다.
 */
internal object LogxFormatter {
    /**
     * 기본 로그 포맷 문자열을 생성합니다.
     *
     * Builds a basic log output string.
     * <br><br>
     * 기본 로그 출력 문자열을 생성합니다.
     *
     * @param frame 현재 프레임 정보.
     * @param msg 출력 메시지(없을 수 있음).
     * @param hasMessage 메시지 포함 여부.
     */
    fun formatBasic(frame: StackFrame, msg: String?, hasMessage: Boolean): String =
        buildString {
            append(formatMeta(frame))
            if (hasMessage) {
                append(" - ")
                append(msg ?: "null")
            }
        }

    /**
     * PARENT 로그 포맷 문자열 목록을 생성합니다.
     *
     * Builds the PARENT log output lines.
     * <br><br>
     * 부모/현재 프레임을 포함한 PARENT 출력 라인을 생성합니다.
     *
     * @param frames 현재/부모 프레임 묶음.
     * @param msg 출력 메시지(없을 수 있음).
     * @param hasMessage 메시지 포함 여부.
     */
    fun formatParent(frames: StackFrames, msg: String?, hasMessage: Boolean): List<String> {
        val parentPayload = if (frames.parent != null) {
            "┌[PARENT] ${formatMeta(frames.parent)}"
        } else {
            "┌[PARENT]"
        }

        val currentPayload = buildString {
            append("└[PARENT] ")
            append(formatMeta(frames.current))
            if (hasMessage) {
                append(" - ")
                append(msg ?: "null")
            }
        }

        return listOf(parentPayload, currentPayload)
    }

    /**
     * 스레드 ID가 포함된 로그 포맷 문자열을 생성합니다.
     *
     * Builds a log output string that includes thread id.
     * <br><br>
     * 스레드 ID 정보를 포함한 로그 문자열을 생성합니다.
     *
     * @param frame 현재 프레임 정보.
     * @param threadId 현재 스레드 ID.
     * @param msg 출력 메시지(없을 수 있음).
     * @param hasMessage 메시지 포함 여부.
     */
    fun formatThread(frame: StackFrame, threadId: Long, msg: String?, hasMessage: Boolean): String =
        buildString {
            append("[TID = ")
            append(threadId)
            append("]")
            append(formatMeta(frame))
            if (hasMessage) {
                append(" - ")
                append(msg ?: "null")
            }
        }

    /**
     * JSON 로그 출력 구성 요소를 생성합니다.
     *
     * Builds a formatted JSON output container.
     * <br><br>
     * JSON 로그 출력 구성을 생성합니다.
     *
     * @param frame 현재 프레임 정보.
     * @param json JSON 원문 문자열.
     */
    fun formatJson(frame: StackFrame, json: String): FormattedJson {
        val meta = formatMeta(frame)
        val header = "[JSON]$meta -"
        val bodyLines = formatJsonBody(json)
        return FormattedJson(header = header, bodyLines = bodyLines)
    }

    /**
     * 파일/라인/메서드 정보를 포함한 메타 문자열을 생성합니다.
     *
     * Builds a meta string containing file, line, and method info.
     * <br><br>
     * 파일명/라인/메서드를 포함한 메타 문자열을 생성합니다.
     *
     * @param frame 현재 프레임 정보.
     */
    private fun formatMeta(frame: StackFrame): String {
//        val fileName = normalizeFileName(frame.fileName)
        val fileName = frame.fileName
        val lineNumber = if (frame.lineNumber > 0) frame.lineNumber else 0
        return "(${fileName}:${lineNumber}).${frame.methodName}"
    }

    /**
     * 파일 확장자를 제거한 이름으로 정규화합니다.
     *
     * Normalizes a file name by removing its extension.
     * <br><br>
     * 파일명에서 확장자를 제거해 정규화합니다.
     *
     * @param fileName 파일명(확장자 포함).
     */
    private fun normalizeFileName(fileName: String): String {
        val dotIndex = fileName.lastIndexOf('.')
        return if (dotIndex > 0) fileName.substring(0, dotIndex) else fileName
    }

    /**
     * JSON 본문 라인을 생성합니다.
     *
     * Builds the JSON body lines for output.
     * <br><br>
     * JSON 본문을 라인 단위로 분리합니다.
     *
     * @param json JSON 원문 문자열.
     */
    private fun formatJsonBody(json: String): List<String> {
        val trimmed = json.trim()
        if (trimmed.isEmpty()) return listOf(json)

        val formatted = try {
            if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
                formatJsonPretty(trimmed)
            } else {
                trimmed
            }
        } catch (_: Exception) {
            json
        }

        return formatted.split("\n")
    }

    /**
     * JSON 문자열을 보기 좋게 들여쓰기합니다.
     *
     * Pretty-prints a JSON string using the configured indent size.
     * <br><br>
     * 설정된 들여쓰기 크기로 JSON을 pretty-print 합니다.
     *
     * @param jsonString JSON 문자열.
     */
    private fun formatJsonPretty(jsonString: String): String {
        val result = StringBuilder()
        val indentUnit = " ".repeat(LogxConstants.jsonIndent)
        var indentLevel = 0
        var inQuotes = false
        var prevChar = ' '

        for (char in jsonString) {
            when {
                char == '"' && prevChar != '\\' -> {
                    inQuotes = !inQuotes
                    result.append(char)
                }
                inQuotes -> {
                    result.append(char)
                }
                char == '{' || char == '[' -> {
                    result.append(char)
                    result.append('\n')
                    indentLevel++
                    result.append(indentUnit.repeat(indentLevel))
                }
                char == '}' || char == ']' -> {
                    result.append('\n')
                    indentLevel = maxOf(0, indentLevel - 1)
                    result.append(indentUnit.repeat(indentLevel))
                    result.append(char)
                }
                char == ',' -> {
                    result.append(char)
                    result.append('\n')
                    result.append(indentUnit.repeat(indentLevel))
                }
                char == ':' -> {
                    result.append(char)
                    result.append(' ')
                }
                char.isWhitespace() && result.lastOrNull()?.isWhitespace() == true -> {
                    // 연속 공백 제거
                }
                else -> {
                    result.append(char)
                }
            }
            prevChar = char
        }

        return result.toString().trim()
    }
}
