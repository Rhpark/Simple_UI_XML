package kr.open.library.simple_ui.xml.system_manager.controller.systembar.internal.helper.base

import android.view.View

internal abstract class SystemBarHelperBase {
    /**
     * Background view for StatusBar/Navigation bar color on API 35+.<br><br>
     * API 35+에서 StatusBar/NavigationBar 색상을 위한 배경 뷰입니다.<br>
     */
    protected var barBackgroundView: View? = null

    /**
     * Configures this View as a non-interactive system bar overlay.<br>
     * Prevents the overlay from intercepting touch events, ensuring proper functionality of:<br>
     * - NavigationBar: Gesture navigation (swipe gestures, back gesture)<br>
     * - StatusBar: Pull-down notification panel gesture<br>
     * - Accessibility: Screen readers ignore these decorative overlays<br><br>
     * 이 뷰를 비대화형 시스템 바 오버레이로 구성합니다.<br>
     * 터치 이벤트 가로채기를 방지하여 다음 기능들이 정상 작동하도록 보장합니다:<br>
     * - NavigationBar: 제스처 내비게이션 (스와이프 제스처, 뒤로 가기 제스처)<br>
     * - StatusBar: 알림 패널 끌어내리기 제스처<br>
     * - 접근성: 스크린 리더가 이러한 장식용 오버레이를 무시<br>
     */
    protected fun View.configureAsSystemBarOverlay() {
        isClickable = false
        isFocusable = false
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
    }
}
