# SDK 버전 분기 패턴 규칙

## 규칙

- checkSdkVersion 패턴 사용
- 직접 if (Build.VERSION.SDK_INT >= X) 금지

## 심각도 기준

- HIGH: SDK 분기 직접 if 사용

## 예시

❌ BAD
```kotlin
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    requestPermissions(
        permissions = listOf(Manifest.permission.POST_NOTIFICATIONS),
        onDeniedResult = { deniedResults ->
            if (deniedResults.isEmpty()) showNotification()
        },
    )
}
```

✅ GOOD
```kotlin
checkSdkVersion(Build.VERSION_CODES.TIRAMISU) {
    requestPermissions(
        permissions = listOf(Manifest.permission.POST_NOTIFICATIONS),
        onDeniedResult = { deniedResults ->
            if (deniedResults.isEmpty()) showNotification()
        },
    )
}
```
