package kr.open.library.simple_ui.presenter.ui.activity

import android.os.Bundle
import androidx.annotation.LayoutRes

/**
 * A basic activity that handles layout inflation.
 * 레이아웃 인플레이션을 처리하는 기본 activity.
 *
 * @param layoutRes The layout resource ID to be inflated.
 */
public abstract class BaseActivity(@LayoutRes private val layoutRes: Int) : RootActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutRes)
    }
}