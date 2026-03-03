# 로깅 패턴 규칙

## 규칙

- Log.d, Log.e, println 등 Android 기본 로그 API 사용 금지 → Logx 사용
- Application.onCreate()에서 `Logx.initialize(applicationContext)` 초기화 필수

## Logx API 사용 기준

| 상황 | 사용 API |
|------|---------|
| 일반 디버그 로그 | `Logx.d(msg)` / `Logx.d(tag, msg)` |
| 정보 로그 | `Logx.i(msg)` / `Logx.i(tag, msg)` |
| JSON 데이터 출력 | `Logx.j(tag, jsonData)` |
| 호출자(Parent) 추적 | `Logx.p(msg)` |
| Thread 정보 추적 | `Logx.t(msg)` |
| 확장 함수 사용 | `"msg".logd()` |

## 파일 저장 사용 시

- 권장 storage: `APP_EXTERNAL` (권한 불필요, 파일 관리자 접근 가능)
- `PUBLIC_EXTERNAL`은 Android 9 이하에서 권한 필요 → `checkSdkVersion` 패턴 사용

| 저장소 타입 | 권한 필요 | 사용자 접근 |
|------------|---------|-----------|
| INTERNAL | 불필요 | 불가 |
| APP_EXTERNAL (권장) | 불필요 | 가능 |
| PUBLIC_EXTERNAL | Android 9 이하만 필요 | 가능 |

## 심각도 기준

- HIGH: Log.d, println 등 기본 API 사용
- HIGH: Logx.initialize() 미호출

## 예시

### 기본 로깅

❌ BAD
```kotlin
Log.d("Login", "login start")
println("debug login")
```

✅ GOOD
```kotlin
Logx.d("Login start")
Logx.d("Login", "Login start")
```

---

### JSON 로깅

❌ BAD
```kotlin
val jsonObject = JSONObject(jsonData)
val prettyJson = jsonObject.toString(2)
for (line in prettyJson.split("\n")) {
    Log.d("JSON_LOG", line)
}
```

✅ GOOD
```kotlin
Logx.j("JSON_TAG", jsonData)
```

---

### 호출자 추적

❌ BAD
```kotlin
val stackTrace = Thread.currentThread().stackTrace
val currentMethod = stackTrace[2]
Log.d("STACK", "${currentMethod.fileName}:${currentMethod.lineNumber}")
```

✅ GOOD
```kotlin
Logx.p("어떤 함수에서 호출되었는지 추적")
```

---

### 파일 저장 초기화

```kotlin
// Application.onCreate()
Logx.initialize(applicationContext)
Logx.setSaveEnabled(true)
Logx.setStorageType(LogStorageType.APP_EXTERNAL) // 권장
```

---

### PUBLIC_EXTERNAL 사용 시 권한 처리

```kotlin
checkSdkVersion(Build.VERSION_CODES.P,
    positiveWork = {
        // Android 10+ 권한 불필요
        setupLogxWithPublicStorage()
    },
    negativeWork = {
        requestPermissions(
            permissions = listOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            onDeniedResult = { deniedResults ->
                if (deniedResults.isEmpty()) setupLogxWithPublicStorage()
                else setupLogxWithAppExternalStorage() // 권한 거부 시 APP_EXTERNAL로 대체
            }
        )
    }
)
```
