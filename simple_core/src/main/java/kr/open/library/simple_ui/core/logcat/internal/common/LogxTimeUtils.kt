package kr.open.library.simple_ui.core.logcat.internal.common

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Time utilities for Logx formatting.<br><br>
 * Logx 포맷팅을 위한 시간 유틸리티이다.<br>
 */
internal object LogxTimeUtils {
    /**
     * Pattern for log line timestamps.<br><br>
     * 로그 라인 타임스탬프 패턴.<br>
     */
    private const val timePattern = "yyyy-MM-dd HH:mm:ss.SSS"

    /**
     * Pattern for log file name timestamps.<br><br>
     * 로그 파일명 타임스탬프 패턴.<br>
     */
    private const val filePattern = "yyyy_MM_dd__HH-mm-ss-SSS"

    /**
     * Thread-local formatter for log timestamps.<br><br>
     * 로그 타임스탬프용 ThreadLocal 포매터.<br>
     */
    private val formatter: ThreadLocal<SimpleDateFormat> = ThreadLocal.withInitial {
        SimpleDateFormat(timePattern, Locale.getDefault())
    }

    /**
     * Thread-local formatter for file timestamps.<br><br>
     * 파일 타임스탬프용 ThreadLocal 포매터.<br>
     */
    private val fileFormatter: ThreadLocal<SimpleDateFormat> = ThreadLocal.withInitial {
        SimpleDateFormat(filePattern, Locale.getDefault())
    }

    /**
     * Returns current timestamp for log lines.<br><br>
     * 로그 라인에 사용할 현재 타임스탬프를 반환한다.<br>
     */
    fun nowTimestamp(): String = formatter.get().format(Date())

    /**
     * Returns current timestamp for file names.<br><br>
     * 파일명에 사용할 현재 타임스탬프를 반환한다.<br>
     */
    fun fileTimestamp(): String = fileFormatter.get().format(Date())
}

