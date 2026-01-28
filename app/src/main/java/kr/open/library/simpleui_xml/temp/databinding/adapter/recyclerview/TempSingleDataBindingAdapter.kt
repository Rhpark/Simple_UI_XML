package kr.open.library.simpleui_xml.temp.databinding.adapter.recyclerview

import kr.open.library.simple_ui.xml.ui.temp.normal.binding.databind.BaseSingleDataBindingAdapter
import kr.open.library.simple_ui.xml.ui.temp.viewholder.binding.BaseDataBindingViewHolder
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.databinding.ItemTempSingleDatabindingBinding
import kr.open.library.simpleui_xml.temp.data.TempItem
import java.util.concurrent.Executor

/**
 * RecyclerView DataBinding single-type adapter example.<br><br>
 * RecyclerView DataBinding 단일 타입 어댑터 예제입니다.<br>
 */
class TempSingleDataBindingAdapter(
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
) : BaseSingleDataBindingAdapter<TempItem, ItemTempSingleDatabindingBinding>(
        R.layout.item_temp_single_databinding,
        diffUtilEnabled,
        diffExecutor,
    ) {
    /**
     * Creates the default DataBinding ViewHolder.<br><br>
     * 기본 DataBinding ViewHolder를 생성합니다.<br>
     */
    override fun getCreateViewHolder(
        binding: ItemTempSingleDatabindingBinding,
    ): BaseDataBindingViewHolder<ItemTempSingleDatabindingBinding> = BaseDataBindingViewHolder(binding)

    /**
     * Binds item data to the provided DataBinding holder.<br><br>
     * DataBinding 홀더에 아이템 데이터를 바인딩합니다.<br>
     */
    override fun onBind(holder: BaseDataBindingViewHolder<ItemTempSingleDatabindingBinding>, item: TempItem, position: Int) {
        val binding = holder.binding
        binding.item = item
        binding.position = position
        binding.executePendingBindings()
    }
}
