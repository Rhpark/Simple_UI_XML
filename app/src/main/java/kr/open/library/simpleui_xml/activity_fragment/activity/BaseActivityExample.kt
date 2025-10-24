package kr.open.library.simpleui_xml.activity_fragment.activity

import android.graphics.Color
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

        tvStatusBarHeight = findViewById(R.id.tvStatusBarHeight)
        tvNavigationBarHeight = findViewById(R.id.tvNavigationBarHeight)

        window.decorView.post {
            tvStatusBarHeight.text = "StatusBar Height: $statusBarHeight px"
            tvNavigationBarHeight.text = "NavigationBar Height: $navigationBarHeight px"
        }

        setupStatusBarButtons()
        setupNavigationBarButtons()

        Logx.d("BaseActivityExample - onCreate() completed")
    }

    private fun setupStatusBarButtons() {
        listOf(
            Triple(R.id.btnStatusBarRed, Color.RED, "RED"),
            Triple(R.id.btnStatusBarBlue, Color.BLUE, "BLUE"),
            Triple(R.id.btnStatusBarGreen, Color.GREEN, "GREEN")
        ).forEach { (buttonId, color, label) ->
            findViewById<Button>(buttonId).setOnClickListener {
                setStatusBarColor(color)
                Logx.d("StatusBar color changed to $label")
            }
        }

        findViewById<Button>(R.id.btnStatusBarTransparent).setOnClickListener {
            setStatusBarTransparent()
            Logx.d("StatusBar set to TRANSPARENT")
        }
    }

    private fun setupNavigationBarButtons() {
        listOf(
            Triple(R.id.btnNavigationBarRed, Color.RED, "RED"),
            Triple(R.id.btnNavigationBarBlue, Color.BLUE, "BLUE"),
            Triple(R.id.btnNavigationBarGreen, Color.GREEN, "GREEN")
        ).forEach { (buttonId, color, label) ->
            findViewById<Button>(buttonId).setOnClickListener {
                setNavigationBarColor(color)
                Logx.d("NavigationBar color changed to $label")
            }
        }
    }
}
