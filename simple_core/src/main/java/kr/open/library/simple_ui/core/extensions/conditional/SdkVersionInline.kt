/**
 * SDK version gating helpers that keep version checks centralized.<br><br>
 * SDK 버전 조건을 한곳에 모아 버전별 분기를 깔끔하게 처리합니다.<br>
 */
package kr.open.library.simple_ui.core.extensions.conditional

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast

/**
 * Runs [doWork] only when `Build.VERSION.SDK_INT` is at least [ver].<br><br>
 * 현재 SDK 레벨이 [ver] 이상일 때만 [doWork]를 실행합니다.<br>
 *
 * @param ver Minimum SDK level required to run [doWork].<br><br>
 *        [doWork]를 실행하기 위한 최소 SDK 수준입니다.
 * @param doWork Lambda to invoke when the SDK requirement is satisfied.<br><br>
 *        SDK 조건을 만족했을 때 실행할 람다입니다.
 */
@ChecksSdkIntAtLeast(parameter = 0, lambda = 1)
public inline fun checkSdkVersion(ver: Int, doWork: () -> Unit) {
    if (Build.VERSION.SDK_INT >= ver) {
        doWork()
    }
}

/**
 * Returns [doWork] result when `Build.VERSION.SDK_INT` is at least [ver], otherwise null.<br><br>
 * 현재 SDK 레벨이 [ver] 이상이면 [doWork] 결과를 돌려주고, 그렇지 않으면 null을 반환합니다.<br>
 *
 * @param ver Minimum SDK level required to run [doWork].<br><br>
 *        [doWork]를 실행하기 위한 최소 SDK 수준입니다.
 * @param doWork Lambda to invoke once the SDK requirement is satisfied.<br><br>
 *        SDK 조건을 만족했을 때 실행할 람다입니다.
 * @return Result of [doWork] when condition is met, or null when skipped.<br><br>
 *         조건이 충족되면 [doWork] 반환값을, 아니면 null을 돌려줍니다.<br>
 */
@ChecksSdkIntAtLeast(parameter = 0, lambda = 1)
public inline fun <T> checkSdkVersion(ver: Int, doWork: () -> T): T? = if (Build.VERSION.SDK_INT >= ver) {
    doWork()
} else {
    null
}

/**
 * Chooses between [positiveWork] and [negativeWork] based on the SDK requirement defined by [ver].<br><br>
 * SDK 레벨이 [ver] 이상인지 여부에 따라 [positiveWork] 또는 [negativeWork]를 실행합니다.<br>
 *
 * @param ver Boundary SDK level that decides the branch.<br><br>
 *        분기를 결정하는 기준 SDK 버전입니다.
 * @param positiveWork Action executed when the requirement is met.<br><br>
 *        조건이 충족되었을 때 실행할 동작입니다.
 * @param negativeWork Action executed when the requirement is not met.<br><br>
 *        조건이 충족되지 않았을 때 실행할 동작입니다.
 * @return Result from whichever branch executed.<br><br>
 *         실행된 분기에서 반환된 값을 돌려줍니다.<br>
 */
@ChecksSdkIntAtLeast(parameter = 0, lambda = 1)
public inline fun <T> checkSdkVersion(
    ver: Int,
    positiveWork: () -> T,
    negativeWork: (ver: Int) -> T,
): T = if (Build.VERSION.SDK_INT >= ver) {
    positiveWork()
} else {
    negativeWork(ver)
}
