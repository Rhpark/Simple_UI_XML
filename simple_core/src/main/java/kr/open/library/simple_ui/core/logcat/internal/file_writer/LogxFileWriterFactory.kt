package kr.open.library.simple_ui.core.logcat.internal.file_writer

import android.content.Context
import kr.open.library.simple_ui.core.logcat.config.LogxConfig
import kr.open.library.simple_ui.core.logcat.internal.file_writer.base.LogxFileWriterImp


/**
 * LogFileWriter 생성을 담당하는 팩토리
 * Android Lifecycle 지원을 위해 Context를 활용
 */
object LogxFileWriterFactory {

    /**
     * 설정에 따라 적절한 LogFileWriter를 생성
     * @param config 로그 설정
     * @param context Android Context (Lifecycle 플러시를 위해 필요)
     * @return 적절한 LogFileWriter 구현체
     */
    fun create(config: LogxConfig, context: Context? = null): LogxFileWriterImp {
        return if (config.isDebugSave) {
            LogxFileWriter(config.saveFilePath, context)
        } else {
            NoOpLogFileWriter()
        }
    }
}