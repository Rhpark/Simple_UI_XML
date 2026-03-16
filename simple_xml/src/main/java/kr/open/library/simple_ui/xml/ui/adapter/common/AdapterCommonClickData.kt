package kr.open.library.simple_ui.xml.ui.adapter.common

import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Shared click/long-click callback container for RecyclerView ViewHolder.<br><br>
 * RecyclerView ViewHolder용 클릭/롱클릭 콜백 공통 컨테이너입니다.<br>
 *
 * @param ITEM Adapter item type.<br><br>
 *             어댑터 아이템 타입입니다.<br>
 * @param VH RecyclerView ViewHolder type.<br><br>
 *           RecyclerView ViewHolder 타입입니다.<br>
 */
internal class AdapterCommonClickData<ITEM, VH : RecyclerView.ViewHolder> {
    /**
     * Item click callback that receives position, item, and clicked view.<br><br>
     * position, item, clicked view를 전달하는 아이템 클릭 콜백입니다.<br>
     */
    var onItemClickListener: ((Int, ITEM, View) -> Unit)? = null

    /**
     * Item long-click callback that receives position, item, and clicked view.<br><br>
     * position, item, clicked view를 전달하는 아이템 롱클릭 콜백입니다.<br>
     */
    var onItemLongClickListener: ((Int, ITEM, View) -> Unit)? = null

    /**
     * Attaches click listener once per ViewHolder creation and resolves item at click time.<br><br>
     * ViewHolder 생성 시 클릭 리스너를 1회 연결하고 아이템은 클릭 시점에 조회합니다.<br>
     *
     * @param holder Target ViewHolder.<br><br>
     *               대상 ViewHolder입니다.<br>
     * @param positionMapper Mapper that converts adapter position to callback position.<br><br>
     *                       adapter position을 콜백 position으로 변환하는 매퍼입니다.<br>
     * @param itemProvider Provider that returns item from mapped callback position.<br><br>
     *                    변환된 콜백 position 기준으로 아이템을 조회하는 공급자입니다.<br>
     */
    fun attachClickListeners(holder: VH, positionMapper: (Int) -> Int?, itemProvider: (Int) -> ITEM?) {
        holder.itemView.setOnClickListener { view ->
            val listener = onItemClickListener ?: return@setOnClickListener
            val adapterPosition = holder.bindingAdapterPosition
            if (adapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
            val callbackPosition = positionMapper(adapterPosition) ?: return@setOnClickListener
            val item = itemProvider(callbackPosition) ?: return@setOnClickListener
            listener.invoke(callbackPosition, item, view)
        }
    }

    /**
     * Attaches long-click listener once per ViewHolder creation and resolves item at long-click time.<br><br>
     * ViewHolder 생성 시 롱클릭 리스너를 1회 연결하고 아이템은 롱클릭 시점에 조회합니다.<br>
     *
     * @param holder Target ViewHolder.<br><br>
     *               대상 ViewHolder입니다.<br>
     * @param positionMapper Mapper that converts adapter position to callback position.<br><br>
     *                       adapter position을 콜백 position으로 변환하는 매퍼입니다.<br>
     * @param itemProvider Provider that returns item from mapped callback position.<br><br>
     *                    변환된 콜백 position 기준으로 아이템을 조회하는 공급자입니다.<br>
     */
    fun attachLongClickListeners(holder: VH, positionMapper: (Int) -> Int?, itemProvider: (Int) -> ITEM?) {
        holder.itemView.setOnLongClickListener { view ->
            val listener = onItemLongClickListener ?: return@setOnLongClickListener false
            val adapterPosition = holder.bindingAdapterPosition
            if (adapterPosition == RecyclerView.NO_POSITION) return@setOnLongClickListener false
            val callbackPosition = positionMapper(adapterPosition) ?: return@setOnLongClickListener false
            val item = itemProvider(callbackPosition) ?: return@setOnLongClickListener false
            listener.invoke(callbackPosition, item, view)
            true
        }
    }
}
