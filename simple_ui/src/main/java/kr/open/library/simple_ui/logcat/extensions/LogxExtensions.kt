package kr.open.library.simple_ui.logcat.extensions

import kr.open.library.simple_ui.logcat.Logx


/**
 * Logging extension functions for all objects to enable method chaining and fluent API style.<br><br>
 * 메서드 체이닝과 유창한 API 스타일을 가능하게 하는 모든 객체에 대한 로깅 확장 함수입니다.<br>
 *
 * Example usage:<br>
 * ```kotlin
 * // Method chaining
 * val result = someData
 *     .logxD()              // Debug log
 *     .transform()
 *     .logxV("Transform")   // Verbose log with tag
 *     .process()
 *
 * // Inline logging
 * getUserData().logxI("UserData")
 *
 * // JSON logging for String
 * jsonString.logxJ("API Response")
 *
 * // Parent method tracking
 * someValue.logxP()
 */

/**
 * Logs this object as a debug message and returns it for method chaining.<br><br>
 * 이 객체를 Debug 메시지로 로깅하고 메서드 체이닝을 위해 반환합니다.<br>
 */
public /*inline*/ fun Any.logxD(): Unit = Logx.d1(this)

/**
 * Logs this object as a debug message with a custom tag and returns it for method chaining.<br><br>
 * 이 객체를 커스텀 태그와 함께 Debug 메시지로 로깅하고 메서드 체이닝을 위해 반환합니다.<br>
 *
 * @param tag The custom tag for this log entry.<br><br>
 *            이 로그 항목의 커스텀 태그.
 */
public /*inline*/ fun Any.logxD(tag: String): Unit = Logx.d1(tag, this)

/**
 * Logs this object as a verbose message and returns it for method chaining.<br><br>
 * 이 객체를 Verbose 메시지로 로깅하고 메서드 체이닝을 위해 반환합니다.<br>
 */
public /*inline*/ fun Any.logxV(): Unit = Logx.v1(this)

/**
 * Logs this object as a verbose message with a custom tag and returns it for method chaining.<br><br>
 * 이 객체를 커스텀 태그와 함께 Verbose 메시지로 로깅하고 메서드 체이닝을 위해 반환합니다.<br>
 *
 * @param tag The custom tag for this log entry.<br><br>
 *            이 로그 항목의 커스텀 태그.
 */
public /*inline*/ fun Any.logxV(tag: String): Unit = Logx.v1(tag, this)

/**
 * Logs this object as a warning message and returns it for method chaining.<br><br>
 * 이 객체를 Warning 메시지로 로깅하고 메서드 체이닝을 위해 반환합니다.<br>
 */
public /*inline*/ fun Any.logxW(): Unit = Logx.w1(this)

/**
 * Logs this object as a warning message with a custom tag and returns it for method chaining.<br><br>
 * 이 객체를 커스텀 태그와 함께 Warning 메시지로 로깅하고 메서드 체이닝을 위해 반환합니다.<br>
 *
 * @param tag The custom tag for this log entry.<br><br>
 *            이 로그 항목의 커스텀 태그.
 */
public /*inline*/ fun Any.logxW(tag: String): Unit =  Logx.w1(tag,this)

/**
 * Logs this object as an info message and returns it for method chaining.<br><br>
 * 이 객체를 Info 메시지로 로깅하고 메서드 체이닝을 위해 반환합니다.<br>
 */
public /*inline*/ fun Any.logxI(): Unit = Logx.i1(this)

/**
 * Logs this object as an info message with a custom tag and returns it for method chaining.<br><br>
 * 이 객체를 커스텀 태그와 함께 Info 메시지로 로깅하고 메서드 체이닝을 위해 반환합니다.<br>
 *
 * @param tag The custom tag for this log entry.<br><br>
 *            이 로그 항목의 커스텀 태그.
 */
public /*inline*/ fun Any.logxI(tag: String): Unit = Logx.i1(tag,this)

/**
 * Logs this object as an error message and returns it for method chaining.<br><br>
 * 이 객체를 Error 메시지로 로깅하고 메서드 체이닝을 위해 반환합니다.<br>
 */
public /*inline*/ fun Any.logxE(): Unit = Logx.e1(this)

/**
 * Logs this object as an error message with a custom tag and returns it for method chaining.<br><br>
 * 이 객체를 커스텀 태그와 함께 Error 메시지로 로깅하고 메서드 체이닝을 위해 반환합니다.<br>
 *
 * @param tag The custom tag for this log entry.<br><br>
 *            이 로그 항목의 커스텀 태그.
 */
public /*inline*/ fun Any.logxE(tag: String): Unit = Logx.e1(tag,this)

/**
 * Logs this JSON string with proper formatting and visual markers.<br><br>
 * 이 JSON 문자열을 적절한 포맷팅과 시각적 마커로 로깅합니다.<br>
 */
public /*inline*/ fun String.logxJ(): Unit = Logx.j1(this)

/**
 * Logs this JSON string with a custom tag, proper formatting and visual markers.<br><br>
 * 이 JSON 문자열을 커스텀 태그, 적절한 포맷팅과 시각적 마커로 로깅합니다.<br>
 *
 * @param tag The custom tag for this log entry.<br><br>
 *            이 로그 항목의 커스텀 태그.
 */
public /*inline*/ fun String.logxJ(tag: String): Unit = Logx.j1(tag,this)

/**
 * Logs this object with parent method call tracking information and returns it for method chaining.<br><br>
 * 이 객체를 부모 메서드 호출 추적 정보와 함께 로깅하고 메서드 체이닝을 위해 반환합니다.<br>
 */
public /*inline*/ fun Any.logxP(): Unit = Logx.p1(this)

/**
 * Logs this object with a custom tag and parent method call tracking information, returns it for method chaining.<br><br>
 * 이 객체를 커스텀 태그와 부모 메서드 호출 추적 정보와 함께 로깅하고 메서드 체이닝을 위해 반환합니다.<br>
 *
 * @param tag The custom tag for this log entry.<br><br>
 *            이 로그 항목의 커스텀 태그.
 */
public /*inline*/ fun Any.logxP(tag:String): Unit = Logx.p1(tag,this)
