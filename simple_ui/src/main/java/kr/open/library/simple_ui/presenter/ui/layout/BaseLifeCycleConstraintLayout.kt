package kr.open.library.simple_ui.presenter.ui.layout

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.DefaultLifecycleObserver
import kr.open.library.simple_ui.presenter.extensions.view.bindLifecycleObserver
import kr.open.library.simple_ui.presenter.extensions.view.unbindLifecycleObserver

class BaseLifeCycleConstraintLayout : ConstraintLayout, DefaultLifecycleObserver {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
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