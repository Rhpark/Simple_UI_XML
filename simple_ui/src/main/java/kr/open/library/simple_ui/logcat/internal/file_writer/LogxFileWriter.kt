package kr.open.library.simple_ui.logcat.internal.file_writer

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kr.open.library.simple_ui.logcat.internal.file_writer.base.LogxFileWriterImp
import kr.open.library.simple_ui.logcat.model.LogxType
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.write

/**
 * Logcat Log 파일 저장 구현체 (Android Lifecycle 기반 플러시 지원)
 */
class LogxFileWriter(
    private val filePath: String,
    private val context: Context? = null
) : LogxFileWriterImp {

    private val lock = ReentrantReadWriteLock()
    private val dateFormatter = SimpleDateFormat("yy-MM-dd, HH:mm:ss.SSS", Locale.getDefault())
    private val fileNameFormatter = SimpleDateFormat("yy-MM-dd", Locale.getDefault())

    private companion object {
        private val logWriterScope = CoroutineScope(Dispatchers.IO + Job()) // Singleton
    }

    /**
     * 로그 파일 작성 시 발생할 수 있는 예외
     */
    class LogFileWriteException(message: String, cause: Throwable? = null) : Exception(message, cause)

    init {
        createDirectoryIfNeeded()
        setupAndroidLifecycleFlush()
    }
    
    override fun writeLog(logType: LogxType, tag: String, message: String) {
        lock.write {
            try {
                val timestamp = dateFormatter.format(Date())
                val logFile = createLogFile()
                val logLine = "$timestamp/${logType.logTypeString}/$tag : $message"
                
                writeToFile(logFile, logLine)
                
            } catch (e: Exception) {
                Log.e("ImmediateLogFileWriter", "Failed to write log immediately: ${e.message}", e)
                throw LogFileWriteException("Failed to write log immediately", e)
            }
        }
    }
    
    override fun cleanup() {
        lock.write {
            // 즉시 저장 방식에서는 특별한 정리 작업이 필요하지 않음
            Log.d("ImmediateLogFileWriter", "Cleanup completed")
        }
    }
    
    private fun createDirectoryIfNeeded() {
        val directory = File(filePath)
        if (directory.exists()) return
        
        try {
            if (directory.mkdirs()) {
                Log.d("ImmediateLogFileWriter", "Directory created: $filePath")
            } else {
                Log.e("ImmediateLogFileWriter", "Failed to create directory: $filePath")
                throw LogFileWriteException("Failed to create directory: $filePath")
            }
        } catch (e: Exception) {
            Log.e("ImmediateLogFileWriter", "Exception while creating directory", e)
            throw LogFileWriteException("Exception while creating directory", e)
        }
    }
    
    private fun createLogFile(): File {
        val fileName = "${fileNameFormatter.format(Date())}_Log.txt"
        val logFile = File(filePath, fileName)
        
        if (!logFile.exists()) {
            try {
                if (logFile.createNewFile()) {
                    Log.d("ImmediateLogFileWriter", "Log file created: ${logFile.path}")
                } else {
                    Log.e("ImmediateLogFileWriter", "Failed to create log file: ${logFile.path}")
                    throw LogFileWriteException("Failed to create log file: ${logFile.path}")
                }
            } catch (e: IOException) {
                Log.e("ImmediateLogFileWriter", "IOException creating file: ${logFile.path}", e)
                throw LogFileWriteException("IOException creating file: ${logFile.path}", e)
            }
        }
        
        return logFile
    }
    
    private fun writeToFile(file: File, logLine: String) {
        logWriterScope.launch {
            try {
                BufferedWriter(FileWriter(file, true)).use { writer ->
                    writer.write(logLine)
                    writer.newLine()
                    writer.flush() // 즉시 플러시하여 데이터 손실 방지
                }
            } catch (e: IOException) {
                Log.e("ImmediateLogFileWriter", "Failed to write to file: ${file.path}", e)
                throw LogFileWriteException("Failed to write to file: ${file.path}", e)
            }
        }
    }

    /**
     * Android Lifecycle 기반 플러시 시스템 설정
     * Runtime.addShutdownHook를 대체하는 안전한 방식
     */
    private fun setupAndroidLifecycleFlush() {
        try {
            if (context != null) {
                // Android Lifecycle 기반 플러시 매니저 초기화
                val flushManager = LogxLifecycleFlushManager.getInstance()
                flushManager.initialize(context, logWriterScope)
                Log.d("LogxFileWriter", "Android Lifecycle flush manager initialized")
            } else {
                // Context가 없는 경우 fallback (라이브러리 외부 사용 시)
                Log.w("LogxFileWriter", "No Context provided - using fallback flush mechanism")
                setupFallbackFlush()
            }
        } catch (e: Exception) {
            Log.e("LogxFileWriter", "Failed to setup lifecycle flush", e)
            setupFallbackFlush()
        }
    }

    /**
     * Context가 없을 때 사용하는 fallback 플러시 (최소한의 안전장치)
     */
    private fun setupFallbackFlush() {
        try {
            // 크래시 시에만 최소한의 플러시 (기존 핸들러 존중)
            val originalHandler = Thread.getDefaultUncaughtExceptionHandler()
            Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
                try {
                    shutdownLogger()
                } catch (e: Exception) {
                    Log.e("LogxFileWriter", "Error during fallback flush", e)
                } finally {
                    originalHandler?.uncaughtException(thread, throwable)
                        ?: throwable.printStackTrace()
                }
            }
        } catch (e: Exception) {
            Log.e("LogxFileWriter", "Failed to setup fallback flush", e)
        }
    }

    private fun shutdownLogger() {
        try {
            logWriterScope.cancel() // 모든 코루틴 작업 취소
            Log.d("LogxFileWriter", "LogWriter shutdown completed")
        } catch (e: Exception) {
            Log.e("LogxFileWriter", "[Error] Error during logger shutdown: ${e.message}", e)
        }
    }
}