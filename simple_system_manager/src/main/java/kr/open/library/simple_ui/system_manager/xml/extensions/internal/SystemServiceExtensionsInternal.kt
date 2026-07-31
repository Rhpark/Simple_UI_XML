package kr.open.library.simple_ui.system_manager.xml.extensions.internal

import android.content.Context
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager

/**
 * Returns the window manager for XML-layer components.<br><br>
 * XML 계층 컴포넌트에서 사용하는 윈도우 매니저를 반환합니다.<br>
 *
 * @return The window manager registered for this context.<br><br>
 *         이 컨텍스트에 등록된 윈도우 매니저입니다.<br>
 */
internal fun Context.getWindowManagerInternal(): WindowManager = getSystemService(WindowManager::class.java)

/**
 * Returns the input method manager for XML-layer components.<br><br>
 * XML 계층 컴포넌트에서 사용하는 입력기 매니저를 반환합니다.<br>
 *
 * @return The input method manager registered for this context.<br><br>
 *         이 컨텍스트에 등록된 입력기 매니저입니다.<br>
 */
internal fun Context.getInputMethodManagerInternal(): InputMethodManager = getSystemService(InputMethodManager::class.java)
