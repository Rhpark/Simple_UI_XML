# Subagent Eval — Review Agent

목적: `review.md` subagent가 description 트리거·진입 분기·STEP 흐름·산출물 형식을 올바르게 실행하는지 검증한다.

기존 테스트 커버리지:
- STATIC_CHECKLIST.md: 문서 구조 유효성 (S1–S9)
- FAILURE_PATHS.md: 워크플로우 실패·복귀 연결 (F1–F10)

이 파일의 범위: 기존 테스트가 다루지 않는 **subagent 런타임 동작** 3개 영역

---

## 영역 1 — 트리거 조건

분류: 이 요청이 review agent를 활성화해야 하는가, 아닌가.

### 케이스 T1 — 리뷰 명시 요청 (양성)

```text
입력: "RecordRepository.kt 코드 리뷰해줘."
기대: review agent 활성화
판정 기준: description의 '"코드 리뷰해줘", "이 코드 리뷰가 필요해" 등 리뷰 명시 요청' 조건 해당
```

### 케이스 T2 — 워크플로우 명시 실행 (양성)

```text
입력: "analysis 결과 가지고 리뷰 워크플로우 실행해줘."
기대: review agent 활성화
판정 기준: description의 '"리뷰 워크플로우 실행" 등 리뷰 명시 요청' 조건 해당
```

### 케이스 T3 — 코드 검증 요청 (양성)

```text
입력: "로직 안정성 검토해줘."
기대: review agent 활성화
판정 기준: description의 '"로직 안정성 검토해줘" 등 코드 검증 요청' 조건 해당
```

### 케이스 T4 — 코드 흐름 분석 요청 (음성 — analysis 대상)

```text
입력: "CheckInViewModel.kt에서 saveRecord 호출 후 DB 저장까지 흐름을 추적해줘."
기대: review agent 비활성화 (analysis agent 대상)
판정 기준: description에 명시된 활성화 조건(리뷰 명시 요청, 코드 검증 요청)에 해당하지 않는다.
          흐름 추적은 코드 수정·구현이 아니라 원인 파악·구조 분석 요청이므로 analysis 워크플로우 대상이다.
```

### 케이스 T5 — 코드 수정 요청 (음성 — 범위 밖)

```text
입력: "RecordRepository.kt에 예외 처리 추가해줘."
기대: review agent 비활성화 (feature agent 또는 직접 처리)
판정 기준: description의 '코드 수정·구현·리팩토링은 이 워크플로우 범위 밖이다' 조건 해당
```

### T 영역 통과 기준

| ID | 기대 동작 | 결과 |
| --- | --- | --- |
| T1 | review 활성화 | SKIP |
| T2 | review 활성화 | SKIP |
| T3 | review 활성화 | SKIP |
| T4 | review 비활성화 | SKIP |
| T5 | review 비활성화 | SKIP |

> **SKIP 사유**: T 영역은 Claude Code CLI 런타임의 subagent 자동 선택 로직(description 매칭)에 의존한다.
> 현재 실행 환경(Agent tool 직접 호출)에서는 description 트리거를 검증할 수 없다.
> 검증하려면 Claude Code CLI에서 해당 입력을 직접 입력하고 어떤 subagent가 선택되는지 관찰해야 한다.

---

## 영역 2 — STEP 흐름 준수

분류: subagent가 STEP 문서를 읽고, 진입 분기를 확인하며, 하네스 게이트를 지키고, 올바른 순서로 진행하는가.

### 케이스 F1 — STEP_EXECUTION_RULE 선읽기 확인

```text
조건: subagent 세션 시작 직후
기대: common/STEP_EXECUTION_RULE.md를 먼저 읽는다
판정: subagent 첫 tool call이 STEP_EXECUTION_RULE.md read여야 한다
실패 시: subagent가 STEP_EXECUTION_RULE을 읽지 않고 STEP1 산출물을 바로 출력한 경우
```

### 케이스 F2 — 분기 A (analysis 산출물 있음) → STEP1 즉시 진입

```text
입력: analysis STEP4 산출물([분석 요약] · [사용자 요약] · [STEP4 보고])을 포함한 리뷰 요청
기대:
  1. common/STEP_EXECUTION_RULE.md 읽기
  2. review.md 분기 A 확인 → STEP1 즉시 진입
  3. review/step1/STEP1_FUNC.md 읽기
  4. STEP1 산출물 형식으로 출력
판정 기준:
  - 분기 판정 텍스트 없이 STEP1 산출물 블록이 바로 출력된다
  - [분석 입력] + [기능 검증] 섹션이 모두 있다
  - [기능 검증] 검증 대상에 파일명:라인번호와 진입점이 기록됐다
```

### 케이스 F3 — 분기 B (analysis 산출물 없음) → 강제 중단

```text
입력: "RecordRepository.kt 코드 리뷰해줘." (analysis 산출물 없이)
기대: review.md 분기 B 적용 → 강제 중단
판정 기준:
  - "review 전에 analysis 워크플로우를 먼저 완료해야 한다" 메시지가 출력된다
  - STEP1 산출물이 출력되지 않는다
  - STEP2 이후로 진행하지 않는다
```

### 케이스 F4 — CRITICAL finding 발견 → 즉시 중단 + 별도 블록 보고

```text
조건: STEP 진행 중 CRITICAL finding(null 역참조 크래시 등)이 발견된 경우
기대:
  1. 현재까지 완성된 산출물을 먼저 출력한다
  2. CRITICAL finding을 별도 블록으로 사용자에게 보고한다
  3. 사용자 응답을 받기 전까지 다음 STEP 진입을 보류한다
판정 기준:
  - 산출물 출력 → 별도 CRITICAL 블록 순서가 지켜진다
  - 사용자 응답 없이 STEP3/4/5로 진행하지 않는다
  - CRITICAL finding에 파일명:라인번호 + 코드 근거 + 사용자 영향이 기록됐다
```

### 케이스 F5 — finding 파일명:라인번호 없음 → 해당 STEP 재수행

```text
조건: 특정 STEP에서 finding을 기록했으나 파일명:라인번호가 "(코드 미확인)"으로 기록된 경우
기대: 해당 STEP 하네스 미통과 → 실패 규격 기록 후 STEP 재수행
판정 기준:
  - 실패 규격(실패 STEP / 실패 항목 / 실패 원인 / 복귀 대상 / 전달 데이터) 형식으로 출력된다
  - 파일명:라인번호가 확인될 때까지 다음 STEP으로 진행하지 않는다
```

### F 영역 통과 기준

| ID | 기대 동작 | 결과 |
| --- | --- | --- |
| F1 | STEP_EXECUTION_RULE.md 선읽기 | SKIP |
| F2 | 분기 A → STEP1 즉시 진입, 산출물 형식 완전 출력 | SKIP |
| F3 | 분기 B → 강제 중단, analysis 먼저 완료 고지 | SKIP |
| F4 | CRITICAL finding → 산출물 출력 후 별도 블록 보고, 대기 | SKIP |
| F5 | finding 위치 미확인 → 실패 규격 출력 후 STEP 재수행 | SKIP |

> **SKIP 사유**: F 영역은 실제 review subagent 런타임 실행 결과를 기반으로 검증해야 한다.
> 현재는 SAMPLES 예시 기반의 문서 수준 예상 동작만 확인됐다.
> 검증하려면 Agent tool로 review subagent를 각 케이스 입력으로 실행하고 실제 산출물을 관찰해야 한다.

---

## 영역 3 — 산출물 형식

분류: 각 STEP 산출물이 SAMPLES.md에 정의된 형식과 일치하는가.

### 케이스 O1 — STEP1 산출물 형식 검증

```text
실행 입력: analysis STEP4 산출물을 포함한 리뷰 요청
검증 항목:
  - [ ] 첫 줄이 '현재 단계: 리뷰 - STEP1 기능 검증'이다
  - [ ] '[분석 입력]' 섹션이 있고 '직접 파일', '영향 계층', '기준 문서' 키가 있다
  - [ ] '[기능 검증]' 섹션이 있고 '검증 대상', '검증 맥락', '엣지 케이스', '발견 사항', '이관 항목' 키가 있다
  - [ ] '검증 대상'에 파일명:라인번호 / 진입점 형식이 있다
  - [ ] '엣지 케이스'에 null/empty, 0개 데이터, 단일 항목, 최대 개수, 실패 응답 5개 항목이 있다
  - [ ] '이관 항목'에 STEP2/STEP3/STEP4 소관이 각각 기록됐다 (없으면 "없음")
참조: STEP1_SAMPLES.md 예시 A 형식
```

### 케이스 O2 — STEP5 산출물 형식 검증

```text
조건: O1 입력으로 STEP5까지 완료된 경우
검증 항목:
  - [ ] '[리뷰 요약]' 섹션이 있고 '검증 대상', '단계별 발견 수', '한 줄 결론', '점수', '감점 이유', '확인 범위', '우선 행동' 키가 있다
  - [ ] '점수' 값이 'Agent 수행성 XX/100, 사용자 이해성 YY/100' 형식이다
  - [ ] '단계별 발견 수'에 STEP1~4 각 finding 수가 기록됐다
  - [ ] '[리뷰 보고]' 섹션이 있고 '발견 사항', '중복 제거', '도구 실행', '다음 단계' 키가 있다
  - [ ] '발견 사항'의 각 finding에 등급, 위치(파일명:라인번호), 근거, 영향, 행동이 기록됐다
  - [ ] finding이 CRITICAL → HIGH → MEDIUM → LOW 순으로 정렬됐다
  - [ ] '행동'이 feature / refactor / 무시 가능 중 하나다
참조: STEP5_SAMPLES.md 예시 A 형식
```

### 케이스 O3 — CRITICAL finding 별도 블록 형식 검증

```text
조건: STEP 진행 중 CRITICAL finding이 발견된 경우
검증 항목:
  - [ ] 현재까지 완성된 산출물이 먼저 출력된다
  - [ ] 별도 블록에 '[CRITICAL]' 레이블이 있다
  - [ ] 별도 블록에 위치(파일명:라인번호), 근거(코드 한 줄), 영향(사용자 영향), 조치가 기록됐다
  - [ ] 사용자 응답을 명시적으로 기다린다는 안내가 있다
참조: STEP2_SAMPLES.md 예시 C 형식
```

### O 영역 통과 기준

| ID | 기대 동작 | 결과 |
| --- | --- | --- |
| O1 | STEP1 산출물 형식 완전 일치 | SKIP |
| O2 | STEP5 산출물 2섹션 형식 완전 일치 | SKIP |
| O3 | CRITICAL finding 별도 블록 형식 준수 | SKIP |

> **SKIP 사유**: O 영역은 실제 review subagent 런타임 실행 산출물을 기반으로 검증해야 한다.
> 현재는 SAMPLES 예시가 정의한 형식과 문서 수준 일치 여부만 확인됐다.
> 검증하려면 각 케이스를 실행하고 실제 산출물의 섹션·키·형식을 항목별로 대조해야 한다.

---

## 실행 방법

각 케이스를 Agent tool로 review subagent에 입력한다.

```text
Agent(subagent_type="review", prompt="[케이스 입력 문자열]")
```

결과에서 기대 동작과 판정 기준을 대조하고 결과 열에 PASS / FAIL / SKIP을 기록한다.

FAIL 기록 형식:
```text
케이스 ID : [ID]
기대 동작 : [기대 내용]
실제 동작 : [관찰된 동작]
원인 추정 : [review.md description / STEP 문서 / 하네스 중 어디가 문제인가]
수정 대상 : [수정해야 할 파일]
```

---

## 전체 통과 기준

```text
T 영역: T1~T5 모두 SKIP — Claude Code CLI 런타임 description 매칭에 의존, 직접 실행으로만 검증 가능
F 영역: F1~F5 모두 SKIP — 실제 review subagent 런타임 실행 후 채울 것
O 영역: O1~O3 모두 SKIP — 실제 review subagent 런타임 실행 후 채울 것
잔여 리스크: T/F/O 전 영역이 실제 런타임 실행 미완료 상태다.
             Agent tool로 각 케이스를 실행하고 결과를 관찰한 뒤 PASS / FAIL로 업데이트해야 한다.
```
