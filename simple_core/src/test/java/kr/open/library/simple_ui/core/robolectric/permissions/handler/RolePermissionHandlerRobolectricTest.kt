package kr.open.library.simple_ui.core.robolectric.permissions.handler

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import kr.open.library.simple_ui.core.permissions.handler.RolePermissionHandler
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Robolectric tests for RoleManager availability, held-state, and request Intent handling.<br><br>
 * RoleManager의 가용성·보유 상태·요청 Intent 처리를 검증하는 Robolectric 테스트입니다.<br>
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class RolePermissionHandlerRobolectricTest {
    /**
     * Verifies that every Role API returns an unsupported result below Android Q without accessing Context.<br><br>
     * Android Q 미만에서 Context에 접근하지 않고 모든 Role API가 미지원 결과를 반환하는지 검증합니다.<br>
     */
    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun roleApis_returnUnsupportedResults_belowAndroidQ() {
        val context = mock(Context::class.java)
        val handler = RolePermissionHandler(context)

        assertFalse(handler.isRoleAvailable(ROLE_NAME))
        assertFalse(handler.isRoleHeld(ROLE_NAME))
        assertNull(handler.createRequestIntent(ROLE_NAME))
        verifyNoInteractions(context)
    }

    /**
     * Verifies that Role API results are delegated to RoleManager on supported SDK levels.<br><br>
     * 지원 SDK에서 Role API 결과를 RoleManager에 위임하는지 검증합니다.<br>
     */
    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun roleApis_delegateToRoleManager_onAndroidQAndAbove() {
        val context = mock(Context::class.java)
        val roleManager = mock(RoleManager::class.java)
        val requestIntent = Intent("test.role.REQUEST")
        `when`(context.getSystemService(RoleManager::class.java)).thenReturn(roleManager)
        `when`(roleManager.isRoleAvailable(ROLE_NAME)).thenReturn(true)
        `when`(roleManager.isRoleHeld(ROLE_NAME)).thenReturn(true)
        `when`(roleManager.createRequestRoleIntent(ROLE_NAME)).thenReturn(requestIntent)
        val handler = RolePermissionHandler(context)

        assertTrue(handler.isRoleAvailable(ROLE_NAME))
        assertTrue(handler.isRoleHeld(ROLE_NAME))
        assertSame(requestIntent, handler.createRequestIntent(ROLE_NAME))
    }

    /**
     * Verifies that a missing RoleManager service produces safe default results.<br><br>
     * RoleManager 서비스를 얻지 못했을 때 안전한 기본 결과를 반환하는지 검증합니다.<br>
     */
    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun roleApis_returnSafeDefaults_whenRoleManagerIsUnavailable() {
        val context = mock(Context::class.java)
        `when`(context.getSystemService(RoleManager::class.java)).thenReturn(null)
        val handler = RolePermissionHandler(context)

        assertFalse(handler.isRoleAvailable(ROLE_NAME))
        assertFalse(handler.isRoleHeld(ROLE_NAME))
        assertNull(handler.createRequestIntent(ROLE_NAME))
    }

    /**
     * Verifies that platform exceptions are converted to safe default results.<br><br>
     * 플랫폼 예외를 안전한 기본 결과로 변환하는지 검증합니다.<br>
     */
    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun roleApis_returnSafeDefaults_whenRoleManagerThrows() {
        val context = mock(Context::class.java)
        val roleManager = mock(RoleManager::class.java)
        `when`(context.getSystemService(RoleManager::class.java)).thenReturn(roleManager)
        `when`(roleManager.isRoleAvailable(ROLE_NAME)).thenThrow(SecurityException("unavailable"))
        `when`(roleManager.isRoleHeld(ROLE_NAME)).thenThrow(SecurityException("unavailable"))
        `when`(roleManager.createRequestRoleIntent(ROLE_NAME)).thenThrow(SecurityException("unavailable"))
        val handler = RolePermissionHandler(context)

        assertFalse(handler.isRoleAvailable(ROLE_NAME))
        assertFalse(handler.isRoleHeld(ROLE_NAME))
        assertNull(handler.createRequestIntent(ROLE_NAME))
    }

    private companion object {
        const val ROLE_NAME = "android.app.role.TEST"
    }
}
