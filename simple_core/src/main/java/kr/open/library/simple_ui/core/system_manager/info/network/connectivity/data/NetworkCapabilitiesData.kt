package kr.open.library.simple_ui.core.system_manager.info.network.connectivity.data

import android.net.NetworkCapabilities
import android.net.NetworkSpecifier
import android.net.TransportInfo
import android.os.Build
import android.os.ext.SdkExtensions
import androidx.annotation.RequiresApi
import kr.open.library.simple_ui.core.extensions.conditional.checkSdkVersion

/**
 * Data class wrapper for NetworkCapabilities.<br><br>
 * NetworkCapabilities를 위한 데이터 클래스 래퍼입니다.<br>
 *
 * Provides safe accessors and string parsing fallbacks for NetworkCapabilities fields.<br><br>
 * NetworkCapabilities 필드에 대한 안전한 접근자와 문자열 파싱 폴백을 제공합니다.<br>
 *
 * @property networkCapabilities The original NetworkCapabilities object.<br><br>
 *                               원본 NetworkCapabilities 객체.
 */
public data class NetworkCapabilitiesData(
    public val networkCapabilities: NetworkCapabilities,
) : NetworkBase(networkCapabilities) {
    private val transportInfoStr = "TransportInfo: <SSID:"

    /**
     * Gets the upstream bandwidth in Kbps.<br><br>
     * 업스트림 대역폭(Kbps)을 가져옵니다.<br>
     *
     * @return Upstream bandwidth.<br><br>
     *         업스트림 대역폭.
     */
    public fun getLinkUpstreamBandwidthKbps(): Int = networkCapabilities.linkUpstreamBandwidthKbps

    /**
     * Gets the downstream bandwidth in Kbps.<br><br>
     * 다운스트림 대역폭(Kbps)을 가져옵니다.<br>
     *
     * @return Downstream bandwidth.<br><br>
     *         다운스트림 대역폭.
     */
    public fun getLinkDownstreamBandwidthKbps(): Int = networkCapabilities.linkDownstreamBandwidthKbps

    /**
     * Gets the list of capabilities as integer codes.<br><br>
     * 기능 목록을 정수 코드로 가져옵니다.<br>
     *
     * @return List of capability integer codes.<br><br>
     *         기능 정수 코드 목록.
     */
    public fun getCapabilities(): IntArray? =
        checkSdkVersion(
            Build.VERSION_CODES.S,
            positiveWork = { networkCapabilities.capabilities },
            negativeWork = { getCapabilitiesNumber(splitStr("Capabilities: ", " LinkUpBandwidth", "&")) },
        )

    /**
     * Converts capability strings to integer codes.<br><br>
     * 기능 문자열을 정수 코드로 변환합니다.<br>
     *
     * @param capabilitiesStr List of capability strings.<br><br>
     *                        기능 문자열 목록.
     * @return List of capability integer codes.<br><br>
     *         기능 정수 코드 목록.
     */
    private fun getCapabilitiesNumber(capabilitiesStr: List<String>?): IntArray? {
        if (capabilitiesStr == null) return null

        val res = mutableListOf<Int>()
        capabilitiesStr.forEach {
            val data =
                when (it) {
                    "MMS" -> NetworkCapabilities.NET_CAPABILITY_MMS
                    "SUPL" -> NetworkCapabilities.NET_CAPABILITY_SUPL
                    "DUN" -> NetworkCapabilities.NET_CAPABILITY_DUN
                    "FOTA" -> NetworkCapabilities.NET_CAPABILITY_FOTA
                    "IMS" -> NetworkCapabilities.NET_CAPABILITY_IMS
                    "CBS" -> NetworkCapabilities.NET_CAPABILITY_CBS
                    "WIFI_P2P" -> NetworkCapabilities.NET_CAPABILITY_WIFI_P2P
                    "IA" -> NetworkCapabilities.NET_CAPABILITY_IA
                    "RCS" -> NetworkCapabilities.NET_CAPABILITY_RCS
                    "XCAP" -> NetworkCapabilities.NET_CAPABILITY_XCAP
                    "EIMS" -> NetworkCapabilities.NET_CAPABILITY_EIMS
                    "NOT_METERED" -> NetworkCapabilities.NET_CAPABILITY_NOT_METERED
                    "INTERNET" -> NetworkCapabilities.NET_CAPABILITY_INTERNET
                    "NOT_RESTRICTED" -> NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED
                    "TRUSTED" -> NetworkCapabilities.NET_CAPABILITY_TRUSTED
                    "NOT_VPN" -> NetworkCapabilities.NET_CAPABILITY_NOT_VPN
                    "VALIDATED" -> NetworkCapabilities.NET_CAPABILITY_VALIDATED
                    "CAPTIVE_PORTAL" -> NetworkCapabilities.NET_CAPABILITY_CAPTIVE_PORTAL
                    "NOT_ROAMING" -> NetworkCapabilities.NET_CAPABILITY_NOT_ROAMING
                    "FOREGROUND" -> NetworkCapabilities.NET_CAPABILITY_FOREGROUND
                    "NOT_CONGESTED" -> NetworkCapabilities.NET_CAPABILITY_NOT_CONGESTED
                    "NOT_SUSPENDED" -> NetworkCapabilities.NET_CAPABILITY_NOT_SUSPENDED
                    "OEM_PAID" -> 22 // NetworkCapabilities.NET_CAPABILITY_OEM_PAID
                    "MCX" -> {
                        checkSdkVersion(
                            Build.VERSION_CODES.S,
                            positiveWork = { NetworkCapabilities.NET_CAPABILITY_MCX },
                            negativeWork = { 23 },
                        )
                    }
                    "PARTIAL_CONNECTIVITY" -> 24 // NetworkCapabilities.NET_CAPABILITY_PARTIAL_CONNECTIVITY
                    "TEMPORARILY_NOT_METERED" -> {
                        checkSdkVersion(
                            Build.VERSION_CODES.R,
                            positiveWork = { NetworkCapabilities.NET_CAPABILITY_TEMPORARILY_NOT_METERED },
                            negativeWork = { 25 },
                        )
                    }
                    "OEM_PRIVATE" -> 26 // NetworkCapabilities.NET_CAPABILITY_OEM_PRIVATE
                    "VEHICLE_INTERNAL" -> 27 // NetworkCapabilities.NET_CAPABILITY_VEHICLE_INTERNAL
                    "NOT_VCN_MANAGED" -> 28 // NetworkCapabilities.NET_CAPABILITY_NOT_VCN_MANAGED
                    "ENTERPRISE" -> {
                        checkSdkVersion(
                            Build.VERSION_CODES.S,
                            positiveWork = { NetworkCapabilities.NET_CAPABILITY_ENTERPRISE },
                            negativeWork = { 29 },
                        )
                    }
                    "VSIM" -> 30 // NetworkCapabilities.NET_CAPABILITY_VSIM
                    "BIP" -> 31 // NetworkCapabilities.NET_CAPABILITY_BIP
                    "HEAD_UNIT" -> {
                        checkSdkVersion(
                            Build.VERSION_CODES.S,
                            positiveWork = { NetworkCapabilities.NET_CAPABILITY_HEAD_UNIT },
                            negativeWork = { 32 },
                        )
                    }
                    "MMTEL" -> {
                        checkSdkVersion(
                            Build.VERSION_CODES.TIRAMISU,
                            positiveWork = { NetworkCapabilities.NET_CAPABILITY_MMTEL },
                            negativeWork = { 33 },
                        )
                    }
                    "PRIORITIZE_LATENCY" -> {
                        checkSdkVersion(
                            Build.VERSION_CODES.TIRAMISU,
                            positiveWork = { NetworkCapabilities.NET_CAPABILITY_PRIORITIZE_LATENCY },
                            negativeWork = { 34 },
                        )
                    }
                    "PRIORITIZE_BANDWIDTH" -> {
                        checkSdkVersion(
                            Build.VERSION_CODES.TIRAMISU,
                            positiveWork = { NetworkCapabilities.NET_CAPABILITY_PRIORITIZE_BANDWIDTH },
                            negativeWork = { 35 },
                        )
                    }
                    "LOCAL_NETWORK" -> {
                        checkSdkVersion(
                            Build.VERSION_CODES.UPSIDE_DOWN_CAKE,
                            positiveWork = { NetworkCapabilities.NET_CAPABILITY_LOCAL_NETWORK },
                            negativeWork = { 36 },
                        )
                    }
                    else -> {
                        -1
                    }
                }
            res.add(data)
        }
        return res.toIntArray()
    }

    /**
     * Gets the subscription IDs associated with the network.<br><br>
     * 네트워크와 연관된 구독 ID들을 가져옵니다.<br>
     *
     * @return List of subscription IDs.<br><br>
     *         구독 ID 목록.
     */
    public fun getSubscriptionIds(): List<Int>? =
        checkSdkVersion(
            Build.VERSION_CODES.S,
            positiveWork = {
                if (SdkExtensions.getExtensionVersion(Build.VERSION_CODES.UPSIDE_DOWN_CAKE) >= 12) {
                    networkCapabilities.subscriptionIds.toList()
                } else {
                    val data = splitStr("SubscriptionIds: {", "}", ",")
                    data?.map { it -> it.toInt() }?.toList()
                }
            },
            negativeWork = {
                val data = splitStr("SubscriptionIds: {", "}", ",")
                data?.map { it -> it.toInt() }?.toList()
            },
        )

    /**
     * Gets the network specifier (API 30+).<br><br>
     * 네트워크 지정자를 가져옵니다 (API 30+).<br>
     *
     * @return NetworkSpecifier, or null.<br><br>
     *         NetworkSpecifier, 또는 null.
     */
    @RequiresApi(Build.VERSION_CODES.R)
    public fun getNetworkSpecifier(): NetworkSpecifier? = networkCapabilities.networkSpecifier

    /**
     * Gets the signal strength.<br><br>
     * 신호 강도를 가져옵니다.<br>
     *
     * @return Signal strength value.<br><br>
     *         신호 강도 값.
     */
    public fun getSignalStrength(): Int =
        checkSdkVersion(
            Build.VERSION_CODES.Q,
            positiveWork = { networkCapabilities.signalStrength },
            negativeWork = { splitStr("SignalStrength: ", " ", "")?.get(0)?.toInt() ?: Int.MIN_VALUE },
        )

    /**
     * Gets the UID of the app that owns this network (API 30+).<br><br>
     * 이 네트워크를 소유한 앱의 UID를 가져옵니다 (API 30+).<br>
     *
     * @return Owner UID.<br><br>
     *         소유자 UID.
     */
    @RequiresApi(Build.VERSION_CODES.R)
    public fun getOwnerUid(): Int = networkCapabilities.ownerUid

    /**
     * Gets the enterprise IDs (API 33+).<br><br>
     * 기업 ID들을 가져옵니다 (API 33+).<br>
     *
     * @return Array of enterprise IDs.<br><br>
     *         기업 ID 배열.
     */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    public fun getEnterpriseIds(): IntArray = networkCapabilities.enterpriseIds

    /**
     * Gets the transport info (API 29+).<br><br>
     * 전송 정보를 가져옵니다 (API 29+).<br>
     *
     * @return TransportInfo, or null.<br><br>
     *         TransportInfo, 또는 null.
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    public fun getTransportInfo(): TransportInfo? = networkCapabilities.transportInfo

    /**
     * Gets the BSSID from TransportInfo.<br><br>
     * TransportInfo에서 BSSID를 가져옵니다.<br>
     */
    public fun getBssidInTransportInfo(): String? = getDataInTransportInfoStr(", BSSID: ", ", ")

    /**
     * Gets the MAC address from TransportInfo.<br><br>
     * TransportInfo에서 MAC 주소를 가져옵니다.<br>
     */
    public fun getMacInTransportInfo(): String? = getDataInTransportInfoStr(", MAC: ", ", ")

    /**
     * Gets the IP address from TransportInfo.<br><br>
     * TransportInfo에서 IP 주소를 가져옵니다.<br>
     */
    public fun getIpTransportInfo(): String? = getDataInTransportInfoStr(", IP: ", ", ")

    /**
     * Gets the security type from TransportInfo.<br><br>
     * TransportInfo에서 보안 타입을 가져옵니다.<br>
     */
    public fun getSecurityTypeTransportInfo(): String? = getDataInTransportInfoStr(", Security type: ", ", ")

    /**
     * Gets the supplicant state from TransportInfo.<br><br>
     * TransportInfo에서 서플리컨트 상태를 가져옵니다.<br>
     */
    public fun getSupplicantStateTransportInfo(): String? = getDataInTransportInfoStr(", Supplicant state: ", ", ")

    /**
     * Gets the Wi-Fi standard from TransportInfo.<br><br>
     * TransportInfo에서 Wi-Fi 표준을 가져옵니다.<br>
     */
    public fun getWifiStandardTransportInfo(): String? = getDataInTransportInfoStr(", Wi-Fi standard: ", ", ")

    /**
     * Gets the RSSI from TransportInfo.<br><br>
     * TransportInfo에서 RSSI를 가져옵니다.<br>
     */
    public fun getRssiTransportInfo(): String? = getDataInTransportInfoStr(", RSSI: ", ", ")

    /**
     * Gets the link speed from TransportInfo.<br><br>
     * TransportInfo에서 링크 속도를 가져옵니다.<br>
     */
    public fun getLinkSpeedTransportInfo(): String? = getDataInTransportInfoStr(", Link speed: ", ", ")

    /**
     * Gets the Tx link speed from TransportInfo.<br><br>
     * TransportInfo에서 Tx 링크 속도를 가져옵니다.<br>
     */
    public fun getTxLinkSpeedTransportInfo(): String? = getDataInTransportInfoStr(", Tx Link speed: ", ", ")

    /**
     * Gets the max supported Tx link speed from TransportInfo.<br><br>
     * TransportInfo에서 최대 지원 Tx 링크 속도를 가져옵니다.<br>
     */
    public fun getMaxSupportedTxLinkSpeedTransportInfo(): String? = getDataInTransportInfoStr(", Max Supported Tx Link speed: ", ", ")

    /**
     * Gets the Rx link speed from TransportInfo.<br><br>
     * TransportInfo에서 Rx 링크 속도를 가져옵니다.<br>
     */
    public fun getRxLinkSpeedTransportInfo(): String? = getDataInTransportInfoStr(", Rx Link speed: ", ", ")

    /**
     * Gets the max supported Rx link speed from TransportInfo.<br><br>
     * TransportInfo에서 최대 지원 Rx 링크 속도를 가져옵니다.<br>
     */
    public fun getMaxRxSupportedLinkSpeedTransportInfo(): String? = getDataInTransportInfoStr(", Max Supported Rx Link speed: ", ", ")

    /**
     * Gets the frequency from TransportInfo.<br><br>
     * TransportInfo에서 주파수를 가져옵니다.<br>
     */
    public fun getFrequencyTransportInfo(): String? = getDataInTransportInfoStr(", Frequency: ", ", ")

    /**
     * Gets the Net ID from TransportInfo.<br><br>
     * TransportInfo에서 Net ID를 가져옵니다.<br>
     */
    public fun getNetIdTransportInfo(): String? = getDataInTransportInfoStr(", Net ID: ", ", ")

    /**
     * Checks if metered hint is present in TransportInfo.<br><br>
     * TransportInfo에 metered 힌트가 있는지 확인합니다.<br>
     */
    public fun isMeteredHintTransportInfo(): Boolean = getDataInTransportInfoStr(", Metered hint: ", ", ")?.equals("true") ?: false

    /**
     * Gets the score from TransportInfo.<br><br>
     * TransportInfo에서 점수를 가져옵니다.<br>
     */
    public fun getScoreTransportInfo(): String? = getDataInTransportInfoStr(", score: ", ", ")

    /**
     * Gets the subscription ID from TransportInfo.<br><br>
     * TransportInfo에서 구독 ID를 가져옵니다.<br>
     */
    public fun getSubscriptionIdTransportInfo(): String? = getDataInTransportInfoStr(", SubscriptionId: ", ", ")

    /**
     * Gets the IsPrimary flag from TransportInfo.<br><br>
     * TransportInfo에서 IsPrimary 플래그를 가져옵니다.<br>
     */
    public fun getIsPrimaryTransportInfo(): String? = getDataInTransportInfoStr(", IsPrimary: ", ", ")

    /**
     * Checks if usable flag is present in TransportInfo.<br><br>
     * TransportInfo에 사용 가능 플래그가 있는지 확인합니다.<br>
     */
    public fun isUsableTransportInfo(): Boolean = getDataInTransportInfoStr(", isUsable: ", ", ")?.equals("true") ?: false

    /**
     * Checks if carrier merged flag is present in TransportInfo.<br><br>
     * TransportInfo에 통신사 병합 플래그가 있는지 확인합니다.<br>
     */
    public fun isCarrierMergedTransportInfo(): Boolean = getDataInTransportInfoStr(", CarrierMerged: ", ", ")?.equals("true") ?: false

    /**
     * Checks if trusted flag is present in TransportInfo.<br><br>
     * TransportInfo에 신뢰할 수 있음 플래그가 있는지 확인합니다.<br>
     */
    public fun isTrustedTransportInfo(): Boolean = getDataInTransportInfoStr(", Trusted: ", ", ")?.equals("true") ?: false

    /**
     * Checks if restricted flag is present in TransportInfo.<br><br>
     * TransportInfo에 제한됨 플래그가 있는지 확인합니다.<br>
     */
    public fun isRestrictedTransportInfo(): Boolean = getDataInTransportInfoStr(", Restricted: ", ", ")?.equals("true") ?: false

    /**
     * Checks if ephemeral flag is present in TransportInfo.<br><br>
     * TransportInfo에 임시 플래그가 있는지 확인합니다.<br>
     */
    public fun isEphemeralTransportInfo(): Boolean = getDataInTransportInfoStr(", Ephemeral: ", ", ")?.equals("true") ?: false

    /**
     * Checks if OEM paid flag is present in TransportInfo.<br><br>
     * TransportInfo에 OEM 지불 플래그가 있는지 확인합니다.<br>
     */
    public fun isOemPaidTransportInfo(): Boolean = getDataInTransportInfoStr(", OEM paid: ", ", ")?.equals("true") ?: false

    /**
     * Checks if OEM private flag is present in TransportInfo.<br><br>
     * TransportInfo에 OEM 전용 플래그가 있는지 확인합니다.<br>
     */
    public fun isOemPrivateTransportInfo(): Boolean = getDataInTransportInfoStr(", OEM private: ", ", ")?.equals("true") ?: false

    /**
     * Checks if OSU AP flag is present in TransportInfo.<br><br>
     * TransportInfo에 OSU AP 플래그가 있는지 확인합니다.<br>
     */
    public fun isOsuApTransportInfo(): Boolean = getDataInTransportInfoStr(", OSU AP: ", ", ")?.equals("true") ?: false

    /**
     * Helper to extract data from TransportInfo string representation.<br><br>
     * TransportInfo 문자열 표현에서 데이터를 추출하는 헬퍼 함수입니다.<br>
     */
    private fun getDataInTransportInfoStr(
        start: String,
        end: String,
    ): String? =
        checkSdkVersion(
            Build.VERSION_CODES.Q,
            positiveWork = {
                networkCapabilities.transportInfo?.let {
                    val str = networkCapabilities.transportInfo.toString()
                    str.split(start, end)
                }
            },
            negativeWork = {
                if (isContains(transportInfoStr)) {
                    getResStr().split(transportInfoStr)[1]?.split(start, end)
                } else {
                    null
                }
            },
        )

    /**
     * Converts all properties to a readable string.<br><br>
     * 모든 속성을 읽기 쉬운 문자열로 변환합니다.<br>
     *
     * @return Formatted string representation.<br><br>
     *         포맷된 문자열 표현.
     */
    public fun toResString(): String {
        var res: String =
            " getCapabilities : ${getCapabilities()?.toList()}\n" +
                " getLinkUpstreamBandwidthKbps : ${getLinkUpstreamBandwidthKbps()}\n" +
                " getLinkDownstreamBandwidthKbps : ${getLinkDownstreamBandwidthKbps()}\n" +
                " getLinkUpstreamBandwidthKbps : ${getLinkUpstreamBandwidthKbps()}\n" +
                " getSubscriptionIds ${getSubscriptionIds()}\n" +
                " getSignalStrength ${getSignalStrength()}\n" +
                " getBssidInTransportInfo ${getBssidInTransportInfo()}\n" +
                " getMacInTransportInfo ${getMacInTransportInfo()}\n" +
                " getIpTransportInfo ${getIpTransportInfo()}\n" +
                " getSecurityTypeTransportInfo ${getSecurityTypeTransportInfo()}\n" +
                " getSupplicantStateTransportInfo ${getSupplicantStateTransportInfo()}\n" +
                " getWifiStandardTransportInfo ${getWifiStandardTransportInfo()}\n" +
                " getRssiTransportInfo ${getRssiTransportInfo()}\n" +
                " getLinkSpeedTransportInfo ${getLinkSpeedTransportInfo()}\n" +
                " getTxLinkSpeedTransportInfo ${getTxLinkSpeedTransportInfo()}\n" +
                " getMaxSupportedTxLinkSpeedTransportInfo ${getMaxSupportedTxLinkSpeedTransportInfo()}\n" +
                " getRxLinkSpeedTransportInfo ${getRxLinkSpeedTransportInfo()}\n" +
                " getMaxRxSupportedLinkSpeedTransportInfo ${getMaxRxSupportedLinkSpeedTransportInfo()}\n" +
                " getFrequencyTransportInfo ${getFrequencyTransportInfo()}\n" +
                " getNetIdTransportInfo ${getNetIdTransportInfo()}\n" +
                " isMeteredHintTransportInfo ${isMeteredHintTransportInfo()}\n" +
                " getScoreTransportInfo ${getScoreTransportInfo()}\n" +
                " getSubscriptionIdTransportInfo ${getSubscriptionIdTransportInfo()}\n" +
                " getIsPrimaryTransportInfo ${getIsPrimaryTransportInfo()}\n" +
                " isUsableTransportInfo ${isUsableTransportInfo()}\n" +
                " isCarrierMergedTransportInfo ${isCarrierMergedTransportInfo()}\n" +
                " isTrustedTransportInfo ${isTrustedTransportInfo()}\n" +
                " isRestrictedTransportInfo ${isRestrictedTransportInfo()}\n" +
                " isEphemeralTransportInfo ${isEphemeralTransportInfo()}\n" +
                " isOemPaidTransportInfo ${isOemPaidTransportInfo()}\n" +
                " isOemPrivateTransportInfo ${isOemPrivateTransportInfo()}\n" +
                " isOsuApTransportInfo ${isOsuApTransportInfo()}\n"

        checkSdkVersion(Build.VERSION_CODES.Q) {
            res += " getTransportInfo : ${getTransportInfo()}\n"
        }
        checkSdkVersion(Build.VERSION_CODES.R) {
            res += " getNetworkSpecifier : ${getNetworkSpecifier()}\n" +
                " getOwnerUid : ${getOwnerUid()}\n"
        }
        checkSdkVersion(Build.VERSION_CODES.TIRAMISU) {
            res += " getEnterpriseIds : ${getEnterpriseIds().toList()}\n"
        }
        res += "\n\n"

        return res
    }
}
