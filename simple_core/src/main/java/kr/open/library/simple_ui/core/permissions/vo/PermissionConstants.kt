package kr.open.library.simple_ui.core.permissions.vo

import kr.open.library.simple_ui.core.permissions.internal.PermissionPolicy

/**
 * Centralizes permission-related constants shared across the library.<br><br>
 * 라이브러리 전반에서 사용하는 권한 관련 상수를 모아둔 객체입니다.<br>
 */
public object PermissionConstants {
    /**
     * Maps each special permission to the Settings action required to grant it.<br><br>
     * 특수 권한을 부여하기 위해 이동해야 하는 Settings 액션을 매핑합니다.<br>
     */
    val SPECIAL_PERMISSION_ACTIONS: Map<String, String>
        get() = PermissionPolicy.specialPermissionActions

    /**
     * Enumerates special permissions that require a package URI when launching settings.<br><br>
     * 설정 화면 호출 시 package URI가 필요한 특수 권한 목록입니다.<br>
     */
    val PERMISSIONS_REQUIRING_PACKAGE_URI: Set<String>
        get() = PermissionPolicy.permissionsRequiringPackageUri

    /**
     * Groups permissions that only exist from specific API levels upward.<br><br>
     * 특정 API 레벨 이상에서만 존재하는 권한을 묶어둔 영역입니다.<br>
     *
     * **Structural limitation / 구조적 한계:**<br>
     * These sets are maintained manually by the internal permission policy. Permissions not listed here fall through to `else → true`
     * in [PermissionClassifier.isSupported], which treats them as universally supported.
     * When a new Android version introduces new dangerous permissions, the corresponding set
     * must be added here and a matching branch must be added to [PermissionClassifier.isSupported].<br><br>
     * 이 공개 집합과 내부 권한 정책은 함께 수동으로 관리됩니다. 여기에 없는 권한은 [PermissionClassifier.isSupported]의
     * `else → true` 분기로 흘러 모든 API 레벨에서 지원되는 것처럼 처리됩니다.
     * 새 Android 버전에서 신규 dangerous 권한이 추가되면 해당 집합과 분기를 반드시 추가해야 합니다.<br>
     */
    object ApiLevelRequirements {
        /**
         * Permissions that were introduced on Android R.<br><br>
         * Android R에서 추가된 권한 목록입니다.<br>
         */
        val ANDROID_R_PERMISSIONS: Set<String>
            get() = PermissionPolicy.ApiLevelRequirements.androidRPermissions

        /**
         * Permissions that were introduced on Android S.<br><br>
         * Android S에서 추가된 권한 목록입니다.<br>
         */
        val ANDROID_S_PERMISSIONS: Set<String>
            get() = PermissionPolicy.ApiLevelRequirements.androidSPermissions

        /**
         * Permissions that were introduced on Android 13 (Tiramisu).<br><br>
         * Android 13(Tiramisu)에서 추가된 권한 목록입니다.<br>
         */
        val ANDROID_TIRAMISU_PERMISSIONS: Set<String>
            get() = PermissionPolicy.ApiLevelRequirements.androidTiramisuPermissions

        /**
         * Permissions that were introduced on Android 14 (U).<br><br>
         * Android 14(U)에서 추가된 권한 목록입니다.<br>
         */
        val ANDROID_U_PERMISSIONS: Set<String>
            get() = PermissionPolicy.ApiLevelRequirements.androidUPermissions
    }

    /**
     * Default configuration values used by permission components.<br><br>
     * 권한 컴포넌트에서 사용하는 기본 설정 값입니다.<br>
     */
    object Defaults {
        /**
         * Timeout (in milliseconds) after which pending requests are cleaned up.<br><br>
         * 대기 중인 권한 요청을 정리하는 타임아웃(밀리초)입니다.<br>
         */
        const val REQUEST_TIMEOUT_MS = 300_000L // 5분
    }
}
