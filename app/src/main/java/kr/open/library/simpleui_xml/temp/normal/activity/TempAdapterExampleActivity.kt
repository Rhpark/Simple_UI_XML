package kr.open.library.simpleui_xml.temp.normal.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import kr.open.library.simple_ui.xml.ui.components.activity.normal.BaseActivity
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.temp.multi.normal.activity.TempAdapterListMultiActivity
import kr.open.library.simpleui_xml.temp.multi.normal.activity.TempAdapterRcvMultiActivity

/**
 * Menu activity that routes to temp adapter example screens.<br><br>
 * temp 어댑터 예제 화면으로 이동하는 메뉴 Activity입니다.<br>
 */
class TempAdapterExampleActivity : BaseActivity(R.layout.activity_temp_adapter_menu) {
    /**
     * Initializes menu UI and navigation actions.<br><br>
     * 메뉴 UI와 내비게이션 동작을 초기화합니다.<br>
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tvMenuTitle: TextView = findViewById(R.id.tvMenuTitle)
        val btnListSingle: Button = findViewById(R.id.btnListSingle)
        val btnListMulti: Button = findViewById(R.id.btnListMulti)
        val btnAdapterSingle: Button = findViewById(R.id.btnAdapterSingle)
        val btnAdapterMulti: Button = findViewById(R.id.btnAdapterMulti)

        tvMenuTitle.text = "Temp Adapter Examples (Normal)"

        btnListSingle.setOnClickListener {
            startActivity(Intent(this, TempAdapterListSingleActivity::class.java))
        }
        btnListMulti.setOnClickListener {
            startActivity(Intent(this, TempAdapterListMultiActivity::class.java))
        }
        btnAdapterSingle.setOnClickListener {
            startActivity(Intent(this, TempAdapterRcvSingleActivity::class.java))
        }
        btnAdapterMulti.setOnClickListener {
            startActivity(Intent(this, TempAdapterRcvMultiActivity::class.java))
        }
    }
}