package kr.open.library.simple_ui.core.robolectric.system_manager.info.battery

import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.core.system_manager.info.battery.BatteryStateConstants
import kr.open.library.simple_ui.core.system_manager.info.battery.internal.helper.BatteryPropertyReader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Robolectric tests for BatteryPropertyReader.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class BatteryPropertyReaderRobolectricTest {
    private lateinit var context: Context
    private lateinit var batteryPropertyReader: BatteryPropertyReader

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        batteryPropertyReader = BatteryPropertyReader(context)
    }

    @Test
    fun `getStatus returns extra value when present`() {
        val intent = createBatteryIntent(status = BatteryManager.BATTERY_STATUS_CHARGING)
        val status = batteryPropertyReader.getStatus(intent, BatteryManager.EXTRA_STATUS, 123)
        assertEquals(BatteryManager.BATTERY_STATUS_CHARGING, status)
    }

    @Test
    fun `getStatus returns default when extra missing`() {
        val intent = Intent(Intent.ACTION_BATTERY_CHANGED)
        val status = batteryPropertyReader.getStatus(intent, BatteryManager.EXTRA_STATUS, 777)
        assertEquals(777, status)
    }

    @Test
    fun `isChargingUsb Ac Wireless detect bitmask`() {
        val plug = BatteryManager.BATTERY_PLUGGED_USB or BatteryManager.BATTERY_PLUGGED_AC
        assertTrue(batteryPropertyReader.isChargingUsb(plug))
        assertTrue(batteryPropertyReader.isChargingAc(plug))
        assertTrue(!batteryPropertyReader.isChargingWireless(plug))
    }

    @Test
    fun `getChargePlugList returns expected labels`() {
        val plug = BatteryManager.BATTERY_PLUGGED_USB or BatteryManager.BATTERY_PLUGGED_AC
        val labels = batteryPropertyReader.getChargePlugList(plug)
        assertTrue(labels.contains(BatteryStateConstants.STR_CHARGE_PLUG_USB))
        assertTrue(labels.contains(BatteryStateConstants.STR_CHARGE_PLUG_AC))
    }

    @Test
    fun `getChargePlugList returns NONE when not charging`() {
        val labels = batteryPropertyReader.getChargePlugList(0)
        assertEquals(listOf(BatteryStateConstants.STR_CHARGE_PLUG_NONE), labels)
    }

    @Test
    fun `getChargePlugList returns UNKNOWN when error`() {
        val labels = batteryPropertyReader.getChargePlugList(BatteryStateConstants.BATTERY_ERROR_VALUE)
        assertEquals(listOf(BatteryStateConstants.STR_CHARGE_PLUG_UNKNOWN), labels)
    }

    @Test
    fun `getTemperature converts raw value`() {
        val intent = createBatteryIntent(temperature = 350)
        val temp = batteryPropertyReader.getTemperature(intent)
        assertEquals(35.0, temp, 0.1)
    }

    @Test
    fun `getTemperature returns error for out of range`() {
        val intent = createBatteryIntent(temperature = 1300)
        val temp = batteryPropertyReader.getTemperature(intent)
        assertEquals(BatteryStateConstants.BATTERY_ERROR_VALUE_DOUBLE, temp, 0.1)
    }

    @Test
    fun `getTemperature returns error when extra missing`() {
        val intent = Intent(Intent.ACTION_BATTERY_CHANGED)
        val temp = batteryPropertyReader.getTemperature(intent)
        assertEquals(BatteryStateConstants.BATTERY_ERROR_VALUE_DOUBLE, temp, 0.1)
    }

    @Test
    fun `getVoltage converts millivolts to volts`() {
        val intent = createBatteryIntent(voltage = 4200)
        val voltage = batteryPropertyReader.getVoltage(intent)
        assertEquals(4.2, voltage, 0.01)
    }

    @Test
    fun `getVoltage returns error for missing or invalid value`() {
        val intent = createBatteryIntent(voltage = BatteryStateConstants.BATTERY_ERROR_VALUE)
        val voltage = batteryPropertyReader.getVoltage(intent)
        assertEquals(BatteryStateConstants.BATTERY_ERROR_VALUE_DOUBLE, voltage, 0.01)
    }

    @Test
    fun `getPresent returns true or false when present extra exists`() {
        val trueIntent = createBatteryIntent(present = true)
        val falseIntent = createBatteryIntent(present = false)
        assertEquals(true, batteryPropertyReader.getPresent(trueIntent))
        assertEquals(false, batteryPropertyReader.getPresent(falseIntent))
    }

    @Test
    fun `getPresent returns null when extra missing`() {
        val intent = Intent(Intent.ACTION_BATTERY_CHANGED)
        assertNull(batteryPropertyReader.getPresent(intent))
    }

    @Test
    fun `getHealth reads intent and maps to string`() {
        val intent = createBatteryIntent(health = BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE)
        val health = batteryPropertyReader.getHealth(intent)
        val healthStr = batteryPropertyReader.getHealthStr(health)
        assertEquals(BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE, health)
        assertEquals(BatteryStateConstants.STR_BATTERY_HEALTH_OVER_VOLTAGE, healthStr)
    }

    @Test
    fun `getTechnology returns string or null`() {
        val intent = createBatteryIntent(technology = "Li-ion")
        assertEquals("Li-ion", batteryPropertyReader.getTechnology(intent))
        val emptyIntent = Intent(Intent.ACTION_BATTERY_CHANGED)
        assertNull(batteryPropertyReader.getTechnology(emptyIntent))
    }

    @Test
    fun `getChargeStatus returns valid status`() {
        val intent = createBatteryIntent(status = BatteryManager.BATTERY_STATUS_CHARGING)
        val status = batteryPropertyReader.getChargeStatus(intent)
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
    fun `getTotalCapacity returns positive value or error`() {
        val capacity = batteryPropertyReader.getTotalCapacity(chargeCounter = 3000000, capacity = 50)
        assertTrue(capacity > 0.0 || capacity == BatteryStateConstants.BATTERY_ERROR_VALUE_DOUBLE)
    }

    // ==============================================
    // Temperature Boundary Tests
    // ==============================================

    @Test
    fun `getTemperature returns valid at minus 40 boundary`() {
        val intent = createBatteryIntent(temperature = -400) // -400 / 10 = -40.0°C
        val temp = batteryPropertyReader.getTemperature(intent)
        assertEquals(-40.0, temp, 0.01)
    }

    @Test
    fun `getTemperature returns valid at 120 boundary`() {
        val intent = createBatteryIntent(temperature = 1200) // 1200 / 10 = 120.0°C
        val temp = batteryPropertyReader.getTemperature(intent)
        assertEquals(120.0, temp, 0.01)
    }

    @Test
    fun `getTemperature returns error just beyond upper boundary`() {
        val intent = createBatteryIntent(temperature = 1210) // 1210 / 10 = 121.0°C → out of range
        val temp = batteryPropertyReader.getTemperature(intent)
        assertEquals(BatteryStateConstants.BATTERY_ERROR_VALUE_DOUBLE, temp, 0.01)
    }

    // ==============================================
    // Voltage Boundary Tests
    // ==============================================

    @Test
    fun `getVoltage returns error for zero millivolts`() {
        val intent = createBatteryIntent(voltage = 0) // 0 / 1000 = 0.0V → not > 0
        val voltage = batteryPropertyReader.getVoltage(intent)
        assertEquals(BatteryStateConstants.BATTERY_ERROR_VALUE_DOUBLE, voltage, 0.01)
    }

    @Test
    fun `getVoltage returns valid for positive millivolts`() {
        val intent = createBatteryIntent(voltage = 3700) // 3700 / 1000 = 3.7V
        val voltage = batteryPropertyReader.getVoltage(intent)
        assertEquals(3.7, voltage, 0.01)
    }

    private fun createBatteryIntent(
        plugged: Int = BatteryManager.BATTERY_PLUGGED_AC,
        status: Int = BatteryManager.BATTERY_STATUS_CHARGING,
        temperature: Int = 350,
        voltage: Int = 4200,
        health: Int = BatteryManager.BATTERY_HEALTH_GOOD,
        present: Boolean = true,
        technology: String = "Li-ion"
    ): Intent = Intent(Intent.ACTION_BATTERY_CHANGED).apply {
        putExtra(BatteryManager.EXTRA_PLUGGED, plugged)
        putExtra(BatteryManager.EXTRA_STATUS, status)
        putExtra(BatteryManager.EXTRA_TEMPERATURE, temperature)
        putExtra(BatteryManager.EXTRA_VOLTAGE, voltage)
        putExtra(BatteryManager.EXTRA_HEALTH, health)
        putExtra(BatteryManager.EXTRA_PRESENT, present)
        putExtra(BatteryManager.EXTRA_TECHNOLOGY, technology)
    }
}
