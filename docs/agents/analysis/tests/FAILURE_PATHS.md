# Analysis Failure Path Matrix

목적: 정상 경로 외에 실패·복귀·중단 조건이 올바른 STEP으로 연결되는지 검증한다.

## 실패 경로 검증표

| ID | 상황 | 기대 동작 | 근거 문서 | 결과 |
| --- | --- | --- | --- | --- |
| F1 | 사용자 요청에 기능명, 화면명, 파일명, 오류 문구, 문서명이 전혀 없음 | STEP1에서 강제 중단하고 사용자에게 범위 재정의 요청 | [../step1/STEP1_SCOPE.md](../step1/STEP1_SCOPE.md) | PASS |
| F2 | STEP1 핵심 파일이 0개 | STEP1에서 강제 중단하고 진입점 정보 재확인 요청 | [../step1/STEP1_SCOPE.md](../step1/STEP1_SCOPE.md) | PASS |
| F3 | STEP2에서 핵심 파일을 열 수 없음 | STEP2 강제 중단, 사용자에게 핵심 파일 재확인 요청 | [../step2/STEP2_READ.md](../step2/STEP2_READ.md) | PASS |
| F4 | STEP2에서 제외 범위 안으로 들어가야만 추적 가능 | STEP1로 복귀해 제외 범위 또는 핵심 파일 재확정 | [../step2/STEP2_READ.md](../step2/STEP2_READ.md) | PASS |
| F5 | 혼합 타입에서 보조 타입 근거를 읽지 않음 | STEP2 리스크 또는 인계 항목에 이유 기록 | [../step2/STEP2_READ.md](../step2/STEP2_READ.md) | PASS |
| F6 | STEP3에서 영향 계층 식별 불가 | STEP2로 복귀해 호출 흐름 또는 문서 주장 재추적 | [../step3/STEP3_IMPACT.md](../step3/STEP3_IMPACT.md) | PASS |
| F7 | STEP3에서 직접 파일 0개 | STEP2로 복귀해 근거 파일 재확인 | [../step3/STEP3_IMPACT.md](../step3/STEP3_IMPACT.md) | PASS |
| F8 | STEP4에서 사실/추정 분리 불가 | STEP2 또는 STEP3으로 복귀해 사실 관계 재확인 | [../step4/STEP4_REPORT.md](../step4/STEP4_REPORT.md) | PASS |
| F9 | STEP4 직행 경로에서 직접 파일·간접 파일·사용자 영향 불명확 | STEP2로 복귀해 [STEP4 직행 보강] 재작성 | [../step4/STEP4_REPORT.md](../step4/STEP4_REPORT.md) | PASS |
| F10 | 같은 원인으로 동일 STEP에 2회 이상 복귀 | STEP_ROLLBACK에 따라 작업 중단 후 사용자에게 상태·원인·선택지 보고 | [../../common/STEP_ROLLBACK.md](../../common/STEP_ROLLBACK.md) | PASS |

## 검증 결론

```text
결과: PASS
근거: 각 실패 상황이 가장 가까운 이전 STEP 또는 강제 중단 조건으로 연결된다.
잔여 리스크: 실제 에이전트 실행 중 복귀 횟수를 자동 집계하는 별도 도구는 없다. 현재는 문서 규칙 기반 수동 집계가 전제다.
```
