# RecyclerView / Adapter 패턴 규칙

## 규칙

- Simple UI Adapter로 해결 가능한 경우 RecyclerView Adapter 직접 구현 금지
- `DiffUtil.ItemCallback` 별도 클래스 생성 금지 → `RcvListDiffUtilCallBack` 사용
- `RecyclerView.OnScrollListener` 직접 구현 금지 → `RecyclerScrollStateView` + Flow 사용

## 베이스 선택 기준

| 상황 | 베이스 클래스 |
|------|------------|
| 동적 리스트 (DiffUtil 필요) | `BaseRcvListAdapter` |
| 즉시 갱신 (DiffUtil 불필요) | `BaseRcvAdapter` |
| Header/Footer 섹션 필요 | `HeaderFooterRcvAdapter` |
| 라이브러리 미사용 또는 복잡한 ViewType | `RecyclerView.Adapter` |

## Simple Adapter 선택 기준

> Simple Adapter는 서브클래싱 없이 람다로 사용한다. 가능하면 우선 사용한다.

| 상황 | 사용 Adapter |
|------|------------|
| DiffUtil + DataBinding | `SimpleRcvDataBindingListAdapter` |
| DiffUtil + ViewBinding | `SimpleRcvViewBindingListAdapter` |
| DiffUtil + 기본 ViewHolder | `SimpleRcvListAdapter` |
| 즉시 notify + DataBinding | `SimpleBindingRcvAdapter` |
| 즉시 notify + ViewBinding | `SimpleViewBindingRcvAdapter` |
| 즉시 notify + 기본 ViewHolder | `SimpleRcvAdapter` |
| Header/Footer + DataBinding | `SimpleHeaderFooterDataBindingRcvAdapter` |
| Header/Footer + ViewBinding | `SimpleHeaderFooterViewBindingRcvAdapter` |
| Header/Footer + 기본 ViewHolder | `SimpleHeaderFooterRcvAdapter` |

## 커스텀 Adapter 작성 규칙

Simple Adapter로 해결 불가한 경우(복잡한 다중 ViewType 등)에만 직접 구현한다.

- `onCreateViewHolder`는 `RootRcvAdapter`에서 **`final`** 선언 → override 금지
- ViewHolder 생성은 반드시 `createViewHolderInternal`을 override한다
- `RecyclerView.Adapter` 직접 상속 시에는 `onCreateViewHolder` override 사용

## 심각도 기준

- HIGH: Simple Adapter로 대체 가능한데 Adapter를 직접 구현한 경우
- HIGH: `RecyclerView.OnScrollListener` 직접 구현
- MEDIUM: `DiffUtil.ItemCallback` 별도 클래스 생성

## 예시

### Simple Adapter 구현

❌ BAD
```kotlin
class CustomAdapter : ListAdapter<SampleItem, CustomAdapter.ViewHolder>(DiffCallback()) {
    class DiffCallback : DiffUtil.ItemCallback<SampleItem>() {
        override fun areItemsTheSame(old: SampleItem, new: SampleItem) = old.id == new.id
        override fun areContentsTheSame(old: SampleItem, new: SampleItem) = old == new
    }
    class ViewHolder(val binding: ItemBinding) : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder { ... }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) { ... }
}
```

✅ GOOD
```kotlin
val adapter = SimpleRcvDataBindingListAdapter<SampleItem, ItemBinding>(
    layoutRes = R.layout.item,
    listDiffUtil = RcvListDiffUtilCallBack(
        itemsTheSame = { old, new -> old.id == new.id },
        contentsTheSame = { old, new -> old == new },
    ),
) { holder, item, position ->
    holder.binding.tvTitle.text = item.title
}.apply {
    setOnItemClickListener { position, _, _ -> /* 클릭 처리 */ }
}
```

---

### 커스텀 Adapter 구현 (Simple Adapter 불가 시)

❌ BAD — `onCreateViewHolder` override 시도 (final이므로 컴파일 오류)
```kotlin
class MultiTypeAdapter : BaseRcvAdapter<Item, RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder { ... }
}
```

✅ GOOD — `createViewHolderInternal` override
```kotlin
class MultiTypeAdapter : BaseRcvAdapter<Item, RecyclerView.ViewHolder>() {
    override fun createViewHolderInternal(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_A -> ViewHolderA(ItemABinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else   -> ViewHolderB(ItemBBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, item: Item, position: Int) {
        when (holder) {
            is ViewHolderA -> holder.binding.tvTitle.text = item.title
            is ViewHolderB -> holder.binding.tvDesc.text = item.desc
        }
    }
}
```

---

### 스크롤 감지

❌ BAD
```kotlin
recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        // 50줄+ 스크롤 방향/Edge 수동 계산
    }
})
```

✅ GOOD
```kotlin
lifecycleScope.launch {
    binding.rcvItems.sfScrollDirectionFlow.collect { direction ->
        when (direction) {
            ScrollDirection.UP -> { /* 위로 스크롤 */ }
            ScrollDirection.DOWN -> { /* 아래로 스크롤 */ }
            else -> {}
        }
    }
}

lifecycleScope.launch {
    binding.rcvItems.sfEdgeReachedFlow.collect { (edge, isReached) ->
        if (edge == ScrollEdge.BOTTOM && isReached) loadMoreItems()
    }
}
```

---

### Header/Footer Adapter

```kotlin
val adapter = SimpleHeaderFooterDataBindingRcvAdapter<Item, ItemBinding>(
    layoutRes = R.layout.item,
) { holder, item, position ->
    holder.binding.tvTitle.text = item.title
}.apply {
    setHeaderItems(listOf(headerItem))
    setItems(contentItems)
    setFooterItems(listOf(footerItem))
}
```
