package kr.open.library.simple_ui.xml.ui.temp.base.internal

import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Helper for binding click/long-click listeners once per ViewHolder.<br><br>
 * ViewHolder 생성 시점에 클릭/롱클릭 리스너를 1회 바인딩하는 헬퍼입니다.<br>
 */
internal object AdapterClickBinder {
    /**
     * Attaches click listeners and resolves items from the current binding position.<br><br>
     * 현재 바인딩 어댑터 위치로 아이템을 조회하여 클릭 리스너를 연결합니다.<br>
     */
    fun <ITEM : Any> bind(
        holder: RecyclerView.ViewHolder,
        getItemOrNull: (Int) -> ITEM?,
        getClickListener: () -> ((Int, ITEM, View) -> Unit)?,
        getLongClickListener: () -> ((Int, ITEM, View) -> Unit)?,
    ) {
        holder.itemView.setOnClickListener { view ->
            // Current binding adapter position at click time.<br><br>클릭 시점의 바인딩 어댑터 위치입니다.<br>
            val adapterPosition = holder.bindingAdapterPosition
            if (adapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
            // Item resolved from current position at click time.<br><br>클릭 시점 위치로 조회한 아이템입니다.<br>
            val item = getItemOrNull(adapterPosition) ?: return@setOnClickListener
            getClickListener()?.invoke(adapterPosition, item, view)
        }

        holder.itemView.setOnLongClickListener { view ->
            // Optional long-click listener; return false when absent.<br><br>롱클릭 리스너가 없으면 false를 반환합니다.<br>
            val listener = getLongClickListener() ?: return@setOnLongClickListener false
            // Current binding adapter position at long-click time.<br><br>롱클릭 시점의 바인딩 어댑터 위치입니다.<br>
            val adapterPosition = holder.bindingAdapterPosition
            if (adapterPosition == RecyclerView.NO_POSITION) return@setOnLongClickListener false
            // Item resolved from current position at long-click time.<br><br>롱클릭 시점 위치로 조회한 아이템입니다.<br>
            val item = getItemOrNull(adapterPosition) ?: return@setOnLongClickListener false
            listener.invoke(adapterPosition, item, view)
            true
        }
    }
}

