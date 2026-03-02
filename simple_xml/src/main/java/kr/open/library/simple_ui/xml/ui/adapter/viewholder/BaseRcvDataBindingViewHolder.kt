package kr.open.library.simple_ui.xml.ui.adapter.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

/**
 * Base RecyclerView ViewHolder with DataBinding support.<br>
 * Inflates XML once and exposes a lazily bound `binding` instance.<br><br>
 * DataBinding을 지원하는 기본 RecyclerView ViewHolder입니다.<br>
 * XML을 한 번 인플레이트하고 지연 초기화된 `binding` 인스턴스를 제공합니다.<br>
 *
 * @param BINDING DataBinding type generated from item layout.<br><br>
 *                아이템 레이아웃에서 생성된 DataBinding 타입입니다.<br>
 * @param xmlRes Item layout resource ID.<br><br>
 *               아이템 레이아웃 리소스 ID입니다.<br>
 * @param parent Parent ViewGroup used for inflation context.<br><br>
 *               인플레이트 컨텍스트로 사용하는 부모 ViewGroup입니다.<br>
 * @param attachToRoot Whether to attach inflated view to parent immediately.<br><br>
 *                     인플레이트한 뷰를 부모에 즉시 부착할지 여부입니다.<br>
 */
public open class BaseRcvDataBindingViewHolder<BINDING : ViewDataBinding>(
    @LayoutRes xmlRes: Int,
    parent: ViewGroup,
    attachToRoot: Boolean = false,
) : RootViewHolder(
        LayoutInflater.from(parent.context).inflate(xmlRes, parent, attachToRoot),
    ) {
    /**
     * Lazily initialized DataBinding instance bound to `itemView`.<br><br>
     * `itemView`에 바인딩된 지연 초기화 DataBinding 인스턴스입니다.<br>
     *
     * Throws `IllegalStateException` when binding cannot be created.<br><br>
     * 바인딩 생성에 실패하면 `IllegalStateException`을 발생시킵니다.<br>
     */
    public val binding: BINDING by lazy {
        DataBindingUtil.bind<BINDING>(itemView) ?: throw IllegalStateException("DataBinding bind failed: binding is null")
    }

    /**
     * Executes pending bindings for DataBinding.<br><br>
     * DataBinding의 pending 바인딩을 실행합니다.<br>
     */
    protected fun executePendingBindings() {
        binding.executePendingBindings()
    }
}
