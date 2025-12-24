package kr.open.library.simple_ui.core.system_manager.info.battery.internal.helper.power

import android.annotation.SuppressLint
import android.content.Context
import kr.open.library.simple_ui.core.extensions.trycatch.safeCatch
import kr.open.library.simple_ui.core.logcat.Logx
import java.lang.reflect.Method

/**
 * Provides access to device power consumption information through the internal Android PowerProfile class using reflection.<br><br>
 * 리플렉션을 사용하여 내부 Android PowerProfile 클래스를 통해 기기의 전력 소비 정보에 접근할 수 있도록 합니다.<br>
 *
 * The primary use case is to retrieve the battery's total capacity (rated capacity),<br>
 * but it can also be used to get other power consumption metrics defined in [PowerProfileVO].<br><br>
 * 주요 용도는 배터리의 총 용량(정격 용량)을 가져오는 것이지만,<br>
 * [PowerProfileVO]에 정의된 다른 전력 소비 메트릭을 가져오는 데에도 사용할 수 있습니다.<br>
 *
 * Note: Because this uses reflection to access internal Android APIs, it may not work<br>
 * on all devices or future Android versions.<br><br>
 * 참고: 내부 Android API에 접근하기 위해 리플렉션을 사용하므로,<br>
 * 모든 기기나 향후 Android 버전에서 작동하지 않을 수 있습니다.<br>
 *
 * @param context The application context.<br><br>
 *                애플리케이션 컨텍스트.<br>
 *
 * @see PowerProfileVO for available power metrics
 */
@SuppressLint("PrivateApi")
public class PowerProfile(
    private val context: Context,
) {
    /**
     * Fully qualified name of the internal PowerProfile class.<br><br>
     * 내부 PowerProfile 클래스의 FQN입니다.<br>
     */
    private val classNamePowerProfile: String = CLASS_NAME_POWER_PROFILE

    /**
     * Method name used to retrieve average power from PowerProfile.<br><br>
     * PowerProfile에서 평균 전력을 조회할 때 사용하는 메서드 이름입니다.<br>
     */
    private val averagePower: String = METHOD_GET_AVERAGE_POWER

    companion object {
        /**
         * Internal Android PowerProfile class name.<br><br>
         * 내부 Android PowerProfile 클래스 이름입니다.<br>
         */
        private const val CLASS_NAME_POWER_PROFILE = "com.android.internal.os.PowerProfile"

        /**
         * Method name for getting average power consumption.<br><br>
         * 평균 전력 소비를 가져오는 메서드 이름입니다.<br>
         */
        private const val METHOD_GET_AVERAGE_POWER = "getAveragePower"

        /**
         * Default battery capacity when PowerProfile is unavailable.<br><br>
         * PowerProfile을 사용할 수 없을 때의 기본 배터리 용량입니다.<br>
         */
        private const val DEFAULT_BATTERY_CAPACITY = 0.0
    }

    /**
     * Lazy initialization to prevent app crash on init failure.<br><br>
     * 초기화 실패 시 앱 크래시를 방지하기 위한 지연 초기화입니다.<br>
     */
    private val powerProfileClass: Class<*>? by lazy {
        safeCatch(defaultValue = null) { Class.forName(classNamePowerProfile) }
    }

    /**
     * Reflection handle for `getAveragePower(String)`. <br><br>
     * `getAveragePower(String)`에 대한 리플렉션 핸들입니다.<br>
     */
    private val getAveragePowerMethod: Method? by lazy {
        safeCatch(defaultValue = null) { powerProfileClass?.getMethod(averagePower, String::class.java) }
    }

    /**
     * Reflection handle for `getAveragePower(String, Int)`. <br><br>
     * `getAveragePower(String, Int)`용 리플렉션 핸들입니다.<br>
     */
    private val getAveragePowerMethodWithInt: Method? by lazy {
        safeCatch(defaultValue = null) { powerProfileClass?.getMethod(averagePower, String::class.java, Int::class.javaPrimitiveType) }
    }

    /**
     * PowerProfile instance created via reflection.<br><br>
     * 리플렉션으로 생성한 PowerProfile 인스턴스입니다.<br>
     */
    private val powerProfileInstance: Any? by lazy {
        safeCatch(defaultValue = null) { powerProfileClass?.getConstructor(Context::class.java)?.newInstance(context) }
    }

    /**
     * Checks if PowerProfile is available on this device/Android version.<br><br>
     * 이 기기/Android 버전에서 PowerProfile을 사용할 수 있는지 확인합니다.<br>
     *
     * @return `true` if PowerProfile is available, `false` otherwise.<br><br>
     *         PowerProfile 사용 가능 시 `true`, 아니면 `false`.<br>
     */
    public fun isPowerProfileAvailable(): Boolean = powerProfileInstance != null

    /**
     * Retrieves the average power consumption for the specified power profile type.<br><br>
     * 지정된 전력 프로파일 유형에 대한 평균 전력 소비를 가져옵니다.<br>
     *
     * @param type The power profile type to retrieve from [PowerProfileVO].<br><br>
     *             [PowerProfileVO]에서 가져올 전력 프로파일 유형.<br>
     *
     * @return The average power consumption, or null if an error occurred.<br><br>
     *         평균 전력 소비량, 오류 발생 시 null.<br>
     */
    public fun getAveragePower(type: PowerProfileVO): Any? = safeCatch(defaultValue = null) {
        if (!isPowerProfileAvailable()) {
            Logx.w("PowerProfile not available, cannot get average power for ${type.res}")
            return@safeCatch null
        }
        getAveragePowerMethod?.invoke(powerProfileInstance, type.res)
    }

    /**
     * Retrieves the average power consumption for the specified power profile type and index.<br>
     * This is used for metrics that have multiple values (like CPU clusters).<br><br>
     * 지정된 전력 프로파일 유형과 인덱스에 대한 평균 전력 소비를 가져옵니다.<br>
     * 여러 값을 가진 메트릭(예: CPU 클러스터)에 사용됩니다.<br>
     *
     * @param type The power profile type to retrieve from [PowerProfileVO].<br><br>
     *             [PowerProfileVO]에서 가져올 전력 프로파일 유형.<br>
     *
     * @param index The index of the power profile value.<br><br>
     *              전력 프로파일 값의 인덱스.<br>
     *
     * @return The average power consumption, or null if an error occurred.<br><br>
     *         평균 전력 소비량, 오류 발생 시 null.<br>
     */
    public fun getAveragePower(type: PowerProfileVO, index: Int): Any? = safeCatch(defaultValue = null) {
        if (!isPowerProfileAvailable()) {
            Logx.w("PowerProfile not available, cannot get average power for ${type.res}[$index]")
            return@safeCatch null
        }
        getAveragePowerMethodWithInt?.invoke(powerProfileInstance, type.res, index)
    }

    /**
     * Gets the total battery capacity in milliampere-hours (mAh).<br><br>
     * 배터리의 총 용량을 밀리암페어시(mAh) 단위로 가져옵니다.<br>
     *
     * @return The battery capacity in mAh, or default value if unable to retrieve.<br><br>
     *         배터리 용량(mAh), 가져올 수 없는 경우 기본값.<br>
     */
    public fun getBatteryCapacity(): Double = safeCatch(defaultValue = DEFAULT_BATTERY_CAPACITY) {
        // Try PowerProfile first (primary method)
        // PowerProfile을 먼저 시도 (주요 방법)
        val powerProfileCapacity = getAveragePower(PowerProfileVO.POWER_BATTERY_CAPACITY) as? Double
        if (powerProfileCapacity != null && powerProfileCapacity > 0) {
            return powerProfileCapacity
        }

        // Last resort: return default capacity
        // 최후 수단: 기본 용량 반환
        Logx.w("Unable to retrieve battery capacity, using default: $DEFAULT_BATTERY_CAPACITY mAh")
        return DEFAULT_BATTERY_CAPACITY
    }
}
