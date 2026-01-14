package kr.open.library.simple_ui.xml.ui.components.dialog.root

import android.app.Activity
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.view.Window
import androidx.annotation.StyleRes
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.xml.system_manager.extensions.getDisplayInfo

class DialogConfig {
    /**
     * Custom animation style resource for dialog transitions.<br><br>
     * 다이얼로그 전환에 대한 커스텀 애니메이션 스타일 리소스입니다.<br>
     */
    @StyleRes
    public var animationStyle: Int? = null

    /**
     * Gravity value for dialog positioning on screen.<br>
     * Defaults to Gravity.CENTER.<br><br>
     * 화면에서 다이얼로그 위치 지정을 위한 gravity 값입니다.<br>
     * 기본값은 Gravity.CENTER입니다.<br>
     */
    public var dialogGravity: Int = Gravity.CENTER

    /**
     * Whether the dialog can be canceled by back button or outside touch.<br>
     * Defaults to true.<br><br>
     * 뒤로 가기 버튼이나 외부 터치로 다이얼로그를 취소할 수 있는지 여부입니다.<br>
     * 기본값은 true입니다.<br>
     */
    public var dialogCancelable: Boolean = true

    /**
     * Background color for the dialog.<br>
     * Defaults to null (no background color set).<br><br>
     * 다이얼로그의 배경색입니다.<br>
     * 기본값은 null (배경색 미설정)입니다.<br>
     */
    private var backgroundColor: Int? = null

    /**
     * Background drawable resource ID for the dialog.<br>
     * Defaults to null (no background resource set).<br><br>
     * 다이얼로그의 배경 drawable 리소스 ID입니다.<br>
     * 기본값은 null (배경 리소스 미설정)입니다.<br>
     */
    private var backgroundResId: Int? = null

    /**
     * Sets the background color of the dialog.<br>
     * Clears any previously set background drawable.<br>
     * If rootView is null, only stores the color to be applied when view is created.<br><br>
     * 다이얼로그의 배경색을 설정합니다.<br>
     * 이전에 설정된 배경 drawable을 지웁니다.<br>
     * rootView가 null인 경우, 뷰 생성 시 적용될 색상만 저장합니다.<br>
     *
     * @param rootView The root view to apply the background color to, or null to only store the color.<br><br>
     *                 배경색을 적용할 루트 뷰, 또는 색상만 저장하려면 null.<br>
     * @param color The color value to set as background.<br><br>
     *              배경으로 설정할 색상 값.<br>
     */
    public fun setBackgroundColor(color: Int, rootView: View? = null) {
        this.backgroundColor = color
        this.backgroundResId = null
        rootView?.let { updateBackgroundColor(it) }
    }

    public fun updateBackgroundColor(rootView: View) {
        if (backgroundColor != null) {
            rootView.setBackgroundColor(backgroundColor!!)
        } else if (backgroundResId != null) {
            rootView.setBackgroundResource(backgroundResId!!)
        }
    }

    /**
     * Sets the background drawable of the dialog.<br>
     * Clears any previously set background color.<br>
     * If rootView is null, only stores the resource ID to be applied when view is created.<br><br>
     * 다이얼로그의 배경 drawable을 설정합니다.<br>
     * 이전에 설정된 배경색을 지웁니다.<br>
     * rootView가 null인 경우, 뷰 생성 시 적용될 리소스 ID만 저장합니다.<br>
     *
     * @param rootView The root view to apply the background resource to, or null to only store the resource ID.<br><br>
     *                 배경 리소스를 적용할 루트 뷰, 또는 리소스 ID만 저장하려면 null.<br>
     * @param resId The drawable resource ID to set as background.<br><br>
     *              배경으로 설정할 drawable 리소스 ID.<br>
     */
    public fun setBackgroundResource(resId: Int, rootView: View? = null) {
        this.backgroundColor = null
        this.backgroundResId = resId
        rootView?.let { updateBackgroundColor(it) }
    }

    /**
     * Returns the current background color of the dialog.<br><br>
     * 다이얼로그의 현재 배경색을 반환합니다.<br>
     *
     * @return The background color value, or null if not set.<br><br>
     *         배경색 값, 설정되지 않은 경우 null.<br>
     */
    public fun getBackgroundColor() = backgroundColor

    /**
     * Returns the current background drawable resource ID of the dialog.<br><br>
     * 다이얼로그의 현재 배경 drawable 리소스 ID를 반환합니다.<br>
     *
     * @return The background drawable resource ID, or null if not set.<br><br>
     *         배경 drawable 리소스 ID, 설정되지 않은 경우 null.<br>
     */
    public fun getBackgroundResId() = backgroundResId

    /**
     * Resizes the dialog based on screen size ratios.<br>
     * Width is calculated as screen width * widthRatio.<br>
     * Height is set to WRAP_CONTENT.<br><br>
     * 화면 크기 비율을 기반으로 다이얼로그 크기를 조정합니다.<br>
     * 너비는 화면 너비 * widthRatio로 계산됩니다.<br>
     * 높이는 WRAP_CONTENT로 설정됩니다.<br>
     *
     * @param widthRatio The ratio of screen width (0.0 to 1.0).<br><br>
     *                   화면 너비 비율 (0.0 ~ 1.0).<br>
     *
     * @param heightRatio The ratio of screen height (currently unused, height is WRAP_CONTENT).<br><br>
     *                    화면 높이 비율 (현재 사용되지 않음, 높이는 WRAP_CONTENT).<br>
     */
    public fun resizeDialog(window: Window, activity: Activity, widthRatio: Float? = null, heightRatio: Float?) {
        val displayInfo = window.context.getDisplayInfo()
        Logx.d("Screen Size " + displayInfo.getAppWindowSize(activity))
        val screenSize = displayInfo.getAppWindowSize(activity)
        screenSize?.let { size ->
            val width = widthRatio?.let { (size.width * it).toInt() } ?: LayoutParams.WRAP_CONTENT
            val height = heightRatio?.let { (size.height * it).toInt() } ?: LayoutParams.WRAP_CONTENT
            window.setLayout(width, height)
//            window.setLayout(width, LayoutParams.WRAP_CONTENT) // WRAP_
        }
    }
}
