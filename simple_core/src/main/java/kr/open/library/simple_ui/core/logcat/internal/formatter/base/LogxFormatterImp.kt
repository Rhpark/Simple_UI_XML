package kr.open.library.simple_ui.core.logcat.internal.formatter.base

import kr.open.library.simple_ui.core.logcat.model.LogxType


/**
 * 로그 포맷팅을 담당하는 인터페이스
 * OCP 원칙을 준수하여 새로운 포맷터를 쉽게 추가할 수 있도록 함
 */
interface LogxFormatterImp {
    /**
     * 로그 메시지를 포맷팅 (메모리 효율적인 방식)
     * @param tag 로그 태그
     * @param message 로그 메시지
     * @param logType 로그 타입
     * @param stackInfo 스택 정보 (옵션)
     * @return 포맷팅된 로그 메시지, null이면 필터링됨
     */
    fun format(tag: String, message: Any?, logType: LogxType, stackInfo: String = ""): LogxFormattedData?
}

