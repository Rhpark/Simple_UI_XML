package kr.open.library.simple_ui.xml.ui.adapter.normal.root

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kr.open.library.simple_ui.core.extensions.trycatch.safeCatch
import kr.open.library.simple_ui.xml.ui.adapter.common.AdapterCommonClickData
import kr.open.library.simple_ui.xml.ui.adapter.common.AdapterCommonDataLogic
import kr.open.library.simple_ui.xml.ui.adapter.common.imp.AdapterClickable
import kr.open.library.simple_ui.xml.ui.adapter.common.imp.AdapterReadApi
import kr.open.library.simple_ui.xml.ui.adapter.common.thread.assertAdapterMainThread
import kr.open.library.simple_ui.xml.ui.adapter.normal.base.BaseRcvAdapter
import kr.open.library.simple_ui.xml.ui.adapter.normal.headerfooter.HeaderFooterRcvAdapter
import kr.open.library.simple_ui.xml.ui.adapter.viewholder.BaseRcvViewHolder

/**
 * Common infrastructure base for all RecyclerView adapters in this library.<br><br>
 * 이 라이브러리의 모든 RecyclerView adapter가 공통으로 사용하는 기반 클래스입니다.<br>
 * [BaseRcvAdapter]와 [HeaderFooterRcvAdapter]가 이 클래스를 상속합니다.<br>
 *
 * @param ITEM adapter가 다루는 아이템 타입입니다.<br><br>
 * @param VH adapter가 다루는 ViewHolder 타입입니다.<br>
 */
public abstract class RootRcvAdapter<ITEM : Any, VH : RecyclerView.ViewHolder> :
    RecyclerView.Adapter<VH>(),
    AdapterReadApi<ITEM>,
    AdapterClickable<ITEM, VH> {
    internal val commonDataLogic: AdapterCommonDataLogic<ITEM> = AdapterCommonDataLogic<ITEM>()
    internal val clickData: AdapterCommonClickData<ITEM, VH> = AdapterCommonClickData<ITEM, VH>()

    /**
     * ViewHolder를 생성하고 클릭 리스너를 1회만 연결합니다.<br><br>
     */
    public final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val holder = createViewHolderInternal(parent, viewType)
        bindClickListeners(holder)
        return holder
    }

    /**
     * parent와 viewType에 맞는 ViewHolder를 생성합니다.<br><br>
     */
    protected abstract fun createViewHolderInternal(parent: ViewGroup, viewType: Int): VH

    /**
     * payload 기반 부분 바인딩 훅입니다.<br><br>
     * 기본 구현은 전체 바인딩으로 위임합니다.<br>
     */
    protected open fun onBindViewHolder(
        holder: VH,
        item: ITEM,
        position: Int,
        payloads: List<Any>,
    ) {
        onBindViewHolder(holder, item, position)
    }

    /**
     * 하위 클래스가 구현해야 하는 전체 바인딩 계약입니다.<br><br>
     */
    protected abstract fun onBindViewHolder(holder: VH, item: ITEM, position: Int)

    /**
     * 클릭/롱클릭 리스너를 1회만 연결합니다.<br><br>
     */
    protected open fun bindClickListeners(holder: VH) {
        clickData.attachClickListeners(
            holder = holder,
            positionMapper = { adapterPosition -> adapterPosition },
            itemProvider = { contentPosition -> getItemOrNull(contentPosition) },
        )
        clickData.attachLongClickListeners(
            holder = holder,
            positionMapper = { adapterPosition -> adapterPosition },
            itemProvider = { contentPosition -> getItemOrNull(contentPosition) },
        )
    }

    /**
     * 메인 스레드를 검증한 뒤 [block]을 실행합니다.<br><br>
     */
    internal inline fun <T> runOnMainThread(apiName: String, block: () -> T): T {
        assertAdapterMainThread(apiName)
        return block()
    }

    /**
     * 현재 호출이 메인 스레드인지 검증합니다.<br><br>
     */
    protected fun assertMainThread(apiName: String) {
        assertAdapterMainThread(apiName)
    }

    /**
     * 전달된 결과 값으로 결과 콜백을 안전하게 호출합니다.<br><br>
     */
    protected fun <RESULT> runResultCallback(
        result: RESULT,
        onResult: ((RESULT) -> Unit)?,
    ) {
        safeCatch { onResult?.invoke(result) }
    }

    /**
     * content 섹션 viewType 훅입니다.<br><br>
     */
    protected open fun getContentItemViewType(position: Int, item: ITEM): Int =
        super.getItemViewType(position)

    /**
     * holder가 재활용될 때 캐시된 child view를 정리합니다.<br><br>
     */
    public override fun onViewRecycled(holder: VH) {
        super.onViewRecycled(holder)
        if (holder is BaseRcvViewHolder) {
            holder.clearViewCache()
        }
    }
}
