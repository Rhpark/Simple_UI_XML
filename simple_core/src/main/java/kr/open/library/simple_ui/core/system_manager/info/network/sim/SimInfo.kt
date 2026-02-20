package kr.open.library.simple_ui.core.system_manager.info.network.sim

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.READ_PHONE_NUMBERS
import android.Manifest.permission.READ_PHONE_STATE
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.telephony.euicc.EuiccManager
import android.util.SparseArray
import androidx.annotation.RequiresPermission
import kr.open.library.simple_ui.core.extensions.conditional.checkSdkVersion
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.core.permissions.extentions.hasPermissions
import kr.open.library.simple_ui.core.system_manager.base.BaseSystemService
import kr.open.library.simple_ui.core.system_manager.extensions.getEuiccManager
import kr.open.library.simple_ui.core.system_manager.extensions.getSubscriptionManager
import kr.open.library.simple_ui.core.system_manager.extensions.getTelephonyManager

/**
 * SIM Card and Subscription Information Management Class.<br><br>
 * SIM 카드 및 구독 정보 전문 관리 클래스입니다.<br>
 *
 * This class manages the physical/logical state of SIM cards and subscription information.<br><br>
 * 이 클래스는 SIM 카드의 물리적/논리적 상태와 구독 정보를 관리합니다.<br>
 *
 * Main Features:<br>
 * - SIM card state management<br>
 * - Multi-SIM support<br>
 * - eSIM support and management<br>
 * - Subscription information retrieval<br>
 * - Carrier information (MCC/MNC)<br>
 * - Phone number retrieval<br><br>
 * 주요 기능:<br>
 * - SIM 카드 상태 관리<br>
 * - 멀티 SIM 지원<br>
 * - eSIM 지원 및 관리<br>
 * - 구독 정보 조회<br>
 * - 통신사 정보 (MCC/MNC)<br>
 * - 전화번호 조회<br>
 *
 * Required Permissions:<br>
 * - `android.permission.READ_PHONE_STATE` (Required)<br>
 * - `android.permission.READ_PHONE_NUMBERS` (Phone numbers)<br>
 * - `android.permission.ACCESS_FINE_LOCATION` (Some features)<br><br>
 * 필수 권한:<br>
 * - `android.permission.READ_PHONE_STATE` (필수)<br>
 * - `android.permission.READ_PHONE_NUMBERS` (전화번호)<br>
 * - `android.permission.ACCESS_FINE_LOCATION` (일부 기능)<br>
 *
 * ⚠️ Permission fallback:<br>
 * - Permissions are checked through BaseSystemService. Missing permissions lead to default values (e.g., empty list, null) and warning logs.<br>
 * - Call refreshPermissions() after acquiring permissions to update the state.<br><br>
 * ⚠️ 권한 폴백:<br>
 * - BaseSystemService를 통해 권한을 확인합니다. 권한이 없으면 기본값(예: 빈 리스트, null)을 반환하고 경고 로그를 남깁니다.<br>
 * - 권한을 획득한 뒤에는 refreshPermissions()를 호출하여 상태를 갱신하세요.<br>
 *
 * Usage Example:<br>
 * ```kotlin
 * val simInfo = SimInfo(context)
 *
 * // Basic info
 * val simCount = simInfo.getActiveSimCount()
 * val isDualSim = simInfo.isDualSim()
 *
 * // Subscription info
 * val subscriptions = simInfo.getActiveSubscriptionInfoList()
 * val defaultSubId = simInfo.getSubIdFromDefaultUSim()
 *
 * // eSIM support check
 * val isESimSupported = simInfo.isESimSupported()
 * ```
 *
 * @param context The application context.<br><br>
 *                애플리케이션 컨텍스트.
 */
public class SimInfo(
    context: Context,
) : BaseSystemService(context, listOf(READ_PHONE_STATE, READ_PHONE_NUMBERS, ACCESS_FINE_LOCATION)) {
    // =================================================
    // Core System Services
    // =================================================

    /**
     * TelephonyManager for telephony operations.<br><br>
     * 기본 TelephonyManager입니다.<br>
     */
    public val telephonyManager: TelephonyManager by lazy { context.getTelephonyManager() }

    /**
     * SubscriptionManager for subscription operations.<br><br>
     * 구독 관리를 위한 SubscriptionManager입니다.<br>
     */
    public val subscriptionManager: SubscriptionManager by lazy { context.getSubscriptionManager() }

    /**
     * EuiccManager for eSIM operations.<br><br>
     * eSIM 관리를 위한 EuiccManager입니다.<br>
     */
    public val euiccManager: EuiccManager by lazy { context.getEuiccManager() }

    // =================================================
    // Internal State Management
    // =================================================

    /**
     * Stores TelephonyManager instances per SIM slot.<br><br>
     * SIM 슬롯별 TelephonyManager 인스턴스를 저장합니다.<br>
     */
    private val uSimTelephonyManagerList = SparseArray<TelephonyManager>()

    /**
     * Whether SIM information can be read.<br><br>
     * SIM 정보를 읽을 수 있는지 여부입니다.<br>
     */
    private var isReadSimInfoFromDefaultUSim = false

    // =================================================
    // Initialization
    // =================================================

    init {
        initialization()
    }

    @SuppressLint("MissingPermission")
    /**
     * Initializes SIM info by checking permission and seeding state.<br><br>
     * 권한을 확인하고 상태를 초기화하며 SIM 정보를 준비합니다.<br>
     */
    private fun initialization() {
        if (!context.hasPermissions(READ_PHONE_STATE)) {
            Logx.e("SimInfo: Cannot read SimInfo!")
            return
        }
        getSubIdFromDefaultUSim()
        updateUSimTelephonyManagerList()
    }

    // =================================================
    // Basic SIM Information / 기본 SIM 정보
    // =================================================

    /**
     * Checks if SIM information can be read.<br><br>
     * SIM 정보를 읽을 수 있는지 확인합니다.<br>
     *
     * @return `true` if SIM info is readable, `false` otherwise.<br><br>
     *         SIM 정보를 읽을 수 있으면 `true`, 그렇지 않으면 `false`.<br>
     */
    public fun isCanReadSimInfo(): Boolean = isReadSimInfoFromDefaultUSim

    /**
     * Checks if device has dual SIM.<br><br>
     * 듀얼 SIM 여부를 확인합니다.<br>
     *
     * @return `true` if device has dual SIM, `false` otherwise.<br><br>
     *         듀얼 SIM이면 `true`, 그렇지 않으면 `false`.<br>
     */
    public fun isDualSim(): Boolean = getMaximumUSimCount() == 2

    /**
     * Checks if device has single SIM.<br><br>
     * 단일 SIM 여부를 확인합니다.<br>
     *
     * @return `true` if device has single SIM, `false` otherwise.<br><br>
     *         단일 SIM이면 `true`, 그렇지 않으면 `false`.<br>
     */
    public fun isSingleSim(): Boolean = getMaximumUSimCount() == 1

    /**
     * Checks if device has multi SIM.<br><br>
     * 멀티 SIM 여부를 확인합니다.<br>
     *
     * @return `true` if device has multi SIM, `false` otherwise.<br><br>
     *         멀티 SIM이면 `true`, 그렇지 않으면 `false`.<br>
     */
    public fun isMultiSim(): Boolean = getMaximumUSimCount() > 1

    /**
     * Gets maximum SIM count.<br><br>
     * 최대 SIM 수를 반환합니다.<br>
     *
     * @return Maximum number of SIMs supported.<br><br>
     *         지원되는 최대 SIM 수.
     */
    public fun getMaximumUSimCount(): Int = subscriptionManager.activeSubscriptionInfoCountMax

    /**
     * Gets active SIM count.<br><br>
     * 활성화된 SIM 수를 반환합니다.<br>
     *
     * @return Number of active SIM cards.<br><br>
     *         활성화된 SIM 카드 수.
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getActiveSimCount(): Int = tryCatchSystemManager(0) { return subscriptionManager.activeSubscriptionInfoCount }

    /**
     * Gets active SIM slot index list.<br><br>
     * 활성화된 SIM 슬롯 인덱스 목록을 반환합니다.<br>
     *
     * @return List of active SIM slot indices.<br><br>
     *         활성화된 SIM 슬롯 인덱스 목록.
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getActiveSimSlotIndexList(): List<Int> = tryCatchSystemManager(emptyList()) {
        getActiveSubscriptionInfoList().map { it.simSlotIndex }
    }

    /**
     * Updates TelephonyManager list per SIM slot.<br><br>
     * SIM 슬롯별 TelephonyManager 목록을 업데이트합니다.<br>
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun updateUSimTelephonyManagerList() {
        Logx.d("SimInfo", "activeSubscriptionInfoList size ${getActiveSubscriptionInfoList().size}")
        getActiveSubscriptionInfoList().forEach {
            Logx.d("SimInfo", "SubID $it")
            uSimTelephonyManagerList[it.simSlotIndex] = telephonyManager.createForSubscriptionId(it.subscriptionId)
        }
    }

    /**
     * Gets TelephonyManager for specific SIM slot.<br><br>
     * 특정 SIM 슬롯의 TelephonyManager를 반환합니다.<br>
     *
     * @param slotIndex SIM slot index.<br><br>
     *                  SIM 슬롯 인덱스.
     * @return TelephonyManager for the slot, or null if unavailable.<br><br>
     *         해당 슬롯의 TelephonyManager, 사용할 수 없는 경우 null.
     */
    public fun getTelephonyManagerFromUSim(slotIndex: Int): TelephonyManager? = uSimTelephonyManagerList[slotIndex]

    // =================================================
    // Subscription Information / 구독 정보
    // =================================================

    /**
     * Gets default subscription ID.<br><br>
     * 기본 subscription ID를 반환합니다.<br>
     *
     * @return Default subscription ID, or null if unavailable.<br><br>
     *         기본 subscription ID, 사용할 수 없는 경우 null.
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getSubIdFromDefaultUSim(): Int? = getSubIdFromDefaultUSimInternal()

    /**
     * Gets default subscription ID (Result pattern).<br><br>
     * 기본 subscription ID를 반환합니다 (Result 패턴).<br>
     *
     * @return Default subscription ID, or null if unavailable.<br><br>
     *         기본 subscription ID, 사용할 수 없는 경우 null.
     */
    @RequiresPermission(READ_PHONE_STATE)
    private fun getSubIdFromDefaultUSimInternal(): Int? = tryCatchSystemManager(null) {
        isReadSimInfoFromDefaultUSim = false

        val id = checkSdkVersion(Build.VERSION_CODES.R,
            positiveWork = { telephonyManager.subscriptionId },
            negativeWork = { getActiveSubscriptionInfoList().firstOrNull()?.subscriptionId },
        )
        isReadSimInfoFromDefaultUSim = id != null
        return id
    }

    /**
     * Gets subscription ID for specific SIM slot.<br><br>
     * 특정 SIM 슬롯의 subscription ID를 반환합니다.<br>
     *
     * @param simSlotIndex SIM slot index.<br><br>
     *                     SIM 슬롯 인덱스.
     * @return Subscription ID, or null if unavailable.<br><br>
     *         Subscription ID, 사용할 수 없는 경우 null.
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getSubId(simSlotIndex: Int): Int? = tryCatchSystemManager(null) {
        checkSdkVersion(Build.VERSION_CODES.R,
            positiveWork = { uSimTelephonyManagerList[simSlotIndex]?.subscriptionId },
            negativeWork = { getActiveSubscriptionInfoSimSlot(simSlotIndex)?.subscriptionId },
        )
    }

    /**
     * Converts subscription ID to SIM slot index.<br><br>
     * subscription ID를 SIM 슬롯 인덱스로 변환합니다.<br>
     *
     * @param currentSubId Subscription ID.<br><br>
     *                     Subscription ID.
     * @return SIM slot index, or null if not found.<br><br>
     *         SIM 슬롯 인덱스, 찾을 수 없는 경우 null.
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun subIdToSimSlotIndex(currentSubId: Int): Int? = getActiveSubscriptionInfoSubId(currentSubId)?.simSlotIndex

    /**
     * Gets all active subscription information list.<br><br>
     * 활성화된 모든 subscription 정보 목록을 반환합니다.<br>
     *
     * @return List of SubscriptionInfo.<br><br>
     *         SubscriptionInfo 목록.
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getActiveSubscriptionInfoList(): List<SubscriptionInfo> = tryCatchSystemManager(emptyList()) {
        return subscriptionManager.activeSubscriptionInfoList ?: emptyList()
    }

    /**
     * Gets SubscriptionInfo for default subscription.<br><br>
     * 기본 subscription의 SubscriptionInfo를 반환합니다.<br>
     *
     * @return SubscriptionInfo, or throws exception if unavailable.<br><br>
     *         SubscriptionInfo, 사용할 수 없는 경우 예외 발생.
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getSubscriptionInfoSubIdFromDefaultUSim(): SubscriptionInfo? =
        getSubIdFromDefaultUSim()?.let { getActiveSubscriptionInfoSubId(it) } ?: throw IllegalArgumentException("Cannot read uSim Chip")

    /**
     * Gets SubscriptionInfo for specific subscription ID.<br><br>
     * 특정 subscription ID의 SubscriptionInfo를 반환합니다.<br>
     *
     * @param subId Subscription ID.<br><br>
     *              Subscription ID.
     * @return SubscriptionInfo, or null if not found.<br><br>
     *         SubscriptionInfo, 찾을 수 없는 경우 null.
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getActiveSubscriptionInfoSubId(subId: Int): SubscriptionInfo? = subscriptionManager.getActiveSubscriptionInfo(subId)

    /**
     * Gets SubscriptionInfo for specific SIM slot.<br><br>
     * 특정 SIM 슬롯의 SubscriptionInfo를 반환합니다.<br>
     *
     * @param slotIndex SIM slot index.<br><br>
     *                  SIM 슬롯 인덱스.
     * @return SubscriptionInfo, or null if not found.<br><br>
     *         SubscriptionInfo, 찾을 수 없는 경우 null.
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getActiveSubscriptionInfoSimSlot(slotIndex: Int): SubscriptionInfo? =
        subscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(slotIndex)

    /**
     * Gets SubscriptionInfo for default SIM slot.<br><br>
     * 기본 SIM 슬롯의 SubscriptionInfo를 반환합니다.<br>
     *
     * @return SubscriptionInfo, or throws exception if unavailable.<br><br>
     *         SubscriptionInfo, 사용할 수 없는 경우 예외 발생.
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getSubscriptionInfoSimSlotFromDefaultUSim(): SubscriptionInfo? =
        getSubIdFromDefaultUSim()?.let { getActiveSubscriptionInfoSimSlot(it) } ?: throw NoSuchMethodError("Cannot read uSim Chip")

    // =================================================
    // SIM State Information / SIM 상태 정보
    // =================================================

    /**
     * Gets default SIM state.<br><br>
     * 기본 SIM의 상태를 반환합니다.<br>
     *
     * @return `TelephonyManager.SIM_STATE_*` constant.<br><br>
     *         `TelephonyManager.SIM_STATE_*` 상수.
     */
    public fun getStatusFromDefaultUSim(): Int = telephonyManager.simState

    /**
     * Gets MCC (Mobile Country Code) from default SIM.<br><br>
     * 기본 SIM의 MCC (Mobile Country Code)를 반환합니다.<br>
     *
     * @return MCC string, or null if unavailable.<br><br>
     *         MCC 문자열, 사용할 수 없는 경우 null.
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getMccFromDefaultUSimString(): String? = getSubscriptionInfoSubIdFromDefaultUSim()?.let { info ->
        checkSdkVersion(Build.VERSION_CODES.Q,
            positiveWork = { info.mccString },
            negativeWork = { info.mcc.toString() },
        )
    }

    /**
     * Gets MNC (Mobile Network Code) from default SIM.<br><br>
     * 기본 SIM의 MNC (Mobile Network Code)를 반환합니다.<br>
     *
     * @return MNC string, or null if unavailable.<br><br>
     *         MNC 문자열, 사용할 수 없는 경우 null.
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getMncFromDefaultUSimString(): String? = getSubscriptionInfoSubIdFromDefaultUSim()?.let { info ->
        checkSdkVersion(Build.VERSION_CODES.Q,
            positiveWork = { info.mncString },
            negativeWork = { info.mnc.toString() },
        )
    }

    /**
     * Gets MCC for specific SIM slot.<br><br>
     * 특정 SIM 슬롯의 MCC를 반환합니다.<br>
     *
     * @param slotIndex SIM slot index.<br><br>
     *                  SIM 슬롯 인덱스.
     * @return MCC string, or null if unavailable.<br><br>
     *         MCC 문자열, 사용할 수 없는 경우 null.
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getMcc(slotIndex: Int): String? = getActiveSubscriptionInfoSimSlot(slotIndex)?.let { info ->
        checkSdkVersion(Build.VERSION_CODES.Q,
            positiveWork = { info.mccString },
            negativeWork = { info.mcc.toString() },
        )
    }

    /**
     * Gets MNC for specific SIM slot.<br><br>
     * 특정 SIM 슬롯의 MNC를 반환합니다.<br>
     *
     * @param slotIndex SIM slot index.<br><br>
     *                  SIM 슬롯 인덱스.
     * @return MNC string, or null if unavailable.<br><br>
     *         MNC 문자열, 사용할 수 없는 경우 null.
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getMnc(slotIndex: Int): String? = getActiveSubscriptionInfoSimSlot(slotIndex)?.let { info ->
        checkSdkVersion(Build.VERSION_CODES.Q,
            positiveWork = { info.mncString },
            negativeWork = { info.mnc.toString() },
        )
    }

    // =================================================
    // Phone Number Information / 전화번호 정보
    // =================================================

    /**
     * Gets phone number from default SIM.<br><br>
     * 기본 SIM의 전화번호를 반환합니다.<br>
     *
     * @return Phone number, or null if unavailable.<br><br>
     *         전화번호, 사용할 수 없는 경우 null.
     */
    @SuppressLint("HardwareIds")
    @RequiresPermission(anyOf = [READ_PHONE_STATE, READ_PHONE_NUMBERS])
    public fun getPhoneNumberFromDefaultUSim(): String? =
        telephonyManager.line1Number // Required SDK Version 1 ~ 33
            ?: getSubscriptionInfoSubIdFromDefaultUSim()?.number // number Required SDK Version 30+

    /**
     * Gets phone number for specific SIM slot.<br><br>
     * 특정 SIM 슬롯의 전화번호를 반환합니다.<br>
     *
     * @param slotIndex SIM slot index.<br><br>
     *                  SIM 슬롯 인덱스.
     * @return Phone number, or null if unavailable.<br><br>
     *         전화번호, 사용할 수 없는 경우 null.
     */
    @SuppressLint("HardwareIds")
    @RequiresPermission(anyOf = [READ_PHONE_STATE, READ_PHONE_NUMBERS])
    public fun getPhoneNumber(slotIndex: Int): String? =
        getTelephonyManagerFromUSim(slotIndex)?.line1Number // line1Number Required SDK Version 1 ~ 33
            ?: getActiveSubscriptionInfoSimSlot(slotIndex)?.number // number Required SDK Version 30+

    // =================================================
    // Display Information / 표시 정보
    // =================================================

    /**
     * Gets display name from default SIM.<br><br>
     * 기본 SIM의 표시 이름을 반환합니다.<br>
     *
     * @return Display name, or null if unavailable.<br><br>
     *         표시 이름, 사용할 수 없는 경우 null.
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getDisplayNameFromDefaultUSim(): String? = getSubscriptionInfoSubIdFromDefaultUSim()?.displayName?.toString()

    /**
     * Gets country ISO from default SIM.<br><br>
     * 기본 SIM의 국가 ISO 코드를 반환합니다.<br>
     *
     * @return Country ISO code, or null if unavailable.<br><br>
     *         국가 ISO 코드, 사용할 수 없는 경우 null.
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getCountryIsoFromDefaultUSim(): String? = getSubscriptionInfoSimSlotFromDefaultUSim()?.countryIso

    /**
     * Checks if default SIM is roaming.<br><br>
     * 기본 SIM의 로밍 상태를 확인합니다.<br>
     *
     * @return `true` if roaming, `false` otherwise.<br><br>
     *         로밍 중이면 `true`, 그렇지 않으면 `false`.<br>
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun isNetworkRoamingFromDefaultUSim(): Boolean =
        getSubIdFromDefaultUSim()?.let { subscriptionManager.isNetworkRoaming(it) } ?: false

    // =================================================
    // eSIM Support / eSIM 지원
    // =================================================

    /**
     * Checks if eSIM is supported.<br><br>
     * eSIM 지원 여부를 확인합니다.<br>
     *
     * @return `true` if eSIM is supported, `false` otherwise.<br><br>
     *         eSIM이 지원되면 `true`, 그렇지 않으면 `false`.<br>
     */
    public fun isESimSupported(): Boolean = (euiccManager.euiccInfo != null && euiccManager.isEnabled)

    /**
     * Checks if eSIM is registered.<br><br>
     * eSIM 등록 여부를 확인합니다.<br>
     *
     * @param eSimSlotIndex eSIM slot index.<br><br>
     *                      eSIM 슬롯 인덱스.
     * @return `true` if eSIM is registered, `false` otherwise.<br><br>
     *         eSIM이 등록되어 있으면 `true`, 그렇지 않으면 `false`.<br>
     */
    public fun isRegisterESim(eSimSlotIndex: Int): Boolean =
        !(isESimSupported() && eSimSlotIndex != 0 && subscriptionManager.accessibleSubscriptionInfoList == null)

    /**
     * Gets active SIM status.<br><br>
     * 활성 SIM 상태를 반환합니다.<br>
     *
     * @param slotIndex SIM slot index.<br><br>
     *                  SIM 슬롯 인덱스.
     * @return SIM status constant.<br><br>
     *         SIM 상태 상수.
     */
    public fun getActiveSimStatus(slotIndex: Int): Int = getActiveSimStatus(isESimSupported(), isRegisterESim(slotIndex), slotIndex)

    /**
     * Detailed SIM status check (internal method).<br><br>
     * SIM 상태 상세 확인 (내부 메서드)입니다.<br>
     */
    private fun getActiveSimStatus(isAbleEsim: Boolean, isRegisterESim: Boolean, slotIndex: Int): Int {
        val status = telephonyManager.getSimState(slotIndex)

        return if (isAbleEsim && slotIndex == 0 && status == TelephonyManager.SIM_STATE_UNKNOWN) {
            Logx.w("SimInfo: SimSlot 0, may be pSim is not ready")
            TelephonyManager.SIM_STATE_NOT_READY
        } else if (!isRegisterESim) {
            Logx.w("SimInfo: SimSlot $slotIndex, may be eSim is not register")
            TelephonyManager.SIM_STATE_UNKNOWN
        } else {
            status
        }
    }

    // =================================================
    // Cleanup / 정리
    // =================================================

    /**
     * Cleans up resources used by SimInfo.<br><br>
     * SimInfo에서 사용한 리소스를 정리합니다.<br>
     */
    override fun onDestroy() {
        Logx.d("SimInfo destroyed")
        super.onDestroy()
    }
}
