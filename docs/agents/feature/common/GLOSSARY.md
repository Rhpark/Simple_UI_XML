# Glossary — Feature 전용

이 파일은 feature 워크플로우 전용 용어 정의다.
두 워크플로우 공용 용어는 [../../common/GLOSSARY.md](../../common/GLOSSARY.md)를 참조한다.

---

## Tier 정의

| 용어 | 정의 |
|---|---|
| **Tier A** | 복잡/중간 수정. PRD/SPEC/PLAN 필요. review 권고. STEP2 실행. |
| **Tier B** | 단순 수정. PRD/SPEC/PLAN 불필요. review 불필요. STEP2 생략. |

판정 기준 → [STEP1_ENTRY.md](../step1/STEP1_ENTRY.md) 참조

---

## 분기 정의

Tier A에서만 유효하다. Tier B는 분기 판단 없이 STEP3으로 직행한다.

| 용어 | 정의 |
|---|---|
| **분기 A** | `docs/agents/output/review/`에 review 산출물이 있다. analysis 산출물도 자동으로 포함된다. |
| **분기 B** | `docs/agents/output/analysis/`에 analysis 산출물만 있다. review 산출물은 없다. |
| **분기 C** | analysis·review 산출물 모두 없다. |

---

## 마일스톤 용어

| 용어 | 정의 |
|---|---|
| **마일스톤** | PLAN.md에 정의된 구현의 최소 독립 단위. 각 마일스톤은 빌드·테스트로 독립 검증 가능해야 한다. |
| **마일스톤 루프** | STEP4에서 마일스톤을 순서대로 구현하는 반복 사이클. 각 사이클: 구현 → 빌드 → 테스트 → 하네스 → 사용자 보고. |
| **마일스톤 기준선** | 각 마일스톤 완료 판정 조건. PLAN.md에 마일스톤별로 명시된다. |

---

## 안전망 용어

| 용어 | 정의 |
|---|---|
| **빌드 기준선** | STEP3에서 구현 전 확인한 빌드 통과 상태. STEP5 회귀 비교 기준으로 사용한다. |
| **테스트 기준선** | STEP3에서 구현 전 확인한 테스트 통과/실패 목록. 구현 후 새로 실패한 테스트를 판별하는 기준이다. |
| **API 스냅샷** | STEP3에서 `apiDump`로 기록한 공개 API 현황. STEP5 `apiCheck`의 Breaking Change 판별 기준이다. |

---

## 하네스 용어

Tier A에서만 유효하다. Tier B는 PRD·SPEC 없으므로 해당 없다.

| 용어 | 정의 |
|---|---|
| **PRD 금지 패턴** | PRD.md에 정의된 구현 시 사용해서는 안 되는 패턴. 마일스톤마다 준수 여부를 확인한다. |
| **PRD 경계 조건** | PRD.md에 정의된 이 기능이 책임지는 범위와 책임지지 않는 범위. 마일스톤마다 이탈 여부를 확인한다. |
| **SPEC 판단 기준** | SPEC.md에 정의된 구현 결정 시 따르는 기준. 설계 선택의 근거로 사용한다. |

---

## 산출물 경로

| 구분 | 경로 |
|---|---|
| **출력 디렉토리** | `docs/agents/output/feature/YYYYMMDD_HHmmss/` — 워크플로우 시작 시 STEP1에서 경로 확정 |
| **PRD.md** | `docs/agents/output/feature/YYYYMMDD_HHmmss/PRD.md` — Tier A STEP2에서 생성 |
| **SPEC.md** | `docs/agents/output/feature/YYYYMMDD_HHmmss/SPEC.md` — Tier A STEP2에서 생성 |
| **PLAN.md** | `docs/agents/output/feature/YYYYMMDD_HHmmss/PLAN.md` — Tier A STEP2에서 생성 |
| **REPORT.md** | `docs/agents/output/feature/YYYYMMDD_HHmmss/REPORT.md` — STEP6에서 생성 |

---

## STEP 간 전달 필드

### [진입 판정] — STEP1 산출물

| 필드 | 정의 |
|---|---|
| `Tier` | A 또는 B. 판정 근거 포함. |
| `분기` | Tier A일 때만. A·B·C 중 하나. 사용한 산출물 경로 포함. |
| `직접 파일` | 수정 대상 파일 목록. 파일명:라인번호 형식. |
| `간접 파일` | 직접 파일 수정으로 함께 영향받는 파일 목록. 없으면 "없음". |
| `STEP2 적용 여부` | Tier A → 필수 / Tier B → 생략. |
| `출력 디렉토리` | `docs/agents/output/feature/YYYYMMDD_HHmmss/` — 이 STEP에서 경로 확정. |

### [계획 문서] — STEP2 산출물 (Tier A만)

| 필드 | 정의 |
|---|---|
| `PRD 경로` | 생성된 PRD.md 경로. |
| `SPEC 경로` | 생성된 SPEC.md 경로. |
| `PLAN 경로` | 생성된 PLAN.md 경로. |
| `마일스톤 목록` | PLAN.md에서 추출한 마일스톤 순서 목록. |

### [안전망] — STEP3 산출물

| 필드 | 정의 |
|---|---|
| `빌드 기준선` | 빌드 명령 및 통과 여부. |
| `테스트 기준선` | 테스트 실행 결과. 통과/실패 목록. |
| `API 스냅샷` | apiDump 결과 요약. 공개 API 목록. |

### [마일스톤 N 완료] — STEP4 마일스톤마다

| 필드 | 정의 |
|---|---|
| `구현 파일` | 해당 마일스톤에서 수정한 파일 목록. |
| `빌드` | 빌드 통과 여부. |
| `테스트` | 마일스톤 범위 테스트 결과. |
| `하네스 준수` | PRD 금지 패턴·경계 조건·SPEC 판단 기준 준수 여부. |

### [전체 검증] — STEP5 산출물

| 필드 | 정의 |
|---|---|
| `apiCheck` | Breaking Change 판정 결과. STEP3 API 스냅샷과 비교. |
| `ktlintCheck` | 코드 스타일 검사 결과. |
| `전체 테스트` | testRobolectric + testUnit 결과. |

### [보고] — STEP6 산출물

| 필드 | 정의 |
|---|---|
| `REPORT.md 경로` | `docs/agents/output/feature/YYYYMMDD_HHmmss/REPORT.md` |
| `변경 파일 목록` | 전체 수정된 파일 목록. |
| `다음 단계` | 후속 권장 워크플로우. review 권고 / 완료 중 하나. |
