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
import kr.open.library.simple_ui.core.extensions.trycatch.safeCatch
import kr.open.library.simple_ui.core.logcat.Logx
import kr.open.library.simple_ui.core.permissions.extentions.hasPermissions
import kr.open.library.simple_ui.core.system_manager.base.BaseSystemService
import kr.open.library.simple_ui.core.system_manager.extensions.getEuiccManager
import kr.open.library.simple_ui.core.system_manager.extensions.getSubscriptionManager
import kr.open.library.simple_ui.core.system_manager.extensions.getTelephonyManager

/**
 * SimInfo - SIM 카드 및 구독 정보 전문 관리 클래스
 * SIM Card and Subscription Information Management Class
 * 
 * 이 클래스는 SIM 카드의 물리적/논리적 상태와 구독 정보를 관리합니다.
 * This class manages the physical/logical state of SIM cards and subscription information.
 * 
 * 주요 기능 / Main Features:
 * - SIM 카드 상태 관리 / SIM card state management
 * - 멀티 SIM 지원 / Multi-SIM support  
 * - eSIM 지원 및 관리 / eSIM support and management
 * - 구독 정보 조회 / Subscription information retrieval
 * - 통신사 정보 (MCC/MNC) / Carrier information (MCC/MNC)
 * - 전화번호 조회 / Phone number retrieval
 * 
 * 필수 권한 / Required Permissions:
 * - android.permission.READ_PHONE_STATE (필수/Required)
 * - android.permission.READ_PHONE_NUMBERS (전화번호/Phone numbers)
 * - android.permission.ACCESS_FINE_LOCATION (일부 기능/Some features)
 *
 * ⚠️ Permission fallback:
 * - Permissions are checked through BaseSystemService. Missing permissions lead to
 *   default values (예: 빈 리스트, null) and warning logs.
 * - 권한을 획득한 뒤에는 refreshPermissions()를 호출하여 상태를 갱신하세요.
 *
 * 사용 예시 / Usage Example:
 * ```
 * val simInfo = SimInfo(context)
 * 
 * // 기본 정보 조회 / Basic info
 * val simCount = simInfo.getActiveSimCount()
 * val isDualSim = simInfo.isDualSim()
 * 
 * // 구독 정보 조회 / Subscription info
 * val subscriptions = simInfo.getActiveSubscriptionInfoList()
 * val defaultSubId = simInfo.getSubIdFromDefaultUSim()
 * 
 * // eSIM 지원 확인 / eSIM support check
 * val isESimSupported = simInfo.isESimSupported()
 * ```
 */
public class SimInfo(context: Context) :
    BaseSystemService(context, listOf(READ_PHONE_STATE, READ_PHONE_NUMBERS, ACCESS_FINE_LOCATION)) {

    // =================================================
    // Core System Services
    // =================================================
    
    /**
     * TelephonyManager for telephony operations
     * 기본 TelephonyManager
     */
    public val telephonyManager: TelephonyManager by lazy { context.getTelephonyManager() }
    
    /**
     * SubscriptionManager for subscription operations  
     * 구독 관리를 위한 SubscriptionManager
     */
    public val subscriptionManager: SubscriptionManager by lazy { context.getSubscriptionManager() }
    
    /**
     * EuiccManager for eSIM operations
     * eSIM 관리를 위한 EuiccManager
     */
    public val euiccManager: EuiccManager by lazy { context.getEuiccManager() }

    // =================================================
    // Internal State Management
    // =================================================
    
    /**
     * SIM 슬롯별 TelephonyManager 인스턴스 저장
     * Stores TelephonyManager instances per SIM slot
     */
    private val uSimTelephonyManagerList = SparseArray<TelephonyManager>()

    /**
     * SIM 정보를 읽을 수 있는지 여부
     * Whether SIM information can be read
     */
    private var isReadSimInfoFromDefaultUSim = false

    // =================================================
    // Initialization
    // =================================================
    
    init {
        initialization()
    }

    @SuppressLint("MissingPermission")
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
     * SIM 정보를 읽을 수 있는지 확인
     * Check if SIM information can be read
     */
    public fun isCanReadSimInfo(): Boolean = isReadSimInfoFromDefaultUSim

    /**
     * 듀얼 SIM 여부 확인
     * Check if device has dual SIM
     */
    public fun isDualSim(): Boolean = getMaximumUSimCount() == 2

    /**
     * 단일 SIM 여부 확인  
     * Check if device has single SIM
     */
    public fun isSingleSim(): Boolean = getMaximumUSimCount() == 1

    /**
     * 멀티 SIM 여부 확인
     * Check if device has multi SIM
     */
    public fun isMultiSim(): Boolean = getMaximumUSimCount() > 1

    /**
     * 최대 SIM 수 반환
     * Get maximum SIM count
     */
    public fun getMaximumUSimCount(): Int = subscriptionManager.activeSubscriptionInfoCountMax

    /**
     * 활성화된 SIM 수 반환
     * Get active SIM count
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getActiveSimCount(): Int = safeCatch(defaultValue = 0) {
        subscriptionManager.activeSubscriptionInfoCount
    }

    /**
     * 활성화된 SIM 슬롯 인덱스 목록 반환
     * Get active SIM slot index list
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getActiveSimSlotIndexList(): List<Int> = 
        getActiveSubscriptionInfoList().map { it.simSlotIndex }

    /**
     * SIM 슬롯별 TelephonyManager 목록 업데이트
     * Update TelephonyManager list per SIM slot
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun updateUSimTelephonyManagerList() {
        Logx.d("SimInfo", "activeSubscriptionInfoList size ${getActiveSubscriptionInfoList().size}")
        getActiveSubscriptionInfoList().forEach {
            Logx.d("SimInfo", "SubID ${it}")
            uSimTelephonyManagerList[it.simSlotIndex] = telephonyManager.createForSubscriptionId(it.subscriptionId)
        }
    }

    /**
     * 특정 SIM 슬롯의 TelephonyManager 반환
     * Get TelephonyManager for specific SIM slot
     */
    public fun getTelephonyManagerFromUSim(slotIndex: Int): TelephonyManager? = 
        uSimTelephonyManagerList[slotIndex]

    // =================================================
    // Subscription Information / 구독 정보
    // =================================================
    
    /**
     * 기본 subscription ID 반환
     * Get default subscription ID
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getSubIdFromDefaultUSim(): Int? = getSubIdFromDefaultUSimInternal()


    /**
     * 기본 subscription ID 반환 (Result 패턴)
     * Get default subscription ID (Result pattern)
     */
    @RequiresPermission(READ_PHONE_STATE)
    private fun getSubIdFromDefaultUSimInternal(): Int? = safeCatch(null) {
        isReadSimInfoFromDefaultUSim = false

        val id = checkSdkVersion(Build.VERSION_CODES.R,
            positiveWork = { telephonyManager.subscriptionId },
            negativeWork = { getActiveSubscriptionInfoList().firstOrNull()?.subscriptionId }
        )
        isReadSimInfoFromDefaultUSim = id != null
        id
    }

    /**
     * 특정 SIM 슬롯의 subscription ID 반환
     * Get subscription ID for specific SIM slot
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getSubId(simSlotIndex: Int): Int? = tryCatchSystemManager(null) {
        checkSdkVersion(Build.VERSION_CODES.R,
            positiveWork = { uSimTelephonyManagerList[simSlotIndex]?.subscriptionId },
            negativeWork = { getActiveSubscriptionInfoSimSlot(simSlotIndex)?.subscriptionId }
        )
    }

    /**
     * subscription ID를 SIM 슬롯 인덱스로 변환
     * Convert subscription ID to SIM slot index
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun subIdToSimSlotIndex(currentSubId: Int): Int? = 
        getActiveSubscriptionInfoSubId(currentSubId)?.simSlotIndex

    /**
     * 활성화된 모든 subscription 정보 목록 반환
     * Get all active subscription information list
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getActiveSubscriptionInfoList(): List<SubscriptionInfo> = safeCatch(defaultValue = emptyList()) {
        subscriptionManager.activeSubscriptionInfoList ?: emptyList()
    }

    /**
     * 기본 subscription의 SubscriptionInfo 반환
     * Get SubscriptionInfo for default subscription
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getSubscriptionInfoSubIdFromDefaultUSim(): SubscriptionInfo? =
        getSubIdFromDefaultUSim()?.let { getActiveSubscriptionInfoSubId(it) }
            ?: throw IllegalArgumentException("Cannot read uSim Chip")

    /**
     * 특정 subscription ID의 SubscriptionInfo 반환
     * Get SubscriptionInfo for specific subscription ID
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getActiveSubscriptionInfoSubId(subId: Int): SubscriptionInfo? =
        subscriptionManager.getActiveSubscriptionInfo(subId)

    /**
     * 특정 SIM 슬롯의 SubscriptionInfo 반환
     * Get SubscriptionInfo for specific SIM slot
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getActiveSubscriptionInfoSimSlot(slotIndex: Int): SubscriptionInfo? =
        subscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(slotIndex)

    /**
     * 기본 SIM 슬롯의 SubscriptionInfo 반환
     * Get SubscriptionInfo for default SIM slot
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getSubscriptionInfoSimSlotFromDefaultUSim(): SubscriptionInfo? =
        getSubIdFromDefaultUSim()?.let { getActiveSubscriptionInfoSimSlot(it) }
            ?: throw NoSuchMethodError("Cannot read uSim Chip")

    // =================================================
    // SIM State Information / SIM 상태 정보  
    // =================================================
    
    /**
     * 기본 SIM의 상태 반환
     * Get default SIM state
     * @return TelephonyManager.SIM_STATE_* 상수
     */
    public fun getStatusFromDefaultUSim(): Int = telephonyManager.simState

    /**
     * 기본 SIM의 MCC (Mobile Country Code) 반환
     * Get MCC (Mobile Country Code) from default SIM
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getMccFromDefaultUSimString(): String? = getSubscriptionInfoSubIdFromDefaultUSim()?.let {
        checkSdkVersion(Build.VERSION_CODES.Q,
            positiveWork = { it.mccString },
            negativeWork = { it.mcc.toString() }
        )
    }

    /**
     * 기본 SIM의 MNC (Mobile Network Code) 반환
     * Get MNC (Mobile Network Code) from default SIM
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getMncFromDefaultUSimString(): String? = getSubscriptionInfoSubIdFromDefaultUSim()?.let {
        checkSdkVersion(Build.VERSION_CODES.Q,
            positiveWork = { it.mncString },
            negativeWork = { it.mnc.toString() }
        )
    }

    /**
     * 특정 SIM 슬롯의 MCC 반환
     * Get MCC for specific SIM slot
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getMcc(slotIndex: Int): String? = getActiveSubscriptionInfoSimSlot(slotIndex)?.let {
        checkSdkVersion(Build.VERSION_CODES.Q,
            positiveWork = { it.mccString },
            negativeWork = { it.mcc.toString() }
        )
    }

    /**
     * 특정 SIM 슬롯의 MNC 반환
     * Get MNC for specific SIM slot
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getMnc(slotIndex: Int): String? = getActiveSubscriptionInfoSimSlot(slotIndex)?.let {
        checkSdkVersion(Build.VERSION_CODES.Q,
            positiveWork = { it.mncString },
            negativeWork = { it.mnc.toString() }
        )
    }

    // =================================================
    // Phone Number Information / 전화번호 정보
    // =================================================
    
    /**
     * 기본 SIM의 전화번호 반환
     * Get phone number from default SIM
     */
    @SuppressLint("HardwareIds")
    @RequiresPermission(anyOf = [READ_PHONE_STATE, READ_PHONE_NUMBERS])
    public fun getPhoneNumberFromDefaultUSim(): String? =
        telephonyManager?.line1Number // Required SDK Version 1 ~ 33
            ?: getSubscriptionInfoSubIdFromDefaultUSim()?.number // number Required SDK Version 30+

    /**
     * 특정 SIM 슬롯의 전화번호 반환
     * Get phone number for specific SIM slot
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
     * 기본 SIM의 표시 이름 반환
     * Get display name from default SIM
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getDisplayNameFromDefaultUSim(): String? = 
        getSubscriptionInfoSubIdFromDefaultUSim()?.displayName?.toString()

    /**
     * 기본 SIM의 국가 ISO 코드 반환
     * Get country ISO from default SIM
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun getCountryIsoFromDefaultUSim(): String? = 
        getSubscriptionInfoSimSlotFromDefaultUSim()?.countryIso

    /**
     * 기본 SIM의 로밍 상태 확인
     * Check if default SIM is roaming
     */
    @RequiresPermission(READ_PHONE_STATE)
    public fun isNetworkRoamingFromDefaultUSim(): Boolean =
        getSubIdFromDefaultUSim()?.let { subscriptionManager.isNetworkRoaming(it) } ?: false

    // =================================================
    // eSIM Support / eSIM 지원
    // =================================================
    
    /**
     * eSIM 지원 여부 확인
     * Check if eSIM is supported
     */
    public fun isESimSupported(): Boolean = 
        (euiccManager.euiccInfo != null && euiccManager.isEnabled)

    /**
     * eSIM 등록 여부 확인
     * Check if eSIM is registered
     */
    public fun isRegisterESim(eSimSlotIndex: Int): Boolean =
        !(isESimSupported() && eSimSlotIndex != 0 && subscriptionManager.accessibleSubscriptionInfoList == null)

    /**
     * 활성 SIM 상태 반환
     * Get active SIM status
     */
    public fun getActiveSimStatus(slotIndex: Int): Int =
        getActiveSimStatus(isESimSupported(), isRegisterESim(slotIndex), slotIndex)

    /**
     * SIM 상태 상세 확인 (내부 메서드)
     * Detailed SIM status check (internal method)
     */
    private fun getActiveSimStatus(isAbleEsim: Boolean, isRegisterESim: Boolean, slotIndex: Int): Int {
        val status = telephonyManager.getSimState(slotIndex)

        return if (isAbleEsim && slotIndex == 0 && status == TelephonyManager.SIM_STATE_UNKNOWN) {
            Logx.w("SimInfo: SimSlot 0, may be pSim is not ready")
            TelephonyManager.SIM_STATE_NOT_READY
        } else if (!isRegisterESim) {
            Logx.w("SimInfo: SimSlot $slotIndex, may be eSim is not register")
            TelephonyManager.SIM_STATE_UNKNOWN
        } else status
    }

    // =================================================
    // Cleanup / 정리
    // =================================================
    
    override fun onDestroy() {
        try {
            Logx.d("SimInfo destroyed")
        } catch (e: Exception) {
            Logx.e("Error during SimInfo cleanup: ${e.message}")
        } finally {
            super.onDestroy()
        }
    }
}
