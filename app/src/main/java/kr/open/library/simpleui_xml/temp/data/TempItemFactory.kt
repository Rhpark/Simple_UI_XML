package kr.open.library.simpleui_xml.temp.data

/**
 * Factory for creating sample items used in adapter examples.<br><br>
 * 어댑터 예제에서 사용하는 샘플 아이템을 생성하는 팩토리입니다.<br>
 */
object TempItemFactory {
    /**
     * Default title prefix for generated items.<br><br>
     * 생성 아이템의 기본 제목 접두어입니다.<br>
     */
    private const val DEFAULT_TITLE_PREFIX = "Title"

    /**
     * Default description prefix for generated items.<br><br>
     * 생성 아이템의 기본 설명 접두어입니다.<br>
     */
    private const val DEFAULT_DESCRIPTION_PREFIX = "Description for item"

    /**
     * Creates a single-type item list starting from the given id.<br><br>
     * 지정 ID부터 시작하는 단일 타입 아이템 리스트를 생성합니다.<br>
     */
    fun createSingleItems(size: Int = 20, startId: Long = 0L): List<TempItem> {
        // Generated list based on size.<br><br>크기 기준으로 생성된 리스트입니다.<br>
        return List(size) { index ->
            // Item id computed from start offset.<br><br>시작 오프셋 기반으로 계산된 아이템 ID입니다.<br>
            val itemId = startId + index
            createItem(itemId, TempItemType.PRIMARY)
        }
    }

    /**
     * Creates a multi-type item list alternating by index.<br><br>
     * 인덱스 기준으로 교차되는 다중 타입 아이템 리스트를 생성합니다.<br>
     */
    fun createMultiItems(size: Int = 20, startId: Long = 0L): List<TempItem> {
        // Generated list based on size.<br><br>크기 기준으로 생성된 리스트입니다.<br>
        return List(size) { index ->
            // Item id computed from start offset.<br><br>시작 오프셋 기반으로 계산된 아이템 ID입니다.<br>
            val itemId = startId + index
            // Alternating type by index.<br><br>인덱스 기준으로 교차되는 타입입니다.<br>
            val type = if (index % 2 == 0) TempItemType.PRIMARY else TempItemType.SECONDARY
            createItem(itemId, type)
        }
    }

    /**
     * Creates a single item with optional label suffix.<br><br>
     * 라벨 접미어를 포함한 단일 아이템을 생성합니다.<br>
     */
    fun createItem(id: Long, type: TempItemType, label: String? = null): TempItem {
        // Optional label suffix for UI clarity.<br><br>UI 가독성을 위한 선택적 라벨 접미어입니다.<br>
        val suffix = label?.let { " [$it]" } ?: ""
        return TempItem(
            id = id,
            title = "$DEFAULT_TITLE_PREFIX $id$suffix",
            description = "$DEFAULT_DESCRIPTION_PREFIX $id$suffix",
            type = type,
        )
    }

    /**
     * Creates an updated item that keeps the same id but changes content.<br><br>
     * 동일한 ID를 유지하면서 콘텐츠를 변경한 아이템을 생성합니다.<br>
     */
    fun createUpdatedItem(item: TempItem, label: String): TempItem {
        // Update marker appended to title/description.<br><br>제목/설명에 추가하는 업데이트 표시입니다.<br>
        val suffix = " [$label]"
        return item.copy(
            title = "${item.title}$suffix",
            description = "${item.description}$suffix",
        )
    }
}
