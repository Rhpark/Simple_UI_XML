package kr.open.library.simple_ui.system_manager.info.battery.power

import android.annotation.SuppressLint
import android.content.Context
import android.os.BatteryManager
import android.os.Build
import kr.open.library.simple_ui.extensions.conditional.checkSdkVersion
import kr.open.library.simple_ui.extensions.trycatch.safeCatch
import kr.open.library.simple_ui.logcat.Logx
import java.lang.reflect.Method

/**
 * PowerProfile
 *
 * This class provides access to device power consumption information through the internal
 * Android PowerProfile class using reflection.
 * 
 * 이 클래스는 리플렉션을 사용하여 내부 Android PowerProfile 클래스를 통해
 * 기기의 전력 소비 정보에 접근할 수 있도록 합니다.
 *
 * The primary use case is to retrieve the battery's total capacity (rated capacity),
 * but it can also be used to get other power consumption metrics defined in [PowerProfileVO].
 * 
 * 주요 용도는 배터리의 총 용량(정격 용량)을 가져오는 것이지만,
 * [PowerProfileVO]에 정의된 다른 전력 소비 메트릭을 가져오는 데에도 사용할 수 있습니다.
 *
 * Note: Because this uses reflection to access internal Android APIs, it may not work
 * on all devices or future Android versions.
 * 
 * 참고: 내부 Android API에 접근하기 위해 리플렉션을 사용하므로,
 * 모든 기기나 향후 Android 버전에서 작동하지 않을 수 있습니다.
 *
 * @see PowerProfileVO for available power metrics
 */
@SuppressLint("PrivateApi")
public class PowerProfile(private val context: Context) {

    private val classNamePowerProfile: String = CLASS_NAME_POWER_PROFILE
    private val averagePower: String = METHOD_GET_AVERAGE_POWER

    companion object {
        /**
         * Internal Android PowerProfile class name.
         * 내부 Android PowerProfile 클래스 이름.
         */
        private const val CLASS_NAME_POWER_PROFILE = "com.android.internal.os.PowerProfile"
        
        /**
         * Method name for getting average power consumption.
         * 평균 전력 소비를 가져오는 메서드 이름.
         */
        private const val METHOD_GET_AVERAGE_POWER = "getAveragePower"
        
        /**
         * Default battery capacity when PowerProfile is unavailable.
         * PowerProfile을 사용할 수 없을 때의 기본 배터리 용량.
         */
        private const val DEFAULT_BATTERY_CAPACITY = 3000.0
    }

    // Lazy initialization to prevent app crash on init failure
    // 초기화 실패 시 앱 크래시를 방지하기 위한 지연 초기화
    private val powerProfileClass: Class<*>? by lazy {
        safeCatch(defaultValue = null) { Class.forName(classNamePowerProfile) }
    }
    
    private val getAveragePowerMethod: Method? by lazy {
        safeCatch(defaultValue =  null) {
            powerProfileClass?.getMethod(averagePower, String::class.java)
        }
    }
    
    private val getAveragePowerMethodWithInt: Method? by lazy {
        safeCatch(defaultValue = null) {
            powerProfileClass?.getMethod(averagePower, String::class.java, Int::class.javaPrimitiveType)
        }
    }
    
    private val powerProfileInstance: Any? by lazy {
        safeCatch(defaultValue = null) {
            powerProfileClass?.getConstructor(Context::class.java)?.newInstance(context)
        }
    }
    
    /**
     * Checks if PowerProfile is available on this device/Android version.
     * 이 기기/Android 버전에서 PowerProfile을 사용할 수 있는지 확인합니다.
     * 
     * @return True if PowerProfile is available, false otherwise
     * @return PowerProfile 사용 가능 시 true, 아니면 false
     */
    public fun isPowerProfileAvailable(): Boolean {
        return powerProfileInstance != null
    }

    /**
     * Retrieves the average power consumption for the specified power profile type.
     * 지정된 전력 프로파일 유형에 대한 평균 전력 소비를 가져옵니다.
     *
     * @param type The power profile type to retrieve from [PowerProfileVO]
     * @param type [PowerProfileVO]에서 가져올 전력 프로파일 유형
     * @return The average power consumption, or null if an error occurred
     * @return 평균 전력 소비량, 오류 발생 시 null
     */
    public fun getAveragePower(type: PowerProfileVO): Any? = safeCatch(defaultValue =  null) {
        if (!isPowerProfileAvailable()) {
            Logx.w("PowerProfile not available, cannot get average power for ${type.res}")
            return@safeCatch null
        }
        getAveragePowerMethod?.invoke(powerProfileInstance, type.res)
    }

    /**
     * Retrieves the average power consumption for the specified power profile type and index.
     * This is used for metrics that have multiple values (like CPU clusters).
     * 
     * 지정된 전력 프로파일 유형과 인덱스에 대한 평균 전력 소비를 가져옵니다.
     * 여러 값을 가진 메트릭(예: CPU 클러스터)에 사용됩니다.
     *
     * @param type The power profile type to retrieve from [PowerProfileVO]
     * @param type [PowerProfileVO]에서 가져올 전력 프로파일 유형
     * @param index The index of the power profile value
     * @param index 전력 프로파일 값의 인덱스
     * @return The average power consumption, or null if an error occurred
     * @return 평균 전력 소비량, 오류 발생 시 null
     */
    public fun getAveragePower(type: PowerProfileVO, index: Int): Any? = safeCatch(defaultValue =  null) {
        if (!isPowerProfileAvailable()) {
            Logx.w("PowerProfile not available, cannot get average power for ${type.res}[$index]")
            return@safeCatch null
        }
        getAveragePowerMethodWithInt?.invoke(powerProfileInstance, type.res, index)
    }

    /**
     * Gets the total battery capacity in milliampere-hours (mAh).
     * Uses multiple fallback methods for better compatibility.
     * 
     * 배터리의 총 용량을 밀리암페어시(mAh) 단위로 가져옵니다.
     * 더 나은 호환성을 위해 여러 fallback 방법을 사용합니다.
     *
     * @return The battery capacity in mAh, or default value if unable to retrieve
     * @return 배터리 용량(mAh), 가져올 수 없는 경우 기본값
     */
    public fun getBatteryCapacity(): Double = safeCatch(defaultValue = DEFAULT_BATTERY_CAPACITY) {
        // Try PowerProfile first (primary method)
        // PowerProfile을 먼저 시도 (주요 방법)
        val powerProfileCapacity = getAveragePower(PowerProfileVO.POWER_BATTERY_CAPACITY) as? Double
        if (powerProfileCapacity != null && powerProfileCapacity > 0) {
            return@safeCatch powerProfileCapacity
        }
        
        // Fallback to BatteryManager if available (API 21+)
        // BatteryManager로 fallback (API 21+)
        val batteryManagerCapacity = getBatteryCapacityFromBatteryManager()
        if (batteryManagerCapacity > 0) {
            return@safeCatch batteryManagerCapacity
        }
        
        // Last resort: return default capacity
        // 최후 수단: 기본 용량 반환
        Logx.w("Unable to retrieve battery capacity, using default: $DEFAULT_BATTERY_CAPACITY mAh")
        DEFAULT_BATTERY_CAPACITY
    }
    
    /**
     * Fallback method to estimate total battery capacity using BatteryManager.
     * This method calculates total capacity from current charge and percentage.
     * 
     * BatteryManager를 사용하여 총 배터리 용량을 추정하는 fallback 메서드.
     * 현재 충전량과 백분율로부터 총 용량을 계산합니다.
     * 
     * @return Estimated total battery capacity in mAh, or 0.0 if unavailable
     * @return 추정된 총 배터리 용량(mAh), 사용할 수 없는 경우 0.0
     */
    private fun getBatteryCapacityFromBatteryManager(defaultValue: Double = 0.0): Double = safeCatch(defaultValue = defaultValue) {
        checkSdkVersion(Build.VERSION_CODES.LOLLIPOP,
            positiveWork = {
                val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
                val chargeCounter = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER) // Current charge in µAh
                val capacity = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) // Current percentage

                if (chargeCounter != null && capacity != null && chargeCounter > 0 && capacity > 5 && capacity <= 100) {
                    // Calculate total capacity: (current_charge_µAh / current_percentage) * 100 / 1000 = mAh
                    // 총 용량 계산: (현재_충전량_µAh / 현재_백분율) * 100 / 1000 = mAh
                    val estimatedTotalCapacity = (chargeCounter.toDouble() / capacity.toDouble()) * 100.0 / 1000.0

                    // Sanity check: reasonable mobile device battery capacity
                    // 정상성 검사: 합리적인 모바일 기기 배터리 용량
                    if (estimatedTotalCapacity in 1000.0..10000.0) {
                        estimatedTotalCapacity
                    } else { defaultValue }
                } else { defaultValue }
            },
            negativeWork = { defaultValue }
        )
    }
}