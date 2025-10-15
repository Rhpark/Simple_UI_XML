# 📦 Simple UI Extensions & Style vs Pure Android - Complete Comparison Guide

![extensions_style_example.gif](example%2Fextensions_style_example.gif)

> **"More Features with Less Code!"** See the immediate difference Simple UI Extensions makes compared to pure Android development.

<br>
</br>

## 🔎 At a Glance

<br>
</br>

### Extensions Comparison
| Feature | Pure Android | Simple UI | Impact |
|:--|:--|:--|:--|
| 🍞 **Toast Display** | `Toast.makeText(this, "msg", LENGTH_SHORT).show()` | `toastShowShort("msg")` | **60% shorter** |
| 🎨 **TextView Styling** | `setTypeface()` + `paintFlags` manipulation | `bold().underline()` | **Chainable** |
| 📏 **Unit Conversion** | `TypedValue.applyDimension(...)` | `16.dpToPx(this)` | **82% shorter** |
| ✉️ **String Validation** | `Patterns.EMAIL_ADDRESS.matcher().matches()` | `email.isEmailValid()` | **Intuitive** |
| 🔢 **Number Rounding** | `Math.round(x * 100.0) / 100.0` | `x.roundTo(2)` | **Concise** |
| 🎯 **Conditional Execution** | `if (Build.VERSION.SDK_INT >= S) { }` | `checkSdkVersion(S) { }` | **55% shorter** |
| 🎬 **View Animation** | ValueAnimator + Listener (15~20 lines) | `view.fadeIn()` | **1 line** |
| 🚫 **Prevent Double Click** | lastClickTime variable + if statement (8 lines) | `setOnDebouncedClickListener { }` | **Automatic** |
| 📦 **Bundle Access** | `getInt()`, `getString()` type-specific calls | `getValue<T>("key", default)` | **Type-safe** |
| 🎨 **ImageView Effects** | ColorMatrix + ColorMatrixColorFilter setup | `imageView.makeGrayscale()` | **Instant** |

> **Key takeaway:** Simple UI solves "repetitive code" with **extension functions**. Your development speed will transform.

<br>
</br>

## 💡 Why Simple UI Extensions are Essential

### 🚀 **Immediate Productivity Boost**
- **Typing time saved**: Shortening long method calls
  - Example: `Toast.makeText(this, "text", Toast.LENGTH_SHORT).show()` (56 chars) → `toastShowShort("text")` (22 chars)
- **Automated SDK version branching**: Simplifying Build.VERSION.SDK_INT checks into functions
- **Double-click bug elimination**: Automatic prevention without manual timing checks

<br>
</br>

### 🛡️ **Safe and Robust Code**
- **Compile-time type checking**: Prevent runtime errors with Bundle.getValue<T>
- **Null safety**: Safe default value chains with firstNotNull()
- **Simplified exception handling**: Automatic logging and default values with safeCatch()
- **Unified permission handling**: Solve both normal/special permissions with one hasPermission()

<br>
</br>

### 🎨 **Intuitive and Readable Code**
- **Method chaining**: `textView.bold().underline().italic()` - Clear intent
- **Natural extensions**: `3.14159.roundTo(2)` - Reads like a number
- **Conditional chaining**: `list.ifNotEmpty { }.ifEmpty { }` - Functional style
- **Animation DSL**: `view.fadeIn()`, `view.shake()` - Self-explanatory

<br>
</br>

## 📦 Complete Comparison: Extensions & Style vs Pure Android

### 📂 **Available Extension Packages** (Organized by Package)

#### **🎨 view/** - UI Manipulation Extensions
- **Toast/SnackBar**: Simple message display
- **TextView**: bold(), underline(), italic() chaining
- **EditText**: getTextToString(), textToInt(), isTextEmpty()
- **ImageView**: setTint(), makeGrayscale(), centerCrop(), fadeIn()
- **View animations**: fadeIn/Out(), shake(), pulse(), rotate(), slideIn/Out()
- **View manipulation**: setVisible/Gone(), setMargins(), setOnDebouncedClickListener()

<br>
</br>

#### **📏 display/** - Unit Conversion Extensions
- **dp↔px conversion**: 16.dpToPx(), 48.pxToDp()
- **sp↔px conversion**: 14.spToPx(), 42.pxToSp()
- **Instant use**: `view.setWidth(100.dpToPx(this))`

<br>
</br>

#### **🔢 round_to/** - Number Rounding Extensions
- **Decimal rounding**: 3.14159.roundTo(2) → 3.14
- **Round up/down**: price.roundUp(2), price.roundDown(2)
- **Integer rounding**: 1234.roundTo(2) → 1200

<br>
</br>

#### **🎯 conditional/** - Conditional Execution Extensions
- **SDK check**: checkSdkVersion(S) { ... }
- **Number comparison**: score.ifGreaterThan(80) { ... }
- **Boolean**: isLoggedIn.ifTrue { ... }.ifFalse { ... }
- **Collection**: list.ifNotEmpty { }.filterIf(condition) { }

<br>
</br>

#### **📦 bundle/** - Bundle Type-Safe Extensions
- **Type-safe access**: bundle.getValue<Int>("id", 0)
- **Automatic type inference**: Compile-time check with Reified Type

<br>
</br>

#### **📝 string/** - String Validation/Processing Extensions
- **Email validation**: email.isEmailValid()
- **Numeric check**: text.isNumeric()
- **Whitespace removal**: text.removeWhitespace()

<br>
</br>

#### **📅 date/** - Date Format Extensions
- **Long → String**: timestamp.timeDateToString("yyyy-MM-dd")

<br>
</br>

#### **⚠️ trycatch/** - Exception Handling Extensions
- **Safe execution**: safeCatch(defaultValue) { ... }
- **Automatic logging**: Auto-record with Logx on exceptions

<br>
</br>

#### **🔐 permissions/** - Permission Check Extensions
- **Unified permission check**: hasPermission(Manifest.permission.CAMERA)
- **Supports both normal/special permissions**

<br>
</br>

#### **🎨 resource/** - Resource Access Extensions
- **Safe access**: getDrawableCompat(R.drawable.icon)
- **Automatic version handling**: Auto-handles SDK version branches

<br>
</br>

## 📝 Code Comparison

### First: Toast/SnackBar Display Comparison

<details>
<summary><strong>Pure Android - Builder Pattern</strong></summary>

```kotlin
class MainActivity : AppCompatActivity() {

    // Toast - Repetitive Builder pattern
    private fun showToastShort(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showToastLong(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    // SnackBar - Complex setup
    private fun showSnackBar(message: String) {
        val snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT)
        snackbar.show()
    }

    // SnackBar with Action - Even more complex
    private fun showSnackBarWithAction(message: String) {
        val snackbar = Snackbar.make(
            findViewById(android.R.id.content),
            message,
            Snackbar.LENGTH_SHORT
        )
        snackbar.setAction("OK") {
            Toast.makeText(this, "Action executed!", Toast.LENGTH_SHORT).show()
        }
        snackbar.setActionTextColor(ContextCompat.getColor(this, android.R.color.holo_blue_light))
        snackbar.show()
    }

    // Usage
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.btnToast.setOnClickListener {
            showToastShort("Toast message")
        }

        binding.btnSnackBar.setOnClickListener {
            showSnackBarWithAction("SnackBar message")
        }
    }
}
```
**Problems:** Repetitive Builder pattern, complex SnackBar setup, lengthy code every time
</details>

<details>
<summary><strong>Simple UI - One-liner Extensions</strong></summary>

```kotlin
class MainActivity : BaseBindingActivity<ActivityMainBinding>(R.layout.activity_main) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Toast - One line!
        binding.btnToastShort.setOnClickListener {
            toastShowShort("Toast Short display")
        }

        binding.btnToastLong.setOnClickListener {
            toastShowLong("Toast Long display - Shows for a bit longer")
        }

        // SnackBar - One line!
        binding.btnSnackBarShort.setOnClickListener {
            binding.root.snackBarShowShort("SnackBar display!")
        }

        // SnackBar with Action - Simple with Option!
        binding.btnSnackBarAction.setOnClickListener {
            binding.root.snackBarShowShort(
                "SnackBar with action button",
                SnackBarOption(
                    actionText = "OK",
                    action = { toastShowShort("Action executed!") }
                )
            )
        }
    }
}
```
**Result:** One line, clear setup with SnackBarOption, intuitive method names!
</details>

<br>
</br>

### Second: TextView Styling Comparison

<details>
<summary><strong>Pure Android - Direct Paint/Typeface Setup</strong></summary>

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

    // Usage
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
**Problems:** Complex Paint/Typeface manipulation, need method for each feature, no chaining
</details>

<details>
<summary><strong>Simple UI - Extensions Chaining</strong></summary>

```kotlin
class MainActivity : BaseBindingActivity<ActivityMainBinding>(R.layout.activity_main) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TextView styling - One line!
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

        // Chaining possible!
        binding.btnResetStyle.setOnClickListener {
            binding.tvSampleText.normal()
                .removeUnderline()
                .removeStrikeThrough()
        }

        // Chaining example_gif!
        binding.tvSampleText.bold().underline()
    }
}
```
**Result:** One-line method calls, chainable, intuitive examples!
</details>

<br>
</br>

### Third: Unit Conversion (dp ↔ px, sp ↔ px) Comparison

<details>
<summary><strong>Pure Android - Repetitive TypedValue Coding</strong></summary>

```kotlin
class MainActivity : AppCompatActivity() {

    // DP to PX conversion
    private fun dpToPx(dp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            resources.displayMetrics
        )
    }

    // PX to DP conversion
    private fun pxToDp(px: Float): Float {
        return px / resources.displayMetrics.density
    }

    // SP to PX conversion
    private fun spToPx(sp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            sp,
            resources.displayMetrics
        )
    }

    // PX to SP conversion
    private fun pxToSp(px: Float): Float {
        return px / resources.displayMetrics.density / resources.configuration.fontScale
    }

    // Usage
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.btnConvert.setOnClickListener {
            val value = binding.edtValue.text.toString().toFloatOrNull() ?: 0f
            val result = dpToPx(value)
            binding.tvResult.text = "Result: ${value}dp = ${result}px"
        }
    }
}
```
**Problems:** Repetitive TypedValue API, need method for each conversion, displayMetrics access
</details>

<details>
<summary><strong>Simple UI - One-liner Extensions</strong></summary>

```kotlin
class MainActivity : BaseBindingActivity<ActivityMainBinding>(R.layout.activity_main) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // DP to PX - One line!
        binding.btnDpToPx.setOnClickListener {
            val value = binding.edtDisplayValue.text.toString().toFloatOrNull() ?: 0f
            val result = value.dpToPx(this)
            binding.tvDisplayResult.text = "Result: ${value}dp = ${result}px"
        }

        // PX to DP - One line!
        binding.btnPxToDp.setOnClickListener {
            val value = binding.edtDisplayValue.text.toString().toFloatOrNull() ?: 0f
            val result = value.pxToDp(this)
            binding.tvDisplayResult.text = "Result: ${value}px = ${result}dp"
        }

        // SP to PX - One line!
        binding.btnSpToPx.setOnClickListener {
            val value = binding.edtDisplayValue.text.toString().toFloatOrNull() ?: 0f
            val result = value.spToPx(this)
            binding.tvDisplayResult.text = "Result: ${value}sp = ${result}px"
        }
    }
}
```
**Result:** Intuitive with Number Extension, one-line conversion done!
</details>

<br>
</br>

### Fourth: String Validation (Email/Number) Comparison

<details>
<summary><strong>Pure Android - Manual Patterns Matching</strong></summary>

```kotlin
class MainActivity : AppCompatActivity() {

    // Email validation
    private fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Numeric check
    private fun isNumeric(text: String): Boolean {
        return text.matches("^[0-9]*$".toRegex())
    }

    // Whitespace removal
    private fun removeWhitespace(text: String): String {
        return text.replace("\\s".toRegex(), "")
    }

    // Real-time validation
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.edtEmail.addTextChangedListener {
            val email = it.toString()
            if (email.isEmpty()) {
                binding.tvEmailResult.text = "Please enter email"
            } else if (isEmailValid(email)) {
                binding.tvEmailResult.text = "✅ Valid email"
                binding.tvEmailResult.setTextColor(Color.GREEN)
            } else {
                binding.tvEmailResult.text = "❌ Invalid email"
                binding.tvEmailResult.setTextColor(Color.RED)
            }
        }

        binding.edtNumber.addTextChangedListener {
            val number = it.toString()
            if (isNumeric(number)) {
                binding.tvNumberResult.text = "✅ Is numeric"
            } else {
                binding.tvNumberResult.text = "❌ Not numeric"
            }
        }
    }
}
```
**Problems:** Need to write methods every time, difficult Regex patterns, repetitive validation logic
</details>

<details>
<summary><strong>Simple UI - One-liner Extensions</strong></summary>

```kotlin
class MainActivity : BaseBindingActivity<ActivityMainBinding>(R.layout.activity_main) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Real-time email validation - isEmailValid() one line!
        binding.edtEmail.addTextChangedListener {
            val email = it.toString()
            if (email.isEmpty()) {
                binding.tvEmailResult.text = "Please enter email"
            } else if (email.isEmailValid()) {
                binding.tvEmailResult.text = "✅ Valid email"
                binding.tvEmailResult.setTextColor(Color.GREEN)
            } else {
                binding.tvEmailResult.text = "❌ Invalid email"
                binding.tvEmailResult.setTextColor(Color.RED)
            }
        }

        // Real-time numeric validation - isNumeric() one line!
        binding.edtNumber.addTextChangedListener {
            val number = it.toString()
            if (number.isEmpty()) {
                binding.tvNumberResult.text = "Please enter number"
            } else if (number.isNumeric()) {
                binding.tvNumberResult.text = "✅ Is numeric"
                binding.tvNumberResult.setTextColor(Color.GREEN)
            } else {
                binding.tvNumberResult.text = "❌ Not numeric"
                binding.tvNumberResult.setTextColor(Color.RED)
            }
        }

        // Whitespace removal - removeWhitespace() one line!
        binding.btnRemoveWhitespace.setOnClickListener {
            val original = binding.edtWhitespace.text.toString()
            val removed = original.removeWhitespace()
            binding.tvWhitespaceResult.text = "Original: \"$original\"\nResult: \"$removed\""
        }
    }
}
```
**Result:** Intuitive with String Extension, compile-time safe, one-line validation done!
</details>

<br>
</br>

## 🎯 Key Advantages of Simple UI Extensions & Style

### 1. 📝 **Dramatic Code Reduction** - Real Typing Comparison

#### **Code amount comparison for repetitive tasks**

| Feature | Pure Android | Simple UI | Improvement |
|:--|:--|:--|:--|
| **Toast display** | `Toast.makeText(this, "msg", Toast.LENGTH_SHORT).show()` <br>(56 chars) | `toastShowShort("msg")` <br>(22 chars) | **60% reduction** |
| **SnackBar + Action** | Snackbar.make() + setAction() + setActionTextColor() + show() <br>(~7 lines, 180+ chars) | `snackBarShowShort("msg", SnackBarOption(...))` <br>(~4 lines, 90 chars) | **Cut in half** |
| **TextView styling** | `textView.setTypeface(..., Typeface.BOLD)` <br>`textView.paintFlags = ... or Paint.UNDERLINE_TEXT_FLAG` <br>(2 lines, 112 chars) | `textView.bold().underline()` <br>(1 line, 28 chars) | **75% reduction** |
| **Prevent double-click** | lastClickTime variable + if check <br>(8 lines) | `setOnDebouncedClickListener { }` <br>(1 line) | **Complete removal** |
| **Unit conversion** | `TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)` <br>(90 chars) | `dp.dpToPx(this)` <br>(16 chars) | **82% reduction** |
| **SDK version branch** | `if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { }` <br>(54 chars) | `checkSdkVersion(S) { }` <br>(24 chars) | **55% reduction** |

<br>
</br>

#### **Write once, use everywhere (No utility functions needed)**

| Feature | Pure Android | Simple UI |
|:--|:--|:--|
| **Email validation** | Need to write Patterns.EMAIL_ADDRESS.matcher(email).matches() method | `email.isEmailValid()` - Ready to use |
| **Number rounding** | Write Math.round() calculation each time | `price.roundTo(2)` - Ready to use |
| **View animation** | ValueAnimator + Listener implementation (15~20 lines) | `view.fadeIn()` - Ready to use |

<br>
</br>

**💡 Actual results**:
- **Average 55~82% reduction in typing**
- **No need to write repetitive utility methods** (provided by library)
- **Dramatically improved readability** (long method chains → short, clear names)

<br>
</br>

### 2. 🛡️ **Type Safety** - Catch Errors at Compile Time

#### **Move runtime errors to compile time**
```kotlin
// ❌ Pure Android - Crashes at runtime
val value = bundle.getInt("age")  // Key typo → returns 0 (bug!)
val name = bundle.getString("name")  // Can return null

// ✅ Simple UI - Compile-time check + default value
val age = bundle.getValue("age", 0)  // Reified Type
val name = bundle.getValue("name", "Unknown")  // Null safe
```

<br>
</br>

#### **Prevent mistakes with type inference**
```kotlin
// ❌ Pure Android
val price = 3.14159
val rounded = Math.round(price * 100.0) / 100.0  // Complex!

// ✅ Simple UI
val rounded = price.roundTo(2)  // Automatic type inference
```

<br>
</br>

### 3. 🎨 **Intuitive API** - Code as Documentation

#### **Code that reads like natural language**
```kotlin
// Conditional execution
score.ifGreaterThan(80) { showCongratulations() }
isLoggedIn.ifTrue { navigateToMain() }
list.ifNotEmpty { adapter.update(it) }

// Number rounding
price.roundTo(2)      // "Round price to 2 decimal places"
count.roundUp(2)      // "Round count up to hundreds"

// View animations
errorField.shake()    // "Shake the error field"
heartIcon.pulse()     // "Pulse the heart icon"
panel.slideIn(RIGHT)  // "Slide panel in from right"
```

<br>
</br>

#### **Clear intent through chaining**
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

### 4. 📦 **Organized Package Structure** - Easy to Find, Easy to Learn

```
kr.open.library.simple_ui.extensions/
├─ view/           → UI manipulation (Toast, TextView, ImageView, animations)
├─ display/        → Unit conversion (dp↔px, sp↔px)
├─ round_to/       → Number rounding (roundTo, roundUp, roundDown)
├─ conditional/    → Conditional execution (SDK check, ifTrue, ifGreaterThan)
├─ bundle/         → Bundle type-safe access
├─ string/         → String validation (isEmailValid, isNumeric)
├─ date/           → Date formatting
├─ trycatch/       → Exception handling (safeCatch)
├─ permissions/    → Unified permission checking
└─ resource/       → Safe resource access
```

**💡 Find what you need instantly by package name!**

<br>
</br>

### 6. ⚡ **Production-Ready Features**

#### **Solve everyday problems**
```kotlin
// 🚫 Prevent double-click screen navigation bugs
button.setOnDebouncedClickListener { navigateToDetail() }

// 🎬 Loading indicator fade in/out
loadingView.fadeIn()
loadingView.fadeOut { it.setGone() }

// 📧 Real-time email validation
if (email.isEmailValid()) { enableSubmit() }

// 🔢 Price display (2 decimal places)
priceText.text = "${actualPrice.roundTo(2)} USD"

// 🎯 SDK version branching
checkSdkVersion(Build.VERSION_CODES.S) {
    // Android 12 exclusive features
}

// 📦 Safely get Intent data
val userId = intent.extras?.getValue("user_id", -1) ?: -1
```

<br>
</br>

## 📣 Real User Testimonials

> 💬 **"I was so tired of checking lastClickTime for double-click bugs. Now it's just one setOnDebouncedClickListener(). Amazing!"**

> 💬 **"TextView styling chaining is a killer feature. bold().underline() makes code so readable!"**

> 💬 **"3.14159.roundTo(2) - This is Kotlin at its best! No more Math.round() mistakes"**

> 💬 **"Hated typing if (Build.VERSION.SDK_INT >= ...) every time. checkSdkVersion() cleaned everything up"**

> 💬 **"One-line animations like fadeIn(), shake(), pulse() made UX improvements lightning fast!"**

> 💬 **"Bundle.getValue<T>() type safety definitely reduced runtime bugs"**

<br>
</br>

## 🎯 Conclusion: Essential Tool for Android Developers

**Simple UI Extensions** was created to solve the **repetition and inconvenience** of pure Android development.

✅ **Extensions comparison** - Simplify repetitive code!
✅ **Organized structure** - Clear roles by package!
✅ **Type safety** - Prevent runtime errors at compile time!

**Want to boost your development speed?**
**Try Simple UI!** 🚀

<br>
</br>

## 📂 Check Out the Actual Code

**Example files:**
> - 🎯 Activity: `package kr.open.library.simpleui_xml.extenstions_style/ExtensionsStyleActivity`
> - ⚡ Run it yourself to feel the difference!

<br>
</br>

**Implemented example features:**
- 📦 view/ - Toast, SnackBar, TextView styling (bold, italic, underline, strikethrough)
- 📦 display/ - Unit conversion (dp↔px, sp↔px)
- 📦 resource/ - Type-safe Drawable, Color access
- 📦 string/ - Real-time email/number validation, whitespace removal
- 📦 date/ - Various date formats
- 📦 trycatch/ - safeCatch exception handling
- 📦 permissions/ - CAMERA permission check

<br>
</br>

## 📦 Additional Extensions (Not in Example Code, Built into Library)

These powerful extensions are provided by Simple UI library but not included in the example Activity above.

<br>
</br>

### 🔢 round_to/ - Number Rounding Extensions

**Features:**
- `roundTo(decimals)` - Round to specified decimal places
- `roundUp(decimals)` - Round up to specified places
- `roundDown(decimals)` - Round down to specified places

<br>
</br>

**Supported types:** Double, Float, Int, Long, Short

<br>
</br>

**Usage examples:**
```kotlin
// Decimal rounding
val pi = 3.14159
val rounded = pi.roundTo(2)  // 3.14

// Round up/down
val price = 3.14159
val up = price.roundUp(2)    // 3.15
val down = price.roundDown(2) // 3.14

// Integer rounding (by digits)
val number = 1234
val rounded = number.roundTo(2)  // 1200
val up = number.roundUp(2)       // 1300
```

**Benefits:**
- Eliminates repetitive Math.round/ceil/floor code
- Consistent API across all number types
- Same method names for decimals/integers

<br>
</br>

### 🔀 conditional/ - Conditional Execution Extensions

**Features:**

**1. SDK Version Check (checkSdkVersion)**
```kotlin
// Traditional approach
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    // Android 12+ code
}

// Simple UI
checkSdkVersion(Build.VERSION_CODES.S) {
    // Android 12+ code
}

// With branching
val result = checkSdkVersion(Build.VERSION_CODES.S,
    positiveWork = { "Android 12 or higher" },
    negativeWork = { "Below Android 12" }
)
```

<br>
</br>

**2. Number Conditional Execution (ifGreaterThan, ifEquals)**
```kotlin
// Traditional approach
val score = 85
if (score > 80) {
    showCongratulations()
}

// Simple UI
score.ifGreaterThan(80) {
    showCongratulations()
}

// With branching
val grade = score.ifGreaterThan(80,
    positiveWork = { "A" },
    negativeWork = { "B" }
)

// Value equality check
value.ifEquals(100) {
    showPerfectScore()
}
```

<br>
</br>

**3. Boolean Conditional Execution (ifTrue, ifFalse)**
```kotlin
// Traditional approach
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

// Single condition
isNetworkAvailable.ifTrue {
    syncData()
}
```

<br>
</br>

**4. Null Check (firstNotNull)**
```kotlin
// Traditional approach
val finalValue = userInput ?: cachedValue ?: defaultValue

// Return first non-null value from multiple values
val finalValue = firstNotNull(userInput, cachedValue, defaultValue)

```

<br>
</br>

**5. Collection Conditionals (filterIf, ifNotEmpty)**
```kotlin
// Conditional filtering
val results = products.filterIf(showOnSale) { it.isOnSale }

// Execute only when not empty (chainable)
notifications
    .ifNotEmpty { updateBadgeCount(it.size) }
    .ifEmpty { hideNotificationIcon() }

searchResults
    .ifEmpty { showNoResultsMessage() }
    .ifNotEmpty { hideNoResultsMessage() }
```

<br>
</br>

**Benefits:**
- Improved readability by eliminating nested if statements
- Functional programming style
- Chainable API
- Simplified SDK version branching

<br>
</br>

### 📦 bundle/ - Bundle Type-Safe Access

**Features:**
- `getValue<T>(key, defaultValue)` - Type-safe Bundle value extraction

**Usage examples:**
```kotlin
// Traditional approach
val userId = bundle.getInt("user_id", -1)
val userName = bundle.getString("user_name", "")
val isActive = bundle.getBoolean("is_active", false)

// Simple UI - Automatic type inference with Reified Type
val userId = bundle.getValue("user_id", -1)
val userName = bundle.getValue("user_name", "")
val isActive = bundle.getValue("is_active", false)

// Supported types: Int, Boolean, Float, Long, Double, String, Char, Short, Byte, ByteArray, Bundle
```

**Benefits:**
- No need for type-specific method calls
- Compile-time type checking with Reified Type
- Automatic defaultValue return and logging on missing keys
- Improved code consistency

<br>
</br>

### 🎨 view/ Package - Additional View Extensions

While the example code only includes Toast/SnackBar/TextView, many more extensions are actually provided.

#### **EditText Extensions**
```kotlin
// Text extraction
val text = editText.getTextToString()  // Shortcut for text.toString()

// Check if empty
if (editText.isTextEmpty()) {
    showError("Please enter a value")
}

// Type conversion
val age = editText.textToInt() ?: 0
val price = editText.textToFloat() ?: 0f
val distance = editText.textToDouble() ?: 0.0
```

<br>
</br>

#### **ImageView Extensions**
```kotlin
// Image setting
imageView.setImageDrawableRes(R.drawable.icon)

// Tint setting
imageView.setTint(R.color.primary)
imageView.clearTint()

// Grayscale effect
imageView.makeGrayscale()  // Convert to black & white
imageView.removeGrayscale()  // Restore original

// ScaleType shortcuts
imageView.centerCrop()
imageView.centerInside()
imageView.fitCenter()
imageView.fitXY()

// Chaining
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

#### **View Extensions - Prevent Double-Click**
```kotlin
// Traditional approach - Manual timing check
private var lastClickTime = 0L
button.setOnClickListener {
    val currentTime = System.currentTimeMillis()
    if (currentTime - lastClickTime >= 600) {
        lastClickTime = currentTime
        // Actual logic
    }
}

// Simple UI - Debounced Click
button.setOnDebouncedClickListener(600L) { view ->
    // Automatically prevents double-clicks within 600ms
    navigateToNextScreen()
}
```

<br>
</br>

#### **View Extensions - Layout Manipulation**
```kotlin
// Margin setting
view.setMargins(16, 8, 16, 8)  // left, top, right, bottom
view.setMargin(16)              // Same for all sides

// Padding setting
view.setPadding(12)  // Same for all sides

// Width/Height setting
view.setWidth(200)
view.setHeight(100)
view.setSize(200, 100)

// Match/Wrap setting
view.setWidthMatchParent()
view.setHeightWrapContent()
```

<br>
</br>

#### **View Extensions - Animations**
```kotlin
// Fade animations
view.fadeIn(duration = 300L) {
    // Completion callback
}
view.fadeOut(duration = 300L, hideOnComplete = true)
view.fadeToggle()  // Toggle

// Scale animation
button.animateScale(fromScale = 1f, toScale = 1.2f, duration = 150L)

// Pulse effect
heartIcon.pulse(minScale = 0.9f, maxScale = 1.1f, duration = 800L)
heartIcon.stopPulse()

// Slide animation
panel.slideIn(SlideDirection.RIGHT, duration = 250L)
panel.slideOut(SlideDirection.LEFT, hideOnComplete = true)

// Shake effect (error feedback)
errorField.shake(intensity = 15f) {
    // Shake complete
}

// Rotate animation
arrowIcon.rotate(toDegrees = 180f, duration = 200L)
```

<br>
</br>

#### **View Extensions - Advanced Features**
```kotlin
// Execute after layout complete
customView.doOnLayout { view ->
    val width = view.width
    val height = view.height
    // Work with actual dimensions
}

// Get screen coordinates
val (x, y) = button.getLocationOnScreen()

// Apply Window Insets as Padding
rootView.applyWindowInsetsAsPadding(
    left = true,
    top = true,
    right = true,
    bottom = true
)

// Iterate ViewGroup children
viewGroup.forEachChild { child ->
    // Work with each child View
}
```

**Benefits:**
- Massive reduction in repetitive code
- Simplified animation code
- Clean, chainable API
- Memory leak prevention (utilizing View Tag system)

<br>
</br>

.
