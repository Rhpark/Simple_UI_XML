package kr.open.library.simple_ui.ui.layout

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.lifecycle.DefaultLifecycleObserver
import kr.open.library.simple_ui.extensions.bindLifecycleObserver
import kr.open.library.simple_ui.extensions.unbindLifecycleObserver

class BaseLifeCycleLinearLayout : LinearLayout, DefaultLifecycleObserver {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
            super(context, attrs, defStyleAttr, defStyleRes)

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        bindLifecycleObserver(this)
    }

    override fun onDetachedFromWindow() {
        unbindLifecycleObserver(this)
        super.onDetachedFromWindow()
    }
}