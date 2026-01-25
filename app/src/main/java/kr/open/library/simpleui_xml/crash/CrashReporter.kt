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
 * Verification 鍮뚮뱶???щ옒??由ы룷??
 * <br>
 *
 * @param cloudFunctionUrl Cloud Functions URL for crash reporting<br><br>?щ옒??蹂닿퀬??Cloud Functions URL<br>
 * @param apiKey API Key for authentication<br><br>?몄쬆??API ??br>
 * @param appVersion App version name<br><br>??踰꾩쟾 ?대쫫<br>
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
            // ?щ옒???뺣낫 ?섏쭛
            Log.e(TAG, "Failed to report crash + $throwable")
            val crashData = collectCrashData(throwable)

            // Send crash report to Cloud Functions
            // Cloud Functions濡??щ옒??蹂닿퀬 ?꾩넚
            sendCrashReport(crashData)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to report crash", e)
        } finally {
            // Call default handler to terminate the app
            // 湲곕낯 ?몃뱾???몄텧?섏뿬 ??醫낅즺
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    /**
     * Collect crash data from throwable
     * <br><br>
     * Throwable濡쒕????щ옒???곗씠???섏쭛
     * <br>
     *
     * @param throwable Exception that caused the crash<br><br>?щ옒?쒕? 諛쒖깮?쒗궓 Exception<br>
     * @return JSON object containing crash information<br><br>?щ옒???뺣낫瑜??댁? JSON 媛앹껜<br>
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
     * Cloud Functions濡??щ옒??蹂닿퀬 ?꾩넚
     * <br>
     *
     * @param crashData JSON object containing crash information<br><br>?щ옒???뺣낫瑜??댁? JSON 媛앹껜<br>
     */
    private fun sendCrashReport(crashData: JSONObject) {
        // Send in background thread (quick execution before app dies)
        // 諛깃렇?쇱슫???ㅻ젅?쒖뿉???꾩넚 (?깆씠 二쎄린 ??鍮좊Ⅸ ?ㅽ뻾)
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
                // JSON ?곗씠???꾩넚
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
        // ?꾩넚 ?꾨즺瑜??꾪빐 ?좎떆 ?湲?
        Thread.sleep(500)
    }

    companion object {
        private const val TAG = "CrashReporter"
    }
}
