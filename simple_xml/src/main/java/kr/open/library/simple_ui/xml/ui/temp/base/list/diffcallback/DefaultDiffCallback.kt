package kr.open.library.simple_ui.xml.ui.temp.base.list.diffcallback

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil

class DefaultDiffCallback<ITEM : Any> : DiffUtil.ItemCallback<ITEM>() {
    override fun areItemsTheSame(oldItem: ITEM, newItem: ITEM): Boolean = oldItem == newItem

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: ITEM, newItem: ITEM): Boolean = oldItem == newItem
}
