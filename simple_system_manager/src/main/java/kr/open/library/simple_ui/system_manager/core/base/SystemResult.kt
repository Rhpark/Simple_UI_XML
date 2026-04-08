package kr.open.library.simple_ui.system_manager.core.base

/**
 * Represents the outcome of a system service operation with cause distinction.<br><br>
 * 시스템 서비스 작업의 결과를 원인별로 구분하여 표현합니다.<br>
 *
 * Use [Success] when the operation completed successfully.<br>
 * Use [PermissionDenied] when a required runtime/special permission is missing.<br>
 * Use [PolicyRestricted] when the operation is blocked by OS policy (e.g. API 29+ WiFi control).<br>
 * Use [Failure] when an unexpected exception occurred during execution.<br><br>
 *
 * 작업이 정상적으로 완료된 경우 [Success]를 사용하세요.<br>
 * 필수 런타임/특수 권한이 없는 경우 [PermissionDenied]를 사용하세요.<br>
 * OS 정책으로 작업이 차단된 경우 [PolicyRestricted]를 사용하세요 (예: API 29+ WiFi 제어).<br>
 * 실행 중 예외가 발생한 경우 [Failure]를 사용하세요.<br>
 *
 * Usage for action APIs (T = Unit):<br>
 * 액션 API 사용 예시 (T = Unit):<br>
 * ```kotlin
 * when (val result = wifiController.setWifiEnabled(true)) {
 *     is SystemResult.Success        -> { /* 성공 */ }
 *     is SystemResult.PermissionDenied -> { /* 권한 없음 처리 */ }
 *     is SystemResult.PolicyRestricted -> { /* API 29+: 설정에서 직접 변경 안내 */ }
 *     is SystemResult.Failure        -> { /* result.cause 로 원인 확인 */ }
 * }
 * ```
 *
 * Usage for query APIs (T = actual type):<br>
 * 조회 API 사용 예시 (T = 실제 타입):<br>
 * ```kotlin
 * when (val result = wifiController.getWifiState()) {
 *     is SystemResult.Success -> handleState(result.value)
 *     is SystemResult.PermissionDenied -> { /* 권한 없음 처리 */ }
 *     is SystemResult.Failure -> { /* result.cause 로 원인 확인 */ }
 *     else -> { /* PolicyRestricted 등 */ }
 * }
 * ```
 *
 * @param T The type of the success value.<br><br>
 *          성공 값의 타입입니다.<br>
 */
public sealed interface SystemResult<out T> {
    /**
     * The operation completed successfully.<br><br>
     * 작업이 정상적으로 완료되었습니다.<br>
     *
     * @param value The result value. Use [Unit] for action-only operations.<br><br>
     *              결과 값입니다. 액션 전용 작업에는 [Unit]을 사용합니다.<br>
     */
    public data class Success<T>(
        val value: T
    ) : SystemResult<T>

    /**
     * The operation was skipped because a required runtime/special permission is missing.<br><br>
     * 필수 런타임/특수 권한이 없어 작업이 건너뛰어졌습니다.<br>
     */
    public data object PermissionDenied : SystemResult<Nothing>

    /**
     * The operation is not allowed due to OS policy restrictions.<br>
     * Example: WiFi enable/disable is deprecated on API 29+.<br><br>
     * OS 정책 제한으로 작업이 허용되지 않습니다.<br>
     * 예: API 29+에서 WiFi 활성화/비활성화가 deprecated 처리됩니다.<br>
     */
    public data object PolicyRestricted : SystemResult<Nothing>

    /**
     * The operation failed due to an unexpected exception.<br><br>
     * 예상치 못한 예외로 작업이 실패했습니다.<br>
     *
     * @param cause The exception that caused the failure, or null if not available.<br><br>
     *              실패 원인 예외입니다. 없으면 null입니다.<br>
     */
    public data class Failure(
        val cause: Throwable?
    ) : SystemResult<Nothing>
}
