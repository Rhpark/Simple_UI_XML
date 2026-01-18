package kr.open.library.simple_ui.xml.ui.temp.base.normal

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import java.util.concurrent.Executor

/**
 * Base core for non-binding RecyclerView adapters.<br><br>
 * Non-binding RecyclerView 어댑터용 베이스 코어입니다.<br>
 */
abstract class BaseRcvNormalAdapterCore<ITEM : Any, VH : RecyclerView.ViewHolder>(
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
) : RootRcvAdapterCore<ITEM, VH>(diffUtilEnabled, diffExecutor) {
    /**
     * Creates a ViewHolder from the given view.<br><br>
     * 주어진 View로 ViewHolder를 생성합니다.<br>
     */
    abstract fun getCreateViewHolder(view: View): VH
}
