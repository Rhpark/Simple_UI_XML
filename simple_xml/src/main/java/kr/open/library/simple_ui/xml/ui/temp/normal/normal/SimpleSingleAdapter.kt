package kr.open.library.simple_ui.xml.ui.temp.normal.normal

import android.view.View
import androidx.annotation.LayoutRes
import kr.open.library.simple_ui.xml.ui.temp.viewholder.normal.BaseRcvViewHolder

open class SimpleSingleAdapter<ITEM : Any>(
    @LayoutRes layoutRes: Int,
    diffUtilEnabled: Boolean = false,
    private val onBindItem: (view: View, item: ITEM, position: Int) -> Unit,
) : BaseSingleAdapter<ITEM, BaseRcvViewHolder>(layoutRes, diffUtilEnabled) {
    override fun onBind(view: View, item: ITEM, position: Int) {
        onBindItem(view, item, position)
    }

    override fun getCreateViewHolder(view: View): BaseRcvViewHolder = BaseRcvViewHolder(view)
}
