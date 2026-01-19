package kr.open.library.simpleui_xml.temp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kr.open.library.simpleui_xml.R

/**
 * Menu activity that routes to temp adapter example screens.<br><br>
 * Temp 어댑터 예제 화면으로 이동하는 메뉴 액티비티입니다.<br>
 */
class TempAdapterExampleActivity : AppCompatActivity() {
    /**
     * Title text view for the menu screen.<br><br>
     * 메뉴 화면 제목을 표시하는 텍스트 뷰입니다.<br>
     */
    private lateinit var tvMenuTitle: TextView

    /**
     * Button to open List single example.<br><br>
     * List 단일 예제를 여는 버튼입니다.<br>
     */
    private lateinit var btnListSingle: Button

    /**
     * Button to open List multi example.<br><br>
     * List 다중 예제를 여는 버튼입니다.<br>
     */
    private lateinit var btnListMulti: Button

    /**
     * Button to open Adapter single example.<br><br>
     * Adapter 단일 예제를 여는 버튼입니다.<br>
     */
    private lateinit var btnAdapterSingle: Button

    /**
     * Button to open Adapter multi example.<br><br>
     * Adapter 다중 예제를 여는 버튼입니다.<br>
     */
    private lateinit var btnAdapterMulti: Button

    /**
     * Initializes menu UI and navigation actions.<br><br>
     * 메뉴 UI와 네비게이션 동작을 초기화합니다.<br>
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_temp_adapter_menu)

        bindViews()
        setupListeners()
        tvMenuTitle.text = "Temp Adapter Examples"
    }

    /**
     * Binds menu views from the layout.<br><br>
     * 레이아웃의 메뉴 뷰를 바인딩합니다.<br>
     */
    private fun bindViews() {
        tvMenuTitle = findViewById(R.id.tvMenuTitle)
        btnListSingle = findViewById(R.id.btnListSingle)
        btnListMulti = findViewById(R.id.btnListMulti)
        btnAdapterSingle = findViewById(R.id.btnAdapterSingle)
        btnAdapterMulti = findViewById(R.id.btnAdapterMulti)
    }

    /**
     * Sets up click listeners for menu buttons.<br><br>
     * 메뉴 버튼 클릭 리스너를 설정합니다.<br>
     */
    private fun setupListeners() {
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
