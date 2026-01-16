package kr.open.library.simpleui_xml.temp.data

data class TempItem(
    val id: Long,
    val title: String,
    val description: String,
    val type: TempItemType = TempItemType.PRIMARY,
)
