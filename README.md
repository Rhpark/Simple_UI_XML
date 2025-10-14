[![](https://jitpack.io/v/rhpark/Simple_UI_XML.svg)](https://jitpack.io/#rhpark/Simple_UI_XML)

This library helps you make easy and more simple code for Android developers

안드로이드 개발자를 위해 좀 더 간단히 확인 할 수 있거나, 좀 더 간단히 만들 수 있거나.

<br>
</br>

## Project Structure

**library**  :simple_ui module 

**testing samples** : app module

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
    implementation("com.github.Rhpark:Simple_UI_XML:0.1.2")
    //..
}
```

<br>
</br>

## More Information
**Korea Ver [README_KR.md](README_KR.md)**

**English ver [README_ENG.md](README_ENG.md)**

<br>
</br>

### Feedback / 피드백
- Q&A: 사용법/오류 질문은 여기로 → <Q&A URL>(https://github.com/Rhpark/Simple_UI_XML/new?template=first-run.yml)
- Ideas: API/기능 제안(사용사례 환영) → <Open Discussion>(https://github.com/Rhpark/Simple_UI_XML/discussions/categories/open-discussion)
- Showcase: 사용 사례 공유 → <Showcase URL>(https://github.com/Rhpark/Simple_UI_XML/discussions/categories/showcase)
- 첫 실행에서 막히면 **First-Run Issue**로 남겨주세요. (https://github.com/Rhpark/Simple_UI_XML/new?template=first-run.yml)

<br>
</br>

.