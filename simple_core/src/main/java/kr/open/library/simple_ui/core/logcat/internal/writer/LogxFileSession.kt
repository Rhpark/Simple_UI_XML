package kr.open.library.simple_ui.core.logcat.internal.writer

import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import kr.open.library.simple_ui.core.logcat.config.StorageType
import kr.open.library.simple_ui.core.logcat.internal.common.LogxTimeUtils

/**
 * 파일 로깅 세션에서 파일/Writer를 관리합니다.
 *
 * Manages file and writer lifecycle for file logging.
 * <br><br>
 * 파일 로그 세션의 파일 및 writer를 관리합니다.
 */
internal class LogxFileSession {
    /**
     * 현재 사용 중인 로그 파일입니다.
     *
     * Currently active log file.
     * <br><br>
     * 현재 세션에서 사용 중인 로그 파일입니다.
     */
    private var logFile: File? = null

    /**
     * 로그 파일의 식별 시그니처입니다.
     *
     * Signature to detect when a new file should be created.
     * <br><br>
     * 파일 재생성을 판단하기 위한 식별 문자열입니다.
     */
    private var logFileSignature: String? = null

    /**
     * 로그 파일에 쓰기 위한 BufferedWriter입니다.
     *
     * BufferedWriter used for appending log lines.
     * <br><br>
     * 로그 라인을 기록하기 위한 writer입니다.
     */
    private var writer: BufferedWriter? = null

    /**
     * 현재 설정에 맞는 writer를 반환합니다.
     *
     * Returns a writer for the active log file.
     * <br><br>
     * 현재 설정에 맞는 로그 파일 writer를 반환합니다.
     *
     * @param logDirectory 로그 디렉터리.
     * @param appName 앱 이름.
     * @param storageType 저장 타입.
     */
    fun getWriter(logDirectory: File, appName: String, storageType: StorageType): BufferedWriter {
        val signature = "${logDirectory.absolutePath}|${storageType.name}|$appName"
        val file = if (logFile != null && logFileSignature == signature) {
            logFile
        } else {
            close()
            null
        } ?: createNewFile(logDirectory, appName).also { created ->
            logFile = created
            logFileSignature = signature
        }

        return writer ?: BufferedWriter(FileWriter(file, true)).also { writer = it }
    }

    /**
     * 열린 writer를 닫고 세션을 정리합니다.
     *
     * Closes the writer and resets the session.
     * <br><br>
     * writer를 닫고 세션 상태를 초기화합니다.
     */
    fun close() {
        try {
            writer?.close()
        } catch (_: Exception) {
            // ignore
        } finally {
            writer = null
        }
    }

    /**
     * 새로운 로그 파일을 생성합니다.
     *
     * Creates a new log file under the given directory.
     * <br><br>
     * 지정된 디렉터리에 새 로그 파일을 생성합니다.
     *
     * @param directory 로그 디렉터리.
     * @param appName 앱 이름.
     */
    private fun createNewFile(directory: File, appName: String): File {
        val fileName = "${appName}_${LogxTimeUtils.fileTimestamp()}.txt"
        return File(directory, fileName)
    }
}


