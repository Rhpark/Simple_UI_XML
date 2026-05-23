# Review Failure Path Matrix

목적: 정상 경로 외에 실패·복귀·중단 조건이 올바른 STEP으로 연결되는지 검증한다.

## 실패 경로 검증표

| ID | 상황 | 기대 동작 | 근거 문서 | 결과 |
| --- | --- | --- | --- | --- |
| F1 | analysis STEP4 산출물 없이 review 진입 | 분기 B 적용 → 강제 중단, "analysis 먼저 완료" 고지 | [.claude/agents/review.md](../../../../.claude/agents/review.md) 분기 B | PASS |
| F2 | [기능 검증] finding에 파일명:라인번호 없음 | STEP1 하네스 미통과 → STEP1 재수행, 코드 근거 확인 | [../step1/STEP1_FUNC.md](../step1/STEP1_FUNC.md) | PASS |
| F3 | STEP1 이관 항목을 STEP2에서 미처리 | STEP2 하네스 미통과 → STEP2 재수행, 이관 항목 재확인 | [../step2/STEP2_LOGIC.md](../step2/STEP2_LOGIC.md) | PASS |
| F4 | [아키텍처] 기준 문서 미기록 | STEP3 하네스 미통과 → STEP3 재수행, 기준 문서 기록 | [../step3/STEP3_ARCH.md](../step3/STEP3_ARCH.md) | PASS |
| F5 | hallucination 판정에 선언부 근거 없음 | STEP4 하네스 미통과 → STEP4 재수행, 선언부 직접 확인 | [../step4/STEP4_QUALITY.md](../step4/STEP4_QUALITY.md) | PASS |
| F6 | STEP 진행 중 CRITICAL finding 발견 | 즉시 중단 → 현재까지 산출물 출력 후 CRITICAL finding 별도 블록 보고, 사용자 응답 대기 | [../step2/STEP2_LOGIC.md](../step2/STEP2_LOGIC.md) / [../step5/STEP5_REPORT.md](../step5/STEP5_REPORT.md) | PASS |
| F7 | STEP5 finding에 파일명:라인번호 없음 | 해당 finding 원본 STEP으로 복귀, 코드 근거 재확인 | [../step5/STEP5_REPORT.md](../step5/STEP5_REPORT.md) | PASS |
| F8 | STEP5 중복 제거 대표 STEP 불명확 | finding이 발생한 두 STEP 중 상위 STEP으로 복귀 | [../step5/STEP5_REPORT.md](../step5/STEP5_REPORT.md) | PASS |
| F9 | 같은 원인으로 동일 STEP에 2회 이상 복귀 | 루프 판정 → 작업 중단, 실패 규격 전체 사용자 보고 | [../../common/STEP_ROLLBACK.md](../../common/STEP_ROLLBACK.md) | PASS |
| F10 | 코드 수정·구현·리팩토링 요청 포함 | 분기 C 적용 → 3가지 선택지 제시 (리뷰만 / 다른 워크플로우 안내 / 취소) | [.claude/agents/review.md](../../../../.claude/agents/review.md) 분기 C | PASS |

## 검증 결론

```text
결과: PASS
근거: 각 실패 상황이 가장 가까운 이전 STEP 또는 강제 중단 조건으로 연결된다.
잔여 리스크: 실제 에이전트 실행 중 복귀 횟수를 자동 집계하는 별도 도구는 없다.
             현재는 문서 규칙 기반 수동 집계가 전제다.
             F6(CRITICAL finding) 경로는 STEP2 SAMPLES 예시 C에서 시뮬레이션 완료됐으나
             실제 에이전트 런타임에서 별도 블록 출력 형식 준수 여부는 직접 실행으로 확인이 필요하다.
```
