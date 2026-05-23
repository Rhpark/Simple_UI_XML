# STEP1. 출력 예시

STEP1_SCOPE.md의 산출물 형식을 실제 분석 상황별로 채운 예시 모음이다.

출력 예시 A — 단순 케이스 (분석 타입 명확, 이슈 없음):
    
```text
타입     : analysis — code-flow — 저장 버튼 클릭 후 DB 저장까지의 런타임 흐름 분석
단계     : STEP 1 (분석 범위 정의)
진행도   : (13/13) - 진행도 최종 갱신 (완료)

복원 매핑              : 없음                                                            → STEP2 진입점 보강
대상 기능/모듈         : 체크인 저장 기능                                               → STEP2 진입점
핵심 파일              : (주) CheckInViewModel.kt:142 — 저장 요청 진입점                     → STEP2 파일 열기 기준
                         RecordRepository.kt:38 — DB 기록 실행부
제외 범위/시나리오     : CalendarScreen.kt — 저장 로직과 호출 관계 없음, 제거해도 결론 불변  → STEP2 추적 경계
STEP3 적용 여부        : 필수 — code-flow 타입                                           → STEP3 진입 기준
입력 fixture/재현 조건 : 없음(해당 없음)
분석 환경              : 없음

이슈 : 없음
```

---

출력 예시 B — 혼합 타입 + 이슈 포함:

```text
타입     : analysis — 주: code-flow — 저장 버튼 클릭 후 흐름 추적 / 보조: ui-spec — 저장 실패 시 화면 표시 기준 확인
           보조 타입 관련 항목은 STEP2 리스크에 기록 후 STEP3에서 영향 범위 확인
단계     : STEP 1 (분석 범위 정의)
진행도   : (13/13) - 진행도 최종 갱신 (완료)

복원 매핑              : 없음                                                              → STEP2 진입점 보강
대상 기능/모듈         : 체크인 저장 기능                                                  → STEP2 진입점
핵심 파일              : (주) CheckInViewModel.kt:142 — 저장 요청 진입점                        → STEP2 파일 열기 기준
                         RecordRepository.kt:38 — DB 기록 실행부
제외 범위/시나리오     : CalendarScreen.kt — 저장 로직과 호출 관계 없음                    → STEP2 추적 경계
                         RecordDao.kt — Room 자동 생성 쿼리, 로직 변경 없음
STEP3 적용 여부        : 필수 — code-flow 주 타입                                          → STEP3 진입 기준
입력 fixture/재현 조건 : 없음(해당 없음)
분석 환경              : 없음

이슈 : RecordRepository가 인터페이스 없이 직접 주입된다고 판단 — DI 설정 파일 미확인
```

---

출력 예시 C — 문서 정합성 분석:

```text
타입     : analysis — doc-consistency — 태그 저장 모델과 UI 문서의 기준 일치 여부 분석
단계     : STEP 1 (분석 범위 정의)
진행도   : (13/13) - 진행도 최종 갱신 (완료)

복원 매핑              : 없음                                                              → STEP2 진입점 보강
대상 기능/모듈         : 체크인 원인 태그 저장 및 UI 선택                                   → STEP2 진입점
핵심 파일              : (주) RECORD_DATA_SPEC.md:2 — EmotionEntry causeTags 데이터 기준          → STEP2 파일 열기 기준
                         CHECKIN_UI_SPEC.md:2 — 원인 태그 UI 선택 기준
제외 범위/시나리오     : AI_REPORT_DATA_SPEC.md — 1단계 태그 저장 모델과 직접 관련 없음      → STEP2 추적 경계
STEP3 적용 여부        : 필수 — doc-consistency 타입                                         → STEP3 진입 기준
입력 fixture/재현 조건 : 없음(해당 없음)
분석 환경              : 없음

이슈 : 없음
```
