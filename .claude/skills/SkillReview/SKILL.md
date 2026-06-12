---
name: SkillReview
description: |
  프로젝트의 Skill(.claude/skills/)과 구식 커맨드(.claude/commands/)를 품질 루브릭(6섹션 30항목,
  규칙/모델 검사 분리, S~F 등급)으로 점검하고 등급표와 개선 조치를 보고한다.
  스킬을 새로 만들었거나 수정한 직후, 또는 스킬이 호출되지 않거나 이상 동작할 때 반드시 사용한다.
  트리거: /SkillReview, "스킬 점검", "스킬 품질 검사", "스킬 평가", "SKILL.md 검사", "skill review",
  "루브릭 점검", "스킬이 왜 호출 안 되지".
argument-hint: "[all | 스킬명] (생략 시 all)"
---

# SkillReview — Skill 품질 루브릭 점검

스킬의 결함은 컴파일 에러처럼 드러나지 않고 **"호출되지 않거나, 호출돼도 효과가 없는 형태로 조용히 누적"** 된다.
이 스킬은 그 조용한 결함을 두 단계로 잡아낸다:

1. **규칙 검사 (결정적)** — 파싱·카운트·경로 실존처럼 기계적으로 판정 가능한 항목. 항상 전수 실행.
2. **의미 검사 (모델 판단)** — 트리거 충실도·콘텐츠 구체성처럼 해석이 필요한 항목. **규칙 검사를 통과한 대상에만** 실행 (fail-fast, 낭비 방지).

등급은 S~F이며 **머지/유지 차단 여부는 BLOCKER 유무 1비트로만 판단**한다. 등급의 세세함보다 결정적 게이트가 중요하다.

## 대상 결정

1. `$ARGUMENTS`에 스킬명이 있으면 → 해당 스킬만 점검한다.
2. `$ARGUMENTS`가 없거나 `all`이면 → 아래 전부를 점검한다.
   - `.claude/skills/*/SKILL.md` (정식 스킬)
   - `.claude/commands/*.md` (구식 커맨드 — 존재 자체가 마이그레이션 권고 대상)
   - `.claude/skills/` 하위의 SKILL.md 없는 폴더 (`-workspace` 잔재 등 오염 감지)

## 점검 절차

### STEP 1 — 인벤토리 (규칙)

Glob/PowerShell로 다음을 수집한다. 추측 금지 — 전부 실측한다.

- 스킬 폴더 목록, 각 SKILL.md 존재 여부·줄 수, 하위 폴더 구조(references/scripts/assets/기타)
- `.claude/commands/*.md` 목록 (구식 형식)
- SKILL.md 없는 폴더 목록 (잔재)

### STEP 2 — 규칙 검사 (결정적, 전수)

`references/rubric.md`의 **[규칙]** 표기 항목을 대상 전체에 적용한다. 핵심:

- frontmatter 파싱 가능 / name·폴더명 일치 / description 1~1024자·XML 태그 없음 (BLOCKER)
- **참조 경로 실존** — 본문이 참조하는 모든 파일·스크립트·슬래시 명령이 실제로 존재하는가 (BLOCKER, 이 프로젝트 최다 결함 유형)
- secret/credential·destructive 패턴 없음 (BLOCKER)
- 본문 500줄 이하, references 평탄(중첩 금지), 100줄 이상 reference에 목차 (MAJOR/MINOR)

검사 방법(Grep 패턴·PowerShell 명령 예시)은 rubric.md 각 항목에 있다.

### STEP 3 — 의미 검사 (모델 판단, 규칙 통과분만)

BLOCKER가 없는 대상에만 **[모델]** 표기 항목을 적용한다. 핵심:

- description에 WHAT(무엇)+WHEN(언제) 모두 포함, 트리거 키워드 충분
- **description ↔ 실동작 정합성** — `disable-model-invocation: true`인데 "~요청 시 사용"으로 자동 트리거를 약속하지 않는가
- 콘텐츠 구체성 — 일반론 감점, 조직 고유 수치·경로·규칙 가점
- 리소스 분류 정확성 — 읽기용은 references/, 실행용은 scripts/, 산출물용은 assets/
- 타당성 — 반복 워크플로우인가, 기본 에이전트 능력으로 대체 불가한가

### STEP 4 — 등급 산정

| 등급 | 조건 | 의미 |
|------|------|------|
| S | BLOCKER 0 + MAJOR 0 | 모범 |
| A | BLOCKER 0 + MAJOR 1~2 | 사용 가능, 소폭 개선 |
| B | BLOCKER 0 + MAJOR 3~4 | 개선 필요 |
| C | BLOCKER 0 + MAJOR 5+ | 대폭 개선 필요 |
| F | BLOCKER 1+ | 유지 불가, 즉시 수정/재작성 |

**환경 보정 (반드시 적용, 사유는 rubric.md 0절)**: name kebab-case 위반은 이 환경(Claude Code)이
PascalCase를 실인식하므로 기존 스킬에는 비차단(MINOR)으로 보정한다. 신규 스킬은 프로젝트 관례(PascalCase)를 따르되 보고서에 명시한다.

### STEP 5 — 보고

`references/report_format.md`의 형식대로 출력한다. 모든 지적에는 "왜 문제인가 + 어떻게 고치는가"를
한 묶음으로 제시한다 (단순 지적 금지). 본문을 정독하지 않은 대상이 있으면 "미정독" 표기로 정직하게 알린다.

## 하지 말 것

- 파일을 수정하지 않는다 — 이 스킬은 읽기 전용 점검이다. 수정은 보고 후 사용자 승인을 받아 별도로 진행한다.
- 근거 없는 판정 금지 — 모든 지적에 파일:라인 또는 실측 명령 결과를 첨부한다.
- 루브릭 원문(토스)과 환경 보정이 충돌하면 rubric.md 0절의 보정을 따르고, 보고서에 보정 적용 사실을 명시한다.

## 참조 문서 (해당 시점에 읽기)

- 루브릭 30항목 상세·검사 방법: `references/rubric.md` (STEP 2~3 진입 시 읽기)
- 보고 출력 형식: `references/report_format.md` (STEP 5 진입 시 읽기)
