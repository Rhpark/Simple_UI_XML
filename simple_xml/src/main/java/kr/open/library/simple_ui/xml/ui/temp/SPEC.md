# 규격: RecyclerView 어댑터 temp

## 목적
- ui.temp 어댑터 설계의 규격(클래스/시그니처/정책)을 정의합니다.
- 단일 ViewType이 기본 흐름이며, 다중 ViewType은 별도 계열로 제공합니다.

## 범위
- RecyclerView.Adapter 계열
- ListAdapter 계열
- ViewHolder 계열
- DiffUtil 정책 및 Executor 주입
- 아이템 조작 API와 이벤트 정책

## 용어
- 단일 ViewType: 모든 아이템이 동일 레이아웃/뷰홀더를 사용
- 다중 ViewType: 아이템에 따라 레이아웃/뷰홀더가 달라짐
- Simple: 단일 ViewType 전용, 람다 기반 바인딩 제공
- Normal: 단일(BaseSingle)과 다중(BaseMulti)을 포함
- Root/Core: 큐/스레딩/이벤트/검증 정책을 제공하는 공통 코어

## 패키지 구조
- ui.temp.base
- ui.temp.base.list
- ui.temp.base.list.diffcallback
- ui.temp.base.normal
- ui.temp.viewholder
- ui.temp.viewholder.root
- ui.temp.viewholder.normal
- ui.temp.viewholder.binding
- ui.temp.list
- ui.temp.list.normal
- ui.temp.list.binding
- ui.temp.list.binding.databind
- ui.temp.list.binding.viewbind
- ui.temp.normal
- ui.temp.normal.normal
- ui.temp.normal.binding
- ui.temp.normal.binding.databind
- ui.temp.normal.binding.viewbind

## 코어/베이스 계열
- `RootRcvAdapterCore`
- `RootListAdapterCore`
- `BaseRcvNormalAdapterCore`
- `BaseRcvDataBindingAdapterCore`
- `BaseRcvViewBindingAdapterCore`
- `BaseListNormalAdapterCore`
- `BaseListDataBindingAdapterCore`
- `BaseListViewBindingAdapterCore`

## 공통 헬퍼
- `AdapterOperationQueueCoordinator`
- `AdapterQueueMergeKeys`

## ViewHolder 계열
- `RootViewHolder`
- `BaseRcvViewHolder`
- `BaseBindingViewHolder`
- `BaseViewBindingViewHolder`
- `BaseDataBindingViewHolder`

## Adapter 네이밍 규칙
### RecyclerView.Adapter
- `BaseSingleAdapter`
- `SimpleSingleAdapter`
- `BaseMultiAdapter`
- `BaseSingleDataBindingAdapter`
- `SimpleSingleDataBindingAdapter`
- `BaseMultiDataBindingAdapter`
- `BaseSingleViewBindingAdapter`
- `SimpleSingleViewBindingAdapter`
- `BaseMultiViewBindingAdapter`

### ListAdapter
- `BaseSingleListAdapter`
- `SimpleSingleListAdapter`
- `BaseMultiListAdapter`
- `BaseSingleDataBindingListAdapter`
- `SimpleSingleDataBindingListAdapter`
- `BaseMultiDataBindingListAdapter`
- `BaseSingleViewBindingListAdapter`
- `SimpleSingleViewBindingListAdapter`
- `BaseMultiViewBindingListAdapter`

## 공통 정책
- 단일 ViewType이 기본 흐름입니다.
- Simple 계열은 단일 ViewType 전용이며 람다 기반 바인딩을 제공합니다.
- BaseSingle 계열은 단일 ViewType 전용입니다.
- BaseMulti 계열은 다중 ViewType 전용입니다.
- Simple/Normal 분류는 클래스명 규칙으로 구분합니다.
- 큐 처리 공통화: Root*는 결과 적용만 담당하고 큐 실행/드롭/오류 처리는 공통 코디네이터에서 처리합니다.
- 큐 병합: 동일 이름의 연속 연산은 머지 키로 병합하여 대기 큐 누적을 줄일 수 있습니다.
- ListAdapter는 DiffUtil이 필수이며 기본 비교는 `DefaultDiffCallback`입니다.
- RecyclerView.Adapter는 DiffUtil 기본 OFF이며 필요한 경우 `setDiffUtilEnabled(true)`로 활성화합니다.
- Diff 설정(diffCallback/diffExecutor/diffUtilEnabled)은 생성 시점에 결정합니다.
- RecyclerView.Adapter에서 Diff OFF일 때는 큐 연산 타입에 맞춰 notifyItem*를 사용하며, 범위가 불명확하면 전체 갱신으로 폴백합니다.
- RecyclerView.Adapter에서 Diff OFF + 메인 스레드 경로는 내부 리스트 재사용으로 할당을 줄입니다.
- Diff 계산은 백그라운드 Executor에서 수행되며 주입이 가능합니다.
- 아이템 변환은 기본적으로 백그라운드에서 수행되며, 필요 시 메인 스레드로 전환할 수 있습니다.
- 실패 원인은 `AdapterOperationFailure`로 전달할 수 있습니다.
- 큐 폭주 대응은 `maxPending`과 `QueueOverflowPolicy`로 제어합니다.
- 큐 디버깅 이벤트는 `QueueDebugEvent`로 전달할 수 있습니다.
- 공개 API는 메인 스레드 호출이 기본이며 AdapterThreadCheckMode(LOG/CRASH/OFF)로 위반 처리 정책을 선택합니다.
- 기본값: Debug=CRASH, Release=LOG.
- ListAdapter의 `submitList` 직접 호출은 경고하며 큐 API 사용을 권장합니다.

## 공통 API(큐 기반)
- `setItems(items: List<ITEM>, commitCallback: ((Boolean) -> Unit)? = null)`
- `setItemsLatest(items: List<ITEM>, commitCallback: ((Boolean) -> Unit)? = null)`
- `updateItems(commitCallback: ((Boolean) -> Unit)? = null, updater: (MutableList<ITEM>) -> Unit)`
- `getItems(): List<ITEM>`
- `addItem(item: ITEM, commitCallback: ((Boolean) -> Unit)? = null)`
- `addItemAt(position: Int, item: ITEM, commitCallback: ((Boolean) -> Unit)? = null)`
- `addItemsAt(position: Int, items: List<ITEM>, commitCallback: ((Boolean) -> Unit)? = null)`
- `addItems(items: List<ITEM>, commitCallback: ((Boolean) -> Unit)? = null)`
- `removeItem(item: ITEM, commitCallback: ((Boolean) -> Unit)? = null)`
- `removeAll(commitCallback: ((Boolean) -> Unit)? = null)`
- `replaceItemAt(position: Int, item: ITEM, commitCallback: ((Boolean) -> Unit)? = null)`
- `removeAt(position: Int, commitCallback: ((Boolean) -> Unit)? = null)`
- `moveItem(from: Int, to: Int, commitCallback: ((Boolean) -> Unit)? = null)`
- `setOperationExecutor(executor: Executor?)`
- `clearQueue()`
## 큐 정책/디버깅 API
- `setQueuePolicy(maxPending: Int, overflowPolicy: QueueOverflowPolicy)`
- `setQueueDebugListener(listener: ((QueueDebugEvent) -> Unit)?)`
- `setQueueMergeKeys(mergeKeys: Set<String>)` (예: `AdapterQueueMergeKeys.SET_ITEMS`)
- `setOnAdapterOperationFailureListener(listener: ((AdapterOperationFailureInfo) -> Unit)?)`

### 공통 API 동작
- 모든 연산은 큐에 적재되어 순차 처리됩니다.
- 반환값은 없으며 성공 여부는 commitCallback의 Boolean으로 전달됩니다.
- commitCallback은 메인 스레드에서 호출됩니다.

## Diff 설정 API(RecyclerView.Adapter 계열)
- `setDiffUtilEnabled(enabled: Boolean)`
- `setDiffUtilItemSame(callback: (old: ITEM, new: ITEM) -> Boolean)`
- `setDiffUtilContentsSame(callback: (old: ITEM, new: ITEM) -> Boolean)`
- `setDiffUtilChangePayload(callback: (old: ITEM, new: ITEM) -> Any?)`

## Diff 설정(ListAdapter 계열)
- 생성자에서 `diffCallback`, `diffExecutor`를 주입합니다.
- `diffExecutor`는 AsyncDifferConfig 지원 버전에서만 적용되며, 미지원/리플렉션 실패 시 기본 경로로 동작합니다.

## 클릭 이벤트
- `setOnItemClickListener((position: Int, item: ITEM, view: View) -> Unit)`
- `setOnItemLongClickListener((position: Int, item: ITEM, view: View) -> Unit)`
- 리스너는 onCreateViewHolder에서 1회 바인딩합니다.
- 하위 클래스는 createViewHolderInternal만 구현하며 attachClickListeners는 코어에서 처리합니다.
- 클릭 시점의 bindingAdapterPosition 기준으로 아이템을 조회합니다.
- 롱클릭 리스너가 없으면 false를 반환합니다.

## 다중 ViewBinding 규격
- `viewTypeProvider` + `inflateMap` 기반으로 viewType별 바인딩을 제공합니다.
- viewType별 서로 다른 ViewBinding 혼합이 가능합니다.
- 매핑 누락 시 로그 후 `IllegalArgumentException`을 발생시킵니다.

## 오류/검증 규칙
- position 범위 오류 시 로그 후 실패 처리합니다.
- viewType 매핑 누락 시 예외를 발생시킵니다.
- 메인 스레드 계약 위반은 AdapterThreadCheckMode 정책에 따라 로그/예외/무시 처리합니다.
- 병합된 연산은 `QueueDropReason.MERGED`로 전달됩니다.
- clearQueue로 드롭된 연산은 `QueueDropReason.CLEARED_BY_API`로 전달됩니다.

## 문서화
- README_* 반영은 코드 작성 완료 후 진행합니다.


