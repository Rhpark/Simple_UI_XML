package kr.open.library.simple_ui.core.logcat.internal.writer

import kr.open.library.simple_ui.core.extensions.trycatch.safeCatch
import kr.open.library.simple_ui.core.logcat.config.LogStorageType
import kr.open.library.simple_ui.core.logcat.internal.common.LogxTimeUtils
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

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
     * 로테이션 기준이 되는 기본 파일명입니다.
     *
     * Base file name seed for rotation.
     * <br><br>
     * 로테이션 시 _count를 붙이기 위한 기준 파일명입니다.
     */
    private var baseFileName: String? = null

    /**
     * 로테이션 순번 카운트입니다.
     *
     * Rotation sequence counter.
     * <br><br>
     * 파일 크기 초과 시 증가하는 카운트입니다.
     */
    private var rotationCount: Int = 0

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
    fun getWriter(logDirectory: File, appName: String, storageType: LogStorageType): BufferedWriter {
        val signature = "${logDirectory.absolutePath}|${storageType.name}|$appName"
        if (logFile == null || logFileSignature != signature) {
            resetSession()
            logFileSignature = signature
            baseFileName = buildBaseFileName(appName)
            rotationCount = 0
            logFile = createNewFile(logDirectory, baseFileName!!, rotationCount)
        } else if (shouldRotate(logFile)) {
            rotate(logDirectory, appName)
        }

        val file = logFile ?: run {
            baseFileName = buildBaseFileName(appName)
            rotationCount = 0
            createNewFile(logDirectory, baseFileName!!, rotationCount).also { created ->
                logFile = created
                logFileSignature = signature
            }
        }

        return writer ?: BufferedWriter(FileWriter(file, true)).also { writer = it }
    }

    /**
     * 열린 writer를 닫습니다.
     *
     * Closes the writer.
     * <br><br>
     * writer를 닫고 writer 상태만 초기화합니다.
     */
    fun close() {
        closeWriter()
    }

    private fun resetSession() {
        closeWriter()
        logFile = null
        logFileSignature = null
        baseFileName = null
        rotationCount = 0
    }

    private fun closeWriter() {
        safeCatch {
            writer?.close()
        }
        writer = null
    }

    private fun rotate(directory: File, appName: String) {
        closeWriter()
        if (baseFileName == null) {
            baseFileName = buildBaseFileName(appName)
            rotationCount = 0
        }
        rotationCount += 1
        logFile = createNewFile(directory, baseFileName!!, rotationCount)
    }

    private fun shouldRotate(file: File?): Boolean {
        if (file == null) return false
        return file.length() >= LogxFileConstants.MAX_FILE_SIZE_BYTES
    }

    private fun buildBaseFileName(appName: String): String = "${appName}_${LogxTimeUtils.fileTimestamp()}"

    /**
     * 새로운 로그 파일을 생성합니다.
     *
     * Creates a new log file under the given directory.
     * <br><br>
     * 지정된 디렉터리에 새 로그 파일을 생성합니다.
     *
     * @param directory 로그 디렉터리.
     * @param baseFileName 기본 파일명(타임스탬프 포함).
     * @param count 로테이션 카운트.
     */
    private fun createNewFile(directory: File, baseFileName: String, count: Int): File {
        val suffix = if (count > 0) "_$count" else ""
        val fileName = "$baseFileName$suffix.txt"
        return File(directory, fileName)
    }
}
