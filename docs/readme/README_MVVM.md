# 📱 Simple UI MVVM Pattern – Complete Guide
> **Simple UI MVVM 패턴 - 가이드**

## 📦 Module Information (모듈 정보)

This feature **spans two modules** (이 기능은 **두 모듈**에 걸쳐 있습니다):

### **simple_core** - ViewModel Base Classes (ViewModel 베이스 클래스)
- **Package**: `kr.open.library.simple_ui.core.viewmodel.*`
- **Provides**: BaseViewModel, BaseViewModelEvent
- **Purpose**: UI-independent ViewModel logic and event system

### **simple_xml** - MVVM Integration (MVVM 통합)
- **Package**: `kr.open.library.simple_ui.xml.ui.*`
- **Provides**: `BaseDataBindingActivity`, `BaseDataBindingFragment`, `BaseDataBindingDialogFragment`, `BaseViewBindingActivity`, `BaseViewBindingFragment`, `BaseViewBindingDialogFragment`
- **Purpose**: Seamless DataBinding/ViewBinding + ViewModel connection

<br></br>

### Activity + ViewModel Quick Setup (Activity + ViewModel 초기 설정)
![mvvm_activity_init.gif](../../example_gif/mvvm_activity_init.gif)

### Activity + ViewModel Immediate Usage (Activity + ViewModel 초기 사용)
![mvvm_activity_vm_init_2.gif](../../example_gif/mvvm_activity_vm_init_2.gif)

### ViewModel Event System (ViewModel 이벤트 시스템)
![mvvm_vm.png](../../example_gif/mvvm_vm.png)

**"Review an MVVM setup example condensed to around 10 lines."** Compare the setup flow against a classic Activity/Fragment + ViewModel implementation.
> **"MVVM 세팅 예제를 10줄 안팎으로 정리해 보자!"** 기존 Activity/Fragment + ViewModel 구현과 설정 흐름을 비교해 보세요.

<br>
</br>

## 🔎 At a Glance (한눈 비교)

<br>
</br>

### Activity/Fragment + ViewModel Initialization

| Category (항목) | Plain Android (기본 Android) | Simple UI (심플 UI) |
|:--------------------------|:------------------------------------------:|:--------------------------------------:|
| DataBinding setup (DataBinding 설정) | Manual inflate + setContentView (7+ lines)<br>수동 inflate + setContentView (7줄 이상) | ✅ Automatic via constructor parameters<br>생성자 파라미터로 자동 처리 |
| LifecycleOwner assignment (LifecycleOwner 연결) | Manually set `binding.lifecycleOwner`<br>`binding.lifecycleOwner` 수동 지정 | ✅ Automatically wired<br>자동 연결 |
| ViewModel binding (ViewModel 바인딩) | Manually assign `binding.viewModel`<br>`binding.viewModel` 수동 할당 | ✅ Optional, streamlined usage<br>선택적이고 간결한 사용 |
| `onCreate` boilerplate (`onCreate` 보일러플레이트) | Complex initialization code<br>복잡한 초기화 코드 | ✅ Minimal code<br>최소한의 코드 |

<br>
</br>

### ViewModel Event System
| Category (항목) | Plain Android (기본 Android) | Simple UI (심플 UI) |
|:--|:--:|:--:|
| Event channel setup (이벤트 채널 구성) | Manually wire Flow/Channel (10+ lines)<br>Flow/Channel 수동 구성 (10줄 이상) | Automatically handled by `BaseViewModelEvent` ✅<br>`BaseViewModelEvent`가 자동 처리 |
| Event dispatch (이벤트 전송) | `viewModelScope.launch` + `send`<br>`viewModelScope.launch` + `send` | Single-line `sendEventVm()` ✅<br>`sendEventVm()` 한 줄 |
| Channel resource management (채널 리소스 관리) | Manually call `close()`<br>`close()` 수동 호출 | Managed automatically ✅<br>자동 관리 |
| Event collection (이벤트 수집) | Manually launch with `lifecycleScope`<br>`lifecycleScope`로 수동 실행 | Override `onEventVmCollect(binding)` ✅ (automatically called)<br>`onEventVmCollect(binding)` 오버라이드 ✅ (자동 호출됨) |

**Key takeaway:** Simple UI boosts development speed through **automation of complex MVVM boilerplate**.
> **핵심:** Simple UI는 "복잡한 MVVM 보일러플레이트"의 **자동화**를 통해 개발 속도를 향상시킵니다.

<br>
</br>

## 💡 Why Simple UI MVVM Matters (왜 Simple UI MVVM이 중요한가)

- **Shorter development time:** Remove ViewModel boilerplate so you can focus on core logic.
- **Fewer mistakes:** Prevent bugs around ViewModel binding and event-channel setup.
- **Consistent patterns:** Keep the entire team aligned on the same MVVM structure.
- **Maintainability:** Standardize events through `BaseViewModelEvent`.
- **Rapid prototyping:** Turn ideas into working tests immediately.
> - **개발 시간 단축**: ViewModel 연동 보일러플레이트 제거로 핵심 로직에 집중 가능
> - **실수 방지**: ViewModel 바인딩, 이벤트 채널 구성 등에서 발생하는 버그 예방
> - **일관된 패턴**: 팀 전체가 동일한 MVVM 구조 사용
> - **유지보수성**: BaseViewModelEvent로 표준화된 이벤트 시스템
> - **빠른 프로토타이핑**: 아이디어를 바로 구현하여 테스트 가능

<br>
</br>

## 📦 Before You Begin (시작하기 전에)

To leverage Simple UI’s MVVM features, first choose between the **DataBinding path** and the **ViewBinding path**, then understand the base classes you will use.
> Simple UI의 MVVM 기능을 사용하려면 먼저 **DataBinding 경로**와 **ViewBinding 경로** 중 하나를 선택하고, 사용할 Base 클래스를 이해해야 합니다.

<br>

### ✅ **Check This First! (먼저 확인하세요!)**

📌 **Unsure about the basic Activity/Fragment flow?**  
Visit [README_ACTIVITY_FRAGMENT.md](README_ACTIVITY_FRAGMENT.md) and review:
- Required setup (`dataBinding = true` or `viewBinding = true`)
- Basics of DataBinding/ViewBinding base classes
- Common troubleshooting steps
> **Activity/Fragment 기본 사용법을 모르시나요?**  
> → [README_ACTIVITY_FRAGMENT.md](README_ACTIVITY_FRAGMENT.md)에서 다음 내용을 먼저 확인하세요:
> - 필수 설정 (`dataBinding = true` 또는 `viewBinding = true`)
> - DataBinding/ViewBinding Base 클래스 기본 사용법
> - 자주 발생하는 오류 해결 방법

<br>

### 📌 **Quick Summary (빠른 요약)**

To adopt MVVM with Simple UI, choose one of these two setup paths:
> Simple UI에서 MVVM을 사용하려면 아래 두 경로 중 하나를 선택하세요.

**Path A. DataBinding-based MVVM**
> **경로 A. DataBinding 기반 MVVM**

1. Enable DataBinding in **build.gradle.kts**:
```kotlin
android {
    buildFeatures {
        dataBinding = true
    }
}
```

2. Wrap your **layout file** with a `<layout>` tag:
```xml
<layout xmlns:android="http://schemas.android.com/apk/res/android">
    <data>
        <variable
            name="vm"
            type="com.example.MainViewModel" />
    </data>
    <!-- UI 요소들 -->
</layout>
```

3. Use `BaseDataBindingActivity`, `BaseDataBindingFragment`, or `BaseDataBindingDialogFragment`

**Path B. ViewBinding-based MVVM**
> **경로 B. ViewBinding 기반 MVVM**

1. Enable ViewBinding in **build.gradle.kts**:
```kotlin
android {
    buildFeatures {
        viewBinding = true
    }
}
```

2. Use `BaseViewBindingActivity`, `BaseViewBindingFragment`, or `BaseViewBindingDialogFragment`
> 2. `BaseViewBindingActivity`, `BaseViewBindingFragment`, `BaseViewBindingDialogFragment` 중 하나를 사용하세요.

3. Collect one-off ViewModel events in `onEventVmCollect(binding)` as needed
> 3. 필요에 따라 `onEventVmCollect(binding)`에서 일회성 ViewModel 이벤트를 수집하세요.

### ViewModel Helper APIs (ViewModel 헬퍼 API)

| API | 적용 대상 (Scope) | 설명 (Description) |
| :--- | :--- | :--- |
| `getViewModel<T>()` | Activity / Fragment / DialogFragment | `defaultViewModelProviderFactory` 사용 — SavedStateHandle·Hilt 자동 지원<br>Uses `defaultViewModelProviderFactory` — SavedStateHandle & Hilt supported |
| `getViewModel<T>(factory)` | Activity / Fragment / DialogFragment | 커스텀 Factory를 직접 지정할 때 사용<br>Use when a custom Factory is needed |
| `getActivityViewModel<T>()` | Fragment / DialogFragment only | 호스트 Activity 스코프 ViewModel 공유 — `defaultViewModelProviderFactory` 사용<br>Share ViewModel with host Activity — uses `defaultViewModelProviderFactory` |
| `getActivityViewModel<T>(factory)` | Fragment / DialogFragment only | 커스텀 Factory로 Activity 스코프 ViewModel 공유<br>Share ViewModel with host Activity using a custom Factory |

**`getActivityViewModel()` 사용 예시 (Fragment에서 Activity ViewModel 공유)**
> **`getActivityViewModel()` example (sharing Activity ViewModel from Fragment)**

```kotlin
class HomeFragment : BaseDataBindingFragment<FragmentHomeBinding>(R.layout.fragment_home) {

    // Activity 스코프 ViewModel — MainActivity와 동일한 인스턴스
    private val activityVm: MainViewModel by lazy { getActivityViewModel() }

    override fun onViewCreated(binding: FragmentHomeBinding, savedInstanceState: Bundle?) {
        binding.vm = activityVm
    }
}
```

<br></br>

## 🎯 Activity/Fragment Development with MVVM (MVVM 기반 Activity/Fragment 개발)

**Sample capabilities showcased:**
- Activity + ViewModel combinations
- Fragment + ViewModel combinations
- Automated DataBinding wiring
- Event system (button clicks, data updates)
- Automated lifecycle management
> **구현 예제 기능:**
> - Activity + ViewModel 조합
> - Fragment + ViewModel 조합
> - DataBinding 자동화
> - 이벤트 시스템 (버튼 클릭, 데이터 업데이트)
> - Lifecycle 관리 자동화

<br>
</br>

## 🧩 Plain Android MVVM VS Simple UI MVVM Comparisons (코드 비교)


### 1. Activity + ViewModel Integration (첫째: Activity + ViewModel 연동)

<details>
<summary><strong>Plain Android — manual Activity + ViewModel setup/ 기본 Android - Activity + ViewModel 수동 초기화</strong></summary>

```kotlin
class MainActivity : AppCompatActivity() {

    // 1. binding 선언
    private lateinit var binding: ActivityMainBinding

    // 2. viewmodel 선언
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 3. DataBinding 설정 (복잡한 초기화)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        // 4. LifecycleOwner 연결
        binding.lifecycleOwner = this

        // 5. ViewModel 바인딩
        binding.viewModel = viewModel

        // 6. 생명 주기 콜백
        lifecycle.addObserver(viewModel)

        // 7. 이벤트 수집 설정 (함수명 달라질 가능성 존재)
        setupObservers()

        // 8. 초기화 로직
        initViews(binding)
    }

    private fun setupObservers() {
        // ViewModel 이벤트 수집
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        is MainEvent.ShowMessage -> {
                            Toast.makeText(
                                this@MainActivity,
                                event.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        is MainEvent.UpdateCounter -> {
                            binding.tvCounter.text = event.count.toString()
                        }
                    }
                }
            }
        }
    }

    private fun initViews(binding: ActivityMainBinding) {
        binding.btnIncrement.setOnClickListener {
            viewModel.onIncrementClick()
        }
    }
}
```
**Issues:** Complex DataBinding setup, manual LifecycleOwner wiring, verbose event collection boilerplate, and no standardized function names.
>**문제점:** 복잡한 DataBinding 설정, 수동 LifecycleOwner 연결, 이벤트 수집 보일러플레이트, 함수명 표준화 없음

<br></br>
</details>
<details>
<summary><strong>Simple UI — automatic Activity + ViewModel setup/ Simple UI - Activity + ViewModel 자동 초기화</strong></summary>

```kotlin
class MainActivity : BaseDataBindingActivity<ActivityMainBinding>(R.layout.activity_main) {

    // 1. viewmodel 선언
    private val vm: MainViewModel by viewModels()

    override fun onCreate(binding: ActivityMainBinding, savedInstanceState: Bundle?) {
        // DataBinding 자동 설정! ✅
        // LifecycleOwner 자동 연결! ✅

        // 2. ViewModel 바인딩
        binding.vm = vm

        // 3. 생명 주기 콜백
        lifecycle.addObserver(vm)

        // 4. 핵심 로직만 집중!
        initViews(binding)
    }

    // 이벤트 수집 규격화
    override fun onEventVmCollect(binding: ActivityMainBinding) {
        // 이벤트 수집
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {  // ✅ Best Practice
                vm.eventVmFlow.collect { event ->
                    when (event) {
                        is MainEvent.ShowMessage -> {
                            binding.root.snackBarShowShort(event.message)
                        }
                        is MainEvent.UpdateCounter -> {
                            binding.tvCounter.text = event.count.toString()
                        }
                    }
                }
            }
        }
    }

    // 핵심 로직에 더 집중!
    private fun initViews(binding: ActivityMainBinding) {
        binding.btnIncrement.setOnClickListener {
            vm.onIncrementClick()
        }
    }
}
```
**Result:** Automatic DataBinding, automatic LifecycleOwner wiring, streamlined event collection, and a standardized `onEventVmCollect(binding)`!
> **결과:** DataBinding 자동, LifecycleOwner 자동, 이벤트 수집 간소화, 표준화된 onEventVmCollect(binding)!
</details>

---

### ⚠️ Important: Event Collection Best Practices (중요: 이벤트 수집 모범 사례)

When using `onEventVmCollect(binding)` in your Activities, Fragments, or DialogFragments, **always** use `repeatOnLifecycle(Lifecycle.State.STARTED)` to prevent duplicate event collectors during configuration changes.

Activity, Fragment, DialogFragment에서 `onEventVmCollect(binding)`를 사용할 때는 구성 변경 시 중복 이벤트 수집을 방지하기 위해 **반드시** `repeatOnLifecycle(Lifecycle.State.STARTED)`를 사용하세요.

#### ❌ Wrong Way (Causes Duplicate Collectors) / 잘못된 방법 (중복 수집 발생)

```kotlin
override fun onEventVmCollect(binding: ActivityMainBinding) {
    lifecycleScope.launch {
        vm.events.collect { event ->  // ❌ May cause duplicate collectors
            handleEvent(event)
        }
    }
}
```

**Problem:** During screen rotation, the old collector keeps running while a new one starts, causing events to trigger twice.

> **문제점:** 화면 회전 시 기존 수집기가 계속 실행되면서 새 수집기도 시작되어 이벤트가 두 번 실행됩니다.

#### ✅ Correct Way (Safe for Configuration Changes) / 올바른 방법 (구성 변경에 안전)

```kotlin
override fun onEventVmCollect(binding: ActivityMainBinding) {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {  // ✅ Recommended
            vm.events.collect { event ->
                handleEvent(event)
            }
        }
    }
}
```

**Why It Works:** `repeatOnLifecycle` automatically cancels collection when the lifecycle goes below `STARTED` and restarts it when returning to `STARTED`, ensuring only one active collector.

> **작동 원리:** `repeatOnLifecycle`은 생명주기가 `STARTED` 이하로 내려가면 자동으로 수집을 취소하고, 다시 `STARTED`로 돌아오면 재시작하여 항상 하나의 활성 수집기만 유지합니다.

📖 **For more details, see:** [README_ACTIVITY_FRAGMENT.md - Event Collection Best Practices](./README_ACTIVITY_FRAGMENT.md#⚠️-important-event-collection-best-practices-중요-이벤트-수집-모범-사례)

> 📖 **자세한 내용은 다음을 참조하세요:** [README_ACTIVITY_FRAGMENT.md - 이벤트 수집 모범 사례](./README_ACTIVITY_FRAGMENT.md#⚠️-important-event-collection-best-practices-중요-이벤트-수집-모범-사례)

---

<br>
</br>

### 2. Fragment + ViewModel Integration (둘째: Fragment + ViewModel 연동)

<details>
<summary><strong>Plain Android — manual Fragment + ViewModel setup/ 순수 Android - Fragment + ViewModel 수동 초기화</strong></summary>

```kotlin
class MainFragment : Fragment() {
    // 1. binding 선언
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    // 2. viewmodel 선언
    private val viewModel: MainViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 3. DataBinding inflate
        _binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_main,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 4. LifecycleOwner 설정
        binding.lifecycleOwner = viewLifecycleOwner

        // 5. ViewModel 바인딩
        binding.viewModel = viewModel

        // 6. 생명 주기 콜백
        lifecycle.addObserver(viewModel)

        // 7. 이벤트 수집 설정
        setupObservers()

        // 8. 초기화 로직
        initViews()
    }

    // 이벤트 수집 설정 (함수명 달라질 가능성 존재)
    private fun setupObservers() {
        // ViewModel 이벤트 수집
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        is MainEvent.ShowMessage -> {
                            Toast.makeText(
                                requireContext(),
                                event.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        is MainEvent.UpdateData -> {
                            binding.tvData.text = event.data
                        }
                    }
                }
            }
        }
    }

    private fun initViews() {
        binding.btnAction.setOnClickListener {
            viewModel.onActionClick()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 9. 메모리 누수 방지 수동 처리
        _binding = null
    }
}
```
**Issues:** Complicated inflate logic, manual nullable-binding handling, manual LifecycleOwner wiring, memory-leak safeguards, and no standardized function names.
> **문제점:** 복잡한 inflate, nullable binding 처리, 수동 LifecycleOwner, 메모리 누수 방지 코드, 함수명 표준화 없음

<br></br>
</details>

<details>
<summary><strong>Simple UI — automatic Fragment + ViewModel setup/ Simple UI - Fragment + ViewModel 자동 초기화</strong></summary>

```kotlin
class MainFragment : BaseDataBindingFragment<FragmentMainBinding>(R.layout.fragment_main) {

    // 1. viewmodel 선언
    private val vm: MainViewModel by viewModels()

    // DataBinding 자동 설정! ✅
    // LifecycleOwner 자동 연결! ✅
    // nullable binding 자동 처리! ✅
    // 메모리 누수 방지 자동! ✅

    override fun onViewCreated(binding: FragmentMainBinding, savedInstanceState: Bundle?) {
        // 2. ViewModel 바인딩
        binding.vm = vm

        // 3. 생명 주기 콜백
        lifecycle.addObserver(vm)

        // 4. 핵심 로직만 집중!
        initViews()
    }

    // 이벤트 수집 규격화 (BaseDataBindingFragment가 자동으로 호출)
    override fun onEventVmCollect(binding: FragmentMainBinding) {
        // 이벤트 수집만 간단하게
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {  // ✅ Best Practice
                vm.eventVmFlow.collect { event ->
                    when (event) {
                        is MainEvent.ShowMessage -> {
                            binding.root.snackBarShowShort(event.message)
                        }
                        is MainEvent.UpdateData -> {
                            binding.tvData.text = event.data
                    }
                }
            }
        }
    }

    private fun initViews() {
        binding.btnAction.setOnClickListener {
            vm.onActionClick()
        }
    }
}
```
**Result:** DataBinding handled automatically, LifecycleOwner wired, nullable bindings managed safely, memory leaks prevented, and `onEventVmCollect(binding)` standardized!
> **결과:** DataBinding 자동, LifecycleOwner 자동, nullable 처리 자동, 메모리 누수 방지 자동, 표준화된 `onEventVmCollect(binding)`!

**Note:** Fragment/Dialog binding hierarchies also support `onBindingCreated(binding, savedInstanceState)` for early binding-variable assignment before `viewLifecycleOwner` becomes available.
> **참고:** Fragment/Dialog 바인딩 계층은 `viewLifecycleOwner`가 준비되기 전의 초기 바인딩 변수 할당을 위해 `onBindingCreated(binding, savedInstanceState)` 훅도 지원합니다.


</details>
<br></br>

### 3. DialogFragment + ViewModel Integration (셋째: DialogFragment + ViewModel 연동)

<details>
<summary><strong>Plain Android — manual DialogFragment + ViewModel setup/ 순수 Android - DialogFragment + ViewModel 수동 초기화</strong></summary>

```kotlin
class InfoDialog : AppCompatDialogFragment() {

    private var _binding: DialogInfoBinding? = null
    private val binding get() = _binding!!
    private val viewModel: InfoDialogViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.vm = viewModel

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        InfoDialogEvent.Dismiss -> dismissAllowingStateLoss()
                        is InfoDialogEvent.ShowToast ->
                            Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        binding.btnConfirm.setOnClickListener { viewModel.onConfirm() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
```
**Issues:** Manual nullable-binding 처리, lifecycleOwner 지정, event Flow 구독, 다이얼로그 종료 처리 등이 모두 반복됩니다.
> **문제점:** nullable-binding, lifecycleOwner, 이벤트 구독, 다이얼로그 종료 로직까지 매번 작성해야 합니다.

<br></br>
</details>

<details>
<summary><strong>Simple UI — BaseDataBindingDialogFragment + ViewModel/ Simple UI - BaseDataBindingDialogFragment + ViewModel</strong></summary>

```kotlin
class InfoDialog : BaseDataBindingDialogFragment<DialogInfoBinding>(R.layout.dialog_info) {

    private val vm: InfoDialogViewModel by lazy { getViewModel<InfoDialogViewModel>() }

    override fun onViewCreated(binding: DialogInfoBinding, savedInstanceState: Bundle?) {
        binding.vm = vm
        lifecycle.addObserver(vm)

        // null은 WRAP_CONTENT, 비율 값은 0.0f..1.0f 범위만 허용
        resizeDialog(0.85f, 0.5f)

    }

    // 이벤트 수집 규격화 (BaseDataBindingDialogFragment가 자동으로 호출)
    override fun onEventVmCollect(binding: DialogInfoBinding) {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {  // ✅ Best Practice
                vm.eventVmFlow.collect { event ->
                    when (event) {
                        InfoDialogEvent.Dismiss -> safeDismiss()
                        is InfoDialogEvent.ShowToast ->
                            binding.root.snackBarShowShort(event.message)
                    }
                }
            }
        }
    }
}
```
**Result:** DataBinding, lifecycleOwner 연결, nullable-binding 관리, 이벤트 구독, 다이얼로그 종료까지 동일한 패턴으로 자동화됩니다.
> **결과:** DataBinding/lifecycleOwner/이벤트 처리/다이얼로그 종료가 모두 통일된 패턴으로 해결됩니다.
</details>

<br>
</br>

### 4. ViewModel Event System Comparison (넷째: ViewModel 이벤트 시스템 비교)

<details>
<summary><strong>Plain Android — manual Flow/Channel wiring (순수 Android - Flow/Channel 수동 구성)</strong></summary>

```kotlin
class MainViewModel : ViewModel() {
    // 1. Flow 채널 구성
    private val _events = Channel<MainEvent>(Channel.BUFFERED)
    val events: Flow<MainEvent> = _events.receiveAsFlow()

    // 2. StateFlow 관리
    private val _counter = MutableStateFlow(0)
    val counter: StateFlow<Int> = _counter.asStateFlow()

    private val _data = MutableStateFlow("")
    val data: StateFlow<String> = _data.asStateFlow()

    // 3. 이벤트 전송 - 복잡한 viewModelScope.launch 필요
    fun onIncrementClick() {
        viewModelScope.launch {
            _counter.value += 1
            viewModelScope.launch {
                _events.send(MainEvent.UpdateCounter(_counter.value))
            }
            viewModelScope.launch {
                _events.send(MainEvent.ShowMessage("Counter: ${_counter.value}"))
            }
        }
    }

    fun onActionClick() {
        viewModelScope.launch {
            val newData = "Data updated at ${System.currentTimeMillis()}"
            _data.value = newData
            viewModelScope.launch {
                _events.send(MainEvent.UpdateData(newData))
            }
            viewModelScope.launch {
                _events.send(MainEvent.ShowMessage("Action completed"))
            }
        }
    }

    // 4. 채널 해제도 수동
    override fun onCleared() {
        super.onCleared()
        _events.close()
    }
}

// 5. 이벤트 sealed class 정의
sealed class MainEvent {
    data class ShowMessage(val message: String) : MainEvent()
    data class UpdateCounter(val count: Int) : MainEvent()
    data class UpdateData(val data: String) : MainEvent()
}
```
**Issues:** Complex channel setup, manual event dispatch, and resource cleanup you must handle yourself.
> **문제점:** 복잡한 채널 구성, 수동 이벤트 전송, 리소스 해제 직접 관리

<br></br>
</details>

<details>
<summary><strong>Simple UI — automatic BaseViewModelEvent (Simple UI - BaseViewModelEvent 자동)</strong></summary>

```kotlin
class MainViewModel : BaseViewModelEvent<MainEvent>() {
    // 채널 자동 구성! ✅
    // eventVmFlow 자동 제공! ✅

    // 1. StateFlow 관리
    private val _counter = MutableStateFlow(0)
    val counter: StateFlow<Int> = _counter.asStateFlow()

    private val _data = MutableStateFlow("")
    val data: StateFlow<String> = _data.asStateFlow()

    // 2. 이벤트 전송 한 줄로 완성!
    fun onIncrementClick() {
        viewModelScope.launch {
            _counter.value += 1
            sendEventVm(MainEvent.UpdateCounter(_counter.value))
            sendEventVm(MainEvent.ShowMessage("Counter: ${_counter.value}"))
        }
    }

    fun onActionClick() {
        viewModelScope.launch {
            val newData = "Data updated at ${System.currentTimeMillis()}"
            _data.value = newData
            sendEventVm(MainEvent.UpdateData(newData))
            sendEventVm(MainEvent.ShowMessage("Action completed"))
        }
    }

    // 채널 해제 자동! ✅
}

// 이벤트 sealed class 정의
sealed class MainEvent {
    data class ShowMessage(val message: String) : MainEvent()
    data class UpdateCounter(val count: Int) : MainEvent()
    data class UpdateData(val data: String) : MainEvent()
}
```
**Result:** Channels are created automatically, event dispatch stays simple, and resources are managed for you!
> **결과:** 채널 자동 구성, 이벤트 전송 간단, 리소스 관리 자동!
</details>

<br>
</br>

## 🚀 Core Advantages of Simple UI MVVM/ Simple UI MVVM의 핵심 장점


### 1. **⚡ Effortless DataBinding (사용이 편한 DataBinding)**
- **Automatic inflate:** Constructor parameters handle layout wiring.
- **Automatic LifecycleOwner:** No manual hookups necessary.
- **Memory management:** Nullable bindings are handled for you.
> - **자동 inflate**: 생성자 파라미터로 레이아웃 자동 설정
> - **자동 LifecycleOwner**: 수동 연결 불필요
> - **메모리 관리**: nullable binding 처리 자동

<br>
</br>

### 2. **🛠️ Standardized Event System (표준화된 이벤트 시스템)**
- **`BaseViewModelEvent`:** Flow/Channel automatically prepared.
- **`sendEventVm()`:** Dispatch events in one line.
- **`onEventVmCollect(binding)`:** Unified entry point (hook) for event collection; automatically called by BaseDataBindingActivity/Fragment/DialogFragment.
- **Resource management:** Channels are released automatically.
> - **`BaseViewModelEvent`**: Flow/Channel 자동 구성
> - **`sendEventVm()`**: 이벤트 전송 한 줄
> - **`onEventVmCollect(binding)`**: 표준화된 이벤트 수집 훅 (BaseDataBindingActivity/Fragment/DialogFragment에서 자동 호출)
> - **리소스 관리**: 채널 자동 해제

<br>
</br>

### 3. **🎯 Optimized Developer Experience (개발자 경험 최적화)**
- **Type safety:** Prevent errors at compile time.
- **Consistent patterns:** Align the whole team on the same MVVM structure.
- **Faster development:** Remove boilerplate and boost productivity.
> - **타입 안전성**: 컴파일 타임 오류 방지
> - **일관된 패턴**: 팀 전체 동일한 MVVM 구조
> - **빠른 개발**: 보일러플레이트 제거로 생산성 향상

<br>
</br>

### 4. **🔧 Mistake-Proofing (실수 방지)**
- **LifecycleOwner omissions:** Automatically wired to prevent mistakes.
- **Memory leaks:** Fragment nullable bindings handled safely.
- **Channel cleanup:** `BaseViewModelEvent` manages teardown for you.
> - **LifecycleOwner 누락**: 자동 연결로 방지
> - **메모리 누수**: Fragment nullable binding 자동 처리
> - **채널 해제**: BaseViewModelEvent가 자동 관리

<br>
</br>

### 5. **📉 Less Setup Code in the Guide Example (가이드 예제 기준 코드 간소화)**
- **Activity + ViewModel:** 30–40 lines → under 15 (**60% reduction**)
- **Fragment + ViewModel:** 50–60 lines → under 20 (**65% reduction**)
- **ViewModel events:** Channel setup 10+ lines → single-line `sendEventVm()`
> - **Activity + ViewModel**: 30-40줄 → 15줄 미만 **60% 단축**
> - **Fragment + ViewModel**: 50-60줄 → 20줄 미만 **65% 단축**
> - **ViewModel 이벤트**: Channel 구성 10줄+ → sendEventVm() 한 줄

<br>
</br>

## 💡 What the Guide Highlights (가이드에서 드러나는 개선점)

- **DataBinding setup becomes more concise** through the provided base classes.
- **Nullable binding handling in Fragments** is moved into the base hierarchy.
- **`BaseViewModelEvent` standardizes event flow setup** and cleanup.
- **`onEventVmCollect(binding)` unifies the event collection entry point.**
- **Activity/Fragment + ViewModel setup code is reduced in the guide comparison.**
- **`sendEventVm()` keeps one-off event dispatch concise.**

> - **제공하는 Base 클래스로 DataBinding 설정을 더 간결하게 정리할 수 있습니다.**
> - **Fragment의 nullable binding 처리를 베이스 계층으로 옮길 수 있습니다.**
> - **`BaseViewModelEvent`로 이벤트 흐름 구성과 정리를 표준화할 수 있습니다.**
> - **`onEventVmCollect(binding)`로 이벤트 수집 진입점을 통일할 수 있습니다.**
> - **가이드 비교 기준으로 Activity/Fragment + ViewModel 설정 코드가 줄어듭니다.**
> - **`sendEventVm()`로 일회성 이벤트 전송을 간결하게 유지할 수 있습니다.**

<br>
</br>

## 🎉 Conclusion: What This MVVM Guide Demonstrates (결론: 이 MVVM 가이드가 보여주는 것)

**Simple UI MVVM** focuses on reducing repeated Activity/Fragment + ViewModel setup work through base classes and helper APIs.

✅ **Automated DataBinding wiring** — constructor-based setup reduces repeated initialization.  
✅ **Standardized event system** — Flow/Channel wiring is wrapped in `BaseViewModelEvent`.  
✅ **Consistent function names** — `onEventVmCollect(binding)`, `sendEventVm()` keep usage patterns aligned.  
✅ **Less setup code in the guide example** — the comparison illustrates a 60–65% reduction in setup-oriented code.

This guide is intended to show how the library simplifies common MVVM setup patterns.

> **Simple UI MVVM**은 Base 클래스와 헬퍼 API를 통해 Activity/Fragment + ViewModel의 반복 설정을 줄이는 데 초점을 둡니다.
>
> ✅ **DataBinding 연결 자동화** - 생성자 기반 설정으로 반복 초기화를 줄입니다.  
> ✅ **표준화된 이벤트 시스템** - `BaseViewModelEvent`가 Flow/Channel 구성을 감쌉니다.  
> ✅ **일관된 함수명** - `onEventVmCollect(binding)`, `sendEventVm()`로 사용 패턴을 맞춥니다.  
> ✅ **가이드 예제 기준 코드 감소** - 비교 예제에서는 설정 중심 코드가 60-65% 줄어드는 흐름을 보여줍니다.
>
> 이 가이드는 라이브러리가 공통 MVVM 설정 패턴을 어떻게 단순화하는지 보여주기 위한 문서입니다.

---

<br>
</br>

## 🚀 Explore Real Implementations (실제 구현 예제 보기)

**Live sample code:**
- Simple UI sample: `app/src/main/java/kr/open/library/simpleui_xml/activity_fragment/`
- Launch the app to see the implementation in action!
> - 실제로 앱을 구동시켜서 실제 구현 예제를 확인해 보세요!

<br>
</br>

**Try these features:**
- Activity + ViewModel automatic initialization
- Fragment + ViewModel automatic initialization
- DialogFragment + ViewModel automatic initialization
- `BaseViewModelEvent` event system
- Event dispatch via `sendEventVm()`
- Event collection via `onEventVmCollect(binding)` (automatically called)
- Automatic DataBinding wiring
- Automated lifecycle management
- Automatic nullable binding handling
> **테스트 가능한 기능:**
> - Activity + ViewModel 자동 초기화
> - Fragment + ViewModel 자동 초기화
> - DialogFragment + ViewModel 자동 초기화
> - BaseViewModelEvent 이벤트 시스템
> - sendEventVm() 이벤트 전송
> - onEventVmCollect(binding) 이벤트 수집 (자동 호출됨)
> - DataBinding 자동 연동
> - Lifecycle 자동 관리
> - nullable binding 자동 처리

<br>
</br>
.

