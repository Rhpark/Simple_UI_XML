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
        verify(vibrator).vibrate(any(VibrationEffect::class.java))
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

        val result =
            controller.createWaveform(
                longArrayOf(0L, 10L, 20L),
                intArrayOf(0, 180, 255),
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

    // ========== Input Validation Tests ==========

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun createOneShot_withZeroTimer_returnsFalse() {
        grantVibratePermission()
        val controller = VibratorController(application)

        val result = controller.createOneShot(0L)

        assertFalse(result)
        verifyNoInteractions(vibrator)
        verifyNoInteractions(vibratorManager)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun createOneShot_withNegativeTimer_returnsFalse() {
        grantVibratePermission()
        val controller = VibratorController(application)

        val result = controller.createOneShot(-100L)

        assertFalse(result)
        verifyNoInteractions(vibrator)
        verifyNoInteractions(vibratorManager)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun createOneShot_withInvalidEffectNegative_returnsFalse() {
        grantVibratePermission()
        val controller = VibratorController(application)

        val result = controller.createOneShot(100L, effect = -2)

        assertFalse(result)
        verifyNoInteractions(vibrator)
        verifyNoInteractions(vibratorManager)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun createOneShot_withInvalidEffectTooLarge_returnsFalse() {
        grantVibratePermission()
        val controller = VibratorController(application)

        val result = controller.createOneShot(100L, effect = 256)

        assertFalse(result)
        verifyNoInteractions(vibrator)
        verifyNoInteractions(vibratorManager)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun createOneShot_withValidEffect_succeeds() {
        grantVibratePermission()
        val controller = VibratorController(application)

        val result = controller.createOneShot(100L, effect = 128)

        assertTrue(result)
        verify(vibrator).vibrate(any(VibrationEffect::class.java))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun vibrate_withZeroMilliseconds_returnsFalse() {
        grantVibratePermission()
        val controller = VibratorController(application)

        val result = controller.vibrate(0L)

        assertFalse(result)
        verifyNoInteractions(vibrator)
        verifyNoInteractions(vibratorManager)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun vibrate_withNegativeMilliseconds_returnsFalse() {
        grantVibratePermission()
        val controller = VibratorController(application)

        val result = controller.vibrate(-50L)

        assertFalse(result)
        verifyNoInteractions(vibrator)
        verifyNoInteractions(vibratorManager)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun createWaveform_withEmptyTimesArray_returnsFalse() {
        grantVibratePermission()
        val controller = VibratorController(application)

        val result = controller.createWaveform(longArrayOf(), intArrayOf())

        assertFalse(result)
        verifyNoInteractions(vibrator)
        verifyNoInteractions(vibratorManager)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun createWaveform_withEmptyAmplitudesArray_returnsFalse() {
        grantVibratePermission()
        val controller = VibratorController(application)

        val result = controller.createWaveform(longArrayOf(100L), intArrayOf())

        assertFalse(result)
        verifyNoInteractions(vibrator)
        verifyNoInteractions(vibratorManager)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun createWaveform_withMismatchedArraySizes_returnsFalse() {
        grantVibratePermission()
        val controller = VibratorController(application)

        val result = controller.createWaveform(
            longArrayOf(0L, 100L, 200L),
            intArrayOf(0, 128) // size mismatch
        )

        assertFalse(result)
        verifyNoInteractions(vibrator)
        verifyNoInteractions(vibratorManager)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun createWaveform_withNegativeTime_returnsFalse() {
        grantVibratePermission()
        val controller = VibratorController(application)

        val result = controller.createWaveform(
            longArrayOf(0L, -100L, 200L),
            intArrayOf(0, 128, 255)
        )

        assertFalse(result)
        verifyNoInteractions(vibrator)
        verifyNoInteractions(vibratorManager)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun createWaveform_withInvalidAmplitudeTooLarge_returnsFalse() {
        grantVibratePermission()
        val controller = VibratorController(application)

        val result = controller.createWaveform(
            longArrayOf(0L, 100L, 200L),
            intArrayOf(0, 128, 256) // 256 exceeds max
        )

        assertFalse(result)
        verifyNoInteractions(vibrator)
        verifyNoInteractions(vibratorManager)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun createWaveform_withInvalidAmplitudeNegative_returnsFalse() {
        grantVibratePermission()
        val controller = VibratorController(application)

        val result = controller.createWaveform(
            longArrayOf(0L, 100L, 200L),
            intArrayOf(0, -2, 255) // -2 is not DEFAULT_AMPLITUDE and out of range
        )

        assertFalse(result)
        verifyNoInteractions(vibrator)
        verifyNoInteractions(vibratorManager)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun createWaveform_withDefaultAmplitude_succeeds() {
        grantVibratePermission()
        val controller = VibratorController(application)

        val result = controller.createWaveform(
            longArrayOf(0L, 100L),
            intArrayOf(VibrationEffect.DEFAULT_AMPLITUDE, 128)
        )

        assertTrue(result)
        verify(vibrator).vibrate(any(VibrationEffect::class.java))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun createWaveform_withInvalidRepeatTooLarge_returnsFalse() {
        grantVibratePermission()
        val controller = VibratorController(application)

        val result = controller.createWaveform(
            longArrayOf(0L, 100L),
            intArrayOf(0, 128),
            repeat = 2 // index out of bounds (size is 2, max index is 1)
        )

        assertFalse(result)
        verifyNoInteractions(vibrator)
        verifyNoInteractions(vibratorManager)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun createWaveform_withInvalidRepeatNegativeNotMinusOne_returnsFalse() {
        grantVibratePermission()
        val controller = VibratorController(application)

        val result = controller.createWaveform(
            longArrayOf(0L, 100L),
            intArrayOf(0, 128),
            repeat = -2 // only -1 is valid for no repeat
        )

        assertFalse(result)
        verifyNoInteractions(vibrator)
        verifyNoInteractions(vibratorManager)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun createWaveform_withValidRepeat_succeeds() {
        grantVibratePermission()
        val controller = VibratorController(application)

        val result = controller.createWaveform(
            longArrayOf(0L, 100L, 200L),
            intArrayOf(0, 128, 255),
            repeat = 1 // valid index
        )

        assertTrue(result)
        verify(vibrator).vibrate(any(VibrationEffect::class.java))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun vibratePattern_withEmptyPattern_returnsFalse() {
        grantVibratePermission()
        val controller = VibratorController(application)

        val result = controller.vibratePattern(longArrayOf())

        assertFalse(result)
        verifyNoInteractions(vibrator)
        verifyNoInteractions(vibratorManager)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun vibratePattern_withInvalidRepeatTooLarge_returnsFalse() {
        grantVibratePermission()
        val controller = VibratorController(application)

        val result = controller.vibratePattern(
            longArrayOf(0L, 100L, 200L),
            repeat = 3 // index out of bounds
        )

        assertFalse(result)
        verifyNoInteractions(vibrator)
        verifyNoInteractions(vibratorManager)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun vibratePattern_withInvalidRepeatNegativeNotMinusOne_returnsFalse() {
        grantVibratePermission()
        val controller = VibratorController(application)

        val result = controller.vibratePattern(
            longArrayOf(0L, 100L),
            repeat = -3
        )

        assertFalse(result)
        verifyNoInteractions(vibrator)
        verifyNoInteractions(vibratorManager)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun vibratePattern_withValidRepeat_succeeds() {
        grantVibratePermission()
        val controller = VibratorController(application)

        val result = controller.vibratePattern(
            longArrayOf(0L, 100L, 200L),
            repeat = 0
        )

        assertTrue(result)
        verify(vibrator).vibrate(any(VibrationEffect::class.java))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun hasVibrator_withoutPermission_stillReturnsTrue() {
        // hasVibrator should work without permission
        doReturn(true).`when`(vibrator).hasVibrator()
        val controller = VibratorController(application)

        val result = controller.hasVibrator()

        assertTrue(result)
        verify(vibrator).hasVibrator()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun hasVibrator_withoutPermission_returnsFalseWhenNoHardware() {
        // hasVibrator should work without permission
        doReturn(false).`when`(vibrator).hasVibrator()
        val controller = VibratorController(application)

        val result = controller.hasVibrator()

        assertFalse(result)
        verify(vibrator).hasVibrator()
    }

    private fun shadowApp() = Shadows.shadowOf(application)

    private fun grantVibratePermission() {
        shadowApp().grantPermissions(Manifest.permission.VIBRATE)
    }
}
