# SkillReview 루브릭 — 6섹션 30항목

> 출처: 토스 AI DX팀 Skill 품질 루브릭(toss.tech/article/skill-quality-rubric)을 이 프로젝트 환경에 맞게 보정.
> 각 항목 표기: **[규칙]** = 기계적 판정(Grep/Glob/PowerShell), **[모델]** = 의미 판단.
> 심각도: **BLOCKER**(1개라도 있으면 F, 즉시 수정) / **MAJOR**(등급 감점) / **MINOR**(보고만).

## 목차

- [0. 환경 보정 (토스 원문과 다른 부분 + 사유)](#0-환경-보정)
- [1. 타당성 (3항목, 모델)](#1-타당성)
- [2. 구조 (8항목, 규칙)](#2-구조)
- [3. 트리거 (6항목, 규칙 1 + 모델 5)](#3-트리거)
- [4. 콘텐츠 (3항목, 규칙 1 + 모델 2)](#4-콘텐츠)
- [5. 리소스 (8항목, 규칙 5 + 모델 3)](#5-리소스)
- [6. 안전성 (2항목, 규칙)](#6-안전성)

---

## 0. 환경 보정

토스 원문과 의도적으로 다르게 적용하는 항목. 보고서에 보정 적용 사실을 명시한다.

| 보정 | 내용 | 사유 |
|------|------|------|
| 2-2 완화 | name kebab-case 위반을 BLOCKER → MINOR로 강등 (기존·신규 공통) | 이 환경(Claude Code)은 PascalCase name을 실인식·실동작함을 검증함(260612). 기존 스킬 전부와 AGENTS.md의 슬래시 명령 문서가 PascalCase 관례라 신규만 kebab-case면 오히려 비일관. 단 다른 프로젝트로 이식할 때는 kebab-case 변환 권장 |
| 2-8 신설 | 참조 경로 실존 검사를 **BLOCKER로 승격** | 이 프로젝트의 실측 최다 결함 유형: CodeReview의 `/Planning` dead ref, skill-creator의 플러그인 캐시 해시 절대경로(실제로 깨짐 확인), 과거 AGENTS.md의 rules/ dead ref |
| 3-6 신설 | `disable-model-invocation` ↔ description 정합성 | 실측 결함: 7개 스킬이 자동 호출 차단 설정인데 description은 `"~요청 시 사용"`으로 자동 트리거를 약속 |
| 5-6~5-8 신설 | assets/ 검사 3항목 추가 | 토스 원문은 references/scripts만 다루나, Anthropic 공식 스킬 체계는 assets/(산출물용 파일: 템플릿·이미지·폰트)를 포함하므로 보완 |
| 인벤토리 확장 | `.claude/commands/*.md`(구식)와 SKILL.md 없는 폴더(잔재)도 점검 대상 | 실측 결함: commands 3종이 frontmatter 없이 잘린 첫 줄이 description으로 노출, `-workspace` 잔재 5개가 skills 디렉터리 오염 |

---

## 1. 타당성 — 스킬이 존재할 가치가 있는가 (3항목, 전부 [모델] MAJOR)

셋 다 NO면 스킬 자체를 폐기/통합 권고한다.

- **1-1** 반복되는 워크플로우인가 — 한 번 쓰고 말 절차면 스킬일 필요 없음
- **1-2** 범용성이 있는가 — 특정 파일 하나에만 묶인 절차는 문서/주석이 맞음
- **1-3** 기본 에이전트 능력으로 대체 불가능한가 — "파일 읽고 요약해줘" 수준이면 스킬 불필요

## 2. 구조 — 파일 형식의 정확성 (8항목, 전부 [규칙])

- **2-1 [BLOCKER]** YAML frontmatter가 파싱 가능한가 — `---` 블록 존재 + 유효 YAML
- **2-2 [MINOR(보정)]** name이 kebab-case이고 64자 이하인가 — 0절 보정 참조 (이 프로젝트는 PascalCase 관례 허용, 64자 초과만 MAJOR)
- **2-3 [BLOCKER]** name과 폴더명이 일치하는가
- **2-4 [BLOCKER]** description이 1~1024자인가
- **2-5 [BLOCKER]** description에 XML 태그(`<...>`)가 없는가
- **2-6 [MAJOR]** frontmatter에 인식되지 않는 키가 없는가 (허용: name, description, argument-hint, disable-model-invocation, compatibility, allowed-tools, model)
- **2-7 [MINOR]** 예약어/충돌 — 내장 명령(/help 등)·다른 스킬과 name 충돌이 없는가
- **2-8 [BLOCKER, 환경 신설]** **본문이 참조하는 경로가 전부 실존하는가** — 파일 경로, 스크립트, 다른 스킬의 슬래시 명령, 절대경로(특히 캐시·해시 포함 경로는 즉시 의심)
  - 검사: 본문에서 경로·`/명령` 패턴 추출 → Test-Path / Glob / 스킬 목록 대조

## 3. 트리거 — LLM이 제때 호출할 수 있는가 (6항목)

- **3-1 [규칙, BLOCKER]** "Use When/트리거 조건이 본문에만 있는 안티패턴" — description에 WHEN 정보가 전혀 없고 본문에만 있으면 LLM이 스킬을 발견하지 못한다
- **3-2 [모델, MAJOR]** description에 WHAT(기능) + WHEN(시점)이 모두 있는가
- **3-3 [모델, MAJOR]** 트리거 키워드가 충분한가 — 사용자가 실제로 쓸 표현(한국어 다양성 포함)이 들어 있는가
- **3-4 [모델, MAJOR]** description과 본문의 의미가 일치하는가
- **3-5 [모델, MAJOR]** 트리거 범위가 과도하게 넓지 않은가 — 무관한 요청까지 빨아들이면 다른 스킬/에이전트와 충돌
- **3-6 [모델, MAJOR, 환경 신설]** `disable-model-invocation: true`면 description이 자동 트리거를 약속하지 않는가 — 슬래시 전용 스킬은 `"슬래시 명령 /X로 실행"`처럼 실동작과 일치하게 쓴다. 자연어 트리거 문구는 그 요청을 실제로 처리하는 주체(예: 전용 agent)와 충돌을 일으킨다

## 4. 콘텐츠 — 본문이 실제 가치를 담는가 (3항목)

- **4-1 [모델, MAJOR]** 구체성 — 수치·코드·경로·시나리오가 있는가. "Redis는 인메모리 DB" 같은 일반론 감점, "N > 100이면 SCAN 사용" 같은 조직 고유 기준 가점
- **4-2 [모델, MAJOR]** 일반론이 아닌 조직 고유 지식인가 — LLM이 이미 아는 내용의 반복이면 컨텍스트 낭비
- **4-3 [규칙, MAJOR]** 본문 500줄 이하인가 — 초과 시 references/로 분리 권고

## 5. 리소스 — 파일 구조와 자료 분리 (8항목)

권장 구조:
```
my-skill/
├── SKILL.md      (핵심 절차만)
├── references/   (읽기용 문서 — 필요 시 컨텍스트 로드, 평탄하게)
├── scripts/      (실행용 코드 — 결정적 작업)
└── assets/       (산출물용 파일 — 템플릿·이미지·폰트, 컨텍스트에 로드하지 않음)
```

- **5-1 [모델, MAJOR]** 핵심/상세 분리 — 무거운 자료가 본문에 있으면 references/로 분리 (컨텍스트 비용)
- **5-2 [규칙, MAJOR]** references/ 중첩 금지 — 평탄 구조 유지
- **5-3 [규칙, MAJOR]** scripts/ 문법 유효 — 파싱/실행 가능해야 함
- **5-4 [규칙, MINOR]** 100줄 이상 reference에 목차 존재
- **5-5 [규칙, MINOR]** SKILL.md 없는 폴더(`-workspace` 잔재 등)가 skills 디렉터리에 없는가 — 정리 권고
- **5-6 [모델, MAJOR, 환경 신설]** 리소스 분류 정확성 — 읽기용 문서가 assets/에, 템플릿·보일러플레이트가 references/에 잘못 들어가 있지 않은가
- **5-7 [규칙, MAJOR, 환경 신설]** dead asset — assets/ 파일 중 본문에서 참조되지 않는 것이 없는가 (2-8의 변형)
- **5-8 [모델, MINOR, 환경 신설]** assets를 통째로 "읽으라"고 지시하지 않는가 — 템플릿은 복사/가공 대상이지 컨텍스트 로드 대상이 아님

## 6. 안전성 — 배포 가능한가 (2항목, 전부 [규칙] BLOCKER)

False Positive는 감수하고 False Negative를 0에 가깝게 — 의심되면 지적한다.

- **6-1 [BLOCKER]** 평문 secret/credential 없음
  - 검사: `password\s*=|api[_-]?key\s*=|secret\s*=|token\s*=` (대소문자 무시)
- **6-2 [BLOCKER]** destructive 패턴 없음
  - 검사: `rm -rf|dd if=|mkfs|chmod -R 777|Remove-Item.*-Recurse.*-Force.*(\\\*|/\*)|format [a-z]:`
  - 예외: 패턴을 "금지 예시"로 인용하는 문서(이 파일 포함)는 맥락 확인 후 통과

---

## 검사 방법 빠른 참조 (규칙 항목)

```powershell
# 인벤토리: 폴더·줄수·하위구조
Get-ChildItem .claude\skills -Directory | ForEach-Object { ... SKILL.md 존재·줄수·하위폴더 ... }

# frontmatter 전수 추출
$raw -match '(?s)^---\s*\r?\n(.*?)\r?\n---'
```

```
# 참조 경로 추출(2-8): 본문에서 경로처럼 보이는 토큰을 찾아 실존 대조
Grep: (docs/|\.claude/|references/|scripts/|assets/)[\w./-]+\.(md|ps1|py|sh|json)
Grep: (?<![\w/])/[A-Z]\w+   ← 슬래시 명령 참조 (스킬 목록과 대조)
Grep: [A-Z]:[/\\]           ← 절대경로 (이식성·해시 경로 의심)
```
