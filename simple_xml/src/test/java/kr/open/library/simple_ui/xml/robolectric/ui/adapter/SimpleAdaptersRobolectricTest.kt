package kr.open.library.simple_ui.xml.robolectric.ui.adapter

import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.databinding.ViewDataBinding
import androidx.test.core.app.ApplicationProvider
import androidx.viewbinding.ViewBinding
import kr.open.library.simple_ui.xml.ui.adapter.list.base.diffutil.RcvListDiffUtilCallBack
import kr.open.library.simple_ui.xml.ui.adapter.list.simple.SimpleRcvDataBindingListAdapter
import kr.open.library.simple_ui.xml.ui.adapter.list.simple.SimpleRcvListAdapter
import kr.open.library.simple_ui.xml.ui.adapter.list.simple.SimpleRcvViewBindingListAdapter
import kr.open.library.simple_ui.xml.ui.adapter.normal.base.BaseRcvAdapter
import kr.open.library.simple_ui.xml.ui.adapter.normal.simple.SimpleRcvDataBindingAdapter
import kr.open.library.simple_ui.xml.ui.adapter.normal.simple.SimpleRcvAdapter
import kr.open.library.simple_ui.xml.ui.adapter.normal.simple.SimpleRcvViewBindingAdapter
import kr.open.library.simple_ui.xml.ui.adapter.viewholder.BaseRcvDataBindingViewHolder
import kr.open.library.simple_ui.xml.ui.adapter.viewholder.BaseRcvViewBindingViewHolder
import kr.open.library.simple_ui.xml.ui.adapter.viewholder.BaseRcvViewHolder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows

@RunWith(RobolectricTestRunner::class)
class SimpleAdaptersRobolectricTest {
    private lateinit var parent: FrameLayout

    // sealed interface — Header/Content/Footer를 단일 ITEM 타입으로 표현
    private sealed interface SealedItem {
        data class Header(val title: String) : SealedItem
        data class Content(val value: String) : SealedItem
        data class Footer(val count: Int) : SealedItem
    }

    @Before
    fun setUp() {
        parent = FrameLayout(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun simpleRcvListAdapter_bindsUsingLambda() {
        var capturedItem: String? = null
        var capturedPos = -1
        val adapter =
            SimpleRcvListAdapter(
                android.R.layout.simple_list_item_1,
                RcvListDiffUtilCallBack({ old, new -> old == new }, { old, new -> old == new }),
            ) { _, item: String, position ->
                capturedItem = item
                capturedPos = position
            }

        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.submitList(listOf("alpha"))
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        adapter.onBindViewHolder(holder, 0)

        assertEquals("alpha", capturedItem)
        assertEquals(0, capturedPos)
    }

    @Test
    fun simpleBindingRcvListAdapter_createsHolder() {
        val adapter =
            SimpleRcvDataBindingListAdapter<String, ViewDataBinding>(
                android.R.layout.simple_list_item_1,
                RcvListDiffUtilCallBack({ old, new -> old == new }, { old, new -> old == new }),
            ) { _, _, _ -> }

        val holder = adapter.onCreateViewHolder(parent, 0)
        assertNotNull(holder)
    }

    @Test
    fun simpleBindingRcvListAdapter_bindsUsingLambda() {
        var capturedItem: String? = null
        val adapter =
            SimpleRcvDataBindingListAdapter<String, ViewDataBinding>(
                android.R.layout.simple_list_item_1,
                RcvListDiffUtilCallBack({ old, new -> old == new }, { old, new -> old == new }),
            ) { _, item: String, _ ->
                capturedItem = item
            }

        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.submitList(listOf("beta"))
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        adapter.onBindViewHolder(holder, 0)

        assertEquals("beta", capturedItem)
    }

    @Test
    fun simpleRcvAdapter_invokesOnBindLambda() {
        var capturedItem: String? = null
        val adapter =
            SimpleRcvAdapter<String>(android.R.layout.simple_list_item_1) { _, item: String, _ ->
                capturedItem = item
            }
        val holder = adapter.onCreateViewHolder(parent, 0)
        invokeSimpleAdapterBind(adapter, holder, "gamma")

        assertEquals("gamma", capturedItem)
    }

    @Test
    fun simpleBindingRcvAdapter_invokesOnBindLambda() {
        var capturedItem: String? = null
        val adapter =
            SimpleRcvDataBindingAdapter<String, ViewDataBinding>(
                android.R.layout.simple_list_item_1,
            ) { _, item: String, _ ->
                capturedItem = item
            }
        val holder = adapter.onCreateViewHolder(parent, 0)
        invokeBindingAdapterBind(adapter, holder, "delta")

        assertEquals("delta", capturedItem)
    }

    @Test
    fun simpleViewBindingRcvListAdapter_createsHolder() {
        val adapter =
            SimpleRcvViewBindingListAdapter<String, TestSimpleViewBinding>(
                inflate = { inflater, group, attach ->
                    TestSimpleViewBinding.inflate(inflater, group, attach)
                },
                listDiffUtil = RcvListDiffUtilCallBack({ old, new -> old == new }, { old, new -> old == new }),
            ) { _, _, _ -> }

        val holder = adapter.onCreateViewHolder(parent, 0)
        assertNotNull(holder)
    }

    @Test
    fun simpleViewBindingRcvListAdapter_bindsUsingLambda() {
        var capturedItem: String? = null
        val adapter =
            SimpleRcvViewBindingListAdapter<String, TestSimpleViewBinding>(
                inflate = { inflater, group, attach ->
                    TestSimpleViewBinding.inflate(inflater, group, attach)
                },
                listDiffUtil = RcvListDiffUtilCallBack({ old, new -> old == new }, { old, new -> old == new }),
            ) { _, item: String, _ ->
                capturedItem = item
            }

        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.submitList(listOf("zeta"))
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        adapter.onBindViewHolder(holder, 0)

        assertEquals("zeta", capturedItem)
    }

    @Test
    fun simpleViewBindingRcvAdapter_invokesOnBindLambda() {
        var capturedItem: String? = null
        val adapter =
            SimpleRcvViewBindingAdapter<String, TestSimpleViewBinding>(
                inflate = { inflater, parent, attachToParent ->
                    TestSimpleViewBinding.inflate(inflater, parent, attachToParent)
                },
            ) { _, item: String, _ ->
                capturedItem = item
            }
        val holder = adapter.onCreateViewHolder(parent, 0)
        invokeViewBindingAdapterBind(adapter, holder, "epsilon")

        assertEquals("epsilon", capturedItem)
    }

    // sealed interface 기반 Header/Content/Footer — BaseRcvAdapter 단독으로 처리
    @Test
    fun sealedInterfaceAdapter_supportsHeaderContentFooterInSingleList() {
        val adapter = object : BaseRcvAdapter<SealedItem, RecyclerView.ViewHolder>() {
            override fun getContentItemViewType(position: Int, item: SealedItem): Int = when (item) {
                is SealedItem.Header -> 0
                is SealedItem.Content -> 1
                is SealedItem.Footer -> 2
            }

            override fun createViewHolderInternal(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
                object : RecyclerView.ViewHolder(View(parent.context)) {}

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, item: SealedItem, position: Int) {}
        }

        adapter.setItems(
            listOf(
                SealedItem.Header("헤더"),
                SealedItem.Content("C1"),
                SealedItem.Content("C2"),
                SealedItem.Content("C3"),
                SealedItem.Footer(3),
            ),
        )

        assertEquals(5, adapter.itemCount)
        assertEquals(SealedItem.Header("헤더"), adapter.getItemOrNull(0))
        assertEquals(SealedItem.Content("C1"), adapter.getItemOrNull(1))
        assertEquals(SealedItem.Content("C3"), adapter.getItemOrNull(3))
        assertEquals(SealedItem.Footer(3), adapter.getItemOrNull(4))
    }

    @Test
    fun sealedInterfaceAdapter_viewTypeDispatchedByItemType() {
        val adapter = object : BaseRcvAdapter<SealedItem, RecyclerView.ViewHolder>() {
            override fun getContentItemViewType(position: Int, item: SealedItem): Int = when (item) {
                is SealedItem.Header -> 0
                is SealedItem.Content -> 1
                is SealedItem.Footer -> 2
            }

            override fun createViewHolderInternal(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
                object : RecyclerView.ViewHolder(View(parent.context)) {}

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, item: SealedItem, position: Int) {}
        }

        adapter.setItems(
            listOf(
                SealedItem.Header("H"),
                SealedItem.Content("C"),
                SealedItem.Footer(1),
            ),
        )

        assertEquals(0, adapter.getItemViewType(0))
        assertEquals(1, adapter.getItemViewType(1))
        assertEquals(2, adapter.getItemViewType(2))
    }

    @Test
    fun sealedInterfaceAdapter_removeContentItemOnly() {
        val adapter = object : BaseRcvAdapter<SealedItem, RecyclerView.ViewHolder>() {
            override fun createViewHolderInternal(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
                object : RecyclerView.ViewHolder(View(parent.context)) {}

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, item: SealedItem, position: Int) {}
        }

        val items = listOf(
            SealedItem.Header("H"),
            SealedItem.Content("C1"),
            SealedItem.Content("C2"),
            SealedItem.Footer(2),
        )
        adapter.setItems(items)
        adapter.removeAt(1) // C1 제거

        assertEquals(3, adapter.itemCount)
        assertEquals(SealedItem.Content("C2"), adapter.getItemOrNull(1))
    }

    private fun invokeSimpleAdapterBind(
        adapter: SimpleRcvAdapter<String>,
        holder: BaseRcvViewHolder,
        item: String,
    ) {
        val method =
            SimpleRcvAdapter::class.java.getDeclaredMethod(
                "onBindViewHolder",
                BaseRcvViewHolder::class.java,
                Any::class.java,
                Int::class.javaPrimitiveType,
            )
        method.isAccessible = true
        method.invoke(adapter, holder, item, 0)
    }

    private fun invokeBindingAdapterBind(
        adapter: SimpleRcvDataBindingAdapter<String, ViewDataBinding>,
        holder: BaseRcvDataBindingViewHolder<ViewDataBinding>,
        item: String,
    ) {
        val method =
            SimpleRcvDataBindingAdapter::class.java.getDeclaredMethod(
                "onBindViewHolder",
                BaseRcvDataBindingViewHolder::class.java,
                Any::class.java,
                Int::class.javaPrimitiveType,
            )
        method.isAccessible = true
        method.invoke(adapter, holder, item, 0)
    }

    private fun invokeViewBindingAdapterBind(
        adapter: SimpleRcvViewBindingAdapter<String, TestSimpleViewBinding>,
        holder: BaseRcvViewBindingViewHolder<TestSimpleViewBinding>,
        item: String,
    ) {
        val method =
            SimpleRcvViewBindingAdapter::class.java.getDeclaredMethod(
                "onBindViewHolder",
                BaseRcvViewBindingViewHolder::class.java,
                Any::class.java,
                Int::class.javaPrimitiveType,
            )
        method.isAccessible = true
        method.invoke(adapter, holder, item, 0)
    }

    private class TestSimpleViewBinding private constructor(
        private val rootLayout: FrameLayout,
    ) : ViewBinding {
        override fun getRoot(): FrameLayout = rootLayout

        companion object {
            fun inflate(
                inflater: LayoutInflater,
                parent: ViewGroup,
                attachToParent: Boolean,
            ): TestSimpleViewBinding {
                val root = FrameLayout(parent.context)
                if (attachToParent) {
                    parent.addView(root)
                }
                return TestSimpleViewBinding(root)
            }
        }
    }
}
