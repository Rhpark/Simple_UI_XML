package kr.open.library.simple_ui.permissions.vo

import android.Manifest
import android.os.Build
import android.provider.Settings
import kr.open.library.simple_ui.extensions.conditional.checkSdkVersion

/**
 * Centralizes permission-related constants shared across the library.<br><br>
 * 라이브러리 전반에서 사용하는 권한 관련 상수를 모아둔 객체입니다.<br>
 */
internal object PermissionConstants {

    /**
     * Maps each special permission to the Settings action required to grant it.<br><br>
     * 특수 권한을 부여하기 위해 이동해야 하는 Settings 액션을 매핑합니다.<br>
     */
    val SPECIAL_PERMISSION_ACTIONS: Map<String, String> = buildMap {
        PermissionSpecialType.entries.forEach {
            when(it) {
                PermissionSpecialType.SYSTEM_ALERT_WINDOW           -> put(it.permission, Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                PermissionSpecialType.WRITE_SETTINGS                -> put(it.permission, Settings.ACTION_MANAGE_WRITE_SETTINGS)
                PermissionSpecialType.PACKAGE_USAGE_STATS           -> put(it.permission, Settings.ACTION_USAGE_ACCESS_SETTINGS)
                PermissionSpecialType.BIND_ACCESSIBILITY_SERVICE    -> put(it.permission, Settings.ACTION_ACCESSIBILITY_SETTINGS)
                PermissionSpecialType.REQUEST_INSTALL_PACKAGES      -> put(it.permission, Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                PermissionSpecialType.ACCESS_NOTIFICATION_POLICY    -> put(it.permission, Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                PermissionSpecialType.BIND_NOTIFICATION_LISTENER_SERVICE    -> put(it.permission, Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                PermissionSpecialType.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS  -> put(it.permission, Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)

                else -> {
                    checkSdkVersion(Build.VERSION_CODES.S) {
                        if(it == PermissionSpecialType.SCHEDULE_EXACT_ALARM) {
                            put(Manifest.permission.SCHEDULE_EXACT_ALARM, Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                        }
                    }

                    checkSdkVersion(Build.VERSION_CODES.R) {
                        if(it == PermissionSpecialType.MANAGE_EXTERNAL_STORAGE) {
                            put(Manifest.permission.MANAGE_EXTERNAL_STORAGE, Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                        }
                    }
                }
            }
        }
    }

    /**
     * Enumerates special permissions that require a package URI when launching settings.<br><br>
     * 설정 화면 호출 시 package URI가 필요한 특수 권한 목록입니다.<br>
     */
    val PERMISSIONS_REQUIRING_PACKAGE_URI = buildSet<String> {
        add(Manifest.permission.SYSTEM_ALERT_WINDOW)
        add(Manifest.permission.WRITE_SETTINGS)
        add(Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
        add(Manifest.permission.REQUEST_INSTALL_PACKAGES)
        checkSdkVersion(Build.VERSION_CODES.R) {
            add(Manifest.permission.MANAGE_EXTERNAL_STORAGE)
        }
    }

    /**
     * Groups permissions that only exist from specific API levels upward.<br><br>
     * 특정 API 레벨 이상에서만 존재하는 권한을 묶어둔 영역입니다.<br>
     */
    object ApiLevelRequirements {

        /**
         * Permissions that were introduced on Android R.<br><br>
         * Android R에서 추가된 권한 목록입니다.<br>
         */
        val ANDROID_R_PERMISSIONS = buildSet<String> {
            checkSdkVersion(Build.VERSION_CODES.R) {
                add(Manifest.permission.MANAGE_EXTERNAL_STORAGE)
            }
        }

        /**
         * Permissions that were introduced on Android S.<br><br>
         * Android S에서 추가된 권한 목록입니다.<br>
         */
        val ANDROID_S_PERMISSIONS = buildSet<String> {
            checkSdkVersion(Build.VERSION_CODES.S) {
                add(Manifest.permission.SCHEDULE_EXACT_ALARM)
            }
        }

        /**
         * Permissions that were introduced on Android 13 (Tiramisu).<br><br>
         * Android 13(Tiramisu)에서 추가된 권한 목록입니다.<br>
         */
        val ANDROID_TIRAMISU_PERMISSIONS = buildSet<String> {
            checkSdkVersion(Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
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