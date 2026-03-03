# XML 레이아웃 패턴 규칙

## 규칙

- XML 레이아웃 작성 시 Simple UI Style을 우선 사용한다
- layout_width / layout_height / orientation / gravity 등 반복 속성 직접 작성 금지
- Weight 사용 시 layout_width="0dp" + layout_weight 직접 작성 금지

## 스타일 네이밍 패턴

```
[ViewType].[WidthHeight].[추가속성].[추가속성]
```

| ViewType | WidthHeight | 추가속성 예시 |
|----------|------------|--------------|
| Layout   | MatchWrap, WrapWrap, AllMatch | Vertical, Horizontal, Center |
| View     | MatchWrap, AllWrap, WeightWrap | - |
| TextView | MatchWrap, AllWrap | Bold, Normal, Center |
| Button   | MatchWrap, WeightWrap | Bold |
| EditText | MatchWrap | Email, Number, Center |
| RecyclerView | MatchWrap | LinearLayoutManager.Vertical |

## 심각도 기준

- MEDIUM: Style 미사용, layout_width/height 반복 직접 작성

## 예시

### 레이아웃 속성 직접 작성

❌ BAD
```xml
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:gravity="center">
    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Title" />
</LinearLayout>
```

✅ GOOD
```xml
<LinearLayout style="@style/Layout.MatchWrap.Vertical.Center">
    <TextView
        style="@style/TextView.MatchWrap"
        android:text="Title" />
</LinearLayout>
```

---

### Weight 직접 작성

❌ BAD
```xml
<Button
    android:layout_width="0dp"
    android:layout_height="wrap_content"
    android:layout_weight="1"
    android:text="확인" />
```

✅ GOOD
```xml
<Button
    style="@style/Button.WeightWrap"
    android:text="확인" />
```

---

### RecyclerView LayoutManager 설정

❌ BAD
```xml
<androidx.recyclerview.widget.RecyclerView
    android:layout_width="match_parent"
    android:layout_height="wrap_content" />
```

✅ GOOD
```xml
<androidx.recyclerview.widget.RecyclerView
    style="@style/RecyclerView.MatchWrap.LinearLayoutManager.Vertical"
    android:id="@+id/rcvList" />
```
