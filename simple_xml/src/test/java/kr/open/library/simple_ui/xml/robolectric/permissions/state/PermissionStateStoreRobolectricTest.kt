package kr.open.library.simple_ui.xml.robolectric.permissions.state

import android.os.Build
import android.os.Bundle
import kr.open.library.simple_ui.core.permissions.model.OrphanedDeniedRequestResult
import kr.open.library.simple_ui.core.permissions.model.PermissionDecisionType
import kr.open.library.simple_ui.core.permissions.model.PermissionDeniedItem
import kr.open.library.simple_ui.core.permissions.model.PermissionDeniedType
import kr.open.library.simple_ui.xml.permissions.state.PermissionStateSnapshot
import kr.open.library.simple_ui.xml.permissions.state.PermissionStateStore
import kr.open.library.simple_ui.xml.permissions.state.RequestState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Robolectric tests for permission state Bundle persistence.<br><br>
 * 권한 상태의 Bundle 저장·복원을 검증하는 Robolectric 테스트입니다.<br>
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class PermissionStateStoreRobolectricTest {
    /**
     * Verifies full snapshot round-trip through typed Parcelable APIs on API 33.<br><br>
     * API 33의 타입 지정 Parcelable API로 전체 스냅샷이 왕복 복원되는지 검증합니다.<br>
     */
    @Test
    fun saveAndRestore_preservesEntireSnapshot_onApi33() {
        assertSnapshotRoundTrip()
    }

    /**
     * Verifies full snapshot round-trip through legacy Parcelable APIs on API 28.<br><br>
     * API 28의 기존 Parcelable API로 전체 스냅샷이 왕복 복원되는지 검증합니다.<br>
     */
    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun saveAndRestore_preservesEntireSnapshot_onApi28() {
        assertSnapshotRoundTrip()
    }

    /**
     * Verifies that null restoration clears values without replacing the shared snapshot.<br><br>
     * null 복원이 공유 스냅샷을 교체하지 않고 기존 값을 초기화하는지 검증합니다.<br>
     */
    @Test
    fun restoreState_withNull_clearsValuesAndPreservesSnapshotReference() {
        val store = PermissionStateStore()
        val originalSnapshot = store.getSnapshot()
        store.update { snapshot ->
            snapshot.requestQueue.add("stale-request")
            snapshot.requestStates["stale-request"] = RequestState(
                requestId = "stale-request",
                permissions = listOf(CAMERA_PERMISSION),
                results = mapOf(CAMERA_PERMISSION to PermissionDecisionType.DENIED),
            )
            snapshot.requestedHistory.add(CAMERA_PERMISSION)
            snapshot.orphanedResults.add(
                OrphanedDeniedRequestResult(
                    requestId = "stale-request",
                    deniedResults = listOf(
                        PermissionDeniedItem(CAMERA_PERMISSION, PermissionDeniedType.DENIED),
                    ),
                ),
            )
        }

        store.restoreState(null)

        assertSame(originalSnapshot, store.getSnapshot())
        assertTrue(originalSnapshot.requestQueue.isEmpty())
        assertTrue(originalSnapshot.requestStates.isEmpty())
        assertTrue(originalSnapshot.requestedHistory.isEmpty())
        assertTrue(originalSnapshot.orphanedResults.isEmpty())
    }

    private fun assertSnapshotRoundTrip() {
        val sourceStore = PermissionStateStore()
        val sourceSnapshot = createSnapshot()
        sourceStore.update { snapshot ->
            snapshot.requestQueue.addAll(sourceSnapshot.requestQueue)
            snapshot.requestStates.putAll(sourceSnapshot.requestStates)
            snapshot.requestedHistory.addAll(sourceSnapshot.requestedHistory)
            snapshot.orphanedResults.addAll(sourceSnapshot.orphanedResults)
        }
        val outState = Bundle()

        sourceStore.saveState(outState)
        val restoredStore = PermissionStateStore()
        val originalRestoredSnapshot = restoredStore.getSnapshot()
        restoredStore.restoreState(outState)

        assertSame(originalRestoredSnapshot, restoredStore.getSnapshot())
        assertEquals(sourceSnapshot, restoredStore.getSnapshot())
    }

    private fun createSnapshot(): PermissionStateSnapshot = PermissionStateSnapshot(
        requestQueue = mutableListOf("request-camera", "request-audio"),
        requestStates = mutableMapOf(
            "request-camera" to RequestState(
                requestId = "request-camera",
                permissions = listOf(CAMERA_PERMISSION, AUDIO_PERMISSION),
                results = mapOf(
                    CAMERA_PERMISSION to PermissionDecisionType.GRANTED,
                    AUDIO_PERMISSION to PermissionDecisionType.PERMANENTLY_DENIED,
                ),
            ),
            "request-audio" to RequestState(
                requestId = "request-audio",
                permissions = listOf(AUDIO_PERMISSION),
                results = mapOf(AUDIO_PERMISSION to PermissionDecisionType.DENIED),
            ),
        ),
        requestedHistory = mutableSetOf(CAMERA_PERMISSION, AUDIO_PERMISSION),
        orphanedResults = mutableListOf(
            OrphanedDeniedRequestResult(
                requestId = "orphaned-request",
                deniedResults = listOf(
                    PermissionDeniedItem(CAMERA_PERMISSION, PermissionDeniedType.DENIED),
                    PermissionDeniedItem(AUDIO_PERMISSION, PermissionDeniedType.LIFECYCLE_NOT_READY),
                ),
            ),
        ),
    )

    private companion object {
        const val CAMERA_PERMISSION = "android.permission.CAMERA"
        const val AUDIO_PERMISSION = "android.permission.RECORD_AUDIO"
    }
}
