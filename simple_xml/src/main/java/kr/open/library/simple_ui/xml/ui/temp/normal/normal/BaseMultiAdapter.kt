package kr.open.library.simple_ui.xml.ui.temp.normal.normal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kr.open.library.simple_ui.xml.ui.temp.base.normal.BaseRcvNormalAdapterCore

abstract class BaseMultiAdapter<ITEM : Any, VH : RecyclerView.ViewHolder>(
    private val layoutResProvider: (item: ITEM, position: Int) -> Int,
    diffUtilEnabled: Boolean = false,
) : BaseRcvNormalAdapterCore<ITEM, VH>(diffUtilEnabled) {
    override fun getItemViewType(position: Int): Int = layoutResProvider(getItem(position), position)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return getCreateViewHolder(view)
    }

    final override fun onBindItem(holder: VH, position: Int, item: ITEM) {
        onBind(holder.itemView, item, position, holder.itemViewType)
    }

    protected abstract fun onBind(view: View, item: ITEM, position: Int, viewType: Int)
}
