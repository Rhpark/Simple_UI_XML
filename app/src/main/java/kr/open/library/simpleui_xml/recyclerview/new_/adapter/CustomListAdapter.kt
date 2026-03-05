package kr.open.library.simpleui_xml.recyclerview.new_.adapter

import android.view.ViewGroup
import kr.open.library.simple_ui.xml.ui.adapter.list.base.BaseRcvListAdapter
import kr.open.library.simple_ui.xml.ui.adapter.list.base.diffutil.RcvListDiffUtilCallBack
import kr.open.library.simple_ui.xml.ui.adapter.viewholder.BaseRcvDataBindingViewHolder
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.databinding.ItemRcvTextviewBinding
import kr.open.library.simpleui_xml.recyclerview.model.SampleItem

class CustomListAdapter :
    BaseRcvListAdapter<SampleItem, BaseRcvDataBindingViewHolder<ItemRcvTextviewBinding>>(
        listDiffUtil = RcvListDiffUtilCallBack(
            itemsTheSame = { oldItem, newItem -> oldItem.id == newItem.id },
            contentsTheSame = { oldItem, newItem -> oldItem == newItem },
        ),
    ) {
    override fun onBindViewHolder(
        holder: BaseRcvDataBindingViewHolder<ItemRcvTextviewBinding>,
        item: SampleItem,
        position: Int,
    ) {
        holder.binding.apply {
            tvTitle.text = item.title
            tvDescription.text = item.description
            tvPosition.text = "Position: $position"
        }
    }

    override fun createViewHolderInternal(parent: ViewGroup, viewType: Int): BaseRcvDataBindingViewHolder<ItemRcvTextviewBinding> =
        BaseRcvDataBindingViewHolder(R.layout.item_rcv_textview, parent)
}
