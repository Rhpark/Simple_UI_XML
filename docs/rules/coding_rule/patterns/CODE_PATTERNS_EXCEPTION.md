# 예외 처리 패턴 규칙

## 규칙

- safeCatch / tryCatchSystemManager 사용
- 빈 catch 블록 금지 (예외를 삼키는 코드 금지)

## 심각도 기준

- HIGH: 빈 catch 블록 (예외 무시)
- MEDIUM: safeCatch 미사용

## 예시

❌ BAD
```kotlin
fun parseUser(raw: String): User? {
    return try {
        parser.parse(raw)
    } catch (e: Exception) {
        null
    }
}
```

✅ GOOD
```kotlin
fun parseUser(raw: String): User? {
    return safeCatch(defaultValue = null) {
        parser.parse(raw)
    }
}
```
