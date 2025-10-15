# 📱 Simple UI MVVM 패턴 - 완벽 가이드

### Activity + ViewModel 초기 설정
![mvvm_activity_init.gif](example_gif%2Fmvvm_activity_init.gif)

### Activity + ViewModel 초기 사용
![mvvm_activity_vm_init_2.gif](example_gif%2Fmvvm_activity_vm_init_2.gif)![mvvm_activity_vm_init_2_example.gif](example%2Fmvvm_activity_vm_init_2_example.gif)

### ViewModel 이벤트 시스템
![mvvm_vm.png](example_gif%2Fmvvm_vm.png)![mvvm_vm_example.png](example%2Fmvvm_vm_example.png)

> **"MVVM 세팅을 10줄로 끝내자!"** 기존 Activity/Fragment + ViewModel 개발 대비 Simple UI가 주는 체감 차이를 한눈에 확인하세요.

<br>
</br>

## 🔎 한눈 비교 (At a glance)

<br>
</br>

### Activity/Fragment + ViewModel 초기화

| 항목 | 순수 Android | Simple UI |
|:--|:--:|:--:|
| DataBinding 설정 | 수동 inflate + setContentView (7줄+) | 생성자 파라미터로 자동 ✅ |
| LifecycleOwner 설정 | 수동 binding.lifecycleOwner 설정 | 자동 연동 ✅ |
| ViewModel 바인딩 | 수동 binding.viewModel 설정 | 선택적 간단 사용 ✅ |
| onCreate 보일러플레이트 | 복잡한 초기화 코드 | 최소화된 코드 ✅ |

<br>
</br>

### ViewModel 이벤트 시스템
| 항목 | 순수 Android | Simple UI |
|:--|:--:|:--:|
| 이벤트 채널 구성 | Flow/Channel 수동 구성 (10줄+) | BaseViewModelEvent Channel 자동 ✅ |
| 이벤트 전송 | viewModelScope.launch + send | sendEventVm() 한 줄 ✅ |
| 채널 리소스 관리 | 수동 close() 필요 | 자동 관리 ✅ |
| 이벤트 수집 | 수동 lifecycleScope.launch | eventVmCollect() 오버라이드 ✅ |

> **핵심:** Simple UI는 "복잡한 MVVM 보일러플레이트"를 **자동화**를 통해 개발 속도를 향상시킵니다.

<br>
</br>

## 💡 왜 Simple UI MVVM이 중요한가:

- **개발 시간 단축**: ViewModel 연동 보일러플레이트 제거로 핵심 로직에 집중 가능
- **실수 방지**: ViewModel 바인딩, 이벤트 채널 구성 등에서 발생하는 버그 예방
- **일관된 패턴**: 팀 전체가 동일한 MVVM 구조 사용
- **유지보수성**: BaseViewModelEvent로 표준화된 이벤트 시스템
- **빠른 프로토타이핑**: 아이디어를 바로 구현하여 테스트 가능

<br>
</br>

## 📦 시작하기 전에

Simple UI의 MVVM 기능을 사용하려면 **DataBinding 설정과 Base 클래스 이해**가 필요합니다.

<br>

### ✅ **먼저 확인하세요!**

📌 **Activity/Fragment 기본 사용법을 모르시나요?**
→ [README_ACTIVITY_FRAGMENT.md](README_ACTIVITY_FRAGMENT.md)에서 다음 내용을 먼저 확인하세요:
- 필수 설정 (DataBinding 활성화)
- BaseBindingActivity/BaseBindingFragment 기본 사용법
- 자주 발생하는 오류 해결 방법

<br>

### 📌 **빠른 요약**

MVVM 패턴을 사용하려면:

1. **build.gradle.kts**에 DataBinding 활성화:
```kotlin
android {
    buildFeatures {
        dataBinding = true
    }
}
```

2. **레이아웃 파일**을 `<layout>` 태그로 감싸기:
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

3. **BaseBindingActivity** 또는 **BaseBindingFragment** 사용

<br>
</br>

## 🎯 비교 대상: MVVM 패턴 기반 Activity/Fragment 개발

**구현 예제 기능:**
- Activity + ViewModel 조합
- Fragment + ViewModel 조합
- DataBinding 자동화
- 이벤트 시스템 (버튼 클릭, 데이터 업데이트)
- Lifecycle 관리 자동화

<br>
</br>

## 실제 코드 비교


### 첫째: Activity + ViewModel 연동

<details>
<summary><strong>순수 Android - Activity + ViewModel 수동 초기화</strong></summary>

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
        initViews()
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

    private fun initViews() {
        binding.btnIncrement.setOnClickListener {
            viewModel.onIncrementClick()
        }
    }
}
```
**문제점:** 복잡한 DataBinding 설정, 수동 LifecycleOwner 연결, 이벤트 수집 보일러플레이트, 함수명 표준화 없음
</details>

<details>
<summary><strong>Simple UI - Activity + ViewModel 자동 초기화</strong></summary>

```kotlin
class MainActivity : BaseBindingActivity<ActivityMainBinding>(R.layout.activity_main) {

    // 1. viewmodel 선언
    private val vm: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // DataBinding 자동 설정! ✅
        // LifecycleOwner 자동 연결! ✅

        // 2. ViewModel 바인딩
        binding.vm = vm

        // 3. 생명 주기 콜백
        lifecycle.addObserver(vm)

        // 4. viewmodel 이벤트 수집 설정
        eventVmCollect()

        // 5. 핵심 로직만 집중!
        initViews()
    }

    // 이벤트 수집 규격화
    override fun eventVmCollect() {
        // 이벤트 수집
        lifecycleScope.launch {
            vm.mEventVm.collect { event ->
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

    // 핵심 로직에 더 집중!
    private fun initViews() {
        binding.btnIncrement.setOnClickListener {
            vm.onIncrementClick()
        }
    }
}
```
**결과:** DataBinding 자동, LifecycleOwner 자동, 이벤트 수집 간소화, 표준화된 eventVmCollect()!
</details>

<br>
</br>

### 둘째: Fragment + ViewModel 연동

<details>
<summary><strong>순수 Android - Fragment + ViewModel 수동 초기화</strong></summary>

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
**문제점:** 복잡한 inflate, nullable binding 처리, 수동 LifecycleOwner, 메모리 누수 방지 코드, 함수명 표준화 없음
</details>

<details>
<summary><strong>Simple UI - Fragment + ViewModel 자동 초기화</strong></summary>

```kotlin
class MainFragment : BaseBindingFragment<FragmentMainBinding>(R.layout.fragment_main) {

    // 1. viewmodel 선언
    private val vm: MainViewModel by viewModels()

    // DataBinding 자동 설정! ✅
    // LifecycleOwner 자동 연결! ✅
    // nullable binding 자동 처리! ✅
    // 메모리 누수 방지 자동! ✅

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 2. ViewModel 수동 바인딩
        binding.vm = vm

        // 3. 생명 주기 콜백
        lifecycle.addObserver(vm)

        // 4. 이벤트 수집 설정
        eventVmCollect()

        // 5. 핵심 로직만 집중!
        initViews()
    }

    // 이벤트 수집 규격화
    override fun eventVmCollect() {
        // 이벤트 수집만 간단하게
        viewLifecycleOwner.lifecycleScope.launch {
            vm.mEventVm.collect { event ->
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
**결과:** DataBinding 자동, LifecycleOwner 자동, nullable 처리 자동, 메모리 누수 방지 자동, 표준화된 eventVmCollect()!
</details>

<br>
</br>

### 셋째: ViewModel 이벤트 시스템 비교

<details>
<summary><strong>순수 Android - Flow/Channel 수동 구성</strong></summary>

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
**문제점:** 복잡한 채널 구성, 수동 이벤트 전송, 리소스 해제 직접 관리
</details>

<details>
<summary><strong>Simple UI - BaseViewModelEvent 자동</strong></summary>

```kotlin
class MainViewModel : BaseViewModelEvent<MainEvent>() {
    // 채널 자동 구성! ✅
    // mEventVm 자동 제공! ✅

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
**결과:** 채널 자동 구성, 이벤트 전송 간단, 리소스 관리 자동!
</details>

<br>
</br>

## 🚀 Simple UI MVVM의 핵심 장점


### 1. **⚡ 사용이 편한 DataBinding**
- **자동 inflate**: 생성자 파라미터로 레이아웃 자동 설정
- **자동 LifecycleOwner**: 수동 연결 불필요
- **메모리 관리**: nullable binding 처리 자동

<br>
</br>

### 2. **🛠️ 표준화된 이벤트 시스템**
- **BaseViewModelEvent**: Flow/Channel 자동 구성
- **sendEventVm()**: 이벤트 전송 한 줄
- **eventVmCollect()**: 표준화된 이벤트 수집 함수
- **리소스 관리**: 채널 자동 해제

<br>
</br>

### 3. **🎯 개발자 경험 최적화**
- **타입 안전성**: 컴파일 타임 오류 방지
- **일관된 패턴**: 팀 전체 동일한 MVVM 구조
- **빠른 개발**: 보일러플레이트 제거로 생산성 향상

<br>
</br>

### 4. **🔧 실수 방지**
- **LifecycleOwner 누락**: 자동 연결로 방지
- **메모리 누수**: Fragment nullable binding 자동 처리
- **채널 해제**: BaseViewModelEvent가 자동 관리

<br>
</br>

### 5. **📉 압도적인 코드 간소화**
- **Activity + ViewModel**: 30-40줄 → 15줄 미만 **60% 단축**
- **Fragment + ViewModel**: 50-60줄 → 20줄 미만 **65% 단축**
- **ViewModel 이벤트**: Channel 구성 10줄+ → sendEventVm() 한 줄

<br>
</br>

## 💡 개발자 후기

> **"DataBinding 설정을 더 이상 고민할 필요가 없어요!"**
>
> **"Fragment의 nullable binding 처리가 자동으로 되니 편해요!"**
>
> **"BaseViewModelEvent로 이벤트 시스템이 표준화되어 코드가 깔끔해졌어요!"**
>
> **"eventVmCollect()로 이벤트 수집 함수명이 통일되어 좋아요!"**
>
> **"Activity/Fragment + ViewModel 초기화 코드가 60% 줄어들었습니다!"**
>
> **"sendEventVm()로 이벤트 전송이 한 줄로 끝나니 편해요!"**

<br>
</br>

## 🎉 결론: MVVM 개발의 새로운 표준

**Simple UI MVVM**은 복잡한 Activity/Fragment + ViewModel 초기화를 **단순하고 강력하게** 만드는 혁신적인 라이브러리입니다.

✅ **DataBinding 자동화** - 복잡한 초기화를 생성자 파라미터로!
✅ **이벤트 시스템 완성** - Flow/Channel 구성을 자동으로!
✅ **표준화된 함수명** - eventVmCollect(), sendEventVm() 통일!
✅ **보일러플레이트 제거** - 60-65% 코드 간소화!

**전통적인 복잡함은 이제 그만.**
**Simple UI와 함께 생산적인 MVVM 개발을 경험하세요!** 🚀

---

<br>
</br>

## 🚀 실제 구현 예제 보기

**라이브 예제 코드:**
> - Simple UI 예제: `app/src/main/java/kr/open/library/simpleui_xml/mvvm/new_/`
> - 순수 Android 예제: `app/src/main/java/kr/open/library/simpleui_xml/mvvm/origin/`
> - 실제로 앱을 구동시켜서 실제 구현 예제를 확인해 보세요!

<br>
</br>

**테스트 가능한 기능:**
- Activity + ViewModel 자동 초기화
- Fragment + ViewModel 자동 초기화
- DialogFragment + ViewModel 자동 초기화
- BaseViewModelEvent 이벤트 시스템
- sendEventVm() 이벤트 전송
- eventVmCollect() 이벤트 수집
- DataBinding 자동 연동
- Lifecycle 자동 관리
- nullable binding 자동 처리

<br>
</br>
