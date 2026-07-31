package kr.open.library.simpleui_xml.deviceverification.harness

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.TelephonyManager

/**
 * Captures device properties required to interpret a physical-device test result.<br><br>
 * 실기기 테스트 결과를 해석하는 데 필요한 단말 속성을 보관합니다.<br>
 */
internal data class DeviceEnvironmentSnapshot(
    val manufacturer: String,
    val model: String,
    val apiLevel: Int,
    val abi: String,
    val simState: Int,
    val hasWifi: Boolean,
    val hasLocation: Boolean,
    val hasTelephony: Boolean,
) {
    fun asDiagnosticText(): String =
        buildString {
            append("manufacturer=")
            append(manufacturer)
            append(", model=")
            append(model)
            append(", api=")
            append(apiLevel)
            append(", abi=")
            append(abi)
            append(", simState=")
            append(simState)
            append(", wifi=")
            append(hasWifi)
            append(", location=")
            append(hasLocation)
            append(", telephony=")
            append(hasTelephony)
        }
}

/**
 * Reads the current physical-device environment without mutating device state.<br><br>
 * 단말 상태를 변경하지 않고 현재 실기기 환경을 조회합니다.<br>
 */
internal object DeviceEnvironment {
    fun capture(context: Context): DeviceEnvironmentSnapshot {
        val packageManager = context.packageManager
        val telephonyManager = context.getSystemService(TelephonyManager::class.java)

        return DeviceEnvironmentSnapshot(
            manufacturer = Build.MANUFACTURER.orEmpty(),
            model = Build.MODEL.orEmpty(),
            apiLevel = Build.VERSION.SDK_INT,
            abi = Build.SUPPORTED_ABIS.firstOrNull().orEmpty(),
            simState = telephonyManager?.simState ?: TelephonyManager.SIM_STATE_UNKNOWN,
            hasWifi = packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI),
            hasLocation = packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION),
            hasTelephony = packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY),
        )
    }
}
