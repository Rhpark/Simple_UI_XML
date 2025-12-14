# DESIGN PRINCIPLES / 설계 원칙

> **We handle the complexity, you keep your speed.**  
> **복잡한 부분은 라이브러리가 맡고, 속도는 개발자가 가져갑니다.**

Simple UI XML is designed to reduce repetitive boilerplate in Android XML-based development and to provide consistent patterns and infrastructure for teams.  
Simple UI XML은 안드로이드 XML 기반 개발에서 반복되는 보일러플레이트를 줄이고, 팀 단위로 일관된 패턴과 인프라를 제공하기 위해 설계되었습니다.

This document explains why we chose this structure and rules, and how to extend the library under the same principles.  
이 문서는 왜 이런 구조와 규칙을 선택했는지, 그리고 같은 원칙 아래에서 라이브러리를 어떻게 확장해야 하는지를 설명합니다.

---

## 1. Motivation & Background (동기와 배경)

### 1.1 What pain did we see? (어떤 문제를 보았는가)

Android XML-based projects often repeat similar patterns and suffer from the same pain points.  
Android XML 기반 프로젝트들은 비슷한 패턴을 반복하며 동일한 문제를 겪습니다.

- **Repetitive Activity/Fragment setup**  
  **반복적인 Activity/Fragment 초기 설정**

  Each screen rewrites similar `onCreate` logic, status bar and navigation bar setup, edge-to-edge flags, and initialization code.  
  각 화면에서 `onCreate` 로직, 상태바·내비게이션바 설정, edge-to-edge 플래그, 초기화 코드를 매번 비슷하게 다시 작성해야 합니다.

- **Never-ending permission handling**  
  **끝나지 않는 권한 처리 보일러플레이트**

  Every feature reimplements permission flows such as request, denial, “don’t ask again”, and navigation to Settings.  
  각 기능마다 권한 요청, 거부, “다시 묻지 않기”, 설정 화면 이동 같은 흐름을 매번 다시 구현해야 합니다.

- **RecyclerView boilerplate hell**  
  **RecyclerView 보일러플레이트 지옥**

  Adapters, ViewHolders, DiffUtil, and scroll state handling are duplicated across multiple screens with slightly different shapes.  
  Adapter, ViewHolder, DiffUtil, 스크롤 상태 처리 로직이 여러 화면에서 형태만 조금 다른 채로 중복됩니다.

- **Scattered system service usage**  
  **프로젝트 전역에 흩어진 시스템 서비스 사용 코드**

  Battery, network, location, notifications, vibration, and keyboard access are implemented ad-hoc in many different places.  
  배터리, 네트워크, 위치, 알림, 진동, 키보드 접근 코드가 여러 위치에 제각각 구현됩니다.

- **Inconsistent logging & diagnostics**  
  **일관되지 않은 로깅과 진단 환경**

  Log format, tags, filtering, and persistence differ by developer, making team debugging harder than it needs to be.  
  로그 포맷, 태그, 필터링, 저장 방식이 개발자마다 달라 팀 차원의 디버깅이 불필요하게 어려워집니다.

These issues increase code size and also make it hard to predict where to look when bugs occur.  
이러한 문제들은 코드량을 늘릴 뿐 아니라, 버그가 발생했을 때 어디를 봐야 할지 예측하기 어렵게 만듭니다.

They slow down onboarding of new teammates and distract code reviews away from core business logic.  
또한 신규 팀원의 온보딩을 늦추고, 코드 리뷰를 핵심 비즈니스 로직이 아닌 패턴 통일 문제로 소모하게 만듭니다.

---

### 1.2 What Simple UI XML aims to do (Simple UI XML이 하려는 일)

Simple UI XML has a straightforward goal for XML-based Android projects.  
Simple UI XML은 XML 기반 안드로이드 프로젝트를 위해 매우 단순한 목표를 가집니다.

> **Move common infrastructure and boilerplate into a shared library, so developers can focus on business logic and UX.**  
> **공통 인프라와 보일러플레이트는 라이브러리로 옮기고, 개발자는 비즈니스 로직과 UX에 집중할 수 있게 만드는 것.**

Concretely, the library aims to:  
구체적으로, 이 라이브러리는 다음을 목표로 합니다.

- **Standardize common patterns**  
  **공통 패턴을 표준화**

  Provide shared patterns for Activity/Fragment setup, permission flows, RecyclerView, system services, and logging.  
  Activity/Fragment 초기 설정, 권한 흐름, RecyclerView, 시스템 서비스, 로깅에 대한 공통 패턴을 제공합니다.

- **Make frequently used features immediately available**  
  **실무에서 자주 쓰는 기능을 즉시 사용 가능하게 제공**

  Ship practical features (Logx, PermissionManager, System Manager, View/Toast/SnackBar/Anim extensions, etc.) ready to use on day one.  
  Logx, PermissionManager, System Manager, View/Toast/SnackBar/Anim 확장 등 실무에서 자주 쓰는 기능을 바로 사용할 수 있게 제공합니다.

- **Increase team-wide speed and consistency**  
  **팀 전체의 속도와 일관성을 동시에 향상**

  Encourage all team members to work on top of the same abstractions (BaseActivity, BaseFragment, BaseViewModel, etc.).  
  모든 팀원이 BaseActivity, BaseFragment, BaseViewModel 같은 동일한 추상화 위에서 작업하도록 유도합니다.

---

## 2. Goals (목표)

### 2.1 Developer Experience & Speed (개발자 경험과 속도)

The first goal is to improve the day-to-day experience and speed of Android XML development.  
첫 번째 목표는 안드로이드 XML 개발의 일상적인 경험과 속도를 향상시키는 것입니다.

- **Reduce boilerplate significantly**  
  **보일러플레이트를 눈에 띄게 줄이기**

  Remove repetitive setup code around activities, fragments, permissions, RecyclerView, and system services.  
  Activity, Fragment, 권한 처리, RecyclerView, 시스템 서비스 주변에서 반복되는 설정 코드를 줄입니다.

- **Turn complex Android APIs into “one liners”**  
  **복잡한 안드로이드 API를 “한 줄 호출”로 만들기**

  Wrap complex API sequences inside extensions, base classes, and managers so screen code expresses intentions, not mechanics.  
  복잡한 API 호출 시퀀스를 확장 함수, 베이스 클래스, 매니저 안에 숨겨 화면 코드는 “의도”만 표현하도록 만듭니다.

- **Make everyday features feel built-in**  
  **매일 쓰는 기능을 기본 기능처럼 느끼게 만들기**

  Provide logging, permission handling, system utilities, and view helpers as if they were part of the platform.  
  로깅, 권한 처리, 시스템 유틸리티, 뷰 헬퍼를 마치 플랫폼 기본 기능처럼 제공하도록 설계합니다.

---

### 2.2 Consistency & Maintainability (일관성과 유지보수성)

The second goal is to make code predictable and easy to maintain over time.  
두 번째 목표는 시간이 지나도 예측 가능하고 유지보수하기 쉬운 코드를 만드는 것입니다.

- **Enforce shared patterns**  
  **공유된 패턴을 강제**

  Use RootActivity, BaseActivity, BaseFragment, and BaseViewModel so all screens share the same base behavior.  
  RootActivity, BaseActivity, BaseFragment, BaseViewModel을 사용해 모든 화면이 동일한 베이스 동작을 공유하도록 합니다.

- **Centralize cross-cutting concerns**  
  **횡단 관심사를 한 곳에 모으기**

  Manage permissions, logging, system information, and common UI patterns in dedicated modules and packages.  
  권한, 로깅, 시스템 정보, 공통 UI 패턴을 전용 모듈·패키지에서 관리합니다.

- **Encourage documentation-first changes**  
  **문서를 먼저 고치는 변경 문화를 장려**

  Update `claude.md` and the relevant `docs/readme/README_*.md` before adding or modifying features in the code.  
  코드를 추가·수정하기 전에 `claude.md`와 관련 `docs/readme/README_*.md`를 먼저 수정하는 흐름을 권장합니다.

---

### 2.3 Testability & Observability (테스트 가능성과 관측 가능성)

The third goal is to keep behavior observable and verifiable.  
세 번째 목표는 동작을 관측 가능하고 검증 가능하게 유지하는 것입니다.

- **State & event exposure via Flows**  
  **Flow를 통한 상태 및 이벤트 노출**

  System managers expose device state via StateFlow, scroll helpers use SharedFlow, and BaseViewModelEvent exposes ViewModel events as Flow.  
  System Manager들은 기기 상태를 StateFlow로 노출하고, 스크롤 헬퍼는 SharedFlow를 사용하며, BaseViewModelEvent는 ViewModel 이벤트를 Flow 형태로 노출합니다.

- **Dedicated test structures**  
  **전용 테스트 구조**

  Unit and Robolectric tests live under clear package paths with consistent naming conventions.  
  Unit 및 Robolectric 테스트는 일관된 네이밍 규칙과 명확한 패키지 경로 아래에 위치합니다.

---

## 3. Non-goals (비목표)

Simple UI XML is intentionally limited in scope and does not try to be a full framework for every project.  
Simple UI XML은 의도적으로 범위를 제한하며, 모든 프로젝트를 위한 거대한 프레임워크가 되려 하지 않습니다.

Defining non-goals helps keep the library focused and maintainable.  
비목표를 명시함으로써 라이브러리를 집중되고 유지보수 가능한 상태로 유지합니다.

---

### 3.1 No business domain logic (비즈니스 도메인 로직은 포함하지 않음)

The library never contains business-specific rules such as payment flows or product-specific logic.  
이 라이브러리는 결제 흐름이나 서비스 고유의 비즈니스 규칙 같은 도메인 전용 로직을 절대 포함하지 않습니다.

All domain logic must live in the application code that uses this library.  
모든 도메인 로직은 이 라이브러리를 사용하는 애플리케이션 코드 안에만 존재해야 합니다.

Simple UI XML focuses only on UI infrastructure and shared utilities.  
Simple UI XML은 UI 인프라와 공용 유틸리티에만 집중합니다.

---

### 3.2 No mixed UI paradigms in one module (한 모듈 안에서 UI 패러다임을 섞지 않음)

The `simple_xml` module is dedicated to the XML View system.  
`simple_xml` 모듈은 XML View 시스템에만 집중합니다.

Compose support is planned for a separate module (for example, `simple_compose`) rather than being mixed into XML modules.  
Compose 지원은 XML 모듈 안에 섞지 않고, 별도 모듈(예: `simple_compose`)에서 다룰 계획입니다.

Each module should own exactly one UI paradigm to keep dependencies and maintenance simple.  
각 모듈이 하나의 UI 패러다임만 책임지도록 해 의존성과 유지보수를 단순하게 유지합니다.

---

### 3.3 No “magic” beyond Android standards (안드로이드 표준을 벗어나는 매직은 지양)

The library does not provide hacks that bypass official Android lifecycle or permission flows.  
이 라이브러리는 공식 안드로이드 라이프사이클이나 권한 흐름을 우회하는 해킹성 기능을 제공하지 않습니다.

Instead, it wraps standard patterns in safer and more readable forms.  
대신 표준 패턴을 더 안전하고 읽기 쉬운 형태로 감쌉니다.

Even if you stop using the library, the underlying Android concepts should remain clear and familiar.  
설령 라이브러리 사용을 중단하더라도, 안에 숨어 있는 안드로이드 개념들은 여전히 명확하고 익숙해야 합니다.

---

### 3.4 Not a full-stack architecture framework (풀스택 아키텍처 프레임워크가 아님)

Simple UI XML is not a full application architecture framework.  
Simple UI XML은 완전한 애플리케이션 아키텍처 프레임워크가 아닙니다.

It supports MVVM and DataBinding well, but it does not force a specific architecture or DI framework.  
MVVM과 DataBinding을 잘 지원하지만, 특정 아키텍처나 DI 프레임워크를 강제하지는 않습니다.

Teams are free to choose their own high-level architecture and DI strategy on top of this library.  
팀은 이 라이브러리 위에서 자신들의 상위 아키텍처와 DI 전략을 자유롭게 선택할 수 있습니다.

---

## 4. High-level Architecture (고수준 아키텍처)

Simple UI XML is organized as a small set of focused modules.  
Simple UI XML은 소수의 역할이 분명한 모듈들로 구성됩니다.

The goal is to separate UI-independent core logic from XML-specific UI components.  
목표는 UI에 독립적인 코어 로직과 XML 전용 UI 컴포넌트를 명확히 분리하는 것입니다.

---

### 4.1 Module Overview (모듈 개요)

At the time of this design, the library is split into two main modules.  
이 문서가 다루는 시점에서 라이브러리는 두 개의 주요 모듈로 나뉩니다.

- **`simple_core` – UI-independent core**  
  **`simple_core` – UI에 의존하지 않는 코어**

  This module contains core utilities, extensions, logging, permissions, system managers, and ViewModel infrastructure that do not depend on specific UI widgets.  
  이 모듈은 특정 UI 위젯에 의존하지 않는 유틸리티, 확장 함수, 로깅, 권한 처리, 시스템 매니저, ViewModel 인프라를 포함합니다.

  It can be reused in both XML-based and future Compose-based projects.  
  이 모듈은 XML 기반 프로젝트뿐 아니라 향후 Compose 기반 프로젝트에서도 재사용할 수 있습니다.

- **`simple_xml` – XML UI components**  
  **`simple_xml` – XML UI 전용 컴포넌트**

  This module provides XML-centric UI components such as activities, fragments, adapters, custom views, and view-related extensions.  
  이 모듈은 Activity, Fragment, Adapter, 커스텀 View, View 관련 확장 함수 등 XML 중심 UI 컴포넌트를 제공합니다.

  It builds on top of `simple_core` and focuses on the Android View system.  
  이 모듈은 `simple_core` 위에서 동작하며, 안드로이드 View 시스템에 초점을 맞춥니다.

---

### 4.2 Layered Responsibilities (레이어 책임 분리)

We intentionally separate concerns into layers so that each layer has a clear responsibility.  
각 레이어가 명확한 책임을 가지도록 의도적으로 관심사를 분리했습니다.

- **Core Layer (`simple_core`)**  
  **코어 레이어 (`simple_core`)**

  Deals with extensions, logging, permissions, system information/controllers, and base ViewModel logic.  
  확장 함수, 로깅, 권한, 시스템 정보·컨트롤러, 기본 ViewModel 로직을 담당합니다.

  This layer does not know about layouts or XML resources.  
  이 레이어는 레이아웃이나 XML 리소스를 알지 못합니다.

- **UI Layer (`simple_xml`)**  
  **UI 레이어 (`simple_xml`)**

  Provides base activities, fragments, dialogs, adapters, RecyclerView helpers, and view-related utilities.  
  Base Activity/Fragment/Dialog, Adapter, RecyclerView 헬퍼, View 관련 유틸리티를 제공합니다.

  It is responsible for wiring XML layouts to the core utilities.  
  XML 레이아웃을 코어 유틸리티들과 연결하는 역할을 맡습니다.

- **App Layer (your application)**  
  **앱 레이어 (사용자 애플리케이션)**

  Implements business logic, feature flows, navigation, and domain-specific rules using this library.  
  이 라이브러리를 사용해 비즈니스 로직, 기능 플로우, 내비게이션, 도메인 규칙을 구현합니다.

  Business rules remain in the app, not inside Simple UI XML.  
  비즈니스 규칙은 Simple UI XML 안이 아니라 앱 코드 안에 남습니다.

---

### 4.3 Data & Flow Direction (데이터와 흐름 방향)

The architecture is designed so that data and control flow are easy to follow.  
데이터와 제어 흐름이 쉽게 추적되도록 아키텍처를 설계했습니다.

- Core utilities expose state and events via flows and well-defined APIs.  
  코어 유틸리티들은 플로우와 명확한 API를 통해 상태와 이벤트를 노출합니다.

- UI components observe and react to those states in activities, fragments, and views.  
  UI 컴포넌트는 Activity, Fragment, View에서 해당 상태를 관찰하고 반응합니다.

- Logging and system managers sit on the side, observing events and providing services without mixing with business logic.
  로깅과 시스템 매니저는 비즈니스 로직과 섞이지 않고, 옆에서 이벤트를 관찰하고 서비스를 제공합니다.

This structure keeps the core logic reusable and testable, while the XML UI layer stays focused on presentation.  
이 구조는 코어 로직을 재사용 가능하고 테스트하기 쉽게 유지하면서, XML UI 레이어가 표현에 집중하도록 합니다.

---

## 5. Design Principles (설계 원칙)

Simple UI XML follows a small set of design principles across all modules.  
Simple UI XML은 모든 모듈에서 일관되게 적용되는 몇 가지 설계 원칙을 따릅니다.

These principles guide how we structure code and how we decide what belongs in the library.  
이 원칙들은 코드를 어떻게 구조화할지, 무엇을 라이브러리에 포함할지 결정하는 기준이 됩니다.

---

### 5.1 Separation of Concerns (관심사 분리)

We separate responsibilities so that each component focuses on a single concern.  
각 컴포넌트가 하나의 책임에만 집중하도록 책임을 분리합니다.

- **Module-level separation**  
  **모듈 단위 분리**

  `simple_core` handles core utilities and infrastructure, while `simple_xml` handles XML UI components.  
  `simple_core`는 코어 유틸리티와 인프라를, `simple_xml`은 XML UI 컴포넌트를 담당합니다.

- **Feature-level separation**  
  **기능 단위 분리**

  Permissions, logging, system managers, and RecyclerView helpers live in dedicated packages instead of being scattered across screens.  
  권한, 로깅, 시스템 매니저, RecyclerView 헬퍼는 화면 곳곳이 아니라 전용 패키지에 모여 있습니다.

---

### 5.2 XML-first, UI-agnostic Core (XML 우선, UI 비의존 코어)

We prioritize the XML View system while keeping the core layer free from specific UI widgets.  
코어 레이어는 특정 UI 위젯에 의존하지 않게 유지하면서, XML View 시스템을 우선 지원합니다.

- **XML-focused UI layer**  
  **XML 중심 UI 레이어**

  `simple_xml` contains Activity, Fragment, Adapter, View, and layout helpers tailored for XML.  
  `simple_xml`에는 XML에 최적화된 Activity, Fragment, Adapter, View, 레이아웃 헬퍼가 포함됩니다.

- **UI-agnostic utilities**  
  **UI에 중립적인 유틸리티**

  `simple_core` keeps extensions, logging, permissions, and system managers usable from both XML and future Compose modules.  
  `simple_core`는 확장 함수, 로깅, 권한, 시스템 매니저를 XML과 향후 Compose 모듈에서 모두 재사용 가능하게 유지합니다.

---

### 5.3 Convention over Configuration (설정보다 관례 우선)

We prefer clear conventions over complex configuration options.  
복잡한 설정 옵션보다 명확한 관례를 우선합니다.

- **Base classes instead of manual setup**  
  **직접 설정 대신 베이스 클래스 사용**

  Screens extend RootActivity, BaseActivity, BaseFragment, and BaseViewModel instead of configuring everything by hand.  
  화면은 모든 설정을 직접 하는 대신 RootActivity, BaseActivity, BaseFragment, BaseViewModel을 확장해서 사용합니다.

- **Standard directory and naming patterns**  
  **표준 디렉터리 및 네이밍 패턴**

  Test paths, package structures, and class names follow consistent patterns so files are easy to find.  
  테스트 경로, 패키지 구조, 클래스 이름은 일관된 패턴을 따라 파일을 쉽게 찾을 수 있게 합니다.

---

### 5.4 Safety & Defensive Coding (안전성과 방어적 코딩)

We design APIs to be safe by default and to fail in predictable ways.  
기본적으로 안전하고, 실패하더라도 예측 가능한 방식으로 동작하도록 API를 설계합니다.

- **Permission and SDK guards**  
  **권한 및 SDK 가드**

  PermissionExtensions and `checkSdkVersion` help guard calls that require specific permissions or API levels.  
  PermissionExtensions와 `checkSdkVersion`는 특정 권한이나 API 레벨이 필요한 호출을 보호하는 데 도움을 줍니다.

- **Safe utility wrappers**  
  **안전한 유틸리티 래퍼**

  Try-catch extensions and defensive checks reduce the chance of crashes from common mistakes.  
  try-catch 확장과 방어적 체크는 흔한 실수로 인한 크래시 가능성을 줄여 줍니다.

---

### 5.5 Testability & Observability (테스트 가능성과 관측 가능성)

We make it possible to test and observe behavior without rewriting components.  
컴포넌트를 다시 작성하지 않고도 동작을 테스트하고 관측할 수 있도록 설계합니다.

- **State & event exposure via Flows**  
  **Flow를 통한 상태 및 이벤트 노출**

  System managers expose device state via StateFlow, scroll helpers use SharedFlow, and BaseViewModelEvent exposes ViewModel events as Flow.  
  System Manager들은 기기 상태를 StateFlow로 노출하고, 스크롤 헬퍼는 SharedFlow를 사용하며, BaseViewModelEvent는 ViewModel 이벤트를 Flow 형태로 노출합니다.

- **Dedicated test structures**  
  **전용 테스트 구조**

  Unit and Robolectric tests live under clear package paths with consistent naming conventions.  
  Unit 및 Robolectric 테스트는 일관된 네이밍 규칙과 명확한 패키지 경로 아래에 위치합니다.

---

### 5.6 Documentation-first & Explicit Rules (문서 우선과 명시적인 규칙)

We document rules and patterns before or alongside code changes.  
코드를 변경하기 전에, 또는 변경과 동시에 규칙과 패턴을 문서로 남깁니다.

- **Module-level `claude.md` files**  
  **모듈별 `claude.md` 파일**

  Each module has its own `claude.md` describing allowed patterns, boundaries, and expectations.  
  각 모듈은 허용되는 패턴, 경계, 기대사항을 기술한 자체 `claude.md`를 가집니다.

- **Theme-specific READMEs**  
  **주제별 README 문서**

  READMEs such as Activity/Fragment, MVVM, RecyclerView, Extensions, and System Manager explain both how and why.  
  Activity/Fragment, MVVM, RecyclerView, Extensions, System Manager 관련 README들은 “어떻게” 뿐 아니라 “왜”도 함께 설명합니다.

---

## 6. Key Abstractions (핵심 추상화)

Simple UI XML exposes a small set of core abstractions that appear repeatedly across projects.  
Simple UI XML은 프로젝트 전반에서 반복해서 등장하는 소수의 핵심 추상화를 제공합니다.

These abstractions embody the design principles and drive consistency in real code.  
이 추상화들은 설계 원칙을 코드 수준에서 구현하고 일관성을 이끌어냅니다.

---

### 6.1 BaseActivity & BaseFragment Family (BaseActivity 및 BaseFragment 계열)

We provide base classes to unify how activities and fragments are structured.  
Activity와 Fragment 구조를 통일하기 위해 베이스 클래스를 제공합니다.

- **Activity base classes**  
  **Activity 베이스 클래스**

  RootActivity and BaseActivity handle window configuration, edge-to-edge, and common lifecycle patterns.  
  RootActivity와 BaseActivity는 윈도 설정, edge-to-edge, 공통 라이프사이클 패턴을 처리합니다.

  BaseBindingActivity adds DataBinding support on top of those behaviors.  
  BaseBindingActivity는 위 동작 위에 DataBinding 지원을 추가합니다.

- **Fragment base classes**  
  **Fragment 베이스 클래스**

  BaseFragment and BaseBindingFragment unify fragment creation, view lifecycle, and binding usage.  
  BaseFragment와 BaseBindingFragment는 Fragment 생성, View 라이프사이클, 바인딩 사용 방식을 통일합니다.

---

### 6.2 PermissionManager & PermissionExtensions (PermissionManager와 PermissionExtensions)

We centralize permission handling into a small, focused set of APIs.  
권한 처리를 소수의 집중된 API로 중앙화합니다.

- **PermissionExtensions in `simple_core`**  
  **`simple_core`의 PermissionExtensions**

  Extensions on Context encapsulate permission checks, rationale, and special cases.  
  Context 확장 함수는 권한 체크, 설명 필요 여부, 특수 케이스를 캡슐화합니다.

- **PermissionManager in `simple_xml`**  
  **`simple_xml`의 PermissionManager**

  PermissionManager orchestrates request flows using ActivityResult and exposes results in a predictable way.  
  PermissionManager는 ActivityResult를 사용해 권한 요청 흐름을 관리하고, 결과를 예측 가능한 방식으로 제공합니다.

---

### 6.3 System Manager & BaseSystemService (System Manager와 BaseSystemService)

We provide a structured way to interact with Android system services.  
안드로이드 시스템 서비스와 상호작용하는 구조화된 방식을 제공합니다.

- **BaseSystemService**  
  **BaseSystemService**

  A base abstraction that standardizes how system services are initialized, observed, and cleaned up.  
  시스템 서비스의 초기화, 관찰, 정리 방식을 표준화하는 베이스 추상화입니다.

- **Info and Controller managers**  
  **Info 및 Controller 매니저**

  Separate “information” (battery, network, location) from “control” (Wi-Fi, notifications, vibration) managers.  
  “정보”(배터리, 네트워크, 위치)와 “제어”(Wi-Fi, 알림, 진동) 매니저를 분리합니다.

  Many managers expose state as flows so UI layers can observe changes.  
  다수의 매니저는 상태를 플로우로 노출하여 UI 레이어가 변화를 관찰할 수 있게 합니다.

---

### 6.4 Logx DSL (Logx DSL)

We use a DSL-style logging API to standardize how the team logs.  
팀의 로깅 방식을 표준화하기 위해 DSL 스타일 로깅 API를 사용합니다.

- **Configurable yet consistent logging**  
  **설정 가능하지만 일관된 로깅**

  Logx supports configuration for tags, levels, and outputs while keeping a consistent style across the codebase.  
  Logx는 태그, 레벨, 출력 설정을 지원하면서도 코드베이스 전체에서 일관된 스타일을 유지합니다.

- **File and console support**  
  **파일 및 콘솔 지원**

  Logs can be directed to console and optionally to files for deeper diagnostics.  
  로그는 콘솔로 출력되며, 필요 시 파일에도 기록되어 보다 깊은 진단을 돕습니다.

---

### 6.5 Core Extensions (코어 확장 함수)

We rely on targeted extension functions instead of global utility classes.  
전역 유틸리티 클래스 대신 목적이 분명한 확장 함수에 의존합니다.

- **Bundle, String, Date, Time, Display, TryCatch, etc.**  
  **Bundle, String, Date, Time, Display, TryCatch 등**

  Each extension group focuses on a single concern and is placed under a clear package.  
  각 확장 함수 그룹은 하나의 관심사에 집중하며 명확한 패키지 아래에 위치합니다.

- **Readable and intent-focused APIs**  
  **가독성이 높고 의도가 드러나는 API**

  Call sites become more readable, and the intention of the code is clearer than with generic utility helpers.  
  호출부가 더 읽기 쉬워지고, 일반적인 유틸리티 헬퍼보다 코드의 의도가 더 잘 드러납니다.

---

### 6.6 RecyclerView Stack (RecyclerView 계층 구조)

We formalize a set of patterns for building RecyclerView-based UIs.  
RecyclerView 기반 UI를 만들기 위한 패턴 세트를 정형화했습니다.

- **AdapterOperationQueue and adapters**  
  **AdapterOperationQueue 및 다양한 어댑터**

  AdapterOperationQueue helps manage queued list operations, while list and normal adapters cover common scenarios.  
  AdapterOperationQueue는 리스트 작업 큐를 관리하고, list 및 normal 어댑터는 일반적인 시나리오를 처리합니다.

- **ViewHolder and RecyclerScrollStateView**  
  **ViewHolder와 RecyclerScrollStateView**

  A structured ViewHolder hierarchy and RecyclerScrollStateView provide scroll direction and edge detection.  
  구조화된 ViewHolder 계층과 RecyclerScrollStateView는 스크롤 방향과 상단/하단 감지를 제공합니다.

---

### 6.7 BaseViewModel & Event Pattern (BaseViewModel 및 이벤트 패턴)

We provide a base ViewModel and event pattern to standardize UI state handling.  
UI 상태 처리를 표준화하기 위해 베이스 ViewModel과 이벤트 패턴을 제공합니다.

- **BaseViewModel**  
  **BaseViewModel**

  Provides a lifecycle-aware base ViewModel that integrates DefaultLifecycleObserver for direct lifecycle event handling.  
  DefaultLifecycleObserver를 통합해 라이프사이클 이벤트를 직접 처리할 수 있는, 라이프사이클 인지형 기본 ViewModel을 제공합니다.

- **BaseViewModelEvent**  
  **BaseViewModelEvent**

  Extends BaseViewModel with type-safe event channels and Flow-based one-way communication from ViewModel to UI.  
  BaseViewModel 위에 타입 안전한 이벤트 채널과 Flow 기반 단방향 ViewModel→UI 통신을 추가합니다.

- **Event and UI communication**  
  **이벤트 및 UI 통신**

  Events are delivered as Flow so UI layers can collect them in a structured way without ad-hoc LiveData or callback patterns.  
  이벤트는 Flow로 전달되어, UI 레이어가 임시 LiveData나 콜백 패턴 없이 구조화된 방식으로 수신할 수 있습니다.

---

## 7. Extensibility Guide (확장 가이드)

This section describes how to add or change features while staying aligned with the library’s design.  
이 섹션은 라이브러리의 설계 방향을 유지하면서 기능을 추가하거나 변경하는 방법을 설명합니다.

---

### 7.1 Deciding Where New Code Belongs (새 코드의 위치 결정)

Before adding new code, decide which module and layer it should live in.  
새 코드를 추가하기 전에, 어떤 모듈과 레이어에 들어가야 할지 먼저 결정합니다.

- **Use `simple_core` when…**  
  **다음과 같은 경우 `simple_core`에 추가합니다…**

  The feature does not depend on specific views, layouts, or XML resources.  
  기능이 특정 View나 레이아웃, XML 리소스에 의존하지 않을 때입니다.

  Examples: extensions, logging utilities, system managers, permission helpers, base ViewModel.  
  예시: 확장 함수, 로깅 유틸리티, 시스템 매니저, 권한 헬퍼, 베이스 ViewModel 등입니다.

- **Use `simple_xml` when…**  
  **다음과 같은 경우 `simple_xml`에 추가합니다…**

  The feature directly involves XML layouts, activities, fragments, adapters, or custom views.  
  기능이 XML 레이아웃, Activity, Fragment, Adapter, 커스텀 View와 직접 관련될 때입니다.

  Examples: new base activity variations, XML-specific view helpers, RecyclerView components.  
  예시: 새로운 Base Activity 변형, XML 전용 View 헬퍼, RecyclerView 컴포넌트 등입니다.

---

### 7.2 Change Process (변경 절차)

We recommend following a consistent sequence when extending the library.  
라이브러리를 확장할 때 일관된 순서를 따를 것을 권장합니다.

1. **Clarify intent in `claude.md`.**  
   **의도를 `claude.md`에 먼저 정리합니다.**

   Describe what you want to add, where it belongs, and any constraints or non-goals.  
   무엇을 추가할지, 어디에 둘지, 어떤 제약과 비목표가 있는지 명시합니다.

2. **Update or create the relevant README.**  
   **관련 README를 수정하거나 새로 만듭니다.**

   Document how to use the feature and why it exists before or alongside implementation.  
   구현 전후로, 기능의 사용법과 존재 이유를 먼저 문서에 기록합니다.

3. **Implement the feature in the chosen module.**  
   **선택한 모듈에 기능을 구현합니다.**

   Follow existing package structures, naming conventions, and design principles.  
   기존 패키지 구조, 네이밍 규칙, 설계 원칙을 따릅니다.

4. **Add tests in the appropriate path.**  
   **적절한 경로에 테스트를 추가합니다.**

   Use unit tests for core logic and Robolectric tests for Android-specific behavior.  
   코어 로직에는 단위 테스트를, 안드로이드 종속 동작에는 Robolectric 테스트를 사용합니다.

5. **Verify coverage and behavior.**  
   **커버리지와 동작을 검증합니다.**

   Run the relevant Kover tasks and manually verify key flows where necessary.  
   관련 Kover 태스크를 실행하고, 필요 시 핵심 흐름을 수동으로 검증합니다.

---

### 7.3 When Not to Extend (확장하지 말아야 할 때)

Sometimes the best choice is to keep functionality in the application instead of the library.  
어떤 경우에는 기능을 라이브러리에 넣지 않고 애플리케이션에 두는 것이 최선입니다.

- **Business-specific logic**  
  **비즈니스 특화 로직**

  If the code is tightly coupled to a specific product or domain, keep it in the app layer.  
  코드가 특정 제품이나 도메인에 강하게 묶여 있다면 앱 레이어에 남겨야 합니다.

- **Experimental or one-off patterns**  
  **실험적이거나 일회성 패턴**

  If the pattern has not proven useful across multiple screens or projects, avoid adding it to the shared library.  
  여러 화면이나 프로젝트에서 유용성이 검증되지 않았다면 공유 라이브러리에 추가하지 않는 것이 좋습니다.

- **Conflicting with existing design principles**  
  **기존 설계 원칙과 충돌하는 경우**

  If a change breaks separation of concerns, mixes UI paradigms, or introduces unsafe behavior, reconsider its place.  
  변경이 관심사 분리를 깨거나 UI 패러다임을 섞거나 안전하지 않은 동작을 도입한다면 위치를 재고해야 합니다.

---

## 8. Summary (요약)

Simple UI XML is a focused library designed to make Android XML development faster, safer, and more consistent.  
Simple UI XML은 안드로이드 XML 개발을 더 빠르고 안전하며 일관되게 만들기 위해 설계된, 집중된 라이브러리입니다.

It separates core infrastructure from XML UI components, follows clear design principles, and offers well-defined abstractions for everyday tasks.  
이 라이브러리는 코어 인프라와 XML UI 컴포넌트를 분리하고, 명확한 설계 원칙을 따르며, 일상적인 작업을 위한 잘 정의된 추상화를 제공합니다.

By following the guidelines in this document, you can extend the library while preserving its philosophy and structure.  
이 문서의 가이드를 따르면 라이브러리의 철학과 구조를 유지하면서 기능을 확장할 수 있습니다.

> **Handle the complexity here, and keep your app code simple.**  
> **복잡함은 여기서 처리하고, 앱 코드는 단순하게 유지하십시오.**
