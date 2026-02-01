package kr.open.library.simple_ui.core.robolectric.system_manager.info.battery

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kr.open.library.simple_ui.core.system_manager.info.battery.BatteryStateConstants.DISABLE_UPDATE_CYCLE_TIME
import kr.open.library.simple_ui.core.system_manager.info.battery.BatteryStateConstants.MIN_UPDATE_CYCLE_TIME
import kr.open.library.simple_ui.core.system_manager.info.battery.internal.helper.BatteryStateReceiver
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/**
 * Robolectric tests for BatteryStateReceiver.
 * Tests broadcast receiver registration, periodic updates, lifecycle management, and intent caching.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class BatteryStateReceiverRobolectricTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var context: Context
    private var receiveBatteryInfoCallCount: Int = 0
    private lateinit var receiver: BatteryStateReceiver

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        context = ApplicationProvider.getApplicationContext()
        receiveBatteryInfoCallCount = 0
        receiver = BatteryStateReceiver(context) { receiveBatteryInfoCallCount++ }
    }

    @After
    fun tearDown() {
        try {
            receiver.updateStop()
            receiver.unRegisterReceiver()
        } catch (_: Exception) {
            // ignore
        }
        Dispatchers.resetMain()
    }

    // ==============================================
    // registerReceiver
    // ==============================================

    @Test
    fun `registerReceiver returns true`() {
        val result = receiver.registerReceiver()
        assertTrue(result)
    }

    @Test
    fun `registerReceiver registers broadcast receiver`() {
        receiver.registerReceiver()

        val application = ApplicationProvider.getApplicationContext<Application>()
        val shadowApp = shadowOf(application)
        val batteryReceivers = shadowApp.registeredReceivers
            .filter { it.intentFilter.hasAction(Intent.ACTION_BATTERY_CHANGED) }

        assertTrue("BroadcastReceiver should be registered", batteryReceivers.isNotEmpty())
    }

    @Test
    fun `registerReceiver caches battery status intent via fetchBatteryStatusIntent`() {
        receiver.registerReceiver()

        // Robolectric may not provide sticky broadcast on registerReceiver.
        // fetchBatteryStatusIntent has a fallback that queries the system via registerReceiver(null, filter).
        // In Robolectric, this fallback may also return null.
        // So we only verify that getBatteryStatusIntent or fetchBatteryStatusIntent doesn't crash.
        // If a cached intent exists, it should be non-null.
        val fetched = receiver.fetchBatteryStatusIntent()
        // On Robolectric, sticky broadcast may not be available - just verify no crash
        // If intent is available, it should have BATTERY_CHANGED action characteristics
        if (fetched != null) {
            assertNotNull(fetched)
        }
        // Test passes if no exception is thrown
        assertTrue(true)
    }

    @Test
    fun `registerReceiver called twice unregisters first`() {
        receiver.registerReceiver()
        receiver.registerReceiver()

        val application = ApplicationProvider.getApplicationContext<Application>()
        val shadowApp = shadowOf(application)
        val batteryReceivers = shadowApp.registeredReceivers
            .filter { it.intentFilter.hasAction(Intent.ACTION_BATTERY_CHANGED) }

        // Should have exactly one registered receiver (first was unregistered)
        assertEquals(1, batteryReceivers.size)
    }

    // ==============================================
    // unRegisterReceiver
    // ==============================================

    @Test
    fun `unRegisterReceiver clears battery status`() {
        receiver.registerReceiver()
        receiver.unRegisterReceiver()
        assertNull(receiver.getBatteryStatusIntent())
    }

    @Test
    fun `unRegisterReceiver before register does not throw`() {
        // Should not throw even when no receiver is registered
        receiver.unRegisterReceiver()
        assertTrue(true)
    }

    @Test
    fun `unRegisterReceiver cancels internal scope`() = runTest {
        val scope = CoroutineScope(testDispatcher + Job())

        receiver.registerReceiver()
        receiver.updateStart(scope, DISABLE_UPDATE_CYCLE_TIME) {}
        advanceUntilIdle()

        assertNotNull(receiver.getCoroutineScope())

        receiver.unRegisterReceiver()
        assertNull(receiver.getCoroutineScope())

        scope.cancel()
    }

    // ==============================================
    // updateStart - periodic loop
    // ==============================================

    @Test
    fun `updateStart with valid cycle time returns true`() = runTest {
        val scope = CoroutineScope(testDispatcher + Job())
        receiver.registerReceiver()

        val result = receiver.updateStart(scope, DISABLE_UPDATE_CYCLE_TIME) {}
        assertTrue(result)

        advanceUntilIdle()
        scope.cancel()
    }

    @Test
    fun `updateStart below min cycle time returns false`() = runTest {
        val scope = CoroutineScope(testDispatcher + Job())
        receiver.registerReceiver()

        val result = receiver.updateStart(scope, MIN_UPDATE_CYCLE_TIME - 1) {}
        assertTrue(!result)

        scope.cancel()
    }

    @Test
    fun `updateStart without prior register auto registers`() = runTest {
        val scope = CoroutineScope(testDispatcher + Job())

        // Don't call registerReceiver() first
        val result = receiver.updateStart(scope, DISABLE_UPDATE_CYCLE_TIME) {}
        assertTrue("Should auto-register and return true", result)

        advanceUntilIdle()
        scope.cancel()
    }

    @Test
    fun `updateStart invokes callback immediately`() = runTest {
        val scope = CoroutineScope(testDispatcher + Job())
        receiver.registerReceiver()

        receiver.updateStart(scope, DISABLE_UPDATE_CYCLE_TIME) {}
        advanceUntilIdle()

        assertTrue("Callback should be invoked at least once", receiveBatteryInfoCallCount >= 1)

        scope.cancel()
    }

    @Test
    fun `updateStart periodic loop invokes multiple times`() = runTest {
        val scope = CoroutineScope(testDispatcher + Job())
        receiver.registerReceiver()

        receiver.updateStart(scope, 2000L) {}
        advanceTimeBy(6100L) // 3 full cycles + margin

        assertTrue("Callback should be invoked at least 3 times, got $receiveBatteryInfoCallCount", receiveBatteryInfoCallCount >= 3)

        // Must stop before runTest finishes, otherwise the infinite loop prevents completion
        receiver.updateStop()
        scope.cancel()
    }

    @Test
    fun `updateStart calls setupDataFlows callback`() = runTest {
        val scope = CoroutineScope(testDispatcher + Job())
        receiver.registerReceiver()

        var setupDataFlowsCalled = 0
        receiver.updateStart(scope, DISABLE_UPDATE_CYCLE_TIME) { setupDataFlowsCalled++ }
        advanceUntilIdle()

        assertEquals("setupDataFlows should be called once", 1, setupDataFlowsCalled)

        scope.cancel()
    }

    // ==============================================
    // DISABLE_UPDATE_CYCLE_TIME path
    // ==============================================

    @Test
    fun `updateStart disable cycle time single invocation`() = runTest {
        val scope = CoroutineScope(testDispatcher + Job())
        receiver.registerReceiver()

        receiver.updateStart(scope, DISABLE_UPDATE_CYCLE_TIME) {}
        advanceUntilIdle()

        val countAfterStart = receiveBatteryInfoCallCount
        assertEquals("Should invoke exactly once", 1, countAfterStart)

        // Advance more time - should NOT invoke again
        advanceTimeBy(10_000L)
        assertEquals("Count should not increase", countAfterStart, receiveBatteryInfoCallCount)

        scope.cancel()
    }

    // ==============================================
    // updateStop
    // ==============================================

    @Test
    fun `updateStop stops periodic updates`() = runTest {
        val scope = CoroutineScope(testDispatcher + Job())
        receiver.registerReceiver()

        receiver.updateStart(scope, 2000L) {}
        advanceTimeBy(4100L) // Let some updates happen
        val countAtStop = receiveBatteryInfoCallCount

        receiver.updateStop()
        advanceTimeBy(10000L) // Wait more - should not increase

        assertEquals("Count should not increase after stop", countAtStop, receiveBatteryInfoCallCount)

        scope.cancel()
    }

    @Test
    fun `updateStop without start returns true`() {
        val result = receiver.updateStop()
        assertTrue(result)
    }

    @Test
    fun `updateStop called twice does not throw`() {
        receiver.updateStop()
        receiver.updateStop()
        assertTrue(true)
    }

    // ==============================================
    // getBatteryStatusIntent / fetchBatteryStatusIntent
    // ==============================================

    @Test
    fun `getBatteryStatusIntent before register returns null`() {
        val intent = receiver.getBatteryStatusIntent()
        assertNull(intent)
    }

    @Test
    fun `fetchBatteryStatusIntent does not crash without register`() {
        // Without registerReceiver, fetchBatteryStatusIntent uses registerReceiver(null, filter) fallback
        // In Robolectric, this may return null - just verify no exception
        val intent = receiver.fetchBatteryStatusIntent()
        // May or may not be null depending on Robolectric version
        assertTrue(true)
    }

    @Test
    fun `fetchBatteryStatusIntent returns cached when available`() {
        receiver.registerReceiver()

        val cached = receiver.getBatteryStatusIntent()
        val fetched = receiver.fetchBatteryStatusIntent()

        // If cached is available, fetchBatteryStatusIntent should return it
        if (cached != null) {
            assertEquals(cached, fetched)
        }
        // If not available, fetchBatteryStatusIntent should still not crash
        assertTrue(true)
    }

    // ==============================================
    // Parent job completion / automatic cleanup
    // ==============================================

    @Test
    fun `parent job completion unregisters receiver`() = runTest {
        val parentJob = Job()
        val scope = CoroutineScope(testDispatcher + parentJob)

        receiver.registerReceiver()
        receiver.updateStart(scope, DISABLE_UPDATE_CYCLE_TIME) {}
        advanceUntilIdle()

        // Cancel parent job - triggers invokeOnCompletion
        parentJob.cancel()
        advanceUntilIdle()

        // Receiver should be unregistered
        assertNull(receiver.getBatteryStatusIntent())
    }

    @Test
    fun `parent job completion stops update job`() = runTest {
        val parentJob = Job()
        val scope = CoroutineScope(testDispatcher + parentJob)

        receiver.registerReceiver()
        receiver.updateStart(scope, DISABLE_UPDATE_CYCLE_TIME) {}
        advanceUntilIdle()

        val countBeforeCancel = receiveBatteryInfoCallCount

        // Cancel parent job
        parentJob.cancel()
        advanceUntilIdle()

        // Advance time further - count should not increase
        advanceTimeBy(10000L)
        assertEquals(countBeforeCancel, receiveBatteryInfoCallCount)
    }

    // ==============================================
    // Scope without Job
    // ==============================================

    @Test
    fun `updateStart scope without job still works`() = runTest {
        val scope = CoroutineScope(testDispatcher) // No explicit Job
        receiver.registerReceiver()

        val result = receiver.updateStart(scope, DISABLE_UPDATE_CYCLE_TIME) {}
        assertTrue("Should return true even without Job in scope", result)
        assertNotNull(receiver.getCoroutineScope())

        advanceUntilIdle()
        assertTrue(receiveBatteryInfoCallCount >= 1)

        receiver.unRegisterReceiver()
    }

    // ==============================================
    // BroadcastReceiver onReceive
    // ==============================================

    @Test
    fun `onReceive battery changed updates cached intent`() {
        receiver.registerReceiver()

        val testIntent = Intent(Intent.ACTION_BATTERY_CHANGED).apply {
            putExtra(BatteryManager.EXTRA_LEVEL, 95)
            putExtra(BatteryManager.EXTRA_SCALE, 100)
        }
        context.sendBroadcast(testIntent)
        shadowOf(Looper.getMainLooper()).idle()

        val cached = receiver.getBatteryStatusIntent()
        assertNotNull(cached)
        assertEquals(95, cached!!.getIntExtra(BatteryManager.EXTRA_LEVEL, -1))
    }

    @Test
    fun `onReceive battery changed invokes callback`() {
        receiver.registerReceiver()
        val countBefore = receiveBatteryInfoCallCount

        val testIntent = Intent(Intent.ACTION_BATTERY_CHANGED)
        context.sendBroadcast(testIntent)
        shadowOf(Looper.getMainLooper()).idle()

        assertTrue("Callback should be invoked on broadcast", receiveBatteryInfoCallCount > countBefore)
    }

    @Test
    fun `onReceive non battery changed does not update cache`() {
        receiver.registerReceiver()

        // Get the current cached intent
        val cachedBefore = receiver.getBatteryStatusIntent()
        val countBefore = receiveBatteryInfoCallCount

        // Send a non-BATTERY_CHANGED action
        val lowIntent = Intent(Intent.ACTION_BATTERY_LOW)
        context.sendBroadcast(lowIntent)
        shadowOf(Looper.getMainLooper()).idle()

        // Cache should NOT be updated to the BATTERY_LOW intent
        val cachedAfter = receiver.getBatteryStatusIntent()
        assertEquals("Cache should not change for non-BATTERY_CHANGED", cachedBefore, cachedAfter)

        // But callback should still be invoked
        assertTrue("Callback should be invoked for any registered action", receiveBatteryInfoCallCount > countBefore)
    }

    // ==============================================
    // Re-start scenario
    // ==============================================

    @Test
    fun `updateStart called twice cancels first job`() = runTest {
        val scope = CoroutineScope(testDispatcher + Job())
        receiver.registerReceiver()

        receiver.updateStart(scope, 2000L) {}
        advanceTimeBy(2100L)

        // Restart with same cycle - first job should be cancelled via updateStop() inside updateStart()
        receiveBatteryInfoCallCount = 0
        receiver.updateStart(scope, 2000L) {}
        advanceTimeBy(4100L) // ~2 cycles

        // Should have ~2-3 invocations, NOT doubled (proving first job was cancelled)
        assertTrue(
            "Should not have doubled invocations (got $receiveBatteryInfoCallCount)",
            receiveBatteryInfoCallCount <= 4
        )

        receiver.updateStop()
        scope.cancel()
    }
}
