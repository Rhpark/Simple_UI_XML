package kr.open.library.simpleui_xml.recyclerview.new_.adapter

import android.view.ViewGroup
import kr.open.library.simple_ui.xml.ui.adapter.list.base.BaseRcvListAdapter
import kr.open.library.simple_ui.xml.ui.adapter.list.diffutil.RcvListDiffUtilCallBack
import kr.open.library.simple_ui.xml.ui.adapter.viewholder.BaseBindingRcvViewHolder
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.databinding.ItemRcvTextviewBinding
import kr.open.library.simpleui_xml.recyclerview.model.SampleItem

class CustomListAdapter :
    BaseRcvListAdapter<SampleItem, BaseBindingRcvViewHolder<ItemRcvTextviewBinding>>(
        listDiffUtil =
            RcvListDiffUtilCallBack(
                itemsTheSame = { oldItem, newItem -> oldItem.id == newItem.id },
                contentsTheSame = { oldItem, newItem -> oldItem == newItem },
            ),
    ) {
    override fun onBindViewHolder(
        holder: BaseBindingRcvViewHolder<ItemRcvTextviewBinding>,
        position: Int,
        item: SampleItem,
    ) {
        holder.binding.apply {
            tvTitle.text = item.title
            tvDescription.text = item.description
            tvPosition.text = "Position: $position"
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): BaseBindingRcvViewHolder<ItemRcvTextviewBinding> = BaseBindingRcvViewHolder(R.layout.item_rcv_textview, parent)
}
