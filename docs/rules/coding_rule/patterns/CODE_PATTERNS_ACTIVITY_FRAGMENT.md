# Activity / Fragment 패턴 규칙

## 규칙

### 베이스 클래스 상속
- Activity → `BaseActivity` 또는 `BaseDataBindingActivity` 상속 (순수 AppCompatActivity 직접 상속 금지)
- Fragment → `BaseFragment` 또는 `BaseDataBindingFragment` 상속
- DialogFragment → `BaseDataBindingDialogFragment` 상속
- MVVM + DataBinding 사용 시 → `BaseDataBindingActivity` / `BaseDataBindingFragment` 선택

### ViewModel 생성
- `ViewModelProvider(this).get(...)` 직접 사용 금지 → `getViewModel()` 사용

### 이벤트 수집
- `onEventVmCollect(binding)`에서 Flow 수집
- `repeatOnLifecycle(Lifecycle.State.STARTED)` 필수 (`repeatOnLifecycle` 없이 Flow 직접 수집 금지)
- `lifecycleScope.launch`는 `repeatOnLifecycle` 블록을 감싸는 용도로만 사용

### 권한 요청
- `registerForActivityResult` 직접 사용 금지 → `requestPermissions()` 사용

### SystemBar 제어
- `SystemBarController(window)` 직접 생성 금지 → `window.getSystemBarController()` 사용
- 캐시 정리는 Activity `onDestroy()`에서 `window.destroySystemBarControllerCache()` 1회 처리

## 베이스 클래스 선택 기준

| 요구사항 | 사용 클래스 |
|---------|-----------|
| 가장 가벼운 Activity/Fragment | `BaseActivity`, `BaseFragment` |
| DataBinding + MVVM | `BaseDataBindingActivity`, `BaseDataBindingFragment` |
| DialogFragment + Binding | `BaseDataBindingDialogFragment` |

## 심각도 기준

- HIGH: 베이스 클래스 미상속 (AppCompatActivity 직접 상속)
- HIGH: `repeatOnLifecycle` 없이 Flow 직접 수집 (중복 이벤트 위험)
- MEDIUM: `getViewModel()` 미사용, `requestPermissions()` 미사용

## 예시

### ViewModel 생성 및 이벤트 수집

❌ BAD
```kotlin
class MainActivity : AppCompatActivity() {
    private val vm: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            vm.eventFlow.collect { event -> handleEvent(event) } // ❌ 중복 수집 위험
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

    override fun onEventVmCollect(binding: ActivityMainBinding) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) { // ✅ 안전한 수집
                vm.eventFlow.collect { event -> handleEvent(event) }
            }
        }
    }
}
```

---

### 권한 요청

❌ BAD
```kotlin
val launcher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { ... }
launcher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
```

✅ GOOD
```kotlin
requestPermissions(
    permissions = listOf(Manifest.permission.ACCESS_FINE_LOCATION),
    onDeniedResult = { deniedResults ->
        if (deniedResults.isEmpty()) startLocationTracking()
    }
)
```

---

### SystemBar 제어

❌ BAD
```kotlin
val controller = SystemBarController(window)
```

✅ GOOD
```kotlin
val controller = window.getSystemBarController()

override fun onDestroy() {
    window.destroySystemBarControllerCache()
    super.onDestroy()
}
```
