package kr.open.library.simple_ui.xml.robolectric.permissions.register

import kr.open.library.simple_ui.core.permissions.model.PermissionDeniedItem
import kr.open.library.simple_ui.core.permissions.model.PermissionRationaleRequest
import kr.open.library.simple_ui.core.permissions.model.PermissionSettingsRequest
import kr.open.library.simple_ui.xml.permissions.register.PermissionRequestInterface
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Robolectric tests for PermissionRequestInterface contract.<br><br>
 * PermissionRequestInterface 계약을 검증하는 Robolectric 테스트입니다.<br>
 */
@RunWith(RobolectricTestRunner::class)
class PermissionRequestInterfaceRobolectricTest {
    /**
     * Test implementation that records the last call parameters.<br><br>
     * 마지막 호출 파라미터를 기록하는 테스트 구현체입니다.<br>
     */
    private class TestRequester : PermissionRequestInterface {
        /**
         * Last requested permissions.<br><br>
         * 마지막 요청 권한 목록입니다.<br>
         */
        var lastPermissions: List<String>? = null

        /**
         * Last denied result callback reference.<br><br>
         * 마지막 거부 결과 콜백 참조입니다.<br>
         */
        var lastOnDeniedResult: ((List<PermissionDeniedItem>) -> Unit)? = null

        /**
         * Last rationale callback reference.<br><br>
         * 마지막 rationale 콜백 참조입니다.<br>
         */
        var lastOnRationaleNeeded: ((PermissionRationaleRequest) -> Unit)? = null

        /**
         * Last settings navigation callback reference.<br><br>
         * 마지막 설정 이동 콜백 참조입니다.<br>
         */
        var lastOnNavigateToSettings: ((PermissionSettingsRequest) -> Unit)? = null

        /**
         * Number of times requestPermissions was called.<br><br>
         * requestPermissions 호출 횟수입니다.<br>
         */
        var callCount: Int = 0

        /**
         * Records the call parameters for verification.<br><br>
         * 검증을 위해 호출 파라미터를 기록합니다.<br>
         *
         * @param permissions Requested permissions list.<br><br>
         *                    요청된 권한 목록입니다.<br>
         * @param onDeniedResult Callback for denied results.<br><br>
         *                       거부 결과 콜백입니다.<br>
         * @param onRationaleNeeded Callback for rationale requests.<br><br>
         *                          rationale 요청 콜백입니다.<br>
         * @param onNavigateToSettings Callback for settings navigation.<br><br>
         *                             설정 이동 콜백입니다.<br>
         */
        private fun recordCall(
            permissions: List<String>,
            onDeniedResult: (List<PermissionDeniedItem>) -> Unit,
            onRationaleNeeded: ((PermissionRationaleRequest) -> Unit)?,
            onNavigateToSettings: ((PermissionSettingsRequest) -> Unit)?,
        ) {
            lastPermissions = permissions
            lastOnDeniedResult = onDeniedResult
            lastOnRationaleNeeded = onRationaleNeeded
            lastOnNavigateToSettings = onNavigateToSettings
            callCount++
        }

        /**
         * Records the request with rationale/settings hooks.<br><br>
         * rationale/settings 훅을 포함한 요청을 기록합니다.<br>
         *
         * @param permissions Requested permissions list.<br><br>
         *                    요청된 권한 목록입니다.<br>
         * @param onDeniedResult Callback for denied results.<br><br>
         *                       거부 결과 콜백입니다.<br>
         * @param onRationaleNeeded Callback for rationale requests.<br><br>
         *                          rationale 요청 콜백입니다.<br>
         * @param onNavigateToSettings Callback for settings navigation.<br><br>
         *                             설정 이동 콜백입니다.<br>
         */
        override fun requestPermissions(
            permissions: List<String>,
            onDeniedResult: (List<PermissionDeniedItem>) -> Unit,
            onRationaleNeeded: ((PermissionRationaleRequest) -> Unit)?,
            onNavigateToSettings: ((PermissionSettingsRequest) -> Unit)?,
        ) {
            recordCall(
                permissions = permissions,
                onDeniedResult = onDeniedResult,
                onRationaleNeeded = onRationaleNeeded,
                onNavigateToSettings = onNavigateToSettings,
            )
        }

        /**
         * Records the request without rationale/settings hooks.<br><br>
         * rationale/settings 훅 없이 요청을 기록합니다.<br>
         *
         * @param permissions Requested permissions list.<br><br>
         *                    요청된 권한 목록입니다.<br>
         * @param onDeniedResult Callback for denied results.<br><br>
         *                       거부 결과 콜백입니다.<br>
         */
        override fun requestPermissions(
            permissions: List<String>,
            onDeniedResult: (List<PermissionDeniedItem>) -> Unit,
        ) {
            recordCall(
                permissions = permissions,
                onDeniedResult = onDeniedResult,
                onRationaleNeeded = null,
                onNavigateToSettings = null,
            )
        }
    }

    /**
     * Verifies that the interface can be implemented.<br><br>
     * 인터페이스 구현 가능 여부를 검증합니다.<br>
     */
    @Test
    fun permissionRequestInterface_canBeImplemented() {
        /**
         * Test requester instance.<br><br>
         * 테스트 요청기 인스턴스입니다.<br>
         */
        val requester = TestRequester()

        assertNotNull(requester)
        assertTrue(PermissionRequestInterface::class.java.isAssignableFrom(requester::class.java))
    }

    /**
     * Verifies that the hook-capable overload captures parameters.<br><br>
     * 훅을 포함한 오버로드가 파라미터를 캡처하는지 검증합니다.<br>
     */
    @Test
    fun requestPermissionsWithHooks_capturesArguments() {
        /**
         * Test requester instance.<br><br>
         * 테스트 요청기 인스턴스입니다.<br>
         */
        val requester = TestRequester()

        /**
         * Permissions to request.<br><br>
         * 요청할 권한 목록입니다.<br>
         */
        val permissions = listOf("android.permission.CAMERA")

        requester.requestPermissions(
            permissions = permissions,
            onDeniedResult = { },
            onRationaleNeeded = { },
            onNavigateToSettings = { },
        )

        assertEquals(permissions, requester.lastPermissions)
        assertNotNull(requester.lastOnDeniedResult)
        assertNotNull(requester.lastOnRationaleNeeded)
        assertNotNull(requester.lastOnNavigateToSettings)
        assertEquals(1, requester.callCount)
    }

    /**
     * Verifies that the simple overload captures parameters without hooks.<br><br>
     * 훅 없는 오버로드가 파라미터를 캡처하는지 검증합니다.<br>
     */
    @Test
    fun requestPermissionsWithoutHooks_capturesArguments() {
        /**
         * Test requester instance.<br><br>
         * 테스트 요청기 인스턴스입니다.<br>
         */
        val requester = TestRequester()

        /**
         * Permissions to request.<br><br>
         * 요청할 권한 목록입니다.<br>
         */
        val permissions = listOf("android.permission.RECORD_AUDIO")

        requester.requestPermissions(
            permissions = permissions,
            onDeniedResult = { },
        )

        assertEquals(permissions, requester.lastPermissions)
        assertNotNull(requester.lastOnDeniedResult)
        assertEquals(1, requester.callCount)
    }
}
