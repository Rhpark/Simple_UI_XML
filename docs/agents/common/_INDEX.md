# common/ INDEX

목적: `common/` 디렉터리에 있는 SSOT(Single Source of Truth) 파일들을 한눈에 찾아가기 위한 지도.
모든 워크플로우(analysis / review / feature / refactor)는 공통 규칙을 이 디렉터리에서만 가져온다.

## SSOT 파일 일람

| 파일 | 무엇의 SSOT인가 | 무엇을 정의하지 않는가 |
| --- | --- | --- |
| [SEVERITY_RULE.md](SEVERITY_RULE.md) | 심각도 4단계(CRITICAL / HIGH / MEDIUM / LOW)의 의미, 다음 행동, 분석 타입별 보정, 판정 원칙 | STEP별 등급 적용 예시(각 STEP 파일에 위임) |
| [STEP_ROLLBACK.md](STEP_ROLLBACK.md) | 루프 판정 기준(동일 원인 2회 복귀 = 루프), 복귀 조건 · 대상 STEP, 부분 복귀 원칙 | STEP별 복귀 트리거 조건(각 STEP 파일에 위임) |
| [STEP_FORCE_STOP.md](STEP_FORCE_STOP.md) | 강제 중단 조건(공통), 강제 중단 vs 복귀 구분 | 워크플로우별 강제 중단 추가 조건(각 STEP 파일에 위임) |
| [STEP_HARNESS.md](STEP_HARNESS.md) | 하네스 심각도별 실패 행동(CRITICAL/HIGH/MEDIUM/LOW) | 각 STEP 하네스 항목(각 STEP 파일에 위임) |
| [ROLLBACK_RULES.md](ROLLBACK_RULES.md) | _(위 세 파일로 분리됨 — thin redirect)_ | — |
| [STEP_EXECUTION_RULE.md](STEP_EXECUTION_RULE.md) | STEP 실행/완료 판정 규칙, 하네스 통과 요건, 실패 처리 절차 | 각 STEP의 체크리스트·하네스 항목(각 STEP 파일에 위임) |
| [GLOSSARY.md](GLOSSARY.md) | 워크플로우 공통 용어 정의(핵심 파일, 근거 파일, 직접/간접/공통 파일, 영향 범위 등) | STEP별 산출물 형식, 도메인 고유 용어 |

## 참조 관계

각 SSOT 파일을 참조하는 STEP / 테스트 파일.

| SSOT | 참조 위치 |
| --- | --- |
| SEVERITY_RULE.md | `docs/agents/analysis/step1/STEP1_SCOPE.md` · `docs/agents/analysis/step2/STEP2_READ.md` · `docs/agents/analysis/step3/STEP3_IMPACT.md`(2회) · `docs/agents/analysis/step4/STEP4_REPORT.md` |
| STEP_ROLLBACK.md | `docs/agents/analysis/step1~4` · `docs/agents/review/step1~6` · `docs/agents/common/STEP_EXECUTION_RULE.md` · `.claude/agents/review.md` |
| STEP_FORCE_STOP.md | 위 동일 (공통 규칙 섹션에서 참조) |
| STEP_HARNESS.md | 위 동일 (공통 규칙 섹션에서 참조) |
| STEP_EXECUTION_RULE.md | `docs/agents/analysis/tests/SUBAGENT_EVAL.md` |
| GLOSSARY.md | `docs/agents/analysis/step1/STEP1_SCOPE.md` · `docs/agents/analysis/step2/STEP2_READ.md` · `docs/agents/analysis/step3/STEP3_IMPACT.md` · `docs/agents/analysis/step4/STEP4_REPORT.md` · `docs/agents/analysis/tests/STATIC_CHECKLIST.md` |

## 규칙 수정 시 지켜야 할 원칙

1. **SSOT 우선**: 등급 정의, 복귀 기준, 실행 규칙, 용어를 바꿔야 하면 반드시 `common/`의 해당 파일을 먼저 고친다. STEP 파일에 같은 규칙을 새로 박아 넣지 않는다.
2. **STEP 파일은 예시만**: STEP 파일은 SSOT의 규칙을 자기 맥락에 어떻게 적용하는지(예: 어떤 누락이 HIGH인지)만 보유한다. 등급 정의 자체나 루프 판정식을 STEP 파일에서 재정의하면 안 된다.
3. **참조 형식 통일**: STEP 파일에서 SSOT를 참조할 때는 `→ FILE_NAME.md (../../common/FILE_NAME.md) 참조` 형식을 유지한다.
4. **SSOT 수정 시 동기화 확인**: `common/` 파일을 수정했다면, 위 "참조 관계" 표에 있는 모든 참조 위치를 열어 모순된 표현이 남아 있지 않은지 확인한다.
5. **네 SSOT 간 일관성 유지**: SEVERITY_RULE · ROLLBACK_RULES · STEP_EXECUTION_RULE · GLOSSARY는 서로를 전제로 동작한다. 한쪽을 바꾸면 다른 쪽의 표현·용어가 깨지지 않는지 함께 본다.
