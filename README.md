[![](https://jitpack.io/v/rhpark/Simple_UI_XML.svg)](https://jitpack.io/#rhpark/Simple_UI_XML)

This library helps you make easy and more simple code for Android developers

<br>
</br>

안드로이드 개발자를 위해 좀 더 간단히 확인 할 수 있거나, 좀 더 간단히 만들 수 있거나.

<br>
</br>

## Project Structure

**library**  :simple_ui module 

**testing samples** : app module

<br>
</br>

## Gradle

#### 1. settings.gradle.kts 설정
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

#### 2. build.gradle.kts (Module level) 디펜던시 추가
```kotlin
dependencies {
    implementation("com.github.Rhpark:Simple_UI_XML:0.1.2")
}
```

<br>
</br>

## More Information
**Korea Ver [README_KR.md](README_KR.md)**

<br>
</br>
 
**English ver [README_ENG.md](README_ENG.md)**