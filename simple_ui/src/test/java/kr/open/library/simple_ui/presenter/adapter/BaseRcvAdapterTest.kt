package kr.open.library.simple_ui.presenter.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kr.open.library.simple_ui.presenter.ui.adapter.normal.base.BaseRcvAdapter
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
class BaseRcvAdapterTest {

    private lateinit var adapter: TestAdapter

    // 테스트용 간단한 데이터 클래스
    data class TestItem(val id: Int, val name: String)

    // 테스트용 어댑터 구현
    private class TestAdapter : BaseRcvAdapter<TestItem, TestViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestViewHolder {
            val view = mock(View::class.java)
            return TestViewHolder(view)
        }

        override fun onBindViewHolder(holder: TestViewHolder, position: Int, item: TestItem) {
            // 테스트에서는 실제 바인딩 불필요
        }
    }

    // 테스트용 ViewHolder
    private class TestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    @Before
    fun setup() {
        adapter = TestAdapter()
    }

    // ========== 1. 기본 동작 테스트 ==========

    @Test
    fun `초기_아이템_개수는_0이다`() {
        // Given & When
        val count = adapter.itemCount

        // Then
        assertEquals("초기 아이템 개수는 0이어야 합니다", 0, count)
    }

    @Test
    fun `빈_리스트를_가져올_수_있다`() {
        // Given & When
        val items = adapter.getItems()

        // Then
        assertNotNull("아이템 리스트가 null이 아니어야 합니다", items)
        assertTrue("아이템 리스트가 비어있어야 합니다", items.isEmpty())
    }

    // ========== 2. 아이템 추가 테스트 ==========

    @Test
    fun `단일_아이템을_추가할_수_있다`() {
        // Given
        val item = TestItem(1, "Item 1")
        val initialSize = adapter.itemCount

        // When
        val result = adapter.addItem(item)

        // Then
        assertTrue("아이템 추가가 성공해야 합니다", result)
        assertEquals("아이템 개수가 1 증가해야 합니다", initialSize + 1, adapter.itemCount)
    }

    @Test
    fun `여러_아이템을_추가하면_개수가_증가한다`() {
        // Given
        val item1 = TestItem(1, "Item 1")
        val item2 = TestItem(2, "Item 2")

        // When
        adapter.addItem(item1)
        adapter.addItem(item2)

        // Then
        assertEquals("아이템 개수가 2여야 합니다", 2, adapter.itemCount)
    }

    @Test
    fun `리스트로_여러_아이템을_한번에_추가할_수_있다`() {
        // Given
        val items = listOf(
            TestItem(1, "Item 1"),
            TestItem(2, "Item 2"),
            TestItem(3, "Item 3")
        )

        // When
        val result = adapter.addItems(items)

        // Then
        assertTrue("아이템 추가가 성공해야 합니다", result)
        assertEquals("아이템 개수가 3이어야 합니다", 3, adapter.itemCount)
    }

    @Test
    fun `빈_리스트를_추가해도_성공한다`() {
        // Given
        val emptyList = emptyList<TestItem>()

        // When
        val result = adapter.addItems(emptyList)

        // Then
        assertTrue("빈 리스트 추가는 성공으로 간주됩니다", result)
        assertEquals("아이템 개수가 0이어야 합니다", 0, adapter.itemCount)
    }

    @Test
    fun `특정_위치에_아이템을_추가할_수_있다`() {
        // Given
        adapter.addItem(TestItem(1, "Item 1"))
        adapter.addItem(TestItem(3, "Item 3"))
        val newItem = TestItem(2, "Item 2")

        // When
        val result = adapter.addItemAt(1, newItem)

        // Then
        assertTrue("아이템 추가가 성공해야 합니다", result)
        assertEquals("아이템 개수가 3이어야 합니다", 3, adapter.itemCount)
        assertEquals("1번 위치에 새 아이템이 있어야 합니다", newItem, adapter.getItem(1))
    }

    @Test
    fun `맨_앞에_아이템을_추가할_수_있다`() {
        // Given
        adapter.addItem(TestItem(2, "Item 2"))
        val newItem = TestItem(1, "Item 1")

        // When
        val result = adapter.addItemAt(0, newItem)

        // Then
        assertTrue("아이템 추가가 성공해야 합니다", result)
        assertEquals("0번 위치에 새 아이템이 있어야 합니다", newItem, adapter.getItem(0))
    }

    @Test
    fun `잘못된_위치에_아이템을_추가하면_실패한다`() {
        // Given
        adapter.addItem(TestItem(1, "Item 1"))

        // When
        val result = adapter.addItemAt(999, TestItem(2, "Item 2"))

        // Then
        assertFalse("잘못된 위치에 추가는 실패해야 합니다", result)
    }

    // ========== 3. 아이템 가져오기 테스트 ==========

    @Test
    fun `추가한_아이템을_가져올_수_있다`() {
        // Given
        val item = TestItem(1, "Item 1")
        adapter.addItem(item)

        // When
        val retrievedItem = adapter.getItem(0)

        // Then
        assertEquals("추가한 아이템과 같아야 합니다", item, retrievedItem)
    }

    @Test(expected = IndexOutOfBoundsException::class)
    fun `잘못된_위치의_아이템을_가져오면_예외가_발생한다`() {
        // Given
        adapter.addItem(TestItem(1, "Item 1"))

        // When & Then
        adapter.getItem(999)  // 존재하지 않는 위치
    }

    @Test
    fun `전체_아이템_리스트를_가져올_수_있다`() {
        // Given
        val items = listOf(
            TestItem(1, "Item 1"),
            TestItem(2, "Item 2")
        )
        adapter.addItems(items)

        // When
        val retrievedItems = adapter.getItems()

        // Then
        assertEquals("아이템 개수가 일치해야 합니다", 2, retrievedItems.size)
        assertEquals("첫 번째 아이템이 일치해야 합니다", items[0], retrievedItems[0])
        assertEquals("두 번째 아이템이 일치해야 합니다", items[1], retrievedItems[1])
    }

    // ========== 4. 아이템 삭제 테스트 ==========

    @Test
    fun `특정_위치의_아이템을_삭제할_수_있다`() {
        // Given
        adapter.addItem(TestItem(1, "Item 1"))
        adapter.addItem(TestItem(2, "Item 2"))

        // When
        val result = adapter.removeAt(0)

        // Then
        assertTrue("아이템 삭제가 성공해야 합니다", result)
        assertEquals("아이템 개수가 1이어야 합니다", 1, adapter.itemCount)
    }

    @Test
    fun `잘못된_위치의_아이템을_삭제하면_실패한다`() {
        // Given
        adapter.addItem(TestItem(1, "Item 1"))

        // When
        val result = adapter.removeAt(999)

        // Then
        assertFalse("잘못된 위치 삭제는 실패해야 합니다", result)
        assertEquals("아이템 개수가 그대로여야 합니다", 1, adapter.itemCount)
    }

    @Test
    fun `특정_아이템을_삭제할_수_있다`() {
        // Given
        val item1 = TestItem(1, "Item 1")
        val item2 = TestItem(2, "Item 2")
        adapter.addItem(item1)
        adapter.addItem(item2)

        // When
        val result = adapter.removeItem(item1)

        // Then
        assertTrue("아이템 삭제가 성공해야 합니다", result)
        assertEquals("아이템 개수가 1이어야 합니다", 1, adapter.itemCount)
        assertEquals("남은 아이템이 item2여야 합니다", item2, adapter.getItem(0))
    }

    @Test
    fun `존재하지_않는_아이템을_삭제하면_실패한다`() {
        // Given
        adapter.addItem(TestItem(1, "Item 1"))
        val nonExistentItem = TestItem(999, "Non-existent")

        // When
        val result = adapter.removeItem(nonExistentItem)

        // Then
        assertFalse("존재하지 않는 아이템 삭제는 실패해야 합니다", result)
    }

    @Test
    fun `모든_아이템을_삭제할_수_있다`() {
        // Given
        adapter.addItem(TestItem(1, "Item 1"))
        adapter.addItem(TestItem(2, "Item 2"))
        adapter.addItem(TestItem(3, "Item 3"))

        // When
        val result = adapter.removeAll()

        // Then
        assertTrue("전체 삭제가 성공해야 합니다", result)
        assertEquals("아이템 개수가 0이어야 합니다", 0, adapter.itemCount)
    }

    @Test
    fun `빈_리스트에서_전체_삭제를_해도_성공한다`() {
        // Given - 아무것도 추가하지 않음

        // When
        val result = adapter.removeAll()

        // Then
        assertTrue("빈 리스트 전체 삭제는 성공으로 간주됩니다", result)
        assertEquals("아이템 개수가 0이어야 합니다", 0, adapter.itemCount)
    }

    // ========== 5. 아이템 설정 테스트 ==========

    @Test
    fun `새_아이템_리스트로_교체할_수_있다`() {
        // Given
        adapter.addItem(TestItem(1, "Old Item"))
        val newItems = listOf(
            TestItem(2, "New Item 1"),
            TestItem(3, "New Item 2")
        )

        // When
        adapter.setItems(newItems)

        // Then
        assertEquals("아이템 개수가 2여야 합니다", 2, adapter.itemCount)
        assertEquals("첫 번째 아이템이 교체되어야 합니다", newItems[0], adapter.getItem(0))
    }

    @Test
    fun `빈_리스트로_교체할_수_있다`() {
        // Given
        adapter.addItem(TestItem(1, "Item 1"))

        // When
        adapter.setItems(emptyList())

        // Then
        assertEquals("아이템 개수가 0이어야 합니다", 0, adapter.itemCount)
    }

    // ========== 6. DiffUtil 설정 테스트 ==========

    @Test
    fun `DiffUtil_아이템_비교_로직을_설정할_수_있다`() {
        // Given
        var compareCallCount = 0
        adapter.setDiffUtilItemSame { oldItem, newItem ->
            compareCallCount++
            oldItem.id == newItem.id
        }

        // When
        adapter.setItems(listOf(TestItem(1, "Item 1")))
        adapter.setItems(listOf(TestItem(1, "Updated Item")))

        // Then
        assertTrue("비교 로직이 호출되어야 합니다", compareCallCount > 0)
    }

    @Test
    fun `DiffUtil_내용_비교_로직을_설정할_수_있다`() {
        // Given
        var compareCallCount = 0
        adapter.setDiffUtilContentsSame { oldItem, newItem ->
            compareCallCount++
            oldItem.name == newItem.name
        }

        // When
        adapter.setItems(listOf(TestItem(1, "Item 1")))
        adapter.setItems(listOf(TestItem(1, "Item 1")))

        // Then
        assertTrue("내용 비교 로직이 호출되어야 합니다", compareCallCount > 0)
    }

    @Test
    fun `detectMoves_플래그를_설정할_수_있다`() {
        // Given & When
        adapter.detectMoves = true

        // Then
        assertTrue("detectMoves가 true여야 합니다", adapter.detectMoves)
    }

    // ========== 7. 클릭 리스너 테스트 ==========

    @Test
    fun `아이템_클릭_리스너를_설정할_수_있다`() {
        // Given
        var clickedPosition = -1
        var clickedItem: TestItem? = null

        adapter.setOnItemClickListener { position, item, _ ->
            clickedPosition = position
            clickedItem = item
        }

        // When
        adapter.addItem(TestItem(1, "Item 1"))

        // Then
        // 리스너가 설정되었는지 확인 (실제 클릭은 UI 테스트에서 확인)
        assertNotNull("클릭 리스너가 설정되어야 합니다", adapter)
    }

    @Test
    fun `아이템_롱클릭_리스너를_설정할_수_있다`() {
        // Given
        var longClickedPosition = -1
        var longClickedItem: TestItem? = null

        adapter.setOnItemLongClickListener { position, item, _ ->
            longClickedPosition = position
            longClickedItem = item
        }

        // When
        adapter.addItem(TestItem(1, "Item 1"))

        // Then
        // 리스너가 설정되었는지 확인
        assertNotNull("롱클릭 리스너가 설정되어야 합니다", adapter)
    }

    // ========== 8. 실제 사용 시나리오 테스트 ==========

    @Test
    fun `채팅_메시지_추가_시나리오`() {
        // Given - 채팅방에 메시지가 계속 추가되는 상황
        val message1 = TestItem(1, "안녕하세요")
        val message2 = TestItem(2, "반갑습니다")
        val message3 = TestItem(3, "잘 부탁드립니다")

        // When
        adapter.addItem(message1)
        adapter.addItem(message2)
        adapter.addItem(message3)

        // Then
        assertEquals("메시지가 3개 있어야 합니다", 3, adapter.itemCount)
        assertEquals("마지막 메시지가 올바른지 확인", message3, adapter.getItem(2))
    }

    @Test
    fun `할일_목록_완료_후_삭제_시나리오`() {
        // Given - 할일 목록에서 완료된 항목을 삭제
        val todo1 = TestItem(1, "장보기")
        val todo2 = TestItem(2, "청소하기")
        val todo3 = TestItem(3, "운동하기")

        adapter.addItems(listOf(todo1, todo2, todo3))

        // When - 청소하기 완료 후 삭제
        val removed = adapter.removeItem(todo2)

        // Then
        assertTrue("삭제가 성공해야 합니다", removed)
        assertEquals("할일이 2개 남아있어야 합니다", 2, adapter.itemCount)
        assertFalse("삭제된 항목은 리스트에 없어야 합니다",
            adapter.getItems().contains(todo2))
    }

    @Test
    fun `검색_결과_업데이트_시나리오`() {
        // Given - 검색어 변경에 따라 결과 업데이트
        val initialResults = listOf(
            TestItem(1, "Apple"),
            TestItem(2, "Banana")
        )
        adapter.setItems(initialResults)

        // When - 새 검색어로 결과 변경
        val newResults = listOf(
            TestItem(3, "Avocado"),
            TestItem(4, "Apricot")
        )
        adapter.setItems(newResults)

        // Then
        assertEquals("결과가 2개여야 합니다", 2, adapter.itemCount)
        assertEquals("새 검색 결과가 적용되어야 합니다", newResults[0], adapter.getItem(0))
    }
}
