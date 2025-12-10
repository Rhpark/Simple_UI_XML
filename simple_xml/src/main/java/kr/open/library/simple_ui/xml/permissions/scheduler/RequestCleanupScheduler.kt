/**
 * Scheduler for cleaning up expired permission requests.<br><br>
 * 만료된 권한 요청을 정리하는 스케줄러입니다.<br>
 */

package kr.open.library.simple_ui.xml.permissions.scheduler

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kr.open.library.simple_ui.xml.permissions.repository.PermissionRequestRepository

/**
 * Internal scheduler that manages periodic cleanup of expired permission requests.<br><br>
 * 만료된 권한 요청의 주기적 정리를 관리하는 내부 스케줄러입니다.<br>
 *
 * This class handles the timing and coordination of cleanup tasks.<br><br>
 * 이 클래스는 정리 작업의 타이밍과 조정을 처리합니다.<br>
 *
 * @param scope CoroutineScope for launching cleanup jobs.<br><br>
 *              정리 Job을 실행할 CoroutineScope입니다.<br>
 * @param mutex Mutex for thread-safe access to shared state.<br><br>
 *              공유 상태에 대한 스레드 안전 접근을 위한 Mutex입니다.<br>
 * @param repository Repository to clean up expired requests from.<br><br>
 *                   만료된 요청을 정리할 저장소입니다.<br>
 * @param onRequestExpired Callback invoked for each expired request ID.<br><br>
 *                         만료된 각 요청 ID에 대해 호출되는 콜백입니다.<br>
 */
internal class RequestCleanupScheduler(
    private val scope: CoroutineScope,
    private val mutex: Mutex,
    private val repository: PermissionRequestRepository,
    private val onRequestExpired: (String) -> Unit,
) {
    // Job for the periodic cleanup task.<br><br>
    // 주기적인 정리 작업을 위한 Job입니다.<br>
    private var cleanupJob: Job? = null

    /**
     * Schedules the cleanup job (default interval: 60 seconds).<br><br>
     * 정리 Job을 예약합니다. 기본 주기는 60초입니다.<br>
     *
     * Cancels any existing cleanup job before scheduling a new one.<br><br>
     * 새 작업을 예약하기 전에 기존 정리 Job을 취소합니다.<br>
     */
    fun scheduleCleanup() {
        cleanupJob?.cancel()

        cleanupJob = scope.launch {
            delay(60_000)
            mutex.withLock { cleanupExpiredRequests() }
        }
    }

    /**
     * Cleans up expired requests and notifies about each expired request.<br><br>
     * 만료된 요청을 정리하고 각 만료된 요청에 대해 알립니다.<br>
     *
     * This method should be called within a mutex lock.<br><br>
     * 이 메서드는 mutex 잠금 내에서 호출되어야 합니다.<br>
     */
    fun cleanupExpiredRequests() {
        // Delegates cleanup to repository and receives expired request IDs.<br><br>
        // Repository에 정리를 위임하고 만료된 요청 ID 목록을 받습니다.<br>
        val expiredRequestIds = repository.cleanupExpiredRequests()

        // Notifies about each expired request for additional cleanup (e.g., delegates).<br><br>
        // 추가 정리(예: delegate)를 위해 각 만료된 요청에 대해 알립니다.<br>
        expiredRequestIds.forEach { requestId -> onRequestExpired(requestId) }

        // Schedule another cleanup pass only when requests remain.<br><br>
        // 요청이 남아 있을 때만 다음 정리 작업을 예약합니다.<br>
        if (repository.getPendingRequestCount() > 0) {
            scheduleCleanup()
        }
    }

    /**
     * Cancels the scheduled cleanup job.<br><br>
     * 예약된 정리 Job을 취소합니다.<br>
     *
     * Useful for testing or shutdown scenarios.<br><br>
     * 테스트나 종료 시나리오에 유용합니다.<br>
     */
    fun cancelCleanup() {
        cleanupJob?.cancel()
        cleanupJob = null
    }
}
