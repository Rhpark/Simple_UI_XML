package kr.open.library.simple_ui.core.robolectric.permissions.handler

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.provider.Settings
import kr.open.library.simple_ui.core.permissions.handler.SpecialPermissionHandler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Robolectric tests for special-permission grant checks and Settings Intent mapping.<br><br>
 * 특수 권한의 허용 확인과 Settings Intent 매핑을 검증하는 Robolectric 테스트입니다.<br>
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class SpecialPermissionHandlerRobolectricTest {
    /**
     * Verifies that MANAGE_MEDIA is always denied by the ordinary-app policy without accessing Context.<br><br>
     * 일반 앱 정책상 MANAGE_MEDIA를 Context 접근 없이 항상 미허용으로 처리하는지 검증합니다.<br>
     */
    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun isGranted_returnsFalseForManageMedia_withoutContextAccess() {
        val context = mock(Context::class.java)
        val handler = SpecialPermissionHandler(context)

        assertFalse(handler.isGranted(Manifest.permission.MANAGE_MEDIA))
        verifyNoInteractions(context)
    }

    /**
     * Verifies that other special permissions delegate their grant state to the common permission checker.<br><br>
     * 다른 특수 권한의 허용 상태를 공통 권한 검사기에 위임하는지 검증합니다.<br>
     */
    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun isGranted_delegatesToCommonPermissionCheck_forMappedPermission() {
        val context = mock(Context::class.java)
        val notificationManager = mock(NotificationManager::class.java)
        `when`(context.getSystemService(Context.NOTIFICATION_SERVICE)).thenReturn(notificationManager)
        `when`(notificationManager.isNotificationPolicyAccessGranted).thenReturn(true)
        val handler = SpecialPermissionHandler(context)

        assertTrue(handler.isGranted(Manifest.permission.ACCESS_NOTIFICATION_POLICY))
    }

    /**
     * Verifies that a package-scoped Settings action contains the application package URI.<br><br>
     * 패키지 범위 Settings Action에 애플리케이션 package URI가 포함되는지 검증합니다.<br>
     */
    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun buildSettingsIntent_addsPackageUri_whenPermissionRequiresIt() {
        val context = mock(Context::class.java)
        `when`(context.packageName).thenReturn(PACKAGE_NAME)
        val handler = SpecialPermissionHandler(context)

        val intent = handler.buildSettingsIntent(Manifest.permission.SYSTEM_ALERT_WINDOW)

        assertEquals(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, intent?.action)
        assertEquals("package:$PACKAGE_NAME", intent?.data?.toString())
    }

    /**
     * Verifies that a global Settings action does not receive an unnecessary package URI.<br><br>
     * 전역 Settings Action에 불필요한 package URI를 추가하지 않는지 검증합니다.<br>
     */
    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun buildSettingsIntent_omitsPackageUri_whenPermissionDoesNotRequireIt() {
        val context = mock(Context::class.java)
        val handler = SpecialPermissionHandler(context)

        val intent = handler.buildSettingsIntent(Manifest.permission.PACKAGE_USAGE_STATS)

        assertEquals(Settings.ACTION_USAGE_ACCESS_SETTINGS, intent?.action)
        assertNull(intent?.data)
    }

    /**
     * Verifies that MANAGE_MEDIA produces its Settings action only on Android S and above.<br><br>
     * MANAGE_MEDIA가 Android S 이상에서만 Settings Action을 생성하는지 검증합니다.<br>
     */
    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun buildSettingsIntent_returnsManageMediaIntent_onAndroidSAndAbove() {
        val handler = SpecialPermissionHandler(mock(Context::class.java))

        val intent = handler.buildSettingsIntent(Manifest.permission.MANAGE_MEDIA)

        assertEquals(Settings.ACTION_REQUEST_MANAGE_MEDIA, intent?.action)
        assertNull(intent?.data)
    }

    /**
     * Verifies that unsupported SDK and unmapped permission inputs return null.<br><br>
     * 미지원 SDK와 매핑되지 않은 권한 입력이 null을 반환하는지 검증합니다.<br>
     */
    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun buildSettingsIntent_returnsNull_forUnsupportedOrUnmappedPermission() {
        val handler = SpecialPermissionHandler(mock(Context::class.java))

        assertNull(handler.buildSettingsIntent(Manifest.permission.MANAGE_MEDIA))
        assertNull(handler.buildSettingsIntent("com.example.permission.UNKNOWN"))
    }

    private companion object {
        const val PACKAGE_NAME = "com.example.permission.test"
    }
}
