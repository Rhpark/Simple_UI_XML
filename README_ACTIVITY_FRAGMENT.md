# 📘 Simple UI Activity & Fragment - Complete Guide 
> (Simple UI Activity & Fragment 전체 가이드)

Simple UI's Activity/Fragment base classes are productivity tools that eliminate repetitive initialization code and centralize permission handling, system bar management, and MVVM interactions in one place. This document provides the philosophy behind each base class, usage scenarios, and practical examples.
 > Simple UI의 Activity/Fragment 베이스 클래스는 반복되는 초기화 코드를 걷어내고, 권한/시스템바 처리, MVVM 상호 작용까지 한 곳에 모아둔 생산성 도구입니다. 이 문서는 각 베이스 클래스의 철학과 사용 시나리오, 실제 예제까지 상세히 정리했습니다.

### BaseBindingActivity Quick Setup (BaseBindingActivity 빠른 설정)
![mvvm_activity_init.gif](example_gif%2Fmvvm_activity_init.gif)

### BaseActivity Quick Setup (BaseActivity 빠른 설정)
![baseActivity.gif](example_gif%2FbaseActivity.gif)

**“Complete Activity / Fragment initialization in just three lines!”
>** – Simple UI는 순정 Android 대비 초기화 시간을 압축합니다.

Complete Activity/Fragment initialization in just 3 lines!" – See how much faster Simple UI is compared to vanilla Android
> **“Activity/Fragment 초기화를 단 3줄로 끝냅니다!”** – 순정 Android 대비 Simple UI가 얼마나 빠른지 바로 확인해 보세요.

<br></br>

## 🔎 At a Glance (한눈에 비교)

### Activity / Fragment Initialization
| Category  | Plain Android                     | Simple UI                                              |
|:--|:---------------------------------------------|:-------------------------------------------------------|
|  `setContentView` setup | Multiple boilerplate `onCreate` code | Automatically handled via constructor parameter        |
| DataBinding setup | Manual inflate +  `binding.lifecycleOwner`  assignment | Auto-assigned by BaseBinding classes                   | 
| Nullable Fragment binding |`_binding?` null safety + manual `onDestroyView` cleanup | Lifecycle automatically managed by BaseBindingFragment |
| Lifecycle observer connection | Manually write `lifecycleScope` / `repeatOnLifecycle` | Provides hook methods like `eventVmCollect()`            |
| MVVM glue code | Manually write ViewModel factory and binding connection | Instant injection with getViewModel() helper          |

### Permission Management (권한 관리)
| Category | Plain Android | Simple UI                                       |
|:-------------|:--|:------------------------------------------------|
| Request flow setup | Need to register/unregister `ActivityResultContract` | Single line call to `onRequestPermissions()`   |
| Special permission branching | Branch/exception handling for each permission |  `PermissionDelegate` automatically distinguishes  |
| Result  | Need to implement callback interface | Just handle the  `deniedPermissions` list          |
| State preservation | Manually implement `onSaveInstanceState` | Base class preserves internally                 |

### SystemBars Control (시스템바 제어)
| Category            | Plain Android | Simple UI |
|:--------------------|:--|:--|
| StatusBar height  | SDK branching + complex WindowInset calculation | `statusBarHeight` property instantly available | 
| NavigationBar height  | Need root view calculation logic | `navigationBarHeight` property instantly available | 
| Bar color/transparency control | 10+ lines of `WindowCompat` code | One-liner: `setStatusBarColor()`, `setSystemBarsColor()`  |
| API 35+ support | Manually add custom view | RootActivity already handles it |

<br></br>

## 💡 Why It Matters (왜 중요한가)
- **Faster Development:** Minimize Activity·Fragment initialization code to focus on core logic.
- **Fewer Mistakes:** Replace error-prone parts like binding null handling, permission flow, and system bar calculations with proven code.
- **Team Consistency:** All screens use the same base classes, making code reviews and onboarding easier.
- **Better Maintainability:** Manage common features in one place for faster OS upgrade responses.
- **Rapid Prototyping:** Turn new ideas into screens in minutes.

> - **Shorter development time / 개발 시간 단축:** Activity·Fragment 초기화 코드를 최소화하여 핵심 로직에 집중합니다.
> - **Fewer mistakes / 실수 감소:** Binding null 처리, 권한 흐름, 시스템바 계산 등 오류가 잦은 부분을 검증된 코드로 대체합니다.
> - **Consistent patterns / 팀 내 일관성:** 모든 화면이 동일한 베이스 클래스를 사용하므로 코드 리뷰와 온보딩이 쉬워집니다.
> - **Better maintainability / 유지보수성 향상:** 공통 기능을 한 곳에서 관리해 OS 업그레이드 대응이 빨라집니다.
> - **Rapid prototyping / 빠른 프로토타이핑:** 새로운 아이디어를 수분 만에 화면으로 옮길 수 있습니다.

<br></br>

## ⚙️ Required Setup (필수 설정)
To use Simple UI's Activity/Fragment base classes, you need to verify the minimal Gradle setup and XML structure.
> Simple UI의 Activity/Fragment 베이스 클래스를 사용하려면 최소한의 Gradle 설정과 XML 구조를 확인해야 합니다.

### ✅ `build.gradle.kts` configuration (`build.gradle.kts` 설정)
```kotlin
android {
    buildFeatures {
//        viewBinding = true // ViewBinding만 사용할 경우
        dataBinding = true   // BaseBindingActivity / BaseBindingFragment 사용 시 필수
    }
}
```


### 🔍 How to verify the setup (설정 검증 방법)
1. **Sync Gradle:** Run Gradle Sync after making changes.
2. **Rebuild project:** Go to `Build > Rebuild` to regenerate annotation output and generate binding classes.
3. **Check generated binding class:** Verify that `ActivityMainBinding` etc. are generated in the `build/generated/...`  path.

### ⚠️ Common pitfalls (자주 발생하는 문제)

#### 1. DataBinding not enabled (DataBinding 비활성화)
   - Symptom: BaseBindingActivity throws `UninitializedPropertyAccessException`
   - Solution: Make sure to add the `dataBinding = true` option and Sync.

#### 2. Missing `<layout>` tag in the layout file (레이아웃에 `<layout>` 태그 누락)
   - Symptom: `ActivitySomethingBinding` class is not generated
   - Solution: Wrap the root tag with `<layout>` and place `<data>` + actual view tree inside it.

#### 3. Gradle sync not executed (Gradle Sync 미실행)
   - Symptom: ViewBinding/DataBinding settings are not applied
   - Solution: Click `Sync Now` immediately after changing settings to build the new Binding classes.

<br></br>

## 🧱 Tier 1: BaseActivity / BaseFragment
### Key traits(핵심 특징)
- Keeps layout inflation minimal – only write  `setContentView(layoutRes)` or override `onCreateView`.
- Permission requests and system bar control are inherited directly from RootActivity/RootFragment.
- Lifecycle safe: You can use the beforeOnCreated() preprocessing hook.

### When to use(언제 사용?)
- Screens that don't use ViewBinding/DataBinding
- When an external SDK already controls its own view system
- Very lightweight demo/test screens

<br></br>

### Activity initialization comparison (Activity 초기화 비교)
| Category            | Plain Android | BaseActivity |
|:--------------------|:--|:--|
| Bind Layout | Call `setContentView` + manually create permission delegate | Pass layoutRes as constructor argument |
| Permission delegate | Manual field declaration | RootActivity automatically creates it | 
| System bar | Write utility for each screen | Use `setStatusBarColor()` immediately |

### Fragment initialization comparison (Fragment 초기화 비교)
| Category | Plain Android | BaseFragment |
|:--|:--|:--|
| `onCreateView` | 수동 inflate + container attach 여부 판단 | `return inflater.inflate(layoutRes, container, false)`만 작성 |
| Permission request | `registerForActivityResult` 필요 | `onRequestPermissions()` 상속 |
| Insets 처리 | ViewCompat 로직 | RootFragment가 이미 로직 보유 |

<br></br>

## 🧱 Tier 2: BaseBindingActivity / BaseBindingFragment (DataBinding 기반)

### Key features(주요 기능)
- Provides binding object as `protected val binding` and cleans up with lifecycle
- Automatically sets `binding.lifecycleOwner = this`
- Supports ViewModelProvider + SavedState with `getViewModel<T>()` method
- Provides UI event collection point using `eventVmCollect()` hook

### When to use(언제 사용?)
- MVVM + DataBinding View
- When binding object is required or using `@{viewModel...}`expressions in XML
- When you want to maintain the same pattern in DialogFragment / Fragment


### Activity Initialization Comparison
| Category         | Plain Android | BaseBindingActivity |
|:-----------------|:--|:--|
| Binding creation | Need `DataBindingUtil.setContentView`/cast | Complete by just passing layoutRes to constructor | 
| LifecycleOwner   | Manual assignment | Automatically assigned |
| bind ViewModel   | Write `ViewModelProvider` directly | Use `getViewModel()` helper |
| Event collection | Repeat `lifecycleScope.launch` | Common implementation inside `eventVmCollect()` |

### Fragment Initialization Comparison
| Category | Plain Android | BaseBindingFragment |
|:--|:--|:--|
| Nullable binding | `_binding` 관리 + `onDestroyView`에서 null 처리 | 내부에서 자동 정리 |
| ViewModel 범위 | `by viewModels()`/`activityViewModels()` 분기 | `getViewModel()` 선택 사용 |
| SavedState | 별도 Bundle 처리 | ViewModelProvider가 자동 처리 |

### MVVM Pattern Tip 
BaseBinding classes call `binding.setVariable()` and `binding.executePendingBindings()` inside `onCreateView()`, so you can use @{} expressions in XML right away. Also, override `eventVmCollect()` to safely receive one-time events flowing from the ViewModel.
> BaseBinding 계열은 `binding.setVariable()`과 `binding.executePendingBindings()`를 `onCreateView()` 내부에서 호출해주므로 XML의 `@{}` 표현식을 바로 사용할 수 있습니다. 또한 `eventVmCollect()`를 override하여 ViewModel에서 흘러오는 단발성 이벤트를 안전하게 수신하세요.

<br></br>

## 🔁 Third: Permission Request System (공통 권한 요청)
RootActivity/RootFragment have built-in `PermissionDelegate` to automatically handle permission requests and restoration.
> RootActivity/RootFragment는 `PermissionDelegate`를 내장하고 있어 권한 요청/복원을 자동으로 처리합니다.

### Permission Request Method Comparison (권한 요청 방식 비교)
| Category           | Plain Android | Simple UI |
|:-------------------|:--|:--|
| Request API        | `registerForActivityResult(RequestMultiplePermissions())` | `onRequestPermissions(listOf(...))` |
| State preservation | Manual Bundle storage | Delegate saves/restores | 
| Special permission | Write branching code directly | Delegate branches with predefined rules |

**Usage example / 사용 예시**
```kotlin
onRequestPermissions(
    permissions = listOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.READ_PHONE_STATE
    )
) { denied ->
    if (denied.isEmpty()) {
        startLocationTracking()
    } else {
        toastShort("승인 되지 않은 권한: $denied")
    }
}
```

<br></br>

## 🎨 Fourth: SystemBars Control - RootActivity
`RootActivity` provides common APIs to control StatusBar and NavigationBar.
>`RootActivity`는 StatusBar, NavigationBar를 제어하는 공용 API를 제공합니다.

### SystemBars Control Comparison (SystemBars 제어 비교)
| Category            | Plain Android | Simple UI (RootActivity) |
|:--------------------|:--|:--|
| StatusBar Color | Manipulate Window flags + Theme | `setStatusBarColor(color, isLight)` | 
| NavigationBar Color | Write WindowCompat logic directly |  `setNavigationBarColor(color, isLight)` | 
| Change both Bars simultaneously | Call each separately |  `setSystemBarsColor(color, isLightBars)` |
| Query Insets values | Need decorView calculation | `statusBarHeight`, `navigationBarHeight` properties |
| API 35+ support | Need to insert custom view | Already implemented inside RootActivity |

<br></br>

## 🧩 Base Class Features Summary (베이스 클래스 기능 정리)
#### Common RootActivity / RootFragment
- Automatic PermissionDelegate configuration

#### Only RootActivity
- get statusBar, navigationBar, size
- change statusBar, navigationBar color
- `attachRootContentView()` Util
- `beforeOnCreated()` Hook

#### BaseActivity / BaseFragment
- Lightest layer, only pass layout resource
- BaseFragment controls attach behavior with `isAttachToParent` flag

#### BaseBindingActivity / BaseBindingFragment
- Exposes binding object and manages lifecycle
- Provides `getViewModel()`, `eventVmCollect()`
- Extends same pattern to `BaseBindingDialogFragment`
<br></br>

## ⚙️ Advanced Features – Initialization Callbacks (고급 초기화 콜백)
### RootActivity - `beforeOnCreated(savedInstanceState: Bundle?)`
- **Call timing:** Right before super.onCreate()
- **Use case:** Theme switching, pre-applying Window flags, Logger initialization

```kotlin
override fun beforeOnCreated(savedInstanceState: Bundle?) {
    setStatusBarTransparent()
    Logx.d("Before onCreate executed")
}
```

### BaseBindingActivity - `onCreateView(rootView: View, savedInstanceState: Bundle?)`
- **Call timing:** After Binding inflate , Before `setContentView`  
- **Use case:** `binding.viewModel = vm`, Init View 
```kotlin
override fun onCreateView(rootView: View, savedInstanceState: Bundle?) {
    binding.vm = viewModel
    binding.recyclerview.adapter = listAdapter
}
```

### BaseBindingFragment - `afterOnCreateView(rootView: View, savedInstanceState: Bundle?)`
- **Call timing:** After onCreateView(..)  
- **Use case:** config Transaction, ex childFragmentManager
```kotlin
override fun afterOnCreateView(rootView: View, savedInstanceState: Bundle?) {
    childFragmentManager.beginTransaction()
        .replace(R.id.container, DetailFragment.newInstance())
        .commit()
}
```

### 🪟 BaseBindingDialogFragment same BaseBindingFragment!
DialogFragment also overrides `onCreateView()`, `eventVmCollect()`, etc. in the same way to provide an identical coding experience with Activity/Fragment.
> DialogFragment 역시 `onCreateView()`, `eventVmCollect()` 등을 동일하게 override하여 Activity/Fragment와 완벽히 동일한 코딩 경험을 제공합니다.

<br></br>

## 🔄 Initialization Flow Summary (초기화 흐름 요약)
### Activity
1. `beforeOnCreated()` – Ready to Window/Theme   
2. `onCreate()` – Ready to RootActivity Permission delegate  
3. (BaseBindingActivity) `onCreateView()` – Binding inflate & viewModel bind  
4. `eventVmCollect()` – event Collect form Viewmodel to Activity  
5. `onDestroy()` – Binding unBind

### Fragment
1. `onCreate()` – RootFragment 권한 delegate 준비  
2. `onCreateView()` – Layout inflate (BaseBinding이면 Binding 생성)  
3. `afterOnCreateView()` – UI initialization 
4. `eventVmCollect()` –event Collect form Viewmodel to Fragment
5. `onDestroyView()` – Binding/리소스 자동 정리

<br></br>

## ⭐ Core Advantages of Simple UI Activity/Fragment (핵심 장점)
1. **Overwhelming code simplification / 압도적인 코드 단순화**  
2. **Automated boilerplate handling / 반복 코드 자동화**  
3. **Unified permission management / 일원화된 권한 관리**  
4. **Easy SystemBars control / 쉬운 시스템바 제어**  
5. **Optimized developer experience / 개발자 경험 최적화**  
6. **Mistake prevention / 휴먼 에러 방지**

<br></br>

## 🗣️ Developer Reviews (사용자 후기)
- "Every time I create a new screen, I just copy-paste the BaseBindingActivity template and I'm done—it's more than twice as fast."
- "Permission requests and system bar code are the same across the entire team, making reviews much easier."
- "Being able to manage even DialogFragments with the same pattern has made maintenance easier than I ever imagined."
> - “새 화면을 만들 때마다 BaseBindingActivity 템플릿을 복붙하면 끝이라 작업 속도가 2배 이상 빨라졌습니다.”
> - “권한 요청/시스템바 코드가 팀 전체에서 동일하니 리뷰가 쉬워졌어요.”
> - “DialogFragment까지 동일한 패턴으로 관리할 수 있어 유지보수가 상상 이상으로 편해졌습니다.”

<br></br>

## ✅ Conclusion: A New Standard (결론)
Simple UI Activity/Fragment base classes set a new standard for Android UI development by formalizing repetitive setup tasks. Once configured, all screens move to the same rhythm, boosting team-wide productivity.
> Simple UI Activity/Fragment 베이스 클래스는 **반복되는 세팅 작업을 공식화**함으로써 안드로이드 UI 개발의 새로운 표준을 제시합니다. 한 번 세팅하면 모든 화면이 동일한 리듬으로 움직이며, 팀 전체 생산성을 끌어올립니다.

<br></br>

## 🧭 Selection Guide: Which Base Class Should I Use? (선택 가이드)

### Selection Guide Table (선택표)
|Requirements	|Recommended Base Class|
|:--|:--|
|Lightest Activity/Fragment, no ViewBinding	|  `BaseActivity`, `BaseFragment` |
|DataBinding + MVVM	|  `BaseBindingActivity`, `BaseBindingFragment`|
|DialogFragment + Binding|	`BaseBindingDialogFragment`|
|system bar control|	`RootActivity`|
|permission requests|	 `RootActivity`, `RootFragment`|


### Which One Should I Choose? (어떤 것을 선택할까?)
#### Choose BaseActivity / BaseFragment
- Quick demo screens/빠른 데모 화면
- When not using DataBinding syntax in XML/XML에서 DataBinding 문법을 사용하지 않는 경우
- When mixing with custom view libraries/커스텀 뷰 라이브러리와 혼용할 때


#### Choose BaseBindingActivity / BaseBindingFragment
- Using MVVM + LiveData/StateFlow/ MVVM + LiveData/StateFlow 사용
- When binding safety and event hooks are needed/ Binding 안전성과 이벤트 훅이 필요한 경우
- When you want to maintain the same code style in Dialog/Fragment/ Dialog/Fragment에서도 동일한 코드 스타일을 유지하고 싶은 경우


### Advanced Parameter: `isAttachToParent`
#### Constructor Signature (생성자 시그니처)
```kotlin
abstract class BaseFragment(
    @LayoutRes private val layoutRes: Int,
    private val isAttachToParent: Boolean = false
)
```
#### What is `isAttachToParent`?
- Works identically to the third parameter of LayoutInflater.inflate(layoutRes, container, isAttachToParent).
> - `LayoutInflater.inflate(layoutRes, container, isAttachToParent)`의 세 번째 파라미터와 동일하게 동작합니다.

#### When to use `true`? (언제 true를 쓸까?)
- When a custom ViewGroup needs to control the attach process directly/ 커스텀 ViewGroup이 attach 과정을 직접 제어해야 하는 경우
- Special cases where the Fragment container requires attach status in advance/ Fragment 컨테이너가 attach 여부를 미리 요구하는 특수 케이스

#### Usage example (사용 예시)
```kotlin
class CustomFragment : BaseFragment(
    layoutRes = R.layout.fragment_custom,
    isAttachToParent = true
)
```

<br></br>

## 🧠 Are You Using MVVM Pattern? (MVVM 패턴 활용)
### `getViewModel()` - Easy ViewModel Creation (간편 ViewModel 생성)
```kotlin
class SampleActivity :
    BaseBindingActivity<ActivitySampleBinding>(R.layout.activity_sample) {

    private val vm: SampleViewModel by lazy { getViewModel() }

    override fun onCreateView() {
        binding.vm = vm
    }
}
```
- SavedStateHandle까지 자동 연결되어 Configuration 변화에도 안전합니다.

### `eventVmCollect()` - ViewModel Event Subscription (ViewModel 이벤트 수집)
```kotlin
override fun eventVmCollect() {
    lifecycleScope.launch {
        vm.eventFlow.collect { event ->
            when (event) {
                is SampleEvent.ShowToast -> toastShort(event.message)
            }
        }
    }
}
```
- Activity automatically connects to lifecycleScope, Fragment to viewLifecycleOwner.lifecycleScope, allowing safe handling of one-time events.
> - Activity는 `lifecycleScope`, Fragment는 `viewLifecycleOwner.lifecycleScope`로 자동 연결해 단발성 이벤트를 안전하게 처리할 수 있습니다.

<br></br>

## 👀 View Real Implementation Examples (실제 구현 예제)
- `app/src/main/java/kr/open/library/simpleui_xml/activity_fragment/activity/BaseBindingActivityExample.kt`
- `app/src/main/java/kr/open/library/simpleui_xml/activity_fragment/fragment/FragmentContainerActivity.kt`
- `app/src/main/java/kr/open/library/simpleui_xml/activity_fragment/ActivityFragmentActivity.kt`

Run the actual app module to see each base class in action with your own eyes. Don't stop at the README—run the code yourself!
> 실제 앱 모듈을 실행하면 각 베이스 클래스의 동작을 눈으로 확인할 수 있습니다. README에서 끝나지 말고 코드를 직접 실행해 보세요!
