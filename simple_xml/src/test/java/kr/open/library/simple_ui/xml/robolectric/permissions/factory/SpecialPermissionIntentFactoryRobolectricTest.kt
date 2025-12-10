/**
 * Robolectric tests for SpecialPermissionIntentFactory.<br><br>
 * SpecialPermissionIntentFactory의 Robolectric 테스트입니다.<br>
 */

package kr.open.library.simple_ui.xml.robolectric.permissions.factory

import android.Manifest
import android.app.Activity
import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.xml.extensions.resource.isContextDestroyed
import kr.open.library.simple_ui.xml.permissions.factory.SpecialPermissionIntentFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class SpecialPermissionIntentFactoryRobolectricTest {
    private lateinit var context: Context
    private lateinit var activity: Activity

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
    }

    // ==================== Special Permission Intent Tests ====================

    @Test
    fun createSpecialPermissionIntent_withSystemAlertWindow_returnsIntentWithPackageUri() {
        // Given
        val permission = Manifest.permission.SYSTEM_ALERT_WINDOW

        // When
        val intent = SpecialPermissionIntentFactory.createSpecialPermissionIntent(context, permission)

        // Then
        assertNotNull(intent)
        assertEquals(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, intent?.action)
        assertNotNull(intent?.data)
        assertTrue(intent?.data.toString().contains("package:"))
    }

    @Test
    fun createSpecialPermissionIntent_withWriteSettings_returnsIntentWithPackageUri() {
        // Given
        val permission = Manifest.permission.WRITE_SETTINGS

        // When
        val intent = SpecialPermissionIntentFactory.createSpecialPermissionIntent(context, permission)

        // Then
        assertNotNull(intent)
        assertEquals(Settings.ACTION_MANAGE_WRITE_SETTINGS, intent?.action)
        assertNotNull(intent?.data)
        assertTrue(intent?.data.toString().contains("package:"))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun createSpecialPermissionIntent_withManageExternalStorage_returnsIntentWithPackageUri() {
        // Given
        val permission = Manifest.permission.MANAGE_EXTERNAL_STORAGE

        // When
        val intent = SpecialPermissionIntentFactory.createSpecialPermissionIntent(context, permission)

        // Then
        assertNotNull(intent)
        assertEquals(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, intent?.action)
        assertNotNull(intent?.data)
        assertTrue(intent?.data.toString().contains("package:"))
    }

    // Note: SCHEDULE_EXACT_ALARM requires API S+
    // This test is removed because PermissionConstants.SPECIAL_PERMISSION_ACTIONS
    // is initialized at class load time, and @Config cannot affect static initialization

    // Note: POST_NOTIFICATIONS is a normal runtime permission in API 33+, not a special permission

    @Test
    fun createSpecialPermissionIntent_withUnknownPermission_returnsNull() {
        // Given
        val permission = "android.permission.UNKNOWN_PERMISSION"

        // When
        val intent = SpecialPermissionIntentFactory.createSpecialPermissionIntent(context, permission)

        // Then
        assertNull(intent)
    }

    @Test
    fun createSpecialPermissionIntent_withNormalPermission_returnsNull() {
        // Given
        val permission = Manifest.permission.CAMERA

        // When
        val intent = SpecialPermissionIntentFactory.createSpecialPermissionIntent(context, permission)

        // Then
        assertNull(intent)
    }

    @Test
    fun createSpecialPermissionIntent_withEmptyPermission_returnsNull() {
        // Given
        val permission = ""

        // When
        val intent = SpecialPermissionIntentFactory.createSpecialPermissionIntent(context, permission)

        // Then
        assertNull(intent)
    }

    // ==================== API R Permissions Tests ====================

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun createSpecialPermissionIntent_withManageExternalStorageOnApiQ_returnsNull() {
        // Given - API level below R
        val permission = Manifest.permission.MANAGE_EXTERNAL_STORAGE

        // When
        val intent = SpecialPermissionIntentFactory.createSpecialPermissionIntent(context, permission)

        // Then
        assertNull(intent)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun createSpecialPermissionIntent_withManageExternalStorageOnApiR_returnsIntent() {
        // Given - API level R
        val permission = Manifest.permission.MANAGE_EXTERNAL_STORAGE

        // When
        val intent = SpecialPermissionIntentFactory.createSpecialPermissionIntent(context, permission)

        // Then
        assertNotNull(intent)
        assertEquals(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, intent?.action)
    }

    // ==================== API S Permissions Tests ====================

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun createSpecialPermissionIntent_withScheduleExactAlarmOnApiR_returnsNull() {
        // Given - API level below S
        val permission = Manifest.permission.SCHEDULE_EXACT_ALARM

        // When
        val intent = SpecialPermissionIntentFactory.createSpecialPermissionIntent(context, permission)

        // Then
        assertNull(intent)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun createSpecialPermissionIntent_withScheduleExactAlarmOnApiS_returnsIntent() {
        // Given - API level S
        val permission = Manifest.permission.SCHEDULE_EXACT_ALARM

        // When
        val intent = SpecialPermissionIntentFactory.createSpecialPermissionIntent(context, permission)

        // Then
        assertNotNull(intent)
        assertEquals(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, intent?.action)
    }

    // Note: API Tiramisu permissions tests for special permissions only
    // POST_NOTIFICATIONS is not a special permission

    // ==================== Context Destroyed Tests ====================

    @Test
    fun isContextDestroyed_withApplicationContext_returnsFalse() {
        // Given
        val context = ApplicationProvider.getApplicationContext<Context>()

        // When
        val isDestroyed = context.isContextDestroyed()

        // Then
        assertFalse(isDestroyed)
    }

    @Test
    fun isContextDestroyed_withCreatedActivity_returnsFalse() {
        // Given
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()

        // When
        val isDestroyed = activity.isContextDestroyed()

        // Then
        assertFalse(isDestroyed)
    }

    @Test
    fun isContextDestroyed_withDestroyedActivity_returnsTrue() {
        // Given
        val activity =
            Robolectric
                .buildActivity(Activity::class.java)
                .create()
                .destroy()
                .get()

        // When
        val isDestroyed = activity.isContextDestroyed()

        // Then
        assertTrue(isDestroyed)
    }

    @Test
    fun isContextDestroyed_withFinishingActivity_returnsTrue() {
        // Given
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.finish()

        // When
        val isDestroyed = activity.isContextDestroyed()

        // Then
        assertTrue(isDestroyed)
    }

    // ==================== Intent Data Validation Tests ====================

    @Test
    fun createSpecialPermissionIntent_packageUriContainsCorrectPackageName() {
        // Given
        val permission = Manifest.permission.SYSTEM_ALERT_WINDOW

        // When
        val intent = SpecialPermissionIntentFactory.createSpecialPermissionIntent(context, permission)

        // Then
        assertNotNull(intent)
        val packageName = context.packageName
        assertEquals("package:$packageName", intent?.data.toString())
    }

    // Note: SCHEDULE_EXACT_ALARM test removed for same reason as above
}
