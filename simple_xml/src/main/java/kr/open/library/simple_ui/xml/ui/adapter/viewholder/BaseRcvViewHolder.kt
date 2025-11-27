package kr.open.library.simple_ui.xml.ui.adapter.viewholder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView

/**
 * Base ViewHolder for RecyclerView without ViewDataBinding
 *
 * @param xmlRes Layout resource ID
 * @param parent Parent ViewGroup
 * @param attachToRoot Whether to attach to root (default: false)
 */
public open class BaseRcvViewHolder(
    @LayoutRes xmlRes: Int,
    parent: ViewGroup,
    attachToRoot: Boolean = false
) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(xmlRes, parent, attachToRoot)
) {

    @PublishedApi
    internal val viewCache = mutableMapOf<Int, View>()

    /**
     * Find view by ID with type casting and caching
     * 타입 캐스팅 및 캐싱이 적용된 findViewById
     * @param id View ID
     * @return Found view with type T
     */
    public inline fun <reified T : View> findViewById(id: Int): T {
        @Suppress("UNCHECKED_CAST")
        val cached = viewCache[id]
        if (cached != null && cached is T) return cached

        val view = itemView.findViewById<View>(id)
            ?: throw IllegalArgumentException("View with id $id not found in layout")

        if (view !is T) {
            throw ClassCastException("View with id $id is ${view::class.java.simpleName}, not ${T::class.java.simpleName}")
        }

        viewCache[id] = view
        return view
    }

    /**
     * Find view by ID with null safety and caching
     * null 안전성 및 캐싱이 적용된 findViewById
     * @param id View ID
     * @return Found view with type T or null
     */
    public inline fun <reified T : View> findViewByIdOrNull(id: Int): T? {
        // Check cache first
        val cached = viewCache[id]
        if (cached != null) {
            return if (cached is T) cached else null
        }

        // Find view
        val view = itemView.findViewById<View>(id) ?: return null

        // Check type
        return if (view is T) {
            viewCache[id] = view
            view
        } else {
            null
        }
    }

    /**
     * Clear view cache manually if needed
     * 필요시 뷰 캐시를 수동으로 정리
     */
    public fun clearViewCache() {
        viewCache.clear()
    }

    /**
     * Verification of the existence of an item
     * for listener(ex OnItemClickListener...)
     * @return true if position is valid
     */
    protected fun isValidPosition(): Boolean = (adapterPosition > RecyclerView.NO_POSITION)

    /**
     * Get current adapter position safely
     * @return adapter position or -1 if invalid
     */
    protected fun getAdapterPositionSafe(): Int = if (isValidPosition()) adapterPosition else RecyclerView.NO_POSITION


}
