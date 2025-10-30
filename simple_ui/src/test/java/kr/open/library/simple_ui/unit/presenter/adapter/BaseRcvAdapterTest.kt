package kr.open.library.simple_ui.unit.presenter.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kr.open.library.simple_ui.presenter.ui.adapter.normal.base.BaseRcvAdapter
import org.junit.Ignore
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.mockito.Mockito.mock

/**
 * BaseRcvAdapter에 대한 단위 테스트
 *
 * 테스트 대상:
 * - 아이템 추가/삭제
 * - 리스트 크기 확인
 * - 위치 유효성 검사
 * - DiffUtil 설정
 */
// TODO: Android 프레임워크 의존성 때문에 Instrumentation 테스트 전환 검토 중.
//@Ignore("Android 프레임워크 의존성 때문에 Instrumentation 테스트 전환 검토 중")
//class BaseRcvAdapterTest {
//
//    private lateinit var adapter: TestAdapter
//
//    // 테스트용 간단한 데이터 클래스
//    data class TestItem(val id: Int, val name: String)
//
//    // 테스트용 어댑터 구현
//    private class TestAdapter : BaseRcvAdapter<TestItem, TestViewHolder>() {
//        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestViewHolder {
//            val view = mock(View::class.java)
//            return TestViewHolder(view)
//        }
//
//        override fun onBindViewHolder(holder: TestViewHolder, position: Int, item: TestItem) {
//            // 테스트에서는 실제 바인딩 불필요
//        }
//    }
//
//    // 테스트용 ViewHolder
//    private class TestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
//
//    @Before
//    fun setup() {
//        adapter = TestAdapter()
//    }
//
//    // ========== 1. 기본 동작 테스트 ==========
//
//    @Test
//    fun initialItemCount_isZero() {
//        // 어댑터 초기 상태에서 아이템 개수가 0인지 확인
//        // Given & When
//        val count = adapter.itemCount
//
//        // Then
//        assertEquals("초기 아이템 개수는 0이어야 합니다", 0, count)
//    }
//
//    @Test
//    fun emptyList_canBeRetrieved() {
//        // 초기 아이템 리스트가 비어 있는지 확인
//        // Given & When
//        val items = adapter.getItems()
//
//        // Then
//        assertNotNull("아이템 리스트가 null이 아니어야 합니다", items)
//        assertTrue("아이템 리스트가 비어있어야 합니다", items.isEmpty())
//    }
//
//    // ========== 2. 아이템 추가 테스트 ==========
//
//    @Test
//    fun singleItem_canBeAdded() {
//        // 단일 아이템 추가 시 리스트 크기가 증가하는지 확인
//        // Given
//        val item = TestItem(1, "Item 1")
//        val initialSize = adapter.itemCount
//
//        // When
//        val result = adapter.addItem(item)
//
//        // Then
//        assertTrue("아이템 추가가 성공해야 합니다", result)
//        assertEquals("아이템 개수가 1 증가해야 합니다", initialSize + 1, adapter.itemCount)
//    }
//
//    @Test
//    fun addingMultipleItems_increasesCount() {
//        // 여러 아이템을 순차 추가했을 때 개수가 정상 증가하는지 확인
//        // Given
//        val item1 = TestItem(1, "Item 1")
//        val item2 = TestItem(2, "Item 2")
//
//        // When
//        adapter.addItem(item1)
//        adapter.addItem(item2)
//
//        // Then
//        assertEquals("아이템 개수가 2여야 합니다", 2, adapter.itemCount)
//    }
//
//    @Test
//    fun multipleItems_canBeAddedInBatch() {
//        // 리스트로 여러 아이템을 한 번에 추가할 수 있는지 확인
//        // Given
//        val items = listOf(
//            TestItem(1, "Item 1"),
//            TestItem(2, "Item 2"),
//            TestItem(3, "Item 3")
//        )
//
//        // When
//        val result = adapter.addItems(items)
//
//        // Then
//        assertTrue("아이템 추가가 성공해야 합니다", result)
//        assertEquals("아이템 개수가 3이어야 합니다", 3, adapter.itemCount)
//    }
//
//    @Test
//    fun addingEmptyList_succeeds() {
//        // 빈 리스트 추가 시에도 메서드가 성공으로 처리되는지 확인
//        // Given
//        val emptyList = emptyList<TestItem>()
//
//        // When
//        val result = adapter.addItems(emptyList)
//
//        // Then
//        assertTrue("빈 리스트 추가는 성공으로 간주됩니다", result)
//        assertEquals("아이템 개수가 0이어야 합니다", 0, adapter.itemCount)
//    }
//
//    @Test
//    fun item_canBeInsertedAtPosition() {
//        // 지정한 인덱스에 아이템을 삽입할 수 있는지 확인
//        // Given
//        adapter.addItem(TestItem(1, "Item 1"))
//        adapter.addItem(TestItem(3, "Item 3"))
//        val newItem = TestItem(2, "Item 2")
//
//        // When
//        val result = adapter.addItemAt(1, newItem)
//
//        // Then
//        assertTrue("아이템 추가가 성공해야 합니다", result)
//        assertEquals("아이템 개수가 3이어야 합니다", 3, adapter.itemCount)
//        assertEquals("1번 위치에 새 아이템이 있어야 합니다", newItem, adapter.getItem(1))
//    }
//
//    @Test
//    fun item_canBeInsertedAtFront() {
//        // 리스트 맨 앞에 아이템을 삽입하는 경우를 확인
//        // Given
//        adapter.addItem(TestItem(2, "Item 2"))
//        val newItem = TestItem(1, "Item 1")
//
//        // When
//        val result = adapter.addItemAt(0, newItem)
//
//        // Then
//        assertTrue("아이템 추가가 성공해야 합니다", result)
//        assertEquals("0번 위치에 새 아이템이 있어야 합니다", newItem, adapter.getItem(0))
//    }
//
//    @Test
//    fun addingItemToInvalidPosition_fails() {
//        // 잘못된 위치 삽입 시 실패 처리되는지 확인
//        // Given
//        adapter.addItem(TestItem(1, "Item 1"))
//
//        // When
//        val result = adapter.addItemAt(999, TestItem(2, "Item 2"))
//
//        // Then
//        assertFalse("잘못된 위치에 추가는 실패해야 합니다", result)
//    }
//
//    // ========== 3. 아이템 가져오기 테스트 ==========
//
//    @Test
//    fun addedItem_canBeRetrieved() {
//        // 추가한 아이템을 올바르게 조회할 수 있는지 확인
//        // Given
//        val item = TestItem(1, "Item 1")
//        adapter.addItem(item)
//
//        // When
//        val retrievedItem = adapter.getItem(0)
//
//        // Then
//        assertEquals("추가한 아이템과 같아야 합니다", item, retrievedItem)
//    }
//
//    @Test(expected = IndexOutOfBoundsException::class)
//    fun gettingItemWithInvalidPosition_throws() {
//        // 잘못된 위치 조회 시 예외가 발생하는지 확인
//        // Given
//        adapter.addItem(TestItem(1, "Item 1"))
//
//        // When & Then
//        adapter.getItem(999)  // 존재하지 않는 위치
//    }
//
//    @Test
//    fun allItems_canBeRetrieved() {
//        // 전체 아이템 리스트를 가져올 수 있는지 확인
//        // Given
//        val items = listOf(
//            TestItem(1, "Item 1"),
//            TestItem(2, "Item 2")
//        )
//        adapter.addItems(items)
//
//        // When
//        val retrievedItems = adapter.getItems()
//
//        // Then
//        assertEquals("아이템 개수가 일치해야 합니다", 2, retrievedItems.size)
//        assertEquals("첫 번째 아이템이 일치해야 합니다", items[0], retrievedItems[0])
//        assertEquals("두 번째 아이템이 일치해야 합니다", items[1], retrievedItems[1])
//    }
//
//    // ========== 4. 아이템 삭제 테스트 ==========
//
//    @Test
//    fun item_canBeRemovedAtPosition() {
//        // 특정 위치의 아이템을 삭제할 수 있는지 확인
//        // Given
//        adapter.addItem(TestItem(1, "Item 1"))
//        adapter.addItem(TestItem(2, "Item 2"))
//
//        // When
//        val result = adapter.removeAt(0)
//
//        // Then
//        assertTrue("아이템 삭제가 성공해야 합니다", result)
//        assertEquals("아이템 개수가 1이어야 합니다", 1, adapter.itemCount)
//    }
//
//    @Test
//    fun removingItemWithInvalidPosition_fails() {
//        // 잘못된 위치 삭제가 실패로 처리되는지 확인
//        // Given
//        adapter.addItem(TestItem(1, "Item 1"))
//
//        // When
//        val result = adapter.removeAt(999)
//
//        // Then
//        assertFalse("잘못된 위치 삭제는 실패해야 합니다", result)
//        assertEquals("아이템 개수가 그대로여야 합니다", 1, adapter.itemCount)
//    }
//
//    @Test
//    fun specificItem_canBeRemoved() {
//        // 특정 아이템 객체를 삭제할 수 있는지 확인
//        // Given
//        val item1 = TestItem(1, "Item 1")
//        val item2 = TestItem(2, "Item 2")
//        adapter.addItem(item1)
//        adapter.addItem(item2)
//
//        // When
//        val result = adapter.removeItem(item1)
//
//        // Then
//        assertTrue("아이템 삭제가 성공해야 합니다", result)
//        assertEquals("아이템 개수가 1이어야 합니다", 1, adapter.itemCount)
//        assertEquals("남은 아이템이 item2여야 합니다", item2, adapter.getItem(0))
//    }
//
//    @Test
//    fun removingMissingItem_fails() {
//        // 존재하지 않는 아이템 삭제 시 실패하는지 확인
//        // Given
//        adapter.addItem(TestItem(1, "Item 1"))
//        val nonExistentItem = TestItem(999, "Non-existent")
//
//        // When
//        val result = adapter.removeItem(nonExistentItem)
//
//        // Then
//        assertFalse("존재하지 않는 아이템 삭제는 실패해야 합니다", result)
//    }
//
//    @Test
//    fun allItems_canBeCleared() {
//        // 전체 제거 시 리스트가 비워지는지 확인
//        // Given
//        adapter.addItem(TestItem(1, "Item 1"))
//        adapter.addItem(TestItem(2, "Item 2"))
//        adapter.addItem(TestItem(3, "Item 3"))
//
//        // When
//        val result = adapter.removeAll()
//
//        // Then
//        assertTrue("전체 삭제가 성공해야 합니다", result)
//        assertEquals("아이템 개수가 0이어야 합니다", 0, adapter.itemCount)
//    }
//
//    @Test
//    fun clearOnEmptyList_succeeds() {
//        // 비어 있는 리스트에서 전체 삭제를 호출해도 성공하는지 확인
//        // Given - 아무것도 추가하지 않음
//
//        // When
//        val result = adapter.removeAll()
//
//        // Then
//        assertTrue("빈 리스트 전체 삭제는 성공으로 간주됩니다", result)
//        assertEquals("아이템 개수가 0이어야 합니다", 0, adapter.itemCount)
//    }
//
//    // ========== 5. 아이템 설정 테스트 ==========
//
//    @Test
//    fun items_canBeReplaced() {
//        // 새 리스트로 교체했을 때 기존 데이터가 대체되는지 확인
//        // Given
//        adapter.addItem(TestItem(1, "Old Item"))
//        val newItems = listOf(
//            TestItem(2, "New Item 1"),
//            TestItem(3, "New Item 2")
//        )
//
//        // When
//        adapter.setItems(newItems)
//
//        // Then
//        assertEquals("아이템 개수가 2여야 합니다", 2, adapter.itemCount)
//        assertEquals("첫 번째 아이템이 교체되어야 합니다", newItems[0], adapter.getItem(0))
//    }
//
//    @Test
//    fun items_canBeReplacedWithEmpty() {
//        // 빈 리스트로 교체하면 모든 데이터가 제거되는지 확인
//        // Given
//        adapter.addItem(TestItem(1, "Item 1"))
//
//        // When
//        adapter.setItems(emptyList())
//
//        // Then
//        assertEquals("아이템 개수가 0이어야 합니다", 0, adapter.itemCount)
//    }
//
//    // ========== 6. DiffUtil 설정 테스트 ==========
//
//    @Test
//    fun diffUtilItemComparison_canBeConfigured() {
//        // DiffUtil 아이템 동일성 비교 로직이 호출되는지 확인
//        // Given
//        var compareCallCount = 0
//        adapter.setDiffUtilItemSame { oldItem, newItem ->
//            compareCallCount++
//            oldItem.id == newItem.id
//        }
//
//        // When
//        adapter.setItems(listOf(TestItem(1, "Item 1")))
//        adapter.setItems(listOf(TestItem(1, "Updated Item")))
//
//        // Then
//        assertTrue("비교 로직이 호출되어야 합니다", compareCallCount > 0)
//    }
//
//    @Test
//    fun diffUtilContentComparison_canBeConfigured() {
//        // DiffUtil 내용 비교 로직이 작동하는지 확인
//        // Given
//        var compareCallCount = 0
//        adapter.setDiffUtilContentsSame { oldItem, newItem ->
//            compareCallCount++
//            oldItem.name == newItem.name
//        }
//
//        // When
//        adapter.setItems(listOf(TestItem(1, "Item 1")))
//        adapter.setItems(listOf(TestItem(1, "Item 1")))
//
//        // Then
//        assertTrue("내용 비교 로직이 호출되어야 합니다", compareCallCount > 0)
//    }
//
//    @Test
//    fun detectMovesFlag_canBeSet() {
//        // detectMoves 플래그를 true로 설정할 수 있는지 확인
//        // Given & When
//        adapter.detectMoves = true
//
//        // Then
//        assertTrue("detectMoves가 true여야 합니다", adapter.detectMoves)
//    }
//
//    // ========== 7. 클릭 리스너 테스트 ==========
//
//    @Test
//    fun itemClickListener_canBeConfigured() {
//        // 아이템 클릭 리스너 설정이 가능한지 확인
//        // Given
//        var clickedPosition = -1
//        var clickedItem: TestItem? = null
//
//        adapter.setOnItemClickListener { position, item, _ ->
//            clickedPosition = position
//            clickedItem = item
//        }
//
//        // When
//        adapter.addItem(TestItem(1, "Item 1"))
//
//        // Then
//        // 리스너가 설정되었는지 확인 (실제 클릭은 UI 테스트에서 확인)
//        assertNotNull("클릭 리스너가 설정되어야 합니다", adapter)
//    }
//
//    @Test
//    fun itemLongClickListener_canBeConfigured() {
//        // 아이템 롱클릭 리스너 설정이 가능한지 확인
//        // Given
//        var longClickedPosition = -1
//        var longClickedItem: TestItem? = null
//
//        adapter.setOnItemLongClickListener { position, item, _ ->
//            longClickedPosition = position
//            longClickedItem = item
//        }
//
//        // When
//        adapter.addItem(TestItem(1, "Item 1"))
//
//        // Then
//        // 리스너가 설정되었는지 확인
//        assertNotNull("롱클릭 리스너가 설정되어야 합니다", adapter)
//    }
//
//    // ========== 8. 실제 사용 시나리오 테스트 ==========
//
//    @Test
//    fun chatMessageScenario_behavesAsExpected() {
//        // 채팅 메시지가 순차적으로 추가될 때 상태가 유지되는지 확인
//        // Given - 채팅방에 메시지가 계속 추가되는 상황
//        val message1 = TestItem(1, "안녕하세요")
//        val message2 = TestItem(2, "반갑습니다")
//        val message3 = TestItem(3, "잘 부탁드립니다")
//
//        // When
//        adapter.addItem(message1)
//        adapter.addItem(message2)
//        adapter.addItem(message3)
//
//        // Then
//        assertEquals("메시지가 3개 있어야 합니다", 3, adapter.itemCount)
//        assertEquals("마지막 메시지가 올바른지 확인", message3, adapter.getItem(2))
//    }
//
//    @Test
//    fun todoRemovalScenario_behavesAsExpected() {
//        // 할 일 완료 후 삭제 흐름이 정상 동작하는지 확인
//        // Given - 할일 목록에서 완료된 항목을 삭제
//        val todo1 = TestItem(1, "장보기")
//        val todo2 = TestItem(2, "청소하기")
//        val todo3 = TestItem(3, "운동하기")
//
//        adapter.addItems(listOf(todo1, todo2, todo3))
//
//        // When - 청소하기 완료 후 삭제
//        val removed = adapter.removeItem(todo2)
//
//        // Then
//        assertTrue("삭제가 성공해야 합니다", removed)
//        assertEquals("할일이 2개 남아있어야 합니다", 2, adapter.itemCount)
//        assertFalse("삭제된 항목은 리스트에 없어야 합니다",
//            adapter.getItems().contains(todo2))
//    }
//
//    @Test
//    fun searchResultUpdateScenario_behavesAsExpected() {
//        // 검색 결과를 새 리스트로 교체했을 때 상태가 갱신되는지 확인
//        // Given - 검색어 변경에 따라 결과 업데이트
//        val initialResults = listOf(
//            TestItem(1, "Apple"),
//            TestItem(2, "Banana")
//        )
//        adapter.setItems(initialResults)
//
//        // When - 새 검색어로 결과 변경
//        val newResults = listOf(
//            TestItem(3, "Avocado"),
//            TestItem(4, "Apricot")
//        )
//        adapter.setItems(newResults)
//
//        // Then
//        assertEquals("결과가 2개여야 합니다", 2, adapter.itemCount)
//        assertEquals("새 검색 결과가 적용되어야 합니다", newResults[0], adapter.getItem(0))
//    }
//}
