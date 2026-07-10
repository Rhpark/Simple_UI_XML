[![Maven Central](https://img.shields.io/maven-central/v/io.github.rhpark/dash-droid-core)](https://central.sonatype.com/search?q=io.github.rhpark)
[![](https://jitpack.io/v/rhpark/Simple_UI_XML.svg)](https://jitpack.io/#rhpark/Simple_UI_XML)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![API](https://img.shields.io/badge/API-28%2B-brightgreen.svg?style=flat)](https://developer.android.com/studio/releases/platforms#9.0)

# Simple UI

Simple UI is a modular Android library for reducing recurring platform, XML UI, permission,
logging, and system-service boilerplate.

> Simple UI는 반복되는 Android 플랫폼·XML UI·권한·로깅·시스템 서비스 코드를 줄이기 위한 모듈형 라이브러리입니다.

<br>

## Published Modules (현재 배포 모듈)

The following modules are available in the current `0.4.16` release.

| Module | Maven Central | Purpose |
| --- | --- | --- |
| `simple_core` | `io.github.rhpark:dash-droid-core:0.4.16` | Logging, common extensions, permission policies, and ViewModel helpers |
| `simple_xml` | `io.github.rhpark:dash-droid-xml:0.4.16` | XML Activity/Fragment base classes, permission requests, View extensions, and RecyclerView helpers |
| `simple_compose` | `io.github.rhpark:dash-droid-compose:0.4.16` | Compose permission request State, lifecycle-aware event/effect Flow collection, system bar/edge-to-edge helpers, and LazyList scroll-state helpers |
| `simple_system_manager` | `io.github.rhpark:dash-droid-system-manager:0.4.16` | Android system controllers, Window-based helpers, and device information |

> 아래 모듈은 현재 `0.4.16` 릴리스에서 사용할 수 있습니다.
>
> - `simple_core`: 로깅, 공통 확장, 권한 정책, ViewModel 헬퍼
> - `simple_xml`: XML Activity/Fragment 기반 클래스, 권한 요청, View 확장, RecyclerView 헬퍼
> - `simple_compose`: Compose 권한 요청 State, 이벤트/effect Flow 수집, 시스템 바/edge-to-edge 헬퍼, LazyList 스크롤 상태 헬퍼
> - `simple_system_manager`: Android 시스템 제어, Window 기반 헬퍼, 디바이스 정보
>
> 저장소의 `app` 모듈은 라이브러리 배포물이 아니라 테스트·사용 예제용 앱입니다.

<br>

## Requirements (사용 환경)

- Android `minSdk 28` or higher
- Java/Kotlin JVM target 11
- Current library build baseline: `compileSdk 35`

> - Android `minSdk 28` 이상
> - Java/Kotlin JVM target 11
> - 현재 라이브러리 빌드 기준: `compileSdk 35`

## Maven Central (Recommended)

### 1. `settings.gradle.kts`
```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
```

### 2. Select dependencies (필요한 모듈 선택)

Add only the modules used by the app.

> 앱에서 사용하는 모듈만 추가하세요.

#### Core APIs

Use this coordinate when the app directly imports `simple_core` APIs or types.

> 앱 코드에서 `simple_core` API 또는 타입을 직접 사용하면 이 의존성을 추가해야 합니다.

```kotlin
dependencies {
    implementation("io.github.rhpark:dash-droid-core:0.4.16")
}
```

#### XML UI

`dataBinding` and `viewBinding` are optional. Enable only the binding mode used by the selected XML base classes.

> `dataBinding`과 `viewBinding`은 선택 사항입니다. 사용하는 XML 기반 클래스에 필요한 바인딩 방식만 활성화하세요.

```kotlin
android {
    buildFeatures {
        dataBinding = true // BaseDataBinding* 사용 시
        // viewBinding = true // BaseViewBinding* 사용 시
    }
}

dependencies {
    implementation("io.github.rhpark:dash-droid-xml:0.4.16")
}
```

#### Compose UI

```kotlin
plugins {
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation("io.github.rhpark:dash-droid-compose:0.4.16")
}
```

#### System Manager

```kotlin
dependencies {
    implementation("io.github.rhpark:dash-droid-system-manager:0.4.16")
}
```

`dash-droid-xml`, `dash-droid-compose`, and `dash-droid-system-manager` are built on
`dash-droid-core`. This internal relationship does not replace an app's direct dependency
declaration. Add `dash-droid-core` whenever app source code imports Core APIs or Core model types.

> `dash-droid-xml`, `dash-droid-compose`, `dash-droid-system-manager`는 `dash-droid-core`를
> 기반으로 동작합니다. 이 내부 관계가 앱의 직접 의존성 선언을 대신하지는 않습니다. 앱 코드에서
> Core API나 Core 모델 타입을 사용한다면 `dash-droid-core`를 직접 추가하세요.

<br>

## JitPack (Compatibility)

Add the repository in `settings.gradle.kts`.

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

| Module | JitPack coordinate |
| --- | --- |
| Core | `com.github.Rhpark.Simple_UI_XML:Simple_UI_Core:0.4.16` |
| XML | `com.github.Rhpark.Simple_UI_XML:Simple_UI_XML:0.4.16` |
| Compose | `com.github.Rhpark.Simple_UI_XML:Simple_UI_Compose:0.4.16` |
| System Manager | `com.github.Rhpark.Simple_UI_XML:Simple_UI_System_Manager:0.4.16` |

Add only the required coordinates to the app module's `dependencies` block.

> `settings.gradle.kts`에 JitPack 저장소를 추가한 뒤 앱 모듈의 `dependencies` 블록에 필요한 좌표만 추가하세요.
>
> Maven Central 좌표를 권장합니다. JitPack 좌표는 기존 프로젝트 호환용으로만 유지됩니다.  

<br>

## More Information (더 많은 정보 보기)
**General Guides**
- **[Getting Started Guide](docs/readme/README_START.md)** - 시작 가이드, 릴리즈 파이프라인, App Distribution 설정
- **[Activity/Fragment Guide](docs/readme/README_ACTIVITY_FRAGMENT.md)** - 베이스 클래스 및 사용 패턴
- **[Extensions Guide](docs/readme/README_EXTENSIONS.md)** - View/Resource 확장 함수 가이드
- **[Permission Guide](docs/readme/README_PERMISSION.md)** - 권한 처리 가이드
- **[Compose Guide](docs/readme/README_COMPOSE.md)** - Compose 권한/이벤트/시스템 바/스크롤 상태 가이드
- **[MVVM Guide](docs/readme/README_MVVM.md)** - MVVM 구성 가이드
- **[RecyclerView Guide](docs/readme/README_RECYCLERVIEW.md)** - Adapter/RecyclerView 가이드
- **[Style Guide](docs/readme/README_STYLE.md)** - 스타일 가이드
- **[Logx Guide](docs/readme/README_LOGX.md)** - 로깅 가이드
- **[Sample Guide](docs/readme/README_SAMPLE.md)** - 샘플 비교 가이드

**System Manager**
- **[System Manager Extensions](docs/readme/system_manager/README_SYSTEM_MANAGER_EXTENSIONS.md)** - 확장 함수 진입점
- **[SystemBar Controller Guide](docs/readme/system_manager/controller/xml/README_SYSTEMBAR_CONTROLLER.md)** - 상태 모델, 가시성/색상 정책, `@MainThread` 계약
- **[SoftKeyboard Controller Guide](docs/readme/system_manager/controller/xml/README_SOFTKEYBOARD_CONTROLLER.md)** - IME 제어/반환 계약
- **[FloatingView Controller Guide](docs/readme/system_manager/controller/xml/README_FLOATING_VIEW_CONTROLLER.md)** - 플로팅 뷰 제어
- **[System Manager Control Index](docs/readme/system_manager/controller/README_SERVICE_MANAGER_CONTROL.md)** - Controller 문서 인덱스
- **[System Manager Info Index](docs/readme/system_manager/info/README_SERVICE_MANAGER_INFO.md)** - Info 문서 인덱스
- **[Display Info Guide](docs/readme/system_manager/info/xml/README_DISPLAY_INFO.md)** - 디스플레이 정보
- **[Location Info Guide](docs/readme/system_manager/info/core/README_LOCATION_INFO.md)** - 위치 정보
- **[Battery Info Guide](docs/readme/system_manager/info/core/README_BATTERY_INFO.md)** - 배터리 정보
- **[Network Info Guide](docs/readme/system_manager/info/core/README_NETWORK_INFO.md)** - 네트워크 정보
- **[SIM Info Guide](docs/readme/system_manager/info/core/README_SIM_INFO.md)** - SIM 정보
- **[Telephony Info Guide](docs/readme/system_manager/info/core/README_TELEPHONY_INFO.md)** - 텔레포니 정보

**Project Artifacts**
- **[API Documentation](https://rhpark.github.io/Simple_UI_XML/api)** - Dokka로 생성된 API 문서
- **[Code Coverage Report](https://rhpark.github.io/Simple_UI_XML/coverage)** - Kover 커버리지 리포트
- **[Core API baseline](simple_core/api/simple_core.api)** - `simple_core` 공개 API 기준 파일
- **[XML API baseline](simple_xml/api/simple_xml.api)** - `simple_xml` 공개 API 기준 파일
- **[Compose API baseline](simple_compose/api/simple_compose.api)** - `simple_compose` 공개 API 기준 파일
- **[System Manager API baseline](simple_system_manager/api/simple_system_manager.api)** - `simple_system_manager` 공개 API 기준 파일
- **API validation commands (API 검증 명령)**
  - `./gradlew :simple_core:apiCheck`
  - `./gradlew :simple_system_manager:apiCheck`
  - `./gradlew :simple_xml:apiCheck`
  - `./gradlew :simple_compose:apiCheck`
- **Local Dokka generation (로컬 API 문서 생성)**
  - `./gradlew dokkaHtmlMultiModuleCustom`


<br>

### Feedback / 피드백
- [Q&A: 사용법/오류 질문](https://github.com/Rhpark/Simple_UI_XML/discussions/categories/q-a)
- [Open Discussion: API/기능 제안](https://github.com/Rhpark/Simple_UI_XML/discussions/categories/open-discussion)
- [Showcase: 사용 사례 공유](https://github.com/Rhpark/Simple_UI_XML/discussions/categories/showcase)
- [첫 실행에서 막히면 **First-Run Issue**로 남겨주세요.](https://github.com/Rhpark/Simple_UI_XML/issues/new)

<br>


