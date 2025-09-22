package kr.open.library.simple_ui.logcat.internal.file_writer

import kr.open.library.simple_ui.logcat.internal.file_writer.base.LogxFileWriterImp
import kr.open.library.simple_ui.logcat.model.LogxType


/**
 * 파일 저장을 하지 않는 No-Op 구현체
 * SRP: 파일 저장 비활성화에만 집중
 * Null Object Pattern 적용
 */
class NoOpLogFileWriter : LogxFileWriterImp {
    
    override fun writeLog(logType: LogxType, tag: String, message: String) {
        // 아무것도 하지 않음
    }
    
    override fun cleanup() {
        // 아무것도 하지 않음
    }
}