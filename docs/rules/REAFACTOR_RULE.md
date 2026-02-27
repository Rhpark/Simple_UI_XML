
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