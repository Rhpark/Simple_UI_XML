package kr.open.library.simpleui_xml.temp.ui

import androidx.recyclerview.widget.RecyclerView

/**
 * RecyclerView.Adapter single-type example screen.<br><br>
 * RecyclerView.Adapter 단일 타입 예제 화면입니다.<br>
 */
class TempAdapterRcvSingleActivity : TempAdapterExampleBaseActivity() {
    /**
     * Adapter kind for this screen.<br><br>
     * 이 화면에서 사용하는 어댑터 종류입니다.<br>
     */
    override val adapterKind: TempAdapterKind = TempAdapterKind.RV

    /**
     * Item mode for this screen.<br><br>
     * 이 화면에서 사용하는 아이템 모드입니다.<br>
     */
    override val itemMode: TempItemMode = TempItemMode.SINGLE

    /**
     * Title shown at the top of the screen.<br><br>
     * 화면 상단에 표시되는 제목입니다.<br>
     */
    override val screenTitle: String = "Adapter - Single"

    /**
     * Creates an adapter for this screen.<br><br>
     * 이 화면에서 사용할 어댑터를 생성합니다.<br>
     */
    override fun createAdapter(config: TempAdapterConfig): RecyclerView.Adapter<*> =
        TempAdapterFactory.createAdapter(config)
}
