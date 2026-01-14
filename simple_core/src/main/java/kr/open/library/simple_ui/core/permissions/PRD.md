# PermissionRequester PRD

## 문서 정보
- 문서명: PermissionRequester PRD
- 작성일: 2026-01-13
- 대상 모듈: simple_core
- 패키지: kr.open.library.simple_ui.core.permissions.*
- 상태: 확정
- 작업 가이드: [AGENTS.md](AGENTS.md) (권한 모듈 작업 규칙)

## 배경/문제 정의
- 권한 요청은 시스템 UI/설정 화면과 연동되므로 Activity/Fragment 의존이 불가피하다.
- 하지만 권한 분류, 결과 모델링, 거부 판정, 큐 처리 등은 의존성을 제거한 순수 로직으로 분리할 수 있다.
- 현재 권한 요청 방식이 분산되면 UX 일관성과 유지보수성이 저하된다.

## 목표
- 런타임 위험 권한 + 특수 권한 + 역할(Role) 요청을 하나의 API로 통합한다.
- PermissionRequester는 `PermissionRequester(this)` 형태로 간단히 생성한다.
- 거부 결과만 단일 모델로 통일해 호출부에서 일관되게 처리할 수 있게 한다.
- 동시 요청을 안전하게 직렬화하고, 중복 권한 요청을 병합한다.
- 의존적인 영역과 비의존적인 영역을 분리해 테스트 가능성을 높인다.

## 비목표
- UI 제공(다이얼로그/토스트/스낵바 등)은 제외한다.
- Compose 지원은 범위에서 제외한다(추후 고려).

## 범위
### 지원 환경
- minSdk: 28
- targetSdk: 35

### 권한 범위
- 런타임 위험 권한: Android 위험 권한 전체(Manifest에 선언된 dangerous 권한).
- 특수 권한: 시스템 설정에서 사용자가 직접 허용해야 하는 권한/액세스.
- 역할(Role) 요청: RoleManager 기반 사용자 동의가 필요한 역할.
- Role 문자열은 `android.app.role.` prefix로 구분한다.

### 특수 권한(확정)
> 특수 권한은 OS/제조사에 따라 항목이 다를 수 있으므로, 런타임 가용성 체크 및 매핑 테이블을 기본 정책으로 한다.
- 오버레이: SYSTEM_ALERT_WINDOW
- 시스템 설정 변경: WRITE_SETTINGS
- 전체 파일 접근: MANAGE_EXTERNAL_STORAGE
- 알 수 없는 앱 설치: REQUEST_INSTALL_PACKAGES
- 정확한 알람 예약: SCHEDULE_EXACT_ALARM
- 방해금지 접근: ACCESS_NOTIFICATION_POLICY
- 배터리 최적화 제외: REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
- 사용 기록 접근: PACKAGE_USAGE_STATS
- 알림 접근(특수 앱 액세스): BIND_NOTIFICATION_LISTENER_SERVICE (런타임 요청 권한 아님, 설정에서만 활성화)
- 접근성 서비스(특수 앱 액세스): BIND_ACCESSIBILITY_SERVICE (런타임 요청 권한 아님, 설정에서만 활성화)
- 역할(Role): ROLE_* (RoleManager 기반)
- MANAGE_MEDIA: 요청 Intent는 존재하지만 signature|appop|preinstalled 보호 수준으로 일반 앱에 부여되기 어려움

## 설계 원칙
- 의존적 영역과 비의존적 영역을 명확히 분리한다.
  - 쉽게 말해, Activity/Fragment 없이도 동작하는 로직은 core에 두고, Activity/Fragment가 필요한 요청/라이프사이클 로직은 xml에 둔다.
  - 의존적 영역: Activity/Fragment 기반 요청 실행기, Activity Result 등록/호출.
  - 비의존적 영역: 권한 분류, 결과 모델, 영구 거부 판정, 큐/병합 정책.
- 단일 API 제공, 내부 분기 처리(런타임 vs 특수 vs 역할).
- 인스턴스 범위는 Activity/Fragment 단위로 제한한다(전역 싱글턴 금지).

## 모듈/패키지 구조(요약)
- 권한 요청 기능은 `simple_core`와 `simple_xml`로 분리한다.
- `kr.open.library.simple_ui.core.permissions`
  - Activity/Fragment 비의존 로직을 담당한다.
  - 권한 분류, 결과 모델, 특수/Role 처리 규칙, 큐/병합 정책, 확장 함수, 상수 정의가 포함된다.
- `kr.open.library.simple_ui.xml.permissions`
  - Activity/Fragment 의존 로직을 담당한다.
  - PermissionRequester 공개 API, ActivityResult 등록/실행, 요청 흐름/직렬화, 상태 저장/복원이 포함된다.

## API 설계(확정)
### 생성
```
val requester = PermissionRequester(this)
```

### 상태 복원/저장
```
requester.restoreState(savedInstanceState)
requester.saveState(outState)
```
- savedInstanceState/outState는 Activity/Fragment에서 전달한다.
- restoreState는 요청 전에 1회 호출한다.

### 단일 권한 요청
```
requester.requestPermission(
    permission = permission,
    onDeniedResult = { deniedList ->
        // deniedList: List<PermissionDeniedItem>
    },
)
```

### 복수 권한 요청
```
requester.requestPermissions(
    permissions = permissions,
    onDeniedResult = { deniedList ->
        // deniedList: List<PermissionDeniedItem>
    },
)
```

### 결과 모델
```
data class PermissionDeniedItem(
    val permission: String,
    val result: PermissionDeniedType
)
```
```
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
- `permission`: 요청한 권한 문자열
- `result`: 거부/실패 결과 상태
- `MANIFEST_UNDECLARED`: Manifest에 선언되지 않은 권한 문자열(빈 문자열 포함)
- `EMPTY_REQUEST`: 요청 리스트가 비어 있음
- `LIFECYCLE_NOT_READY`: Lifecycle 상태가 요청/실행에 적합하지 않음(INITIALIZED/DESTROYED)
- `onDeniedResult`는 항상 호출되며, 모두 승인된 경우 빈 리스트로 전달된다.

### UI 훅(콜백)
- `onRationaleNeeded`: 권한 설명 UI를 호출부에서 제공할 수 있게 훅 제공
- `onNavigateToSettings`: 특수 권한의 설정 화면 이동 전 훅 제공
- 설정 화면 복귀 이후 결과는 onDeniedResult 콜백으로 전달한다.
- 콜백은 requestPermission(s) 호출 시 전달한다.
- 훅은 proceed/cancel 콜백으로 흐름을 제어한다.

## 입력 검증 정책
- 잘못된 권한 문자열(Manifest 미선언/빈 문자열)은 MANIFEST_UNDECLARED로 거부 목록에 포함하고 나머지는 정상 처리한다.
- 빈 요청은 permission 빈 문자열로 EMPTY_REQUEST를 반환한다.

## Lifecycle 정책
- 요청 수신은 CREATED 이상에서 허용한다.
- 시스템 UI/설정 화면 launch는 STARTED 이상에서만 수행한다.
- INITIALIZED/DESTROYED 상태에서는 LIFECYCLE_NOT_READY로 결과를 반환한다.

## 동시 요청 처리 정책
- 동일 권한 요청은 병합한다.
- 병합되지 않은 요청은 FIFO 큐로 순차 처리한다.
- 큐 처리는 인스턴스 범위(Activity/Fragment) 내에서만 동작한다.

## 구성 변경(회전) 보존 정책
- 회전 시에도 시스템 UI가 유지되고, 결과 콜백을 반드시 수신해야 한다.
- 특수 권한은 설정 화면 복귀 시점에 재확인하여 결과를 매칭한다.
- ViewModel을 사용하지 않고, 외부 savedInstanceState Bundle을 통해 큐/진행 상태를 저장/복원한다.
- 프로세스 종료 후 복원까지 고려한다.
- 프로세스 복원 후 콜백 미전달 결과는 별도 회수 API로 제공한다.
- 프로세스 복원 후 런타임/특수/Role 요청 UI는 자동 재진입하지 않고 현재 상태로 결과를 확정한다.

## 동작 흐름 요약
### 런타임 위험 권한
- 권한 보유 여부 체크 → 필요 시 rationale 훅 → 시스템 권한 다이얼로그 요청
- 결과 수신 후 거부 목록(PermissionDeniedItem 리스트)으로 반환

### 특수 권한
- 권한 보유 여부를 각 권한의 전용 체크 방식으로 확인
- 필요한 경우 설정 화면으로 이동
- 복귀 시점에 재확인 후 결과 반환

### 역할(Role)
- RoleManager로 가용성 확인 → 요청 인텐트 실행 → 결과 확인

## SDK 분기/주의사항
- 권한 다이얼로그 노출 여부는 시스템이 결정하며 항상 뜨는 것이 아니다.
- 특수 권한은 런타임 다이얼로그가 없고 설정 화면 이동 후 복귀 시점에서 결과를 확인해야 한다.
- 오버레이 권한 화면 이동 시 Android 11+에서는 패키지 지정이 무시될 수 있다.
- MANAGE_MEDIA는 일반 앱이 사용자에게 요청해서 받는 권한이 아니므로 기본 결과는 NOT_SUPPORTED로 처리한다.

## 에러 처리/로깅
- 안전한 기본값 반환을 우선한다.
- 예외 발생 시 Logx로 통일하여 로깅한다.
- 시스템 서비스 접근은 tryCatchSystemManager 사용을 권장한다.
- try-catch에서 결과가 필요한 경우 safeCatch 사용을 권장한다.

## 테스트 전략
- 단위 테스트: 비의존 로직(권한 분류, 큐/병합 정책, 결과 모델)
- Robolectric 테스트: Activity/Fragment 의존 요청 흐름
- 테스트 파일 위치/네이밍은 프로젝트 규칙을 따른다.

## 리스크/오픈 이슈
- 제조사 커스텀 ROM에 따른 설정 화면 경로 차이
- 권한 요청 동시성에 따른 엣지 케이스(화면 회전/백그라운드 전환)







