package kr.open.library.simpleui_xml.temp.data

import kr.open.library.simpleui_xml.temp.multi.data.TempItemType

/**
 * Generator for temp example items.<br><br>
 * temp 예제 아이템 생성기입니다.<br>
 */
object TempItemGenerator {
    /**
     * Default item count for examples.<br><br>
     * 예제 기본 아이템 개수입니다.<br>
     */
    private const val DEFAULT_ITEM_COUNT: Int = 10

    /**
     * Large item count for performance testing.<br><br>
     * 성능 테스트용 대량 아이템 개수입니다.<br>
     */
    private const val LARGE_ITEM_COUNT: Int = 200

    /**
     * Generates default single-type items.<br><br>
     * 기본 단일 타입 아이템을 생성합니다.<br>
     */
    fun generateDefaultItemsSingle(): List<TempItem> =
        TempItemFactory.createSingleItems(DEFAULT_ITEM_COUNT)

    /**
     * Generates default multi-type items.<br><br>
     * 기본 다중 타입 아이템을 생성합니다.<br>
     */
    fun generateDefaultItemsMulti(): List<TempItem> =
        TempItemFactory.createMultiItems(DEFAULT_ITEM_COUNT)

    /**
     * Generates large single-type items for performance testing.<br><br>
     * 성능 테스트용 대량 단일 타입 아이템을 생성합니다.<br>
     */
    fun generateLargeItemsSingle(): List<TempItem> =
        TempItemFactory.createSingleItems(LARGE_ITEM_COUNT)

    /**
     * Generates large multi-type items for performance testing.<br><br>
     * 성능 테스트용 대량 다중 타입 아이템을 생성합니다.<br>
     */
    fun generateLargeItemsMulti(): List<TempItem> =
        TempItemFactory.createMultiItems(LARGE_ITEM_COUNT)

    /**
     * Generates a single new single-type item.<br><br>
     * 단일 타입 신규 아이템 1개를 생성합니다.<br>
     */
    fun generateSingleItemSingle(index: Int): TempItem {
        val id = index.toLong()
        return TempItemFactory.createItem(id, TempItemType.PRIMARY, "Item $index")
    }

    /**
     * Generates a single new multi-type item.<br><br>
     * 다중 타입 신규 아이템 1개를 생성합니다.<br>
     */
    fun generateSingleItemMulti(index: Int): TempItem {
        val id = index.toLong()
        val type = if (index % 2 == 0) TempItemType.PRIMARY else TempItemType.SECONDARY
        return TempItemFactory.createItem(id, type, "Item $index")
    }

    /**
     * Creates an updated item with the provided label.<br><br>
     * 제공된 라벨로 갱신된 아이템을 생성합니다.<br>
     */
    fun updateItem(item: TempItem, label: String): TempItem = TempItemFactory.createUpdatedItem(item, label)
}