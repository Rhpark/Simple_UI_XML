package kr.open.library.simpleui_xml.temp.multi.viewbinding.adapter.recyclerview

import androidx.viewbinding.ViewBinding
import kr.open.library.simple_ui.xml.ui.temp.normal.binding.viewbind.BaseMultiViewBindingAdapter
import kr.open.library.simple_ui.xml.ui.temp.viewholder.binding.BaseViewBindingViewHolder
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.databinding.ItemTempMultiPrimaryBinding
import kr.open.library.simpleui_xml.databinding.ItemTempMultiSecondaryBinding
import kr.open.library.simpleui_xml.temp.data.TempItem
import kr.open.library.simpleui_xml.temp.multi.data.TempItemType
import java.util.concurrent.Executor

/**
 * RecyclerView ViewBinding multi-type adapter example.<br><br>
 * RecyclerView ViewBinding 다중 타입 어댑터 예제입니다.<br>
 */
class TempMultiViewBindingAdapter(
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
) : BaseMultiViewBindingAdapter<TempItem, ViewBinding>(
        viewTypeProvider = { item: TempItem, _: Int ->
            // View type resolved by item type.<br><br>아이템 타입 기준으로 뷰 타입을 결정합니다.<br>
            when (item.type) {
                TempItemType.PRIMARY -> R.layout.item_temp_multi_primary
                TempItemType.SECONDARY -> R.layout.item_temp_multi_secondary
            }
        },
        inflateMap = mapOf(
            R.layout.item_temp_multi_primary to ItemTempMultiPrimaryBinding::inflate,
            R.layout.item_temp_multi_secondary to ItemTempMultiSecondaryBinding::inflate,
        ),
        diffUtilEnabled = diffUtilEnabled,
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
     * 뷰 타입 정보를 포함해 ViewBinding 홀더에 아이템 데이터를 바인딩합니다.<br>
     */
    override fun onBind(holder: BaseViewBindingViewHolder<ViewBinding>, item: TempItem, position: Int, viewType: Int) {
        when (val binding = holder.binding) {
            is ItemTempMultiPrimaryBinding -> {
                binding.tvTitle.text = item.title
                binding.tvDescription.text = item.description
                binding.tvType.text = "Id: ${item.id} | Type: ${item.type} | Pos: $position"
            }
            is ItemTempMultiSecondaryBinding -> {
                binding.tvTitle.text = item.title
                binding.tvDescription.text = item.description
                binding.tvType.text = "Id: ${item.id} | Type: ${item.type} | Pos: $position"
            }
        }
    }
}