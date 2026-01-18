package kr.open.library.simple_ui.xml.ui.temp.normal.normal

import android.view.View
import androidx.annotation.LayoutRes
import kr.open.library.simple_ui.xml.ui.temp.viewholder.normal.BaseRcvViewHolder
import java.util.concurrent.Executor

/**
 * Simple single-layout adapter using a lambda-based binder.<br><br>
 * 람다 기반 바인더를 사용하는 간단 단일 레이아웃 어댑터입니다.<br>
 */
open class SimpleSingleAdapter<ITEM : Any>(
    /**
     * Layout resource for item view inflation.<br><br>
     * 아이템 뷰 인플레이트에 사용할 레이아웃 리소스입니다.<br>
     */
    @LayoutRes layoutRes: Int,
    /**
     * Enables DiffUtil for normal adapter updates; keep OFF for very large replacements.<br><br>
     * 일반 어댑터 업데이트에서 DiffUtil을 활성화하며 대량 치환은 OFF를 권장합니다.<br>
     */
    diffUtilEnabled: Boolean = false,
    /**
     * Executor used for DiffUtil background computation.<br><br>
     * DiffUtil 백그라운드 계산에 사용하는 Executor입니다.<br>
     */
    diffExecutor: Executor? = null,
    /**
     * Lambda invoked to bind the view with item data.<br><br>
     * 아이템 데이터를 뷰에 바인딩하는 람다입니다.<br>
     */
    private val onBindItem: (view: View, item: ITEM, position: Int) -> Unit,
) : BaseSingleAdapter<ITEM, BaseRcvViewHolder>(layoutRes, diffUtilEnabled, diffExecutor) {
    /**
     * Binds the view using the provided lambda.<br><br>
     * 제공된 람다로 뷰를 바인딩합니다.<br>
     */
    override fun onBind(view: View, item: ITEM, position: Int) {
        onBindItem(view, item, position)
    }

    /**
     * Creates a basic RecyclerView ViewHolder.<br><br>
     * 기본 RecyclerView ViewHolder를 생성합니다.<br>
     */
    override fun getCreateViewHolder(view: View): BaseRcvViewHolder = BaseRcvViewHolder(view)
}
