# Adapter Feature SPEC

## 문서 정보
- 문서명: Adapter Feature SPEC
- 작성일: 2026-03-02
- 수정일: 2026-07-30
- 대상 모듈: simple_xml
- 패키지: `kr.open.library.simple_ui.xml.ui.adapter`
- 수준: 구현 재현 가능 수준(Implementation-ready)
- 상태: 현행(as-is)

## 전제/참조
- 요구사항: `PRD.md`
- 기능 인덱스: `AGENTS.md`
- 사용자 가이드: `docs/readme/README_RECYCLERVIEW.md`
- 예제 앱:
  - `app/src/main/java/kr/open/library/simpleui_xml/recyclerview/new_/RecyclerViewActivity.kt`
  - `app/src/main/java/kr/open/library/simpleui_xml/recyclerview/new_/adapter/CustomListAdapter.kt`

## 아키텍처 개요

### 패키지 구성
- `common`
  - 계약: `AdapterReadApi`, `AdapterWriteApi`, `AdapterClickable`
  - 유틸: `AdapterCommonClickData`, `AdapterCommonDataLogic`, `AdapterThreadGuard`
- `normal`
  - `RootRcvAdapter`: 공통 normal 인프라
  - `BaseRcvAdapter`: content-only 즉시 반영
  - Header/Content/Footer: `BaseRcvAdapter` + sealed interface item 패턴
  - `NormalAdapterResult`
- `list`
  - `BaseRcvListAdapter`: DiffUtil + queue 기반 ListAdapter
  - `RcvListDiffUtilCallBack`
  - `AdapterOperationQueue`
  - `OperationQueueProcessor`
  - `QueuePolicy`
  - `ListAdapterResult`
  - `AdapterDropReason`
- `viewholder`
  - `RootViewHolder`
  - `BaseRcvViewHolder`
  - `BaseRcvDataBindingViewHolder`
  - `BaseRcvViewBindingViewHolder`

### 계층 관계

```kotlin
AdapterReadApi<ITEM>
AdapterWriteApi<ITEM, RESULT>
AdapterClickable<ITEM, VH>

RootRcvAdapter<ITEM, VH>
 └─ BaseRcvAdapter<ITEM, VH>

ListAdapter<ITEM, VH>
 └─ BaseRcvListAdapter<ITEM, VH>
```

## 공개 API 명세

### 공통 읽기 계약

```kotlin
public interface AdapterReadApi<ITEM> {
    fun getItems(): List<ITEM>
    fun getItemOrNull(position: Int): ITEM?
    fun getItemPosition(item: ITEM): Int
    fun getMutableItemList(): MutableList<ITEM>
}
```

### 공통 쓰기 계약

```kotlin
public interface AdapterWriteApi<ITEM, RESULT> {
    fun setItems(items: List<ITEM>, onResult: ((RESULT) -> Unit)? = null)
    fun addItem(item: ITEM, onResult: ((RESULT) -> Unit)? = null)
    fun addItemAt(position: Int, item: ITEM, onResult: ((RESULT) -> Unit)? = null)
    fun addItems(items: List<ITEM>, onResult: ((RESULT) -> Unit)? = null)
    fun addItemsAt(position: Int, items: List<ITEM>, onResult: ((RESULT) -> Unit)? = null)
    fun removeItem(item: ITEM, onResult: ((RESULT) -> Unit)? = null)
    fun removeItems(items: List<ITEM>, onResult: ((RESULT) -> Unit)? = null)
    fun removeRange(start: Int, count: Int, onResult: ((RESULT) -> Unit)? = null)
    fun removeAt(position: Int, onResult: ((RESULT) -> Unit)? = null)
    fun removeAll(onResult: ((RESULT) -> Unit)? = null)
    fun moveItem(fromPosition: Int, toPosition: Int, onResult: ((RESULT) -> Unit)? = null)
    fun replaceItemAt(position: Int, item: ITEM, onResult: ((RESULT) -> Unit)? = null)
}
```

### normal 결과 계약

```kotlin
public sealed interface NormalAdapterResult {
    data object Applied : NormalAdapterResult

    sealed interface Rejected : NormalAdapterResult {
        data object EmptyInput : Rejected
        data object InvalidPosition : Rejected
        data object ItemNotFound : Rejected
        data object NoMatchingItems : Rejected
    }
}
```

### list 결과 계약

```kotlin
public sealed interface ListAdapterResult {
    data object Applied : ListAdapterResult

    sealed interface Rejected : ListAdapterResult {
        data object EmptyInput : Rejected
        data object InvalidPosition : Rejected
        data object ItemNotFound : Rejected
        data object NoMatchingItems : Rejected
    }

    sealed interface Failed : ListAdapterResult {
        data class Dropped(val reason: AdapterDropReason) : Failed
        data class ExecutionError(val cause: Throwable?) : Failed
    }
}
```

## normal 계층 명세

### RootRcvAdapter
- 책임
  - normal adapter 공통 기반
  - `onCreateViewHolder`에서 클릭 리스너 1회 연결
  - 메인 스레드 가드
  - 결과 콜백 안전 실행
  - payload 기본 위임
  - `BaseRcvViewHolder` 캐시 정리

### BaseRcvAdapter
- 책임
  - content-only 리스트 저장/조회
  - 즉시 반영형 mutation
  - notify 기반 UI 갱신
- 데이터 저장소
  - `BaseRcvAdapterData`
- 특징
  - `setItems()`는 전체 교체 후 `notifyDataSetChanged()`
  - 단건/범위 추가/삭제는 범위 notify 사용
  - `removeItems()`는 best-effort 다건 제거

### Header/Content/Footer 패턴
- 별도의 Header/Footer adapter나 section CRUD API는 제공하지 않습니다.
- 호출부는 Header/Content/Footer를 하나의 sealed interface item 계층으로 정의합니다.
- `getContentItemViewType(position, item)`에서 item 타입을 viewType으로 변환합니다.
- `onBindViewHolder(holder, item, position)`에서 sealed item 타입별로 바인딩합니다.
- `setItems()`에는 Header/Content/Footer가 포함된 전체 목록을 전달합니다.
- position과 클릭 콜백은 전체 목록의 adapter index를 사용합니다.

## list 계층 명세

### BaseRcvListAdapter
- 책임
  - `ListAdapter` façade
  - DiffUtil 기반 submit
  - 연속 mutation을 queue에 직렬화
  - queue policy 설정
  - 클릭/롱클릭 공통 처리
- 특징
  - `setItems()`는 `clearQueueAndExecute(SetItemsOp)` 사용
  - 나머지 mutation은 `enqueueOperation(...)`
  - 검증 실패는 즉시 `Rejected`
  - 큐 종료 상태는 `Applied` 또는 `Failed`

### AdapterOperationQueue
- 책임
  - adapter 전용 operation 모델 제공
  - operation 실행 결과를 public result로 매핑 가능한 terminal state 제공
- terminal state
  - `Applied`
  - `Dropped(reason: QueueDropReason)`
  - `ExecutionError(cause: Throwable?)`
- operation 종류
  - `SetItemsOp`
  - `AddItemOp`
  - `AddItemAtOp`
  - `AddItemsOp`
  - `AddItemsAtOp`
  - `RemoveItemOp`
  - `RemoveAtOp`
  - `RemoveItemsOp`
  - `RemoveRangeOp`
  - `ClearItemsOp`
  - `MoveItemOp`
  - `ReplaceItemAtOp`

### OperationQueueProcessor
- 책임
  - queue pending 처리
  - overflow policy 적용
  - 메인 스레드 실행 스케줄링

### QueuePolicy
- `QueueOverflowPolicy`
  - `DROP_NEW`
  - `DROP_OLDEST`
  - `CLEAR_AND_ENQUEUE`
- `QueueDropReason`
  - `QUEUE_FULL_DROP_NEW`
  - `QUEUE_FULL_DROP_OLDEST`
  - `QUEUE_FULL_CLEAR`
  - `CLEARED_EXPLICIT`
  - `CLEARED_BY_API`

### AdapterDropReason
- `QueueDropReason`의 public 결과 모델용 공용 사유
- 현재 코드 기준 enum 값
  - `DROP_NEW`
  - `DROP_OLDEST`
  - `CLEAR_AND_ENQUEUE`
  - `CLEARED_EXPLICIT`
  - `CLEARED_BY_API`

## 공통 유틸 명세

### AdapterCommonDataLogic
- 역할
  - mutation 검증 전담
- 실패 모델
  - `EMPTY_INPUT`
  - `INVALID_POSITION`
  - `ITEM_NOT_FOUND`
  - `NO_MATCHING_ITEMS`
- 제공 검증
  - `validateAddItemAt`
  - `validateAddItems`
  - `validateAddItemsAt`
  - `validateRemoveItem`
  - `validateRemoveItems`
  - `validateRemoveItemAt`
  - `validateRemoveRange`
  - `validateMoveItem`
  - `validateReplaceItemAt`
  - `isPositionValid`

### AdapterCommonClickData
- 클릭 리스너를 ViewHolder 생성 시 1회 부착
- 클릭 시점에 `bindingAdapterPosition` 확인
- `positionMapper`, `itemProvider`로 현재 position/item 해석

## 클릭 처리 명세

### 권장 패턴
- adapter 바인딩 람다에서는 뷰 바인딩만 수행
- 클릭은 `setOnItemClickListener`, `setOnItemLongClickListener`를 사용

### 비권장 패턴

```kotlin
root.setOnClickListener { currentRemoveAtAdapter(position) }
```

- 이유
  - 바인딩 시점 `position`을 캡처함
  - `DiffUtil` 이동 후 stale position 버그 가능

### 예제 앱 적용 패턴

```kotlin
simpleListAdapter.setOnItemClickListener { position, _, _ ->
    currentRemoveAtAdapter(position)
}
```

## 예제 앱 명세

### RecyclerViewActivity
- 제공 시나리오
  - simple normal adapter 전환
  - simple list adapter 전환
  - custom list adapter 전환
  - add / clear / shuffle / click remove
- 주요 포인트
  - `Change` 버튼은 실제로 `shuffled()`를 호출하는 reorder 시나리오
  - `ListAdapter`에서도 클릭 삭제가 올바르도록 공통 클릭 API 사용

### CustomListAdapter
- `BaseRcvListAdapter` 상속 예시
- `RcvListDiffUtilCallBack` 주입
- `onBindViewHolder(holder, item, position)`만 구현

## 스레드/동시성
- 공개 adapter API는 메인 스레드 호출 전제
- `normal`
  - 즉시 실행
  - 결과 콜백도 같은 흐름에서 실행
- `list`
  - 호출 시점 검증 후 queue 등록
  - 적용/드롭/실패는 queue 종료 시점에 전달

## 성능/비용 정책
- `BaseRcvAdapter`
  - 단순성/즉시성을 우선
  - 전체 교체는 `notifyDataSetChanged()`
- Header/Content/Footer
  - `BaseRcvAdapter.setItems()`의 전체 목록 교체 계약을 사용
  - 단건·범위 변경 시에도 전체 sealed item 목록의 index를 사용
- `BaseRcvListAdapter`
  - DiffUtil + submitList + queue 직렬화
  - 연속 변경 안정성이 중요할 때 사용
  - 각 mutation은 하나의 큐 연산으로 처리되고 `submitList()`로 반영
  - 대부분의 단건 mutation은 현재 리스트의 복사본을 생성한 뒤 변경을 적용
  - 단건 mutation을 반복하면 리스트 복사와 DiffUtil 적용도 큐 순서대로 반복
  - 여러 변경을 하나의 종료 결과로 처리할 수 있으면 `addItems`, `addItemsAt`, `removeItems`, `removeRange`, `setItems` 같은 일괄 API를 우선 사용
  - 각 변경의 순서나 종료 결과가 개별적으로 필요하면 단건 API 사용
  - 현행 계약은 큐 연산을 자동 병합하지 않으며 개별 `onResult` 의미를 유지

## 테스트 명세(현행)
- Robolectric
  - `simple_xml/src/test/java/kr/open/library/simple_ui/xml/robolectric/ui/adapter/`
- Unit
  - `simple_xml/src/test/java/kr/open/library/simple_ui/xml/unit/ui/adapter/`
- 검증 축
  - simple adapter 바인딩
  - normal/list mutation
  - sealed interface 기반 Header/Content/Footer 동작
  - queue 정책/종료 상태

## 알려진 구조적 이슈
- `BaseRcvListAdapter`는 façade와 mutation engine을 함께 들고 있어 파일 크기가 큽니다.
- Header/Content/Footer 조립과 content 개수 계산은 호출부 책임이므로 예제 패턴을 명확히 유지해야 합니다.
- `AdapterWriteApi`가 public 공통 계약이므로, 향후 normal/list 의미 차이가 더 벌어지면 재평가가 필요합니다.

## 추적 매트릭스
- `normal` 즉시 반영
  - 코드: `normal/base/BaseRcvAdapter.kt`
  - 문서: PRD/SPEC/README
- `header/footer` 표현 계약
  - 코드: `normal/base/BaseRcvAdapter.kt`, 앱 `RecyclerViewActivity.kt`
  - 문서: PRD/SPEC/README
- `list` queue 종료 결과
  - 코드: `list/base/BaseRcvListAdapter.kt`, `list/base/queue/`
  - 문서: PRD/SPEC/README
- 예제 앱 클릭 안전성
  - 코드: `RecyclerViewActivity.kt`
  - 문서: PRD/SPEC/PLAN
