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
                SampleItem(1, "첫 번째 아이템", "Simple UI RecyclerView 예제입니다"),
                SampleItem(2, "두 번째 아이템", "DiffUtil 자동 지원"),
                SampleItem(3, "세 번째 아이템", "스크롤 상태 감지 기능"),
                SampleItem(4, "네 번째 아이템", "Edge 도달 감지"),
                SampleItem(5, "다섯 번째 아이템", "Flow 기반 이벤트"),
                SampleItem(6, "여섯 번째 아이템", "DataBinding 지원"),
                SampleItem(7, "일곱 번째 아이템", "간편한 Adapter 구현"),
                SampleItem(8, "여덟 번째 아이템", "MVVM 패턴"),
                SampleItem(9, "아홉 번째 아이템", "코루틴 기반"),
                SampleItem(10, "열 번째 아이템", "생산성 향상"),
            )
    }
}
