# 📦 Simple Style vs Plain Android - Complete Comparison Guide
> **Simple Style vs 순수 Android - 비교 가이드**

## 📦 Module Information (모듈 정보)
- **Module**: `simple_xml` (UI-dependent module / UI 의존 모듈)
- **Location**: `simple_xml/src/main/res/values/style.xml`
- **Provides**: Comprehensive XML style library
  - Layout styles (MatchWrap, WrapWrap, MatchMatch, etc.)
  - View size combinations
  - RecyclerView LayoutManager orientations
  - Weight-based layouts

> - **모듈**: `simple_xml` (UI 의존 모듈)
> - **위치**: `simple_xml/src/main/res/values/style.xml`
> - **제공 기능**: XML Style 라이브러리
>   - Layout Style (MatchWrap, WrapWrap, MatchMatch 등)
>   - View 크기 조합
>   - RecyclerView LayoutManager 방향
>   - Weight 기반 Layout

### Style Output Example (style 출력 예시)
![xml_style.png](../../example_gif/xml_style.png)

> **"Achieve Simpler Layouts with Style XML!"** See the tangible difference that Style provides compared to Plain XML at a glance.
<br>
> **"더 간단한 Layout Style XML로!"** 순수 XML 대비 Style이 주는 체감 차이를 한눈에 확인하세요.

<br>

## 🔎 At a Glance Comparison (한눈 비교)

### XML Style System (XML Style 시스템)
| Category                                  | Plain Android                                                                                                     | Simple UI                                         | Impact                 |
|:------------------------------------------|:------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------|:-----------------------|
| **Basic Layout**                          | `layout_width="match_parent"`<br>`layout_height="wrap_content"`<br>`orientation="vertical"`<br>`gravity="center"` | `style="@style/Layout.MatchWrap.Vertical.Center"` | **4 attrs→1 line**     |
| **Weight Equal Distribution**             | `layout_width="0dp"`<br>`layout_weight="1"`<br>`layout_height="wrap_content"`                                     | `style="@style/View.WeightWrap"`                  | **3 attrs→1 line**     |
| **View Size**                             | `layout_width="match_parent"`<br>`layout_height="wrap_content"`                                                   | `style="@style/View.MatchWrap"`                   | **2 attrs→1 line**     |
| **Nested Properties**                     | orientation + gravity + width + height + etc.. written every time                                                 | Combine various styles through chaining           | **Remove Repetition ** |

**Key Point:** Style XML replaces repeated layout attributes with a reusable style reference.
> **핵심:** Style XML은 반복되는 Layout 속성을 재사용 가능한 Style 참조로 바꿉니다.

<br>

## 💡 Why Simple Style XML Matters (Style XML이 유용한 이유)

### ⚡ **Concise XML Too (XML도 간결하게)**
- **Style Inheritance**: `Layout.MatchWrap.Vertical.Center` - 4 properties in 1 line
- **Weight Automation**: `View.WeightWrap` - automatically sets width=0dp + weight=10
- **Error Prevention**: width/height cannot be omitted

> - **Style 상속**: `Layout.MatchWrap.Vertical.Center`로 네 개 속성을 한 줄에서 지정합니다.
> - **Weight 자동 설정**: `View.WeightWrap`이 width=0dp와 weight=10을 설정합니다.
> - **누락 방지**: Style이 width/height를 함께 제공하므로 속성 누락 가능성을 줄입니다.

<br>

## 📦 Simple UI Style vs Plain XML Setup (Simple UI Style과 순수 XML 설정 비교)


<details>
<summary><strong>Plain Android - Write Properties Every Time (순수 Android - 매번 속성 작성)</strong></summary>

```xml
<!-- Write width/height every time (매번 width/height 작성) -->
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical">

    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Title" />

    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Content" />
</LinearLayout>

<!-- Weight setting - repetitive writing (Weight 설정 - 반복 작성) -->
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal">

    <Button
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:text="Button 1" />

    <Button
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:text="Button 2" />
</LinearLayout>

<!-- Gravity direction - combined writing (Gravity 방향 - 조합 작성) -->
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:gravity="center">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Centered" />
</LinearLayout>
```
**Issues:** Write width/height every time, repeat weight + width=0dp, combine orientation + gravity direction
> **문제점:** 매번 width/height 작성, weight + width=0dp 반복, orientation + gravity 방향 조합

<br>
</details>

<details>
<summary><strong>Simple UI - Automatic Through Style Inheritance (Simple UI - Style 상속으로 자동)</strong></summary>

```xml
<!-- Layout.MatchWrap.Vertical - Set direction! (방향 설정!) -->
<LinearLayout
    style="@style/Layout.MatchWrap.Vertical">

    <TextView
        style="@style/TextView.MatchWrap"
        android:text="Title" />

    <TextView
        style="@style/TextView.MatchWrap"
        android:text="Content" />
</LinearLayout>

<!-- View.WeightWrap - Weight enabled! (Weight 가능!) -->
<LinearLayout
    style="@style/Layout.MatchWrap.Horizontal">

    <Button
        style="@style/Button.WeightWrap"
        android:text="Button 1" />

    <Button
        style="@style/Button.WeightWrap"
        android:text="Button 2" />
</LinearLayout>

<!-- Layout.MatchWrap.Horizontal.Center - Gravity direction! (Gravity 방향!) -->
<LinearLayout
    style="@style/Layout.MatchWrap.Horizontal.Center">

    <TextView
        style="@style/TextView.AllWrap"
        android:text="Centered" />
</LinearLayout>

<!-- Multiple direction example! (다중 방향 예시!) -->
<LinearLayout
    style="@style/Layout.MatchWrap.Vertical.CenterHorizontal"
    android:background="#E8EAF6"
    android:padding="16dp">

    <TextView
        style="@style/TextView.AllWrap"
        android:text="Centered Content" />

</LinearLayout>
```
**Result:** Automatically handled via Style inheritance, simple direction setting, eliminate repetitive writing!
> **결과:** Style 상속으로 자동, 방향 설정 간단, 반복 작성 제거!
</details>

<br>

## 🎯 Key Advantages of Style (Style의 주요 장점)

### 📝 Attribute Reduction in This Example (이 예제의 속성 감소)

The example replaces four repeated XML attributes with one style reference and keeps width/height in the shared style.
> 이 예제는 반복되는 XML 속성 네 개를 Style 참조 한 줄로 바꾸고, width/height를 공통 Style에서 관리합니다.

<br>

## 🎯 Conclusion (결론)

**Style XML** was created to solve the **repetition and inconvenience** of Layout Style configuration in Plain Android.

✅ **XML Style System** - Layout basic properties in just one line! (레이아웃 기본 속성을 단 한줄로!)

Use Style XML when repeated layout attributes should be shared across screens.

> - **Style XML**은 순수 Android에서 Layout Style 설정의 **반복과 불편함**을 해결하기 위해 만들어졌습니다.
> - 화면마다 반복되는 Layout 속성을 공유해야 할 때 Style XML을 사용할 수 있습니다.

<br>

## 📂 Example (예제)

**Path:**
> - 🎨 Layout: `app/src/main/res/layout/activity_extensions_style.xml`

<br>

### 🎯 Supported Style Patterns (지원 가능한 Style 패턴)

### 📐 View.WidthHeight.

Basic size combinations available for all Views (모든 View에서 사용 가능한 기본 크기 조합):

```xml
<!-- Width x Height combinations (Width x Height 조합) -->
<View style="@style/View.MatchWrap" />      <!-- match_parent x wrap_content -->
<View style="@style/View.WrapMatch" />      <!-- wrap_content x match_parent -->
<View style="@style/View.AllMatch" />       <!-- match_parent x match_parent -->
<View style="@style/View.AllWrap" />        <!-- wrap_content x wrap_content -->

<!-- Weight combinations (inside LinearLayout) (Weight 조합 (LinearLayout 내부)) -->
<View style="@style/View.WeightWrap" />     <!-- 0dp(weight=10) x wrap_content -->
<View style="@style/View.WeightMatch" />    <!-- 0dp(weight=10) x match_parent -->
<View style="@style/View.WrapWeight" />     <!-- wrap_content x 0dp(weight=10) -->
<View style="@style/View.MatchWeight" />    <!-- match_parent x 0dp(weight=10) -->
```

<br>

### 🎯 Supported Style Combinations (지원 가능한 Style 조합)

The following lists applicable Style patterns organized by View type.
> 각 View 타입별로 적용 가능한 Style 패턴을 정리.

#### **1️⃣ View.WidthHeight**
```xml
<!-- Pattern (패턴) -->
<View style="@style/View.MatchWrap" />
<View style="@style/View.WrapMatch" />
<View style="@style/View.AllMatch" />
<View style="@style/View.AllWrap" />
<View style="@style/View.WeightWrap" />
<View style="@style/View.WeightMatch" />
```

#### **2️⃣ Layout.WidthHeight.Orientation.Orientation**
```xml
<LinearLayout style="@style/Layout.MatchWrap.Vertical" />
<LinearLayout style="@style/Layout.MatchWrap.Horizontal.Center" />
<LinearLayout style="@style/Layout.WeightWrap.Vertical.CenterHorizontal" />
<LinearLayout style="@style/Layout.AllMatch.Horizontal.CenterVertical" />

<!-- Same for FrameLayout/RelativeLayout (FrameLayout/RelativeLayout도 동일) -->
<FrameLayout style="@style/Layout.MatchWrap" />
<RelativeLayout style="@style/Layout.AllMatch" />
```

#### **3️⃣ TextView.WidthHeight.TextStyle.Gravity**
```xml
<!-- Pattern usage (패턴 사용) -->
<TextView
    style="@style/TextView.MatchWrap.Bold.Center"
    android:text="Title (제목)" />

<!-- Additional properties specified individually (추가 속성은 개별 지정) -->
<TextView
    style="@style/TextView.AllWrap.Normal"
    android:text="Content (내용)"
    android:textColor="@color/black"
    android:textSize="16sp" />
```

#### **4️⃣ Button.WidthHeight.TextStyle**
```xml
<Button
    style="@style/Button.MatchWrap.Bold"
    android:text="OK (확인)" />

<Button
    style="@style/Button.AllWrap"
    android:text="Cancel (취소)" />
```

#### **5️⃣ EditText.WidthHeight.InputType.Gravity**
```xml
<EditText
    style="@style/EditText.MatchWrap.Email"
    android:hint="Enter email (이메일 입력)" />

<EditText
    style="@style/EditText.MatchWrap.Number.Center"
    android:hint="Enter number (숫자 입력)"
    android:inputType="number" />
```

#### **6️⃣ ImageView.WidthHeight.ScaleType**
```xml
<ImageView
    style="@style/ImageView.AllWrap.CenterCrop"
    android:src="@drawable/icon" />

<ImageView
    style="@style/ImageView.MatchWrap.FitCenter"
    android:src="@drawable/banner" />
```

#### **7️⃣ CheckBox / RadioButton / Switch - *.WidthHeight.Checked**
```xml
<CheckBox
    style="@style/CheckBox.MatchWrap.Checked"
    android:text="I agree (동의합니다)"
    android:checked="false" />

<RadioButton
    style="@style/RadioButton.MatchWrap.Unchecked"
    android:text="Option 1 (옵션 1)" />

<Switch
    style="@style/Switch.MatchWrap.On"
    android:text="Receive notifications (알림 받기)" />
```

#### **8️⃣ RadioGroup.WidthHeight.Orientation**
```xml
<RadioGroup
    style="@style/RadioGroup.MatchWrap.Vertical">

    <RadioButton
        style="@style/RadioButton.MatchWrap.Checked"
        android:text="Option 1 (옵션 1)" />

    <RadioButton
        style="@style/RadioButton.MatchWrap.Unchecked"
        android:text="Option 2 (옵션 2)" />
</RadioGroup>

<!-- Horizontal also possible (Horizontal도 가능) -->
<RadioGroup
    style="@style/Layout.MatchWrap.Horizontal">
    <!-- ... -->
</RadioGroup>
```

#### **9️⃣ ProgressBar.WidthHeight.Horizontal.indeterminate**
```xml

<ProgressBar style="@style/ProgressBar.WeightWrap.Horizontal.Indeterminate" />

```

#### **🔟 RecyclerView.WidthHeight.LayoutManager.Orientation**
```xml

<androidx.recyclerview.widget.RecyclerView
    style="@style/RecyclerView.MatchWrap.LinearLayoutManager.Vertical"
    android:id="@+id/rcvList" />

<!-- LayoutManager is set in code (LayoutManager는 코드에서 설정) -->
```

<br>

### 💡 Style System Tips (Style 시스템 활용 팁)

**1. Consistent Naming Pattern (일관된 네이밍 패턴)**
```
[ViewType].[WidthHeight].[Additional Property (추가속성)].[Additional Property (추가속성)]
```

**2. Systematic Combination (체계적 조합)**
```xml
<!-- Bad example - repetitive every time (나쁜 예 - 매번 반복) -->
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:gravity="center">
    <!-- ... -->
</LinearLayout>

<!-- Good example - simplified with Style (좋은 예 - Style로 간결화) -->
<LinearLayout style="@style/Layout.MatchWrap.Vertical.Center">
    <!-- ... -->
</LinearLayout>
```

**3. Custom Extension (커스텀 확장)**
```xml
<!-- Extend Simple UI Style by inheriting in styles.xml (styles.xml에서 Simple UI Style을 상속하여 확장) -->
<style name="MyButton" parent="View.MatchWrap">
    <item name="android:textColor">@color/white</item>
    <item name="android:background">@drawable/button_bg</item>
</style>
```

**4. Weight Utilization (Weight 활용)**
```xml
<!-- Weight-based equal distribution (Weight 기반 균등 분할) -->
<LinearLayout style="@style/Layout.MatchWrap.Horizontal">
    <Button style="@style/View.WeightWrap" android:text="Button1 (버튼1)" />
    <Button style="@style/View.WeightWrap" android:text="Button2 (버튼2)" />
    <Button style="@style/View.WeightWrap" android:text="Button3 (버튼3)" />
</LinearLayout>
```

**Advantages (장점):**
- Reuse width, height, weight, and other shared attributes
- Reduce width/height omissions by defining them in the selected style
- Update shared defaults in one resource
- Apply the same naming pattern across layouts

> - width, height, weight와 그 밖의 공통 속성을 재사용합니다.
> - 선택한 Style에서 width/height를 정의해 속성 누락 가능성을 줄입니다.
> - 공통 기본값을 하나의 리소스에서 변경합니다.
> - Layout 전반에 같은 이름 패턴을 적용합니다.

<br>
