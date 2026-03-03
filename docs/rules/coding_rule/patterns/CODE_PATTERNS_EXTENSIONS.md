# 확장 함수 패턴 규칙

## 규칙

- 라이브러리가 제공하는 확장 함수가 있으면 직접 구현 금지
- Toast/SnackBar 직접 호출 금지 → 확장 함수 사용
- dp/px/sp 단위 변환 수동 계산 금지 → `dpToPx()`, `pxToDp()` 등 사용
- 중복 클릭 방지 수동 구현 금지 → `setOnDebouncedClickListener()` 사용

## 카테고리별 사용 기준

| 상황 | 직접 작성 (금지) | 확장 함수 사용 |
|------|--------------|-------------|
| Toast 표시 | `Toast.makeText(this, "msg", LENGTH_SHORT).show()` | `toastShowShort("msg")` |
| SnackBar 표시 | `Snackbar.make(view, "msg", LENGTH_SHORT).show()` | `view.snackBarShowShort("msg")` |
| dp→px 변환 | `TypedValue.applyDimension(COMPLEX_UNIT_DIP, dp, metrics)` | `dp.dpToPx(context)` |
| String 이메일 검증 | `Patterns.EMAIL_ADDRESS.matcher(email).matches()` | `email.isEmailValid()` |
| 소수점 반올림 | `Math.round(x * 100.0) / 100.0` | `x.roundTo(2)` |
| 뷰 애니메이션 | `ValueAnimator` + listener (15~20줄) | `view.fadeIn()` |
| 중복 클릭 방지 | `lastClickTime` 변수 + if 체크 (8줄) | `view.setOnDebouncedClickListener { }` |
| Bundle 값 접근 | `bundle.getInt("key", default)` 타입별 호출 | `bundle.getValue<Int>("key", default)` |
| SDK 버전 분기 | `if (Build.VERSION.SDK_INT >= S) { }` | `checkSdkVersion(S) { }` |

## 심각도 기준

- MEDIUM: Toast/SnackBar 직접 호출
- MEDIUM: dp/px 단위 변환 수동 계산
- MEDIUM: 중복 클릭 방지 수동 구현

## 예시

### Toast / SnackBar

❌ BAD
```kotlin
Toast.makeText(this, "저장 완료", Toast.LENGTH_SHORT).show()
Snackbar.make(binding.root, "오류 발생", Snackbar.LENGTH_SHORT).show()
```

✅ GOOD
```kotlin
toastShowShort("저장 완료")
binding.root.snackBarShowShort("오류 발생")
```

---

### 단위 변환

❌ BAD
```kotlin
val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, resources.displayMetrics).toInt()
```

✅ GOOD
```kotlin
val px = 16.dpToPx(this)
```

---

### 중복 클릭 방지

❌ BAD
```kotlin
var lastClickTime = 0L
binding.btnSubmit.setOnClickListener {
    val now = System.currentTimeMillis()
    if (now - lastClickTime > 500) {
        lastClickTime = now
        submitForm()
    }
}
```

✅ GOOD
```kotlin
binding.btnSubmit.setOnDebouncedClickListener {
    submitForm()
}
```

---

### TextView 스타일

❌ BAD
```kotlin
binding.tvTitle.setTypeface(binding.tvTitle.typeface, Typeface.BOLD)
binding.tvTitle.paintFlags = binding.tvTitle.paintFlags or Paint.UNDERLINE_TEXT_FLAG
```

✅ GOOD
```kotlin
binding.tvTitle.bold().underline()
```

---

### String 검증

❌ BAD
```kotlin
if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) { ... }
```

✅ GOOD
```kotlin
if (email.isEmailValid()) { ... }
```

---

### Boolean 조건 실행

❌ BAD
```kotlin
if (isLoggedIn) { showDashboard() } else { showLogin() }
```

✅ GOOD
```kotlin
isLoggedIn.ifTrue { showDashboard() }.ifFalse { showLogin() }
```
