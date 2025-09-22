package kr.open.library.simple_ui.presenter.extensions.view

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import kr.open.library.simple_ui.logcat.Logx


/*********************
 *      SnackBar     *
 *********************/
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

private fun Snackbar.snackBarOption(snackBarOption: SnackBarOption) = apply {
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

public fun View.snackBarMakeShort(msg: CharSequence, snackBarOption: SnackBarOption? = null)
: Snackbar = Snackbar.make(this, msg, Snackbar.LENGTH_SHORT).apply {
        snackBarOption?.let { snackBarOption(it) }
}

public fun View.snackBarMakeLong(msg: CharSequence, snackBarOption: SnackBarOption? = null
): Snackbar = Snackbar.make(this, msg, Snackbar.LENGTH_LONG).apply {
        snackBarOption?.let { snackBarOption(it) }
}

public fun View.snackBarMakeIndefinite(msg: CharSequence, snackBarOption: SnackBarOption? = null,
): Snackbar = Snackbar.make(this, msg, Snackbar.LENGTH_INDEFINITE).apply {
        snackBarOption?.let { snackBarOption(it) }
}

public fun View.snackBarShowShort(msg: CharSequence, snackBarOption: SnackBarOption? = null) {
    snackBarMakeShort(msg, snackBarOption).show()
}

public fun Fragment.snackBarShowShort(msg: CharSequence, snackBarOption: SnackBarOption? = null) {
    this.view?.let {
        it.snackBarMakeShort(msg, snackBarOption).show()
    } ?: Logx.e("Fragment view is null, can not show SnackBar!!!")
}

@SuppressLint("RestrictedApi")
public fun View.snackBarShowShort(
    msg: CharSequence,
    customView: View,
    @BaseTransientBottomBar.AnimationMode animMode: Int? = null,
    isGestureInsetBottomIgnored: Boolean? = null,
) {
    snackBarMakeShort(msg).apply {
        val snackBarLayout =
            (this.view as? Snackbar.SnackbarLayout)?.let {
                it.removeAllViews()
                it.setPadding(0, 0, 0, 0)
                it.addView(customView)
                animMode?.let { animationMode = it }
                isGestureInsetBottomIgnored?.let { setGestureInsetBottomIgnored(it) }
            }
        if (snackBarLayout == null) {
            Logx.e("Snackbar view is not of type Snackbar.SnackbarLayout")
        }
    }.show()
}

public fun View.snackBarShowLong(msg: CharSequence, snackBarOption: SnackBarOption?) {
    snackBarMakeLong(msg, snackBarOption).show()
}

public fun Fragment.snackBarShowLong(msg: CharSequence, snackBarOption: SnackBarOption?) {
    this.view?.let {
        it.snackBarMakeLong(msg, snackBarOption).show()
    } ?: Logx.e("Fragment view is null, can not show SnackBar!!!")
}

@SuppressLint("RestrictedApi")
public fun View.snackBarShowLong(
    msg: CharSequence,
    customView: View,
    @BaseTransientBottomBar.AnimationMode animMode: Int? = null,
    isGestureInsetBottomIgnored: Boolean? = null,
) {
    snackBarMakeLong(msg).apply {
        val snackBarLayout =
            (this.view as? Snackbar.SnackbarLayout)?.let {
                it.removeAllViews()
                it.setPadding(0, 0, 0, 0)
                it.addView(customView)
                animMode?.let { animationMode = it }
                isGestureInsetBottomIgnored?.let { setGestureInsetBottomIgnored(it) }
            }
        if (snackBarLayout == null) {
            Logx.e("Snackbar view is not of type Snackbar.SnackbarLayout")
        }
    }.show()
}

public fun View.snackBarShowIndefinite(msg: CharSequence, snackBarOption: SnackBarOption?) {
    snackBarMakeIndefinite(msg, snackBarOption).show()
}

public fun Fragment.snackBarShowIndefinite(msg: CharSequence, snackBarOption: SnackBarOption?) {
    this.view?.let {
        it.snackBarMakeIndefinite(msg, snackBarOption).show()
    } ?: Logx.e("Fragment view is null, can not show SnackBar!!!")
}

@SuppressLint("RestrictedApi")
public fun View.snackBarShowIndefinite(
    msg: CharSequence,
    customView: View,
    @BaseTransientBottomBar.AnimationMode animMode: Int? = null,
    isGestureInsetBottomIgnored: Boolean? = null,
) {
    snackBarMakeIndefinite(msg).apply {
        val snackBarLayout =
            (this.view as? Snackbar.SnackbarLayout)?.let {
                it.removeAllViews()
                it.setPadding(0, 0, 0, 0)
                it.addView(customView)
                animMode?.let { animationMode = it }
                isGestureInsetBottomIgnored?.let { setGestureInsetBottomIgnored(it) }
            }

        if (snackBarLayout == null) {
            Logx.e("Snackbar view is not of type Snackbar.SnackbarLayout")
        }
    }.show()
}
