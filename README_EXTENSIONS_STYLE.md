# 📦 Simple UI Extensions & Style vs 순수 Android - 완벽 비교 가이드

![extensions_style_example.gif](example%2Fextensions_style_example.gif)

> **"더 짧은 코드로 Extensions를, 더 명확한 XML로 Style을!"** 순수 Android 대비 Simple UI Extensions & Style이 주는 체감 차이를 한눈에 확인하세요.

<br>
</br>

## 🔎 한눈 비교 (At a glance)

<br>
</br>

### Extensions 비교
| 항목 |           순수 Android            |            Simple UI            |
|:--|:-------------------------------:|:-------------------------------:|
| Toast 표시 | Toast.makeText() Builder 패턴  |   toastShowShort() 심플하게 사용    |
| SnackBar 표시 |     Snackbar.make() 복잡한 설정      | snackBarShowShort() + Option 패턴 |
| TextView 스타일링 |      Paint/Typeface 직접 설정       |    bold() / underline() 체이닝     |
| 단위 변환 (dp↔px) |        TypedValue 반복 코딩         |          dpToPx() 한 줄           |
| 문자열 검증 |         Patterns 수동 매칭          |       isEmailValid() 한 줄        |
| 리소스 접근 |       ContextCompat 버전 분기       |    getDrawableCompat() 자동 처리    |
| 날짜 포맷 |     SimpleDateFormat 생성 필요      |     timeDateToString() 한 줄      |
| 예외 처리 |         try-catch 블록 필요         |       safeCatch() 기본값 지정        |
| 권한 확인 |         일반/특수 권한 분기 처리          |       hasPermission() 통합        |

<br>
</br>

### XML Style 시스템
| 항목 | 순수 Android |           Simple UI           |
|:--|:--:|:-----------------------------:|
| 레이아웃 크기 설정 | 매번 width/height 작성 |         Style 상속으로 간편         |
| 방향 설정 | 매번 orientation 작성 | Layout.MatchWrap.Vertical 한 줄 |
| Weight 설정 | weight + width=0dp 반복 |      View.WeightWrap 한 줄      |
| Gravity 방향 | orientation + gravity 조합 |    .Horizontal.Center 한 줄     |

> **핵심:** Simple UI는 "반복 코드"를 **확장함수**로 해결합니다. 개발 속도가 달라집니다.

<br>
</br>

## 💡 왜 중요한가:

- **개발 속도 향상**: 반복 코드 작성 시간 제거로 생산성 증가
- **코드 가독성**: 직관적 Extensions로 의도 명확화
- **유지보수 용이**: 중복 제거로 Extensions/Style 재사용
- **타입 안전성**: 컴파일 타임 Extensions로 런타임 에러 방지
- **일관성**: XML Style 시스템으로 통일된 UI 구조

<br>
</br>

## 📦 완벽 비교 목록: Extensions & Style vs 순수 Android

**상세 비교 섹션:**
- Extensions: Toast, SnackBar, TextView, 단위 변환, 문자열 검증, 리소스 접근, 날짜, 예외, 권한
- XML Style: Layout 방향 설정 (MatchWrap, WeightWrap, Horizontal.Center 등)
- 실제 예제로 확인
- 코드 라인수 비교

---

<br>
</br>

## 📝 코드 비교

### 첫째: Toast/SnackBar 표시 비교

<details>
<summary><strong>순수 Android - Builder 패턴</strong></summary>

```kotlin
class MainActivity : AppCompatActivity() {

    // Toast - 반복적 Builder 패턴
    private fun showToastShort(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showToastLong(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    // SnackBar - 복잡한 설정
    private fun showSnackBar(message: String) {
        val snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT)
        snackbar.show()
    }

    // SnackBar with Action - 더 복잡한 설정
    private fun showSnackBarWithAction(message: String) {
        val snackbar = Snackbar.make(
            findViewById(android.R.id.content),
            message,
            Snackbar.LENGTH_SHORT
        )
        snackbar.setAction("확인") {
            Toast.makeText(this, "액션 실행!", Toast.LENGTH_SHORT).show()
        }
        snackbar.setActionTextColor(ContextCompat.getColor(this, android.R.color.holo_blue_light))
        snackbar.show()
    }

    // 사용
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.btnToast.setOnClickListener {
            showToastShort("Toast 메시지")
        }

        binding.btnSnackBar.setOnClickListener {
            showSnackBarWithAction("SnackBar 메시지")
        }
    }
}
```
**문제점:** 반복 Builder 패턴, 복잡한 SnackBar 설정, 매번 길게 작성
</details>

<details>
<summary><strong>Simple UI - Extensions 한 줄</strong></summary>

```kotlin
class MainActivity : BaseBindingActivity<ActivityMainBinding>(R.layout.activity_main) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Toast - 한 줄 끝!
        binding.btnToastShort.setOnClickListener {
            toastShowShort("Toast Short 표시")
        }

        binding.btnToastLong.setOnClickListener {
            toastShowLong("Toast Long 표시 - 조금 더 긴 시간 표시됩니다")
        }

        // SnackBar - 한 줄 끝!
        binding.btnSnackBarShort.setOnClickListener {
            binding.root.snackBarShowShort("SnackBar 표시!")
        }

        // SnackBar with Action - Option으로 간단!
        binding.btnSnackBarAction.setOnClickListener {
            binding.root.snackBarShowShort(
                "액션 버튼이 있는 SnackBar",
                SnackBarOption(
                    actionText = "확인",
                    action = { toastShowShort("액션 실행!") }
                )
            )
        }
    }
}
```
**결과:** 한 줄 끝, SnackBarOption으로 명확한 설정, 직관적 메서드명!
</details>

<br>
</br>

### 둘째: TextView 스타일링 비교

<details>
<summary><strong>순수 Android - Paint/Typeface 직접 설정</strong></summary>

```kotlin
class MainActivity : AppCompatActivity() {

    private fun makeTextBold(textView: TextView) {
        textView.setTypeface(textView.typeface, Typeface.BOLD)
    }

    private fun makeTextItalic(textView: TextView) {
        textView.setTypeface(textView.typeface, Typeface.ITALIC)
    }

    private fun addUnderline(textView: TextView) {
        textView.paintFlags = textView.paintFlags or Paint.UNDERLINE_TEXT_FLAG
    }

    private fun removeUnderline(textView: TextView) {
        textView.paintFlags = textView.paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
    }

    private fun addStrikeThrough(textView: TextView) {
        textView.paintFlags = textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
    }

    private fun removeStrikeThrough(textView: TextView) {
        textView.paintFlags = textView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
    }

    private fun resetStyle(textView: TextView) {
        textView.setTypeface(textView.typeface, Typeface.NORMAL)
        removeUnderline(textView)
        removeStrikeThrough(textView)
    }

    // 사용
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.btnBold.setOnClickListener {
            makeTextBold(binding.tvSample)
        }

        binding.btnUnderline.setOnClickListener {
            addUnderline(binding.tvSample)
        }

        binding.btnReset.setOnClickListener {
            resetStyle(binding.tvSample)
        }
    }
}
```
**문제점:** 복잡한 Paint/Typeface 조작, 각 기능마다 메서드 작성 필요, 체이닝 불가
</details>

<details>
<summary><strong>Simple UI - Extensions 체이닝</strong></summary>

```kotlin
class MainActivity : BaseBindingActivity<ActivityMainBinding>(R.layout.activity_main) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TextView 스타일링 - 한 줄 끝!
        binding.btnBold.setOnClickListener {
            binding.tvSampleText.bold()
        }

        binding.btnItalic.setOnClickListener {
            binding.tvSampleText.italic()
        }

        binding.btnUnderline.setOnClickListener {
            binding.tvSampleText.underline()
        }

        binding.btnStrikeThrough.setOnClickListener {
            binding.tvSampleText.strikeThrough()
        }

        // 체이닝 가능!
        binding.btnResetStyle.setOnClickListener {
            binding.tvSampleText.normal()
                .removeUnderline()
                .removeStrikeThrough()
        }

        // 체이닝 예시!
        binding.tvSampleText.bold().underline()
    }
}
```
**결과:** 한 줄 메서드 호출, 체이닝 가능, 직관적 예제!
</details>

<br>
</br>

### 셋째: 단위 변환 (dp↔px, sp↔px) 비교

<details>
<summary><strong>순수 Android - TypedValue 반복 코딩</strong></summary>

```kotlin
class MainActivity : AppCompatActivity() {

    // DP to PX 변환
    private fun dpToPx(dp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            resources.displayMetrics
        )
    }

    // PX to DP 변환
    private fun pxToDp(px: Float): Float {
        return px / resources.displayMetrics.density
    }

    // SP to PX 변환
    private fun spToPx(sp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            sp,
            resources.displayMetrics
        )
    }

    // PX to SP 변환
    private fun pxToSp(px: Float): Float {
        return px / resources.displayMetrics.density / resources.configuration.fontScale
    }

    // 사용
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.btnConvert.setOnClickListener {
            val value = binding.edtValue.text.toString().toFloatOrNull() ?: 0f
            val result = dpToPx(value)
            binding.tvResult.text = "결과: ${value}dp = ${result}px"
        }
    }
}
```
**문제점:** TypedValue 반복 API, 각 변환마다 메서드 작성 필요, displayMetrics 접근
</details>

<details>
<summary><strong>Simple UI - Extensions 한 줄</strong></summary>

```kotlin
class MainActivity : BaseBindingActivity<ActivityMainBinding>(R.layout.activity_main) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // DP to PX - 한 줄 끝!
        binding.btnDpToPx.setOnClickListener {
            val value = binding.edtDisplayValue.text.toString().toFloatOrNull() ?: 0f
            val result = value.dpToPx(this)
            binding.tvDisplayResult.text = "결과: ${value}dp = ${result}px"
        }

        // PX to DP - 한 줄 끝!
        binding.btnPxToDp.setOnClickListener {
            val value = binding.edtDisplayValue.text.toString().toFloatOrNull() ?: 0f
            val result = value.pxToDp(this)
            binding.tvDisplayResult.text = "결과: ${value}px = ${result}dp"
        }

        // SP to PX - 한 줄 끝!
        binding.btnSpToPx.setOnClickListener {
            val value = binding.edtDisplayValue.text.toString().toFloatOrNull() ?: 0f
            val result = value.spToPx(this)
            binding.tvDisplayResult.text = "결과: ${value}sp = ${result}px"
        }
    }
}
```
**결과:** Number Extension으로 직관적, 한 줄 변환 끝!
</details>

<br>
</br>

### 넷째: 문자열 검증 (이메일/숫자) 비교

<details>
<summary><strong>순수 Android - Patterns 수동 매칭</strong></summary>

```kotlin
class MainActivity : AppCompatActivity() {

    // 이메일 검증
    private fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // 숫자 검증
    private fun isNumeric(text: String): Boolean {
        return text.matches("^[0-9]*$".toRegex())
    }

    // 공백 제거
    private fun removeWhitespace(text: String): String {
        return text.replace("\\s".toRegex(), "")
    }

    // 실제 - 실시간 검증
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.edtEmail.addTextChangedListener {
            val email = it.toString()
            if (email.isEmpty()) {
                binding.tvEmailResult.text = "이메일을 입력하세요"
            } else if (isEmailValid(email)) {
                binding.tvEmailResult.text = "✅ 유효한 이메일입니다"
                binding.tvEmailResult.setTextColor(Color.GREEN)
            } else {
                binding.tvEmailResult.text = "❌ 유효하지 않은 이메일입니다"
                binding.tvEmailResult.setTextColor(Color.RED)
            }
        }

        binding.edtNumber.addTextChangedListener {
            val number = it.toString()
            if (isNumeric(number)) {
                binding.tvNumberResult.text = "✅ 숫자입니다"
            } else {
                binding.tvNumberResult.text = "❌ 숫자가 아닙니다"
            }
        }
    }
}
```
**문제점:** 매번 메서드 작성 필요, Regex 패턴 작성 어려움, 반복 검증 로직
</details>

<details>
<summary><strong>Simple UI - Extensions 한 줄</strong></summary>

```kotlin
class MainActivity : BaseBindingActivity<ActivityMainBinding>(R.layout.activity_main) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 이메일 검증 실시간 - isEmailValid() 한 줄!
        binding.edtEmail.addTextChangedListener {
            val email = it.toString()
            if (email.isEmpty()) {
                binding.tvEmailResult.text = "이메일을 입력하세요"
            } else if (email.isEmailValid()) {
                binding.tvEmailResult.text = "✅ 유효한 이메일입니다"
                binding.tvEmailResult.setTextColor(Color.GREEN)
            } else {
                binding.tvEmailResult.text = "❌ 유효하지 않은 이메일입니다"
                binding.tvEmailResult.setTextColor(Color.RED)
            }
        }

        // 숫자 검증 실시간 - isNumeric() 한 줄!
        binding.edtNumber.addTextChangedListener {
            val number = it.toString()
            if (number.isEmpty()) {
                binding.tvNumberResult.text = "숫자를 입력하세요"
            } else if (number.isNumeric()) {
                binding.tvNumberResult.text = "✅ 숫자입니다"
                binding.tvNumberResult.setTextColor(Color.GREEN)
            } else {
                binding.tvNumberResult.text = "❌ 숫자가 아닙니다"
                binding.tvNumberResult.setTextColor(Color.RED)
            }
        }

        // 공백 제거 - removeWhitespace() 한 줄!
        binding.btnRemoveWhitespace.setOnClickListener {
            val original = binding.edtWhitespace.text.toString()
            val removed = original.removeWhitespace()
            binding.tvWhitespaceResult.text = "원본: \"$original\"\n결과: \"$removed\""
        }
    }
}
```
**결과:** String Extension으로 직관적, 컴파일 타임, 한 줄 검증 끝!
</details>

<br>
</br>

### 다섯째: XML Style 시스템 비교

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

---

## 🎯 Simple UI Extensions & Style의 주요 장점

### 1. 📝 압도적 코드 단축

- **Toast/SnackBar**: Builder 패턴 5+ 줄을 Extension 1줄로
- **TextView 스타일링**: Paint/Typeface 설정 여러 줄을 bold() / underline() 체이닝
- **단위 변환**: TypedValue 반복 코딩을 dpToPx() 한 줄로
- **생산성 향상**: 반복 코드 작성 시간 **50% 단축**

<br>
</br>

### 2. 🔧 타입 안전성 Extensions

- **String Extension**: isEmailValid(), isNumeric(), removeWhitespace()
- **Number Extension**: dpToPx(), pxToDp(), spToPx()
- **Context Extension**: getDrawableCompat(), getColorCompat()
- **컴파일 타임 확인**: 런타임 에러 방지

<br>
</br>

### 3. 📦 체계적 패키지 구조

- **view/**: Toast, SnackBar, TextView 스타일링
- **display/**: 단위 변환 (dp↔px, sp↔px)
- **resource/**: 리소스 타입 안전 접근
- **string/**: 문자열 검증/가공
- **date/**: 날짜 포맷
- **trycatch/**: 타입 안전 예외 처리
- **permissions/**: 통합 권한 확인

<br>
</br>

### 4. 🎨 강력한 XML Style 시스템

- **방향 설정**: Layout.MatchWrap.Vertical.Center
- **Weight 자동**: View.WeightWrap (width=0dp + weight 자동)
- **Gravity 방향**: .Horizontal.CenterVertical
- **유지보수 용이**: 중복 제거로 Style 재사용

<br>
</br>

### 5. 🎯 실전 예제 코드

- **실시간 검증**: EditText 입력 시 즉시 피드백
- **체이닝 스타일**: TextView 스타일 동시 적용
- **타입 안전성**: 컴파일 타임 확인으로 예외 방지

---

<br>
</br>

## 📣 개발자 반응

> **"귀찮은 기능을 간단히 끝나니까 너무 편해요!"**
>
> **"TextView 등 여러 View 스타일링도 체이닝으로 한 줄에 끝나서 가독성이 너무 좋습니다!"**
>
> **"dpToPx() 한 줄로 단위 변환이 끝나니 시간이 단축되었어요!"**
>
> **"isEmailValid()로 이메일 검증이 즉시 되니 너무 편합니다!"**
>
> **"XML Style 방향으로 중복 작성 작성이 사라졌어요!"**
>
> **"safeCatch로 try-catch 블록이 간단해졌어요!"**

---

<br>
</br>

## 🎯 결론: Android 개발자를 위한 필수 도구

**Simple UI Extensions & Style**은 순수 Android 개발의 **반복과 불편함**을 해결하기 위해 만들어졌습니다.

✅ **Extensions 비교** - 반복 코드를 간단히!
✅ **XML Style 시스템** - 레이아웃 속성을 방향으로!
✅ **체계적 구조** - 패키지별 명확한 역할!
✅ **타입 안전성** - 컴파일 타임 런타임 에러 방지!

**개발 속도를 높이고 싶다면,**
**Simple UI를 사용해 보세요!** 🚀

---

<br>
</br>

## 📂 실제 코드 확인

**실제 예시 파일:**
> - 🎯 Activity: `app/src/main/java/kr/open/library/simpleui_xml/extenstions_style/ExtensionsStyleActivity`
> - 🎨 Layout: `app/src/main/res/layout/activity_extensions_style.xml`
> - ⚡ 직접 실행해보면 체감 차이를 확실히 느끼실 수 있습니다!

<br>
</br>

**구현된 예제 기능:**
- 📦 view/ - Toast, SnackBar, TextView 스타일링 (bold, italic, underline, strikethrough)
- 📦 display/ - 단위 변환 상호 변환 (dp↔px, sp↔px)
- 📦 resource/ - Drawable, Color 타입 안전 접근
- 📦 string/ - 이메일/숫자 검증 실시간, 공백 제거
- 📦 date/ - 날짜 포맷 다양한 형식
- 📦 trycatch/ - safeCatch 예외 처리
- 📦 permissions/ - CAMERA 권한 확인
- 🎨 XML Style - 다양한 Layout 방향 설정 예시

<br>
</br>

---

## 📦 추가 Extensions 기능 (실제 코드에는 미포함, 라이브러리 내장)

위 예제 Activity에는 포함되지 않았지만, Simple UI 라이브러리에서 제공하는 강력한 Extensions입니다.

<br>
</br>

### 🔢 round_to/ - 숫자 반올림 확장

**제공 기능:**
- `roundTo(decimals)` - 지정된 소수점 자리로 반올림
- `roundUp(decimals)` - 지정된 자리로 올림
- `roundDown(decimals)` - 지정된 자리로 내림

**지원 타입:** Double, Float, Int, Long, Short

**사용 예시:**
```kotlin
// 소수점 반올림
val pi = 3.14159
val rounded = pi.roundTo(2)  // 3.14

// 올림/내림
val price = 3.14159
val up = price.roundUp(2)    // 3.15
val down = price.roundDown(2) // 3.14

// 정수 반올림 (자릿수 기준)
val number = 1234
val rounded = number.roundTo(2)  // 1200
val up = number.roundUp(2)       // 1300
```

**장점:**
- 반복적인 Math.round/ceil/floor 코드 제거
- 모든 숫자 타입에서 일관된 API
- 소수점/정수 모두 동일한 메서드명

<br>
</br>

### 🔀 conditional/ - 조건부 실행 확장

**제공 기능:**

**1. SDK 버전 체크 (checkSdkVersion)**
```kotlin
// 기존 방식
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    // Android 12 이상 코드
}

// Simple UI
checkSdkVersion(Build.VERSION_CODES.S) {
    // Android 12 이상 코드
}

// 분기 처리
val result = checkSdkVersion(Build.VERSION_CODES.S,
    positiveWork = { "Android 12 이상" },
    negativeWork = { "Android 12 미만" }
)
```

**2. 숫자 조건부 실행 (ifGreaterThan, ifEquals)**
```kotlin
// 기존 방식
val score = 85
if (score > 80) {
    showCongratulations()
}

// Simple UI
score.ifGreaterThan(80) {
    showCongratulations()
}

// 분기 처리
val grade = score.ifGreaterThan(80,
    positiveWork = { "A" },
    negativeWork = { "B" }
)

// 같은 값 체크
value.ifEquals(100) {
    showPerfectScore()
}
```

**3. Boolean 조건부 실행 (ifTrue, ifFalse)**
```kotlin
// 기존 방식
if (isLoggedIn) {
    navigateToMain()
} else {
    navigateToLogin()
}

// Simple UI
isLoggedIn.ifTrue(
    positiveWork = { navigateToMain() },
    negativeWork = { navigateToLogin() }
)

// 단일 조건
isNetworkAvailable.ifTrue {
    syncData()
}
```

**4. Null 체크 (firstNotNull)**
```kotlin
// 여러 값 중 첫 번째 null이 아닌 값 반환
val finalValue = firstNotNull(userInput, cachedValue, defaultValue)

// 기존 방식
val finalValue = userInput ?: cachedValue ?: defaultValue
```

**5. Collection 조건부 (filterIf, ifNotEmpty)**
```kotlin
// 조건부 필터링
val results = products.filterIf(showOnSale) { it.isOnSale }

// 비어있지 않을 때만 실행 (체이닝 가능)
notifications
    .ifNotEmpty { updateBadgeCount(it.size) }
    .ifEmpty { hideNotificationIcon() }

searchResults
    .ifEmpty { showNoResultsMessage() }
    .ifNotEmpty { hideNoResultsMessage() }
```

**장점:**
- if문 중첩 제거로 가독성 향상
- 함수형 프로그래밍 스타일
- 체이닝 가능한 API
- SDK 버전 분기 간소화

<br>
</br>

### 📦 bundle/ - Bundle 타입 안전 접근

**제공 기능:**
- `getValue<T>(key, defaultValue)` - 타입 안전한 Bundle 값 추출

**사용 예시:**
```kotlin
// 기존 방식
val userId = bundle.getInt("user_id", -1)
val userName = bundle.getString("user_name", "")
val isActive = bundle.getBoolean("is_active", false)

// Simple UI - Reified Type으로 타입 자동 추론
val userId = bundle.getValue("user_id", -1)
val userName = bundle.getValue("user_name", "")
val isActive = bundle.getValue("is_active", false)

// 지원 타입: Int, Boolean, Float, Long, Double, String, Char, Short, Byte, ByteArray, Bundle
```

**장점:**
- 타입별로 다른 메서드 호출 불필요
- Reified Type으로 컴파일 타임 타입 체크
- Key 누락 시 자동으로 defaultValue 반환 및 로그 출력
- 코드 일관성 향상

<br>
</br>

### 🎨 view/ 패키지 - 추가 View Extensions

예제 코드에는 Toast/SnackBar/TextView만 포함되었지만, 실제로는 더 많은 Extensions를 제공합니다.

#### **EditText Extensions**
```kotlin
// 텍스트 추출
val text = editText.getTextToString()  // text.toString() 단축

// 비어있는지 체크
if (editText.isTextEmpty()) {
    showError("입력해주세요")
}

// 타입 변환
val age = editText.textToInt() ?: 0
val price = editText.textToFloat() ?: 0f
val distance = editText.textToDouble() ?: 0.0
```

#### **ImageView Extensions**
```kotlin
// 이미지 설정
imageView.setImageDrawableRes(R.drawable.icon)

// Tint 설정
imageView.setTint(R.color.primary)
imageView.clearTint()

// Grayscale 효과
imageView.makeGrayscale()  // 흑백 전환
imageView.removeGrayscale()  // 원래대로

// ScaleType 단축
imageView.centerCrop()
imageView.centerInside()
imageView.fitCenter()
imageView.fitXY()

// 체이닝
imageView.load(R.drawable.icon) {
    setTint(R.color.primary)
    centerCrop()
}
```

#### **View Extensions - Visibility**
```kotlin
view.setVisible()    // visibility = VISIBLE
view.setGone()       // visibility = GONE
view.setInvisible()  // visibility = INVISIBLE
```

#### **View Extensions - 중복 클릭 방지**
```kotlin
// 기존 방식 - 수동으로 타이밍 체크
private var lastClickTime = 0L
button.setOnClickListener {
    val currentTime = System.currentTimeMillis()
    if (currentTime - lastClickTime >= 600) {
        lastClickTime = currentTime
        // 실제 로직
    }
}

// Simple UI - Debounced Click
button.setOnDebouncedClickListener(600L) { view ->
    // 600ms 내 중복 클릭 자동 방지
    navigateToNextScreen()
}
```

#### **View Extensions - 레이아웃 조작**
```kotlin
// Margin 설정
view.setMargins(16, 8, 16, 8)  // left, top, right, bottom
view.setMargin(16)              // 모든 면 동일

// Padding 설정
view.setPadding(12)  // 모든 면 동일

// Width/Height 설정
view.setWidth(200)
view.setHeight(100)
view.setSize(200, 100)

// Match/Wrap 설정
view.setWidthMatchParent()
view.setHeightWrapContent()
```

#### **View Extensions - 애니메이션**
```kotlin
// Fade 애니메이션
view.fadeIn(duration = 300L) {
    // 완료 콜백
}
view.fadeOut(duration = 300L, hideOnComplete = true)
view.fadeToggle()  // 토글

// Scale 애니메이션
button.animateScale(fromScale = 1f, toScale = 1.2f, duration = 150L)

// Pulse 효과
heartIcon.pulse(minScale = 0.9f, maxScale = 1.1f, duration = 800L)
heartIcon.stopPulse()

// Slide 애니메이션
panel.slideIn(SlideDirection.RIGHT, duration = 250L)
panel.slideOut(SlideDirection.LEFT, hideOnComplete = true)

// Shake 효과 (에러 피드백)
errorField.shake(intensity = 15f) {
    // 흔들림 완료
}

// Rotate 애니메이션
arrowIcon.rotate(toDegrees = 180f, duration = 200L)
```

#### **View Extensions - 고급 기능**
```kotlin
// Layout 완료 후 실행
customView.doOnLayout { view ->
    val width = view.width
    val height = view.height
    // 실제 크기로 작업
}

// 화면 좌표 가져오기
val (x, y) = button.getLocationOnScreen()

// Window Insets를 Padding으로 적용
rootView.applyWindowInsetsAsPadding(
    left = true,
    top = true,
    right = true,
    bottom = true
)

// ViewGroup 자식 순회
viewGroup.forEachChild { child ->
    // 각 자식 View에 작업
}
```

**장점:**
- 반복 코드 대폭 감소
- 애니메이션 코드 간소화
- 체이닝 가능한 깔끔한 API
- 메모리 누수 방지 (View Tag 시스템 활용)

<br>
</br>

---

## 🎨 XML Style 시스템 상세

Simple UI는 포괄적인 XML Style 시스템을 제공하여 반복적인 속성 작성을 제거합니다.

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
