package kr.open.library.simple_ui.xml.ui.fragment.dialog

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StyleRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.xml.permissions.register.PermissionRequester
import kr.open.library.simple_ui.xml.permissions.register.PermissionDelegate
import kr.open.library.simple_ui.xml.system_manager.extensions.getDisplayInfo

/**
 * Root DialogFragment class providing common dialog functionality and permission management.<br>
 * Serves as the foundation for all DialogFragment classes in the library.<br><br>
 * 공통 다이얼로그 기능과 권한 관리를 제공하는 루트 DialogFragment 클래스입니다.<br>
 * 라이브러리의 모든 DialogFragment 클래스의 기반이 됩니다.<br>
 *
 * Features:<br>
 * - Custom animation styles for dialog appearance/disappearance<br>
 * - Dialog position control via gravity settings<br>
 * - Cancelable behavior configuration<br>
 * - Background color and drawable customization<br>
 * - Click listener support (positive, negative, other)<br>
 * - Safe show/dismiss methods with exception handling<br>
 * - Runtime permission management via PermissionDelegate<br>
 * - Dialog size resizing based on screen ratio<br><br>
 * 기능:<br>
 * - 다이얼로그 나타남/사라짐에 대한 커스텀 애니메이션 스타일<br>
 * - gravity 설정을 통한 다이얼로그 위치 제어<br>
 * - 취소 가능 동작 구성<br>
 * - 배경색 및 drawable 커스터마이징<br>
 * - 클릭 리스너 지원 (positive, negative, other)<br>
 * - 예외 처리가 포함된 안전한 show/dismiss 메서드<br>
 * - PermissionDelegate를 통한 런타임 권한 관리<br>
 * - 화면 비율 기반 다이얼로그 크기 조정<br>
 *
 * @see BaseDialogFragment For simple layout-based DialogFragment.<br><br>
 *      간단한 레이아웃 기반 DialogFragment는 BaseDialogFragment를 참조하세요.<br>
 *
 * @see BaseBindingDialogFragment For DataBinding-enabled DialogFragment.<br><br>
 *      DataBinding을 사용하는 DialogFragment는 BaseBindingDialogFragment를 참조하세요.<br>
 */
public abstract class RootDialogFragment() : DialogFragment(), PermissionRequester {

    /**
     * Callback for positive button click events.<br><br>
     * 긍정 버튼 클릭 이벤트에 대한 콜백입니다.<br>
     */
    private var onPositiveClickListener: ((View) -> Unit)? = null

    /**
     * Callback for negative button click events.<br><br>
     * 부정 버튼 클릭 이벤트에 대한 콜백입니다.<br>
     */
    private var onNegativeClickListener: ((View) -> Unit)? = null

    /**
     * Callback for other button click events.<br><br>
     * 기타 버튼 클릭 이벤트에 대한 콜백입니다.<br>
     */
    private var onOtherClickListener: ((View) -> Unit)? = null

    /**
     * Custom animation style resource for dialog transitions.<br><br>
     * 다이얼로그 전환에 대한 커스텀 애니메이션 스타일 리소스입니다.<br>
     */
    @StyleRes
    private var animationStyle: Int? = null

    /**
     * Gravity value for dialog positioning on screen.<br>
     * Defaults to Gravity.CENTER.<br><br>
     * 화면에서 다이얼로그 위치 지정을 위한 gravity 값입니다.<br>
     * 기본값은 Gravity.CENTER입니다.<br>
     */
    private var dialogGravity: Int = Gravity.CENTER

    /**
     * Whether the dialog can be canceled by back button or outside touch.<br>
     * Defaults to true.<br><br>
     * 뒤로 가기 버튼이나 외부 터치로 다이얼로그를 취소할 수 있는지 여부입니다.<br>
     * 기본값은 true입니다.<br>
     */
    private var dialogCancelable: Boolean = true

    /**
     * Background color for the dialog.<br>
     * Defaults to Color.TRANSPARENT.<br><br>
     * 다이얼로그의 배경색입니다.<br>
     * 기본값은 Color.TRANSPARENT입니다.<br>
     */
    private var backgroundColor: Int? = Color.TRANSPARENT

    /**
     * Background drawable resource ID for the dialog.<br><br>
     * 다이얼로그의 배경 drawable 리소스 ID입니다.<br>
     */
    private var backgroundResId: Int? = null

    /**
     * Delegate for handling runtime permission requests.<br><br>
     * 런타임 권한 요청을 처리하는 델리게이트입니다.<br>
     */
    protected lateinit var permissionDelegate: PermissionDelegate<DialogFragment>

    /**
     * Sets the background color of the dialog.<br>
     * Clears any previously set background drawable.<br><br>
     * 다이얼로그의 배경색을 설정합니다.<br>
     * 이전에 설정된 배경 drawable을 지웁니다.<br>
     *
     * @param color The color value to set as background.<br><br>
     *              배경으로 설정할 색상 값.<br>
     */
    public open fun setBackgroundColor(@ColorInt color: Int) {
        this.backgroundColor = color
        this.backgroundResId = null
    }

    /**
     * Sets the background drawable of the dialog.<br>
     * Clears any previously set background color.<br><br>
     * 다이얼로그의 배경 drawable을 설정합니다.<br>
     * 이전에 설정된 배경색을 지웁니다.<br>
     *
     * @param resId The drawable resource ID to set as background.<br><br>
     *              배경으로 설정할 drawable 리소스 ID.<br>
     */
    public open fun setBackgroundDrawable(@DrawableRes resId: Int) {
        this.backgroundColor = null
        this.backgroundResId = resId
    }

    /**
     * Interface for item click callbacks in the dialog.<br><br>
     * 다이얼로그에서 아이템 클릭 콜백을 위한 인터페이스입니다.<br>
     */
    public interface OnItemClick {
        /**
         * Called when an item in the dialog is clicked.<br><br>
         * 다이얼로그의 아이템이 클릭되었을 때 호출됩니다.<br>
         *
         * @param v The view that was clicked.<br><br>
         *          클릭된 뷰.<br>
         */
        public fun onItemClickListener(v: View)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionDelegate = PermissionDelegate(this)
        permissionDelegate.onRestoreInstanceState(savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return super.onCreateDialog(savedInstanceState).apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setCancelable(dialogCancelable)

            // Apply animation if set
            animationStyle?.let { style ->
                window?.attributes?.windowAnimations = style
            }

            // Apply gravity if not center
            if (dialogGravity != Gravity.CENTER) {
                window?.setGravity(dialogGravity)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        permissionDelegate.onSaveInstanceState(outState)
    }

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
    protected fun resizeDialog(widthRatio: Float, heightRatio: Float) {
        dialog?.window?.let {
            val screenSize = requireContext().getDisplayInfo().getScreenSize()
            Logx.d("Screen Size $screenSize, " + requireContext().getDisplayInfo().getFullScreenSize())
            val x = (screenSize.x * widthRatio).toInt()
            val y = (screenSize.y * heightRatio).toInt()
//            it.setLayout(x, y)
            it.setLayout(x, -2) //WARP_
        } ?: Logx.e("Error dialog window is null!")
    }

    /**
     * Sets the custom animation style for dialog appearance/disappearance.<br>
     * Applied immediately if dialog is already showing.<br><br>
     * 다이얼로그 나타남/사라짐에 대한 커스텀 애니메이션 스타일을 설정합니다.<br>
     * 다이얼로그가 이미 표시 중인 경우 즉시 적용됩니다.<br>
     *
     * @param style The animation style resource ID.<br><br>
     *              애니메이션 스타일 리소스 ID.<br>
     */
    public fun setAnimationStyle(@StyleRes style: Int) {
        this.animationStyle = style
        dialog?.window?.attributes?.windowAnimations = style
    }

    /**
     * Sets the position of the dialog on screen.<br>
     * Applied immediately if dialog is already showing.<br><br>
     * 화면에서 다이얼로그의 위치를 설정합니다.<br>
     * 다이얼로그가 이미 표시 중인 경우 즉시 적용됩니다.<br>
     *
     * @param gravity The gravity value for positioning (e.g., Gravity.BOTTOM, Gravity.TOP).<br><br>
     *                위치 지정을 위한 gravity 값 (예: Gravity.BOTTOM, Gravity.TOP).<br>
     */
    public fun setDialogGravity(gravity: Int) {
        this.dialogGravity = gravity
        dialog?.window?.setGravity(gravity)
    }

    /**
     * Sets whether the dialog can be canceled by pressing back button or touching outside.<br>
     * Applied immediately if dialog is already showing.<br><br>
     * 뒤로 가기 버튼을 누르거나 외부를 터치하여 다이얼로그를 취소할 수 있는지 설정합니다.<br>
     * 다이얼로그가 이미 표시 중인 경우 즉시 적용됩니다.<br>
     *
     * @param cancelable True to allow cancellation, false to prevent it.<br><br>
     *                   취소를 허용하려면 true, 방지하려면 false.<br>
     */
    public fun setCancelableDialog(cancelable: Boolean) {
        this.dialogCancelable = cancelable
        dialog?.setCancelable(cancelable)
    }

    /**
     * Sets the listener for positive button click events.<br><br>
     * 긍정 버튼 클릭 이벤트에 대한 리스너를 설정합니다.<br>
     *
     * @param listener The callback to invoke when positive button is clicked.<br><br>
     *                 긍정 버튼이 클릭되었을 때 호출할 콜백.<br>
     */
    public fun setOnPositiveClickListener(listener: (View) -> Unit) {
        onPositiveClickListener = listener
    }

    /**
     * Sets the listener for negative button click events.<br><br>
     * 부정 버튼 클릭 이벤트에 대한 리스너를 설정합니다.<br>
     *
     * @param listener The callback to invoke when negative button is clicked.<br><br>
     *                 부정 버튼이 클릭되었을 때 호출할 콜백.<br>
     */
    public fun setOnNegativeClickListener(listener: (View) -> Unit) {
        onNegativeClickListener = listener
    }

    /**
     * Sets the listener for other button click events.<br><br>
     * 기타 버튼 클릭 이벤트에 대한 리스너를 설정합니다.<br>
     *
     * @param listener The callback to invoke when other button is clicked.<br><br>
     *                 기타 버튼이 클릭되었을 때 호출할 콜백.<br>
     */
    public fun setOnOtherClickListener(listener: (View) -> Unit) {
        onOtherClickListener = listener
    }

    /**
     * Safely dismisses the dialog with exception handling.<br>
     * Catches any exceptions that may occur during dismissal.<br><br>
     * 예외 처리와 함께 다이얼로그를 안전하게 닫습니다.<br>
     * 닫는 동안 발생할 수 있는 모든 예외를 잡습니다.<br>
     */
    public fun safeDismiss() {
        try {
            dismiss()
        } catch (e: Exception) {
            Logx.e("Error $e")
        }
    }

    /**
     * Safely shows the dialog with exception handling.<br>
     * Catches any exceptions that may occur during showing.<br><br>
     * 예외 처리와 함께 다이얼로그를 안전하게 표시합니다.<br>
     * 표시하는 동안 발생할 수 있는 모든 예외를 잡습니다.<br>
     *
     * @param fragmentManager The FragmentManager to use for showing the dialog.<br><br>
     *                        다이얼로그를 표시하는 데 사용할 FragmentManager.<br>
     *
     * @param tag The tag for this fragment, as per FragmentTransaction.add.<br><br>
     *            FragmentTransaction.add에 따른 이 프래그먼트의 태그.<br>
     */
    public fun safeShow(fragmentManager: FragmentManager, tag: String) {
        try {
            show(fragmentManager, tag)
        } catch (e: Exception) {
            Logx.e("Error $e")
        }
    }

    override fun onRequestPermissions(permissions: List<String>, onResult: (deniedPermissions: List<String>) -> Unit) {
        permissionDelegate.requestPermissions(permissions, onResult)
    }

    /**
     * Returns the current background color of the dialog.<br><br>
     * 다이얼로그의 현재 배경색을 반환합니다.<br>
     *
     * @return The background color value, or null if not set.<br><br>
     *         배경색 값, 설정되지 않은 경우 null.<br>
     */
    protected fun getBackgroundColor() = backgroundColor

    /**
     * Returns the current background drawable resource ID of the dialog.<br><br>
     * 다이얼로그의 현재 배경 drawable 리소스 ID를 반환합니다.<br>
     *
     * @return The background drawable resource ID, or null if not set.<br><br>
     *         배경 drawable 리소스 ID, 설정되지 않은 경우 null.<br>
     */
    protected fun getBackgroundResId() = backgroundResId
}