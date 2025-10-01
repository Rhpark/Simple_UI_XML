# 📦 Simple Style vs 순수 Android - 완벽 비교 가이드



> **"더 명확한 XML로 Style을!"** 순수 XML 대비  Style이 주는 체감 차이를 한눈에 확인하세요.

<br>
</br>

## 🔎 한눈 비교 (At a glance)

<br>
</br>

### XML Style 시스템
| 항목 | 순수 Android                                                                                                        | Simple UI                                         | 효과 |
|:--|:------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------|:--|
| **기본 레이아웃** | `layout_width="match_parent"`<br>`layout_height="wrap_content"`<br>`orientation="vertical"`<br>`gravity="center"` | `style="@style/Layout.MatchWrap.Vertical.Center"` | **4속성→1줄** |
| **Weight 균등 분할** | `layout_width="0dp"`<br>`layout_weight="1"`<br>`layout_height="wrap_content"`                                     | `style="@style/View.WeightWrap"`                  | **3속성→1줄** |
| **View 크기** | `layout_width="match_parent"`<br>`layout_height="wrap_content"`                                                   | `style="@style/View.MatchWrap"`                   | **2속성→1줄** |
| **중첩 속성** | orientation + gravity + width + height + etc..매번 작성                                                               | Style 다양한 스타일로 체이닝으로 조합                           | **반복 제거** |

> **핵심:** Style XML은 "반복 코드"를 한줄로 해결합니다. 개발 속도가 달라집니다.

<br>
</br>

## 💡 왜 Style XML이 필수인가?

### ⚡ **XML도 간결하게**
- **Style 상속**: `Layout.MatchWrap.Vertical.Center` - 4개 속성을 1줄로
- **Weight 자동화**: `View.WeightWrap` - width=0dp + weight=10 자동 설정
- **실수 방지**: width/height 누락 불가능

<br>
</br>

## 📦 완벽 비교 목록: Style vs 순수 XML


<details>
<summary><strong>순수 Android - 매번 속성 작성</strong></summary>

```xml
<!-- 매번 width/height 작성 -->
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

<!-- Weight 설정 - 반복 작성 -->
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

<!-- Gravity 방향 - 조합 작성 -->
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
**문제점:** 매번 width/height 작성, weight + width=0dp 반복, orientation + gravity 방향 조합
</details>

<details>
<summary><strong>Simple UI - Style 상속으로 자동</strong></summary>

```xml
<!-- Layout.MatchWrap.Vertical - 방향 설정! -->
<LinearLayout
    style="@style/Layout.MatchWrap.Vertical">

    <TextView
        style="@style/TextView.MatchWrap"
        android:text="Title" />

    <TextView
        style="@style/TextView.MatchWrap"
        android:text="Content" />
</LinearLayout>

<!-- View.WeightWrap - Weight 가능! -->
<LinearLayout
    style="@style/Layout.MatchWrap.Horizontal">

    <Button
        style="@style/Button.WeightWrap"
        android:text="Button 1" />

    <Button
        style="@style/Button.WeightWrap"
        android:text="Button 2" />
</LinearLayout>

<!-- Layout.MatchWrap.Horizontal.Center - Gravity 방향! -->
<LinearLayout
    style="@style/Layout.MatchWrap.Horizontal.Center">

    <TextView
        style="@style/TextView.AllWrap"
        android:text="Centered" />
</LinearLayout>

<!-- 다중 방향 예시! -->
<LinearLayout
    style="@style/Layout.MatchWrap.Vertical.CenterHorizontal"
    android:background="#E8EAF6"
    android:padding="16dp">

    <TextView
        android:text="Centered Content" />
    style="@style/TextView.AllWrap"
</LinearLayout>
```
**결과:** Style 상속으로 자동, 방향 설정 간단, 반복 작성 제거!
</details>

<br>
</br>

## 🎯 Style의 주요 장점

### 📝 **압도적 코드 단축** - XML 속성 **4줄→1줄 (75% 단축)** + width/height 누락 실수 방지

<br>
</br>


## 📣 실제 사용 후기

> 💬 **"XML Style 시스템 도입 후 레이아웃 작성 시간 반 토막. Layout.MatchWrap.Vertical.Center 한 줄이면 끝!"**

---

<br>
</br>

## 🎯 결론: Android 개발자를 위한 필수 도구

**Style XML**은 순수 Android에서 Layout 개발의 **반복과 불편함**을 해결하기 위해 만들어졌습니다.

✅ **XML Style 시스템** - 레이아웃 기본 속성을 단 한줄로!

**개발 속도를 높이고 싶다면,**
**Style XML을 사용해 보세요!** 🚀

---

<br>
</br>

## 📂 실제 코드 확인

**실제 예시 파일:**
> - 🎨 Layout: `app/src/main/res/layout/activity_extensions_style.xml`

<br>
</br>

**구현된 예제 기능:**
- 🎨 XML Style - 다양한 Layout 방향 설정 예시

<br>
</br>

### 📐 기본 View Style 패턴

모든 View에서 사용 가능한 기본 크기 조합:

```xml
<!-- Width x Height 조합 -->
<View style="@style/View.MatchWrap" />      <!-- match_parent x wrap_content -->
<View style="@style/View.WrapMatch" />      <!-- wrap_content x match_parent -->
<View style="@style/View.AllMatch" />       <!-- match_parent x match_parent -->
<View style="@style/View.AllWrap" />        <!-- wrap_content x wrap_content -->

<!-- Weight 조합 (LinearLayout 내부) -->
<View style="@style/View.WeightWrap" />     <!-- 0dp(weight=10) x wrap_content -->
<View style="@style/View.WeightMatch" />    <!-- 0dp(weight=10) x match_parent -->
<View style="@style/View.WrapWeight" />     <!-- wrap_content x 0dp(weight=10) -->
<View style="@style/View.MatchWeight" />    <!-- match_parent x 0dp(weight=10) -->
```

<br>
</br>

### 🔲 Layout Style 패턴 (LinearLayout, FrameLayout, RelativeLayout 등)

Layout은 기본 크기 + Orientation + Gravity 조합을 지원합니다.

#### **기본 구조**
```
Layout.[WidthHeight].[Orientation].[Gravity]
```

#### **사용 예시**
```xml
<!-- Orientation 설정 -->
<LinearLayout style="@style/Layout.MatchWrap.Vertical" />
<LinearLayout style="@style/Layout.MatchWrap.Horizontal" />

<!-- Orientation + Gravity 설정 -->
<LinearLayout style="@style/Layout.MatchWrap.Vertical.Center" />
<LinearLayout style="@style/Layout.MatchWrap.Vertical.CenterHorizontal" />
<LinearLayout style="@style/Layout.MatchWrap.Vertical.CenterVertical" />

<LinearLayout style="@style/Layout.MatchWrap.Horizontal.Center" />
<LinearLayout style="@style/Layout.MatchWrap.Horizontal.CenterHorizontal" />
<LinearLayout style="@style/Layout.MatchWrap.Horizontal.CenterVertical" />

<!-- Weight 조합도 동일하게 지원 -->
<LinearLayout style="@style/Layout.WeightWrap.Vertical" />
<LinearLayout style="@style/Layout.MatchWeight.Horizontal.Center" />
```

<br>
</br>

### 🎯 지원 가능한 View별 Style 조합

각 View 타입별로 적용 가능한 Style 패턴을 정리했습니다.

#### **1️⃣ View.WidthHeight**
```xml
<!-- 패턴:  -->
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

<!-- FrameLayout/RelativeLayout도 동일 -->
<FrameLayout style="@style/Layout.MatchWrap" />
<RelativeLayout style="@style/Layout.AllMatch" />
```

#### **3️⃣ TextView.WidthHeight.TextStyle.Gravity**
```xml
<!-- 패턴:  사용 -->
<TextView
    style="@style/TextView.MatchWrap.Bold.Center"
    android:text="제목" />

<!-- 추가 속성은 개별 지정 -->
<TextView
    style="@style/TextView.AllWrap.Normal"
    android:text="내용"
    android:textColor="@color/black"
    android:textSize="16sp" />
```

#### **4️⃣ Button.WidthHeight.TextStyle**
```xml
<Button
    style="@style/Button.MatchWrap.Bold"
    android:text="확인" />

<Button
    style="@style/Button.AllWrap"
    android:text="취소" />
```

#### **5️⃣ EditText.WidthHeight.InputType.Gravity**
```xml
<EditText
    style="@style/EditText.MatchWrap.TextEmailAddress"
    android:hint="이메일 입력" />

<EditText
    style="@style/View.MatchWrap.Number.Center"
    android:hint="숫자 입력"
    android:inputType="number" />
```

#### **6️⃣ ImageView.WidthHeight.scaleType**
```xml
<ImageView
    style="@style/ImageView.AllWrap.CenterCrop"
    android:src="@drawable/icon" />

<ImageView
    style="@style/View.MatchWrap.FitCenter"
    android:src="@drawable/banner" />
```

#### **7️⃣ CheckBox / RadioButton / Switch - *.WidthHeight.Checked**
```xml
<CheckBox
    style="@style/CheckBox.WidthHeight.Checked"
    android:text="동의합니다"
    android:checked="false" />

<RadioButton
    style="@style/RadioButton.WidthHeight.Unchecked"
    android:text="옵션 1" />

<Switch
    style="@style/Switch.WidthHeight.Checked"
    android:text="알림 받기" />
```

#### **8️⃣ RadioGroup.WidthHeight.Orientation**
```xml
<RadioGroup
    style="@style/RadioGroup.MatchWrap.Vertical">

    <RadioButton
        style="@style/RadioButton.MatchWrap.Check"
        android:text="옵션 1" />

    <RadioButton
        style="@style/RadioButton.MatchWrap.Uncheck"
        android:text="옵션 2" />
</RadioGroup>

<!-- Horizontal도 가능 -->
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

<!-- LayoutManager는 코드에서 설정 -->
```

<br>
</br>

### 💡 Style 시스템 활용 팁

**1. 일관된 네이밍 패턴**
```
[ViewType].[WidthHeight].[추가속성].[추가속성]
```

**2. 체계적 조합**
```xml
<!-- 나쁜 예 - 매번 반복 -->
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:gravity="center">
    <!-- ... -->
</LinearLayout>

<!-- 좋은 예 - Style로 간결화 -->
<LinearLayout style="@style/Layout.MatchWrap.Vertical.Center">
    <!-- ... -->
</LinearLayout>
```

**3. 커스텀 확장**
```xml
<!-- styles.xml에서 Simple UI Style을 상속하여 확장 -->
<style name="MyButton" parent="View.MatchWrap">
    <item name="android:textColor">@color/white</item>
    <item name="android:background">@drawable/button_bg</item>
</style>
```

**4. Weight 활용**
```xml
<!-- Weight 기반 균등 분할 -->
<LinearLayout style="@style/Layout.MatchWrap.Horizontal">
    <Button style="@style/View.WeightWrap" android:text="버튼1" />
    <Button style="@style/View.WeightWrap" android:text="버튼2" />
    <Button style="@style/View.WeightWrap" android:text="버튼3" />
</LinearLayout>
```

**장점:**
- XML 코드 50% 이상 단축
- 실수 방지 (width/height 누락 등)
- 유지보수 용이
- 프로젝트 전체 일관성 유지

<br>
</br>

.
