package kr.open.library.simpleui_xml.activity_fragment.activity

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.xml.ui.activity.BaseActivity
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
            tvStatusBarHeight.text = "StatusBar Height: ${getStatusBarHeight()} px"
            tvNavigationBarHeight.text = "NavigationBar Height: ${getNavigationBarHeight()} px"
        }

        setupStatusBarButtons()
        setupSystemBarsButtons()
        setupNavigationBarButtons()

        Logx.d("BaseActivityExample - onCreate() completed")
    }

    private fun setupStatusBarButtons() {
        listOf(
            Triple(R.id.btnStatusBarRed, Color.RED, "RED"),
            Triple(R.id.btnStatusBarBlue, Color.BLUE, "BLUE"),
            Triple(R.id.btnStatusBarGreen, Color.GREEN, "GREEN"),
        ).forEach { (buttonId, color, label) ->
            findViewById<Button>(buttonId).setOnClickListener {
                statusBarVisible()
                setStatusBarColor(color)
                Logx.d("StatusBar color changed to $label")
            }
        }

        findViewById<Button>(R.id.btnStatusBarGone).setOnClickListener {
            statusBarGone()
            Logx.d("StatusBar set to GONE")
        }

        findViewById<Button>(R.id.btnStatusBarVisible).setOnClickListener {
            statusBarVisible()
            Logx.d("StatusBar set to VISIBLE")
        }

        findViewById<Button>(R.id.btnStatusBarReset).setOnClickListener {
            statusBarVisible()
            statusBarReset()
            Logx.d("StatusBar RESET")
        }
    }

    private fun setupSystemBarsButtons() {
        listOf(
            Triple(R.id.btnSystemBarsRed, Color.RED, "RED"),
            Triple(R.id.btnSystemBarsBlue, Color.BLUE, "BLUE"),
            Triple(R.id.btnSystemBarsGreen, Color.GREEN, "GREEN"),
        ).forEach { (buttonId, color, label) ->
            findViewById<Button>(buttonId).setOnClickListener {
                setSystemBarsColor(color)
                Logx.d("SystemBars color changed to $label")
            }
        }

        findViewById<Button>(R.id.btnSystemBarsLight).setOnClickListener {
            setSystemBarsAppearance(isDarkIcon = false)
            Logx.d("SystemBars appearance set to LIGHT (dark icons)")
        }

        findViewById<Button>(R.id.btnSystemBarsDark).setOnClickListener {
            setSystemBarsAppearance(isDarkIcon = true)
            Logx.d("SystemBars appearance set to DARK (light icons)")
        }
    }

    private fun setupNavigationBarButtons() {
        listOf(
            Triple(R.id.btnNavigationBarRed, Color.RED, "RED"),
            Triple(R.id.btnNavigationBarBlue, Color.BLUE, "BLUE"),
            Triple(R.id.btnNavigationBarGreen, Color.GREEN, "GREEN"),
        ).forEach { (buttonId, color, label) ->
            findViewById<Button>(buttonId).setOnClickListener {
                setNavigationBarColor(color)
                Logx.d("NavigationBar color changed to $label")
            }
        }

        findViewById<Button>(R.id.btnNavigationBarGone).setOnClickListener {
            navigationBarGone()
        }

        findViewById<Button>(R.id.btnNavigationBarVisible).setOnClickListener {
            navigationBarVisible()
        }

        findViewById<Button>(R.id.btnNavigationBarReset).setOnClickListener {
            navigationBarReset()
        }
    }
}
