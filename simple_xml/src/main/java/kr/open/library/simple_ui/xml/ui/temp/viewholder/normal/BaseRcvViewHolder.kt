package kr.open.library.simple_ui.xml.ui.temp.viewholder.normal

import android.view.View
import kr.open.library.simple_ui.xml.ui.temp.viewholder.root.RootViewHolder

/**
 * ViewHolder with cached findViewById helpers.<br><br>
 * findViewById 캐시 헬퍼를 포함한 ViewHolder입니다.<br>
 */
open class BaseRcvViewHolder(
    /**
     * Root item view for this holder.<br><br>
     * 해당 홀더의 루트 아이템 뷰입니다.<br>
     */
    itemView: View,
) : RootViewHolder(itemView) {
    /**
     * Cache for findViewById results to reduce lookup cost.<br><br>
     * findViewById 결과를 캐싱하여 조회 비용을 줄입니다.<br>
     */
    @PublishedApi
    internal val viewCache = mutableMapOf<Int, View>()

    /**
     * Finds a view by ID with type casting and caching.<br><br>
     * ID로 뷰를 찾고 타입 캐스팅 및 캐싱을 적용합니다.<br>
     */
    inline fun <reified T : View> findViewById(id: Int): T {
        // Cached view lookup by ID.<br><br>ID 기준 캐시된 뷰 조회입니다.<br>
        val cached = viewCache[id]
        if (cached != null && cached is T) return cached

        // Actual view lookup on the itemView.<br><br>itemView에서 실제 뷰를 조회합니다.<br>
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
     * null 안전을 포함해 ID로 뷰를 조회하고 캐싱합니다.<br>
     */
    inline fun <reified T : View> findViewByIdOrNull(id: Int): T? {
        // Cached view lookup by ID.<br><br>ID 기준 캐시된 뷰 조회입니다.<br>
        val cached = viewCache[id]
        if (cached != null) {
            return if (cached is T) cached else null
        }

        // Actual view lookup on the itemView.<br><br>itemView에서 실제 뷰를 조회합니다.<br>
        val view = itemView.findViewById<View>(id) ?: return null

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
    fun clearViewCache() {
        viewCache.clear()
    }
}
