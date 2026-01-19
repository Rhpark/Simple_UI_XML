package kr.open.library.simpleui_xml.temp.adapter.listadapter.viewbinding

import androidx.recyclerview.widget.DiffUtil
import kr.open.library.simple_ui.xml.ui.temp.base.list.diffcallback.DefaultDiffCallback
import kr.open.library.simple_ui.xml.ui.temp.list.binding.viewbind.BaseSingleViewBindingListAdapter
import kr.open.library.simple_ui.xml.ui.temp.viewholder.binding.BaseViewBindingViewHolder
import kr.open.library.simpleui_xml.databinding.ItemTempSingleBinding
import kr.open.library.simpleui_xml.temp.data.TempItem
import kr.open.library.simpleui_xml.temp.util.TempItemViewBindingBinder
import java.util.concurrent.Executor

/**
 * ListAdapter ViewBinding single-type adapter example.<br><br>
 * ListAdapter ViewBinding 단일 타입 어댑터 예제입니다.<br>
 */
class TempSingleViewBindingListAdapter(
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
) : BaseSingleViewBindingListAdapter<TempItem, ItemTempSingleBinding>(
        ItemTempSingleBinding::inflate,
        diffCallback,
        diffExecutor,
    ) {
    /**
     * Creates the default ViewBinding ViewHolder.<br><br>
     * 기본 ViewBinding ViewHolder를 생성합니다.<br>
     */
    override fun getCreateViewHolder(
        binding: ItemTempSingleBinding,
    ): BaseViewBindingViewHolder<ItemTempSingleBinding> = BaseViewBindingViewHolder(binding)

    /**
     * Binds item data to the provided ViewBinding holder.<br><br>
     * 제공된 ViewBinding 홀더에 아이템 데이터를 바인딩합니다.<br>
     */
    override fun onBind(holder: BaseViewBindingViewHolder<ItemTempSingleBinding>, item: TempItem, position: Int) {
        TempItemViewBindingBinder.bind(holder.binding, item, position)
    }
}
