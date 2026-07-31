# STEP2. 출력 예시

STEP2_PLAN.md의 산출물 형식을 실제 상황별로 채운 예시 모음이다.

→ 산출물 형식·하네스 기준은 [STEP2_PLAN.md](STEP2_PLAN.md) 참조

---

## 예시 A — Tier A / 분기 A / 하네스 통과

```text
[feature workflow — STEP2 산출물]
단계     : STEP 2 (계획 문서 작성)
진행도   : (7/7) - 산출물 출력 (완료)

PRD 경로       : docs/agents/output/feature/20260526_093000/PRD.md
SPEC 경로      : docs/agents/output/feature/20260526_093000/SPEC.md
PLAN 경로      : docs/agents/output/feature/20260526_093000/PLAN.md
마일스톤 목록  : 1. PermissionDelegate 공개 API 추가 / 2. PermissionViewModel 연동 / 3. 테스트 작성

이슈 : 없음
```

**생성된 PRD.md 요약:**
```markdown
# PRD — 권한 위임 API 추가

## 기능 목적
외부 모듈이 PermissionDelegate를 통해 런타임 권한을 요청할 수 있어야 한다.

## 금지 패턴
- 금지: Activity 직접 참조 — PermissionDelegate는 Context만 허용, Activity 의존 금지

## 경계 조건
- 책임 범위: 권한 요청 흐름 (요청 → 결과 콜백)
- 책임 외 범위: 권한 거부 후 UI 안내, 권한 영구 거부 처리
```

**생성된 PLAN.md 요약:**
```markdown
# PLAN — 권한 위임 API 추가

## 마일스톤 목록

| 순서 | 마일스톤명 |
|---|---|
| 1 | PermissionDelegate 공개 API 추가 |
| 2 | PermissionViewModel 연동 |
| 3 | 테스트 작성 |

### 마일스톤 1. PermissionDelegate 공개 API 추가
**목적**: requestPermission() 함수를 공개 API로 추가한다
**수정 파일**: PermissionDelegate.kt:87
**기준선**: assembleDebug 통과, apiDump에 requestPermission() 노출 확인
```

---

## 예시 B — 하네스 실패 (마일스톤 기준선 누락)

```text
[feature workflow — STEP2 산출물]
단계     : STEP 2 (계획 문서 작성)
진행도   : (5/7) - PLAN 작성 (진행중)

PRD 경로       : docs/agents/output/feature/20260526_093000/PRD.md
SPEC 경로      : docs/agents/output/feature/20260526_093000/SPEC.md
PLAN 경로      : 미완성
마일스톤 목록  : 미완성

이슈 : [마일스톤] PLAN.md 마일스톤 2번에 기준선 없음 — 빌드·테스트 통과 조건 미명시

실패 STEP   : STEP2
실패 항목   : 5번 통과 — PLAN.md 마일스톤 기준선
실패 원인   : 마일스톤 2. PermissionViewModel 연동에 기준선 필드가 비어 있음
복귀 대상   : 체크리스트 5번
전달 데이터 : PRD 경로 완료 / SPEC 경로 완료 / PLAN 경로 미완성 / 마일스톤 목록 미완성 / 이슈 위 참조
```
