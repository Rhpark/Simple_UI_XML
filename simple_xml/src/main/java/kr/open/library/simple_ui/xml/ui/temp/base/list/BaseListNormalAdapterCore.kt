package kr.open.library.simple_ui.xml.ui.temp.base.list

import android.view.View
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kr.open.library.simple_ui.xml.ui.temp.base.list.diffcallback.DefaultDiffCallback

abstract class BaseListNormalAdapterCore<ITEM : Any, VH : RecyclerView.ViewHolder>(
    diffCallback: DiffUtil.ItemCallback<ITEM> = DefaultDiffCallback(),
) : RootListAdapterCore<ITEM, VH>(diffCallback) {
    abstract fun getCreateViewHolder(view: View): VH
}
