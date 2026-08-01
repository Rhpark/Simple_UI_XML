package kr.open.library.simple_ui.system_manager.robolectric.core.info.network.sim

import android.Manifest
import android.app.Application
import android.content.Context
import android.os.Build
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.system_manager.core.info.network.sim.SimInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.S])
class SimInfoPermissionRobolectricTest {
    private lateinit var application: Application
    private lateinit var telephonyManager: TelephonyManager
    private lateinit var subscriptionManager: SubscriptionManager

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
        telephonyManager = mock(TelephonyManager::class.java)
        subscriptionManager = mock(SubscriptionManager::class.java)

        val shadowApp = Shadows.shadowOf(application)
        shadowApp.grantPermissions(Manifest.permission.READ_PHONE_STATE)
        shadowApp.denyPermissions(
            Manifest.permission.READ_PHONE_NUMBERS,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
        shadowApp.setSystemService(Context.TELEPHONY_SERVICE, telephonyManager)
        shadowApp.setSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE, subscriptionManager)
    }

    @Test
    fun phoneStateApis_workWithoutUnrelatedOptionalPermissions() {
        val subscriptionInfo = mock(SubscriptionInfo::class.java)
        doReturn(0).`when`(subscriptionInfo).simSlotIndex
        doReturn(1).`when`(subscriptionInfo).subscriptionId
        doReturn(1).`when`(subscriptionManager).activeSubscriptionInfoCount
        doReturn(listOf(subscriptionInfo)).`when`(subscriptionManager).activeSubscriptionInfoList
        doReturn(1).`when`(telephonyManager).subscriptionId
        doReturn(telephonyManager).`when`(telephonyManager).createForSubscriptionId(1)

        val simInfo = SimInfo(application)

        assertEquals(1, simInfo.getActiveSimCount())
        assertEquals(listOf(0), simInfo.getActiveSimSlotIndexList())
        assertSame(subscriptionInfo, simInfo.getActiveSubscriptionInfoList().single())
    }
}
