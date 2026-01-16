package kr.open.library.simple_ui.xml.ui.temp.viewholder.binding

import androidx.viewbinding.ViewBinding
import kr.open.library.simple_ui.xml.ui.temp.viewholder.root.RootViewHolder

abstract class BaseBindingViewHolder<BINDING : ViewBinding>(
    public val binding: BINDING,
) : RootViewHolder(binding.root)
