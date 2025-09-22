package kr.open.library.simple_ui.presenter.extensions.view

import android.content.Context
import android.widget.Toast
import androidx.fragment.app.Fragment
import kr.open.library.simple_ui.logcat.Logx


/*********************
 *  Toast Extensions *
 *********************/
public fun Context.toastShowShort(msg: CharSequence) { toastShort(msg).show() }

public fun Context.toastShowLong(msg: CharSequence) { toastLong(msg).show() }

public fun Context.toastShort(msg: CharSequence): Toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT)

public fun Context.toastLong(msg: CharSequence): Toast = Toast.makeText(this, msg, Toast.LENGTH_LONG)

public fun Fragment.toastShowShort(msg: CharSequence) {
    this.context?.toastShowShort(msg)
        ?: Logx.e("Can not Toast Show, Fragment Context is null!!")
}

public fun Fragment.toastShowLong(msg: CharSequence) {
    this.context?.toastShowLong(msg)
        ?: Logx.e("Can not Toast Show, Fragment Context is null!!")
}
