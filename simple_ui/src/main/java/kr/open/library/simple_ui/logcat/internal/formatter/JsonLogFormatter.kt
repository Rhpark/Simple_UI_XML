package kr.open.library.simple_ui.logcat.internal.formatter

import kr.open.library.simple_ui.logcat.config.LogxConfig
import kr.open.library.simple_ui.logcat.internal.formatter.base.LogxBaseFormatter
import kr.open.library.simple_ui.logcat.internal.formatter.base.LogxFormatterImp
import kr.open.library.simple_ui.logcat.model.LogxType


/**
 * logcat JSON 전용 포맷터 부분 설정 및 반환
 */
class JsonLogFormatter(config: LogxConfig) :
    LogxBaseFormatter(config), LogxFormatterImp {

    override fun isIncludeLogType(logType: LogxType): Boolean = logType == LogxType.JSON

    override fun getTagSuffix(): String = "[JSON]"

    /**
     * JSON 문자열을 보기 좋게 포맷팅
     */
    override fun formatMessage(message: Any?, stackInfo: String): String = formatJsonMessage(message.toString())
    

    private fun formatJsonMessage(jsonString: String): String {
        val trimmed = jsonString.trim()

        // JSON이 아닌 경우 그대로 반환
        if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) {
            return trimmed
        }

        // 개선된 JSON 포맷팅
        return try {
            formatJsonPretty(trimmed)
        } catch (e: Exception) {
            // 포맷팅 실패 시 원본 반환
            trimmed
        }
    }

    /**
     * 개선된 JSON 포맷팅 (간단하면서도 깔끔하게)
     */
    private fun formatJsonPretty(jsonString: String): String {
        val result = StringBuilder()
        var indentLevel = 0
        var inQuotes = false
        var prevChar = ' '

        for (i in jsonString.indices) {
            val char = jsonString[i]

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
                    result.append("  ".repeat(indentLevel))
                }
                char == '}' || char == ']' -> {
                    result.append('\n')
                    indentLevel = maxOf(0, indentLevel - 1)
                    result.append("  ".repeat(indentLevel))
                    result.append(char)
                }
                char == ',' -> {
                    result.append(char)
                    result.append('\n')
                    result.append("  ".repeat(indentLevel))
                }
                char == ':' -> {
                    result.append(char)
                    result.append(' ')
                }
                char.isWhitespace() && result.lastOrNull()?.isWhitespace() == true -> {
                    // 연속된 공백 제거
                }
                else -> {
                    result.append(char)
                }
            }
            prevChar = char
        }

        return result.toString().trim()
//
//        val result = StringBuilder()
//        var indentLevel = 0
//        var inQuotes = false
//
//        for (char in jsonString) {
//            when (char) {
//                '{', '[' -> {
//                    result.append(char)
//                    if (!inQuotes) {
//                        result.append("\n")
//                        indentLevel++
//                        result.append("  ".repeat(indentLevel))
//                    }
//                }
//                '}', ']' -> {
//                    if (!inQuotes) {
//                        result.append("\n")
//                        indentLevel = maxOf(0, indentLevel - 1)
//                        result.append("  ".repeat(indentLevel))
//                    }
//                    result.append(char)
//                }
//                ',' -> {
//                    result.append(char)
//                    if (!inQuotes) {
//                        result.append("\n")
//                        result.append("  ".repeat(indentLevel))
//                    }
//                }
//                '"' -> {
//                    result.append(char)
//                    if (result.lastOrNull() != '\\') {
//                        inQuotes = !inQuotes
//                    }
//                }
//                else -> result.append(char)
//            }
//        }
//
//        return result.toString()
    }
}