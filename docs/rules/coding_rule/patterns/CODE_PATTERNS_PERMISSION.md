# 권한 요청 패턴 규칙

## 규칙

- `registerForActivityResult` 직접 사용 금지 → `requestPermissions()` 사용
- 단일 권한도 `requestPermissions(permissions = listOf(...))` 형태로 통일
- `onDeniedResult`에서 `deniedResults.isEmpty()` 확인 후 작업 수행
- SystemManager 사용 시, 권한 허용 직후 해당 인스턴스의 `refreshPermissions()` 호출 필수 (예: `locationInfo.refreshPermissions()`)

## 권한 타입별 요청 방식

| 권한 타입 | 예시 권한 | 요청 방식 |
|----------|---------|---------|
| Normal | `ACCESS_NETWORK_STATE` | Manifest 선언 시 자동 허용 |
| Dangerous | `ACCESS_FINE_LOCATION`, `READ_PHONE_STATE` | 런타임 요청 필수 |
| Signature/System | `BATTERY_STATS` | 시스템 앱 전용 (라이브러리에서 강제 안 함) |

## 심각도 기준

- HIGH: `registerForActivityResult` 직접 사용 (`requestPermissions()` 미사용)
- MEDIUM: 권한 허용 후 `refreshPermissions()` 미호출 (SystemManager 사용 시)

## 예시

### 기본 권한 요청

❌ BAD
```kotlin
val launcher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
    if (results.all { it.value }) startLocationTracking()
}
launcher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
```

✅ GOOD
```kotlin
requestPermissions(
    permissions = listOf(Manifest.permission.ACCESS_FINE_LOCATION),
    onDeniedResult = { deniedResults ->
        if (deniedResults.isEmpty()) startLocationTracking()
    },
)
```

---

### 여러 권한 동시 요청

```kotlin
requestPermissions(
    permissions = listOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.READ_PHONE_STATE
    ),
    onDeniedResult = { deniedResults ->
        if (deniedResults.isEmpty()) {
            // 모든 권한 허용
            startLocationTracking()
            loadSimInfo()
        }
        // 일부만 허용된 경우 deniedResults에 거부된 권한 목록이 전달됨
    },
)
```

---

### SystemManager 사용 시 권한 갱신

```kotlin
requestPermissions(
    permissions = listOf(Manifest.permission.ACCESS_FINE_LOCATION),
    onDeniedResult = { deniedResults ->
        if (deniedResults.isEmpty()) {
            // 권한 허용 후 내부 캐시 갱신 필수
            locationInfo.refreshPermissions()
            locationInfo.registerStart(...)
        }
    },
)
```
