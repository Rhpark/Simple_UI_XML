package kr.open.library.simple_ui.core.logcat.internal.common

/**
 * Helper utilities for log tag and prefix handling.<br><br>
 * 로그 태그와 프리픽스 처리용 유틸리티이다.<br>
 */
internal object LogxTagHelper {
    /**
     * Validates that a tag is not null or blank.<br><br>
     * 태그가 null/공백인지 여부를 검사한다.<br>
     *
     * @param tag Tag string to validate.<br><br>
     *            검사할 태그 문자열.<br>
     */
    fun isValidTag(tag: String?): Boolean = !tag.isNullOrBlank()

    /**
     * Builds log prefix using app name and optional tag.<br><br>
     * 앱 이름과 태그로 로그 프리픽스를 만든다.<br>
     *
     * @param appName Application name.<br><br>
     *                앱 이름.<br>
     * @param tag Optional tag string.<br><br>
     *            선택 태그 문자열.<br>
     */
    fun buildPrefix(appName: String, tag: String?): String = if (isValidTag(tag)) "$appName[$tag]" else appName

    /**
     * Returns tag to use for internal error logs.<br><br>
     * 내부 에러 로그에 사용할 태그를 반환한다.<br>
     *
     * @param inputTag Input tag from caller.<br><br>
     *                 호출자가 입력한 태그.<br>
     */
    fun errorTag(inputTag: String?): String = if (isValidTag(inputTag)) {
        inputTag ?: LogxConstants.ERROR_TAG
    } else {
        LogxConstants.ERROR_TAG
    }
}
