package kr.open.library.simple_ui.presenter.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * A base fragment class that uses View Binding and provides common functionality for fragments with data binding.
 * 바인딩을 사용하는 프래그먼트에 대한 공통 기능을 제공하는 BaseBindingFragment.
 *
 * This class handles the following tasks:
 * - Inflates the layout and sets up View Binding.
 * - Sets the lifecycle owner for the binding.
 * - Provides a convenient method to obtain a ViewModel.
 *
 * 이 클래스는 다음과 같은 작업을 처리.
 * - 레이아웃을 확장하고 뷰 바인딩을 설정.
 * - 바인딩에 대한 수명 주기 소유자를 설정.
 * - ViewModel을 얻는 편리한 방법을 제공.
 *
 * @param BINDING The type of the View Binding class.
 * @param layoutRes The layout resource ID for the fragment.
 *
 * @param BINDING 뷰 바인딩 클래스의 유형.
 * @param layoutRes 프래그먼트의 레이아웃 리소스 ID.
 */
public abstract class BaseBindingFragment<BINDING : ViewDataBinding>(
    @LayoutRes private val layoutRes: Int,
    private val isAttachToParent: Boolean = false
) : RootFragment() {

    /**
     * The View Binding object for the fragment.
     * 프래그먼트에 대한 뷰 바인딩 객체.
     */
    protected lateinit var binding: BINDING


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, layoutRes, container, isAttachToParent)
        afterOnCrateView(binding.root, savedInstanceState)
        return binding.root
    }


    /**
     * Called immediately after onCreateView() has returned, but before any saved state has been restored in to the view.
     * onCreateView()가 반환된 직후에 호출되지만 저장된 상태가 뷰에 복원되기 전에 호출.
     *
     * @param rootView The root view of the fragment's layout.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     *
     * @param rootView 프래그먼트 레이아웃의 루트 뷰.
     * @param savedInstanceState null이 아닌 경우 이 프래그먼트는 여기에 지정된 이전에 저장된 상태에서 다시 생성.
     */
    protected open fun afterOnCrateView(rootView: View, savedInstanceState: Bundle?) {

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
    }



    /*********************************
     *  ViewModel 이벤트 구독 및 처리   *
     *********************************/
    protected open fun eventVmCollect() {}



    /**
     * Obtains a ViewModel of the specified type.
     * 지정된 유형의 ViewModel을 가져옴.
     *
     * This method uses the [ViewModelProvider] to create or retrieve a ViewModel instance.
     * 이 메서드는 [ViewModelProvider]를 사용하여 ViewModel 인스턴스를 생성하거나 검색.
     *
     * @param T The type of the ViewModel to obtain.
     * @return A ViewModel of the specified type.
     *
     * @param T 가져올 ViewModel의 유형.
     * @return 지정된 유형의 ViewModel.
     */
    protected inline fun <reified T : ViewModel> Fragment.getViewModel(): T {
        return ViewModelProvider(this)[T::class.java]
    }
}