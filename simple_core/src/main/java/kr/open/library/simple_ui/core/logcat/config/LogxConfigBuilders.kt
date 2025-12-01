package kr.open.library.simple_ui.core.logcat.config

import kr.open.library.simple_ui.core.logcat.model.LogxType
import java.util.EnumSet

/**
 * DSL builder for configuring log file settings.<br><br>
 * 로그 파일 설정을 구성하기 위한 DSL 빌더입니다.<br>
 *
 * @property saveToFile Whether to save logs to a file.<br><br>
 *                      로그를 파일에 저장할지 여부.
 *
 * @property filePath The absolute path where log files will be saved.<br><br>
 *                    로그 파일이 저장될 절대 경로.
 */
@LogxConfigDsl
class LogxFileConfigBuilder {
    var saveToFile: Boolean = false
    var filePath: String = LogxPathUtils.getDefaultLogPath()
}

/**
 * DSL builder for configuring which log types to display.<br><br>
 * 표시할 로그 타입을 구성하기 위한 DSL 빌더입니다.<br>
 *
 * Provides convenient methods and operators to add/remove log types:<br>
 * - Unary `+` operator: Adds a log type<br>
 * - Unary `-` operator: Removes a log type<br>
 * - `all()`: Adds all available log types<br>
 * - `basic()`: Adds basic log types (VERBOSE, DEBUG, INFO, WARN, ERROR)<br>
 * - `extended()`: Adds extended log types (PARENT, JSON, THREAD_ID)<br><br>
 * 로그 타입을 추가/제거하는 편리한 메서드와 연산자를 제공합니다:<br>
 * - 단항 `+` 연산자: 로그 타입 추가<br>
 * - 단항 `-` 연산자: 로그 타입 제거<br>
 * - `all()`: 사용 가능한 모든 로그 타입 추가<br>
 * - `basic()`: 기본 로그 타입 추가 (VERBOSE, DEBUG, INFO, WARN, ERROR)<br>
 * - `extended()`: 확장 로그 타입 추가 (PARENT, JSON, THREAD_ID)<br>
 */
@LogxConfigDsl
class LogxTypeConfigBuilder {
    private val _types = EnumSet.noneOf(LogxType::class.java)

    /**
     * Returns the current set of configured log types.<br><br>
     * 현재 구성된 로그 타입 집합을 반환합니다.<br>
     */
    val types: EnumSet<LogxType>
        get() = _types

    /**
     * Unary plus operator to add a log type to the configuration.<br><br>
     * 로그 타입을 구성에 추가하는 단항 플러스 연산자입니다.<br>
     *
     * Usage example: `+LogxType.DEBUG`
     */
    operator fun LogxType.unaryPlus() {
        _types.add(this)
    }

    /**
     * Unary minus operator to remove a log type from the configuration.<br><br>
     * 로그 타입을 구성에서 제거하는 단항 마이너스 연산자입니다.<br>
     *
     * Usage example: `-LogxType.VERBOSE`
     */
    operator fun LogxType.unaryMinus() {
        _types.remove(this)
    }

    /**
     * Adds all available log types to the configuration.<br><br>
     * 사용 가능한 모든 로그 타입을 구성에 추가합니다.<br>
     */
    fun all() {
        _types.addAll(LogxType.entries)
    }

    /**
     * Adds basic log types: VERBOSE, DEBUG, INFO, WARN, ERROR.<br><br>
     * 기본 로그 타입을 추가합니다: VERBOSE, DEBUG, INFO, WARN, ERROR.<br>
     */
    fun basic() {
        _types.addAll(
            listOf(
                LogxType.VERBOSE,
                LogxType.DEBUG,
                LogxType.INFO,
                LogxType.WARN,
                LogxType.ERROR,
            ),
        )
    }

    /**
     * Adds extended log types: PARENT, JSON, THREAD_ID.<br><br>
     * 확장 로그 타입을 추가합니다: PARENT, JSON, THREAD_ID.<br>
     */
    fun extended() {
        _types.addAll(
            listOf(
                LogxType.PARENT,
                LogxType.JSON,
                LogxType.THREAD_ID,
            ),
        )
    }

    init {
        _types.addAll(LogxType.entries.toTypedArray()) // Initialize with all types by default | 기본적으로 모든 타입으로 초기화
    }
}

/**
 * DSL builder for configuring tag-based log filters.<br><br>
 * 태그 기반 로그 필터를 구성하기 위한 DSL 빌더입니다.<br>
 *
 * Provides convenient methods and operators to manage filter tags:<br>
 * - Unary `+` operator: Adds a filter tag<br>
 * - Unary `-` operator: Removes a filter tag<br>
 * - `addAll()`: Adds multiple filter tags at once<br>
 * - `clear()`: Removes all filter tags<br><br>
 * 필터 태그를 관리하는 편리한 메서드와 연산자를 제공합니다:<br>
 * - 단항 `+` 연산자: 필터 태그 추가<br>
 * - 단항 `-` 연산자: 필터 태그 제거<br>
 * - `addAll()`: 여러 필터 태그를 한 번에 추가<br>
 * - `clear()`: 모든 필터 태그 제거<br>
 */
@LogxConfigDsl
class LogxFilterConfigBuilder {
    private val _filters = mutableSetOf<String>()

    /**
     * Returns an immutable copy of the current filter tags.<br><br>
     * 현재 필터 태그의 불변 복사본을 반환합니다.<br>
     */
    val filters: Set<String>
        get() = _filters.toSet()

    /**
     * Unary plus operator to add a filter tag.<br><br>
     * 필터 태그를 추가하는 단항 플러스 연산자입니다.<br>
     *
     * Usage example: `+"MyTag"`
     */
    operator fun String.unaryPlus() {
        _filters.add(this)
    }

    /**
     * Unary minus operator to remove a filter tag.<br><br>
     * 필터 태그를 제거하는 단항 마이너스 연산자입니다.<br>
     *
     * Usage example: `-"MyTag"`
     */
    operator fun String.unaryMinus() {
        _filters.remove(this)
    }

    /**
     * Adds multiple filter tags at once.<br><br>
     * 여러 필터 태그를 한 번에 추가합니다.<br>
     *
     * @param filters Vararg parameter of filter tags to add.<br><br>
     *                추가할 필터 태그의 가변 인자 매개변수.
     */
    fun addAll(vararg filters: String) {
        _filters.addAll(filters)
    }

    /**
     * Removes all filter tags from the configuration.<br><br>
     * 구성에서 모든 필터 태그를 제거합니다.<br>
     */
    fun clear() {
        _filters.clear()
    }
}
