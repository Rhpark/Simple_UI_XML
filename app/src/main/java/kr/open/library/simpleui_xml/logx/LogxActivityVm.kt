package kr.open.library.simpleui_xml.logx

import kr.open.library.simple_ui.core.viewmodel.BaseViewModelEvent

class LogxActivityVm : BaseViewModelEvent<LogxActivityVmEvent>() {

    fun onClickBasicLogging() = sendEventVm(LogxActivityVmEvent.OnClickBasicLogging)

    fun onClickJsonLogging() = sendEventVm(LogxActivityVmEvent.OnClickJsonLogging)

    fun onClickParentTracking() = sendEventVm(LogxActivityVmEvent.OnClickParentTracking)

    fun onClickThreadTracking() = sendEventVm(LogxActivityVmEvent.OnClickThreadTracking)

    fun onClickFileLogging() = sendEventVm(LogxActivityVmEvent.OnClickFileLogging)

    fun onClickStorageConfig() = sendEventVm(LogxActivityVmEvent.OnClickStorageConfig)

    fun onClickAdvancedConfig() = sendEventVm(LogxActivityVmEvent.OnClickAdvancedConfig)

    fun onClickLogFiltering() = sendEventVm(LogxActivityVmEvent.OnClickLogFiltering)
}