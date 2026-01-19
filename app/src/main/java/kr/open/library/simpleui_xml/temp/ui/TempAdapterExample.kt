package kr.open.library.simpleui_xml.temp.ui

import androidx.recyclerview.widget.DiffUtil
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
 * Binding mode used to select normal, DataBinding, or ViewBinding adapters.<br><br>
 * 일반/DataBinding/ViewBinding 어댑터를 선택하는 바인딩 모드입니다.<br>
 */
enum class TempBindingMode {
    /**
     * Normal view holder binding mode.<br><br>
     * 일반 뷰홀더 바인딩 모드입니다.<br>
     */
    NORMAL,

    /**
     * DataBinding binding mode.<br><br>
     * DataBinding 바인딩 모드입니다.<br>
     */
    DATABINDING,

    /**
     * ViewBinding binding mode.<br><br>
     * ViewBinding 바인딩 모드입니다.<br>
     */
    VIEWBINDING,
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
