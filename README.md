[![](https://jitpack.io/v/rhpark/Simple_UI_XML.svg)](https://jitpack.io/#rhpark/Simple_UI_XML)

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
    implementation("com.github.Rhpark.Simple_UI_XML:Simple_UI_Core:0.3.2")  // Core functionality only
    implementation("com.github.Rhpark.Simple_UI_XML:Simple_UI_XML:0.3.2")   // XML UI components (includes Core)

    // Or use XML module only (it automatically includes Core as transitive dependency)
    // implementation("com.github.Rhpark.Simple_UI_XML:Simple_UI_XML:0.3.2")
    //..
}
```

<br>
</br>

## More Information (더 많은 정보 보기)
- **[Getting Started Guide](README_START.md)** - 시작 가이드
- **[📚 API Documentation](https://rhpark.github.io/Simple_UI_XML/)** - Dokka로 생성된 API 문서


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