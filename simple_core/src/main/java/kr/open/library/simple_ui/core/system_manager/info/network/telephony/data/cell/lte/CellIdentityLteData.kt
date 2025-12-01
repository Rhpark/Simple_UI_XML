package kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.cell.lte

import android.os.Build
import android.telephony.CellIdentityLte
import androidx.annotation.RequiresApi

/**
 * Wrapper class for CellIdentityLte.<br><br>
 * CellIdentityLte를 위한 래퍼 클래스입니다.<br>
 *
 * Provides accessors for LTE cell identity information.<br><br>
 * LTE 셀 식별 정보에 대한 접근자를 제공합니다.<br>
 *
 * @property cellIdentity The original CellIdentityLte object.<br><br>
 *                        원본 CellIdentityLte 객체.
 */
public data class CellIdentityLteData(
    public val cellIdentity: CellIdentityLte? = null,
) {
    /**
     * Gets the bands of the cell (API 30+).<br><br>
     * 셀의 밴드를 가져옵니다 (API 30+).<br>
     */
    @RequiresApi(Build.VERSION_CODES.R)
    public fun getBandList(): IntArray? = cellIdentity?.bands

    /**
     * Gets the PCI (Physical Cell Id).<br><br>
     * PCI (물리적 셀 ID)를 가져옵니다.<br>
     */
    public fun getPci(): Int? = cellIdentity?.pci

    /**
     * Gets the TAC (Tracking Area Code).<br><br>
     * TAC (추적 영역 코드)를 가져옵니다.<br>
     */
    public fun getTac(): Int? = cellIdentity?.tac

    /**
     * Gets the EARFCN (E-UTRA Absolute Radio Frequency Channel Number).<br><br>
     * EARFCN (E-UTRA 절대 무선 주파수 채널 번호)을 가져옵니다.<br>
     */
    public fun getEarfcn(): Int? = cellIdentity?.earfcn

    /**
     * Gets the bandwidth.<br><br>
     * 대역폭을 가져옵니다.<br>
     */
    public fun getBandWidth(): Int? = cellIdentity?.bandwidth

    /**
     * Gets the CI (Cell Identity).<br><br>
     * CI (셀 식별자)를 가져옵니다.<br>
     */
    public fun getEci(): Int? = cellIdentity?.ci

    /**
     * Gets the MCC (Mobile Country Code) string.<br><br>
     * MCC (모바일 국가 코드) 문자열을 가져옵니다.<br>
     */
    public fun getMcc(): String? = cellIdentity?.mccString

    /**
     * Gets the MNC (Mobile Network Code) string.<br><br>
     * MNC (모바일 네트워크 코드) 문자열을 가져옵니다.<br>
     */
    public fun getMnc(): String? = cellIdentity?.mncString

    /**
     * Gets the eNodeB ID.<br><br>
     * eNodeB ID를 가져옵니다.<br>
     */
    public fun getEnb(): Int? = cellIdentity?.ci?.ushr(8)?.and(0xFFFFF)

    /**
     * Gets the LCID (Logical Cell ID).<br><br>
     * LCID (논리적 셀 ID)를 가져옵니다.<br>
     */
    public fun getLcid(): Int? = cellIdentity?.ci?.and(0xFF)

    /**
     * Gets the Mobile Network Operator.<br><br>
     * 모바일 네트워크 사업자를 가져옵니다.<br>
     */
    public fun getMobileNetworkOperator(): String? = cellIdentity?.mobileNetworkOperator

    /**
     * Gets the long alpha tag.<br><br>
     * 긴 알파 태그를 가져옵니다.<br>
     */
    public fun getOperatorAlphaLong(): CharSequence? = cellIdentity?.operatorAlphaLong

    /**
     * Gets the short alpha tag.<br><br>
     * 짧은 알파 태그를 가져옵니다.<br>
     */
    public fun getOperatorAlphaShort(): CharSequence? = cellIdentity?.operatorAlphaShort
}
