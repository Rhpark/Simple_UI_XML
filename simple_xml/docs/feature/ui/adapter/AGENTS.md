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

## 문서 작성 시 주의사항
- 현재 로컬 확정 코드(as-is)를 기준으로 작성합니다.
- `normal`과 `list`를 같은 의미 체계로 설명하지 않습니다.
- `normal`은 즉시 반영, `list`는 큐 기반 종료 결과라는 차이를 명확히 씁니다.
- 예제 앱 설명에는 바인딩 시점 `position` 캡처 대신 `setOnItemClickListener` 사용 원칙을 반영합니다.
