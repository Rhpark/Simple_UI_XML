package kr.open.library.simple_ui.core.logcat.runtime

import android.content.Context
import android.util.Log
import kr.open.library.simple_ui.core.logcat.config.LogxConfig
import kr.open.library.simple_ui.core.logcat.internal.file_writer.LogxFileWriterFactory
import kr.open.library.simple_ui.core.logcat.internal.file_writer.base.LogxFileWriterImp
import kr.open.library.simple_ui.core.logcat.internal.filter.DefaultLogFilter
import kr.open.library.simple_ui.core.logcat.internal.filter.base.LogFilterImp
import kr.open.library.simple_ui.core.logcat.internal.formatter.DefaultLogFormatter
import kr.open.library.simple_ui.core.logcat.internal.formatter.JsonLogFormatter
import kr.open.library.simple_ui.core.logcat.internal.formatter.ParentLogFormatter
import kr.open.library.simple_ui.core.logcat.internal.formatter.ThreadIdLogFormatter
import kr.open.library.simple_ui.core.logcat.internal.formatter.base.LogxFormattedData
import kr.open.library.simple_ui.core.logcat.internal.formatter.base.LogxFormatterImp
import kr.open.library.simple_ui.core.logcat.internal.stacktrace.LogxStackTrace
import kr.open.library.simple_ui.core.logcat.model.LogxType

/**
 * Core runtime component responsible for log output to Logcat and file storage.<br>
 * Supports Android Lifecycle-based flush management and processes stack trace metadata.<br><br>
 * Logcat 출력과 파일 저장을 담당하는 핵심 런타임 컴포넌트입니다.<br>
 * Android Lifecycle 기반 플러시 관리를 지원하고 스택 트레이스 메타데이터를 처리합니다.<br>
 *
 * Architecture:<br>
 * - Formatting: Handled by LogFormatter implementations<br>
 * - Filtering: Handled by LogFilter<br>
 * - File storage: Handled by LogFileWriter (with Lifecycle-based flushing)<br>
 * - Log output control: Determined by LogxConfig<br><br>
 * 아키텍처:<br>
 * - 포맷팅: LogFormatter 구현체가 담당<br>
 * - 필터링: LogFilter가 담당<br>
 * - 파일 저장: LogFileWriter가 담당 (Lifecycle 기반 플러시)<br>
 * - 로그 출력 제어: LogxConfig에 의해 결정<br>
 *
 * Performance optimizations:<br>
 * - Configuration caching (isDebug, debugLogTypeList) for fast log level checks<br>
 * - Lazy initialization of stack trace processor<br><br>
 * 성능 최적화:<br>
 * - 빠른 로그 레벨 체크를 위한 설정 캐싱 (isDebug, debugLogTypeList)<br>
 * - 스택 트레이스 프로세서의 지연 초기화<br>
 *
 * @param config The initial configuration for log behavior.<br><br>
 *               로그 동작을 위한 초기 설정.
 *
 * @param context Optional Android context for file writing with lifecycle support. If null, file logging may be limited.<br><br>
 *                Lifecycle 지원과 함께 파일 작성을 위한 선택적 Android 컨텍스트. null이면 파일 로깅이 제한될 수 있습니다.
 */
class LogxWriter(
    private var config: LogxConfig,
    private val context: Context? = null,
) {
    private val stackTrace by lazy { LogxStackTrace() }

    // 스택 트레이스 캐싱

    // 초기화 (Context를 FileWriter에 전달)
    private var logFilter: LogFilterImp = DefaultLogFilter(config)
    private var fileWriter: LogxFileWriterImp = LogxFileWriterFactory.create(config, context)

    // 포맷터들
    private var defaultFormatter = DefaultLogFormatter(config)
    private var jsonFormatter = JsonLogFormatter(config)
    private var threadIdFormatter = ThreadIdLogFormatter(config)
    private var parentFormatter = ParentLogFormatter(config, stackTrace, false)
    private var parentExtensionsFormatter = ParentLogFormatter(config, stackTrace, true)

    // 성능을 위한 설정 캐싱
    @Volatile
    private var cachedIsDebug = config.isDebug

    @Volatile
    private var cachedDebugLogTypes = config.debugLogTypeList.toSet()

    /**
     * Updates the configuration and invalidates caches, regenerating all dependencies.<br>
     * This method is thread-safe but should be called sparingly due to resource cleanup and recreation.<br><br>
     * 설정을 업데이트하고 캐시를 무효화하며 모든 의존성을 재생성합니다.<br>
     * 이 메서드는 스레드 안전하지만 리소스 정리 및 재생성으로 인해 자주 호출하지 않는 것이 좋습니다.<br>
     *
     * @param newConfig The new configuration to apply.<br><br>
     *                  적용할 새로운 설정.
     */
    fun updateConfig(newConfig: LogxConfig) {
        config = newConfig

        // 캐시 업데이트 (성능 최적화)
        cachedIsDebug = newConfig.isDebug
        cachedDebugLogTypes = newConfig.debugLogTypeList.toSet()

        // 기존 리소스 정리
        fileWriter.cleanup()

        // 새 인스턴스 생성 (Context를 FileWriter에 전달)
        logFilter = DefaultLogFilter(config)
        fileWriter = LogxFileWriterFactory.create(config, context)

        // 포맷터들 재생성
        defaultFormatter = DefaultLogFormatter(config)
        jsonFormatter = JsonLogFormatter(config)
        threadIdFormatter = ThreadIdLogFormatter(config)
        parentFormatter = ParentLogFormatter(config, stackTrace, false)
        parentExtensionsFormatter = ParentLogFormatter(config, stackTrace, true)
    }

    /**
     * Writes a log entry for extension function calls (adjusts stack depth for correct source location).<br><br>
     * 확장 함수 호출을 위한 로그 항목을 작성합니다 (올바른 소스 위치를 위해 스택 깊이를 조정함).<br>
     *
     * @param tag The log tag.<br><br>
     *            로그 태그.
     *
     * @param msg The message to log.<br><br>
     *            로깅할 메시지.
     *
     * @param type The log type/level.<br><br>
     *            로그 타입/레벨.
     */
    fun writeExtensions(
        tag: String,
        msg: Any?,
        type: LogxType,
    ) {
        if (!shouldLog(type)) return

        try {
            val stackInfo = getExtensionsStackInfo(tag) ?: return
            writeLogWithFormatter(tag, msg, type, stackInfo, defaultFormatter)
        } catch (e: Exception) {
            Log.e("LogxWriter", "Failed to write extensions log: ${e.message}", e)
        }
    }

    /**
     * Writes a standard log entry with the specified tag, message, and type.<br><br>
     * 지정된 태그, 메시지 및 타입으로 표준 로그 항목을 작성합니다.<br>
     *
     * @param tag The log tag.<br><br>
     *            로그 태그.
     *
     * @param msg The message to log.<br><br>
     *            로깅할 메시지.
     *
     * @param type The log type/level.<br><br>
     *            로그 타입/레벨.
     */
    fun write(
        tag: String,
        msg: Any?,
        type: LogxType,
    ) {
        if (!shouldLog(type)) return

        try {
            val stackInfo = getNormalStackInfo(tag) ?: return
            writeLogWithFormatter(tag, msg, type, stackInfo, defaultFormatter)
        } catch (e: Exception) {
            Log.e("LogxWriter", "Failed to write log: ${e.message}", e)
        }
    }

    /**
     * Writes a log entry that includes the current thread ID.<br><br>
     * 현재 스레드 ID를 포함하는 로그 항목을 작성합니다.<br>
     *
     * @param tag The log tag.<br><br>
     *            로그 태그.
     *
     * @param msg The message to log.<br><br>
     *            로깅할 메시지.
     */
    fun writeThreadId(tag: String, msg: Any?) {
        if (!shouldLog(LogxType.THREAD_ID)) return

        try {
            val stackInfo = getNormalStackInfo(tag) ?: return
            writeLogWithFormatter(tag, msg, LogxType.THREAD_ID, stackInfo, threadIdFormatter)
        } catch (e: Exception) {
            Log.e("LogxWriter", "Failed to write thread ID log: ${e.message}", e)
        }
    }

    /**
     * Writes a log entry that includes parent method call information.<br><br>
     * 부모 메서드 호출 정보를 포함하는 로그 항목을 작성합니다.<br>
     *
     * @param tag The log tag.<br><br>
     *            로그 태그.
     *
     * @param msg The message to log.<br><br>
     *            로깅할 메시지.
     */
    fun writeParent(tag: String, msg: Any?) {
        if (!shouldLog(LogxType.PARENT)) return

        try {
            val stackInfo = getNormalStackInfo(tag) ?: return
            writeParentLog(tag, msg, stackInfo, parentFormatter)
        } catch (e: Exception) {
            Log.e("LogxWriter", "Failed to write parent log: ${e.message}", e)
        }
    }

    /**
     * Writes a log entry that includes parent method call information for extension function calls.<br><br>
     * 확장 함수 호출을 위한 부모 메서드 호출 정보를 포함하는 로그 항목을 작성합니다.<br>
     *
     * @param tag The log tag.<br><br>
     *            로그 태그.
     *
     * @param msg The message to log.<br><br>
     *            로깅할 메시지.
     */
    fun writeExtensionsParent(tag: String, msg: Any?) {
        if (!shouldLog(LogxType.PARENT)) return

        try {
            val stackInfo = getExtensionsStackInfo(tag) ?: return
            writeParentLog(tag, msg, stackInfo, parentExtensionsFormatter)
        } catch (e: Exception) {
            Log.e("LogxWriter", "Failed to write extensions parent log: ${e.message}", e)
        }
    }

    /**
     * Writes a JSON-formatted log entry for extension function calls with proper indentation and visual markers.<br><br>
     * 확장 함수 호출을 위해 적절한 들여쓰기와 시각적 마커를 사용하여 JSON 포맷 로그 항목을 작성합니다.<br>
     *
     * @param tag The log tag.<br><br>
     *            로그 태그.
     *
     * @param msg The JSON string to format and log.<br><br>
     *            포맷하고 로깅할 JSON 문자열.
     */
    fun writeJsonExtensions(tag: String, msg: String) {
        if (!shouldLog(LogxType.JSON)) return

        try {
            val stackInfo = getExtensionsStackInfo(tag) ?: return
            writeJsonLog(tag, msg, stackInfo, jsonFormatter)
        } catch (e: Exception) {
            Log.e("LogxWriter", "Failed to write JSON extensions log: ${e.message}", e)
        }
    }

    /**
     * Writes a JSON-formatted log entry with proper indentation and visual markers.<br><br>
     * 적절한 들여쓰기와 시각적 마커를 사용하여 JSON 포맷 로그 항목을 작성합니다.<br>
     *
     * @param tag The log tag.<br><br>
     *            로그 태그.
     *
     * @param msg The JSON string to format and log.<br><br>
     *            포맷하고 로깅할 JSON 문자열.
     */
    fun writeJson(tag: String, msg: String) {
        if (!shouldLog(LogxType.JSON)) return

        try {
            val stackInfo = getNormalStackInfo(tag) ?: return
            writeJsonLog(tag, msg, stackInfo, jsonFormatter)
        } catch (e: Exception) {
            Log.e("LogxWriter", "Failed to write JSON log: ${e.message}", e)
        }
    }

    /**
     * Writes log using the specified formatter, then outputs to Logcat and saves to file.<br><br>
     * 지정된 포맷터를 사용하여 로그를 작성한 다음 Logcat에 출력하고 파일에 저장합니다.<br>
     *
     * @param tag The log tag.<br><br>
     *            로그 태그.<br>
     *
     * @param msg The message to log.<br><br>
     *            로깅할 메시지.<br>
     *
     * @param type The log type/level.<br><br>
     *            로그 타입/레벨.<br>
     *
     * @param stackInfo Stack trace information string for source location.<br><br>
     *                  소스 위치를 위한 스택 트레이스 정보 문자열.<br>
     *
     * @param formatter The formatter to use for formatting the log message.<br><br>
     *                  로그 메시지 포맷팅에 사용할 포맷터.<br>
     */
    private fun writeLogWithFormatter(
        tag: String,
        msg: Any?,
        type: LogxType,
        stackInfo: String,
        formatter: LogxFormatterImp,
    ) {
        val formatted = formatter.format(tag, msg, type, stackInfo) ?: return
        outputLog(formatted)
        saveToFile(formatted)
    }

    /**
     * Writes a parent tracking log with two-stage output: parent call information first, then the actual message.<br><br>
     * 부모 추적 로그를 두 단계로 출력합니다: 먼저 부모 호출 정보, 그 다음 실제 메시지.<br>
     *
     * @param tag The log tag.<br><br>
     *            로그 태그.<br>
     *
     * @param msg The message to log.<br><br>
     *            로깅할 메시지.<br>
     *
     * @param stackInfo Stack trace information string for source location.<br><br>
     *                  소스 위치를 위한 스택 트레이스 정보 문자열.<br>
     *
     * @param formatter The ParentLogFormatter to use for formatting parent information.<br><br>
     *                  부모 정보 포맷팅에 사용할 ParentLogFormatter.<br>
     */
    private fun writeParentLog(
        tag: String,
        msg: Any?,
        stackInfo: String,
        formatter: ParentLogFormatter,
    ) {
        // 부모 정보를 먼저 출력
        formatter.formatParentInfo(tag)?.let { parentInfo ->
            outputLog(parentInfo)
            saveToFile(parentInfo)
        }

        // 실제 메시지 출력
        formatter.format(tag, msg, LogxType.PARENT, stackInfo)?.let { mainLog ->
            outputLog(mainLog)
            saveToFile(mainLog)
        }
    }

    /**
     * Writes a JSON log with visual markers (JSON_START and JSON_END) for better readability.<br><br>
     * 가독성 향상을 위해 시각적 마커(JSON_START 및 JSON_END)와 함께 JSON 로그를 작성합니다.<br>
     *
     * @param tag The log tag.<br><br>
     *            로그 태그.<br>
     *
     * @param msg The JSON string to format and log.<br><br>
     *            포맷하고 로깅할 JSON 문자열.<br>
     *
     * @param stackInfo Stack trace information string for source location.<br><br>
     *                  소스 위치를 위한 스택 트레이스 정보 문자열.<br>
     *
     * @param formatter The JsonLogFormatter to use for formatting JSON content.<br><br>
     *                  JSON 내용 포맷팅에 사용할 JsonLogFormatter.<br>
     */
    private fun writeJsonLog(
        tag: String,
        msg: String,
        stackInfo: String,
        formatter: JsonLogFormatter,
    ) {
        // JSON 시작 마커
        val startMarker = formatter.format(tag, "$stackInfo ［ JSON_START ］", LogxType.JSON) ?: return
        outputLog(startMarker)
        saveToFile(startMarker)

        // JSON 내용
        val jsonContent = formatter.format(tag, msg, LogxType.JSON) ?: return
        outputLog(jsonContent)
        saveToFile(jsonContent)

        // JSON 종료 마커
        val endMarker = formatter.format(tag, "［ JSON_END ］", LogxType.JSON) ?: return
        outputLog(endMarker)
        saveToFile(endMarker)
    }

    /**
     * Routes the formatted log to the appropriate Android Log level based on log type.<br><br>
     * 로그 타입에 따라 포맷된 로그를 적절한 Android Log 레벨로 라우팅합니다.<br>
     *
     * @param formattedLog The formatted log data containing tag, message, and type.<br><br>
     *                     태그, 메시지 및 타입을 포함하는 포맷된 로그 데이터.<br>
     */
    private fun outputLog(formattedLog: LogxFormattedData) {
        when (formattedLog.logType) {
            LogxType.VERBOSE -> Log.v(formattedLog.tag, formattedLog.message)
            LogxType.INFO -> Log.i(formattedLog.tag, formattedLog.message)
            LogxType.JSON -> Log.i(formattedLog.tag, formattedLog.message)
            LogxType.DEBUG -> Log.d(formattedLog.tag, formattedLog.message)
            LogxType.THREAD_ID -> Log.d(formattedLog.tag, formattedLog.message)
            LogxType.PARENT -> Log.d(formattedLog.tag, formattedLog.message)
            LogxType.WARN -> Log.w(formattedLog.tag, formattedLog.message)
            LogxType.ERROR -> Log.e(formattedLog.tag, formattedLog.message)
        }
    }

    /**
     * Saves the formatted log to file with error handling.<br><br>
     * 에러 처리와 함께 포맷된 로그를 파일에 저장합니다.<br>
     *
     * @param formattedLog The formatted log data to save.<br><br>
     *                     저장할 포맷된 로그 데이터.<br>
     */
    private fun saveToFile(formattedLog: LogxFormattedData) {
        try {
            fileWriter.writeLog(formattedLog.logType, formattedLog.tag, formattedLog.message)
        } catch (e: Exception) {
            Log.e("LogxWriter", "Failed to save log to file: ${e.message}", e)
        }
    }

    /**
     * Retrieves stack trace information for normal (non-extension) function calls with filtering.<br><br>
     * 필터링과 함께 일반(확장 함수가 아닌) 함수 호출에 대한 스택 트레이스 정보를 검색합니다.<br>
     *
     * @param tag The log tag used for filtering.<br><br>
     *            필터링에 사용되는 로그 태그.<br>
     *
     * @return Stack trace information string if allowed by filter, null otherwise.<br><br>
     *         필터에 의해 허용되면 스택 트레이스 정보 문자열, 그 외는 null.<br>
     */
    private fun getNormalStackInfo(tag: String): String? {
        val stackInfo = stackTrace.getStackTrace()
        val fileName = stackInfo.fileName.split(".")[0]

        return if (logFilter.shouldLog(tag, fileName)) {
            stackInfo.getMsgFrontNormal()
        } else {
            null
        }
    }

    /**
     * Retrieves stack trace information for extension function calls with filtering and adjusted stack depth.<br><br>
     * 필터링 및 조정된 스택 깊이와 함께 확장 함수 호출에 대한 스택 트레이스 정보를 검색합니다.<br>
     *
     * @param tag The log tag used for filtering.<br><br>
     *            필터링에 사용되는 로그 태그.<br>
     *
     * @return Stack trace information string if allowed by filter, null otherwise.<br><br>
     *         필터에 의해 허용되면 스택 트레이스 정보 문자열, 그 외는 null.<br>
     */
    private fun getExtensionsStackInfo(tag: String): String? {
        val stackInfo = stackTrace.getExtensionsStackTrace()
        val fileName = stackInfo.fileName.split(".")[0]

        return if (logFilter.shouldLog(tag, fileName)) {
            stackInfo.getMsgFrontNormal()
        } else {
            null
        }
    }

    /**
     * Fast log level check using cached values for performance.<br><br>
     * 성능을 위해 캐시된 값을 사용하여 빠른 로그 레벨 체크를 수행합니다.<br>
     *
     * @param logType The type of log to check.<br><br>
     *                체크할 로그 타입.<br>
     *
     * @return `true` if logging is enabled and the type is allowed, `false` otherwise.<br><br>
     *         로깅이 활성화되어 있고 해당 타입이 허용되면 `true`, 그 외는 `false`.<br>
     */
    private inline fun shouldLog(logType: LogxType): Boolean = cachedIsDebug && cachedDebugLogTypes.contains(logType)

    /**
     * Cleans up resources by flushing and closing the file writer.<br>
     * Should be called when the logging system is shutting down to ensure all buffered logs are written.<br><br>
     * 파일 작성기를 플러시하고 닫아 리소스를 정리합니다.<br>
     * 모든 버퍼링된 로그가 작성되도록 로깅 시스템이 종료될 때 호출해야 합니다.<br>
     */
    fun cleanup() {
        fileWriter.cleanup()
    }
}
