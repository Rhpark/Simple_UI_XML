package kr.open.library.simple_ui.core.logcat.extension

import kr.open.library.simple_ui.core.logcat.Logx

/**
 * Logs this value as VERBOSE.<br><br>
 * 이 값을 VERBOSE로 출력한다.<br>
 */
fun Any.logv() = Logx.v(this)

/**
 * Logs this value as VERBOSE with a custom tag.<br><br>
 * 이 값을 커스텀 태그로 VERBOSE 출력한다.<br>
 *
 * @param tag Tag to use for this log.<br><br>
 *            사용할 태그.<br>
 */
fun Any.logv(tag: String) = Logx.v(tag, this)

/**
 * Logs this value as DEBUG.<br><br>
 * 이 값을 DEBUG로 출력한다.<br>
 */
fun Any.logd() = Logx.d(this)

/**
 * Logs this value as DEBUG with a custom tag.<br><br>
 * 이 값을 커스텀 태그로 DEBUG 출력한다.<br>
 *
 * @param tag Tag to use for this log.<br><br>
 *            사용할 태그.<br>
 */
fun Any.logd(tag: String) = Logx.d(tag, this)

/**
 * Logs this value as INFO.<br><br>
 * 이 값을 INFO로 출력한다.<br>
 */
fun Any.logi() = Logx.i(this)

/**
 * Logs this value as INFO with a custom tag.<br><br>
 * 이 값을 커스텀 태그로 INFO 출력한다.<br>
 *
 * @param tag Tag to use for this log.<br><br>
 *            사용할 태그.<br>
 */
fun Any.logi(tag: String) = Logx.i(tag, this)

/**
 * Logs this value as WARN.<br><br>
 * 이 값을 WARN으로 출력한다.<br>
 */
fun Any.logw() = Logx.w(this)

/**
 * Logs this value as WARN with a custom tag.<br><br>
 * 이 값을 커스텀 태그로 WARN 출력한다.<br>
 *
 * @param tag Tag to use for this log.<br><br>
 *            사용할 태그.<br>
 */
fun Any.logw(tag: String) = Logx.w(tag, this)

/**
 * Logs this value as ERROR.<br><br>
 * 이 값을 ERROR로 출력한다.<br>
 */
fun Any.loge() = Logx.e(this)

/**
 * Logs this value as ERROR with a custom tag.<br><br>
 * 이 값을 커스텀 태그로 ERROR 출력한다.<br>
 *
 * @param tag Tag to use for this log.<br><br>
 *            사용할 태그.<br>
 */
fun Any.loge(tag: String) = Logx.e(tag, this)

/**
 * Logs this value with parent trace output.<br><br>
 * 이 값을 부모 호출 트레이스와 함께 출력한다.<br>
 */
fun Any.logp() = Logx.p(this)

/**
 * Logs this value with parent trace output and a custom tag.<br><br>
 * 이 값을 부모 호출 트레이스와 커스텀 태그로 출력한다.<br>
 *
 * @param tag Tag to use for this log.<br><br>
 *            사용할 태그.<br>
 */
fun Any.logp(tag: String) = Logx.p(tag, this)

/**
 * Logs this value with current thread id.<br><br>
 * 이 값을 현재 스레드 ID와 함께 출력한다.<br>
 */
fun Any.logt() = Logx.t(this)

/**
 * Logs this value with current thread id and a custom tag.<br><br>
 * 이 값을 현재 스레드 ID와 커스텀 태그로 출력한다.<br>
 *
 * @param tag Tag to use for this log.<br><br>
 *            사용할 태그.<br>
 */
fun Any.logt(tag: String) = Logx.t(tag, this)

/**
 * Logs this JSON string with JSON formatting.<br><br>
 * 이 JSON 문자열을 JSON 포맷으로 출력한다.<br>
 */
fun String.logj() = Logx.j(this)

/**
 * Logs this JSON string with JSON formatting and a custom tag.<br><br>
 * 이 JSON 문자열을 JSON 포맷과 커스텀 태그로 출력한다.<br>
 *
 * @param tag Tag to use for this log.<br><br>
 *            사용할 태그.<br>
 */
fun String.logj(tag: String) = Logx.j(tag, this)
