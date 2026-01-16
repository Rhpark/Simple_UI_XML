package kr.open.library.simpleui_xml.temp.data

object TempItemFactory {
    fun createSingleItems(size: Int = 20): List<TempItem> =
        List(size) { index ->
            TempItem(
                id = index.toLong(),
                title = "Title $index",
                description = "Description for item $index",
                type = TempItemType.PRIMARY,
            )
        }

    fun createMultiItems(size: Int = 20): List<TempItem> =
        List(size) { index ->
            val type = if (index % 2 == 0) TempItemType.PRIMARY else TempItemType.SECONDARY
            TempItem(
                id = index.toLong(),
                title = "Title $index",
                description = "Description for item $index",
                type = type,
            )
        }
}
