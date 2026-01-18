package kr.open.library.simple_ui.xml.ui.temp.viewholder.root

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Base ViewHolder providing safe position helpers.<br><br>
 * 안전한 포지션 헬퍼를 제공하는 기본 ViewHolder입니다.<br>
 */
abstract class RootViewHolder(
    /**
     * Root item view for this holder.<br><br>
     * 해당 홀더의 루트 아이템 뷰입니다.<br>
     */
    itemView: View,
) : RecyclerView.ViewHolder(itemView) {
    /**
     * Checks whether the adapter position is valid for listener usage.<br><br>
     * 리스너 사용을 위한 어댑터 포지션이 유효한지 확인합니다.<br>
     */
    protected fun isValidPosition(): Boolean = (adapterPosition > RecyclerView.NO_POSITION)

    /**
     * Returns the current adapter position safely or -1 when invalid.<br><br>
     * 현재 어댑터 포지션을 안전하게 반환하며 유효하지 않으면 -1을 반환합니다.<br>
     */
    protected fun getAdapterPositionSafe(): Int = if (isValidPosition()) adapterPosition else RecyclerView.NO_POSITION

    /**
     * Returns the itemView context.<br><br>
     * itemView의 Context를 반환합니다.<br>
     */
    protected fun getContext(): Context = itemView.context
}
