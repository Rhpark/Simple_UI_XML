package kr.open.library.simple_ui.xml.ui.adapter.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

public open class BaseBindingRcvViewHolder<BINDING : ViewDataBinding>(
    @LayoutRes xmlRes: Int,
    parent: ViewGroup,
    attachToRoot: Boolean = false,
) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(xmlRes, parent, attachToRoot),
    ) {
    public val binding: BINDING by lazy {
        DataBindingUtil.bind<BINDING>(itemView) ?: throw IllegalStateException("Exception Binding is null!!")
    }

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

    /**
     * Executes pending bindings for DataBinding.<br><br>
     * DataBinding의 pending 바인딩을 실행합니다.<br>
     */
    protected fun executePendingBindings() {
        binding.executePendingBindings()
    }
}
