# 📱 Simple UI RecyclerView vs 순수 Android - 완벽 비교 가이드

### RecyclerView 예시
![recyclerview.gif](example_gif%2Frecyclerview.gif)

### adapter code 예시 
![adapter.png](example_gif%2Fadapter.png)

> **"복잡한 RecyclerView 구현을 단 몇 줄로 끝내자!"** 기존 RecyclerView 개발 대비 Simple UI가 주는 체감 차이를 한눈에 확인하세요.

<br>
</br>

## 🔎 한눈 비교 (At a glance)

<br>
</br>

### Adapter 
| 항목 | 순수 Android | Simple UI |
|:--|:--:|:--:|
| Adapter 구현 방식 | 수동 구현 필요 (50-74줄) | 라이브러리 제공 ✅ |
| DiffUtil 처리 | 별도 클래스 작성 | 자동 내장 ✅ ||
| 개발자 경험 | 복잡한 보일러플레이트 | 간결한 라이브러리 호출 ✅ |


<br>
</br>

### RecyclerView
| 항목 | 순수 Android | Simple UI |
|:--|:--:|:--:|
| 스크롤 방향 감지 | OnScrollListener 구현 (50줄+) | Flow 기반 자동 ✅ |
| Edge 도달 감지 | canScrollVertically 수동 | Flow 기반 자동 ✅ |
| RecyclerView 고급 기능 | 직접 구현 | RecyclerScrollStateView ✅ |
| 개발자 경험 | 복잡한 보일러플레이트 | 간결한 라이브러리 호출 ✅ |

> **핵심:** Simple UI는 "복잡한 RecyclerView 구현"을 **자동화**합니다. 개발 속도가 달라집니다.

<br>
</br>

## 💡 왜 중요한가:

- **개발 시간 단축**: Adapter 보일러플레이트 제거로 핵심 로직에 집중 가능
- **성능 최적화**: DiffUtil 자동 적용으로 리스트 업데이트 효율화
- **실시간 피드백**: Flow 기반 스크롤 상태로 UX 개선 가능
- **유지보수성**: 통합 API로 일관된 코드 스타일 유지

<br>
</br>

## 📚 Simple UI가 제공하는 4가지 Adapter

Simple UI는 다양한 상황에 맞는 **4가지 RecyclerView Adapter**를 제공합니다:

| Adapter 종류 | DiffUtil | DataBinding | 사용 상황 | 보일러플레이트 감소율 |
|:--|:--:|:--:|:--|:--:|
| **SimpleBindingRcvListAdapter** | ✅ 내장 | ✅ 지원 | 대부분의 경우 (추천) | **~90%** |
| **SimpleRcvListAdapter** | ✅ 내장 | ❌ 없음 | DataBinding 없이 DiffUtil 필요 시 | **~85%** |
| **SimpleBindingRcvAdapter** | ⚠️ 수동 설정 | ✅ 지원 | DiffUtil 불필요 시 | **~80%** |
| **SimpleRcvAdapter** | ⚠️ 수동 설정 | ❌ 없음 | 최소 종속성 필요 시 | **~75%** |

### 어떤 Adapter를 선택해야 할까?

**🥇 1순위: SimpleBindingRcvListAdapter (가장 추천!)**
- ✅ DiffUtil 자동 내장 → 성능 최적화 자동
- ✅ DataBinding 지원 → 코드 간결
- ✅ commitCallback 지원 → DiffUtil 완료 후 로직 실행
- 📌 **대부분의 RecyclerView 개발에 최적**

**🥈 2순위: SimpleRcvListAdapter**
- ✅ DiffUtil 자동 내장
- ❌ DataBinding 없음 (findViewById 사용)
- 📌 **DataBinding 미사용 프로젝트**에 적합

**🥉 3순위: SimpleBindingRcvAdapter**
- ⚠️ DiffUtil 수동 설정 필요 (setDiffUtilItemSame 등)
- ✅ DataBinding 지원
- 📌 **DiffUtil이 불필요한 정적 리스트**에 적합

**4순위: SimpleRcvAdapter**
- ⚠️ DiffUtil 수동 설정 필요
- ❌ DataBinding 없음
- 📌 **최소 종속성**이 필요한 경우

<br>
</br>

## 🎯 비교 대상: Activity 기반 다중 Adapter RecyclerView 시스템

**구현 예제 기능:**
- **Simple UI**: 3가지 라이브러리 Adapter 지원
- **순수 Android**: 2가지 전통적인 Adapter 구현
- Flow 기반 vs 수동 스크롤 방향/Edge 감지
- 동적 아이템 추가/삭제/섞기/전체삭제
- RadioButton으로 Adapter 동적 전환
- Activity 기반 전체 로직 처리 (ViewModel 없음)

<br>
</br>

## 실제 코드 비교

### 첫째: Adapter 구현 방식 비교

<details>
<summary><strong>순수 Android - 수동 Adapter 구현</strong></summary>

```kotlin
// OriginCustomListAdapter.kt (50줄) - ListAdapter 수동 구현
class OriginCustomListAdapter(private val onItemClick: (SampleItem, Int) -> Unit) :
    ListAdapter<SampleItem, OriginCustomListAdapter.SampleItemViewHolder>(SampleItemDiffCallback()) {

    // DiffCallback 수동 구현 필요
    class SampleItemDiffCallback : DiffUtil.ItemCallback<SampleItem>() {
        override fun areItemsTheSame(oldItem: SampleItem, newItem: SampleItem): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: SampleItem, newItem: SampleItem): Boolean = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SampleItemViewHolder {
        val binding = DataBindingUtil.inflate<ItemRcvTextviewBinding>(
            LayoutInflater.from(parent.context),
            R.layout.item_rcv_textview,
            parent,
            false
        )
        return SampleItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SampleItemViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, position, onItemClick)
    }

    class SampleItemViewHolder(private val binding: ItemRcvTextviewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SampleItem, position: Int, onItemClick: (SampleItem, Int) -> Unit) {
            binding.apply {
                tvTitle.text = item.title
                tvDescription.text = item.description
                tvPosition.text = "Position: $position"
                root.setOnClickListener { onItemClick(item, position) }
                executePendingBindings()
            }
        }
    }
}
```
**문제점:** ViewHolder 클래스, DiffCallback 클래스, 복잡한 바인딩 로직 모두 수동 구현
</details>

<details>
<summary><strong>Simple UI - 라이브러리 Adapter 활용</strong></summary>

```kotlin
// SimpleBindingRcvListAdapter - DiffUtil 내장, 한 번에 완성!
private val simpleListAdapter = SimpleBindingRcvListAdapter<SampleItem, ItemRcvTextviewBinding>(
    R.layout.item_rcv_textview,
    listDiffUtil = RcvListDiffUtilCallBack(
        itemsTheSame = { oldItem, newItem -> oldItem.id == newItem.id },
        contentsTheSame = { oldItem, newItem -> oldItem == newItem }
    )
) { holder, item, position ->
    holder.binding.apply {
        tvTitle.text = item.title
        tvDescription.text = item.description
        tvPosition.text = "Position: $position"
        root.setOnClickListener { currentRemoveAtAdapter(position) }
    }
}

// SimpleBindingRcvAdapter - DiffUtil 없이 더 간단!
private val simpleAdapter = SimpleBindingRcvAdapter<SampleItem, ItemRcvTextviewBinding>(
    R.layout.item_rcv_textview
) { holder, item, position ->
    // 동일한 간단한 바인딩 로직
}
```
**결과:** ViewHolder, DiffCallback 자동 처리, 바인딩 로직만 작성!
</details>

<br>
</br>

### 둘째: 스크롤 감지 구현 비교

<details>
<summary><strong>순수 Android - OnScrollListener 수동 구현</strong></summary>

```kotlin
// 50줄+ 복잡한 스크롤 감지 구현
private fun setupManualScrollDetection() {
    binding.rcvItems.addOnScrollListener(object : RecyclerView.OnScrollListener() {

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            when (newState) {
                RecyclerView.SCROLL_STATE_IDLE -> {
                    isScrolling = false
                    accumulatedDy = 0
                    lastScrollDirection = "정지"
                    binding.tvScrollInfo.text = "🔄 방향: 스크롤 정지"
                }
                RecyclerView.SCROLL_STATE_DRAGGING -> { isScrolling = true }
                RecyclerView.SCROLL_STATE_SETTLING -> { isScrolling = true }
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            // 스크롤 방향 수동 계산
            accumulatedDy += dy
            if (abs(accumulatedDy) >= scrollDirectionThreshold) {
                val currentDirection = if (accumulatedDy > 0) "아래로 스크롤" else "위로 스크롤"
                if (currentDirection != lastScrollDirection) {
                    lastScrollDirection = currentDirection
                    binding.tvScrollInfo.text = "🔄 방향: $currentDirection"
                }
                accumulatedDy = 0
            }

            // Edge 감지 수동 구현
            checkEdgeReach(recyclerView)
        }
    })
}

private fun checkEdgeReach(recyclerView: RecyclerView) {
    // 상단/하단 Edge 수동 감지 로직
    val newIsAtTop = !recyclerView.canScrollVertically(-1)
    val newIsAtBottom = !recyclerView.canScrollVertically(1)
    // ... 복잡한 상태 비교 및 업데이트
}
```
**문제점:** 복잡한 상태 관리, 수동 계산, Edge 감지 별도 구현 필요
</details>

<details>
<summary><strong>Simple UI - Flow 기반 자동 감지</strong></summary>

```kotlin
// Flow 기반 자동 스크롤 감지 - 단 20줄!
private fun setupScrollStateDetection() {
    binding.rcvItems.apply {
        // 스크롤 방향 자동 감지
        lifecycleScope.launch {
            sfScrollDirectionFlow.collect { direction ->
                val directionText = when (direction) {
                    ScrollDirection.UP -> "위로 스크롤"
                    ScrollDirection.DOWN -> "아래로 스크롤"
                    ScrollDirection.LEFT -> "왼쪽으로 스크롤"
                    ScrollDirection.RIGHT -> "오른쪽으로 스크롤"
                    ScrollDirection.IDLE -> "스크롤 정지"
                }
                binding.tvScrollInfo.text = "방향: $directionText"
            }
        }

        // Edge 도달 자동 감지
        lifecycleScope.launch {
            sfEdgeReachedFlow.collect { (edge, isReached) ->
                val edgeText = when (edge) {
                    ScrollEdge.TOP -> "상단"
                    ScrollEdge.BOTTOM -> "하단"
                    ScrollEdge.LEFT -> "좌측"
                    ScrollEdge.RIGHT -> "우측"
                }
                val statusText = if (isReached) "도달" else "벗어남"
                binding.tvScrollInfo.text = "$edgeText $statusText"
            }
        }
    }
}
```
**결과:** Flow로 자동 감지, 상태 관리 자동, 방향/Edge 정보 실시간 제공!
</details>

<br>
</br>

### 셋째: DiffUtil 처리 방식 비교

<details>
<summary><strong>순수 Android - DiffUtil 클래스 수동 생성</strong></summary>

```kotlin
// 별도 DiffCallback 클래스 생성 필요
class SampleItemDiffCallback : DiffUtil.ItemCallback<SampleItem>() {
    override fun areItemsTheSame(oldItem: SampleItem, newItem: SampleItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: SampleItem, newItem: SampleItem): Boolean {
        return oldItem == newItem
    }
}

// ListAdapter에 수동 적용
class OriginCustomListAdapter : ListAdapter<SampleItem, ViewHolder>(SampleItemDiffCallback()) {
    // ... 추가 구현 필요
}
```
**문제점:** 별도 클래스 생성, 메서드 오버라이드, Adapter와 분리된 관리
</details>

<details>
<summary><strong>Simple UI - DiffUtil 자동 내장</strong></summary>

```kotlin
// 인라인으로 DiffUtil 자동 처리
listDiffUtil = RcvListDiffUtilCallBack(
    itemsTheSame = { oldItem, newItem -> oldItem.id == newItem.id },
    contentsTheSame = { oldItem, newItem -> oldItem == newItem }
)
```
**결과:** 별도 클래스 불필요, 인라인 람다로 간결, Adapter와 통합 관리!
</details>

<br>
</br>

## 🚀 Simple UI RecyclerView/Adapter의 핵심 장점

### 1. **📉 압도적인 생산성 향상**
- **Adapter 구현**: CustomListAdapter - 50줄, CustomAdapter - 74줄의 복잡한 구현 → 라이브러리 호출로 완성 - 10여줄
- **DiffUtil 처리**: 별도 클래스 생성 → 인라인 람다로 간단 처리
- **개발 시간**: 2시간 → 1시간 미만 **60% 단축**

<br>
</br>

### 2. **⚡ Flow 기반 고급 스크롤 기능**
- **자동 방향 감지**: UP/DOWN/LEFT/RIGHT/IDLE 실시간 분류
- **Edge 감지**: TOP/BOTTOM/LEFT/RIGHT 도달 상태 실시간 제공
- **RecyclerScrollStateView**: 일반 RecyclerView → 고급 기능 자동 업그레이드

<br>
</br>

### 3. **🛠️ 개발자 친화적 설계**
- **통합 API**: 서로 다른 Adapter도 동일한 메서드로 조작
- **타입 안전성**: 컴파일 타임 오류 방지
- **확장성**: 필요에 따라 커스텀 Adapter 쉽게 추가

<br>
</br>

## 💡 개발자 후기

> **"3가지 Adapter를 바로 비교할 수 있어서 최적 선택이 쉬워요!"**
>
> **"Flow로 스크롤 이벤트 받으니 OnScrollListener 지옥에서 해방!"**
>
> **"DiffUtil 클래스 만들 필요 없이 바로 성능 최적화!"**
>
> **"통합 API로 모든 Adapter를 동일하게 다룰 수 있어 코드가 깔끔해졌어요!"**

<br>
</br>

## 🎉 결론: RecyclerView 개발의 새로운 표준

**Simple UI RecyclerView/Adapter**는 복잡한 RecyclerView 개발을 **단순하고 강력하게** 만드는 혁신적인 라이브러리입니다.

✅ **Flow 기반 자동화** - 복잡한 스크롤 감지를 Flow로 간단하게!
✅ **라이브러리 기반 Adapter** - 보일러플레이트 없이 핵심 로직에 집중!
✅ **통합 API** - 다양한 Adapter를 일관된 방식으로 관리!

**전통적인 복잡함은 이제 그만.**
**Simple UI와 함께 생산적인 개발을 경험하세요!** 🚀

<br>
</br>

## 🚀 Simple UI Adapter 고급 기능 가이드

### ListAdapter 계열 고급 메서드 (SimpleBindingRcvListAdapter, SimpleRcvListAdapter)

**기본 메서드:**
- `setItems(list, commitCallback?)` - 리스트 설정
- `addItem(item, commitCallback?)` - 끝에 아이템 추가
- `addItemAt(position, item, commitCallback?)` - 특정 위치에 추가
- `addItems(list, commitCallback?)` - 끝에 여러 아이템 추가
- `removeAt(position, commitCallback?)` - 특정 위치 제거
- `removeItem(item, commitCallback?)` - 특정 아이템 제거
- `clearItems(commitCallback?)` - 전체 삭제

**고급 메서드:**
- `addItems(position, itemList, commitCallback?)` - **특정 위치에 여러 아이템 추가**
  - 사용 예: `adapter.addItems(0, newItems) { /* DiffUtil 완료 후 실행 */ }`

- `moveItem(fromPosition, toPosition, commitCallback?)` - **아이템 위치 이동**
  - 드래그 앤 드롭 구현에 필수!
  - 사용 예: `adapter.moveItem(3, 0) { /* 이동 완료 */ }`

- `replaceItemAt(position, item, commitCallback?)` - **특정 위치 아이템 교체**
  - 개별 아이템 수정 시 효율적
  - 사용 예: `adapter.replaceItemAt(5, updatedItem)`

- `getItems()` - **현재 아이템 리스트 조회**
  - 현재 상태 확인 필요 시

- `onBindViewHolder(..., payloads: List<Any>)` - **부분 업데이트 지원**
  - DiffUtil payload로 성능 최적화
  - 전체 아이템이 아닌 변경된 부분만 업데이트

**commitCallback 파라미터:**
모든 변경 메서드는 선택적 `commitCallback` 파라미터를 지원합니다.
이 콜백은 DiffUtil 계산 및 업데이트가 완료된 후 실행됩니다.

```kotlin
// 예제: 아이템 추가 후 스크롤
adapter.addItem(newItem) {
    recyclerView.smoothScrollToPosition(adapter.itemCount - 1)
}

// 예제: 드래그 앤 드롭
adapter.moveItem(fromPos, toPos) {
    Toast.makeText(context, "이동 완료", Toast.LENGTH_SHORT).show()
}
```

**Payload를 활용한 부분 업데이트:**
```kotlin
// DiffUtil에서 payload 설정
RcvListDiffUtilCallBack<Item>(
    itemsTheSame = { old, new -> old.id == new.id },
    contentsTheSame = { old, new -> old == new },
    getChangePayload = { old, new ->
        if (old.title != new.title) "title_changed" else null
    }
)

// Adapter 서브클래스에서 payload 처리
class MyAdapter : BaseRcvListAdapter<Item, VH>(diffUtil) {
    override fun onBindViewHolder(holder: VH, position: Int, item: Item, payloads: List<Any>) {
        if (payloads.contains("title_changed")) {
            // 제목만 업데이트 (성능 향상!)
            holder.binding.tvTitle.text = item.title
        } else {
            // 전체 업데이트
            super.onBindViewHolder(holder, position, item, payloads)
        }
    }
}
```

<br>
</br>

### 일반 Adapter 계열 고급 메서드 (SimpleBindingRcvAdapter, SimpleRcvAdapter)

**기본 메서드:**
- `setItems(list)` - 리스트 설정 (DiffUtil 자동 적용)
- `addItem(item)` - 끝에 아이템 추가
- `addItemAt(position, item)` - 특정 위치에 추가
- `addItems(list)` - 끝에 여러 아이템 추가
- `addItems(position, list)` - 특정 위치에 여러 아이템 추가
- `removeAt(position)` - 특정 위치 제거
- `removeItem(item)` - 특정 아이템 제거
- `removeAll()` - 전체 삭제
- `getItems()` - 현재 아이템 리스트 조회

**DiffUtil 동적 설정:**

일반 Adapter 계열은 **런타임에 DiffUtil 로직을 동적으로 설정**할 수 있습니다!

```kotlin
val adapter = SimpleBindingRcvAdapter<Item, Binding>(R.layout.item) { holder, item, pos ->
    // 바인딩 로직
}

// DiffUtil 아이템 비교 로직 설정
adapter.setDiffUtilItemSame { oldItem, newItem ->
    oldItem.id == newItem.id
}

// DiffUtil 내용 비교 로직 설정
adapter.setDiffUtilContentsSame { oldItem, newItem ->
    oldItem == newItem
}

// DiffUtil payload 설정 (부분 업데이트용)
adapter.setDiffUtilChangePayload { oldItem, newItem ->
    when {
        oldItem.title != newItem.title -> "title_changed"
        oldItem.count != newItem.count -> "count_changed"
        else -> null
    }
}

// 아이템 이동 감지 활성화
adapter.detectMoves = true  // 기본값: false
```

**DiffUtil 미설정 시 기본 동작:**
- `itemsTheSame`: 참조 비교 (`oldItem === newItem`)
- `contentsTheSame`: equals 비교 (`oldItem == newItem`)
- `changePayload`: null (전체 업데이트)

**setItems() 사용 시 자동 DiffUtil 적용:**
```kotlin
// setItems() 호출 시 설정된 DiffUtil 로직으로 자동 비교
adapter.setItems(newList)  // DiffUtil 자동 실행!
```

**언제 DiffUtil 설정이 필요한가?**
- ✅ **데이터 클래스가 아닌 경우**: equals 오버라이드 없음
- ✅ **복잡한 비교 로직**: ID만 비교하고 싶을 때
- ✅ **부분 업데이트 필요**: payload로 성능 최적화
- ❌ **데이터 클래스 + 간단한 비교**: 설정 불필요 (기본 동작으로 충분)

<br>
</br>

## 🎨 RecyclerScrollStateView 고급 설정

### 스크롤 감지 민감도 조절

RecyclerScrollStateView는 **스크롤 방향 감지**와 **Edge 도달 감지**의 민감도를 세밀하게 조절할 수 있습니다.

**코드로 설정:**
```kotlin
recyclerView.apply {
    // 스크롤 방향 감지 threshold (기본값: 20px)
    setScrollDirectionThreshold(30)  // 30px 이상 스크롤 시 방향 감지

    // Edge 도달 감지 threshold (기본값: 10px)
    setEdgeReachThreshold(15)  // Edge 15px 이내 도달 감지
}
```

**XML 속성으로 설정:**
```xml
<kr.open.library.simple_ui.presenter.ui.view.recyclerview.RecyclerScrollStateView
    android:id="@+id/recyclerView"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:scrollDirectionThreshold="30"
    app:edgeReachThreshold="15" />
```

### 리스너 등록 방법 (3가지)

**방법 1: 람다 방식 (간편)**
```kotlin
// 스크롤 방향 감지
recyclerView.setOnScrollDirectionListener { direction ->
    when (direction) {
        ScrollDirection.UP -> Log.d("Scroll", "위로")
        ScrollDirection.DOWN -> Log.d("Scroll", "아래로")
        ScrollDirection.IDLE -> Log.d("Scroll", "정지")
        else -> {}
    }
}

// Edge 도달 감지
recyclerView.setOnReachEdgeListener { edge, isReached ->
    if (isReached) {
        when (edge) {
            ScrollEdge.TOP -> Log.d("Edge", "상단 도달")
            ScrollEdge.BOTTOM -> Log.d("Edge", "하단 도달")
            else -> {}
        }
    }
}
```

**방법 2: 인터페이스 방식 (고급)**
```kotlin
recyclerView.setOnScrollDirectionListener(object : OnScrollDirectionChangedListener {
    override fun onScrollDirectionChanged(scrollDirection: ScrollDirection) {
        // 스크롤 방향 변경 처리
    }
})

recyclerView.setOnReachEdgeListener(object : OnEdgeReachedListener {
    override fun onEdgeReached(edge: ScrollEdge, isReached: Boolean) {
        // Edge 도달 처리
    }
})
```

**방법 3: Flow 방식 (Coroutine, 추천!)**
```kotlin
lifecycleScope.launch {
    // 스크롤 방향 Flow
    recyclerView.sfScrollDirectionFlow.collect { direction ->
        // Flow 기반 처리
    }
}

lifecycleScope.launch {
    // Edge 도달 Flow
    recyclerView.sfEdgeReachedFlow.collect { (edge, isReached) ->
        // Flow 기반 처리
    }
}
```

### 메모리 관리 (자동)

RecyclerScrollStateView는 **WeakReference**를 사용하여 리스너를 관리합니다.
- ✅ Activity/Fragment가 종료되어도 메모리 누수 없음
- ✅ 명시적인 리스너 해제 불필요
- ✅ onAttachedToWindow/onDetachedFromWindow에서 자동 관리

### threshold 값 선택 가이드

**scrollDirectionThreshold (스크롤 방향 감지):**
- **낮은 값 (10-15px)**: 민감한 감지, 작은 스크롤에도 반응
- **중간 값 (20-30px, 기본)**: 일반적인 사용
- **높은 값 (40-50px)**: 큰 스크롤만 감지, 노이즈 감소

**edgeReachThreshold (Edge 도달 감지):**
- **낮은 값 (5px)**: 정확한 Edge 도달
- **중간 값 (10px, 기본)**: 일반적인 사용
- **높은 값 (20-30px)**: 여유있는 Edge 감지 (무한 스크롤 등)

<br>
</br>

## 🛠️ ViewHolder 고급 기능

Simple UI는 **2가지 ViewHolder**를 제공합니다:

### BaseBindingRcvViewHolder (DataBinding용)

**주요 기능:**
- `binding` 프로퍼티 - 자동 DataBinding 객체
- `executePendingBindings()` - DataBinding 즉시 실행
- `isValidPosition()` - 안전한 position 검증
- `getAdapterPositionSafe()` - 안전한 position 조회

**사용 예제:**
```kotlin
SimpleBindingRcvListAdapter<Item, ItemBinding>(
    R.layout.item,
    diffUtil
) { holder, item, position ->
    holder.binding.apply {
        tvTitle.text = item.title
        tvDescription.text = item.description
        executePendingBindings()  // 즉시 바인딩 실행
    }
}
```

### BaseRcvViewHolder (일반 View용)

**주요 기능:**
- `findViewById<T>(id)` - **타입 안전 + 자동 캐싱** findViewById
- `findViewByIdOrNull<T>(id)` - **null-safe + 자동 캐싱** findViewById
- `clearViewCache()` - 뷰 캐시 수동 정리
- `isValidPosition()` - 안전한 position 검증
- `getAdapterPositionSafe()` - 안전한 position 조회

**View 캐싱 시스템 (성능 최적화!):**

BaseRcvViewHolder는 `findViewById()` 결과를 **자동으로 캐싱**합니다!

```kotlin
class CustomViewHolder(layout: Int, parent: ViewGroup)
    : BaseRcvViewHolder(layout, parent) {

    // 첫 호출: findViewById 실행 + 캐시 저장
    // 이후 호출: 캐시에서 즉시 반환 (성능 향상!)
    private val titleView = findViewById<TextView>(R.id.tvTitle)
    private val descView = findViewByIdOrNull<TextView>(R.id.tvDesc)  // null-safe

    fun bind(item: Item) {
        if (isValidPosition()) {  // position 검증
            titleView.text = item.title
            descView?.text = item.description
        }
    }
}
```

**SimpleRcvListAdapter와 함께 사용:**
```kotlin
val adapter = SimpleRcvListAdapter<Item>(R.layout.item, diffUtil) { holder, item, pos ->
    // findViewById는 자동 캐싱됨!
    val titleView = holder.findViewById<TextView>(R.id.tvTitle)
    val descView = holder.findViewByIdOrNull<TextView>(R.id.tvDesc)

    titleView.text = item.title
    descView?.text = item.description
}
```

**캐싱의 장점:**
- ✅ **성능 향상**: findViewById 반복 호출 방지
- ✅ **자동 관리**: 별도 변수 선언 불필요
- ✅ **메모리 효율**: onViewRecycled() 시 자동 정리
- ✅ **타입 안전**: 제네릭으로 타입 캐스팅 자동

**onViewRecycled 시 자동 캐시 정리:**
```kotlin
// BaseRcvAdapter/BaseRcvListAdapter에서 자동 호출
override fun onViewRecycled(holder: VH) {
    super.onViewRecycled(holder)
    if(holder is BaseRcvViewHolder) {
        holder.clearViewCache()  // 캐시 자동 정리!
    }
}
```

<br>
</br>

## 실제 구현 예제보기

**라이브 예제 코드:**
> - Simple UI 예제: `app/src/main/java/kr/open/library/simpleui_xml/recyclerview/new_/`
> - 순수 Android 예제: `app/src/main/java/kr/open/library/simpleui_xml/recyclerview/origin/`
> - 실제로 앱을 구동시켜서 실제 구현 예제를 확인해 보세요!

<br>
</br>

**테스트 가능한 기능:**
- 3가지 Simple UI Adapter vs 2가지 전통 Adapter 비교
- Flow 기반 실시간 스크롤 방향/Edge 감지
- RadioButton으로 동적 Adapter 전환
- 실시간 아이템 추가/삭제/섞기/전체삭제
- DiffUtil 자동 적용 성능 비교
- 통합 API 일관성 테스트

<br>
</br>

.