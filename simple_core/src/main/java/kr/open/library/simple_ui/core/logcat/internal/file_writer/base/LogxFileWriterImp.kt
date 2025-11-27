package kr.open.library.simple_ui.core.logcat.internal.file_writer.base

import kr.open.library.simple_ui.core.logcat.model.LogxType


/**
 * 로그 파일 작성 전략을 위한 인터페이스
 * OCP 원칙을 준수하여 다양한 저장 전략을 구현할 수 있도록 함
 */
interface LogxFileWriterImp {
    /**
     * 로그를 파일에 작성
     * @param logType 로그 타입
     * @param tag 로그 태그
     * @param message 로그 메시지
     */
    fun writeLog(logType: LogxType, tag: String, message: String)
    
    /**
     * 리소스 정리
     */
    fun cleanup()
}
