package kr.open.library.simple_ui.xml.ui.adapter.viewholder

import androidx.viewbinding.ViewBinding

/**
 * Base ViewHolder for RecyclerView using ViewBinding.<br><br>
 * ViewBinding을 사용하는 RecyclerView용 기본 ViewHolder입니다.<br>
 *
 * @param binding Generated ViewBinding instance for the item view.<br><br>
 *                아이템 뷰에 대응하는 생성된 ViewBinding 인스턴스입니다.<br>
 */
public open class BaseRcvViewBindingViewHolder<BINDING : ViewBinding>(
    public val binding: BINDING,
) : RootViewHolder(binding.root)
