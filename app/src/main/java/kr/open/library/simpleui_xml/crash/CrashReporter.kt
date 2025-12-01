package kr.open.library.simpleui_xml.crash

import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.PrintWriter
import java.io.StringWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Crash Reporter for Verification Build
 * <br><br>
 * Verification 빌드용 크래시 리포터
 * <br>
 *
 * @param cloudFunctionUrl Cloud Functions URL for crash reporting<br><br>크래시 보고용 Cloud Functions URL<br>
 * @param apiKey API Key for authentication<br><br>인증용 API 키<br>
 * @param appVersion App version name<br><br>앱 버전 이름<br>
 */
class CrashReporter(
    private val cloudFunctionUrl: String,
    private val apiKey: String,
    private val appVersion: String,
) : Thread.UncaughtExceptionHandler {
    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(
        thread: Thread,
        throwable: Throwable,
    ) {
        try {
            // Collect crash information
            // 크래시 정보 수집
            Log.e(TAG, "Failed to report crash + $throwable")
            val crashData = collectCrashData(throwable)

            // Send crash report to Cloud Functions
            // Cloud Functions로 크래시 보고 전송
            sendCrashReport(crashData)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to report crash", e)
        } finally {
            // Call default handler to terminate the app
            // 기본 핸들러 호출하여 앱 종료
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    /**
     * Collect crash data from throwable
     * <br><br>
     * Throwable로부터 크래시 데이터 수집
     * <br>
     *
     * @param throwable Exception that caused the crash<br><br>크래시를 발생시킨 Exception<br>
     * @return JSON object containing crash information<br><br>크래시 정보를 담은 JSON 객체<br>
     */
    private fun collectCrashData(throwable: Throwable): JSONObject {
        val stackTrace =
            StringWriter()
                .apply {
                    throwable.printStackTrace(PrintWriter(this))
                }.toString()

        return JSONObject().apply {
            put("exceptionType", throwable.javaClass.simpleName)
            put("message", throwable.message ?: "No message")
            put("stackTrace", stackTrace)
            put("osVersion", "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
            put("deviceModel", "${Build.MANUFACTURER} ${Build.MODEL}")
            put("appVersion", appVersion)
            put("timestamp", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))
        }
    }

    /**
     * Send crash report to Cloud Functions
     * <br><br>
     * Cloud Functions로 크래시 보고 전송
     * <br>
     *
     * @param crashData JSON object containing crash information<br><br>크래시 정보를 담은 JSON 객체<br>
     */
    private fun sendCrashReport(crashData: JSONObject) {
        // Send in background thread (quick execution before app dies)
        // 백그라운드 스레드에서 전송 (앱이 죽기 전 빠른 실행)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(cloudFunctionUrl)
                val connection = url.openConnection() as HttpURLConnection

                connection.apply {
                    requestMethod = "POST"
                    doOutput = true
                    connectTimeout = 3000 // 3 seconds timeout
                    readTimeout = 3000
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("X-API-Key", apiKey)
                }

                // Send JSON data
                // JSON 데이터 전송
                connection.outputStream.use { outputStream ->
                    outputStream.write(crashData.toString().toByteArray())
                }

                val responseCode = connection.responseCode
                Log.d(TAG, "Crash report sent: $responseCode")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send crash report", e)
            }
        }

        // Wait briefly for transmission to complete
        // 전송 완료를 위해 잠시 대기
        Thread.sleep(500)
    }

    companion object {
        private const val TAG = "CrashReporter"
    }
}
