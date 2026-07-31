# Permission Refactor Plan

## 문서 정보
- 문서명: Permission Refactor Plan
- 작성일: 2026-03-08
- 수정일: 2026-03-08
- 대상 모듈: simple_xml
- 주 대상 패키지: `kr.open.library.simple_ui.xml.permissions.*`
- 연관 패키지:
  - `kr.open.library.simple_ui.xml.ui.components.fragment.binding`
  - `kr.open.library.simple_ui.xml.ui.view.recyclerview`
- 상태: Draft
- 문서 성격: 리팩토링/안정화 작업 계획서

## 문서 목적
- 현재 구현의 런타임 안정성 이슈와 공개 API 계약 불일치를 단계적으로 정리합니다.
- 수정 범위가 큰 작업을 마일스톤 단위로 분리해 회귀 위험을 낮춥니다.
- 각 단계별 수정 파일, 선결정 사항, 검증 기준을 고정해 작업 중 의사결정 흔들림을 줄입니다.

## 배경
- `BaseDataBindingFragment`는 `viewLifecycleOwner` 접근 시점이 너무 빨라 런타임 예외 가능성이 있습니다.
- 특수 권한 복귀 흐름은 `onResume` 경로에만 과도하게 의존해, 정상적인 `ActivityResult`가 와도 완료 시점이 불필요하게 늦습니다.
- `PermissionRequester(fragment)` 공개 생성자는 미래 사용자가 attach 이전에 호출할 가능성이 높지만, 현재 구현은 생성자 단계에서 `requireContext()`를 타서 즉시 크래시할 수 있습니다.
- rationale/settings 콜백 미응답 시 현재 큐가 영구 대기할 수 있습니다.
- `RecyclerScrollStateView` 리스너 저장 방식과 README 예시가 서로 맞지 않습니다.
- orphaned denied result 저장 정책은 현재 API 이름과 동작 의미가 완전히 일치하지 않습니다.

## 목표
- 런타임 예외 가능성이 있는 권한/바인딩 흐름을 우선 정리합니다.
- `PermissionRequester`를 미래 라이브러리 사용자 기준으로 더 안전한 공개 API로 정비합니다.
- `registerForActivityResult()` 등록 시점 제약을 깨지 않으면서, context 의존 객체 초기화 시점을 분리합니다.
- 각 마일스톤 완료 시점마다 컴파일 검증을 수행해 회귀 위치를 빠르게 좁힐 수 있게 합니다.
- 최종적으로 문서/KDoc/README와 실제 동작을 일치시킵니다.

## 비목표
- 권한 기능 범위를 새로 확장하지 않습니다.
- 새로운 특수 권한 타입을 추가하지 않습니다.
- 권한 기능과 무관한 UI 전반 리디자인은 이번 작업 범위에 포함하지 않습니다.
- 즉시 PRD/SPEC를 새로 작성하는 것을 이번 문서의 1차 목표로 두지 않습니다.

## 선결정 사항

### 1) 문서 전략
- 이번 문서는 `PRD.md`, `SPEC.md`, `IMPLEMENTATION_PLAN.md`를 대체하는 공식 기능 명세 문서가 아닙니다.
- 현재 단계에서는 `리팩토링 실행 계획`을 고정하는 데 집중합니다.
- 외부 공개 API 계약이나 사용자 가이드가 실제로 바뀌면 이후 `PRD/SPEC/README` 승격 여부를 따로 판단합니다.

### 2) PermissionRequester 공개 API 방향
- 선택: `B`
- 기준:
  - 생성은 언제든 안전해야 합니다.
  - lifecycle 제약은 `requestPermissions()` 같은 실행 시점에만 드러나야 합니다.
- 이유:
  - 아직 라이브러리 외부 사용자가 없어서 구조 개선 비용을 지금 감수하는 편이 유리합니다.
  - 생성자에서 lifecycle 때문에 즉시 크래시하는 공개 API는 장기적으로 사용성이 낮습니다.

### 3) 특수 권한 복귀 처리 정책
- `didLeaveForSpecial`는 유지합니다.
- `onStop()` fallback도 유지합니다.
- `handleActivityResultReturn()`에서 `Special`도 즉시 처리합니다.
- 기대 효과:
  - `ActivityResult`가 오는 기기에서는 불필요한 `onResume` 대기를 줄입니다.
  - `ActivityResult`가 늦거나 불안정한 기기에서는 기존 `onResume` fallback이 계속 동작합니다.

### 4) rationale/settings 미응답 정책
- 권장안: 타임아웃보다 `lifecycle 종료/취소 시 자동 cancel 또는 denied 처리`
- 이유:
  - 사용자 입력 속도를 시간으로 강제하면 오탐이 생기기 쉽습니다.
  - lifecycle 종료는 권한 UI를 더 이상 안전하게 유지할 수 없는 명확한 시점입니다.

### 5) orphaned denied result 정책
- 아직 최종 확정 전입니다.
- 기본 권장안:
  - `deniedResults`가 비어 있으면 orphaned denied result로 저장하지 않습니다.
- 이유:
  - 현재 API 이름은 `거부 결과` 보관 의미에 더 가깝습니다.
  - 성공 결과까지 같은 저장소에 섞으면 해석 비용이 커집니다.

## 영향 범위

### 직접 수정 대상
- `simple_xml/src/main/java/kr/open/library/simple_ui/xml/permissions/api/PermissionRequester.kt`
- `simple_xml/src/main/java/kr/open/library/simple_ui/xml/permissions/host/PermissionHostAdapter.kt`
- `simple_xml/src/main/java/kr/open/library/simple_ui/xml/permissions/flow/PermissionFlowProcessor.kt`
- `simple_xml/src/main/java/kr/open/library/simple_ui/xml/permissions/result/PermissionResultAggregator.kt`
- `simple_xml/src/main/java/kr/open/library/simple_ui/xml/permissions/coordinator/PermissionRequestCoordinator.kt`
- `simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/components/fragment/binding/BaseDataBindingFragment.kt`
- `simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/components/fragment/binding/ParentsBindingFragment.kt`
- `simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/view/recyclerview/RecyclerScrollStateView.kt`

### 문서 동기화 대상
- `docs/readme/README_RECYCLERVIEW.md`
- 권한 관련 README 또는 향후 추가될 `PRD.md`, `SPEC.md`, `IMPLEMENTATION_PLAN.md`

## 마일스톤

### M1. BaseDataBindingFragment lifecycleOwner 시점 수정
- 상태: 완료
- 목표:
  - `viewLifecycleOwner`를 뷰 생성 완료 이후에만 설정합니다.
- 대상 파일:
  - `simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/components/fragment/binding/BaseDataBindingFragment.kt`
  - `simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/components/fragment/binding/ParentsBindingFragment.kt`
- 작업 포인트:
  - 조기 `viewLifecycleOwner` 접근 제거
  - 실제 뷰 생성 완료 후 호출되는 내부 훅 또는 동등한 안전 지점으로 이동
- 완료 기준:
  - `BaseDataBindingFragment`에서 `binding.lifecycleOwner`가 `onViewCreated(view, savedInstanceState)` 이후 경로에서만 설정됩니다.
  - 생성 경로에서 `viewLifecycleOwner` 조기 접근이 사라집니다.
  - `:simple_xml:compileDebugKotlin`이 성공합니다.

### M2. 특수 권한 복귀 흐름 최적화
- 상태: 완료
- 목표:
  - `ActivityResult + onResume fallback` 조합으로 복귀 지연을 줄입니다.
- 대상 파일:
  - `simple_xml/src/main/java/kr/open/library/simple_ui/xml/permissions/flow/PermissionFlowProcessor.kt`
- 작업 포인트:
  - `didLeaveForSpecial` 유지
  - `onStop()` 유지
  - `handleActivityResultReturn()`에서 `Special` 처리 추가
  - 현재 동작과 맞지 않는 주석 정리
- 완료 기준:
  - 특수 권한이 `ActivityResult`로 돌아오면 즉시 완료 경로에 진입합니다.
  - `onResume` fallback은 계속 유효합니다.
  - `:simple_xml:compileDebugKotlin`이 성공합니다.

### M3. PermissionRequester 공개 API 계약 확정
- 상태: 완료
- 목표:
  - 생성 시점과 실행 시점의 lifecycle 계약을 명확히 분리합니다.
- 대상 파일:
  - `simple_xml/src/main/java/kr/open/library/simple_ui/xml/permissions/api/PermissionRequester.kt`
  - `simple_xml/src/main/java/kr/open/library/simple_ui/xml/permissions/host/PermissionHostAdapter.kt`
- 작업 포인트:
  - 생성은 안전
  - 실행 시점에만 lifecycle 요구
  - `registerForActivityResult()` 등록 시점 제약을 유지하는 설계 초안 고정
- 완료 기준:
  - `:simple_xml:compileDebugKotlin`이 성공합니다.
  - `registerForActivityResult()` 등록 객체는 eager 유지, context 의존 객체는 provider/lazy 경계로 분리한다는 결정이 코드와 문서에 명시됩니다.
  - M4에서 어떤 객체를 eager/provider/lazy로 둘지 문서에 명시됩니다.

### M4-1. PermissionRequester 구조 분리
- 상태: 완료
- 목표:
  - context 의존 객체 초기화 시점을 분리하기 위한 기반을 만듭니다.
- 대상 파일:
  - `simple_xml/src/main/java/kr/open/library/simple_ui/xml/permissions/api/PermissionRequester.kt`
  - `simple_xml/src/main/java/kr/open/library/simple_ui/xml/permissions/host/PermissionHostAdapter.kt`
- 작업 포인트:
  - `FragmentHost.context` eager field 제거
  - provider 기반 접근 경계 또는 동등한 지연 평가 구조 도입
  - 생성자 단계에서 `requireContext()`가 평가되지 않도록 보장
- 완료 기준:
  - attach 이전 `PermissionRequester(fragment)` 생성이 즉시 크래시를 만들지 않습니다.
  - `FragmentHost.context`가 eager field가 아니라 지연 조회 방식으로 동작합니다.
  - `:simple_xml:compileDebugKotlin`이 성공합니다.

### M4-2. 권한 흐름 구성요소 시그니처 반영
- 상태: 완료
- 목표:
  - launcher 등록 시점은 유지하면서, 실제 권한 처리 객체는 사용 시점 접근 구조로 변경합니다.
- 대상 파일:
  - `simple_xml/src/main/java/kr/open/library/simple_ui/xml/permissions/flow/PermissionFlowProcessor.kt`
  - `simple_xml/src/main/java/kr/open/library/simple_ui/xml/permissions/result/PermissionResultAggregator.kt`
  - `simple_xml/src/main/java/kr/open/library/simple_ui/xml/permissions/coordinator/PermissionRequestCoordinator.kt`
  - 필요 시 `PermissionRequester.kt`
- 작업 포인트:
  - `registerForActivityResult()` 등록 시점 보장
  - `PermissionClassifier`, `SpecialPermissionHandler`, `RolePermissionHandler`, `PermissionResultAggregator` 접근 구조 재정리
  - provider 기반 wiring 또는 동등한 구조 분리
- 완료 기준:
  - 공개 API 안전성과 launcher 등록 시점 제약이 동시에 만족됩니다.
  - `PermissionFlowProcessor`는 launcher 등록을 eager로 유지합니다.
  - classifier/handler/aggregator/coordinator는 provider 또는 lazy 경계를 통해 생성 시점 context 접근을 피합니다.
  - `:simple_xml:compileDebugKotlin`이 성공합니다.

### M4-3. 구조 개편 직후 중간 검증
- 상태: 완료
- 목표:
  - 큰 범위 변경 직후 회귀를 빠르게 확인합니다.
- 검증 항목:
  - `:simple_xml:compileDebugKotlin`
  - 가능하면 수동 시나리오 1회 이상
- 권장 수동 시나리오:
  - attach 전 `PermissionRequester(fragment)` 생성 후 attach 뒤 요청
  - 특수 권한 단일 요청 후 복귀
  - `ActivityResult`가 먼저 오거나 `onResume`이 먼저 오는 경로 모두 중복 처리 없이 종료되는지 확인

### M5. rationale/settings 미응답 영구 대기 방지
- 상태: 완료
- 목표:
  - 콜백 미응답이 직렬 큐 전체 정지로 이어지지 않게 합니다.
- 대상 파일:
  - `simple_xml/src/main/java/kr/open/library/simple_ui/xml/permissions/flow/PermissionFlowProcessor.kt`
- 작업 포인트:
  - 공통 decision 대기 helper 도입 여부 검토
  - lifecycle 종료/취소 시 자동 종료 정책 반영
  - 중복 `proceed/cancel` 호출 방어
- 완료 기준:
  - host 종료 또는 coroutine 취소 시 무기한 `await()`가 남지 않습니다.
  - `:simple_xml:compileDebugKotlin`이 성공합니다.

### M6. RecyclerScrollStateView listener 정책 정리
- 상태: 완료
- 목표:
  - 리스너 저장 정책과 외부 문서 예시를 일치시킵니다.
- 대상 파일:
  - `simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/view/recyclerview/RecyclerScrollStateView.kt`
  - `docs/readme/README_RECYCLERVIEW.md`
- 작업 포인트:
  - strong reference 유지 또는 weak 정책 문서화 중 하나를 명확히 선택
  - README 예시와 실제 GC 동작이 어긋나지 않게 수정
- 완료 기준:
  - API 동작과 예시 코드가 일치합니다.
  - `:simple_xml:compileDebugKotlin`이 성공합니다.

### M7-1. orphaned denied result 정책 확정
- 상태: 완료
- 목표:
  - 저장 대상 의미를 코드보다 먼저 확정합니다.
- 대상 파일:
  - `simple_xml/src/main/java/kr/open/library/simple_ui/xml/permissions/result/PermissionResultAggregator.kt`
  - 관련 API 문서/KDoc
- 작업 포인트:
  - 빈 결과 저장 여부 결정
  - 성공 결과 추적이 필요하면 별도 저장소 분리 여부 검토
- 완료 기준:
  - orphaned denied result의 의미가 문서와 코드에서 동일하게 설명됩니다.
  - 빈 `deniedResults`는 orphaned denied result로 저장하지 않는 정책이 명시됩니다.

### M7-2. orphaned denied result 정책 반영
- 상태: 완료
- 목표:
  - M7-1에서 확정한 정책을 코드에 반영합니다.
- 대상 파일:
  - `simple_xml/src/main/java/kr/open/library/simple_ui/xml/permissions/result/PermissionResultAggregator.kt`
- 완료 기준:
  - 정책과 코드 동작이 일치합니다.
  - `:simple_xml:compileDebugKotlin`이 성공합니다.

### M8. 최종 문서/테스트/통합 검증
- 목표:
  - 분산 수정 결과를 문서와 검증 증적으로 마무리합니다.
- 대상:
  - 변경된 KDoc/README/작업 문서
  - 필요 시 향후 추가될 `PRD.md`, `SPEC.md`, `IMPLEMENTATION_PLAN.md`
- 검증 항목:
  - `:simple_xml:compileDebugKotlin`
  - 가능하면 관련 단위 테스트 또는 Robolectric 테스트
  - 권한 수동 시나리오 재점검
- 완료 기준:
  - 주요 변경 흐름이 문서와 코드에서 일치합니다.
  - 회귀 확인 결과를 추적할 수 있습니다.

## 권장 작업 순서
1. `M1`
2. `M2`
3. `M3`
4. `M4-1`
5. `M4-2`
6. `M4-3`
7. `M5`
8. `M6`
9. `M7-1`
10. `M7-2`
11. `M8`

## 검증 전략

### 기본 원칙
- 각 마일스톤 종료 직후 최소 1회 컴파일 검증을 수행합니다.
- 범위가 큰 구조 변경은 최종 검증까지 미루지 않고 중간 검증을 별도로 둡니다.
- 권한 흐름은 가능한 한 컴파일 검증과 수동 시나리오를 함께 봅니다.

### 공통 검증 명령
- `./gradlew :simple_xml:compileDebugKotlin`

### 권장 추가 검증
- 권한 관련 테스트가 존재하면 해당 테스트를 함께 실행합니다.
- 수동 시나리오 예시:
  - attach 전 생성 후 attach 뒤 요청
  - 특수 권한 요청 후 복귀
  - rationale/settings UI에서 취소 또는 host 종료

## 리스크 및 대응
- `registerForActivityResult()` 등록 시점 위반 위험
  - 대응: launcher 등록 객체는 eager 유지, context 의존 객체만 분리
- provider 기반 구조 변경으로 인한 wiring 회귀 위험
  - 대응: M4를 세 단계로 분리하고 중간 검증 수행
- 정책 미확정 상태에서 코드만 먼저 변경할 위험
  - 대응: M5, M7은 구현 전에 정책을 먼저 확정
- 문서와 코드 괴리 재발 위험
  - 대응: M8에서 KDoc/README 동시 점검

## 문서 승격 기준
- 아래 중 하나에 해당하면 이번 문서를 기반으로 공식 문서를 추가 또는 갱신합니다.
  - 공개 API 계약이 실제로 변경되는 경우
  - 외부 사용자 가이드가 바뀌는 경우
  - 권한 결과 의미나 정책이 README 수준에서 설명되어야 하는 경우
- 승격 대상 문서 예시:
  - `simple_xml/docs/feature/permissions/PRD.md`
  - `simple_xml/docs/feature/permissions/SPEC.md`
  - `simple_xml/docs/feature/permissions/IMPLEMENTATION_PLAN.md`
  - 관련 README

## 산출물 기준
- 문서만 읽어도 작업 범위와 선결정 사항을 추적할 수 있어야 합니다.
- 마일스톤별 완료 기준과 검증 기준이 구분되어 있어야 합니다.
- 큰 구조 변경은 중간 검증 지점이 명확해야 합니다.
- 이후 실제 코드 수정 시 본 문서를 기준으로 순서를 재현할 수 있어야 합니다.
