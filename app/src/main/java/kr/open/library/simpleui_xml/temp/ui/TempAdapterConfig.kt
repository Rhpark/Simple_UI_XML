package kr.open.library.simpleui_xml.temp.ui

import java.util.concurrent.Executor

/**
 * Configuration for temp adapter examples.<br><br>
 * Temp 어댑터 예제 설정입니다.<br>
 */
data class TempAdapterConfig(
    /**
     * Adapter kind (RV or LIST).<br><br>
     * 어댑터 종류 (RV 또는 LIST)입니다.<br>
     */
    val adapterKind: TempAdapterKind,
    /**
     * Item mode (SINGLE or MULTI).<br><br>
     * 아이템 모드 (SINGLE 또는 MULTI)입니다.<br>
     */
    val itemMode: TempItemMode,
    /**
     * Binding type (NORMAL, DATA_BINDING, or VIEW_BINDING).<br><br>
     * 바인딩 타입 (NORMAL, DATA_BINDING, VIEW_BINDING)입니다.<br>
     */
    val bindingType: TempBindingType,
    /**
     * Whether to use Simple adapter variant.<br><br>
     * Simple 어댑터 변형 사용 여부입니다.<br>
     */
    val useSimpleAdapter: Boolean,
    /**
     * Whether to enable DiffUtil for RecyclerView.Adapter.<br><br>
     * RecyclerView.Adapter에서 DiffUtil 활성화 여부입니다.<br>
     */
    val enableDiffUtil: Boolean,
    /**
     * Whether to use custom DiffCallback for ListAdapter.<br><br>
     * ListAdapter에서 커스텀 DiffCallback 사용 여부입니다.<br>
     */
    val useCustomDiffCallback: Boolean,
    /**
     * Executor for background diff computation.<br><br>
     * 백그라운드 diff 계산에 사용하는 Executor입니다.<br>
     */
    val diffExecutor: Executor?,
) {
    companion object {
        /**
         * Creates default config for given adapter kind and item mode.<br><br>
         * 주어진 어댑터 종류와 아이템 모드에 대한 기본 설정을 생성합니다.<br>
         */
        fun createDefault(
            adapterKind: TempAdapterKind,
            itemMode: TempItemMode,
        ): TempAdapterConfig = TempAdapterConfig(
            adapterKind = adapterKind,
            itemMode = itemMode,
            bindingType = TempBindingType.NORMAL,
            useSimpleAdapter = false,
            enableDiffUtil = false,
            useCustomDiffCallback = false,
            diffExecutor = null,
        )
    }
}
