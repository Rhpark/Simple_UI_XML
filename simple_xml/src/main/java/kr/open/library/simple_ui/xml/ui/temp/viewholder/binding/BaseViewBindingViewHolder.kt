package kr.open.library.simple_ui.xml.ui.temp.viewholder.binding

import androidx.viewbinding.ViewBinding

/**
 * ViewBinding-based ViewHolder implementation.<br><br>
 * ViewBinding 기반 ViewHolder 구현체입니다.<br>
 */
open class BaseViewBindingViewHolder<BINDING : ViewBinding>(
    /**
     * ViewBinding instance for this holder.<br><br>
     * 해당 홀더의 ViewBinding 인스턴스입니다.<br>
     */
    binding: BINDING,
) : BaseBindingViewHolder<BINDING>(binding)
