package kr.open.library.simple_ui.xml.ui.temp.list.normal

import android.view.View
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.DiffUtil
import kr.open.library.simple_ui.xml.ui.temp.base.list.diffcallback.DefaultDiffCallback
import kr.open.library.simple_ui.xml.ui.temp.viewholder.normal.BaseRcvViewHolder

open class SimpleSingleListAdapter<ITEM : Any>(
    @LayoutRes layoutRes: Int,
    diffCallback: DiffUtil.ItemCallback<ITEM> = DefaultDiffCallback(),
    private val onBindItem: (view: View, item: ITEM, position: Int) -> Unit,
) : BaseSingleListAdapter<ITEM>(layoutRes, diffCallback) {
    override fun onBind(view: View, item: ITEM, position: Int) {
        onBindItem(view, item, position)
    }

    override fun getCreateViewHolder(view: View): BaseRcvViewHolder = BaseRcvViewHolder(view)
}
