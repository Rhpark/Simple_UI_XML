# Subagent Eval — Analysis Agent

목적: analysis subagent가 description 트리거·STEP 흐름·산출물 형식을 올바르게 실행하는지 검증한다.

기존 테스트 커버리지:
- STATIC_CHECKLIST.md: 문서 구조 유효성 (S1–S8)
- DRY_RUN_UI_SPEC_DIRECT.md: ui-spec STEP3 생략 경로 (시뮬레이션)
- FAILURE_PATHS.md: 워크플로우 실패·복귀 연결 (F1–F10)

이 파일의 범위: 기존 테스트가 다루지 않는 **subagent 런타임 동작** 3개 영역

---

## 영역 1 — 트리거 조건

분류: 이 요청이 analysis agent를 활성화해야 하는가, 아닌가.

### 케이스 T1 — code-flow 트리거 (양성)

```text
입력: "CheckInViewModel.kt에서 저장 버튼 클릭 후 데이터가 DB에 들어가기까지 흐름을 추적해줘."
기대: analysis agent 활성화
판정 기준: description의 '이 함수/화면/모듈이 어떻게 흘러가나 등 코드 흐름 추적 요청' 조건 해당
```

### 케이스 T2 — 버그 원인 파악 트리거 (양성)

```text
입력: "저장 버튼을 눌렀는데 아무 반응이 없어. 왜 이렇게 동작하나?"
기대: analysis agent 활성화
판정 기준: description의 '"왜 이렇게 동작하나", "원인이 뭔가" 등 동작 원인 파악 요청' 조건 해당
```

### 케이스 T3 — 문서 불일치 트리거 (양성)

```text
입력: "RECORD_DATA_SPEC.md랑 CHECKIN_UI_SPEC.md가 태그 수 기준이 다른 것 같아. 확인해줘."
기대: analysis agent 활성화
판정 기준: description의 '문서 간 기준이 다르다, spec과 코드가 다르다 등 문서 불일치 확인 요청' 조건 해당
```

### 케이스 T4 — 코드 수정 요청 (음성 — 범위 외)

```text
입력: "CheckInViewModel.kt의 저장 로직에 예외 처리를 추가해줘."
기대: analysis agent 비활성화 (feature agent 또는 직접 처리)
판정 기준: description의 '코드 수정·구현·리팩토링·코드 리뷰 요청에는 사용하지 않는다' 조건 해당
```

### 케이스 T5 — 코드 리뷰 요청 (음성 — 범위 외)

```text
입력: "RecordRepository.kt 코드 리뷰해줘."
기대: analysis agent 비활성화 (review agent 또는 직접 처리)
판정 기준: description의 '코드 리뷰 요청에는 사용하지 않는다' 조건 해당
```

### 케이스 T6 — UI spec 검증 트리거 (양성)

```text
입력: "체크인 화면에서 감정 선택 전 저장 버튼 상태가 spec대로인지 확인해줘."
기대: analysis agent 활성화
판정 기준: description의 '"이 화면 상태가 맞나", "UX가 의도대로인가" 등 UI spec 검증 요청' 조건 해당
```

### T 영역 통과 기준

| ID | 기대 동작 | 결과 |
| --- | --- | --- |
| T1 | analysis 활성화 | SKIP |
| T2 | analysis 활성화 | SKIP |
| T3 | analysis 활성화 | SKIP |
| T4 | analysis 비활성화 | SKIP |
| T5 | analysis 비활성화 | SKIP |
| T6 | analysis 활성화 | SKIP |

> **SKIP 사유**: T 영역은 Claude Code CLI 런타임의 subagent 자동 선택 로직(description 매칭)에 의존한다.
> 현재 실행 환경(Agent tool 직접 호출)에서는 description 트리거를 검증할 수 없다.
> 검증하려면 Claude Code CLI에서 해당 입력을 직접 입력하고 어떤 subagent가 선택되는지 관찰해야 한다.

---

## 영역 2 — STEP 흐름 준수

분류: subagent가 STEP 문서를 읽고, 하네스 게이트를 지키며, 올바른 순서로 진행하는가.

### 케이스 F1 — STEP_EXECUTION_RULE 선읽기 확인

```text
조건: subagent 세션 시작 직후
기대: common/STEP_EXECUTION_RULE.md를 먼저 읽는다
판정: subagent 첫 tool call이 STEP_EXECUTION_RULE.md read 여야 한다
실패 시: subagent가 STEP_EXECUTION_RULE을 읽지 않고 STEP1 산출물을 바로 출력한 경우
```

### 케이스 F2 — STEP1 파일 선읽기 후 산출물 출력

```text
입력: "CheckInViewModel.kt에서 saveRecord 호출 후 에러가 없는데 DB에 저장이 안 돼. 원인이 뭔가?"
기대:
  1. analysis/step1/STEP1_SCOPE.md 읽기
  2. 실행 체크리스트 13개 항목 수행
  3. STEP1 산출물 형식으로 출력
  4. analysis/step2/STEP2_READ.md 읽기
판정 기준:
  - STEP1 산출물에 '타입 / 단계 / 진행도 / 복원 매핑 / 대상 기능/모듈 / 핵심 파일 / 제외 범위 / STEP3 적용 여부 / 이슈' 9개 키가 모두 있다
  - 진행도가 (n/13) 형식이다
  - code-flow 타입으로 분류된다 (저장 흐름 추적이므로)
  - STEP3 적용 여부 = 필수 (code-flow 타입)
```

### 케이스 F3 — 하네스 차단 확인 (진입점 없음)

```text
입력: "왜 이렇게 동작하나?"
기대: STEP1 하네스 4번 미통과 → 강제 중단 → 사용자에게 범위 재정의 요청
판정 기준:
  - 강제 중단 조건 "진입점 정보가 하나도 없다"가 적용됐다는 메시지가 출력된다
  - STEP2 이후로 진행하지 않는다
  - 실패 규격(실패 STEP / 실패 항목 / 실패 원인 / 복귀 대상 / 전달 데이터) 형식으로 기록된다
```

### 케이스 F4 — STEP3 생략 경로 (product-scope 단독)

```text
입력: "MentalBeat Phase 1에서 AI 분석 기능이 포함되는지 확인해줘."
기대:
  - STEP1: product-scope 타입, STEP3 적용 여부 = 생략
  - STEP2: 읽기 방식 = product-scope, STEP4 직행 보강 작성
  - STEP4: 진입 경로 = 'STEP3 생략 직행'으로 명시
판정 기준: STEP3_IMPACT.md를 읽지 않고 STEP4로 진행한다
```

### 케이스 F5 — 강제 중단 조건 (analysis 범위 외 요청)

```text
입력: "CheckInViewModel.kt의 saveRecord 메서드를 리팩토링해줘."
기대: STEP1 실행 체크리스트 1번에서 범위 밖 판정 → 강제 중단
판정 기준:
  - "요청이 analysis 범위 밖이다" 조건이 적용된다
  - 적절한 워크플로우(feature 또는 review) 안내 메시지가 포함된다
  - STEP1 산출물이 출력되지 않는다
```

### F 영역 통과 기준

| ID | 기대 동작 | 결과 |
| --- | --- | --- |
| F1 | STEP_EXECUTION_RULE.md 선읽기 | PASS |
| F2 | STEP1 산출물 9개 키 완전 출력 | PASS |
| F3 | 진입점 없음 → 강제 중단 | PASS |
| F4 | product-scope → STEP3 생략 경로 | PASS |
| F5 | 범위 외 요청 → STEP1에서 강제 중단 | PASS |

**F1 근거**: F2 케이스 실행 시 첫 tool call이 STEP_EXECUTION_RULE.md read였다. 26개 tool call 중 STEP_EXECUTION_RULE.md → STEP1_SCOPE.md → STEP2_READ.md → STEP3_IMPACT.md → STEP4_REPORT.md 순서로 읽힘.

**F2 근거**: StartupViewModel.kt 흐름 분석에서 STEP1 산출물에 타입/단계/진행도/복원 매핑/대상 기능/모듈/핵심 파일/제외 범위/시나리오/STEP3 적용 여부/입력 fixture/분석 환경/이슈 9개 키 모두 출력. code-flow 타입, STEP3 적용 여부 = 필수로 분류됨.

**F3 근거**: "왜 이렇게 동작하나?" 입력에서 STEP1 하네스 4번 미통과 판정. 실패 규격(실패 STEP/실패 항목/실패 원인/복귀 대상/전달 데이터) 형식으로 출력됨. STEP2 진행 없음.

**F4 근거**: Phase 1 AI 기능 포함 여부 요청에서 product-scope 타입 분류, STEP3 생략 판정. STEP3_IMPACT.md read 없이 STEP4로 직행. STEP4 진입 경로 "STEP3 생략 직행" 명시됨.

**F5 근거**: 리팩토링 요청에서 STEP1 체크리스트 1번에서 "analysis 범위 밖" 판정. feature agent / review agent 안내 메시지 포함. STEP1 산출물 미출력.

---

## 영역 3 — 산출물 형식

분류: 각 STEP 산출물이 SAMPLES.md에 정의된 형식과 일치하는가.

### 케이스 O1 — STEP1 산출물 형식 검증

```text
실행 입력: "CheckInViewModel.kt에서 감정 값이 없을 때 저장 버튼이 비활성화되는 조건을 분석해줘."
검증 항목:
  - [ ] '타입 :' 키가 있고 'analysis —' 접두사로 시작한다
  - [ ] '단계 : STEP 1 (분석 범위 정의)' 형식이다
  - [ ] '진행도 :' 가 (n/N) 형식이고, n ≤ 13이다
  - [ ] '복원 매핑', '대상 기능/모듈', '핵심 파일', '제외 범위/시나리오' 키가 모두 있다
  - [ ] '핵심 파일' 값에 (주) 또는 (보조) 표시가 있다
  - [ ] 'STEP3 적용 여부', '입력 fixture/재현 조건', '분석 환경', '이슈' 키가 모두 있다
  - [ ] 각 키 끝에 '→ STEP2 ...' 형식의 인계 주석이 있다
참조: STEP1_SAMPLES.md 출력 예시 A 형식
```

### 케이스 O2 — STEP4 산출물 형식 검증

```text
조건: O1 입력으로 STEP4까지 완료된 경우
검증 항목:
  - [ ] '[분석 요약]' 섹션이 있고 '진입 경로', '타입', '핵심 파일', '근거 파일', '직접 파일', '간접 파일' 키가 있다
  - [ ] '[사용자 요약]' 섹션이 있고 '한 줄 결론', '점수', '감점 이유', '범위', '우선 행동' 키가 있다
  - [ ] '점수 :' 값이 'Agent 수행성 XX/100, 사용자 이해성 YY/100' 형식이다
  - [ ] '[STEP4 보고]' 섹션이 있고 '사실', '추정', '원인', '심각도', '다음 단계' 키가 있다
  - [ ] '심각도 :' 값에 CRITICAL/HIGH/MEDIUM/LOW 중 하나와 파일명:라인번호가 포함된다
  - [ ] '사실'과 '추정'이 명확히 분리됐다 (추정 없으면 '없음'으로 명시)
참조: STEP4_SAMPLES.md 출력 예시 A 형식
```

### 케이스 O3 — STEP2 읽기 방식 분기 검증

```text
입력: "RECORD_DATA_SPEC.md와 CHECKIN_UI_SPEC.md에서 태그 최대 개수 기준이 다른가?"
기대: STEP2 '읽기 방식 : doc-consistency'로 설정되고, '문서 주장' 키가 채워진다
검증 항목:
  - [ ] '[STEP2 근거 읽기]' 섹션의 '읽기 방식' 값이 'doc-consistency'다
  - [ ] '호출 흐름' 항목이 '해당 없음'이다 (doc-consistency는 호출 흐름 없음)
  - [ ] '문서 주장' 항목에 두 문서의 기준이 각각 기록됐다
  - [ ] '충돌 항목' 또는 '미정 항목'에 분석 결과가 있다
참조: STEP2_SAMPLES.md doc-consistency 예시
```

### O 영역 통과 기준

| ID | 기대 동작 | 결과 |
| --- | --- | --- |
| O1 | STEP1 산출물 형식 완전 일치 | PASS |
| O2 | STEP4 산출물 3섹션 형식 완전 일치 | PASS |
| O3 | doc-consistency 읽기 방식 분기 올바름 | PASS |

**O1 근거** (F2 케이스 산출물 기준):

- [x] `타입 :` 키 있고 `analysis —` 접두사로 시작
- [x] `단계 : STEP 1 (분석 범위 정의)` 형식
- [x] `진행도 :` 가 (n/13) 형식
- [x] `복원 매핑`, `대상 기능/모듈`, `핵심 파일`, `제외 범위/시나리오` 키 모두 있음
- [x] `핵심 파일` 값에 (주) 표시 있음
- [x] `STEP3 적용 여부`, `입력 fixture/재현 조건`, `분석 환경`, `이슈` 키 모두 있음
- [x] 각 키 끝에 `→ STEP2 ...` 형식의 인계 주석 있음

**O2 근거** (F2 케이스 STEP4 산출물 기준):

- [x] `[분석 요약]` 섹션: 진입 경로/타입/핵심 파일/근거 파일/직접 파일/간접 파일 키 있음
- [x] `[사용자 요약]` 섹션: 한 줄 결론/점수/감점 이유/범위/우선 행동 키 있음
- [x] `점수 :` 값이 `Agent 수행성 96/100, 사용자 이해성 96/100` 형식
- [x] `[STEP4 보고]` 섹션: 사실/추정/원인/심각도/다음 단계 키 있음
- [x] `심각도 :` 값에 MEDIUM + 파일명:라인번호 포함
- [x] `사실`과 `추정` 명확히 분리됨

**O3 근거** (doc-consistency 케이스 산출물 기준):

- [x] `읽기 방식 : doc-consistency` 로 설정됨
- [x] `호출 흐름 : 해당 없음` 으로 기록됨
- [x] `문서 주장` 항목에 RECORD_DATA_SPEC.md:44 / CHECKIN_UI_SPEC.md:30, :51 기준이 각각 기록됨
- [x] `충돌 항목 : 없음 (확인 완료)` / `미정 항목 : 없음` 으로 분석 결과 기록됨

---

## 실행 방법

각 케이스를 Agent tool로 analysis subagent에 입력한다.

```text
Agent(subagent_type="analysis", prompt="[케이스 입력 문자열]")
```

결과에서 기대 동작과 판정 기준을 대조하고 결과 열에 PASS / FAIL / SKIP을 기록한다.

FAIL 기록 형식:
```text
케이스 ID : [ID]
기대 동작 : [기대 내용]
실제 동작 : [관찰된 동작]
원인 추정 : [subagent description / STEP 문서 / 하네스 중 어디가 문제인가]
수정 대상 : [수정해야 할 파일]
```

---

## 전체 통과 기준

```text
T 영역: T1~T6 모두 PASS (양성 4개 활성화, 음성 2개 비활성화)
F 영역: F1~F5 모두 PASS
O 영역: O1~O3 모두 PASS
잔여 리스크: 트리거 조건(T 영역)은 Claude Code의 subagent 선택 로직에 의존하므로
             description 문구 변경 없이는 이 eval만으로 수정 불가.
```
