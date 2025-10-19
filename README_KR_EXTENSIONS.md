# 📦 Simple UI Extensions & Style vs 순수 Android - 완벽 비교 가이드

![extensions_style_example.gif](example%2Fextensions_style_example.gif)

> **"더 짧은 코드로 Extensions을!"** 순수 Android 대비 Simple UI Extensions이 주는 체감 차이를 한눈에 확인하세요.

<br>
</br>

## 🔎 한눈 비교 (At a glance)

<br>
</br>

### Extensions 비교
| 항목 | 순수 Android | Simple UI | 효과 |
|:--|:--|:--|:--|
| 🍞 **Toast 표시** | `Toast.makeText(this, "msg", LENGTH_SHORT).show()` | `toastShowShort("msg")` | **60% 짧게** |
| 🎨 **TextView 스타일** | `setTypeface()` + `paintFlags` 조작 | `bold().underline()` | **체이닝** |
| 📏 **단위 변환** | `TypedValue.applyDimension(...)` | `16.dpToPx(this)` | **82% 짧게** |
| ✉️ **문자열 검증** | `Patterns.EMAIL_ADDRESS.matcher().matches()` | `email.isEmailValid()` | **직관적** |
| 🔢 **숫자 반올림** | `Math.round(x * 100.0) / 100.0` | `x.roundTo(2)` | **간결** |
| 🎯 **조건부 실행** | `if (Build.VERSION.SDK_INT >= S) { }` | `checkSdkVersion(S) { }` | **55% 짧게** |
| 🎬 **View 애니메이션** | ValueAnimator + Listener (15~20줄) | `view.fadeIn()` | **1줄로 완료** |
| 🚫 **중복 클릭 방지** | lastClickTime 변수 + if문 (8줄) | `setOnDebouncedClickListener { }` | **자동 처리** |
| 📦 **Bundle 접근** | `getInt()`, `getString()` 타입별 호출 | `getValue<T>("key", default)` | **타입 안전** |
| 🎨 **ImageView 효과** | ColorMatrix + ColorMatrixColorFilter 설정 | `imageView.makeGrayscale()` | **즉시 적용** |

> **핵심:** Simple UI는 "반복 코드"를 **확장함수**로 해결합니다. 개발 속도가 달라집니다.

<br>
</br>

## 💡 왜 Simple UI Extensions가 필수인가?

### 🚀 **즉시 체감되는 생산성**
- **타이핑 시간 절약**: 긴 메서드 호출을 짧게 단축
  - 예: `Toast.makeText(this, "text", Toast.LENGTH_SHORT).show()` (56자) → `toastShowShort("text")` (22자)
- **SDK 버전 분기 자동화**: Build.VERSION.SDK_INT 체크를 함수로 간소화
- **중복 클릭 버그 제거**: 수동 타이밍 체크 없이 자동 방지

<br>
</br>

### 🛡️ **안전하고 견고한 코드**
- **컴파일 타임 타입 체크**: Bundle.getValue<T>로 런타임 에러 사전 차단
- **Null 안전성**: firstNotNull()로 안전한 기본값 체인
- **예외 처리 간소화**: safeCatch()로 기본값 지정 및 자동 로깅
- **권한 처리 통합**: 일반/특수 권한을 hasPermission() 하나로 해결

<br>
</br>

### 🎨 **직관적이고 읽기 쉬운 코드**
- **메서드 체이닝**: `textView.bold().underline().italic()` - 의도가 명확
- **자연스러운 확장**: `3.14159.roundTo(2)` - 숫자처럼 읽힘
- **조건부 체이닝**: `list.ifNotEmpty { }.ifEmpty { }` - 함수형 스타일
- **애니메이션 DSL**: `view.fadeIn()`, `view.shake()` - 설명 불필요

<br>
</br>

## 📦 완벽 비교 목록: Extensions & Style vs 순수 Android

### 📂 **제공되는 Extensions 패키지** (패키지별 정리)

#### **🎨 view/** - UI 조작 Extensions
- **Toast/SnackBar**: 간단 메시지 표시
- **TextView**: bold(), underline(), italic() 체이닝
- **EditText**: getTextToString(), textToInt(), isTextEmpty()
- **ImageView**: setTint(), makeGrayscale(), centerCrop(), fadeIn()
- **View 애니메이션**: fadeIn/Out(), shake(), pulse(), rotate(), slideIn/Out()
- **View 조작**: setVisible/Gone(), setMargins(), setOnDebouncedClickListener()

<br>
</br>

#### **📏 display/** - 단위 변환 Extensions
- **dp↔px 변환**: 16.dpToPx(), 48.pxToDp()
- **sp↔px 변환**: 14.spToPx(), 42.pxToSp()
- **즉시 사용**: `view.setWidth(100.dpToPx(this))`

<br>
</br>

#### **🔢 round_to/** - 숫자 반올림 Extensions
- **소수점 반올림**: 3.14159.roundTo(2) → 3.14
- **올림/내림**: price.roundUp(2), price.roundDown(2)
- **정수 반올림**: 1234.roundTo(2) → 1200

<br>
</br>

#### **🎯 conditional/** - 조건부 실행 Extensions
- **SDK 체크**: checkSdkVersion(S) { ... }
- **숫자 비교**: score.ifGreaterThan(80), age.ifGreaterThanOrEqual(18), value.ifEquals(100), errorCode.ifNotEquals(0)
- **Boolean**: isLoggedIn.ifTrue { ... }.ifFalse { ... }
- **Collection**: list.ifNotEmpty { }.filterIf(condition) { }

<br>
</br>

#### **📦 bundle/** - Bundle 타입 안전 Extensions
- **타입 안전 접근**: bundle.getValue<Int>("id", 0)
- **자동 타입 추론**: Reified Type으로 컴파일 타임 체크

<br>
</br>

#### **📝 string/** - 문자열 검증/가공 Extensions
- **이메일 검증**: email.isEmailValid()
- **전화번호 검증**: phone.isPhoneNumberValid()
- **URL 검증**: url.isUrlValid()
- **숫자/영문 검증**: text.isNumeric(), username.isAlphaNumeric()
- **공백 제거**: text.removeWhitespace()
- **HTML 태그 제거**: html.stripHtmlTags()

<br>
</br>

#### **📅 date/** - 날짜 포맷 Extensions
- **Long → String**: timestamp.timeDateToString("yyyy-MM-dd")

<br>
</br>

#### **⚠️ trycatch/** - 예외 처리 Extensions
- **3가지 오버로드**: safeCatch(block), safeCatch(defaultValue, block), safeCatch(block, onCatch)
- **코루틴 안전**: CancellationException 자동 전파
- **자동 로깅**: 예외 발생 시 자동 printStackTrace

<br>
</br>

#### **🔐 permissions/** - 권한 확인 Extensions
- **통합 권한 체크**: hasPermission(Manifest.permission.CAMERA)
- **일반/특수 권한 모두 지원**

<br>
</br>

#### **🎨 resource/** - 리소스 접근 Extensions
- **안전한 접근**: getDrawableCompat(R.drawable.icon)
- **버전 분기 자동**: SDK 버전별 자동 처리

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

### 셋째: 단위 변환 (dp ↔ px, sp ↔ px) 비교

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

## 🎯 Simple UI Extensions & Style의 주요 장점

### 1. 📝 **압도적 코드 단축** - 실제 타이핑 비교

#### **매번 작성하는 코드량 비교**

| 기능 | 순수 Android | Simple UI | 개선점 |
|:--|:--|:--|:--|
| **Toast 표시** | `Toast.makeText(this, "msg", Toast.LENGTH_SHORT).show()` <br>(56자) | `toastShowShort("msg")` <br>(22자) | **60% 감소** |
| **SnackBar + Action** | Snackbar.make() + setAction() + setActionTextColor() + show() <br>(약 7줄, 180자+) | `snackBarShowShort("msg", SnackBarOption(...))` <br>(약 4줄, 90자) | **절반으로 단축** |
| **TextView 스타일** | `textView.setTypeface(..., Typeface.BOLD)` <br>`textView.paintFlags = ... or Paint.UNDERLINE_TEXT_FLAG` <br>(2줄, 112자) | `textView.bold().underline()` <br>(1줄, 28자) | **75% 감소** |
| **중복 클릭 방지** | lastClickTime 변수 + if문 체크 <br>(8줄) | `setOnDebouncedClickListener { }` <br>(1줄) | **완전 제거** |
| **단위 변환** | `TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)` <br>(90자) | `dp.dpToPx(this)` <br>(16자) | **82% 감소** |
| **SDK 버전 분기** | `if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { }` <br>(54자) | `checkSdkVersion(S) { }` <br>(24자) | **55% 감소** |

<br>
</br>

#### **한 번 작성으로 반복 사용 (유틸 함수 불필요)**

| 기능 | 순수 Android | Simple UI |
|:--|:--|:--|
| **이메일 검증** | Patterns.EMAIL_ADDRESS.matcher(email).matches() 메서드 작성 필요 | `email.isEmailValid()` - 바로 사용 |
| **숫자 반올림** | Math.round() 계산식 매번 작성 | `price.roundTo(2)` - 바로 사용 |
| **View 애니메이션** | ValueAnimator + Listener 구현 (15~20줄) | `view.fadeIn()` - 바로 사용 |

<br>
</br>

**💡 실측 결과**:
- **타이핑량 평균 55~82% 감소**
- **반복 유틸 메서드 작성 불필요** (라이브러리가 제공)
- **가독성 대폭 향상** (긴 메서드 체인 → 짧고 명확한 이름)

<br>
</br>

### 2. 🛡️ **타입 안전성** - 컴파일 타임에 에러 차단

#### **런타임 에러를 컴파일 타임으로**
```kotlin
// ❌ 순수 Android - 런타임에 터짐
val value = bundle.getInt("age")  // Key 오타 → 0 반환 (버그!)
val name = bundle.getString("name")  // null 반환 가능

// ✅ Simple UI - 컴파일 타임 체크 + 기본값
val age = bundle.getValue("age", 0)  // Reified Type
val name = bundle.getValue("name", "Unknown")  // Null 안전
```

<br>
</br>

#### **타입 추론으로 실수 방지**
```kotlin
// ❌ 순수 Android
val price = 3.14159
val rounded = Math.round(price * 100.0) / 100.0  // 복잡!

// ✅ Simple UI
val rounded = price.roundTo(2)  // 타입 자동 추론
```

<br>
</br>

### 3. 🎨 **직관적 API** - 코드가 곧 문서

#### **자연어처럼 읽히는 코드**
```kotlin
// 조건부 실행
score.ifGreaterThan(80) { showCongratulations() }
isLoggedIn.ifTrue { navigateToMain() }
list.ifNotEmpty { adapter.update(it) }

// 숫자 반올림
price.roundTo(2)      // "가격을 소수점 2자리로 반올림"
count.roundUp(2)      // "개수를 백 단위로 올림"

// View 애니메이션
errorField.shake()    // "에러 필드를 흔들어"
heartIcon.pulse()     // "하트 아이콘을 펄스 효과로"
panel.slideIn(RIGHT)  // "패널을 오른쪽에서 슬라이드"
```

<br>
</br>

#### **체이닝으로 의도 명확화**
```kotlin
textView
    .bold()
    .underline()
    .setTextColor(getColorCompat(R.color.primary))

imageView.load(R.drawable.icon) {
    setTint(R.color.accent)
    centerCrop()
    fadeIn()
}
```

<br>
</br>

### 4. 📦 **체계적 패키지 구조** - 찾기 쉽고 배우기 쉬운 구조

```
kr.open.library.simple_ui.extensions/
├─ view/           → UI 조작 (Toast, TextView, ImageView, 애니메이션)
├─ display/        → 단위 변환 (dp↔px, sp↔px)
├─ round_to/       → 숫자 반올림 (roundTo, roundUp, roundDown)
├─ conditional/    → 조건부 실행 (SDK 체크, ifTrue, ifGreaterThan)
├─ bundle/         → Bundle 타입 안전 접근
├─ string/         → 문자열 검증/가공 (isEmailValid, isPhoneNumberValid, isUrlValid, isNumeric, isAlphaNumeric, stripHtmlTags)
├─ date/           → 날짜 포맷팅
├─ trycatch/       → 예외 처리 (safeCatch)
├─ permissions/    → 권한 확인 통합
└─ resource/       → 리소스 안전 접근
```

**💡 원하는 기능을 패키지명으로 바로 찾기!**

<br>
</br>

### 6. ⚡ **실전에서 바로 쓸 수 있는 기능들**

#### **매일 마주치는 문제를 해결**
```kotlin
// 🚫 중복 클릭으로 화면 2번 열리는 버그
button.setOnDebouncedClickListener { navigateToDetail() }

// 🎬 로딩 인디케이터 페이드 인/아웃
loadingView.fadeIn()
loadingView.fadeOut { it.setGone() }

// 📧 실시간 이메일 검증
if (email.isEmailValid()) { enableSubmit() }

// 🔢 가격 표시 (소수점 2자리)
priceText.text = "${actualPrice.roundTo(2)}원"

// 🎯 SDK 버전별 기능 분기
checkSdkVersion(Build.VERSION_CODES.S) {
    // Android 12 전용 기능
}

// 📦 Intent 데이터 안전하게 가져오기
val userId = intent.extras?.getValue("user_id", -1) ?: -1
```
<br>
</br>

## 📣 실제 사용 후기

> 💬 **"중복 클릭 버그 때문에 매번 lastClickTime 체크하던 게 너무 귀찮았는데, setOnDebouncedClickListener() 하나로 끝나니 감동..."**

> 💬 **"TextView 스타일링 체이닝이 진짜 킬러 기능. bold().underline() 이렇게 쓰니까 코드 읽기 너무 편함!"**

> 💬 **"3.14159.roundTo(2) 이게 Kotlin답다! 더 이상 Math.round() 쓰다가 실수 안 해도 돼요"**

> 💬 **"SDK 버전 분기할 때마다 if (Build.VERSION.SDK_INT >= ...) 타이핑하기 싫었는데, checkSdkVersion()으로 깔끔하게 정리됨"**

> 💬 **"fadeIn(), shake(), pulse() 같은 애니메이션이 한 줄로 되니까 UX 개선 작업이 엄청 빨라졌어요!"**

> 💬 **"Bundle.getValue<T>()로 타입 안전하게 데이터 가져오니까 런타임 버그가 확실히 줄었어요"**

<br>
</br>

## 🎯 결론: Android 개발자를 위한 필수 도구

**Simple UI Extensions**은 순수 Android 개발의 **반복과 불편함**을 해결하기 위해 만들어졌습니다.

✅ **Extensions 비교** - 반복 코드를 간단히!
✅ **체계적 구조** - 패키지별 명확한 역할!
✅ **타입 안전성** - 컴파일 타임 런타임 에러 방지!

**개발 속도를 높이고 싶다면,**
**Simple UI를 사용해 보세요!** 🚀

<br>
</br>

## 📂 실제 코드 확인

**실제 예시 파일:**
> - 🎯 Activity: `package kr.open.library.simpleui_xml.extenstions_style/ExtensionsStyleActivity`
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

<br>
</br>

## 📦 추가 Extensions 기능 (실제 코드에는 미포함, 라이브러리 내장)

위 예제 Activity에는 포함되지 않았지만, Simple UI 라이브러리에서 제공하는 강력한 Extensions입니다.

<br>
</br>

### 🔢 round_to/ - 숫자 반올림 확장

**제공 기능:**
- `roundTo(decimals)` - 지정된 소수점 자리로 반올림
- `roundUp(decimals)` - 지정된 자리로 올림
- `roundDown(decimals)` - 지정된 자리로 내림

<br>
</br>

**지원 타입:** Double, Float, Int, Long, Short

<br>
</br>

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

<br>
</br>

**2. 숫자 조건부 실행 (ifGreaterThan, ifGreaterThanOrEqual, ifEquals, ifNotEquals)**
```kotlin
// 기존 방식
val score = 85
if (score > 80) {
    showCongratulations()
}

// Simple UI - 초과 (>)
score.ifGreaterThan(80) {
    showCongratulations()
}

// 이상 (>=)
age.ifGreaterThanOrEqual(18) {
    allowAccess()
}

// 분기 처리
val grade = score.ifGreaterThan(80,
    positiveWork = { "A" },
    negativeWork = { "B" }
)

val accessLevel = age.ifGreaterThanOrEqual(18,
    positiveWork = { "Adult" },
    negativeWork = { "Minor" }
)

// 같은 값 체크
value.ifEquals(100) {
    showPerfectScore()
}

// 다른 값 체크
errorCode.ifNotEquals(0) {
    handleError(errorCode)
}
```

**지원하는 숫자 타입:**
- `ifGreaterThan`, `ifGreaterThanOrEqual`, `ifEquals`: `Int`, `Float`, `Double`, `Short`, `Long`
- `ifNotEquals`: `Int`, `Float`, `Double`, `Long` (Short 제외)

<br>
</br>

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

<br>
</br>

**4. Null 체크 (firstNotNull)**
```kotlin
// 기존 방식
val finalValue = userInput ?: cachedValue ?: defaultValue

// 여러 값 중 첫 번째 null이 아닌 값 반환
val finalValue = firstNotNull(userInput, cachedValue, defaultValue)

```

<br>
</br>

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

<br>
</br>

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

<br>
</br>

#### **TextView Extensions**
```kotlin
// 텍스트 추출 헬퍼
val text = textView.getString()  // TextView.text.toString() 단축

// EditText는 별도 메서드 사용
val inputText = editText.getTextToString()  // EditText 전용

// 비어있는지 체크 (TextView, EditText 공통)
if (textView.isTextEmpty()) {
    textView.text = "기본값"
}

// Null 또는 Empty 체크 (TextView 전용)
if (textView.isTextNullOrEmpty()) {
    hideTextView()
}

// 타입 변환 (TextView, EditText 공통)
val count = textView.textToInt() ?: 0
val ratio = textView.textToFloat() ?: 0f
val value = textView.textToDouble() ?: 0.0

// 스타일 적용 (체이닝 가능)
textView.bold()              // 굵게
textView.italic()            // 기울임
textView.boldItalic()        // 굵게 + 기울임
textView.normal()            // 스타일 제거

textView.underline()         // 밑줄
textView.removeUnderline()   // 밑줄 제거

textView.strikeThrough()     // 취소선
textView.removeStrikeThrough()  // 취소선 제거

// 색상 설정
textView.setTextColorRes(R.color.primary)

// 체이닝으로 여러 스타일 동시 적용
textView.bold().underline().setTextColorRes(R.color.red)

// 커스텀 스타일 블록
textView.style {
    bold()
    underline()
    setTextColorRes(R.color.accent)
}
```

**특징:**
- TextView와 EditText 모두에서 동작
- 체이닝 가능한 fluent API
- `style { }` 블록으로 여러 스타일 동시 적용

<br>
</br>

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

<br>
</br>

#### **View Extensions - Visibility**
```kotlin
view.setVisible()    // visibility = VISIBLE
view.setGone()       // visibility = GONE
view.setInvisible()  // visibility = INVISIBLE
```

<br>
</br>

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

<br>
</br>

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

<br>
</br>

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

<br>
</br>

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

// Lifecycle 관련 고급 기능
// 1. View의 LifecycleOwner 찾기 (ViewTree 또는 Context에서)
val lifecycleOwner = customView.findHostLifecycleOwner()

// 2. LifecycleObserver 바인딩 (자동 메모리 관리)
val observer = object : DefaultLifecycleObserver {
    override fun onResume(owner: LifecycleOwner) {
        // View가 속한 화면이 Resume될 때
        startAnimation()
    }

    override fun onPause(owner: LifecycleOwner) {
        // View가 속한 화면이 Pause될 때
        stopAnimation()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        // 자동으로 정리됨
    }
}

customView.bindLifecycleObserver(observer)

// 3. LifecycleObserver 바인딩 해제 (필요시)
customView.unbindLifecycleObserver(observer)
```

**Lifecycle 기능 특징:**
- **자동 Owner 전환**: View가 다른 화면으로 이동하면 자동으로 새 LifecycleOwner에 바인딩
- **중복 등록 방지**: 동일한 Observer를 여러 번 등록해도 안전
- **메모리 누수 방지**: View Tag 시스템으로 자동 관리
- **Fragment/Activity 감지**: ViewTree 또는 Context에서 LifecycleOwner 자동 추출

**실용 예제:**
```kotlin
// 커스텀 View에서 애니메이션 자동 관리
class AnimatedView(context: Context) : View(context) {

    private val animationObserver = object : DefaultLifecycleObserver {
        override fun onResume(owner: LifecycleOwner) {
            startAnimation()  // 화면 보일 때 시작
        }

        override fun onPause(owner: LifecycleOwner) {
            pauseAnimation()  // 화면 숨겨질 때 일시정지
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        bindLifecycleObserver(animationObserver)
    }

    override fun onDetachedFromWindow() {
        unbindLifecycleObserver(animationObserver)
        super.onDetachedFromWindow()
    }
}
```

**장점:**
- 반복 코드 대폭 감소
- 애니메이션 코드 간소화
- 체이닝 가능한 깔끔한 API
- 메모리 누수 방지 (View Tag 시스템 활용)
- Lifecycle 자동 관리로 안전한 리소스 해제

<br>
</br>

### 📝 string/ 패키지 - 문자열 검증/가공 Extensions

Simple UI는 다양한 문자열 검증 및 가공 Extensions를 제공합니다.

#### **문자열 검증 Extensions**
```kotlin
// 이메일 검증 (Android Patterns.EMAIL_ADDRESS 사용)
val email = "user@example.com"
if (email.isEmailValid()) {
    sendEmail(email)
} else {
    showError("유효하지 않은 이메일입니다")
}

// 전화번호 검증 (Android Patterns.PHONE 사용)
val phone = "+82-10-1234-5678"
if (phone.isPhoneNumberValid()) {
    makeCall(phone)
}

// URL 검증 (Android Patterns.WEB_URL 사용)
val url = "https://www.example.com"
if (url.isUrlValid()) {
    openBrowser(url)
}

// 숫자만 포함 체크 (0-9)
val code = "12345"
if (code.isNumeric()) {
    processNumericCode(code)
}

// 영문자+숫자만 포함 체크 (a-z, A-Z, 0-9)
val username = "User123"
if (username.isAlphaNumeric()) {
    createAccount(username)
}
```

#### **문자열 가공 Extensions**
```kotlin
// 모든 공백 문자 제거 (스페이스, 탭, 개행 등)
val input = "  Hello  World\n\t"
val cleaned = input.removeWhitespace()  // "HelloWorld"

// HTML 태그 제거 (기본적인 HTML 파싱)
val html = "<p>Hello <b>World</b></p>"
val plainText = html.stripHtmlTags()  // "Hello World"

// 실용 예제: 사용자 입력 정제
val userInput = " <script>alert('xss')</script> Hello "
val safe = userInput.stripHtmlTags().trim()  // " Hello "
```

**장점:**
- Android Patterns API 기반으로 검증 정확도 향상
- Precompiled Regex로 성능 최적화
- `@CheckResult` 어노테이션으로 결과 사용 강제
- 체이닝 가능한 깔끔한 API

**성능 최적화:**
```kotlin
// 내부 구현 - Precompiled Regex 패턴 재사용
private val WHITESPACE_REGEX = "\\s".toRegex()
private val HTML_TAG_REGEX = "<[^>]*>".toRegex()
private val NUMERIC_REGEX = "^[0-9]*$".toRegex()
private val ALPHANUMERIC_REGEX = "^[a-zA-Z0-9]*$".toRegex()

// 매번 Regex 객체를 생성하지 않고 재사용하여 성능 향상
```

<br>
</br>

### ⚠️ trycatch/ 패키지 - 예외 처리 Extensions

Simple UI는 안전한 예외 처리를 위한 3가지 `safeCatch` 오버로드를 제공합니다.

#### **1. safeCatch(block) - Unit 반환형**
반환값이 필요없는 단순 실행 보호
```kotlin
// 기존 방식
try {
    riskyOperation()
} catch (e: Exception) {
    e.printStackTrace()
}

// Simple UI
safeCatch {
    riskyOperation()
}

// 실용 예제: 파일 삭제
safeCatch {
    file.delete()
}

// 실용 예제: 리소스 해제
safeCatch {
    mediaPlayer.release()
}
```

#### **2. safeCatch(defaultValue, block) - 기본값 반환형**
예외 발생 시 기본값 반환
```kotlin
// 기존 방식
val result = try {
    parseJson(data)
} catch (e: Exception) {
    e.printStackTrace()
    emptyList()
}

// Simple UI
val result = safeCatch(emptyList()) {
    parseJson(data)
}

// 실용 예제: 설정 값 읽기
val timeout = safeCatch(5000) {
    preferences.getString("timeout", "5000")?.toInt()
}

// 실용 예제: 네트워크 요청
val userInfo = safeCatch(UserInfo.EMPTY) {
    apiClient.getUserInfo()
}
```

#### **3. safeCatch(block, onCatch) - 예외 핸들러 반환형**
예외 객체를 받아서 커스텀 처리 후 값 반환
```kotlin
// 기존 방식
val result = try {
    database.query(sql)
} catch (e: Exception) {
    e.printStackTrace()
    when (e) {
        is SQLException -> emptyList()
        is TimeoutException -> cachedData
        else -> emptyList()
    }
}

// Simple UI
val result = safeCatch(
    block = { database.query(sql) },
    onCatch = { e ->
        when (e) {
            is SQLException -> {
                showError("데이터베이스 오류: ${e.message}")
                emptyList()
            }
            is TimeoutException -> {
                showWarning("시간 초과, 캐시 데이터 사용")
                cachedData
            }
            else -> emptyList()
        }
    }
)

// 실용 예제: API 오류 코드별 처리
val response = safeCatch(
    block = { apiClient.fetchData() },
    onCatch = { e ->
        logError("API Error", e)
        ErrorResponse(message = e.message ?: "Unknown error")
    }
)
```

**안전성 보장:**
```kotlin
// 모든 safeCatch는 다음 예외를 자동으로 재전파합니다:
// 1. CancellationException - 코루틴 취소는 반드시 전파
// 2. Error (OutOfMemoryError 등) - 시스템 에러는 절대 삼키지 않음

// 내부 구현 예시:
public inline fun <T> safeCatch(defaultValue: T, block: () -> T): T {
    return try {
        block()
    } catch (e: CancellationException) {
        throw e  // 코루틴 취소는 반드시 전파
    } catch (e: Error) {
        throw e  // OOM 등은 절대 삼키지 않음
    } catch (e: Exception) {
        e.printStackTrace()
        defaultValue
    }
}
```

**장점:**
- try-catch 보일러플레이트 제거
- 코루틴 취소 및 시스템 에러 안전하게 처리
- 자동 예외 로깅 (printStackTrace)
- 3가지 오버로드로 모든 상황 커버
- 간결하고 읽기 쉬운 코드

**비교표:**
| 오버로드 | 사용 시기 | 예외 시 동작 |
|---------|----------|------------|
| `safeCatch(block)` | 반환값 불필요 | 예외 무시 (로깅만) |
| `safeCatch(defaultValue, block)` | 기본값 반환 필요 | defaultValue 반환 |
| `safeCatch(block, onCatch)` | 예외별 처리 필요 | onCatch 람다 실행 |

<br>
</br>

.
