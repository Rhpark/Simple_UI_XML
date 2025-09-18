package kr.open.library.simple_ui.ui.activity

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider


/**
 * A base activityclass that uses View Binding and provides common functionality for activities with data binding.
 * Binding을 사용하하는 액티비티에 대한 공통 기능을 제공하는 기본 액티비티 클래스.
 *
 * This class handles thefollowing tasks:
 * - Inflates the layout and sets up View Binding.
 * - Sets the lifecycle owner for the binding.
 * - Provides a convenient method to obtain a ViewModel.
 *
 * 이 클래스는 다음과 같은 작업을 처리합니다.
 * - 레이아웃을 확장하고 뷰 바인딩을 설정합니다.
 * - 바인딩에 대한 수명 주기 소유자를 설정합니다.
 * - ViewModel을 얻는 편리한 방법을 제공합니다.
 *
 * @param BINDING The type of the View Binding class.
 * @param layoutRes The layout resource ID for the activity.
 *
 * @param BINDING 뷰 바인딩 클래스의 유형입니다.
 * @param layoutRes 액티비티의 레이아웃 리소스 ID입니다.
 */
public abstract class BaseBindingActivity<BINDING : ViewDataBinding>(@LayoutRes private val layoutRes: Int) :
    RootActivity() {

    /**
     * The View Binding object for the activity.
     * 뷰 바인딩 객체.
     */
    protected lateinit var binding: BINDING
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, layoutRes)
        onCreateView(binding.root, savedInstanceState)
        binding.lifecycleOwner = this
    }

    protected open fun onCreateView(rootView: View, savedInstanceState: Bundle?) {

    }


    /*********************************
     *  ViewModel 이벤트 구독 및 처리   *
     *********************************/
    protected open fun eventVmCollect() {}


    /**
     * Obtains a ViewModel of the specified type.
     * 지정된 유형의 ViewModel을 가져옵니다.
     */
    protected inline fun <reified T : ViewModel> getViewModel(): T {
        return ViewModelProvider(this)[T::class.java]
    }

}