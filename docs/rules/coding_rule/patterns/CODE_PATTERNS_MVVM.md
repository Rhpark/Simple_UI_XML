# MVVM 패턴 규칙

## 규칙

- ViewModel에서 이벤트용 Channel/Flow 직접 구성 금지 → `BaseViewModelEvent` 상속 + `sendEventVm()` 사용
- 이벤트 전송 시 `viewModelScope.launch { _events.send(...) }` 직접 사용 금지 → `sendEventVm()` 사용
- Channel `close()` 수동 호출 금지 → `BaseViewModelEvent`가 자동 해제
- Activity/Fragment에서 이벤트 수집 함수명 자의적 작성 금지 → `onEventVmCollect(binding)` 오버라이드
- Fragment에서 `_binding = null` 수동 처리 금지 → `BaseDataBindingFragment` 자동 처리

> Activity/Fragment 기본 규칙은 docs/rules/coding_rule/patterns/CODE_PATTERNS_ACTIVITY_FRAGMENT.md 참조
> UIState(StateFlow) 규칙은 docs/rules/coding_rule/CODE_ARCHITECTURE.md 기준을 따른다.

## 심각도 기준

- HIGH: 이벤트용 Channel/Flow 직접 구성 (`_events = Channel(...)` 직접 선언)
- HIGH: `onCleared()`에서 `_events.close()` 수동 호출 (BaseViewModelEvent 미사용 시 누락 위험)
- MEDIUM: 이벤트 수집 함수명 비표준화 (`setupObservers()` 등 자의적 함수명 사용)

## 예시

### ViewModel 이벤트 시스템

❌ BAD
```kotlin
class MainViewModel : ViewModel() {
    private val _events = Channel<MainEvent>(Channel.BUFFERED) // ❌ 직접 구성
    val events: Flow<MainEvent> = _events.receiveAsFlow()

    fun onButtonClick() {
        viewModelScope.launch {
            _events.send(MainEvent.ShowMessage("Hello")) // ❌ 직접 send
        }
    }

    override fun onCleared() {
        super.onCleared()
        _events.close() // ❌ 수동 해제 (누락 위험)
    }
}
```

✅ GOOD
```kotlin
class MainViewModel : BaseViewModelEvent<MainEvent>() { // ✅ 상속
    fun onButtonClick() {
        viewModelScope.launch {
            sendEventVm(MainEvent.ShowMessage("Hello")) // ✅ 한 줄
        }
    }
    // 채널 자동 해제 ✅
}

sealed class MainEvent {
    data class ShowMessage(val message: String) : MainEvent()
    data class UpdateCounter(val count: Int) : MainEvent()
}
```

---

### 이벤트 수집

❌ BAD
```kotlin
class MainActivity : BaseDataBindingActivity<ActivityMainBinding>(R.layout.activity_main) {
    override fun onCreate(binding: ActivityMainBinding, savedInstanceState: Bundle?) {
        setupObservers() // ❌ 비표준 함수명
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            vm.events.collect { event -> handleEvent(event) } // ❌ repeatOnLifecycle 없음
        }
    }
}
```

✅ GOOD
```kotlin
class MainActivity : BaseDataBindingActivity<ActivityMainBinding>(R.layout.activity_main) {
    private val vm: MainViewModel by lazy { getViewModel() }

    override fun onCreate(binding: ActivityMainBinding, savedInstanceState: Bundle?) {
        binding.vm = vm
    }

    // ✅ 표준화된 함수명, BaseDataBindingActivity가 자동 호출
    override fun onEventVmCollect(binding: ActivityMainBinding) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) { // ✅ 중복 수집 방지
                vm.eventVmFlow.collect { event ->
                    when (event) {
                        is MainEvent.ShowMessage -> binding.root.snackBarShowShort(event.message)
                        is MainEvent.UpdateCounter -> binding.tvCounter.text = event.count.toString()
                    }
                }
            }
        }
    }
}
```
