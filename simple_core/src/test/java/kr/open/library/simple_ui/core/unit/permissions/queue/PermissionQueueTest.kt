package kr.open.library.simple_ui.core.unit.permissions.queue

import kr.open.library.simple_ui.core.permissions.queue.PermissionQueue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for PermissionQueue ordering and de-duplication.<br><br>
 * PermissionQueue의 순서 유지 및 중복 제거를 검증하는 단위 테스트입니다.<br>
 */
class PermissionQueueTest {
    /**
     * Verifies that enqueue/peek preserves FIFO order.<br><br>
     * enqueue/peek가 FIFO 순서를 유지하는지 검증합니다.<br>
     */
    @Test
    fun enqueueAndPeekShouldPreserveOrder() {
        /* Backing list used by PermissionQueue.<br><br>
         * PermissionQueue에서 사용하는 백킹 리스트입니다.<br>
         */
        val backing = mutableListOf<String>()

        /* Queue instance under test.<br><br>
         * 테스트 대상 큐 인스턴스입니다.<br>
         */
        val queue = PermissionQueue(backing)

        queue.enqueue("req1")
        queue.enqueue("req2")

        assertEquals("req1", queue.peek())
        queue.remove("req1")
        assertEquals("req2", queue.peek())
    }

    /**
     * Verifies that duplicate enqueue requests are ignored.<br><br>
     * 중복 enqueue 요청이 무시되는지 검증합니다.<br>
     */
    @Test
    fun enqueueShouldIgnoreDuplicates() {
        /* Backing list used by PermissionQueue.<br><br>
         * PermissionQueue에서 사용하는 백킹 리스트입니다.<br>
         */
        val backing = mutableListOf<String>()

        /* Queue instance under test.<br><br>
         * 테스트 대상 큐 인스턴스입니다.<br>
         */
        val queue = PermissionQueue(backing)

        queue.enqueue("req1")
        queue.enqueue("req1")

        assertEquals(1, queue.asList().size)
        assertEquals("req1", queue.peek())
    }

    /**
     * Verifies that peek returns null when the queue is empty.<br><br>
     * 큐가 비어 있을 때 peek가 null을 반환하는지 검증합니다.<br>
     */
    @Test
    fun peekReturnsNullWhenEmpty() {
        /* Backing list used by PermissionQueue.<br><br>
         * PermissionQueue에서 사용하는 백킹 리스트입니다.<br>
         */
        val backing = mutableListOf<String>()

        /* Queue instance under test.<br><br>
         * 테스트 대상 큐 인스턴스입니다.<br>
         */
        val queue = PermissionQueue(backing)

        assertNull(queue.peek())
    }
}
