# selectLayout

커스텀 Layout View 생성 시 타입별 규칙을 정의합니다.
SKILL.md의 공통 원칙(모듈 고정, 패키지 자동 생성, 결과 보고 형식)을 따릅니다.

> ViewModel 미적용: Layout View는 ViewModel을 직접 보유하지 않으므로 공통 Q2(ViewModel 선택)를 적용하지 않습니다.
> Q_Layout 미적용: 레이아웃 유형은 Q1 선택에서 결정됩니다.

---

## 생성 산출물

| Q2 | 파일 | 경로 |
|----|------|------|
| 1~3 (라이브러리 계열) | Layout 클래스 | `app/src/main/java/{package}/{ClassName}.kt` |
| 2~3 (ViewBinding / DataBinding) | Layout XML | `app/src/main/res/layout/layout_{name}.xml` |
| 4 (Vanilla) | Layout 클래스 | `app/src/main/java/{package}/{ClassName}.kt` |

> Q2 = 1(Normal) 및 Q2 = 4(Vanilla) 선택 시 Layout XML은 생성하지 않습니다.

---

## Q1 — 어떤 Layout을 사용하실건가요?

1. `LinearLayout`
2. `FrameLayout`
3. `ConstraintLayout`
4. `RelativeLayout`

---

## Q2 — 어떤 방식으로 구현하실건가요?

Q1 선택에 따라 아래 베이스 클래스 목록을 제시한다.

### Q1 = LinearLayout
1. `BaseLinearLayout` — 단순 생명주기 인식
2. `BaseViewBindingLinearLayout` — 생명주기 인식 + ViewBinding
3. `BaseDataBindingLinearLayout` — 생명주기 인식 + DataBinding
4. `LinearLayout` — 기본 Layout (생명주기 없음)

### Q1 = FrameLayout
1. `BaseFrameLayout` — 단순 생명주기 인식
2. `BaseViewBindingFrameLayout` — 생명주기 인식 + ViewBinding
3. `BaseDataBindingFrameLayout` — 생명주기 인식 + DataBinding
4. `FrameLayout` — 기본 Layout (생명주기 없음)

### Q1 = ConstraintLayout
1. `BaseConstraintLayout` — 단순 생명주기 인식
2. `BaseViewBindingConstraintLayout` — 생명주기 인식 + ViewBinding
3. `BaseDataBindingConstraintLayout` — 생명주기 인식 + DataBinding
4. `ConstraintLayout` — 기본 Layout (생명주기 없음)

### Q1 = RelativeLayout
1. `BaseRelativeLayout` — 단순 생명주기 인식
2. `BaseViewBindingRelativeLayout` — 생명주기 인식 + ViewBinding
3. `BaseDataBindingRelativeLayout` — 생명주기 인식 + DataBinding
4. `RelativeLayout` — 기본 Layout (생명주기 없음)

---

## Layout name 규칙 (Q2 = 2 / 3 시)

- `View` 또는 `Layout` suffix 제거 후 snake_case → 접두사 `layout_`
- suffix 없는 경우 클래스명 전체 snake_case 변환 후 `layout_` 접두사 적용

| 클래스명 예시 | Layout 파일명 |
|-------------|-------------|
| `SettingsItemView` | `layout_settings_item.xml` |
| `ProfileCardLayout` | `layout_profile_card.xml` |
| `LoadingOverlayView` | `layout_loading_overlay.xml` |
| `MyCard` | `layout_my_card.xml` |

---

## Binding 클래스명 규칙 (Q2 = 2 / 3 시)

- `layout_{snake_case}.xml` → `Layout{PascalCase}Binding`

| Layout 파일명 | Binding 클래스명 |
|-------------|----------------|
| `layout_settings_item.xml` | `LayoutSettingsItemBinding` |
| `layout_profile_card.xml` | `LayoutProfileCardBinding` |

---

## Layout 클래스 작성 규칙

### 생성자 패턴

**Q2 = 1 (Normal)**:
```kotlin
class {ClassName} @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : Base{Type}Layout(context, attrs, defStyleAttr)
```

**Q2 = 2 (ViewBinding)**:
```kotlin
class {ClassName} @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : BaseViewBinding{Type}Layout<{BindingClassName}>(
    context, attrs,
    inflate = {BindingClassName}::inflate,
)
```

**Q2 = 3 (DataBinding)**:
```kotlin
class {ClassName} @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : BaseDataBinding{Type}Layout<{BindingClassName}>(
    context, attrs,
    layoutId = R.layout.layout_{name},
)
```

**Q2 = 4 (Vanilla)**:
```kotlin
class {ClassName} @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : {Type}Layout(context, attrs, defStyleAttr)
```

### 콜백 패턴 (Q2 = 2 / 3 시)

초기화는 `onInitBind(binding)` 오버라이드로 처리합니다. 내부 로직은 비워둡니다.

```kotlin
override fun onInitBind(binding: {BindingClassName}) {
    // 초기화 로직
}
```

> Q2 = 1(Normal) / 4(Vanilla) 는 `onInitBind` 없음.

### R 클래스 import 규칙 (Q2 = 3 DataBinding 시)

- `R.layout.*` 참조 시 반드시 명시적 import를 추가합니다.
- app 모듈의 namespace는 `app/build.gradle.kts`의 `namespace` 값을 따릅니다.
- 예) `namespace = "kr.open.library.simpleui_xml"` → `import kr.open.library.simpleui_xml.R`

---

## XML 레이아웃 작성 규칙 (Q2 = 2 / 3 시)

> 참조: `docs/rules/coding_rule/patterns/CODE_PATTERNS_XML.md`
> 참조: `docs/rules/coding_rule/CODE_NAMING_RULE.md`

- XML ID는 camelCase + View 타입 접두사 필수 (예: `tvTitle`, `btnOk`, `ivIcon`)
- Layout 속성(width/height 등) 직접 작성 금지 → Simple UI Style 사용

**ViewBinding 루트 태그 (Q2 = 2)**:
```xml
<merge xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- 자식 뷰 -->
</merge>
```

**DataBinding 루트 태그 (Q2 = 3)**:
```xml
<layout xmlns:android="http://schemas.android.com/apk/res/android">
    <data>
        <!-- 필요 시 variable 추가 -->
    </data>
    <merge>
        <!-- 자식 뷰 -->
    </merge>
</layout>
```

> `<merge>` 사용 권장: 부모 ViewGroup과 동일 타입일 때 뷰 계층 최소화.
> 미리보기가 필요하거나 타입이 다른 경우 직접 ViewGroup 태그 사용 가능.

---

## 다음 단계 안내

- XML에서 사용 시 전체 패키지명(FQCN) 태그 필요: `<kr.open.library.simpleui_xml.{패키지}.{ClassName} .../>`
- `onInitBind()` 내부에 초기화 로직 구현 (Q2 = 2 / 3 시)
