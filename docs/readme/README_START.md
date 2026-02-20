# 🚀 Simple UI XML — We handle the complexity, you keep your speed
> **복잡함은 우리가, 속도는 당신에게**

<br>,</br>

**Repetitive Activity/Fragment setups**, **never-ending permission handling**, and the swelling boilerplate... 

We built **Simple UI XML** to give you that time back.

> **반복되는 Activity/Fragment 세팅**, **끝나지 않는 권한 처리**, 그리고 불어나는 보일러플레이트...  
> 그 시간을 돌려주기 위해 **Simple UI XML**을 만들었다.

<br>
</br>

## ✨ Simple UI XML: Core Impact & Reasons to Adopt (핵심 효과 & 도입 이유)

- **250 lines → 87 lines / 4–5h → 2–3h (≈50% faster)** — Removes boilerplate so only the core flow remains.
- **Turn complex Android APIs into "one liners"** — Automates boilerplate like activity setup and permission handling.
- **Ships with the features teams want every day** — Feel the speed boost the moment you adopt it.
- **Less repetitive coding → More focus on core features** — Boosts both lead time and quality for the entire team.
 
**One-line takeaway:** Give the complexity to **Simple UI XML** and keep the speed for **yourself**.
> - **250줄 → 87줄 / 4–5h → 2~3h (≈50% 단축)** — 보일러플레이트를 걷어내 핵심 흐름만 남깁니다.
> - **복잡한 Android API를 "한 줄"로** — Activity 세팅·권한 처리 등 상용구 자동화.
> - **현업이 매일 바라던 기능을 기본 제공** — 도입 즉시 체감 속도 상승.
> - **반복 코딩 감소 → 핵심 기능 개발 집중** — 팀 전체 **리드타임·품질** 동시 향상.

> - **한 줄 결론:** 복잡함은 **Simple UI XML**에게, 속도는 **당신에게**.

<br>
</br>

### 👥 **Team Development Productivity Boost (팀 개발 생산성 혁신)**

- Improve code consistency: Every teammate uses the same base classes and extensions → improves efficiency for bug tracking and maintenance.
- Accelerate onboarding for new members: No need to master complex Android APIs → shortens the ramp-up period.
- Cut code review time by 70%: Standardized patterns clarify review points → lets reviewers focus on the core logic.
- Minimize collaboration conflicts: Integrated systems like PermissionRequester and Logx prevent duplicate implementations.
> - 코드 일관성 향상: 모든 팀원이 동일한 Base 클래스 & Extension 사용 → 버그 추적·유지보수 효율성 향상
> - 신규 멤버 온보딩 가속화: 복잡한 Android API 학습 불필요 → 적응 기간 단축
> - 코드리뷰 시간 70% 단축: 표준화된 패턴으로 리뷰 포인트 명확화 → 핵심 로직에만 집중
> - 협업 충돌 최소화: PermissionRequester, Logx 등 통합 시스템으로 중복 구현 방지

<br>
</br>

## 🎯 **Target Users (타겟 사용자)**

**XML View system** environment

<br>
</br>

## 📚 **Documentation Index (문서 인덱스)**

- 전체 README 문서는 [README.md](README.md)에서 기능별로 바로 찾을 수 있습니다.

<br>
</br>

## 📋 **Library Defaults (라이브러리 기본 설정)**

- **minSdk**: 28
- **compileSdk**: 35
- **Kotlin**: 2.0.21
- **Android Gradle Plugin**: 8.8.2


<br>
</br>

## 🔁 **Release Pipeline Notes (릴리즈 파이프라인 주의사항)**

- The workflow chain is `1. Android CI -> 2. Android CD -> 3. Documentation (Dokka, Kover)`.
- Artifacts are bound to the triggering run using `workflow_run.id` to avoid cross-run mixups.
- `release-metadata` and `coverage-report` are relayed through CD for downstream consistency.
> - 워크플로 체인은 `1. Android CI -> 2. Android CD -> 3. Documentation (Dokka, Kover)` 입니다.
> - 아티팩트는 `workflow_run.id` 기준으로 트리거된 실행에 결합되어, 다른 실행과 섞이는 문제를 방지합니다.
> - `release-metadata`, `coverage-report`는 하위 단계 정합성을 위해 CD 단계를 거쳐 전달됩니다.

<br>
</br>

## 🔐 **Firebase App Distribution Config (Firebase App Distribution 설정)**

App Distribution `appId` is resolved in this order:
1. `gradle.properties` (`firebaseAppIdVerification`, `firebaseAppIdDebug`, `firebaseAppIdRelease`)
2. Environment variables (`FIREBASE_APP_ID_VERIFICATION`, `FIREBASE_APP_ID_DEBUG`, `FIREBASE_APP_ID_RELEASE`)
3. Hardcoded emergency fallback in `app/build.gradle.kts`

> App Distribution `appId`는 아래 순서로 해석됩니다.
> 1. `gradle.properties` (`firebaseAppIdVerification`, `firebaseAppIdDebug`, `firebaseAppIdRelease`)
> 2. 환경변수 (`FIREBASE_APP_ID_VERIFICATION`, `FIREBASE_APP_ID_DEBUG`, `FIREBASE_APP_ID_RELEASE`)
> 3. `app/build.gradle.kts`의 비상용 하드코딩 폴백

```properties
# gradle.properties
firebaseAppIdVerification=1:549084067814:android:3ecfc4be81884ce0738827
firebaseAppIdDebug=1:549084067814:android:d467d3ea55c4c608738827
firebaseAppIdRelease=1:549084067814:android:2477eceb48b0314a738827
```

```bash
# CI/CD environment variables (optional override)
FIREBASE_APP_ID_VERIFICATION=...
FIREBASE_APP_ID_DEBUG=...
FIREBASE_APP_ID_RELEASE=...
```

<br>
</br>

## ✨ **Key Features (핵심 특징)**

### 📱 **Accelerate UI Development (UI 개발 가속화)**

- **Base classes**: RootActivity, BaseActivity, BaseDataBindingActivity
- **Fragment**: RootFragment, BaseFragment, BaseDataBindingFragment, RootDialogFragment, BaseDialogFragment, BaseDataBindingDialogFragment
- **RecyclerView**: Rich adapters, view holders, DiffUtil + RecyclerScrollStateView
- **Custom layouts**: Layout components with lifecycle awareness
- **XML style system**: Comprehensive UI style library (style.xml)
- **MVVM support**: Fully compatible with ViewModel and DataBinding
> - **기본 클래스**: RootActivity, BaseActivity, BaseDataBindingActivity
> - **Fragment**: RootFragment, BaseFragment, BaseDataBindingFragment, RootDialogFragment, BaseDialogFragment, BaseDataBindingDialogFragment
> - **RecyclerView**: 다양한 Adapter, ViewHolder, DiffUtil + RecyclerScrollStateView
> - **커스텀 레이아웃**: Lifecycle 지원하는 Layout 컴포넌트들
> - **XML 스타일 시스템**: 포괄적인 UI 스타일 라이브러리 (style.xml)
> - **MVVM 지원**: ViewModel, DataBinding 호환 지원
 
<br></br>

### 🔧 **Developer Convenience (개발 편의성)**

- **Extension functions**: Practical add-ons for Bundle, String, Date, Time, TryCatch, and more
- **Safe coding**: Simplified exception handling with safeCatch
- **Permission management**: Unified support through PermissionRequester
- **Advanced logging**: Logx with file storage, filtering, and custom formatting
- **Local storage**: Delegate-based preferences with safe commits (BaseSharedPreference)
> - **확장 함수**: Bundle, String, Date, Time, TryCatch 등 실용적인 Extensions
> - **안전한 코딩**: safeCatch를 통한 예외 처리 간소화
> - **권한 관리**: PermissionRequester 통합 지원
> - **고급 로깅**: Logx - 파일 저장, 필터링, 커스텀 포매팅 지원
> - **로컬 저장**: 위임자 기반 설정 관리 + 안전 커밋 (BaseSharedPreference)
  
<br> </br>

### ⚙️ **Effortless System Control (System Manager) (간단히 사용가능한 시스템 제어 (System Manager))**

- **Notification system**: Control alarms and notifications
- **Network tools**: Detailed management for Wi-Fi, connectivity, and SIM info
- **Carrier information**: Telephony support for GSM/LTE/5G NR/CDMA/WCDMA
- **Device monitoring**: Real-time monitoring for battery, display, and location
- **UI controls**: SoftKeyboard, Vibrator, FloatingView (Drag/Fixed)
- **SystemBar note**: `BEHAVIOR_DEFAULT` is reset only in visibility APIs (`setStatusBarVisible/Gone`, `setNavigationBarVisible/Gone`)
> - **알림 시스템**: Alarm, Notification 제어
> - **네트워크 종합**: WiFi, Network Connectivity, Sim Info 상세 관리
> - **통신망 정보**: Telephony (GSM/LTE/5G NR/CDMA/WCDMA)  지원
> - **디바이스 정보**: Battery, Display, Location 실시간 모니터링
> - **UI 제어**: SoftKeyboard, Vibrator, FloatingView (Drag/Fixed)
> - **SystemBar 주의사항**: 가시성 API(`setStatusBarVisible/Gone`, `setNavigationBarVisible/Gone`)에서만 `BEHAVIOR_DEFAULT`가 재설정됩니다.

<br>
</br>

### 🪟 **SystemBar Quick Example (빠른 사용 예시)**

```kotlin
import android.graphics.Color
import kr.open.library.simple_ui.xml.system_manager.extensions.destroySystemBarControllerCache
import kr.open.library.simple_ui.xml.system_manager.extensions.getSystemBarController

fun applySystemBar(window: Window) {
    val controller = window.getSystemBarController() // 권장 진입 경로

    controller.setStatusBarColor(Color.TRANSPARENT, isDarkIcon = true)
    controller.setNavigationBarColor(Color.BLACK, isDarkIcon = false)

    controller.setStatusBarVisible()      // 여기서만 BEHAVIOR_DEFAULT 재설정
    controller.setNavigationBarVisible()  // 여기서만 BEHAVIOR_DEFAULT 재설정
}

fun clearSystemBar(window: Window) {
    window.destroySystemBarControllerCache() // Window 캐시 정리
}
```

- 아이콘/색상 API(`setStatusBarDarkIcon`, `setNavigationBarDarkIcon`, `setStatusBarColor`, `setNavigationBarColor`)는 `systemBarsBehavior`를 변경하지 않습니다.
- `window.getSystemBarController()` / `window.destroySystemBarControllerCache()`는 `@MainThread` 계약이며 Debug 빌드에서는 오프 메인스레드 호출 시 `IllegalStateException`으로 즉시 실패합니다.
- 상세 계약(상태 모델, Hidden 기준, API 35+ 폴백)은 `README_SYSTEMBAR_CONTROLLER.md`를 참고하세요.
- View 확장 연계: `clearTint()`는 Image tint만 제거하며 `makeGrayscale()`의 `colorFilter`는 유지됩니다.
- View 확장 연계: `applyWindowInsetsAsPadding(bottom = true)`는 `systemBars.bottom`과 `ime.bottom` 중 큰 값을 반영합니다.
- View 확장 연계: `bindLifecycleObserver`/`unbindLifecycleObserver`는 Observer별 독립 추적 모델입니다.
- 상세 내용은 `README_EXTENSIONS.md`를 참고하세요.

<br>
</br>

## **Examples (예제)**

- **Logx example**: [README_LOGX.md](README_LOGX.md)
- **Activity/Fragment example**: [README_ACTIVITY_FRAGMENT.md](README_ACTIVITY_FRAGMENT.md)
- **MVVM pattern example**: [README_MVVM.md](README_MVVM.md)
- **Layout style XML example**: [README_STYLE.md](README_STYLE.md)
- **Recycler/Adapter example**: [README_RECYCLERVIEW.md](README_RECYCLERVIEW.md)
- **Extensions example**: [README_EXTENSIONS.md](README_EXTENSIONS.md)
- **Permission example**: [README_PERMISSION.md](README_PERMISSION.md)
- **System Service Manager Info example**: [README_SERVICE_MANAGER_INFO.md](system_manager/info/README_SERVICE_MANAGER_INFO.md)
- **System Service Manager Controller example**: [README_SERVICE_MANAGER_CONTROL.md](system_manager/controller/README_SERVICE_MANAGER_CONTROL.md)
- **SystemBar controller detail**: [README_SYSTEMBAR_CONTROLLER.md](system_manager/controller/xml/README_SYSTEMBAR_CONTROLLER.md)
- **Quick start** example: [README_SAMPLE.md](README_SAMPLE.md)


> **Note:** Check [JitPack Releases](https://jitpack.io/#Rhpark/Simple_UI_XML) for the latest version.

<br>
</br>

.


