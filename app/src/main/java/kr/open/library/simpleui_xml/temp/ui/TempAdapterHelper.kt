package kr.open.library.simpleui_xml.temp.ui

import androidx.recyclerview.widget.RecyclerView
import kr.open.library.simple_ui.xml.ui.adapter.queue.QueueDebugEvent
import kr.open.library.simple_ui.xml.ui.adapter.queue.QueueEventType
import kr.open.library.simple_ui.xml.ui.adapter.queue.QueueOverflowPolicy
import kr.open.library.simple_ui.xml.ui.temp.base.OperationFailure
import kr.open.library.simple_ui.xml.ui.temp.base.OperationFailureInfo
import kr.open.library.simple_ui.xml.ui.temp.base.list.RootListAdapterCore
import kr.open.library.simple_ui.xml.ui.temp.base.normal.RootRcvAdapterCore
import kr.open.library.simpleui_xml.temp.data.TempItem

/**
 * Helper for adapter operations in temp examples.<br><br>
 * Temp 예제의 어댑터 연산 헬퍼입니다.<br>
 */
object TempAdapterHelper {
    /**
     * Default max pending queue size.<br><br>
     * 기본 최대 대기 큐 크기입니다.<br>
     */
    private const val DEFAULT_QUEUE_MAX_PENDING: Int = 100

    /**
     * Retrieves items from the adapter.<br><br>
     * 어댑터의 아이템을 조회합니다.<br>
     */
    @Suppress("UNCHECKED_CAST")
    fun getItems(adapter: RecyclerView.Adapter<*>): List<TempItem> = when (adapter) {
        is RootRcvAdapterCore<*, *> -> (adapter as RootRcvAdapterCore<TempItem, *>).getItems()
        is RootListAdapterCore<*, *> -> (adapter as RootListAdapterCore<TempItem, *>).getItems()
        else -> emptyList()
    }

    /**
     * Submits items to the adapter.<br><br>
     * 어댑터에 아이템을 제출합니다.<br>
     */
    fun submitItems(
        adapter: RecyclerView.Adapter<*>,
        items: List<TempItem>,
        actionLabel: String,
        onStatus: (String) -> Unit,
    ): Boolean = withRootAdapter(
        adapter = adapter,
        onRcv = { rcvAdapter ->
            rcvAdapter.setItems(items) { success ->
                onStatus("$actionLabel(${items.size}) -> $success")
            }
        },
        onList = { listAdapter ->
            listAdapter.setItems(items) { success ->
                onStatus("$actionLabel(${items.size}) -> $success")
            }
        },
    )

    /**
     * Adds a single item to the adapter.<br><br>
     * 단일 아이템을 어댑터에 추가합니다.<br>
     */
    fun addItem(
        adapter: RecyclerView.Adapter<*>,
        item: TempItem,
        onStatus: (String) -> Unit,
    ): Boolean = withRootAdapter(
        adapter = adapter,
        onRcv = { rcvAdapter ->
            rcvAdapter.addItem(item) { success ->
                onStatus("addItem(id=${item.id}) -> $success")
            }
        },
        onList = { listAdapter ->
            listAdapter.addItem(item) { success ->
                onStatus("addItem(id=${item.id}) -> $success")
            }
        },
    )

    /**
     * Adds a single item at the specified position.<br><br>
     * 지정한 위치에 단일 아이템을 추가합니다.<br>
     */
    fun addItemAt(
        adapter: RecyclerView.Adapter<*>,
        position: Int,
        item: TempItem,
        onStatus: (String) -> Unit,
    ): Boolean = withRootAdapter(
        adapter = adapter,
        onRcv = { rcvAdapter ->
            rcvAdapter.addItemAt(position, item) { success ->
                onStatus("addItemAt($position, id=${item.id}) -> $success")
            }
        },
        onList = { listAdapter ->
            listAdapter.addItemAt(position, item) { success ->
                onStatus("addItemAt($position, id=${item.id}) -> $success")
            }
        },
    )

    /**
     * Adds multiple items to the adapter.<br><br>
     * 여러 아이템을 어댑터에 추가합니다.<br>
     */
    fun addItems(
        adapter: RecyclerView.Adapter<*>,
        items: List<TempItem>,
        onStatus: (String) -> Unit,
    ): Boolean = withRootAdapter(
        adapter = adapter,
        onRcv = { rcvAdapter ->
            rcvAdapter.addItems(items) { success ->
                onStatus("addItems(${items.size}) -> $success")
            }
        },
        onList = { listAdapter ->
            listAdapter.addItems(items) { success ->
                onStatus("addItems(${items.size}) -> $success")
            }
        },
    )

    /**
     * Removes a single item from the adapter.<br><br>
     * 단일 아이템을 어댑터에서 제거합니다.<br>
     */
    fun removeItem(
        adapter: RecyclerView.Adapter<*>,
        item: TempItem,
        onStatus: (String) -> Unit,
    ): Boolean = withRootAdapter(
        adapter = adapter,
        onRcv = { rcvAdapter ->
            rcvAdapter.removeItem(item) { success ->
                onStatus("removeItem(id=${item.id}) -> $success")
            }
        },
        onList = { listAdapter ->
            listAdapter.removeItem(item) { success ->
                onStatus("removeItem(id=${item.id}) -> $success")
            }
        },
    )

    /**
     * Removes an item at the specified position.<br><br>
     * 지정한 위치의 아이템을 제거합니다.<br>
     */
    fun removeAt(
        adapter: RecyclerView.Adapter<*>,
        position: Int,
        onStatus: (String) -> Unit,
    ): Boolean = withRootAdapter(
        adapter = adapter,
        onRcv = { rcvAdapter ->
            rcvAdapter.removeAt(position) { success ->
                onStatus("removeAt($position) -> $success")
            }
        },
        onList = { listAdapter ->
            listAdapter.removeAt(position) { success ->
                onStatus("removeAt($position) -> $success")
            }
        },
    )

    /**
     * Moves an item within the adapter.<br><br>
     * 어댑터 내 아이템을 이동합니다.<br>
     */
    fun moveItem(
        adapter: RecyclerView.Adapter<*>,
        from: Int,
        to: Int,
        onStatus: (String) -> Unit,
    ): Boolean = withRootAdapter(
        adapter = adapter,
        onRcv = { rcvAdapter ->
            rcvAdapter.moveItem(from, to) { success ->
                onStatus("moveItem($from -> $to) -> $success")
            }
        },
        onList = { listAdapter ->
            listAdapter.moveItem(from, to) { success ->
                onStatus("moveItem($from -> $to) -> $success")
            }
        },
    )

    /**
     * Replaces an item at the specified position.<br><br>
     * 지정한 위치의 아이템을 교체합니다.<br>
     */
    fun replaceItemAt(
        adapter: RecyclerView.Adapter<*>,
        position: Int,
        item: TempItem,
        onStatus: (String) -> Unit,
    ): Boolean = withRootAdapter(
        adapter = adapter,
        onRcv = { rcvAdapter ->
            rcvAdapter.replaceItemAt(position, item) { success ->
                onStatus("replaceItemAt($position, id=${item.id}) -> $success")
            }
        },
        onList = { listAdapter ->
            listAdapter.replaceItemAt(position, item) { success ->
                onStatus("replaceItemAt($position, id=${item.id}) -> $success")
            }
        },
    )

    /**
     * Removes all items from the adapter.<br><br>
     * 어댑터의 모든 아이템을 제거합니다.<br>
     */
    fun removeAll(
        adapter: RecyclerView.Adapter<*>,
        onStatus: (String) -> Unit,
    ): Boolean = withRootAdapter(
        adapter = adapter,
        onRcv = { rcvAdapter ->
            rcvAdapter.removeAll { success ->
                onStatus("removeAll -> $success")
            }
        },
        onList = { listAdapter ->
            listAdapter.removeAll { success ->
                onStatus("removeAll -> $success")
            }
        },
    )

    /**
     * Binds click listeners to the adapter.<br><br>
     * 어댑터에 클릭 리스너를 바인딩합니다.<br>
     */
    fun bindClickListeners(
        adapter: RecyclerView.Adapter<*>,
        onStatus: (String) -> Unit,
    ): Boolean = withRootAdapter(
        adapter = adapter,
        onRcv = { rcvAdapter ->
            rcvAdapter.setOnItemClickListener { position, item, _ ->
                onStatus("click(pos=$position, id=${item.id})")
            }
            rcvAdapter.setOnItemLongClickListener { position, item, _ ->
                onStatus("longClick(pos=$position, id=${item.id})")
            }
        },
        onList = { listAdapter ->
            listAdapter.setOnItemClickListener { position, item, _ ->
                onStatus("click(pos=$position, id=${item.id})")
            }
            listAdapter.setOnItemLongClickListener { position, item, _ ->
                onStatus("longClick(pos=$position, id=${item.id})")
            }
        },
    )

    /**
     * Applies queue policy to the adapter.<br><br>
     * 어댑터에 큐 정책을 적용합니다.<br>
     */
    fun applyQueuePolicy(
        adapter: RecyclerView.Adapter<*>,
        onStatus: (String) -> Unit,
    ): Boolean = withRootAdapter(
        adapter = adapter,
        onRcv = { rcvAdapter ->
            rcvAdapter.setQueuePolicy(DEFAULT_QUEUE_MAX_PENDING, QueueOverflowPolicy.DROP_NEW)
            rcvAdapter.setOnOperationFailureListener { info ->
                onStatus("queueFail(${formatFailure(info)})")
            }
            rcvAdapter.setQueueDebugListener { event ->
                if (event.type == QueueEventType.ERROR || event.type == QueueEventType.DROPPED) {
                    onStatus(formatQueueEvent(event))
                }
            }
        },
        onList = { listAdapter ->
            listAdapter.setQueuePolicy(DEFAULT_QUEUE_MAX_PENDING, QueueOverflowPolicy.DROP_NEW)
            listAdapter.setOnOperationFailureListener { info ->
                onStatus("queueFail(${formatFailure(info)})")
            }
            listAdapter.setQueueDebugListener { event ->
                if (event.type == QueueEventType.ERROR || event.type == QueueEventType.DROPPED) {
                    onStatus(formatQueueEvent(event))
                }
            }
        },
    )

    /**
     * Executes action with RootRcvAdapterCore or RootListAdapterCore.<br><br>
     * RootRcvAdapterCore 또는 RootListAdapterCore로 동작을 실행합니다.<br>
     */
    @Suppress("UNCHECKED_CAST")
    private fun withRootAdapter(
        adapter: RecyclerView.Adapter<*>,
        onRcv: (RootRcvAdapterCore<TempItem, *>) -> Unit,
        onList: (RootListAdapterCore<TempItem, *>) -> Unit,
    ): Boolean = when (adapter) {
        is RootRcvAdapterCore<*, *> -> {
            onRcv(adapter as RootRcvAdapterCore<TempItem, *>)
            true
        }
        is RootListAdapterCore<*, *> -> {
            onList(adapter as RootListAdapterCore<TempItem, *>)
            true
        }
        else -> false
    }

    /**
     * Formats operation failure info.<br><br>
     * 연산 실패 정보를 포맷팅합니다.<br>
     */
    private fun formatFailure(info: OperationFailureInfo): String {
        val detail = when (val failure = info.failure) {
            is OperationFailure.Validation -> "validation=${failure.message}"
            is OperationFailure.Exception -> "exception=${failure.error.message ?: "unknown"}"
            is OperationFailure.Dropped -> "dropped=${failure.reason}"
        }
        return "op=${info.operationName}, $detail"
    }

    /**
     * Formats queue debug event.<br><br>
     * 큐 디버그 이벤트를 포맷팅합니다.<br>
     */
    private fun formatQueueEvent(event: QueueDebugEvent): String {
        val name = event.operationName ?: "unknown"
        val drop = event.dropReason?.let { ", drop=$it" } ?: ""
        val message = event.message?.let { ", msg=$it" } ?: ""
        return "queue:${event.type} op=$name, pending=${event.pendingSize}$drop$message"
    }
}
