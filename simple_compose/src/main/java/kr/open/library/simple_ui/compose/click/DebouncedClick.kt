package kr.open.library.simple_ui.compose.click

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.debugInspectorInfo

// ---------------------------------------------------------------------------
// 내부 상수
// Internal constants
// ---------------------------------------------------------------------------

/**
 * 기본 중복 클릭 차단 간격(밀리초)입니다.<br>
 * Default interval (in milliseconds) during which repeated clicks are ignored.<br>
 */
public const val DEFAULT_CLICK_DEBOUNCE_MS: Long = 500L

// ---------------------------------------------------------------------------
// 내부 순수 로직 (unit 테스트 대상)
// Internal pure logic (subject to unit testing)
// ---------------------------------------------------------------------------

/**
 * 마지막 클릭 시각과 현재 시각을 비교해 클릭을 허용할지 판단합니다.<br>
 * Decides whether a click is accepted by comparing the last click time with the current time.<br>
 *
 * @param lastClickTimeMs Timestamp of the previously accepted click.<br><br>
 *                        직전에 허용된 클릭의 시각입니다.<br>
 * @param nowMs Current timestamp.<br><br>
 *              현재 시각입니다.<br>
 * @param debounceMs Minimum interval required between accepted clicks.<br><br>
 *                   허용되는 클릭 사이의 최소 간격입니다.<br>
 * @return `true` when the click is accepted.<br><br>
 *         클릭이 허용되면 `true`입니다.<br>
 */
internal fun isClickAccepted(
    lastClickTimeMs: Long,
    nowMs: Long,
    debounceMs: Long,
): Boolean = nowMs - lastClickTimeMs >= debounceMs

/**
 * 클릭 허용 시각을 보관하는 홀더입니다.<br>
 * Holder that keeps the timestamp of the last accepted click.<br>
 */
internal class ClickDebouncer(
    private val debounceMs: Long,
) {
    private var lastClickTimeMs: Long = 0L

    /**
     * 현재 클릭을 허용할지 판단하고, 허용 시 내부 시각을 갱신합니다.<br>
     * Decides whether the current click is accepted and updates the internal timestamp when it is.<br>
     *
     * @param nowMs Current timestamp.<br><br>
     *              현재 시각입니다.<br>
     * @return `true` when the click is accepted.<br><br>
     *         클릭이 허용되면 `true`입니다.<br>
     */
    fun tryAccept(nowMs: Long): Boolean {
        if (!isClickAccepted(lastClickTimeMs, nowMs, debounceMs)) return false
        lastClickTimeMs = nowMs
        return true
    }
}

// ---------------------------------------------------------------------------
// 공개 Modifier API
// Public Modifier API
// ---------------------------------------------------------------------------

/**
 * 지정한 간격 안의 중복 클릭을 무시하는 [clickable] Modifier입니다.<br>
 * A [clickable] Modifier that ignores repeated clicks within the given interval.<br>
 *
 * 화면 전환이나 네트워크 요청처럼 한 번만 실행되어야 하는 동작에서, 빠른 연타로 인해
 * [onClick]이 여러 번 실행되는 문제를 막습니다.<br>
 * Prevents [onClick] from running multiple times on rapid taps for actions that must run once,
 * such as navigation or a network request.<br>
 *
 * 차단 시각은 컴포지션 동안 유지되며, 구성 변경으로 컴포지션이 재생성되면 초기화됩니다.<br>
 * The debounce timestamp is kept for the composition lifetime and resets when the composition is
 * recreated by a configuration change.<br>
 *
 * **사용 예시 / Usage example**:
 * ```kotlin
 * Box(
 *     modifier = Modifier.debouncedClickable {
 *         navigateToDetail()
 *     },
 * )
 * ```
 *
 * @param debounceMs Interval during which repeated clicks are ignored. Must be zero or greater.
 *                   Defaults to [DEFAULT_CLICK_DEBOUNCE_MS].<br><br>
 *                   중복 클릭을 무시할 간격입니다. 0 이상이어야 하며 기본값은
 *                   [DEFAULT_CLICK_DEBOUNCE_MS]입니다.<br>
 * @param enabled Whether the click is enabled.<br><br>
 *                클릭 활성화 여부입니다.<br>
 * @param onClick Action executed when a click is accepted.<br><br>
 *                클릭이 허용되었을 때 실행할 동작입니다.<br>
 * @throws IllegalArgumentException when [debounceMs] is negative.<br><br>
 *                                  [debounceMs]가 음수이면 발생합니다.<br>
 */
public fun Modifier.debouncedClickable(
    debounceMs: Long = DEFAULT_CLICK_DEBOUNCE_MS,
    enabled: Boolean = true,
    onClick: () -> Unit,
): Modifier = composed(
    inspectorInfo = debugInspectorInfo {
        name = "debouncedClickable"
        properties["debounceMs"] = debounceMs
        properties["enabled"] = enabled
    },
) {
    require(debounceMs >= 0) { "debounceMs must be >= 0, but was $debounceMs" }
    val debouncer = remember(debounceMs) { ClickDebouncer(debounceMs) }

    clickable(enabled = enabled) {
        if (debouncer.tryAccept(System.currentTimeMillis())) {
            onClick()
        }
    }
}

/**
 * 중복 클릭을 무시하는 클릭 핸들러를 생성합니다.<br>
 * Creates a click handler that ignores repeated clicks.<br>
 *
 * [debouncedClickable]을 사용할 수 없는 호출부(외부 컴포넌트의 `onClick` 파라미터 등)에서
 * 동일한 차단 규칙을 적용할 때 사용합니다.<br>
 * Use this when the same debounce rule is needed at a call site that cannot use
 * [debouncedClickable], such as an external component's `onClick` parameter.<br>
 *
 * **사용 예시 / Usage example**:
 * ```kotlin
 * val onSubmit = rememberDebouncedClick { viewModel.submit() }
 * ExternalButton(onClick = onSubmit)
 * ```
 *
 * @param debounceMs Interval during which repeated clicks are ignored. Must be zero or greater.
 *                   Defaults to [DEFAULT_CLICK_DEBOUNCE_MS].<br><br>
 *                   중복 클릭을 무시할 간격입니다. 0 이상이어야 하며 기본값은
 *                   [DEFAULT_CLICK_DEBOUNCE_MS]입니다.<br>
 * @param onClick Action executed when a click is accepted.<br><br>
 *                클릭이 허용되었을 때 실행할 동작입니다.<br>
 * @return Click handler that applies the debounce rule.<br><br>
 *         차단 규칙이 적용된 클릭 핸들러입니다.<br>
 * @throws IllegalArgumentException when [debounceMs] is negative.<br><br>
 *                                  [debounceMs]가 음수이면 발생합니다.<br>
 */
@Composable
public fun rememberDebouncedClick(
    debounceMs: Long = DEFAULT_CLICK_DEBOUNCE_MS,
    onClick: () -> Unit,
): () -> Unit {
    require(debounceMs >= 0) { "debounceMs must be >= 0, but was $debounceMs" }
    val debouncer = remember(debounceMs) { ClickDebouncer(debounceMs) }

    return remember(debouncer, onClick) {
        {
            if (debouncer.tryAccept(System.currentTimeMillis())) {
                onClick()
            }
        }
    }
}
