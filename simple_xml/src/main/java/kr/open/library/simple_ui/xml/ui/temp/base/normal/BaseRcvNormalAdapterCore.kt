package kr.open.library.simple_ui.xml.ui.temp.base.normal

import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class BaseRcvNormalAdapterCore<ITEM : Any, VH : RecyclerView.ViewHolder>(
    diffUtilEnabled: Boolean = false
) : RootRcvAdapterCore<ITEM, VH>(diffUtilEnabled) {
    abstract fun getCreateViewHolder(view: View): VH
}
