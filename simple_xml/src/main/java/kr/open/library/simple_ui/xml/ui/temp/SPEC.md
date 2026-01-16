# SPEC: RecyclerView Adapter Temp

## 목적
- ui.temp 어댑터 설계의 정확한 규격(클래스/시그니처/정책)을 정의합니다.
- 단일 ViewType이 가장 쉽게 쓰이도록 기본 흐름을 설계하고, 다중 ViewType은 별도 계열로 제공합니다.

## 범위
- RecyclerView.Adapter 계열
- ListAdapter 계열
- ViewHolder 계열
- DiffUtil 정책(옵션)
- 아이템 조작 API(공통)

## 용어
- 단일 ViewType: 모든 아이템이 동일 레이아웃/뷰홀더를 사용
- 다중 ViewType: 아이템에 따라 레이아웃/뷰홀더가 달라짐
- Simple: 단일 ViewType 전용, 람다 기반 사용
- Normal: 단일(BaseSingle)과 다중(BaseMulti)을 모두 포함, 확장/오버라이드 중심

## 네이밍 규칙(확정)
### RecyclerView.Adapter
- BaseSingleDataBindingAdapter
- BaseSingleViewBindingAdapter
- BaseSingleAdapter
- SimpleSingleDataBindingAdapter
- SimpleSingleViewBindingAdapter
- SimpleSingleAdapter
- BaseMultiDataBindingAdapter
- BaseMultiViewBindingAdapter
- BaseMultiAdapter

### ListAdapter
- BaseSingleDataBindingListAdapter
- BaseSingleViewBindingListAdapter
- BaseSingleListAdapter
- SimpleSingleDataBindingListAdapter
- SimpleSingleViewBindingListAdapter
- SimpleSingleListAdapter
- BaseMultiDataBindingListAdapter
- BaseMultiViewBindingListAdapter
- BaseMultiListAdapter

## 패키지 구조(정렬 기준)
- ui.temp.base
- ui.temp.viewholder
- ui.temp.list.binding.simple.databind
- ui.temp.list.binding.simple.viewbind
- ui.temp.list.binding.normal.databind
- ui.temp.list.binding.normal.viewbind
- ui.temp.list.normal.simple
- ui.temp.list.normal.normal
- ui.temp.normal.binding.simple.databind
- ui.temp.normal.binding.simple.viewbind
- ui.temp.normal.binding.normal.databind
- ui.temp.normal.binding.normal.viewbind
- ui.temp.normal.normal.simple
- ui.temp.normal.normal.normal

## 공통 정책
- 단일 ViewType이 기본 흐름이며, 가장 쉽게 사용할 수 있어야 합니다.
- Simple 계열은 단일 ViewType 전용입니다.
- Normal 계열은 단일(BaseSingle)과 다중(BaseMulti)을 모두 포함합니다.
- BaseSingle 계열은 단일 ViewType 전용이며 Simple이 이를 상속합니다.
- BaseMulti 계열은 다중 ViewType 전용입니다.
- RecyclerView.Adapter 계열 DiffUtil 기본값은 OFF입니다.
- ListAdapter 계열은 DiffUtil이 필수이며 기본 비교는 (old == new)입니다.

## 공통 API(아이템 조작)
모든 Base 계열에서 제공해야 합니다.
- setItems(items: List<ITEM>)
- getItems(): List<ITEM> (불변 복사본)
- addItem(item: ITEM)
- addItemAt(position: Int, item: ITEM): Boolean
- addItems(items: List<ITEM>)
- removeItem(item: ITEM): Boolean
- removeAll()
- replaceItemAt(position: Int, item: ITEM): Boolean
- removeAt(position: Int): Boolean
- moveItem(from: Int, to: Int): Boolean

## 클릭 이벤트
- setOnItemClickListener((position: Int, item: ITEM, view: View) -> Unit)
- setOnItemLongClickListener((position: Int, item: ITEM, view: View) -> Unit)

## ViewHolder 규격
### BaseRcvViewHolder (Non-binding)
- itemView 기반 기본 ViewHolder

### BaseBindingViewHolder (Binding)
- ViewBinding/ViewDataBinding을 보관하는 ViewHolder
- binding 접근은 항상 보장되어야 함

## RecyclerView.Adapter 계열 규격
### 1) BaseSingleAdapter (Non-binding)
- 입력: @LayoutRes layoutRes: Int
- onBind(view: View, item: ITEM, position: Int)
- 단일 ViewType 고정

### 2) BaseSingleDataBindingAdapter
- 입력: @LayoutRes layoutRes: Int
- onBind(holder: BaseBindingViewHolder<BINDING>, item: ITEM, position: Int)
- binding: ViewDataBinding
- 단일 ViewType 고정

### 3) BaseSingleViewBindingAdapter
- 입력: inflate: (LayoutInflater, ViewGroup, Boolean) -> VB
- onBind(holder: BaseBindingViewHolder<VB>, item: ITEM, position: Int)
- 단일 ViewType 고정

### 4) BaseMultiAdapter (Non-binding)
- 입력: layoutResProvider: (item: ITEM, position: Int) -> Int
- onBind(view: View, item: ITEM, position: Int, viewType: Int)
- 다중 ViewType 지원

### 5) BaseMultiDataBindingAdapter
- 입력: layoutResProvider: (item: ITEM, position: Int) -> Int
- onBind(holder: BaseBindingViewHolder<ViewDataBinding>, item: ITEM, position: Int, viewType: Int)
- 다중 ViewType 지원

### 6) BaseMultiViewBindingAdapter
- 입력:
  - viewTypeProvider: (item: ITEM, position: Int) -> Int
  - inflateMap: Map<Int, (LayoutInflater, ViewGroup, Boolean) -> ViewBinding>
- onBind(holder: BaseBindingViewHolder<ViewBinding>, item: ITEM, position: Int, viewType: Int)
- 다중 ViewType 지원
- viewType에 매핑되지 않은 inflate는 예외 처리(IllegalArgumentException) 또는 Logx 에러 후 return

### 7) SimpleSingle*Adapter
- BaseSingle*Adapter 상속
- 단일 ViewType 전용
- 람다 기반 최소 입력으로 사용 가능

## ListAdapter 계열 규격
### 1) BaseSingleListAdapter (Non-binding)
- 단일 ViewType 고정
- DiffUtil 기본 비교: (old == new)

### 2) BaseSingleDataBindingListAdapter
- @LayoutRes layoutRes: Int
- 단일 ViewType 고정

### 3) BaseSingleViewBindingListAdapter
- inflate: (LayoutInflater, ViewGroup, Boolean) -> VB
- 단일 ViewType 고정

### 4) BaseMultiListAdapter (Non-binding)
- layoutResProvider: (item: ITEM, position: Int) -> Int
- 다중 ViewType 지원

### 5) BaseMultiDataBindingListAdapter
- layoutResProvider: (item: ITEM, position: Int) -> Int
- 다중 ViewType 지원

### 6) BaseMultiViewBindingListAdapter
- viewTypeProvider: (item: ITEM, position: Int) -> Int
- inflateMap: Map<Int, (LayoutInflater, ViewGroup, Boolean) -> ViewBinding>
- 다중 ViewType 지원

### 7) SimpleSingle*ListAdapter
- BaseSingle*ListAdapter 상속
- 단일 ViewType 전용
- 람다 기반 최소 입력으로 사용 가능

## DiffUtil 옵션(RecyclerView.Adapter 계열)
- 기본 OFF
- OFF 상태:
  - add/remove/replace/move 등은 notifyItem* 계열 사용
  - setItems는 notifyDataSetChanged 사용
- ON 상태(옵션):
  - setItems 시 DiffUtil을 사용하여 변경 전파
  - 기본 비교는 (old == new), 필요 시 람다로 교체 가능

## 오류/검증 규칙
- position 범위 오류 시 false 반환 + Logx 기록
- viewType 매핑 누락 시 IllegalArgumentException 또는 Logx 에러
- 모든 public API는 안전한 입력 검증 포함

## 문서화
- README_*.md 반영은 코드 작성 완료 후 진행
