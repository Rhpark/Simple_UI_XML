package kr.open.library.simple_ui.core.logcat.internal.stacktrace

import android.util.Log
import kr.open.library.simple_ui.core.extensions.trycatch.requireInBounds
import kr.open.library.simple_ui.core.logcat.Logx

/**
 * 특정 조건내에
 * 현제 파일과 메소드의 위치와 라인 정보를 가져있는
 * LogxStackTraceMetaData() 객체를 반환한다.
 */
class LogxStackTrace {
    private val logExtensionsParentStackLevel = 11
    private val logParentStackLevel = 10
    private val logExtensionsStackLevel = 8
    private val logNormalStackLevel = 7

    fun getParentStackTrace() = getStackTrace(logParentStackLevel)

    fun getParentExtensionsStackTrace() = getStackTrace(logExtensionsParentStackLevel)

    fun getExtensionsStackTrace() = getStackTrace(logExtensionsStackLevel)

    fun getStackTrace() = getStackTrace(logNormalStackLevel)

    private fun getStackTrace(level: Int): LogxStackTraceMetaData {
        val currentThread = Thread.currentThread()
        val stackTraceSize = currentThread.stackTrace.size

        requireInBounds(level < stackTraceSize) {
            Log.e(Logx.getAppName(), "[Error] Logx: Stack trace level $level exceeds available stack size $stackTraceSize")
            "Stack trace level $level exceeds available stack size $stackTraceSize"
        }

        var isCoroutine = false

        for (i in level until stackTraceSize) {
//            Log.d("Test", "isNormalMethod index $i, class Name ${currentThread.stackTrace[i].className}, ${currentThread.stackTrace[i].fileName}, ${currentThread.stackTrace[i].methodName}, ${currentThread.stackTrace[i].lineNumber}")
            val item = currentThread.stackTrace[i]
            if (!isNormalMethod(item)) {
//                Log.d("Test","continue isNormalMethod index $i, class Name ${item.className}, ${item.fileName}, ${item.methodName}, ${item.lineNumber}")
                continue
            }

            if (isCoroutinePath(item.className)) {
                isCoroutine = true
//                Log.d("Test","continue isCoroutinePath index $i, class Name ${item.className}, ${item.fileName}")
                continue
            }
            if (!isCoroutine) {
                return LogxStackTraceMetaData(item)
            } else {
                isCoroutine = false
            }
        }

        val defaultItem = currentThread.stackTrace[level]

        Log.w(
            Logx.getAppName(),
            "[Warning] Logx: Could not find appropriate class, using fallback: ${defaultItem.className}.${defaultItem.methodName}",
        )

        return LogxStackTraceMetaData(defaultItem)
    }

    private fun isCoroutinePath(className: String): Boolean =
        (className.startsWith("kotlin.coroutines") || className.startsWith("kotlinx.coroutines"))

    private fun isNormalMethod(item: StackTraceElement): Boolean =
        !(
            item.methodName.contains("access$") ||
//                    item.methodName.contains("lambda$") ||
//                    item.className.contains("SyntheticClass") ||
                item.className.contains("Lambda0") ||
                item.className.contains("Lambda$")
        )
}
