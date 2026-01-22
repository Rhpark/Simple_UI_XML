package kr.open.library.simpleui_xml.temp.adapter.listadapter.databinding

import androidx.recyclerview.widget.DiffUtil
import kr.open.library.simple_ui.xml.ui.temp.base.list.diffcallback.DefaultDiffCallback
import kr.open.library.simple_ui.xml.ui.temp.list.binding.databind.BaseSingleDataBindingListAdapter
import kr.open.library.simple_ui.xml.ui.temp.viewholder.binding.BaseDataBindingViewHolder
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.databinding.ItemTempSingleDatabindingBinding
import kr.open.library.simpleui_xml.temp.data.TempItem
import kr.open.library.simpleui_xml.temp.util.TempItemDataBindingBinder
import java.util.concurrent.Executor

/**
 * ListAdapter DataBinding single-type adapter example.<br><br>
 * ListAdapter DataBinding 단일 타입 어댑터 예제입니다.<br>
 */
class TempSingleDataBindingListAdapter(
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
) : BaseSingleDataBindingListAdapter<TempItem, ItemTempSingleDatabindingBinding>(
        R.layout.item_temp_single_databinding,
        diffCallback,
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
     * 제공된 DataBinding 홀더에 아이템 데이터를 바인딩합니다.<br>
     */
    override fun onBind(holder: BaseDataBindingViewHolder<ItemTempSingleDatabindingBinding>, item: TempItem, position: Int) {
        TempItemDataBindingBinder.bind(holder.binding, item, position)
    }
}
