# selectActivity

Activity 생성 시 타입별 규칙을 정의합니다.
SKILL.md의 공통 규칙과 함께 적용합니다.

---

## 생성 산출물

| 파일 | 경로 |
|------|------|
| Activity | `app/src/main/java/{package}/{ClassName}.kt` |
| Layout | `app/src/main/res/layout/activity_{name}.xml` |
| ViewModel (선택) | `app/src/main/java/{package}/{ClassName}Vm.kt` |
| VmEvent (선택) | `app/src/main/java/{package}/{ClassName}VmEvent.kt` |

---

## Q1 — Activity 베이스 선택

1. `BaseActivity`
2. `BaseDataBindingActivity`
3. `BaseViewBindingActivity`
4. `AppCompatActivity`

> Q1 결과로 UI 방식이 결정된다. ViewBinding/DataBinding/findViewById는 별도 질문 없이 Q1 선택을 따른다.

---

## Q3 — Manifest 등록 및 진입 방식

### 3-1) 등록 여부
1. 등록 안함
2. 등록함

### 3-2) 진입 방식 (2번 선택 시에만)
1. 일반 (다른 화면에서 Intent로 진입)
2. LAUNCHER (앱 시작 화면)
3. 딥링크 (scheme/host/pathPrefix 등 필요)
4. 특수 launchMode/task 설정 필요

> 규칙:
- "딥링크" 선택 시 추가로 1회 질문을 허용하여 scheme/host/pathPrefix 파라미터를 수집한다.
- "특수 launchMode/task 설정" 선택 시 추가로 1회 질문을 허용하여 필요한 항목(launchMode/taskAffinity/flags)을 확정한다.
- "LAUNCHER"는 위험도가 높으므로, 기존 LAUNCHER가 존재하면 자동 교체하지 않고 중단 + 안내한다.
- "등록 안함"이면 `app/src/main/AndroidManifest.xml`은 절대 수정하지 않는다.

---

## Layout name 규칙
- `Activity` suffix 제거 후 snake_case → 접두사 `activity_`
- 예) `MyActivity` → `activity_my.xml`, `FooBarActivity` → `activity_foo_bar.xml`

---

## UI 방식 결정

| Q1 선택 | UI 방식 |
|--------|--------|
| `BaseDataBindingActivity` | DataBinding |
| `BaseViewBindingActivity` | ViewBinding |
| `BaseActivity` / `AppCompatActivity` | findViewById (기본값) |

---

## Activity 작성 규칙
> 참조: `docs/rules/coding_rule/patterns/CODE_PATTERNS_ACTIVITY_FRAGMENT.md`
> 참조: `docs/rules/coding_rule/patterns/CODE_PATTERNS_MVVM.md`

- 불필요한 샘플 로직(토스트/로그 남발) 금지

#### ViewModel 선언
- `androidx.activity:activity-ktx` 사용 시: `private val vm: {ClassName}Vm by viewModels()`
- 미사용 시: `private val vm: {ClassName}Vm by lazy { getViewModel() }`

#### 생성자 패턴

| 베이스 클래스 | 생성자 패턴 | setContentView |
|-------------|-----------|---------------|
| `BaseActivity` | `BaseActivity(R.layout.activity_my)` | 자동 |
| `BaseDataBindingActivity` | `BaseDataBindingActivity<ActivityMyBinding>(R.layout.activity_my)` | 자동 |
| `BaseViewBindingActivity` | `BaseViewBindingActivity<ActivityMyBinding>(ActivityMyBinding::inflate)` | 자동 |
| `AppCompatActivity` | 생성자 파라미터 없음 | `onCreate`에서 수동 필수 |

#### onCreate 패턴

**`BaseDataBindingActivity`**:
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    getBinding().vm = vm       // DataBinding 변수 연결
    lifecycle.addObserver(vm)  // BaseViewModel / BaseViewModelEvent 선택 시에만
}
```

**`BaseViewBindingActivity`**:
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    lifecycle.addObserver(vm)  // BaseViewModel / BaseViewModelEvent 선택 시에만
}
```

**`BaseActivity`**:
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    lifecycle.addObserver(vm)  // BaseViewModel / BaseViewModelEvent 선택 시에만
}
```

**`AppCompatActivity`**:
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_my)
    lifecycle.addObserver(vm)  // BaseViewModel / BaseViewModelEvent 선택 시에만
}
```

> `lifecycle.addObserver(vm)`: Q2에서 `BaseViewModel` 또는 `BaseViewModelEvent` 선택 시에만 추가.

#### onEventVmCollect 패턴 (BaseViewModelEvent 선택 시)

**`BaseDataBindingActivity` / `BaseViewBindingActivity`**:
```kotlin
override fun onEventVmCollect(binding: ActivityMyBinding) {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) { // 권장
            vm.eventVmFlow.collect { event -> when (event) { } }
        }
    }
}
```

**`BaseActivity` / `AppCompatActivity`** — `onCreate` 내부에서 수집:
```kotlin
lifecycleScope.launch {
    repeatOnLifecycle(Lifecycle.State.STARTED) { // 권장
        vm.eventVmFlow.collect { event -> when (event) { } }
    }
}
```

---

## AndroidManifest 처리 규칙
- "등록함" 선택 시 프로젝트의 기존 activity name 표기 스타일을 따른다.
- LAUNCHER: 기존 LAUNCHER가 있으면 중단 + 안내, 없다면 MAIN/LAUNCHER intent-filter 추가
- 딥링크: 추가 질문으로 수집한 파라미터로 intent-filter 추가
- 특수 설정: 추가 질문으로 확정한 항목 적용

---

## 다음 단계 안내
- 진입 경로 연결 (호출 측 Intent 구성)
- 딥링크 선택 시: 딥링크 테스트 방법 안내
