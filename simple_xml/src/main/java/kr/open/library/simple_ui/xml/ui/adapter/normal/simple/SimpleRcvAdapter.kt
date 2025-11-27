package kr.open.library.simple_ui.xml.ui.adapter.normal.simple

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import kr.open.library.simple_ui.xml.ui.adapter.normal.base.BaseRcvAdapter
import kr.open.library.simple_ui.xml.ui.adapter.viewholder.BaseRcvViewHolder

/**
 * A simple implementation of RecyclerView adapter without databinding support
 *
 * @param ITEM Type of data items in the list
 * @property layoutRes Layout resource ID for item views
 * @property onBind Function to bind data to the ViewHolder
 */
public open class SimpleRcvAdapter<ITEM : Any>(
    @LayoutRes private val layoutRes: Int,
    private val onBind: (BaseRcvViewHolder, ITEM, position: Int) -> Unit
) : BaseRcvAdapter<ITEM, BaseRcvViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseRcvViewHolder =
        BaseRcvViewHolder(layoutRes, parent)

    override fun onBindViewHolder(holder: BaseRcvViewHolder, position: Int, item: ITEM) {
        onBind(holder, item, position)
    }
}
