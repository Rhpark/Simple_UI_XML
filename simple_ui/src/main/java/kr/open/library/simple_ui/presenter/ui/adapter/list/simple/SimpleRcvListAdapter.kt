package kr.open.library.simple_ui.presenter.ui.adapter.list.simple

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import kr.open.library.simple_ui.presenter.ui.adapter.list.diffutil.RcvListDiffUtilCallBack
import kr.open.library.simple_ui.presenter.ui.adapter.list.base.BaseRcvListAdapter
import kr.open.library.simple_ui.presenter.ui.adapter.viewholder.BaseRcvViewHolder

/**
 * A simple implementation of RecyclerView adapter without databinding support
 *
 * @param ITEM Type of data items in the list
 * @property layoutRes Layout resource ID for item views
 * @property onBind Function to bind data to the ViewHolder
 */
public open class SimpleRcvListAdapter<ITEM : Any>(
    @LayoutRes private val layoutRes: Int,
    listDiffUtil: RcvListDiffUtilCallBack<ITEM>,
    private val onBind: (BaseRcvViewHolder, ITEM, position: Int) -> Unit
) : BaseRcvListAdapter<ITEM, BaseRcvViewHolder>(listDiffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseRcvViewHolder =
        BaseRcvViewHolder(layoutRes, parent)

    override fun onBindViewHolder(holder: BaseRcvViewHolder, position: Int, item: ITEM) {
        onBind(holder, item, position)
    }
}
