package kr.open.library.simple_ui.system_manager.controller.wifi.internal

internal class WifiOperationGuard(
    private val executor: (defaultValue: Any?, block: () -> Any?) -> Any?
) {

    @Suppress("UNCHECKED_CAST")
    fun <T> run(defaultValue: T, block: () -> T): T =
        executor(defaultValue, block) as T
}
