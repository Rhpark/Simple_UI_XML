# PermissionRequester SPEC

## 문서 정보
- 문서명: PermissionRequester SPEC
- 작성일: 2026-01-13
- 대상 모듈: simple_core
- 패키지: kr.open.library.simple_ui.core.permissions.*
- 상태: 확정
- 연계 문서: PRD.md
- 작업 가이드: [AGENTS.md](AGENTS.md) (권한 모듈 작업 규칙)

## 목표
- PRD의 요구사항을 구현 가능한 수준으로 구체화한다.
- Activity/Fragment 의존 영역과 비의존 영역을 분리한다.
- 회전/프로세스 종료 후 복원까지 고려한 상태 보존을 정의한다.

## 공개 API(확정)
### 생성
```
val requester = PermissionRequester(this)
```
- Activity 또는 Fragment에서 생성한다.
- 권장 생성 시점: Activity onCreate, Fragment onAttach~onCreate.

### 상태 복원/저장
```
requester.restoreState(savedInstanceState)
requester.saveState(outState)
```
- savedInstanceState/outState는 Activity/Fragment에서 전달한다.
- restoreState는 요청 전에 1회 호출한다.
- 요청 이후 restoreState 호출은 무시한다.

### 요청
```
requester.requestPermission(
    permission = permission,
    onDeniedResult = { deniedList ->
        // deniedList: List<PermissionDeniedItem>
    },
)

requester.requestPermissions(
    permissions = permissions,
    onDeniedResult = { deniedList ->
        // deniedList: List<PermissionDeniedItem>
    },
)
```

### 복원 결과 회수
```
val orphanedDeniedResults = requester.consumeOrphanedDeniedResults()
// List<OrphanedDeniedRequestResult>
```
- 프로세스 복원으로 콜백을 전달하지 못한 결과를 반환하고 내부 캐시는 비운다.

```
data class OrphanedDeniedRequestResult(
    val requestId: String,
    val deniedResults: List<PermissionDeniedItem>
)
```
### 결과 모델
```
data class PermissionDeniedItem(
    val permission: String,
    val result: PermissionDeniedType
)

enum class PermissionDeniedType {
    DENIED,
    PERMANENTLY_DENIED,
    MANIFEST_UNDECLARED,
    EMPTY_REQUEST,
    NOT_SUPPORTED,
    FAILED_TO_LAUNCH_SETTINGS,
    LIFECYCLE_NOT_READY
}
```
```
enum class PermissionDecisionType {
    GRANTED,
    DENIED,
    PERMANENTLY_DENIED,
    MANIFEST_UNDECLARED,
    EMPTY_REQUEST,
    NOT_SUPPORTED,
    FAILED_TO_LAUNCH_SETTINGS,
    LIFECYCLE_NOT_READY
}
```
- `PermissionDeniedType`는 외부 콜백에 사용한다(거부/실패만 포함).
- `PermissionDecisionType`는 내부 처리/저장에 사용하며 GRANTED를 포함한다.

### 결과 타입 기준
- MANIFEST_UNDECLARED: Manifest에 선언되지 않은 권한 문자열(빈 문자열 포함)
- EMPTY_REQUEST: 요청 리스트가 비어 있음
- LIFECYCLE_NOT_READY: Lifecycle 상태가 요청/실행에 적합하지 않음(INITIALIZED/DESTROYED)
- onDeniedResult는 항상 호출되며, 모두 승인된 경우 빈 리스트로 전달된다.

### UI 훅
- onRationaleNeeded: 런타임 권한 설명 UI 표시용
- onNavigateToSettings: 특수 권한 설정 화면 이동 전 안내용
- 설정 화면 복귀 이후 결과는 onDeniedResult 콜백으로 전달한다.
- 콜백은 requestPermission(s) 호출 시 전달한다.
- 훅은 proceed/cancel 콜백으로 흐름을 제어한다.

## 입력 검증 정책
- 잘못된 권한 문자열(Manifest 미선언/빈 문자열)은 MANIFEST_UNDECLARED로 거부 목록에 포함하고 나머지는 정상 처리한다.
- 빈 요청은 permission 빈 문자열로 EMPTY_REQUEST를 반환한다.
- INITIALIZED/DESTROYED 상태에서는 LIFECYCLE_NOT_READY로 결과를 반환한다.

## 내부 구조(의존/비의존 분리)
### 쉬운 설명
- Activity/Fragment 없이도 되는 로직은 core에, Activity/Fragment가 필요한 요청/라이프사이클 로직은 xml에 둔다.
### 의존 영역
- PermissionRequester(외부 API)
- ActivityResult 등록/실행
- Settings/Role 인텐트 실행
- LifecycleObserver(onResume) 처리

### 비의존 영역
- Permission 분류기(런타임/특수/Role)
- 큐/병합 정책
- 내부 결정 결과를 거부 모델로 매핑
- permanently denied 판정
- SavedState 직렬화/복원

## 주요 컴포넌트(예시)
- PermissionRequester: 외부 공개 API, 요청 수신 및 거부 결과 콜백 집계
- PermissionHostAdapter: Activity/Fragment 래핑(필요 인터페이스 제공)
- PermissionQueue: 요청 큐/병합 관리
- PermissionStateStore: Bundle 기반 상태 저장/복원
- PermissionClassifier: 권한 타입 분류
- SpecialPermissionHandler: 특수 권한 체크/요청 매핑
- RolePermissionHandler: Role 요청/체크

## 파일/클래스 구조(확정)
### core (UI 비의존)
- classifier
  - PermissionClassifier.kt
- extentions
  - PermissionExtensions.kt
- handler
  - SpecialPermissionHandler.kt
  - RolePermissionHandler.kt
- model
  - PermissionModels.kt
- queue
  - PermissionQueue.kt
- vo
  - PermissionConstants.kt
  - PermissionSpecialType.kt

### xml (UI 의존)
- api
  - PermissionRequester.kt
- host
  - PermissionHostAdapter.kt
- flow
  - PermissionFlowProcessor.kt
  - RuntimePermissionHandler.kt
- coordinator
  - PermissionRequestCoordinator.kt
- result
  - PermissionResultAggregator.kt
- state
  - PermissionStateStore.kt
- register
  - PermissionRequestInterface.kt

## 패키지 역할 상세
### core
- `classifier`: 권한 문자열을 런타임/특수/Role로 분류하고 지원 여부를 판단한다.
- `extentions`: 권한 보유 여부 등 공통 확장 함수를 제공한다.
- `handler`: 특수 권한/Role 권한의 체크 및 인텐트 생성 규칙을 담당한다.
- `model`: 결과/훅/복원 모델을 정의한다.
- `queue`: 요청 큐 및 중복 병합 정책을 담당한다.
- `vo`: 권한 상수 및 특수 권한 타입 정의를 제공한다.

### xml
- `api`: 호출부가 사용하는 PermissionRequester 공개 API를 제공한다.
- `host`: Activity/Fragment 기능을 추상화하여 요청 흐름에 필요한 기능을 제공한다.
- `flow`: 런타임/특수/Role 권한 요청 흐름을 순차 처리한다.
- `coordinator`: 요청 직렬화, 큐 처리, 복원 요청 재처리를 담당한다.
- `result`: 요청 결과 집계, 완료 판정, orphaned 결과 처리를 담당한다.
- `state`: 저장/복원 상태를 Bundle로 직렬화/역직렬화한다.
- `register`: RootActivity/RootFragment가 구현하는 PermissionRequestInterface 계약을 제공한다.

## 상태 보존 정책
- 회전 시 시스템 UI 유지 + 결과 콜백 반드시 수신.
- 특수 권한은 설정 화면 복귀 시점에 재확인하여 결과를 매칭.
- ViewModel 없이 외부 savedInstanceState Bundle을 직접 사용한다.
- 프로세스 종료 후 복원까지 고려한다.
  - 큐/진행 상태/요청 이력은 복원 가능.
  - 콜백 객체는 복원 불가이므로 결과를 캐시에 보관하고, consumeOrphanedDeniedResults로 회수한다.
- 프로세스 복원 후 런타임/특수/Role 요청 UI는 자동 재진입하지 않고 현재 상태로 결과를 확정한다.

## Lifecycle 정책
- 요청 수신은 CREATED 이상에서 허용한다.
- 시스템 UI/설정 화면 launch는 STARTED 이상에서만 수행한다.

## 큐/병합 정책 상세
- 동일 권한 요청은 병합한다.
- 병합되지 않은 요청은 FIFO 큐로 순차 처리한다.
- 큐 범위는 Activity/Fragment 인스턴스 단위로 제한한다.
- 요청 처리 흐름
  1) 요청 수신 → 권한 리스트 정규화(중복 제거)
  2) 각 권한에 대한 PendingEntry 조회/생성
  3) 동일 권한은 PendingEntry에 콜백을 병합 등록
  4) 요청 토큰 단위로 결과 집계
  5) 모든 권한 결과 수신 시 콜백 호출

## 런타임 권한 처리
- ActivityResultContracts.RequestMultiplePermissions 사용.
- 결과 매핑 규칙:
  - granted = true → GRANTED
  - granted = false:
    - shouldShowRequestPermissionRationale = true → DENIED
    - shouldShowRequestPermissionRationale = false:
      - 과거 요청 이력 있음 → PERMANENTLY_DENIED
      - 요청 이력 없음 → DENIED
- 요청 이력은 상태 저장(Bundle)에 포함한다.
- GRANTED 결과는 내부 처리용이며 onDeniedResult에는 포함되지 않는다.

## 특수 권한 처리(설정 화면 이동형)
- 요청: Settings 인텐트 실행
- 결과: onResume 시점에 재확인
- 인텐트 실행 실패 시 FAILED_TO_LAUNCH_SETTINGS
- 기기/OS에서 미지원 시 NOT_SUPPORTED

## 역할(Role) 요청 처리
- RoleManager 가용성 확인
- createRequestRoleIntent 실행
- 결과 수신 후 isRoleHeld로 최종 판단
- Role 문자열은 `android.app.role.` prefix로 구분한다.

## 특수 권한 매핑(확정)
> 이 표는 PRD의 특수 권한 목록과 동일하며 최종 확정됨.
- SYSTEM_ALERT_WINDOW
  - 요청: Settings.ACTION_MANAGE_OVERLAY_PERMISSION
  - 체크: Settings.canDrawOverlays(context)
- WRITE_SETTINGS
  - 요청: Settings.ACTION_MANAGE_WRITE_SETTINGS
  - 체크: Settings.System.canWrite(context)
- REQUEST_INSTALL_PACKAGES
  - 요청: Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES
  - 체크: PackageManager.canRequestPackageInstalls()
- REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
  - 요청: Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
  - 체크: PowerManager.isIgnoringBatteryOptimizations(packageName)
- ACCESS_NOTIFICATION_POLICY
  - 요청: Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS
  - 체크: NotificationManager.isNotificationPolicyAccessGranted()
- PACKAGE_USAGE_STATS
  - 요청: Settings.ACTION_USAGE_ACCESS_SETTINGS
  - 체크: UsageStatsManager 접근 가능 여부(정책 기반 판단)
- MANAGE_EXTERNAL_STORAGE
  - 요청: Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
  - 체크: Environment.isExternalStorageManager()
- SCHEDULE_EXACT_ALARM
  - 요청: Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
  - 체크: AlarmManager.canScheduleExactAlarms()
- Notification Listener(특수 앱 액세스)
  - 요청: Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
  - 체크: NotificationManager.isNotificationListenerAccessGranted(ComponentName)
- Accessibility Service(특수 앱 액세스)
  - 요청: Settings.ACTION_ACCESSIBILITY_SETTINGS
  - 체크: AccessibilityManager.getEnabledAccessibilityServiceList(...)
- RoleManager(ROLE_*)
  - 요청: RoleManager.createRequestRoleIntent(role)
  - 체크: RoleManager.isRoleHeld(role)
- MANAGE_MEDIA
  - 요청: Settings.ACTION_REQUEST_MANAGE_MEDIA
  - 체크: 보호 수준상 일반 앱 부여 어려움 → 기본 NOT_SUPPORTED 처리

## 로깅/에러 처리
- Logx로 통일하여 로깅한다.
- 예외 발생 시 기본값을 반환하고 null 반환은 최소화한다.
- 시스템 서비스 접근 시 tryCatchSystemManager 사용을 권장한다.
- try-catch에서 결과가 필요한 경우 safeCatch 사용을 권장한다.
- 로그에는 requestId, permission, type, deniedType를 포함한다.

## 테스트 전략
- 단위 테스트: 권한 분류, 큐/병합 정책, 거부 결과 매핑, 저장/복원 로직
- Robolectric 테스트: Activity/Fragment 요청 흐름, 설정 화면 이동 후 복귀 시나리오

## 오픈 이슈
- 제조사 커스텀 ROM에서 설정 화면 경로 차이 대응 범위 결정





