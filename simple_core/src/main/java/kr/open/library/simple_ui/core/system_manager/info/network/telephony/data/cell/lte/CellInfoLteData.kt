package kr.open.library.simple_ui.core.system_manager.info.network.telephony.data.cell.lte

import android.os.Build
import android.telephony.CellInfoLte
import androidx.annotation.RequiresApi

/**
 * Wrapper class for CellInfoLte.<br><br>
 * CellInfoLte를 위한 래퍼 클래스입니다.<br>
 *
 * Provides accessors for LTE cell info, identity, and signal strength.<br><br>
 * LTE 셀 정보, 식별자, 신호 강도에 대한 접근자를 제공합니다.<br>
 *
 * @property cellInfo The original CellInfoLte object.<br><br>
 *                    원본 CellInfoLte 객체.
 */
public data class CellInfoLteData(
    private var cellInfo : CellInfoLte?
) {
    private var cellDataLteIdentity = CellIdentityLteData(cellInfo?.cellIdentity)
    private var cellDataLteSignalStrength = CellSignalStrengthLteData(cellInfo?.cellSignalStrength)

    /*******************
     * get CellInfoLte *
     *******************/
    /**
     * Gets the timestamp in milliseconds (API 30+).<br><br>
     * 밀리초 단위의 타임스탬프를 가져옵니다 (API 30+).<br>
     */
    @RequiresApi(Build.VERSION_CODES.R)
    public fun getTimestampMillis(): Long? = cellInfo?.timestampMillis
    /**
     * Gets the cell connection status.<br><br>
     * 셀 연결 상태를 가져옵니다.<br>
     */
    public fun getCellConnectionStatus(): Int? = cellInfo?.cellConnectionStatus
    /**
     * Checks if the cell is registered.<br><br>
     * 셀이 등록되었는지 확인합니다.<br>
     */
    public fun isRegistered(): Boolean? = cellInfo?.isRegistered

    /**
     * Gets the cell identity data.<br><br>
     * 셀 식별 데이터를 가져옵니다.<br>
     */
    public fun getCellIdentity(): CellIdentityLteData = cellDataLteIdentity
    /**
     * Gets the cell signal strength data.<br><br>
     * 셀 신호 강도 데이터를 가져옵니다.<br>
     */
    public fun getCellSignalStrength(): CellSignalStrengthLteData = cellDataLteSignalStrength

}