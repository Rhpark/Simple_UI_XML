package kr.open.library.simple_ui.xml.ui.temp.viewholder.binding

import androidx.viewbinding.ViewBinding
import kr.open.library.simple_ui.xml.ui.temp.viewholder.root.RootViewHolder

/**
 * Base ViewHolder that holds a ViewBinding instance.<br><br>
 * ViewBinding 인스턴스를 보관하는 베이스 ViewHolder입니다.<br>
 */
abstract class BaseBindingViewHolder<BINDING : ViewBinding>(
    /**
     * ViewBinding instance for this holder.<br><br>
     * 해당 홀더의 ViewBinding 인스턴스입니다.<br>
     */
    public val binding: BINDING,
) : RootViewHolder(binding.root)
