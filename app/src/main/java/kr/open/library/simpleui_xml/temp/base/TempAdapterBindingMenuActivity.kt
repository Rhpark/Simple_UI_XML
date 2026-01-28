package kr.open.library.simpleui_xml.temp.base

import android.content.Intent
import android.os.Bundle
import kr.open.library.simple_ui.xml.ui.components.activity.binding.BaseDataBindingActivity
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.databinding.ActivityTempAdapterBindingMenuBinding
import kr.open.library.simpleui_xml.temp.databinding.activity.TempAdapterExampleActivity as DataBindingMenuActivity
import kr.open.library.simpleui_xml.temp.normal.activity.TempAdapterExampleActivity as NormalMenuActivity
import kr.open.library.simpleui_xml.temp.viewbinding.activity.TempAdapterExampleActivity as ViewBindingMenuActivity

/**
 * Entry menu for selecting the binding type.<br><br>
 * 바인딩 타입 선택을 위한 진입 메뉴입니다.<br>
 */
class TempAdapterBindingMenuActivity :
    BaseDataBindingActivity<ActivityTempAdapterBindingMenuBinding>(R.layout.activity_temp_adapter_binding_menu) {
    /**
     * Initializes menu UI and navigation actions.<br><br>
     * 메뉴 UI와 내비게이션 동작을 초기화합니다.<br>
     */
    override fun onCreate(binding: ActivityTempAdapterBindingMenuBinding, savedInstanceState: Bundle?) {
        binding.tvBindingMenuTitle.text = "Temp Adapter Examples"

        binding.btnMenuNormal.setOnClickListener {
            startActivity(Intent(this, NormalMenuActivity::class.java))
        }
        binding.btnMenuDataBinding.setOnClickListener {
            startActivity(Intent(this, DataBindingMenuActivity::class.java))
        }
        binding.btnMenuViewBinding.setOnClickListener {
            startActivity(Intent(this, ViewBindingMenuActivity::class.java))
        }
    }
}
