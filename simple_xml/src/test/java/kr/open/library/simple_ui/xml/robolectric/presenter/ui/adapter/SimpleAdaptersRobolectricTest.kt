package kr.open.library.simple_ui.xml.robolectric.presenter.ui.adapter

import android.os.Looper
import android.widget.FrameLayout
import androidx.databinding.ViewDataBinding
import androidx.test.core.app.ApplicationProvider
import kr.open.library.simple_ui.xml.ui.adapter.list.diffutil.RcvListDiffUtilCallBack
import kr.open.library.simple_ui.xml.ui.adapter.list.simple.SimpleBindingRcvListAdapter
import kr.open.library.simple_ui.xml.ui.adapter.list.simple.SimpleRcvListAdapter
import kr.open.library.simple_ui.xml.ui.adapter.normal.simple.SimpleBindingRcvAdapter
import kr.open.library.simple_ui.xml.ui.adapter.normal.simple.SimpleRcvAdapter
import kr.open.library.simple_ui.xml.ui.adapter.viewholder.BaseBindingRcvViewHolder
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
            SimpleBindingRcvListAdapter<String, ViewDataBinding>(
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
            SimpleBindingRcvListAdapter<String, ViewDataBinding>(
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
            SimpleBindingRcvAdapter<String, ViewDataBinding>(android.R.layout.simple_list_item_1) { _, item: String, _ ->
                capturedItem = item
            }
        val holder = adapter.onCreateViewHolder(parent, 0)
        invokeBindingAdapterBind(adapter, holder, "delta")

        assertEquals("delta", capturedItem)
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
                Int::class.javaPrimitiveType,
                Any::class.java,
            )
        method.isAccessible = true
        method.invoke(adapter, holder, 0, item)
    }

    private fun invokeBindingAdapterBind(
        adapter: SimpleBindingRcvAdapter<String, ViewDataBinding>,
        holder: BaseBindingRcvViewHolder<ViewDataBinding>,
        item: String,
    ) {
        val method =
            SimpleBindingRcvAdapter::class.java.getDeclaredMethod(
                "onBindViewHolder",
                BaseBindingRcvViewHolder::class.java,
                Int::class.javaPrimitiveType,
                Any::class.java,
            )
        method.isAccessible = true
        method.invoke(adapter, holder, 0, item)
    }
}
