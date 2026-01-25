package kr.open.library.simpleui_xml.temp.viewbinding.adapter.recyclerview

import kr.open.library.simple_ui.xml.ui.temp.normal.binding.viewbind.BaseSingleViewBindingAdapter
import kr.open.library.simple_ui.xml.ui.temp.viewholder.binding.BaseViewBindingViewHolder
import kr.open.library.simpleui_xml.databinding.ItemTempSingleBinding
import kr.open.library.simpleui_xml.temp.data.TempItem
import java.util.concurrent.Executor

/**
 * RecyclerView ViewBinding single-type adapter example.<br><br>
 * RecyclerView ViewBinding 단일 타입 어댑터 예제입니다.<br>
 */
class TempSingleViewBindingAdapter(
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
) : BaseSingleViewBindingAdapter<TempItem, ItemTempSingleBinding>(
        ItemTempSingleBinding::inflate,
        diffUtilEnabled,
        diffExecutor,
    ) {
    /**
     * Creates the default ViewBinding ViewHolder.<br><br>
     * 기본 ViewBinding ViewHolder를 생성합니다.<br>
     */
    override fun getCreateViewHolder(binding: ItemTempSingleBinding): BaseViewBindingViewHolder<ItemTempSingleBinding> =
        BaseViewBindingViewHolder(binding)

    /**
     * Binds item data to the provided ViewBinding holder.<br><br>
     * ViewBinding 홀더에 아이템 데이터를 바인딩합니다.<br>
     */
    override fun onBind(holder: BaseViewBindingViewHolder<ItemTempSingleBinding>, item: TempItem, position: Int) {
        val binding = holder.binding
        binding.tvTitle.text = item.title
        binding.tvDescription.text = item.description
        binding.tvType.text = "Id: ${item.id} | Type: ${item.type} | Pos: $position"
    }
}