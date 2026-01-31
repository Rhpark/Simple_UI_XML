package kr.open.library.simple_ui.core.system_manager.base

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import kr.open.library.simple_ui.core.extensions.conditional.checkSdkVersion
import kr.open.library.simple_ui.core.extensions.trycatch.safeCatch
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.core.permissions.extentions.remainPermissions

/**
 * Base class for all system service wrappers providing unified permission management and error handling.<br><br>
 * 통합 권한 관리와 에러 처리를 제공하는 모든 시스템 서비스 래퍼의 기본 클래스입니다.<br>
 *
 * Why this class exists.<br>
 * - Android system services require repetitive permission checks, null checks, and try-catch blocks in every call.<br>
 * - Different system services have inconsistent error handling patterns and return types.<br>
 * - Permission status tracking requires manual state management and refresh logic.<br>
 * - This class reduces boilerplate code and provides a consistent, safe pattern for accessing system services.<br><br>
 * 이 클래스가 필요한 이유.<br>
 * - Android 시스템 서비스는 매 호출마다 반복적인 권한 체크, null 체크, try-catch 블록이 필요합니다.<br>
 * - 각 시스템 서비스마다 일관되지 않은 에러 처리 패턴과 반환 타입을 가집니다.<br>
 * - 권한 상태 추적을 위해 수동 상태 관리와 갱신 로직이 필요합니다.<br>
 * - 이 클래스는 보일러플레이트 코드를 줄이고 시스템 서비스 접근을 위한 일관되고 안전한 패턴을 제공합니다.<br>
 *
 * Design decisions.<br>
 * - **Runtime/Special permissions only policy**: Only validates runtime and special permissions (like SYSTEM_ALERT_WINDOW).<br>
 *   System-only permissions (like BATTERY_STATS) are declared but not enforced, allowing graceful degradation.<br>
 *   This enables the library to work in regular apps while providing richer data in system/preloaded apps.<br>
 * - **Default value pattern**: Uses default values (including null when needed) instead of throwing or Result types.<br>
 *   This prevents null pointer exceptions and provides predictable fallback behavior without requiring null checks in every usage.<br>
 *   Errors are logged automatically, so developers can diagnose issues without complex error handling at call sites.<br>
 * - **Automatic permission tracking**: Tracks permission status in init block and provides refresh mechanism.<br>
 *   Child classes don't need to implement permission logic - just call tryCatchSystemManager().<br><br>
 * 설계 결정 이유.<br>
 * - **런타임/특수 권한만 검증 정책**: 런타임 권한과 특수 권한(SYSTEM_ALERT_WINDOW 등)만 검증합니다.<br>
 *   시스템 전용 권한(BATTERY_STATS 등)은 선언만 하고 강제하지 않으므로 권한 없이도 라이브러리가 정상 동작합니다.<br>
 *   이를 통해 일반 앱에서도 라이브러리가 작동하면서 시스템/프리로드 앱에서는 더 풍부한 데이터를 제공할 수 있습니다.<br>
 * - **기본값 패턴**: 필요 시 null을 포함한 기본값(BATTERY_ERROR_VALUE 등)을 사용합니다.<br>
 *   이는 null pointer exception을 방지하고 모든 사용처에서 null 체크 없이도 예측 가능한 fallback 동작을 제공합니다.<br>
 *   에러는 자동으로 로깅되므로 호출 지점에서 복잡한 에러 처리 없이도 문제를 진단할 수 있습니다.<br>
 * - **자동 권한 추적**: init 블록에서 권한 상태를 추적하고 갱신 메커니즘을 제공합니다.<br>
 *   자식 클래스는 권한 로직을 구현할 필요 없이 tryCatchSystemManager()만 호출하면 됩니다.<br>
 *
 * Caution.<br>
 * - Permission state is cached; getPermissionInfo() and isPermissionGranted() reflect the cached state.<br>
 * - Call refreshPermissions() after permissions are granted to update the cache.<br><br>
 * 주의사항.<br>
 * - 권한 상태는 캐시되며, getPermissionInfo()와 isPermissionGranted()는 캐시 상태를 반영합니다.<br>
 * - 권한 허용 후 refreshPermissions()를 호출해 캐시를 갱신하세요.<br>
 *
 * Usage.<br>
 * 1. Extend this class and pass required permissions to the constructor.<br>
 * 2. Wrap all system service calls with tryCatchSystemManager(defaultValue) { ... }.<br>
 * 3. Override onDestroy() if you need cleanup logic (don't forget to call super.onDestroy()).<br><br>
 * 사용법.<br>
 * 1. 이 클래스를 상속하고 필요한 권한을 생성자에 전달하세요.<br>
 * 2. 모든 시스템 서비스 호출을 tryCatchSystemManager(defaultValue) { ... }로 감싸세요.<br>
 * 3. 정리 로직이 필요하면 onDestroy()를 재정의하세요 (super.onDestroy() 호출을 잊지 마세요).<br>
 *
 * @param context The application context.<br><br>
 *                애플리케이션 컨텍스트.<br>
 * @param requiredPermissions The list of required permissions (runtime/special only).<br><br>
 *                            필요한 권한 목록입니다 (런타임/특수 권한만 검증).<br>
 */
public abstract class BaseSystemService(
    protected val context: Context,
    private val requiredPermissions: List<String>? = null,
) {
    private var remainPermissions = emptyList<String>()

    init {
        requiredPermissions?.let {
            logMissingManifestPermissions(it)
            remainPermissions = context.remainPermissions(it)
            if (remainPermissions.isEmpty()) {
                Logx.d("All required runtime/special permissions granted for ${this::class.simpleName}")
            } else {
                Logx.w("Missing runtime/special permissions for ${this::class.simpleName}: $remainPermissions")
            }
        }
    }

    /**
     * Logs a warning if any required permissions are not declared in AndroidManifest.xml.<br><br>
     * 필요한 권한이 AndroidManifest.xml에 선언되지 않은 경우 경고 로그를 출력합니다.<br>
     *
     * @param permissions The list of permissions to check against the manifest.<br><br>
     *                    매니페스트에서 확인할 권한 목록.
     */
    private fun logMissingManifestPermissions(permissions: List<String>) {
        val declaredPermissions = getDeclaredManifestPermissions()
        val missingPermissions = permissions.filterNot { declaredPermissions.contains(it) }
        if (missingPermissions.isNotEmpty()) {
            Logx.w("${this::class.simpleName}: AndroidManifest.xml에 선언되지 않은 권한이 있습니다. missing=$missingPermissions")
        }
    }

    /**
     * Retrieves the set of permissions declared in AndroidManifest.xml.<br>
     * Uses SDK TIRAMISU+ API when available, falls back to deprecated API for older versions.<br><br>
     * AndroidManifest.xml에 선언된 권한 집합을 조회합니다.<br>
     * SDK TIRAMISU+ API를 우선 사용하고, 이전 버전에서는 deprecated API로 폴백합니다.<br>
     *
     * @return The set of declared permissions, or an empty set if retrieval fails.<br><br>
     *         선언된 권한 집합, 조회 실패 시 빈 집합을 반환.<br>
     */
    private fun getDeclaredManifestPermissions(): Set<String> = safeCatch(defaultValue = emptySet()) {
        val packageInfo = checkSdkVersion(
            Build.VERSION_CODES.TIRAMISU,
            positiveWork = {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong()),
                )
            },
            negativeWork = {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
            },
        )
        packageInfo.requestedPermissions?.toSet() ?: emptySet()
    }

    /**
     * Gets the list of denied permissions.<br><br>
     * 거부된 권한 목록을 가져옵니다.<br>
     *
     * @return The list of permissions that have been denied.<br><br>
     *         거부된 권한 목록.<br>
     */
    protected fun getDeniedPermissionList(): List<String> {
        refreshPermissions()
        return remainPermissions
    }

    /**
     * Checks if all required permissions have been granted.<br><br>
     * 모든 필수 권한이 부여되었는지 확인합니다.<br>
     *
     * @return Returns true if all permissions have been granted, false otherwise.<br><br>
     *         모든 권한이 부여된 경우 true, 그렇지 않으면 false를 반환.<br>
     */
    protected fun isPermissionAllGranted(): Boolean = remainPermissions.isEmpty()

    /**
     * Simple safe execution with basic error handling.
     * Executes the given block if all required runtime/special permissions are granted.
     * If runtime/special permissions are missing, returns the default value and logs a warning.
     * If an exception occurs during execution, catches it and returns the default value.<br><br>
     *
     * 기본 오류 처리가 포함된 간단한 안전 실행입니다.
     * 모든 필수 런타임/특수 권한이 부여된 경우 주어진 블록을 실행합니다.
     * 런타임/특수 권한이 누락된 경우 기본값을 반환하고 경고를 로깅합니다.
     * 실행 중 예외가 발생하면 이를 포착하고 기본값을 반환합니다.<br>
     *
     * @param T The type of the return value.<br><br>
     *          반환값의 타입.
     * @param defaultValue The default value to return if runtime/special permissions are missing or an error occurs.<br><br>
     *                     런타임/특수 권한이 누락되거나 오류가 발생할 경우 반환할 기본값.
     * @param block The operation to execute if all runtime/special permissions are granted.<br><br>
     *              모든 런타임/특수 권한이 부여된 경우 실행할 작업.
     * @return Returns the result of the block execution if successful, or defaultValue if runtime/special
     *         permissions are missing or an error occurs.<br><br>
     *         성공 시 블록 실행 결과를 반환하고, 런타임/특수 권한이 누락되거나 오류 발생 시 defaultValue를 반환.<br>
     */
    protected inline fun <T> tryCatchSystemManager(defaultValue: T, block: () -> T): T {
        val deniedPermissions = getDeniedPermissionList()
        if (deniedPermissions.isNotEmpty()) {
            Logx.w("${this::class.simpleName}: Missing runtime/special permissions!!! - $deniedPermissions")
            return defaultValue
        }
        return safeCatch(
            block = block,
            onCatch = { e ->
                Logx.e("${this::class.simpleName}: Error occurred : ${e.message}")
                defaultValue
            },
        )
    }

    /**
     * Refreshes the permission status. Call this after requesting permissions.<br><br>
     * 권한 상태를 새로고침합니다. 권한 요청 후 이를 호출하세요.<br>
     */
    public fun refreshPermissions() {
        requiredPermissions?.let {
            remainPermissions = context.remainPermissions(it)
            if (remainPermissions.isNotEmpty()) {
                Logx.d("remainPermissions $remainPermissions")
            }
        }
    }

    /**
     * Gets information about required permissions and their status.<br><br>
     * 필요한 권한과 그 상태에 대한 정보를 가져옵니다.<br>
     *
     * @return A map where keys are permission names and values are their granted status (true if granted, false otherwise).<br><br>
     *         권한 이름을 키로, 부여 상태를 값으로 하는 맵 (부여된 경우 true, 그렇지 않으면 false).<br>
     */
    public fun getPermissionInfo(): Map<String, Boolean> =
        requiredPermissions?.associateWith { permission -> !remainPermissions.contains(permission) } ?: emptyMap()

    /**
     * Checks if a specific permission is granted.<br><br>
     * 특정 권한이 부여되었는지 확인합니다.<br>
     *
     * @param permission The permission to check.<br><br>
     *                   확인할 권한.
     * @return Returns true if the permission is granted, false otherwise.<br><br>
     *         권한이 부여된 경우 true, 그렇지 않으면 false를 반환.<br>
     */
    public fun isPermissionGranted(permission: String): Boolean = !remainPermissions.contains(permission)

    /**
     * Called when the service is being destroyed. Override to perform cleanup.<br><br>
     * 서비스가 소멸될 때 호출됩니다. 정리 작업을 수행하려면 재정의하세요.<br>
     */
    public open fun onDestroy() {
        Logx.d("${this::class.simpleName} destroyed")
    }
}
