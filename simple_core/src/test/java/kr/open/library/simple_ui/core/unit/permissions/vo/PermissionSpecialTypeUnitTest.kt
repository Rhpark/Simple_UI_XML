package kr.open.library.simple_ui.core.unit.permissions.vo

import android.Manifest
import kr.open.library.simple_ui.core.permissions.vo.PermissionSpecialType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for PermissionSpecialType enum
 * Android Manifest 의존성이 있지만 컴파일 타임 상수이므로 Unit Test 가능
 */
class PermissionSpecialTypeUnitTest {

    @Test
    fun allEnumValues_haveCorrectPermissionStrings() {
        assertEquals(Manifest.permission.SYSTEM_ALERT_WINDOW, PermissionSpecialType.SYSTEM_ALERT_WINDOW.permission)
        assertEquals(Manifest.permission.WRITE_SETTINGS, PermissionSpecialType.WRITE_SETTINGS.permission)
        assertEquals(Manifest.permission.PACKAGE_USAGE_STATS, PermissionSpecialType.PACKAGE_USAGE_STATS.permission)
        assertEquals(Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, PermissionSpecialType.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS.permission)
        assertEquals(Manifest.permission.BIND_ACCESSIBILITY_SERVICE, PermissionSpecialType.BIND_ACCESSIBILITY_SERVICE.permission)
        assertEquals(Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE, PermissionSpecialType.BIND_NOTIFICATION_LISTENER_SERVICE.permission)
        assertEquals(Manifest.permission.REQUEST_INSTALL_PACKAGES, PermissionSpecialType.REQUEST_INSTALL_PACKAGES.permission)
        assertEquals(Manifest.permission.ACCESS_NOTIFICATION_POLICY, PermissionSpecialType.ACCESS_NOTIFICATION_POLICY.permission)
        assertEquals(Manifest.permission.MANAGE_EXTERNAL_STORAGE, PermissionSpecialType.MANAGE_EXTERNAL_STORAGE.permission)
        assertEquals(Manifest.permission.SCHEDULE_EXACT_ALARM, PermissionSpecialType.SCHEDULE_EXACT_ALARM.permission)
    }

    @Test
    fun enumEntries_containsAllExpectedValues() {
        val entries = PermissionSpecialType.entries
        assertEquals(10, entries.size)

        assertTrue(entries.contains(PermissionSpecialType.SYSTEM_ALERT_WINDOW))
        assertTrue(entries.contains(PermissionSpecialType.WRITE_SETTINGS))
        assertTrue(entries.contains(PermissionSpecialType.PACKAGE_USAGE_STATS))
        assertTrue(entries.contains(PermissionSpecialType.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS))
        assertTrue(entries.contains(PermissionSpecialType.BIND_ACCESSIBILITY_SERVICE))
        assertTrue(entries.contains(PermissionSpecialType.BIND_NOTIFICATION_LISTENER_SERVICE))
        assertTrue(entries.contains(PermissionSpecialType.REQUEST_INSTALL_PACKAGES))
        assertTrue(entries.contains(PermissionSpecialType.ACCESS_NOTIFICATION_POLICY))
        assertTrue(entries.contains(PermissionSpecialType.MANAGE_EXTERNAL_STORAGE))
        assertTrue(entries.contains(PermissionSpecialType.SCHEDULE_EXACT_ALARM))
    }

    @Test
    fun valueOf_returnsCorrectEnum() {
        assertEquals(PermissionSpecialType.SYSTEM_ALERT_WINDOW, PermissionSpecialType.valueOf("SYSTEM_ALERT_WINDOW"))
        assertEquals(PermissionSpecialType.WRITE_SETTINGS, PermissionSpecialType.valueOf("WRITE_SETTINGS"))
        assertEquals(PermissionSpecialType.SCHEDULE_EXACT_ALARM, PermissionSpecialType.valueOf("SCHEDULE_EXACT_ALARM"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun valueOf_throwsExceptionForInvalidName() {
        PermissionSpecialType.valueOf("INVALID_PERMISSION")
    }

    @Test
    fun enumName_matchesExpectedFormat() {
        assertEquals("SYSTEM_ALERT_WINDOW", PermissionSpecialType.SYSTEM_ALERT_WINDOW.name)
        assertEquals("WRITE_SETTINGS", PermissionSpecialType.WRITE_SETTINGS.name)
        assertEquals("MANAGE_EXTERNAL_STORAGE", PermissionSpecialType.MANAGE_EXTERNAL_STORAGE.name)
    }
}
