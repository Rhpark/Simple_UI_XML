package kr.open.library.simple_ui.core.robolectric.permissions.classifier

import android.Manifest
import android.app.Application
import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.core.permissions.classifier.PermissionClassifier
import kr.open.library.simple_ui.core.permissions.classifier.PermissionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Robolectric tests for PermissionClassifier.<br><br>
 * API 레벨별 isSupported() 결과와 분류 로직을 검증합니다.<br>
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class PermissionClassifierRobolectricTest {
    private lateinit var context: Context
    private lateinit var classifier: PermissionClassifier

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Application>()
        classifier = PermissionClassifier(context)
    }

    // ==============================================
    // isSupported() — API 28 (P)
    // ==============================================

    /**
     * API 28에서는 R/S/Tiramisu 전용 권한이 모두 미지원이어야 한다.<br><br>
     * setOf() 상수가 아닌 buildSet+checkSdkVersion 방식이었다면 집합이 비어 else→true로 빠져 버그가 발생한다.
     */
    @Test
    @Config(sdk = [Build.VERSION_CODES.P]) // API 28
    fun isSupported_androidR_permission_returnsFalse_onApi28() {
        assertFalse(classifier.isSupported(Manifest.permission.MANAGE_EXTERNAL_STORAGE))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P]) // API 28
    fun isSupported_androidS_permission_returnsFalse_onApi28() {
        assertFalse(classifier.isSupported(Manifest.permission.SCHEDULE_EXACT_ALARM))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P]) // API 28
    fun isSupported_androidTiramisu_permission_returnsFalse_onApi28() {
        assertFalse(classifier.isSupported(Manifest.permission.POST_NOTIFICATIONS))
    }

    // ==============================================
    // isSupported() — API 29 (Q)
    // ==============================================

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q]) // API 29
    fun isSupported_androidR_permission_returnsFalse_onApi29() {
        assertFalse(classifier.isSupported(Manifest.permission.MANAGE_EXTERNAL_STORAGE))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q]) // API 29
    fun isSupported_androidS_permission_returnsFalse_onApi29() {
        assertFalse(classifier.isSupported(Manifest.permission.SCHEDULE_EXACT_ALARM))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q]) // API 29
    fun isSupported_androidTiramisu_permission_returnsFalse_onApi29() {
        assertFalse(classifier.isSupported(Manifest.permission.POST_NOTIFICATIONS))
    }

    // ==============================================
    // isSupported() — API 30 (R)
    // ==============================================

    @Test
    @Config(sdk = [Build.VERSION_CODES.R]) // API 30
    fun isSupported_androidR_permission_returnsTrue_onApi30() {
        assertTrue(classifier.isSupported(Manifest.permission.MANAGE_EXTERNAL_STORAGE))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R]) // API 30
    fun isSupported_androidS_permission_returnsFalse_onApi30() {
        assertFalse(classifier.isSupported(Manifest.permission.SCHEDULE_EXACT_ALARM))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R]) // API 30
    fun isSupported_androidTiramisu_permission_returnsFalse_onApi30() {
        assertFalse(classifier.isSupported(Manifest.permission.POST_NOTIFICATIONS))
    }

    // ==============================================
    // isSupported() — API 31 (S)
    // ==============================================

    @Test
    @Config(sdk = [Build.VERSION_CODES.S]) // API 31
    fun isSupported_androidR_permission_returnsTrue_onApi31() {
        assertTrue(classifier.isSupported(Manifest.permission.MANAGE_EXTERNAL_STORAGE))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S]) // API 31
    fun isSupported_androidS_permission_returnsTrue_onApi31() {
        assertTrue(classifier.isSupported(Manifest.permission.SCHEDULE_EXACT_ALARM))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S]) // API 31
    fun isSupported_androidTiramisu_permission_returnsFalse_onApi31() {
        assertFalse(classifier.isSupported(Manifest.permission.POST_NOTIFICATIONS))
    }

    // ==============================================
    // isSupported() — API 32 (S_V2)
    // ==============================================

    @Test
    @Config(sdk = [Build.VERSION_CODES.S_V2]) // API 32
    fun isSupported_androidR_permission_returnsTrue_onApi32() {
        assertTrue(classifier.isSupported(Manifest.permission.MANAGE_EXTERNAL_STORAGE))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S_V2]) // API 32
    fun isSupported_androidS_permission_returnsTrue_onApi32() {
        assertTrue(classifier.isSupported(Manifest.permission.SCHEDULE_EXACT_ALARM))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S_V2]) // API 32
    fun isSupported_androidTiramisu_permission_returnsFalse_onApi32() {
        assertFalse(classifier.isSupported(Manifest.permission.POST_NOTIFICATIONS))
    }

    // ==============================================
    // isSupported() — API 33 (Tiramisu)
    // ==============================================

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU]) // API 33
    fun isSupported_androidR_permission_returnsTrue_onApi33() {
        assertTrue(classifier.isSupported(Manifest.permission.MANAGE_EXTERNAL_STORAGE))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU]) // API 33
    fun isSupported_androidS_permission_returnsTrue_onApi33() {
        assertTrue(classifier.isSupported(Manifest.permission.SCHEDULE_EXACT_ALARM))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU]) // API 33
    fun isSupported_androidTiramisu_permission_returnsTrue_onApi33() {
        assertTrue(classifier.isSupported(Manifest.permission.POST_NOTIFICATIONS))
    }

    // ==============================================
    // isSupported() — MANAGE_MEDIA 항상 false
    // ==============================================

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun isSupported_manageMedia_alwaysReturnsFalse() {
        assertFalse(classifier.isSupported(Manifest.permission.MANAGE_MEDIA))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun isSupported_manageMedia_returnsFalse_onApi28() {
        assertFalse(classifier.isSupported(Manifest.permission.MANAGE_MEDIA))
    }

    // ==============================================
    // isSupported() — 일반 런타임 권한은 항상 true
    // ==============================================

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun isSupported_runtimePermission_alwaysReturnsTrue_onApi28() {
        assertTrue(classifier.isSupported(Manifest.permission.CAMERA))
        assertTrue(classifier.isSupported(Manifest.permission.RECORD_AUDIO))
        assertTrue(classifier.isSupported(Manifest.permission.ACCESS_FINE_LOCATION))
    }

    // ==============================================
    // classify() — 분류 위임 검증
    // ==============================================

    @Test
    fun classify_rolePermission_returnsRole() {
        assertEquals(PermissionType.ROLE, classifier.classify("android.app.role.DIALER"))
    }

    @Test
    fun classify_specialPermission_returnsSpecial() {
        assertEquals(PermissionType.SPECIAL, classifier.classify(Manifest.permission.SYSTEM_ALERT_WINDOW))
    }

    @Test
    fun classify_runtimePermission_returnsRuntime() {
        assertEquals(PermissionType.RUNTIME, classifier.classify(Manifest.permission.CAMERA))
    }

    // ==============================================
    // isInvalid() — 유효성 검증
    // ==============================================

    @Test
    fun isInvalid_emptyString_returnsTrue() {
        assertTrue(classifier.isInvalid(""))
    }

    @Test
    fun isInvalid_rolePermission_returnsFalse() {
        assertFalse(classifier.isInvalid("android.app.role.DIALER"))
    }

    @Test
    fun isInvalid_specialAppAccess_returnsFalse() {
        assertFalse(classifier.isInvalid(Manifest.permission.BIND_ACCESSIBILITY_SERVICE))
        assertFalse(classifier.isInvalid(Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE))
    }
}
