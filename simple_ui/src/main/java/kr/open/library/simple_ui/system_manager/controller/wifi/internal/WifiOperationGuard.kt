package kr.open.library.simple_ui.system_manager.controller.wifi.internal

internal fun interface WifiOperationGuard {
    fun <T> run(defaultValue: T, block: () -> T): T
}
