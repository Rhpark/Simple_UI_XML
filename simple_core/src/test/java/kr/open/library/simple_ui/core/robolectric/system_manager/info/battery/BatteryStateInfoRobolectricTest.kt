package kr.open.library.simple_ui.core.robolectric.system_manager.info.battery

import android.content.Context
import android.content.Intent
import android.content.pm.PermissionInfo
import android.os.BatteryManager
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.runTest
import kr.open.library.simple_ui.core.system_manager.info.battery.BatteryStateConstants
import kr.open.library.simple_ui.core.system_manager.info.battery.BatteryStateInfo
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/**
 * Robolectric tests for BatteryStateInfo.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class BatteryStateInfoRobolectricTest {
    private lateinit var context: Context
    private lateinit var batteryStateInfo: BatteryStateInfo

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        val shadowPackageManager = Shadows.shadowOf(context.packageManager)
        val permissionInfo = PermissionInfo().apply {
            name = android.Manifest.permission.BATTERY_STATS
            packageName = context.packageName
        }
        PermissionInfo::class.java.getDeclaredField("protectionLevel").apply {
            isAccessible = true
            setInt(permissionInfo, PermissionInfo.PROTECTION_SIGNATURE)
        }
        shadowPackageManager.addPermissionInfo(permissionInfo)
        batteryStateInfo = BatteryStateInfo(context)
    }

    @After
    fun tearDown() {
        try {
            batteryStateInfo.onDestroy()
        } catch (_: Exception) {
            // ignore
        }

        try {
            val application = ApplicationProvider.getApplicationContext<android.app.Application>()
            val shadowApp = shadowOf(application)
            shadowApp.registeredReceivers
                .filter { it.intentFilter.hasAction(Intent.ACTION_BATTERY_CHANGED) }
                .forEach { receiverInfo ->
                    try {
                        context.unregisterReceiver(receiverInfo.broadcastReceiver)
                    } catch (_: Exception) {
                        // ignore
                    }
                }
        } catch (_: Exception) {
            // ignore
        }
    }

    @Test
    fun `BatteryStateInfo can be instantiated with context`() {
        val newInstance = BatteryStateInfo(context)
        assertTrue(newInstance != null)
        newInstance.onDestroy()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `registerStart throws when updateCycleTime below minimum`() = runTest {
        batteryStateInfo.registerStart(this, BatteryStateConstants.MIN_UPDATE_CYCLE_TIME - 1)
    }

    @Test
    fun `registerStart works with scope without Job`() = runTest {
        val separate = BatteryStateInfo(context)
        val scopeWithoutJob = CoroutineScope(Dispatchers.Default)

        val result = separate.registerStart(scopeWithoutJob, BatteryStateConstants.DISABLE_UPDATE_CYCLE_TIME)
        assertTrue(result)

        separate.unRegister()
        separate.onDestroy()
        scopeWithoutJob.cancel()
    }

    @Test
    fun `unRegister can be called safely`() {
        val separate = BatteryStateInfo(context)
        separate.unRegister()
        separate.onDestroy()
        assertTrue(true)
    }

    @Test
    fun `onDestroy multiple times does not throw`() {
        val separate = BatteryStateInfo(context)
        separate.onDestroy()
        separate.onDestroy()
        assertTrue(true)
    }

    @Test
    fun `isCharging helpers match charge status`() {
        val status = batteryStateInfo.getChargeStatus()
        assertEquals(status == BatteryManager.BATTERY_STATUS_CHARGING, batteryStateInfo.isCharging())
        assertEquals(status == BatteryManager.BATTERY_STATUS_DISCHARGING, batteryStateInfo.isDischarging())
        assertEquals(status == BatteryManager.BATTERY_STATUS_NOT_CHARGING, batteryStateInfo.isNotCharging())
        assertEquals(status == BatteryManager.BATTERY_STATUS_FULL, batteryStateInfo.isFull())
    }

    @Test
    fun `isChargingPlug helpers match plug bitmask`() {
        val plug = batteryStateInfo.getChargePlug()
        val isUsb = plug != BatteryStateConstants.BATTERY_ERROR_VALUE &&
            (plug and BatteryManager.BATTERY_PLUGGED_USB) != 0
        val isAc = plug != BatteryStateConstants.BATTERY_ERROR_VALUE &&
            (plug and BatteryManager.BATTERY_PLUGGED_AC) != 0
        val isWireless = plug != BatteryStateConstants.BATTERY_ERROR_VALUE &&
            (plug and BatteryManager.BATTERY_PLUGGED_WIRELESS) != 0

        assertEquals(isUsb, batteryStateInfo.isChargingUsb())
        assertEquals(isAc, batteryStateInfo.isChargingAc())
        assertEquals(isWireless, batteryStateInfo.isChargingWireless())
    }

    @Test
    fun `getChargePlugList returns consistent values`() {
        val plug = batteryStateInfo.getChargePlug()
        val result = batteryStateInfo.getChargePlugList()

        assertTrue(result.isNotEmpty())
        assertTrue(result.all { it.isNotEmpty() })

        when (plug) {
            BatteryStateConstants.BATTERY_ERROR_VALUE ->
                assertEquals(listOf(BatteryStateConstants.STR_CHARGE_PLUG_UNKNOWN), result)
            0 ->
                assertEquals(listOf(BatteryStateConstants.STR_CHARGE_PLUG_NONE), result)
        }
    }

    @Test
    fun `getCurrentHealthStr returns known value`() {
        val result = batteryStateInfo.getCurrentHealthStr()
        val valid = setOf(
            BatteryStateConstants.STR_BATTERY_HEALTH_GOOD,
            BatteryStateConstants.STR_BATTERY_HEALTH_COLD,
            BatteryStateConstants.STR_BATTERY_HEALTH_DEAD,
            BatteryStateConstants.STR_BATTERY_HEALTH_OVER_VOLTAGE,
            BatteryStateConstants.STR_BATTERY_HEALTH_UNKNOWN
        )
        assertTrue(result in valid)
    }

    @Test
    fun `getTemperature returns double within range or error`() {
        val temperature = batteryStateInfo.getTemperature()
        assertTrue(
            temperature == BatteryStateConstants.BATTERY_ERROR_VALUE_DOUBLE ||
                (temperature >= -40.0 && temperature <= 120.0)
        )
    }

    @Test
    fun `getVoltage returns double or error`() {
        val voltage = batteryStateInfo.getVoltage()
        assertTrue(voltage == BatteryStateConstants.BATTERY_ERROR_VALUE_DOUBLE || voltage > 0.0)
    }

    @Test
    fun `getCapacity returns percent or error`() {
        val capacity = batteryStateInfo.getCapacity()
        assertTrue(capacity == BatteryStateConstants.BATTERY_ERROR_VALUE || capacity in 0..100)
    }

    @Test
    fun `getChargeStatus returns valid status or error`() {
        val status = batteryStateInfo.getChargeStatus()
        val validStatuses = setOf(
            BatteryManager.BATTERY_STATUS_CHARGING,
            BatteryManager.BATTERY_STATUS_DISCHARGING,
            BatteryManager.BATTERY_STATUS_FULL,
            BatteryManager.BATTERY_STATUS_NOT_CHARGING,
            BatteryManager.BATTERY_STATUS_UNKNOWN,
            BatteryStateConstants.BATTERY_ERROR_VALUE,
            0
        )
        assertTrue(status in validStatuses)
    }

    @Test
    fun `getChargePlug returns value or error`() {
        val plug = batteryStateInfo.getChargePlug()
        assertTrue(plug == BatteryStateConstants.BATTERY_ERROR_VALUE || plug >= 0)
    }

    @Test
    fun `getHealth returns value or error`() {
        val health = batteryStateInfo.getHealth()
        assertTrue(health == BatteryStateConstants.BATTERY_ERROR_VALUE || health >= 0)
    }

    @Test
    fun `getPresent returns Boolean or null`() {
        val present = batteryStateInfo.getPresent()
        assertTrue(present == null || present is Boolean)
    }

    @Test
    fun `getTechnology returns String or null`() {
        val technology = batteryStateInfo.getTechnology()
        assertTrue(technology == null || technology.isNotEmpty())
    }

    @Test
    fun `getCurrentAmpere returns reasonable range or error`() {
        val current = batteryStateInfo.getCurrentAmpere()
        assertTrue(
            current == BatteryStateConstants.BATTERY_ERROR_VALUE ||
                current in -20_000_000..20_000_000
        )
    }

    @Test
    fun `getCurrentAverageAmpere returns reasonable range or error`() {
        val current = batteryStateInfo.getCurrentAverageAmpere()
        assertTrue(
            current == BatteryStateConstants.BATTERY_ERROR_VALUE ||
                current in -20_000_000..20_000_000
        )
    }

    @Test
    fun `getChargeCounter returns non-negative or error`() {
        val counter = batteryStateInfo.getChargeCounter()
        assertTrue(counter == BatteryStateConstants.BATTERY_ERROR_VALUE || counter >= 0)
    }

    @Test
    fun `getEnergyCounter returns non-negative or error`() {
        val energy = batteryStateInfo.getEnergyCounter()
        assertTrue(energy == BatteryStateConstants.BATTERY_ERROR_VALUE_LONG || energy >= 0)
    }

    @Test
    fun `getTotalCapacity returns positive value or error`() {
        val capacity = batteryStateInfo.getTotalCapacity()
        assertTrue(capacity > 0.0 || capacity == BatteryStateConstants.BATTERY_ERROR_VALUE_DOUBLE)
    }
}
