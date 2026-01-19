package kr.open.library.simpleui_xml.temp.adapter.recyclerview.viewbinding

import kr.open.library.simple_ui.xml.ui.temp.normal.binding.viewbind.SimpleSingleViewBindingAdapter
import kr.open.library.simple_ui.xml.ui.temp.viewholder.binding.BaseViewBindingViewHolder
import kr.open.library.simpleui_xml.databinding.ItemTempSingleBinding
import kr.open.library.simpleui_xml.temp.data.TempItem
import kr.open.library.simpleui_xml.temp.util.TempItemViewBindingBinder
import java.util.concurrent.Executor

/**
 * RecyclerView ViewBinding simple single-type adapter example.<br><br>
 * RecyclerView ViewBinding 단일 타입(Simple) 어댑터 예제입니다.<br>
 */
class TempSimpleSingleViewBindingAdapter(
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
) : SimpleSingleViewBindingAdapter<TempItem, ItemTempSingleBinding>(
        inflate = ItemTempSingleBinding::inflate,
        diffUtilEnabled = diffUtilEnabled,
        diffExecutor = diffExecutor,
        onBindItem = { holder: BaseViewBindingViewHolder<ItemTempSingleBinding>, item: TempItem, position: Int ->
            TempItemViewBindingBinder.bind(holder.binding, item, position)
        },
    )
