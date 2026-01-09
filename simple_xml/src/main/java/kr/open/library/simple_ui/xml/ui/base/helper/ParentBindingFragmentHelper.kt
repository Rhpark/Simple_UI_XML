package kr.open.library.simple_ui.xml.ui.base.helper

internal class ParentBindingFragmentHelper : ParentBaseBindingHelper() {
    /**
     * Resets the event collection state.<br>
     * Should be called when the view is destroyed (e.g., in Fragment's onDestroyView) to allow re-collection when the view is recreated.<br><br>
     *
     * 이벤트 수집 상태를 초기화합니다.<br>
     * 뷰가 파괴될 때(예: Fragment의 onDestroyView) 호출하여 뷰가 재생성될 때 다시 수집할 수 있도록 해야 합니다.<br>
     */
    public fun reset() {
        eventCollectStarted = false
    }
}
