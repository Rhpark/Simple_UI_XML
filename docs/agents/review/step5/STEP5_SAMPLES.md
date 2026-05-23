# STEP5 산출물 예시

→ 산출물 형식·하네스 기준은 [STEP5_REPORT.md](STEP5_REPORT.md) 참조

---

## 예시 A — 하네스 통과 / finding 있음 / 다음 단계: feature + refactor

```text
현재 단계: 리뷰 - STEP5 최종 리뷰 보고

[리뷰 요약]
검증 대상      : CheckInViewModel.kt:142 / onSaveClick() — 감정 기록 저장 진입점
단계별 발견 수 : STEP1 1건 / STEP2 2건 / STEP3 2건 / STEP4 2건
한 줄 결론     : 저장 실패 처리 부재와 가변 상태 노출이 우선이며, 구현 보완 후 구조 리팩토링이 필요하다.
점수           : Agent 수행성 89/100, 사용자 이해성 88/100
감점 이유      :
  Agent 수행성 감점 — Android Lint 미실행으로 [코드 품질] 도구 실행 근거가 일부 부족함
  사용자 이해성 감점 — 비동기 누수와 예외 미전파의 사용자 영향이 저장 실패와 일부 겹쳐 원인 구분 설명이 더 필요함
확인 범위      : 확인 범위: CheckInViewModel/RecordRepository / 제외 범위: CalendarUtils/DI 모듈 / 미확인 범위: 없음
우선 행동      :
  1순위 — 저장 실패 시 에러 상태 emit 구현 (CheckInViewModel.kt:155)
  2순위 — _uiState private backing property 적용 (CheckInViewModel.kt:21)
  3순위 — viewModelScope 취소 시 insert 보호 (RecordRepository.kt:38)

[리뷰 보고]
발견 사항      :
  [HIGH] 저장 실패 무응답
  위치  : CheckInViewModel.kt:155
  근거  : viewModelScope.launch 내 catch 없이 emit 없음
  영향  : 사용자가 저장 실패를 인지 불가 — UI 무응답
  행동  : feature — catch 후 에러 상태 emit 구현

  [HIGH] 비동기 누수
  위치  : RecordRepository.kt:38
  근거  : viewModelScope 취소 시 insert 중단 — NonCancellable 컨텍스트 미사용
  영향  : 화면 이탈 시 저장 미완료 무응답
  행동  : feature — NonCancellable 컨텍스트 또는 SupervisorJob 적용

  [HIGH] 예외 미전파
  위치  : RecordRepository.kt:38
  근거  : try-catch 없음 — DB 예외가 상위로 전달되지 않음
  영향  : 저장 오류 시 원인 미전달 — 무응답과 동일 증상
  행동  : feature — try-catch 후 Result 또는 sealed class로 오류 전파

  [HIGH] 가변 상태 노출
  위치  : CheckInViewModel.kt:21
  근거  : _uiState MutableStateFlow internal 노출 — 외부 모듈이 직접 변경 가능
  영향  : 예측 불가 상태 변경
  행동  : refactor — private + backing property 적용

  [MEDIUM] SRP 위반
  위치  : CheckInViewModel.kt:142
  근거  : 저장+유효성 검사+UI 상태 처리 혼재
  영향  : 변경 시 영향 범위 과대 — 사용자 직접 영향 없음
  행동  : refactor (보조) — UseCase 분리

  [LOW] 미사용 import
  위치  : CheckInViewModel.kt:5
  근거  : import android.util.Log 미사용
  영향  : 없음
  행동  : 무시 가능 — LOW, 사용자 영향 없음

  [LOW] TODO 주석
  위치  : CheckInViewModel.kt:156
  근거  : // TODO: 에러 처리 — 미완성 주석
  영향  : 없음
  행동  : 무시 가능 — LOW, 사용자 영향 없음

중복 제거      : 없음
도구 실행      : Android Lint 실행 — 미사용 import 1건 외 경고 없음
다음 단계      : feature (주) + refactor (보조) — HIGH finding 4건 중 3건이 저장 실패/비동기 동일 원인, 구현 후 상태 노출 리팩토링
```

---

## 예시 B — 하네스 통과 / finding 없음 / 다음 단계: 리뷰 완료

```text
현재 단계: 리뷰 - STEP5 최종 리뷰 보고

[리뷰 요약]
검증 대상      : ProfileViewModel.kt:88 / onLoadProfile() — 프로필 조회 진입점
단계별 발견 수 : STEP1 0건 / STEP2 0건 / STEP3 0건 / STEP4 0건
한 줄 결론     : 전 단계에서 finding이 없으며 코드 품질과 아키텍처 모두 기준 범위 내다.
점수           : Agent 수행성 95/100, 사용자 이해성 94/100
감점 이유      :
  Agent 수행성 감점 — 컨벤션 기준 문서가 없어 컨벤션 항목 확인 불가로 처리됨
  사용자 이해성 감점 — 없음
확인 범위      : 확인 범위: ProfileViewModel/UserRepository / 제외 범위: 없음 / 미확인 범위: 없음
우선 행동      : 없음

[리뷰 보고]
발견 사항      : 없음

중복 제거      : 없음
도구 실행      : Android Lint 실행 — 경고 없음
다음 단계      : 리뷰 완료 — CRITICAL/HIGH finding 없음, 추가 작업 불필요
```

---

## 예시 C — CRITICAL finding 있음 / 별도 블록 보고

```text
현재 단계: 리뷰 - STEP5 최종 리뷰 보고

[리뷰 요약]
검증 대상      : CheckInViewModel.kt:142 / onSaveClick() — 감정 기록 저장 진입점
단계별 발견 수 : STEP1 1건 / STEP2 1건 / STEP3 1건 / STEP4 1건
한 줄 결론     : null 역참조 크래시로 저장 시도 시 앱이 강제 종료되며, 즉시 수정이 필요하다.
점수           : Agent 수행성 82/100, 사용자 이해성 85/100
감점 이유      :
  Agent 수행성 감점 — CRITICAL finding 발견으로 STEP2에서 즉시 중단됨 — STEP3/STEP4 확인 범위 축소
  사용자 이해성 감점 — 없음
확인 범위      : 확인 범위: CheckInViewModel/RecordRepository (STEP1~2) / 제외 범위: CalendarUtils / 미확인 범위: STEP3/STEP4 (CRITICAL 중단으로 미수행)
우선 행동      :
  1순위 — RecordRepository.kt:52 에서 !! 연산자 제거 후 null 분기 처리 (앱 크래시 방지)

[리뷰 보고]
발견 사항      :
  [CRITICAL] null 역참조 크래시
  위치  : RecordRepository.kt:52
  근거  : `emotionEntity!!` — getEmotion() 반환값 null 시 NullPointerException
  영향  : 저장 시도 시 앱 강제 종료 (데이터 손실)
  행동  : feature — !! 제거 후 null 반환 시 에러 상태 emit 처리

  [HIGH] 저장 실패 무응답
  위치  : CheckInViewModel.kt:155
  근거  : viewModelScope.launch 내 catch 없음
  영향  : 저장 실패 인지 불가
  행동  : feature — catch 후 에러 상태 emit

중복 제거      : 없음
도구 실행      : Android Lint 미실행 — CRITICAL finding 발견으로 STEP4 미수행
다음 단계      : feature (주) — CRITICAL finding 수정 후 review 재실행 권장

---
⚠️ CRITICAL finding — 즉시 조치 필요

[CRITICAL] null 역참조 크래시
위치  : RecordRepository.kt:52
근거  : `emotionEntity!!` — getEmotion() null 반환 시 NullPointerException 발생
영향  : 저장 시도 시 앱 강제 종료 (데이터 손실 포함)
조치  : RecordRepository.kt:52 에서 !! 연산자 제거 후 null 분기 처리
       수정 완료 후 review 재실행 권장
```
