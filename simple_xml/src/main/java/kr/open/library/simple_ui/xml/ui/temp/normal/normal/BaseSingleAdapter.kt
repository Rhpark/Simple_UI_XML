package kr.open.library.simple_ui.xml.ui.temp.normal.normal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import kr.open.library.simple_ui.xml.ui.temp.base.normal.BaseRcvNormalAdapterCore

abstract class BaseSingleAdapter<ITEM : Any, VH : RecyclerView.ViewHolder>(
    @LayoutRes private val layoutRes: Int,
    diffUtilEnabled: Boolean = false,
) : BaseRcvNormalAdapterCore<ITEM, VH>(diffUtilEnabled) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        return getCreateViewHolder(view)
    }

    final override fun onBindItem(holder: VH, position: Int, item: ITEM) {
        onBind(holder.itemView, item, position)
    }

    protected abstract fun onBind(view: View, item: ITEM, position: Int)
}
