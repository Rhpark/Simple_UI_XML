package kr.open.library.simple_ui.xml.ui.adapter.common.imp

import android.view.View
import androidx.annotation.MainThread
import androidx.recyclerview.widget.RecyclerView

/**
 * Common click callback contract for RecyclerView adapter implementations.<br><br>
 * RecyclerView 어댑터 구현체의 공통 클릭 콜백 계약입니다.<br>
 *
 * @param ITEM Adapter item type.<br><br>
 *             어댑터 아이템 타입입니다.<br>
 * @param VH RecyclerView ViewHolder type.<br><br>
 *           RecyclerView ViewHolder 타입입니다.<br>
 */
public interface AdapterClickable<ITEM, VH : RecyclerView.ViewHolder> {
    /**
     * Sets the item click listener.<br><br>
     * 아이템 클릭 리스너를 설정합니다.<br>
     */
    @MainThread
    public fun setOnItemClickListener(listener: (Int, ITEM, View) -> Unit)

    /**
     * Sets the item long-click listener.<br><br>
     * 아이템 롱클릭 리스너를 설정합니다.<br>
     */
    @MainThread
    public fun setOnItemLongClickListener(listener: (Int, ITEM, View) -> Unit)
}
