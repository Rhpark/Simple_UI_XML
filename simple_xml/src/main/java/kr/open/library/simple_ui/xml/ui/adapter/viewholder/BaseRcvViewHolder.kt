package kr.open.library.simple_ui.xml.ui.adapter.viewholder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView

/**
 * Base ViewHolder for RecyclerView without ViewDataBinding.<br><br>
 * ViewDataBinding을 사용하지 않는 RecyclerView용 기본 ViewHolder입니다.<br>
 *
 * @param xmlRes Layout resource ID.<br><br>
 *               아이템 레이아웃 리소스 ID입니다.<br>
 * @param parent Parent ViewGroup.<br><br>
 *               부모 ViewGroup입니다.<br>
 * @param attachToRoot Whether to attach to root (default: false).<br><br>
 *                     루트에 붙일지 여부입니다(기본값: false).<br>
 */
public open class BaseRcvViewHolder(
    @LayoutRes xmlRes: Int,
    parent: ViewGroup,
    attachToRoot: Boolean = false,
) : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(xmlRes, parent, attachToRoot)) {
    @PublishedApi
    internal val viewCache = mutableMapOf<Int, View>()

    /**
     * Finds a view by ID with type casting and caching.<br><br>
     * 타입 캐스팅과 캐싱을 적용해 findViewById를 수행합니다.<br>
     *
     * @param id View ID.<br><br>
     *           조회할 뷰 ID입니다.<br>
     * @return Found view with type `T`.<br><br>
     *         타입 `T`로 캐스팅된 뷰를 반환합니다.<br>
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
     * Finds a view by ID with null safety and caching.<br><br>
     * null 안전성과 캐싱을 적용해 findViewById를 수행합니다.<br>
     *
     * @param id View ID.<br><br>
     *           조회할 뷰 ID입니다.<br>
     * @return Found view with type `T` or null.<br><br>
     *         타입이 맞으면 뷰를, 아니면 null을 반환합니다.<br>
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
     * Clears the internal view cache manually.<br><br>
     * 내부 뷰 캐시를 수동으로 비웁니다.<br>
     */
    public fun clearViewCache() {
        viewCache.clear()
    }

    /**
     * Verifies whether the adapter position is valid (for listeners).<br><br>
     * 어댑터 포지션이 유효한지 확인합니다(리스너용).<br>
     *
     * @return true if the position is valid.<br><br>
     *         포지션이 유효하면 true를 반환합니다.<br>
     */
    protected fun isValidPosition(): Boolean = (adapterPosition > RecyclerView.NO_POSITION)

    /**
     * Gets the current adapter position safely.<br><br>
     * 현재 어댑터 포지션을 안전하게 반환합니다.<br>
     *
     * @return Adapter position, or -1 if invalid.<br><br>
     *         유효하지 않으면 -1을 반환합니다.<br>
     */
    protected fun getAdapterPositionSafe(): Int = if (isValidPosition()) adapterPosition else RecyclerView.NO_POSITION
}
