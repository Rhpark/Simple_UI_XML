package kr.open.library.simple_ui.core.system_manager.controller.wifi.internal

/**
 * Guard class that safely executes WiFi operations with exception handling.<br><br>
 * 예외 처리와 함께 WiFi 작업을 안전하게 실행하는 가드 클래스입니다.<br>
 *
 * @param executor Function that executes operations with default value fallback.<br><br>
 *                 기본값 폴백과 함께 작업을 실행하는 함수.
 */
internal class WifiOperationGuard(
    private val executor: (defaultValue: Any?, block: () -> Any?) -> Any?,
) {
    /**
     * Executes a block of code safely, returning defaultValue on exception.<br><br>
     * 코드 블록을 안전하게 실행하며, 예외 발생 시 defaultValue를 반환합니다.<br>
     *
     * @param T The return type.<br><br>
     *          반환 타입.
     * @param defaultValue Value to return if execution fails.<br><br>
     *                     실행 실패 시 반환할 값.
     * @param block Code block to execute.<br><br>
     *              실행할 코드 블록.
     * @return Result of block execution or defaultValue on failure.<br><br>
     *         블록 실행 결과 또는 실패 시 defaultValue.<br>
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> run(
        defaultValue: T,
        block: () -> T,
    ): T = executor(defaultValue, block) as T
}
