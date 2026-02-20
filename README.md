[![](https://jitpack.io/v/rhpark/Simple_UI_XML.svg)](https://jitpack.io/#rhpark/Simple_UI_XML)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![API](https://img.shields.io/badge/API-28%2B-brightgreen.svg?style=flat)](https://developer.android.com/studio/releases/platforms#9.0)

This library helps you make easy and more simple code for Android developers

> **안드로이드 개발자를 위해 좀 더 간단히 확인 할 수 있거나, 좀 더 간단히 만들 수 있거나.**

<br></br>

## Project Structure

**libraries** (v0.3.0+):
- **simple_core**: UI-independent core functionality (usable with Compose or XML)
- **simple_xml**: XML UI-specific components and extensions

**testing samples**: app module

<br>
</br>

## Gradle

#### 1. settings.gradle.kts
```kotlin
pluginManagement {
    repositories {
        google {
            //...
        }
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://jitpack.io") } // JitPack 추가
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") } // JitPack 추가
    }
}
```

<br>
</br>

#### 2. build.gradle.kts (Module level) 
```kotlin
android {
    //..
    buildFeatures {
        dataBinding = true  // MVVM 패턴 사용 시 필수
        // viewBinding = true  // ViewBinding만 사용할 경우
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    //..
}
//..
dependencies {
    //..
    // Version 0.3.0+ (Modular Structure)
    implementation("com.github.Rhpark.Simple_UI_XML:Simple_UI_Core:0.3.46")  // Core functionality only
    implementation("com.github.Rhpark.Simple_UI_XML:Simple_UI_XML:0.3.46")   // XML UI components (includes Core)

    // Or use XML module only (it automatically includes Core as transitive dependency)
    // implementation("com.github.Rhpark.Simple_UI_XML:Simple_UI_XML:0.3.46")
    //..
}
```

<br>
</br>

## More Information (더 많은 정보 보기)
**General Guides**
- **[Getting Started Guide](docs/readme/README_START.md)** - 시작 가이드, 릴리즈 파이프라인, App Distribution 설정
- **[Activity/Fragment Guide](docs/readme/README_ACTIVITY_FRAGMENT.md)** - 베이스 클래스 및 사용 패턴
- **[Extensions Guide](docs/readme/README_EXTENSIONS.md)** - View/Resource 확장 함수 가이드
- **[Permission Guide](docs/readme/README_PERMISSION.md)** - 권한 처리 가이드
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
- **[Service Manager Control Index](docs/readme/system_manager/controller/README_SERVICE_MANAGER_CONTROL.md)** - Controller 문서 인덱스
- **[Service Manager Info Index](docs/readme/system_manager/info/README_SERVICE_MANAGER_INFO.md)** - Info 문서 인덱스
- **[Display Info Guide](docs/readme/system_manager/info/xml/README_DISPLAY_INFO.md)** - 디스플레이 정보
- **[Location Info Guide](docs/readme/system_manager/info/core/README_LOCATION_INFO.md)** - 위치 정보
- **[Battery Info Guide](docs/readme/system_manager/info/core/README_BATTERY_INFO.md)** - 배터리 정보
- **[Network Info Guide](docs/readme/system_manager/info/core/README_NETWORK_INFO.md)** - 네트워크 정보
- **[SIM Info Guide](docs/readme/system_manager/info/core/README_SIM_INFO.md)** - SIM 정보
- **[Telephony Info Guide](docs/readme/system_manager/info/core/README_TELEPHONY_INFO.md)** - 텔레포니 정보

**Project Artifacts**
- **[API Documentation](https://rhpark.github.io/Simple_UI_XML/api)** - Dokka로 생성된 API 문서
- **[Code Coverage Report](https://rhpark.github.io/Simple_UI_XML/coverage)** - Kover 커버리지 리포트


<br>
</br>

### Feedback / 피드백
- [Q&A: 사용법/오류 질문](https://github.com/Rhpark/Simple_UI_XML/discussions/categories/q-a)
- [Open Discussion: API/기능 제안](https://github.com/Rhpark/Simple_UI_XML/discussions/categories/open-discussion)
- [Showcase: 사용 사례 공유](https://github.com/Rhpark/Simple_UI_XML/discussions/categories/showcase)
- [첫 실행에서 막히면 **First-Run Issue**로 남겨주세요.](https://github.com/Rhpark/Simple_UI_XML/issues/new)

<br>
</br>

.


