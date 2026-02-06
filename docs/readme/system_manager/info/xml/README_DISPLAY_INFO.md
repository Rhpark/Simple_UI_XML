# Display Info vs Plain Android - Complete Comparison Guide
> **Display Info vs 순수 Android - 비교 가이드**

## Module Information (모듈 정보)
- **Module**: `simple_xml` (UI-dependent module / UI 의존 모듈)
- **Package**: `kr.open.library.simple_ui.xml.system_manager.info.display`

<br></br>

## Overview (개요)
Provides display size/insets utilities with automatic SDK branching.  
> SDK 자동 분기를 포함한 디스플레이 크기/인셋 유틸을 제공합니다.

<br></br>

## At a Glance (한눈 비교)
- **Physical Screen Size:** `getPhysicalScreenSize()` - Physical screen size (물리적 화면 크기)
- **App Window Size:** `getAppWindowSize(activity?)` - App window size excluding bars, supports multi-window (상태바/네비게이션바 제외한 앱 윈도우 크기, 멀티윈도우 지원)
- **Status Bar Size:** `getStatusBarSize()` - Status bar size as DisplayInfoSize (상태바 크기 (DisplayInfoSize))
- **Navigation Bar Size:** `getNavigationBarSize()` - Navigation bar size as DisplayInfoSize (네비게이션바 크기 (DisplayInfoSize))
- **Status Bar Insets:** `getStatusBarStableInsets(activity?)` - Status bar stable insets (상태바 고정 인셋)
- **Navigation Bar Insets:** `getNavigationBarStableInsets(activity?)` - Navigation bar stable insets (네비게이션바 고정 인셋)
- **Multi-window Mode:** `isInMultiWindowMode(activity)` - Check if app is in multi-window mode (멀티윈도우 모드 확인)
- **Context-based:** Uses Context for construction, Activity only when needed (생성 시 Context 사용, 필요 시에만 Activity 사용)
- **Auto SDK Branching:** Automatic handling for Android R (API 30) and above/below (Android R (API 30) 이상/이하 자동 처리)

<br></br>

## Why It Matters (중요한 이유)
**Issues**
- Manual SDK version branching
- Direct use of Deprecated API
- Manual Resources ID query
- Complex Insets calculation
- Code duplication (repeated version branching)
> - SDK 버전별 분기 수동 처리
> - Deprecated API 직접 사용
> - Resources ID 수동 조회
> - 복잡한 Insets 계산
> - 코드 중복 (버전별 분기 반복)

**Advantages**
- **Dramatically simplified** (Auto SDK branching)
- Context-based architecture (no Activity required for construction)
- Automatic Android R (API 30) branching
- Automatic Deprecated API avoidance
- Automatic Resources query
- Automatic Insets calculation
- DisplayInfoSize data class for structured size information
- Multi-window mode support
- Optional Activity parameter for methods requiring it
> - **대폭 간소화** (SDK 분기 자동 처리)
> - Context 기반 아키텍처 (생성 시 Activity 불필요)
> - Android R (API 30) 자동 분기
> - Deprecated API 자동 회피
> - Resources 자동 조회
> - Insets 자동 계산
> - DisplayInfoSize 데이터 클래스로 구조화된 크기 정보 제공
> - 멀티윈도우 모드 지원
> - 필요한 메서드에만 선택적 Activity 파라미터 사용

<br></br>

## Plain Android (순수 Android 방식)
```kotlin
// Traditional Display information query method (기존의 Display 정보 조회 방법)
class DisplayHelper(private val context: Context) {

    // 1. Manual SDK version branching (SDK 버전별 분기 처리 (수동))
    fun getFullScreenSize(): Point {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val point = Point()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android R (API 30) and above (Android R (API 30) 이상)
            val windowMetrics = windowManager.currentWindowMetrics
            val bounds = windowMetrics.bounds
            point.x = bounds.width()
            point.y = bounds.height()
        } else {
            // Below Android R (Android R 미만)
            @Suppress("DEPRECATION")
            val display = windowManager.defaultDisplay
            @Suppress("DEPRECATION")
            display.getRealSize(point)
        }

        return point
    }

    // 2. Available screen size (manual calculation) (사용 가능 화면 크기 (수동 계산))
    fun getAvailableScreenSize(): Point {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val point = Point()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = windowManager.currentWindowMetrics
            val insets = windowMetrics.windowInsets
                .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            val bounds = windowMetrics.bounds
            point.x = bounds.width()
            point.y = bounds.height() - insets.top - insets.bottom
        } else {
            @Suppress("DEPRECATION")
            val display = windowManager.defaultDisplay
            @Suppress("DEPRECATION")
            display.getSize(point)
        }

        return point
    }

    // 3. Query status bar height (manual Resources access) (상태바 높이 조회 (Resources 수동 접근))
    fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = context.resources.getIdentifier(
            "status_bar_height",
            "dimen",
            "android"
        )
        if (resourceId > 0) {
            result = context.resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    // 4. Query navigation bar height (manual Resources access) (네비게이션바 높이 조회 (Resources 수동 접근))
    fun getNavigationBarHeight(): Int {
        var result = 0
        val resourceId = context.resources.getIdentifier(
            "navigation_bar_height",
            "dimen",
            "android"
        )
        if (resourceId > 0) {
            result = context.resources.getDimensionPixelSize(resourceId)
        }
        return result
    }
}
```

<br></br>

## Simple UI Approach (Simple UI 방식)
```kotlin
// Simple Display information query - Auto SDK handling (간단한 Display 정보 조회 - SDK 자동 처리)
class MainActivity : BaseDataBindingActivity<ActivityMainBinding>(R.layout.activity_main) {

    private val displayInfo by lazy { getDisplayInfo(this) }

    override fun onCreate(binding: ActivityMainBinding, savedInstanceState: Bundle?) {
        // 1. Physical screen size (auto SDK branching) (물리적 화면 크기 (SDK 자동 분기))
        val physicalSize = displayInfo.getPhysicalScreenSize()
        Log.d("Display", "Physical: ${physicalSize.width} x ${physicalSize.height}") // (물리적)

        // 2. App window size (exclude bars automatically, supports multi-window)
        // (앱 윈도우 크기 (상단/하단 바 자동 제외, 멀티윈도우 지원))
        val windowSize = displayInfo.getAppWindowSize(this)
        windowSize?.let {
            Log.d("Display", "Window: ${it.width} x ${it.height}") // (윈도우)
        }

        // 3. Status bar size (상태바 크기)
        val statusBarSize = displayInfo.getStatusBarSize()
        statusBarSize?.let {
            Log.d("Display", "Status bar: ${it.width} x ${it.height}") // (상태바)
        }

        // 4. Navigation bar size (네비게이션바 크기)
        val navBarSize = displayInfo.getNavigationBarSize()
        navBarSize?.let {
            Log.d("Display", "Navigation bar: ${it.width} x ${it.height}") // (네비게이션바)
        }

        // 5. Multi-window mode check (멀티윈도우 모드 확인)
        val isMultiWindow = displayInfo.isInMultiWindowMode(this)
        Log.d("Display", "Multi-window: $isMultiWindow")
    }
}
```

<br></br>

## Permissions (권한)
No permission is required for DisplayInfo, but see the guide for policy context.  
> DisplayInfo는 권한이 필요하지 않지만, 정책 맥락은 가이드를 참고하세요.

- [README_PERMISSION.md](../../../README_PERMISSION.md)

<br></br>

## Related Docs (관련 문서)
- Summary: [README_SERVICE_MANAGER_INFO.md](../README_SERVICE_MANAGER_INFO.md)
- Permission Guide: [README_PERMISSION.md](../../../README_PERMISSION.md)

<br></br>

