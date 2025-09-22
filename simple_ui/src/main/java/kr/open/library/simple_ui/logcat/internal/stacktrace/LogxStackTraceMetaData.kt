package kr.open.library.simple_ui.logcat.internal.stacktrace

import android.util.Log
import kr.open.library.simple_ui.logcat.Logx


/**
 *  LogxStackTrace의 metaData 클래스
 *  출력 부분 중 현 위치 파악 정보를 전달
 *  ex)
 *  1. 일반 - fileName:lineNumber.methodName -
 *  2. 부모 - fileName:lineNumber - [className.methodName]
 *  3. JSON - fileName:lineNumber -
 */
data class LogxStackTraceMetaData(private val item: StackTraceElement) {

    // 파일 이름을 한 번만 계산하고 저장
    val fileName: String by lazy {
        try {
            item.fileName ?: Class.forName(item.className).simpleName.split("\$")[0]
        } catch (e: ClassNotFoundException) {
            Log.e(Logx.getAppName(), "[ERROR] LogxStackTrace, Failed to resolve className: ${item.className}", e)
            "Unknown"
        } catch (e: Exception) {
            Log.e(Logx.getAppName(), "[ERROR] LogxStackTrace,  LogxStackTrace, Unexpected error getting fileName: ${e.message}", e)
            "Unknown"
        }
    }

    // 위치 정보 캐싱
    private val fileLocation by lazy { "(${fileName}:${item.lineNumber})" }

    // 일반 메시지 앞부분 캐싱
    private val msgFrontNormalCache by lazy {   "${fileLocation}.${item.methodName} - " }

    // 부모 메시지 앞부분 캐싱
    private val msgFrontParentCache by lazy { "${fileLocation} - [${item.className}.${item.methodName}]" }

    // JSON 메시지 앞부분 캐싱
    private val msgFrontJsonCache by lazy { "${fileLocation} - " }

    fun getMsgFrontNormal(): String = msgFrontNormalCache

    fun getMsgFrontParent(): String = msgFrontParentCache

    fun getMsgFrontJson(): String = msgFrontJsonCache
}
