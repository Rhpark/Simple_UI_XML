package kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.cell.gsm

import android.telephony.CellIdentityGsm

/**
 * Wrapper class for CellIdentityGsm.<br><br>
 * CellIdentityGsm을 위한 래퍼 클래스입니다.<br>
 *
 * Provides accessors for GSM cell identity information.<br><br>
 * GSM 셀 식별 정보에 대한 접근자를 제공합니다.<br>
 *
 * @property cellIdentity The original CellIdentityGsm object.<br><br>
 *                        원본 CellIdentityGsm 객체.
 */
public data class CellIdentityGsmData(
    public val cellIdentity: CellIdentityGsm? = null
) {
    /******************************
     * get currentCellIdentityLTE *
     ******************************/
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
    /**
     * Gets the ARFCN (Absolute Radio Frequency Channel Number).<br><br>
     * ARFCN (절대 무선 주파수 채널 번호)을 가져옵니다.<br>
     */
    public fun getArfcn(): Int? = cellIdentity?.arfcn
    /**
     * Gets the CID (Cell Identity).<br><br>
     * CID (셀 식별자)를 가져옵니다.<br>
     */
    public fun getCid(): Int? = cellIdentity?.cid
    /**
     * Gets the LAC (Location Area Code).<br><br>
     * LAC (위치 영역 코드)를 가져옵니다.<br>
     */
    public fun getLac(): Int? = cellIdentity?.lac
    /**
     * Gets the BSIC (Base Station Identity Code).<br><br>
     * BSIC (기지국 식별 코드)를 가져옵니다.<br>
     */
    public fun getBsic(): Int? = cellIdentity?.bsic
    /**
     * Gets the MCC (Mobile Country Code) string.<br><br>
     * MCC (모바일 국가 코드) 문자열을 가져옵니다.<br>
     */
    public fun getMccString(): String? = cellIdentity?.mccString
    /**
     * Gets the MNC (Mobile Network Code) string.<br><br>
     * MNC (모바일 네트워크 코드) 문자열을 가져옵니다.<br>
     */
    public fun getMncString(): String? = cellIdentity?.mncString
    /**
     * Gets the Mobile Network Operator.<br><br>
     * 모바일 네트워크 사업자를 가져옵니다.<br>
     */
    public fun getMobileNetworkOperator(): String? = cellIdentity?.mobileNetworkOperator
}