package kr.open.library.simple_ui.core.robolectric.system_manager.controller.vibrator

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PermissionInfo
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.core.system_manager.controller.vibrator.VibratorController
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class VibratorControllerRobolectricTest {

    private lateinit var application: Application
    private lateinit var vibrator: Vibrator
    private lateinit var vibratorManager: VibratorManager

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
        val shadowApp = Shadows.shadowOf(application)

        val permissionInfo = PermissionInfo().apply {
            name = Manifest.permission.VIBRATE
            protectionLevel = PermissionInfo.PROTECTION_DANGEROUS
        }
        Shadows.shadowOf(application.packageManager).addPermissionInfo(permissionInfo)

        vibrator = mock(Vibrator::class.java)
        vibratorManager = mock(VibratorManager::class.java)

        shadowApp.setSystemService(Context.VIBRATOR_SERVICE, vibrator)
        shadowApp.setSystemService(Context.VIBRATOR_MANAGER_SERVICE, vibratorManager)
        shadowApp.denyPermissions(Manifest.permission.VIBRATE)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun createOneShot_preQ_usesLegacyVibrator() {
        grantVibratePermission()
        val controller = VibratorController(application)

        val result = controller.createOneShot(200L)

        assertTrue(result)
        verify(vibrator).vibrate(200L)
        verifyNoInteractions(vibratorManager)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun createOneShot_q_usesVibrationEffect() {
        grantVibratePermission()
        val controller = VibratorController(application)

        val result = controller.createOneShot(150L)

        assertTrue(result)
        val effectCaptor = ArgumentCaptor.forClass(VibrationEffect::class.java)
        verify(vibrator).vibrate(effectCaptor.capture())
        assertNotNull(effectCaptor.value)
        verifyNoInteractions(vibratorManager)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun createOneShot_s_usesVibratorManager() {
        grantVibratePermission()
        val controller = VibratorController(application)

        val result = controller.createOneShot(150L)

        assertTrue(result)
        val combinedCaptor = ArgumentCaptor.forClass(CombinedVibration::class.java)
        verify(vibratorManager).vibrate(combinedCaptor.capture())
        assertNotNull(combinedCaptor.value)
        verifyNoInteractions(vibrator)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun vibrate_s_usesVibratorManager() {
        grantVibratePermission()
        val controller = VibratorController(application)

        val result = controller.vibrate(120L)

        assertTrue(result)
        val combinedCaptor = ArgumentCaptor.forClass(CombinedVibration::class.java)
        verify(vibratorManager).vibrate(combinedCaptor.capture())
        assertNotNull(combinedCaptor.value)
        verifyNoInteractions(vibrator)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun vibrate_preS_usesLegacyVibrator() {
        grantVibratePermission()
        val controller = VibratorController(application)

        val result = controller.vibrate(90L)

        assertTrue(result)
        val effectCaptor = ArgumentCaptor.forClass(VibrationEffect::class.java)
        verify(vibrator).vibrate(effectCaptor.capture())
        assertNotNull(effectCaptor.value)
        verifyNoInteractions(vibratorManager)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun createWaveform_s_usesVibratorManager() {
        grantVibratePermission()
        val controller = VibratorController(application)

        val result = controller.createWaveform(
            longArrayOf(0L, 10L, 20L),
            intArrayOf(0, 180, 255)
        )

        assertTrue(result)
        val combinedCaptor = ArgumentCaptor.forClass(CombinedVibration::class.java)
        verify(vibratorManager).vibrate(combinedCaptor.capture())
        assertNotNull(combinedCaptor.value)
        verifyNoInteractions(vibrator)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun createWaveform_preS_usesLegacyVibrator() {
        grantVibratePermission()
        val controller = VibratorController(application)

        val result = controller.createWaveform(longArrayOf(0L, 5L), intArrayOf(0, 128))

        assertTrue(result)
        val effectCaptor = ArgumentCaptor.forClass(VibrationEffect::class.java)
        verify(vibrator).vibrate(effectCaptor.capture())
        assertNotNull(effectCaptor.value)
        verifyNoInteractions(vibratorManager)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun vibratePattern_preS_usesLegacyVibrator() {
        grantVibratePermission()
        val controller = VibratorController(application)

        val result = controller.vibratePattern(longArrayOf(0L, 25L, 50L), repeat = 1)

        assertTrue(result)
        val effectCaptor = ArgumentCaptor.forClass(VibrationEffect::class.java)
        verify(vibrator).vibrate(effectCaptor.capture())
        assertNotNull(effectCaptor.value)
        verifyNoInteractions(vibratorManager)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun createPredefined_s_usesVibratorManager() {
        grantVibratePermission()
        val controller = VibratorController(application)

        val result = controller.createPredefined(VibrationEffect.EFFECT_CLICK)

        assertTrue(result)
        val combinedCaptor = ArgumentCaptor.forClass(CombinedVibration::class.java)
        verify(vibratorManager).vibrate(combinedCaptor.capture())
        assertNotNull(combinedCaptor.value)
        verifyNoInteractions(vibrator)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun createPredefined_preS_usesLegacyVibrator() {
        grantVibratePermission()
        val controller = VibratorController(application)

        val result = controller.createPredefined(VibrationEffect.EFFECT_CLICK)

        assertTrue(result)
        val effectCaptor = ArgumentCaptor.forClass(VibrationEffect::class.java)
        verify(vibrator).vibrate(effectCaptor.capture())
        assertNotNull(effectCaptor.value)
        verifyNoInteractions(vibratorManager)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun vibratePattern_s_usesVibratorManager() {
        grantVibratePermission()
        val controller = VibratorController(application)

        val result = controller.vibratePattern(longArrayOf(0L, 30L, 60L), repeat = 0)

        assertTrue(result)
        val combinedCaptor = ArgumentCaptor.forClass(CombinedVibration::class.java)
        verify(vibratorManager).vibrate(combinedCaptor.capture())
        assertNotNull(combinedCaptor.value)
        verifyNoInteractions(vibrator)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun cancel_preS_callsVibrator() {
        grantVibratePermission()
        val controller = VibratorController(application)

        val result = controller.cancel()

        assertTrue(result)
        verify(vibrator).cancel()
        verifyNoInteractions(vibratorManager)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun cancel_s_callsVibratorManager() {
        grantVibratePermission()
        val controller = VibratorController(application)

        val result = controller.cancel()

        assertTrue(result)
        verify(vibratorManager).cancel()
        verifyNoInteractions(vibrator)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun hasVibrator_preS_delegatesToLegacyService() {
        grantVibratePermission()
        doReturn(true).`when`(vibrator).hasVibrator()
        val controller = VibratorController(application)

        assertTrue(controller.hasVibrator())
        verify(vibrator).hasVibrator()
        verifyNoInteractions(vibratorManager)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun hasVibrator_s_usesDefaultVibratorFromManager() {
        grantVibratePermission()
        doReturn(vibrator).`when`(vibratorManager).defaultVibrator
        doReturn(true).`when`(vibrator).hasVibrator()
        val controller = VibratorController(application)

        assertTrue(controller.hasVibrator())
        verify(vibratorManager).defaultVibrator
        verify(vibrator).hasVibrator()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun createOneShot_withoutPermission_returnsFalse() {
        val controller = VibratorController(application)

        val result = controller.createOneShot(100L)

        assertFalse(result)
        verifyNoInteractions(vibrator)
        verifyNoInteractions(vibratorManager)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun createOneShot_whenVibratorThrows_returnsFalse() {
        grantVibratePermission()
        val controller = VibratorController(application)
        doThrow(RuntimeException("boom"))
            .`when`(vibrator)
            .vibrate(any(VibrationEffect::class.java))

        val result = controller.createOneShot(100L)

        assertFalse(result)
    }

    private fun shadowApp() = Shadows.shadowOf(application)

    private fun grantVibratePermission() {
        shadowApp().grantPermissions(Manifest.permission.VIBRATE)
    }
}
