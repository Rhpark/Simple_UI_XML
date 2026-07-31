# Permission Plan Check Risk

## 문서 정보
- 문서명: Permission Plan Check Risk
- 작성일: 2026-03-22
- 대상 모듈: simple_xml, simple_core
- 대상 패키지:
  - `kr.open.library.simple_ui.xml.permissions.*`
  - `kr.open.library.simple_ui.core.permissions.model.*`
- 상태: Draft
- 연계 문서:
  - `simple_xml/docs/feature/permissions/REFACTOR_PLAN.md`
  - `simple_core/docs/feature/permissions/AGENTS.md`
  - `simple_core/docs/feature/permissions/PRD.md`
  - `simple_core/docs/feature/permissions/SPEC.md`
  - `simple_core/docs/feature/permissions/IMPLEMENTATION_PLAN.md`

## 목적
- `defer()` 기반 권한 결정 흐름 재설계를 실행 가능한 체크리스트로 정리합니다.
- 구현 전후 확인해야 할 정책, 검증 포인트, 리스크를 한 문서에서 관리합니다.
- 공개 API 변경과 내부 상태머신 변경이 함께 일어나는 작업을 단계적으로 통제합니다.

## 배경
- 현재 `rationale/settings` 콜백은 무응답 시 영구 대기로 이어질 수 있습니다.
- 임시 패치로 추가한 `cancelIfPending`는 현재 `cancel`과 실질 동작이 동일합니다.
- `PermissionRationaleRequest`, `PermissionSettingsRequest`는 함수 프로퍼티를 가진 `data class`라서 값 객체로서의 의미가 약합니다.
- `defer()` 이후의 대기 정책은 `onStop`, `onDestroy` 중 어디까지 자동 취소할지 명시적으로 설계할 필요가 있습니다.

## 목표
- `cancelIfPending` 없이도 안전한 비동기 decision 흐름을 제공합니다.
- `defer(policy)`로 lifecycle 기반 자동 취소 정책을 명확히 합니다.
- request 모델을 `data class`에서 handle 타입으로 재설계합니다.
- `awaitUserDecision()`를 상태머신으로 재구성해 무응답, 중복 호출, host 종료를 일관되게 처리합니다.

## 비목표
- 새로운 권한 종류를 추가하지 않습니다.
- Compose 전용 API를 추가하지 않습니다.
- UI 구현체(AlertDialog, BottomSheet, Snackbar 등)를 라이브러리에서 제공하지 않습니다.

## 선행 결정 사항
### 공개 계약
- `cancelIfPending`: 제거
- `defer(policy: PermissionDeferredPolicy = CANCEL_ON_STOP)`: 도입
- request 모델: `data class` 제거, handle 타입으로 전환

### 정책 enum
위치: `simple_core/src/main/java/kr/open/library/simple_ui/core/permissions/model/PermissionModels.kt`

```kotlin
enum class PermissionDeferredPolicy {
    CANCEL_ON_STOP,
    CANCEL_ON_DESTROY,
}
```

### 상태머신
위치: `simple_xml/src/main/java/kr/open/library/simple_ui/xml/permissions/flow/PermissionFlowProcessor.kt` 내부 `private enum`

```kotlin
private enum class PermissionDecisionState {
    ACTIVE,
    DEFERRED_CANCEL_ON_STOP,
    DEFERRED_CANCEL_ON_DESTROY,
    FINISHED,
}
```

## 단계별 체크리스트

### 1. 공개 계약 고정
- [ ] `PermissionDeferredPolicy` 이름과 값 확정
- [ ] `defer(policy)` 기본 정책을 `CANCEL_ON_STOP`으로 고정
- [ ] `cancelIfPending` 제거 결정 유지
- [ ] README/KDoc에 반영해야 할 공개 계약 변화 목록 정리

### 2. 모델 구조 변경 (선행 필수)
- [ ] `PermissionRationaleRequest`를 `data class`에서 handle 타입으로 변경
- [ ] `PermissionSettingsRequest`를 `data class`에서 handle 타입으로 변경
- [ ] 외부에서 필요한 것은 읽기 전용 데이터(`permissions`, `permission`)와 행동 메서드(`proceed`, `cancel`, `defer`)만 남기기
- [ ] handle 타입 전환 시 `defer` 기본 no-op 람다 제거 확인
- [ ] `equals`, `hashCode`, `copy`, `componentN`에 의존하는 사용처가 없는지 확인

### 3. 내부 상태머신 설계 반영
- [ ] `ACTIVE` 상태에서 콜백이 반환되면 auto-cancel
- [ ] `defer(CANCEL_ON_STOP)` 호출 시 `DEFERRED_CANCEL_ON_STOP` 전이
- [ ] `defer(CANCEL_ON_DESTROY)` 호출 시 `DEFERRED_CANCEL_ON_DESTROY` 전이
- [ ] rationale 경로와 settings 경로 모두 동일한 상태 전이 규칙을 적용
- [ ] `finish()` 호출 시 항상 `FINISHED` 전이
- [ ] `proceed`, `cancel` 중복 호출은 no-op 보장

### 4. Lifecycle observer 정리
- [ ] 단일 observer 사용
- [ ] 현재 onDestroy-only observer를 onStop/onDestroy 분기 observer로 교체
- [ ] `onStop`은 `DEFERRED_CANCEL_ON_STOP` 상태에서만 `finish(false)`
- [ ] `onDestroy`는 모든 미완료 상태에서 `finish(false)`
- [ ] `finish()` 내부에서 observer 제거
- [ ] coroutine cancellation에서도 observer 제거

### 5. PermissionFlowProcessor 구현 변경 (2 완료 후 진행)
- [ ] `awaitUserDecision()`를 상태머신 기반으로 재구현
- [ ] rationale 경로와 settings 경로 모두 새 handle 주입
- [ ] 임시 `deferred` Boolean과 `cancelIfPending` 전달 제거
- [ ] 새 정책과 충돌하는 기존 분기 제거

### 6. 테스트 보강
- [ ] rationale 경로 무응답 콜백 auto-cancel 테스트
- [ ] rationale 경로 `defer(CANCEL_ON_STOP)` 후 `onStop` cancel 테스트
- [ ] rationale 경로 `defer(CANCEL_ON_DESTROY)` 후 `onStop` 유지 테스트
- [ ] rationale 경로 `defer(CANCEL_ON_DESTROY)` 후 `onDestroy` cancel 테스트
- [ ] settings 경로 무응답 콜백 auto-cancel 테스트
- [ ] settings 경로 `defer(CANCEL_ON_STOP)` 후 `onStop` cancel 테스트
- [ ] settings 경로 `defer(CANCEL_ON_DESTROY)` 후 `onDestroy` cancel 테스트
- [ ] 기존 `cancelIfPending` 테스트 제거 또는 `defer(policy)` 기반 테스트로 교체
- [ ] `proceed`, `cancel` 중복 호출 no-op 테스트
- [ ] callback 예외 시 auto-cancel 테스트 (`safeCatch(defaultValue = false)` 경로 검증)

### 7. API 및 문서 정리
- [ ] `:simple_core:apiDump`로 `simple_core/api/simple_core.api` baseline 갱신
- [ ] 권한 관련 KDoc 정리
- [ ] 필요 시 README/REFACTOR_PLAN에 계약 변경 반영

### 8. 최종 검증
- [ ] `:simple_xml:testRobolectric --tests 'kr.open.library.simple_ui.xml.robolectric.permissions.flow.PermissionFlowProcessorRobolectricTest'`
- [ ] `:simple_core:apiCheck`
- [ ] 변경 파일 UTF-8 검수
- [ ] 깨짐 문자 `�` 검색
- [ ] 최종 diff 리뷰

## 리스크 및 대응

### 리스크 1. 공개 API 변경 폭 증가
- 설명: `data class` 제거와 `defer(policy)` 추가는 기존 사용 코드에 영향을 줄 수 있습니다.
- 대응:
  - 변경 전 public type 사용 패턴 검색
  - `simple_core.api` baseline 갱신 후 `apiCheck`로 명시적 검증
  - README/KDoc 동시 갱신

### 리스크 2. `CANCEL_ON_STOP` 기본값의 오탐
- 설명: 화면 잠금, 일시적 백그라운드, 멀티윈도우 전환에서도 자동 취소될 수 있습니다.
- 대응:
  - 기본값은 유지하되, `CANCEL_ON_DESTROY` 선택 경로를 함께 제공
  - KDoc에 정책 차이를 명확히 설명
  - `onStop` 테스트를 반드시 분리해 검증
  - 화면 잠금, 앱 백그라운드, 멀티윈도우 전환 시나리오를 수동으로 1회 이상 검증

### 리스크 3. 상태 전이 중복 처리 버그
- 설명: `onStop`, `onDestroy`, `proceed`, `cancel`이 거의 동시에 들어오면 중복 resume 위험이 있습니다.
- 대응:
  - `finish()`에서 `FINISHED` 가드 단일화
  - 모든 종료 경로를 `finish()`로만 수렴
  - 중복 호출 테스트 추가

### 리스크 4. 임시 패치와 새 설계 충돌
- 설명: 현재 들어가 있는 `defer`, `cancelIfPending` 임시 구현이 새 상태머신과 충돌할 수 있습니다.
- 대응:
  - 새 설계 반영 전 임시 분기 목록 먼저 확인
  - 반영 후 불필요 분기를 삭제
  - 관련 테스트를

### 리스크 5. 문서-구현 불일치 재발
- 설명: 공개 계약이 바뀌는데 README, KDoc가 따라오지 않으면 같은 문제가 반복됩니다.
- 대응:
  - 구현 직후 문서 변경 목록 점검
  - 마지막 단계에서 README, KDoc를 별도 체크리스트로 검토

## 검증 명령
```powershell
$env:GRADLE_USER_HOME='d:\Android Project\SimpleUI_XML\.gradle-user-home'
$env:ANDROID_USER_HOME='d:\Android Project\SimpleUI_XML\.android-user-home'
.\gradlew.bat :simple_xml:testRobolectric --tests 'kr.open.library.simple_ui.xml.robolectric.permissions.flow.PermissionFlowProcessorRobolectricTest'
.\gradlew.bat :simple_core:apiCheck
```

## 완료 기준
- 새 request 모델이 `data class`가 아니며, 공개 계약이 문서와 일치합니다.
- `awaitUserDecision()`가 상태머신으로 정리되어 무응답, 중복 호출, lifecycle 종료를 일관되게 처리합니다.
- `onStop`, `onDestroy` 정책이 테스트로 검증됩니다.
- `simple_core.api`와 실제 소스가 일치합니다.
- 변경 파일에 깨짐 문자가 없습니다.

## 구현 메모
- `cancel()`는 항상 idempotent여야 하므로, 별도 `cancelIfPending()` 없이도 안전하게 재호출 가능해야 합니다.
- `defer()`는 내부 상태를 전이시키는 메서드이지, no-op 기본 람다로 노출되면 안 됩니다.
- request 객체는 값 객체가 아니라 행동 객체이므로 `data class`보다 일반 class 또는 interface가 적합합니다.
