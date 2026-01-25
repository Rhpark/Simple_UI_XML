package kr.open.library.simpleui_xml.temp.databinding.adapter.listadapter

import androidx.recyclerview.widget.DiffUtil
import kr.open.library.simple_ui.xml.ui.temp.base.list.diffcallback.DefaultDiffCallback
import kr.open.library.simple_ui.xml.ui.temp.list.binding.databind.SimpleSingleDataBindingListAdapter
import kr.open.library.simple_ui.xml.ui.temp.viewholder.binding.BaseBindingViewHolder
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.databinding.ItemTempSingleDatabindingBinding
import kr.open.library.simpleui_xml.temp.data.TempItem
import java.util.concurrent.Executor

/**
 * ListAdapter DataBinding simple single-type adapter example.<br><br>
 * ListAdapter DataBinding 간단 단일 타입 어댑터 예제입니다.<br>
 */
class TempSimpleSingleDataBindingListAdapter(
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
) : SimpleSingleDataBindingListAdapter<TempItem, ItemTempSingleDatabindingBinding>(
        layoutRes = R.layout.item_temp_single_databinding,
        diffCallback = diffCallback,
        diffExecutor = diffExecutor,
        onBindItem = { holder: BaseBindingViewHolder<ItemTempSingleDatabindingBinding>, item: TempItem, position: Int ->
            val binding = holder.binding
            binding.item = item
            binding.position = position
            binding.executePendingBindings()
        },
    )