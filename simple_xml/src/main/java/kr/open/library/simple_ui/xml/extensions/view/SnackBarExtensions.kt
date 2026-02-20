/**
 * SnackBar display and customization extension functions for View and Fragment.<br>
 * Provides convenient methods to create and show SnackBar with various durations and custom options.<br><br>
 * View와 Fragment를 위한 SnackBar 표시 및 커스터마이징 확장 함수입니다.<br>
 * 다양한 지속 시간과 커스텀 옵션으로 SnackBar를 생성하고 표시하는 편리한 메서드를 제공합니다.<br>
 *
 * Example usage:<br>
 * ```kotlin
 * // Simple SnackBar
 * view.snackBarShowShort("Hello")
 *
 * // With options
 * val options = SnackBarOption(
 *     bgTint = Color.BLUE,
 *     textColor = Color.WHITE,
 *     actionText = "UNDO",
 *     action = { /* handle action */ }
 * )
 * view.snackBarShowLong("Message", options)
 *
 * // Custom view
 * view.snackBarShowShort("Message", customView)
 * ```
 */
package kr.open.library.simple_ui.xml.extensions.view

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.xml.extensions.fragment.withView

/**
 * Configuration options for customizing SnackBar appearance and behavior.<br><br>
 * SnackBar 외관과 동작을 커스터마이징하기 위한 설정 옵션입니다.<br>
 *
 * @property animMode Animation mode for the SnackBar.<br><br>
 *                    SnackBar의 애니메이션 모드.<br>
 *
 * @property bgTint Background tint color (as color integer).<br><br>
 *                  배경 색조 색상 (색상 정수값).<br>
 *
 * @property bgTintStateList Background tint color as ColorStateList.<br><br>
 *                           ColorStateList로 표현된 배경 색조 색상.<br>
 *
 * @property textColor Text color (as color integer).<br><br>
 *                     텍스트 색상 (색상 정수값).<br>
 *
 * @property textColorStateList Text color as ColorStateList.<br><br>
 *                               ColorStateList로 표현된 텍스트 색상.<br>
 *
 * @property isGestureInsetBottomIgnored Whether to ignore bottom gesture insets.<br><br>
 *                                        하단 제스처 인셋을 무시할지 여부.<br>
 *
 * @property actionTextColor Action button text color (as color integer).<br><br>
 *                           액션 버튼 텍스트 색상 (색상 정수값).<br>
 *
 * @property actionTextColorStateList Action button text color as ColorStateList.<br><br>
 *                                     ColorStateList로 표현된 액션 버튼 텍스트 색상.<br>
 *
 * @property actionText Text to display on the action button.<br><br>
 *                      액션 버튼에 표시할 텍스트.<br>
 *
 * @property action Callback to invoke when the action button is clicked.<br><br>
 *                  액션 버튼 클릭 시 실행할 콜백.<br>
 */
public data class SnackBarOption(
    @BaseTransientBottomBar.AnimationMode public val animMode: Int? = null,
    public val bgTint: Int? = null,
    public val bgTintStateList: ColorStateList? = null,
    public val textColor: Int? = null,
    public val textColorStateList: ColorStateList? = null,
    public val isGestureInsetBottomIgnored: Boolean? = null,
    public val actionTextColor: Int? = null,
    public val actionTextColorStateList: ColorStateList? = null,
    public val actionText: CharSequence? = null,
    public val action: ((View) -> Unit)? = null,
)

/**
 * Applies SnackBarOption configuration to this Snackbar.<br><br>
 * 이 Snackbar에 SnackBarOption 설정을 적용합니다.<br>
 *
 * @param snackBarOption The configuration options to apply.<br><br>
 *                       적용할 설정 옵션.<br>
 *
 * @return This Snackbar instance for method chaining.<br><br>
 *         메서드 체이닝을 위한 이 Snackbar 인스턴스.<br>
 */
private fun Snackbar.snackBarOption(snackBarOption: SnackBarOption) =
    apply {
        snackBarOption.bgTint?.let { setBackgroundTint(it) }
        snackBarOption.bgTintStateList?.let { setBackgroundTintList(it) }
        snackBarOption.textColor?.let { setTextColor(it) }
        snackBarOption.textColorStateList?.let { setTextColor(it) }
        snackBarOption.isGestureInsetBottomIgnored?.let { setGestureInsetBottomIgnored(it) }
        snackBarOption.animMode?.let { animationMode = it }
        snackBarOption.actionTextColor?.let { setActionTextColor(it) }
        snackBarOption.actionTextColorStateList?.let { setActionTextColor(it) }
        snackBarOption.actionText?.let { setAction(it, snackBarOption.action) }
    }

/**
 * Creates a short duration Snackbar without showing it.<br>
 * Allows further customization before displaying.<br><br>
 * 표시하지 않고 짧은 시간 Snackbar를 생성합니다.<br>
 * 표시하기 전에 추가 커스터마이징이 가능합니다.<br>
 *
 * @param msg The message to display in the Snackbar.<br><br>
 *            Snackbar에 표시할 메시지.<br>
 *
 * @param snackBarOption Optional configuration for customizing the Snackbar.<br><br>
 *                       Snackbar 커스터마이징을 위한 선택적 설정.<br>
 *
 * @return The created Snackbar instance.<br><br>
 *         생성된 Snackbar 인스턴스.<br>
 */
public fun View.snackBarMakeShort(
    msg: CharSequence,
    snackBarOption: SnackBarOption? = null,
): Snackbar =
    Snackbar.make(this, msg, Snackbar.LENGTH_SHORT).apply {
        snackBarOption?.let { snackBarOption(it) }
    }

/**
 * Creates a long duration Snackbar without showing it.<br>
 * Allows further customization before displaying.<br><br>
 * 표시하지 않고 긴 시간 Snackbar를 생성합니다.<br>
 * 표시하기 전에 추가 커스터마이징이 가능합니다.<br>
 *
 * @param msg The message to display in the Snackbar.<br><br>
 *            Snackbar에 표시할 메시지.<br>
 *
 * @param snackBarOption Optional configuration for customizing the Snackbar.<br><br>
 *                       Snackbar 커스터마이징을 위한 선택적 설정.<br>
 *
 * @return The created Snackbar instance.<br><br>
 *         생성된 Snackbar 인스턴스.<br>
 */
public fun View.snackBarMakeLong(
    msg: CharSequence,
    snackBarOption: SnackBarOption? = null,
): Snackbar =
    Snackbar.make(this, msg, Snackbar.LENGTH_LONG).apply {
        snackBarOption?.let { snackBarOption(it) }
    }

/**
 * Creates an indefinite duration Snackbar without showing it.<br>
 * The Snackbar will remain visible until dismissed.<br><br>
 * 표시하지 않고 무제한 시간 Snackbar를 생성합니다.<br>
 * Snackbar는 닫힐 때까지 계속 표시됩니다.<br>
 *
 * @param msg The message to display in the Snackbar.<br><br>
 *            Snackbar에 표시할 메시지.<br>
 *
 * @param snackBarOption Optional configuration for customizing the Snackbar.<br><br>
 *                       Snackbar 커스터마이징을 위한 선택적 설정.<br>
 *
 * @return The created Snackbar instance.<br><br>
 *         생성된 Snackbar 인스턴스.<br>
 */
public fun View.snackBarMakeIndefinite(
    msg: CharSequence,
    snackBarOption: SnackBarOption? = null,
): Snackbar =
    Snackbar.make(this, msg, Snackbar.LENGTH_INDEFINITE).apply {
        snackBarOption?.let { snackBarOption(it) }
    }

/**
 * Creates and shows a short duration Snackbar from a View.<br><br>
 * View에서 짧은 시간 Snackbar를 생성하고 표시합니다.<br>
 *
 * @param msg The message to display in the Snackbar.<br><br>
 *            Snackbar에 표시할 메시지.<br>
 *
 * @param snackBarOption Optional configuration for customizing the Snackbar.<br><br>
 *                       Snackbar 커스터마이징을 위한 선택적 설정.<br>
 */
public fun View.snackBarShowShort(
    msg: CharSequence,
    snackBarOption: SnackBarOption? = null,
) {
    snackBarMakeShort(msg, snackBarOption).show()
}

/**
 * Creates and shows a short duration Snackbar from a Fragment.<br>
 * Logs an error if the Fragment's view is null.<br><br>
 * Fragment에서 짧은 시간 Snackbar를 생성하고 표시합니다.<br>
 * Fragment의 view가 null이면 에러를 로깅합니다.<br>
 *
 * @param msg The message to display in the Snackbar.<br><br>
 *            Snackbar에 표시할 메시지.<br>
 *
 * @param snackBarOption Optional configuration for customizing the Snackbar.<br><br>
 *                       Snackbar 커스터마이징을 위한 선택적 설정.<br>
 */
public fun Fragment.snackBarShowShort(
    msg: CharSequence,
    snackBarOption: SnackBarOption? = null,
) {
    withView("Fragment view is null, can not show SnackBar!!!") {
        it.snackBarMakeShort(msg, snackBarOption).show()
    }
}

/**
 * Creates and shows a short duration Snackbar with a custom view.<br><br>
 * 커스텀 뷰로 짧은 시간 Snackbar를 생성하고 표시합니다.<br>
 *
 * @param msg The message to display in the Snackbar.<br><br>
 *            Snackbar에 표시할 메시지.<br>
 *
 * @param customView The custom view to display in the Snackbar.<br><br>
 *                   Snackbar에 표시할 커스텀 뷰.<br>
 *
 * @param animMode Animation mode for the Snackbar.<br><br>
 *                 Snackbar의 애니메이션 모드.<br>
 *
 * @param isGestureInsetBottomIgnored Whether to ignore bottom gesture insets.<br><br>
 *                                     하단 제스처 인셋을 무시할지 여부.<br>
 */
@SuppressLint("RestrictedApi")
public fun View.snackBarShowShort(
    msg: CharSequence,
    customView: View,
    @BaseTransientBottomBar.AnimationMode animMode: Int? = null,
    isGestureInsetBottomIgnored: Boolean? = null,
) {
    snackBarMakeShort(msg)
        .applyCustomView(customView, animMode, isGestureInsetBottomIgnored)
        .show()
}

/**
 * Creates and shows a long duration Snackbar from a View.<br><br>
 * View에서 긴 시간 Snackbar를 생성하고 표시합니다.<br>
 *
 * @param msg The message to display in the Snackbar.<br><br>
 *            Snackbar에 표시할 메시지.<br>
 *
 * @param snackBarOption Optional configuration for customizing the Snackbar.<br><br>
 *                       Snackbar 커스터마이징을 위한 선택적 설정.<br>
 */
public fun View.snackBarShowLong(
    msg: CharSequence,
    snackBarOption: SnackBarOption? = null,
) {
    snackBarMakeLong(msg, snackBarOption).show()
}

/**
 * Creates and shows a long duration Snackbar from a Fragment.<br>
 * Logs an error if the Fragment's view is null.<br><br>
 * Fragment에서 긴 시간 Snackbar를 생성하고 표시합니다.<br>
 * Fragment의 view가 null이면 에러를 로깅합니다.<br>
 *
 * @param msg The message to display in the Snackbar.<br><br>
 *            Snackbar에 표시할 메시지.<br>
 *
 * @param snackBarOption Optional configuration for customizing the Snackbar.<br><br>
 *                       Snackbar 커스터마이징을 위한 선택적 설정.<br>
 */
public fun Fragment.snackBarShowLong(
    msg: CharSequence,
    snackBarOption: SnackBarOption? = null,
) {
    withView("Fragment view is null, can not show SnackBar!!!") {
        it.snackBarMakeLong(msg, snackBarOption).show()
    }
}

/**
 * Creates and shows a long duration Snackbar with a custom view.<br><br>
 * 커스텀 뷰로 긴 시간 Snackbar를 생성하고 표시합니다.<br>
 *
 * @param msg The message to display in the Snackbar.<br><br>
 *            Snackbar에 표시할 메시지.<br>
 *
 * @param customView The custom view to display in the Snackbar.<br><br>
 *                   Snackbar에 표시할 커스텀 뷰.<br>
 *
 * @param animMode Animation mode for the Snackbar.<br><br>
 *                 Snackbar의 애니메이션 모드.<br>
 *
 * @param isGestureInsetBottomIgnored Whether to ignore bottom gesture insets.<br><br>
 *                                     하단 제스처 인셋을 무시할지 여부.<br>
 */
@SuppressLint("RestrictedApi")
public fun View.snackBarShowLong(
    msg: CharSequence,
    customView: View,
    @BaseTransientBottomBar.AnimationMode animMode: Int? = null,
    isGestureInsetBottomIgnored: Boolean? = null,
) {
    snackBarMakeLong(msg)
        .applyCustomView(customView, animMode, isGestureInsetBottomIgnored)
        .show()
}

/**
 * Creates and shows an indefinite duration Snackbar from a View.<br><br>
 * View에서 무제한 시간 Snackbar를 생성하고 표시합니다.<br>
 *
 * @param msg The message to display in the Snackbar.<br><br>
 *            Snackbar에 표시할 메시지.<br>
 *
 * @param snackBarOption Optional configuration for customizing the Snackbar.<br><br>
 *                       Snackbar 커스터마이징을 위한 선택적 설정.<br>
 */
public fun View.snackBarShowIndefinite(
    msg: CharSequence,
    snackBarOption: SnackBarOption? = null,
) {
    snackBarMakeIndefinite(msg, snackBarOption).show()
}

/**
 * Creates and shows an indefinite duration Snackbar from a Fragment.<br>
 * Logs an error if the Fragment's view is null.<br><br>
 * Fragment에서 무제한 시간 Snackbar를 생성하고 표시합니다.<br>
 * Fragment의 view가 null이면 에러를 로깅합니다.<br>
 *
 * @param msg The message to display in the Snackbar.<br><br>
 *            Snackbar에 표시할 메시지.<br>
 *
 * @param snackBarOption Optional configuration for customizing the Snackbar.<br><br>
 *                       Snackbar 커스터마이징을 위한 선택적 설정.<br>
 */
public fun Fragment.snackBarShowIndefinite(
    msg: CharSequence,
    snackBarOption: SnackBarOption? = null,
) {
    withView("Fragment view is null, can not show SnackBar!!!") {
        it.snackBarMakeIndefinite(msg, snackBarOption).show()
    }
}

/**
 * Creates and shows an indefinite duration Snackbar with a custom view.<br><br>
 * 커스텀 뷰로 무제한 시간 Snackbar를 생성하고 표시합니다.<br>
 *
 * @param msg The message to display in the Snackbar.<br><br>
 *            Snackbar에 표시할 메시지.<br>
 *
 * @param customView The custom view to display in the Snackbar.<br><br>
 *                   Snackbar에 표시할 커스텀 뷰.<br>
 *
 * @param animMode Animation mode for the Snackbar.<br><br>
 *                 Snackbar의 애니메이션 모드.<br>
 *
 * @param isGestureInsetBottomIgnored Whether to ignore bottom gesture insets.<br><br>
 *                                     하단 제스처 인셋을 무시할지 여부.<br>
 */
@SuppressLint("RestrictedApi")
public fun View.snackBarShowIndefinite(
    msg: CharSequence,
    customView: View,
    @BaseTransientBottomBar.AnimationMode animMode: Int? = null,
    isGestureInsetBottomIgnored: Boolean? = null,
) {
    snackBarMakeIndefinite(msg)
        .applyCustomView(customView, animMode, isGestureInsetBottomIgnored)
        .show()
}

/**
 * Applies a custom view to a Snackbar and configures its animation and gesture settings.<br>
 * This is an internal helper function to avoid code duplication across different duration methods.<br><br>
 * Snackbar에 커스텀 뷰를 적용하고 애니메이션 및 제스처 설정을 구성합니다.<br>
 * 이는 다양한 지속 시간 메서드에서 코드 중복을 피하기 위한 내부 헬퍼 함수입니다.<br>
 *
 * @param customView The custom view to display in the Snackbar.<br><br>
 *                   Snackbar에 표시할 커스텀 뷰.<br>
 *
 * @param animMode Optional animation mode for the Snackbar.<br><br>
 *                 Snackbar의 선택적 애니메이션 모드.<br>
 *
 * @param isGestureInsetBottomIgnored Whether to ignore bottom gesture insets.<br><br>
 *                                     하단 제스처 인셋을 무시할지 여부.<br>
 *
 * @return The modified Snackbar instance for method chaining.<br><br>
 *         메서드 체이닝을 위한 수정된 Snackbar 인스턴스.<br>
 */
@SuppressLint("RestrictedApi")
private fun Snackbar.applyCustomView(
    customView: View,
    @BaseTransientBottomBar.AnimationMode animMode: Int? = null,
    isGestureInsetBottomIgnored: Boolean? = null,
): Snackbar = apply {
    val snackBarLayout = (this.view as? Snackbar.SnackbarLayout)?.let {
        it.removeAllViews()
        it.setPadding(0, 0, 0, 0)
        it.addView(customView)
        animMode?.let { mode -> animationMode = mode }
        isGestureInsetBottomIgnored?.let { ignored -> setGestureInsetBottomIgnored(ignored) }
    }

    if (snackBarLayout == null) {
        Logx.e("Snackbar view is not of type Snackbar.SnackbarLayout")
    }
}
