package kr.open.library.simple_ui.xml.ui.internal.window

import android.app.Activity
import android.os.Build
import android.util.DisplayMetrics
import android.view.Window

internal data class DialogWindowSize(
    val width: Int,
    val height: Int,
)

internal fun resolveDialogWindowSize(window: Window, activity: Activity): DialogWindowSize? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val bounds = activity.windowManager.currentWindowMetrics.bounds
        return DialogWindowSize(bounds.width(), bounds.height())
    }

    val decorView = window.decorView
    if (decorView.width > 0 && decorView.height > 0) {
        return DialogWindowSize(decorView.width, decorView.height)
    }

    @Suppress("DEPRECATION")
    val displayMetrics = DisplayMetrics().also { activity.windowManager.defaultDisplay.getMetrics(it) }

    if (displayMetrics.widthPixels <= 0 || displayMetrics.heightPixels <= 0) {
        return null
    }

    return DialogWindowSize(
        width = displayMetrics.widthPixels,
        height = displayMetrics.heightPixels,
    )
}
