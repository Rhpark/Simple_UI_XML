package kr.open.library.simpleui_xml.recyclerview.origin.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.databinding.ItemRcvTextviewBinding
import kr.open.library.simpleui_xml.recyclerview.model.SampleItem

class OriginCustomAdapter(
    private val onItemClick: (SampleItem, Int) -> Unit,
) : RecyclerView.Adapter<OriginCustomAdapter.SampleItemViewHolder>() {
    private var items = mutableListOf<SampleItem>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): SampleItemViewHolder {
        val binding =
            DataBindingUtil.inflate<ItemRcvTextviewBinding>(
                LayoutInflater.from(parent.context),
                R.layout.item_rcv_textview,
                parent,
                false,
            )
        return SampleItemViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: SampleItemViewHolder,
        position: Int,
    ) {
        val item = items[position]
        holder.bind(item, position, onItemClick)
    }

    override fun getItemCount(): Int = items.size

    fun setItems(newItems: List<SampleItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun addItem(item: SampleItem) {
        items.add(item)
        notifyItemInserted(items.size - 1)
    }

    fun removeAt(position: Int) {
        if (position in items.indices) {
            items.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, items.size)
        }
    }

    fun removeAll() {
        val oldSize = items.size
        items.clear()
        notifyItemRangeRemoved(0, oldSize)
    }

    fun getItems(): List<SampleItem> = items.toList()

    class SampleItemViewHolder(
        private val binding: ItemRcvTextviewBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: SampleItem,
            position: Int,
            onItemClick: (SampleItem, Int) -> Unit,
        ) {
            binding.apply {
                tvTitle.text = item.title
                tvDescription.text = item.description
                tvPosition.text = "Position: $position"

                root.setOnClickListener { onItemClick(item, position) }

                executePendingBindings()
            }
        }
    }
}
