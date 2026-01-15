package kr.open.library.simple_ui.xml.ui.layout.frame.normal

import android.content.Context
import android.util.AttributeSet
import kr.open.library.simple_ui.xml.ui.layout.frame.root.RootFrameLayout

/**
 * Basic FrameLayout with lifecycle awareness and no binding.<br><br>
 * 바인딩 없이 생명주기 인식만 제공하는 기본 FrameLayout입니다.<br>
 */
open class BaseFrameLayout : RootFrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
        super(context, attrs, defStyleAttr, defStyleRes)
}
