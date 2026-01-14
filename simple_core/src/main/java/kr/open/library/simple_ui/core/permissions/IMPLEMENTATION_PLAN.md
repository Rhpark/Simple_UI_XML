# PermissionRequester Implementation Plan

## 문서 정보
- 문서명: PermissionRequester Implementation Plan
- 작성일: 2026-01-13
- 대상 모듈: simple_core, simple_xml
- 패키지: kr.open.library.simple_ui.core.permissions.*, kr.open.library.simple_ui.xml.permissions.*
- 상태: Draft

## 목표
- SPEC 기반으로 구현 순서와 범위를 명확히 한다.
- 단위 테스트 + Robolectric 테스트를 포함한다.
- 권한 요청 실패는 예외 대신 결과로 반환한다.
- Activity/Fragment 의존/비의존 영역을 분리한다.
  - 쉽게 말해, Activity/Fragment 없이도 되는 로직은 core에 두고, Activity/Fragment가 필요한 요청/라이프사이클 로직은 xml에 둔다.

## 구현 범위
- core(비의존)
  - 모델/상수/특수 타입 정의
  - 권한 분류기, Role/특수 권한 핸들러
  - 권한 보유 여부 확장 함수
  - 큐/병합 정책
- xml(의존)
  - PermissionRequester 공개 API 및 계약(Interface)
  - Activity/Fragment 호스트 어댑터
  - 런타임/특수/Role 요청 흐름 처리
  - 요청 직렬화/거부 결과 집계/상태 저장
- Bundle 기반 상태 보존(프로세스 복원 포함, 외부 savedInstanceState 전달)
- 런타임/특수/Role 처리 흐름 구현
- EMPTY_REQUEST, MANIFEST_UNDECLARED, NOT_SUPPORTED, FAILED_TO_LAUNCH_SETTINGS, LIFECYCLE_NOT_READY 처리

## 구현 순서
1. 모델/상수 정의 (core)
   - PermissionDeniedItem/PermissionDeniedType
   - PermissionDecisionType
   - OrphanedDeniedRequestResult, Rationale/Settings 요청 모델
   - PermissionConstants / PermissionSpecialType
2. 분류기/핸들러/확장 (core)
   - PermissionClassifier
   - SpecialPermissionHandler / RolePermissionHandler
   - PermissionExtensions
3. 큐/병합 (core)
   - PermissionQueue 구현
   - 동일 권한 병합, FIFO 처리
4. 상태 저장/복원 (xml)
   - PermissionStateStore (Bundle 사용)
   - 요청 이력/큐/진행 상태 직렬화
   - orphaned 거부 결과 캐시/회수 처리
5. 결과 집계 (xml)
   - PermissionResultAggregator
6. 호스트/런타임 처리 (xml)
   - PermissionHostAdapter
   - RuntimePermissionHandler
7. 요청 흐름/조정 (xml)
   - PermissionFlowProcessor
   - PermissionRequestCoordinator
8. 공개 API/계약 (xml)
   - PermissionRequester
   - PermissionRequestInterface
9. 테스트
   - 단위 테스트: 큐/특수 타입 등 비의존 로직
   - Robolectric: Activity/Fragment 요청 흐름, 계약 테스트

## 실패 처리 정책
- FAILED_TO_LAUNCH_SETTINGS는 예외 없이 결과로만 반환한다.
- EMPTY_REQUEST는 permission 빈 문자열로 1건 반환한다.
- MANIFEST_UNDECLARED는 거부 목록에 포함하고 나머지는 정상 처리한다.
- NOT_SUPPORTED는 OS/Role/특권 권한 미지원으로 반환한다.
- LIFECYCLE_NOT_READY는 Lifecycle 상태가 요청/실행에 적합하지 않을 때 반환한다.

## 테스트 범위
### 단위 테스트
- PermissionQueueTest
- PermissionSpecialTypeUnitTest

### Robolectric 테스트
- PermissionRequesterRobolectricTest
- PermissionRequestInterfaceRobolectricTest
- PermissionConstantsRobolectricTest
- PermissionExtensionsRobolectricTest

## 테스트 방법(수동 시나리오)
- 특수 권한 단일 요청: SYSTEM_ALERT_WINDOW를 OFF로 만든 뒤 요청하고, 설정 화면 복귀 후에만 onDeniedResult가 호출되는지 확인한다.
- 런타임+특수 조합: WRITE_EXTERNAL_STORAGE는 이미 승인된 상태에서 SYSTEM_ALERT_WINDOW를 OFF로 만든 뒤 요청하고, 설정 화면 복귀 후에 결과가 확정되는지 확인한다.
- 홈 버튼 이탈: 설정 화면에서 홈으로 나갔다가 복귀했을 때, 복귀 시점 상태 기준으로 onDeniedResult가 호출되는지 확인한다.
- 특수 권한 허용 후 복귀: 설정에서 허용한 뒤 복귀하면 deniedResults가 비어있는지 확인한다.
- 테스트 화면 경로: app/src/main/java/kr/open/library/simpleui_xml/permission/PermissionsActivity.kt

## 리스크/체크리스트
- ActivityResult 등록 시점 준수
- SavedState 키 충돌 방지
- 회전/프로세스 복원 시 결과 유실 방지
- 특수 권한 설정 화면 이동 실패 처리
- restoreState 호출 시점(요청 전 1회) 준수
