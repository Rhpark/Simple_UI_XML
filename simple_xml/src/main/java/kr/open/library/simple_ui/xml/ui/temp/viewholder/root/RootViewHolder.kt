package kr.open.library.simple_ui.xml.ui.temp.viewholder.root

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class RootViewHolder(
    itemView: View
) : RecyclerView.ViewHolder(itemView) {
    /**
     * Verifies whether the adapter position is valid (for listeners).<br><br>
     * 어댑터 포지션이 유효한지 확인합니다(리스너용).<br>
     */
    protected fun isValidPosition(): Boolean = (adapterPosition > RecyclerView.NO_POSITION)

    /**
     * Gets the current adapter position safely.<br><br>
     * 현재 어댑터 포지션을 안전하게 반환합니다.<br>
     *
     * @return Adapter position, or -1 if invalid.<br><br>
     *         유효하지 않으면 -1을 반환합니다.<br>
     */
    protected fun getAdapterPositionSafe(): Int = if (isValidPosition()) adapterPosition else RecyclerView.NO_POSITION

    protected fun getContext(): Context = itemView.context
}
