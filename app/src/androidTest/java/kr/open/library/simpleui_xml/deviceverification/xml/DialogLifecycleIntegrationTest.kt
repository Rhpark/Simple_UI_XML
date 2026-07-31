package kr.open.library.simpleui_xml.deviceverification.xml

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.activity_fragment.ActivityFragmentActivity
import kr.open.library.simpleui_xml.activity_fragment.dialog.BaseBindingDialogFragmentExample
import kr.open.library.simpleui_xml.activity_fragment.dialog.BaseDialogFragmentExample
import kr.open.library.simpleui_xml.deviceverification.harness.PhysicalDeviceRule
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class DialogLifecycleIntegrationTest {
    @get:Rule
    val physicalDeviceRule = PhysicalDeviceRule()

    @Test
    fun xmlP0003_dialogsCanBeSafelyShownAndDismissed() {
        ActivityScenario.launch(ActivityFragmentActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val manager = activity.supportFragmentManager
                val normalDialog = BaseDialogFragmentExample()
                normalDialog.safeShow(manager, "device_normal_dialog")
                manager.executePendingTransactions()
                assertTrue(normalDialog.isAdded)
                assertNotNull(normalDialog.requireView().findViewById(R.id.btnOk))
                normalDialog.safeDismiss()
                manager.executePendingTransactions()
                assertFalse(normalDialog.isAdded)

                val bindingDialog = BaseBindingDialogFragmentExample()
                bindingDialog.safeShow(manager, "device_binding_dialog")
                manager.executePendingTransactions()
                assertTrue(bindingDialog.isAdded)
                assertNotNull(bindingDialog.requireView().findViewById(R.id.btnCancel))
                bindingDialog.safeDismiss()
                manager.executePendingTransactions()
                assertFalse(bindingDialog.isAdded)
            }
        }
    }
}
