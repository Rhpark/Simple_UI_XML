# 라이프사이클 규칙 (Life Cycle Rule)

## 메모리 누수 방지

- Activity / View / Context를 static 또는 장기 보유 객체에 저장 금지
- 뷰 참조는 onDestroyView()에서 null 처리

## 코루틴 스코프

- GlobalScope 사용 금지
- ViewModel → viewModelScope 사용
- Activity / Fragment → lifecycleScope 사용
- Fragment 뷰 관찰 → viewLifecycleOwner.lifecycleScope 사용

## 코루틴 취소 처리

- CancellationException은 catch하지 않거나 반드시 rethrow
- Job 취소 시 자식 코루틴이 함께 취소되는지 확인

## 심각도 기준

- CRITICAL: 메모리 누수 (Activity/View Context 수명 누수)
- CRITICAL: GlobalScope 사용 (라이프사이클 위반)
- CRITICAL: 코루틴 누수 / 미취소
- HIGH: Fragment에서 viewLifecycleOwner 미사용

## 예시

### GlobalScope 사용 (라이프사이클 위반)

❌ BAD
```kotlin
class PaymentViewModel : ViewModel() {
    fun load() {
        GlobalScope.launch { repository.fetch() }
    }
}
```

✅ GOOD
```kotlin
class PaymentViewModel : ViewModel() {
    fun load() {
        viewModelScope.launch { repository.fetch() }
    }
}
```

---

### Fragment에서 잘못된 lifecycle owner

❌ BAD
```kotlin
class PaymentFragment : Fragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        lifecycleScope.launch {
            viewModel.uiState.collect { render(it) }
        }
    }
}
```

✅ GOOD
```kotlin
class PaymentFragment : Fragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { render(it) }
        }
    }
}
```
