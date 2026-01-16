package kr.open.library.simpleui_xml.temp.ui

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kr.open.library.simple_ui.xml.ui.temp.base.list.RootListAdapterCore
import kr.open.library.simple_ui.xml.ui.temp.base.normal.RootRcvAdapterCore
import kr.open.library.simpleui_xml.R
import kr.open.library.simpleui_xml.temp.data.TempItem

class TempAdapterExampleActivity : AppCompatActivity() {
    private lateinit var tvExampleTitle: TextView
    private lateinit var rcvTempExample: RecyclerView
    private var currentIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_temp_adapter_example)

        tvExampleTitle = findViewById(R.id.tvExampleTitle)
        rcvTempExample = findViewById(R.id.rcvTempExample)
        val btnPrevExample: Button = findViewById(R.id.btnPrevExample)
        val btnNextExample: Button = findViewById(R.id.btnNextExample)

        rcvTempExample.layoutManager = LinearLayoutManager(this)

        btnPrevExample.setOnClickListener { showExample(currentIndex - 1) }
        btnNextExample.setOnClickListener { showExample(currentIndex + 1) }

        showExample(currentIndex)
    }

    private fun showExample(index: Int) {
        if (TempAdapterExamples.all.isEmpty()) return

        currentIndex =
            when {
                index < 0 -> TempAdapterExamples.all.lastIndex
                index > TempAdapterExamples.all.lastIndex -> 0
                else -> index
            }

        val example = TempAdapterExamples.all[currentIndex]
        tvExampleTitle.text = example.title

        val adapter = example.createAdapter()
        rcvTempExample.adapter = adapter

        val items = example.createItems()
        submitItems(adapter, items)
    }

    @Suppress("UNCHECKED_CAST")
    private fun submitItems(adapter: RecyclerView.Adapter<*>, items: List<TempItem>) {
        when (adapter) {
            is RootRcvAdapterCore<*, *> -> {
                (adapter as RootRcvAdapterCore<TempItem, *>).setItems(items)
            }
            is RootListAdapterCore<*, *> -> {
                (adapter as RootListAdapterCore<TempItem, *>).setItems(items)
            }
        }
    }
}
