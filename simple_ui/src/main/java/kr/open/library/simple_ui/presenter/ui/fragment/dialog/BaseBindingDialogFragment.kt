package kr.open.library.simple_ui.presenter.ui.fragment.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

public abstract class BaseBindingDialogFragment<BINDING : ViewDataBinding>(
    @LayoutRes private val layoutRes: Int,
    private val isAttachToParent: Boolean = false
) :
    RootDialogFragment() {


    /*********************************************
     * The View Binding object for the fragment.
     * 프래그먼트에 대한 뷰 바인딩 객체.
     **********************************************/
    protected lateinit var binding: BINDING

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, layoutRes, container, isAttachToParent)
        afterOnCreateView(binding.root, savedInstanceState)
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
    protected open fun afterOnCreateView(rootView: View, savedInstanceState: Bundle?) {

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
    }


    /*********************************
     *  ViewModel 이벤트 구독 및 처리   *
     *********************************/
    protected open fun eventVmCollect() {}

    override fun onDestroyView() {
        super.onDestroyView()
        if (::binding.isInitialized) {
            binding.lifecycleOwner = null
        }
    }

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
    protected inline fun <reified T : ViewModel> DialogFragment.getViewModel(): T {
        return ViewModelProvider(this)[T::class.java]
    }
}