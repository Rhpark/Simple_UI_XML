# Create Skill

## 0. 목표
사용자가 FQCN을 입력하면, 타입을 자동 감지하여 해당 타입에 맞는 파일들을 **한 번에** 생성합니다.

**호출 예시:**
```
# suffix로 타입 자동 감지
/Create kr.open.library.simpleui_xml.feature.MainActivity
/Create kr.open.library.simpleui_xml.feature.MyListFragment
/Create kr.open.library.simpleui_xml.feature.ConfirmDialogFragment

# suffix 불명확 → 타입 질문 후 진행
/Create kr.open.library.simpleui_xml.feature.D
/Create kr.open.library.simpleui_xml.feature.UserCard
```

- 지원 타입: `Activity` / `Fragment` / `DialogFragment` / `Adapter`

> 원칙: **덮어쓰지 않는다.** 기존 파일이 있으면 중단하고, 충돌 내역을 명확히 안내한다.

---

## 1. 실행 흐름

### STEP 0. 타입 감지 + 클래스명 검증 (select 파일 로드 전)
FQCN의 클래스명 suffix를 확인하여 타입을 결정한다.
**이 단계에서 타입과 클래스명을 확정한 후에만 STEP 1로 진행한다.**

#### 0-1. 타입 감지

| 클래스명 suffix | 감지 타입 |
|---------------|---------|
| `...DialogFragment` | `DialogFragment` |
| `...Fragment` | `Fragment` |
| `...Activity` | `Activity` |
| `...Adapter` | `Adapter` |
| 불명확 | 사용자에게 1회 질문 |

> `DialogFragment`는 `Fragment`보다 먼저 판단한다. (`ConfirmDialogFragment`가 Fragment로 오분류되지 않도록)

> 타입이 불명확한 경우 (예: `a.b.c.D`, `a.b.c.UserCard`) 아래 질문을 1회 한다:
> "어떤 타입으로 생성할까요? 1) Activity 2) Fragment 3) DialogFragment 4) Adapter"

#### 0-2. 클래스명 suffix 검증
타입이 확정된 후, 클래스명에 해당 타입의 suffix가 없으면 suffix를 추가한 이름을 제안하고 확정한다.

| 입력 클래스명 | 확정 타입 | suffix 없음 → 제안 |
|------------|---------|------------------|
| `D` | Activity | `DActivity` 로 생성 |
| `UserCard` | Fragment | `UserCardFragment` 로 생성 |
| `Confirm` | DialogFragment | `ConfirmDialogFragment` 로 생성 |

> suffix가 이미 올바른 경우(예: `MainActivity`, `MyFragment`) 그대로 사용한다.
> suffix 추가 시 사용자에게 `"클래스명을 {제안명}으로 생성합니다."` 라고 안내하고 진행한다. (재확인 질문 불필요)

### STEP 1. select 파일 로드
STEP 0에서 확정된 타입의 파일을 **반드시** 읽은 후 진행한다.

| 확정 타입 | 로드할 파일 |
|----------|-----------|
| `Activity` | `.claude/skills/Create/selectActivity.md` |
| `Fragment` | `.claude/skills/Create/selectFragment.md` |
| `DialogFragment` | `.claude/skills/Create/selectDialogFragment.md` |
| `Adapter` | `.claude/skills/Create/selectAdapter.md` |

### STEP 2. 대화형 질문 진행
- **Q1**: select 파일에 정의된 베이스 클래스 선택 질문
- **Q2**: 아래 공통 ViewModel 선택 질문 — **단, `Adapter` 타입은 예외. select 파일의 전용 Q2를 사용한다.**
- **Q_Layout**: 아래 공통 루트 레이아웃 선택 질문 (Section 3-1 참조)
- 추가 질문: select 파일에 정의된 타입별 추가 질문이 있는 경우에만

### STEP 3. 충돌 체크
질문이 완료된 후, **파일 생성 직전**에 생성 예정 파일 전체의 존재 여부를 확인한다.
하나라도 존재하면 **즉시 중단**하고 아래 내용을 안내한다.
- 충돌 파일 목록
- 충돌 원인 (이미 존재)
- 대안 (클래스명 변경 제안 등)

### STEP 4. 파일 생성
충돌 없음이 확인된 후, select 파일의 생성 규칙 + 아래 공통 규칙을 합산하여 파일을 생성한다.

### STEP 5. 결과 보고
아래 공통 결과 보고 형식으로 보고한다.

---

## 2. 공통 원칙

### 2.1 모듈 고정
- 생성 대상 모듈: **`app` 모듈 고정**
- 라이브러리 모듈(`simple_core`, `simple_xml`)에는 생성하지 않는다.

### 2.2 패키지 디렉터리 자동 생성
- 경로 중간의 패키지 디렉터리가 없어도 파일 생성 시 자동으로 함께 생성한다.

---

## 3. Q2 — ViewModel 베이스 선택 (공통)
다음 중 ViewModel은 어떤 베이스를 사용할까요?

1. `BaseViewModel`
2. `BaseViewModelEvent` → `*VmEvent` sealed interface 자동 생성
3. `ViewModel`
4. `ViewModel 사용 안함`

> 규칙:
- 4번이면 ViewModel 파일을 생성하지 않고 컴포넌트에서도 ViewModel 연결 코드를 넣지 않는다.
- 1~3번이면 `{ClassName}Vm`을 생성하고 컴포넌트에서 적절히 연결한다.
- 2번이면 추가로 `{ClassName}VmEvent` sealed interface를 생성한다.

---

## 3-1. Q_Layout — 루트 레이아웃 선택 (공통)
루트 레이아웃은 어떤 타입으로 생성할까요?

1. `LinearLayout` (vertical)
2. `LinearLayout` (horizontal)
3. `ConstraintLayout`
4. `FrameLayout`

> 적용 스타일 (Simple UI Style 기반):

| 선택 | Activity / Fragment / DialogFragment | Adapter item |
|------|--------------------------------------|-------------|
| LinearLayout vertical | `style="@style/Layout.AllMatch.Vertical"` | `style="@style/Layout.MatchWrap.Vertical"` |
| LinearLayout horizontal | `style="@style/Layout.AllMatch.Horizontal"` | `style="@style/Layout.MatchWrap.Horizontal"` |
| ConstraintLayout | `style="@style/Layout.AllMatch"` | `style="@style/Layout.MatchWrap"` |
| FrameLayout | `style="@style/Layout.AllMatch"` | `style="@style/Layout.MatchWrap"` |

---

## 4. 공통 Layout 작성 규칙
> 참조: `docs/rules/coding_rule/patterns/CODE_PATTERNS_XML.md`
> 참조: `docs/rules/coding_rule/CODE_NAMING_RULE.md`

- Layout 속성(width/height/orientation 등) 직접 작성 금지 → Simple UI Style 사용
- XML ID는 camelCase + View 타입 접두사 필수 (예: TextView → `tvTitle`)

---

## 5. 공통 ViewModel 작성 규칙
> 참조: `docs/rules/coding_rule/patterns/CODE_PATTERNS_MVVM.md`

- 네이밍: `{ClassName}Vm` (예: `MainActivityVm`, `MyFragmentVm`)
- 내부 로직은 비워두고 상속만 정확히 맞춘다.

| Q2 선택 | 선언 |
|---------|------|
| `BaseViewModel` | `class {ClassName}Vm : BaseViewModel()` |
| `BaseViewModelEvent` | `class {ClassName}Vm : BaseViewModelEvent<{ClassName}VmEvent>()` |
| `ViewModel` | `class {ClassName}Vm : ViewModel()` |

---

## 6. 공통 VmEvent 작성 규칙 (Q2 = BaseViewModelEvent 시에만 생성)
> 참조: `docs/rules/coding_rule/patterns/CODE_PATTERNS_MVVM.md`

- 네이밍: `{ClassName}VmEvent` (예: `MainActivityVmEvent`, `MyFragmentVmEvent`)
- `sealed interface`로 선언한다. (`sealed class` 사용 금지)
- 기본 항목으로 `data object Dump`를 반드시 포함한다.

```kotlin
sealed interface {ClassName}VmEvent {
    data object Dump : {ClassName}VmEvent
}
```

---

## 7. 결과 보고 형식 (공통)
스킬 실행 결과는 아래 형식을 따른다.

1) 결정된 옵션 요약
- 타입 / Q1 선택 / Q2 선택 / 타입별 추가 선택

2) 생성/수정 파일 목록
- 생성된 파일 경로를 모두 나열

3) 주의/다음 단계(최대 2개)
- select 파일에 정의된 타입별 안내 문구 사용
