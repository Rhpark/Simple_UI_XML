package kr.open.library.simple_ui.xml.robolectric.ui.adapter

import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.databinding.ViewDataBinding
import androidx.test.core.app.ApplicationProvider
import androidx.viewbinding.ViewBinding
import kr.open.library.simple_ui.xml.ui.adapter.list.base.diffutil.RcvListDiffUtilCallBack
import kr.open.library.simple_ui.xml.ui.adapter.list.simple.SimpleRcvDataBindingListAdapter
import kr.open.library.simple_ui.xml.ui.adapter.list.simple.SimpleRcvListAdapter
import kr.open.library.simple_ui.xml.ui.adapter.list.simple.SimpleRcvViewBindingListAdapter
import kr.open.library.simple_ui.xml.ui.adapter.normal.simple.SimpleBindingRcvAdapter
import kr.open.library.simple_ui.xml.ui.adapter.normal.simple.SimpleHeaderFooterDataBindingRcvAdapter
import kr.open.library.simple_ui.xml.ui.adapter.normal.simple.SimpleHeaderFooterRcvAdapter
import kr.open.library.simple_ui.xml.ui.adapter.normal.simple.SimpleHeaderFooterViewBindingRcvAdapter
import kr.open.library.simple_ui.xml.ui.adapter.normal.simple.SimpleRcvAdapter
import kr.open.library.simple_ui.xml.ui.adapter.normal.simple.SimpleViewBindingRcvAdapter
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
            SimpleBindingRcvAdapter<String, ViewDataBinding>(
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
            SimpleViewBindingRcvAdapter<String, TestSimpleViewBinding>(
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

    @Test
    fun simpleHeaderFooterRcvAdapter_supportsHeaderFooterSectionCount() {
        val adapter =
            SimpleHeaderFooterRcvAdapter<String>(android.R.layout.simple_list_item_1) { _, _, _ -> }

        adapter.setHeaderItems(listOf("H1", "H2"))
        adapter.setItems(listOf("C1", "C2", "C3"))
        adapter.setFooterItems(listOf("F1"))

        assertEquals(6, adapter.itemCount)
        assertEquals(listOf("C1", "C2", "C3"), adapter.getItems())
        assertEquals(listOf("H1", "H2"), adapter.getHeaderItems())
        assertEquals(listOf("F1"), adapter.getFooterItems())
        assertEquals("C1", adapter.getItem(0))
        assertEquals("C3", adapter.getItem(2))
    }

    @Test
    fun simpleHeaderFooterDataBindingRcvAdapter_supportsHeaderFooterSectionCount() {
        val adapter =
            SimpleHeaderFooterDataBindingRcvAdapter<String, ViewDataBinding>(
                android.R.layout.simple_list_item_1,
            ) { _, _, _ -> }

        adapter.setHeaderItems(listOf("H1", "H2"))
        adapter.setItems(listOf("C1", "C2", "C3"))
        adapter.setFooterItems(listOf("F1"))

        assertEquals(6, adapter.itemCount)
        assertEquals(listOf("C1", "C2", "C3"), adapter.getItems())
        assertEquals(listOf("H1", "H2"), adapter.getHeaderItems())
        assertEquals(listOf("F1"), adapter.getFooterItems())
    }

    @Test
    fun simpleHeaderFooterViewBindingRcvAdapter_supportsHeaderFooterSectionCount() {
        val adapter =
            SimpleHeaderFooterViewBindingRcvAdapter<String, TestSimpleViewBinding>(
                inflate = { inflater, parent, attachToParent ->
                    TestSimpleViewBinding.inflate(inflater, parent, attachToParent)
                },
            ) { _, _, _ -> }

        adapter.setHeaderItems(listOf("H1", "H2"))
        adapter.setItems(listOf("C1", "C2", "C3"))
        adapter.setFooterItems(listOf("F1"))

        assertEquals(6, adapter.itemCount)
        assertEquals(listOf("C1", "C2", "C3"), adapter.getItems())
        assertEquals(listOf("H1", "H2"), adapter.getHeaderItems())
        assertEquals(listOf("F1"), adapter.getFooterItems())
        assertEquals("C1", adapter.getItem(0))
        assertEquals("C3", adapter.getItem(2))
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
        adapter: SimpleBindingRcvAdapter<String, ViewDataBinding>,
        holder: BaseRcvDataBindingViewHolder<ViewDataBinding>,
        item: String,
    ) {
        val method =
            SimpleBindingRcvAdapter::class.java.getDeclaredMethod(
                "onBindViewHolder",
                BaseRcvDataBindingViewHolder::class.java,
                Any::class.java,
                Int::class.javaPrimitiveType,
            )
        method.isAccessible = true
        method.invoke(adapter, holder, item, 0)
    }

    private fun invokeViewBindingAdapterBind(
        adapter: SimpleViewBindingRcvAdapter<String, TestSimpleViewBinding>,
        holder: BaseRcvViewBindingViewHolder<TestSimpleViewBinding>,
        item: String,
    ) {
        val method =
            SimpleViewBindingRcvAdapter::class.java.getDeclaredMethod(
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
