package kr.open.library.simple_ui.core.permissions.queue

/**
 * Maintains permission request IDs in FIFO order.<br><br>
 * 권한 요청 ID를 FIFO 순서로 관리합니다.<br>
 *
 * @param backingQueue The mutable list used as the backing store.<br><br>
 *                     내부 저장소로 사용하는 가변 리스트입니다.<br>
 */
class PermissionQueue(
    private val backingQueue: MutableList<String>,
) {
    /**
     * Enqueues [requestId] when it is not already present.<br><br>
     * [requestId]가 이미 없을 때만 큐에 추가합니다.<br>
     *
     * @param requestId The request identifier to enqueue.<br><br>
     *                  큐에 추가할 요청 식별자입니다.<br>
     */
    fun enqueue(requestId: String) {
        if (backingQueue.contains(requestId)) return
        backingQueue.add(requestId)
    }

    /**
     * Returns the next request ID without removing it.<br><br>
     * 다음 요청 ID를 제거하지 않고 반환합니다.<br>
     *
     * @return Return value: next request ID or null when empty. Log behavior: none.<br><br>
     *         반환값: 비어 있으면 null, 아니면 다음 요청 ID. 로그 동작: 없음.<br>
     */
    fun peek(): String? = backingQueue.firstOrNull()

    /**
     * Removes [requestId] from the queue.<br><br>
     * 큐에서 [requestId]를 제거합니다.<br>
     *
     * @param requestId The request identifier to remove.<br><br>
     *                  제거할 요청 식별자입니다.<br>
     */
    fun remove(requestId: String) {
        backingQueue.remove(requestId)
    }

    /**
     * Clears all queued request IDs.<br><br>
     * 큐에 있는 모든 요청 ID를 비웁니다.<br>
     */
    fun clear() {
        backingQueue.clear()
    }

    /**
     * Returns whether the queue is empty.<br><br>
     * 큐가 비어 있는지 여부를 반환합니다.<br>
     *
     * @return Return value: true when empty, false otherwise. Log behavior: none.<br><br>
     *         반환값: 비어 있으면 true, 아니면 false. 로그 동작: 없음.<br>
     */
    fun isEmpty(): Boolean = backingQueue.isEmpty()

    /**
     * Returns a snapshot list of queued request IDs.<br><br>
     * 큐에 있는 요청 ID 스냅샷 목록을 반환합니다.<br>
     *
     * @return Return value: list of queued request IDs. Log behavior: none.<br><br>
     *         반환값: 큐에 있는 요청 ID 목록. 로그 동작: 없음.<br>
     */
    fun asList(): List<String> = backingQueue.toList()
}
