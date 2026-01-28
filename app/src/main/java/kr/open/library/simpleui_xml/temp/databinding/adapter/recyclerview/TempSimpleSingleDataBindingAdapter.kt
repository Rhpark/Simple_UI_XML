package kr.open.library.simpleui_xml.temp.databinding.adapter.recyclerview

import kr.open.library.simple_ui.xml.ui.temp.normal.binding.databind.SimpleSingleDataBindingAdapter
import kr.open.library.simple_ui.xml.ui.temp.viewholder.binding.BaseBindingViewHolder
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.databinding.ItemTempSingleDatabindingBinding
import kr.open.library.simpleui_xml.temp.data.TempItem
import java.util.concurrent.Executor

/**
 * RecyclerView DataBinding simple single-type adapter example.<br><br>
 * RecyclerView DataBinding 간단 단일 타입 어댑터 예제입니다.<br>
 */
class TempSimpleSingleDataBindingAdapter(
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
) : SimpleSingleDataBindingAdapter<TempItem, ItemTempSingleDatabindingBinding>(
        layoutRes = R.layout.item_temp_single_databinding,
        diffUtilEnabled = diffUtilEnabled,
        diffExecutor = diffExecutor,
        onBindItem = { holder: BaseBindingViewHolder<ItemTempSingleDatabindingBinding>, item: TempItem, position: Int ->
            val binding = holder.binding
            binding.item = item
            binding.position = position
            binding.executePendingBindings()
        },
    )
