package kr.open.library.simpleui_xml.logx

import kr.open.library.simple_ui.core.viewmodel.BaseViewModelEvent

class LogxActivityVm : BaseViewModelEvent<LogxActivityVmEvent>() {
    fun onClickBasicLogging() = sendEventVm(LogxActivityVmEvent.OnClickBasicLogging)

    fun onClickJsonLogging() = sendEventVm(LogxActivityVmEvent.OnClickJsonLogging)

    fun onClickParentLogging() = sendEventVm(LogxActivityVmEvent.OnClickParentLogging)

    fun onClickThreadLogging() = sendEventVm(LogxActivityVmEvent.OnClickThreadLogging)

    fun onClickFileLogging() = sendEventVm(LogxActivityVmEvent.OnClickFileLogging)

    fun onClickStorageConfig() = sendEventVm(LogxActivityVmEvent.OnClickStorageConfig)

    fun onClickTagBlockList() = sendEventVm(LogxActivityVmEvent.OnClickTagBlockList)

    fun onClickSkipPackages() = sendEventVm(LogxActivityVmEvent.OnClickSkipPackages)

    fun onClickSaveDirectory() = sendEventVm(LogxActivityVmEvent.OnClickSaveDirectory)
}
