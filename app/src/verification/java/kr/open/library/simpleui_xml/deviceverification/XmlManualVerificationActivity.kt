package kr.open.library.simpleui_xml.deviceverification

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import kr.open.library.simple_ui.xml.extensions.view.rotate
import kr.open.library.simple_ui.xml.extensions.view.setOnDebouncedClickListener
import kr.open.library.simple_ui.xml.extensions.view.snackBarShowShort
import kr.open.library.simple_ui.xml.extensions.view.toastShowShort
import kr.open.library.simple_ui.xml.ui.components.activity.normal.BaseActivity
import kr.open.library.simpleui_xml.R

/**
 * Hosts manual XML extension checks in the verification build.<br><br>
 * verification 빌드에서 XML 확장 함수의 수동 검증 화면을 제공합니다.<br>
 */
internal class XmlManualVerificationActivity : BaseActivity(R.layout.activity_xml_manual_verification) {
    private lateinit var sampleText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sampleText = findViewById(R.id.tvAnimationSample)
        setupManualActions()
    }

    override fun onDestroy() {
        if (::sampleText.isInitialized) {
            sampleText.animate().cancel()
            sampleText.rotation = 0f
        }
        super.onDestroy()
    }

    private fun setupManualActions() {
        findViewById<Button>(R.id.btnShowToast).setOnDebouncedClickListener {
            toastShowShort(getString(R.string.xml_manual_toast_message))
        }
        findViewById<Button>(R.id.btnShowSnackBar).setOnDebouncedClickListener {
            findViewById<View>(R.id.rootManualVerification)
                .snackBarShowShort(getString(R.string.xml_manual_snackbar_message))
        }
        findViewById<Button>(R.id.btnRotateSample).setOnDebouncedClickListener {
            sampleText.setText(R.string.xml_manual_animation_running)
            sampleText.rotate(toDegrees = 360f, duration = ANIMATION_DURATION_MILLIS) {
                sampleText.rotation = 0f
                sampleText.setText(R.string.xml_manual_animation_completed)
            }
        }
    }

    private companion object {
        const val ANIMATION_DURATION_MILLIS = 1_500L
    }
}
