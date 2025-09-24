package kr.open.library.simple_ui.system_manager.base

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * StateFlow-based data update utility that efficiently tracks changes to data values.
 * Only emits new values when they differ from the previous value, providing automatic
 * distinctUntilChanged behavior with thread safety.
 *
 * StateFlow 기반 데이터 업데이트 유틸리티로 데이터 값의 변경을 효율적으로 추적합니다.
 * 이전 값과 다를 때만 새 값을 방출하여 자동 중복 제거 기능과 스레드 안전성을 제공합니다.
 *
 * @param TYPE The type of data being tracked
 * @param initialValue The initial value to start with
 */
internal class DataUpdate<TYPE>(initialValue: TYPE) {

    private val _state = MutableStateFlow(initialValue)
    
    /**
     * StateFlow that emits the current value and any subsequent changes.
     * Automatically filters out duplicate consecutive values.
     * 현재 값과 이후 변경사항을 방출하는 StateFlow입니다.
     * 연속된 중복 값을 자동으로 필터링합니다.
     */
    public val state: StateFlow<TYPE> = _state.asStateFlow()

    /**
     * Gets the current value without subscribing to changes
     * 변경 사항을 구독하지 않고 현재 값을 가져옵니다.
     */
    public val currentValue: TYPE
        get() = _state.value

    /**
     * Updates the data value. Only emits if the new value differs from the current value.
     * Thread-safe operation that automatically handles concurrent access.
     *
     * 데이터 값을 업데이트합니다. 새 값이 현재 값과 다를 때만 방출합니다.
     * 동시 접근을 자동으로 처리하는 스레드 안전 작업입니다.
     *
     * @param newValue The new value to set
     */
    public fun update(newValue: TYPE) {
        _state.value = newValue
    }
}