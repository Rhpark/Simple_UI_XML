package kr.open.library.simpleui_xml.deviceverification.harness

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Vibrator
import android.os.VibratorManager
import kr.open.library.simple_ui.core.extensions.conditional.checkSdkVersion

/**
 * Runs registered cleanup actions in reverse order and clears shared device side effects.<br><br>
 * 등록된 정리 작업을 역순으로 실행하고 실기기에 남은 공통 부수 효과를 제거합니다.<br>
 *
 * @param context Target application context used to access system services.<br><br>
 *                시스템 서비스 접근에 사용하는 대상 애플리케이션 Context입니다.<br>
 */
internal class DeviceStateCleanup(
    private val context: Context,
) {
    private val cleanupActions = ArrayDeque<Pair<String, () -> Unit>>()

    fun register(
        name: String,
        action: () -> Unit,
    ) {
        cleanupActions.addFirst(name to action)
    }

    fun runAll(): List<Throwable> {
        val failures = mutableListOf<Throwable>()

        cleanupActions.forEach { (name, action) ->
            runCatching(action).exceptionOrNull()?.let { error ->
                failures += IllegalStateException("단말 상태 정리 실패: $name", error)
            }
        }
        cleanupActions.clear()

        runCatching {
            context.getSystemService(NotificationManager::class.java)?.cancelAll()
        }.exceptionOrNull()?.let(failures::add)

        runCatching {
            checkSdkVersion(
                ver = Build.VERSION_CODES.S,
                positiveWork = {
                    context.getSystemService(VibratorManager::class.java)?.defaultVibrator?.cancel()
                },
                negativeWork = {
                    @Suppress("DEPRECATION")
                    context.getSystemService(Vibrator::class.java)?.cancel()
                },
            )
        }.exceptionOrNull()?.let(failures::add)

        return failures
    }
}
