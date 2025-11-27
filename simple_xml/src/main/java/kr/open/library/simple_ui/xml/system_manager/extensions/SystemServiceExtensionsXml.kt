package kr.open.library.simple_ui.xml.system_manager.extensions

import android.content.Context
import kr.open.library.simple_ui.xml.system_manager.controller.softkeyboard.SoftKeyboardController
import kr.open.library.simple_ui.xml.system_manager.controller.window.FloatingViewController
import kr.open.library.simple_ui.xml.system_manager.info.display.DisplayInfo

/****************************
 * SystemService Controller (XML-specific)*
 ****************************/

public fun Context.getSoftKeyboardController(): SoftKeyboardController = SoftKeyboardController(this)

public fun Context.getFloatingViewController(): FloatingViewController = FloatingViewController(this)

public fun Context.getDisplayInfo(): DisplayInfo = DisplayInfo(this)
