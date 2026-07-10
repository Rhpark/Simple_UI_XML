# 🚀 Simple UI — We handle the complexity, you keep your speed
> **복잡함은 우리가, 속도는 당신에게**

<br></br>

**Repetitive Activity/Fragment setups**, **never-ending permission handling**, and the swelling boilerplate... 

We built **Simple UI** to give you that time back.

> **반복되는 Activity/Fragment 세팅**, **끝나지 않는 권한 처리**, 그리고 불어나는 보일러플레이트...  
> 그 시간을 돌려주기 위해 **Simple UI**를 만들었다.

<br>
</br>

## ✨ Simple UI: Core Impact & Reasons to Adopt (핵심 효과 & 도입 이유)

- **Less screen and permission boilerplate** — Keeps application code focused on its core flow.
- **Wrap common Android platform flows in focused APIs** — Reduces setup code for activities and permissions.
- **Provide reusable Android building blocks** — Covers recurring permission, logging, UI, and system-service tasks.
- **Less repetitive coding → More focus on core features** — Can improve delivery consistency when teams share the same patterns.
 
**One-line takeaway:** Give the complexity to **Simple UI** and keep the speed for **yourself**.
> - **화면·권한 보일러플레이트 감소** — 애플리케이션 코드는 핵심 흐름에 집중합니다.
> - **자주 쓰는 Android 플랫폼 흐름을 목적이 분명한 API로 래핑** — Activity·권한 설정 코드를 줄입니다.
> - **재사용 가능한 Android 구성 요소 제공** — 반복되는 권한·로깅·UI·시스템 서비스 작업을 다룹니다.
> - **반복 코딩 감소 → 핵심 기능 개발 집중** — 팀이 같은 패턴을 사용할 때 개발 일관성을 높일 수 있습니다.
> **한 줄 결론:** 복잡함은 **Simple UI**에게, 속도는 **당신에게**.

<br>
</br>

### 👥 **Team Development Productivity Boost (팀 개발 생산성 혁신)**

- Improve code consistency: Shared base classes and extensions can make bug tracking and maintenance more predictable.
- Support onboarding: Common wrappers reduce the platform-specific details needed for recurring flows.
- Reduce code review overhead: Standardized patterns clarify review points and keep reviews focused on core logic.
- Reduce duplicate implementations: Shared systems such as PermissionRequester and Logx provide a common starting point.
> - 코드 일관성 향상: 공통 Base 클래스와 Extension으로 버그 추적·유지보수 흐름을 예측 가능하게 구성
> - 신규 멤버 온보딩 지원: 반복 흐름에서 직접 다뤄야 하는 플랫폼 세부사항 감소
> - 코드 리뷰 부담 감소: 표준화된 패턴으로 리뷰 지점을 명확히 해 핵심 로직에 집중
> - 중복 구현 감소: PermissionRequester, Logx 같은 공통 시스템을 팀의 시작점으로 활용

<br>
</br>

## 🎯 **Target Users (타겟 사용자)**
- Android developers who want to use permissions, logging, and common utilities more simply (Simple_UI_Core)
- Android developers who want to build XML-based screens faster (Simple_UI_XML)
- Android developers who want Compose-native permission, lifecycle effect, system bar, and lazy-list helpers (Simple_UI_Compose)
- Android developers who want to handle system controls and device information more easily (Simple_UI_System_Manager)

> - 권한, 로깅, 공통 유틸리티를 함께 간단히 쓰고 싶은 안드로이드 개발자 (Simple_UI_Core)
> - XML View 기반 화면을 빠르게 개발하고 싶은 안드로이드 개발자 (Simple_UI_XML)
> - Compose 방식의 권한·라이프사이클 효과·시스템 바·LazyList 헬퍼가 필요한 안드로이드 개발자 (Simple_UI_Compose)
> - 시스템 제어와 디바이스 정보를 손쉽게 다루고 싶은 안드로이드 개발자 (Simple_UI_System_Manager)


<br>
</br>

## 📚 **Documentation Index (문서 인덱스)**

- 전체 README 문서는 [README.md](../../README.md)에서 기능별로 바로 찾을 수 있습니다.
- Compose 전용 설치와 사용법은 [README_COMPOSE.md](README_COMPOSE.md)를 참조하세요.

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

## 🧩 **API Compatibility Baseline (API 호환성 베이스라인)**

- Public API signatures are tracked in:
    - `simple_core/api/simple_core.api`
    - `simple_xml/api/simple_xml.api`
    - `simple_system_manager/api/simple_system_manager.api`
    - `simple_compose/api/simple_compose.api`
- Run `./gradlew :simple_core:apiCheck :simple_system_manager:apiCheck :simple_xml:apiCheck :simple_compose:apiCheck` before merge/release to prevent unintended API breaks.
- If an API change is intentional, run the corresponding `apiDump` task and include the updated `.api` diff.

> - 공개 API 시그니처는 아래 기준 파일로 관리합니다.
>   - `simple_core/api/simple_core.api`
>   - `simple_xml/api/simple_xml.api`
>   - `simple_system_manager/api/simple_system_manager.api`
>   - `simple_compose/api/simple_compose.api`
> - 머지/릴리즈 전 `./gradlew :simple_core:apiCheck :simple_system_manager:apiCheck :simple_xml:apiCheck :simple_compose:apiCheck`를 실행해 의도치 않은 API 변경을 차단합니다.
> - API 변경이 의도된 경우 해당 모듈의 `apiDump` 실행 후 `.api` 변경분을 함께 반영합니다.


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

- **Base classes**: RootActivity, BaseActivity, BaseDataBindingActivity, BaseViewBindingActivity
- **Fragment**: RootFragment, BaseFragment, BaseDataBindingFragment, BaseViewBindingFragment, RootDialogFragment, BaseDialogFragment, BaseDataBindingDialogFragment, BaseViewBindingDialogFragment
- **RecyclerView**: content-only normal adapters (`SimpleRcvAdapter`, `SimpleBindingRcvAdapter`, `SimpleViewBindingRcvAdapter`), section normal adapters (`SimpleHeaderFooterRcvAdapter`, `SimpleHeaderFooterDataBindingRcvAdapter`, `SimpleHeaderFooterViewBindingRcvAdapter`, `HeaderFooterRcvAdapter`), list adapters (`SimpleRcvListAdapter`, `SimpleRcvDataBindingListAdapter`, `SimpleRcvViewBindingListAdapter`), DiffUtil(ListAdapter) + RecyclerScrollStateView
- **RecyclerView click contract**: listeners are attached once in `onCreateViewHolder`, and position/item are resolved at click time (`BaseRcvAdapter`: content index only, `BaseRcvListAdapter`: adapter index)
- **RecyclerView bind signature**: override order is `onBindViewHolder(holder, item, position)` (same order for header/footer bind overrides)
- **RecyclerView mutation contract**: mutation APIs (`setItems`, `addItems`, `removeItem` ...) use `onResult` callbacks and report `NormalAdapterResult` / `ListAdapterResult`
- **Section replace contract**: `HeaderFooterRcvAdapter.setHeaderItems` / `setFooterItems` use `notifyItemRangeChanged` when size/viewType are compatible, otherwise fallback to remove+insert
- **Large removal note**: `BaseRcvAdapter.removeItems(...)` emits per-item `notifyItemRemoved`; for large/contiguous removals, prefer `removeRange` / `removeAll`
- **ListAdapter queue controls**: `BaseRcvListAdapter` provides `setQueuePolicy`
- **Custom layouts**: Layout components with lifecycle awareness
- **XML style system**: Comprehensive UI style library (style.xml)
- **MVVM support**: Fully compatible with ViewModel and DataBinding
> - **기본 클래스**: RootActivity, BaseActivity, BaseDataBindingActivity, BaseViewBindingActivity
> - **Fragment**: RootFragment, BaseFragment, BaseDataBindingFragment, BaseViewBindingFragment, RootDialogFragment, BaseDialogFragment, BaseDataBindingDialogFragment, BaseViewBindingDialogFragment
> - **RecyclerView**: content 전용 normal 어댑터(`SimpleRcvAdapter`, `SimpleBindingRcvAdapter`, `SimpleViewBindingRcvAdapter`), 섹션 normal 어댑터(`SimpleHeaderFooterRcvAdapter`, `SimpleHeaderFooterDataBindingRcvAdapter`, `SimpleHeaderFooterViewBindingRcvAdapter`, `HeaderFooterRcvAdapter`), list 어댑터(`SimpleRcvListAdapter`, `SimpleRcvDataBindingListAdapter`, `SimpleRcvViewBindingListAdapter`), DiffUtil(ListAdapter) + RecyclerScrollStateView
> - **RecyclerView 클릭 규약**: 리스너는 `onCreateViewHolder`에서 1회 연결되고, position/item은 클릭 시점에 조회됩니다(`BaseRcvAdapter`: content 인덱스만 전달, `BaseRcvListAdapter`: adapter 인덱스 전달)
> - **RecyclerView 바인딩 시그니처**: `onBindViewHolder(holder, item, position)` 순서로 오버라이드합니다(header/footer 바인딩도 동일 순서)
> - **RecyclerView 변경 규약**: 변경 API(`setItems`, `addItems`, `removeItem` 등)는 `onResult` 콜백을 통해 `NormalAdapterResult` / `ListAdapterResult`를 전달합니다.
> - **섹션 교체 규약**: `HeaderFooterRcvAdapter.setHeaderItems` / `setFooterItems`는 크기/뷰타입 호환 시 `notifyItemRangeChanged`를 사용하고, 아니면 remove+insert로 반영합니다.
> - **대량 제거 주의**: `BaseRcvAdapter`의 `removeItems(...)`는 항목별 `notifyItemRemoved`를 호출하므로, 대량/연속 제거는 `removeRange` / `removeAll`을 권장합니다.
> - **ListAdapter 큐 제어**: `BaseRcvListAdapter`에서 `setQueuePolicy`를 제공합니다.
> - **커스텀 레이아웃**: Lifecycle 지원하는 Layout 컴포넌트들
> - **XML 스타일 시스템**: 포괄적인 UI 스타일 라이브러리 (style.xml)
> - **MVVM 지원**: ViewModel, DataBinding 호환 지원
 
<br></br>

### Compose Platform Integration (Compose 플랫폼 통합)

- **Unified permissions**: Runtime, special, and role permissions through `rememberPermissionRequestState`
- **Lifecycle effects**: `CollectVmEvent` for channel events and `CollectAsEffect` for non-replaying effect flows
- **System bars**: Icon appearance control with composition-lifetime restoration
- **Lazy list state**: Direction and edge state that returns to `IDLE` when scrolling stops
> - **통합 권한**: `rememberPermissionRequestState`로 런타임·특수·Role 권한 처리
> - **라이프사이클 효과**: Channel 이벤트용 `CollectVmEvent`, 재수집 시 재방출하지 않는 effect Flow용 `CollectAsEffect`
> - **시스템 바**: 컴포지션 수명에 맞춘 아이콘 명암 적용·복원
> - **LazyList 상태**: 스크롤 종료 시 `IDLE`로 복귀하는 방향 상태와 Boolean 엣지 도달 상태

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

### ⚙️ **Effortless System Control (간편한 시스템 제어)**

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


## **Examples (예제)**

- **Logx example**: [README_LOGX.md](README_LOGX.md)
- **Activity/Fragment example**: [README_ACTIVITY_FRAGMENT.md](README_ACTIVITY_FRAGMENT.md)
- **MVVM pattern example**: [README_MVVM.md](README_MVVM.md)
- **Layout style XML example**: [README_STYLE.md](README_STYLE.md)
- **Recycler/Adapter example**: [README_RECYCLERVIEW.md](README_RECYCLERVIEW.md)
- **Extensions example**: [README_EXTENSIONS.md](README_EXTENSIONS.md)
- **Permission example**: [README_PERMISSION.md](README_PERMISSION.md)
- **System Manager Info example**: [README_SERVICE_MANAGER_INFO.md](system_manager/info/README_SERVICE_MANAGER_INFO.md)
- **System Manager Controller example**: [README_SERVICE_MANAGER_CONTROL.md](system_manager/controller/README_SERVICE_MANAGER_CONTROL.md)
- **SystemBar controller detail**: [README_SYSTEMBAR_CONTROLLER.md](system_manager/controller/xml/README_SYSTEMBAR_CONTROLLER.md)
- **Quick start** example: [README_SAMPLE.md](README_SAMPLE.md)
- **Compose guide**: [README_COMPOSE.md](README_COMPOSE.md)


> **Note:** Check [Maven Central](https://central.sonatype.com/search?q=io.github.rhpark) for the latest version.
> JitPack is maintained for legacy compatibility only.
> **참고:** 최신 버전은 [Maven Central](https://central.sonatype.com/search?q=io.github.rhpark)에서 확인할 수 있습니다.
> JitPack은 기존 프로젝트 호환용으로만 유지됩니다.


<br>
</br>

.


