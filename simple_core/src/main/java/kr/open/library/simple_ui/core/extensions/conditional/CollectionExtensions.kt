/**
 * Conditional collection helpers that keep fluent chains tidy.<br><br>
 * 조건 기반 컬렉션 연산을 간결하게 이어 갈 수 있게 해 주는 확장 모음입니다.
 */
package kr.open.library.simple_ui.core.extensions.conditional

/**
 * Applies [predicate] only when [condition] is `true`, otherwise returns the original list.<br><br>
 * [condition]이 `true`일 때만 [predicate]를 적용하고, 그렇지 않으면 원본 리스트를 그대로 돌려줍니다.<br>
 *
 * @param condition Boolean flag that decides whether filtering should run.<br><br>
 *        필터를 수행할지 여부를 판단하는 불리언 조건입니다.
 * @param predicate Filtering rule applied when [condition] is `true`.<br><br>
 *        조건이 충족될 때 실행할 필터링 람다입니다.
 * @return Filtered list when [condition] is `true`, otherwise `this`.<br><br>
 *         조건이 참이면 필터링된 리스트, 거짓이면 현재 리스트를 그대로 반환합니다.<br>
 */
public inline fun <T> List<T>.filterIf(condition: Boolean, predicate: (T) -> Boolean, ): List<T> =
    if (condition) filter(predicate) else this

/**
 * Runs [action] only when the list is not empty and keeps fluent chains intact.<br><br>
 * 리스트가 비어 있지 않을 때만 [action]을 실행해 체이닝 흐름을 유지합니다.
 *
 * @param action Callback that receives the non-empty list.<br><br>
 *        비어 있지 않은 리스트를 전달받는 콜백입니다.
 * @return Always returns `this` to allow further chaining.<br><br>
 *         후속 연산을 위해 항상 현재 리스트를 그대로 돌려줍니다.
 */
public inline fun <T> List<T>.ifNotEmpty(action: (List<T>) -> Unit): List<T> {
    if (isNotEmpty()) action(this)
    return this
}

/**
 * Runs [action] only when the list is empty and keeps fluent chains intact.<br><br>
 * 리스트가 비어 있을 때만 [action]을 실행해 체이닝 흐름을 유지합니다.
 *
 * @param action Callback executed when the list is empty.<br><br>
 *        리스트가 비었을 때 실행할 콜백입니다.
 * @return Always returns `this` to allow further chaining.<br><br>
 *         후속 연산을 위해 항상 현재 리스트를 그대로 돌려줍니다.
 */
public inline fun <T> List<T>.ifEmpty(action: () -> Unit): List<T> {
    if (isEmpty()) action()
    return this
}

/**
 * Runs [action] only when the map is not empty and keeps fluent chains intact.<br><br>
 * 맵이 비어 있지 않을 때만 [action]을 실행해 체이닝 흐름을 유지합니다.
 *
 * @param action Callback that receives the non-empty map.<br><br>
 *        비어 있지 않은 맵을 전달받는 콜백입니다.
 * @return Always returns `this` to allow further chaining.<br><br>
 *         후속 연산을 위해 항상 현재 맵을 그대로 돌려줍니다.
 */
public inline fun <K, V> Map<K, V>.ifNotEmpty(action: (Map<K, V>) -> Unit): Map<K, V> {
    if (isNotEmpty()) action(this)
    return this
}

/**
 * Runs [action] only when the map is empty and keeps fluent chains intact.<br><br>
 * 맵이 비어 있을 때만 [action]을 실행해 체이닝 흐름을 유지합니다.
 *
 * @param action Callback executed when the map is empty.<br><br>
 *        맵이 비었을 때 실행할 콜백입니다.
 * @return Always returns `this` to allow further chaining.<br><br>
 *         후속 연산을 위해 항상 현재 맵을 그대로 돌려줍니다.
 */
public inline fun <K, V> Map<K, V>.ifEmpty(action: (Map<K, V>) -> Unit): Map<K, V> {
    if (isEmpty()) action(this)
    return this
}
