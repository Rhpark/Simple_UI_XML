# 📱 Simple UI Activity & Fragment - 완벽 가이드

### BaseBindingActivity 초기 설정
![mvvm_activity_init.gif](example_gif%2Fmvvm_activity_init.gif)

### BaseActivity 초기 설정
![baseActivity.gif](example_gif%2FbaseActivity.gif)
> **"Activity/Fragment 초기화를 3줄로 끝내자!"** 기존 순수 Android 개발 대비 Simple UI가 주는 체감 차이를 한눈에 확인하세요.

<br>
</br>

## 🔎 한눈 비교 (At a glance)

<br>
</br>

### Activity/Fragment 초기화

| 항목 | 순수 Android | Simple UI |
|:--|:--:|:--:|
| setContentView 설정 | 수동 설정 (3줄+) | 생성자 파라미터로 자동 ✅ |
| DataBinding 설정 | 수동 inflate + setContentView (7줄+) | 생성자 파라미터로 자동 ✅ |
| LifecycleOwner 설정 | 수동 binding.lifecycleOwner 설정 | 자동 연동 ✅ |
| Fragment nullable binding | 수동 처리 (_binding?, onDestroyView) | 자동 관리 ✅ |
| onCreate 보일러플레이트 | 복잡한 초기화 코드 | 최소화된 코드 ✅ |

<br>
</br>

### 권한 관리
| 항목 | 순수 Android | Simple UI |
|:--|:--:|:--:|
| 권한 요청 방식 | ActivityResultContract 수동 등록 | onRequestPermissions() 한 줄 ✅ |
| 특수 권한 처리 | 별도 분리 로직 (50줄+) | 자동 구분 처리 ✅ |
| 권한 결과 처리 | 수동 콜백 구현 | 통합 콜백 제공 ✅ |
| 개발자 경험 | 복잡한 보일러플레이트 | 간결한 라이브러리 호출 ✅ |

<br>
</br>

### SystemBars 제어
| 항목 | 순수 Android | Simple UI |
|:--|:--:|:--:|
| StatusBar 높이 계산 | 수동 계산 (SDK 분기 필요) | statusBarHeight 프로퍼티로 자동 ✅ |
| NavigationBar 높이 계산 | 수동 계산 (복잡한 로직) | navigationBarHeight 프로퍼티로 자동 ✅ |
| StatusBar 투명 설정 | WindowManager 수동 설정 (10줄+) | setStatusBarTransparent() 한 줄 ✅ |
| StatusBar 색상 설정 | WindowCompat 수동 처리 | setStatusBarColor() 한 줄 ✅ |
| NavigationBar 색상 설정 | WindowCompat 수동 처리 | setNavigationBarColor() 한 줄 ✅ |
| SystemBars 동시 설정 | 개별 설정 필요 | setSystemBarsColor() 한 줄 ✅ |

> **핵심:** Simple UI는 "복잡한 Activity/Fragment 보일러플레이트"를 **자동화**를 통해 개발 속도를 향상시킵니다.

<br>
</br>

## 💡 왜 중요한가:

- **개발 시간 단축**: Activity/Fragment 초기화 보일러플레이트 제거로 핵심 로직에 집중 가능
- **실수 방지**: DataBinding 설정, nullable binding 처리 등에서 발생하는 버그 예방
- **일관된 패턴**: 팀 전체가 동일한 Activity/Fragment 구조 사용
- **유지보수성**: 표준화된 베이스 클래스로 코드 유지보수 용이
- **빠른 프로토타이핑**: 아이디어를 바로 구현하여 테스트 가능

<br>
</br>

## ⚙️ **필수 설정**

Simple UI의 Activity/Fragment를 사용하려면 기본 설정이 필요합니다.

### 📦 build.gradle.kts 설정

**BaseBindingActivity**와 **BaseBindingFragment**를 사용하려면 **DataBinding 활성화가 필수**입니다.

> **참고**: `BaseActivity`와 `BaseFragment`는 DataBinding 없이도 사용 가능합니다.

**Module-level build.gradle.kts**에 다음 설정을 추가하세요:

```kotlin
android {
    buildFeatures {
        dataBinding = true  // BaseBindingActivity/Fragment 사용 시 필수!
    }
}
```

<br>

### ✅ 설정 확인 방법

DataBinding이 올바르게 설정되었는지 확인하려면:

1. **Sync Gradle** 실행
2. **Rebuild Project** 실행
3. 레이아웃 파일이 `<layout>` 태그로 감싸져 있는지 확인:

```xml
<!-- activity_main.xml -->
<layout xmlns:android="http://schemas.android.com/apk/res/android">
    <data>
        <!-- ViewModel 바인딩 (선택사항) -->
        <variable
            name="vm"
            type="com.example.MainViewModel" />
    </data>
    <LinearLayout
        style="@style/Layout.AllMatch.Vertical">
        <!-- UI 요소들 -->
    </LinearLayout>
</layout>
```

4. Build 성공 후 `ActivityMainBinding` 클래스가 자동 생성되는지 확인

<br>

### 🚨 자주 발생하는 오류

#### ❌ DataBinding 미활성화
```
Unresolved reference: ActivityMainBinding
```
**해결방법**: `build.gradle.kts`에 `dataBinding = true` 추가 후 Sync Gradle

#### ❌ 레이아웃 파일 `<layout>` 태그 누락
```
Cannot find symbol class ActivityMainBinding
```
**해결방법**: XML 파일을 `<layout>` 태그로 감싸기

#### ❌ Gradle Sync 미실행
```
DataBindingUtil not found
```
**해결방법**: **File → Sync Project with Gradle Files** 실행

<br>
</br>

## 🎯 첫째: BaseActivity/BaseFragment (간단한 화면용)

**BaseActivity**와 **BaseFragment**는 DataBinding이 필요 없는 간단한 화면에 적합합니다.

<br>

### 💡 **특징**
- ✅ 레이아웃만 지정하면 자동으로 `setContentView()` 처리 (Activity)
- ✅ 레이아웃만 지정하면 자동으로 inflate 처리 (Fragment)
- ✅ Fragment는 `rootView` 프로퍼티 자동 제공
- ✅ 매우 가벼움 (오버헤드 최소)
- ✅ findViewById() 또는 ViewBinding 직접 사용
- ✅ DataBinding 불필요

<br>

### 📌 **언제 사용하나요?**
- ✅ 간단한 정보 표시 화면
- ✅ 설정(Settings) 화면
- ✅ 정적 컨텐츠 페이지
- ✅ DataBinding이 과한 경우

<br>
</br>

### Activity 초기화 비교

<details>
<summary><strong>순수 Android - Activity 수동 초기화</strong></summary>

```kotlin
class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. setContentView 수동 설정
        setContentView(R.layout.activity_settings)

        // 2. findViewById로 뷰 접근
        val btnPermissions = findViewById<Button>(R.id.btnPermissions)
        val btnNotification = findViewById<Button>(R.id.btnNotification)
        val tvVersion = findViewById<TextView>(R.id.tvVersion)

        // 3. 클릭 리스너 설정
        btnPermissions.setOnClickListener {
            requestPermissions()
        }

        btnNotification.setOnClickListener {
            openNotificationSettings()
        }

        // 4. 초기 데이터 설정
        tvVersion.text = "v1.0.0"
    }

    private fun requestPermissions() {
        // 권한 요청 로직
    }

    private fun openNotificationSettings() {
        // 알림 설정 열기
    }
}
```
**문제점:** 반복적인 setContentView, 권한요청/승인 복잡
</details>

<details>
<summary><strong>Simple UI - Activity 자동 초기화</strong></summary>

```kotlin
class SettingsActivity : BaseActivity(R.layout.activity_settings) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // setContentView 자동 처리! ✅

        // findViewById로 뷰 접근
        val btnPermissions = findViewById<Button>(R.id.btnPermissions)
        val btnNotification = findViewById<Button>(R.id.btnNotification)
        val tvVersion = findViewById<TextView>(R.id.tvVersion)

        // 클릭 리스너 설정
        btnPermissions.setOnClickListener {
            requestPermissions()
        }

        btnNotification.setOnClickListener {
            openNotificationSettings()
        }

        // 초기 데이터 설정
        tvVersion.text = "v1.0.0"
    }

    private fun requestPermissions() {
        // 권한 요청 로직
    }

    private fun openNotificationSettings() {
        // 알림 설정 열기
    }
}
```
**결과:** setContentView 자동 처리로 한 줄 감소!
</details>

<br>
</br>

### Fragment 초기화 비교

<details>
<summary><strong>순수 Android - Fragment 수동 초기화</strong></summary>

```kotlin
class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 1. 수동 inflate
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 2. findViewById로 뷰 접근
        val btnPermissions = view.findViewById<Button>(R.id.btnPermissions)
        val btnNotification = view.findViewById<Button>(R.id.btnNotification)
        val tvVersion = view.findViewById<TextView>(R.id.tvVersion)

        // 3. 클릭 리스너 설정
        btnPermissions.setOnClickListener {
            requestPermissions()
        }

        btnNotification.setOnClickListener {
            openNotificationSettings()
        }

        // 4. 초기 데이터 설정
        tvVersion.text = "v1.0.0"
    }

    private fun requestPermissions() {
        // 권한 요청 로직
    }

    private fun openNotificationSettings() {
        // 알림 설정 열기
    }
}
```
**문제점:** 수동 inflate,  권한요청/승인 복잡
</details>

<details>
<summary><strong>Simple UI - Fragment 자동 초기화</strong></summary>

```kotlin
class SettingsFragment : BaseFragment(R.layout.fragment_settings) {

    // onCreateView 자동 처리! ✅
    // rootView 프로퍼티 자동 제공! ✅

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 방법 1: view 파라미터로 접근
        val btnPermissions = view.findViewById<Button>(R.id.btnPermissions)

        // 방법 2: rootView 프로퍼티로 접근 (BaseFragment 제공)
        val btnNotification = rootView.findViewById<Button>(R.id.btnNotification)
        val tvVersion = rootView.findViewById<TextView>(R.id.tvVersion)

        // 클릭 리스너 설정
        btnPermissions.setOnClickListener {
            requestPermissions()
        }

        btnNotification.setOnClickListener {
            openNotificationSettings()
        }

        // 초기 데이터 설정
        tvVersion.text = "v1.0.0"
    }

    private fun requestPermissions() {
        // 권한 요청 로직
    }

    private fun openNotificationSettings() {
        // 알림 설정 열기
    }
}
```
**결과:** onCreateView 자동 처리로 inflate 코드 제거!

**💡 rootView 프로퍼티:**
- BaseFragment는 `protected lateinit var rootView: View` 프로퍼티를 제공합니다
- `onViewCreated()`의 `view` 파라미터와 동일한 참조입니다
- 클래스 내부 어디서든 `rootView`로 루트 뷰에 접근 가능합니다
</details>

<br>
</br>

## 🎨 둘째: BaseBindingActivity/BaseBindingFragment (DataBinding용)

**BaseBindingActivity**와 **BaseBindingFragment**는 DataBinding을 사용하는 화면에 적합합니다.

<br>

### 💡 **특징**
- ✅ DataBinding 자동 설정 (inflate + setContentView + lifecycleOwner)
- ✅ ViewModel과 양방향 바인딩 가능
- ✅ XML에서 직접 데이터 표시 및 이벤트 처리
- ✅ Fragment nullable binding 자동 관리

<br>

### 📌 **언제 사용하나요?**
- ✅ DataBinding이 필요한 화면
- ✅ XML에서 직접 데이터 바인딩
- ✅ 복잡한 UI 상태 관리
- ✅ MVVM 패턴 (ViewModel 연동 시)

<br>
</br>

### Activity 초기화 비교

<details>
<summary><strong>순수 Android - Activity 수동 초기화</strong></summary>

```kotlin
class MainActivity : AppCompatActivity() {

    // 1. binding 선언
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 2. DataBinding 설정 (복잡한 초기화)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        // 3. LifecycleOwner 연결
        binding.lifecycleOwner = this

        // 4. 초기화 로직
        initViews()
    }

    private fun initViews() {
        binding.btnAction.setOnClickListener {
            // 클릭 이벤트 처리
            binding.tvMessage.text = "Button clicked!"
        }
    }
}
```
**문제점:** 복잡한 DataBinding 설정, 수동 LifecycleOwner 연결
</details>

<details>
<summary><strong>Simple UI - Activity 자동 초기화</strong></summary>

```kotlin
class MainActivity : BaseBindingActivity<ActivityMainBinding>(R.layout.activity_main) {

    // DataBinding 자동 설정! ✅
    // LifecycleOwner 자동 연결! ✅

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 핵심 로직만 집중!
        initViews()
    }

    private fun initViews() {
        binding.btnAction.setOnClickListener {
            // 클릭 이벤트 처리
            binding.tvMessage.text = "Button clicked!"
        }
    }
}
```
**결과:** DataBinding 자동, LifecycleOwner 자동, 코드 50% 감소!
</details>

<br>
</br>

### Fragment 초기화 비교

<details>
<summary><strong>순수 Android - Fragment 수동 초기화</strong></summary>

```kotlin
class MainFragment : Fragment() {
    // 1. nullable binding 선언
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 2. DataBinding inflate
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

        // 3. LifecycleOwner 설정
        binding.lifecycleOwner = viewLifecycleOwner

        // 4. 초기화 로직
        initViews()
    }

    private fun initViews() {
        binding.btnAction.setOnClickListener {
            binding.tvMessage.text = "Button clicked!"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 5. 메모리 누수 방지 수동 처리
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

    // DataBinding 자동 설정! ✅
    // binding.lifecycleOwner = this 자동! ✅ (onViewCreated에서)
    // lateinit binding으로 null 체크 불필요! ✅
    // onDestroyView 처리 불필요! ✅

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 이 시점에 이미 binding.lifecycleOwner = this 완료됨

        // 핵심 로직만 집중!
        initViews()
    }

    private fun initViews() {
        binding.btnAction.setOnClickListener {
            binding.tvMessage.text = "Button clicked!"
        }
    }
}
```
**결과:** DataBinding 자동, lifecycleOwner 자동 연결 (onViewCreated에서 this로 설정), lateinit으로 null 체크 불필요, 코드 70% 감소!

**💡 lifecycleOwner 설정 상세:**
- BaseBindingFragment는 `onViewCreated()`에서 `binding.lifecycleOwner = this`를 자동으로 설정합니다
- `this`는 Fragment 자체를 가리킵니다
- LiveData 옵저버가 Fragment의 생명주기를 따라갑니다

**💡 nullable vs lateinit 비교:**

| 구분 | 순수 Android | Simple UI |
|:--|:--|:--|
| **binding 선언** | `private var _binding: Type? = null`<br>`private val binding get() = _binding!!` | `protected lateinit var binding: Type` |
| **null 체크** | 필요 (`_binding?.` 또는 `!!`) | 불필요 (lateinit 보장) |
| **onDestroyView** | `_binding = null` 필수 | 불필요 (자동 관리) |
| **메모리 관리** | 수동 null 할당 필요 | 자동 처리 |
| **코드량** | 3줄 (선언 + getter + null 처리) | 1줄 (선언만) |

**⚠️ 중요한 차이점:**
- **순수 Android**: nullable binding (`_binding?`) 패턴으로 `onDestroyView()`에서 수동으로 null 처리
- **Simple UI**: `lateinit var` 패턴으로 null 체크 불필요, onDestroyView 오버라이드 불필요
</details>

<br>
</br>

### 💡 **MVVM 패턴을 사용하시나요?**

BaseBindingActivity/Fragment와 함께 **ViewModel**을 연동하여 MVVM 패턴을 구현할 수 있습니다!

🚀 **ViewModel 연동 방법과 이벤트 시스템**은 다음 문서를 참고하세요:
- 📖 [README_MVVM.md](README_MVVM.md) - ViewModel 연동 완벽 가이드

<br>
</br>

## 🔐 셋째: 권한 요청 시스템 (공통)

Simple UI는 복잡한 권한 요청 시스템을 **한 줄로** 처리할 수 있는 통합 권한 관리 시스템을 제공합니다.

**특징:**
- ✅ 일반 권한과 특수 권한 자동 구분
- ✅ ActivityResultContract 자동 등록
- ✅ 통합 콜백으로 결과 처리
- ✅ 복잡한 보일러플레이트 제거

<br>
</br>

### 권한 요청 방식 비교

<details>
<summary><strong>순수 Android - ActivityResultContract 수동 등록</strong></summary>

```kotlin
class PermissionsActivity : AppCompatActivity() {

    // 1. 복잡한 Permission Launchers 직접 등록
    private val requestMultiplePermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions -> handlePermissionResults(permissions) }

    private val requestOverlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { handleOverlayPermissionResult() }

    // 2. 복잡한 권한 분리 로직 (일반 vs 특수)
    private fun requestPermissions(permissions: List<String>) {
        val normalPermissions = permissions.filter {
            it != Manifest.permission.SYSTEM_ALERT_WINDOW
        }
        val hasOverlayPermission = permissions.contains(
            Manifest.permission.SYSTEM_ALERT_WINDOW
        )

        // 일반 권한 처리
        if (normalPermissions.isNotEmpty()) {
            requestMultiplePermissionsLauncher.launch(normalPermissions.toTypedArray())
        }

        // 특수 권한 별도 처리
        if (hasOverlayPermission) {
            if (Settings.canDrawOverlays(this)) {
                handleOverlayPermissionResult()
            } else {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                requestOverlayPermissionLauncher.launch(intent)
            }
        }
    }

    // 3. 권한 결과 처리도 직접 구현 (30줄+)
    private fun handlePermissionResults(permissions: Map<String, Boolean>) {
        val deniedPermissions = permissions.filter { !it.value }.keys.toList()
        if (deniedPermissions.isEmpty()) {
            Toast.makeText(this, "모든 권한이 승인되었습니다", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(
                this,
                "거부된 권한: $deniedPermissions",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun handleOverlayPermissionResult() {
        if (Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "오버레이 권한이 승인되었습니다", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "오버레이 권한이 거부되었습니다", Toast.LENGTH_SHORT).show()
        }
    }

    // 4. 사용 예시
    private fun requestCameraPermission() {
        requestPermissions(listOf(Manifest.permission.CAMERA))
    }

    private fun requestMultiplePermissions() {
        requestPermissions(listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.SYSTEM_ALERT_WINDOW  // 특수 권한도 섞여있음
        ))
    }
}
```
**문제점:** 복잡한 launcher 등록, 일반/특수 권한 분리 로직, 개별 결과 처리 필요, 50줄 이상의 보일러플레이트
</details>

<details>
<summary><strong>Simple UI - onRequestPermissions() 한 줄</strong></summary>

```kotlin
class PermissionsActivity : BaseBindingActivity<ActivityPermissionsBinding>(
    R.layout.activity_permissions
) {

    // launcher 등록 불필요! ✅
    // 권한 분리 로직 불필요! ✅

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.btnCameraPermission.setOnClickListener {
            requestCameraPermission()
        }

        binding.btnMultiplePermissions.setOnClickListener {
            requestMultiplePermissions()
        }
    }

    // 권한 요청이 단 한 줄!
    private fun requestCameraPermission() {
        onRequestPermissions(listOf(Manifest.permission.CAMERA)) { deniedPermissions ->
            if (deniedPermissions.isEmpty()) {
                binding.root.snackBarShowShort("카메라 권한이 승인되었습니다")
            } else {
                binding.root.snackBarShowShort("카메라 권한이 거부되었습니다")
            }
        }
    }

    // 일반 권한과 특수 권한을 동일하게 처리!
    private fun requestMultiplePermissions() {
        onRequestPermissions(listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.SYSTEM_ALERT_WINDOW  // 특수권한도 동일하게!
        )) { deniedPermissions ->
            if (deniedPermissions.isEmpty()) {
                binding.root.snackBarShowShort("모든 권한이 승인되었습니다")
            } else {
                binding.root.snackBarShowShort("거부된 권한: $deniedPermissions")
            }
        }
    }
}
```
**결과:** launcher 등록 불필요, 일반/특수 권한 자동 구분, 통합 콜백 제공, 코드 80% 감소!
</details>

<br>
</br>

## 🎨 넷째: SystemBars 제어 (RootActivity)

Simple UI의 **RootActivity**는 StatusBar와 NavigationBar를 쉽게 제어할 수 있는 기능을 제공합니다.

**특징:**
- ✅ StatusBar/NavigationBar 높이 자동 계산 (SDK 버전별 자동 분기)
- ✅ 투명도 설정 한 줄
- ✅ 색상 설정 한 줄
- ✅ 아이콘 라이트/다크 모드 한 줄

<br>
</br>

### SystemBars 제어 비교

<details>
<summary><strong>순수 Android - StatusBar/NavigationBar 수동 처리</strong></summary>

```kotlin
class MainActivity : AppCompatActivity() {

    // 1. StatusBar 높이 계산 - 복잡한 로직
    private fun getStatusBarHeight(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.decorView.rootWindowInsets
                ?.getInsets(WindowInsets.Type.statusBars())?.top ?: 0
        } else {
            val rect = Rect()
            window.decorView.getWindowVisibleDisplayFrame(rect)
            rect.top
        }
    }

    // 2. NavigationBar 높이 계산 - 복잡한 로직
    private fun getNavigationBarHeight(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.decorView.rootWindowInsets
                ?.getInsets(WindowInsets.Type.navigationBars())?.bottom ?: 0
        } else {
            val rootView = window.decorView.rootView
            val contentViewHeight = findViewById<View>(android.R.id.content).height
            val statusBarHeight = getStatusBarHeight()
            (rootView.height - contentViewHeight) - statusBarHeight
        }
    }

    // 3. StatusBar 투명하게 설정 - 수동 처리
    private fun setStatusBarTransparent() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }
    }

    // 4. StatusBar 색상 설정 - 수동 처리
    private fun setStatusBarColor(@ColorInt color: Int, isLightStatusBar: Boolean = false) {
        window.statusBarColor = color
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = isLightStatusBar
    }

    // 5. NavigationBar 색상 설정 - 수동 처리
    private fun setNavigationBarColor(@ColorInt color: Int, isLightNavigationBar: Boolean = false) {
        window.navigationBarColor = color
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightNavigationBars = isLightNavigationBar
    }

    // 6. SystemBars 동시 색상 설정 - 수동 처리
    private fun setSystemBarsColor(@ColorInt color: Int, isLightSystemBars: Boolean = false) {
        setStatusBarColor(color, isLightSystemBars)
        setNavigationBarColor(color, isLightSystemBars)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 사용 예시
        val statusHeight = getStatusBarHeight()
        val navHeight = getNavigationBarHeight()

        setStatusBarTransparent()
        setSystemBarsColor(Color.BLACK, isLightSystemBars = false)
    }
}
```
**문제점:** 복잡한 SDK 버전 분기, 긴 코드, 반복적인 WindowInsets 처리, 60줄 이상의 보일러플레이트
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
**결과:** SDK 버전 분기 자동, 프로퍼티로 간편 접근, protected 메서드로 즉시 사용, 코드 90% 감소!
</details>

<br>
</br>

### 🎯 Base 클래스 제공 기능 정리

#### **RootActivity/RootFragment 공통 기능**
| 기능 | 설명 |
|:--|:--|
| **onRequestPermissions()** | 통합 권한 요청 (일반/특수 권한 자동 구분) |

#### **RootActivity 전용 기능 (Activity만 사용 가능)**
| 기능 | 설명 |
|:--|:--|
| **statusBarHeight** | StatusBar 높이 자동 계산 (SDK 버전별 자동 분기) |
| **navigationBarHeight** | NavigationBar 높이 자동 계산 |
| **setStatusBarTransparent()** | StatusBar를 투명하게 설정 |
| **setStatusBarColor()** | StatusBar 색상 및 아이콘 모드 설정 |
| **setNavigationBarColor()** | NavigationBar 색상 및 아이콘 모드 설정 |
| **setSystemBarsColor()** | SystemBars 동시 색상 설정 |
| **setSystemBarsAppearance()** | SystemBars 아이콘 라이트/다크 모드 설정 |
| **beforeOnCreated()** | onCreate 전 초기화 훅 |

#### **BaseActivity/BaseFragment 기능**
| 기능 | 설명 |
|:--|:--|
| **자동 inflate** | 레이아웃 자동 설정 (Activity: setContentView, Fragment: inflate) |
| **rootView** | Fragment만 - 루트 뷰 접근 프로퍼티 |

#### **BaseBindingActivity/BaseBindingFragment 기능**
| 기능 | 설명 |
|:--|:--|
| **binding** | DataBinding 자동 초기화 및 제공 |
| **lifecycleOwner 자동 설정** | Activity: onCreate에서, Fragment: onViewCreated에서 |
| **onCreateView()** | Activity만 - binding 초기화 직후 콜백 |
| **afterOnCreateView()** | Fragment만 - binding 초기화 직후 콜백 |
| **getViewModel()** | ViewModel 간편 생성 메서드 |
| **eventVmCollect()** | ViewModel 이벤트 구독 전용 메서드 |

<br>
</br>

## 🎨 다섯째: 고급 기능 - 초기화 콜백

Simple UI는 Activity와 Fragment의 초기화 시점을 제어할 수 있는 고급 콜백을 제공합니다.

**특징:**
- ✅ Lifecycle의 특정 시점에 커스텀 로직 삽입 가능
- ✅ Binding 초기화 전후 시점 제어
- ✅ 유연한 초기화 흐름

<br>
</br>

### 📌 **RootActivity - beforeOnCreated()**

모든 Activity가 상속하는 RootActivity는 `beforeOnCreated()` 콜백을 제공합니다.

#### **호출 시점**
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    permissionDelegate = PermissionDelegate(this)
    beforeOnCreated(savedInstanceState)  // ⬅️ 여기서 호출!
}
```

#### **사용 예시**
```kotlin
class MainActivity : BaseBindingActivity<ActivityMainBinding>(R.layout.activity_main) {

    override fun beforeOnCreated(savedInstanceState: Bundle?) {
        super.beforeOnCreated(savedInstanceState)

        // onCreate 전에 실행되는 로직
        // 예: 전역 설정, 테마 설정, 초기화 준비
        setupTheme()
        initializeGlobalSettings()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 일반적인 onCreate 로직
        // 이 시점에는 이미 beforeOnCreated()가 실행됨
        initViews()
    }

    private fun setupTheme() {
        // 테마 설정 로직
    }

    private fun initializeGlobalSettings() {
        // 전역 설정 초기화
    }
}
```

**언제 사용하나요?**
- ✅ Activity 생성 전 전역 설정이 필요한 경우
- ✅ 테마나 스타일을 동적으로 변경해야 하는 경우
- ✅ onCreate 전에 실행되어야 하는 초기화 로직

<br>
</br>

### 📌 **BaseBindingActivity - onCreateView()**

BaseBindingActivity는 `onCreateView(rootView, savedInstanceState)` 콜백을 제공합니다.

#### **호출 시점**
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = DataBindingUtil.setContentView(this, layoutRes)
    onCreateView(binding.root, savedInstanceState)  // ⬅️ 여기서 호출!
    binding.lifecycleOwner = this
}
```

#### **사용 예시**
```kotlin
class MainActivity : BaseBindingActivity<ActivityMainBinding>(R.layout.activity_main) {

    override fun onCreateView(rootView: View, savedInstanceState: Bundle?) {
        super.onCreateView(rootView, savedInstanceState)

        // Binding 초기화 직후, lifecycleOwner 설정 전에 실행
        // rootView로 직접 접근 가능
        setupViewBeforeLifecycle(rootView)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 이 시점에는 이미 onCreateView()와 lifecycleOwner 설정이 완료됨
        binding.tvTitle.text = "Hello World"
    }

    private fun setupViewBeforeLifecycle(rootView: View) {
        // Lifecycle 설정 전 뷰 초기화
        rootView.setBackgroundColor(Color.WHITE)
    }
}
```

**언제 사용하나요?**
- ✅ Binding 초기화 직후 작업이 필요한 경우
- ✅ lifecycleOwner 설정 전에 뷰를 조작해야 하는 경우
- ✅ rootView에 직접 접근해야 하는 경우

<br>
</br>

### 📌 **BaseBindingFragment - afterOnCreateView()**

BaseBindingFragment는 `afterOnCreateView(rootView, savedInstanceState)` 콜백을 제공합니다.

#### **호출 시점**
```kotlin
override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
): View {
    binding = DataBindingUtil.inflate(inflater, layoutRes, container, isAttachToParent)
    afterOnCreateView(binding.root, savedInstanceState)  // ⬅️ 여기서 호출!
    return binding.root
}

override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.lifecycleOwner = viewLifecycleOwner  // lifecycleOwner는 onViewCreated에서 설정
}
```

#### **사용 예시**
```kotlin
class MainFragment : BaseBindingFragment<FragmentMainBinding>(R.layout.fragment_main) {

    override fun afterOnCreateView(rootView: View, savedInstanceState: Bundle?) {
        super.afterOnCreateView(rootView, savedInstanceState)

        // Binding 초기화 직후, onViewCreated 전에 실행
        // lifecycleOwner 설정 전에 뷰 준비 가능
        prepareView(rootView)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 이 시점에는 이미 afterOnCreateView()와 lifecycleOwner 설정이 완료됨
        binding.btnAction.setOnClickListener {
            // 클릭 이벤트 처리
        }
    }

    private fun prepareView(rootView: View) {
        // onViewCreated 전 뷰 준비
        rootView.alpha = 0f
        rootView.animate().alpha(1f).setDuration(300).start()
    }
}
```

**언제 사용하나요?**
- ✅ onCreateView와 onViewCreated 사이에 실행되어야 하는 로직
- ✅ Binding 초기화 직후, lifecycleOwner 설정 전 작업
- ✅ Fragment의 View가 생성된 직후 초기 설정이 필요한 경우

<br>
</br>

### 🔄 **초기화 흐름 정리**

#### **Activity 초기화 흐름**
```
1. onCreate() 시작
2. super.onCreate()
3. beforeOnCreated(savedInstanceState)      ⬅️ 커스텀 훅 #1
4. binding 초기화
5. onCreateView(binding.root, savedInstanceState)  ⬅️ 커스텀 훅 #2
6. binding.lifecycleOwner = this
7. onCreate() 나머지 로직
```

#### **Fragment 초기화 흐름**
```
1. onCreateView() 시작
2. binding 초기화
3. afterOnCreateView(binding.root, savedInstanceState)  ⬅️ 커스텀 훅
4. binding.root 반환
5. onViewCreated() 시작
6. binding.lifecycleOwner = this
7. onViewCreated() 나머지 로직
```

<br>
</br>

## 🚀 Simple UI Activity/Fragment의 핵심 장점


### 1. **⚡ 압도적인 코드 간소화**
- **Activity 초기화**: 20-30줄 → 5-10줄 **70% 단축**
- **Fragment 초기화**: 40-50줄 → 10-15줄 **70% 단축**
- **권한 요청**: 50줄+ → 5줄 **90% 단축**
- **SystemBars 제어**: 60줄+ → 1줄 **95% 단축**

<br>
</br>

### 2. **🛠️ 자동화된 보일러플레이트 처리**
- **setContentView**: 자동 처리
- **DataBinding 초기화**: 자동 처리
- **LifecycleOwner 연결**: 자동 처리
- **nullable binding 관리**: 자동 처리
- **메모리 누수 방지**: 자동 처리

<br>
</br>

### 3. **🔐 통합 권한 관리 시스템**
- **일반/특수 권한 자동 구분**: 개발자가 권한 타입 구분 불필요
- **통합 콜백**: 모든 권한 결과를 한 곳에서 처리
- **보일러플레이트 제거**: ActivityResultContract 등록 불필요

<br>
</br>

### 4. **🎨 간편한 SystemBars 제어**
- **높이 계산 자동화**: SDK 버전별 분기 자동 처리
- **한 줄 설정**: 투명/색상/아이콘 모드 한 줄로 설정
- **프로퍼티 접근**: statusBarHeight/navigationBarHeight 즉시 사용

<br>
</br>

### 5. **🎯 개발자 경험 최적화**
- **타입 안전성**: 컴파일 타임 오류 방지
- **일관된 패턴**: 팀 전체 동일한 Activity/Fragment 구조
- **빠른 개발**: 보일러플레이트 제거로 생산성 향상

<br>
</br>

### 6. **🔧 실수 방지**
- **LifecycleOwner 누락**: 자동 연결로 방지
- **메모리 누수**: Fragment nullable binding 자동 처리
- **권한 요청 오류**: 통합 시스템으로 예외 처리 자동

<br>
</br>

## 💡 개발자 후기

> **"Activity 초기화 코드가 5줄로 끝나요!"**
>
> **"Fragment nullable binding 처리를 더 이상 신경 쓸 필요가 없어요!"**
>
> **"권한 요청이 정말 간단해졌어요! 일반 권한과 특수 권한을 구분할 필요도 없고요!"**
>
> **"SystemBars 제어가 한 줄로 끝나니 UI 구현이 빨라졌어요!"**
>
> **"statusBarHeight를 프로퍼티로 바로 접근할 수 있어서 편해요!"**
>
> **"팀 전체가 동일한 베이스 클래스를 사용하니 코드 리뷰가 쉬워졌어요!"**

<br>
</br>

## 🎉 결론: Activity/Fragment 개발의 새로운 표준

**Simple UI**는 복잡한 Activity/Fragment 초기화를 **단순하고 강력하게** 만드는 혁신적인 라이브러리입니다.

✅ **보일러플레이트 제거** - setContentView, DataBinding, LifecycleOwner 자동화!
✅ **메모리 관리 자동화** - Fragment nullable binding, 메모리 누수 방지 자동!
✅ **통합 권한 시스템** - 일반/특수 권한 자동 구분, 한 줄로 요청!
✅ **SystemBars 제어 간소화** - 높이 계산, 투명/색상 설정 한 줄로!
✅ **70~95% 코드 간소화** - 핵심 로직에만 집중!

**전통적인 복잡함은 이제 그만.**
**Simple UI와 함께 생산적인 개발을 경험하세요!** 🚀

---

<br>
</br>

## 📚 선택 가이드: 어떤 Base 클래스를 사용할까?

Simple UI는 **네 가지 Base 클래스**를 제공합니다. 프로젝트 상황에 맞춰 선택하세요.

<br>
</br>

### 🎯 **선택 가이드**

| 구분 | BaseActivity | BaseBindingActivity |
|:--|:--|:--|
| **사용 시기** | 간단한 화면, DataBinding 불필요 | DataBinding 필요, 복잡한 UI |
| **View 접근** | `findViewById()` 또는 ViewBinding | DataBinding (양방향 바인딩 가능) |
| **코드량** | 매우 간결 (레이아웃만 지정) | 간결 (Binding 자동 처리) |
| **ViewModel 연동** | 수동 연결 필요 | 자동 lifecycleOwner 설정 |
| **추천 용도** | 단순 UI, 설정 화면, 정적 페이지 | 데이터 기반 UI, MVVM 패턴 |

<br>

| 구분 | BaseFragment | BaseBindingFragment |
|:--|:--|:--|
| **사용 시기** | 간단한 화면, DataBinding 불필요 | DataBinding 필요, 복잡한 UI |
| **View 접근** | `findViewById()` 또는 ViewBinding | DataBinding (양방향 바인딩 가능) |
| **코드량** | 매우 간결 (레이아웃만 지정) | 간결 (Binding 자동 처리) |
| **ViewModel 연동** | 수동 연결 필요 | 자동 lifecycleOwner 설정 |
| **메모리 관리** | 자동 (inflate만) | 자동 (nullable binding 처리) |
| **추천 용도** | 단순 UI, 정적 페이지 | 데이터 기반 UI, MVVM 패턴 |

<br>
</br>

### 🤔 **어떤 걸 선택해야 할까?**

#### **BaseActivity/BaseFragment를 선택하세요 👉**
- ✅ 간단한 정보 표시 화면
- ✅ 설정(Settings) 화면
- ✅ 정적 컨텐츠 페이지
- ✅ DataBinding이 과한 경우

#### **BaseBindingActivity/BaseBindingFragment를 선택하세요 👉**
- ✅ DataBinding이 필요한 화면
- ✅ XML에서 직접 데이터 바인딩
- ✅ 복잡한 UI 상태 관리
- ✅ MVVM 패턴 (ViewModel 연동 시)

<br>
</br>

### ⚙️ **고급 파라미터: isAttachToParent**

BaseFragment와 BaseBindingFragment는 선택적으로 `isAttachToParent` 파라미터를 지원합니다.

#### **생성자 시그니처**
```kotlin
// BaseFragment
abstract class BaseFragment(
    @LayoutRes private val layoutRes: Int,
    private val isAttachToParent: Boolean = false  // 기본값: false
) : RootFragment()

// BaseBindingFragment
abstract class BaseBindingFragment<BINDING : ViewDataBinding>(
    @LayoutRes private val layoutRes: Int,
    private val isAttachToParent: Boolean = false  // 기본값: false
) : RootFragment()
```

#### **isAttachToParent란?**
- **`false` (기본값)**: inflate된 뷰를 container에 즉시 붙이지 않음 (일반적인 Fragment 동작)
- **`true`**: inflate된 뷰를 container에 즉시 부착

#### **언제 true를 사용하나요?**
대부분의 경우 **기본값 `false`를 사용**하면 됩니다. `true`는 다음과 같은 특수한 경우에만 사용합니다:

❌ **일반적으로 사용하지 마세요:**
- 일반적인 Fragment 화면
- FragmentManager가 자동으로 뷰를 관리하는 경우

✅ **다음과 같은 특수한 경우에만 사용:**
- 커스텀 뷰 그룹 내부에서 수동으로 Fragment를 관리
- ViewGroup에 직접 추가해야 하는 경우

#### **사용 예시**
```kotlin
// 일반적인 사용 (대부분의 경우)
class NormalFragment : BaseFragment(R.layout.fragment_normal)
// isAttachToParent 생략 시 기본값 false 사용

// 특수한 경우 (거의 사용하지 않음)
class CustomFragment : BaseFragment(
    layoutRes = R.layout.fragment_custom,
    isAttachToParent = true  // 명시적으로 true 지정
)
```

**⚠️ 주의사항:**
- `isAttachToParent = true`를 잘못 사용하면 "The specified child already has a parent" 예외가 발생할 수 있습니다
- 대부분의 Fragment는 기본값 `false`를 사용해야 합니다

<br>
</br>

### 💡 **MVVM 패턴을 사용하시나요?**

BaseBindingActivity/Fragment와 함께 **ViewModel**을 연동하여 완전한 MVVM 패턴을 구현하세요!

<br>

#### **getViewModel() - ViewModel 간편 생성**

BaseBindingActivity와 BaseBindingFragment는 `getViewModel<T>()` 메서드를 제공합니다.

```kotlin
// Activity에서 사용
class MainActivity : BaseBindingActivity<ActivityMainBinding>(R.layout.activity_main) {

    // ViewModel 생성 - 한 줄로 끝!
    private val viewModel: MainViewModel by lazy { getViewModel<MainViewModel>() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ViewModel 사용
        viewModel.loadData()
        binding.vm = viewModel  // DataBinding에 ViewModel 연결
    }
}

// Fragment에서 사용 (확장 함수 형태)
class MainFragment : BaseBindingFragment<FragmentMainBinding>(R.layout.fragment_main) {

    // Fragment.getViewModel() - 확장 함수로 제공
    private val viewModel: MainViewModel by lazy { getViewModel<MainViewModel>() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ViewModel 사용
        viewModel.loadData()
        binding.vm = viewModel  // DataBinding에 ViewModel 연결
    }
}
```

**특징:**
- ✅ Reified Type으로 타입 자동 추론
- ✅ ViewModelProvider 보일러플레이트 제거
- ✅ Activity와 Fragment 모두 동일한 API

**⚠️ 차이점:**
- **Activity**: `getViewModel<T>()`
- **Fragment**: `Fragment.getViewModel<T>()` (확장 함수)

<br>

#### **eventVmCollect() - ViewModel 이벤트 구독 전용 메서드**

BaseBindingActivity와 BaseBindingFragment는 `eventVmCollect()` 메서드를 제공합니다.

이 메서드는 ViewModel의 이벤트(StateFlow, SharedFlow 등)를 구독하기 위한 **전용 초기화 지점**입니다.

```kotlin
// Activity에서 사용
class MainActivity : BaseBindingActivity<ActivityMainBinding>(R.layout.activity_main) {

    private val viewModel: MainViewModel by lazy { getViewModel<MainViewModel>() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.vm = viewModel

        // ViewModel 이벤트 구독
        eventVmCollect()
    }

    override fun eventVmCollect() {
        // ViewModel의 이벤트 Flow를 구독
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is UiState.Loading -> showLoading()
                    is UiState.Success -> showData(state.data)
                    is UiState.Error -> showError(state.message)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.navigationEvent.collect { event ->
                when (event) {
                    is NavigationEvent.NavigateToDetail -> navigateToDetail(event.id)
                    is NavigationEvent.ShowToast -> showToast(event.message)
                }
            }
        }
    }
}

// Fragment에서 사용
class MainFragment : BaseBindingFragment<FragmentMainBinding>(R.layout.fragment_main) {

    private val viewModel: MainViewModel by lazy { getViewModel<MainViewModel>() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.vm = viewModel

        // ViewModel 이벤트 구독
        eventVmCollect()
    }

    override fun eventVmCollect() {
        // Fragment의 viewLifecycleOwner.lifecycleScope 사용 권장
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                // UI 상태 처리
                updateUI(state)
            }
        }
    }
}
```

**언제 사용하나요?**
- ✅ ViewModel의 StateFlow/SharedFlow 구독
- ✅ UI 상태 관리 (로딩, 성공, 에러)
- ✅ 일회성 이벤트 처리 (네비게이션, 토스트 등)
- ✅ 이벤트 구독 로직을 한 곳에 모아 관리

**장점:**
- ✅ 이벤트 구독 코드를 별도 메서드로 분리하여 가독성 향상
- ✅ onCreate/onViewCreated가 복잡해지는 것을 방지
- ✅ 일관된 패턴으로 팀 전체 코드 통일

<br>

🚀 **더 자세한 MVVM 연동 방법과 이벤트 시스템**은 다음 문서를 참고하세요:
- 📖 [README_MVVM.md](README_MVVM.md) - MVVM 패턴 완벽 가이드

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
- BaseActivity 자동 초기화
- BaseFragment 자동 초기화
- BaseBindingActivity DataBinding 자동 연동
- BaseBindingFragment DataBinding 자동 연동
- nullable binding 자동 처리
- 통합 권한 요청 시스템
- 일반/특수 권한 자동 구분
- statusBarHeight/navigationBarHeight 자동 계산
- SystemBars 제어 (투명/색상/아이콘 모드)

<br>
</br>
