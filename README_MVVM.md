# 📱 Simple UI MVVM vs 순수 Android - 완벽 비교 가이드


> **"MVVM 세팅을 10줄로 끝내자!"** 기존 Activity/Fragment + ViewModel 개발 대비 Simple UI가 주는 체감 차이를 한눈에 확인하세요.

<br>
</br>

## 🔎 한눈 비교 (At a glance)

<br>
</br>

### Activity/Fragment 초기화

| 항목 | 순수 Android |   Simple UI    |
|:--|:--:|:--------------:|
| DataBinding 설정 | 수동 inflate + setContentView (7줄+) | 생성자 파라미터로 자동 ✅ |
| LifecycleOwner 설정 | 수동 binding.lifecycleOwner 설정 |    자동 연동 ✅     |
| ViewModel 바인딩 | 수동 binding.viewModel 설정 |  선택적 간단 사용 ✅   |
| onCreate 보일러플레이트 | 복잡한 초기화 코드 |   최소화된 코드 ✅    |

#### 초기 설정
![mvvm_activity_init_example.gif](example%2Fmvvm_activity_init_example.gif)

#### 초기 사용
![mvvm_activity_vm_init_2_example.gif](example%2Fmvvm_activity_vm_init_2_example.gif)

<br>
</br>

### ViewModel 이벤트 시스템
| 항목 | 순수 Android |            Simple UI            |
|:--|:--:|:-------------------------------:|
| 이벤트 채널 구성 | Flow/Channel 수동 구성 (10줄+) | BaseViewModelEvent Channel 자동 ✅ |
| 이벤트 전송 | viewModelScope.launch + send |       sendEventVm() 한 줄 ✅       |
| 채널 리소스 관리 | 수동 close() 필요 |             자동 관리 ✅             |
| 이벤트 수집 | 수동 lifecycleScope.launch |    eventVmCollect() 오버라이드 ✅     |

![mvvm_vm_example.png](example%2Fmvvm_vm_example.png)

> **핵심:** Simple UI는 "복잡한 MVVM 보일러플레이트"를 **자동화**를 통해 개발 속도가 향상 됩니다.

<br>
</br>

## 💡 왜 중요한가:

- **개발 시간 단축**: Activity/Fragment 초기화 보일러플레이트 제거로 핵심 로직에 집중 가능
- **실수 방지**: DataBinding 설정, LifecycleOwner 연결 등에서 발생하는 버그 예방
- **일관된 패턴**: 팀 전체가 동일한 MVVM 구조 사용
- **유지보수성**: BaseViewModelEvent로 표준화된 이벤트 시스템
- **빠른 프로토타이핑**: 아이디어를 바로 구현하여 테스트 가능

<br>
</br>

## 🎯 비교 대상: MVVM 패턴 기반 Activity/Fragment 개발

**구현 예제 기능:**
- Activity + ViewModel 조합
- Fragment + ViewModel 조합
- DataBinding 자동화
- 이벤트 시스템 (버튼 클릭, 데이터 업데이트)
- Lifecycle 관리 자동화

---

<br>
</br>


<br>
</br>

---

## 실제 코드 비교


### 첫째: Activity 초기화 비교

<details>
<summary><strong>순수 Android - Activity 수동 초기화</strong></summary>

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
                            Toast.makeText(this@MainActivity, event.message, Toast.LENGTH_SHORT).show()
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
**문제점:** 복잡한 DataBinding 설정, 수동 LifecycleOwner 연결, 이벤트 수집 보일러플레이트
</details>

<details>
<summary><strong>Simple UI - Activity 자동 초기화</strong></summary>

```kotlin
class MainActivity : BaseBindingActivity<ActivityMainBinding>(R.layout.activity_main) {

    // 1. viewmodel 선언
    private val vm: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 2. ViewModel 바인딩
        binding.vm = vm

        // 3. 생명 주기 콜백
        lifecycle.addObserver(vm)

        // 4. viewmodel 이벤트 수집 설정
        eventVmCollect()
        
        // 5. 초기화 로직핵심 로직만 집중!
        initViews()
    }

    // 이벤트 수집 규격화
    override fun eventVmCollect() {
        // 이벤트 수집
        lifecycleScope.launch {
            vm.mEventVm.collect { event ->
                when (event) {
                    is MainEvent.ShowMessage -> binding.root.snackBarShowShort(event.message)
                    is MainEvent.UpdateCounter -> binding.tvCounter.text = event.count.toString()
                }
            }
        }
    }
    
    //핵심 로직에 더 집중!
    private fun initViews() {
        binding.btnIncrement.setOnClickListener {
            viewModel.onIncrementClick()
        }
    }
}
```
**결과:** DataBinding 자동, LifecycleOwner 자동, 이벤트 수집 간소화!
</details>

<br>
</br>

### 둘째: Fragment 초기화 비교

<details>
<summary><strong>순수 Android - Fragment 수동 초기화</strong></summary>

```kotlin
class MainFragment : Fragment() {
    // 1. binding 선언
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    
    // 2. viewmodel 선언
    private val viewModel: MainViewModel by viewModels()

    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // 3. DataBinding inflate
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, ontainer, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 4. LifecycleOwner 설정
        binding.lifecycleOwner = viewLifecycleOwner

        // 5. ViewModel 바인딩
        binding.viewModel = viewModel

        // 6.  생명 주기 콜백
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
                            Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
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
**문제점:** 복잡한 inflate, nullable binding 처리, 수동 LifecycleOwner, 메모리 누수 방지 코드
</details>

<details>
<summary><strong>Simple UI - Fragment 자동 초기화</strong></summary>

```kotlin
class MainFragment : BaseBindingFragment<FragmentMainBinding>(R.layout.fragment_main) {
    
    // 1. viewmodel 선언
    private val vm: MainViewModel by viewModels()

    // DataBinding, LifecycleOwner 자동 설정!
    // nullable binding 처리 자동!
    // 메모리 누수 방지 자동!



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState) 
        
        // 2. ViewModel 수동 바인딩
        binding.vm = vm

        // 3.  생명 주기 콜백
        lifecycle.addObserver(vm)

        // 4. 이벤트 수집 설정
        eventVmCollect()
        
        // 5. 핵심 로직만 집중!
        initViews()
    }

    //이벤트 수집 설정 (함수명 달라질 가능성 존재)
    override fun eventVmCollect() {
        // 이벤트 수집만 간단하게
        viewLifecycleOwner.lifecycleScope.launch {
            vm.mEventVm.collect { event ->
                when (event) {
                    is MainEvent.ShowMessage -> binding.root.snackBarShowShort(event.message)
                    is MainEvent.UpdateData -> binding.tvData.text = event.data
                }
            }
        }
    }
    
    private fun initViews() {
        binding.btnAction.setOnClickListener {
            viewModel.onActionClick()
        }
    }
}
```
**결과:** DataBinding 자동, LifecycleOwner 자동, nullable 처리 자동, 메모리 누수 방지 자동!
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
            viewModelScope.launch { _events.send(MainEvent.UpdateCounter(_counter.value)) }
            viewModelScope.launch { _events.send(MainEvent.ShowMessage("Counter: ${_counter.value}")) }
        }
    }

    fun onActionClick() {
        viewModelScope.launch {
            val newData = "Data updated at ${System.currentTimeMillis()}"
            _data.value = newData
            viewModelScope.launch { _events.send(MainEvent.UpdateData(newData)) }
            viewModelScope.launch { _events.send(MainEvent.ShowMessage("Action completed")) }
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

    // 채널 해제 자동!
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

### 넷째: RootActivity 추가 기능 (SystemBars 제어 & 높이 계산)

<details>
<summary><strong>순수 Android - StatusBar/NavigationBar 수동 처리</strong></summary>

```kotlin
class MainActivity : AppCompatActivity() {

    // StatusBar 높이 계산 - 복잡한 로직
    private fun getStatusBarHeight(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.decorView.rootWindowInsets?.getInsets(WindowInsets.Type.statusBars())?.top ?: 0
        } else {
            val rect = Rect()
            window.decorView.getWindowVisibleDisplayFrame(rect)
            rect.top
        }
    }

    // NavigationBar 높이 계산 - 복잡한 로직
    private fun getNavigationBarHeight(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.decorView.rootWindowInsets?.getInsets(WindowInsets.Type.navigationBars())?.bottom ?: 0
        } else {
            val rootView = window.decorView.rootView
            val contentViewHeight = findViewById<View>(android.R.id.content).height
            val statusBarHeight = getStatusBarHeight()
            (rootView.height - contentViewHeight) - statusBarHeight
        }
    }

    // StatusBar 투명하게 설정 - 수동 처리
    private fun setStatusBarTransparent() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }
    }

    // StatusBar 색상 설정 - 수동 처리
    private fun setStatusBarColor(@ColorInt color: Int, isLightStatusBar: Boolean = false) {
        window.statusBarColor = color
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = isLightStatusBar
    }

    // NavigationBar 색상 설정 - 수동 처리
    private fun setNavigationBarColor(@ColorInt color: Int, isLightNavigationBar: Boolean = false) {
        window.navigationBarColor = color
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightNavigationBars = isLightNavigationBar
    }

    // SystemBars 동시 색상 설정 - 수동 처리
    private fun setSystemBarsColor(@ColorInt color: Int, isLightSystemBars: Boolean = false) {
        setStatusBarColor(color, isLightSystemBars)
        setNavigationBarColor(color, isLightSystemBars)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 사용 예시
        val statusHeight = getStatusBarHeight()
        val navHeight = getNavigationBarHeight()

        setStatusBarTransparent()
        setSystemBarsColor(Color.BLACK, isLightSystemBars = false)
    }
}
```
**문제점:** 복잡한 SDK 버전 분기, 긴 코드, 반복적인 WindowInsets 처리
</details>

<details>
<summary><strong>Simple UI - RootActivity 자동 제공</strong></summary>

```kotlin
class MainActivity : BaseBindingActivity<ActivityMainBinding>(R.layout.activity_main) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. StatusBar/NavigationBar 높이 - 자동 계산!
        val statusHeight = statusBarHeight  // 프로퍼티로 바로 접근
        val navHeight = navigationBarHeight  // 프로퍼티로 바로 접근

        // 2. StatusBar 투명 설정 - 한 줄!
        setStatusBarTransparent()

        // 3. StatusBar 색상 설정 - 한 줄!
        setStatusBarColor(Color.BLACK, isLightStatusBar = false)

        // 4. NavigationBar 색상 설정 - 한 줄!
        setNavigationBarColor(Color.WHITE, isLightNavigationBar = true)

        // 5. SystemBars 동시 설정 - 한 줄!
        setSystemBarsColor(Color.TRANSPARENT, isLightSystemBars = true)

        // 6. SystemBars 아이콘 모드 변경 - 한 줄!
        setSystemBarsAppearance(isLightSystemBars = false)
    }
}
```
**결과:** SDK 버전 분기 자동, 프로퍼티로 간편 접근, protected 메서드로 즉시 사용!
</details>

<br>
</br>

**RootActivity 제공 기능:**

| 기능 | 설명 |
|:--|:--|
| **statusBarHeight** | StatusBar 높이 자동 계산 (SDK 버전별 자동 분기) |
| **navigationBarHeight** | NavigationBar 높이 자동 계산 |
| **setStatusBarTransparent()** | StatusBar를 투명하게 설정 |
| **setStatusBarColor()** | StatusBar 색상 및 아이콘 모드 설정 |
| **setNavigationBarColor()** | NavigationBar 색상 및 아이콘 모드 설정 |
| **setSystemBarsColor()** | SystemBars 동시 색상 설정 |
| **setSystemBarsAppearance()** | SystemBars 아이콘 라이트/다크 모드 설정 |
| **onRequestPermissions()** | 통합 권한 요청 (일반/특수 권한 자동 구분) |
| **beforeOnCreated()** | onCreate 전 초기화 훅 |

<br>
</br>

---

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
- **리소스 관리**: 채널 자동 해제

<br>
</br>

### 3. **🎨 RootActivity 시스템바 제어**
- **statusBarHeight/navigationBarHeight**: SDK 버전별 자동 계산
- **SystemBars 제어**: 투명/색상/아이콘 모드 한 줄 설정
- **beforeOnCreated()**: onCreate 전 초기화 훅 제공

<br>
</br>

### 4. **🎯 개발자 경험 최적화**
- **타입 안전성**: 컴파일 타임 오류 방지
- **일관된 패턴**: 팀 전체 동일한 MVVM 구조
- **빠른 개발**: 보일러플레이트 제거로 생산성 향상

<br>
</br>

### 5. **🔧 실수 방지**
- **LifecycleOwner 누락**: 자동 연결로 방지
- **메모리 누수**: Fragment nullable binding 자동 처리
- **채널 해제**: BaseViewModelEvent가 자동 관리

<br>
</br>

### 6. **📉 압도적인 코드 간소화**
- **Activity 초기화**: 20-30줄 → 10줄 미만 **70% 단축**
- **Fragment 초기화**: 40-50줄 → 15줄 미만 **70% 단축**
- **ViewModel 이벤트**: Channel 구성 10줄+ → sendEventVm() 한 줄

---

<br>
</br>

## 💡 개발자 후기

> **"DataBinding 설정을 더 이상 고민할 필요가 없어요!"**
>
> **"Fragment의 nullable binding 처리가 자동으로 되니 편해요!"**
>
> **"BaseViewModelEvent로 이벤트 시스템이 표준화되어 코드가 깔끔해졌어요!"**
>
> **"Activity/Fragment 초기화 코드가 70% 줄어들었습니다!"**
>
> **"statusBarHeight를 프로퍼티로 바로 접근할 수 있어서 편해요!"**
>
> **"SystemBars 제어가 한 줄로 끝나니 UI 구현이 빨라졌어요!"**

---

<br>
</br>

## 🎉 결론: MVVM 개발의 새로운 표준

**Simple UI MVVM**은 복잡한 Activity/Fragment 초기화를 **단순하고 강력하게** 만드는 혁신적인 라이브러리입니다.

✅ **DataBinding 자동화** - 복잡한 초기화를 생성자 파라미터로!
✅ **이벤트 시스템 완성** - Flow/Channel 구성을 자동으로!
✅ **SystemBars 제어** - statusBarHeight 등 시스템바 제어 한 줄로!
✅ **보일러플레이트 제거** - 70% 코드 간소화!

**전통적인 복잡함은 이제 그만.**
**Simple UI와 함께 생산적인 MVVM 개발을 경험하세요!** 🚀

---

<br>
</br>

## 실제 구현 예제보기

**라이브 예제 코드:**
> - Simple UI 예제: `app/src/main/java/kr/open/library/simpleui_xml/mvvm/new_/`
> - 순수 Android 예제: `app/src/main/java/kr/open/library/simpleui_xml/mvvm/origin/`
> - 실제로 앱을 구동시켜서 실제 구현 예제를 확인해 보세요!

<br>
</br>

**테스트 가능한 기능:**
- Activity + ViewModel 자동 초기화
- Fragment + ViewModel 자동 초기화
- DialogFragment 자동 초기화
- BaseViewModelEvent 이벤트 시스템
- DataBinding 자동 연동
- Lifecycle 자동 관리
- nullable binding 자동 처리
- statusBarHeight/navigationBarHeight 자동 계산
- SystemBars 제어 (투명/색상/아이콘 모드)

<br>
</br>


## 📚 BaseActivity vs BaseBindingActivity - 어떤 걸 선택할까?

Simple UI는 **두 가지 Base 클래스**를 제공합니다. 프로젝트 상황에 맞춰 선택하세요.

<br>
</br>

### 🎯 **선택 가이드**

| 구분 | BaseActivity | BaseBindingActivity |
|:--|:--|:--|
| **사용 시기** | 간단한 화면, DataBinding 불필요 | MVVM 패턴, 복잡한 데이터 바인딩 |
| **View 접근** | `findViewById()` 또는 ViewBinding | DataBinding (양방향 바인딩 가능) |
| **코드량** | 매우 간결 (레이아웃만 지정) | 간결 (Binding 자동 처리) |
| **ViewModel 연동** | 수동 연결 필요 | 자동 lifecycleOwner 설정 |
| **추천 용도** | 단순 UI, 설정 화면, 정적 페이지 | 데이터 기반 UI, 실시간 업데이트 |

<br>
</br>

### 💡 **BaseActivity - 간단한 화면용**

DataBinding이 필요 없는 간단한 화면에 적합합니다.

#### **특징**
- ✅ 레이아웃만 지정하면 자동으로 `setContentView()` 처리
- ✅ 매우 가벼움 (오버헤드 최소)
- ✅ findViewById() 또는 ViewBinding 직접 사용

#### **코드 예시**
```kotlin
class SettingsActivity : BaseActivity(R.layout.activity_settings) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // findViewById로 직접 접근
        val btnSave = findViewById<Button>(R.id.btnSave)
        btnSave.setOnClickListener {
            saveSettings()
        }
    }
}
```

**장점:**
- 코드 3~4줄로 Activity 완성
- DataBinding 오버헤드 없음
- 간단한 화면에 최적

<br>
</br>

### 🎨 **BaseBindingActivity - MVVM 패턴용**

DataBinding + ViewModel을 사용하는 MVVM 패턴에 적합합니다.

#### **특징**
- ✅ DataBinding 자동 설정 (inflate + setContentView + lifecycleOwner)
- ✅ ViewModel과 양방향 바인딩 가능
- ✅ XML에서 직접 데이터 표시 및 이벤트 처리

#### **코드 예시**
```kotlin
class MainActivity : BaseBindingActivity<ActivityMainBinding>(R.layout.activity_main) {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // binding은 자동으로 초기화됨
        // lifecycleOwner도 자동 설정됨
        binding.viewModel = viewModel

        // XML에서 직접 ViewModel 데이터 사용 가능
    }
}
```

**장점:**
- DataBinding 보일러플레이트 완전 제거
- lifecycleOwner 자동 설정
- XML에서 `@{viewModel.data}` 직접 사용

<br>
</br>

### 🤔 **어떤 걸 선택해야 할까?**

#### **BaseActivity를 선택하세요 👉**
- ✅ 간단한 정보 표시 화면
- ✅ 설정(Settings) 화면
- ✅ 정적 컨텐츠 페이지
- ✅ DataBinding이 과한 경우

#### **BaseBindingActivity를 선택하세요 👉**
- ✅ 실시간 데이터 업데이트가 필요한 화면
- ✅ ViewModel과 함께 MVVM 패턴 사용
- ✅ 복잡한 UI 상태 관리
- ✅ 양방향 데이터 바인딩 필요

<br>
</br>

### 📖 **Fragment도 동일한 패턴**

Fragment도 동일하게 두 가지 Base 클래스를 제공합니다:

- **BaseFragment** - 간단한 Fragment용
- **BaseBindingFragment** - MVVM 패턴용

```kotlin
// 간단한 Fragment
class SimpleFragment : BaseFragment(R.layout.fragment_simple) {
    // findViewById() 사용
}

// MVVM Fragment
class DataFragment : BaseBindingFragment<FragmentDataBinding>(R.layout.fragment_data) {
    private val viewModel: DataViewModel by viewModels()
    // DataBinding 사용
}
```

.
