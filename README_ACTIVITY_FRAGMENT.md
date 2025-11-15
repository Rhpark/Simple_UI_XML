# 📘 Simple UI Activity & Fragment - Complete Guide (Simple UI Activity & Fragment 전체 가이드)

Simple UI의 Activity/Fragment 베이스 클래스는 반복되는 초기화 코드를 걷어내고, 권한/시스템바 처리, MVVM 상호 작용까지 한 곳에 모아둔 생산성 도구입니다. 이 문서는 각 베이스 클래스의 철학과 사용 시나리오, 실제 예제까지 **영문/한글**로 상세히 정리했습니다.

### BaseBindingActivity Quick Setup (BaseBindingActivity 빠른 설정)
![mvvm_activity_init.gif](example_gif%2Fmvvm_activity_init.gif)

### BaseActivity Quick Setup (BaseActivity 빠른 설정)
![baseActivity.gif](example_gif%2FbaseActivity.gif)

> **“Complete Activity / Fragment initialization in just three lines!”** – Simple UI는 순정 Android 대비 초기화 시간을 압축합니다.  
> **“Activity/Fragment 초기화를 단 3줄로 끝냅니다!”** – 순정 Android 대비 Simple UI가 얼마나 빠른지 바로 확인해 보세요.

<br></br>

## 🔎 At a Glance (한눈에 비교)

### Activity / Fragment Initialization
| Category (항목) | Plain Android (순정 Android) | Simple UI |
|:--|:--|:--|
| `setContentView` setup | Boilerplate `onCreate` 코드 다수 | 생성자 파라미터로 자동 처리 |
| DataBinding setup | 수동 inflate + `binding.lifecycleOwner` 지정 | BaseBinding 계열이 자동 지정 |
| Nullable Fragment binding | `_binding?` 안전 처리 + `onDestroyView` 정리 필요 | BaseBindingFragment가 생명주기 자동 관리 |
| Lifecycle observer 연결 | 직접 `lifecycleScope` / `repeatOnLifecycle` 작성 | `eventVmCollect()` 등 후킹 메서드 제공 |
| MVVM glue code | ViewModel 팩토리와 binding 연결 직접 작성 | `getViewModel()` 헬퍼로 즉시 주입 |

### Permission Management (권한 관리)
| Category (항목) | Plain Android | Simple UI |
|:--|:--|:--|
| 요청 흐름 구성 | `ActivityResultContract` 등록/해제 코드 필요 | `onRequestPermissions()` 한 줄 호출 |
| 특별 권한 분기 | 권한마다 분기/예외 처리 | `PermissionDelegate`가 자동 구분 |
| 결과 전달 | 콜백 인터페이스 구현 필요 | `deniedPermissions` 리스트만 받아 처리 |
| 상태 저장 | `onSaveInstanceState` 직접 구현 | Base 클래스가 내부적으로 보존 |

### SystemBars Control (시스템바 제어)
| Category (항목) | Plain Android | Simple UI |
|:--|:--|:--|
| StatusBar 높이 계산 | SDK 분기 + 복잡한 WindowInset 계산 | `statusBarHeight` 프로퍼티 즉시 제공 |
| NavigationBar 높이 계산 | 루트 뷰 계산 로직 필요 | `navigationBarHeight` 프로퍼티 즉시 제공 |
| Bar 색/투명도 제어 | 10+ 줄 `WindowCompat` 코드 | `setStatusBarColor()`, `setSystemBarsColor()` 원라인 |
| API 35+ 지원 | 직접 커스텀 뷰 추가 | RootActivity가 이미 대응 |

<br></br>

## 💡 Why It Matters (왜 중요한가)
- **Shorter development time / 개발 시간 단축:** Activity·Fragment 초기화 코드를 최소화하여 핵심 로직에 집중합니다.
- **Fewer mistakes / 실수 감소:** Binding null 처리, 권한 흐름, 시스템바 계산 등 오류가 잦은 부분을 검증된 코드로 대체합니다.
- **Consistent patterns / 팀 내 일관성:** 모든 화면이 동일한 베이스 클래스를 사용하므로 코드 리뷰와 온보딩이 쉬워집니다.
- **Better maintainability / 유지보수성 향상:** 공통 기능을 한 곳에서 관리해 OS 업그레이드 대응이 빨라집니다.
- **Rapid prototyping / 빠른 프로토타이핑:** 새로운 아이디어를 수분 만에 화면으로 옮길 수 있습니다.

<br></br>

## ⚙️ Required Setup (필수 설정)
Simple UI의 Activity/Fragment 베이스 클래스를 사용하려면 최소한의 Gradle 설정과 XML 구조를 확인해야 합니다.

### ✅ `build.gradle.kts` configuration (`build.gradle.kts` 설정)
```kotlin
android {
    buildFeatures {
        viewBinding = true
        dataBinding = true   // BaseBindingActivity / BaseBindingFragment 사용 시 필수
    }
}
```
- **EN:** Enable both ViewBinding and DataBinding so that BaseBinding 계열이 올바르게 동작합니다.  
- **KO:** BaseBinding 계열은 DataBinding 기반으로 작성되었으므로 두 옵션을 모두 켜야 합니다.

### 🔍 How to verify the setup (설정 검증 방법)
1. **Sync Gradle** – After editing the Gradle file, run *Sync Project* / 변경 후 Gradle Sync를 실행합니다.  
2. **Rebuild project** – `Build > Rebuild`로 annotation output을 재생성합니다 / 리빌드로 바인딩 클래스를 생성합니다.  
3. **Check generated binding class** – `build/generated/...` 경로에 `ActivityMainBinding` 등이 생성됐는지 확인합니다 / 생성된 바인딩 클래스를 확인합니다.

### ⚠️ Common pitfalls (자주 발생하는 문제)
#### 1. DataBinding not enabled (DataBinding 미활성화)
- 증상: BaseBindingActivity가 `UninitializedPropertyAccessException`을 던짐  
- 해결: `dataBinding = true` 옵션을 반드시 추가하고 Sync 합니다.

#### 2. Missing `<layout>` tag in the layout file (레이아웃에 `<layout>` 태그 누락)
- 증상: `ActivitySomethingBinding` 클래스가 생성되지 않음  
- 해결: 루트 태그를 `<layout>`으로 감싸고 그 안에 `<data>` + 실제 뷰 트리를 배치합니다.

#### 3. Gradle sync not executed (Gradle Sync 미실행)
- 증상: ViewBinding/DataBinding 설정이 반영되지 않음  
- 해결: 설정 변경 직후 `Sync Now`를 눌러야 새로운 Binding 클래스가 빌드됩니다.

<br></br>

## 🧱 Tier 1: BaseActivity / BaseFragment (기본 ViewBinding 없음)
### Key traits (핵심 특징)
- **EN:** Keeps layout inflation minimal – only `setContentView(layoutRes)` 또는 `onCreateView` 오버라이드만 작성합니다.  
- **KO:** 권한 요청, 시스템바 제어는 RootActivity/RootFragment에서 그대로 상속합니다.
- **Lifecycle safe:** `beforeOnCreated()` 전처리 Hook를 사용할 수 있습니다.

### When to use (언제 사용?)
- ViewBinding/DataBinding을 사용하지 않는 화면
- 외부 SDK가 이미 자체 뷰 시스템을 제어하는 경우
- 매우 가벼운 데모/테스트 화면

### Activity initialization comparison (Activity 초기화 비교)
| 항목 | Plain Android | BaseActivity |
|:--|:--|:--|
| Layout 연결 | `setContentView` 호출 + 권한 delegate 직접 생성 | 생성자 인자로 layoutRes 전달 |
| Permission delegate | 수동 필드 선언 | RootActivity가 자동 생성 |
| System bar | 각 화면마다 유틸 작성 | `setStatusBarColor()` 즉시 사용 |

### Fragment initialization comparison (Fragment 초기화 비교)
| 항목 | Plain Android | BaseFragment |
|:--|:--|:--|
| `onCreateView` | 수동 inflate + container attach 여부 판단 | `return inflater.inflate(layoutRes, container, false)`만 작성 |
| Permission request | `registerForActivityResult` 필요 | `onRequestPermissions()` 상속 |
| Insets 처리 | ViewCompat 로직 | RootFragment가 이미 로직 보유 |

<br></br>

## 🧱 Tier 2: BaseBindingActivity / BaseBindingFragment (DataBinding 기반)
### Key features (주요 기능)
- Binding 객체를 `protected val binding` 으로 제공하고 생명주기와 함께 정리
- `binding.lifecycleOwner = this` 자동 지정
- `getViewModel<T>()` 메서드로 ViewModelProvider + SavedState 지원
- `eventVmCollect()` Hook를 활용한 UI 이벤트 수집 지점 제공

### When to use (언제 사용?)
- MVVM + DataBinding 화면
- Binding 객체가 반드시 필요하거나, XML에서 `@{viewModel...}` 표현식을 사용하는 경우
- DialogFragment / Fragment에서도 동일 패턴을 유지하고 싶은 경우

### Activity Initialization Comparison
| 항목 | Plain Android | BaseBindingActivity |
|:--|:--|:--|
| Binding 생성 | `DataBindingUtil.setContentView`/cast 필요 | 생성자 layoutRes 전달만으로 완료 |
| LifecycleOwner | 수동 지정 | 자동 지정 |
| ViewModel 연결 | `ViewModelProvider` 직접 작성 | `getViewModel()` 헬퍼 사용 |
| 이벤트 수집 | `lifecycleScope.launch` 반복 | `eventVmCollect()` 내에서 공통 구현 |

### Fragment Initialization Comparison
| 항목 | Plain Android | BaseBindingFragment |
|:--|:--|:--|
| Nullable binding | `_binding` 관리 + `onDestroyView`에서 null 처리 | 내부에서 자동 정리 |
| ViewModel 범위 | `by viewModels()`/`activityViewModels()` 분기 | `getViewModel()` 선택 사용 |
| SavedState | 별도 Bundle 처리 | ViewModelProvider가 자동 처리 |

### MVVM Pattern Tip (MVVM 활용 팁)
BaseBinding 계열은 `binding.setVariable()`과 `binding.executePendingBindings()`를 `onCreateView()` 내부에서 호출해주므로 XML의 `@{}` 표현식을 바로 사용할 수 있습니다. 또한 `eventVmCollect()`를 override하여 ViewModel에서 흘러오는 단발성 이벤트를 안전하게 수신하세요.

<br></br>

## 🔁 Third: Permission Request System (공통 권한 요청)
RootActivity/RootFragment는 `PermissionDelegate`를 내장하고 있어 권한 요청/복원을 자동으로 처리합니다.

### Permission Request Method Comparison (권한 요청 방식 비교)
| 항목 | Plain Android | Simple UI |
|:--|:--|:--|
| 요청 API | `registerForActivityResult(RequestMultiplePermissions())` | `onRequestPermissions(listOf(...))` |
| 상태 저장 | Bundle 수동 보관 | Delegate가 저장/복원 |
| Special permission | 분기 코드 직접 작성 | Delegate가 미리 정의된 규칙으로 분기 |

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
        toastShort("권한이 필요합니다: $denied")
    }
}
```

<br></br>

## 🎨 Fourth: SystemBars Control (RootActivity)
`RootActivity`는 StatusBar, NavigationBar를 제어하는 공용 API를 제공합니다.

### SystemBars Control Comparison (SystemBars 제어 비교)
| 항목 | Plain Android | Simple UI (RootActivity) |
|:--|:--|:--|
| StatusBar 색상 | Window flags + Theme 조작 | `setStatusBarColor(color, isLight)` |
| NavigationBar 색상 | WindowCompat 로직 직접 작성 | `setNavigationBarColor(color, isLight)` |
| 두 Bar 동시에 변경 | 각자 호출 | `setSystemBarsColor(color, isLightBars)` |
| Insets 값 조회 | decorView 계산 필요 | `statusBarHeight`, `navigationBarHeight` 프로퍼티 |
| API 35 이상 지원 | 커스텀 뷰 삽입 필요 | RootActivity 내부 구현 완료 |

<br></br>

## 🧩 Base Class Features Summary (베이스 클래스 기능 정리)
#### RootActivity / RootFragment (공통)
- PermissionDelegate 자동 구성
- `statusBarHeight`, `navigationBarHeight`
- `setStatusBarTransparent()`, `setSystemBarsColor()`
- `beforeOnCreated()` Hook 제공

#### RootActivity 전용
- WindowInsets 대응, API 35+ 커스텀 StatusBar 뷰 주입
- `attachRootContentView()` 유틸

#### BaseActivity / BaseFragment
- 가장 가벼운 레이어, 레이아웃 리소스만 전달
- BaseFragment는 `isAttachToParent` 플래그로 attach 여부 제어

#### BaseBindingActivity / BaseBindingFragment
- Binding 객체 노출 및 생명주기 관리
- `getViewModel()`, `eventVmCollect()` 제공
- `BaseBindingDialogFragment`까지 동일 패턴 확장

<br></br>

## ⚙️ Advanced Features – Initialization Callbacks (고급 초기화 콜백)
### RootActivity - `beforeOnCreated()`
- **Call timing:** `super.onCreate()` 직전  
- **Use case:** Theme 교체, Window 플래그 선적용, Logger 초기화
```kotlin
override fun beforeOnCreated(savedInstanceState: Bundle?) {
    setStatusBarTransparent()
    Logx.d("Before onCreate executed")
}
```

### BaseBindingActivity - `onCreateView()`
- **Call timing:** Binding inflate 직후, `setContentView` 이전  
- **Use case:** `binding.viewModel = vm`, RecyclerView adapter 연결
```kotlin
override fun onCreateView() {
    binding.vm = viewModel
    binding.recyclerview.adapter = listAdapter
}
```

### BaseBindingFragment - `afterOnCreateView()`
- **Call timing:** Fragment View 생성 후  
- **Use case:** childFragmentManager 트랜잭션, Transition 설정
```kotlin
override fun afterOnCreateView() {
    childFragmentManager.beginTransaction()
        .replace(R.id.container, DetailFragment.newInstance())
        .commit()
}
```

### 🪟 BaseBindingDialogFragment도 동일한 패턴!
DialogFragment 역시 `onCreateView()`, `eventVmCollect()` 등을 동일하게 override하여 Activity/Fragment와 완벽히 동일한 코딩 경험을 제공합니다.

<br></br>

## 🔄 Initialization Flow Summary (초기화 흐름 요약)
### Activity
1. `beforeOnCreated()` – Window/Theme 준비  
2. `onCreate()` – RootActivity가 권한 delegate 준비  
3. (BaseBindingActivity) `onCreateView()` – Binding inflate & viewModel 연결  
4. `eventVmCollect()` – UI 단발 이벤트 수집  
5. `onDestroy()` – Binding 자동 해제

### Fragment
1. `onCreate()` – RootFragment 권한 delegate 준비  
2. `onCreateView()` – Layout inflate (BaseBinding이면 Binding 생성)  
3. `afterOnCreateView()` – 추가 UI 구성  
4. `eventVmCollect()` – ViewLifecycleOwner로 collect  
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
- “새 화면을 만들 때마다 BaseBindingActivity 템플릿을 복붙하면 끝이라 작업 속도가 2배 이상 빨라졌습니다.”  
- “권한 요청/시스템바 코드가 팀 전체에서 동일하니 리뷰가 쉬워졌어요.”  
- “DialogFragment까지 동일한 패턴으로 관리할 수 있어 유지보수가 상상 이상으로 편해졌습니다.”

<br></br>

## ✅ Conclusion: A New Standard (결론)
Simple UI Activity/Fragment 베이스 클래스는 **반복되는 세팅 작업을 공식화**함으로써 안드로이드 UI 개발의 새로운 표준을 제시합니다. 한 번 세팅하면 모든 화면이 동일한 리듬으로 움직이며, 팀 전체 생산성을 끌어올립니다.

<br></br>

## 🧭 Selection Guide: Which Base Class Should I Use? (선택 가이드)

### Selection Guide Table (선택표)
| 요구 사항 | 추천 베이스 클래스 |
|:--|:--|
| 가장 가벼운 Activity/Fragment, ViewBinding 미사용 | `BaseActivity`, `BaseFragment` |
| DataBinding + MVVM | `BaseBindingActivity`, `BaseBindingFragment` |
| DialogFragment + Binding | `BaseBindingDialogFragment` |
| 시스템바 제어/권한 요청만 필요 | `RootActivity`, `RootFragment` |

### Which One Should I Choose? (어떤 것을 선택할까?)
#### Choose BaseActivity / BaseFragment
- 빠른 데모 화면
- XML에서 DataBinding 문법을 사용하지 않는 경우
- 커스텀 뷰 라이브러리와 혼용할 때

#### Choose BaseBindingActivity / BaseBindingFragment
- MVVM + LiveData/StateFlow 사용
- Binding 안전성과 이벤트 훅이 필요한 경우
- Dialog/Fragment에서도 동일한 코드 스타일을 유지하고 싶은 경우

### Advanced Parameter: `isAttachToParent`
#### Constructor Signature (생성자 시그니처)
```kotlin
abstract class BaseFragment(
    @LayoutRes private val layoutRes: Int,
    private val isAttachToParent: Boolean = false
)
```
#### What is `isAttachToParent`?
- **EN:** Controls whether the inflated view is attached to the parent immediately.  
- **KO:** `LayoutInflater.inflate(layoutRes, container, isAttachToParent)`의 세 번째 파라미터와 동일하게 동작합니다.

#### When to use `true`? (언제 true를 쓸까?)
- 커스텀 ViewGroup이 attach 과정을 직접 제어해야 하는 경우  
- Fragment 컨테이너가 attach 여부를 미리 요구하는 특수 케이스

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
- Activity는 `lifecycleScope`, Fragment는 `viewLifecycleOwner.lifecycleScope`로 자동 연결해 단발성 이벤트를 안전하게 처리할 수 있습니다.

<br></br>

## 👀 View Real Implementation Examples (실제 구현 예제)
- `app/src/main/java/kr/open/library/simpleui_xml/activity_fragment/activity/BaseBindingActivityExample.kt`
- `app/src/main/java/kr/open/library/simpleui_xml/activity_fragment/fragment/FragmentContainerActivity.kt`
- `app/src/main/java/kr/open/library/simpleui_xml/activity_fragment/ActivityFragmentActivity.kt`

실제 앱 모듈을 실행하면 각 베이스 클래스의 동작을 눈으로 확인할 수 있습니다. README에서 끝나지 말고 코드를 직접 실행해 보세요!
