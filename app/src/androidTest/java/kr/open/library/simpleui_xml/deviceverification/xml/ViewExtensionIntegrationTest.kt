package kr.open.library.simpleui_xml.deviceverification.xml

import android.graphics.Typeface
import android.widget.EditText
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.deviceverification.harness.PhysicalDeviceRule
import kr.open.library.simpleui_xml.extenstions_style.ExtensionsStyleActivity
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class ViewExtensionIntegrationTest {
    @get:Rule
    val physicalDeviceRule = PhysicalDeviceRule()

    @Test
    fun xmlP0006_viewTextResourceAndDisplayExtensionsAffectRealViews() {
        ActivityScenario.launch(ExtensionsStyleActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val sample = activity.findViewById<TextView>(R.id.tvSampleText)
                activity.findViewById<android.view.View>(R.id.btnBold).performClick()
                val hasBoldTypeface = sample.typeface.style and Typeface.BOLD != 0
                val usesFakeBold = sample.paint.isFakeBoldText
                assertTrue(
                    "XML-P0-006 굵게 표시가 적용되지 않았습니다: " +
                        "typefaceStyle=${sample.typeface.style}, fakeBold=$usesFakeBold",
                    hasBoldTypeface || usesFakeBold,
                )

                val displayInput = activity.findViewById<EditText>(R.id.edtDisplayValue)
                displayInput.setText("10")
                activity.findViewById<android.view.View>(R.id.btnDpToPx).performClick()
                assertTrue(activity.findViewById<TextView>(R.id.tvDisplayResult).text.contains("10.0dp"))

                activity.findViewById<android.view.View>(R.id.btnGetDrawable).performClick()
                assertTrue(activity.findViewById<TextView>(R.id.tvResourceResult).text.contains("성공"))

                activity.findViewById<EditText>(R.id.edtEmail).setText("device@example.com")
                assertTrue(activity.findViewById<TextView>(R.id.tvEmailResult).text.contains("유효한 이메일"))
            }
        }
    }
}
