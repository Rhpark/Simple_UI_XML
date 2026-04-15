# Adapter Feature AGENT

## 역할
- 이 문서는 `ui/adapter` 기능 문서의 인덱스입니다.
- 구현/분석/수정 시 아래 문서를 순서대로 확인합니다.

## 문서 순서
1. `PRD.md`
2. `SPEC.md`
3. `IMPLEMENTATION_PLAN.md`
4. `docs/readme/README_RECYCLERVIEW.md`
5. `simple_xml/AGENTS.md`

## 실제 사용 경로(중요)
- `normal` 계층
  - `RootRcvAdapter` -> `BaseRcvAdapter` -> `HeaderFooterRcvAdapter`
- `list` 계층
  - `BaseRcvListAdapter`
  - 내부 큐: `AdapterOperationQueue`, `OperationQueueProcessor`, `QueuePolicy`
- 공통 계약
  - `AdapterReadApi`
  - `AdapterWriteApi`
  - `AdapterClickable`
  - `NormalAdapterResult`
  - `ListAdapterResult`

## 관련 코드 범위
- 공통
  - `simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/adapter/common/AdapterCommonClickData.kt`
  - `simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/adapter/common/AdapterCommonDataLogic.kt`
  - `simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/adapter/common/thread/AdapterThreadGuard.kt`
  - `simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/adapter/common/imp/AdapterReadApi.kt`
  - `simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/adapter/common/imp/AdapterWriteApi.kt`
  - `simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/adapter/common/imp/AdapterClickable.kt`
- normal
  - `simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/adapter/normal/root/RootRcvAdapter.kt`
  - `simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/adapter/normal/base/BaseRcvAdapter.kt`
  - `simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/adapter/normal/base/BaseRcvAdapterData.kt`
  - `simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/adapter/normal/headerfooter/HeaderFooterRcvAdapter.kt`
  - `simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/adapter/normal/headerfooter/HeaderFooterAdapterData.kt`
  - `simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/adapter/normal/result/NormalAdapterResult.kt`
- list
  - `simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/adapter/list/base/BaseRcvListAdapter.kt`
  - `simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/adapter/list/base/diffutil/RcvListDiffUtilCallBack.kt`
  - `simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/adapter/list/base/queue/AdapterOperationQueue.kt`
  - `simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/adapter/list/base/queue/OperationQueueProcessor.kt`
  - `simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/adapter/list/base/queue/QueuePolicy.kt`
  - `simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/adapter/list/base/result/ListAdapterResult.kt`
  - `simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/adapter/list/base/result/AdapterDropReason.kt`
- simple adapter
  - `simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/adapter/normal/simple/`
  - `simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/adapter/list/simple/`
- viewholder
  - `simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/adapter/viewholder/`

## 예제 앱 확인 경로
- `app/src/main/java/kr/open/library/simpleui_xml/recyclerview/new_/RecyclerViewActivity.kt`
- `app/src/main/java/kr/open/library/simpleui_xml/recyclerview/new_/adapter/CustomListAdapter.kt`

## 테스트 확인 경로
- `simple_xml/src/test/java/kr/open/library/simple_ui/xml/robolectric/ui/adapter/`
- `simple_xml/src/test/java/kr/open/library/simple_ui/xml/unit/ui/adapter/`

## 어댑터 선택 기준

- 단순 content 즉시 반영 → normal 패키지 (`BaseRcvAdapter`, `SimpleRcvAdapter` 계열)
- Header / Content / Footer 섹션 필요 → `HeaderFooterRcvAdapter`, `SimpleHeaderFooter...` 계열
- 빈번하거나 대량의 리스트 변경 → list 패키지 (`BaseRcvListAdapter`)
- list 패키지 어댑터는 `AdapterOperationQueue`를 내부 사용
- DiffUtil 사용 시 list 패키지 어댑터 활용
- ViewHolder는 viewholder 패키지 기본 클래스 참고

## 코드 경로 (구현 참고)

- RootRcvAdapter (공통 normal 기반): `simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/adapter/normal/root/RootRcvAdapter.kt`
- BaseRcvAdapter (일반 content 전용): `simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/adapter/normal/base/BaseRcvAdapter.kt`
- HeaderFooterRcvAdapter (section 지원): `simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/adapter/normal/headerfooter/HeaderFooterRcvAdapter.kt`
- BaseRcvListAdapter (ListAdapter 기반): `simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/adapter/list/base/BaseRcvListAdapter.kt`
- AdapterOperationQueue (BaseRcvListAdapter 내부): `simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/adapter/list/base/queue/AdapterOperationQueue.kt`
- DiffUtil 콜백: `simple_xml/src/main/java/kr/open/library/simple_ui/xml/ui/adapter/list/base/diffutil/RcvListDiffUtilCallBack.kt`

## 금지 패턴

- `normal`과 `list`를 동일한 의미 체계로 혼용하지 않는다
  - normal은 즉시 반영, list는 큐 기반 비동기 처리 — 둘의 실패 의미가 다름
- `position` 캡처 방식의 클릭 처리를 사용하지 않는다 → `setOnItemClickListener` 사용
- `BaseRcvListAdapter` 계열을 즉시 반영 용도로 사용하지 않는다
  - 즉시 반영이 필요하면 반드시 normal 패키지 사용

## 판단 기준

- 즉시 반영 필요 → normal 패키지 (`BaseRcvAdapter`, `SimpleRcvAdapter` 계열)
- Header / Content / Footer 섹션 필요 → `HeaderFooterRcvAdapter` 계열
- 빈번하거나 대량의 리스트 변경 → list 패키지 (`BaseRcvListAdapter`)
- 새 클래스 추가 시: UI 공통 계약(클릭/읽기/쓰기) → common 패키지 / normal 즉시 반영 → normal 패키지 / 큐 기반 → list 패키지

## 경계 조건

- 책임지는 범위: RecyclerView Adapter 계층 구현 (normal / list / common / viewholder)
- 책임지지 않는 범위:
  - Activity/Fragment UI 흐름 및 ViewModel 데이터 처리
  - RecyclerView LayoutManager 설정
  - 아이템 애니메이션 및 ItemDecoration

## 문서 작성 시 주의사항
- 현재 로컬 확정 코드(as-is)를 기준으로 작성합니다.
- `normal`과 `list`를 같은 의미 체계로 설명하지 않습니다.
- `normal`은 즉시 반영, `list`는 큐 기반 종료 결과라는 차이를 명확히 씁니다.
- 예제 앱 설명에는 바인딩 시점 `position` 캡처 대신 `setOnItemClickListener` 사용 원칙을 반영합니다.
