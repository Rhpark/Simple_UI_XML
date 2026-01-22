package kr.open.library.simpleui_xml.temp.ui

import androidx.recyclerview.widget.RecyclerView
import kr.open.library.simple_ui.xml.ui.temp.base.list.diffcallback.DefaultDiffCallback
import kr.open.library.simpleui_xml.temp.adapter.listadapter.databinding.TempMultiDataBindingListAdapter
import kr.open.library.simpleui_xml.temp.adapter.listadapter.databinding.TempSimpleSingleDataBindingListAdapter
import kr.open.library.simpleui_xml.temp.adapter.listadapter.databinding.TempSingleDataBindingListAdapter
import kr.open.library.simpleui_xml.temp.adapter.listadapter.normal.TempMultiNormalListAdapter
import kr.open.library.simpleui_xml.temp.adapter.listadapter.normal.TempSimpleSingleNormalListAdapter
import kr.open.library.simpleui_xml.temp.adapter.listadapter.normal.TempSingleNormalListAdapter
import kr.open.library.simpleui_xml.temp.adapter.listadapter.viewbinding.TempMultiViewBindingListAdapter
import kr.open.library.simpleui_xml.temp.adapter.listadapter.viewbinding.TempSimpleSingleViewBindingListAdapter
import kr.open.library.simpleui_xml.temp.adapter.listadapter.viewbinding.TempSingleViewBindingListAdapter
import kr.open.library.simpleui_xml.temp.adapter.recyclerview.databinding.TempMultiDataBindingAdapter
import kr.open.library.simpleui_xml.temp.adapter.recyclerview.databinding.TempSimpleSingleDataBindingAdapter
import kr.open.library.simpleui_xml.temp.adapter.recyclerview.databinding.TempSingleDataBindingAdapter
import kr.open.library.simpleui_xml.temp.adapter.recyclerview.normal.TempMultiNormalAdapter
import kr.open.library.simpleui_xml.temp.adapter.recyclerview.normal.TempSimpleSingleNormalAdapter
import kr.open.library.simpleui_xml.temp.adapter.recyclerview.normal.TempSingleNormalAdapter
import kr.open.library.simpleui_xml.temp.adapter.recyclerview.viewbinding.TempMultiViewBindingAdapter
import kr.open.library.simpleui_xml.temp.adapter.recyclerview.viewbinding.TempSimpleSingleViewBindingAdapter
import kr.open.library.simpleui_xml.temp.adapter.recyclerview.viewbinding.TempSingleViewBindingAdapter
import kr.open.library.simpleui_xml.temp.data.TempItem
import kr.open.library.simpleui_xml.temp.util.TempItemDiffCallback

/**
 * Factory for creating temp adapter instances.<br><br>
 * Temp 어댑터 인스턴스 생성 팩토리입니다.<br>
 */
object TempAdapterFactory {
    /**
     * Creates an adapter based on the provided configuration.<br><br>
     * 제공된 설정 기준으로 어댑터를 생성합니다.<br>
     */
    fun createAdapter(config: TempAdapterConfig): RecyclerView.Adapter<*> = when (config.adapterKind) {
        TempAdapterKind.RV -> createRcvAdapter(config)
        TempAdapterKind.LIST -> createListAdapter(config)
    }

    /**
     * Creates RecyclerView.Adapter based on config.<br><br>
     * 설정 기준으로 RecyclerView.Adapter를 생성합니다.<br>
     */
    private fun createRcvAdapter(config: TempAdapterConfig): RecyclerView.Adapter<*> = when (config.itemMode) {
        TempItemMode.SINGLE -> createRcvSingleAdapter(config)
        TempItemMode.MULTI -> createRcvMultiAdapter(config)
    }

    /**
     * Creates ListAdapter based on config.<br><br>
     * 설정 기준으로 ListAdapter를 생성합니다.<br>
     */
    private fun createListAdapter(config: TempAdapterConfig): RecyclerView.Adapter<*> = when (config.itemMode) {
        TempItemMode.SINGLE -> createListSingleAdapter(config)
        TempItemMode.MULTI -> createListMultiAdapter(config)
    }

    /**
     * Creates single-type RecyclerView.Adapter.<br><br>
     * 단일 타입 RecyclerView.Adapter를 생성합니다.<br>
     */
    private fun createRcvSingleAdapter(config: TempAdapterConfig): RecyclerView.Adapter<*> = when (config.bindingType) {
        TempBindingType.NORMAL -> {
            if (config.useSimpleAdapter) {
                TempSimpleSingleNormalAdapter(
                    diffUtilEnabled = config.enableDiffUtil,
                    diffExecutor = config.diffExecutor,
                )
            } else {
                TempSingleNormalAdapter(
                    diffUtilEnabled = config.enableDiffUtil,
                    diffExecutor = config.diffExecutor,
                )
            }
        }
        TempBindingType.DATA_BINDING -> {
            if (config.useSimpleAdapter) {
                TempSimpleSingleDataBindingAdapter(
                    diffUtilEnabled = config.enableDiffUtil,
                    diffExecutor = config.diffExecutor,
                )
            } else {
                TempSingleDataBindingAdapter(
                    diffUtilEnabled = config.enableDiffUtil,
                    diffExecutor = config.diffExecutor,
                )
            }
        }
        TempBindingType.VIEW_BINDING -> {
            if (config.useSimpleAdapter) {
                TempSimpleSingleViewBindingAdapter(
                    diffUtilEnabled = config.enableDiffUtil,
                    diffExecutor = config.diffExecutor,
                )
            } else {
                TempSingleViewBindingAdapter(
                    diffUtilEnabled = config.enableDiffUtil,
                    diffExecutor = config.diffExecutor,
                )
            }
        }
    }

    /**
     * Creates multi-type RecyclerView.Adapter.<br><br>
     * 다중 타입 RecyclerView.Adapter를 생성합니다.<br>
     */
    private fun createRcvMultiAdapter(config: TempAdapterConfig): RecyclerView.Adapter<*> = when (config.bindingType) {
        TempBindingType.NORMAL -> TempMultiNormalAdapter(
            diffUtilEnabled = config.enableDiffUtil,
            diffExecutor = config.diffExecutor,
        )
        TempBindingType.DATA_BINDING -> TempMultiDataBindingAdapter(
            diffUtilEnabled = config.enableDiffUtil,
            diffExecutor = config.diffExecutor,
        )
        TempBindingType.VIEW_BINDING -> TempMultiViewBindingAdapter(
            diffUtilEnabled = config.enableDiffUtil,
            diffExecutor = config.diffExecutor,
        )
    }

    /**
     * Creates single-type ListAdapter.<br><br>
     * 단일 타입 ListAdapter를 생성합니다.<br>
     */
    private fun createListSingleAdapter(config: TempAdapterConfig): RecyclerView.Adapter<*> {
        val diffCallback = if (config.useCustomDiffCallback) {
            TempItemDiffCallback()
        } else {
            DefaultDiffCallback<TempItem>()
        }

        return when (config.bindingType) {
            TempBindingType.NORMAL -> {
                if (config.useSimpleAdapter) {
                    TempSimpleSingleNormalListAdapter(
                        diffCallback = diffCallback,
                        diffExecutor = config.diffExecutor,
                    )
                } else {
                    TempSingleNormalListAdapter(
                        diffCallback = diffCallback,
                        diffExecutor = config.diffExecutor,
                    )
                }
            }
            TempBindingType.DATA_BINDING -> {
                if (config.useSimpleAdapter) {
                    TempSimpleSingleDataBindingListAdapter(
                        diffCallback = diffCallback,
                        diffExecutor = config.diffExecutor,
                    )
                } else {
                    TempSingleDataBindingListAdapter(
                        diffCallback = diffCallback,
                        diffExecutor = config.diffExecutor,
                    )
                }
            }
            TempBindingType.VIEW_BINDING -> {
                if (config.useSimpleAdapter) {
                    TempSimpleSingleViewBindingListAdapter(
                        diffCallback = diffCallback,
                        diffExecutor = config.diffExecutor,
                    )
                } else {
                    TempSingleViewBindingListAdapter(
                        diffCallback = diffCallback,
                        diffExecutor = config.diffExecutor,
                    )
                }
            }
        }
    }

    /**
     * Creates multi-type ListAdapter.<br><br>
     * 다중 타입 ListAdapter를 생성합니다.<br>
     */
    private fun createListMultiAdapter(config: TempAdapterConfig): RecyclerView.Adapter<*> {
        val diffCallback = if (config.useCustomDiffCallback) {
            TempItemDiffCallback()
        } else {
            DefaultDiffCallback<TempItem>()
        }

        return when (config.bindingType) {
            TempBindingType.NORMAL -> TempMultiNormalListAdapter(
                diffCallback = diffCallback,
                diffExecutor = config.diffExecutor,
            )
            TempBindingType.DATA_BINDING -> TempMultiDataBindingListAdapter(
                diffCallback = diffCallback,
                diffExecutor = config.diffExecutor,
            )
            TempBindingType.VIEW_BINDING -> TempMultiViewBindingListAdapter(
                diffCallback = diffCallback,
                diffExecutor = config.diffExecutor,
            )
        }
    }
}
