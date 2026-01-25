package kr.open.library.simpleui_xml.temp.multi.databinding.adapter.recyclerview

import androidx.databinding.ViewDataBinding
import kr.open.library.simple_ui.xml.ui.temp.normal.binding.databind.BaseMultiDataBindingAdapter
import kr.open.library.simple_ui.xml.ui.temp.viewholder.binding.BaseDataBindingViewHolder
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.databinding.ItemTempMultiPrimaryDatabindingBinding
import kr.open.library.simpleui_xml.databinding.ItemTempMultiSecondaryDatabindingBinding
import kr.open.library.simpleui_xml.temp.data.TempItem
import kr.open.library.simpleui_xml.temp.multi.data.TempItemType
import java.util.concurrent.Executor

/**
 * RecyclerView DataBinding multi-type adapter example.<br><br>
 * RecyclerView DataBinding 다중 타입 어댑터 예제입니다.<br>
 */
class TempMultiDataBindingAdapter(
    /**
     * Enables DiffUtil for normal adapter updates.<br><br>
     * 일반 어댑터 업데이트에서 DiffUtil 사용 여부입니다.<br>
     */
    diffUtilEnabled: Boolean = false,
    /**
     * Executor used for background diff computation.<br><br>
     * 백그라운드 diff 계산에 사용하는 Executor입니다.<br>
     */
    diffExecutor: Executor? = null,
) : BaseMultiDataBindingAdapter<TempItem, ViewDataBinding>(
        layoutResProvider = { item: TempItem, _: Int ->
            // Layout resolved by item type.<br><br>아이템 타입 기준으로 레이아웃을 결정합니다.<br>
            when (item.type) {
                TempItemType.PRIMARY -> R.layout.item_temp_multi_primary_databinding
                TempItemType.SECONDARY -> R.layout.item_temp_multi_secondary_databinding
            }
        },
        diffUtilEnabled = diffUtilEnabled,
        diffExecutor = diffExecutor,
    ) {
    /**
     * Creates the default DataBinding ViewHolder.<br><br>
     * 기본 DataBinding ViewHolder를 생성합니다.<br>
     */
    override fun getCreateViewHolder(binding: ViewDataBinding): BaseDataBindingViewHolder<ViewDataBinding> =
        BaseDataBindingViewHolder(binding)

    /**
     * Binds item data to the provided DataBinding holder with view type info.<br><br>
     * 뷰 타입 정보를 포함해 DataBinding 홀더에 아이템 데이터를 바인딩합니다.<br>
     */
    override fun onBind(
        holder: BaseDataBindingViewHolder<ViewDataBinding>,
        item: TempItem,
        position: Int,
        viewType: Int,
    ) {
        when (val binding = holder.binding) {
            is ItemTempMultiPrimaryDatabindingBinding -> {
                binding.item = item
                binding.position = position
                binding.executePendingBindings()
            }
            is ItemTempMultiSecondaryDatabindingBinding -> {
                binding.item = item
                binding.position = position
                binding.executePendingBindings()
            }
        }
    }
}