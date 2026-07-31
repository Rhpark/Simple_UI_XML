package kr.open.library.simpleui.consumer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kr.open.library.simple_ui.system_manager.core.info.battery.BatteryStateEvent
import kr.open.library.simple_ui.system_manager.core.info.battery.BatteryStateInfo

public fun startBatteryMonitor(
    batteryStateInfo: BatteryStateInfo,
    coroutineScope: CoroutineScope,
): Boolean = batteryStateInfo.registerStart(coroutineScope)

public fun observeBatteryEvents(batteryStateInfo: BatteryStateInfo): SharedFlow<BatteryStateEvent> =
    batteryStateInfo.sfUpdate
