# Permission Feature AGENTS

## 목적
- 권한 모듈 작업 규칙을 한 곳에 모은다.
- core/xml/compose 분리 원칙과 요청 흐름 일관성을 유지한다.
- PRD/SPEC/Implementation과 구현 정합성을 항상 확인한다.

## 적용 범위
- core: `kr.open.library.simple_ui.core.permissions.*`
- xml: `kr.open.library.simple_ui.xml.permissions.*`
- compose: `kr.open.library.simple_ui.compose.permissions.*`

## 공통 규칙
- 분리 원칙: UI·ActivityResult·Lifecycle 비의존 정책은 core, UI 기술 의존 요청/라이프사이클 로직은 xml 또는 compose에 둔다.
- UI(다이얼로그/토스트/스낵바)는 제공하지 않는다.
- 결과는 예외 대신 `PermissionDeniedType`로 반환한다.
- 결정 맵에서 거부 목록을 생성할 때 core `buildPermissionDeniedItems`를 단일 출처로 사용한다.
- 로깅은 Logx로 통일한다.
- 기본값 반환을 우선하고 null 반환은 최소화한다.
- 문서(PRD/SPEC/IMPLEMENTATION_PLAN)와 구현 불일치를 허용하지 않는다.

## core(비의존 영역) 규칙
- Activity/Fragment/ActivityResult/Lifecycle에 의존하지 않는다.
- 권한 분류, 결과 모델, 특수/Role 규칙, 큐/병합 정책을 담당한다.
- Role 문자열은 `android.app.role.` prefix로 구분한다.
- 특수 권한 목록/매핑은 `PermissionSpecialType`/`PermissionConstants` 기준을 사용한다.
- `MANAGE_MEDIA`는 일반 앱 요청 불가로 기본 `NOT_SUPPORTED` 처리한다.
- 테스트는 unit 테스트로 구성한다.

## xml(의존 영역) 규칙
- Activity/Fragment 의존 로직은 모두 xml에 위치한다.
- `PermissionRequester(this)`를 XML 요청의 단일 공개 진입점으로 사용한다.
- ActivityResult 등록은 호스트 생성 시점에만 수행한다.
- 요청 수신은 CREATED 이상에서 허용하고, 실제 launch는 STARTED 이상에서만 수행한다.
- `restoreState(savedInstanceState)`는 요청 전에 1회 호출한다.
- `saveState(outState)`는 onSaveInstanceState에서 호출한다.
- 인스턴스는 Activity/Fragment 단위로 유지하며 전역 싱글턴을 금지한다.
- 요청은 FIFO 큐로 직렬 처리하고, 동일 권한은 병합한다.
- UI 훅(onRationaleNeeded): 설명 UI를 제공하고 proceed/cancel로 흐름을 제어한다.
- UI 훅(onNavigateToSettings): 설정 화면 이동 전 안내 훅만 제공한다.
- 설정 화면 복귀 이후 결과는 onDeniedResult 콜백으로 전달한다.
- 살아 있는 요청의 onDeniedResult는 항상 호출하며, 모두 승인된 경우 빈 리스트를 전달한다.
- 프로세스 복원으로 콜백을 잃은 여러 요청 결과는 requestId별 orphaned 결과로 보존한다.

## compose(의존 영역) 규칙
- ActivityResult와 Compose 수명주기 의존 로직은 simple_compose에 위치한다.
- `rememberPermissionRequestState`를 Compose 요청의 단일 공개 진입점으로 사용한다.
- XML과 결과 타입·사전 판정 정책을 공유하되 요청 식별자와 복원 저장소는 공유하지 않는다.
- 콜백을 복원할 수 없는 경우 해당 State 인스턴스의 최신 완료 결과 한 건을 `deniedItems`와 `COMPLETED` phase로 보존한다.
- Resume 자동 재확인과 `refresh()`는 현재 보유 여부인 `allGranted`만 재계산하며, 마지막 결과인 `deniedItems`와 `phase`는 변경하지 않는다.

## XML 요청 예제
### 기본 요청
```kotlin
val requester = PermissionRequester(this)

requester.requestPermission(Manifest.permission.CAMERA) { results ->
    val allGranted = results.isEmpty()
    results.forEach { item ->
        when (item.result) {
            PermissionDeniedType.DENIED -> Unit
            PermissionDeniedType.PERMANENTLY_DENIED -> Unit
            PermissionDeniedType.MANIFEST_UNDECLARED -> Unit
            PermissionDeniedType.EMPTY_REQUEST -> Unit
            PermissionDeniedType.NOT_SUPPORTED -> Unit
            PermissionDeniedType.FAILED_TO_LAUNCH_SETTINGS -> Unit
            PermissionDeniedType.LIFECYCLE_NOT_READY -> Unit
        }
    }
}

requester.requestPermissions(
    listOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
    ),
) { results ->
    val allGranted = results.isEmpty()
    // 동일한 결과 처리 방식 사용
}
```

- “모두 승인” 판단은 `results.isEmpty()`를 사용한다.
- 빈 요청은 `EMPTY_REQUEST` 1건이 반환된다.
- Manifest 미선언 권한은 `MANIFEST_UNDECLARED`로 반환된다.

### UI 훅 포함 요청
```kotlin
val requester = PermissionRequester(this)

requester.requestPermissions(
    listOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
    ),
    onDeniedResult = { deniedResults ->
        // 결과 처리
    },
    onRationaleNeeded = { rationale ->
        // 권한 설명 UI 표시 후 진행 여부 결정
        // 예) 사용자 동의 시 rationale.proceed(), 거절 시 rationale.cancel()
    },
    onNavigateToSettings = { settings ->
        // 설정 화면 이동 안내 후 진행 여부 결정
        // 예) 안내 후 settings.proceed(), 취소 시 settings.cancel()
    },
)
```

## 상태 보존/프로세스 복원
- Bundle 기반 저장/복원을 사용한다.
- 콜백은 복원되지 않으므로 orphaned 결과는 `consumeOrphanedDeniedResults()`로 회수한다.
- 프로세스 복원 후 자동 재진입은 하지 않는다.

## 금지 패턴

- Activity/Fragment/Lifecycle을 core 패키지에 두지 않는다
- 전역 싱글턴 인스턴스를 허용하지 않는다 (Activity/Fragment 단위 유지)
- UI(다이얼로그/토스트/스낵바)를 권한 모듈 내부에서 직접 제공하지 않는다
- 결과를 예외(Exception)로 반환하지 않는다 — `PermissionDeniedType` 사용

## 판단 기준

- Activity/Fragment 없이 동작 가능한 로직 → core
- ActivityResult / Lifecycle 의존 로직 → xml
- 권한 분류 / 결과 모델 / 특수 권한 매핑 → core
- 요청 큐 / UI 훅 / 상태 보존 → xml

## 경계 조건

- 책임지는 범위: 권한 체크 / 결과 모델 / 요청 흐름 / 상태 보존
- 책임지지 않는 범위:
  - 설명 UI 렌더링 (onRationaleNeeded 훅으로만 제공)
  - 설정 화면 이동 UI (onNavigateToSettings 훅으로만 제공)
  - 권한 거부 후 비즈니스 로직 처리

## 문서 연계

- PRD: `simple_core/docs/feature/permissions/PRD.md`
- SPEC: `simple_core/docs/feature/permissions/SPEC.md`
- Implementation Plan: `simple_core/docs/feature/permissions/IMPLEMENTATION_PLAN.md`
