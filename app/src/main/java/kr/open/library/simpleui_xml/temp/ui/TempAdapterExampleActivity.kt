package kr.open.library.simpleui_xml.temp.ui

import android.content.Intent
import android.os.Bundle
import kr.open.library.simple_ui.xml.ui.components.activity.binding.BaseDataBindingActivity
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.databinding.ActivityTempAdapterMenuBinding

/**
 * Menu activity that routes to temp adapter example screens.<br><br>
 * Temp 어댑터 예제 화면으로 이동하는 메뉴 액티비티입니다.<br>
 */
class TempAdapterExampleActivity : BaseDataBindingActivity<ActivityTempAdapterMenuBinding>(R.layout.activity_temp_adapter_menu) {
    /**
     * Initializes menu UI and navigation actions.<br><br>
     * 메뉴 UI와 네비게이션 동작을 초기화합니다.<br>
     */
    override fun onCreate(binding: ActivityTempAdapterMenuBinding, savedInstanceState: Bundle?) {
        binding.tvMenuTitle.text = "Temp Adapter Examples"

        binding.btnListSingle.setOnClickListener {
            startActivity(Intent(this, TempAdapterListSingleActivity::class.java))
        }
        binding.btnListMulti.setOnClickListener {
            startActivity(Intent(this, TempAdapterListMultiActivity::class.java))
        }
        binding.btnAdapterSingle.setOnClickListener {
            startActivity(Intent(this, TempAdapterRcvSingleActivity::class.java))
        }
        binding.btnAdapterMulti.setOnClickListener {
            startActivity(Intent(this, TempAdapterRcvMultiActivity::class.java))
        }
    }
}
