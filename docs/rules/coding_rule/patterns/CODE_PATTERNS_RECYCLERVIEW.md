# RecyclerView / Adapter 패턴 규칙

## 규칙

- RecyclerView Adapter 직접 구현 금지 → Simple UI Adapter 사용
- `DiffUtil.ItemCallback` 별도 클래스 생성 금지 → `RcvListDiffUtilCallBack` 사용
- `RecyclerView.OnScrollListener` 직접 구현 금지 → `RecyclerScrollStateView` + Flow 사용

## Adapter 선택 기준

| 상황 | 사용 Adapter |
|------|------------|
| DataBinding + DiffUtil (일반적인 경우) | `SimpleRcvDataBindingListAdapter` |
| DataBinding 없이 DiffUtil | `SimpleRcvListAdapter` |
| DataBinding + 즉시 notify (DiffUtil 불필요) | `SimpleBindingRcvAdapter` |
| DataBinding 없이 즉시 notify | `SimpleRcvAdapter` |
| Header/Footer 섹션 필요 + DataBinding | `SimpleHeaderFooterDataBindingRcvAdapter` |
| Header/Footer 섹션 필요 | `SimpleHeaderFooterRcvAdapter` |
| ViewBinding 사용 | `SimpleRcvViewBindingListAdapter` / `SimpleViewBindingRcvAdapter` |

## 심각도 기준

- HIGH: Adapter 직접 구현 (ViewHolder, DiffCallback 수동 작성)
- HIGH: `RecyclerView.OnScrollListener` 직접 구현
- MEDIUM: `DiffUtil.ItemCallback` 별도 클래스 생성

## 예시

### Adapter 구현

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
val listDiffUtil = RcvListDiffUtilCallBack<SampleItem>(
    itemsTheSame = { old, new -> old.id == new.id },
    contentsTheSame = { old, new -> old == new },
)

val adapter = SimpleRcvDataBindingListAdapter<SampleItem, ItemBinding>(
    layoutRes = R.layout.item,
    listDiffUtil = listDiffUtil,
) { holder, item, position ->
    holder.binding.tvTitle.text = item.title
}.apply {
    setOnItemClickListener { position, _, _ -> /* 클릭 처리 */ }
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
// RecyclerScrollStateView (XML에서 교체하거나 기존 RecyclerView를 RecyclerScrollStateView로 사용)
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
