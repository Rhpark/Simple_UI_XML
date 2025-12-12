package kr.open.library.simple_ui.core.logcat.internal.filter.base

/**
 * 로그 필터링을 담당하는 인터페이스
 * SRP: 로그 필터링 로직에만 집중
 */
interface LogFilterImp {
    /**
     * 주어진 태그와 파일명으로 로그를 필터링해야 하는지 확인
     * @param tag 로그 태그
     * @param fileName 파일명
     * @return true면 로그 출력, false면 필터링
     */
    fun shouldLog(tag: String, fileName: String): Boolean
}
