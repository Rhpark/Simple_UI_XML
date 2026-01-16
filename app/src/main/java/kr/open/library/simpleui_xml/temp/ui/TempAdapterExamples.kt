package kr.open.library.simpleui_xml.temp.ui

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
import kr.open.library.simpleui_xml.temp.data.TempItemFactory

object TempAdapterExamples {
    val all: List<TempAdapterExample> = listOf(
        TempAdapterExample(
            title = "RV Normal Single",
            createAdapter = { TempSingleNormalAdapter() },
            createItems = { TempItemFactory.createSingleItems() },
        ),
        TempAdapterExample(
            title = "RV Simple Normal Single",
            createAdapter = { TempSimpleSingleNormalAdapter() },
            createItems = { TempItemFactory.createSingleItems() },
        ),
        TempAdapterExample(
            title = "RV DataBinding Single",
            createAdapter = { TempSingleDataBindingAdapter() },
            createItems = { TempItemFactory.createSingleItems() },
        ),
        TempAdapterExample(
            title = "RV Simple DataBinding Single",
            createAdapter = { TempSimpleSingleDataBindingAdapter() },
            createItems = { TempItemFactory.createSingleItems() },
        ),
        TempAdapterExample(
            title = "RV ViewBinding Single",
            createAdapter = { TempSingleViewBindingAdapter() },
            createItems = { TempItemFactory.createSingleItems() },
        ),
        TempAdapterExample(
            title = "RV Simple ViewBinding Single",
            createAdapter = { TempSimpleSingleViewBindingAdapter() },
            createItems = { TempItemFactory.createSingleItems() },
        ),
        TempAdapterExample(
            title = "RV Normal Multi",
            createAdapter = { TempMultiNormalAdapter() },
            createItems = { TempItemFactory.createMultiItems() },
        ),
        TempAdapterExample(
            title = "RV DataBinding Multi",
            createAdapter = { TempMultiDataBindingAdapter() },
            createItems = { TempItemFactory.createMultiItems() },
        ),
        TempAdapterExample(
            title = "RV ViewBinding Multi",
            createAdapter = { TempMultiViewBindingAdapter() },
            createItems = { TempItemFactory.createMultiItems() },
        ),
        TempAdapterExample(
            title = "List Normal Single",
            createAdapter = { TempSingleNormalListAdapter() },
            createItems = { TempItemFactory.createSingleItems() },
        ),
        TempAdapterExample(
            title = "List Simple Normal Single",
            createAdapter = { TempSimpleSingleNormalListAdapter() },
            createItems = { TempItemFactory.createSingleItems() },
        ),
        TempAdapterExample(
            title = "List DataBinding Single",
            createAdapter = { TempSingleDataBindingListAdapter() },
            createItems = { TempItemFactory.createSingleItems() },
        ),
        TempAdapterExample(
            title = "List Simple DataBinding Single",
            createAdapter = { TempSimpleSingleDataBindingListAdapter() },
            createItems = { TempItemFactory.createSingleItems() },
        ),
        TempAdapterExample(
            title = "List ViewBinding Single",
            createAdapter = { TempSingleViewBindingListAdapter() },
            createItems = { TempItemFactory.createSingleItems() },
        ),
        TempAdapterExample(
            title = "List Simple ViewBinding Single",
            createAdapter = { TempSimpleSingleViewBindingListAdapter() },
            createItems = { TempItemFactory.createSingleItems() },
        ),
        TempAdapterExample(
            title = "List Normal Multi",
            createAdapter = { TempMultiNormalListAdapter() },
            createItems = { TempItemFactory.createMultiItems() },
        ),
        TempAdapterExample(
            title = "List DataBinding Multi",
            createAdapter = { TempMultiDataBindingListAdapter() },
            createItems = { TempItemFactory.createMultiItems() },
        ),
        TempAdapterExample(
            title = "List ViewBinding Multi",
            createAdapter = { TempMultiViewBindingListAdapter() },
            createItems = { TempItemFactory.createMultiItems() },
        ),
    )
}
