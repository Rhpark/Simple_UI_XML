package kr.open.library.simpleui_xml.logx

sealed interface LogxActivityVmEvent {
    data object OnClickBasicLogging : LogxActivityVmEvent

    data object OnClickJsonLogging : LogxActivityVmEvent

    data object OnClickParentTracking : LogxActivityVmEvent

    data object OnClickThreadTracking : LogxActivityVmEvent

    data object OnClickFileLogging : LogxActivityVmEvent

    data object OnClickStorageConfig : LogxActivityVmEvent

    data object OnClickAdvancedConfig : LogxActivityVmEvent

    data object OnClickLogFiltering : LogxActivityVmEvent
}
