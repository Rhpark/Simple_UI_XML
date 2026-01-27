package kr.open.library.simpleui_xml.logx

sealed interface LogxActivityVmEvent {
    data object OnClickBasicLogging : LogxActivityVmEvent

    data object OnClickJsonLogging : LogxActivityVmEvent

    data object OnClickParentLogging : LogxActivityVmEvent

    data object OnClickThreadLogging : LogxActivityVmEvent

    data object OnClickFileLogging : LogxActivityVmEvent

    data object OnClickStorageConfig : LogxActivityVmEvent

    data object OnClickTagBlockList : LogxActivityVmEvent

    data object OnClickSkipPackages : LogxActivityVmEvent

    data object OnClickSaveDirectory : LogxActivityVmEvent
}
