package kr.open.library.simpleui_xml.activity_fragment.fragment

import android.os.Bundle
import android.view.View
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.xml.ui.activity.BaseBindingActivity
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.activity_fragment.ActivityFragmentActivity.Companion.BASE_BINDING_FRAGMENT
import kr.open.library.simpleui_xml.activity_fragment.ActivityFragmentActivity.Companion.BASE_FRAGMENT
import kr.open.library.simpleui_xml.databinding.ActivityFragmentContainerBinding

class FragmentContainerActivity : BaseBindingActivity<ActivityFragmentContainerBinding>(R.layout.activity_fragment_container) {

    override fun onCreateView(rootView: View, savedInstanceState: Bundle?) {
        super.onCreateView(rootView, savedInstanceState)

        val fragmentType = intent.getIntExtra("FRAGMENT_TYPE", 1)
        Logx.d("FragmentContainerActivity - fragmentType: $fragmentType")

        val fragment =
            when (fragmentType) {
                BASE_FRAGMENT -> {
                    Logx.d("Loading BaseFragmentExample")
                    BaseFragmentExample()
                }
                BASE_BINDING_FRAGMENT -> {
                    Logx.d("Loading BaseBindingFragmentExample")
                    BaseBindingFragmentExample()
                }
                else -> {
                    Logx.e("Unknown fragmentType: $fragmentType, defaulting to BaseFragmentExample")
                    BaseFragmentExample()
                }
            }

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()

        // Title 설정
        val title =
            when (fragmentType) {
                BASE_FRAGMENT -> "BaseFragment Example"
                BASE_BINDING_FRAGMENT -> "BaseBindingFragment Example"
                else -> "Fragment Example"
            }
        binding.tvTitle.text = title
    }
}
