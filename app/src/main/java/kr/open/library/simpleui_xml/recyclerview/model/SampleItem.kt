package kr.open.library.simpleui_xml.recyclerview.model

data class SampleItem(
    val id: Long,
    val title: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
) {
    companion object {
        fun createSampleData(): List<SampleItem> =
            listOf(
                SampleItem(1, "泥?踰덉㎏ ?꾩씠??, "Simple UI RecyclerView ?덉젣?낅땲??),
                SampleItem(2, "??踰덉㎏ ?꾩씠??, "DiffUtil ?먮룞 吏??),
                SampleItem(3, "??踰덉㎏ ?꾩씠??, "?ㅽ겕濡??곹깭 媛먯? 湲곕뒫"),
                SampleItem(4, "??踰덉㎏ ?꾩씠??, "Edge ?꾨떖 媛먯?"),
                SampleItem(5, "?ㅼ꽢 踰덉㎏ ?꾩씠??, "Flow 湲곕컲 ?대깽??),
                SampleItem(6, "?ъ꽢 踰덉㎏ ?꾩씠??, "DataBinding 吏??),
                SampleItem(7, "?쇨낢 踰덉㎏ ?꾩씠??, "媛꾪렪??Adapter 援ы쁽"),
                SampleItem(8, "?щ뜜 踰덉㎏ ?꾩씠??, "MVVM ?⑦꽩"),
                SampleItem(9, "?꾪솄 踰덉㎏ ?꾩씠??, "肄붾（??湲곕컲"),
                SampleItem(10, "??踰덉㎏ ?꾩씠??, "?앹궛???μ긽"),
            )
    }
}
