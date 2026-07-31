# Permission Runtime Requestability Refactor Plan

## 문서 정보
- 문서명: Permission Runtime Requestability Refactor Plan
- 작성일: 2026-03-15
- 대상 모듈: simple_core, simple_xml
- 시작 기준 패키지: `kr.open.library.simple_ui.core.permissions.*`
- 연계 패키지: `kr.open.library.simple_ui.xml.permissions.*`
- 상태: In Progress

## 목적
- `supported`, `granted`, `runtime requestable` 의미를 분리한다.
- `PermissionClassifier`, `PermissionExtensions`, `PermissionRequester`, `PermissionFlowProcessor` 간 권한 판정 기준을 일관되게 맞춘다.
- 현재 구조에서 `signature/privileged` 권한이 런타임 요청 흐름으로 흘러갈 수 있는 위험을 제거한다.

## 배경
- 현재 `PermissionClassifier.isSupported()`는 SDK 지원 여부만 판단한다.
- 현재 `PermissionExtensions.hasPermission()`는 권한 보유 여부를 판단한다.
- 현재 `PermissionClassifier.classify()`는 `ROLE`, `SPECIAL`이 아니면 모두 `RUNTIME`으로 분류한다.
- 이 구조에서는 manifest에 선언된 `normal`, `signature`, `privileged` 권한도 `RUNTIME` 흐름으로 들어갈 수 있다.
- 특히 `signature/privileged` 권한은 일반 앱에서 보유 또는 요청이 불가능하므로, `NOT_SUPPORTED`로 분기하는 별도 정책이 필요하다.

## 비목표
- 특수 권한(`SPECIAL`) 또는 역할(`ROLE`) 분류 정책 자체를 재설계하지 않는다.
- `PermissionDeniedType` 공개 모델을 대규모로 변경하지 않는다.
- UI 훅, 상태 저장 키, 직렬 처리 구조를 이번 변경에서 직접 개편하지 않는다.

## 핵심 설계 원칙
1. `isSupported()`는 SDK 가용성 판단만 담당한다.
2. `hasPermission()`는 현재 보유 여부 판단만 담당한다.
3. `runtime requestable`은 별도 축으로 추가한다.
4. `dangerous`만 실제 런타임 다이얼로그 요청 대상으로 본다.
5. `normal`은 `GRANTED_BY_DEFAULT`로 처리한다.
6. `signature/privileged/기타 비요청형 권한`은 `NOT_SUPPORTED`로 처리한다.
7. `SDK 미지원`은 기존 정책대로 `GRANTED`를 유지한다.

## 권장 구조

### core
- `PermissionClassifier`
  - `isSupported(permission: String): Boolean`
  - `classify(permission: String): PermissionType`
  - `getRuntimeRequestability(permission: String): RuntimePermissionRequestability`

- `PermissionExtensions`
  - `hasPermission(permission: String): Boolean`
  - `getPermissionProtectionLevel(permission: String): Int`
  - `getPermissionBaseProtectionLevel(permission: String): Int`

### xml
- `PermissionRequester.resolveRuntimePermission()`
  - `classifier.getRuntimeRequestability(permission)` 기반으로 선분기

- `PermissionFlowProcessor.processRuntimePermissions()`
  - 일반 요청 경로와 복원 경로 모두 동일 정책 적용

## 새로 추가할 모델
```kotlin
enum class RuntimePermissionRequestability {
    REQUESTABLE,
    GRANTED_BY_DEFAULT,
    NOT_SUPPORTED,
}
```

## 분기 정책

### 1. SDK 기준
- `classifier.isSupported(permission) == false`
  - 현재 정책 유지
  - 결과: `GRANTED`

### 2. Runtime requestability 기준
- `REQUESTABLE`
  - `hasPermission() == true`면 `GRANTED`
  - 아니면 실제 런타임 요청

- `GRANTED_BY_DEFAULT`
  - 결과: `GRANTED`

- `NOT_SUPPORTED`
  - 결과: `NOT_SUPPORTED`

## 마일스톤

### M1. 정책 문서화
- 상태: 완료
- 대상 파일:
  - `simple_core/docs/feature/permissions/SPEC.md`
  - `simple_core/docs/feature/permissions/IMPLEMENTATION_PLAN.md`
  - `simple_core/docs/feature/permissions/REFACTOR_PLAN.md`
- 완료 기준:
  - `supported`, `granted`, `runtime requestable` 용어 정의가 문서에 명시된다.
  - `normal -> GRANTED_BY_DEFAULT`, `signature/privileged -> NOT_SUPPORTED` 정책이 문서에 반영된다.

### M2. core 모델/분류 API 추가
- 상태: 완료
- 대상 파일:
  - `simple_core/src/main/java/kr/open/library/simple_ui/core/permissions/classifier/PermissionClassifier.kt`
  - 필요 시 `simple_core/src/main/java/kr/open/library/simple_ui/core/permissions/model/PermissionModels.kt`
- 완료 기준:
  - `RuntimePermissionRequestability`가 추가된다.
  - `PermissionClassifier.getRuntimeRequestability()`가 구현된다.

### M3. core 보조 API 정합성 유지
- 상태: 완료
- 대상 파일:
  - `simple_core/src/main/java/kr/open/library/simple_ui/core/permissions/extensions/PermissionExtensions.kt`
- 완료 기준:
  - `hasPermission()`과 `getPermissionBaseProtectionLevel()` 설명이 새 정책과 충돌하지 않는다.
  - 기존 `isSupported()` 의미를 침범하지 않는다.

### M4. xml 런타임 사전 판정 변경
- 상태: 완료
- 대상 파일:
  - `simple_xml/src/main/java/kr/open/library/simple_ui/xml/permissions/api/PermissionRequester.kt`
- 완료 기준:
  - `resolveRuntimePermission()`가 `classifier.getRuntimeRequestability()`를 사용한다.
  - `GRANTED_BY_DEFAULT -> GRANTED`, `NOT_SUPPORTED -> NOT_SUPPORTED`가 반영된다.

### M5. xml 복원 경로 정책 통일
- 상태: 완료
- 대상 파일:
  - `simple_xml/src/main/java/kr/open/library/simple_ui/xml/permissions/flow/PermissionFlowProcessor.kt`
- 완료 기준:
  - 일반 요청 경로와 복원 경로가 같은 requestability 정책을 사용한다.
  - `signature/privileged` 권한이 복원 시 런타임 요청 흐름으로 가지 않는다.

### M6. 테스트 보강
- 상태: 완료
- 대상 파일:
  - `simple_core/src/test/java/kr/open/library/simple_ui/core/robolectric/permissions/extentions/PermissionExtensionsRobolectricTest.kt`
  - `simple_core` 또는 `simple_xml` 권한 분류/흐름 테스트
- 필수 케이스:
  - dangerous -> `REQUESTABLE`
  - normal -> `GRANTED_BY_DEFAULT`
  - signature -> `NOT_SUPPORTED`
  - signature|privileged -> `NOT_SUPPORTED`
  - SDK 미지원 -> `GRANTED`
  - restore 경로 동일 정책

### M7. 문서/KDoc 정리
- 상태: 진행 중
- 대상 파일:
  - `simple_core/docs/feature/permissions/SPEC.md`
  - `simple_core/docs/feature/permissions/IMPLEMENTATION_PLAN.md`
  - 관련 KDoc
- 완료 기준:
  - 문서와 구현 용어가 일치한다.
  - `non-dangerous는 granted` 같은 과거 표현이 제거된다.

### M8. 검증
- 상태: 미시작
- 검증 순서:
  1. `:simple_core:compileDebugKotlin`
  2. `:simple_xml:compileDebugKotlin`
  3. 관련 unit/Robolectric 선택 실행
- 완료 기준:
  - 컴파일 통과
  - 관련 테스트 통과

## 구현 우선순위
`M1 -> M2 -> M3 -> M4 -> M5 -> M6 -> M7 -> M8`

## 주의사항
- `isSupported()`에 protection level 정책을 섞지 않는다.
- `REQUESTABLE` 판정은 `PermissionClassifier`에 둬서 응집도를 유지한다.
- `normal` 권한이 manifest에 선언되어 requester 입력으로 들어오는 경우를 실제 시나리오로 본다.
- 사용자가 아직 없으므로, 지금이 정책 분리와 의미 정리를 수행하기 가장 좋은 시점이다.

## 기대 효과
- 권한 판정 의미가 명확해진다.
- `simple_core`와 `simple_xml` 간 분기 불일치가 줄어든다.
- `signature/privileged` 권한의 오동작 가능성을 제거할 수 있다.
- 향후 권한 정책 확장 시 `supported`와 `requestable`을 독립적으로 다룰 수 있다.

## 진행 메모
- `M1`: `SPEC.md`, `IMPLEMENTATION_PLAN.md`, `REFACTOR_PLAN.md`에 `supported / granted / runtime requestable` 정의와 정책을 반영했다.
- `M2`: `PermissionClassifier`에 `RuntimePermissionRequestability`와 `getRuntimeRequestability()`를 추가했다.
- `M3`: `PermissionExtensions.hasPermission()` KDoc을 `normal만 granted by design` 정책에 맞게 정리했다.
- `M4`: `PermissionRequester.resolveRuntimePermission()`가 `getRuntimeRequestability()` 기반으로 선분기하도록 변경했다.
- `M5`: `PermissionFlowProcessor.processRuntimePermissions()`의 일반/복원 경로가 같은 requestability 정책을 사용하도록 정리했다.
- `M6`: `PermissionClassifierRobolectricTest`, `PermissionRequesterRobolectricTest`에 requestability/restore 경로 회귀 테스트를 추가했다.
