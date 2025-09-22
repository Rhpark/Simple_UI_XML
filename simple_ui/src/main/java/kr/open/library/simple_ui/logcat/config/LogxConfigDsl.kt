package kr.open.library.simple_ui.logcat.config

@DslMarker
annotation class LogxConfigDsl

/**
 * DSL 진입점
 */
fun logxConfig(block: LogxDslBuilder.() -> Unit): LogxConfig {
    return LogxDslBuilder().apply(block).build()
}

/**
 * 향상된 설정 빌더 (DSL 지원)
 */
@LogxConfigDsl
class LogxDslBuilder {
    var debugMode: Boolean = true
    var debugFilter: Boolean = false
    var appName: String = "RhPark"

    private var fileConfigBlock: LogxFileConfigBuilder.() -> Unit = {}
    private var logTypeConfigBlock: LogxTypeConfigBuilder.() -> Unit = {}
    private var filterConfigBlock: LogxFilterConfigBuilder.() -> Unit = {}

    fun fileConfig(block: LogxFileConfigBuilder.() -> Unit) {
        fileConfigBlock = block
    }

    fun logTypes(block: LogxTypeConfigBuilder.() -> Unit) {
        logTypeConfigBlock = block
    }

    fun filters(block: LogxFilterConfigBuilder.() -> Unit) {
        filterConfigBlock = block
    }

    internal fun build(): LogxConfig {
        val fileConfig = LogxFileConfigBuilder().apply(fileConfigBlock)
        val logTypeConfig = LogxTypeConfigBuilder().apply(logTypeConfigBlock)
        val filterConfig = LogxFilterConfigBuilder().apply(filterConfigBlock)

        return LogxConfig(
            isDebug = debugMode,
            isDebugFilter = debugFilter,
            isDebugSave = fileConfig.saveToFile,
            saveFilePath = fileConfig.filePath,
            appName = appName,
            debugFilterList = filterConfig.filters,
            debugLogTypeList = logTypeConfig.types
        )
    }
}