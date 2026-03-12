# selectDialogFragment

DialogFragment 생성 시 타입별 규칙을 정의합니다.
SKILL.md의 공통 규칙과 함께 적용합니다.

> AndroidManifest 등록 불필요: DialogFragment는 Manifest에 등록하지 않는다.

---

## 생성 산출물

| 파일 | 경로 |
|------|------|
| DialogFragment | `app/src/main/java/{package}/{ClassName}.kt` |
| Layout | `app/src/main/res/layout/dialog_{name}.xml` |
| ViewModel (선택) | `app/src/main/java/{package}/{ClassName}Vm.kt` |
| VmEvent (선택) | `app/src/main/java/{package}/{ClassName}VmEvent.kt` |

---

## Q1 — DialogFragment 베이스 선택

1. `BaseDialogFragment`
2. `BaseDataBindingDialogFragment`
3. `BaseViewBindingDialogFragment`

> Q1 결과로 UI 방식이 결정된다. ViewBinding/DataBinding/findViewById는 별도 질문 없이 Q1 선택을 따른다.

---

## Layout name 규칙
- `DialogFragment` suffix 제거 후 snake_case → 접두사 `dialog_`
- 예) `MyDialogFragment` → `dialog_my.xml`, `ConfirmDialogFragment` → `dialog_confirm.xml`

---

## UI 방식 결정

| Q1 선택 | UI 방식 |
|--------|--------|
| `BaseDataBindingDialogFragment` | DataBinding |
| `BaseViewBindingDialogFragment` | ViewBinding |
| `BaseDialogFragment` | findViewById (`getRootView()` 사용) |

---

## DialogFragment 작성 규칙
> 참조: `docs/rules/coding_rule/patterns/CODE_PATTERNS_ACTIVITY_FRAGMENT.md`
> 참조: `docs/rules/coding_rule/patterns/CODE_PATTERNS_MVVM.md`

- 불필요한 샘플 로직(토스트/로그 남발) 금지

#### Dialog 전용 API
- `resizeDialog(widthRatio: Float, heightRatio: Float?)` — 다이얼로그 크기 설정 (예: `resizeDialog(0.8f, null)`)
- `safeDismiss()` — 안전한 닫기 (직접 `dismiss()` 호출 금지)
- 두 API 모두 `onViewCreated` 이후에서만 호출 가능

#### ViewModel 선언
> **파일 생성 전 반드시 `app/build.gradle.kts`를 읽어 `fragment-ktx` 포함 여부를 확인한다.**

| `fragment-ktx` 포함 여부 | 선언 패턴 |
|------------------------|---------|
| 있음 (`fragment-ktx` 명시) | `private val vm: {ClassName}Vm by viewModels()` |
| 없음 | `private val vm: {ClassName}Vm by lazy { getViewModel() }` |

#### 생성자 패턴

| 베이스 클래스 | 생성자 패턴 |
|-------------|-----------|
| `BaseDialogFragment` | `BaseDialogFragment(R.layout.dialog_my)` |
| `BaseDataBindingDialogFragment` | `BaseDataBindingDialogFragment<DialogMyBinding>(R.layout.dialog_my)` |
| `BaseViewBindingDialogFragment` | `BaseViewBindingDialogFragment<DialogMyBinding>(DialogMyBinding::inflate)` |

#### 콜백 패턴

**`BaseDialogFragment`** — `onViewCreated(view, savedInstanceState)` 오버라이드:
```kotlin
override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    resizeDialog(0.8f, null)  // 필요 시
    // getRootView().findViewById<Button>(R.id.btnOk).setOnClickListener { safeDismiss() }
}
```

**`BaseDataBindingDialogFragment`** — `onBindingCreated(binding, savedInstanceState)` + `onViewCreated(binding, savedInstanceState)` 오버라이드:
```kotlin
override fun onBindingCreated(binding: DialogMyBinding, savedInstanceState: Bundle?) {
    binding.vm = vm  // 바인딩 변수 할당만. viewLifecycleOwner 접근 금지
    // binding.btnOk.setOnClickListener { safeDismiss() }
}

override fun onViewCreated(binding: DialogMyBinding, savedInstanceState: Bundle?) {
    lifecycle.addObserver(vm)  // BaseViewModel / BaseViewModelEvent 선택 시에만
    resizeDialog(0.8f, null)  // 필요 시
}
```

**`BaseViewBindingDialogFragment`** — `onViewCreated(binding, savedInstanceState)` 오버라이드:
```kotlin
override fun onViewCreated(binding: DialogMyBinding, savedInstanceState: Bundle?) {
    lifecycle.addObserver(vm)  // BaseViewModel / BaseViewModelEvent 선택 시에만
    resizeDialog(0.8f, null)  // 필요 시
    // binding.btnOk.setOnClickListener { safeDismiss() }
}
```

> `lifecycle.addObserver(vm)`: Q2에서 `BaseViewModel` 또는 `BaseViewModelEvent` 선택 시에만 추가.

#### onEventVmCollect 패턴 (BaseViewModelEvent 선택 시)

**`BaseDataBindingDialogFragment` / `BaseViewBindingDialogFragment`**:
```kotlin
override fun onEventVmCollect(binding: DialogMyBinding) {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) { // 권장
            vm.eventVmFlow.collect { event ->
                when (event) {
                    is {ClassName}VmEvent.Dump -> { }
                }
            }
        }
    }
}
```

**`BaseDialogFragment`** — `onViewCreated` 내부에서 수집:
```kotlin
viewLifecycleOwner.lifecycleScope.launch {
    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) { // 권장
        vm.eventVmFlow.collect { event ->
            when (event) {
                is {ClassName}VmEvent.Dump -> { }
            }
        }
    }
}
```

---

## 다음 단계 안내
- 호출 측 `show(childFragmentManager, tag)` 사용 방법
- `resizeDialog` 비율 조정 방법 안내
