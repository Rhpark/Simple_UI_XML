---
name: BeforePlan
description: |
  /plan 실행 전 사전 정보를 정리하는 스킬. 사용자와 실시간 대화로 PROMPT/PRD/SPEC 3개 문서를
  docs/agents/output/plan/{타임스탬프}/ 에 작성한다. PRD/SPEC은 이후 /plan 모드에 전달된다.
  트리거: /BeforePlan, "plan 준비", "사전 계획 문서", "PRD/SPEC 먼저 작성", "/plan 전 정리".
---

# BeforePlan — /plan 사전 준비 스킬

## 목적

순수 `/plan` 모드에 진입하기 **전에**, 요구사항을 명확히 정리한 PRD/SPEC을 미리 만들어 둔다.
이렇게 하면 /plan 모드는 잘 정의된 입력 위에서 PLAN(구현 계획) 작성에 집중할 수 있다.

## 산출물 (3종)

| 문서     | 역할                             | 편집 방식                                     |
|--------|----------------------------------|-------------------------------------------|
| PROMPT | 사용자와의 대화 기록 (Q&A 로그)      | **append-only** — 그대로 덧붙이기만, 요약/재해석/삭제 금지 |
| PRD    | 요구사항 정제본 (성공기준·범위·제약)   | **편집 가능** — 사용자 발언에 근거한 갱신은 자유. 발언 없이 임의 변경 금지 |
| SPEC   | 기술 명세 (코드 탐색 기반)           | **편집 가능** — 기술적 수정은 자율, 근거(파일:라인) 필수      |

- PROMPT는 기록용이며 /plan에 전달하지 않는다. **PRD/SPEC만 /plan에 전달**한다.

## 저장 위치 / 명명 규칙

- 폴더: `docs/agents/output/plan/{타임스탬프}/`
- 타임스탬프 형식: `yyMMdd_HHmmss` (예: `260604_121053`)
  - **생성 명령으로만 만든다** (추측·임의 입력 금지): `Get-Date -Format "yyMMdd_HHmmss"` (PowerShell)
  - 스킬 시작 시 1회 생성하여 3개 파일에 일관 적용
- 파일명: 타임스탬프 접두 — `{TS}_PROMPT.md`, `{TS}_PRD.md`, `{TS}_SPEC.md`
  - 예: `260604_121053_PROMPT.md`

## 큰 흐름

```
1. 타임스탬프 생성 + 폴더 생성
2. 사용자와 대화 → PROMPT(로그)와 PRD(정제본)를 병행 기록
   - 종료 게이트: PRD가 빈틈없음 (성공기준·범위·제약이 충분히 채워짐)
3. PRD 기반으로 코드를 직접 탐색하며 SPEC 작성
   - 탐색 중 오류/모호 발견 시 아래 "분기 기준"에 따라 처리
4. 종료 조건 충족 시 3개 문서 확정 저장
4.5 PRD/SPEC 규격 검증 (① — PLAN 없음, 아래 "검증 단계(①)" 절)
   - ps1 `-Stage prd_spec` → PASS면 plan_format_check `Stage=prd_spec`
   - 이상 시 → 종료 조건 미충족으로 보고, 단계 2/3로 돌아가 수정 후 재검증
5. (① 검증 PASS 후) 사용자에게 /plan 실행 안내
   - 생성한 `docs/agents/output/plan/{TS}/` 경로와 PRD/SPEC 파일명을 **명시 출력**한다.
   - 이 `{TS}` 경로가 MakePlan(/plan)의 입력이며, 세션이 길어져 컨텍스트를 잃을 경우 사용자가 이 경로를 그대로 전달하면 된다고 안내한다.
```

## 분기 기준 (SPEC 작성 중 문제 발견 시)

| 상황                                          | 처리                                                    | 비고 |
|----------------------------------------------|-------------------------------------------------------| --- |
| 코드와 SPEC의 기술적 불일치 (사용자 질문 불필요)    | **SPEC만 수정**                                        | PROMPT/PRD 변동 없음 |
| 답이 기술 수준인 질문                            | 질문 → PROMPT 추가 → **SPEC 수정**                      | PRD 변동 없음 |
| 답이 요구사항을 건드리는 질문                     | 질문 → 답변(=근거) → PROMPT 추가 + PRD 수정 → SPEC 수정 | 답변이 곧 근거 |

- 판단 기준: "이게 요구사항(무엇/왜)을 바꾸나?" → 예: PRD 경로 / 아니오: SPEC 경로
- PRD 변경은 **사용자 발언이 뒷받침될 때만** 한다. 발언이 없으면(코드 탐색 발견 등) 먼저 질문해 답을 받고, 그 답을 근거로 반영한다. 별도 승인 절차는 두지 않는다 — 답변이 곧 승인이다.

## 종료 조건 (무한 루프 방지)

아래가 모두 충족되면 완료:
- PRD가 빈틈없음 (성공기준·범위·제약)
- SPEC에 코드로 미확인된 빈틈 없음 (모든 주장에 파일:라인 근거)
- 미답 질문 없음
- **PRD/SPEC 규격 검증(①) PASS** — 주관 판단("빈틈없음")에 더해 객관 게이트로 확인 (아래 "검증 단계(①)")

## 검증 단계 (①: PRD/SPEC, PLAN 미생성 시점)

3개 문서 저장 후, 사용자에게 안내하기 **전에** PRD/SPEC 규격을 기계(ps1) + 에이전트(format_check)로 검증한다.
PLAN은 BeforePlan에서 만들지 않으므로 검증 대상이 아니다 → 항상 `Stage=prd_spec`.

1. `powershell -ExecutionPolicy Bypass -File .claude/hooks/plan_artifact_check.ps1 -TS {TS} -Stage prd_spec` 실행
   - ps1이 PRD/SPEC의 **존재·타임스탬프·라인 수**를 검증하고 `{TS}/_verification.txt`를 생성한다.
2. ps1 PASS면 plan_format_check SubAgent를 `Stage=prd_spec`로 호출 → PRD/SPEC **필수 섹션·충실도** 검증.
   - ps1 FAIL이면 2번(섹션 검증)을 건너뛰고 4번으로 간다(fail-fast).
3. 둘 다 PASS → 종료 조건 충족. 사용자에게 /plan 안내(큰 흐름 5).
4. FAIL → [보완 필요 항목]에 따라 PRD/SPEC을 수정(대화/코드 탐색)하고 1번부터 재검증한다.
   - 무한 루프 방지: 동일 FAIL이 반복되면 사용자에게 보고 후 결정 대기.

## 문서별 상세 작성법 (해당 시점에 참조)

각 문서를 **실제로 작성할 때** 해당 guide를 읽어 규칙을 따른다.

- PROMPT 작성: `.claude/skills/BeforePlan/references/prompt_guide.md`
- PRD 작성: `.claude/skills/BeforePlan/references/prd_guide.md`
- SPEC 작성: `.claude/skills/BeforePlan/references/spec_guide.md`
