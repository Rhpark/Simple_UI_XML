# 🚀 Simple UI XML — We Handle the Complexity, You Get the Speed

> **Tired of repetitive Activity/Fragment setup**, **endless permission handling**, and ever-growing boilerplate code?
> We built **Simple UI XML** to give you that time back.

<br>
</br>

## ✨ Simple UI XML: Core Benefits & Why You Need It (Numbers that matter)

- **250 lines → 87 lines / 4–5h → 2~3h (≈50% reduction)** — Strip away boilerplate, keep only what matters.
- **Complex Android APIs in "one line"** — Automated boilerplate for Activity setup, permission handling, and more.
- **Built-in features developers actually want** — Immediate productivity boost from day one.
- **Less repetitive coding → Focus on core features** — Improved team-wide **lead time and quality** simultaneously.

<br>
</br>

### 👥 **Team Development Productivity Revolution**
- **Code Consistency Guaranteed**: All team members use the same Base classes & Extensions → Maximized bug tracking and maintenance efficiency
- **Accelerated Onboarding**: No need to learn complex Android APIs → Shorter adaptation period for new members
- **70% Reduction in Code Review Time**: Standardized patterns clarify review points → Focus only on core logic
- **Minimized Collaboration Conflicts**: Unified systems like PermissionManager, Logx prevent duplicate implementations


> **Bottom line:** Let **Simple UI XML** handle the complexity while **you** deliver at speed.

<br>
</br>

## 🎯 **Target Users**
**XML View System** environments

<br>
</br>

## 📋 **Library Requirements**

- **minSdk**: 28
- **compileSdk**: 35
- **Kotlin**: 2.0.21
- **Android Gradle Plugin**: 8.8.2

<br>
</br>

## ✨ Key Features

### 📱 **UI Development Acceleration**
- **Base Classes**: RootActivity, BaseActivity, BaseBindingActivity
- **Fragment Support**: RootFragment, BaseFragment, BaseBindingFragment, RootDialogFragment, BaseDialogFragment, BaseBindingDialogFragment
- **RecyclerView**: Various Adapters, ViewHolders, DiffUtil + RecyclerScrollStateView
- **Custom Layouts**: Lifecycle-aware Layout components
- **XML Style System**: Comprehensive UI style library (style.xml)
- **MVVM Support**: Full compatibility with ViewModel and DataBinding

<br>
</br>

### 🔧 **Developer Convenience**
- **Extension Functions**: Practical extensions for Bundle, String, Date, Time, TryCatch, and more
- **Safe Coding**: Simplified exception handling with safeCatch
- **Permission Management**: Integrated PermissionManager support
- **Advanced Logging**: Logx - File saving, filtering, custom formatting support
- **Local Storage**: BaseSharedPreference management

<br>
</br>

### ⚙️ **Easy-to-Use System Controls (System Manager)**
- **Notification System**: Alarm and Notification controls
- **Network Suite**: WiFi, Network Connectivity, Sim Info detailed management
- **Telephony Info**: Support for GSM/LTE/5G NR/CDMA/WCDMA
- **Device Info**: Real-time monitoring of Battery, Display, Location
- **UI Controls**: SoftKeyboard, Vibrator, FloatingView (Drag/Fixed)

<br>
</br>

## Examples
- **Logx Usage** Example: [README_LOGX.md](README_LOGX.md)
- **MVVM Pattern** Example: [README_MVVM.md](README_MVVM.md)
- **Extensions Usage** Example: [README_EXTENSIONS.md](README_EXTENSIONS.md)
- **Layout Style XML** Example: [README_STYLE.md](README_STYLE.md)
- **Recycler/Adapter** Example: [README_RECYCLERVIEW.md](README_RECYCLERVIEW.md)
- **System Service Manager Info** Example: [README_SERVICE_MANAGER_INFO.md](README_SERVICE_MANAGER_INFO.md)
- **System Service Manager Controller** Example: [README_SERVICE_MANAGER_CONTROL.md](README_SERVICE_MANAGER_CONTROL.md)
- **Simple Usage** Example: [README_SAMPLE.md](README_SAMPLE.md)

<br>
</br>

## 🚀 **Installation**

### Step 1: Gradle Configuration

#### 1-1. settings.gradle.kts setup
```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") } // Add JitPack
    }
}
```

<br>
</br>

#### 1-2. build.gradle.kts (Module level) dependency
```kotlin
dependencies {
    implementation("com.github.Rhpark:Simple_UI_XML:0.1.2")
}
```

> **Note**: Check the latest version on [JitPack Releases](https://jitpack.io/#Rhpark/Simple_UI_XML).

<br>
</br>

### Step 2: Enable Build Features

#### 2-1. Enable DataBinding in build.gradle.kts
```kotlin
android {
    buildFeatures {
        dataBinding = true  // Required for MVVM pattern
        // viewBinding = true  // If using ViewBinding only
    }
}
```

<br>
</br>

#### 2-2. Verify Java version settings
```kotlin
android {
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}
```

<br>
</br>

### Step 3: Initialization (Optional)

#### 3-1. Initialize Logx in Application class (if using Logx)
```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize Logx (required for logging features)
        Logx.init(this)
    }
}
```

<br>
</br>

#### 3-2. Register Application in AndroidManifest.xml
```xml
<application
    android:name=".MyApplication"
    ... >
</application>
```

<br>
</br>

### Step 4: Start Using Right Away!

#### 4-1. Activity implementation
```kotlin
class MainActivity : BaseBindingActivity<ActivityMainBinding>(R.layout.activity_main) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // DataBinding and LifecycleOwner automatically configured!
        binding.btnTest.setOnClickListener {
            toastShowShort("Fast development with Simple UI!")
        }
    }
}
```

<br>
</br>

#### 4-2. XML Layout
```xml
<LinearLayout style="@style/Layout.MatchWrap.Vertical">
    <Button
        android:id="@+id/btnTest"
        style="@style/Button.MatchWrap"
        android:text="Click!" />
</LinearLayout>
```

✅ **Done! You can now use all Simple UI features.**


<br>
</br>

**Project Structure**: `simple_ui` module is the actual library, `app` module is for testing samples

<br>
</br>

.