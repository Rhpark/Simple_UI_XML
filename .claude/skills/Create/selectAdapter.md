# selectAdapter

Adapter 생성 시 타입별 규칙을 정의합니다.
SKILL.md의 공통 원칙(모듈 고정, 패키지 자동 생성, 결과 보고 형식)을 따르되,
**공통 Q2(ViewModel 선택)는 Adapter에 적용하지 않는다.**

---

## 생성 산출물

| 파일 | 경로 | 조건 |
|------|------|------|
| Adapter 클래스 | `app/src/main/java/{package}/{ClassName}.kt` | Q2 = 사용 안함 시에만 |
| Item Layout | `app/src/main/res/layout/item_{name}.xml` | 항상 |
| Item 데이터 클래스 | `app/src/main/java/{package}/{ItemClassName}.kt` | Q4 = 예 시에만 |

> Simple 어댑터(Q2 = 1~3)는 클래스 파일을 생성하지 않는다. item layout + 사용 스니펫만 제공한다.

---

## Q1 — Adapter 베이스 선택

1. `BaseRcvListAdapter` — DiffUtil 기반, 큐 연산 지원 (동적 리스트 권장)
2. `BaseRcvAdapter` — notify 기반, 즉시 갱신
3. `HeaderFooterRcvAdapter` — header/footer 섹션 지원 (`BaseRcvAdapter` 확장)
4. `RecyclerView.Adapter` — 라이브러리 미사용, 순수 Android

---

## Q2 — Simple 어댑터 선택 (Q1별 분기)

### Q1 = `BaseRcvListAdapter`
1. `SimpleRcvListAdapter` — 기본 ViewHolder (`itemView.findViewById`)
2. `SimpleRcvDataBindingListAdapter` — DataBinding
3. `SimpleRcvViewBindingListAdapter` — ViewBinding
4. 사용 안함 → Q3(ViewHolder 방식)으로 진행 후 직접 클래스 구현

### Q1 = `BaseRcvAdapter`
1. `SimpleRcvAdapter` — 기본 ViewHolder (`itemView.findViewById`)
2. `SimpleBindingRcvAdapter` — DataBinding
3. `SimpleViewBindingRcvAdapter` — ViewBinding
4. 사용 안함 → Q3(ViewHolder 방식)으로 진행 후 직접 클래스 구현

### Q1 = `HeaderFooterRcvAdapter`
1. `SimpleHeaderFooterRcvAdapter` — 기본 ViewHolder (`itemView.findViewById`)
2. `SimpleHeaderFooterDataBindingRcvAdapter` — DataBinding
3. `SimpleHeaderFooterViewBindingRcvAdapter` — ViewBinding
4. 사용 안함 → Q3(ViewHolder 방식)으로 진행 후 직접 클래스 구현

### Q1 = `RecyclerView.Adapter`
→ Simple 미적용. Q3(ViewHolder 방식)으로 바로 진행.

---

## Q3 — ViewHolder 방식 (Q2 = 사용 안함 또는 Q1 = RecyclerView.Adapter 시)

1. `BaseRcvDataBindingViewHolder` — 라이브러리 제공 DataBinding ViewHolder
2. `BaseRcvViewBindingViewHolder` — 라이브러리 제공 ViewBinding ViewHolder
3. Custom ViewHolder — `RecyclerView.ViewHolder` 직접 상속 (inner class)

> Q3 선택 결과는 커스텀 Adapter 코드의 `createViewHolderInternal`(BaseRcvListAdapter/BaseRcvAdapter/HeaderFooterRcvAdapter) 또는 `onCreateViewHolder`(RecyclerView.Adapter)에 반영한다.
> 구체적인 패턴은 아래 "ViewHolder 패턴 (Q3 선택별)" 섹션을 참조한다.

---

## Q4 — Item 데이터 클래스 생성 여부

1. 예 → `{ItemClassName}.kt` 생성
2. 아니오 → 생성 안 함 (호출 측에서 별도 정의)

---

## Item 데이터 클래스 작성 규칙 (Q4 = 예 시)

- 네이밍: `Adapter` suffix 제거 후 `Item` suffix 추가
  - 예) `UserAdapter` → `UserItem`, `FooBarListAdapter` → `FooBarListItem`
- `data class`로 선언한다.
- 필드는 비워두고 최소 구조만 생성한다. (호출 측에서 채움)
- 생성 위치: Adapter와 동일 패키지

```kotlin
data class UserItem(
    // 필드 추가 필요
)
```

---

## Layout name 규칙
- `Adapter` suffix 제거 후 snake_case → 접두사 `item_`
- 예) `UserAdapter` → `item_user.xml`, `FooBarListAdapter` → `item_foo_bar_list.xml`

---

## Adapter 작성 규칙
> 참조: `docs/rules/coding_rule/patterns/CODE_PATTERNS_RECYCLERVIEW.md`

---

## Simple 어댑터 사용 패턴 (Q2 = 1~3)

> 클래스 파일 미생성. item layout XML만 생성하고, 사용 측 스니펫을 제공한다.

### SimpleRcvListAdapter (Q1=BaseRcvListAdapter, Q2=1)
```kotlin
val adapter = SimpleRcvListAdapter<UserItem>(
    layoutRes = R.layout.item_user,
    listDiffUtil = RcvListDiffUtilCallBack(
        itemsTheSame = { old, new -> old.id == new.id },
        contentsTheSame = { old, new -> old == new },
    ),
) { holder, item, position ->
    holder.itemView.findViewById<TextView>(R.id.tvTitle).text = item.title
}
```

### SimpleRcvDataBindingListAdapter (Q1=BaseRcvListAdapter, Q2=2)
```kotlin
val adapter = SimpleRcvDataBindingListAdapter<UserItem, ItemUserBinding>(
    layoutRes = R.layout.item_user,
    listDiffUtil = RcvListDiffUtilCallBack(
        itemsTheSame = { old, new -> old.id == new.id },
        contentsTheSame = { old, new -> old == new },
    ),
) { holder, item, position ->
    holder.binding.tvTitle.text = item.title
}
```

### SimpleRcvViewBindingListAdapter (Q1=BaseRcvListAdapter, Q2=3)
```kotlin
val adapter = SimpleRcvViewBindingListAdapter<UserItem, ItemUserBinding>(
    inflate = ItemUserBinding::inflate,
    listDiffUtil = RcvListDiffUtilCallBack(
        itemsTheSame = { old, new -> old.id == new.id },
        contentsTheSame = { old, new -> old == new },
    ),
) { holder, item, position ->
    holder.binding.tvTitle.text = item.title
}
```

### SimpleRcvAdapter (Q1=BaseRcvAdapter, Q2=1)
```kotlin
val adapter = SimpleRcvAdapter<UserItem>(
    layoutRes = R.layout.item_user,
) { holder, item, position ->
    holder.itemView.findViewById<TextView>(R.id.tvTitle).text = item.title
}
```

### SimpleBindingRcvAdapter (Q1=BaseRcvAdapter, Q2=2)
```kotlin
val adapter = SimpleBindingRcvAdapter<UserItem, ItemUserBinding>(
    layoutRes = R.layout.item_user,
) { holder, item, position ->
    holder.binding.tvTitle.text = item.title
}
```

### SimpleViewBindingRcvAdapter (Q1=BaseRcvAdapter, Q2=3)
```kotlin
val adapter = SimpleViewBindingRcvAdapter<UserItem, ItemUserBinding>(
    inflate = ItemUserBinding::inflate,
) { holder, item, position ->
    holder.binding.tvTitle.text = item.title
}
```

### SimpleHeaderFooterRcvAdapter (Q1=HeaderFooterRcvAdapter, Q2=1)
```kotlin
val adapter = SimpleHeaderFooterRcvAdapter<UserItem>(
    layoutRes = R.layout.item_user,
) { holder, item, position ->
    holder.itemView.findViewById<TextView>(R.id.tvTitle).text = item.title
}
```

### SimpleHeaderFooterDataBindingRcvAdapter (Q1=HeaderFooterRcvAdapter, Q2=2)
```kotlin
val adapter = SimpleHeaderFooterDataBindingRcvAdapter<UserItem, ItemUserBinding>(
    layoutRes = R.layout.item_user,
) { holder, item, position ->
    holder.binding.tvTitle.text = item.title
}
```

### SimpleHeaderFooterViewBindingRcvAdapter (Q1=HeaderFooterRcvAdapter, Q2=3)
```kotlin
val adapter = SimpleHeaderFooterViewBindingRcvAdapter<UserItem, ItemUserBinding>(
    inflate = ItemUserBinding::inflate,
) { holder, item, position ->
    holder.binding.tvTitle.text = item.title
}
```

---

## 커스텀 Adapter 작성 규칙 (Q2 = 사용 안함 또는 Q1 = RecyclerView.Adapter)

### BaseRcvListAdapter (Q1=1, Q2=사용 안함)

```kotlin
class UserAdapter : BaseRcvListAdapter<UserItem, UserAdapter.ViewHolder>(
    RcvListDiffUtilCallBack(
        itemsTheSame = { old, new -> old.id == new.id },
        contentsTheSame = { old, new -> old == new },
    )
) {
    override fun createViewHolderInternal(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(/* ViewHolder 생성 */)

    override fun onBindViewHolder(holder: ViewHolder, item: UserItem, position: Int) {
        // 바인딩 로직
    }

    class ViewHolder(/* ... */) : RecyclerView.ViewHolder(/* ... */) { }
}
```

### BaseRcvAdapter (Q1=2, Q2=사용 안함)

```kotlin
class UserAdapter : BaseRcvAdapter<UserItem, UserAdapter.ViewHolder>() {
    override fun createViewHolderInternal(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(/* Q3 ViewHolder 패턴 참조 */)

    override fun onBindViewHolder(holder: ViewHolder, item: UserItem, position: Int) {
        // 바인딩 로직
    }

    class ViewHolder(/* ... */) : RecyclerView.ViewHolder(/* ... */) { }
}
```

### HeaderFooterRcvAdapter (Q1=3, Q2=사용 안함)

```kotlin
class UserAdapter : HeaderFooterRcvAdapter<UserItem, UserAdapter.ViewHolder>() {
    override fun createViewHolderInternal(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(/* Q3 ViewHolder 패턴 참조 */)

    override fun onBindViewHolder(holder: ViewHolder, item: UserItem, position: Int) {
        // 바인딩 로직
    }

    // 필요 시 override (시그니처 주의: item, position 포함)
    // override fun onBindHeaderViewHolder(holder: ViewHolder, item: UserItem, position: Int) { }
    // override fun onBindFooterViewHolder(holder: ViewHolder, item: UserItem, position: Int) { }

    class ViewHolder(/* ... */) : RecyclerView.ViewHolder(/* ... */) { }
}
```

### RecyclerView.Adapter (Q1=4)

```kotlin
class UserAdapter : RecyclerView.Adapter<UserAdapter.ViewHolder>() {
    private val items = mutableListOf<UserItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(/* ViewHolder 생성 */)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // 바인딩 로직
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(/* ... */) : RecyclerView.ViewHolder(/* ... */) { }
}
```

### ViewHolder 패턴 (Q3 선택별)

**Q3-1: `BaseRcvDataBindingViewHolder`**
```kotlin
// createViewHolderInternal / onCreateViewHolder
BaseRcvDataBindingViewHolder<ItemUserBinding>(R.layout.item_user, parent)

// onBindViewHolder
holder.binding.tvTitle.text = item.title
```

**Q3-2: `BaseRcvViewBindingViewHolder`**
```kotlin
// createViewHolderInternal / onCreateViewHolder
BaseRcvViewBindingViewHolder(ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false))

// onBindViewHolder
holder.binding.tvTitle.text = item.title
```

**Q3-3: Custom ViewHolder**
```kotlin
class ViewHolder(val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root) {
    // 필요 시 bind() 함수 추가
}

// onCreateViewHolder
ViewHolder(ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false))
```

---

## 결과 보고 시 주의 사항
- ViewModel 생성 없음 (Adapter는 ViewModel을 직접 보유하지 않는다)
- Simple 어댑터 선택 시: 클래스 파일 생성 없음을 명시
- Q4 = 아니오이면 Item 데이터 클래스 미생성 명시
- 다음 단계 안내: RecyclerView에 Adapter를 연결하는 방법 안내
