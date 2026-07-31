package kr.open.library.simpleui_xml.deviceverification.xml

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.activity_fragment.ActivityFragmentActivity
import kr.open.library.simpleui_xml.activity_fragment.fragment.BaseBindingFragmentExample
import kr.open.library.simpleui_xml.activity_fragment.fragment.BaseFragmentExample
import kr.open.library.simpleui_xml.activity_fragment.fragment.FragmentContainerActivity
import kr.open.library.simpleui_xml.deviceverification.harness.PhysicalDeviceRule
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class FragmentLifecycleIntegrationTest {
    @get:Rule
    val physicalDeviceRule = PhysicalDeviceRule()

    @Test
    fun xmlP0002_baseFragmentsRetainValidViewLifecycleAfterRecreation() {
        verifyFragment(
            fragmentType = ActivityFragmentActivity.BASE_FRAGMENT,
            expectedClass = BaseFragmentExample::class.java,
        )
        verifyFragment(
            fragmentType = ActivityFragmentActivity.BASE_BINDING_FRAGMENT,
            expectedClass = BaseBindingFragmentExample::class.java,
        )
    }

    private fun verifyFragment(fragmentType: Int, expectedClass: Class<*>) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val intent = Intent(context, FragmentContainerActivity::class.java)
            .putExtra("FRAGMENT_TYPE", fragmentType)

        ActivityScenario.launch<FragmentContainerActivity>(intent).use { scenario ->
            scenario.onActivity { activity ->
                activity.supportFragmentManager.executePendingTransactions()
                val fragment = activity.supportFragmentManager.findFragmentById(R.id.fragmentContainer)
                assertTrue(expectedClass.isInstance(fragment))
                assertNotNull(fragment?.view)
                assertTrue(
                    fragment
                        ?.viewLifecycleOwner
                        ?.lifecycle
                        ?.currentState
                        ?.isAtLeast(androidx.lifecycle.Lifecycle.State.STARTED) == true)
            }

            scenario.recreate()
            scenario.onActivity { activity ->
                activity.supportFragmentManager.executePendingTransactions()
                val fragment = activity.supportFragmentManager.findFragmentById(R.id.fragmentContainer)
                assertTrue(expectedClass.isInstance(fragment))
                assertNotNull(fragment?.view)
            }
        }
    }
}
