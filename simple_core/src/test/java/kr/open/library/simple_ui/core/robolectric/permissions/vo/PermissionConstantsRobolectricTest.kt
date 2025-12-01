package kr.open.library.simple_ui.core.robolectric.permissions.vo

import android.Manifest
import android.os.Build
import android.provider.Settings
import kr.open.library.simple_ui.core.permissions.vo.PermissionConstants
import kr.open.library.simple_ui.core.permissions.vo.PermissionSpecialType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Robolectric tests for PermissionConstants
 */
@RunWith(RobolectricTestRunner::class)
class PermissionConstantsRobolectricTest {
    // ==============================================
    // SPECIAL_PERMISSION_ACTIONS Tests
    // ==============================================

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU]) // API 33
    fun specialPermissionActions_containsAllBasePermissions() {
        val actions = PermissionConstants.SPECIAL_PERMISSION_ACTIONS

        // Verify base permissions that should always be present
        assertEquals(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            actions[Manifest.permission.SYSTEM_ALERT_WINDOW],
        )

        assertEquals(
            Settings.ACTION_MANAGE_WRITE_SETTINGS,
            actions[Manifest.permission.WRITE_SETTINGS],
        )

        assertEquals(
            Settings.ACTION_USAGE_ACCESS_SETTINGS,
            actions[Manifest.permission.PACKAGE_USAGE_STATS],
        )

        assertEquals(
            Settings.ACTION_ACCESSIBILITY_SETTINGS,
            actions[Manifest.permission.BIND_ACCESSIBILITY_SERVICE],
        )

        assertEquals(
            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
            actions[Manifest.permission.REQUEST_INSTALL_PACKAGES],
        )

        assertEquals(
            Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS,
            actions[Manifest.permission.ACCESS_NOTIFICATION_POLICY],
        )

        assertEquals(
            Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS,
            actions[Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE],
        )

        assertEquals(
            Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
            actions[Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS],
        )
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU]) // API 33
    fun specialPermissionActions_containsAndroidRPermissions() {
        val actions = PermissionConstants.SPECIAL_PERMISSION_ACTIONS

        // Android R (API 30) permission
        assertEquals(
            Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
            actions[Manifest.permission.MANAGE_EXTERNAL_STORAGE],
        )
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU]) // API 33
    fun specialPermissionActions_containsAndroidSPermissions() {
        val actions = PermissionConstants.SPECIAL_PERMISSION_ACTIONS

        // Android S (API 31) permission
        assertEquals(
            Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
            actions[Manifest.permission.SCHEDULE_EXACT_ALARM],
        )
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q]) // API 29 (before Android R)
    fun specialPermissionActions_doesNotContainAndroidRPermissionsOnOlderSdk() {
        val actions = PermissionConstants.SPECIAL_PERMISSION_ACTIONS

        // MANAGE_EXTERNAL_STORAGE should not be present on API 29
        assertFalse(actions.containsKey(Manifest.permission.MANAGE_EXTERNAL_STORAGE))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R]) // API 30 (before Android S)
    fun specialPermissionActions_doesNotContainAndroidSPermissionsOnOlderSdk() {
        val actions = PermissionConstants.SPECIAL_PERMISSION_ACTIONS

        // SCHEDULE_EXACT_ALARM should not be present on API 30
        assertFalse(actions.containsKey(Manifest.permission.SCHEDULE_EXACT_ALARM))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun specialPermissionActions_hasCorrectSize() {
        val actions = PermissionConstants.SPECIAL_PERMISSION_ACTIONS

        // 8 base permissions + 1 Android R + 1 Android S = 10 total on API 33
        assertEquals(10, actions.size)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun specialPermissionActions_allEnumEntriesAreMapped() {
        val actions = PermissionConstants.SPECIAL_PERMISSION_ACTIONS

        // Verify all PermissionSpecialType entries have corresponding actions
        PermissionSpecialType.entries.forEach { specialType ->
            assertTrue(
                "Permission ${specialType.permission} should be in the map",
                actions.containsKey(specialType.permission),
            )
        }
    }

    // ==============================================
    // PERMISSIONS_REQUIRING_PACKAGE_URI Tests
    // ==============================================

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun permissionsRequiringPackageUri_containsExpectedPermissions() {
        val uriPermissions = PermissionConstants.PERMISSIONS_REQUIRING_PACKAGE_URI

        assertTrue(uriPermissions.contains(Manifest.permission.SYSTEM_ALERT_WINDOW))
        assertTrue(uriPermissions.contains(Manifest.permission.WRITE_SETTINGS))
        assertTrue(uriPermissions.contains(Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS))
        assertTrue(uriPermissions.contains(Manifest.permission.REQUEST_INSTALL_PACKAGES))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun permissionsRequiringPackageUri_containsAndroidRPermission() {
        val uriPermissions = PermissionConstants.PERMISSIONS_REQUIRING_PACKAGE_URI

        // MANAGE_EXTERNAL_STORAGE requires package URI on Android R+
        assertTrue(uriPermissions.contains(Manifest.permission.MANAGE_EXTERNAL_STORAGE))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q]) // API 29
    fun permissionsRequiringPackageUri_doesNotContainAndroidRPermissionOnOlderSdk() {
        val uriPermissions = PermissionConstants.PERMISSIONS_REQUIRING_PACKAGE_URI

        // MANAGE_EXTERNAL_STORAGE should not be present on API 29
        assertFalse(uriPermissions.contains(Manifest.permission.MANAGE_EXTERNAL_STORAGE))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun permissionsRequiringPackageUri_hasCorrectSize() {
        val uriPermissions = PermissionConstants.PERMISSIONS_REQUIRING_PACKAGE_URI

        // 4 base permissions + 1 Android R = 5 total on API 33
        assertEquals(5, uriPermissions.size)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun permissionsRequiringPackageUri_sizeChangesWithSdkLevel() {
        val uriPermissions = PermissionConstants.PERMISSIONS_REQUIRING_PACKAGE_URI

        // On API 30 (Android R), should include MANAGE_EXTERNAL_STORAGE
        assertEquals(5, uriPermissions.size)
    }

    // ==============================================
    // ApiLevelRequirements Tests
    // ==============================================

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun apiLevelRequirements_androidRPermissions_containsExpectedPermissions() {
        val rPermissions = PermissionConstants.ApiLevelRequirements.ANDROID_R_PERMISSIONS

        assertTrue(rPermissions.contains(Manifest.permission.MANAGE_EXTERNAL_STORAGE))
        assertEquals(1, rPermissions.size)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q]) // API 29
    fun apiLevelRequirements_androidRPermissions_emptyOnOlderSdk() {
        val rPermissions = PermissionConstants.ApiLevelRequirements.ANDROID_R_PERMISSIONS

        assertTrue(rPermissions.isEmpty())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun apiLevelRequirements_androidSPermissions_containsExpectedPermissions() {
        val sPermissions = PermissionConstants.ApiLevelRequirements.ANDROID_S_PERMISSIONS

        assertTrue(sPermissions.contains(Manifest.permission.SCHEDULE_EXACT_ALARM))
        assertEquals(1, sPermissions.size)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R]) // API 30
    fun apiLevelRequirements_androidSPermissions_emptyOnOlderSdk() {
        val sPermissions = PermissionConstants.ApiLevelRequirements.ANDROID_S_PERMISSIONS

        assertTrue(sPermissions.isEmpty())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun apiLevelRequirements_androidTiramisuPermissions_containsExpectedPermissions() {
        val tiramisuPermissions = PermissionConstants.ApiLevelRequirements.ANDROID_TIRAMISU_PERMISSIONS

        assertTrue(tiramisuPermissions.contains(Manifest.permission.POST_NOTIFICATIONS))
        assertEquals(1, tiramisuPermissions.size)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S]) // API 31
    fun apiLevelRequirements_androidTiramisuPermissions_emptyOnOlderSdk() {
        val tiramisuPermissions = PermissionConstants.ApiLevelRequirements.ANDROID_TIRAMISU_PERMISSIONS

        assertTrue(tiramisuPermissions.isEmpty())
    }

    // ==============================================
    // Integration Tests
    // ==============================================

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun integration_allSpecialPermissionsHaveActions() {
        val actions = PermissionConstants.SPECIAL_PERMISSION_ACTIONS

        // Every special permission should have a corresponding action
        actions.forEach { (permission, action) ->
            assertNotNull("Permission $permission should have non-null action", action)
            assertTrue("Action should not be empty", action.isNotEmpty())
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun integration_packageUriPermissionsAreSubsetOfSpecialPermissions() {
        val specialPermissions = PermissionConstants.SPECIAL_PERMISSION_ACTIONS.keys
        val uriPermissions = PermissionConstants.PERMISSIONS_REQUIRING_PACKAGE_URI

        // All URI-requiring permissions should be in the special permissions map
        uriPermissions.forEach { uriPermission ->
            assertTrue(
                "URI permission $uriPermission should be in special permissions",
                specialPermissions.contains(uriPermission),
            )
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun integration_apiLevelPermissionsAreInSpecialPermissions() {
        val specialPermissions = PermissionConstants.SPECIAL_PERMISSION_ACTIONS.keys
        val rPermissions = PermissionConstants.ApiLevelRequirements.ANDROID_R_PERMISSIONS
        val sPermissions = PermissionConstants.ApiLevelRequirements.ANDROID_S_PERMISSIONS

        // Android R permissions should be in special permissions
        rPermissions.forEach { permission ->
            assertTrue(
                "Android R permission $permission should be in special permissions",
                specialPermissions.contains(permission),
            )
        }

        // Android S permissions should be in special permissions
        sPermissions.forEach { permission ->
            assertTrue(
                "Android S permission $permission should be in special permissions",
                specialPermissions.contains(permission),
            )
        }
    }
}
