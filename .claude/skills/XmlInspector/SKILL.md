---
name: XmlInspector
description: Android XML 레이아웃 파일을 분석하여 Style 준수, 명명 규칙, 하드코딩, 성능 저하, 접근성, 불필요한 속성을 점검하고 수정안을 제시한다. "XML 검사", "레이아웃 분석", "XML 점검" 등의 요청 시 사용.
disable-model-invocation: true
argument-hint: "파일경로 or 레이아웃 파일명"
---

# XmlInspector Skill

이 프로젝트(SimpleUI_XML)의 XML 레이아웃 파일을 분석하고 점검한다.
반드시 아래 규칙을 따른다.

## 대상 코드 결정 우선순위

1. `$ARGUMENTS` 가 있으면 → 해당 파일 경로 또는 파일명을 대상으로 한다.
2. `$ARGUMENTS` 가 없으면 → 현재 IDE에서 선택된 코드(`ide_selection`)를 대상으로 한다.
3. `ide_selection` 도 없으면 → 사용자에게 대상 파일 경로를 요청한 후 진행한다.

---

## 작업 순서

### STEP 1. 대상 파일 읽기

대상 XML 파일을 직접 읽는다.
아래 규칙 파일도 함께 읽어 기준으로 삼는다.

- `docs/rules/coding_rule/patterns/CODE_PATTERNS_XML.md`
- `docs/rules/coding_rule/CODE_NAMING_RULE.md`

파일 읽기 완료 후, 아래 형식으로 반드시 출력한 뒤 STEP 2로 진행한다.
```
> 대상: {파일 경로}
> 루트 레이아웃: {레이아웃 타입}
> ConstraintLayout 사용: [예 / 아니오]
```

### STEP 2. 검사 실행

아래 항목을 순서대로 점검한다. 문제가 발견되어도 모든 항목을 끝까지 수행한다.

#### 검사 1. Simple UI Style 준수
- `layout_width` / `layout_height` 를 직접 작성한 뷰 감지
- `style="@style/..."` 미사용 뷰 감지
- 올바른 Style 적용 예시 제시

#### 검사 2. 명명 규칙 (View ID)
- View ID가 camelCase + View 타입 접두사 규칙을 따르는지 확인
  - 예: `TextView` → `tvTitle`, `Button` → `btnConfirm`, `RecyclerView` → `rvList`
- 상호작용 가능한 뷰(`Button`, `EditText`, `CheckBox`, `RadioButton` 등)에 ID가 없는 경우 감지

#### 검사 3. 하드코딩
- 색상 직접 작성 감지 (`android:textColor="#FF0000"` 등) → `@color/` 참조 권장
- 크기 직접 작성 감지 (`android:textSize="16sp"` 등) → `@dimen/` 참조 권장
- 문자열 직접 작성 감지 (`android:text="확인"` 등) → `@string/` 참조 권장

#### 검사 4. 성능 저하 요소
- 불필요한 뷰 중첩 감지 (예: 자식이 하나뿐인 LinearLayout 감지)
- 제거 가능한 불필요한 컨테이너 감지
- overdraw 가능성 (불필요한 background 중복 설정)
- 각 항목에 수정안 제시

#### 검사 5. 접근성
- `ImageView` / `ImageButton` 에 `contentDescription` 누락 여부
- `importantForAccessibility="no"` 가 적절히 설정됐는지 여부
- `android:onClick` 속성이 있는 뷰에 `clickable="true"` / `focusable="true"` 누락 여부

#### 검사 6. 불필요한 속성
- 기본값과 동일한 속성이 명시된 경우 결과 보고에 속성명과 값을 명확히 표시하고 제거를 권장한다. 실제 제거는 사용자가 판단한다.
  - 예: `android:visibility="visible"`, `android:orientation="horizontal"` (LinearLayout 기본값)
  - 예: `android:enabled="true"`, `android:clickable="false"` (기본값)
- ConstraintLayout 자식에 `layout_weight` 포함 style 사용 감지 → 제거 권장 (ConstraintLayout에서 `layout_weight`는 무시됨)
- 적용된 style에 이미 정의된 속성을 인라인으로 중복 선언한 경우 감지
  - 예: center gravity style에 `android:gravity="center"` 중복 선언

#### 검사 7. ConstraintLayout 제약 (ConstraintLayout 사용 시에만)
- 연결되지 않은 constraint 감지
- 누락된 constraint 감지

### STEP 3. SubAgent 위임 (해당 조건일 때만)

아래 조건에 해당하면 SubAgent(general-purpose)를 호출하여 분석을 위임한다.

| 조건 | 분석 내용 |
|---|---|
| 사용자가 명시적으로 중복 구조 검사를 요청한 경우 | SubAgent가 `res/layout/` 디렉터리 전체를 스캔하여 중복 뷰 구조 감지 → `<include>` / `<merge>` 활용 제안 |

> 해당 조건이 없으면 이 STEP은 건너뛴다.

### STEP 4. 결과 보고

아래 형식으로 결과를 보고한다.

```
✅ XmlInspector 완료
- 파일: {경로}
- 총 이슈: N개

[검사 1] Simple UI Style 준수
  · {뷰ID 또는 위치} - {문제 내용} → {수정안}

[검사 2] 명명 규칙
  · {뷰ID 또는 위치} - {문제 내용} → {수정안}

[검사 3] 하드코딩
  · {뷰ID 또는 위치} - {속성명}: {값} → {수정안}

[검사 4] 성능 저하
  · {뷰ID 또는 위치} - {문제 내용} → {수정안}

[검사 5] 접근성
  · {뷰ID 또는 위치} - {문제 내용} → {수정안}

[검사 6] 불필요한 속성
  · {뷰ID 또는 위치} - 속성: {속성명}="{값}" → 제거 권장 (사용자 판단)

[검사 7] ConstraintLayout 제약  ← ConstraintLayout 사용 시에만 표시
  · {뷰ID} - {문제 내용} → {수정안}

이슈 없는 검사 항목은 생략한다.

[SubAgent] include/merge 제안  ← 중복 구조 검사 요청 시에만 표시
  · {중복 파일 목록} → {수정안}
```
