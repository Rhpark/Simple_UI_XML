package kr.open.library.simpleui_xml.temp.ui

import kr.open.library.simpleui_xml.temp.data.TempItem
import kr.open.library.simpleui_xml.temp.data.TempItemFactory

/**
 * Generator for temp example items.<br><br>
 * Temp 예제 아이템 생성기입니다.<br>
 */
object TempItemGenerator {
    /**
     * Default item count for examples.<br><br>
     * 예제에서 사용하는 기본 아이템 개수입니다.<br>
     */
    private const val DEFAULT_ITEM_COUNT: Int = 10

    /**
     * Large item count for performance testing.<br><br>
     * 성능 테스트용 대량 아이템 개수입니다.<br>
     */
    private const val LARGE_ITEM_COUNT: Int = 200

    /**
     * Generates default item list for given mode.<br><br>
     * 주어진 모드에 대한 기본 아이템 리스트를 생성합니다.<br>
     */
    fun generateDefaultItems(itemMode: TempItemMode): List<TempItem> =
        when (itemMode) {
            TempItemMode.SINGLE -> TempItemFactory.createSingleItems(DEFAULT_ITEM_COUNT)
            TempItemMode.MULTI -> TempItemFactory.createMultiItems(DEFAULT_ITEM_COUNT)
        }

    /**
     * Generates large item list for given mode.<br><br>
     * 주어진 모드에 대한 대량 아이템 리스트를 생성합니다.<br>
     */
    fun generateLargeItems(itemMode: TempItemMode): List<TempItem> =
        when (itemMode) {
            TempItemMode.SINGLE -> TempItemFactory.createSingleItems(LARGE_ITEM_COUNT)
            TempItemMode.MULTI -> TempItemFactory.createMultiItems(LARGE_ITEM_COUNT)
        }

    /**
     * Generates a single new item for given mode.<br><br>
     * 주어진 모드에 대한 단일 신규 아이템을 생성합니다.<br>
     */
    fun generateSingleItem(itemMode: TempItemMode, index: Int): TempItem {
        val id = index.toLong()
        val type = when (itemMode) {
            TempItemMode.SINGLE -> kr.open.library.simpleui_xml.temp.data.TempItemType.PRIMARY
            TempItemMode.MULTI -> if (index % 2 == 0) {
                kr.open.library.simpleui_xml.temp.data.TempItemType.PRIMARY
            } else {
                kr.open.library.simpleui_xml.temp.data.TempItemType.SECONDARY
            }
        }
        return TempItemFactory.createItem(id, type, "Item $index")
    }

    /**
     * Generates multiple new items for given mode.<br><br>
     * 주어진 모드에 대한 여러 신규 아이템을 생성합니다.<br>
     */
    fun generateMultipleItems(itemMode: TempItemMode, startIndex: Int, count: Int): List<TempItem> =
        (0 until count).map { offset ->
            generateSingleItem(itemMode, startIndex + offset)
        }

    /**
     * Creates an updated item with the provided label.<br><br>
     * 제공된 라벨로 갱신된 아이템을 생성합니다.<br>
     */
    fun updateItem(item: TempItem, label: String): TempItem = TempItemFactory.createUpdatedItem(item, label)
}
