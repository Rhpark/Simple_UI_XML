package kr.open.library.simple_ui.xml.ui.components.dialog.root

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams
import androidx.annotation.CallSuper
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StyleRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import kr.open.library.simple_ui.core.extensions.trycatch.safeCatch
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.xml.permissions.register.PermissionDelegate
import kr.open.library.simple_ui.xml.permissions.register.PermissionRequester
import kr.open.library.simple_ui.xml.system_manager.extensions.getDisplayInfo

/**
 * Root DialogFragment class providing common dialog functionality and permission management.<br>
 * Serves as the foundation for all DialogFragment classes in the library.<br><br>
 * 공통 다이얼로그 기능과 권한 관리를 제공하는 루트 DialogFragment 클래스입니다.<br>
 * 라이브러리의 모든 DialogFragment 클래스의 기반이 됩니다.<br>
 *
 * **Why this class exists / 이 클래스가 필요한 이유:**<br>
 * - Android's DialogFragment requires repetitive setup for common dialog features like positioning, animation, and background customization.<br>
 * - Permission management needs careful lifecycle handling to survive configuration changes.<br>
 * - Dialog show/dismiss operations can throw exceptions in edge cases (fragment detached, state loss), requiring safe wrappers.<br>
 * - Provides a centralized foundation for all dialog variants (normal, ViewBinding, DataBinding) to inherit common functionality.<br><br>
 * - Android의 DialogFragment는 위치, 애니메이션, 배경 커스터마이징과 같은 공통 다이얼로그 기능에 대해 반복적인 설정이 필요합니다.<br>
 * - 권한 관리는 구성 변경에서 살아남기 위해 신중한 생명주기 처리가 필요합니다.<br>
 * - 다이얼로그 show/dismiss 작업은 엣지 케이스(프래그먼트 분리, 상태 손실)에서 예외를 던질 수 있어 안전한 래퍼가 필요합니다.<br>
 * - 모든 다이얼로그 변형(normal, ViewBinding, DataBinding)이 공통 기능을 상속받을 수 있는 중앙화된 기반을 제공합니다.<br>
 *
 * **Design decisions / 설계 결정 이유:**<br>
 * - Extends DialogFragment to provide dialog-specific lifecycle and features.<br>
 * - Uses PermissionDelegate pattern for reusable permission handling across DialogFragment/Fragment/Activity.<br>
 * - Provides abstract setBackgroundColor/setBackgroundResource methods for subclasses to implement based on their view access pattern.<br>
 * - Uses safeCatch wrapper for show/dismiss operations to prevent crashes from state loss exceptions.<br>
 * - Stores dialog configuration (gravity, animation, cancelable) as properties to allow dynamic updates after creation.<br><br>
 * - DialogFragment를 확장하여 다이얼로그 전용 생명주기 및 기능을 제공합니다.<br>
 * - DialogFragment/Fragment/Activity에서 재사용 가능한 권한 처리를 위해 PermissionDelegate 패턴을 사용합니다.<br>
 * - 하위 클래스가 뷰 접근 패턴에 따라 구현할 수 있도록 추상 setBackgroundColor/setBackgroundResource 메서드를 제공합니다.<br>
 * - 상태 손실 예외로 인한 크래시를 방지하기 위해 show/dismiss 작업에 safeCatch 래퍼를 사용합니다.<br>
 * - 생성 후 동적 업데이트를 허용하기 위해 다이얼로그 구성(gravity, animation, cancelable)을 프로퍼티로 저장합니다.<br>
 *
 * **Important notes / 주의사항:**<br>
 * - Permission requests must be made only after the DialogFragment is attached (isAdded == true).<br>
 * - Calling onRequestPermissions() before attachment may throw because PermissionDelegate uses requireContext().<br>
 * - Use safeShow/safeDismiss instead of show/dismiss to prevent crashes from IllegalStateException.<br><br>
 * - 권한 요청은 DialogFragment가 attach된 이후(isAdded == true)에만 수행해야 합니다.<br>
 * - attach 이전에 onRequestPermissions()를 호출하면 PermissionDelegate가 requireContext()를 사용해 크래시가 발생할 수 있습니다.<br>
 * - IllegalStateException으로 인한 크래시를 방지하기 위해 show/dismiss 대신 safeShow/safeDismiss를 사용하세요.<br>
 *
 * **Features / 기능:**<br>
 * - Custom animation styles for dialog appearance/disappearance<br>
 * - Dialog position control via gravity settings<br>
 * - Cancelable behavior configuration<br>
 * - Background color and drawable customization<br>
 * - Click listener support (positive, negative, other)<br>
 * - Safe show/dismiss methods with exception handling<br>
 * - Runtime permission management via PermissionDelegate<br>
 * - Dialog size resizing based on screen ratio<br><br>
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
 * @see ParentBindingViewDialogFragment For the abstract parent class of all binding-enabled dialog fragments.<br><br>
 *      모든 바인딩 지원 DialogFragment의 추상 부모 클래스는 ParentBindingViewDialogFragment를 참조하세요.<br>
 *
 * @see BaseViewBindingDialogFragment For ViewBinding-enabled DialogFragment.<br><br>
 *      ViewBinding을 사용하는 DialogFragment는 BaseViewBindingDialogFragment를 참조하세요.<br>
 *
 * @see BaseDataBindingDialogFragment For DataBinding-enabled DialogFragment.<br><br>
 *      DataBinding을 사용하는 DialogFragment는 BaseDataBindingDialogFragment를 참조하세요.<br>
 */
public abstract class RootDialogFragment :
    DialogFragment(),
    PermissionRequester {
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
     * Delegate for handling runtime permission requests.<br><br>
     * 런타임 권한 요청을 처리하는 델리게이트입니다.<br>
     */
    protected lateinit var permissionDelegate: PermissionDelegate<DialogFragment>

    public abstract fun setBackgroundColor(@ColorInt color: Int)

    public abstract fun setBackgroundResource(@DrawableRes resId: Int)

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
    protected fun setBackgroundColor(rootView: View?, color: Int) {
        this.backgroundColor = color
        this.backgroundResId = null
        rootView?.setBackgroundColor(color)
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
    protected fun setBackgroundResource(rootView: View?, resId: Int) {
        this.backgroundColor = null
        this.backgroundResId = resId
        rootView?.setBackgroundResource(resId)
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

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionDelegate = PermissionDelegate(this)
        permissionDelegate.onRestoreInstanceState(savedInstanceState)
    }

    @CallSuper
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = super.onCreateDialog(savedInstanceState).apply {
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

    @CallSuper
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
            val displayInfo = requireContext().getDisplayInfo()

            Logx.d("Screen Size " + displayInfo.getAppWindowSize(requireActivity()))
            val screenSize = displayInfo.getAppWindowSize(requireActivity())
            screenSize?.let { size ->
                val width = (size.width * widthRatio).toInt()
                it.setLayout(width, LayoutParams.WRAP_CONTENT) // WRAP_
//                val height = (size.height * heightRatio).toInt()
//            it.setLayout(width, height)
            }
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
    public fun safeDismiss() = safeCatch {
        dismiss()
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
    public fun safeShow(fragmentManager: FragmentManager, tag: String) = safeCatch {
        show(fragmentManager, tag)
    }

    /**
     * Requests permissions using the delegate.<br><br>
     * 델리게이트를 사용하여 권한을 요청합니다.<br>
     *
     * **Important / 주의사항:**<br>
     * - Call only after the Fragment is attached (isAdded == true).<br>
     * - Calling before attachment or after detachment may throw because PermissionDelegate uses requireContext().<br><br>
     * - Fragment가 attach된 이후(isAdded == true)에만 호출하세요.<br>
     * - attach 이전/분리 이후 호출 시 PermissionDelegate가 requireContext()를 호출해 크래시가 발생할 수 있습니다.<br>
     *
     * @param permissions List of permissions to request.<br><br>
     *                    요청할 권한 목록.<br>
     * @param onResult Callback for permission results.<br><br>
     *                 권한 결과에 대한 콜백.<br>
     */
    @CallSuper
    final override fun onRequestPermissions(permissions: List<String>, onResult: (deniedPermissions: List<String>) -> Unit) {
        check(::permissionDelegate.isInitialized) {
            "PermissionDelegate is not initialized. Please call super.onCreate() first."
        }
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
