# Adapter Feature PRD

## 문서 정보
- 문서명: Adapter Feature PRD
- 작성일: 2026-03-02
- 수정일: 2026-03-02
- 대상 모듈: simple_xml
- 패키지: `kr.open.library.simple_ui.xml.ui.adapter`
- 상태: 현행(as-is)
- 기준 코드:
  - `simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/adapter/common/`
  - `simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/adapter/normal/`
  - `simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/adapter/list/`
  - `simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/adapter/viewholder/`
  - `app/src/main/java/kr/open/library/simpleui_xml/recyclerview/new_/RecyclerViewActivity.kt`
  - `app/src/main/java/kr/open/library/simpleui_xml/recyclerview/new_/adapter/CustomListAdapter.kt`

## 배경/문제 정의
- RecyclerView 구현은 `Adapter`, `ViewHolder`, `DiffUtil`, 클릭 처리, payload, notify 호출, 리스트 상태 동기화까지 여러 책임이 한 번에 얽히기 쉽습니다.
- 단순 즉시 반영형 어댑터와 연속 변경이 잦은 `ListAdapter`는 실패 의미와 업데이트 비용이 다르지만, 호출부는 이를 쉽게 혼동합니다.
- `Header / Content / Footer` 섹션이 필요한 화면은 일반 content-only adapter보다 좌표 변환과 viewType 분기가 훨씬 복잡합니다.
- 클릭 이벤트를 바인딩 시점 `position`에 의존해 처리하면, `DiffUtil` 이동 후 stale position 버그가 생길 수 있습니다.
- 라이브러리 사용자는 “단순 목록”, “섹션 목록”, “대량 변경 목록” 중 어떤 계층을 선택해야 하는지 명확한 기준이 필요합니다.

## 제품 목표
- RecyclerView adapter 계층을 `normal`, `header/footer`, `list`로 분리해 사용 시나리오별 선택 기준을 명확히 한다.
- 공통 조회/쓰기/클릭 계약을 제공해 adapter 계열 간 사용 패턴을 일관되게 유지한다.
- `normal`은 즉시 반영 + 명시적 결과 모델, `list`는 큐 기반 종료 결과 모델로 실패 의미를 분리한다.
- `ListAdapter` 연속 변경을 내부 큐로 직렬화해 호출 시점과 실제 반영 시점의 충돌을 줄인다.
- 예제 앱에서도 올바른 사용 패턴을 보여주어 stale position 같은 대표 오용을 방지한다.

## 비목표
- Compose 전용 adapter 계층 제공
- Paging 3, ConcatAdapter, Selection 라이브러리 통합
- 복잡한 section diff 엔진 제공
- 애니메이션 프레임 단위 최적화

## 범위

### 포함 범위
- 공통 계약
  - `AdapterReadApi`
  - `AdapterWriteApi`
  - `AdapterClickable`
- 공통 유틸
  - `AdapterCommonClickData`
  - `AdapterCommonDataLogic`
  - `AdapterThreadGuard`
- normal 계층
  - `RootRcvAdapter`
  - `BaseRcvAdapter`
  - `HeaderFooterRcvAdapter`
  - `NormalAdapterResult`
- list 계층
  - `BaseRcvListAdapter`
  - `RcvListDiffUtilCallBack`
  - `AdapterOperationQueue`
  - `OperationQueueProcessor`
  - `QueuePolicy`
  - `ListAdapterResult`
  - `AdapterDropReason`
- simple adapter 계층
  - normal simple 계열
  - list simple 계열
- viewholder 계층
  - `RootViewHolder`
  - `BaseRcvViewHolder`
  - `BaseRcvDataBindingViewHolder`
  - `BaseRcvViewBindingViewHolder`
- 예제 앱 사용 방식
  - `RecyclerViewActivity`
  - `CustomListAdapter`

### 제외 범위
- RecyclerView LayoutManager 자체 구현
- Paging / RemoteMediator 통합
- Compose LazyColumn 대응
- feature별 개별 화면 설계 문서

## 사용자/이해관계자
- 라이브러리 사용자(앱 개발자): RecyclerView 보일러플레이트를 줄이고, 즉시 반영형과 큐 기반 반영형 중 적절한 계층을 선택해야 합니다.
- 라이브러리 유지보수자: `normal`/`list`의 책임 경계와 결과 모델을 일관되게 유지해야 합니다.
- QA/리뷰어: 클릭/이동/삭제/큐 드롭/실패 의미가 코드와 문서에서 동일해야 합니다.

## 핵심 시나리오
1. 개발자는 content-only 단순 목록에서 `SimpleRcvAdapter` 또는 `SimpleBindingRcvAdapter`를 사용해 즉시 notify 기반 목록을 구현합니다.
2. 개발자는 연속 추가/삭제/셔플이 빈번한 목록에서 `SimpleRcvListAdapter` 또는 `BaseRcvListAdapter`를 사용해 DiffUtil + queue 기반 업데이트를 사용합니다.
3. 개발자는 header/footer가 필요한 목록에서 `HeaderFooterRcvAdapter` 또는 `SimpleHeaderFooter...` 계열을 사용합니다.
4. 개발자는 클릭 처리 시 바인딩 시점 `position`을 직접 캡처하지 않고, 공통 클릭 API(`setOnItemClickListener`) 또는 클릭 시점 현재 position을 사용합니다.
5. 예제 앱은 `RecyclerViewActivity`에서 simple normal / simple list / custom list를 전환하며 add/remove/shuffle 동작을 보여줍니다.

## 요구사항 (우선순위)

### P0 (필수)
- FR-01 계층 분리
  - `normal`과 `list`는 별도 계층으로 유지한다.
  - `HeaderFooterRcvAdapter`는 `normal` 계층 위에서 section 책임을 추가한다.
- FR-02 공통 계약
  - 모든 adapter는 읽기 계약(`AdapterReadApi`)을 제공한다.
  - 쓰기 계약은 `AdapterWriteApi<ITEM, RESULT>`로 통일한다.
- FR-03 결과 모델
  - `normal`은 `NormalAdapterResult`
  - `list`는 `ListAdapterResult`
  - 두 결과 모델은 실패 의미를 혼동하지 않도록 분리한다.
- FR-04 list 큐 계약
  - `BaseRcvListAdapter`는 연속 변경을 내부 큐로 직렬화한다.
  - 검증 실패는 큐 등록 전에 즉시 `Rejected`로 전달한다.
  - 큐 종료 상태는 `Applied`, `Failed(Dropped/ExecutionError)`로 전달한다.
- FR-05 클릭 안전성
  - 공통 클릭 시스템은 클릭 시점 `bindingAdapterPosition` 기준으로 item/position을 해석한다.
  - 예제 앱은 stale position 패턴을 권장하지 않는다.

### P1 (중요)
- FR-06 section CRUD
  - `HeaderFooterRcvAdapter`는 header/footer CRUD와 섹션별 bind/viewType 훅을 제공한다.
- FR-07 simple adapter 라인업
  - normal, header/footer, list 각각 simple adapter 진입점을 제공한다.
- FR-08 main thread 계약
  - 공개 adapter API는 메인 스레드 호출을 전제로 한다.
  - 런타임 가드로 off-main-thread 호출을 빠르게 실패시킨다.

### P2 (개선)
- FR-09 queue 관찰성
  - 큐 overflow policy와 결과 모델을 통해 backpressure와 실패 상태를 이해할 수 있어야 한다.
- FR-10 문서/테스트 정합성
  - README, feature 문서, 테스트가 실제 adapter 공개 계약과 일치해야 한다.

## 비기능 요구사항
- 안정성
  - 잘못된 입력은 즉시 명시적 결과로 반환한다.
  - queue 실패/드롭은 콜백에 종료 상태로 전달한다.
- 성능
  - list 계층은 DiffUtil과 큐 기반 적용을 사용한다.
  - normal 계층은 단순성과 즉시성을 우선하되 범위 notify를 가능한 활용한다.
- 호환성
  - minSdk 28
  - AndroidX RecyclerView / DataBinding / ViewBinding 사용
- 유지보수성
  - common / normal / list / viewholder 패키지 역할을 분리 유지한다.

## 성공 지표
- 문서 정합성
  - PRD/SPEC/PLAN/README에서 adapter 계층과 결과 모델 설명이 일치한다.
- 기능 정합성
  - `:simple_xml:compileReleaseKotlin`, `:simple_xml:testDebugUnitTest`, `:simple_xml:apiCheck` 통과
- 사용성 정합성
  - 예제 앱에서 `Change` 후 아이템 클릭 시 stale position 삭제 버그가 재발하지 않는다.

## 수용 기준 (Acceptance Criteria)
1. 개발자는 content-only / section / queue 기반 목록 중 어떤 adapter를 선택해야 하는지 문서만으로 판단할 수 있다.
2. `BaseRcvAdapter`와 `BaseRcvListAdapter`의 결과 모델 차이를 호출부에서 혼동하지 않는다.
3. `HeaderFooterRcvAdapter`가 header/content/footer별 바인딩과 notify 계약을 명시적으로 제공한다.
4. 예제 앱이 클릭 시점 현재 position 기반 동작을 보여준다.

## 제약/전제
- adapter 공개 API는 메인 스레드에서 호출된다는 전제가 있습니다.
- `BaseRcvListAdapter`는 큐 기반 구조상 호출 시점과 실제 반영 시점이 다를 수 있습니다.
- `BaseRcvAdapter`는 DiffUtil이 아니라 즉시 notify 기반 구조입니다.

## 리스크 및 완화 전략
- 리스크: `normal`과 `list`의 의미 혼동
  - 완화: 결과 모델 분리, 문서 표기 통일
- 리스크: 바인딩 시점 `position` 캡처 오용
  - 완화: 예제 앱과 문서에서 `setOnItemClickListener` 사용 패턴을 기본 예시로 유지
- 리스크: queue 정책 이해 부족
  - 완화: `QueuePolicy`, `ListAdapterResult.Failed`를 SPEC/README에 명시
- 리스크: public API 변경 후 baseline 누락
  - 완화: `apiDump` / `apiCheck`를 feature 문서 검증 절차에 포함

## 관련 파일
- 코드
  - `simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/adapter/`
  - `app/src/main/java/kr/open/library/simpleui_xml/recyclerview/new_/RecyclerViewActivity.kt`
  - `app/src/main/java/kr/open/library/simpleui_xml/recyclerview/new_/adapter/CustomListAdapter.kt`
- 문서
  - `simple_xml/docs/feature/ui/adapter/SPEC.md`
  - `simple_xml/docs/feature/ui/adapter/IMPLEMENTATION_PLAN.md`
  - `docs/readme/README_RECYCLERVIEW.md`

## 테스트
- `simple_xml/src/test/java/kr/open/library/simple_ui/xml/robolectric/ui/adapter/`
- `simple_xml/src/test/java/kr/open/library/simple_ui/xml/unit/ui/adapter/`
