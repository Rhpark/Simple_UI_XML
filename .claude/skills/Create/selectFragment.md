# selectFragment

Fragment 생성 시 타입별 규칙을 정의합니다.
SKILL.md의 공통 규칙과 함께 적용합니다.

> AndroidManifest 등록 불필요: Fragment는 Manifest에 등록하지 않는다.

---

## 생성 산출물

| 파일 | 경로 |
|------|------|
| Fragment | `app/src/main/java/{package}/{ClassName}.kt` |
| Layout | `app/src/main/res/layout/fragment_{name}.xml` |
| ViewModel (선택) | `app/src/main/java/{package}/{ClassName}Vm.kt` |
| VmEvent (선택) | `app/src/main/java/{package}/{ClassName}VmEvent.kt` |

---

## Q1 — Fragment 베이스 선택

1. `BaseFragment`
2. `BaseDataBindingFragment`
3. `BaseViewBindingFragment`

> Q1 결과로 UI 방식이 결정된다. ViewBinding/DataBinding/findViewById는 별도 질문 없이 Q1 선택을 따른다.

---

## Layout name 규칙
- `Fragment` suffix 제거 후 snake_case → 접두사 `fragment_`
- 예) `MyFragment` → `fragment_my.xml`, `FooBarListFragment` → `fragment_foo_bar_list.xml`

---

## UI 방식 결정

| Q1 선택 | UI 방식 |
|--------|--------|
| `BaseDataBindingFragment` | DataBinding |
| `BaseViewBindingFragment` | ViewBinding |
| `BaseFragment` | findViewById (`getRootView()` 사용) |

---

## Fragment 작성 규칙
> 참조: `docs/rules/coding_rule/patterns/CODE_PATTERNS_ACTIVITY_FRAGMENT.md`
> 참조: `docs/rules/coding_rule/patterns/CODE_PATTERNS_MVVM.md`

- 불필요한 샘플 로직(토스트/로그 남발) 금지

#### ViewModel 선언
> **파일 생성 전 반드시 `app/build.gradle.kts`를 읽어 `fragment-ktx` 포함 여부를 확인한다.**

| `fragment-ktx` 포함 여부 | 선언 패턴 |
|------------------------|---------|
| 있음 (`fragment-ktx` 명시) | `private val vm: {ClassName}Vm by viewModels()` |
| 없음 | `private val vm: {ClassName}Vm by lazy { getViewModel() }` |

#### 생성자 패턴

| 베이스 클래스 | 생성자 패턴 |
|-------------|-----------|
| `BaseFragment` | `BaseFragment(R.layout.fragment_my)` |
| `BaseDataBindingFragment` | `BaseDataBindingFragment<FragmentMyBinding>(R.layout.fragment_my)` |
| `BaseViewBindingFragment` | `BaseViewBindingFragment<FragmentMyBinding>(FragmentMyBinding::inflate)` |

#### 콜백 패턴

**`BaseFragment`** — `onCreateView(rootView, savedInstanceState)` 필수 오버라이드:
```kotlin
override fun onCreateView(rootView: View, savedInstanceState: Bundle?) {
    // getRootView().findViewById<TextView>(R.id.tvTitle)
}
```

**`BaseDataBindingFragment`** — `onCreateView(binding, savedInstanceState)` 오버라이드:
```kotlin
override fun onCreateView(binding: FragmentMyBinding, savedInstanceState: Bundle?) {
    super.onCreateView(binding, savedInstanceState)  // lifecycleOwner 자동 설정
    binding.vm = vm
    lifecycle.addObserver(vm)  // BaseViewModel / BaseViewModelEvent 선택 시에만
}
```

**`BaseViewBindingFragment`** — `onViewCreated(binding, savedInstanceState)` 오버라이드:
```kotlin
override fun onViewCreated(binding: FragmentMyBinding, savedInstanceState: Bundle?) {
    lifecycle.addObserver(vm)  // BaseViewModel / BaseViewModelEvent 선택 시에만
}
```

> `lifecycle.addObserver(vm)`: Q2에서 `BaseViewModel` 또는 `BaseViewModelEvent` 선택 시에만 추가.

#### onEventVmCollect 패턴 (BaseViewModelEvent 선택 시)

**`BaseDataBindingFragment` / `BaseViewBindingFragment`**:
```kotlin
override fun onEventVmCollect(binding: FragmentMyBinding) {
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

> `lifecycleScope` 대신 반드시 `viewLifecycleOwner.lifecycleScope`를 사용한다.

**`BaseFragment`** — `onCreateView` 내부에서 수집:
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
- `FragmentContainerView` 설정 방법
- 호출 측 `supportFragmentManager.commit { replace(...) }` 예시
