package kr.open.library.simple_ui.core.logcat.config

/**
 * DSL marker annotation to prevent implicit receivers in nested DSL scopes.<br><br>
 * 중첩된 DSL 범위에서 암시적 리시버를 방지하는 DSL 마커 어노테이션입니다.<br>
 */
@DslMarker
annotation class LogxConfigDsl

/**
 * DSL entry point function for creating LogxConfig using a type-safe builder pattern.<br><br>
 * 타입 안전 빌더 패턴을 사용하여 LogxConfig를 생성하는 DSL 진입점 함수입니다.<br>
 *
 * Example usage:
 * ```kotlin
 * val config = logxConfig {
 *     debugMode = true
 *     appName = "MyApp"
 *
 *     fileConfig {
 *         saveToFile = true
 *         filePath = "/custom/path"
 *     }
 *
 *     logTypes {
 *         basic()
 *         +LogxType.JSON
 *     }
 *
 *     filters {
 *         +"MyTag"
 *         +"AnotherTag"
 *     }
 * }
 * ```
 *
 * @param block The DSL configuration block.<br><br>
 *              DSL 설정 블록.
 *
 * @return A configured LogxConfig instance.<br><br>
 *         구성된 LogxConfig 인스턴스.<br>
 */
fun logxConfig(block: LogxDslBuilder.() -> Unit): LogxConfig = LogxDslBuilder().apply(block).build()

/**
 * Main DSL builder class for configuring Logx logging system with a fluent API.<br><br>
 * 유창한 API로 Logx 로깅 시스템을 구성하기 위한 주요 DSL 빌더 클래스입니다.<br>
 *
 * @property debugMode Enables or disables debug logging. Defaults to true.<br><br>
 *                     디버그 로깅을 활성화 또는 비활성화합니다. 기본값은 true입니다.
 *
 * @property debugFilter Enables tag-based filtering. Defaults to false.<br><br>
 *                       태그 기반 필터링을 활성화합니다. 기본값은 false입니다.
 *
 * @property appName The application name for log organization. Defaults to "RhPark".<br><br>
 *                   로그 구성에 사용될 애플리케이션 이름입니다. 기본값은 "RhPark"입니다.
 */
@LogxConfigDsl
class LogxDslBuilder {
    var debugMode: Boolean = true
    var debugFilter: Boolean = false
    var appName: String = "RhPark"

    private var fileConfigBlock: LogxFileConfigBuilder.() -> Unit = {}
    private var logTypeConfigBlock: LogxTypeConfigBuilder.() -> Unit = {}
    private var filterConfigBlock: LogxFilterConfigBuilder.() -> Unit = {}

    /**
     * Configures log file settings using a nested DSL block.<br><br>
     * 중첩된 DSL 블록을 사용하여 로그 파일 설정을 구성합니다.<br>
     *
     * @param block The configuration block for file settings.<br><br>
     *              파일 설정을 위한 구성 블록.
     */
    fun fileConfig(block: LogxFileConfigBuilder.() -> Unit) {
        fileConfigBlock = block
    }

    /**
     * Configures which log types to display using a nested DSL block.<br><br>
     * 중첩된 DSL 블록을 사용하여 표시할 로그 타입을 구성합니다.<br>
     *
     * @param block The configuration block for log types.<br><br>
     *              로그 타입을 위한 구성 블록.
     */
    fun logTypes(block: LogxTypeConfigBuilder.() -> Unit) {
        logTypeConfigBlock = block
    }

    /**
     * Configures tag-based filters using a nested DSL block.<br><br>
     * 중첩된 DSL 블록을 사용하여 태그 기반 필터를 구성합니다.<br>
     *
     * @param block The configuration block for filters.<br><br>
     *              필터를 위한 구성 블록.
     */
    fun filters(block: LogxFilterConfigBuilder.() -> Unit) {
        filterConfigBlock = block
    }

    /**
     * Builds and returns the final LogxConfig instance from the DSL configuration.<br><br>
     * DSL 구성으로부터 최종 LogxConfig 인스턴스를 빌드하고 반환합니다.<br>
     *
     * @return A fully configured LogxConfig instance.<br><br>
     *         완전히 구성된 LogxConfig 인스턴스.<br>
     */
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
            debugLogTypeList = logTypeConfig.types,
        )
    }
}
