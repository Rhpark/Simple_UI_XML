package kr.open.library.simpleui_xml.temp.ui

import androidx.recyclerview.widget.DiffUtil
import kr.open.library.simple_ui.xml.ui.temp.base.list.diffcallback.DefaultDiffCallback
import kr.open.library.simpleui_xml.temp.adapter.list.databind.TempMultiDataBindingListAdapter
import kr.open.library.simpleui_xml.temp.adapter.list.databind.TempSimpleSingleDataBindingListAdapter
import kr.open.library.simpleui_xml.temp.adapter.list.databind.TempSingleDataBindingListAdapter
import kr.open.library.simpleui_xml.temp.adapter.list.normal.TempMultiNormalListAdapter
import kr.open.library.simpleui_xml.temp.adapter.list.normal.TempSimpleSingleNormalListAdapter
import kr.open.library.simpleui_xml.temp.adapter.list.normal.TempSingleNormalListAdapter
import kr.open.library.simpleui_xml.temp.adapter.list.viewbind.TempMultiViewBindingListAdapter
import kr.open.library.simpleui_xml.temp.adapter.list.viewbind.TempSimpleSingleViewBindingListAdapter
import kr.open.library.simpleui_xml.temp.adapter.list.viewbind.TempSingleViewBindingListAdapter
import kr.open.library.simpleui_xml.temp.adapter.rcv.databind.TempMultiDataBindingAdapter
import kr.open.library.simpleui_xml.temp.adapter.rcv.databind.TempSimpleSingleDataBindingAdapter
import kr.open.library.simpleui_xml.temp.adapter.rcv.databind.TempSingleDataBindingAdapter
import kr.open.library.simpleui_xml.temp.adapter.rcv.normal.TempMultiNormalAdapter
import kr.open.library.simpleui_xml.temp.adapter.rcv.normal.TempSimpleSingleNormalAdapter
import kr.open.library.simpleui_xml.temp.adapter.rcv.normal.TempSingleNormalAdapter
import kr.open.library.simpleui_xml.temp.adapter.rcv.viewbind.TempMultiViewBindingAdapter
import kr.open.library.simpleui_xml.temp.adapter.rcv.viewbind.TempSimpleSingleViewBindingAdapter
import kr.open.library.simpleui_xml.temp.adapter.rcv.viewbind.TempSingleViewBindingAdapter
import kr.open.library.simpleui_xml.temp.data.TempItem

/**
 * Registry of adapter example entries.<br><br>
 * 어댑터 예제 항목을 보관하는 레지스트리입니다.<br>
 */
object TempAdapterExamples {
    /**
     * Ordered list of all adapter examples.<br><br>
     * 모든 어댑터 예제의 순서 있는 리스트입니다.<br>
     */
    val all: List<TempAdapterExample> = listOf(
        TempAdapterExample(
            title = "RV Normal Single",
            kind = TempAdapterKind.RV,
            itemMode = TempItemMode.SINGLE,
            createAdapter = { deps: TempAdapterDependencies ->
                TempSingleNormalAdapter(
                    diffUtilEnabled = deps.diffUtilEnabled,
                    diffExecutor = deps.diffExecutor,
                )
            },
        ),
        TempAdapterExample(
            title = "RV Simple Normal Single",
            kind = TempAdapterKind.RV,
            itemMode = TempItemMode.SINGLE,
            createAdapter = { deps: TempAdapterDependencies ->
                TempSimpleSingleNormalAdapter(
                    diffUtilEnabled = deps.diffUtilEnabled,
                    diffExecutor = deps.diffExecutor,
                )
            },
        ),
        TempAdapterExample(
            title = "RV DataBinding Single",
            kind = TempAdapterKind.RV,
            itemMode = TempItemMode.SINGLE,
            createAdapter = { deps: TempAdapterDependencies ->
                TempSingleDataBindingAdapter(
                    diffUtilEnabled = deps.diffUtilEnabled,
                    diffExecutor = deps.diffExecutor,
                )
            },
        ),
        TempAdapterExample(
            title = "RV Simple DataBinding Single",
            kind = TempAdapterKind.RV,
            itemMode = TempItemMode.SINGLE,
            createAdapter = { deps: TempAdapterDependencies ->
                TempSimpleSingleDataBindingAdapter(
                    diffUtilEnabled = deps.diffUtilEnabled,
                    diffExecutor = deps.diffExecutor,
                )
            },
        ),
        TempAdapterExample(
            title = "RV ViewBinding Single",
            kind = TempAdapterKind.RV,
            itemMode = TempItemMode.SINGLE,
            createAdapter = { deps: TempAdapterDependencies ->
                TempSingleViewBindingAdapter(
                    diffUtilEnabled = deps.diffUtilEnabled,
                    diffExecutor = deps.diffExecutor,
                )
            },
        ),
        TempAdapterExample(
            title = "RV Simple ViewBinding Single",
            kind = TempAdapterKind.RV,
            itemMode = TempItemMode.SINGLE,
            createAdapter = { deps: TempAdapterDependencies ->
                TempSimpleSingleViewBindingAdapter(
                    diffUtilEnabled = deps.diffUtilEnabled,
                    diffExecutor = deps.diffExecutor,
                )
            },
        ),
        TempAdapterExample(
            title = "RV Normal Multi",
            kind = TempAdapterKind.RV,
            itemMode = TempItemMode.MULTI,
            createAdapter = { deps: TempAdapterDependencies ->
                TempMultiNormalAdapter(
                    diffUtilEnabled = deps.diffUtilEnabled,
                    diffExecutor = deps.diffExecutor,
                )
            },
        ),
        TempAdapterExample(
            title = "RV DataBinding Multi",
            kind = TempAdapterKind.RV,
            itemMode = TempItemMode.MULTI,
            createAdapter = { deps: TempAdapterDependencies ->
                TempMultiDataBindingAdapter(
                    diffUtilEnabled = deps.diffUtilEnabled,
                    diffExecutor = deps.diffExecutor,
                )
            },
        ),
        TempAdapterExample(
            title = "RV ViewBinding Multi",
            kind = TempAdapterKind.RV,
            itemMode = TempItemMode.MULTI,
            createAdapter = { deps: TempAdapterDependencies ->
                TempMultiViewBindingAdapter(
                    diffUtilEnabled = deps.diffUtilEnabled,
                    diffExecutor = deps.diffExecutor,
                )
            },
        ),
        TempAdapterExample(
            title = "List Normal Single",
            kind = TempAdapterKind.LIST,
            itemMode = TempItemMode.SINGLE,
            createAdapter = { deps: TempAdapterDependencies ->
                TempSingleNormalListAdapter(
                    diffCallback = resolveDiffCallback(deps),
                    diffExecutor = deps.diffExecutor,
                )
            },
        ),
        TempAdapterExample(
            title = "List Simple Normal Single",
            kind = TempAdapterKind.LIST,
            itemMode = TempItemMode.SINGLE,
            createAdapter = { deps: TempAdapterDependencies ->
                TempSimpleSingleNormalListAdapter(
                    diffCallback = resolveDiffCallback(deps),
                    diffExecutor = deps.diffExecutor,
                )
            },
        ),
        TempAdapterExample(
            title = "List DataBinding Single",
            kind = TempAdapterKind.LIST,
            itemMode = TempItemMode.SINGLE,
            createAdapter = { deps: TempAdapterDependencies ->
                TempSingleDataBindingListAdapter(
                    diffCallback = resolveDiffCallback(deps),
                    diffExecutor = deps.diffExecutor,
                )
            },
        ),
        TempAdapterExample(
            title = "List Simple DataBinding Single",
            kind = TempAdapterKind.LIST,
            itemMode = TempItemMode.SINGLE,
            createAdapter = { deps: TempAdapterDependencies ->
                TempSimpleSingleDataBindingListAdapter(
                    diffCallback = resolveDiffCallback(deps),
                    diffExecutor = deps.diffExecutor,
                )
            },
        ),
        TempAdapterExample(
            title = "List ViewBinding Single",
            kind = TempAdapterKind.LIST,
            itemMode = TempItemMode.SINGLE,
            createAdapter = { deps: TempAdapterDependencies ->
                TempSingleViewBindingListAdapter(
                    diffCallback = resolveDiffCallback(deps),
                    diffExecutor = deps.diffExecutor,
                )
            },
        ),
        TempAdapterExample(
            title = "List Simple ViewBinding Single",
            kind = TempAdapterKind.LIST,
            itemMode = TempItemMode.SINGLE,
            createAdapter = { deps: TempAdapterDependencies ->
                TempSimpleSingleViewBindingListAdapter(
                    diffCallback = resolveDiffCallback(deps),
                    diffExecutor = deps.diffExecutor,
                )
            },
        ),
        TempAdapterExample(
            title = "List Normal Multi",
            kind = TempAdapterKind.LIST,
            itemMode = TempItemMode.MULTI,
            createAdapter = { deps: TempAdapterDependencies ->
                TempMultiNormalListAdapter(
                    diffCallback = resolveDiffCallback(deps),
                    diffExecutor = deps.diffExecutor,
                )
            },
        ),
        TempAdapterExample(
            title = "List DataBinding Multi",
            kind = TempAdapterKind.LIST,
            itemMode = TempItemMode.MULTI,
            createAdapter = { deps: TempAdapterDependencies ->
                TempMultiDataBindingListAdapter(
                    diffCallback = resolveDiffCallback(deps),
                    diffExecutor = deps.diffExecutor,
                )
            },
        ),
        TempAdapterExample(
            title = "List ViewBinding Multi",
            kind = TempAdapterKind.LIST,
            itemMode = TempItemMode.MULTI,
            createAdapter = { deps: TempAdapterDependencies ->
                TempMultiViewBindingListAdapter(
                    diffCallback = resolveDiffCallback(deps),
                    diffExecutor = deps.diffExecutor,
                )
            },
        ),
    )

    /**
     * Resolves the DiffUtil callback for ListAdapter examples.<br><br>
     * ListAdapter 예제에 사용할 DiffUtil 콜백을 해석합니다.<br>
     */
    private fun resolveDiffCallback(deps: TempAdapterDependencies): DiffUtil.ItemCallback<TempItem> {
        // Custom callback when provided; otherwise default callback.<br><br>커스텀 콜백이 없으면 기본 콜백을 사용합니다.<br>
        return deps.diffCallback ?: DefaultDiffCallback()
    }
}
