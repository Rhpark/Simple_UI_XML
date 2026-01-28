package kr.open.library.simple_ui.core.logcat.internal.pipeline

import android.content.Context
import android.content.pm.ApplicationInfo
import android.util.Log
import kr.open.library.simple_ui.core.BuildConfig
import kr.open.library.simple_ui.core.logcat.config.LogType
import kr.open.library.simple_ui.core.logcat.config.LogxConfigSnapshot
import kr.open.library.simple_ui.core.logcat.config.LogxConfigStore
import kr.open.library.simple_ui.core.logcat.internal.common.LogxTagHelper
import kr.open.library.simple_ui.core.logcat.internal.extractor.LogStackTraceExtractor
import kr.open.library.simple_ui.core.logcat.internal.filter.LogxFilter
import kr.open.library.simple_ui.core.logcat.internal.formatter.FormattedJson
import kr.open.library.simple_ui.core.logcat.internal.formatter.LogxFileLineBuilder
import kr.open.library.simple_ui.core.logcat.internal.formatter.LogxFormatter
import kr.open.library.simple_ui.core.logcat.internal.writer.LogxConsoleWriter
import kr.open.library.simple_ui.core.logcat.internal.writer.LogxFileWriter
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 로그 처리 파이프라인의 중심 오케스트레이터입니다.
 *
 * Orchestrates the logging pipeline: filtering, formatting, and writing.
 * <br><br>
 * 로그 필터링/포맷팅/출력을 조합해 실행합니다.
 *
 * @param contextProvider 파일 저장에 필요한 컨텍스트 제공자.
 * @param fileWriter 파일 로그 작성자.
 * @param configStore 설정 스토어.
 * @param filter 로그 허용 여부 필터.
 * @param formatter 로그 포맷터.
 * @param consoleWriter Logcat 출력기.
 * @param fileLineBuilder 파일 라인 빌더.
 */
internal open class LogxPipeline(
    private val contextProvider: () -> Context?,
    private val fileWriter: LogxFileWriter,
    private val configStore: LogxConfigStore = LogxConfigStore,
    private val filter: LogxFilter = LogxFilter,
    private val formatter: LogxFormatter = LogxFormatter,
    private val consoleWriter: LogxConsoleWriter = LogxConsoleWriter,
    private val fileLineBuilder: LogxFileLineBuilder = LogxFileLineBuilder(),
) {
    /**
     * Development mode cache flag.<br><br>
     * 개발 모드 여부 캐시 플래그.<br>
     */
    @Volatile
    private var developmentMode: Boolean = BuildConfig.DEBUG

    /**
     * 컨텍스트 미설정 경고를 1회만 출력하기 위한 플래그입니다.
     *
     * Ensures the missing-context warning is logged only once.
     * <br><br>
     * 컨텍스트 미설정 경고를 한 번만 출력하도록 제어합니다.
     */
    private val warnedNoContext = AtomicBoolean(false)

    public fun setDevelopmentMode(context: Context) {
        developmentMode = ((context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0)
    }

    public fun isDevelopmentMode(): Boolean = developmentMode

    /**
     * 일반 로그(d/i/w/e/v 등)를 처리합니다.
     *
     * Handles standard log types (d/i/w/e/v).
     * <br><br>
     * 표준 로그 타입을 필터링/포맷팅/출력합니다.
     *
     * @param type 로그 타입.
     * @param inputTag 사용자 입력 태그.
     * @param msg 출력 메시지.
     * @param hasMessage 메시지 포함 여부.
     * @param tagProvided 태그가 제공되었는지 여부.
     */
    open fun logStandard(type: LogType, inputTag: String?, msg: Any?, hasMessage: Boolean, tagProvided: Boolean) {
        val config = configStore.snapshot()
        if (!config.isLogging) return
        if (!config.logTypes.contains(type)) return

        val tag = resolveTag(inputTag, tagProvided)
        if (!filter.isAllowed(type, tag, config)) return

        val frame = LogStackTraceExtractor.extract(config.skipPackages).current
        val prefix = LogxTagHelper.buildPrefix(config.appName, tag)
        val payload = formatter.formatBasic(frame, msg?.toString(), hasMessage)

        consoleWriter.write(type, prefix, payload)
        writeFileLinesIfEnabled(config, inputTag) {
            fileLineBuilder.buildLines(type, prefix, listOf(payload))
        }
    }

    /**
     * PARENT 로그를 처리합니다.
     *
     * Handles the PARENT log type.
     * <br><br>
     * 부모/현재 프레임을 포함하는 PARENT 로그를 처리합니다.
     *
     * @param inputTag 사용자 입력 태그.
     * @param msg 출력 메시지.
     * @param hasMessage 메시지 포함 여부.
     * @param tagProvided 태그가 제공되었는지 여부.
     */
    open fun logParent(inputTag: String?, msg: Any?, hasMessage: Boolean, tagProvided: Boolean) {
        val config = configStore.snapshot()
        if (!config.isLogging) return
        if (!config.logTypes.contains(LogType.PARENT)) return

        val tag = resolveTag(inputTag, tagProvided)
        if (!filter.isAllowed(LogType.PARENT, tag, config)) return

        val frames = LogStackTraceExtractor.extract(config.skipPackages)
        val prefix = LogxTagHelper.buildPrefix(config.appName, tag)
        val payloadLines = formatter.formatParent(frames, msg?.toString(), hasMessage)

        consoleWriter.writeLines(LogType.PARENT, prefix, payloadLines)
        writeFileLinesIfEnabled(config, inputTag) {
            fileLineBuilder.buildLines(LogType.PARENT, prefix, payloadLines)
        }
    }

    /**
     * THREAD 로그를 처리합니다.
     *
     * Handles the THREAD log type.
     * <br><br>
     * 스레드 ID를 포함한 로그를 처리합니다.
     *
     * @param inputTag 사용자 입력 태그.
     * @param msg 출력 메시지.
     * @param hasMessage 메시지 포함 여부.
     * @param tagProvided 태그가 제공되었는지 여부.
     * @param threadId 출력할 스레드 ID.
     */
    open fun logThread(inputTag: String?, msg: Any?, hasMessage: Boolean, tagProvided: Boolean, threadId: Long) {
        val config = configStore.snapshot()
        if (!config.isLogging) return
        if (!config.logTypes.contains(LogType.THREAD)) return

        val tag = resolveTag(inputTag, tagProvided)
        if (!filter.isAllowed(LogType.THREAD, tag, config)) return

        val frame = LogStackTraceExtractor.extract(config.skipPackages).current
        val prefix = LogxTagHelper.buildPrefix(config.appName, tag)
        val payload = formatter.formatThread(frame, threadId, msg?.toString(), hasMessage)

        consoleWriter.write(LogType.THREAD, prefix, payload)
        writeFileLinesIfEnabled(config, inputTag) {
            fileLineBuilder.buildLines(LogType.THREAD, prefix, listOf(payload))
        }
    }

    /**
     * JSON 로그를 처리합니다.
     *
     * Handles JSON log output.
     * <br><br>
     * JSON 로그를 포맷팅해 출력합니다.
     *
     * @param inputTag 사용자 입력 태그.
     * @param json JSON 원문 문자열.
     * @param tagProvided 태그가 제공되었는지 여부.
     */
    open fun logJson(inputTag: String?, json: String, tagProvided: Boolean) {
        val config = configStore.snapshot()
        if (!config.isLogging) return
        if (!config.logTypes.contains(LogType.JSON)) return

        val tag = resolveTag(inputTag, tagProvided)
        if (!filter.isAllowed(LogType.JSON, tag, config)) return

        val frame = LogStackTraceExtractor.extract(config.skipPackages).current
        val prefix = LogxTagHelper.buildPrefix(config.appName, tag)
        val formattedJson = formatter.formatJson(frame, json)

        val consoleMessage = buildConsoleJsonMessage(formattedJson)
        consoleWriter.write(LogType.JSON, prefix, consoleMessage)

        writeFileLinesIfEnabled(config, inputTag) {
            fileLineBuilder.buildJsonLines(prefix, formattedJson)
        }
    }

    /**
     * 입력 태그를 검증하고 유효한 태그만 반환합니다.
     *
     * Validates the input tag and returns a usable tag or null.
     * <br><br>
     * 유효한 태그만 반환하며 잘못된 태그는 무시합니다.
     *
     * @param inputTag 사용자 입력 태그.
     * @param tagProvided 태그가 제공되었는지 여부.
     */
    private fun resolveTag(inputTag: String?, tagProvided: Boolean): String? {
        if (LogxTagHelper.isValidTag(inputTag)) return inputTag
        if (tagProvided && isDevelopmentMode()) {
            Log.e(LogxTagHelper.errorTag(inputTag), "Invalid tag input. Tag will be ignored.")
        }
        return null
    }

    /**
     * 파일 저장이 활성화된 경우 파일 로그를 작성합니다.
     *
     * Writes file log lines if file logging is enabled.
     * <br><br>
     * 파일 저장이 활성화되었을 때만 파일에 기록합니다.
     *
     * @param config 현재 설정 스냅샷.
     * @param inputTag 사용자 입력 태그.
     * @param lines 기록할 라인 목록.
     */
    private fun writeFileLines(config: LogxConfigSnapshot, inputTag: String?, lines: List<String>) {
        if (!config.isSaveEnabled) return

        val context = contextProvider()
        if (context == null) {
            warnNoContextOnce(inputTag)
            return
        }

        val errorTag = LogxTagHelper.errorTag(inputTag)
        fileWriter.writeLines(context, config, lines, errorTag)
    }

    /**
     * 파일 저장이 활성화된 경우 라인 생성 후 기록합니다.
     *
     * Builds and writes file log lines only when enabled.
     * <br><br>
     * 파일 저장이 활성화된 경우에만 라인을 생성해 기록합니다.
     *
     * @param config 현재 설정 스냅샷.
     * @param inputTag 사용자 입력 태그.
     * @param buildLines 라인 목록 생성 함수.
     */
    private inline fun writeFileLinesIfEnabled(config: LogxConfigSnapshot, inputTag: String?, buildLines: () -> List<String>) {
        if (!config.isSaveEnabled) return
        writeFileLines(config, inputTag, buildLines())
    }

    /**
     * 컨텍스트 미설정 경고를 한 번만 출력합니다.
     *
     * Logs the missing-context warning only once.
     * <br><br>
     * 컨텍스트 미설정 경고를 1회만 로그로 남깁니다.
     *
     * @param inputTag 사용자 입력 태그.
     */
    private fun warnNoContextOnce(inputTag: String?) {
        if (!warnedNoContext.compareAndSet(false, true)) return
        if (isDevelopmentMode()) {
            Log.e(
                LogxTagHelper.errorTag(inputTag),
                "Context is not initialized. Call Logx.initialize(applicationContext) before enabling file logging."
            )
        }
    }

    /**
     * 콘솔 출력용 JSON 메시지를 조합합니다.
     *
     * Builds a console message from formatted JSON parts.
     * <br><br>
     * JSON 헤더/본문/종료 라인을 결합합니다.
     *
     * @param formattedJson JSON 출력 구성 요소.
     */
    private fun buildConsoleJsonMessage(formattedJson: FormattedJson): String =
        buildString {
            append(formattedJson.header)
            formattedJson.bodyLines.forEach { line ->
                append('\n')
                append(line)
            }
            append('\n')
            append(formattedJson.endLine)
        }
}
