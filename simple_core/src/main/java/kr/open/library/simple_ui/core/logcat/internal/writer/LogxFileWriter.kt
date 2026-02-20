package kr.open.library.simple_ui.core.logcat.internal.writer

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kr.open.library.simple_ui.core.logcat.config.LogxConfigSnapshot
import java.io.IOException

/**
 * 파일 로그를 비동기로 기록하는 writer입니다.
 *
 * Asynchronous file log writer backed by a coroutine channel.
 * <br><br>
 * 코루틴 채널을 통해 파일 로그를 비동기로 기록합니다.
 */
internal class LogxFileWriter {
    /**
     * 파일 writer 명령을 표현하는 내부 커맨드입니다.
     *
     * Command types consumed by the writer coroutine.
     * <br><br>
     * writer 코루틴이 처리하는 명령 타입입니다.
     */
    private sealed interface WriterCommand {
        /**
         * 로그 라인을 기록하라는 명령입니다.
         *
         * Command to write log lines to file.
         * <br><br>
         * 파일에 로그 라인 목록을 기록합니다.
         *
         * @property context 앱 컨텍스트.
         * @property config 현재 설정 스냅샷.
         * @property lines 기록할 라인 목록.
         * @property errorTag 오류 로그 태그.
         */
        data class WriteLines(
            val context: Context,
            val config: LogxConfigSnapshot,
            val lines: List<String>,
            val errorTag: String,
        ) : WriterCommand

        /**
         * writer를 닫으라는 명령입니다.
         *
         * Command to close the writer session.
         * <br><br>
         * writer 세션을 닫습니다.
         */
        data object Close : WriterCommand
    }

    /**
     * 파일 기록 동기화를 위한 락입니다.
     *
     * Lock object for synchronizing file writes.
     * <br><br>
     * 파일 기록 동기화를 위한 락 객체입니다.
     */
    private val fileLock = Any()

    /**
     * 로그 디렉터리 경로 계산기입니다.
     *
     * Resolves log directory paths.
     * <br><br>
     * 로그 디렉터리 경로를 계산합니다.
     */
    private val pathResolver = LogxFilePathResolver()

    /**
     * 파일/Writer 세션 관리자입니다.
     *
     * Manages file session and writer lifecycle.
     * <br><br>
     * 파일 세션과 writer 생명주기를 관리합니다.
     */
    private val fileSession = LogxFileSession()

    /**
     * 파일 기록 전용 코루틴 스코프입니다.
     *
     * Coroutine scope used for file writing.
     * <br><br>
     * 파일 기록 전용 코루틴 스코프입니다.
     */
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * writer 명령을 전달하는 채널입니다.
     *
     * Channel used to send writer commands.
     * <br><br>
     * writer 명령을 전달하는 채널입니다.
     */
    private val channel = Channel<WriterCommand>(Channel.BUFFERED)

    init {
        // writer 전용 코루틴에서 명령을 순차 처리합니다.
        scope.launch {
            for (command in channel) {
                when (command) {
                    is WriterCommand.WriteLines -> writeLinesInternal(
                        command.context,
                        command.config,
                        command.lines,
                        command.errorTag,
                    )
                    is WriterCommand.Close -> closeWriterInternal()
                }
            }
        }
    }

    /**
     * 파일 로그 라인을 기록 요청합니다.
     *
     * Requests writing log lines to file.
     * <br><br>
     * 파일 로그 라인 기록을 요청합니다.
     *
     * @param context 앱 컨텍스트.
     * @param config 현재 설정 스냅샷.
     * @param lines 기록할 라인 목록.
     * @param errorTag 오류 로그 태그.
     */
    fun writeLines(context: Context, config: LogxConfigSnapshot, lines: List<String>, errorTag: String) {
        if (lines.isEmpty()) return
        val command = WriterCommand.WriteLines(context, config, lines, errorTag)
        val result = channel.trySend(command)
        if (result.isFailure) {
            scope.launch { channel.send(command) }
        }
    }

    /**
     * writer 세션을 닫도록 요청합니다.
     *
     * Requests closing the writer session.
     * <br><br>
     * writer 세션을 닫도록 요청합니다.
     */
    fun requestClose() {
        val result = channel.trySend(WriterCommand.Close)
        if (result.isFailure) {
            scope.launch { channel.send(WriterCommand.Close) }
        }
    }

    /**
     * 실제 파일에 로그 라인을 기록합니다.
     *
     * Performs the actual file write for log lines.
     * <br><br>
     * 실제 파일 기록을 수행합니다.
     *
     * @param context 앱 컨텍스트.
     * @param config 현재 설정 스냅샷.
     * @param lines 기록할 라인 목록.
     * @param errorTag 오류 로그 태그.
     */
    private fun writeLinesInternal(context: Context, config: LogxConfigSnapshot, lines: List<String>, errorTag: String) {
        synchronized(fileLock) {
            val logDirectory = pathResolver.resolveDirectory(context, config, errorTag) ?: return
            try {
                val activeWriter = fileSession.getWriter(logDirectory, config.appName, config.storageType)
                lines.forEach { line ->
                    activeWriter.write(line)
                    activeWriter.newLine()
                }
                activeWriter.flush()
            } catch (e: IOException) {
                Log.e(errorTag, "Failed to write log file: ${e.message}")
                fileSession.close()
            } catch (e: RuntimeException) {
                Log.e(errorTag, "Failed to write log file: ${e.message}")
                fileSession.close()
            }
        }
    }

    /**
     * writer 세션을 닫습니다.
     *
     * Closes the file writer session.
     * <br><br>
     * 파일 writer 세션을 닫습니다.
     */
    private fun closeWriterInternal() {
        synchronized(fileLock) {
            fileSession.close()
        }
    }
}
