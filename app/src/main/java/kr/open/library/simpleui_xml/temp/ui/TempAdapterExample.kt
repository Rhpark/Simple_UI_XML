package kr.open.library.simpleui_xml.temp.ui

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kr.open.library.simpleui_xml.temp.data.TempItem
import java.util.concurrent.Executor

/**
 * Adapter kind used to enable or disable example options.<br><br>
 * 예제 옵션 활성/비활성 기준으로 사용하는 어댑터 종류입니다.<br>
 */
enum class TempAdapterKind {
    /**
     * RecyclerView.Adapter based examples.<br><br>
     * RecyclerView.Adapter 기반 예제입니다.<br>
     */
    RV,

    /**
     * ListAdapter based examples.<br><br>
     * ListAdapter 기반 예제입니다.<br>
     */
    LIST,
}

/**
 * Item mode used for single or multi type examples.<br><br>
 * 단일/다중 타입 예제를 구분하는 아이템 모드입니다.<br>
 */
enum class TempItemMode {
    /**
     * Single-type item mode.<br><br>
     * 단일 타입 아이템 모드입니다.<br>
     */
    SINGLE,

    /**
     * Multi-type item mode.<br><br>
     * 다중 타입 아이템 모드입니다.<br>
     */
    MULTI,
}

/**
 * Dependencies used to create adapters for examples.<br><br>
 * 예제 어댑터 생성을 위한 의존성 묶음입니다.<br>
 */
data class TempAdapterDependencies(
    /**
     * Executor used for background diff computation.<br><br>
     * 백그라운드 diff 계산에 사용하는 Executor입니다.<br>
     */
    val diffExecutor: Executor?,
    /**
     * Custom DiffUtil callback for ListAdapter examples.<br><br>
     * ListAdapter 예제에서 사용하는 커스텀 DiffUtil 콜백입니다.<br>
     */
    val diffCallback: DiffUtil.ItemCallback<TempItem>?,
    /**
     * DiffUtil toggle for RecyclerView.Adapter examples.<br><br>
     * RecyclerView.Adapter 예제에서 사용하는 DiffUtil 토글입니다.<br>
     */
    val diffUtilEnabled: Boolean,
)

/**
 * Descriptor for a single adapter example entry.<br><br>
 * 단일 어댑터 예제 항목을 설명하는 모델입니다.<br>
 */
data class TempAdapterExample(
    /**
     * Title shown in the UI.<br><br>
     * UI에 표시되는 제목입니다.<br>
     */
    val title: String,
    /**
     * Adapter kind used for option availability.<br><br>
     * 옵션 사용 여부 판단을 위한 어댑터 종류입니다.<br>
     */
    val kind: TempAdapterKind,
    /**
     * Item mode used to create sample items.<br><br>
     * 샘플 아이템 생성에 사용하는 아이템 모드입니다.<br>
     */
    val itemMode: TempItemMode,
    /**
     * Factory that builds an adapter with provided dependencies.<br><br>
     * 제공된 의존성으로 어댑터를 생성하는 팩토리입니다.<br>
     */
    val createAdapter: (TempAdapterDependencies) -> RecyclerView.Adapter<*>,
)
