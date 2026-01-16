package kr.open.library.simple_ui.xml.ui.temp.list.normal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import kr.open.library.simple_ui.xml.ui.temp.base.list.BaseListNormalAdapterCore
import kr.open.library.simple_ui.xml.ui.temp.base.list.diffcallback.DefaultDiffCallback
import kr.open.library.simple_ui.xml.ui.temp.viewholder.normal.BaseRcvViewHolder

abstract class BaseMultiListAdapter<ITEM : Any>(
    private val layoutResProvider: (item: ITEM, position: Int) -> Int,
    diffCallback: DiffUtil.ItemCallback<ITEM> = DefaultDiffCallback(),
) : BaseListNormalAdapterCore<ITEM, BaseRcvViewHolder>(diffCallback) {
    override fun getItemViewType(position: Int): Int =
        layoutResProvider(getItem(position), position)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseRcvViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return getCreateViewHolder(view)
    }

    final override fun onBindItem(holder: BaseRcvViewHolder, position: Int, item: ITEM) {
        onBind(holder.itemView, item, position, holder.itemViewType)
    }

    protected abstract fun onBind(view: View, item: ITEM, position: Int, viewType: Int)
}
