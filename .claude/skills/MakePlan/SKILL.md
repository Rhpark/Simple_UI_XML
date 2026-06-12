---
name: MakePlan
description: |
  PRD/SPEC을 입력으로 구현 계획(PLAN)을 작성하는 절차. /plan 진입 시 mainAgent가 이 절차를 따른다.
  선행 PRD/SPEC을 알 때만 적용(세션 내 BeforePlan 산출물 또는 사용자 명시 참조), 모르면 기본 /plan 수행.
  PRD/SPEC의 생성·포맷은 BeforePlan이 단일 소유한다. EnterPlanMode/ExitPlanMode와 plan_format_check로 검증한다.
  트리거: /plan (PRD/SPEC을 아는 경우), /MakePlan, "구현 계획 작성", "PLAN 작성".
---

# MakePlan — 구현 계획(PLAN) 작성 절차

> `/plan` 진입 시 mainAgent가 따르는 절차다. `/plan`의 내장 plan 모드(Explore/Plan SubAgent 자동 호출,
> EnterPlanMode/ExitPlanMode 등등)는 그대로 사용하며, 이 문서는 그 위에서 따를 규칙·산출물 형식을 정의한다.

> **적용 조건 (중요)**: 이 절차는 **선행 PRD/SPEC 산출물을 알 때만** 적용한다.
> - "안다" = 사용자가 명시적으로 참조했거나(경로 / "방금 만든 PRD·SPEC"), 같은 세션에서 BeforePlan이 생성한 `{TS}` 폴더가 있음.
> - 모르면 이 절차를 따르지 않고 **기본 /plan**(이 절차 비적용 — PRD/SPEC/PLAN 산출물 생성 없음)으로 수행한다.
> **PRD/SPEC의 생성·포맷은 BeforePlan 스킬(.claude/skills/BeforePlan/)이 단일 소유한다. 이 절차는 PRD/SPEC을 생성하지 않는다.**

## 실행 순서

진입 조건 감지 → 선행 PRD/SPEC 존재 확인(분기) → EnterPlanMode 호출
→ 기존 `{TS}` 폴더의 PRD/SPEC **소비**(읽기)
→ **PRD/SPEC 입력 검증 (②: ps1 + format_check `Stage=prd_spec`, 조건부 생략 — 아래 "진입 직후" 절)**
→ 코드 분석 ↔ 질문/답변 반복 (필요 시 PRD/SPEC **갱신**만, 신규 생성 금지)
→ PLAN 작성 (PRD + SPEC 기반)
→ PRD + SPEC + PLAN 사용자 제시 → ExitPlanMode
→ (ExitPlanMode 후, ③) PLAN 영속화 + ps1 `-Stage full` + plan_format_check `Stage=full`

## 진입 방식 (필수)

- `/plan` 슬래시 명령은 시스템 레벨 plan mode 표시일 뿐, `ExitPlanMode` 도구가 인식하는 도구 레벨 plan mode와 별개다.
- mainAgent는 산출물(PRD/SPEC/PLAN) 작성·제시 전에 반드시 `EnterPlanMode` 도구를 명시적으로 호출하여 도구 레벨 plan mode로 진입한다.
- `EnterPlanMode` 없이 `ExitPlanMode`를 호출하면 "not in plan mode" 오류가 발생한다.
- 검증은 hook으로 자동 실행되지 않는다. ExitPlanMode 후 1단계에서 mainAgent가 `plan_artifact_check.ps1`을 **직접 실행**한다(settings.json의 ExitPlanMode 글로벌 hook은 제거됨).
- `EnterPlanMode`는 사용자 승인을 요구하므로, `/plan` 수신 직후 분석에 착수하기 전 단계에서 호출한다.
- (발견 근거: 260602 실사용 테스트 — `/plan` 경로에서 `ExitPlanMode`가 "not in plan mode"로 실패. EnterPlanMode 선행 호출 필요 확인.)

## 진입 전 선행 조건

0. **PRD/SPEC 존재 분기 (이 절차 적용 여부 결정)**
   - 있음(사용자 명시 참조 또는 세션 내 BeforePlan 산출) → 그 `{TS}` 폴더를 **대상으로 승계**하여 이 절차를 수행한다.
     - **참조 우선순위**: ① 사용자가 명시한 경로 → ② 직전 BeforePlan이 안내한 `{TS}` 경로(세션 기억) → ③ 둘 다 없으면 사용자에게 경로를 질문(임의 스캔 금지).
   - 없음 → 이 절차를 적용하지 않고 **기본 /plan**으로 수행한다(산출물 폴더·PLAN 생성 없음, 1단계 검증도 수행하지 않음). 필요 시 사용자에게 `/BeforePlan` 선행을 안내한다.
   - **금지**: PRD/SPEC을 찾겠다고 `docs/agents/output/plan/`의 '최신 폴더'를 임의 스캔하지 않는다(무관한 과거 폴더 오인 방지).

1. 모호하거나 불확실한 항목은 추측으로 채우지 않고 반드시 사용자에게 질문한다.

2. 생략어·맥락 의존 표현("그 버그", "아까 그 화면")은 직전 대화 맥락 또는
   발화 내 단서로 복원한다.
    - 복원 가능: 복원 결과를 진입점 식별에 사용하고, 정리해서 사용자에게 확인받는다.
    - 복원 불가: 이슈에 기록하고 사용자에게 질문한다.

## 진입 직후 — PRD/SPEC 입력 검증 (②, PLAN 작성 전)

EnterPlanMode 후 기존 PRD/SPEC을 소비(읽기)한 직후, **PLAN 작성에 착수하기 전에** 입력 PRD/SPEC의 규격을 검증한다.
망가진 PRD/SPEC 위에 PLAN을 작성하는 낭비(작성 토큰 손실)를 막는 fail-fast 게이트다.

- **기본: 검증한다.**
  - `powershell -ExecutionPolicy Bypass -File .claude/hooks/plan_artifact_check.ps1 -TS {TS} -Stage prd_spec` 실행 → `_verification.txt` 확인
  - ps1 PASS면 plan_format_check SubAgent를 `Stage=prd_spec`로 호출 (PRD/SPEC 섹션·충실도 검증, PLAN은 보지 않음)
- **생략 조건 — 아래 (a)·(b)를 모두 충족할 때만 ②를 생략한다:**
  - (a) 같은 세션에서 BeforePlan의 ① 검증이 이 `{TS}`의 PRD/SPEC을 **PASS시킨 기록**이 있다 (검증된 기준선 존재)
  - (b) 사용자가 "① 이후 PRD/SPEC을 **수정하지 않았다**"를 명시했다
  - 위 (a) 기준선을 세션 기억에서 확인할 수 없으면(컨텍스트 유실 등) → **생략하지 말고 검증**(안전한 기본값)
- **외부 반입 등으로 ①이 실행된 적 없으면**: 사용자가 "수정 안 함"이라 해도 **반드시 검증한다** (검증된 기준선이 없으므로).
- **검증 이상 시**: 사용자에게 [보완 필요 항목]을 알리고 **PLAN 작성을 보류**한다. 보완 후 ②부터 재수행한다.

## 행동 원칙

### 지켜야 하는 것

- 직접 열어 확인한 코드·문서·실행 결과만 사실로 기록한다.
- 파일명:라인번호를 반드시 기입한다.
  호출 체인·문서 주장·버전 숫자는 파일명:라인번호 또는 공식 문서 URL 근거를 함께 기록한다.
- 근거 유형(코드 / 주석 / 문서 / 로그 / 실행 결과)을 명시한다.
- 동작은 실제 코드 흐름에 근거해 판단한다.
- 판단 근거가 없으면 이슈에 기록하고 사용자에게 확인을 요청한다.
- 계획 수립 중 새로운 모호함·불확실성이 발견되면 이슈로 누적 기록하고,
  계획 제시 시 함께 사용자에게 질문한다.

### 금지 사항

- 확인하지 않은 내용을 확인한 것처럼 서술하지 않는다.
- 근거 없이 결론을 도출하지 않는다.
- 추측으로 확정 하지 않는다.
- 미해결 이슈가 남아있는 상태에서 ExitPlanMode를 호출하지 않는다.

## 출력 형식

### 파일 구조

PRD/SPEC은 **BeforePlan이 이미 생성한** `{TS}` 폴더의 것을 사용한다(이 절차에서 새로 만들지 않는다).
이 절차에서 새로 만드는 것은 **PLAN뿐**이다.
`_verification.txt`는 ExitPlanMode 후 mainAgent가 검증 스크립트를 직접 실행해 생성한다(아래 1단계).

```
docs/agents/output/plan/{타임스탬프}/
├── {타임스탬프}_PRD.md        (BeforePlan 생성 — 소비 / 필요 시 갱신)
├── {타임스탬프}_SPEC.md       (BeforePlan 생성 — 소비 / 필요 시 갱신)
├── {타임스탬프}_PLAN.md       (이 절차에서 생성)
└── _verification.txt          (ExitPlanMode 후 검증 스크립트가 생성, mainAgent가 1.2에서 Read)
```

타임스탬프 형식: `yyMMdd_HHmmss` (예: `260527_143024`)

타임스탬프 규칙:
- **신규 생성하지 않는다.** BeforePlan 만든 `{TS}` 폴더명을 그대로 **승계**한다.
- (예외) 사용자가 PRD/SPEC을 외부에서 가져와 `{TS}` 폴더가 없을 때만, 동일 형식으로 1회 생성한다:
  PowerShell `Get-Date -Format "yyMMdd_HHmmss"` (Bash: `date +%y%m%d_%H%M%S`). 추정·임의 입력 금지.
- 승계/생성한 값을 PLAN 파일명에 일관 사용한다.

### 문서 정의

**PRD / SPEC** — 이 절차가 **생성하지 않는다.** 기존 PRD/SPEC을 **소비(읽기)** 하고, PLAN 작성 중 새 이슈가 발견되면 **근거와 함께 기존 문서를 갱신(edit)만** 한다(신규 생성·포맷 정의 금지 — BeforePlan 소유).

**PLAN** — 구현 계획

- 수정 순서가 있는 단계별 작업 목록
  ```
  - [ ] 1. 작업 설명
       대상: 파일명:라인번호        (기존 파일 수정)
  - [ ] 2. 작업 설명
       대상: 파일명 (신규)          (신규 파일 생성 — 라인번호 없음)
  ```
- 대상 표기: 기존 파일은 `파일명:라인번호`, 신규 파일은 `파일명 (신규)`
- 체크박스 상태: `[ ]` 미시작 / `[~]` 진행중 / `[x]` 완료 / `[!]` 이슈

### 출력 예시

**PRD / SPEC 예시**

- 이 절차에서 생성하지 않으므로 예시 없음.

**PLAN 예시**

```
- [x] 1. show() 함수에 isAdded 체크 추가
     대상: BottomSheetFragment.kt:42
- [ ] 2. 중복 호출 방지 테스트 추가
     대상: BottomSheetFragmentTest.kt:15
```

## ExitPlanMode 이후 실행 방식

ExitPlanMode 호출 후 mainAgent는 아래 체크리스트를 순서대로 수행한다.
각 항목을 완료할 때마다 사용자에게 결과를 보고하고 체크 상태를 [x]로 갱신한다.
체크되지 않은 항목이 있는 상태에서 다음 단계로 진행하지 않는다.

### 1단계 — 산출물 영속화 + 검증 (필수, 생략 불가)

> hook은 자동 실행되지 않는다(settings.json에서 ExitPlanMode 글로벌 hook 제거됨).
> 검증은 **이 절차를 탄 경우에만** mainAgent가 직접 수행한다. 기본 /plan은 이 1단계를 수행하지 않는다.

- [ ] 1.0 PLAN 영속화
       - 승인된 계획을 대상 `{TS}` 폴더에 `{TS}_PLAN.md`로 저장한다 (PRD/SPEC은 BeforePlan 생성본 사용)
       - PLAN 작성 중 PRD/SPEC 갱신이 있었다면 해당 .md도 함께 저장한다
- [ ] 1.1 검증 스크립트 직접 실행 (Stage=full)
       - `powershell -ExecutionPolicy Bypass -File .claude/hooks/plan_artifact_check.ps1 -TS {승계한 TS} -Stage full` 실행
       - **`-TS`에 승계/생성한 타임스탬프를 반드시 전달한다.** 스크립트는 최신 폴더를 스캔하지 않으며, `-TS` 미전달 시 FAIL을 반환한다.
       - ps1이 **존재·타임스탬프 형식·라인 수**를 검증해 `{TS}/_verification.txt`를 생성한다 (이 항목들은 1.3 format_check에서 재검사하지 않는다 — 역할 분담)
- [ ] 1.2 검증 결과 파일 읽기
       - `{TS}/_verification.txt`를 Read로 읽어 PASS/FAIL과 산출물 폴더 경로({TS})를 확정한다
       - 결과를 사용자에게 출력한다
- [ ] 1.3 plan_format_check SubAgent를 Agent 도구로 호출
       - 입력: ① 1.2에서 확정한 폴더 경로 `docs/agents/output/plan/{TS}/` ② `Stage=full`
       - 호출 자체를 생략한 채 다음 항목으로 넘어가지 않는다.
- [ ] 1.4 plan_format_check의 결과 원문을 사용자에게 그대로 출력
       - `[VERIFY_RESULT_BEGIN]` ~ `[VERIFY_RESULT_END]` 마커 사이 본문을 그대로 노출
       - 요약·재해석·축약 금지
- [ ] 1.5 종합 판정 확인
       - PASS → 2단계로 진행
       - FAIL → 1.6 보완 루프 진입
- [ ] 1.6 보완 루프 (1.5에서 FAIL인 경우만 수행)
       - verify가 정리한 [보완 필요 항목]을 mainAgent가 검토
       - 필요한 보완 작업 수행 (사용자에게 질문이 필요하면 질문, 산출물 .md 파일 수정 등)
       - 보완 완료 후 1.1로 돌아가 재검증
       - 무한 루프 방지: 최대 1회 재검증, 초과 시 사용자에게 보고 후 결정 대기

### 2단계 — 실행 방식 결정 (1단계 PASS인 경우만)

- [ ] 2.1 사용자 요청의 즉시 실행 의도 확인
       - "바로 진행해줘", "바로 실행해줘", "승인할게" 등 포함 여부 판별
- [ ] 2.2 실행 분기
       - 즉시 실행 의도 포함 → 해당 agent(agent-feature / agent-refactor) 자동 실행
       - 즉시 실행 의도 미포함 → 계획(PRD + SPEC + PLAN) 제시 후 사용자 승인 대기
