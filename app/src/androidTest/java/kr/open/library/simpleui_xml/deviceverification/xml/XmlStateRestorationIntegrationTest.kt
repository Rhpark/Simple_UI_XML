package kr.open.library.simpleui_xml.deviceverification.xml

import android.widget.EditText
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.deviceverification.harness.PhysicalDeviceRule
import kr.open.library.simpleui_xml.extenstions_style.ExtensionsStyleActivity
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class XmlStateRestorationIntegrationTest {
    @get:Rule
    val physicalDeviceRule = PhysicalDeviceRule()

    @Test
    fun xmlP1002_editTextStateRestoresAfterActivityRecreation() {
        ActivityScenario.launch(ExtensionsStyleActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<EditText>(R.id.edtEmail).setText(RESTORATION_MARKER)
            }

            scenario.recreate()

            scenario.onActivity { activity ->
                assertEquals(
                    RESTORATION_MARKER,
                    activity.findViewById<EditText>(R.id.edtEmail).text.toString(),
                )
            }
        }
    }

    private companion object {
        const val RESTORATION_MARKER = "device-restoration@example.com"
    }
}
