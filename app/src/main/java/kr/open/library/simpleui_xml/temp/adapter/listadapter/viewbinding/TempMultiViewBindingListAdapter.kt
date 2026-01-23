package kr.open.library.simpleui_xml.temp.adapter.listadapter.viewbinding

import androidx.recyclerview.widget.DiffUtil
import androidx.viewbinding.ViewBinding
import kr.open.library.simple_ui.xml.ui.temp.base.list.diffcallback.DefaultDiffCallback
import kr.open.library.simple_ui.xml.ui.temp.list.binding.viewbind.BaseMultiViewBindingListAdapter
import kr.open.library.simple_ui.xml.ui.temp.viewholder.binding.BaseViewBindingViewHolder
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.databinding.ItemTempMultiPrimaryBinding
import kr.open.library.simpleui_xml.databinding.ItemTempMultiSecondaryBinding
import kr.open.library.simpleui_xml.temp.data.TempItem
import kr.open.library.simpleui_xml.temp.data.TempItemType
import kr.open.library.simpleui_xml.temp.util.TempItemViewBindingBinder
import java.util.concurrent.Executor

/**
 * ListAdapter ViewBinding multi-type adapter example.<br><br>
 * ListAdapter ViewBinding 다중 타입 어댑터 예제입니다.<br>
 */
class TempMultiViewBindingListAdapter(
    /**
     * DiffUtil callback for item comparison.<br><br>
     * 아이템 비교를 위한 DiffUtil 콜백입니다.<br>
     */
    diffCallback: DiffUtil.ItemCallback<TempItem> = DefaultDiffCallback(),
    /**
     * Executor used for background diff computation.<br><br>
     * 백그라운드 diff 계산에 사용하는 Executor입니다.<br>
     */
    diffExecutor: Executor? = null,
) : BaseMultiViewBindingListAdapter<TempItem, ViewBinding>(
        viewTypeProvider = { item: TempItem, _: Int ->
            // View type resolved by item type.<br><br>아이템 타입 기준으로 결정된 뷰 타입입니다.<br>
            when (item.type) {
                TempItemType.PRIMARY -> R.layout.item_temp_multi_primary
                TempItemType.SECONDARY -> R.layout.item_temp_multi_secondary
            }
        },
        inflateMap = mapOf(
            R.layout.item_temp_multi_primary to ItemTempMultiPrimaryBinding::inflate,
            R.layout.item_temp_multi_secondary to ItemTempMultiSecondaryBinding::inflate,
        ),
        diffCallback = diffCallback,
        diffExecutor = diffExecutor,
    ) {
    /**
     * Creates the default ViewBinding ViewHolder.<br><br>
     * 기본 ViewBinding ViewHolder를 생성합니다.<br>
     */
    override fun getCreateViewHolder(binding: ViewBinding): BaseViewBindingViewHolder<ViewBinding> =
        BaseViewBindingViewHolder(binding)

    /**
     * Binds item data to the provided ViewBinding holder with view type info.<br><br>
     * 뷰 타입 정보를 포함해 제공된 ViewBinding 홀더에 아이템 데이터를 바인딩합니다.<br>
     */
    override fun onBind(holder: BaseViewBindingViewHolder<ViewBinding>, item: TempItem, position: Int, viewType: Int) {
        when (val binding = holder.binding) {
            is ItemTempMultiPrimaryBinding -> TempItemViewBindingBinder.bind(binding, item, position)
            is ItemTempMultiSecondaryBinding -> TempItemViewBindingBinder.bind(binding, item, position)
        }
    }
}
