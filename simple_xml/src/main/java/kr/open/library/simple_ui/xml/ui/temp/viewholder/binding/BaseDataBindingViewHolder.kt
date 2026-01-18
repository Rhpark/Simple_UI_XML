package kr.open.library.simple_ui.xml.ui.temp.viewholder.binding

import androidx.databinding.ViewDataBinding

/**
 * DataBinding-based ViewHolder implementation.<br><br>
 * DataBinding 기반 ViewHolder 구현체입니다.<br>
 */
open class BaseDataBindingViewHolder<BINDING : ViewDataBinding>(
    /**
     * ViewDataBinding instance for this holder.<br><br>
     * 해당 홀더의 ViewDataBinding 인스턴스입니다.<br>
     */
    binding: BINDING,
) : BaseBindingViewHolder<BINDING>(binding)
