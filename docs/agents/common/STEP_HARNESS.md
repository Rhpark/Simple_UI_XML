# STEP_HARNESS.md — 하네스 실패 행동

이 파일은 analysis·review 두 워크플로우가 공유하는 하네스 실패 시 행동 규칙이다.

---

## 심각도별 하네스 실패 행동

→ [SEVERITY_RULE.md](SEVERITY_RULE.md) 참조

- **CRITICAL** 항목 미통과 → 강제 중단 또는 해당 STEP 즉시 복귀
- **HIGH** 항목 미통과 → 복귀 후 재수행
- **MEDIUM** 항목 미통과 → 재수행 후 조건부 진행
- **LOW** 항목 미통과 → 기록 후 계속 진행
