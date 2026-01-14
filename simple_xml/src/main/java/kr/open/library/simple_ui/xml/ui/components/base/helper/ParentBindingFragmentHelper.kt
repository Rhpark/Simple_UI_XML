package kr.open.library.simple_ui.xml.ui.components.base.helper

/**
 * Helper for Fragment binding lifecycle management.<br>
 * Inherits the common one-shot event collection logic and allows reset on view destruction.<br><br>
 * Fragment 바인딩 생명주기 관리를 위한 헬퍼입니다.<br>
 * 공통 단일 이벤트 수집 로직을 상속하고, 뷰 종료 시 리셋을 제공합니다.<br>
 */
internal class ParentBindingFragmentHelper : ParentBaseBindingHelper() {
    /**
     * Resets the event collection state.<br>
     * Call this when the view is destroyed to allow re-collection when recreated.<br><br>
     * 이벤트 수집 상태를 초기화합니다.<br>
     * 뷰가 파괴될 때 호출하여 재생성 시 다시 수집할 수 있도록 합니다.<br>
     */
    public fun reset() {
        eventCollectStarted = false
    }
}
