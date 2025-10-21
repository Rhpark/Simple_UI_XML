package kr.open.library.simpleui_xml.activity_fragment.activity

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import kr.open.library.simple_ui.logcat.Logx
import kr.open.library.simple_ui.presenter.ui.activity.BaseActivity
import kr.open.library.simpleui_xml.R

class BaseActivityExample : BaseActivity(R.layout.activity_base_activity_example) {

    private lateinit var tvStatusBarHeight: TextView
    private lateinit var tvNavigationBarHeight: TextView

    override fun beforeOnCreated(savedInstanceState: Bundle?) {
        super.beforeOnCreated(savedInstanceState)
        Logx.d("BaseActivityExample - beforeOnCreated() called")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // UI 초기화
        tvStatusBarHeight = findViewById(R.id.tvStatusBarHeight)
        tvNavigationBarHeight = findViewById(R.id.tvNavigationBarHeight)

        // StatusBar/NavigationBar 높이 표시
        tvStatusBarHeight.text = "StatusBar Height: $statusBarHeight px"
        tvNavigationBarHeight.text = "NavigationBar Height: $navigationBarHeight px"

        // StatusBar 색상 변경 버튼
        findViewById<Button>(R.id.btnStatusBarRed).setOnClickListener {
            setStatusBarColor(Color.RED)
            Logx.d("StatusBar color changed to RED")
        }

        findViewById<Button>(R.id.btnStatusBarBlue).setOnClickListener {
            setStatusBarColor(Color.BLUE)
            Logx.d("StatusBar color changed to BLUE")
        }

        findViewById<Button>(R.id.btnStatusBarGreen).setOnClickListener {
            setStatusBarColor(Color.GREEN)
            Logx.d("StatusBar color changed to GREEN")
        }

        findViewById<Button>(R.id.btnStatusBarTransparent).setOnClickListener {
            setStatusBarTransparent()
            Logx.d("StatusBar set to TRANSPARENT")
        }

        // NavigationBar 색상 변경 버튼
        findViewById<Button>(R.id.btnNavigationBarRed).setOnClickListener {
            setNavigationBarColor(Color.RED)
            Logx.d("NavigationBar color changed to RED")
        }

        findViewById<Button>(R.id.btnNavigationBarBlue).setOnClickListener {
            setNavigationBarColor(Color.BLUE)
            Logx.d("NavigationBar color changed to BLUE")
        }

        findViewById<Button>(R.id.btnNavigationBarGreen).setOnClickListener {
            setNavigationBarColor(Color.GREEN)
            Logx.d("NavigationBar color changed to GREEN")
        }

        Logx.d("BaseActivityExample - onCreate() completed")
    }
}
