package kr.open.library.simpleui.consumer

import android.widget.TextView
import kr.open.library.simple_ui.xml.ui.adapter.normal.simple.SimpleRcvAdapter
import kr.open.library.simple_ui.xml.ui.components.activity.root.RootActivity

public abstract class XmlConsumerActivity : RootActivity()

public fun createXmlAdapter(): SimpleRcvAdapter<String> =
    SimpleRcvAdapter(android.R.layout.simple_list_item_1) { holder, item, _ ->
        holder.findViewById<TextView>(android.R.id.text1).text = item
    }
