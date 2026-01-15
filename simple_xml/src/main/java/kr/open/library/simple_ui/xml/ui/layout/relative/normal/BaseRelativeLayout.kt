package kr.open.library.simple_ui.xml.ui.layout.relative.normal

import android.content.Context
import android.util.AttributeSet
import kr.open.library.simple_ui.xml.ui.layout.relative.root.RootRelativeLayout

/**
 * Basic RelativeLayout with lifecycle awareness and no binding.<br><br>
 * 바인딩 없이 생명주기 인식만 제공하는 기본 RelativeLayout입니다.<br>
 */
open class BaseRelativeLayout : RootRelativeLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
        super(context, attrs, defStyleAttr, defStyleRes)
}
