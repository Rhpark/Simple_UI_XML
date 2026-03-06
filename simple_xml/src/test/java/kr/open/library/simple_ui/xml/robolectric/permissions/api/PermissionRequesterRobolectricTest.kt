package kr.open.library.simple_ui.xml.robolectric.permissions.api

import androidx.activity.ComponentActivity
import kr.open.library.simple_ui.core.permissions.model.PermissionDeniedItem
import kr.open.library.simple_ui.core.permissions.model.PermissionDeniedType
import kr.open.library.simple_ui.xml.permissions.api.PermissionRequester
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

/**
 * Robolectric tests for PermissionRequester empty-request behavior.<br><br>
 * PermissionRequester의 빈 요청 처리 동작을 검증하는 Robolectric 테스트입니다.<br>
 */
@RunWith(RobolectricTestRunner::class)
class PermissionRequesterRobolectricTest {
    /**
     * Verifies that an empty request returns EMPTY_REQUEST with empty permission.<br><br>
     * 빈 요청이 EMPTY_REQUEST와 빈 권한으로 반환되는지 검증합니다.<br>
     */
    @Test
    fun emptyRequestShouldReturnEmptyRequest() {
        /* Activity controller for test lifecycle.<br><br>
         * 테스트 라이프사이클용 Activity 컨트롤러입니다.<br>
         */
        val activityController = Robolectric.buildActivity(ComponentActivity::class.java).create()

        /* Activity instance used as requester host.<br><br>
         * 요청 호스트로 사용하는 Activity 인스턴스입니다.<br>
         */
        val activity = activityController.get()

        /* PermissionRequester instance under test.<br><br>
         * 테스트 대상 PermissionRequester 인스턴스입니다.<br>
         */
        val requester = PermissionRequester(activity)

        /* Captured denied result list.<br><br>
         * 캡처한 거부 결과 목록입니다.<br>
         */
        var results: List<PermissionDeniedItem>? = null

        requester.requestPermissions(
            permissions = emptyList(),
            onDeniedResult = { deniedItems -> results = deniedItems },
        )

        assertNotNull(results)
        assertEquals(1, results?.size)
        assertEquals("", results?.first()?.permission)
        assertEquals(PermissionDeniedType.EMPTY_REQUEST, results?.first()?.result)
    }
}
