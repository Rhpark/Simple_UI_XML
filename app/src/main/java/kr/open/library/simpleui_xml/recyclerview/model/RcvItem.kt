package kr.open.library.simpleui_xml.recyclerview.model

sealed interface RcvItem {
    data class Header(
        val title: String,
        val description: String
    ) : RcvItem

    data class Content(
        val item: SampleItem
    ) : RcvItem

    data class Footer(
        val contentCount: Int
    ) : RcvItem
}
