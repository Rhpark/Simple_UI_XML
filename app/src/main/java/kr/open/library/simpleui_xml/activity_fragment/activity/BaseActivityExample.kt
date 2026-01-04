package kr.open.library.simpleui_xml.activity_fragment.activity

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.xml.system_manager.extensions.getSystemBarController
import kr.open.library.simple_ui.xml.ui.activity.BaseActivity
import kr.open.library.simpleui_xml.R

class BaseActivityExample : BaseActivity(R.layout.activity_base_activity_example) {
    private lateinit var tvStatusBarHeight: TextView
    private lateinit var tvNavigationBarHeight: TextView

    private val systemBarController by lazy { this.window.getSystemBarController() }

    override fun beforeOnCreated(savedInstanceState: Bundle?) {
        super.beforeOnCreated(savedInstanceState)
        Logx.d("BaseActivityExample - beforeOnCreated() called")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tvStatusBarHeight = findViewById(R.id.tvStatusBarHeight)
        tvNavigationBarHeight = findViewById(R.id.tvNavigationBarHeight)

        window.decorView.post {
            tvStatusBarHeight.text = "StatusBar Height: ${systemBarController.getStatusBarVisibleRect()?.height()} px"
            tvNavigationBarHeight.text = "NavigationBar Height: ${systemBarController.getNavigationBarStableRect()?.height()} px"
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
//                statusBarVisible()
                systemBarController.setStatusBarColor(color)
                Logx.d("StatusBar color changed to $label")
            }
        }

        findViewById<Button>(R.id.btnStatusBarGone).setOnClickListener {
            systemBarController.setStatusBarGone()
            Logx.d("StatusBar set to GONE")
        }

        findViewById<Button>(R.id.btnStatusBarVisible).setOnClickListener {
            systemBarController.setStatusBarVisible()
            Logx.d("StatusBar set to VISIBLE")
        }

        findViewById<Button>(R.id.btnStatusBarReset).setOnClickListener {
            systemBarController.setStatusBarVisible()
            systemBarController.resetStatusBarColor()
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
                systemBarController.setStatusBarColor(color)
                systemBarController.setNavigationBarColor(color)
                Logx.d("SystemBars color changed to $label")
            }
        }

        findViewById<Button>(R.id.btnSystemBarsLight).setOnClickListener {
            systemBarController.setNavigationBarDarkIcon(false)
            systemBarController.setStatusBarDarkIcon(false)
            Logx.d("SystemBars appearance set to LIGHT (dark icons)")
        }

        findViewById<Button>(R.id.btnSystemBarsDark).setOnClickListener {
            systemBarController.setNavigationBarDarkIcon(true)
            systemBarController.setStatusBarDarkIcon(true)
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
                systemBarController.setNavigationBarColor(color)
                Logx.d("NavigationBar color changed to $label")
            }
        }

        findViewById<Button>(R.id.btnNavigationBarGone).setOnClickListener {
            systemBarController.setNavigationBarGone()
        }

        findViewById<Button>(R.id.btnNavigationBarVisible).setOnClickListener {
            systemBarController.setNavigationBarVisible()
        }

        findViewById<Button>(R.id.btnNavigationBarReset).setOnClickListener {
            systemBarController.resetNavigationBarColor()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        systemBarController.onDestroy()
    }
}
