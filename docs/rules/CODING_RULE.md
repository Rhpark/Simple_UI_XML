# CODING RULES

## 코딩 컨벤션 & 스타일
 - 네이밍: PascalCase for classes, camelCase for functions
 - Kotlin 스타일: Android 공식 가이드 준수
 - XML에서 ID 생성 시 camelCase로 id 생성

### Kotlin/Coroutine/Flow 우선
 - 상태 StateFlow 관리.
 - 이벤트 SharedFlow/Channel 관리.
 - 동시성 Mutex/SupervisorJob 관리.
- 예제 코드(simple_xml/src/main/java/kr/open/library/simple_ui/xml/permissions/api/PermissionRequester.kt, simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/view/recyclerview/RecyclerScrollStateView.kt).

### 명시적 가시성·어노테이션
 - public/private를 드러내고 @RequiresPermission, @RequiresApi로 API·권한 요구사항을 문서화 (simple_core/src/main/java/kr/open/library/simple_ui/core/system_manager/info/location/LocationStateInfo.kt, simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/components/activity/root/RootActivity.kt).

### API 분기 헬퍼·게으른 초기화
 - checkSdkVersion 인라인 분기와 by lazy로 시스템 서비스 접근을 캡슐화 (simple_core/src/main/java/kr/open/library/simple_ui/core/system_manager/controller/wifi/WifiController.kt).

### 도메인별 패키징·타입 세분화
 - extensions, system_manager, logcat, ui/adapter 등 도메인 단위 디렉터리.
 - 연산 모델은 sealed class(AdapterOperationQueue.kt), 값 객체는 data class(telephony/network 등).


## 주석 스타일
 - 주석은 한·영 병기 규칙을 따른다.
 - 먼저 영어 설명 이후, 곧바로 <br><br>로 두 줄 공백을 만든다
 - 뒤 같은 내용을 한글로 반복해 “영문 → 빈 줄 → 국문” 구성을 유지한다.
 - 국문이 끝나는 곳에 <br>로 한 줄 공백을 만든다.
 - @return 도 동일한 방식이다. 첫 줄은 “반환값”과 “로그 동작”을 영어로 설명한다
 - <br><br>로 줄을 바꾼 뒤 한글 설명을 붙인다.
 - 한글이 끝나는 곳에 <br>로 한 줄 공백을 만든다.
 - 각 @param 블록은 첫 줄에 영어 설명을 적고 <br><br>로 두 줄을 비운다
 - 다음, 동일 의미의 한글을 다음 줄에 쓴다.
 - 추가 설명이 필요하면 들여쓰기를 유지한 채 이어지는 줄에 적는다.
 - 주석 예제 코드 (package kr.open.library.simple_ui.core.extensions.date)

## 주석 예시

### Good
```kotlin
/**
* Formats date to string.<br><br>
* 날짜를 문자열로 변환합니다.<br>
  */
```

### Bad
```kotlin
/** 날짜를 문자열로 변환 */
```

## 클래스 문서화 템플릿 (선택 사항)
 - 복잡한 클래스나 라이브러리의 핵심 클래스는 아래 템플릿을 사용하여 포괄적인 문서를 작성할 수 있다.
 - 템플릿의 각 섹션 제목은 "**영문 / 한글:**<br>" 형식을 따른다.
 - 템플릿의 내용은 "영문 → <br><br> → 한글 → <br>" 형식을 따른다.
 - "Important notes / 주의사항" 섹션은 선택 사항이며, 필요시에만 추가한다.

```kotlin
/**
 * Brief description.<br><br>
 * 간단한 설명.<br>
 *
 * **Why this class exists / 이 클래스가 필요한 이유:**<br>
 * - Reason 1<br>
 * - Reason 2<br><br>
 * - 이유 1<br>
 * - 이유 2<br>
 *
 * **Design decisions / 설계 결정 이유:**<br>
 * - Decision 1<br>
 * - Decision 2<br><br>
 * - 결정 1<br>
 * - 결정 2<br>
 *
 * **Important notes / 주의사항:**<br>
 * - Note 1<br>
 * - Note 2<br><br>
 * - 주의사항 1<br>
 * - 주의사항 2<br>
 *
 * **Usage / 사용법:**<br>
 * 1. Step 1<br>
 * 2. Step 2<br><br>
 * 1. 단계 1<br>
 * 2. 단계 2<br>
 */
```


## 에러 처리 규칙
 - try-catch에서 return값이 필요한 경우 safeCatch를 권장한다.(simple_core/src/main/java/kr/open/library/simple_ui/core/extensions/trycatch/TryCatchExtensions.kt)
 - 시스템 서비스는 tryCatchSystemManager 사용을 권장한다.
 - Logx로 통일된 로깅을 권장한다.
 - 에러 발생 시 기본값 반환, null 반환 최소화


## safeCatch 예시

### Good
```kotlin
// 1. 반환값 없이 안전 실행
safeCatch {
    riskyOperation()
}

// 2. 기본값 반환
val name = safeCatch(defaultValue = "Unknown") {
    getUserName()
}

// 3. 커스텀 에러 핸들러
val result = safeCatch(
    block = { parseJson(raw) },
    onCatch = { e -> FallbackData(error = e.message) }
)
```

### Bad
```kotlin
// 빈 catch 블록
try {
    riskyOperation()
} catch (e: Exception) { }

// CancellationException 삼키기
try {
    coroutineWork()
} catch (e: Exception) {
    // CancellationException도 여기서 잡힘 - 위험!
}
```

- 상세 사용법: docs/readme/README_EXTENSIONS.md 참조


## checkSdkVersion 예시

### Good
```kotlin
// 단순 실행
checkSdkVersion(Build.VERSION_CODES.TIRAMISU) {
    requestPermission(POST_NOTIFICATIONS)
}

// 반환값이 필요한 경우
val result = checkSdkVersion(Build.VERSION_CODES.Q) {
    getExternalFilesDir()
} // API 29 미만이면 null 반환

// 분기가 필요한 경우
val dir = checkSdkVersion(
    ver = Build.VERSION_CODES.Q,
    positiveWork = { getMediaStoreUri() },
    negativeWork = { getExternalStoragePath() }
)
```

### Bad
```kotlin
// 직접 if 분기 사용
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    requestPermission(POST_NOTIFICATIONS)
}
```

- 상세 사용법: simple_core/src/main/java/kr/open/library/simple_ui/core/extensions/conditional/SdkVersionInline.kt 참조


## Logx 예시

### Good
```kotlin
// 기본 사용
Logx.d("사용자 로그인 성공")
Logx.e("네트워크 오류 발생: $errorCode")

// 커스텀 태그
Logx.d("LoginViewModel", "로그인 시도: $userId")

// 호출 위치 추적 (parent trace)
Logx.p("이 함수를 호출한 곳 추적")

// JSON 포맷 출력
Logx.j(responseBody)

// 스레드 정보 포함
Logx.t("현재 스레드에서 실행 중")
```

### Bad
```kotlin
// Android 기본 Log 사용
Log.d("TAG", "메시지")

// println 사용
println("디버그: $value")
```

- 상세 사용법: docs/readme/README_LOGX.md 참조


## XML Id 규칙
 - Button에 id를 추가할 경우 +id/btn* 으로 시작한다.
 - EditText에 id를 추가할 경우 +id/edt* 으로 시작한다.
 - TextView에 id를 추가할 경우 +id/tv* 으로 시작한다.
 - RecyclerScrollStateView에 id를 추가할 경우 +id/rcv* 으로 시작한다.
 - Checkbox에 id를 추가할 경우 +id/cb* 으로 시작한다.
 - 기타 다른 View에 id를 추가 시 위와 비슷한 형식으로 id를 생성한다.


## 코드 작성 금지 패턴
 - 존재하지 않는 API/클래스/메서드를 사용하지 않는다. (hallucination 방지)
 - deprecated된 API를 신규 코드에 사용하지 않는다.
 - simple_core에서 View, Activity, Fragment 등 UI 컴포넌트를 import하지 않는다.
 - simple_core에서 simple_xml을 의존하지 않는다. (역방향 의존 금지)
 - try-catch에서 빈 catch 블록을 작성하지 않는다. (safeCatch 또는 Logx 사용)
 - Logx 외의 로깅 방식(Log.d, println 등)을 사용하지 않는다.
 - XML ID에 snake_case를 사용하지 않는다. (camelCase 필수)
