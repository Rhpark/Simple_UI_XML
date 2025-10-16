package kr.open.library.simple_ui.logcat.runtime

import android.content.Context
import android.util.Log
import kr.open.library.simple_ui.logcat.config.LogxConfig
import kr.open.library.simple_ui.logcat.internal.file_writer.LogxFileWriterFactory
import kr.open.library.simple_ui.logcat.internal.file_writer.base.LogxFileWriterImp
import kr.open.library.simple_ui.logcat.internal.filter.DefaultLogFilter
import kr.open.library.simple_ui.logcat.internal.filter.base.LogFilterImp
import kr.open.library.simple_ui.logcat.internal.formatter.DefaultLogFormatter
import kr.open.library.simple_ui.logcat.internal.formatter.JsonLogFormatter
import kr.open.library.simple_ui.logcat.internal.formatter.ParentLogFormatter
import kr.open.library.simple_ui.logcat.internal.formatter.ThreadIdLogFormatter
import kr.open.library.simple_ui.logcat.internal.formatter.base.LogxFormattedData
import kr.open.library.simple_ui.logcat.internal.formatter.base.LogxFormatterImp
import kr.open.library.simple_ui.logcat.internal.stacktrace.LogxStackTrace
import kr.open.library.simple_ui.logcat.model.LogxType


/**
 * @param LogxStackTraceMetaData.class 정보를 얻어와 처리
 * Logcat 출력 & 파일 저장을 담당 (Android Lifecycle 지원)
 * - 포맷팅: LogFormatter 구현체들이 담당
 * - 필터링: LogFilter가 담당
 * - 파일 저장: LogFileWriter가 담당 (Lifecycle 기반 플러시)
 * - 로그 출력 여부는 LogxConfig에 의해 결정됨
 */
class LogxWriter(
    private var config: LogxConfig,
    private val context: Context? = null
) {

    private val stackTrace by lazy { LogxStackTrace() }

    // 스택 트레이스 캐싱
    private val stackInfoCache = mutableMapOf<String, String>()
    private var lastStackFrameHash = 0

    // 초기화 (Context를 FileWriter에 전달)
    private var logFilter: LogFilterImp = DefaultLogFilter(config)
    private var fileWriter: LogxFileWriterImp = LogxFileWriterFactory. create(config, context)

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
     * 설정 업데이트 시 캐시 무효화 및 의존성 재생성
     */
    fun updateConfig(newConfig: LogxConfig) {
        config = newConfig
        
        // 캐시 업데이트 (성능 최적화)
        cachedIsDebug = newConfig.isDebug
        cachedDebugLogTypes = newConfig.debugLogTypeList.toSet()
        
        // 스택 정보 캐시 초기화
        stackInfoCache.clear()
        lastStackFrameHash = 0
        
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
     * 기본 로그 작성 (Extension 함수용)
     */
    fun writeExtensions(tag: String, msg: Any?, type: LogxType) {
        if (!shouldLog(type)) return

        try {
            val stackInfo = getExtensionsStackInfo(tag) ?: return
            writeLogWithFormatter(tag, msg, type, stackInfo, defaultFormatter)
        } catch (e: Exception) {
            Log.e("LogxWriter", "Failed to write extensions log: ${e.message}", e)
        }
    }

    /**
     * 기본 로그 작성
     */
    fun write(tag: String, msg: Any?, type: LogxType) {
        if (!shouldLog(type)) return

        try {
            val stackInfo = getNormalStackInfo(tag) ?: return
            writeLogWithFormatter(tag, msg, type, stackInfo, defaultFormatter)
        } catch (e: Exception) {
            Log.e("LogxWriter", "Failed to write log: ${e.message}", e)
        }
    }

    /**
     * 스레드 ID 포함 로그 작성
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
     * 부모 메서드 정보 포함 로그 작성
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
     * 부모 메서드 정보 포함 로그 작성 (Extension용)
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
     * JSON 로그 작성 (Extension용)
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
     * JSON 로그 작성
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
     * Logcat 출력 & 파일 저장
     */
    private fun writeLogWithFormatter(
        tag: String,
        msg: Any?,
        type: LogxType,
        stackInfo: String,
        formatter: LogxFormatterImp
    ) {
        val formatted = formatter.format(tag, msg, type, stackInfo) ?: return
        outputLog(formatted)
        saveToFile(formatted)
    }

    private fun writeParentLog(tag: String, msg: Any?, stackInfo: String, formatter: ParentLogFormatter) {
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

    private fun writeJsonLog(tag: String, msg: String, stackInfo: String, formatter: JsonLogFormatter) {
        // JSON 시작 마커
        val startMarker = formatter.format(tag, "${stackInfo} ［ JSON_START ］", LogxType.JSON) ?: return
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

    private fun outputLog(formattedLog: LogxFormattedData) {
        when (formattedLog.logType) {
            LogxType.VERBOSE    -> Log.v(formattedLog.tag, formattedLog.message)
            LogxType.INFO       -> Log.i(formattedLog.tag, formattedLog.message)
            LogxType.JSON       -> Log.i(formattedLog.tag, formattedLog.message)
            LogxType.DEBUG      -> Log.d(formattedLog.tag, formattedLog.message)
            LogxType.THREAD_ID  -> Log.d(formattedLog.tag, formattedLog.message)
            LogxType.PARENT     -> Log.d(formattedLog.tag, formattedLog.message)
            LogxType.WARN       -> Log.w(formattedLog.tag, formattedLog.message)
            LogxType.ERROR      -> Log.e(formattedLog.tag, formattedLog.message)
        }
    }

    private fun saveToFile(formattedLog: LogxFormattedData) {
        try {
            fileWriter.writeLog(formattedLog.logType, formattedLog.tag, formattedLog.message)
        } catch (e: Exception) {
            Log.e("LogxWriter", "Failed to save log to file: ${e.message}", e)
        }
    }

    private fun getNormalStackInfo(tag: String): String? {
        val stackInfo = stackTrace.getStackTrace()
        val fileName = stackInfo.fileName.split(".")[0]

        return if (logFilter.shouldLog(tag, fileName)) {
            stackInfo.getMsgFrontNormal()
        } else {
            null
        }
    }

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
     * 빠른 로그 레벨 체크 (캐시된 값 사용)
     */
    private inline fun shouldLog(logType: LogxType): Boolean =
        cachedIsDebug && cachedDebugLogTypes.contains(logType)


    /**
     * 리소스 정리
     */
    fun cleanup() {
        fileWriter.cleanup()
    }
}
