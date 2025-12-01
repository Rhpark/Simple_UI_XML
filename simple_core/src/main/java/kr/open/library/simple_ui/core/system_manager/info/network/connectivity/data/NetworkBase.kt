package kr.open.library.simple_ui.core.system_manager.info.network.connectivity.data

/**
 * Base class for network data wrapper classes.<br><br>
 * 네트워크 데이터 래퍼 클래스의 기본 클래스로, 공통 유틸리티를 제공합니다.<br>
 *
 * Provides utility functions for string manipulation and parsing.<br><br>
 * 문자열 조작과 파싱을 위한 유틸리티 함수를 포함합니다.<br>
 *
 * @param res The source object to be wrapped and stringified.<br><br>
 *            래핑하고 문자열로 변환할 원본 객체입니다.<br>
 */
public open class NetworkBase(
    res: Any,
) {
    private val resStr = res.toString()

    /**
     * Splits the string representation of the source object.<br><br>
     * 원본 객체의 문자열 표현을 구분자로 분리합니다.<br>
     *
     * @param start Start delimiter.<br><br>
     *              시작 구분자입니다.<br>
     * @param end End delimiter.<br><br>
     *            종료 구분자입니다.<br>
     * @param splitPoint Secondary split delimiter.<br><br>
     *                   2차 분할에 사용할 구분자입니다.<br>
     * @return List of split strings, or `null` if delimiters are missing.<br><br>
     *         분리된 문자열 리스트이며, 구분자를 찾지 못하면 `null`입니다.<br>
     */
    protected fun splitStr(
        start: String,
        end: String,
        splitPoint: String,
    ): List<String>? = resStr.split(start, end)?.split(splitPoint)

    /**
     * Splits the string representation of the source object.<br><br>
     * 원본 객체의 문자열 표현을 구분자로 분리합니다.<br>
     *
     * @param start Start delimiter.<br><br>
     *              시작 구분자입니다.<br>
     * @param end End delimiter.<br><br>
     *            종료 구분자입니다.<br>
     * @return Split string, or `null` if delimiters are missing.<br><br>
     *         분리된 문자열이며, 구분자를 찾지 못하면 `null`입니다.<br>
     */
    protected fun splitStr(
        start: String,
        end: String,
    ): String? = resStr.split(start, end)

    /**
     * Extension function to split a string between two delimiters.<br><br>
     * 두 구분자 사이의 문자열을 추출하는 확장 함수입니다.<br>
     *
     * @param start Start delimiter.<br><br>
     *              시작 구분자입니다.<br>
     * @param end End delimiter.<br><br>
     *            종료 구분자입니다.<br>
     * @return Extracted string, or `null` if delimiters are missing.<br><br>
     *         추출된 문자열이며, 구분자를 찾지 못하면 `null`입니다.<br>
     */
    protected fun String.split(
        start: String,
        end: String,
    ): String? =
        if (contains(start)) {
            val splitRes = split(start)
            // contains(start) == true이면 splitRes.size() >= 2
            val afterStart = splitRes[1]
            if (afterStart.contains(end)) {
                afterStart.split(end)[0]
            } else {
                null
            }
        } else {
            null
        }

    /**
     * Checks if the string representation contains the specified string.<br><br>
     * 문자열 표현에 지정한 문자열이 포함되어 있는지 확인합니다.<br>
     *
     * @param str String to search for.<br><br>
     *            검색할 문자열입니다.<br>
     * @return `true` if found; `false` otherwise.<br><br>
     *         포함되어 있으면 `true`, 아니면 `false`입니다.<br>
     */
    protected fun isContains(str: String): Boolean = resStr.contains(str)

    /**
     * Gets the string representation of the source object.<br><br>
     * 원본 객체의 문자열 표현을 반환합니다.<br>
     *
     * @return String representation of the source object.<br><br>
     *         원본 객체의 문자열 표현입니다.<br>
     */
    protected fun getResStr(): String = resStr
}
