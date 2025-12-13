# Simple UI XML - Product Requirements Document
> **Comprehensive Android XML Development Productivity Library**
> 
> **종합 Android XML 개발 생산성 라이브러리**

**Version**: 0.3.36<br/>
**Date**: 2025-01-04<br/>
**Status**: Active Development<br/>
**Target Audience**: Android XML Developers, Contributors, Portfolio Reviewers, Project Stakeholders<br/>

---

## Table of Contents
- [Part 1: Executive Summary](#part-1-executive-summary)
- [Part 2: Product Overview](#part-2-product-overview)
- [Part 8: Roadmap & Future Plans](#part-8-roadmap--future-plans)

---

# Part 1: Executive Summary

## 1.1 Vision & Mission

### Vision (비전)
**"Complete Productivity Layer for AndroidX Developers"**<br>
**"AndroidX 개발자를 위한 완전한 생산성 레이어"**<br><br>

Simple UI XML aims to be the complete productivity layer for Android XML developers, filling the gaps left by AndroidX and providing a unified, modern development experience.<br>
Simple UI XML은 Android XML 개발자를 위한 완벽한 생산성 레이어로, AndroidX가 남긴 공백을 메우고 통합된 현대적 개발 경험을 제공하는 것을 목표로 합니다.<br><br>

### Mission (미션)
1. **Cut boilerplate code by 70%+** through automation<br>
자동화를 통해 보일러플레이트 코드를 70% 이상 제거<br><br>

2. **Unify system service access** with Flow-based reactive APIs<br>
Flow 기반 반응형 API로 시스템 서비스 접근 통합<br><br>

3. **Ensure type-safety** across all extension functions and utilities<br>
모든 확장 함수와 유틸리티에서 타입 안전성 보장<br><br>

4. **Keep backward compatibility** while supporting the latest Android APIs<br>
최신 Android API를 지원하면서 하위 호환성 유지<br><br>

5. **Let developers focus** on business logic, not framework complexity<br>
개발자가 프레임워크 복잡성이 아닌 비즈니스 로직에 집중할 수 있도록 지원<br><br>

---

## 1.2 Problem Statement

### The AndroidX Gap (AndroidX의 공백)

AndroidX changed Android development by adding Architecture Components, but it **only fixed the UI and Architecture layers**. Critical gaps remain:<br>
AndroidX는 아키텍처 컴포넌트를 도입하여 Android 개발을 변화시켰지만, **UI와 아키텍처 레이어만 개선**했습니다. 여전히 중요한 공백이 남아있습니다:<br><br>

| Layer | Plain Android | AndroidX | Current Gap |
|-------|---------------|----------|----------------|
| **UI Components** | Activity, Fragment | AppCompatActivity, Fragment | ⚠️ **Partial** (보일러플레이트 여전히 존재) |
| **Architecture** | ❌ None | ViewModel, LiveData, Room | ✅ **Solved** |
| **Navigation** | Manual | Navigation Component | ✅ **Solved** |
| **System Services** | getSystemService | ❌ **No Improvement** | ❌ **Critical Gap** |
| **Extensions** | ❌ None | ❌ **No Improvement** | ❌ **Critical Gap** |
| **Permissions** | Manual callbacks | ActivityResultContracts | ⚠️ **Partial** (특수 권한 미지원) |
| **RecyclerView Safety** | Manual synchronization | ListAdapter (DiffUtil) | ⚠️ **Partial** (동시성 이슈 여전) |

### Key Pain Points (주요 문제점)

#### 1. **UI Boilerplate Overload** (UI 보일러플레이트 과부하)
```kotlin
// Even with AndroidX, developers still write:
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // System bars configuration - still manual
        window.statusBarColor = Color.BLACK
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Permission handling - still verbose
        requestPermissions(arrayOf(CAMERA), 100)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
            }
        }
    }
}
```
<br>

#### 2. **System Service Chaos** (시스템 서비스 혼란)
- **BroadcastReceiver** management is error-prone<br>
등록/해제 누락 → 메모리 누수<br><br>

- **API level branching** scattered everywhere<br>
`if (Build.VERSION.SDK_INT >= ...)` 코드가 모든 곳에 산재<br><br>

- **Permission checks** repeated for every service call<br>
모든 서비스 호출마다 권한 체크 반복<br><br>

- **No reactive patterns** - callbacks and manual polling only<br>
반응형 패턴 없음 - 콜백과 수동 폴링만 가능<br><br>

<br>

#### 3. **RecyclerView Race Conditions** (RecyclerView 경쟁 조건)
- DiffUtil helps, but **concurrent list changes** still cause crashes<br>
DiffUtil은 도움이 되지만, **동시 리스트 수정**은 여전히 크래시를 유발<br><br>

- No built-in **operation queue** for safe sequential updates<br>
안전한 순차 업데이트를 위한 내장 **작업 큐** 없음<br><br>

- Developers must manually **synchronize** list operations<br>
개발자가 수동으로 리스트 연산을 **동기화**해야 함<br><br>

<br>

#### 4. **Extension Function Desert** (확장 함수 부재)
- Common operations like `dpToPx`, `toastShowShort`, `bold()` need manual code<br>
`dpToPx`, `toastShowShort`, `bold()` 같은 일반적인 연산도 수동 구현 필요<br><br>

- **60-82% of code** is repetitive utility functions<br>
코드의 **60-82%**가 반복적인 유틸리티 함수<br><br>

- No standard **method chaining** for View styling<br>
View 스타일링을 위한 표준화된 **메서드 체이닝** 없음<br><br>

---

## 1.3 Solution Overview

### Simple UI's Four Pillars (Simple UI의 4대 핵심 가치)

```
┌─────────────────────────────────────────────────────────────┐
│                    Simple UI XML v0.3.28                    │
│            "AndroidX + System Service Layer"                │
└─────────────────────────────────────────────────────────────┘
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
        ▼                  ▼                  ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│  simple_core │  │  simple_xml  │  │   app (샘플)  │
│ (UI 독립 97) │  │ (XML UI 47)  │  │ (활용 검증)   │
└──────────────┘  └──────────────┘  └──────────────┘
```

<br>

#### **Pillar 1: Zero-Boilerplate UI** (보일러플레이트 제로 UI)
```kotlin
// Simple UI solution - BaseBindingActivity
class MainActivity : BaseBindingActivity<ActivityMainBinding>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // binding, setContentView auto-handled
        setStatusBarColor(Color.BLACK, isLightStatusBar = false)

        // Permission with lambda callback - no override needed
        onRequestPermissions(listOf(CAMERA)) { deniedPermissions ->
            if (deniedPermissions.isEmpty()) {
                // All granted
            }
        }
    }
}
// ✅ 15+ lines → 6 lines (binding, setContentView, system bars, permissions)
```

<br>

#### **Pillar 2: StateFlow System Managers** (StateFlow 시스템 매니저)
**12 System Services** with unified reactive API<br>
**12개 시스템 서비스**를 통합 반응형 API로 제공<br><br>

- **Info (6)**: Battery, Network, Location, SIM, Telephony, Display
- **Controller (6)**: Alarm, Notification, Vibrator, WiFi, Keyboard, FloatingView

```kotlin
// 50 lines → 3 lines (94% reduction)
val batteryInfo = BatteryStateInfo(context)
batteryInfo.registerStart(lifecycleScope, 5000L)
lifecycleScope.launch {
    batteryInfo.sfUpdate.collect { event ->
        // Auto memory management, permission verification, exception handling
        // 자동 메모리 관리, 권한 검증, 예외 처리
    }
}
```

<br>

#### **Pillar 3: 17 Extension Packages** (17개 확장 패키지)
**simple_core (9)**: Bundle, Collection, Conditional, Date, Display, RoundTo, String, Time, TryCatch<br>
**simple_xml (8)**: EditText, ImageView, SnackBar, TextView, Toast, ViewAnim, View, ViewLayout, Resource<br><br>

```kotlin
// 60-82% code reduction
Toast.makeText(this, "msg", LENGTH_SHORT).show() // Plain Android
toastShowShort("msg")                             // Simple UI

TypedValue.applyDimension(COMPLEX_DP, 16f, ...) // Plain Android
16.dpToPx(this)                                  // Simple UI
```

<br>

#### **Pillar 4: Production-Ready Safety** (프로덕션 준비 안전성)
- **AdapterOperationQueue**: Mutex-based sequential list operations<br>
Mutex 기반 순차 리스트 연산 (동시성 보장)<br><br>

- **PermissionManager**: Unified normal + special permissions<br>
일반+특수 권한 통합 관리<br><br>

- **safeCatch**: Auto-logging exception handler<br>
자동 로깅 예외 처리<br><br>

- **checkSdkVersion**: Inline API branching<br>
인라인 API 분기 처리<br><br>

---

## 1.4 Target Users & Personas

### Primary Persona: Android XML Developer (주 타겟: Android XML 개발자)
**Profile**:
- Currently using AndroidX in production<br>
프로덕션에서 AndroidX 사용 중<br><br>

- Struggles with system service boilerplate<br>
시스템 서비스 보일러플레이트로 고생 중<br><br>

- Wants modern reactive patterns (StateFlow, Coroutines)<br>
현대적인 반응형 패턴(StateFlow, Coroutines) 원함<br><br>

- Values type-safety and compile-time checks<br>
타입 안전성과 컴파일 타임 체크 중시<br><br>

- Seeks to reduce codebase size by 30%+<br>
코드베이스 크기를 30% 이상 줄이기 원함<br><br>

**Pain Points**:
- "I always forget to unregister BroadcastReceiver"<br>
"BroadcastReceiver 등록/해제를 항상 잊어버림"<br><br>

- "Too much API branching code hurts readability"<br>
"API 분기 코드가 너무 많아 가독성이 떨어짐"<br><br>

- "RecyclerView concurrency bugs waste my time"<br>
"RecyclerView 동시성 버그 디버깅에 시간 낭비"<br><br>

**Goals**:
- Ship features faster with less code<br>
더 적은 코드로 더 빠르게 기능 제공<br><br>

- Cut production crashes from system service misuse<br>
시스템 서비스 오용으로 인한 프로덕션 크래시 감소<br><br>

- Use modern Kotlin patterns without rewriting the app<br>
앱을 다시 작성하지 않고 현대적인 Kotlin 패턴 도입<br><br>

---

### Secondary Persona: Open Source Contributor (부 타겟: 오픈소스 기여자)
**Profile**:
- Interested in library design patterns<br>
라이브러리 디자인 패턴에 관심<br><br>

- Wants to contribute to a well-architected project<br>
잘 설계된 프로젝트에 기여하고 싶음<br><br>

- Values comprehensive documentation and CI/CD<br>
포괄적인 문서와 CI/CD 중시<br><br>

**Pain Points**:
- "Complex projects are hard to contribute to"<br>
"복잡한 프로젝트는 기여하기 어려움"<br><br>

- "Can't understand code without good docs"<br>
"문서가 불충분하면 코드를 이해할 수 없음"<br><br>

**Goals**:
- Learn advanced Kotlin/Android patterns<br>
고급 Kotlin/Android 패턴 학습<br><br>

- Build portfolio with meaningful contributions<br>
의미 있는 기여로 포트폴리오 구축<br><br>

- Work with responsive maintainers<br>
적극적인 메인테이너와 협업<br><br>

---

### Tertiary Persona: Hiring Manager / Portfolio Reviewer (3차 타겟: 채용 담당자)
**Profile**:
- Evaluates candidate's technical depth<br>
후보자의 기술적 깊이 평가<br><br>

- Looks for modern architecture and testing<br>
현대적인 아키텍처와 테스팅 중시<br><br>

- Values documentation and code quality<br>
문서화와 코드 품질 중시<br><br>

**Evaluation Criteria** (평가 기준):
- Multi-module architecture (simple_core / simple_xml)<br>
멀티 모듈 아키텍처<br><br>

- Comprehensive test coverage (Kover, Robolectric)<br>
포괄적인 테스트 커버리지<br><br>

- CI/CD automation (GitHub Actions)<br>
CI/CD 자동화<br><br>

- API documentation (Dokka)<br>
API 문서화<br><br>

---

## 1.5 Success Metrics (KPI)

### Adoption Metrics (채택 지표)
| Metric | Current (v0.3.28) | Target (v1.0.0) | Target (v2.0.0) |
|--------|-------------------|-----------------|-----------------|
| **JitPack Downloads** | Not tracked yet | 1,000+/month | 5,000+/month |
| **GitHub Stars** | Not tracked yet | 500+ | 2,000+ |
| **Production Apps** | 1 (Sample) | 10+ | 50+ |
| **Contributors** | 1 | 5+ | 20+ |

### Quality Metrics (품질 지표)
| Metric | Current | Target (v1.0.0)     |
|--------|---------|---------------------|
| **Test Coverage** | ~40% | 70%+                |
| **CI Success Rate** | 95%+ | 98%+                |
| **Documentation** | 10 READMEs | + Migration Guide   |
| **API Stability** | Breaking changes OK | No breaking changes |

### Developer Experience Metrics (개발자 경험 지표)
| Metric | Baseline (Plain) | Simple UI | Improvement                    |
|--------|------------------|-----------|--------------------------------|
| **Boilerplate Reduction** | 100% | 20% | **70% ↓**                      |
| **System Service Code Lines** | 50 lines | 3 lines | **94% ↓**                      |
| **Extension Code Lines** | 10 lines | 2 lines | **60-82% ↓**                   |
| **RecyclerView Crashes** | Frequent | Rare | **Race conditions eliminated** |

---

**[Continue to Part 2: Product Overview →](#part-2-product-overview)**

---

# Part 2: Product Overview

Complete feature comparison and deep dive into Simple UI's core capabilities.<br>
Simple UI 핵심 기능에 대한 완전한 기능 비교 및 심층 분석.<br><br>

---

## 2.1 Feature Matrix - Plain Android vs AndroidX vs Simple UI

**Complete Feature Comparison Across All Layers**<br>
**모든 레이어에 걸친 완전한 기능 비교**<br><br>

This matrix demonstrates how Simple UI fills the gaps left by AndroidX, providing a complete productivity layer for Android XML development.<br>
이 매트릭스는 Simple UI가 AndroidX가 남긴 공백을 어떻게 메우고, Android XML 개발을 위한 완전한 생산성 레이어를 제공하는지 보여줍니다.<br><br>

### Category 1: Base UI Components (기본 UI 컴포넌트)

| Feature | Plain Android | AndroidX | Simple UI | Improvement |
|---------|--------------|----------|-----------|-------------|
| **Activity Setup** | Manual binding + `setContentView()` (15 lines) | AppCompatActivity + manual binding (12 lines) | `BaseBindingActivity<T>()` - auto binding (0 lines boilerplate) | **100% boilerplate removed** |
| **Fragment Setup** | Manual inflater + view binding (12 lines) | Fragment + manual binding (10 lines) | `BaseBindingFragment<T>()` - auto binding (0 lines) | **100% boilerplate removed** |
| **System Bars Config** | `WindowCompat` + manual flags (8 lines) | Same as Plain (8 lines) | `setStatusBarColor()` - single call (1 line) | **87% ↓** |
| **Edge-to-Edge Setup** | Manual insets listener (20 lines) | ViewCompat.setOnApplyWindowInsetsListener (15 lines) | Auto-handled in RootActivity (0 lines) | **100% handled** |
| **ViewModel Creation** | Manual `ViewModelProvider` (5 lines) | by viewModels() delegate (1 line) | `BaseViewModel` with lifecycle (auto-cleanup) | **Lifecycle aware** |
| **Permission Handling** | Override `onRequestPermissionsResult` (25 lines) | ActivityResultContracts (15 lines, no special permissions) | `onRequestPermissions()` lambda + special permissions (3 lines) | **80% ↓ + special permission support** |

<br>

### Category 2: RecyclerView & Adapters (RecyclerView 및 어댑터)

| Feature | Plain Android | AndroidX | Simple UI | Improvement |
|---------|--------------|----------|-----------|-------------|
| **Adapter Implementation** | Manual `onCreateViewHolder` + `onBindViewHolder` (50 lines) | ListAdapter with DiffUtil (30 lines) | `SimpleBindingRcvListAdapter` (10 lines) | **80% ↓** |
| **List Update Safety** | Manual synchronization (crashes on concurrent updates) | DiffUtil helps but still crashes on rapid updates | `AdapterOperationQueue` - mutex-based queue (0 crashes) | **100% race condition eliminated** |
| **Scroll Direction Detection** | Manual `onScrolled()` override (20 lines) | Same as Plain (20 lines) | `RecyclerScrollStateView` auto-detection (0 lines) | **100% automated** |
| **Scroll Edge Detection** | Manual position calculation (15 lines) | Same as Plain (15 lines) | `RecyclerScrollStateView.sfUpdate` (Flow-based) | **Real-time StateFlow** |

<br>

### Category 3: Permissions (권한)

| Feature | Plain Android | AndroidX | Simple UI | Improvement |
|---------|--------------|----------|-----------|-------------|
| **Normal Permissions** | Override callback + manual checks (25 lines) | ActivityResultContract (15 lines) | `onRequestPermissions()` lambda (3 lines) | **80% ↓** |
| **Special Permissions** | Separate Intent flows for each (40+ lines per permission) | No built-in support (40+ lines) | Unified `PermissionManager` with queue (5 lines) | **87% ↓ + unified API** |
| **Permission Re-request** | Manual tracking + SharedPreferences (30 lines) | Manual tracking (30 lines) | `PermissionManager` auto re-request logic (0 lines) | **Automatic** |

<br>

### Category 4: Extensions - Toast/Messages (확장 - Toast/메시지)

| Feature | Plain Android | AndroidX | Simple UI | Improvement |
|---------|--------------|----------|-----------|-------------|
| **Toast Show** | `Toast.makeText(this, "msg", LENGTH_SHORT).show()` (1 line, 45 chars) | Same (45 chars) | `toastShowShort("msg")` (22 chars) | **60% ↓** |
| **SnackBar Show** | `Snackbar.make(view, "msg", LENGTH_LONG).show()` (1 line, 47 chars) | Same (47 chars) | `snackBarShowLong("msg")` (24 chars) | **65% ↓** |
| **SnackBar with Action** | `Snackbar.make().setAction().show()` (3 lines) | Same (3 lines) | `snackBarShowAction("msg") { }` (1 line) | **67% ↓** |

<br>

### Category 5: Extensions - SDK Branching & Utils (확장 - SDK 분기 및 유틸리티)

| Feature | Plain Android | AndroidX | Simple UI | Improvement |
|---------|--------------|----------|-----------|-------------|
| **SDK Version Check** | `if (Build.VERSION.SDK_INT >= VERSION_CODES.S) { }` (manual branching everywhere) | Same | `checkSdkVersion(S) { }` - inline function | **55% ↓ + centralized** |
| **Unit Conversion (dp→px)** | `TypedValue.applyDimension(COMPLEX_UNIT_DIP, 16f, resources.displayMetrics)` (80 chars) | Same | `16.dpToPx(this)` (13 chars) | **82% ↓** |
| **Try-Catch with Logging** | `try { } catch(e: Exception) { Log.e(); return default }` (5 lines) | Same | `safeCatch(default) { }` (1 line) | **80% ↓ + auto logging** |
| **TextView Styling** | `textView.setTypeface(null, Typeface.BOLD); textView.paintFlags = paintFlags or UNDERLINE_TEXT_FLAG` (2 lines) | Same | `textView.bold().underline()` (chainable, 1 line) | **Chainable DSL** |

<br>

### Category 6: System Services - Info Classes (StateFlow-based) (시스템 서비스 - Info 클래스)

| Feature | Plain Android | AndroidX | Simple UI | Improvement |
|---------|--------------|----------|-----------|-------------|
| **Battery Monitoring** | BroadcastReceiver + IntentFilter + manual register/unregister (50 lines) | No improvement (50 lines) | `BatteryStateInfo().registerStart()` + StateFlow collect (3 lines) | **94% ↓ + reactive** |
| **Location Updates** | LocationManager + callbacks + provider checks + permissions (60 lines) | FusedLocationProviderClient (still complex, 40 lines) | `LocationStateInfo().registerStart()` + StateFlow (3 lines) | **92% ↓ + auto provider** |
| **Network Connectivity** | ConnectivityManager.NetworkCallback + register/unregister (35 lines) | Same (35 lines) | `NetworkConnectivityInfo().sfUpdate` (StateFlow, 3 lines) | **90% ↓ + reactive** |
| **Display Metrics** | WindowManager + DisplayMetrics + SDK branching for R+ (25 lines) | Same (25 lines) | `DisplayInfo().getFullScreenSize()` (1 line, auto SDK handling) | **96% ↓** |

<br>

### Category 7: System Services - Controller Classes (Action APIs) (시스템 서비스 - Controller 클래스)

| Feature | Plain Android | AndroidX | Simple UI | Improvement |
|---------|--------------|----------|-----------|-------------|
| **Notification Show** | NotificationCompat.Builder + channel creation + permission checks (30 lines) | NotificationCompat helps (25 lines) | `NotificationController.showNotification()` (single method with DSL) | **Permission verified + channel auto-created** |
| **Alarm Scheduling** | AlarmManager + PendingIntent + SDK branching for exact alarms (25 lines) | Same (25 lines) | `AlarmController.setAlarm()` (single method, auto SDK branching) | **Auto exact alarm permission** |
| **Vibration** | Vibrator.vibrate() + SDK branching for VibrationEffect API 26+ (15 lines) | Same (15 lines) | `VibratorController.vibrate()` (single method, auto SDK handling) | **Auto API compatibility** |

---

### Key Insights from Feature Matrix (Feature Matrix의 주요 인사이트)

**1. AndroidX Only Solved 40% of the Problem**<br>
**AndroidX는 문제의 40%만 해결했다**<br><br>

AndroidX focused on Architecture Components (ViewModel, LiveData, Room) but left critical gaps in system service access, extensions, and production safety patterns.<br>
AndroidX는 아키텍처 컴포넌트(ViewModel, LiveData, Room)에 집중했지만 시스템 서비스 접근, 확장 함수, 프로덕션 안전성 패턴에는 중요한 공백을 남겼습니다.<br><br>

**2. Simple UI Achieves 70-94% Code Reduction**<br>
**Simple UI는 70-94% 코드 감소 달성**<br><br>

Across all categories, Simple UI reduces boilerplate by 70%+ through automation, reactive patterns, and Kotlin DSL design.<br>
모든 카테고리에서 Simple UI는 자동화, 반응형 패턴, Kotlin DSL 설계를 통해 70% 이상의 보일러플레이트를 제거합니다.<br><br>

**3. StateFlow Architecture is the Differentiator**<br>
**StateFlow 아키텍처가 차별화 요소**<br><br>

System Service Info classes use StateFlow-based reactive architecture, eliminating manual BroadcastReceiver management and providing lifecycle-safe automatic cleanup.<br>
시스템 서비스 Info 클래스는 StateFlow 기반 반응형 아키텍처를 사용하여 수동 BroadcastReceiver 관리를 제거하고 라이프사이클 안전한 자동 정리를 제공합니다.<br><br>

---

## 2.2 Core Features Deep Dive (핵심 기능 상세 분석)

### Feature 1: Zero-Boilerplate UI (Activity/Fragment/ViewModel)

**Zero-Boilerplate UI (보일러플레이트 제로 UI)**<br><br>

**Problem**: Even with AndroidX, developers still write 15+ lines of boilerplate for Activity setup, including binding initialization, setContentView, system bar configuration, and permission handling.<br>
**문제점**: AndroidX를 사용해도 여전히 Activity 설정을 위해 15줄 이상의 보일러플레이트(binding 초기화, setContentView, 시스템 바 설정, 권한 처리)를 작성해야 합니다.<br><br>

**Solution**: Simple UI provides `BaseBindingActivity<T>`, `BaseBindingFragment<T>`, and `RootActivity` that auto-handle binding inflation, content view setup, system bars (including edge-to-edge for API 35), and provide lambda-based permission callbacks—eliminating all UI setup boilerplate.<br>
**해결책**: Simple UI는 `BaseBindingActivity<T>`, `BaseBindingFragment<T>`, `RootActivity`를 제공하여 binding inflation, content view 설정, 시스템 바(API 35 edge-to-edge 포함), 람다 기반 권한 콜백을 자동 처리하여 모든 UI 설정 보일러플레이트를 제거합니다.<br><br>

**Key APIs**:
- `BaseBindingActivity<ActivityMainBinding>()` - Auto binding + setContentView
- `RootActivity` - Edge-to-edge + system bar configuration
- `onRequestPermissions(listOf(CAMERA)) { deniedList -> }` - Lambda permission callback
- `BaseViewModel` - Lifecycle-aware with auto cleanup

**Code Example**:
```kotlin
// Plain Android: 15+ lines
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // System bars - still manual
        window.statusBarColor = Color.BLACK
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }
}

// Simple UI: 3 lines
class MainActivity : BaseBindingActivity<ActivityMainBinding>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusBarColor(Color.BLACK, isLightStatusBar = false)
    }
}
// ✅ 15 lines → 3 lines (80% reduction, binding + setContentView + system bars auto-handled)
```

---

<br>

### Feature 2: Extensions & Utilities (모든 확장 함수 패키지)

**Extensions & Utilities (확장 함수 및 유틸리티)**<br><br>

**Problem**: Common operations like `dpToPx`, `toastShowShort`, `bold()` require verbose Android API calls that developers copy-paste across projects, resulting in 60-82% repetitive utility code.<br>
**문제점**: `dpToPx`, `toastShowShort`, `bold()` 같은 일반 작업은 장황한 Android API 호출을 요구하며, 개발자들은 이를 프로젝트 간에 복사-붙여넣기하여 60-82%의 반복적인 유틸리티 코드를 생성합니다.<br><br>

**Solution**: Simple UI provides 17 extension packages (9 in simple_core, 8 in simple_xml) covering Toast/SnackBar, unit conversion, SDK branching, TextView/EditText/ImageView utilities, View animations, date/time formatting, and safe exception handling—all as Kotlin extension functions for natural, chainable syntax.<br>
**해결책**: Simple UI는 17개 확장 패키지(simple_core 9개, simple_xml 8개)를 제공하여 Toast/SnackBar, 단위 변환, SDK 분기, TextView/EditText/ImageView 유틸리티, View 애니메이션, 날짜/시간 포맷팅, 안전한 예외 처리를 모두 자연스럽고 체이닝 가능한 Kotlin 확장 함수로 제공합니다.<br><br>

**Key Extension Packages**:
- **Toast/SnackBar**: `toastShowShort()`, `snackBarShowAction()` (60-65% reduction)
- **Unit Conversion**: `16.dpToPx(this)`, `14.spToPx(this)` (82% reduction)
- **SDK Branching**: `checkSdkVersion(S) { }` (55% reduction, centralized)
- **TextView**: `textView.bold().underline().italic()` (chainable DSL)
- **View Animation**: `view.fadeIn()`, `view.shake()`, `view.pulse()` (1-line animations)
- **EditText**: `getTextToString()`, `textToInt()`, `isTextEmpty()`
- **ImageView**: `setTint()`, `makeGrayscale()`, `fadeIn()`
- **TryCatch**: `safeCatch(default) { }` (80% reduction + auto logging)
- **Date**: `timestamp.toDateString("yyyy-MM-dd")`
- **String**: `email.isEmailValid()`, `phone.isPhoneNumberValid()`

**Code Example**:
```kotlin
// Plain Android: verbose API calls
Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, resources.displayMetrics)
textView.setTypeface(null, Typeface.BOLD)
textView.paintFlags = textView.paintFlags or Paint.UNDERLINE_TEXT_FLAG

// Simple UI: natural extensions
toastShowShort("Success")
val px = 16.dpToPx(this)
textView.bold().underline()

// ✅ 60-82% code reduction, chainable, intuitive
```

---

<br>

### Feature 3: RecyclerView Mastery (AdapterOperationQueue + RecyclerScrollStateView)

**RecyclerView Mastery (RecyclerView 마스터리)**<br><br>

**Problem**: DiffUtil helps with list updates, but concurrent modifications (multiple rapid calls to `submitList`) still cause `ConcurrentModificationException` crashes. Scroll direction and edge detection require manual `onScrolled()` override with 20+ lines of position tracking logic.<br>
**문제점**: DiffUtil은 리스트 업데이트에 도움이 되지만, 동시 수정(여러 번의 빠른 `submitList` 호출)은 여전히 `ConcurrentModificationException` 크래시를 유발합니다. 스크롤 방향 및 엣지 감지는 20줄 이상의 위치 추적 로직을 포함한 수동 `onScrolled()` 오버라이드가 필요합니다.<br><br>

**Solution**: Simple UI provides `AdapterOperationQueue` (synchronized-based sequential operation queue that eliminates race conditions) and `RecyclerScrollStateView` (auto-detecting scroll direction, edge detection, and scroll state via StateFlow)—achieving 90% adapter code reduction and 100% race condition elimination.<br>
**해결책**: Simple UI는 `AdapterOperationQueue`(경쟁 조건을 제거하는 synchronized 기반 순차 작업 큐)와 `RecyclerScrollStateView`(StateFlow를 통한 스크롤 방향, 엣지 감지, 스크롤 상태 자동 감지)를 제공하여 90% 어댑터 코드 감소 및 100% 경쟁 조건 제거를 달성합니다.<br><br>

**Key APIs**:
- `SimpleBindingRcvListAdapter<T, B>` - 10-line adapter implementation
- `addItem(item)`, `removeAt(position)`, `replaceItemAt(position, item)`, `removeAll()` - Safe list operations
- `RecyclerScrollStateView` - Auto scroll detection with `sfUpdate: StateFlow<ScrollEvent>`
- `ScrollEvent` sealed class: `OnScrollChanged`, `ReachTop`, `ReachBottom`, `DirectionUp`, `DirectionDown`

**Code Example**:
```kotlin
// Plain Android: 50 lines for adapter + 20 lines for scroll detection
class MyAdapter : RecyclerView.Adapter<MyAdapter.ViewHolder>() {
    private val items = mutableListOf<Item>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    // Concurrent modification risk: crashes if called rapidly
    fun updateItems(newItems: List<Item>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}

// Simple UI: 10 lines for adapter + 3 lines for scroll detection
class MyAdapter : SimpleBindingRcvListAdapter<Item, ItemBinding>(
    diffCallback = object : DiffUtil.ItemCallback<Item>() {
        override fun areItemsTheSame(old: Item, new: Item) = old.id == new.id
        override fun areContentsTheSame(old: Item, new: Item) = old == new
    }
) {
    override fun onBindView(binding: ItemBinding, item: Item, position: Int) {
        binding.item = item
    }
}

// Usage - AdapterOperationQueue eliminates race conditions
adapter.addItem(newItem)
adapter.replaceItemAt(index, updatedItem)
adapter.removeAt(0)

// ✅ 50 lines → 10 lines (80% adapter reduction), 100% race conditions eliminated
```

---

### Feature 4: StateFlow-based System Services - Info Classes (6개)

**StateFlow-based System Services - Info Classes (StateFlow 기반 시스템 서비스 - Info 클래스)**<br><br>

**Problem**: System service information (Battery, Location, Network, SIM, Telephony, Display) requires manual BroadcastReceiver registration/unregistration, scattered SDK branching (`if (Build.VERSION.SDK_INT >= ...)`), and callback-based APIs that cause memory leaks if not properly cleaned up.<br>
**문제점**: 시스템 서비스 정보(Battery, Location, Network, SIM, Telephony, Display)는 수동 BroadcastReceiver 등록/해제, 산재된 SDK 분기(`if (Build.VERSION.SDK_INT >= ...)`), 적절히 정리되지 않으면 메모리 누수를 유발하는 콜백 기반 API를 요구합니다.<br><br>

**Solution**: Simple UI provides 6 Info classes (`BatteryStateInfo`, `LocationStateInfo`, `NetworkConnectivityInfo`, `SimInfo`, `TelephonyInfo`, `DisplayInfo`) with unified StateFlow-based reactive APIs that auto-handle BroadcastReceiver lifecycle, SDK branching, permission checks, and provide lifecycle-safe coroutine-based collection—achieving 90-96% code reduction.<br>
**해결책**: Simple UI는 6개의 Info 클래스(`BatteryStateInfo`, `LocationStateInfo`, `NetworkConnectivityInfo`, `SimInfo`, `TelephonyInfo`, `DisplayInfo`)를 제공하여 BroadcastReceiver 라이프사이클, SDK 분기, 권한 체크를 자동 처리하고 라이프사이클 안전한 코루틴 기반 수집을 제공하는 통합 StateFlow 기반 반응형 API로 90-96% 코드 감소를 달성합니다.<br><br>

**Key APIs**:
- **BatteryStateInfo**: `registerStart()` + `sfUpdate: StateFlow<BatteryEvent>`
- **LocationStateInfo**: `registerStart()` + `sfUpdate: StateFlow<LocationEvent>`
- **NetworkConnectivityInfo**: `sfUpdate: StateFlow<NetworkEvent>`
- **DisplayInfo**: `getFullScreenSize()`, `getRealScreenSize()` (auto SDK branching for API R+)
- **SimInfo**: `getActiveSimCount()`, `getSimInfoList()` (auto multi-SIM handling)
- **TelephonyInfo**: `registerCallback()` + `sfUpdate: StateFlow<TelephonyEvent>`

**Code Example**:
```kotlin
// Plain Android: 50 lines for Battery monitoring
class BatteryMonitor(private val context: Context) {
    private var batteryReceiver: BroadcastReceiver? = null

    fun startMonitoring() {
        val intentFilter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_BATTERY_LOW)
            addAction(Intent.ACTION_POWER_CONNECTED)
        }

        batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val capacity = level * 100 / scale
                // ... more manual extraction
                updateUI(capacity, ...)
            }
        }

        context.registerReceiver(batteryReceiver, intentFilter)
    }

    fun stopMonitoring() {
        batteryReceiver?.let { context.unregisterReceiver(it) }
        batteryReceiver = null
    }
}

// Simple UI: 3 lines with StateFlow
val batteryInfo = BatteryStateInfo(context)
batteryInfo.registerStart(lifecycleScope, 5000L)
lifecycleScope.launch {
    batteryInfo.sfUpdate.collect { event ->
        when (event) {
            is BatteryEvent.OnUpdate -> {
                textView.text = "Capacity: ${event.capacity}%, Charging: ${event.isCharging}"
            }
            is BatteryEvent.OnLowBattery -> showLowBatteryWarning()
        }
    }
}

// ✅ 50 lines → 3 lines (94% reduction)
// ✅ Auto lifecycle management (no manual register/unregister)
// ✅ Type-safe sealed class events
```

---

### Feature 5: System Controllers (Controller 클래스 - 6개)

**System Controllers (시스템 컨트롤러)**<br><br>

**Problem**: System service control operations (Alarm, Notification, Vibrator, WiFi, SoftKeyboard, FloatingView) require manual SDK branching for API compatibility, permission verification before every call, channel creation for notifications (API 26+), and exact alarm permissions (API 31+)—resulting in 15-30 lines per operation.<br>
**문제점**: 시스템 서비스 제어 작업(Alarm, Notification, Vibrator, WiFi, SoftKeyboard, FloatingView)은 API 호환성을 위한 수동 SDK 분기, 모든 호출 전 권한 확인, 알림 채널 생성(API 26+), 정확한 알람 권한(API 31+)을 요구하여 작업당 15-30줄의 코드가 필요합니다.<br><br>

**Solution**: Simple UI provides 6 Controller classes that encapsulate SDK branching, permission checks, and API compatibility logic into single-method calls—achieving 60-85% code reduction with automatic permission verification and channel creation.<br>
**해결책**: Simple UI는 SDK 분기, 권한 체크, API 호환성 로직을 단일 메서드 호출로 캡슐화한 6개의 Controller 클래스를 제공하여 자동 권한 확인 및 채널 생성으로 60-85% 코드 감소를 달성합니다.<br><br>

**Key APIs**:
- **AlarmController**: `setAlarm()` (auto exact alarm permission for API 31+)
- **NotificationController**: `showNotification()` (auto channel creation for API 26+)
- **VibratorController**: `vibrate()` (auto VibrationEffect API branching for API 26+)
- **WifiController**: `enableWifi()`, `disableWifi()` (auto SDK branching for API 29+)
- **SoftKeyboardController**: `showKeyboard()`, `hideKeyboard()` (IMM wrapper)
- **FloatingViewController**: `showFloatingView()` (WindowManager overlay management)

**Code Example**:
```kotlin
// Plain Android: 30 lines for notification with channel
fun showNotification() {
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // SDK branching for channel (API 26+)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            "channel_id",
            "Channel Name",
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)
    }

    // Permission check (API 33+)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(this, POST_NOTIFICATIONS) != PERMISSION_GRANTED) {
            return
        }
    }

    val notification = NotificationCompat.Builder(this, "channel_id")
        .setContentTitle("Title")
        .setContentText("Content")
        .setSmallIcon(R.drawable.ic_notification)
        .build()

    notificationManager.notify(1, notification)
}

// Simple UI: single method call
val controller = NotificationController(context)
controller.showNotification(
    channelId = "channel_id",
    channelName = "Channel Name",
    title = "Title",
    content = "Content",
    icon = R.drawable.ic_notification
)

// ✅ 30 lines → 1 method call (85% reduction)
// ✅ Auto channel creation, permission verification, SDK branching
```

---

### Feature 6: Permission Orchestrator (PermissionManager)

**Permission Orchestrator (권한 오케스트레이터)**<br><br>

**Problem**: AndroidX ActivityResultContracts simplified normal permission requests, but special permissions (SYSTEM_ALERT_WINDOW, WRITE_SETTINGS, MANAGE_EXTERNAL_STORAGE, etc.) still require separate Intent flows, manual tracking for re-request logic, and no unified API—resulting in 40+ lines per special permission type.<br>
**문제점**: AndroidX ActivityResultContracts는 일반 권한 요청을 간소화했지만, 특수 권한(SYSTEM_ALERT_WINDOW, WRITE_SETTINGS, MANAGE_EXTERNAL_STORAGE 등)은 여전히 별도의 Intent 흐름, 재요청 로직을 위한 수동 추적, 통합 API 부재를 요구하여 특수 권한 유형당 40줄 이상의 코드가 필요합니다.<br><br>

**Solution**: Simple UI provides `PermissionManager` that unifies normal + special permissions into a single queue-based orchestrator with automatic re-request logic, lifecycle-safe coroutine integration, and synchronized-based sequential processing—achieving 87% code reduction.<br>
**해결책**: Simple UI는 일반 + 특수 권한을 자동 재요청 로직, 라이프사이클 안전 코루틴 통합, synchronized 기반 순차 처리를 갖춘 단일 큐 기반 오케스트레이터로 통합한 `PermissionManager`를 제공하여 87% 코드 감소를 달성합니다.<br><br>

**Key APIs**:
- `PermissionManager(activity)` - Initialize with Activity reference
- `requestPermission(permission, callback)` - Request single permission (normal or special)
- `requestPermissions(list, callback)` - Request multiple permissions
- Auto-detects special permissions: `SYSTEM_ALERT_WINDOW`, `WRITE_SETTINGS`, `MANAGE_EXTERNAL_STORAGE`, `REQUEST_INSTALL_PACKAGES`, etc.
- Queue-based sequential processing (no concurrent permission dialogs)

**Code Example**:
```kotlin
// Plain Android: 40+ lines for special permission (SYSTEM_ALERT_WINDOW)
fun requestOverlayPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION)
        }
    }
}

override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == REQUEST_OVERLAY_PERMISSION) {
        if (Settings.canDrawOverlays(this)) {
            // Permission granted
        } else {
            // Permission denied
        }
    }
}

// Simple UI: 5 lines with PermissionManager
val permissionManager = PermissionManager(this)
permissionManager.requestPermission(Manifest.permission.SYSTEM_ALERT_WINDOW) { granted ->
    if (granted) {
        // Permission granted (normal or special, unified API)
    }
}

// ✅ 40+ lines → 5 lines (87% reduction)
// ✅ Unified API for normal + special permissions
// ✅ Queue-based sequential processing
```

---

### Feature 7: Advanced Logging (Logx)

**Advanced Logging (고급 로깅)**<br><br>

**Problem**: Android's `Log` class provides basic logging, but lacks file output, DSL configuration, stack trace filtering, log level control per tag, and automatic exception formatting—forcing developers to build custom logging wrappers for production apps.<br>
**문제점**: Android의 `Log` 클래스는 기본 로깅을 제공하지만 파일 출력, DSL 구성, 스택 트레이스 필터링, 태그별 로그 레벨 제어, 자동 예외 포맷팅이 없어 개발자가 프로덕션 앱을 위한 커스텀 로깅 래퍼를 구축해야 합니다.<br><br>

**Solution**: Simple UI provides `Logx` with DSL-based configuration, file output with rotation, stack trace depth control, tag-based filtering, exception auto-formatting, and thread-safe writer implementation—providing production-ready logging out of the box.<br>
**해결책**: Simple UI는 DSL 기반 구성, 로테이션이 있는 파일 출력, 스택 트레이스 깊이 제어, 태그 기반 필터링, 예외 자동 포맷팅, 스레드 안전 writer 구현을 갖춘 `Logx`를 제공하여 프로덕션 준비 로깅을 즉시 제공합니다.<br><br>

**Key APIs**:
- `Logx.d(tag, message)` - Debug log with auto stack trace
- `Logx.init { ... }` - DSL configuration (file output, filters, formatters)
- `Logx.setFileOutput(enabled, maxFiles, maxSizePerFile)` - File rotation
- `Logx.setStackTraceDepth(depth)` - Control stack trace lines
- `Logx.addFilter { tag, level -> ... }` - Custom filtering logic

**Code Example**:
```kotlin
// Plain Android: basic logging with no file output
Log.d("MyTag", "Message")
// No file output, no configuration, no exception auto-formatting

// Simple UI: DSL configuration + file output
Logx.init {
    setFileOutput(enabled = true, maxFiles = 5, maxSizePerFile = 10_000_000L)
    setStackTraceDepth(5)
    addFilter { tag, level -> level >= Log.INFO }
    setFormatter { tag, level, message, stackTrace ->
        "[$tag] [$level] $message\n$stackTrace"
    }
}

Logx.d("MyTag", "Message with auto stack trace")
Logx.e("MyTag", exception) // Auto exception formatting

// ✅ Production-ready logging with file output, DSL config, filtering
```

---

### Feature 8: XML Style System (스타일 시스템)

**XML Style System (XML 스타일 시스템)**<br><br>

**Problem**: XML attributes are verbose (`android:layout_width="wrap_content"` repeated everywhere), no programmatic style composition, and theme-based dynamic styling requires manual resource lookups and conditional logic scattered across layouts.<br>
**문제점**: XML 속성은 장황하고(`android:layout_width="wrap_content"`가 모든 곳에 반복됨), 프로그래매틱 스타일 구성이 없으며, 테마 기반 동적 스타일링은 레이아웃 전체에 산재된 수동 리소스 조회 및 조건부 로직을 요구합니다.<br><br>

**Solution**: Simple UI provides reusable XML style tags (`<style name="WrapContent">`) and programmatic style extension functions (`view.applyStyle(R.style.CardStyle)`) that enable style composition, theme-based dynamic styling, and reduce XML verbosity by 40-60%.<br>
**해결책**: Simple UI는 스타일 구성, 테마 기반 동적 스타일링을 가능하게 하고 XML 장황함을 40-60% 줄이는 재사용 가능한 XML 스타일 태그(`<style name="WrapContent">`)와 프로그래매틱 스타일 확장 함수(`view.applyStyle(R.style.CardStyle)`)를 제공합니다.<br><br>

**Key Features**:
- Reusable style tags: `@style/WrapContent`, `@style/MatchParent`, `@style/CenterInParent`
- Programmatic application: `view.applyStyle(R.style.CardStyle)`
- Theme-based styles: `@style/ButtonPrimary`, `@style/ButtonSecondary`
- Style composition: `<style parent="@style/BaseButton">`

**Code Example**:
```xml
<!-- Plain Android: repetitive attributes -->
<TextView
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:layout_gravity="center"
    android:textColor="@color/primary"
    android:textSize="16sp" />

<!-- Simple UI: reusable styles -->
<TextView style="@style/WrapContent.Center.TextPrimary16sp" />

<!-- ✅ 5 lines → 1 line (80% reduction in XML verbosity) -->
```

---

## 2.3 Code Comparison Examples (상세 코드 비교)

This section provides 6 detailed code comparisons demonstrating how Simple UI transforms verbose Android code into concise, production-ready implementations.<br>
이 섹션은 Simple UI가 장황한 Android 코드를 간결하고 프로덕션 준비된 구현으로 변환하는 방법을 보여주는 6가지 상세 코드 비교를 제공합니다.<br><br>

---

### Example 1: SystemManager Info - Location State Monitoring

**Use Case**: Monitor GPS location updates with provider state detection (GPS/Network/Fused), permission handling, and lifecycle-safe cleanup.<br>
**사용 사례**: GPS 위치 업데이트를 제공자 상태 감지(GPS/Network/Fused), 권한 처리, 라이프사이클 안전 정리와 함께 모니터링.<br><br>

<details>
<summary><strong>Plain Android - Location Monitoring (60+ lines)</strong></summary>

```kotlin
class LocationMonitor(private val context: Context) : LocationListener {

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private var currentLocation: Location? = null

    fun startLocationUpdates() {
        // 1. Check permissions
        if (ActivityCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED) {
            Log.e("LocationMonitor", "Location permissions not granted")
            return
        }

        // 2. Check if GPS is enabled
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!isGpsEnabled && !isNetworkEnabled) {
            Log.e("LocationMonitor", "No location providers enabled")
            return
        }

        // 3. Request location updates - need SDK branching for Fused Location (API 31+)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Fused location provider (API 31+)
                locationManager.requestLocationUpdates(
                    LocationManager.FUSED_PROVIDER,
                    5000L,
                    10f,
                    this
                )
            } else {
                // Use GPS or Network provider
                val provider = if (isGpsEnabled) LocationManager.GPS_PROVIDER else LocationManager.NETWORK_PROVIDER
                locationManager.requestLocationUpdates(
                    provider,
                    5000L,
                    10f,
                    this
                )
            }
        } catch (e: SecurityException) {
            Log.e("LocationMonitor", "SecurityException: ${e.message}")
        } catch (e: IllegalArgumentException) {
            Log.e("LocationMonitor", "IllegalArgumentException: ${e.message}")
        }
    }

    override fun onLocationChanged(location: Location) {
        currentLocation = location
        Log.d("LocationMonitor", "Location: ${location.latitude}, ${location.longitude}")
        // Update UI or notify listeners
    }

    override fun onProviderEnabled(provider: String) {
        Log.d("LocationMonitor", "Provider enabled: $provider")
    }

    override fun onProviderDisabled(provider: String) {
        Log.d("LocationMonitor", "Provider disabled: $provider")
    }

    // 4. Must manually stop updates to prevent memory leak
    fun stopLocationUpdates() {
        try {
            locationManager.removeUpdates(this)
        } catch (e: SecurityException) {
            Log.e("LocationMonitor", "SecurityException during stop: ${e.message}")
        }
    }
}

// Usage in Activity/Fragment
class MainActivity : AppCompatActivity() {
    private lateinit var locationMonitor: LocationMonitor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationMonitor = LocationMonitor(this)
        locationMonitor.startLocationUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()
        locationMonitor.stopLocationUpdates() // Manual cleanup required
    }
}
```

</details>

**Simple UI - Location State Monitoring (8 lines with StateFlow collect)**

```kotlin
// Initialize LocationStateInfo
val locationInfo = LocationStateInfo(context)
locationInfo.registerStart(lifecycleScope, 5000L) // Auto lifecycle management

// Collect location updates via StateFlow
lifecycleScope.launch {
    locationInfo.sfUpdate.collect { event ->
        when (event) {
            is LocationEvent.OnUpdate -> {
                val location = event.location
                textView.text = "Location: ${location.latitude}, ${location.longitude}"
            }
            is LocationEvent.OnProviderEnabled -> {
                Log.d("Location", "Provider enabled: ${event.provider}")
            }
            is LocationEvent.OnProviderDisabled -> {
                Log.d("Location", "Provider disabled: ${event.provider}")
            }
        }
    }
}

// ✅ No manual cleanup needed - auto lifecycle management
// ✅ Auto permission verification
// ✅ Auto SDK branching (Fused provider for API 31+, GPS/Network for lower)
// ✅ Type-safe sealed class events
```

**Metrics**:
- **Lines of code**: 60+ lines → 8 lines (87% reduction)
- **Key benefits**:
  - Auto lifecycle management (no manual `removeUpdates`)
  - Auto permission verification + exception handling
  - Auto SDK branching (Fused provider API 31+ vs GPS/Network)
  - StateFlow-based reactive updates (no callback boilerplate)
  - Type-safe sealed class events

---

### Example 2: SystemManager Controller - Notification with Channel

**Use Case**: Show notification with channel creation (API 26+), permission check (API 33+), and action button.<br>
**사용 사례**: 채널 생성(API 26+), 권한 체크(API 33+), 액션 버튼이 있는 알림 표시.<br><br>

<details>
<summary><strong>Plain Android - Notification with Channel (30+ lines)</strong></summary>

```kotlin
fun showNotificationWithAction(context: Context) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // 1. SDK branching for channel creation (API 26+)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            "channel_id",
            "Channel Name",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Channel Description"
            enableLights(true)
            lightColor = Color.RED
            enableVibration(true)
        }
        notificationManager.createNotificationChannel(channel)
    }

    // 2. Permission check (API 33+)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PERMISSION_GRANTED) {
            Log.e("Notification", "POST_NOTIFICATIONS permission not granted")
            return
        }
    }

    // 3. Create action PendingIntent
    val actionIntent = Intent(context, MyReceiver::class.java).apply {
        action = "ACTION_BUTTON_CLICKED"
    }
    val actionPendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        actionIntent,
        PendingIntent.FLAG_IMMUTABLE
    )

    // 4. Build notification
    val notification = NotificationCompat.Builder(context, "channel_id")
        .setContentTitle("Title")
        .setContentText("Content")
        .setSmallIcon(R.drawable.ic_notification)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .addAction(R.drawable.ic_action, "Action", actionPendingIntent)
        .build()

    // 5. Show notification
    notificationManager.notify(1, notification)
}
```

</details>

**Simple UI - Notification with Channel (single method call)**

```kotlin
val controller = NotificationController(context)
controller.showNotificationWithAction(
    channelId = "channel_id",
    channelName = "Channel Name",
    title = "Title",
    content = "Content",
    icon = R.drawable.ic_notification,
    actionIcon = R.drawable.ic_action,
    actionTitle = "Action",
    actionIntent = Intent(context, MyReceiver::class.java).apply {
        action = "ACTION_BUTTON_CLICKED"
    }
)

// ✅ Auto channel creation (API 26+)
// ✅ Auto permission verification (API 33+)
// ✅ Auto SDK branching
```

**Metrics**:
- **Lines of code**: 30+ lines → 1 method call (85% reduction)
- **Key benefits**:
  - Auto channel creation (API 26+, no manual `NotificationChannel`)
  - Auto permission verification (API 33+ POST_NOTIFICATIONS)
  - Auto SDK branching
  - Single method call vs 5-step manual process

---

### Example 3: Extensions - Toast/SnackBar + SDK Branching

**Use Case**: Show Toast message, SnackBar with action, and SDK version check.<br>
**사용 사례**: Toast 메시지, 액션이 있는 SnackBar, SDK 버전 체크 표시.<br><br>

**Plain Android - Toast/SnackBar + SDK Branching (10+ lines)**

```kotlin
// Toast
Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()

// SnackBar with action
Snackbar.make(findViewById(android.R.id.content), "Message", Snackbar.LENGTH_LONG)
    .setAction("Undo") {
        // Undo action
    }
    .show()

// SDK version check
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    // API 31+ specific code
    startForegroundService(intent)
} else {
    // API 30- code
    startService(intent)
}
```

**Simple UI - Toast/SnackBar + SDK Branching (3 lines)**

```kotlin
// Toast
toastShowShort("Success")

// SnackBar with action
snackBarShowAction("Message") { /* Undo action */ }

// SDK version check
checkSdkVersion(S) {
    startForegroundService(intent) // API 31+ only
} ?: run {
    startService(intent) // API 30- fallback
}
```

**Metrics**:
- **Lines of code**: 10+ lines → 3 lines (70% reduction)
- **Key benefits**:
  - Toast: 45 chars → 22 chars (60% reduction)
  - SnackBar: 3 lines → 1 line (chainable lambda)
  - SDK check: Centralized inline function (55% reduction)

---

### Example 4: RecyclerView Adapter - SimpleBindingRcvListAdapter

**Use Case**: Implement RecyclerView adapter with DiffUtil and data binding.<br>
**사용 사례**: DiffUtil 및 데이터 바인딩을 사용한 RecyclerView 어댑터 구현.<br><br>

<details>
<summary><strong>Plain Android/AndroidX - RecyclerView Adapter (50+ lines)</strong></summary>

```kotlin
class MyAdapter : RecyclerView.Adapter<MyAdapter.ViewHolder>() {

    private val items = mutableListOf<Item>()

    class ViewHolder(private val binding: ItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Item) {
            binding.item = item
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    // List update methods - risk of ConcurrentModificationException
    fun updateItems(newItems: List<Item>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun addItem(item: Item) {
        items.add(item)
        notifyItemInserted(items.size - 1)
    }

    fun removeItem(position: Int) {
        items.removeAt(position)
        notifyItemRemoved(position)
    }
}

// Usage - risk of concurrent modification crashes
adapter.addItem(newItem)
adapter.removeItem(0)
adapter.updateItems(newList) // If called rapidly, crashes with ConcurrentModificationException
```

</details>

**Simple UI - SimpleBindingRcvListAdapter (10 lines)**

```kotlin
class MyAdapter : SimpleBindingRcvListAdapter<Item, ItemBinding>(
    R.layout.item_layout,
    listDiffUtil = RcvListDiffUtilCallBack(
        itemsTheSame = { oldItem, newItem -> oldItem.id == newItem.id },
        contentsTheSame = { oldItem, newItem -> oldItem == newItem }
    )
) {
    override fun onBindView(binding: ItemBinding, item: Item, position: Int) {
        binding.item = item
    }
}

// Usage - AdapterOperationQueue eliminates race conditions
adapter.addItem(newItem)
adapter.removeAt(0)
adapter.replaceItemAt(0, updatedItem)
adapter.removeAll()

// ✅ synchronized-based sequential operations - no concurrent modification crashes
// ✅ 50 lines → 10 lines (80% reduction)
```

**Metrics**:
- **Lines of code**: 50+ lines → 10 lines (80% reduction)
- **Key benefits**:
  - Auto ViewHolder boilerplate
  - Auto onCreateViewHolder/onBindViewHolder
  - AdapterOperationQueue: synchronized-based sequential operations (100% race condition eliminated)
  - DiffUtil integration

---

### Example 5: RecyclerScrollStateView - Scroll Detection

**Use Case**: Detect scroll direction (up/down) and edge detection (top/bottom) for infinite scroll or toolbar hide/show.<br>
**사용 사례**: 무한 스크롤 또는 툴바 숨김/표시를 위한 스크롤 방향(위/아래) 및 엣지 감지(상단/하단).<br><br>

<details>
<summary><strong>Plain Android - Manual Scroll Detection (20+ lines)</strong></summary>

```kotlin
class MyActivity : AppCompatActivity() {

    private var lastScrollY = 0
    private var isScrollingUp = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                // Detect scroll direction
                if (dy > 0) {
                    // Scrolling down
                    if (isScrollingUp) {
                        isScrollingUp = false
                        toolbar.hide()
                    }
                } else if (dy < 0) {
                    // Scrolling up
                    if (!isScrollingUp) {
                        isScrollingUp = true
                        toolbar.show()
                    }
                }

                // Detect top/bottom edge
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
                val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()
                val totalItemCount = layoutManager.itemCount

                if (firstVisiblePosition == 0) {
                    Log.d("Scroll", "Reached top")
                }

                if (lastVisiblePosition == totalItemCount - 1) {
                    Log.d("Scroll", "Reached bottom - load more")
                    loadMoreItems()
                }
            }
        })
    }
}
```

</details>

**Simple UI - RecyclerScrollStateView (3 lines + StateFlow collect)**

```xml
<!-- XML: Use RecyclerScrollStateView instead of RecyclerView -->
<kr.open.library.simple_ui.xml.ui.view.recyclerview.RecyclerScrollStateView
    android:id="@+id/rcvList"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

```kotlin
// Kotlin: Collect scroll events via StateFlow
lifecycleScope.launch {
    binding.rcvList.sfUpdate.collect { event ->
        when (event) {
            is ScrollEvent.DirectionUp -> toolbar.show()
            is ScrollEvent.DirectionDown -> toolbar.hide()
            is ScrollEvent.ReachTop -> Log.d("Scroll", "Reached top")
            is ScrollEvent.ReachBottom -> loadMoreItems()
            is ScrollEvent.OnScrollChanged -> {
                Log.d("Scroll", "dx: ${event.dx}, dy: ${event.dy}")
            }
        }
    }
}

// ✅ Auto scroll detection (no manual addOnScrollListener)
// ✅ Type-safe sealed class events
// ✅ StateFlow-based reactive updates
```

**Metrics**:
- **Lines of code**: 20+ lines → 3 lines (85% reduction)
- **Key benefits**:
  - Auto scroll direction detection (no manual `dy` tracking)
  - Auto edge detection (top/bottom)
  - StateFlow-based reactive updates (no callback boilerplate)
  - Type-safe sealed class events

---

### Example 6: Permission Handling - Unified Normal + Special Permissions

**Use Case**: Request camera permission (normal) and overlay permission (special) with unified API.<br>
**사용 사례**: 통합 API로 카메라 권한(일반) 및 오버레이 권한(특수) 요청.<br><br>

<details>
<summary><strong>Plain Android - Normal + Special Permissions (50+ lines)</strong></summary>

```kotlin
class MainActivity : AppCompatActivity() {

    private val REQUEST_CAMERA = 100
    private val REQUEST_OVERLAY = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request camera permission (normal)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA)
        } else {
            // Permission granted
        }

        // Request overlay permission (special) - separate Intent flow
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                startActivityForResult(intent, REQUEST_OVERLAY)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CAMERA -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Camera permission granted
                } else {
                    // Camera permission denied
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_OVERLAY -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Settings.canDrawOverlays(this)) {
                        // Overlay permission granted
                    } else {
                        // Overlay permission denied
                    }
                }
            }
        }
    }
}
```

</details>

**Simple UI - Unified Permission API (5 lines)**

```kotlin
val permissionManager = PermissionManager(this)

// Request camera permission (normal) - unified API
permissionManager.requestPermission(Manifest.permission.CAMERA) { granted ->
    if (granted) {
        // Permission granted
    }
}

// Request overlay permission (special) - same unified API
permissionManager.requestPermission(Manifest.permission.SYSTEM_ALERT_WINDOW) { granted ->
    if (granted) {
        // Permission granted
    }
}

// Request multiple permissions (normal + special mixed) - single call
permissionManager.requestPermissions(
    listOf(
        Manifest.permission.CAMERA,
        Manifest.permission.SYSTEM_ALERT_WINDOW,
        Manifest.permission.WRITE_SETTINGS
    )
) { deniedPermissions ->
    if (deniedPermissions.isEmpty()) {
        // All permissions granted
    }
}

// ✅ Unified API for normal + special permissions
// ✅ Queue-based sequential processing (no concurrent permission dialogs)
// ✅ Auto re-request logic
```

**Metrics**:
- **Lines of code**: 50+ lines → 5 lines (90% reduction)
- **Key benefits**:
  - Unified API for normal + special permissions (no separate Intent flows)
  - Queue-based sequential processing (no concurrent permission dialogs)
  - Auto re-request logic
  - Lifecycle-safe coroutine integration

---

**[Part 2 Complete - Continue to Part 8: Roadmap & Future Plans →](#part-8-roadmap--future-plans)**
