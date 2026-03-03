# 아키텍처 규칙 (Architecture Rule)

## 레이어 책임 분리

- ViewModel: 오케스트레이션만, 비즈니스 로직 포함 금지
- UseCase: 비즈니스 로직 전담
- Repository: 데이터 조작만
- Activity / Fragment: UI 로직만

## 모듈 경계

- simple_core에서 View, Activity, Fragment 등 UI 컴포넌트 import 금지
- simple_core → simple_xml 역의존 금지

## 라이브러리 공개 API

- public / internal / private 접근 제어 적절히 사용
- 불필요하게 public으로 노출하지 않는다
- @RequiresPermission, @RequiresApi 어노테이션 표기 필수
- 하위 호환성 고려

## 상태 관리 (UIState)

- 상태는 StateFlow, 이벤트는 SharedFlow / Channel 사용
- 가변 상태(_mutableState)는 외부에 노출 금지
- UIState는 단일 sealed class로 관리

## SOLID 원칙

- **SRP (단일 책임)**: 클래스/함수는 하나의 책임만 가진다. 하나의 변경 이유만 있어야 한다.
- **OCP (개방/폐쇄)**: 확장에는 열려 있고, 수정에는 닫혀 있어야 한다. 기존 코드 수정 없이 기능 추가가 가능해야 한다.
- **LSP (리스코프 치환)**: 하위 클래스는 상위 클래스를 대체할 수 있어야 한다. 오버라이드 시 계약(사전조건/사후조건)을 깨지 않는다.
- **ISP (인터페이스 분리)**: 클라이언트가 사용하지 않는 메서드에 의존하지 않도록 인터페이스를 작게 분리한다.
- **DIP (의존성 역전)**: 고수준 모듈이 저수준 모듈에 직접 의존하지 않는다. 추상화(인터페이스)에 의존한다.

### SRP 위반 판단 기준

- 함수/클래스가 하는 일을 한 문장으로 설명하기 어렵다 → SRP 위반 의심
- "그리고(and)"가 설명에 들어간다 → 책임이 두 개 이상

## 심각도 기준

- CRITICAL: 모듈 경계 위반 (simple_core UI 의존, 역의존)
- HIGH: 비즈니스 로직 레이어 오배치
- HIGH: 가변 상태 외부 노출
- HIGH: 공개 API 접근 제어 부적절
- HIGH: SRP 위반 (하나의 클래스/함수가 여러 책임 보유)
- MEDIUM: DIP 위반 (구체 구현에 직접 의존)

## 예시

### simple_core에서 UI 의존

❌ BAD
```kotlin
// simple_core/.../FeatureHelper.kt
import android.app.Activity
class FeatureHelper(private val activity: Activity)
```

✅ GOOD
```kotlin
// simple_core/.../FeatureHelper.kt
class FeatureHelper(private val context: Context)
```

---

### simple_core → simple_xml 역의존

❌ BAD
```kotlin
// simple_core/build.gradle.kts
implementation(project(":simple_xml"))
```

✅ GOOD
```kotlin
// simple_core/build.gradle.kts
// simple_xml 의존성 없음 (역의존 금지)
```

---

### API/권한 어노테이션 누락

❌ BAD
```kotlin
fun currentLocation(): Location {
    return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)!!
}
```

✅ GOOD
```kotlin
@RequiresPermission(anyOf = [
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
])
fun currentLocation(): Location? {
    return safeCatch(defaultValue = null) {
        locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
    }
}
```
