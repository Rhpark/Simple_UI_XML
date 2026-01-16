package kr.open.library.simple_ui.xml.ui.temp.viewholder.normal

import android.view.View
import kr.open.library.simple_ui.xml.ui.temp.viewholder.root.RootViewHolder

open class BaseRcvViewHolder(
    itemView: View
) : RootViewHolder(itemView) {
    // viewCache - findViewById 결과 캐싱으로 성능 향상
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
    inline fun <reified T : View> findViewById(id: Int): T {
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
    inline fun <reified T : View> findViewByIdOrNull(id: Int): T? {
        val cached = viewCache[id]
        if (cached != null) {
            return if (cached is T) cached else null
        }

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
